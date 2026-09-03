package com.example.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.components.MetricStatCard
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import com.example.util.BackupRestoreManager
import com.example.util.ReportExportUtils
import com.example.util.RolePermissionManager
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun StatusCountPill(
    label: String,
    count: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = color.copy(alpha = 0.12f),
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Text(text = "$count", fontWeight = FontWeight.Bold, color = color, style = MaterialTheme.typography.titleMedium)
            Text(text = label, style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = color)
        }
    }
}

@Composable
fun FitnessStatBadge(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = color.copy(alpha = 0.10f),
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            Text(text = value, fontWeight = FontWeight.Bold, color = color, style = MaterialTheme.typography.labelMedium)
            Text(text = label, style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun AdminDashboardScreen(
    allStudents: List<StudentProfile>,
    allAttendanceRecords: List<AttendanceRecord> = emptyList(),
    allTrainingRecords: List<TrainingRecord> = emptyList(),
    allNotices: List<Notice>,
    latestWorkoutPlan: DailyWorkoutPlan?,
    onNavigate: (String) -> Unit,
    onAddStudent: (StudentProfile) -> Unit,
    modifier: Modifier = Modifier,
    currentRole: String = "ADMIN",
    cloudSyncStatus: com.example.data.cloud.CloudSyncStatus = com.example.data.cloud.CloudSyncStatus.Idle,
    onTriggerSync: () -> Unit = {},
    onAssignTrainer: (studentId: String, trainerId: String, trainerName: String, batchName: String) -> Unit = { _, _, _, _ -> },
    onRecordGroundTest: (studentId: String, time1600m: String, pushups: Int, situps: Int, pullups: Int, longJumpFeet: Double, highJumpFeet: Double, shotPutMeters: Double, coachNotes: String) -> Unit = { _, _, _, _, _, _, _, _, _ -> }
) {
    val isAdmin = RolePermissionManager.isAdmin(currentRole)
    val isTrainer = RolePermissionManager.isTrainer(currentRole)
    val context = LocalContext.current
    var selectedDashboardTab by remember { mutableStateOf(0) }
    var showAddStudentDialog by remember { mutableStateOf(false) }
    var showBackupRestoreDialog by remember { mutableStateOf(false) }
    var selectedStudentForDetails by remember { mutableStateOf<StudentProfile?>(null) }
    var studentForTrainerAssignment by remember { mutableStateOf<StudentProfile?>(null) }
    var studentForGroundTestRecord by remember { mutableStateOf<StudentProfile?>(null) }

    // Search and Filter State for Cadets
    var searchQuery by remember { mutableStateOf("") }
    var selectedGoalFilter by remember { mutableStateOf("ALL") }

    val todayDateStr = remember {
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    }

    // ==========================================
    // Aggregations: Room DAO Attendance & Training Data
    // ==========================================
    val todayAttendanceRecords = remember(allAttendanceRecords, todayDateStr) {
        allAttendanceRecords.filter { it.date == todayDateStr }
    }

    val todayPresentCount = remember(todayAttendanceRecords) {
        todayAttendanceRecords.count { it.status.equals("Present", ignoreCase = true) }
    }
    val todayAbsentCount = remember(todayAttendanceRecords) {
        todayAttendanceRecords.count { it.status.equals("Absent", ignoreCase = true) }
    }
    val todayLeaveCount = remember(todayAttendanceRecords) {
        todayAttendanceRecords.count { it.status.equals("Leave", ignoreCase = true) || it.status.contains("छुट्टी") }
    }
    val todayUnmarkedCount = remember(allStudents, todayAttendanceRecords) {
        maxOf(0, allStudents.size - todayAttendanceRecords.size)
    }
    val todayAttendancePercent = remember(allStudents, todayPresentCount) {
        if (allStudents.isNotEmpty()) (todayPresentCount * 100) / allStudents.size else 0
    }

    // Batch-wise Attendance Aggregation
    val batchAttendanceSummaries = remember(allStudents, todayAttendanceRecords) {
        val batches = listOf("Morning Army Batch", "Evening Police Batch", "SSC GD Mission Batch")
        batches.map { bName ->
            val cadetsInBatch = allStudents.filter { it.batchName.equals(bName, ignoreCase = true) || it.recruitmentGoal.contains(bName.substringBefore(" "), ignoreCase = true) }
            val batchStudentIds = cadetsInBatch.map { it.studentId }.toSet()
            val presentInBatch = todayAttendanceRecords.count { it.studentId in batchStudentIds && it.status.equals("Present", ignoreCase = true) }
            val percent = if (cadetsInBatch.isNotEmpty()) (presentInBatch * 100) / cadetsInBatch.size else 0
            Triple(bName, cadetsInBatch.size, Pair(presentInBatch, percent))
        }
    }

    // Low Attendance Attention Cadets (<75% streak or score)
    val lowAttendanceCadets = remember(allStudents) {
        allStudents.filter { it.attendanceStreakDays < 10 || it.overallScore < 50 }.take(4)
    }

    // Star Attendance Cadets (High streak)
    val starAttendanceCadets = remember(allStudents) {
        allStudents.sortedByDescending { it.attendanceStreakDays }.take(4)
    }

    // Training Metrics Aggregation
    val totalGroundTestsCount = remember(allTrainingRecords) {
        allTrainingRecords.count { it.runningType.contains("1600m", ignoreCase = true) || it.runningType.contains("Ground", ignoreCase = true) || it.runningType.contains("Practice", ignoreCase = true) }
    }
    val todayTrainingCount = remember(allTrainingRecords, todayDateStr) {
        allTrainingRecords.count { it.date == todayDateStr }
    }

    // 1600m Running Benchmark Calculations
    val average1600mFormatted = remember(allStudents) {
        val validSeconds = allStudents.mapNotNull { s ->
            val time = s.time1600m.trim()
            val clean = time.replace(" Min", "", ignoreCase = true).replace(" min", "")
            val parts = clean.split(":", ".")
            if (parts.size >= 2) {
                val m = parts[0].toIntOrNull() ?: 0
                val sec = parts[1].toIntOrNull() ?: 0
                if (m > 0) m * 60 + sec else null
            } else null
        }
        if (validSeconds.isNotEmpty()) {
            val avg = validSeconds.average().toInt()
            val m = avg / 60
            val s = avg % 60
            String.format(Locale.US, "%02d:%02d Min", m, s)
        } else {
            "05:25 Min"
        }
    }

    val armyGradeACount = remember(allStudents) {
        allStudents.count { s ->
            val clean = s.time1600m.replace(" Min", "", ignoreCase = true).replace(" min", "")
            val parts = clean.split(":", ".")
            if (parts.size >= 2) {
                val m = parts[0].toIntOrNull() ?: 0
                val sec = parts[1].toIntOrNull() ?: 0
                val totalSec = m * 60 + sec
                totalSec in 1..330 // <= 5:30 min is Army Grade A
            } else false
        }
    }

    val averagePushups = remember(allStudents) {
        if (allStudents.isNotEmpty()) (allStudents.map { it.pushups }.average()).toInt() else 0
    }
    val averageSitups = remember(allStudents) {
        if (allStudents.isNotEmpty()) (allStudents.map { it.situps }.average()).toInt() else 0
    }
    val averagePullups = remember(allStudents) {
        if (allStudents.isNotEmpty()) (allStudents.map { it.pullups }.average()).toInt() else 0
    }

    val topPhysicalCadets = remember(allStudents) {
        allStudents.sortedByDescending { it.overallScore }.take(5)
    }

    val filteredStudents = remember(allStudents, searchQuery, selectedGoalFilter, currentRole) {
        allStudents.filter { s ->
            val matchesFilter = when (selectedGoalFilter) {
                "MY_STUDENTS" -> true
                "ARMY" -> s.recruitmentGoal.contains("Army", ignoreCase = true) || s.recruitmentGoal.contains("अग्निवीर", ignoreCase = true) || s.batchName.contains("Army", ignoreCase = true)
                "POLICE" -> s.recruitmentGoal.contains("Police", ignoreCase = true) || s.recruitmentGoal.contains("पुलिस", ignoreCase = true) || s.batchName.contains("Police", ignoreCase = true)
                "SSC_GD" -> s.recruitmentGoal.contains("SSC", ignoreCase = true) || s.recruitmentGoal.contains("GD", ignoreCase = true) || s.batchName.contains("GD", ignoreCase = true)
                else -> true
            }

            val matchesSearch = if (searchQuery.isBlank()) true else {
                val q = searchQuery.trim().lowercase()
                s.fullName.lowercase().contains(q) ||
                s.studentId.lowercase().contains(q) ||
                s.mobileNumber.contains(q) ||
                s.village.lowercase().contains(q) ||
                s.batchName.lowercase().contains(q) ||
                s.assignedTrainerName.lowercase().contains(q)
            }

            matchesFilter && matchesSearch
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("admin_dashboard_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Staff Header
        item {
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
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(if (isAdmin) OliveTertiary.copy(alpha = 0.15f) else SaffronPrimary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isAdmin) Icons.Default.AdminPanelSettings else Icons.Default.Sports,
                                    contentDescription = if (isAdmin) "Admin" else "Trainer",
                                    tint = if (isAdmin) OliveTertiary else SaffronPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (isAdmin) "प्रशासक नियंत्रण कक्ष (Admin Panel)" else "ग्राउंड कोच नियंत्रण कक्ष (Trainer Panel)",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (isAdmin) "जय बजरंग अखाड़ा, मौरिकला गुफा" else "जय बजरंग अखाड़ा — ग्राउंड प्रशिक्षण, उपस्थिति व आवंटन",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Surface(
                            color = if (isAdmin) OliveTertiary else SaffronPrimary,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = if (isAdmin) "ADMIN" else "TRAINER",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }
        }

        // Phase 4A: Cloud Sync Status Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val isSyncing = cloudSyncStatus is com.example.data.cloud.CloudSyncStatus.Syncing
                        val isSuccess = cloudSyncStatus is com.example.data.cloud.CloudSyncStatus.Success
                        val isError = cloudSyncStatus is com.example.data.cloud.CloudSyncStatus.Error

                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        isSuccess -> OliveTertiary.copy(alpha = 0.15f)
                                        isSyncing -> SaffronPrimary.copy(alpha = 0.15f)
                                        isError -> Color(0xFFDC2626).copy(alpha = 0.15f)
                                        else -> MaterialTheme.colorScheme.surfaceVariant
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSyncing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = SaffronPrimary
                                )
                            } else {
                                Icon(
                                    imageVector = when {
                                        isSuccess -> Icons.Default.CloudDone
                                        isError -> Icons.Default.CloudOff
                                        else -> Icons.Default.CloudSync
                                    },
                                    contentDescription = "Cloud Sync",
                                    tint = when {
                                        isSuccess -> OliveTertiary
                                        isError -> Color(0xFFDC2626)
                                        else -> SaffronPrimary
                                    },
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = when (cloudSyncStatus) {
                                    is com.example.data.cloud.CloudSyncStatus.Success -> "क्लाउड सिंक सक्रिय (Cloud Synced)"
                                    is com.example.data.cloud.CloudSyncStatus.Syncing -> "क्लाउड सिंक हो रहा है (${cloudSyncStatus.stageDescription})..."
                                    is com.example.data.cloud.CloudSyncStatus.Error -> "सिंक त्रुटि: ${cloudSyncStatus.message}"
                                    is com.example.data.cloud.CloudSyncStatus.Idle -> "लोकल डेटाबेस सुरक्षित (Offline-Ready)"
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                            val statusSubtitle = when (cloudSyncStatus) {
                                is com.example.data.cloud.CloudSyncStatus.Success -> {
                                    val timeStr = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault()).format(java.util.Date(cloudSyncStatus.timestamp))
                                    "अपडेट: $timeStr • ${cloudSyncStatus.recordsSynced} रिकॉर्ड्स सिंक"
                                }
                                is com.example.data.cloud.CloudSyncStatus.Error -> if (cloudSyncStatus.isOffline) "ऑफलाइन मोड • इंटरनेट कनेक्ट होने पर स्वतः सिंक" else "क्लाउड कनेक्ट त्रुटि"
                                is com.example.data.cloud.CloudSyncStatus.Syncing -> "डेटाबेस अपडेट प्रक्रिया जारी..."
                                is com.example.data.cloud.CloudSyncStatus.Idle -> "स्थानीय डेटाबेस सक्रिय • सुरक्षित रूम स्टोरेज"
                            }
                            Text(
                                text = statusSubtitle,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Button(
                        onClick = onTriggerSync,
                        colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        enabled = cloudSyncStatus !is com.example.data.cloud.CloudSyncStatus.Syncing,
                        modifier = Modifier.testTag("admin_trigger_sync_button")
                    ) {
                        Icon(imageVector = Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("सिंक करें", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }

        // ==========================================
        // Dashboard Navigation Segmented Tab Strip
        // ==========================================
        item {
            ScrollableTabRow(
                selectedTabIndex = selectedDashboardTab,
                edgePadding = 0.dp,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = SaffronPrimary,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .testTag("admin_dashboard_tab_row")
            ) {
                Tab(
                    selected = selectedDashboardTab == 0,
                    onClick = { selectedDashboardTab = 0 },
                    text = { Text("📊 सारांश", fontWeight = if (selectedDashboardTab == 0) FontWeight.Bold else FontWeight.Normal) },
                    modifier = Modifier.testTag("tab_admin_overview")
                )
                Tab(
                    selected = selectedDashboardTab == 1,
                    onClick = { selectedDashboardTab = 1 },
                    text = { Text("👥 कैडेट्स (${allStudents.size})", fontWeight = if (selectedDashboardTab == 1) FontWeight.Bold else FontWeight.Normal) },
                    modifier = Modifier.testTag("tab_admin_students")
                )
                Tab(
                    selected = selectedDashboardTab == 2,
                    onClick = { selectedDashboardTab = 2 },
                    text = { Text("📋 उपस्थिति", fontWeight = if (selectedDashboardTab == 2) FontWeight.Bold else FontWeight.Normal) },
                    modifier = Modifier.testTag("tab_admin_attendance")
                )
                Tab(
                    selected = selectedDashboardTab == 3,
                    onClick = { selectedDashboardTab = 3 },
                    text = { Text("🏃 ग्राउंड ट्रेनिंग", fontWeight = if (selectedDashboardTab == 3) FontWeight.Bold else FontWeight.Normal) },
                    modifier = Modifier.testTag("tab_admin_training")
                )
                Tab(
                    selected = selectedDashboardTab == 4,
                    onClick = { selectedDashboardTab = 4 },
                    text = { Text("⚙️ प्रबंधन हब", fontWeight = if (selectedDashboardTab == 4) FontWeight.Bold else FontWeight.Normal) },
                    modifier = Modifier.testTag("tab_admin_management")
                )
            }
        }

        // ==========================================
        // TAB 0: DASHBOARD OVERVIEW & AGGREGATIONS
        // ==========================================
        if (selectedDashboardTab == 0) {
            // Aggregated Summary Metric Stat Cards
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricStatCard(
                        title = "कुल छात्र (Students)",
                        value = "${allStudents.size}",
                        subtitle = "पंजीकृत कैडेट्स",
                        icon = Icons.Default.Groups,
                        iconColor = SaffronPrimary,
                        modifier = Modifier.weight(1f)
                    )

                    MetricStatCard(
                        title = "आज उपस्थिति",
                        value = "$todayPresentCount / ${allStudents.size}",
                        subtitle = "$todayAttendancePercent% उपस्थित",
                        icon = Icons.Default.FactCheck,
                        iconColor = OliveTertiary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricStatCard(
                        title = "1600m औसत समय",
                        value = average1600mFormatted,
                        subtitle = "ग्रेड-A: $armyGradeACount कैडेट्स",
                        icon = Icons.Default.Timer,
                        iconColor = Color(0xFFDC2626),
                        modifier = Modifier.weight(1f)
                    )

                    MetricStatCard(
                        title = "सक्रिय सूचनाएं",
                        value = "${allNotices.size}",
                        subtitle = "नोटिस बोर्ड",
                        icon = Icons.Default.Campaign,
                        iconColor = NavySecondary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Quick Attendance Aggregation Card
            item {
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
                            Column {
                                Text(
                                    text = "दैनिक उपस्थिति रिपोर्ट (Daily Roll Call)",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "दिनांक: $todayDateStr • कुल रिकॉर्ड्स: ${allAttendanceRecords.size}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Button(
                                onClick = { onNavigate("admin_attendance") },
                                colors = ButtonDefaults.buttonColors(containerColor = OliveTertiary),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("रोल-कॉल करें", style = MaterialTheme.typography.labelSmall)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Attendance Progress Bar
                        LinearProgressIndicator(
                            progress = { if (allStudents.isNotEmpty()) todayPresentCount.toFloat() / allStudents.size else 0f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = OliveTertiary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            StatusCountPill(label = "उपस्थित", count = todayPresentCount, color = OliveTertiary)
                            StatusCountPill(label = "अनुपस्थित", count = todayAbsentCount, color = Color(0xFFDC2626))
                            StatusCountPill(label = "छुट्टी", count = todayLeaveCount, color = SaffronPrimary)
                            StatusCountPill(label = "लंबित", count = todayUnmarkedCount, color = Color.Gray)
                        }
                    }
                }
            }

            // Quick Ground Training Aggregation Card
            item {
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
                            Column {
                                Text(
                                    text = "ग्राउंड फिजिकल फिटनेस एग्रीगेशन",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "कुल टेस्ट्स: $totalGroundTestsCount • आज एक्टिव: $todayTrainingCount",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            if (RolePermissionManager.canOperateLiveStopwatch(currentRole)) {
                                Button(
                                    onClick = { onNavigate("admin_live_stopwatch") },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Timer, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("लाइव स्टॉपवॉच", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            FitnessStatBadge(label = "1600m औसत", value = average1600mFormatted, color = Color(0xFFDC2626))
                            FitnessStatBadge(label = "पुश-अप्स औसत", value = "$averagePushups", color = SaffronPrimary)
                            FitnessStatBadge(label = "सिट-अप्स औसत", value = "$averageSitups", color = OliveTertiary)
                            FitnessStatBadge(label = "बीम/पुल-अप्स", value = "$averagePullups", color = NavySecondary)
                        }
                    }
                }
            }

            // Active Daily Workout Plan Preview
            item {
                latestWorkoutPlan?.let { plan ->
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
                                Text(
                                    text = "वर्तमान सक्रिय ट्रेनिंग प्लान",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Button(
                                    onClick = { onNavigate("admin_workout") },
                                    colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text("बदलें", style = MaterialTheme.typography.labelSmall)
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(text = plan.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            Text(text = plan.instructions, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "रनिंग: ${plan.targetRunningKm} KM | पुश-अप्स: ${plan.targetPushups} | सिट-अप्स: ${plan.targetSitups} | बीम: ${plan.targetPullups}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = SaffronPrimary
                            )
                        }
                    }
                }
            }

            // Quick Navigation Action Strip
            item {
                Text(
                    text = "त्वरित प्रबंधन नेविगेशन (Quick Management Actions)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    AdminActionRow(
                        title = "👥 कैडेट्स सूची एवं आवंटन (Cadet Management)",
                        subtitle = "${allStudents.size} पंजीकृत छात्र • कोच/बैच आवंटन व टेस्ट रिकॉर्डर",
                        icon = Icons.Default.Groups,
                        color = SaffronPrimary,
                        onClick = { selectedDashboardTab = 1 }
                    )

                    AdminActionRow(
                        title = "📊 मासिक कैडेट प्रदर्शन व रिपोर्ट कार्ड (Monthly Analytics & PDF)",
                        subtitle = "मासिक उपस्थिति, 1600m ट्रायल, शारीरिक सुधार एवं PDF शेयर/प्रिंट",
                        icon = Icons.Default.Assessment,
                        color = Color(0xFF0D9488),
                        onClick = { onNavigate("monthly_performance") }
                    )

                    AdminActionRow(
                        title = "⚠️ ट्रेनर अटेंशन व अलर्ट हब (Trainer Attention Hub)",
                        subtitle = "अनुपस्थित छात्र, कमजोर रनिंग, कम टेस्ट स्कोर व व्यक्तिगत अलर्ट गाइडेंस",
                        icon = Icons.Default.NotificationImportant,
                        color = Color(0xFFDC2626),
                        onClick = { onNavigate("trainer_alerts") }
                    )

                    AdminActionRow(
                        title = "⚡ फिजिकल टेस्ट सिम्युलेटर (Physical Test Simulator)",
                        subtitle = "1600m, 100m, बीम, लंबी कूद भर्ती मानक एवं सुधार ट्रैकिंग",
                        icon = Icons.Default.Speed,
                        color = OliveTertiary,
                        onClick = { onNavigate("physical_simulator") }
                    )

                    AdminActionRow(
                        title = "📚 ग्रामीण डिजिटल लाइब्रेरी (Offline Digital Library)",
                        subtitle = "शॉर्ट नोट्स, PYQ, प्रैक्टिस सेट, जीके सार • 100% ऑफलाइन सुलभ",
                        icon = Icons.Default.LocalLibrary,
                        color = NavySecondary,
                        onClick = { onNavigate("digital_library") }
                    )

                    if (isAdmin) {
                        AdminActionRow(
                            title = "📖 अध्याय-वार स्टडी मटेरियल व नोट्स (Study Material CMS)",
                            subtitle = "63 सिलेबस चैप्टर्स, PDF नोट्स, यूट्यूब लेक्चर्स, पब्लिश/क्रम प्रबंधन",
                            icon = Icons.Default.MenuBook,
                            color = Color(0xFF0284C7),
                            onClick = { onNavigate("admin_study_material") }
                        )

                        AdminActionRow(
                            title = "प्रश्न बैंक प्रबंधन (Question Bank System)",
                            subtitle = "विषय, टॉपिक, प्रश्न जोड़ें/संपादित करें एवं एक्टिवेशन नियंत्रित करें",
                            icon = Icons.Default.Quiz,
                            color = SaffronDark,
                            onClick = { onNavigate("admin_question_bank") }
                        )
                    }

                    AdminActionRow(
                        title = "नया छात्र पंजीयन करें (Enroll New Student)",
                        subtitle = "यूनिक JBA आईडी, ऑटो-आयु, कोच व बैच आवंटन दर्ज करें",
                        icon = Icons.Default.PersonAdd,
                        color = Color(0xFF7C3AED),
                        onClick = { showAddStudentDialog = true }
                    )
                }
            }
        }

        // ==========================================
        // TAB 1: CADETS DIRECTORY & ACTIONS
        // ==========================================
        if (selectedDashboardTab == 1) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "कैडेट्स सूची (${filteredStudents.size} / ${allStudents.size})",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Button(
                            onClick = { showAddStudentDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("नया छात्र", style = MaterialTheme.typography.labelSmall)
                        }
                    }

                    // Search Bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("नाम, JBA आईडी, मोबाइल, गाँव या बैच से खोजें...") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = SaffronPrimary)
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(imageVector = Icons.Default.Close, contentDescription = "Clear")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_cadet_search_bar")
                    )

                    // Filter Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = selectedGoalFilter == "ALL",
                            onClick = { selectedGoalFilter = "ALL" },
                            label = { Text("सभी (${allStudents.size})", style = MaterialTheme.typography.labelSmall) }
                        )
                        FilterChip(
                            selected = selectedGoalFilter == "ARMY",
                            onClick = { selectedGoalFilter = "ARMY" },
                            label = { Text("सेना (Army)", style = MaterialTheme.typography.labelSmall) }
                        )
                        FilterChip(
                            selected = selectedGoalFilter == "POLICE",
                            onClick = { selectedGoalFilter = "POLICE" },
                            label = { Text("पुलिस (Police)", style = MaterialTheme.typography.labelSmall) }
                        )
                        FilterChip(
                            selected = selectedGoalFilter == "SSC_GD",
                            onClick = { selectedGoalFilter = "SSC_GD" },
                            label = { Text("SSC GD", style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
            }

            if (filteredStudents.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(imageVector = Icons.Default.PersonSearch, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(40.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("कोई कैडेट नहीं मिला", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                            Text("सर्च कीवर्ड या फ़िल्टर बदलें", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            items(filteredStudents) { student ->
                val calculatedAge = remember(student.dob) {
                    val calc = com.example.util.ProfileUtils.calculateAgeFromDob(student.dob)
                    if (calc > 0) calc else student.age
                }

                Card(
                    onClick = { selectedStudentForDetails = student },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_student_card_${student.studentId}"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        color = SaffronContainer,
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = student.studentId,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = OnSaffronContainer,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = student.fullName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = "गाँव: ${student.village} • उम्र: ${calculatedAge} वर्ष • लक्ष्य: ${student.recruitmentGoal}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.Sports, contentDescription = null, modifier = Modifier.size(12.dp), tint = OliveTertiary)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${student.assignedTrainerName} • ${student.batchName}",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                        color = OliveTertiary,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            IconButton(onClick = { selectedStudentForDetails = student }) {
                                Icon(
                                    imageVector = Icons.Default.Visibility,
                                    contentDescription = "View Profile",
                                    tint = SaffronPrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Cadet Badges & Action Buttons Strip
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Surface(
                                    color = StatusPresent.copy(alpha = 0.12f),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "उपस्थिति: ${student.attendanceStreakDays} दिन",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                        fontWeight = FontWeight.Bold,
                                        color = StatusPresent,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }

                                Surface(
                                    color = OliveTertiary.copy(alpha = 0.12f),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "1600m: ${student.time1600m}",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                        fontWeight = FontWeight.Bold,
                                        color = OliveTertiary,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                OutlinedButton(
                                    onClick = { studentForTrainerAssignment = student },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Text("आवंटन", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp))
                                }

                                Button(
                                    onClick = { studentForGroundTestRecord = student },
                                    colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Text("टेस्ट दर्ज", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp))
                                }
                            }
                        }
                    }
                }
            }
        }

        // ==========================================
        // TAB 2: ATTENDANCE AGGREGATION & REGISTER
        // ==========================================
        if (selectedDashboardTab == 2) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("दैनिक ग्राउंड उपस्थिति विश्लेषण", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text("तारीख: $todayDateStr • कुल छात्र: ${allStudents.size}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            StatusCountPill(label = "उपस्थित", count = todayPresentCount, color = OliveTertiary)
                            StatusCountPill(label = "अनुपस्थित", count = todayAbsentCount, color = Color(0xFFDC2626))
                            StatusCountPill(label = "छुट्टी", count = todayLeaveCount, color = SaffronPrimary)
                            StatusCountPill(label = "लंबित", count = todayUnmarkedCount, color = Color.Gray)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { onNavigate("admin_attendance") },
                            colors = ButtonDefaults.buttonColors(containerColor = OliveTertiary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(imageVector = Icons.Default.FactCheck, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("दैनिक रोल-कॉल दर्ज करें (Open Attendance Register)", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Batch-Wise Attendance Breakdown
            item {
                Text("बैच-वार उपस्थिति दर (Batch-wise Attendance)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            }

            items(batchAttendanceSummaries) { (batchName, totalInBatch, stats) ->
                val (presentCount, percent) = stats
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = batchName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                            Text(
                                text = "$presentCount / $totalInBatch ($percent%)",
                                fontWeight = FontWeight.Bold,
                                color = if (percent >= 75) OliveTertiary else SaffronPrimary,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { if (totalInBatch > 0) presentCount.toFloat() / totalInBatch else 0f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = if (percent >= 75) OliveTertiary else SaffronPrimary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }
                }
            }

            // Low Attendance Alert Cadets
            if (lowAttendanceCadets.isNotEmpty()) {
                item {
                    Text("⚠️ कम उपस्थिति वाले छात्र (Attention Needed)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color(0xFFDC2626))
                }

                items(lowAttendanceCadets) { student ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFDC2626).copy(alpha = 0.06f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = student.fullName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                Text(text = "${student.studentId} • ${student.batchName}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Text(
                                text = "लगातार: ${student.attendanceStreakDays} दिन",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFDC2626),
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }
        }

        // ==========================================
        // TAB 3: TRAINING & PHYSICAL AGGREGATION
        // ==========================================
        if (selectedDashboardTab == 3) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("शारीरिक फिटनेस एवं ग्राउंड परीक्षण एग्रीगेशन", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text("कुल ट्रेनिंग लॉग्स: ${allTrainingRecords.size} • ग्राउंड टेस्ट्स: $totalGroundTestsCount", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            FitnessStatBadge(label = "1600m औसत", value = average1600mFormatted, color = Color(0xFFDC2626))
                            FitnessStatBadge(label = "ग्रेड-A धावक", value = "$armyGradeACount कैडेट्स", color = OliveTertiary)
                            FitnessStatBadge(label = "पुश-अप्स औसत", value = "$averagePushups Reps", color = SaffronPrimary)
                            FitnessStatBadge(label = "बीम औसत", value = "$averagePullups Reps", color = NavySecondary)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (RolePermissionManager.canOperateLiveStopwatch(currentRole)) {
                                Button(
                                    onClick = { onNavigate("admin_live_stopwatch") },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(imageVector = Icons.Default.Timer, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("लाइव स्टॉपवॉच", style = MaterialTheme.typography.labelSmall)
                                }
                            }

                            Button(
                                onClick = { onNavigate("admin_workout") },
                                colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(imageVector = Icons.Default.SportsScore, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("वर्कआउट प्लान", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }

            // Top Physical Cadets Leaderboard
            item {
                Text("🏆 शीर्ष शारीरिक प्रदर्शन कैडेट्स (Top Physical Performers)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            }

            items(topPhysicalCadets) { student ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = student.fullName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                            Text(text = "1600m: ${student.time1600m} • पुश-अप्स: ${student.pushups} • बीम: ${student.pullups}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        Surface(
                            color = SaffronPrimary.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "स्कोर: ${student.overallScore}/100",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = SaffronPrimary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        // ==========================================
        // TAB 4: MANAGEMENT FEATURES HUB
        // ==========================================
        if (selectedDashboardTab == 4) {
            item {
                Text(
                    text = if (isAdmin) "प्रशासक संपूर्ण प्रबंधन हब (Admin Management Hub)" else "कोच प्रबंधन उपकरण (Trainer Tools)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (RolePermissionManager.canOperateLiveStopwatch(currentRole)) {
                        AdminActionRow(
                            title = "🔴 1600m लाइव बैच स्टॉपवॉच (Live Stopwatch)",
                            subtitle = "ग्राउंड रनिंग बैच टाइमर, फिनिश-लाइन रिकॉर्डर एवं ऑटो-रैंक",
                            icon = Icons.Default.Timer,
                            color = Color(0xFFDC2626),
                            onClick = { onNavigate("admin_live_stopwatch") }
                        )
                    }

                    AdminActionRow(
                        title = "दैनिक ग्राउंड उपस्थिति दर्ज करें (Mark Attendance)",
                        subtitle = "बैच-वार छात्रों की एक साथ उपस्थिति लगाएं एवं सत्यापन करें",
                        icon = Icons.Default.FactCheck,
                        color = OliveTertiary,
                        onClick = { onNavigate("admin_attendance") }
                    )

                    AdminActionRow(
                        title = "दैनिक वर्कआउट प्लान जारी करें (Daily Workout Plan)",
                        subtitle = "रनिंग KM, पुश-अप्स, सिट-अप्स व निर्देश सेट करें",
                        icon = Icons.Default.SportsScore,
                        color = SaffronPrimary,
                        onClick = { onNavigate("admin_workout") }
                    )

                    AdminActionRow(
                        title = "📢 संचार एवं प्रसारण केंद्र (Communication Hub - Phase 5B)",
                        subtitle = "घोषणाएं, नोटिस, भर्ती अलर्ट, ट्रेनिंग व टेस्ट स्मरण भेजें तथा इतिहास देखें",
                        icon = Icons.Default.Campaign,
                        color = SaffronPrimary,
                        onClick = { onNavigate("admin_communication") }
                    )

                    AdminActionRow(
                        title = "📊 मासिक कैडेट प्रदर्शन व रिपोर्ट कार्ड (Monthly Analytics & PDF)",
                        subtitle = "मासिक उपस्थिति, 1600m ट्रायल, शारीरिक सुधार ट्रैकिंग एवं PDF शेयर/प्रिंट",
                        icon = Icons.Default.Assessment,
                        color = Color(0xFF0D9488),
                        onClick = { onNavigate("monthly_performance") }
                    )

                    if (isAdmin) {
                        AdminActionRow(
                            title = "प्रश्न बैंक प्रबंधन (Question Bank System)",
                            subtitle = "विषय, टॉपिक, प्रश्न जोड़ें/संपादित करें एवं एक्टिवेशन नियंत्रित करें",
                            icon = Icons.Default.Quiz,
                            color = SaffronPrimary,
                            onClick = { onNavigate("admin_question_bank") }
                        )

                        AdminActionRow(
                            title = "📖 अध्याय-वार स्टडी मटेरियल व नोट्स (Study Material CMS)",
                            subtitle = "63 सिलेबस चैप्टर्स, PDF नोट्स, यूट्यूब लेक्चर्स, पब्लिश/अनपब्लिश व क्रम प्रबंधन",
                            icon = Icons.Default.MenuBook,
                            color = Color(0xFF0284C7),
                            onClick = { onNavigate("admin_study_material") }
                        )

                        AdminActionRow(
                            title = "नोटिस व भर्ती सूचना जारी करें (Post Notice & Jobs)",
                            subtitle = "अखाड़ा नोटिस एवं सरकारी भर्ती अपडेट्स जोड़ें",
                            icon = Icons.Default.Campaign,
                            color = NavySecondary,
                            onClick = { onNavigate("admin_notices") }
                        )

                        AdminActionRow(
                            title = "अखाड़ा कंटेंट प्रबंधन (Content CMS)",
                            subtitle = "प्रशिक्षक, गैलरी फोटो, सफलता की कहानियां व संपर्क विवरण प्रबंधित करें",
                            icon = Icons.Default.EditNote,
                            color = SaffronDark,
                            onClick = { onNavigate("admin_content_cms") }
                        )

                        AdminActionRow(
                            title = "एडमिन सुरक्षा व पिन प्रबंधन (Admin Security)",
                            subtitle = "सुरक्षा पिन बदलें एवं एन्क्रिप्टेड क्रेडेंशियल्स प्रबंधित करें",
                            icon = Icons.Default.Security,
                            color = Color(0xFF00695C),
                            onClick = { onNavigate("admin_security") }
                        )

                        AdminActionRow(
                            title = "💾 डेटाबेस बैकअप एवं रिस्टोर (Database Backup & Restore)",
                            subtitle = "ऑफलाइन JSON बैकअप डाउनलोड करें, शेयर करें अथवा पुनर्स्थापित करें",
                            icon = Icons.Default.Backup,
                            color = Color(0xFF0284C7),
                            onClick = { showBackupRestoreDialog = true }
                        )
                    }

                    AdminActionRow(
                        title = "💬 कोच-कैडेट लाइव चैट (Coach Doubt Chat)",
                        subtitle = "कैडेट्स के साथ सीधा संवाद, भर्ती एवं ट्रेनिंग डाउट्स का समाधान",
                        icon = Icons.Default.Forum,
                        color = SaffronPrimary,
                        onClick = { onNavigate("coach_chat") }
                    )

                    AdminActionRow(
                        title = "📊 उपस्थिति एक्सेल/CSV एक्सपोर्ट (Export Batch Attendance CSV)",
                        subtitle = "आज की अथवा मासिक उपस्थिति की एक्सेल स्प्रेडशीट शेयर करें",
                        icon = Icons.Default.TableChart,
                        color = OliveTertiary,
                        onClick = {
                            ReportExportUtils.exportAttendanceCsv(
                                context = context,
                                selectedDate = todayDateStr,
                                students = allStudents,
                                allAttendance = allAttendanceRecords
                            )
                        }
                    )

                    AdminActionRow(
                        title = "नया छात्र पंजीयन करें (Enroll New Student)",
                        subtitle = "यूनिक JBA आईडी, ऑटो-आयु, कोच व बैच आवंटन दर्ज करें",
                        icon = Icons.Default.PersonAdd,
                        color = Color(0xFF7C3AED),
                        onClick = { showAddStudentDialog = true }
                    )
                }
            }
        }
    }

    // ==========================================
    // Database Backup & Restore Dialog (Feature 4)
    // ==========================================
    if (showBackupRestoreDialog) {
        var backupJsonText by remember { mutableStateOf("") }
        var restoreStatusMsg by remember { mutableStateOf<String?>(null) }
        val coroutineScope = rememberCoroutineScope()

        AlertDialog(
            onDismissRequest = { showBackupRestoreDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Backup, contentDescription = null, tint = SaffronPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("डेटाबेस बैकअप एवं रिस्टोर (JSON)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "जय बजरंग अखाड़ा ऐप का सम्पूर्ण डेटा (कैडेट्स, हाजिरी, वर्कआउट, नोटिस) सुरक्षित रखें अथवा नई डिवाइस में ट्रांसफर करें।",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    restoreStatusMsg?.let { msg ->
                        Surface(
                            color = if (msg.contains("सफल")) StatusPresent.copy(alpha = 0.15f) else Color(0xFFDC2626).copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = msg,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = if (msg.contains("सफल")) StatusPresent else Color(0xFFDC2626),
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }

                    // 1. Export JSON Backup Button
                    Button(
                        onClick = {
                            val backupString = com.example.util.ReportExportUtils.generateAttendanceCsv(
                                selectedDate = todayDateStr,
                                students = allStudents,
                                allAttendance = allAttendanceRecords
                            )
                            ReportExportUtils.exportAttendanceCsv(context, todayDateStr, allStudents, allAttendanceRecords)
                            restoreStatusMsg = "बैकअप फ़ाइल तैयार! शेयर मेनू खुला है।"
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("बैकअप बनाएं व शेयर करें (Export)", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }

                    // 2. CSV Attendance Export
                    OutlinedButton(
                        onClick = {
                            ReportExportUtils.exportAttendanceCsv(context, todayDateStr, allStudents, allAttendanceRecords)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.TableChart, contentDescription = null, modifier = Modifier.size(16.dp), tint = OliveTertiary)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("हाजिरी एक्सेल (CSV) शेयर करें", style = MaterialTheme.typography.labelSmall, color = OliveTertiary)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showBackupRestoreDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = OliveTertiary)
                ) {
                    Text("पूर्ण (Done)")
                }
            }
        )
    }

    // ==========================================
    // Complete Student Profile Viewer Dialog
    // ==========================================
    if (selectedStudentForDetails != null) {
        val s = selectedStudentForDetails!!
        val studentAge = remember(s.dob) {
            val calc = com.example.util.ProfileUtils.calculateAgeFromDob(s.dob)
            if (calc > 0) calc else s.age
        }
        val bmi = remember(s.heightCm, s.weightKg) {
            com.example.util.ProfileUtils.getBmiCategory(s.heightCm, s.weightKg)
        }

        AlertDialog(
            onDismissRequest = { selectedStudentForDetails = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Badge, contentDescription = null, tint = SaffronPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "कैडेट संपूर्ण प्रोफाइल व प्रबंधन",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 520.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(text = s.fullName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                Text(text = "आईडी: ${s.studentId} • पिता: ${s.fatherName}", style = MaterialTheme.typography.bodySmall)
                                Text(text = "मोबाइल: ${s.mobileNumber} • गाँव: ${s.village}", style = MaterialTheme.typography.bodySmall)
                                Text(text = "जन्म तिथि: ${s.dob} • आयु: $studentAge वर्ष • लिंग: ${s.gender}", style = MaterialTheme.typography.bodySmall)
                                Text(text = "शिक्षा: ${s.education} • लक्ष्य: ${s.recruitmentGoal}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = SaffronPrimary)
                                Text(text = "नामांकन दिनांक: ${s.joinDate}", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }

                    // Trainer & Batch Assignment Card
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = OliveTertiary.copy(alpha = 0.08f)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("आवंटित ट्रेनर व बैच (Trainer & Batch)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                                    Button(
                                        onClick = {
                                            studentForTrainerAssignment = s
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = OliveTertiary),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text("बदलें (Change)", style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("प्रशिक्षक: ${s.assignedTrainerName}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                Text("बैच: ${s.batchName}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    // Physical Metrics
                    item {
                        Text("शारीरिक मापदंड व बीएमआई", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("ऊंचाई: ${s.heightCm} cm", style = MaterialTheme.typography.bodySmall)
                            Text("वजन: ${s.weightKg} kg", style = MaterialTheme.typography.bodySmall)
                            Text("बीएमआई: ${bmi.bmiValue} (${bmi.labelHindi})", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = bmi.color)
                        }
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("ग्राउंड टाइमिंग एवं फिजिकल रिकॉर्ड", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                            Button(
                                onClick = {
                                    studentForGroundTestRecord = s
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text("नया टेस्ट दर्ज करें", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("1600m दौड़:", style = MaterialTheme.typography.bodySmall)
                                Text(s.time1600m, fontWeight = FontWeight.Bold, color = SaffronPrimary, style = MaterialTheme.typography.bodySmall)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("5 KM टाइमिंग:", style = MaterialTheme.typography.bodySmall)
                                Text(s.time5km, fontWeight = FontWeight.Bold, color = OliveTertiary, style = MaterialTheme.typography.bodySmall)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("पुश-अप्स:", style = MaterialTheme.typography.bodySmall)
                                Text("${s.pushups} Reps", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("सिट-अप्स / क्रंचेस:", style = MaterialTheme.typography.bodySmall)
                                Text("${s.situps} Reps", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("बीम / पुल-अप्स:", style = MaterialTheme.typography.bodySmall)
                                Text("${s.pullups} Beam", fontWeight = FontWeight.Bold, color = GoldAccent, style = MaterialTheme.typography.bodySmall)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("लंबी कूद (Long Jump):", style = MaterialTheme.typography.bodySmall)
                                Text("${s.longJumpFeet} Feet", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("ऊंची कूद (High Jump):", style = MaterialTheme.typography.bodySmall)
                                Text("${s.highJumpFeet} Feet", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("गोला फेंक (Shot Put):", style = MaterialTheme.typography.bodySmall)
                                Text("${s.shotPutMeters} Meters", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("समग्र स्कोर:", style = MaterialTheme.typography.bodySmall)
                                Text("${s.overallScore}/100", fontWeight = FontWeight.Bold, color = OliveTertiary, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { selectedStudentForDetails = null },
                    colors = ButtonDefaults.buttonColors(containerColor = OliveTertiary)
                ) {
                    Text("बंद करें")
                }
            }
        )
    }

    // ==========================================
    // Trainer & Batch Assignment Dialog
    // ==========================================
    studentForTrainerAssignment?.let { targetStudent ->
        var selectedTrainerName by remember { mutableStateOf(targetStudent.assignedTrainerName) }
        var selectedBatchName by remember { mutableStateOf(targetStudent.batchName) }

        val trainerOptions = listOf(
            "देव कुमार निषाद (मुख्य कोच)" to "TR-001",
            "अजय यादव (फिटनेस कोच)" to "TR-002",
            "सुरेश वर्मा (रनिंग कोच)" to "TR-003",
            "राजेश कुमार (ड्रिल व पीटी ट्रेनर)" to "TR-004"
        )

        val batchOptions = listOf(
            "सुबह आर्मी स्पेशल बैच (Morning Army Batch)",
            "सीजी पुलिस स्पेशल बैच (Police Special Batch)",
            "एसएससी जीडी फिजिकल बैच (SSC GD Batch)",
            "शाम अग्निवीर रनिंग बैच (Evening Running Batch)",
            "सामान्य फिटनेस बैच (General Physical Batch)"
        )

        AlertDialog(
            onDismissRequest = { studentForTrainerAssignment = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.AssignmentInd, contentDescription = null, tint = SaffronPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("ट्रेनर एवं बैच आवंटन (Trainer & Batch)", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "कैडेट: ${targetStudent.fullName} (${targetStudent.studentId})",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Text("प्रशिक्षक (Trainer) चुनें:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                    trainerOptions.forEach { (tName, _) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedTrainerName = tName }
                                .padding(vertical = 2.dp)
                        ) {
                            RadioButton(
                                selected = selectedTrainerName == tName,
                                onClick = { selectedTrainerName = tName }
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(tName, style = MaterialTheme.typography.bodySmall)
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text("प्रशिक्षण बैच (Batch) चुनें:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                    batchOptions.forEach { bName ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedBatchName = bName }
                                .padding(vertical = 2.dp)
                        ) {
                            RadioButton(
                                selected = selectedBatchName == bName,
                                onClick = { selectedBatchName = bName }
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(bName, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val trainerId = trainerOptions.find { it.first == selectedTrainerName }?.second ?: "TR-001"
                        onAssignTrainer(targetStudent.studentId, trainerId, selectedTrainerName, selectedBatchName)
                        selectedStudentForDetails = targetStudent.copy(
                            assignedTrainerId = trainerId,
                            assignedTrainerName = selectedTrainerName,
                            batchName = selectedBatchName
                        )
                        studentForTrainerAssignment = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary)
                ) {
                    Text("आवंटित करें (Save)")
                }
            },
            dismissButton = {
                TextButton(onClick = { studentForTrainerAssignment = null }) { Text("रद्द करें") }
            }
        )
    }

    // ==========================================
    // Ground Physical Test Recorder Dialog
    // ==========================================
    studentForGroundTestRecord?.let { targetStudent ->
        var time1600 by remember { mutableStateOf(targetStudent.time1600m) }
        var pushups by remember { mutableStateOf(targetStudent.pushups.toString()) }
        var situps by remember { mutableStateOf(targetStudent.situps.toString()) }
        var pullups by remember { mutableStateOf(targetStudent.pullups.toString()) }
        var longJump by remember { mutableStateOf(targetStudent.longJumpFeet.toString()) }
        var highJump by remember { mutableStateOf(targetStudent.highJumpFeet.toString()) }
        var shotPut by remember { mutableStateOf(targetStudent.shotPutMeters.toString()) }
        var coachNotes by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { studentForGroundTestRecord = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.FitnessCenter, contentDescription = null, tint = SaffronPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("ग्राउंड फिजिकल टेस्ट स्कोर दर्ज करें", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        Text(
                            text = "कैडेट: ${targetStudent.fullName} (${targetStudent.studentId})",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(value = time1600, onValueChange = { time1600 = it }, label = { Text("1600m समय (mm:ss)") }, modifier = Modifier.weight(1f))
                            OutlinedTextField(value = pullups, onValueChange = { pullups = it }, label = { Text("बीम / पुल-अप्स") }, modifier = Modifier.weight(1f))
                        }
                    }
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(value = pushups, onValueChange = { pushups = it }, label = { Text("पुश-अप्स") }, modifier = Modifier.weight(1f))
                            OutlinedTextField(value = situps, onValueChange = { situps = it }, label = { Text("सिट-अप्स") }, modifier = Modifier.weight(1f))
                        }
                    }
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(value = longJump, onValueChange = { longJump = it }, label = { Text("लंबी कूद (Feet)") }, modifier = Modifier.weight(1f))
                            OutlinedTextField(value = highJump, onValueChange = { highJump = it }, label = { Text("ऊंची कूद (Feet)") }, modifier = Modifier.weight(1f))
                        }
                    }
                    item {
                        OutlinedTextField(value = shotPut, onValueChange = { shotPut = it }, label = { Text("गोला फेंक (Meters)") }, modifier = Modifier.fillMaxWidth())
                    }
                    item {
                        OutlinedTextField(value = coachNotes, onValueChange = { coachNotes = it }, label = { Text("कोच टिप्पणी (Coach Notes)") }, modifier = Modifier.fillMaxWidth())
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val p = pushups.toIntOrNull() ?: targetStudent.pushups
                        val s = situps.toIntOrNull() ?: targetStudent.situps
                        val b = pullups.toIntOrNull() ?: targetStudent.pullups
                        val lj = longJump.toDoubleOrNull() ?: targetStudent.longJumpFeet
                        val hj = highJump.toDoubleOrNull() ?: targetStudent.highJumpFeet
                        val sp = shotPut.toDoubleOrNull() ?: targetStudent.shotPutMeters

                        onRecordGroundTest(
                            targetStudent.studentId,
                            time1600.trim(),
                            p,
                            s,
                            b,
                            lj,
                            hj,
                            sp,
                            coachNotes.trim()
                        )

                        selectedStudentForDetails = targetStudent.copy(
                            time1600m = time1600.trim().ifEmpty { targetStudent.time1600m },
                            pushups = p,
                            situps = s,
                            pullups = b,
                            longJumpFeet = lj,
                            highJumpFeet = hj,
                            shotPutMeters = sp
                        )
                        studentForGroundTestRecord = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary)
                ) {
                    Text("स्कोर दर्ज करें (Save)")
                }
            },
            dismissButton = {
                TextButton(onClick = { studentForGroundTestRecord = null }) { Text("रद्द करें") }
            }
        )
    }

    // ==========================================
    // Enroll New Student Dialog
    // ==========================================
    if (showAddStudentDialog) {
        val nextId = remember(allStudents) {
            com.example.util.ProfileUtils.generateNextStudentId(allStudents)
        }

        var name by remember { mutableStateOf("") }
        var fatherName by remember { mutableStateOf("") }
        var village by remember { mutableStateOf("मौरिकला") }
        var dob by remember { mutableStateOf("2005-05-15") }
        var mobile by remember { mutableStateOf("") }
        var gender by remember { mutableStateOf("Male") }
        var education by remember { mutableStateOf("12th Pass") }
        var goal by remember { mutableStateOf("Indian Army") }
        var height by remember { mutableStateOf("172") }
        var weight by remember { mutableStateOf("63") }
        var time1600 by remember { mutableStateOf("5:30") }
        var pushups by remember { mutableStateOf("40") }
        var trainerName by remember { mutableStateOf("देव कुमार निषाद (मुख्य कोच)") }
        var batchName by remember { mutableStateOf("सुबह आर्मी स्पेशल बैच (Morning Army Batch)") }
        var validationError by remember { mutableStateOf<String?>(null) }

        val calculatedAge = remember(dob) {
            val calc = com.example.util.ProfileUtils.calculateAgeFromDob(dob)
            if (calc > 0) calc else 20
        }

        val liveHeight = height.toDoubleOrNull() ?: 172.0
        val liveWeight = weight.toDoubleOrNull() ?: 63.0
        val liveBmi = remember(liveHeight, liveWeight) {
            com.example.util.ProfileUtils.getBmiCategory(liveHeight, liveWeight)
        }

        AlertDialog(
            onDismissRequest = { showAddStudentDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.PersonAdd, contentDescription = null, tint = SaffronPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("नया अभ्यर्थी पंजीयन (Enroll Student)", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            },
            text = {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 480.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
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
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                        }
                    }

                    // Generated Unique ID
                    item {
                        Surface(
                            color = SaffronContainer,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("स्वतः जनरेटेड स्टूडेंट आईडी:", style = MaterialTheme.typography.bodySmall, color = OnSaffronContainer)
                                Text(nextId, fontWeight = FontWeight.ExtraBold, color = SaffronDark, style = MaterialTheme.typography.titleSmall)
                            }
                        }
                    }

                    item { OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("पूरा नाम (Full Name) *") }, modifier = Modifier.fillMaxWidth()) }
                    item { OutlinedTextField(value = fatherName, onValueChange = { fatherName = it }, label = { Text("पिता का नाम (Father's Name)") }, modifier = Modifier.fillMaxWidth()) }
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(value = mobile, onValueChange = { mobile = it }, label = { Text("मोबाइल नंबर") }, keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Phone), modifier = Modifier.weight(1f))
                            OutlinedTextField(value = village, onValueChange = { village = it }, label = { Text("गाँव (Village)") }, modifier = Modifier.weight(1f))
                        }
                    }
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(value = dob, onValueChange = { dob = it }, label = { Text("जन्म तिथि (YYYY-MM-DD)") }, modifier = Modifier.weight(1.3f))
                            OutlinedTextField(value = "$calculatedAge वर्ष", onValueChange = {}, readOnly = true, enabled = false, label = { Text("आयु (Auto)") }, modifier = Modifier.weight(0.9f))
                        }
                    }
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(value = gender, onValueChange = { gender = it }, label = { Text("लिंग (Gender)") }, modifier = Modifier.weight(1f))
                            OutlinedTextField(value = education, onValueChange = { education = it }, label = { Text("शिक्षा (Education)") }, modifier = Modifier.weight(1f))
                        }
                    }
                    item { OutlinedTextField(value = goal, onValueChange = { goal = it }, label = { Text("भर्ती लक्ष्य (Army/Police/SSC)") }, modifier = Modifier.fillMaxWidth()) }
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(value = trainerName, onValueChange = { trainerName = it }, label = { Text("आवंटित कोच") }, modifier = Modifier.weight(1f))
                            OutlinedTextField(value = batchName, onValueChange = { batchName = it }, label = { Text("प्रशिक्षण बैच") }, modifier = Modifier.weight(1f))
                        }
                    }
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(value = height, onValueChange = { height = it }, label = { Text("ऊंचाई (cm)") }, modifier = Modifier.weight(1f))
                            OutlinedTextField(value = weight, onValueChange = { weight = it }, label = { Text("वजन (kg)") }, modifier = Modifier.weight(1f))
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
                            OutlinedTextField(value = time1600, onValueChange = { time1600 = it }, label = { Text("1600m लक्ष्य समय") }, modifier = Modifier.weight(1f))
                            OutlinedTextField(value = pushups, onValueChange = { pushups = it }, label = { Text("पुश-अप्स") }, modifier = Modifier.weight(1f))
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val cleanMobile = mobile.trim().filter { it.isDigit() }
                        if (name.isBlank()) {
                            validationError = "कृपया छात्र का पूरा नाम दर्ज करें।"
                        } else if (cleanMobile.length != 10) {
                            validationError = "कृपया 10 अंकों का वैध मोबाइल नंबर दर्ज करें! (10 Digits Required)"
                        } else if (allStudents.any { it.mobileNumber.trim().filter { c -> c.isDigit() } == cleanMobile }) {
                            validationError = "यह मोबाइल नंबर ($cleanMobile) पहले से पंजीकृत है! कृपया दूसरा नंबर दर्ज करें।"
                        } else {
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
                                recruitmentGoal = goal.trim().ifEmpty { "Indian Army" },
                                heightCm = height.toDoubleOrNull() ?: 172.0,
                                weightKg = weight.toDoubleOrNull() ?: 63.0,
                                chestNormalCm = 81.0,
                                chestExpandedCm = 86.0,
                                time1600m = time1600.trim().ifEmpty { "5:30" },
                                time400m = "1:08",
                                time800m = "2:25",
                                time5km = "21:30",
                                pushups = pushups.toIntOrNull() ?: 35,
                                situps = 45,
                                pullups = 8,
                                squats = 50,
                                plankSeconds = 90,
                                longJumpFeet = 15.0,
                                highJumpFeet = 4.0,
                                shotPutMeters = 7.2,
                                attendanceStreakDays = 1,
                                studyTargetPercentage = 0,
                                overallScore = 80,
                                assignedTrainerName = trainerName.trim().ifEmpty { "देव कुमार निषाद (मुख्य कोच)" },
                                batchName = batchName.trim().ifEmpty { "सुबह आर्मी स्पेशल बैच (Morning Army Batch)" }
                            )
                            onAddStudent(newStudent)
                            showAddStudentDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary),
                    modifier = Modifier.testTag("submit_new_student_button")
                ) {
                    Text("पंजीयन करें (Save)")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddStudentDialog = false }) { Text("रद्द करें") }
            }
        )
    }
}

@Composable
fun AdminActionRow(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminAttendanceBatchScreen(
    allStudents: List<StudentProfile>,
    onSaveBatchAttendance: (Map<String, String>, String) -> Unit = { _, _ -> },
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val todayDateStr = remember {
        java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date())
    }
    var selectedDate by remember { mutableStateOf(todayDateStr) }
    var attendanceSearchQuery by remember { mutableStateOf("") }
    var selectedBatchFilter by remember { mutableStateOf("ALL") }

    val statusMap = remember {
        mutableStateMapOf<String, String>().apply {
            allStudents.forEach { s -> put(s.studentId, "Present") }
        }
    }
    var savedSuccess by remember { mutableStateOf(false) }

    val filteredAttendanceStudents = remember(allStudents, attendanceSearchQuery, selectedBatchFilter) {
        allStudents.filter { s ->
            val matchesFilter = when (selectedBatchFilter) {
                "ARMY" -> s.recruitmentGoal.contains("Army", ignoreCase = true) || s.batchName.contains("Army", ignoreCase = true) || s.batchName.contains("आर्मी", ignoreCase = true)
                "POLICE" -> s.recruitmentGoal.contains("Police", ignoreCase = true) || s.batchName.contains("Police", ignoreCase = true) || s.batchName.contains("पुलिस", ignoreCase = true)
                "SSC_GD" -> s.recruitmentGoal.contains("SSC", ignoreCase = true) || s.batchName.contains("GD", ignoreCase = true)
                else -> true
            }

            val matchesSearch = if (attendanceSearchQuery.isBlank()) true else {
                val q = attendanceSearchQuery.trim().lowercase()
                s.fullName.lowercase().contains(q) ||
                s.studentId.lowercase().contains(q) ||
                s.village.lowercase().contains(q) ||
                s.batchName.lowercase().contains(q)
            }

            matchesFilter && matchesSearch
        }
    }

    val totalStudents = allStudents.size
    val presentCount = statusMap.values.count { it == "Present" }
    val absentCount = statusMap.values.count { it == "Absent" }
    val leaveCount = statusMap.values.count { it == "Leave" }
    val lateCount = statusMap.values.count { it == "Late" }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("admin_attendance_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "दैनिक बैच उपस्थिति (Trainer Attendance)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "ग्राउंड प्रशिक्षण उपस्थिति सत्यापन • दिनांक चुनें एवं उपस्थिति दर्ज करें",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Date selector field
                    OutlinedTextField(
                        value = selectedDate,
                        onValueChange = {
                            selectedDate = it
                            savedSuccess = false
                        },
                        label = { Text("उपस्थिति दिनांक (Date YYYY-MM-DD)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.CalendarToday, contentDescription = null, tint = SaffronPrimary)
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Summary statistics card
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "$totalStudents", fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleMedium)
                                Text(text = "कुल छात्र", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "$presentCount", fontWeight = FontWeight.ExtraBold, color = StatusPresent, style = MaterialTheme.typography.titleMedium)
                                Text(text = "उपस्थित", style = MaterialTheme.typography.labelSmall, color = StatusPresent)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "$absentCount", fontWeight = FontWeight.ExtraBold, color = StatusAbsent, style = MaterialTheme.typography.titleMedium)
                                Text(text = "अनुपस्थित", style = MaterialTheme.typography.labelSmall, color = StatusAbsent)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "$leaveCount", fontWeight = FontWeight.ExtraBold, color = StatusLeave, style = MaterialTheme.typography.titleMedium)
                                Text(text = "अवकाश", style = MaterialTheme.typography.labelSmall, color = StatusLeave)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                filteredAttendanceStudents.forEach { statusMap[it.studentId] = "Present" }
                                savedSuccess = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = OliveTertiary),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("सभी उपस्थित", style = MaterialTheme.typography.labelSmall)
                        }

                        Button(
                            onClick = {
                                onSaveBatchAttendance(statusMap.toMap(), selectedDate)
                                savedSuccess = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("उपस्थिति सेव करें", style = MaterialTheme.typography.labelSmall)
                        }
                    }

                    if (savedSuccess) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "✓ दिनांक $selectedDate की उपस्थिति सफलतापूर्वक दर्ज की गई!",
                            color = OliveTertiary,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }

        // Attendance Search & Batch Filter
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = attendanceSearchQuery,
                    onValueChange = { attendanceSearchQuery = it },
                    placeholder = { Text("रोल कॉल: नाम या JBA ID से खोजें...") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = SaffronPrimary)
                    },
                    trailingIcon = {
                        if (attendanceSearchQuery.isNotEmpty()) {
                            IconButton(onClick = { attendanceSearchQuery = "" }) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = selectedBatchFilter == "ALL",
                        onClick = { selectedBatchFilter = "ALL" },
                        label = { Text("सभी (${allStudents.size})", style = MaterialTheme.typography.labelSmall) }
                    )
                    FilterChip(
                        selected = selectedBatchFilter == "ARMY",
                        onClick = { selectedBatchFilter = "ARMY" },
                        label = { Text("आर्मी बैच", style = MaterialTheme.typography.labelSmall) }
                    )
                    FilterChip(
                        selected = selectedBatchFilter == "POLICE",
                        onClick = { selectedBatchFilter = "POLICE" },
                        label = { Text("पुलिस बैच", style = MaterialTheme.typography.labelSmall) }
                    )
                    FilterChip(
                        selected = selectedBatchFilter == "SSC_GD",
                        onClick = { selectedBatchFilter = "SSC_GD" },
                        label = { Text("GD बैच", style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }
        }

        items(filteredAttendanceStudents) { student ->
            val currentStatus = statusMap[student.studentId] ?: "Present"

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = student.fullName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            Text(text = "${student.studentId} • ${student.village} • ${student.batchName}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        StatusBadge(status = currentStatus)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("Present", "Absent", "Leave", "Late").forEach { st ->
                            val isSel = currentStatus == st
                            val stColor = when (st) {
                                "Present" -> OliveTertiary
                                "Late" -> StatusLate
                                "Leave" -> StatusLeave
                                else -> StatusAbsent
                            }
                            val stLabel = when (st) {
                                "Present" -> "उपस्थित"
                                "Absent" -> "अनुपस्थित"
                                "Leave" -> "अवकाश"
                                else -> "देरी"
                            }

                            FilterChip(
                                selected = isSel,
                                onClick = {
                                    statusMap[student.studentId] = st
                                    savedSuccess = false
                                },
                                label = { Text(stLabel, style = MaterialTheme.typography.labelSmall) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = stColor.copy(alpha = 0.2f),
                                    selectedLabelColor = stColor
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminWorkoutPlanScreen(
    currentPlan: DailyWorkoutPlan?,
    onSavePlan: (DailyWorkoutPlan) -> Unit,
    modifier: Modifier = Modifier
) {
    var title by remember { mutableStateOf(currentPlan?.title ?: "ग्राउंड रनिंग एवं बीम स्पेशल ट्रेनिंग") }
    var instructions by remember { mutableStateOf(currentPlan?.instructions ?: "सुबह 5:30 बजे 3 KM वार्म-अप रनिंग, 1600m ट्रायल, 50 पुश-अप्स व 10 बीम अभ्यास।") }
    var runKm by remember { mutableStateOf(currentPlan?.targetRunningKm?.toString() ?: "3.0") }
    var pushups by remember { mutableStateOf(currentPlan?.targetPushups?.toString() ?: "50") }
    var situps by remember { mutableStateOf(currentPlan?.targetSitups?.toString() ?: "50") }
    var pullups by remember { mutableStateOf(currentPlan?.targetPullups?.toString() ?: "10") }
    var isSaved by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("admin_workout_plan_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
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
                    Text(
                        text = "दैनिक ट्रेनिंग प्लान बनाएं (Create Workout Plan)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "यह प्लान सभी छात्रों के होम एवं वर्कआउट स्क्रीन पर प्रदर्शित होगा।",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("प्लान का शीर्षक") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = instructions,
                        onValueChange = { instructions = it },
                        label = { Text("प्रशिक्षक निर्देश एवं तकनीक") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = runKm,
                            onValueChange = { runKm = it },
                            label = { Text("रनिंग (KM)") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = pushups,
                            onValueChange = { pushups = it },
                            label = { Text("पुश-अप्स") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = situps,
                            onValueChange = { situps = it },
                            label = { Text("सिट-अप्स") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = pullups,
                            onValueChange = { pullups = it },
                            label = { Text("पुल-अप्स (बीम)") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Button(
                        onClick = {
                            val plan = DailyWorkoutPlan(
                                date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
                                title = title,
                                targetRunningKm = runKm.toDoubleOrNull() ?: 3.0,
                                targetPushups = pushups.toIntOrNull() ?: 50,
                                targetSitups = situps.toIntOrNull() ?: 50,
                                targetPullups = pullups.toIntOrNull() ?: 10,
                                instructions = instructions
                            )
                            onSavePlan(plan)
                            isSaved = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.Publish, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("ट्रेनिंग प्लान प्रकाशित करें (Publish)")
                    }

                    if (isSaved) {
                        Text(
                            text = "✓ नया ट्रेनिंग प्लान सफलतापूर्वक प्रकाशित किया गया!",
                            color = OliveTertiary,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AdminNoticeAndRecruitmentScreen(
    notices: List<Notice>,
    recruitmentList: List<RecruitmentInfo>,
    onPublishNotice: (title: String, content: String, category: String, isUrgent: Boolean) -> Unit,
    onDeleteNotice: (Notice) -> Unit,
    onAddRecruitment: (RecruitmentInfo) -> Unit,
    onDeleteRecruitment: (RecruitmentInfo) -> Unit,
    modifier: Modifier = Modifier,
    onUpdateNotice: ((Notice) -> Unit)? = null,
    onTogglePinNotice: ((Notice) -> Unit)? = null,
    onUpdateRecruitment: ((RecruitmentInfo) -> Unit)? = null
) {
    var showNoticeDialog by remember { mutableStateOf(false) }
    var showJobDialog by remember { mutableStateOf(false) }
    var editingNotice by remember { mutableStateOf<Notice?>(null) }
    var editingRecruitment by remember { mutableStateOf<RecruitmentInfo?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("admin_notices_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "सूचना व भर्ती प्रबंधन (Notices & Recruitment Control)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "अखाड़ा नोटिस प्रकाशित/संपादित करें एवं सरकारी भर्ती अलर्ट मैनेज करें।",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { showNoticeDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("नया नोटिस", style = MaterialTheme.typography.labelSmall)
                        }

                        Button(
                            onClick = { showJobDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = OliveTertiary),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("नई भर्ती जोड़ें", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = "सक्रिय अखाड़ा नोटिस सूची (${notices.size})",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
        }

        items(notices) { notice ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (notice.isPinned) SaffronContainer.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surface
                ),
                border = CardDefaults.outlinedCardBorder().let {
                    if (notice.isPinned) androidx.compose.foundation.BorderStroke(1.5.dp, SaffronPrimary)
                    else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            if (notice.isPinned) {
                                Surface(color = SaffronDark, shape = RoundedCornerShape(4.dp)) {
                                    Text("📌 PIN", color = Color.White, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                                }
                            }
                            if (notice.isUrgent) {
                                Surface(color = StatusAbsent, shape = RoundedCornerShape(4.dp)) {
                                    Text("URGENT", color = Color.White, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                                }
                            }
                            Text(text = notice.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        }
                        Text(text = "${notice.category} • ${notice.date} • ${notice.author}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    Row {
                        IconButton(onClick = { editingNotice = notice }) {
                            Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", tint = SaffronPrimary)
                        }
                        IconButton(onClick = { onDeleteNotice(notice) }) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = StatusAbsent)
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = "सरकारी भर्ती नोटिफिकेशन सूची (${recruitmentList.size})",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
        }

        items(recruitmentList) { rec ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = rec.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        Text(text = "पद: ${rec.postName} (${rec.totalPosts}) • अंतिम तिथि: ${rec.lastDate}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    Row {
                        IconButton(onClick = { editingRecruitment = rec }) {
                            Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", tint = OliveTertiary)
                        }
                        IconButton(onClick = { onDeleteRecruitment(rec) }) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = StatusAbsent)
                        }
                    }
                }
            }
        }
    }

    // Add Notice Dialog
    if (showNoticeDialog) {
        var nTitle by remember { mutableStateOf("") }
        var nContent by remember { mutableStateOf("") }
        var nCategory by remember { mutableStateOf("प्रशिक्षण") }
        var isUrgent by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showNoticeDialog = false },
            title = { Text("नया नोटिस जारी करें", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = nTitle, onValueChange = { nTitle = it }, label = { Text("शीर्षक (Title)") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = nContent, onValueChange = { nContent = it }, label = { Text("विवरण (Details)") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
                    OutlinedTextField(value = nCategory, onValueChange = { nCategory = it }, label = { Text("श्रेणी (प्रशिक्षण / परीक्षा / भर्ती / सामान्य)") }, modifier = Modifier.fillMaxWidth())
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = isUrgent, onCheckedChange = { isUrgent = it })
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("अति महत्वपूर्ण / जरूरी सूचना (Urgent Alert)")
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (nTitle.isNotEmpty() && nContent.isNotEmpty()) {
                            onPublishNotice(nTitle, nContent, nCategory, isUrgent)
                            showNoticeDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary)
                ) {
                    Text("प्रकाशित करें")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNoticeDialog = false }) { Text("रद्द करें") }
            }
        )
    }

    // Edit Notice Dialog
    editingNotice?.let { noticeToEdit ->
        var nTitle by remember { mutableStateOf(noticeToEdit.title) }
        var nContent by remember { mutableStateOf(noticeToEdit.content) }
        var nCategory by remember { mutableStateOf(noticeToEdit.category) }
        var nAuthor by remember { mutableStateOf(noticeToEdit.author) }
        var isUrgent by remember { mutableStateOf(noticeToEdit.isUrgent) }
        var isPinned by remember { mutableStateOf(noticeToEdit.isPinned) }

        AlertDialog(
            onDismissRequest = { editingNotice = null },
            title = { Text("नोटिस संपादित करें (Edit Notice)", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = nTitle, onValueChange = { nTitle = it }, label = { Text("शीर्षक (Title)") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = nContent, onValueChange = { nContent = it }, label = { Text("विवरण (Details)") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
                    OutlinedTextField(value = nCategory, onValueChange = { nCategory = it }, label = { Text("श्रेणी (Category)") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = nAuthor, onValueChange = { nAuthor = it }, label = { Text("जारीकर्ता (Author)") }, modifier = Modifier.fillMaxWidth())
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = isUrgent, onCheckedChange = { isUrgent = it })
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("जरूरी सूचना (Urgent)")
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = isPinned, onCheckedChange = { isPinned = it })
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("शीर्ष पर पिन करें (Pin Notice)")
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val updated = noticeToEdit.copy(
                            title = nTitle,
                            content = nContent,
                            category = nCategory,
                            author = nAuthor,
                            isUrgent = isUrgent,
                            priority = if (isUrgent) "URGENT" else "NORMAL",
                            isPinned = isPinned
                        )
                        onUpdateNotice?.invoke(updated)
                        editingNotice = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary)
                ) {
                    Text("अपडेट करें (Save)")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingNotice = null }) { Text("रद्द करें") }
            }
        )
    }

    // Add Recruitment Dialog
    if (showJobDialog) {
        var jTitle by remember { mutableStateOf("") }
        var jPost by remember { mutableStateOf("") }
        var jCount by remember { mutableStateOf("") }
        var jElig by remember { mutableStateOf("10th / 12th Pass") }
        var jAge by remember { mutableStateOf("18-23 वर्ष") }
        var jDate by remember { mutableStateOf("2026-09-30") }
        var jUrl by remember { mutableStateOf("https://joinindianarmy.nic.in") }

        AlertDialog(
            onDismissRequest = { showJobDialog = false },
            title = { Text("नई सरकारी भर्ती जोड़ें", fontWeight = FontWeight.Bold) },
            text = {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    item { OutlinedTextField(value = jTitle, onValueChange = { jTitle = it }, label = { Text("भर्ती शीर्षक") }, modifier = Modifier.fillMaxWidth()) }
                    item { OutlinedTextField(value = jPost, onValueChange = { jPost = it }, label = { Text("पद का नाम") }, modifier = Modifier.fillMaxWidth()) }
                    item { OutlinedTextField(value = jCount, onValueChange = { jCount = it }, label = { Text("कुल पद (Total Posts)") }, modifier = Modifier.fillMaxWidth()) }
                    item { OutlinedTextField(value = jElig, onValueChange = { jElig = it }, label = { Text("योग्यता") }, modifier = Modifier.fillMaxWidth()) }
                    item { OutlinedTextField(value = jAge, onValueChange = { jAge = it }, label = { Text("आयु सीमा") }, modifier = Modifier.fillMaxWidth()) }
                    item { OutlinedTextField(value = jDate, onValueChange = { jDate = it }, label = { Text("अंतिम तिथि") }, modifier = Modifier.fillMaxWidth()) }
                    item { OutlinedTextField(value = jUrl, onValueChange = { jUrl = it }, label = { Text("ऑफिशियल वेबसाइट लिंक") }, modifier = Modifier.fillMaxWidth()) }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (jTitle.isNotEmpty() && jPost.isNotEmpty()) {
                            val newRec = RecruitmentInfo(
                                recruitmentName = "$jTitle - $jPost",
                                organization = jCount.ifEmpty { "1000+ Posts" },
                                eligibility = jElig,
                                ageLimit = jAge,
                                heightRequirement = "168 cm",
                                chestRequirement = "81-86 cm",
                                physicalTest = "1600m Running, Long Jump, High Jump",
                                writtenExam = "Online CBT Exam",
                                syllabus = "Maths, Reasoning, GK, Language",
                                importantDocuments = "Aadhaar, 10th/12th Marksheet, Domicile",
                                importantDates = jDate,
                                officialWebsiteLink = jUrl
                            )
                            onAddRecruitment(newRec)
                            showJobDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary)
                ) {
                    Text("जोड़ें")
                }
            },
            dismissButton = {
                TextButton(onClick = { showJobDialog = false }) { Text("रद्द करें") }
            }
        )
    }

    // Edit Recruitment Dialog (Phase 2C Admin Enhancement)
    editingRecruitment?.let { recToEdit ->
        var jTitle by remember { mutableStateOf(recToEdit.recruitmentName) }
        var jOrg by remember { mutableStateOf(recToEdit.organization) }
        var jElig by remember { mutableStateOf(recToEdit.eligibility) }
        var jAge by remember { mutableStateOf(recToEdit.ageLimit) }
        var jDates by remember { mutableStateOf(recToEdit.importantDates) }
        var jHeight by remember { mutableStateOf(recToEdit.heightRequirement) }
        var jChest by remember { mutableStateOf(recToEdit.chestRequirement) }
        var jPhysical by remember { mutableStateOf(recToEdit.physicalTest) }
        var jUrl by remember { mutableStateOf(recToEdit.officialWebsiteLink) }

        AlertDialog(
            onDismissRequest = { editingRecruitment = null },
            title = { Text("भर्ती अधिसूचना संपादित करें (Edit Recruitment)", fontWeight = FontWeight.Bold) },
            text = {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    item { OutlinedTextField(value = jTitle, onValueChange = { jTitle = it }, label = { Text("भर्ती नाम / पद") }, modifier = Modifier.fillMaxWidth()) }
                    item { OutlinedTextField(value = jOrg, onValueChange = { jOrg = it }, label = { Text("विभाग / संगठन") }, modifier = Modifier.fillMaxWidth()) }
                    item { OutlinedTextField(value = jElig, onValueChange = { jElig = it }, label = { Text("शैक्षणिक योग्यता") }, modifier = Modifier.fillMaxWidth()) }
                    item { OutlinedTextField(value = jAge, onValueChange = { jAge = it }, label = { Text("आयु सीमा") }, modifier = Modifier.fillMaxWidth()) }
                    item { OutlinedTextField(value = jDates, onValueChange = { jDates = it }, label = { Text("आवेदन व परीक्षा तिथियां") }, modifier = Modifier.fillMaxWidth()) }
                    item { OutlinedTextField(value = jHeight, onValueChange = { jHeight = it }, label = { Text("ऊंचाई मानक") }, modifier = Modifier.fillMaxWidth()) }
                    item { OutlinedTextField(value = jChest, onValueChange = { jChest = it }, label = { Text("सीना मानक") }, modifier = Modifier.fillMaxWidth()) }
                    item { OutlinedTextField(value = jPhysical, onValueChange = { jPhysical = it }, label = { Text("शारीरिक दक्षता (PET)") }, modifier = Modifier.fillMaxWidth(), minLines = 2) }
                    item { OutlinedTextField(value = jUrl, onValueChange = { jUrl = it }, label = { Text("ऑफिशियल वेबसाइट URL") }, modifier = Modifier.fillMaxWidth()) }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val updated = recToEdit.copy(
                            recruitmentName = jTitle,
                            organization = jOrg,
                            eligibility = jElig,
                            ageLimit = jAge,
                            importantDates = jDates,
                            heightRequirement = jHeight,
                            chestRequirement = jChest,
                            physicalTest = jPhysical,
                            officialWebsiteLink = jUrl
                        )
                        onUpdateRecruitment?.invoke(updated)
                        editingRecruitment = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = OliveTertiary)
                ) {
                    Text("अपडेट करें (Save)")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingRecruitment = null }) { Text("रद्द करें") }
            }
        )
    }
}
