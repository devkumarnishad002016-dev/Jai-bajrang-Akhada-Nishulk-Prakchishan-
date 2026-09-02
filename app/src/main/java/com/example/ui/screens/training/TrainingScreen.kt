package com.example.ui.screens.training

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AttendanceRecord
import com.example.data.model.DailyWorkoutPlan
import com.example.data.model.StudentProfile
import com.example.data.model.TrainingRecord
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrainingScreen(
    student: StudentProfile?,
    trainingRecords: List<TrainingRecord>,
    todayTraining: TrainingRecord?,
    latestPlan: DailyWorkoutPlan?,
    attendanceRecords: List<AttendanceRecord>,
    todayAttendance: AttendanceRecord?,
    onSaveTrainingRecord: (TrainingRecord) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedRecordForDetail by remember { mutableStateOf<TrainingRecord?>(null) }
    var selectedTab by remember { mutableIntStateOf(0) } // 0: आज की ट्रेनिंग व सारांश, 1: ट्रेनिंग इतिहास (History)

    val todayDateStr = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()) }

    // Summary calculations
    val totalSessions = trainingRecords.size
    val currentMonthPrefix = remember { SimpleDateFormat("yyyy-MM", Locale.US).format(Date()) }
    val sessionsThisMonth = trainingRecords.count { it.date.startsWith(currentMonthPrefix) }
    val totalRunningKm = trainingRecords.sumOf { it.runningDistanceKm }

    val bestPushups = (trainingRecords.map { it.pushups } + listOf(student?.pushups ?: 0)).maxOrNull() ?: 0
    val bestSitups = (trainingRecords.map { it.situps } + listOf(student?.situps ?: 0)).maxOrNull() ?: 0
    val bestPullups = (trainingRecords.map { it.pullups } + listOf(student?.pullups ?: 0)).maxOrNull() ?: 0
    val bestSquats = (trainingRecords.map { it.squats } + listOf(student?.squats ?: 0)).maxOrNull() ?: 0
    val bestPlank = (trainingRecords.map { it.plankSeconds } + listOf(student?.plankSeconds ?: 0)).maxOrNull() ?: 0

    // Attendance stats
    val presentCount = attendanceRecords.count { it.status == "Present" }
    val absentCount = attendanceRecords.count { it.status == "Absent" }
    val leaveCount = attendanceRecords.count { it.status == "Leave" }
    val totalAtt = attendanceRecords.size.coerceAtLeast(1)
    val attendancePct = (presentCount.toDouble() / totalAtt * 100.0).toInt()

    val streak = remember(attendanceRecords) {
        var count = 0
        for (rec in attendanceRecords.sortedByDescending { it.date }) {
            if (rec.status == "Present") count++ else break
        }
        count.coerceAtLeast(1)
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("student_training_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Header Banner & Action Button
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
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "🏃 मेरी ट्रेनिंग (My Training)",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = "जय बजरंग अखाड़ा • दैनिक शारीरिक अभ्यास एवं ट्रैकिंग",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Button(
                            onClick = { showAddDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("btn_log_training")
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("ट्रेनिंग दर्ज करें", style = MaterialTheme.typography.labelMedium)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Tab Selector: Overview vs History
                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        contentColor = SaffronPrimary,
                        modifier = Modifier.clip(RoundedCornerShape(10.dp))
                    ) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            text = { Text("आज की ट्रेनिंग व सारांश", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = { Text("ट्रेनिंग इतिहास (${trainingRecords.size})", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
                        )
                    }
                }
            }
        }

        if (selectedTab == 0) {
            // 2. Today's Attendance & Streak Quick Status
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
                            Text(
                                text = "उपस्थिति व अनुशासन (Attendance & Streak)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            val todayStatus = todayAttendance?.status ?: "Not Marked"
                            StatusBadge(status = todayStatus)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Streak Card
                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                color = SaffronContainer.copy(alpha = 0.6f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocalFireDepartment,
                                        contentDescription = "Streak",
                                        tint = SaffronPrimary,
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "🔥 $streak दिन",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = SaffronDark
                                        )
                                        Text(
                                            text = "लगातार स्ट्रीक",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = OnSaffronContainer
                                        )
                                    }
                                }
                            }

                            // Monthly Attendance %
                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                color = OliveContainer.copy(alpha = 0.6f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.FactCheck,
                                        contentDescription = "Attendance",
                                        tint = OliveTertiary,
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "$attendancePct%",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = OliveTertiary
                                        )
                                        Text(
                                            text = "मासिक उपस्थिति",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = OnOliveContainer
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Monthly breakdown counts
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Text(
                                text = "उपस्थित: $presentCount",
                                style = MaterialTheme.typography.labelSmall,
                                color = StatusPresent,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "अनुपस्थित: $absentCount",
                                style = MaterialTheme.typography.labelSmall,
                                color = StatusAbsent,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "अवकाश: $leaveCount",
                                style = MaterialTheme.typography.labelSmall,
                                color = StatusLeave,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // 3. Today's Training Card (आज की ट्रेनिंग)
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
                                    text = "आज की ट्रेनिंग (Today's Training)",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "दिनांक: $todayDateStr",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            if (todayTraining != null) {
                                Surface(
                                    color = OliveContainer,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "✓ पूर्ण (Completed)",
                                        color = OliveTertiary,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            } else {
                                Surface(
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "अभी दर्ज नहीं",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        style = MaterialTheme.typography.labelSmall,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        val displayRecord = todayTraining ?: trainingRecords.firstOrNull()
                        if (displayRecord != null) {
                            // Running Highlight
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = SaffronContainer.copy(alpha = 0.4f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.DirectionsRun,
                                            contentDescription = "Running",
                                            tint = SaffronPrimary,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = "🏃 दौड़: ${displayRecord.runningDistanceKm} KM (${displayRecord.runningType})",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = SaffronDark
                                            )
                                            Text(
                                                text = "समय: ${displayRecord.runningDuration.ifEmpty { "05:38" }} मिनट",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = OnSaffronContainer
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Strength Grid
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                TrainingStatPill("💪 पुश-अप्स", "${displayRecord.pushups}", OliveTertiary, Modifier.weight(1f))
                                TrainingStatPill("सिट-अप्स", "${displayRecord.situps}", NavySecondary, Modifier.weight(1f))
                                TrainingStatPill("पुल-अप्स/बीम", "${displayRecord.pullups}", Color(0xFF7C3AED), Modifier.weight(1f))
                                TrainingStatPill("उठक-बैठक", "${displayRecord.squats}", SaffronPrimary, Modifier.weight(1f))
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                TrainingStatPill(
                                    "🔥 प्लैंक",
                                    "${displayRecord.plankSeconds} सेकंड",
                                    Color(0xFFE65100),
                                    Modifier.weight(1f)
                                )
                                TrainingStatPill(
                                    "स्ट्रेचिंग",
                                    if (displayRecord.stretchingDone) "हाँ (पूर्ण)" else "नहीं",
                                    OliveTertiary,
                                    Modifier.weight(1f)
                                )
                            }

                            if (displayRecord.otherTraining.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "अन्य अभ्यास: ${displayRecord.otherTraining}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            if (displayRecord.trainerNotes.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Surface(
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Comment,
                                            contentDescription = null,
                                            tint = SaffronPrimary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "ट्रेनर नोट्स: ${displayRecord.trainerNotes}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        } else {
                            Text(
                                text = "आज का ट्रेनिंग डेटा अभी तक दर्ज नहीं किया गया है। ऊपर दिए गए बटन से अपनी ट्रेनिंग दर्ज करें।",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 12.dp)
                            )
                        }
                    }
                }
            }

            // 4. Training Summary & Best Benchmarks (ट्रेनिंग सारांश - PART 7)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Analytics,
                                contentDescription = "Summary",
                                tint = SaffronPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "ट्रेनिंग सारांश एवं सर्वश्रेष्ठ रिकॉर्ड (Training Summary)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "कुल सत्र, दौड़ दूरी और सर्वश्रेष्ठ शारीरिक क्षमता",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // High level counters
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            SummaryMetricCard("कुल सत्र (Total)", "$totalSessions", SaffronPrimary, Modifier.weight(1f))
                            SummaryMetricCard("इस माह सत्र", "$sessionsThisMonth", OliveTertiary, Modifier.weight(1f))
                            SummaryMetricCard("कुल दौड़ (KM)", String.format(Locale.US, "%.1f", totalRunningKm), NavySecondary, Modifier.weight(1f))
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "🏆 सर्वश्रेष्ठ रिकॉर्ड (Personal Bests)",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Running bests
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            BestBenchmarkBadge("400m", student?.time400m ?: "1:04", SaffronPrimary, Modifier.weight(1f))
                            BestBenchmarkBadge("800m", student?.time800m ?: "2:20", OliveTertiary, Modifier.weight(1f))
                            BestBenchmarkBadge("1600m", student?.time1600m ?: "5:15", NavySecondary, Modifier.weight(1f))
                            BestBenchmarkBadge("5 KM", student?.time5km ?: "20:10", Color(0xFF7C3AED), Modifier.weight(1f))
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Strength bests
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            BestBenchmarkBadge("पुश-अप्स", "$bestPushups", SaffronPrimary, Modifier.weight(1f))
                            BestBenchmarkBadge("सिट-अप्स", "$bestSitups", OliveTertiary, Modifier.weight(1f))
                            BestBenchmarkBadge("पुल-अप्स/बीम", "$bestPullups", NavySecondary, Modifier.weight(1f))
                            BestBenchmarkBadge("उठक-बैठक", "$bestSquats", OliveTertiary, Modifier.weight(1f))
                        }
                    }
                }
            }
        } else {
            // Tab 1: Historical Training Records List (ट्रेनिंग इतिहास - PART 2)
            if (trainingRecords.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.FitnessCenter,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "कोई पुराना ट्रेनिंग रिकॉर्ड नहीं मिला",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "अपनी पहली ट्रेनिंग सत्र दर्ज करने के लिए ऊपर 'ट्रेनिंग दर्ज करें' पर क्लिक करें।",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(trainingRecords) { record ->
                    TrainingRecordCard(
                        record = record,
                        onClick = { selectedRecordForDetail = record }
                    )
                }
            }
        }
    }

    // Modal Dialog: Add/Log Training Record (PART 1 & PART 2)
    if (showAddDialog) {
        AddTrainingRecordDialog(
            studentId = student?.studentId ?: "JBA-2026-001",
            initialDate = todayDateStr,
            onDismiss = { showAddDialog = false },
            onSave = { newRecord ->
                onSaveTrainingRecord(newRecord)
                showAddDialog = false
            }
        )
    }

    // Modal Dialog: Record Detail
    selectedRecordForDetail?.let { rec ->
        AlertDialog(
            onDismissRequest = { selectedRecordForDetail = null },
            title = {
                Text(
                    text = "ट्रेनिंग विवरण: ${rec.date}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("• रनिंग: ${rec.runningDistanceKm} KM (${rec.runningType}) - ${rec.runningDuration}")
                    Text("• पुश-अप्स: ${rec.pushups}")
                    Text("• सिट-अप्स: ${rec.situps}")
                    Text("• पुल-अप्स/बीम: ${rec.pullups}")
                    Text("• उठक-बैठक: ${rec.squats}")
                    Text("• प्लैंक: ${rec.plankSeconds} सेकंड")
                    Text("• स्ट्रेचिंग: ${if (rec.stretchingDone) "हाँ" else "नहीं"}")
                    if (rec.otherTraining.isNotEmpty()) {
                        Text("• अन्य अभ्यास: ${rec.otherTraining}")
                    }
                    if (rec.trainerNotes.isNotEmpty()) {
                        Text("• ट्रेनर नोट्स: ${rec.trainerNotes}")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedRecordForDetail = null }) {
                    Text("बंद करें (Close)")
                }
            }
        )
    }
}

@Composable
fun TrainingRecordCard(
    record: TrainingRecord,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("training_history_card_${record.id}"),
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = SaffronPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = record.date,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                Surface(
                    color = SaffronContainer.copy(alpha = 0.7f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = record.runningType,
                        style = MaterialTheme.typography.labelSmall,
                        color = SaffronDark,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Metrics Summary Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "🏃 ${record.runningDistanceKm} KM (${record.runningDuration})",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = SaffronPrimary
                )
                Text("•", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    text = "💪 पुश-अप्स: ${record.pushups}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text("•", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    text = "बीम: ${record.pullups}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text("•", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    text = "सिट-अप्स: ${record.situps}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            if (record.trainerNotes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "ट्रेनर नोट्स: ${record.trainerNotes}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun TrainingStatPill(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.12f)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = color)
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
        }
    }
}

@Composable
fun SummaryMetricCard(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = color.copy(alpha = 0.1f)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = color)
            Text(text = title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun BestBenchmarkBadge(
    event: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = event, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTrainingRecordDialog(
    studentId: String,
    initialDate: String,
    onDismiss: () -> Unit,
    onSave: (TrainingRecord) -> Unit
) {
    var date by remember { mutableStateOf(initialDate) }
    var runningDistanceKm by remember { mutableStateOf("1.6") }
    var runningDuration by remember { mutableStateOf("5:38") }
    var runningType by remember { mutableStateOf("1600m Practice") }
    var pushups by remember { mutableStateOf("45") }
    var situps by remember { mutableStateOf("50") }
    var pullups by remember { mutableStateOf("12") }
    var squats by remember { mutableStateOf("60") }
    var plankSeconds by remember { mutableStateOf("120") }
    var stretchingDone by remember { mutableStateOf(true) }
    var otherTraining by remember { mutableStateOf("") }
    var trainerNotes by remember { mutableStateOf("") }

    val runningTypes = listOf(
        "1600m Practice",
        "Easy Run",
        "Long Run",
        "Sprint",
        "Interval",
        "400m Practice",
        "800m Practice",
        "5 KM Practice",
        "Other"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "दैनिक ट्रेनिंग दर्ज करें (Log Training)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 450.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = date,
                        onValueChange = { date = it },
                        label = { Text("दिनांक (Date YYYY-MM-DD)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                item {
                    Text(
                        text = "दौड़ का प्रकार (Running Type):",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(runningTypes) { type ->
                            FilterChip(
                                selected = runningType == type,
                                onClick = { runningType = type },
                                label = { Text(type, style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = runningDistanceKm,
                            onValueChange = { runningDistanceKm = it },
                            label = { Text("दूरी (KM)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = runningDuration,
                            onValueChange = { runningDuration = it },
                            label = { Text("समय (MM:SS)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = pushups,
                            onValueChange = { pushups = it },
                            label = { Text("पुश-अप्स") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = situps,
                            onValueChange = { situps = it },
                            label = { Text("सिट-अप्स") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = pullups,
                            onValueChange = { pullups = it },
                            label = { Text("पुल-अप्स/बीम") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = squats,
                            onValueChange = { squats = it },
                            label = { Text("उठक-बैठक") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                }

                item {
                    OutlinedTextField(
                        value = plankSeconds,
                        onValueChange = { plankSeconds = it },
                        label = { Text("प्लैंक अवधि (सेकंड)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "स्ट्रेचिंग की गई (Stretching Done):", style = MaterialTheme.typography.bodyMedium)
                        Switch(checked = stretchingDone, onCheckedChange = { stretchingDone = it })
                    }
                }

                item {
                    OutlinedTextField(
                        value = otherTraining,
                        onValueChange = { otherTraining = it },
                        label = { Text("अन्य अभ्यास (Other Drills / Jumps)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                item {
                    OutlinedTextField(
                        value = trainerNotes,
                        onValueChange = { trainerNotes = it },
                        label = { Text("ट्रेनर या स्वयं के नोट्स (Notes)") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 2
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val rec = TrainingRecord(
                        studentId = studentId,
                        date = date.ifEmpty { initialDate },
                        runningDistanceKm = runningDistanceKm.toDoubleOrNull() ?: 1.6,
                        runningDuration = runningDuration.ifEmpty { "5:38" },
                        runningType = runningType,
                        pushups = pushups.toIntOrNull() ?: 40,
                        situps = situps.toIntOrNull() ?: 45,
                        pullups = pullups.toIntOrNull() ?: 10,
                        squats = squats.toIntOrNull() ?: 50,
                        plankSeconds = plankSeconds.toIntOrNull() ?: 120,
                        stretchingDone = stretchingDone,
                        otherTraining = otherTraining,
                        trainerNotes = trainerNotes,
                        timestamp = System.currentTimeMillis()
                    )
                    onSave(rec)
                },
                colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary)
            ) {
                Text("ट्रेनिंग सेव करें")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("रद्द करें")
            }
        }
    )
}
