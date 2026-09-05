package com.example.data.cloud

/**
 * State of Cloud Data Synchronization between Room local database and Firestore backend.
 */
sealed interface CloudSyncStatus {
    object Idle : CloudSyncStatus
    data class Syncing(val stageDescription: String) : CloudSyncStatus
    data class Success(
        val timestamp: Long = System.currentTimeMillis(),
        val recordsSynced: Int = 0,
        val message: String = "क्लाउड सिंक सफल (Cloud sync successful)"
    ) : CloudSyncStatus
    data class Error(
        val message: String,
        val isOffline: Boolean = false,
        val timestamp: Long = System.currentTimeMillis()
    ) : CloudSyncStatus
}

/**
 * Firebase Cloud Authentication state representation.
 */
sealed interface CloudAuthState {
    object Unauthenticated : CloudAuthState
    data class Authenticating(val progressMessage: String = "सत्यापन जारी...") : CloudAuthState
    data class Authenticated(
        val uid: String,
        val email: String?,
        val displayName: String?,
        val role: String, // "STUDENT" or "ADMIN"
        val studentId: String? = null,
        val isEmailVerified: Boolean = false
    ) : CloudAuthState
    data class AuthError(
        val message: String,
        val errorCode: String? = null
    ) : CloudAuthState
}

/**
 * Cloud user profile model stored in Firestore `users/{uid}`.
 */
data class CloudUserProfile(
    val uid: String = "",
    val email: String = "",
    val displayName: String = "",
    val phoneNumber: String = "",
    val role: String = FirestoreConstants.ROLE_STUDENT,
    val linkedStudentId: String = "",
    val coachId: String = "",
    val name: String = "",
    val achievement: String = "",
    val active: Boolean = true,
    val forcePasswordChange: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val lastLoginAt: Long = System.currentTimeMillis(),
    val isActive: Boolean = true
) {
    fun toMap(): Map<String, Any?> {
        val map = mutableMapOf<String, Any?>(
            "uid" to uid,
            "email" to email,
            "displayName" to displayName,
            "phoneNumber" to phoneNumber,
            "role" to role,
            "linkedStudentId" to linkedStudentId,
            "createdAt" to createdAt,
            "lastLoginAt" to lastLoginAt,
            "isActive" to (isActive && active),
            "active" to (active && isActive),
            "forcePasswordChange" to forcePasswordChange
        )
        if (coachId.isNotBlank()) {
            map["coachId"] = coachId
        }
        if (name.isNotBlank() || displayName.isNotBlank()) {
            map["name"] = if (name.isNotBlank()) name else displayName
        }
        if (achievement.isNotBlank()) {
            map["achievement"] = achievement
        }
        return map
    }

    companion object {
        fun fromMap(map: Map<String, Any?>): CloudUserProfile {
            val nameVal = map["name"] as? String ?: ""
            val dispName = map["displayName"] as? String ?: nameVal
            val activeVal = (map["active"] as? Boolean) ?: (map["isActive"] as? Boolean) ?: true
            return CloudUserProfile(
                uid = map["uid"] as? String ?: "",
                email = map["email"] as? String ?: "",
                displayName = dispName,
                name = if (nameVal.isNotBlank()) nameVal else dispName,
                phoneNumber = map["phoneNumber"] as? String ?: "",
                role = map["role"] as? String ?: FirestoreConstants.ROLE_STUDENT,
                linkedStudentId = map["linkedStudentId"] as? String ?: "",
                coachId = map["coachId"] as? String ?: "",
                achievement = map["achievement"] as? String ?: "",
                active = activeVal,
                forcePasswordChange = map["forcePasswordChange"] as? Boolean ?: false,
                createdAt = (map["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                lastLoginAt = (map["lastLoginAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                isActive = activeVal
            )
        }
    }
}
