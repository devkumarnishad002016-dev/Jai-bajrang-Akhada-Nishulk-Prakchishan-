package com.example.data.cloud

import android.app.Activity
import android.util.Log
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

/**
 * Cloud Authentication Manager handles Firebase Auth, role resolution, and credentials.
 * Designed with safe fallback mechanisms for offline use or local development without cloud config.
 */
class CloudAuthManager(
    private val auth: FirebaseAuth? = runCatching { FirebaseAuth.getInstance() }.getOrNull(),
    private val firestore: FirebaseFirestore? = runCatching { FirebaseFirestore.getInstance() }.getOrNull()
) {
    private val TAG = "CloudAuthManager"

    private val _authState = MutableStateFlow<CloudAuthState>(CloudAuthState.Unauthenticated)
    val authState: StateFlow<CloudAuthState> = _authState.asStateFlow()

    init {
        checkCurrentAuthState()
    }

    /**
     * Checks if a Firebase user is already logged in on device.
     */
    fun checkCurrentAuthState() {
        val currentUser = auth?.currentUser
        if (currentUser != null) {
            _authState.value = CloudAuthState.Authenticated(
                uid = currentUser.uid,
                email = currentUser.email,
                displayName = currentUser.displayName ?: currentUser.email?.substringBefore("@"),
                role = FirestoreConstants.ROLE_STUDENT,
                studentId = null,
                isEmailVerified = currentUser.isEmailVerified
            )
            // Asynchronously resolve role and linked student ID from Firestore
            resolveUserCloudProfile(currentUser.uid)
        } else {
            _authState.value = CloudAuthState.Unauthenticated
        }
    }

    /**
     * Sign in with Email and Password.
     */
    suspend fun signInWithEmail(email: String, pinOrPass: String): Result<CloudAuthState.Authenticated> = withContext(Dispatchers.IO) {
        val authInstance = auth ?: return@withContext Result.failure(
            IllegalStateException("Firebase Auth उपलब्ध नहीं है (Firebase Auth is not initialized)")
        )
        try {
            _authState.value = CloudAuthState.Authenticating("लॉगिन किया जा रहा है...")
            val result = authInstance.signInWithEmailAndPassword(email.trim(), pinOrPass).await()
            val user = result.user ?: throw IllegalStateException("User authentication returned empty")
            
            val profile = fetchOrCreateUserProfile(user)
            val authResult = CloudAuthState.Authenticated(
                uid = user.uid,
                email = user.email,
                displayName = user.displayName ?: profile.displayName.ifEmpty { user.email?.substringBefore("@") },
                role = profile.role,
                studentId = profile.linkedStudentId.ifEmpty { null },
                isEmailVerified = user.isEmailVerified
            )
            _authState.value = authResult
            Result.success(authResult)
        } catch (e: Exception) {
            Log.e(TAG, "signInWithEmail error: ${e.message}", e)
            val errorMsg = formatAuthErrorMessage(e)
            _authState.value = CloudAuthState.AuthError(errorMsg)
            Result.failure(Exception(errorMsg, e))
        }
    }

    /**
     * Registers a new user with Firebase Authentication and writes their initial profile 
     * to the 'users/{uid}' Firestore collection, defaulting to the 'STUDENT' role.
     */
    suspend fun registerUser(
        email: String,
        password: String,
        fullName: String = "",
        role: String = FirestoreConstants.ROLE_STUDENT,
        studentId: String = ""
    ): Result<CloudAuthState.Authenticated> = withContext(Dispatchers.IO) {
        val authInstance = auth ?: return@withContext Result.failure(
            IllegalStateException("Firebase Auth उपलब्ध नहीं है (Firebase Auth is not initialized)")
        )
        try {
            _authState.value = CloudAuthState.Authenticating("नया खाता बनाया जा रहा है...")
            val result = authInstance.createUserWithEmailAndPassword(email.trim(), password).await()
            val user = result.user ?: throw IllegalStateException("User creation returned null FirebaseUser")

            val effectiveDisplayName = fullName.trim().ifEmpty { 
                user.email?.substringBefore("@") ?: "Student" 
            }
            val effectiveRole = role.trim().ifEmpty { FirestoreConstants.ROLE_STUDENT }

            // Initialize Firestore user document under 'users/{uid}'
            val initialUserData = hashMapOf<String, Any?>(
                FirestoreConstants.FIELD_UID to user.uid,
                "email" to (user.email ?: email.trim()),
                "displayName" to effectiveDisplayName,
                "phoneNumber" to (user.phoneNumber ?: ""),
                FirestoreConstants.FIELD_ROLE to effectiveRole,
                "linkedStudentId" to studentId.trim(),
                FirestoreConstants.FIELD_CREATED_AT to System.currentTimeMillis(),
                "lastLoginAt" to System.currentTimeMillis(),
                FirestoreConstants.FIELD_IS_ACTIVE to true
            )

            // Write user document to Firestore 'users/{uid}'
            firestore?.collection(FirestoreConstants.COLLECTION_USERS)
                ?.document(user.uid)
                ?.set(initialUserData)
                ?.await()

            val authResult = CloudAuthState.Authenticated(
                uid = user.uid,
                email = user.email ?: email.trim(),
                displayName = effectiveDisplayName,
                role = effectiveRole,
                studentId = studentId.trim().ifEmpty { null },
                isEmailVerified = user.isEmailVerified
            )
            _authState.value = authResult
            Result.success(authResult)
        } catch (e: Exception) {
            Log.e(TAG, "registerUser error: ${e.message}", e)
            val errorMsg = formatAuthErrorMessage(e)
            _authState.value = CloudAuthState.AuthError(errorMsg)
            Result.failure(Exception(errorMsg, e))
        }
    }

    /**
     * Fetch user profile from Firestore or create initial fallback profile.
     */
    private suspend fun fetchOrCreateUserProfile(user: FirebaseUser): CloudUserProfile {
        val db = firestore ?: return CloudUserProfile(uid = user.uid, email = user.email ?: "")
        return try {
            val doc = db.collection(FirestoreConstants.COLLECTION_USERS).document(user.uid).get().await()
            if (doc.exists() && doc.data != null) {
                CloudUserProfile.fromMap(doc.data!!)
            } else {
                val initial = CloudUserProfile(
                    uid = user.uid,
                    email = user.email ?: "",
                    displayName = user.displayName ?: user.email?.substringBefore("@") ?: "",
                    role = FirestoreConstants.ROLE_STUDENT
                )
                db.collection(FirestoreConstants.COLLECTION_USERS).document(user.uid).set(initial.toMap()).await()
                initial
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to fetch user profile, using fallback", e)
            CloudUserProfile(uid = user.uid, email = user.email ?: "")
        }
    }

    /**
     * Resolve user profile in background and update StateFlow.
     */
    private fun resolveUserCloudProfile(uid: String) {
        val db = firestore ?: return
        db.collection(FirestoreConstants.COLLECTION_USERS).document(uid).get()
            .addOnSuccessListener { doc ->
                if (doc.exists() && doc.data != null) {
                    val profile = CloudUserProfile.fromMap(doc.data!!)
                    val current = _authState.value
                    if (current is CloudAuthState.Authenticated) {
                        _authState.value = current.copy(
                            role = profile.role,
                            studentId = profile.linkedStudentId.ifEmpty { current.studentId },
                            displayName = profile.displayName.ifEmpty { current.displayName }
                        )
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "resolveUserCloudProfile warning: ${e.message}")
            }
    }

    /**
     * Sends password reset email using FirebaseAuth.
     */
    suspend fun sendPasswordResetEmail(email: String): Result<Unit> = withContext(Dispatchers.IO) {
        val trimmedEmail = email.trim()
        if (trimmedEmail.isEmpty()) {
            return@withContext Result.failure(IllegalArgumentException("कृपया अपना पंजीकृत ईमेल पता दर्ज करें।"))
        }
        val authInstance = auth ?: return@withContext Result.failure(
            IllegalStateException("Firebase Auth सेवा उपलब्ध नहीं है (Firebase Auth is not initialized)")
        )
        try {
            authInstance.sendPasswordResetEmail(trimmedEmail).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "sendPasswordResetEmail error: ${e.message}", e)
            val errorMsg = formatAuthErrorMessage(e)
            Result.failure(Exception(errorMsg, e))
        }
    }

    /**
     * Sends OTP to phone number using Firebase Phone Authentication.
     * Real SMS OTP is dispatched via Firebase Phone Auth infrastructure.
     * NEVER uses fake/simulated OTPs.
     */
    fun sendPhoneVerificationCode(
        activity: Activity,
        rawPhoneNumber: String,
        onCodeSent: (verificationId: String) -> Unit,
        onAutoVerified: (authResult: CloudAuthState.Authenticated) -> Unit,
        onVerificationFailed: (errorMessage: String) -> Unit
    ) {
        val authInstance = auth
        if (authInstance == null) {
            onVerificationFailed("Firebase Auth सेवा उपलब्ध नहीं है (Firebase Auth is not initialized or missing google-services.json)")
            return
        }

        val cleaned = rawPhoneNumber.trim().replace(" ", "").replace("-", "")
        val formattedNumber = if (cleaned.startsWith("+")) cleaned else if (cleaned.length == 10) "+91$cleaned" else "+$cleaned"

        if (formattedNumber.length < 10) {
            onVerificationFailed("कृपया एक मान्य 10 अंकों का मोबाइल नंबर दर्ज करें (Invalid phone number)")
            return
        }

        _authState.value = CloudAuthState.Authenticating("मोबाइल नंबर पर OTP भेजा जा रहा है...")

        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                // Auto-retrieval or instant verification
                kotlinx.coroutines.CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val result = authInstance.signInWithCredential(credential).await()
                        val user = result.user ?: return@launch
                        val profile = fetchOrCreateUserProfile(user)
                        val authResult = CloudAuthState.Authenticated(
                            uid = user.uid,
                            email = user.email ?: user.phoneNumber,
                            displayName = user.displayName ?: profile.displayName.ifEmpty { user.phoneNumber ?: "Cadet" },
                            role = profile.role,
                            studentId = profile.linkedStudentId.ifEmpty { null },
                            isEmailVerified = true
                        )
                        _authState.value = authResult
                        withContext(Dispatchers.Main) {
                            onAutoVerified(authResult)
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Auto-verification sign in error", e)
                        val errorMsg = formatAuthErrorMessage(e)
                        _authState.value = CloudAuthState.AuthError(errorMsg)
                        withContext(Dispatchers.Main) {
                            onVerificationFailed(errorMsg)
                        }
                    }
                }
            }

            override fun onVerificationFailed(e: FirebaseException) {
                Log.e(TAG, "PhoneAuthProvider verification failed: ${e.message}", e)
                val errorMsg = formatAuthErrorMessage(e)
                _authState.value = CloudAuthState.AuthError(errorMsg)
                onVerificationFailed(errorMsg)
            }

            override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                Log.d(TAG, "Phone OTP sent successfully, verificationId generated")
                _authState.value = CloudAuthState.Unauthenticated
                onCodeSent(verificationId)
            }
        }

        val options = PhoneAuthOptions.newBuilder(authInstance)
            .setPhoneNumber(formattedNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    /**
     * Signs in with the OTP SMS code and verification ID received from Firebase Phone Auth.
     * Note: Neither OTP nor verification ID is ever stored in Room or Firestore.
     */
    suspend fun signInWithPhoneOtp(
        verificationId: String,
        otpCode: String,
        fullName: String = ""
    ): Result<CloudAuthState.Authenticated> = withContext(Dispatchers.IO) {
        val authInstance = auth ?: return@withContext Result.failure(
            IllegalStateException("Firebase Auth सेवा उपलब्ध नहीं है (Firebase Auth is not initialized)")
        )

        val trimmedCode = otpCode.trim()
        if (trimmedCode.length < 6) {
            return@withContext Result.failure(IllegalArgumentException("कृपया 6 अंकों का सही OTP कोड दर्ज करें।"))
        }

        try {
            _authState.value = CloudAuthState.Authenticating("OTP सत्यापित किया जा रहा है...")
            val credential = PhoneAuthProvider.getCredential(verificationId, trimmedCode)
            val result = authInstance.signInWithCredential(credential).await()
            val user = result.user ?: throw IllegalStateException("User authentication returned null FirebaseUser")

            val profile = fetchOrCreateUserProfile(user)
            val effectiveName = if (fullName.isNotBlank()) fullName.trim() else user.displayName ?: profile.displayName.ifEmpty { user.phoneNumber ?: "Cadet" }

            val authResult = CloudAuthState.Authenticated(
                uid = user.uid,
                email = user.email ?: user.phoneNumber,
                displayName = effectiveName,
                role = profile.role,
                studentId = profile.linkedStudentId.ifEmpty { null },
                isEmailVerified = true
            )
            _authState.value = authResult
            Result.success(authResult)
        } catch (e: Exception) {
            Log.e(TAG, "signInWithPhoneOtp error: ${e.message}", e)
            val errorMsg = formatAuthErrorMessage(e)
            _authState.value = CloudAuthState.AuthError(errorMsg)
            Result.failure(Exception(errorMsg, e))
        }
    }

    /**
     * Sign out current user.
     */
    fun signOut() {
        try {
            auth?.signOut()
        } catch (e: Exception) {
            Log.e(TAG, "signOut error", e)
        }
        _authState.value = CloudAuthState.Unauthenticated
    }

    fun isFirebaseConfigured(): Boolean {
        return auth != null && firestore != null
    }

    /**
     * Syncs Coach profile to Firestore matching requested structure:
     * users/{uid} { role: "TRAINER", coachId, name, achievement, active, forcePasswordChange, createdAt }
     */
    suspend fun syncCoachToFirestore(
        coachId: String,
        name: String,
        achievement: String,
        active: Boolean = true,
        forcePasswordChange: Boolean = true
    ): Result<String> = withContext(Dispatchers.IO) {
        val db = firestore ?: return@withContext Result.failure(IllegalStateException("Firestore is not available"))
        try {
            val query = db.collection(FirestoreConstants.COLLECTION_USERS)
                .whereEqualTo("coachId", coachId.trim())
                .limit(1)
                .get()
                .await()

            if (!query.isEmpty) {
                val existingDoc = query.documents.first()
                val docId = existingDoc.id
                val updates = hashMapOf<String, Any?>(
                    "role" to FirestoreConstants.ROLE_TRAINER,
                    "name" to name.trim(),
                    "achievement" to achievement.trim(),
                    "active" to active,
                    "coachId" to coachId.trim()
                )
                db.collection(FirestoreConstants.COLLECTION_USERS).document(docId).update(updates).await()
                return@withContext Result.success(docId)
            }

            val deterministicDocId = "coach_${coachId.trim().lowercase().replace("-", "_")}"
            val coachData = hashMapOf<String, Any?>(
                "uid" to deterministicDocId,
                "role" to FirestoreConstants.ROLE_TRAINER,
                "coachId" to coachId.trim(),
                "name" to name.trim(),
                "displayName" to name.trim(),
                "achievement" to achievement.trim(),
                "active" to active,
                "isActive" to active,
                "forcePasswordChange" to forcePasswordChange,
                "createdAt" to com.google.firebase.firestore.FieldValue.serverTimestamp(),
                "lastLoginAt" to System.currentTimeMillis()
            )

            db.collection(FirestoreConstants.COLLECTION_USERS)
                .document(deterministicDocId)
                .set(coachData)
                .await()

            Result.success(deterministicDocId)
        } catch (e: Exception) {
            Log.e(TAG, "syncCoachToFirestore error: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Updates forcePasswordChange flag in Firestore when coach updates their password.
     */
    suspend fun updateCoachPasswordChangedInFirestore(coachId: String): Result<Unit> = withContext(Dispatchers.IO) {
        val db = firestore ?: return@withContext Result.success(Unit)
        try {
            val query = db.collection(FirestoreConstants.COLLECTION_USERS)
                .whereEqualTo("coachId", coachId.trim())
                .limit(1)
                .get()
                .await()

            if (!query.isEmpty) {
                val doc = query.documents.first()
                doc.reference.update(
                    mapOf(
                        "forcePasswordChange" to false,
                        "lastPasswordChangeAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                    )
                ).await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.w(TAG, "updateCoachPasswordChangedInFirestore warning: ${e.message}")
            Result.success(Unit)
        }
    }

    fun getCurrentUser(): FirebaseUser? = auth?.currentUser

    private fun formatAuthErrorMessage(e: Exception): String {
        val msg = e.message.orEmpty()
        return when {
            msg.contains("user-not-found", true) || msg.contains("invalid-credential", true) ->
                "गलत ईमेल या पासवर्ड। कृपया पुनः प्रयास करें।"
            msg.contains("wrong-password", true) ->
                "पासवर्ड गलत है।"
            msg.contains("email-already-in-use", true) ->
                "यह ईमेल पहले से पंजीकृत है।"
            msg.contains("network", true) ->
                "नेटवर्क त्रुटि: कृपया इंटरनेट कनेक्शन जांचें।"
            else -> "प्रमाणीकरण त्रुटि: ${e.localizedMessage ?: "अज्ञात त्रुटि"}"
        }
    }
}
