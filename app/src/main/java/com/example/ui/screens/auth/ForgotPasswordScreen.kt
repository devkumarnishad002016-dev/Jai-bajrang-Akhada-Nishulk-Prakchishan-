package com.example.ui.screens.auth

import android.util.Patterns
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * ForgotPasswordScreen allows users and administrators to recover their account
 * credentials by triggering a password reset link to their registered email address
 * via Firebase Authentication (FirebaseAuth.sendPasswordResetEmail).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    initialEmail: String = ""
) {
    val coroutineScope = rememberCoroutineScope()
    var emailInput by remember { mutableStateOf(initialEmail) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSuccess by remember { mutableStateOf(false) }
    var successEmail by remember { mutableStateOf("") }

    fun sendPasswordReset(email: String) {
        val trimmed = email.trim()
        if (trimmed.isEmpty()) {
            errorMessage = "कृपया अपना पंजीकृत ईमेल पता दर्ज करें।"
            return
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(trimmed).matches()) {
            errorMessage = "कृपया एक मान्य ईमेल पता दर्ज करें (उदा. student@gmail.com)।"
            return
        }

        errorMessage = null
        isLoading = true
        coroutineScope.launch {
            try {
                val auth = runCatching { FirebaseAuth.getInstance() }.getOrNull()
                if (auth == null) {
                    throw IllegalStateException("Firebase Auth सेवा उपलब्ध नहीं है।")
                }
                withContext(Dispatchers.IO) {
                    auth.sendPasswordResetEmail(trimmed).await()
                }
                isSuccess = true
                successEmail = trimmed
                isLoading = false
            } catch (e: Exception) {
                isLoading = false
                val msg = e.message.orEmpty()
                errorMessage = when {
                    msg.contains("user-not-found", true) ->
                        "इस ईमेल पते ($trimmed) से कोई पंजीकृत खाता नहीं मिला।"
                    msg.contains("invalid-email", true) ->
                        "ईमेल पता अमान्य प्रारूप में है।"
                    msg.contains("network", true) ->
                        "नेटवर्क त्रुटि: कृपया अपना इंटरनेट कनेक्शन जांचें।"
                    else ->
                        "रीसेट लिंक भेजने में समस्या आई: ${e.localizedMessage ?: "कृपया पुनः प्रयास करें।"}"
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "पासवर्ड रिकवरी (Password Reset)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("back_to_login_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "वापस जाएँ (Back)"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 16.dp)
                    .testTag("forgot_password_screen"),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                // Header Icon Banner
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(SaffronPrimary, SaffronDark)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isSuccess) Icons.Default.MarkEmailRead else Icons.Default.LockReset,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(42.dp)
                    )
                }

                // Title & Description
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = if (isSuccess) "रीसेट लिंक भेजा गया!" else "पासवर्ड भूल गए?",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = if (isSuccess) {
                            "पासवर्ड रीसेट लिंक आपके ईमेल ($successEmail) पर भेज दिया गया है।"
                        } else {
                            "अपना पंजीकृत ईमेल दर्ज करें। हम आपको पासवर्ड रीसेट करने के लिए एक सुरक्षित लिंक भेजेंगे।"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }

                // Error Banner
                AnimatedVisibility(
                    visible = errorMessage != null,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.ErrorOutline,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = errorMessage.orEmpty(),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                // Success Card
                if (isSuccess) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = OliveContainer.copy(alpha = 0.6f)
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("forgot_password_success_card")
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = OliveTertiary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "ईमेल सफलतापूर्वक भेजा गया",
                                    fontWeight = FontWeight.Bold,
                                    color = OnOliveContainer,
                                    style = MaterialTheme.typography.titleSmall
                                )
                            }

                            Text(
                                text = "1. अपना ईमेल इनबॉक्स खोलें।\n2. 'Firebase / Jai Bajrang Akhada' से आए रीसेट लिंक पर क्लिक करें।\n3. नया सुरक्षित पासवर्ड सेट करें और वापस आकर लॉगिन करें।\n\n📌 नोट: यदि इनबॉक्स में ईमेल न मिले तो स्पैम (Spam/Junk) फ़ोल्डर अवश्य जांचें।",
                                style = MaterialTheme.typography.bodyMedium,
                                color = OnOliveContainer.copy(alpha = 0.9f),
                                lineHeight = 20.sp
                            )

                            HorizontalDivider(
                                color = OliveTertiary.copy(alpha = 0.3f),
                                modifier = Modifier.padding(vertical = 4.dp)
                            )

                            Button(
                                onClick = onNavigateBack,
                                colors = ButtonDefaults.buttonColors(containerColor = OliveTertiary),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("success_back_to_login_button")
                            ) {
                                Icon(Icons.Default.Login, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("लॉगिन स्क्रीन पर वापस जाएँ", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } else {
                    // Input Card
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            OutlinedTextField(
                                value = emailInput,
                                onValueChange = {
                                    emailInput = it
                                    errorMessage = null
                                },
                                label = { Text("पंजीकृत ईमेल आईडी (Email Address) *") },
                                placeholder = { Text("उदाहरण: cadet@example.com") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Email,
                                        contentDescription = null,
                                        tint = SaffronPrimary
                                    )
                                },
                                trailingIcon = {
                                    if (emailInput.isNotEmpty()) {
                                        IconButton(onClick = { emailInput = "" }) {
                                            Icon(Icons.Default.Clear, contentDescription = "Clear text")
                                        }
                                    }
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Email,
                                    imeAction = ImeAction.Send
                                ),
                                keyboardActions = KeyboardActions(
                                    onSend = {
                                        if (!isLoading) {
                                            sendPasswordReset(emailInput)
                                        }
                                    }
                                ),
                                enabled = !isLoading,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("forgot_email_input"),
                                shape = RoundedCornerShape(12.dp)
                            )

                            Button(
                                onClick = { sendPasswordReset(emailInput) },
                                enabled = !isLoading && emailInput.isNotBlank(),
                                colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .testTag("send_reset_link_button")
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(
                                        color = Color.White,
                                        modifier = Modifier.size(22.dp),
                                        strokeWidth = 2.5.dp
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text("भेजा जा रहा है...", fontWeight = FontWeight.Bold)
                                } else {
                                    Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("पासवर्ड रीसेट लिंक भेजें", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                }
                            }
                        }
                    }

                    // Helpful Advice / Offline Assistance Card
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = SaffronPrimary,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "क्या आपका ईमेल पंजीकृत नहीं है?",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "यदि आप अपनी छात्र आईडी या पासवर्ड भूल गए हैं, तो कृपया ग्राउंड पर मुख्य कोच/संचालक से संपर्क करें।",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    OutlinedButton(
                        onClick = onNavigateBack,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("back_to_login_alt_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("लॉगिन स्क्रीन पर वापस जाएँ", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

/**
 * Compact Dialog variant for embedding inside existing LoginScreen flows.
 */
@Composable
fun ForgotPasswordDialog(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    initialEmail: String = ""
) {
    val coroutineScope = rememberCoroutineScope()
    var emailInput by remember { mutableStateOf(initialEmail) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSuccess by remember { mutableStateOf(false) }

    fun sendPasswordReset(email: String) {
        val trimmed = email.trim()
        if (trimmed.isEmpty()) {
            errorMessage = "कृपया ईमेल पता दर्ज करें।"
            return
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(trimmed).matches()) {
            errorMessage = "कृपया मान्य ईमेल दर्ज करें।"
            return
        }

        errorMessage = null
        isLoading = true
        coroutineScope.launch {
            try {
                val auth = runCatching { FirebaseAuth.getInstance() }.getOrNull()
                if (auth == null) {
                    throw IllegalStateException("Firebase Auth उपलब्ध नहीं है।")
                }
                withContext(Dispatchers.IO) {
                    auth.sendPasswordResetEmail(trimmed).await()
                }
                isSuccess = true
                isLoading = false
            } catch (e: Exception) {
                isLoading = false
                val msg = e.message.orEmpty()
                errorMessage = when {
                    msg.contains("user-not-found", true) -> "यह ईमेल पंजीकृत नहीं है।"
                    msg.contains("network", true) -> "नेटवर्क त्रुटि: इंटरनेट जांचें।"
                    else -> e.localizedMessage ?: "त्रुटि आई।"
                }
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LockReset,
                    contentDescription = null,
                    tint = SaffronPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "पासवर्ड रीसेट (Forgot Password)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("forgot_password_dialog"),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (isSuccess) {
                    Surface(
                        color = OliveContainer.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                "✅ रीसेट लिंक भेज दिया गया है!",
                                fontWeight = FontWeight.Bold,
                                color = OnOliveContainer,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "कृपया अपने ईमेल (${emailInput.trim()}) का इनबॉक्स और स्पैम फ़ोल्डर चेक करें।",
                                style = MaterialTheme.typography.bodySmall,
                                color = OnOliveContainer
                            )
                        }
                    }
                } else {
                    Text(
                        "अपना पंजीकृत ईमेल दर्ज करें। आपको पासवर्ड रीसेट करने का लिंक भेजा जाएगा:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (errorMessage != null) {
                        Surface(
                            color = MaterialTheme.colorScheme.errorContainer,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = errorMessage.orEmpty(),
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }

                    OutlinedTextField(
                        value = emailInput,
                        onValueChange = {
                            emailInput = it
                            errorMessage = null
                        },
                        label = { Text("ईमेल पता (Email)") },
                        placeholder = { Text("cadet@example.com") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = SaffronPrimary) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(onSend = { if (!isLoading) sendPasswordReset(emailInput) }),
                        enabled = !isLoading,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dialog_email_input")
                    )
                }
            }
        },
        confirmButton = {
            if (isSuccess) {
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = OliveTertiary),
                    modifier = Modifier.testTag("dialog_done_button")
                ) {
                    Text("ठीक है (Done)")
                }
            } else {
                Button(
                    onClick = { sendPasswordReset(emailInput) },
                    enabled = !isLoading && emailInput.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary),
                    modifier = Modifier.testTag("dialog_send_reset_button")
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Text("लिंक भेजें")
                }
            }
        },
        dismissButton = {
            if (!isSuccess) {
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("dialog_cancel_button")
                ) {
                    Text("रद्द करें")
                }
            }
        },
        modifier = modifier
    )
}
