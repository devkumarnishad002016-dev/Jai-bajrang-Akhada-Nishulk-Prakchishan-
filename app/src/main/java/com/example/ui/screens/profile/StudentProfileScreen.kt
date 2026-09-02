package com.example.ui.screens.profile

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.StudentProfile
import com.example.ui.theme.*
import com.example.util.ProfileUtils
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentProfileScreen(
    student: StudentProfile?,
    onUpdateProfile: (StudentProfile) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var showEditDialog by remember { mutableStateOf(false) }
    var showPhotoOptionsDialog by remember { mutableStateOf(false) }

    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null && student != null) {
            val savedUri = ProfileUtils.saveBitmapToInternalStorage(context, bitmap, student.studentId)
            if (savedUri.isNotBlank()) {
                val updated = student.copy(profilePhotoUri = savedUri)
                onUpdateProfile(updated)
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("कैमरा से फोटो सफलतापूर्वक अपडेट की गई!")
                }
            }
        }
    }

    // Gallery launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null && student != null) {
            val savedUri = ProfileUtils.saveUriToInternalStorage(context, uri, student.studentId)
            val updated = student.copy(profilePhotoUri = savedUri)
            onUpdateProfile(updated)
            coroutineScope.launch {
                snackbarHostState.showSnackbar("गैलरी से फोटो सफलतापूर्वक अपडेट की गई!")
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("student_profile_screen"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Profile Top Hero Card
            item {
                student?.let { s ->
                    val calculatedAge = remember(s.dob) {
                        val ageCalc = ProfileUtils.calculateAgeFromDob(s.dob)
                        if (ageCalc > 0) ageCalc else s.age
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Avatar Photo with camera badge
                            Box(
                                modifier = Modifier
                                    .size(96.dp)
                                    .clickable { showPhotoOptionsDialog = true }
                                    .testTag("profile_photo_avatar"),
                                contentAlignment = Alignment.Center
                            ) {
                                if (s.profilePhotoUri.isNotBlank()) {
                                    AsyncImage(
                                        model = s.profilePhotoUri,
                                        contentDescription = "Student Photo",
                                        modifier = Modifier
                                            .size(96.dp)
                                            .clip(CircleShape)
                                            .border(3.dp, SaffronPrimary, CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(96.dp)
                                            .clip(CircleShape)
                                            .background(
                                                Brush.linearGradient(listOf(NavySecondary, SaffronPrimary))
                                            )
                                            .border(3.dp, SaffronPrimary, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = "Student Photo",
                                            tint = Color.White,
                                            modifier = Modifier.size(56.dp)
                                        )
                                    }
                                }

                                // Camera edit icon badge
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .size(30.dp)
                                        .clip(CircleShape)
                                        .background(SaffronPrimary)
                                        .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PhotoCamera,
                                        contentDescription = "Change Photo",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = s.fullName,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(modifier = Modifier.height(2.dp))

                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Badge,
                                        contentDescription = null,
                                        tint = SaffronPrimary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "आईडी: ${s.studentId}",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "गाँव: ${s.village} • उम्र: ${calculatedAge} वर्ष • लिंग: ${s.gender}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    color = SaffronContainer,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "भर्ती लक्ष्य: ${s.recruitmentGoal}",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = OnSaffronContainer,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                                    )
                                }

                                Surface(
                                    color = OliveContainer,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "योग्यता: ${s.education}",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = OnOliveContainer,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { showPhotoOptionsDialog = true },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(imageVector = Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("फोटो बदलें")
                                }

                                Button(
                                    onClick = { showEditDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .weight(1.3f)
                                        .testTag("edit_profile_button")
                                ) {
                                    Icon(imageVector = Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("विवरण संपादित करें")
                                }
                            }
                        }
                    }
                }
            }

            // 2. BMI & Physical Health Card
            item {
                student?.let { s ->
                    val bmiInfo = remember(s.heightCm, s.weightKg) {
                        ProfileUtils.getBmiCategory(s.heightCm, s.weightKg)
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.MonitorWeight,
                                        contentDescription = null,
                                        tint = SaffronPrimary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "बीएमआई एवं फिटनेस स्तर (BMI Assessment)",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Surface(
                                    color = bmiInfo.color.copy(alpha = 0.18f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = bmiInfo.labelHindi,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = bmiInfo.color,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // BMI Score highlight box
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(
                                            text = "बॉडी मास इंडेक्स (BMI Score)",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "${bmiInfo.bmiValue}",
                                            style = MaterialTheme.typography.headlineMedium,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = bmiInfo.color
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "ऊंचाई: ${s.heightCm} cm",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = "वजन: ${s.weightKg} kg",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = "सीना: ${s.chestNormalCm}-${s.chestExpandedCm} cm",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "सलाह: ${bmiInfo.descriptionHindi}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // 3. Ground Best Records (400m, 800m, 1600m, 5KM, Push-ups, Sit-ups, Pull-ups)
            item {
                student?.let { s ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.EmojiEvents,
                                    contentDescription = null,
                                    tint = GoldAccent,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "ग्राउंड सर्वश्रेष्ठ रिकॉर्ड (Ground Performance)",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            RecordRow("400 Meter Sprint", s.time400m, SaffronPrimary)
                            RecordRow("800 Meter Middle Distance", s.time800m, Color(0xFF7C3AED))
                            RecordRow("1600 Meter Running Trial", s.time1600m, SaffronPrimary)
                            RecordRow("5 KM Endurance Timing", s.time5km, OliveTertiary)
                            RecordRow("पुश-अप्स (Push-ups Max)", "${s.pushups} Reps", NavySecondary)
                            RecordRow("सिट-अप्स (Sit-ups Core)", "${s.situps} Reps", Color(0xFF0D9488))
                            RecordRow("बीम / पुल-अप्स (Pull-ups)", "${s.pullups} Beam (पूर्ण 40 अंक)", GoldAccent)
                        }
                    }
                }
            }

            // 4. Personal & Enrollment Details
            item {
                student?.let { s ->
                    val calculatedAge = remember(s.dob) {
                        val ageCalc = ProfileUtils.calculateAgeFromDob(s.dob)
                        if (ageCalc > 0) ageCalc else s.age
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.ContactPage,
                                    contentDescription = null,
                                    tint = NavySecondary,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "व्यक्तिगत व अभियान विवरण (Student Details)",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            DetailRow("छात्र यूनिक आईडी (Permanent ID)", s.studentId)
                            DetailRow("पूरा नाम (Full Name)", s.fullName)
                            DetailRow("पिता का नाम (Father's Name)", s.fatherName)
                            DetailRow("मोबाइल नंबर (Mobile)", s.mobileNumber)
                            DetailRow("गाँव / ब्लॉक (Village)", s.village)
                            DetailRow("जन्म तिथि (Date of Birth)", s.dob)
                            DetailRow("आयु (Auto Calculated Age)", "$calculatedAge वर्ष")
                            DetailRow("लिंग (Gender)", s.gender)
                            DetailRow("शैक्षणिक योग्यता (Education)", s.education)
                            DetailRow("भर्ती लक्ष्य (Target Mission)", s.recruitmentGoal)
                            DetailRow("नामांकन तिथि (Joining Date)", s.joinDate)
                            DetailRow("संस्था", "जय बजरंग अखाड़ा, मौरिकला गुफा")
                        }
                    }
                }
            }
        }
    }

    // Photo selection modal dialog
    if (showPhotoOptionsDialog && student != null) {
        AlertDialog(
            onDismissRequest = { showPhotoOptionsDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.AddAPhoto,
                    contentDescription = null,
                    tint = SaffronPrimary,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = "प्रोफाइल फोटो बदलें",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Card(
                        onClick = {
                            showPhotoOptionsDialog = false
                            cameraLauncher.launch(null)
                        },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.PhotoCamera, contentDescription = null, tint = SaffronPrimary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("कैमरा से नई फोटो लें (Take Photo)", fontWeight = FontWeight.Bold)
                        }
                    }

                    Card(
                        onClick = {
                            showPhotoOptionsDialog = false
                            galleryLauncher.launch("image/*")
                        },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = OliveTertiary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("गैलरी से चुनें (Select from Gallery)", fontWeight = FontWeight.Bold)
                        }
                    }

                    if (student.profilePhotoUri.isNotBlank()) {
                        Card(
                            onClick = {
                                showPhotoOptionsDialog = false
                                val updated = student.copy(profilePhotoUri = "")
                                onUpdateProfile(updated)
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("फोटो हटा दी गई")
                                }
                            },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("फोटो हटाएं (Remove Photo)", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPhotoOptionsDialog = false }) {
                    Text("रद्द करें")
                }
            }
        )
    }

    // Comprehensive Edit Profile Dialog
    if (showEditDialog && student != null) {
        var fullName by remember { mutableStateOf(student.fullName) }
        var fatherName by remember { mutableStateOf(student.fatherName) }
        var mobile by remember { mutableStateOf(student.mobileNumber) }
        var village by remember { mutableStateOf(student.village) }
        var dob by remember { mutableStateOf(student.dob) }
        var gender by remember { mutableStateOf(student.gender) }
        var education by remember { mutableStateOf(student.education) }
        var recruitmentGoal by remember { mutableStateOf(student.recruitmentGoal) }
        var heightStr by remember { mutableStateOf(student.heightCm.toString()) }
        var weightStr by remember { mutableStateOf(student.weightKg.toString()) }
        var time400 by remember { mutableStateOf(student.time400m) }
        var time800 by remember { mutableStateOf(student.time800m) }
        var time1600 by remember { mutableStateOf(student.time1600m) }
        var time5km by remember { mutableStateOf(student.time5km) }
        var pushupsStr by remember { mutableStateOf(student.pushups.toString()) }
        var situpsStr by remember { mutableStateOf(student.situps.toString()) }
        var pullupsStr by remember { mutableStateOf(student.pullups.toString()) }

        // Live automatic Age calculation from DOB
        val liveAge = remember(dob) {
            val calc = ProfileUtils.calculateAgeFromDob(dob)
            if (calc > 0) calc else student.age
        }

        // Live BMI calculation
        val liveHeight = heightStr.toDoubleOrNull() ?: student.heightCm
        val liveWeight = weightStr.toDoubleOrNull() ?: student.weightKg
        val liveBmi = remember(liveHeight, liveWeight) {
            ProfileUtils.getBmiCategory(liveHeight, liveWeight)
        }

        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = null, tint = SaffronPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("छात्र प्रोफाइल एवं माप अपडेट करें", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            },
            text = {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 480.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Read-only Student ID
                    item {
                        OutlinedTextField(
                            value = student.studentId,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("स्टूडेंट आईडी (स्थायी)") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                            ),
                            enabled = false
                        )
                    }

                    // Personal Information
                    item {
                        Text("व्यक्तिगत जानकारी", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                    }

                    item {
                        OutlinedTextField(
                            value = fullName,
                            onValueChange = { fullName = it },
                            label = { Text("पूरा नाम (Full Name)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = fatherName,
                            onValueChange = { fatherName = it },
                            label = { Text("पिता का नाम (Father's Name)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = mobile,
                                onValueChange = { mobile = it },
                                label = { Text("मोबाइल नंबर") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = village,
                                onValueChange = { village = it },
                                label = { Text("गाँव (Village)") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = dob,
                                onValueChange = { dob = it },
                                label = { Text("जन्म तिथि (YYYY-MM-DD)") },
                                placeholder = { Text("2004-06-12") },
                                modifier = Modifier.weight(1.3f)
                            )

                            // Read-only Live Calculated Age Box
                            OutlinedTextField(
                                value = "$liveAge वर्ष",
                                onValueChange = {},
                                readOnly = true,
                                enabled = false,
                                label = { Text("आयु (Auto)") },
                                modifier = Modifier.weight(0.9f),
                                colors = OutlinedTextFieldDefaults.colors(
                                    disabledTextColor = SaffronPrimary,
                                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    disabledBorderColor = SaffronPrimary.copy(alpha = 0.5f)
                                )
                            )
                        }
                    }

                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = gender,
                                onValueChange = { gender = it },
                                label = { Text("लिंग (Male/Female)") },
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = education,
                                onValueChange = { education = it },
                                label = { Text("शिक्षा (उदा. 12th)") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    item {
                        OutlinedTextField(
                            value = recruitmentGoal,
                            onValueChange = { recruitmentGoal = it },
                            label = { Text("भर्ती लक्ष्य (Indian Army, CG Police, SSC GD, आदि)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Physical Measurements & Live BMI
                    item {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("शारीरिक माप एवं बीएमआई", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                    }

                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = heightStr,
                                onValueChange = { heightStr = it },
                                label = { Text("ऊंचाई (cm)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = weightStr,
                                onValueChange = { weightStr = it },
                                label = { Text("वजन (kg)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Live BMI Preview Card in Dialog
                    item {
                        Surface(
                            color = liveBmi.color.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("बीएमआई (BMI): ${liveBmi.bmiValue}", fontWeight = FontWeight.Bold, color = liveBmi.color)
                                    Text(liveBmi.labelHindi, style = MaterialTheme.typography.labelSmall, color = liveBmi.color)
                                }
                                Text(
                                    text = if (liveBmi.bmiValue in 18.5..24.9) "फिट ✓" else "ध्यान दें",
                                    fontWeight = FontWeight.Bold,
                                    color = liveBmi.color
                                )
                            }
                        }
                    }

                    // Ground Timings & Exercises
                    item {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("ग्राउंड टाइमिंग एवं रेप्स", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                    }

                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = time400,
                                onValueChange = { time400 = it },
                                label = { Text("400m टाइमिंग") },
                                placeholder = { Text("1:05") },
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = time800,
                                onValueChange = { time800 = it },
                                label = { Text("800m टाइमिंग") },
                                placeholder = { Text("2:25") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = time1600,
                                onValueChange = { time1600 = it },
                                label = { Text("1600m टाइमिंग") },
                                placeholder = { Text("5:20") },
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = time5km,
                                onValueChange = { time5km = it },
                                label = { Text("5 KM टाइमिंग") },
                                placeholder = { Text("21:00") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = pushupsStr,
                                onValueChange = { pushupsStr = it },
                                label = { Text("पुश-अप्स") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = situpsStr,
                                onValueChange = { situpsStr = it },
                                label = { Text("सिट-अप्स") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = pullupsStr,
                                onValueChange = { pullupsStr = it },
                                label = { Text("बीम / पुल-अप्स") },
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
                        val parsedHeight = heightStr.toDoubleOrNull() ?: student.heightCm
                        val parsedWeight = weightStr.toDoubleOrNull() ?: student.weightKg
                        val finalAge = ProfileUtils.calculateAgeFromDob(dob).let { if (it > 0) it else student.age }

                        val updated = student.copy(
                            fullName = fullName.trim().ifEmpty { student.fullName },
                            fatherName = fatherName.trim(),
                            mobileNumber = mobile.trim(),
                            village = village.trim().ifEmpty { student.village },
                            dob = dob.trim().ifEmpty { student.dob },
                            age = finalAge,
                            gender = gender.trim().ifEmpty { student.gender },
                            education = education.trim().ifEmpty { student.education },
                            recruitmentGoal = recruitmentGoal.trim().ifEmpty { student.recruitmentGoal },
                            heightCm = parsedHeight,
                            weightKg = parsedWeight,
                            time400m = time400.trim().ifEmpty { student.time400m },
                            time800m = time800.trim().ifEmpty { student.time800m },
                            time1600m = time1600.trim().ifEmpty { student.time1600m },
                            time5km = time5km.trim().ifEmpty { student.time5km },
                            pushups = pushupsStr.toIntOrNull() ?: student.pushups,
                            situps = situpsStr.toIntOrNull() ?: student.situps,
                            pullups = pullupsStr.toIntOrNull() ?: student.pullups
                        )

                        onUpdateProfile(updated)
                        showEditDialog = false
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("प्रोफाइल डेटा सफलतापूर्वक सेव हो गया!")
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary),
                    modifier = Modifier.testTag("save_profile_button")
                ) {
                    Text("सेव करें (Save)")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("रद्द करें")
                }
            }
        )
    }
}

@Composable
fun RecordRow(title: String, value: String, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, style = MaterialTheme.typography.bodyMedium)
        Surface(
            color = color.copy(alpha = 0.12f),
            shape = RoundedCornerShape(6.dp)
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = color,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
            )
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f)
        )
    }
}
