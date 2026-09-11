package com.example.ui.screens.attendance

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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
    onDeleteStatusForDate: (date: String) -> Unit = {},
    onNavigateToChat: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val sdf = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US) }
    val todayDateStr = remember { sdf.format(Date()) }

    // Calendar month/year navigation state
    val initialCal = remember { Calendar.getInstance() }
    var currentYear by remember { mutableStateOf(initialCal.get(Calendar.YEAR)) }
    var currentMonth by remember { mutableStateOf(initialCal.get(Calendar.MONTH)) } // 0-indexed: 0 = Jan, 8 = Sep

    // Selected inspection date state (defaults to today)
    var selectedDateStr by remember { mutableStateOf(todayDateStr) }
    var selectedRemarks by remember { mutableStateOf("") }
    var showDatePickerDialog by remember { mutableStateOf(false) }

    // Save feedback banner state
    var saveFeedbackMessage by remember { mutableStateOf<String?>(null) }

    // Hindi Month Names
    val hindiMonths = remember {
        listOf(
            "जनवरी", "फरवरी", "मार्च", "अप्रैल", "मई", "जून",
            "जुलाई", "अगस्त", "सितंबर", "अक्टूबर", "नवंबर", "दिसंबर"
        )
    }

    val currentMonthTitle = remember(currentYear, currentMonth) {
        val mName = hindiMonths.getOrElse(currentMonth) { "माह" }
        "$mName $currentYear"
    }

    // Records mapped by date for fast lookup
    val attendanceMap = remember(attendanceRecords, todayAttendance) {
        val map = attendanceRecords.associateBy { it.date }.toMutableMap()
        if (todayAttendance != null) {
            map[todayDateStr] = todayAttendance
        }
        map
    }

    // Currently inspected record
    val selectedRecord = remember(attendanceMap, selectedDateStr) {
        attendanceMap[selectedDateStr]
    }

    // Sync remarks when selected date changes
    LaunchedEffect(selectedDateStr, selectedRecord) {
        selectedRemarks = selectedRecord?.remarks ?: ""
    }

    // Selected month filter prefix: "YYYY-MM"
    val monthPrefix = remember(currentYear, currentMonth) {
        String.format(Locale.US, "%04d-%02d", currentYear, currentMonth + 1)
    }

    // Selected month attendance records
    val monthRecords = remember(attendanceRecords, monthPrefix) {
        attendanceRecords.filter { it.date.startsWith(monthPrefix) }
    }

    // Monthly stats
    val monthPresent = remember(monthRecords) { monthRecords.count { it.status == "Present" } }
    val monthAbsent = remember(monthRecords) { monthRecords.count { it.status == "Absent" } }
    val monthLate = remember(monthRecords) { monthRecords.count { it.status == "Late" } }
    val monthLeave = remember(monthRecords) { monthRecords.count { it.status == "Leave" } }
    val monthMarkedTotal = monthRecords.size
    val monthTotalDays = remember(currentYear, currentMonth) {
        val c = Calendar.getInstance().apply {
            set(Calendar.YEAR, currentYear)
            set(Calendar.MONTH, currentMonth)
            set(Calendar.DAY_OF_MONTH, 1)
        }
        c.getActualMaximum(Calendar.DAY_OF_MONTH)
    }
    val monthAttendancePct = if (monthMarkedTotal > 0) {
        ((monthPresent.toDouble() / monthMarkedTotal) * 100.0).toInt()
    } else 0

    // Streak calculation
    val streak = remember(attendanceRecords) {
        var count = 0
        for (rec in attendanceRecords.sortedByDescending { it.date }) {
            if (rec.status == "Present") count++ else break
        }
        count.coerceAtLeast(1)
    }

    // Calendar Grid Days computation
    data class CalendarDayItem(
        val dayOfMonth: Int,
        val dateStr: String,
        val isToday: Boolean,
        val isSelected: Boolean,
        val attendance: AttendanceRecord?
    )

    val calendarDays = remember(currentYear, currentMonth, attendanceMap, selectedDateStr, todayDateStr) {
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, currentYear)
            set(Calendar.MONTH, currentMonth)
            set(Calendar.DAY_OF_MONTH, 1)
        }
        // Week starts on Monday: Monday=0, Tuesday=1 ... Sunday=6
        // Calendar.DAY_OF_WEEK: Sunday=1, Monday=2, ... Saturday=7
        val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
        val leadingEmptySlots = (firstDayOfWeek + 5) % 7
        val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

        val items = mutableListOf<CalendarDayItem?>()
        for (i in 0 until leadingEmptySlots) {
            items.add(null)
        }

        for (d in 1..daysInMonth) {
            val dateStr = String.format(Locale.US, "%04d-%02d-%02d", currentYear, currentMonth + 1, d)
            val rec = attendanceMap[dateStr]
            items.add(
                CalendarDayItem(
                    dayOfMonth = d,
                    dateStr = dateStr,
                    isToday = (dateStr == todayDateStr),
                    isSelected = (dateStr == selectedDateStr),
                    attendance = rec
                )
            )
        }
        items
    }

    // Quick Date Options (Today, Yesterday, 2 days ago, 3 days ago, 1st of month)
    val quickDateOptions = remember(todayDateStr, currentYear, currentMonth) {
        val list = mutableListOf<Pair<String, String>>()
        val cal = Calendar.getInstance()
        list.add("आज (Today)" to sdf.format(cal.time))

        cal.add(Calendar.DAY_OF_YEAR, -1)
        list.add("कल (Yesterday)" to sdf.format(cal.time))

        cal.add(Calendar.DAY_OF_YEAR, -1)
        list.add("2 दिन पहले" to sdf.format(cal.time))

        cal.add(Calendar.DAY_OF_YEAR, -1)
        list.add("3 दिन पहले" to sdf.format(cal.time))

        val firstOfMonth = String.format(Locale.US, "%04d-%02d-01", currentYear, currentMonth + 1)
        list.add("माह की 1 तारीख" to firstOfMonth)

        list
    }

    // History list filter state: "MONTH" vs "ALL"
    var historyScopeFilter by remember { mutableStateOf("MONTH") } // "MONTH" or "ALL"
    var historyStatusFilter by remember { mutableStateOf("ALL") } // "ALL", "Present", "Absent", "Late", "Leave"

    val displayedHistoryRecords = remember(attendanceRecords, historyScopeFilter, historyStatusFilter, monthPrefix) {
        val baseList = if (historyScopeFilter == "MONTH") {
            attendanceRecords.filter { it.date.startsWith(monthPrefix) }
        } else {
            attendanceRecords
        }
        if (historyStatusFilter == "ALL") {
            baseList
        } else {
            baseList.filter { it.status == historyStatusFilter }
        }
    }

    // Material 3 Date Picker Dialog
    if (showDatePickerDialog) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = remember(selectedDateStr) {
                try {
                    SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(selectedDateStr)?.time ?: System.currentTimeMillis()
                } catch (e: Exception) {
                    System.currentTimeMillis()
                }
            }
        )

        DatePickerDialog(
            onDismissRequest = { showDatePickerDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val utcCal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                                timeInMillis = millis
                            }
                            val pickedDate = String.format(
                                Locale.US,
                                "%04d-%02d-%02d",
                                utcCal.get(Calendar.YEAR),
                                utcCal.get(Calendar.MONTH) + 1,
                                utcCal.get(Calendar.DAY_OF_MONTH)
                            )
                            selectedDateStr = pickedDate
                            currentYear = utcCal.get(Calendar.YEAR)
                            currentMonth = utcCal.get(Calendar.MONTH)
                        }
                        showDatePickerDialog = false
                    }
                ) {
                    Text("तारीख चुनें (OK)", fontWeight = FontWeight.Bold, color = SaffronPrimary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePickerDialog = false }) {
                    Text("रद्द करें (Cancel)")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Helper to format Hindi date display: "बुधवार, 09 सितंबर 2026"
    fun formatHindiDate(dateStr: String): String {
        return try {
            val date = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(dateStr) ?: return dateStr
            val cal = Calendar.getInstance().apply { time = date }
            val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
            val hindiDays = listOf("", "रविवार", "सोमवार", "मंगलवार", "बुधवार", "गुरुवार", "शुक्रवार", "शनिवार")
            val dayName = hindiDays.getOrElse(dayOfWeek) { "" }
            val dNum = cal.get(Calendar.DAY_OF_MONTH)
            val mNum = cal.get(Calendar.MONTH)
            val yNum = cal.get(Calendar.YEAR)
            val mName = hindiMonths.getOrElse(mNum) { "" }
            "$dayName, $dNum $mName $yNum"
        } catch (e: Exception) {
            dateStr
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("attendance_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header & Actions Card
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
                            Text(
                                text = "दैनिक उपस्थिति प्रबंधन (Daily Attendance)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "गाँव से सेना–पुलिस भर्ती अभियान • दिन-प्रतिदिन का सुरक्षित रिकॉर्ड",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        IconButton(
                            onClick = onNavigateToChat,
                            modifier = Modifier
                                .size(38.dp)
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

                    if (student != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = "Student",
                                        tint = SaffronPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "${student.fullName} (${student.studentId})",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(
                                    text = student.batchName.ifBlank { "अग्निवीर बैच" },
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                    color = SaffronDark,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // PDF & Share Export Buttons
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
                                text = "PDF रिपोर्ट",
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

        // Live Save Feedback Banner
        item {
            AnimatedVisibility(
                visible = saveFeedbackMessage != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = OliveContainer)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Saved",
                            tint = OliveTertiary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = saveFeedbackMessage ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = OnOliveContainer
                        )
                    }
                }
            }
        }

        // SECTION 1: Selected Date Inspector & Real-time Marking Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("date_inspector_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                border = BorderStroke(1.5.dp, SaffronPrimary.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Header with Date and Status
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "📅 तारीख-वार डेटा (Day Inspector)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = SaffronDark
                            )
                            Text(
                                text = formatHindiDate(selectedDateStr),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (selectedDateStr == todayDateStr) {
                                Text(
                                    text = "★ आज की तारीख (Current Date)",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                    color = SaffronPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Status Badge
                        if (selectedRecord != null) {
                            StatusBadge(status = selectedRecord.status)
                        } else {
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "दर्ज नहीं (No Data)",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Database Save Status Indicator Box
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (selectedRecord != null) OliveContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = BorderStroke(
                            1.dp,
                            if (selectedRecord != null) OliveTertiary.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outlineVariant
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (selectedRecord != null) Icons.Default.CloudDone else Icons.Default.CloudOff,
                                contentDescription = "DB Status",
                                tint = if (selectedRecord != null) OliveTertiary else Color.Gray,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (selectedRecord != null) "✅ डेटाबेस में सुरक्षित (Saved in Room DB)" else "⚠️ डेटाबेस में कोई डेटा दर्ज नहीं है",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selectedRecord != null) OliveTertiary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = if (selectedRecord != null) {
                                        "ID: #${selectedRecord.id} • तारीख: ${selectedRecord.date} • स्थिति: ${selectedRecord.status}"
                                    } else {
                                        "नीचे दिए गए बटनों से इस तारीख की हाजिरी दर्ज करें।"
                                    },
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "हाजिरी की स्थिति चुनें (Select Status to Save):",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // 4 Big Status Buttons
                    val statuses = listOf(
                        Triple("Present", "उपस्थित (Present)", StatusPresent),
                        Triple("Absent", "अनुपस्थित (Absent)", StatusAbsent),
                        Triple("Late", "विलंब (Late)", StatusLate),
                        Triple("Leave", "अवकाश (Leave)", StatusLeave)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        statuses.forEach { (statusKey, label, color) ->
                            val isSelected = selectedRecord?.status == statusKey

                            OutlinedButton(
                                onClick = {
                                    if (selectedDateStr == todayDateStr) {
                                        onMarkTodayStatus(statusKey)
                                    } else {
                                        onMarkStatusForDate(selectedDateStr, statusKey, selectedRemarks)
                                    }
                                    saveFeedbackMessage = "✅ दिनांक $selectedDateStr की उपस्थिति '$statusKey' डेटाबेस में सेव हो गई!"
                                    coroutineScope.launch {
                                        delay(3000)
                                        saveFeedbackMessage = null
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("mark_status_${statusKey.lowercase()}"),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (isSelected) color.copy(alpha = 0.2f) else Color.Transparent
                                ),
                                border = BorderStroke(
                                    if (isSelected) 2.dp else 1.dp,
                                    if (isSelected) color else MaterialTheme.colorScheme.outlineVariant
                                ),
                                contentPadding = PaddingValues(horizontal = 2.dp, vertical = 8.dp)
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = when (statusKey) {
                                            "Present" -> Icons.Default.Check
                                            "Absent" -> Icons.Default.Close
                                            "Late" -> Icons.Default.AccessTime
                                            "Leave" -> Icons.Default.BeachAccess
                                            else -> Icons.Default.Check
                                        },
                                        contentDescription = statusKey,
                                        tint = if (isSelected) color else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = statusKey,
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Normal,
                                        color = if (isSelected) color else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Remarks / Notes Input Field
                    OutlinedTextField(
                        value = selectedRemarks,
                        onValueChange = { selectedRemarks = it },
                        label = { Text("टिप्पणी / कारण (Remarks / Note)") },
                        placeholder = { Text("उदा. 1600m रनिंग 5:35, बीमारी, पारिवारिक कार्य") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodySmall,
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Save Remarks & Delete Action Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (selectedRecord != null) {
                            TextButton(
                                onClick = {
                                    onDeleteStatusForDate(selectedDateStr)
                                    saveFeedbackMessage = "🗑️ दिनांक $selectedDateStr का उपस्थिति रिकॉर्ड हटा दिया गया!"
                                    coroutineScope.launch {
                                        delay(3000)
                                        saveFeedbackMessage = null
                                    }
                                },
                                colors = ButtonDefaults.textButtonColors(contentColor = StatusAbsent)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("रिकॉर्ड हटाएं", style = MaterialTheme.typography.labelSmall)
                            }
                        } else {
                            Spacer(modifier = Modifier.width(1.dp))
                        }

                        Button(
                            onClick = {
                                val currentStatus = selectedRecord?.status ?: "Present"
                                onMarkStatusForDate(selectedDateStr, currentStatus, selectedRemarks)
                                saveFeedbackMessage = "✅ टिप्पणी सुरक्षित हो गई!"
                                coroutineScope.launch {
                                    delay(3000)
                                    saveFeedbackMessage = null
                                }
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Save,
                                contentDescription = "Save Note",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("सुरक्षित करें (Save)", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Quick Jump Chips
                    Text(
                        text = "त्वरित तारीख चयन (Quick Jump):",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(quickDateOptions) { (label, dateVal) ->
                            val isSelected = selectedDateStr == dateVal
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    selectedDateStr = dateVal
                                    try {
                                        val cal = Calendar.getInstance().apply {
                                            time = sdf.parse(dateVal) ?: Date()
                                        }
                                        currentYear = cal.get(Calendar.YEAR)
                                        currentMonth = cal.get(Calendar.MONTH)
                                    } catch (_: Exception) {}
                                },
                                label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SaffronContainer,
                                    selectedLabelColor = SaffronDark
                                )
                            )
                        }
                    }
                }
            }
        }

        // SECTION 2: Dynamic Month-by-Month Calendar Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("attendance_calendar_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Month Navigation Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Prev Month Button
                        IconButton(
                            onClick = {
                                if (currentMonth == 0) {
                                    currentMonth = 11
                                    currentYear -= 1
                                } else {
                                    currentMonth -= 1
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChevronLeft,
                                contentDescription = "Previous Month",
                                tint = SaffronPrimary
                            )
                        }

                        // Month & Year Title
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = currentMonthTitle,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "महीने की किसी भी तारीख पर टैप करें",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Next Month Button
                        IconButton(
                            onClick = {
                                if (currentMonth == 11) {
                                    currentMonth = 0
                                    currentYear += 1
                                } else {
                                    currentMonth += 1
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = "Next Month",
                                tint = SaffronPrimary
                            )
                        }
                    }

                    // Month Action Tools (Today & Pick Date)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = {
                                val now = Calendar.getInstance()
                                currentYear = now.get(Calendar.YEAR)
                                currentMonth = now.get(Calendar.MONTH)
                                selectedDateStr = todayDateStr
                            },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Today,
                                contentDescription = "Today",
                                modifier = Modifier.size(14.dp),
                                tint = SaffronPrimary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("आज (Current Month)", style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp))
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        FilledTonalButton(
                            onClick = { showDatePickerDialog = true },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = "Pick Date",
                                modifier = Modifier.size(14.dp),
                                tint = SaffronDark
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("तारीख चुनें (Pick Date)", style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp))
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Weekday Labels (Monday to Sunday)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        listOf("सोम", "मंगल", "बुध", "गुरु", "शुक्र", "शनि", "रवि").forEach { d ->
                            Text(
                                text = d,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                fontWeight = FontWeight.Bold,
                                color = if (d == "रवि") StatusAbsent else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(8.dp))

                    // Calendar Grid Matrix (7 columns)
                    val rows = calendarDays.chunked(7)

                    rows.forEach { week ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            for (i in 0 until 7) {
                                val dayItem = week.getOrNull(i)
                                if (dayItem == null) {
                                    // Empty slot for weekday offset
                                    Spacer(modifier = Modifier.weight(1f))
                                } else {
                                    val isSelected = dayItem.isSelected
                                    val isToday = dayItem.isToday
                                    val rec = dayItem.attendance
                                    val status = rec?.status

                                    val dotColor = when (status) {
                                        "Present" -> StatusPresent
                                        "Absent" -> StatusAbsent
                                        "Late" -> StatusLate
                                        "Leave" -> StatusLeave
                                        else -> null
                                    }

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .aspectRatio(1f)
                                            .padding(2.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                when {
                                                    isSelected -> SaffronContainer
                                                    dotColor != null -> dotColor.copy(alpha = 0.12f)
                                                    else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                                                }
                                            )
                                            .border(
                                                width = when {
                                                    isSelected -> 2.dp
                                                    isToday -> 1.5.dp
                                                    else -> 0.dp
                                                },
                                                color = when {
                                                    isSelected -> SaffronPrimary
                                                    isToday -> OliveTertiary
                                                    else -> Color.Transparent
                                                },
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .clickable {
                                                selectedDateStr = dayItem.dateStr
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Text(
                                                text = "${dayItem.dayOfMonth}",
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                                fontWeight = if (isSelected || isToday) FontWeight.ExtraBold else FontWeight.Normal,
                                                color = when {
                                                    isSelected -> SaffronDark
                                                    isToday -> OliveTertiary
                                                    else -> MaterialTheme.colorScheme.onSurface
                                                }
                                            )

                                            Spacer(modifier = Modifier.height(2.dp))

                                            if (dotColor != null) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(5.dp)
                                                        .clip(CircleShape)
                                                        .background(dotColor)
                                                )
                                            } else {
                                                // Unmarked subtle dot
                                                Box(
                                                    modifier = Modifier
                                                        .size(3.dp)
                                                        .clip(CircleShape)
                                                        .background(Color.Gray.copy(alpha = 0.3f))
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Calendar Color Legend
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        LegendItem("उपस्थित", StatusPresent)
                        LegendItem("अनुपस्थित", StatusAbsent)
                        LegendItem("विलंब", StatusLate)
                        LegendItem("अवकाश", StatusLeave)
                        LegendItem("दर्ज नहीं", Color.Gray.copy(alpha = 0.5f))
                    }
                }
            }
        }

        // SECTION 3: Monthly Statistics Overview Card for the Selected Month
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "📊 $currentMonthTitle — उपस्थिति आँकड़े",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Month Attendance %
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
                                    text = "$monthAttendancePct%",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = OliveTertiary
                                )
                                Text(
                                    text = "माह उपस्थिति %",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = OnOliveContainer
                                )
                            }
                        }

                        // Streak Card
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
                                        modifier = Modifier.size(22.dp)
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
                                    text = "लगातार स्ट्रीक",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = OnSaffronContainer
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Month breakdown counts
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        AttendanceCountItem("उपस्थित", monthPresent, StatusPresent)
                        AttendanceCountItem("अनुपस्थित", monthAbsent, StatusAbsent)
                        AttendanceCountItem("विलंब", monthLate, StatusLate)
                        AttendanceCountItem("अवकाश", monthLeave, StatusLeave)
                        AttendanceCountItem("कुल दर्ज", monthMarkedTotal, OliveTertiary)
                    }
                }
            }
        }

        // SECTION 4: Day-by-Day Attendance History Table
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "उपस्थिति इतिहास (All Records)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "डेटाबेस में दर्ज सभी तारीखों का विवरण",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

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

        // Filter Controls for History
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                // Scope Filter: This Month vs All Time
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = historyScopeFilter == "MONTH",
                        onClick = { historyScopeFilter = "MONTH" },
                        label = { Text("इस माह ($currentMonthTitle)") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SaffronContainer,
                            selectedLabelColor = SaffronDark
                        )
                    )
                    FilterChip(
                        selected = historyScopeFilter == "ALL",
                        onClick = { historyScopeFilter = "ALL" },
                        label = { Text("सभी माह (${attendanceRecords.size})") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SaffronContainer,
                            selectedLabelColor = SaffronDark
                        )
                    )
                }

                // Status Filter Chips
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    val statusChips = listOf("ALL" to "सभी", "Present" to "उपस्थित", "Absent" to "अनुपस्थित", "Late" to "विलंब", "Leave" to "अवकाश")
                    items(statusChips) { (key, label) ->
                        FilterChip(
                            selected = historyStatusFilter == key,
                            onClick = { historyStatusFilter = key },
                            label = { Text(label, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
            }
        }

        // History items
        if (displayedHistoryRecords.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.EventBusy,
                            contentDescription = "No data",
                            tint = Color.Gray,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "इस फिल्टर में कोई रिकॉर्ड नहीं मिला",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "ऊपर कैलेंडर से तारीख चुनकर उपस्थिति दर्ज करें।",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(displayedHistoryRecords) { record ->
                val isCurrentSelected = (record.date == selectedDateStr)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            selectedDateStr = record.date
                            try {
                                val c = Calendar.getInstance().apply {
                                    time = sdf.parse(record.date) ?: Date()
                                }
                                currentYear = c.get(Calendar.YEAR)
                                currentMonth = c.get(Calendar.MONTH)
                            } catch (_: Exception) {}
                        },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isCurrentSelected) SaffronContainer.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surface
                    ),
                    border = if (isCurrentSelected) BorderStroke(1.5.dp, SaffronPrimary) else null,
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = formatHindiDate(record.date),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = OliveContainer.copy(alpha = 0.6f),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "Room DB #${record.id}",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                        color = OliveTertiary,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            if (record.remarks.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "📝 ${record.remarks}",
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
}

@Composable
fun LegendItem(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
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
