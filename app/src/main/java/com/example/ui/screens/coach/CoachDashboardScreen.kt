package com.example.ui.screens.coach

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoachDashboardScreen(
    coach: Trainer?,
    allStudents: List<StudentProfile>,
    allNotices: List<Notice>,
    latestWorkoutPlan: DailyWorkoutPlan?,
    allAttendanceRecords: List<AttendanceRecord>,
    allTrainingRecords: List<TrainingRecord>,
    mustChangePassword: Boolean,
    onNavigate: (String) -> Unit,
    onRecordGroundTest: (
        studentId: String,
        time1600m: String,
        pushups: Int,
        situps: Int,
        pullups: Int,
        longJumpFeet: Double,
        highJumpFeet: Double,
        shotPutMeters: Double,
        coachNotes: String
    ) -> Unit,
    onChangePassword: (coachId: String, newPass: String) -> Result<Unit>,
    onDismissPasswordPrompt: () -> Unit,
    modifier: Modifier = Modifier
) {
    val todayDateStr = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }
    var selectedSection by remember { mutableStateOf(0) } // 0: Training, 1: Cadets, 2: 1600m & Stopwatch, 3: Notices
    var showGroundTestDialog by remember { mutableStateOf(false) }
    var selectedStudentForTest by remember { mutableStateOf<StudentProfile?>(null) }
    var showChangePasswordDialog by remember { mutableStateOf(mustChangePassword) }
    var searchQuery by remember { mutableStateOf("") }

    // Sync state if mustChangePassword changes externally
    LaunchedEffect(mustChangePassword) {
        if (mustChangePassword) {
            showChangePasswordDialog = true
        }
    }

    val assignedStudents: List<StudentProfile> = remember(allStudents, coach, searchQuery) {
        val list = if (coach != null && coach.name.isNotBlank()) {
            val matched = allStudents.filter {
                it.assignedTrainerName.contains(coach.name.trim(), ignoreCase = true) ||
                (coach.coachId.isNotBlank() && it.assignedTrainerId.equals(coach.coachId, ignoreCase = true))
            }
            if (matched.isNotEmpty()) matched else allStudents
        } else {
            allStudents
        }

        if (searchQuery.isBlank()) {
            list
        } else {
            val q = searchQuery.trim()
            list.filter {
                it.fullName.contains(q, ignoreCase = true) ||
                it.studentId.contains(q, ignoreCase = true) ||
                it.recruitmentGoal.contains(q, ignoreCase = true)
            }
        }
    }

    val todayAttendanceMap = remember(allAttendanceRecords, todayDateStr) {
        allAttendanceRecords.filter { it.date == todayDateStr }
            .associateBy({ it.studentId }, { it.status })
    }

    val presentCount = assignedStudents.count {
        val st = todayAttendanceMap[it.studentId]
        st != null && st.equals("Present", ignoreCase = true)
    }

    Scaffold(
        modifier = modifier.testTag("coach_dashboard_screen"),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "ग्राउंड कोच डैशबोर्ड",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "जय बजरंग अखाड़ा — मौरीकला (गुफा)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showChangePasswordDialog = true },
                        modifier = Modifier.testTag("coach_change_password_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.LockReset,
                            contentDescription = "Change Password",
                            tint = SaffronPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // 1. Mandatory Password Change Alert Banner (if applicable)
            if (mustChangePassword) {
                item {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "सुरक्षा अलर्ट: नया पासवर्ड बनाएं",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                                Text(
                                    text = "प्रथम लॉगिन पर अपनी पसंद का सुरक्षित पासवर्ड निर्धारित करना अनिवार्य है।",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                            Button(
                                onClick = { showChangePasswordDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text("बदलें", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // 2. Coach Hero Profile Card (Coach Name, Achievement, Coach ID, Role Badge)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("coach_hero_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(NavySecondary, NavyLight)
                                )
                            )
                            .padding(18.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(52.dp)
                                            .clip(CircleShape)
                                            .background(SaffronPrimary),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.SportsScore,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(30.dp)
                                        )
                                    }
                                    Column {
                                        Text(
                                            text = coach?.name ?: "जय बजरंग अखाड़ा कोच",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 18.sp,
                                            color = Color.White
                                        )
                                        Text(
                                            text = "Coach ID: ${coach?.coachId ?: "JBA-COACH-001"}",
                                            fontSize = 12.sp,
                                            color = SaffronLight,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }

                                Surface(
                                    color = SaffronPrimary,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "TRAINER",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            // Achievement & Specialization
                            Surface(
                                color = Color.White.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.EmojiEvents,
                                        contentDescription = null,
                                        tint = GoldLight,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "उपलब्धि: ${coach?.achievement?.ifBlank { coach.experience } ?: "चयनित जांबाज जवान"}",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                        Text(
                                            text = "विशेषज्ञता: ${coach?.specialization ?: "1600m रनिंग, ग्राउंड ट्रेनिंग, फिजिकल एंड्योरेंस"}",
                                            color = Color.White.copy(alpha = 0.8f),
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 3. Quick Stats Ribbon (Assigned Cadets, Present Today, Live Stopwatch, Notices)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CoachStatChip(
                        title = "असाइन कैडेट्स",
                        value = "${assignedStudents.size}",
                        icon = Icons.Default.Groups,
                        color = NavySecondary,
                        modifier = Modifier.weight(1f)
                    )
                    CoachStatChip(
                        title = "आज उपस्थिति",
                        value = "$presentCount / ${assignedStudents.size}",
                        icon = Icons.Default.FactCheck,
                        color = OliveTertiary,
                        modifier = Modifier.weight(1f)
                    )
                    CoachStatChip(
                        title = "1600m रनिंग",
                        value = "स्टॉपवॉच",
                        icon = Icons.Default.Timer,
                        color = SaffronPrimary,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onNavigate("admin_live_stopwatch") }
                    )
                }
            }

            // 4. Primary Quick Action Buttons
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { onNavigate("admin_attendance") },
                        colors = ButtonDefaults.buttonColors(containerColor = OliveTertiary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("coach_mark_attendance_btn")
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("हाजिरी लें", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { onNavigate("admin_live_stopwatch") },
                        colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("coach_open_stopwatch_btn")
                    ) {
                        Icon(Icons.Default.Timer, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("1600m टाइमर", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = { showGroundTestDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1.1f)
                            .testTag("coach_record_ground_btn")
                    ) {
                        Icon(Icons.Default.FitnessCenter, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("टेस्ट रिकॉर्ड", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // 5. Section Tabs (Today's Training | Cadets | Running/1600m | Notices)
            item {
                TabRow(
                    selectedTabIndex = selectedSection,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.clip(RoundedCornerShape(12.dp))
                ) {
                    Tab(
                        selected = selectedSection == 0,
                        onClick = { selectedSection = 0 },
                        text = { Text("आज का वर्कआउट", fontWeight = FontWeight.SemiBold, fontSize = 12.sp) }
                    )
                    Tab(
                        selected = selectedSection == 1,
                        onClick = { selectedSection = 1 },
                        text = { Text("कैडेट्स (${assignedStudents.size})", fontWeight = FontWeight.SemiBold, fontSize = 12.sp) }
                    )
                    Tab(
                        selected = selectedSection == 2,
                        onClick = { selectedSection = 2 },
                        text = { Text("1600m व परफॉर्मेंस", fontWeight = FontWeight.SemiBold, fontSize = 12.sp) }
                    )
                    Tab(
                        selected = selectedSection == 3,
                        onClick = { selectedSection = 3 },
                        text = { Text("सूचनाएं", fontWeight = FontWeight.SemiBold, fontSize = 12.sp) }
                    )
                }
            }

            // 6. Tab Content Switcher
            when (selectedSection) {
                0 -> {
                    // --- SECTION 0: TODAY'S TRAINING SESSION ---
                    item {
                        TodayTrainingSection(
                            plan = latestWorkoutPlan,
                            onManagePlan = { onNavigate("admin_workout") }
                        )
                    }
                }
                1 -> {
                    // --- SECTION 1: ASSIGNED CADETS LIST & ATTENDANCE STATUS ---
                    item {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("कैडेट खोजें (नाम / आईडी / परीक्षा)...") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("coach_cadet_search"),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }

                    if (assignedStudents.isEmpty()) {
                        item {
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "कोई कैडेट नहीं मिला।",
                                    modifier = Modifier.padding(20.dp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        items(assignedStudents) { student ->
                            val attStatus = todayAttendanceMap[student.studentId] ?: "PENDING"
                            CadetProgressCard(
                                student = student,
                                todayStatus = attStatus,
                                onRecordTest = {
                                    selectedStudentForTest = student
                                    showGroundTestDialog = true
                                }
                            )
                        }
                    }
                }
                2 -> {
                    // --- SECTION 2: RUNNING / 1600M STOPWATCH & PERFORMANCE TRACKER ---
                    item {
                        RunningPerformanceSection(
                            allTrainingRecords = allTrainingRecords,
                            onLaunchStopwatch = { onNavigate("admin_live_stopwatch") },
                            onLaunchGroundRecord = { showGroundTestDialog = true },
                            onNavigateToMonthly = { onNavigate("monthly_performance") }
                        )
                    }
                }
                3 -> {
                    // --- SECTION 3: TRAINING NOTICES & RECRUITMENT ALERTS ---
                    if (allNotices.isEmpty()) {
                        item {
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "वर्तमान में कोई नई सूचना उपलब्ध नहीं है।",
                                    modifier = Modifier.padding(20.dp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        items(allNotices) { notice ->
                            CoachNoticeItem(notice = notice)
                        }
                    }
                }
            }
        }
    }

    // --- Dialog 1: Ground Physical Test Record ---
    if (showGroundTestDialog) {
        GroundTestRecordDialog(
            students = assignedStudents,
            preselectedStudent = selectedStudentForTest,
            onDismiss = {
                showGroundTestDialog = false
                selectedStudentForTest = null
            },
            onSave = { stId, time, push, sit, pull, lj, hj, sp, notes ->
                onRecordGroundTest(stId, time, push, sit, pull, lj, hj, sp, notes)
                showGroundTestDialog = false
                selectedStudentForTest = null
            }
        )
    }

    // --- Dialog 2: Mandatory or User-Requested Change Password ---
    if (showChangePasswordDialog) {
        CoachChangePasswordDialog(
            coach = coach,
            isMandatory = mustChangePassword,
            onDismiss = {
                if (!mustChangePassword) {
                    showChangePasswordDialog = false
                }
            },
            onSavePassword = { newPass ->
                val result = onChangePassword(coach?.coachId ?: "JBA-COACH-001", newPass)
                if (result.isSuccess) {
                    showChangePasswordDialog = false
                    onDismissPasswordPrompt()
                }
                result
            }
        )
    }
}

@Composable
fun CoachStatChip(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(14.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = color)
            Text(text = title, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun TodayTrainingSection(
    plan: DailyWorkoutPlan?,
    onManagePlan: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "आज का निर्धारित ग्राउंड वर्कआउट",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                TextButton(onClick = onManagePlan) {
                    Text("प्लान देखें / अपडेट करें", fontSize = 12.sp, color = SaffronPrimary)
                }
            }

            if (plan != null) {
                Surface(
                    color = SaffronContainer.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = plan.title.ifBlank { "दैनिक फिजिकल एंड्योरेंस सत्र" },
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = OnSaffronContainer
                        )
                        if (plan.sprintDetails.isNotBlank()) {
                            Text(
                                text = "स्प्रिंट ड्रिल: ${plan.sprintDetails}",
                                fontSize = 12.sp,
                                color = OnSaffronContainer
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ExercisePill(label = "रनिंग", detail = "${plan.targetRunningKm} KM", color = SaffronPrimary, modifier = Modifier.weight(1f))
                    ExercisePill(label = "पुश-अप्स", detail = "${plan.targetPushups} रेप्स", color = OliveTertiary, modifier = Modifier.weight(1f))
                    ExercisePill(label = "बीम / पुल-अप्स", detail = "${plan.targetPullups} बीम", color = NavySecondary, modifier = Modifier.weight(1f))
                }

                if (plan.instructions.isNotBlank()) {
                    Text(
                        text = "कोचिंग निर्देश: ${plan.instructions}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "दैनिक मानक ड्रिल: 1600m रनिंग + 40 पुश-अप्स + 10 बीम (Pull-ups) + स्ट्रेचिंग",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ExercisePill(
    label: String,
    detail: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = color.copy(alpha = 0.08f),
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = color)
            Text(text = detail, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
fun CadetProgressCard(
    student: StudentProfile,
    todayStatus: String,
    onRecordTest: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("cadet_card_${student.studentId}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(SaffronLight.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = student.fullName.take(1),
                        fontWeight = FontWeight.Bold,
                        color = SaffronDark,
                        fontSize = 16.sp
                    )
                }

                Column {
                    Text(
                        text = student.fullName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "ID: ${student.studentId} • ${student.recruitmentGoal}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (student.batchName.isNotBlank()) {
                        Text(
                            text = "बैच: ${student.batchName}",
                            fontSize = 11.sp,
                            color = OliveTertiary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                val (badgeColor, badgeText) = when (todayStatus) {
                    "PRESENT" -> Pair(OliveTertiary, "उपस्थित")
                    "ABSENT" -> Pair(MaterialTheme.colorScheme.error, "अनुपस्थित")
                    else -> Pair(MaterialTheme.colorScheme.onSurfaceVariant, "अपुष्ट")
                }

                Surface(
                    color = badgeColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = badgeText,
                        color = badgeColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                IconButton(
                    onClick = onRecordTest,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.EditNote,
                        contentDescription = "Record Test",
                        tint = SaffronPrimary
                    )
                }
            }
        }
    }
}

@Composable
fun RunningPerformanceSection(
    allTrainingRecords: List<TrainingRecord>,
    onLaunchStopwatch: () -> Unit,
    onLaunchGroundRecord: () -> Unit,
    onNavigateToMonthly: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "1600m रनिंग व ग्राउंड टेस्ट रिकॉर्ड्स",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                TextButton(onClick = onNavigateToMonthly) {
                    Text("मासिक रिपोर्ट कार्ड", fontSize = 12.sp, color = NavySecondary)
                }
            }

            // Quick stopwatch banner
            Surface(
                color = SaffronPrimary.copy(alpha = 0.1f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onLaunchStopwatch() }
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = null,
                        tint = SaffronPrimary,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "लाइव 1600m बैच टाइमर खोलें",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = SaffronDark
                        )
                        Text(
                            text = "पूरे बैच की एक साथ दौड़ शुरू करें व व्यक्तिगत फिनिश टाइम रिकॉर्ड करें",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = SaffronPrimary
                    )
                }
            }

            // Recent 1600m ground tests
            val recent1600m = allTrainingRecords
                .filter { it.runningDuration.isNotBlank() }
                .sortedByDescending { it.date }
                .take(5)

            if (recent1600m.isEmpty()) {
                Text(
                    text = "अभी तक कोई 1600m रनिंग टेस्ट रिकॉर्ड नहीं हुआ है। नया टेस्ट जोड़ने के लिए 'टेस्ट रिकॉर्ड' बटन दबाएं।",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Text(
                    text = "हालिया ग्राउंड 1600m टाइमिंग:",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )

                recent1600m.forEach { rec ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                RoundedCornerShape(8.dp)
                            )
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "कैडेट ID: ${rec.studentId}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text(text = rec.date, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Surface(
                                color = SaffronContainer,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "1600m: ${rec.runningDuration}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = OnSaffronContainer,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }
                            if (rec.pullups > 0) {
                                Text(text = "${rec.pullups} बीम", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CoachNoticeItem(notice: Notice) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = notice.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                if (notice.isPinned) {
                    Surface(
                        color = GoldAccent.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "पिन सूचना",
                            color = GoldAccent,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }
            }
            Text(
                text = notice.content,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = notice.date,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroundTestRecordDialog(
    students: List<StudentProfile>,
    preselectedStudent: StudentProfile?,
    onDismiss: () -> Unit,
    onSave: (
        studentId: String,
        time1600m: String,
        pushups: Int,
        situps: Int,
        pullups: Int,
        longJumpFeet: Double,
        highJumpFeet: Double,
        shotPutMeters: Double,
        coachNotes: String
    ) -> Unit
) {
    var selectedStudentId by remember { mutableStateOf(preselectedStudent?.studentId ?: students.firstOrNull()?.studentId ?: "") }
    var time1600m by remember { mutableStateOf("05:30") }
    var pushups by remember { mutableStateOf("35") }
    var situps by remember { mutableStateOf("30") }
    var pullups by remember { mutableStateOf("8") }
    var longJumpFeet by remember { mutableStateOf("14.0") }
    var coachNotes by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "ग्राउंड फिजिकल टेस्ट रिकॉर्ड", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (errorMessage != null) {
                    Text(text = errorMessage ?: "", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }

                Text(text = "कैडेट चुनें:", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                // Student selector
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val st = students.firstOrNull { it.studentId == selectedStudentId }
                    Text(
                        text = "${st?.fullName ?: "कैडेट"} (${selectedStudentId})",
                        modifier = Modifier.padding(10.dp),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = time1600m,
                        onValueChange = { time1600m = it },
                        label = { Text("1600m समय") },
                        placeholder = { Text("उदा. 05:20") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = pullups,
                        onValueChange = { pullups = it },
                        label = { Text("बीम (Pull-ups)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = pushups,
                        onValueChange = { pushups = it },
                        label = { Text("पुश-अप्स") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = situps,
                        onValueChange = { situps = it },
                        label = { Text("सिट-अप्स") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = longJumpFeet,
                    onValueChange = { longJumpFeet = it },
                    label = { Text("लॉन्ग जंप (फीट)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = coachNotes,
                    onValueChange = { coachNotes = it },
                    label = { Text("कोच रिमार्क्स (टिप्स/कमेंट्स)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (selectedStudentId.isBlank()) {
                        errorMessage = "कृपया कैडेट चुनें"
                        return@Button
                    }
                    val push = pushups.toIntOrNull() ?: 0
                    val sit = situps.toIntOrNull() ?: 0
                    val pull = pullups.toIntOrNull() ?: 0
                    val lj = longJumpFeet.toDoubleOrNull() ?: 0.0
                    onSave(selectedStudentId, time1600m.trim(), push, sit, pull, lj, 0.0, 0.0, coachNotes.trim())
                },
                colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary)
            ) {
                Text("सुरक्षित करें")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("रद्द करें")
            }
        }
    )
}

@Composable
fun CoachChangePasswordDialog(
    coach: Trainer?,
    isMandatory: Boolean,
    onDismiss: () -> Unit,
    onSavePassword: (newPassword: String) -> Result<Unit>
) {
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = {
            if (!isMandatory) onDismiss()
        },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Security, contentDescription = null, tint = SaffronPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isMandatory) "प्रथम लॉगिन: नया पासवर्ड बनाएं" else "कोच पासवर्ड बदलें",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "कोच ID: ${coach?.coachId ?: "JBA-COACH-001"} (${coach?.name ?: "कोच"})",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (isMandatory) {
                    Text(
                        text = "सुरक्षा नीति: अपने खाते की सुरक्षा हेतु प्रारंभिक पासवर्ड को बदलकर अपनी पसंद का 6+ अक्षरों का नया पासवर्ड बनाएं।",
                        fontSize = 12.sp,
                        color = SaffronDark
                    )
                }

                if (errorMessage != null) {
                    Text(text = errorMessage ?: "", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }
                if (successMessage != null) {
                    Text(text = successMessage ?: "", color = OliveTertiary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedTextField(
                    value = newPassword,
                    onValueChange = {
                        newPassword = it
                        errorMessage = null
                    },
                    label = { Text("नया पासवर्ड (New Password) *") },
                    placeholder = { Text("कम से कम 6 अक्षर") },
                    singleLine = true,
                    visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        IconButton(onClick = { showPassword = !showPassword }) {
                            Icon(
                                imageVector = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = null
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth().testTag("coach_new_password_input"),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = {
                        confirmPassword = it
                        errorMessage = null
                    },
                    label = { Text("पासवर्ड की पुष्टि (Confirm Password) *") },
                    placeholder = { Text("पुनः वही पासवर्ड दर्ज करें") },
                    singleLine = true,
                    visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth().testTag("coach_confirm_password_input"),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val trimmed = newPassword.trim()
                    if (trimmed.length < 6) {
                        errorMessage = "पासवर्ड कम से कम 6 अक्षरों का होना चाहिए!"
                        return@Button
                    }
                    if (trimmed != confirmPassword.trim()) {
                        errorMessage = "दोनों पासवर्ड समान नहीं हैं!"
                        return@Button
                    }

                    val result = onSavePassword(trimmed)
                    if (result.isFailure) {
                        errorMessage = result.exceptionOrNull()?.message ?: "पासवर्ड अपडेट विफल!"
                    } else {
                        successMessage = "पासवर्ड सफलतापूर्वक अपडेट हो गया!"
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary),
                modifier = Modifier.testTag("coach_save_password_submit_btn")
            ) {
                Text("पासवर्ड अपडेट करें", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            if (!isMandatory) {
                TextButton(onClick = onDismiss) {
                    Text("रद्द करें")
                }
            }
        }
    )
}
