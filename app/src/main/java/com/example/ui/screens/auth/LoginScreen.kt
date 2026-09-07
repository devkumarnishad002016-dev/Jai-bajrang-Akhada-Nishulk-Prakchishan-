package com.example.ui.screens.auth

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.StudentProfile
import com.example.ui.theme.*
import com.example.util.AdminSecurityManager
import com.example.util.ProfileUtils
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    allStudents: List<StudentProfile>,
    onStudentLogin: (String, String) -> Boolean,
    onAdminLogin: (String) -> Boolean,
    onAdminLoginWithCredentials: ((String, String) -> Boolean)? = null,
    onRegisterStudent: ((StudentProfile) -> Unit)? = null,
    isAdminPinConfigured: Boolean = true,
    onSetupInitialAdminPin: ((String) -> Result<Unit>)? = null,
    onTrainerLogin: ((String) -> Boolean)? = null,
    onTrainerLoginWithCredentials: ((String, String) -> Boolean)? = null,
    onSendPhoneOtp: ((activity: android.app.Activity, phone: String, onCodeSent: (String) -> Unit, onAutoVerified: () -> Unit, onError: (String) -> Unit) -> Unit)? = null,
    onVerifyPhoneOtp: ((verificationId: String, otp: String, fullName: String, onSuccess: () -> Unit, onError: (String) -> Unit) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Student, 1 = Coach, 2 = Admin
    var studentLoginMode by remember { mutableIntStateOf(0) } // 0 = Student ID, 1 = Phone OTP
    var studentIdInput by remember { mutableStateOf("") }
    var studentPasswordInput by remember { mutableStateOf("") }
    var showStudentPassword by remember { mutableStateOf(false) }
    var coachIdInput by remember { mutableStateOf("") }
    var coachPasswordInput by remember { mutableStateOf("") }
    var showCoachPassword by remember { mutableStateOf(false) }
    var phoneNumberInput by remember { mutableStateOf("") }
    var phoneOtpInput by remember { mutableStateOf("") }
    var phoneVerificationId by remember { mutableStateOf<String?>(null) }
    var isOtpSent by remember { mutableStateOf(false) }
    var isSendingOtp by remember { mutableStateOf(false) }
    var adminPinInput by remember { mutableStateOf("") }
    var adminIdInput by remember { mutableStateOf("") }
    var adminPasswordInput by remember { mutableStateOf("") }
    var setupNewPinInput by remember { mutableStateOf("") }
    var setupConfirmPinInput by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var showPassword by remember { mutableStateOf(false) }
    var showRegistrationDialog by remember { mutableStateOf(false) }
    var showForgotPasswordScreen by remember { mutableStateOf(false) }

    if (showForgotPasswordScreen) {
        ForgotPasswordScreen(
            onNavigateBack = { showForgotPasswordScreen = false }
        )
        return
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("login_screen"),
        contentPadding = PaddingValues(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Branding Header Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Akhada Shield Emblem
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(CircleShape)
                            .border(3.dp, SaffronPrimary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.app_icon_emblem),
                            contentDescription = "जय बजरंग अखाड़ा मौरीकला Emblem",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "जय बजरंग अखाड़ा",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = "स्वस्थ युवा – सशक्त राष्ट्र",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = SaffronPrimary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "गाँव से सेना–पुलिस भर्ती अभियान • मौरिकला गुफा",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // 2. Role Selector Tabs
        item {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                contentColor = SaffronPrimary,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = {
                        selectedTab = 0
                        errorMessage = null
                    },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "छात्र (Cadet)",
                                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    },
                    modifier = Modifier.testTag("tab_student_login")
                )

                Tab(
                    selected = selectedTab == 1,
                    onClick = {
                        selectedTab = 1
                        errorMessage = null
                    },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.SportsScore,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "कोच (Coach)",
                                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    },
                    modifier = Modifier.testTag("tab_coach_login")
                )

                Tab(
                    selected = selectedTab == 2,
                    onClick = {
                        selectedTab = 2
                        errorMessage = null
                    },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AdminPanelSettings,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "एडमिन (Admin)",
                                fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    },
                    modifier = Modifier.testTag("tab_admin_login")
                )
            }
        }

        // 3. Error Banner if any
        if (errorMessage != null) {
            item {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = errorMessage ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // 4. Tab 0: Student Login
        if (selectedTab == 0) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // --- STUDENT ID & PASSWORD LOGIN ---
                        Text(
                            text = "छात्र आईडी / मोबाइल द्वारा सुरक्षित लॉगिन (Student Login)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )

                        OutlinedTextField(
                            value = studentIdInput,
                            onValueChange = {
                                studentIdInput = it
                                errorMessage = null
                            },
                            label = { Text("पंजीयन संख्या / मोबाइल (Student ID / Mobile) *") },
                            placeholder = { Text("उदा. JBA-2026-001 या 9826100001") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Badge,
                                    contentDescription = null,
                                    tint = SaffronPrimary
                                )
                            },
                            trailingIcon = {
                                if (studentIdInput.isNotEmpty()) {
                                    IconButton(onClick = { studentIdInput = "" }) {
                                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                                    }
                                }
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Next
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("student_id_input"),
                            shape = RoundedCornerShape(12.dp)
                        )

                        OutlinedTextField(
                            value = studentPasswordInput,
                            onValueChange = {
                                studentPasswordInput = it
                                errorMessage = null
                            },
                            label = { Text("छात्र पासवर्ड (Password) *") },
                            placeholder = { Text("अपना गोपनीय पासवर्ड दर्ज करें") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = SaffronPrimary
                                )
                            },
                            trailingIcon = {
                                IconButton(onClick = { showStudentPassword = !showStudentPassword }) {
                                    Icon(
                                        imageVector = if (showStudentPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = "Toggle password visibility"
                                    )
                                }
                            },
                            visualTransformation = if (showStudentPassword) VisualTransformation.None else PasswordVisualTransformation(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    if (studentIdInput.isBlank()) {
                                        errorMessage = "कृपया अपनी स्टूडेंट आईडी या मोबाइल नंबर दर्ज करें।"
                                    } else if (studentPasswordInput.isBlank()) {
                                        errorMessage = "कृपया अपना पासवर्ड दर्ज करें।"
                                    } else {
                                        val success = onStudentLogin(studentIdInput, studentPasswordInput)
                                        if (!success) {
                                            errorMessage = "पंजीयन आईडी या पासवर्ड अमान्य है! कृपया सही विवरण दर्ज करें।"
                                        }
                                    }
                                }
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("student_password_input"),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Button(
                            onClick = {
                                if (studentIdInput.isBlank()) {
                                    errorMessage = "कृपया अपनी स्टूडेंट आईडी या मोबाइल नंबर दर्ज करें।"
                                } else if (studentPasswordInput.isBlank()) {
                                    errorMessage = "कृपया अपना पासवर्ड दर्ज करें।"
                                } else {
                                    val success = onStudentLogin(studentIdInput, studentPasswordInput)
                                    if (!success) {
                                        errorMessage = "पंजीयन आईडी या पासवर्ड अमान्य है! कृपया सही विवरण दर्ज करें।"
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("student_login_button")
                        ) {
                            Icon(Icons.Default.Login, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("प्रवेश करें (Secure Login)", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }

                        if (onRegisterStudent != null) {
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 4.dp),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            )

                            OutlinedButton(
                                onClick = { showRegistrationDialog = true },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = SaffronPrimary
                                ),
                                border = androidx.compose.foundation.BorderStroke(1.5.dp, SaffronPrimary),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("student_register_button")
                            ) {
                                Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("नया छात्र पंजीयन (निःशुल्क प्रवेश / Admission)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        }

                        TextButton(
                            onClick = { showForgotPasswordScreen = true },
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .testTag("forgot_password_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.LockReset,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = SaffronPrimary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "पासवर्ड भूल गए? (Forgot Password / Email Reset)",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = SaffronDark
                            )
                        }
                    }
                }
            }
        }

        // 5. Tab 1: Coach / Trainer Login
        if (selectedTab == 1) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("coach_login_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(SaffronPrimary.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SportsScore,
                                    contentDescription = null,
                                    tint = SaffronPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "ग्राउंड कोच एवं शारीरिक प्रशिक्षक पोर्टल",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "जय बजरंग अखाड़ा अधिकृत कोच लॉगिन",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Coach info banner
                        Surface(
                            color = SaffronContainer.copy(alpha = 0.4f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Verified,
                                    contentDescription = null,
                                    tint = SaffronDark,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "अधिकृत 4 जवान कोच: अपनी Coach ID (उदा. JBA-COACH-001) एवं पासवर्ड दर्ज करें।",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = OnSaffronContainer
                                )
                            }
                        }

                        // 4 Coaches quick selector chips
                        Text(
                            text = "अधिकृत कोच आईडी (Quick Select):",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val coachesQuick = listOf(
                                "JBA-COACH-001" to "001 (CRPF)",
                                "JBA-COACH-002" to "002 (अग्निवीर)",
                                "JBA-COACH-003" to "003 (CG पुलिस)",
                                "JBA-COACH-004" to "004 (ITBP)"
                            )
                            coachesQuick.forEach { (cid, lbl) ->
                                FilterChip(
                                    selected = coachIdInput == cid,
                                    onClick = {
                                        coachIdInput = cid
                                        errorMessage = null
                                    },
                                    label = { Text(lbl, fontSize = 11.sp) },
                                    modifier = Modifier.testTag("chip_$cid")
                                )
                            }
                        }

                        // Coach ID Input
                        OutlinedTextField(
                            value = coachIdInput,
                            onValueChange = {
                                coachIdInput = it
                                errorMessage = null
                            },
                            label = { Text("कोच ID (Coach ID) *") },
                            placeholder = { Text("उदा. JBA-COACH-001") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Badge,
                                    contentDescription = null,
                                    tint = SaffronPrimary
                                )
                            },
                            trailingIcon = {
                                if (coachIdInput.isNotEmpty()) {
                                    IconButton(onClick = { coachIdInput = "" }) {
                                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                                    }
                                }
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Next
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("coach_id_input"),
                            shape = RoundedCornerShape(12.dp)
                        )

                        // Coach Password Input
                        OutlinedTextField(
                            value = coachPasswordInput,
                            onValueChange = {
                                coachPasswordInput = it
                                errorMessage = null
                            },
                            label = { Text("गोपनीय पासवर्ड (Password) *") },
                            placeholder = { Text("अधिकृत पासवर्ड दर्ज करें") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = OliveTertiary
                                )
                            },
                            trailingIcon = {
                                IconButton(onClick = { showCoachPassword = !showCoachPassword }) {
                                    Icon(
                                        imageVector = if (showCoachPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = "Toggle password visibility"
                                    )
                                }
                            },
                            visualTransformation = if (showCoachPassword) VisualTransformation.None else PasswordVisualTransformation(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    val trimmedId = coachIdInput.trim()
                                    val trimmedPass = coachPasswordInput.trim()
                                    if (trimmedId.isBlank() || trimmedPass.isBlank()) {
                                        errorMessage = "कृपया Coach ID एवं पासवर्ड दोनों दर्ज करें!"
                                        return@KeyboardActions
                                    }
                                    val success = onTrainerLoginWithCredentials?.invoke(trimmedId, trimmedPass)
                                        ?: onTrainerLogin?.invoke(trimmedPass) ?: false
                                    if (!success) {
                                        errorMessage = "अमान्य कोच क्रेडेंशियल्स! कृपया सही Coach ID एवं पासवर्ड दर्ज करें।"
                                    }
                                }
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("coach_password_input"),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Button(
                            onClick = {
                                val trimmedId = coachIdInput.trim()
                                val trimmedPass = coachPasswordInput.trim()
                                if (trimmedId.isBlank() || trimmedPass.isBlank()) {
                                    errorMessage = "कृपया Coach ID एवं पासवर्ड दोनों दर्ज करें!"
                                    return@Button
                                }
                                val success = onTrainerLoginWithCredentials?.invoke(trimmedId, trimmedPass)
                                    ?: onTrainerLogin?.invoke(trimmedPass) ?: false
                                if (!success) {
                                    errorMessage = "अमान्य कोच क्रेडेंशियल्स! कृपया सही Coach ID एवं पासवर्ड दर्ज करें।"
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("coach_login_button")
                        ) {
                            Icon(Icons.Default.Login, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("कोच पोर्टल में प्रवेश करें (Coach Login)", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }
                }
            }
        }

        // 6. Tab 2: Admin Login
        if (selectedTab == 2) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AdminPanelSettings,
                                contentDescription = null,
                                tint = OliveTertiary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isAdminPinConfigured) "प्रशिक्षक नियंत्रण कक्ष (Admin Login)" else "प्रथम बार एडमिन सेटअप (Admin Setup)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = if (isAdminPinConfigured) {
                                "ग्राउंड उपस्थिति, ट्रेनिंग प्लान, स्टडी सामग्री एवं छात्र प्रबंधन हेतु"
                            } else {
                                "सुरक्षा हेतु अपना 4 से 6 अंकों का नया एडमिन पिन बनाएं"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        if (!isAdminPinConfigured) {
                            // First time Admin PIN setup
                            OutlinedTextField(
                                value = setupNewPinInput,
                                onValueChange = {
                                    if (it.length <= 6 && it.all { c -> c.isDigit() }) {
                                        setupNewPinInput = it
                                        errorMessage = null
                                    }
                                },
                                label = { Text("नया एडमिन पिन बनाएं (4-6 अंक) *") },
                                placeholder = { Text("उदाहरण: 4589") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.VpnKey,
                                        contentDescription = null,
                                        tint = SaffronPrimary
                                    )
                                },
                                trailingIcon = {
                                    IconButton(onClick = { showPassword = !showPassword }) {
                                        Icon(
                                            imageVector = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                            contentDescription = "Toggle password visibility"
                                        )
                                    }
                                },
                                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.NumberPassword,
                                    imeAction = ImeAction.Next
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("admin_setup_new_pin"),
                                shape = RoundedCornerShape(12.dp)
                            )

                            OutlinedTextField(
                                value = setupConfirmPinInput,
                                onValueChange = {
                                    if (it.length <= 6 && it.all { c -> c.isDigit() }) {
                                        setupConfirmPinInput = it
                                        errorMessage = null
                                    }
                                },
                                label = { Text("पिन की पुष्टि करें (Confirm PIN) *") },
                                placeholder = { Text("पिन पुनः दर्ज करें") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircleOutline,
                                        contentDescription = null,
                                        tint = OliveTertiary
                                    )
                                },
                                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.NumberPassword,
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        if (setupNewPinInput != setupConfirmPinInput) {
                                            errorMessage = "दोनों पिन मेल नहीं खाते! (PINs do not match)"
                                        } else if (setupNewPinInput.length !in 4..6) {
                                            errorMessage = "पिन 4 से 6 अंकों का होना चाहिए!"
                                        } else {
                                            val res = onSetupInitialAdminPin?.invoke(setupNewPinInput)
                                            if (res?.isFailure == true) {
                                                errorMessage = res.exceptionOrNull()?.message ?: "पिन सेटअप विफल।"
                                            }
                                        }
                                    }
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("admin_setup_confirm_pin"),
                                shape = RoundedCornerShape(12.dp)
                            )

                            Button(
                                onClick = {
                                    if (setupNewPinInput != setupConfirmPinInput) {
                                        errorMessage = "दोनों पिन मेल नहीं खाते! (PINs do not match)"
                                    } else if (setupNewPinInput.length !in 4..6) {
                                        errorMessage = "पिन 4 से 6 अंकों का होना चाहिए!"
                                    } else {
                                        val res = onSetupInitialAdminPin?.invoke(setupNewPinInput)
                                        if (res?.isFailure == true) {
                                            errorMessage = res.exceptionOrNull()?.message ?: "पिन सेटअप विफल।"
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .testTag("admin_setup_save_button")
                            ) {
                                Icon(Icons.Default.Security, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("पिन सुरक्षित करें एवं प्रवेश करें", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                        } else {
                            // Secure Admin & Coach Login
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = SaffronPrimary.copy(alpha = 0.08f)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, SaffronPrimary.copy(alpha = 0.3f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(CircleShape)
                                            .background(SaffronPrimary.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.Security,
                                            contentDescription = null,
                                            tint = SaffronPrimary,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = "अधिकृत व्यवस्थापक / कोच पोर्टल",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "कृपया अपनी गोपनीय ID एवं पासवर्ड दर्ज करें",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            // Security Notice
                            Surface(
                                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Security,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "उच्च सुरक्षा: व्यवस्थापक पोर्टल केवल अधिकृत व्यवस्थापक (Admin) क्रेडेंशियल्स से ही सुरक्षित रूप से खुलेगा।",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                            }

                            // 1. Admin ID / Mobile Input
                            OutlinedTextField(
                                value = adminIdInput,
                                onValueChange = {
                                    adminIdInput = it
                                    errorMessage = null
                                },
                                label = { Text("व्यवस्थापक ID (Admin ID) *") },
                                placeholder = { Text("अधिकृत ID या मोबाइल नंबर दर्ज करें") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Badge,
                                        contentDescription = null,
                                        tint = SaffronPrimary
                                    )
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Text,
                                    imeAction = ImeAction.Next
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("admin_id_input"),
                                shape = RoundedCornerShape(12.dp)
                            )

                            // 2. Admin Password / PIN Input
                            OutlinedTextField(
                                value = adminPasswordInput,
                                onValueChange = {
                                    adminPasswordInput = it
                                    adminPinInput = it
                                    errorMessage = null
                                },
                                label = { Text("व्यवस्थापक पासवर्ड (Password) *") },
                                placeholder = { Text("गोपनीय पासवर्ड दर्ज करें") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = OliveTertiary
                                    )
                                },
                                trailingIcon = {
                                    IconButton(onClick = { showPassword = !showPassword }) {
                                        Icon(
                                            imageVector = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                            contentDescription = "Toggle password visibility"
                                        )
                                    }
                                },
                                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Password,
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        val trimmedId = adminIdInput.trim()
                                        val trimmedPass = adminPasswordInput.trim().ifEmpty { adminPinInput.trim() }

                                        if (trimmedId.isBlank() || trimmedPass.isBlank()) {
                                            errorMessage = "व्यवस्थापक ID एवं पासवर्ड दोनों भरना अनिवार्य है!"
                                            return@KeyboardActions
                                        }

                                        if (trimmedPass == "1234" || trimmedPass == "0000" || trimmedPass == "1111") {
                                            errorMessage = "कमजोर पासवर्ड अमान्य है! केवल अधिकृत पासवर्ड से ही एडमिन पैनल खुलेगा।"
                                            return@KeyboardActions
                                        }

                                        val adminSuccess = onAdminLoginWithCredentials?.invoke(trimmedId, trimmedPass)
                                            ?: onAdminLogin(trimmedPass)
                                        if (!adminSuccess) {
                                            val trainerSuccess = onTrainerLoginWithCredentials?.invoke(trimmedId, trimmedPass)
                                                ?: onTrainerLogin?.invoke(trimmedPass) ?: false
                                            if (!trainerSuccess) {
                                                errorMessage = "गलत क्रेडेंशियल्स! केवल अधिकृत व्यवस्थापक ID एवं पासवर्ड दर्ज करें।"
                                            }
                                        }
                                    }
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("admin_pin_input"),
                                shape = RoundedCornerShape(12.dp)
                            )

                            // Login Button
                            Button(
                                onClick = {
                                    val trimmedId = adminIdInput.trim()
                                    val trimmedPass = adminPasswordInput.trim().ifEmpty { adminPinInput.trim() }

                                    if (trimmedId.isBlank() || trimmedPass.isBlank()) {
                                        errorMessage = "व्यवस्थापक ID एवं पासवर्ड दोनों भरना अनिवार्य है!"
                                        return@Button
                                    }

                                    if (trimmedPass == "1234" || trimmedPass == "0000" || trimmedPass == "1111") {
                                        errorMessage = "कमजोर पासवर्ड अमान्य है! केवल अधिकृत पासवर्ड से ही एडमिन पैनल खुलेगा।"
                                        return@Button
                                    }

                                    val adminSuccess = onAdminLoginWithCredentials?.invoke(trimmedId, trimmedPass)
                                        ?: onAdminLogin(trimmedPass)
                                    if (!adminSuccess) {
                                        val trainerSuccess = onTrainerLoginWithCredentials?.invoke(trimmedId, trimmedPass)
                                            ?: onTrainerLogin?.invoke(trimmedPass) ?: false
                                        if (!trainerSuccess) {
                                            errorMessage = "गलत क्रेडेंशियल्स! केवल अधिकृत व्यवस्थापक ID एवं पासवर्ड दर्ज करें।"
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = OliveTertiary),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .testTag("admin_login_button")
                            ) {
                                Icon(Icons.Default.AdminPanelSettings, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("व्यवस्थापक प्रवेश (Admin Login)", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }

                            TextButton(
                                onClick = { showForgotPasswordScreen = true },
                                modifier = Modifier
                                    .align(Alignment.CenterHorizontally)
                                    .testTag("admin_forgot_password_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LockReset,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = OliveTertiary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    "पासवर्ड भूल गए? (Forgot Password / Reset)",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = OliveTertiary
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showRegistrationDialog && onRegisterStudent != null) {
        StudentSelfRegistrationDialog(
            allStudents = allStudents,
            onDismiss = { showRegistrationDialog = false },
            onRegister = { newStudent ->
                onRegisterStudent(newStudent)
                showRegistrationDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentSelfRegistrationDialog(
    allStudents: List<StudentProfile>,
    onDismiss: () -> Unit,
    onRegister: (StudentProfile) -> Unit
) {
    val nextId = remember(allStudents) {
        com.example.util.ProfileUtils.generateNextStudentId(allStudents)
    }

    var name by remember { mutableStateOf("") }
    var fatherName by remember { mutableStateOf("") }
    var mobile by remember { mutableStateOf("") }
    var village by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showRegPassword by remember { mutableStateOf(false) }
    var dob by remember { mutableStateOf("2005-05-15") }
    var gender by remember { mutableStateOf("Male") }
    var education by remember { mutableStateOf("12th Pass") }
    var goal by remember { mutableStateOf("Indian Army (Agniveer GD)") }
    var height by remember { mutableStateOf("170.0") }
    var weight by remember { mutableStateOf("62.0") }
    var time1600 by remember { mutableStateOf("5:30") }
    var pushups by remember { mutableStateOf("30") }
    var validationError by remember { mutableStateOf<String?>(null) }

    val calculatedAge = remember(dob) {
        val age = ProfileUtils.calculateAgeFromDob(dob)
        if (age > 0) age else 19
    }

    val liveBmi = remember(height, weight) {
        val h = height.toDoubleOrNull() ?: 170.0
        val w = weight.toDoubleOrNull() ?: 62.0
        ProfileUtils.getBmiCategory(h, w)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.HowToReg, contentDescription = null, tint = SaffronPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text("नया छात्र पंजीयन (निःशुल्क प्रवेश)", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text("जय बजरंग अखाड़ा, मौरीकला गुफा", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 480.dp)
                    .testTag("self_registration_dialog"),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (validationError != null) {
                    item {
                        Surface(
                            color = MaterialTheme.colorScheme.errorContainer,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = validationError ?: "",
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                }

                item {
                    Surface(
                        color = SaffronContainer.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("आवंटित पंजीयन संख्या (ID):", style = MaterialTheme.typography.bodySmall, color = OnSaffronContainer)
                            Text(nextId, fontWeight = FontWeight.ExtraBold, color = SaffronDark, style = MaterialTheme.typography.titleSmall)
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it; validationError = null },
                        label = { Text("पूरा नाम (Full Name) *") },
                        placeholder = { Text("उदा. राहुल सिंह") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("reg_name_input")
                    )
                }

                item {
                    OutlinedTextField(
                        value = fatherName,
                        onValueChange = { fatherName = it },
                        label = { Text("पिता का नाम (Father's Name)") },
                        placeholder = { Text("उदा. श्री रामेश्वर सिंह") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("reg_father_input")
                    )
                }

                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = mobile,
                            onValueChange = { mobile = it; validationError = null },
                            label = { Text("मोबाइल नंबर (10 अंक) *") },
                            placeholder = { Text("9876543210") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true,
                            modifier = Modifier.weight(1f).testTag("reg_mobile_input")
                        )
                        OutlinedTextField(
                            value = village,
                            onValueChange = { village = it; validationError = null },
                            label = { Text("गाँव (Village) *") },
                            placeholder = { Text("मौरिकला") },
                            singleLine = true,
                            modifier = Modifier.weight(1f).testTag("reg_village_input")
                        )
                    }
                }

                // Password fields for secure registration
                item {
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it; validationError = null },
                        label = { Text("पासवर्ड बनाएं (कम से कम 6 अक्षर) *") },
                        placeholder = { Text("सुरक्षित पासवर्ड") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = SaffronPrimary) },
                        trailingIcon = {
                            IconButton(onClick = { showRegPassword = !showRegPassword }) {
                                Icon(
                                    imageVector = if (showRegPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Toggle password"
                                )
                            }
                        },
                        visualTransformation = if (showRegPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth().testTag("reg_password_input")
                    )
                }

                item {
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it; validationError = null },
                        label = { Text("पासवर्ड की पुष्टि करें (Confirm Password) *") },
                        placeholder = { Text("पासवर्ड पुनः दर्ज करें") },
                        leadingIcon = { Icon(Icons.Default.CheckCircleOutline, contentDescription = null, tint = OliveTertiary) },
                        visualTransformation = if (showRegPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth().testTag("reg_confirm_password_input")
                    )
                }

                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = dob,
                            onValueChange = { dob = it; validationError = null },
                            label = { Text("जन्म तिथि (YYYY-MM-DD)") },
                            placeholder = { Text("2005-05-15") },
                            singleLine = true,
                            modifier = Modifier.weight(1.3f).testTag("reg_dob_input")
                        )
                        OutlinedTextField(
                            value = "$calculatedAge वर्ष",
                            onValueChange = {},
                            readOnly = true,
                            enabled = false,
                            label = { Text("आयु (Auto)") },
                            modifier = Modifier.weight(0.9f)
                        )
                    }
                }

                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = gender,
                            onValueChange = { gender = it },
                            label = { Text("लिंग (Gender)") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = education,
                            onValueChange = { education = it },
                            label = { Text("शिक्षा (Education)") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    OutlinedTextField(
                        value = goal,
                        onValueChange = { goal = it },
                        label = { Text("भर्ती लक्ष्य (Target Force/Exam)") },
                        placeholder = { Text("Indian Army / State Police / SSC GD") },
                        modifier = Modifier.fillMaxWidth().testTag("reg_goal_input")
                    )
                }

                item {
                    Text("प्रारंभिक शारीरिक माप (Physical Measurements):", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                }

                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = height,
                            onValueChange = { height = it },
                            label = { Text("ऊंचाई (cm)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = weight,
                            onValueChange = { weight = it },
                            label = { Text("वजन (kg)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    Surface(
                        color = liveBmi.color.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "बीएमआई: ${liveBmi.bmiValue} • ${liveBmi.labelHindi}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = liveBmi.color,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }

                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = time1600,
                            onValueChange = { time1600 = it },
                            label = { Text("1600m लक्ष्य समय") },
                            placeholder = { Text("5:30") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = pushups,
                            onValueChange = { pushups = it },
                            label = { Text("पुश-अप्स क्षमता") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val cleanMobile = mobile.trim().filter { it.isDigit() }
                    val passPolicyErr = com.example.util.StudentAuthManager.validatePasswordPolicy(password)
                    if (name.isBlank()) {
                        validationError = "कृपया छात्र का पूरा नाम दर्ज करें।"
                    } else if (village.isBlank()) {
                        validationError = "कृपया गाँव का नाम दर्ज करें।"
                    } else if (cleanMobile.length != 10) {
                        validationError = "कृपया वैध 10 अंकों का मोबाइल नंबर दर्ज करें! (10 digits required)"
                    } else if (passPolicyErr != null) {
                        validationError = passPolicyErr
                    } else if (password.trim() != confirmPassword.trim()) {
                        validationError = "दोनों पासवर्ड मेल नहीं खाते! (Passwords do not match)"
                    } else if (allStudents.any { it.mobileNumber.trim().filter { c -> c.isDigit() } == cleanMobile }) {
                        validationError = "यह मोबाइल नंबर ($cleanMobile) पहले से पंजीकृत है! कृपया दूसरा नंबर दर्ज करें।"
                    } else {
                        val (hash, salt) = com.example.util.StudentAuthManager.createPasswordCredentials(password)
                        val newStudent = StudentProfile(
                            studentId = nextId,
                            fullName = name.trim(),
                            fatherName = fatherName.trim().ifEmpty { "श्री रामकुमार" },
                            village = village.trim().ifEmpty { "मौरिकला" },
                            age = calculatedAge,
                            dob = dob.trim().ifEmpty { "2005-05-15" },
                            gender = gender.trim().ifEmpty { "Male" },
                            education = education.trim().ifEmpty { "12th Pass" },
                            mobileNumber = cleanMobile,
                            recruitmentGoal = goal.trim().ifEmpty { "Indian Army (Agniveer GD)" },
                            heightCm = height.toDoubleOrNull() ?: 170.0,
                            weightKg = weight.toDoubleOrNull() ?: 62.0,
                            chestNormalCm = 81.0,
                            chestExpandedCm = 86.0,
                            time1600m = time1600.trim().ifEmpty { "5:30" },
                            time400m = "1:10",
                            time800m = "2:30",
                            time5km = "22:00",
                            pushups = pushups.toIntOrNull() ?: 30,
                            situps = 40,
                            pullups = 8,
                            squats = 45,
                            plankSeconds = 90,
                            longJumpFeet = 14.5,
                            highJumpFeet = 4.0,
                            shotPutMeters = 7.0,
                            attendanceStreakDays = 1,
                            studyTargetPercentage = 0,
                            overallScore = 80,
                            passwordHash = hash,
                            passwordSalt = salt
                        )
                        onRegister(newStudent)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary),
                modifier = Modifier.testTag("reg_submit_button")
            ) {
                Text("पंजीयन पूर्ण करें एवं प्रवेश लें", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("रद्द करें")
            }
        }
    )
}
