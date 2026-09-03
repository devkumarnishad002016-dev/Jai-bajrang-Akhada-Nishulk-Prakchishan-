package com.example.ui.screens.attendance

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AttendanceRecord
import com.example.data.model.StudentProfile
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import com.example.util.ReportExportUtils
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceScreen(
    student: StudentProfile?,
    attendanceRecords: List<AttendanceRecord>,
    todayAttendance: AttendanceRecord?,
    onMarkTodayStatus: (String) -> Unit,
    onMarkStatusForDate: (date: String, status: String, remarks: String) -> Unit = { _, _, _ -> },
    onNavigateToChat: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val totalRecords = attendanceRecords.size.coerceAtLeast(1)
    val presentCount = attendanceRecords.count { it.status == "Present" }
    val absentCount = attendanceRecords.count { it.status == "Absent" }
    val lateCount = attendanceRecords.count { it.status == "Late" }
    val leaveCount = attendanceRecords.count { it.status == "Leave" }
    val attendancePct = (presentCount.toDouble() / totalRecords * 100.0).toInt()

    // Streak calculation
    val streak = remember(attendanceRecords) {
        var count = 0
        for (rec in attendanceRecords.sortedByDescending { it.date }) {
            if (rec.status == "Present") count++ else break
        }
        count.coerceAtLeast(5)
    }

    // Selected Inspection Date for checking / marking past attendance
    val sdf = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    val calendar = remember { Calendar.getInstance() }
    val todayDateStr = remember { sdf.format(Date()) }

    var selectedDateStr by remember { mutableStateOf(todayDateStr) }
    var selectedRemarks by remember { mutableStateOf("") }
    var showDatePickerDialog by remember { mutableStateOf(false) }

    // Quick Date Options (Today, Yesterday, 2 Days Ago, 3 Days Ago)
    val quickDateOptions = remember {
        val list = mutableListOf<Pair<String, String>>()
        val cal = Calendar.getInstance()
        list.add("आज (Today)" to sdf.format(cal.time))
        
        cal.add(Calendar.DAY_OF_YEAR, -1)
        list.add("कल (Yesterday)" to sdf.format(cal.time))
        
        cal.add(Calendar.DAY_OF_YEAR, -1)
        list.add("2 दिन पहले" to sdf.format(cal.time))
        
        cal.add(Calendar.DAY_OF_YEAR, -1)
        list.add("3 दिन पहले" to sdf.format(cal.time))
        
        list
    }

    // Status of selected date
    val selectedDateAttendance = remember(attendanceRecords, selectedDateStr, todayAttendance) {
        if (selectedDateStr == todayDateStr && todayAttendance != null) {
            todayAttendance
        } else {
            attendanceRecords.find { it.date == selectedDateStr }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("attendance_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header card
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
                                text = "दैनिक उपस्थिति प्रबंधन (Attendance)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "गाँव से सेना–पुलिस भर्ती अभियान • ग्राउंड अनुशासन",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        // Doubt / Coach Help quick icon
                        IconButton(
                            onClick = onNavigateToChat,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(SaffronContainer)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChatBubbleOutline,
                                contentDescription = "Doubt",
                                tint = SaffronPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Streak and Monthly stats
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Attendance %
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = OliveContainer.copy(alpha = 0.5f))
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "$attendancePct%",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = OliveTertiary
                                )
                                Text(
                                    text = "मासिक उपस्थिति",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = OnOliveContainer
                                )
                            }
                        }

                        // Streak
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = SaffronContainer.copy(alpha = 0.5f))
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.LocalFireDepartment,
                                        contentDescription = "Streak",
                                        tint = SaffronPrimary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "$streak दिन",
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = SaffronPrimary
                                    )
                                }
                                Text(
                                    text = "लगातार स्ट्रीक (Streak)",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = OnSaffronContainer
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Counts row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        AttendanceCountItem("उपस्थित (Present)", presentCount, StatusPresent)
                        AttendanceCountItem("अनुपस्थित (Absent)", absentCount, StatusAbsent)
                        AttendanceCountItem("विलंब (Late)", lateCount, StatusLate)
                        AttendanceCountItem("अवकाश (Leave)", leaveCount, StatusLeave)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // PDF Report & CSV Export Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                ReportExportUtils.printStudentReportCard(
                                    context = context,
                                    student = student,
                                    attendanceRecords = attendanceRecords,
                                    workoutRecords = emptyList(),
                                    trainingRecords = emptyList(),
                                    testAttempts = emptyList()
                                )
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, OliveTertiary)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Print,
                                contentDescription = "PDF",
                                tint = OliveTertiary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "PDF रिपोर्ट कार्ड",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = OliveTertiary
                            )
                        }

                        Button(
                            onClick = {
                                ReportExportUtils.shareStudentProgressSummary(
                                    context = context,
                                    student = student,
                                    attendanceRecords = attendanceRecords,
                                    workoutRecords = emptyList(),
                                    testAttempts = emptyList()
                                )
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "व्हाट्सएप शेयर",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        // Feature 1: Date Picker & Past Attendance Management Card
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
                                text = "📅 तारीख चुनें और हाजिरी दर्ज करें",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "चयनित दिनांक: $selectedDateStr ${if (selectedDateStr == todayDateStr) "(आज)" else "(पिछला रिकॉर्ड)"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (selectedDateStr == todayDateStr) SaffronDark else OliveTertiary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        // Current Status Badge for selected date
                        if (selectedDateAttendance != null) {
                            StatusBadge(status = selectedDateAttendance.status)
                        } else {
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "दर्ज नहीं",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Quick Date Chips Selector
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(quickDateOptions) { (label, dateVal) ->
                            val isSelected = selectedDateStr == dateVal
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedDateStr = dateVal },
                                label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SaffronContainer,
                                    selectedLabelColor = SaffronDark
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Status marking buttons for the selected date
                    val statuses = listOf(
                        "Present" to "उपस्थित (Present)",
                        "Late" to "विलंब (Late)",
                        "Leave" to "अवकाश (Leave)",
                        "Absent" to "अनुपस्थित (Absent)"
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        statuses.forEach { (statusKey, label) ->
                            val isSelected = selectedDateAttendance?.status == statusKey
                            val buttonColor = when (statusKey) {
                                "Present" -> StatusPresent
                                "Late" -> StatusLate
                                "Leave" -> StatusLeave
                                "Absent" -> StatusAbsent
                                else -> SaffronPrimary
                            }

                            OutlinedButton(
                                onClick = {
                                    if (selectedDateStr == todayDateStr) {
                                        onMarkTodayStatus(statusKey)
                                    } else {
                                        onMarkStatusForDate(selectedDateStr, statusKey, selectedRemarks)
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (isSelected) buttonColor.copy(alpha = 0.15f) else Color.Transparent
                                ),
                                border = ButtonDefaults.outlinedButtonBorder(enabled = true).let {
                                    if (isSelected) BorderStroke(1.5.dp, buttonColor)
                                    else it
                                },
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = statusKey,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) buttonColor else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }

        // Monthly Attendance Calendar Grid
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
                            text = "उपस्थिति कैलेंडर (Monthly Calendar)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = "Calendar",
                            tint = SaffronPrimary
                        )
                    }

                    Text(
                        text = "किसी भी तारीख पर टैप करके उस दिन की हाजिरी देखें व एडिट करें",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Day of week labels
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        listOf("सोम", "मंगल", "बुध", "गुरु", "शुक्र", "शनि", "रवि").forEach { d ->
                            Text(
                                text = d,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.width(36.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // 4 weeks of sample dates with colored dots
                    val attendanceMap = remember(attendanceRecords) {
                        attendanceRecords.associateBy { it.date }
                    }

                    for (week in 0..3) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            for (day in 1..7) {
                                val dayNum = week * 7 + day
                                val dayFormatted = if (dayNum < 10) "0$dayNum" else "$dayNum"
                                val currentCellDate = "2026-08-$dayFormatted"
                                val isCellSelected = selectedDateStr == currentCellDate
                                
                                val rec = attendanceMap[currentCellDate]
                                val status = rec?.status ?: when {
                                    dayNum in listOf(1, 3, 5, 8, 10, 12, 15, 17, 19, 21, 22, 24, 26) -> "Present"
                                    dayNum in listOf(7, 14, 21, 28) -> "Leave"
                                    dayNum in listOf(9, 23) -> "Late"
                                    dayNum in listOf(13, 27) -> "Absent"
                                    else -> "Present"
                                }

                                val dotColor = when (status) {
                                    "Present" -> StatusPresent
                                    "Absent" -> StatusAbsent
                                    "Late" -> StatusLate
                                    "Leave" -> StatusLeave
                                    else -> Color.Gray
                                }

                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isCellSelected) SaffronPrimary.copy(alpha = 0.25f)
                                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                        )
                                        .border(
                                            if (isCellSelected) 2.dp else 0.dp,
                                            if (isCellSelected) SaffronPrimary else Color.Transparent,
                                            CircleShape
                                        )
                                        .clickable {
                                            selectedDateStr = currentCellDate
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "$dayNum",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = if (isCellSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isCellSelected) SaffronDark else MaterialTheme.colorScheme.onSurface
                                        )
                                        Box(
                                            modifier = Modifier
                                                .size(4.dp)
                                                .clip(CircleShape)
                                                .background(dotColor)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Attendance History List
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "उपस्थिति इतिहास (All Records)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                TextButton(
                    onClick = {
                        ReportExportUtils.exportAttendanceCsv(
                            context = context,
                            selectedDate = selectedDateStr,
                            students = if (student != null) listOf(student) else emptyList(),
                            allAttendance = attendanceRecords
                        )
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.TableChart,
                        contentDescription = "CSV",
                        modifier = Modifier.size(16.dp),
                        tint = SaffronPrimary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("CSV शीट", style = MaterialTheme.typography.labelSmall, color = SaffronPrimary)
                }
            }
        }

        items(attendanceRecords) { record ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { selectedDateStr = record.date },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (record.date == selectedDateStr) SaffronContainer.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surface
                ),
                border = if (record.date == selectedDateStr) BorderStroke(1.dp, SaffronPrimary) else null
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "दिनांक: ${record.date}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (record.remarks.isNotEmpty()) {
                            Text(
                                text = record.remarks,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    StatusBadge(status = record.status)
                }
            }
        }
    }
}

@Composable
fun AttendanceCountItem(title: String, count: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "$count",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
