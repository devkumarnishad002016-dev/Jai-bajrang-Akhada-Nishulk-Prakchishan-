package com.example.ui.screens.monthly

import android.content.Context
import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.analytics.monthly.*
import com.example.ui.theme.*
import com.example.util.RolePermissionManager
import com.example.util.pdf.PdfReportCardGenerator
import com.example.util.pdf.PdfShareManager
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthlyPerformanceScreen(
    monthlyReport: MonthlyPerformanceReport?,
    currentRole: String,
    activeStudentId: String,
    selectedMonthKey: String,
    onSelectMonth: (String) -> Unit,
    onSelectPreviousMonth: () -> Unit,
    onSelectNextMonth: () -> Unit,
    onSelectCurrentMonth: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedTab by remember { mutableIntStateOf(0) } // 0: कैडेट मेरिट सूची, 1: विजुअल ट्रेंड्स व चार्ट्स, 2: 1600m ट्रायल सारांश
    var selectedCadetForDialog by remember { mutableStateOf<CadetMonthlyPhysicalPerformance?>(null) }
    var isGeneratingPdf by remember { mutableStateOf(false) }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }

    val isStudent = RolePermissionManager.isStudent(currentRole)
    val isStaff = RolePermissionManager.isStaff(currentRole)

    // Filter cadets according to RBAC and search query
    val visibleCadets = remember(monthlyReport, currentRole, activeStudentId, searchQuery) {
        val allCadets = monthlyReport?.cadetReports ?: emptyList()
        if (isStudent) {
            allCadets.filter { it.studentId.equals(activeStudentId, ignoreCase = true) }
        } else {
            if (searchQuery.isBlank()) {
                allCadets
            } else {
                allCadets.filter {
                    it.studentName.contains(searchQuery, ignoreCase = true) ||
                    it.studentId.contains(searchQuery, ignoreCase = true) ||
                    it.chestNumber.contains(searchQuery, ignoreCase = true) ||
                    it.village.contains(searchQuery, ignoreCase = true)
                }
            }
        }
    }

    val summary = monthlyReport?.summary

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (isStudent) "मेरा मासिक रिपोर्ट कार्ड (My Report)" else "मासिक कैडेट प्रदर्शन व रिपोर्ट",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = monthlyReport?.monthLabel ?: "लोड हो रहा है...",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("btn_back_monthly")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "वापस जाएं",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    if (isStaff && monthlyReport != null && monthlyReport.cadetReports.isNotEmpty()) {
                        IconButton(
                            onClick = {
                                isGeneratingPdf = true
                                try {
                                    val bulkPdf = PdfReportCardGenerator.generateBulkAcademyReportPdf(context, monthlyReport)
                                    PdfShareManager.sharePdfFile(context, bulkPdf, "जय बजरंग अखाड़ा - समग्र मासिक रिपोर्ट (${monthlyReport.monthLabel})")
                                    snackbarMessage = "पूरी अकादमी का PDF तैयार हुआ!"
                                } catch (e: Exception) {
                                    snackbarMessage = "PDF निर्माण में त्रुटि: ${e.message}"
                                } finally {
                                    isGeneratingPdf = false
                                }
                            },
                            modifier = Modifier.testTag("btn_bulk_pdf")
                        ) {
                            Icon(
                                imageVector = Icons.Default.PictureAsPdf,
                                contentDescription = "बल्क रिपोर्ट डाउनलोड",
                                tint = Color.White
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SaffronPrimary
                )
            )
        },
        snackbarHost = {
            snackbarMessage?.let { msg ->
                Snackbar(
                    modifier = Modifier.padding(16.dp),
                    action = {
                        TextButton(onClick = { snackbarMessage = null }) {
                            Text("ठीक है", color = SaffronPrimary)
                        }
                    }
                ) {
                    Text(msg)
                }
            }
        },
        modifier = modifier.fillMaxSize().testTag("monthly_performance_screen")
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Month Selector Header Bar
            item {
                MonthSelectorHeader(
                    currentMonthLabel = monthlyReport?.monthLabel ?: "अगस्त 2026",
                    onPrevious = onSelectPreviousMonth,
                    onNext = onSelectNextMonth,
                    onCurrentMonth = onSelectCurrentMonth
                )
            }

            // 2. Academy Summary Cards (Only for Staff or Overview for Student)
            if (summary != null) {
                item {
                    MonthlySummaryGrid(
                        summary = summary,
                        isStudent = isStudent,
                        cadetReport = visibleCadets.firstOrNull()
                    )
                }
            }

            // 3. Navigation Tabs
            if (!isStudent) {
                item {
                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = SaffronPrimary,
                        modifier = Modifier.padding(horizontal = 16.dp).clip(RoundedCornerShape(12.dp))
                    ) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            text = { Text("कैडेट सूची (${visibleCadets.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = { Text("ट्रेंड व चार्ट्स 📊", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                        )
                        Tab(
                            selected = selectedTab == 2,
                            onClick = { selectedTab = 2 },
                            text = { Text("1600m ट्रायल सारांश ⏱️", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                        )
                    }
                }
            }

            // Content per Tab
            when {
                isStudent -> {
                    // Student direct personal report card
                    val myReport = visibleCadets.firstOrNull()
                    if (myReport != null) {
                        item {
                            StudentPersonalReportCardView(
                                report = myReport,
                                monthLabel = monthlyReport?.monthLabel ?: "अगस्त 2026",
                                onGeneratePdf = {
                                    val pdf = PdfReportCardGenerator.generateCadetReportPdf(
                                        context = context,
                                        report = myReport,
                                        monthLabel = monthlyReport?.monthLabel ?: "अगस्त 2026",
                                        monthKey = selectedMonthKey
                                    )
                                    PdfShareManager.sharePdfFile(context, pdf, "जय बजरंग अखाड़ा - मेरा मासिक रिपोर्ट कार्ड")
                                },
                                onPrintPdf = {
                                    val pdf = PdfReportCardGenerator.generateCadetReportPdf(
                                        context = context,
                                        report = myReport,
                                        monthLabel = monthlyReport?.monthLabel ?: "अगस्त 2026",
                                        monthKey = selectedMonthKey
                                    )
                                    PdfShareManager.printPdfFile(context, pdf, "Report_${myReport.studentId}")
                                }
                            )
                        }
                    } else {
                        item {
                            EmptyStateCard(message = "इस माह के लिए कोई रिकॉर्ड उपलब्ध नहीं है।")
                        }
                    }
                }

                selectedTab == 0 -> {
                    // TAB 0: Cadets Merit List
                    item {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("नाम, चेस्ट नंबर या छात्र ID से खोजें...") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(Icons.Default.Close, contentDescription = "Clear")
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .testTag("input_search_monthly_cadets")
                        )
                    }

                    if (visibleCadets.isEmpty()) {
                        item {
                            EmptyStateCard(message = "कोई कैडेट रिकॉर्ड नहीं मिला।")
                        }
                    } else {
                        items(visibleCadets, key = { it.studentId }) { cadet ->
                            CadetMonthlyPerformanceCard(
                                cadet = cadet,
                                onClick = { selectedCadetForDialog = cadet },
                                onQuickPdf = {
                                    val pdf = PdfReportCardGenerator.generateCadetReportPdf(
                                        context = context,
                                        report = cadet,
                                        monthLabel = monthlyReport?.monthLabel ?: "अगस्त 2026",
                                        monthKey = selectedMonthKey
                                    )
                                    PdfShareManager.sharePdfFile(context, pdf, "जय बजरंग अखाड़ा - ${cadet.studentName} का रिपोर्ट कार्ड")
                                }
                            )
                        }
                    }
                }

                selectedTab == 1 -> {
                    // TAB 1: Visual Charts & Analytics
                    item {
                        MonthlyVisualChartsSection(
                            cadets = monthlyReport?.cadetReports ?: emptyList(),
                            summary = summary
                        )
                    }
                }

                selectedTab == 2 -> {
                    // TAB 2: 1600m Trial Breakdown
                    item {
                        RunningTrialsSummarySection(
                            cadets = monthlyReport?.cadetReports ?: emptyList(),
                            summary = summary
                        )
                    }
                }
            }
        }
    }

    // Detail Dialog for Cadet
    selectedCadetForDialog?.let { cadet ->
        CadetDetailReportDialog(
            report = cadet,
            monthLabel = monthlyReport?.monthLabel ?: "अगस्त 2026",
            onDismiss = { selectedCadetForDialog = null },
            onSharePdf = {
                val pdf = PdfReportCardGenerator.generateCadetReportPdf(
                    context = context,
                    report = cadet,
                    monthLabel = monthlyReport?.monthLabel ?: "अगस्त 2026",
                    monthKey = selectedMonthKey
                )
                PdfShareManager.sharePdfFile(context, pdf, "जय बजरंग अखाड़ा - ${cadet.studentName} का रिपोर्ट कार्ड")
            },
            onPrintPdf = {
                val pdf = PdfReportCardGenerator.generateCadetReportPdf(
                    context = context,
                    report = cadet,
                    monthLabel = monthlyReport?.monthLabel ?: "अगस्त 2026",
                    monthKey = selectedMonthKey
                )
                PdfShareManager.printPdfFile(context, pdf, "Report_${cadet.studentId}")
            }
        )
    }
}

@Composable
fun MonthSelectorHeader(
    currentMonthLabel: String,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onCurrentMonth: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = onPrevious,
                modifier = Modifier.testTag("btn_prev_month")
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "पिछला माह", tint = SaffronPrimary)
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = currentMonthLabel,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = NavySecondary
                )
                Text(
                    text = "चालू माह चुनें (Current)",
                    style = MaterialTheme.typography.labelSmall,
                    color = SaffronDark,
                    modifier = Modifier
                        .clickable { onCurrentMonth() }
                        .padding(top = 2.dp)
                )
            }

            IconButton(
                onClick = onNext,
                modifier = Modifier.testTag("btn_next_month")
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "अगला माह", tint = SaffronPrimary)
            }
        }
    }
}

@Composable
fun MonthlySummaryGrid(
    summary: MonthlyDashboardSummary,
    isStudent: Boolean,
    cadetReport: CadetMonthlyPhysicalPerformance?
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = if (isStudent) "माह का मुख्य अवलोकन (Monthly Summary)" else "अकादमी मासिक सांख्यिकी (Academy Statistics)",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MetricCard(
                title = if (isStudent) "मासिक उपस्थिति" else "औसत उपस्थिति",
                value = if (isStudent && cadetReport != null) String.format(Locale.US, "%.1f%%", cadetReport.attendance.attendancePercentage)
                        else String.format(Locale.US, "%.1f%%", summary.averageAttendancePercentage),
                subtext = if (isStudent && cadetReport != null) "${cadetReport.attendance.presentCount}/${cadetReport.attendance.totalTrainingDays} दिन उपस्थित"
                          else "${summary.activeCadets} सक्रिय कैडेट्स",
                icon = Icons.Default.FactCheck,
                badgeColor = Color(0xFF16A34A),
                modifier = Modifier.weight(1f)
            )

            MetricCard(
                title = if (isStudent) "1600m बेस्ट" else "1600m सर्वश्रेष्ठ",
                value = if (isStudent && cadetReport != null) cadetReport.time1600mBest
                        else summary.best1600mFormatted,
                subtext = if (isStudent && cadetReport != null) cadetReport.time1600mImprovement.diffText
                          else summary.best1600mCadetName.take(14),
                icon = Icons.Default.Timer,
                badgeColor = Color(0xFFDC2626),
                modifier = Modifier.weight(1f)
            )
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MetricCard(
                title = if (isStudent) "पुश-अप्स" else "औसत पुश-अप्स",
                value = if (isStudent && cadetReport != null) "${cadetReport.pushupsBest} reps"
                        else String.format(Locale.US, "%.0f reps", summary.averagePushups),
                subtext = if (isStudent && cadetReport != null) cadetReport.pushupsImprovement.diffText
                          else "स्ट्रेंथ ड्रिल",
                icon = Icons.Default.FitnessCenter,
                badgeColor = Color(0xFF7C3AED),
                modifier = Modifier.weight(1f)
            )

            MetricCard(
                title = if (isStudent) "अकादमी रैंक" else "ग्राउंड टेस्ट व रेस",
                value = if (isStudent && cadetReport != null) "#${cadetReport.overallRank}"
                        else "${summary.totalPhysicalTests} रेस सत्र",
                subtext = if (isStudent && cadetReport != null) cadetReport.performanceGrade
                          else "${summary.totalTrainingSessions} प्रशिक्षण दिन",
                icon = Icons.Default.EmojiEvents,
                badgeColor = Color(0xFFEA580C),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    subtext: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    badgeColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(badgeColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = badgeColor, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(text = title, style = MaterialTheme.typography.labelSmall, color = Color.Gray, maxLines = 1)
                Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Text(text = subtext, style = MaterialTheme.typography.bodySmall, fontSize = 10.sp, color = badgeColor, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
fun CadetMonthlyPerformanceCard(
    cadet: CadetMonthlyPhysicalPerformance,
    onClick: () -> Unit,
    onQuickPdf: () -> Unit
) {
    val rankBadgeColor = when (cadet.overallRank) {
        1 -> Color(0xFFF59E0B) // Gold
        2 -> Color(0xFF94A3B8) // Silver
        3 -> Color(0xFFB45309) // Bronze
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 3.dp)
            .clickable { onClick() }
            .testTag("card_cadet_${cadet.studentId}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rank Circle
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(rankBadgeColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (cadet.overallRank in 1..3) when(cadet.overallRank) { 1 -> "🥇"; 2 -> "🥈"; else -> "🥉" }
                           else "#${cadet.overallRank}",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp,
                    color = if (cadet.overallRank in 1..3) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = cadet.studentName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        color = SaffronPrimary.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "#${cadet.chestNumber.ifBlank { cadet.studentId.takeLast(3) }}",
                            style = MaterialTheme.typography.labelSmall,
                            color = SaffronDark,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "1600m: ${cadet.time1600mBest}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFDC2626)
                    )
                    Text(
                        text = "उपस्थिति: ${String.format(Locale.US, "%.0f%%", cadet.attendance.attendancePercentage)}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF16A34A)
                    )
                    Text(
                        text = "पुश: ${cadet.pushupsBest}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Improvement Chip
                if (cadet.time1600mImprovement.status == ImprovementStatus.IMPROVED) {
                    Text(
                        text = "🔥 1600m: ${cadet.time1600mImprovement.diffText}",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        color = Color(0xFF16A34A),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            IconButton(
                onClick = onQuickPdf,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PictureAsPdf,
                    contentDescription = "PDF शेयर",
                    tint = SaffronPrimary
                )
            }
        }
    }
}

@Composable
fun StudentPersonalReportCardView(
    report: CadetMonthlyPhysicalPerformance,
    monthLabel: String,
    onGeneratePdf: () -> Unit,
    onPrintPdf: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = report.studentName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = NavySecondary
                    )
                    Text(
                        text = "आईडी: ${report.studentId} | चेस्ट: #${report.chestNumber.ifBlank { report.studentId.takeLast(3) }}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
                Surface(
                    color = Color(0xFFFEF3C7),
                    shape = RoundedCornerShape(8.dp),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Text(
                        text = "रैंक #${report.overallRank}",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFD97706),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            HorizontalDivider()

            // Attendance Section
            Text("1. मासिक उपस्थिति विवरण", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                AttMetricItem("कुल दिन", "${report.attendance.totalTrainingDays}")
                AttMetricItem("उपस्थित", "${report.attendance.presentCount}", Color(0xFF16A34A))
                AttMetricItem("अनुपस्थित", "${report.attendance.absentCount}", Color(0xFFDC2626))
                AttMetricItem("उपस्थिति %", String.format(Locale.US, "%.1f%%", report.attendance.attendancePercentage), SaffronPrimary)
                AttMetricItem("बेस्ट स्ट्रीक", "${report.attendance.bestStreak} दिन", Color(0xFF7C3AED))
            }

            HorizontalDivider()

            // Physical Performance Section
            Text("2. शारीरिक परीक्षण व सुधार (Physical Performance)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                PerformanceMetricRow("1600m रनिंग", report.time1600mLatest, report.time1600mBest, report.time1600mImprovement.diffText, report.time1600mImprovement.status)
                PerformanceMetricRow("पुश-अप्स (Push-ups)", "${report.pushupsLatest} reps", "${report.pushupsBest} reps", report.pushupsImprovement.diffText, report.pushupsImprovement.status)
                PerformanceMetricRow("बीम / पुल-अप्स", "${report.pullupsLatest} reps", "${report.pullupsBest} reps", report.pullupsImprovement.diffText, report.pullupsImprovement.status)
                PerformanceMetricRow("सिट-अप्स", "${report.situpsLatest} reps", "-", "दैनिक ड्रिल", ImprovementStatus.SAME)
                PerformanceMetricRow("लंबी कूद", if (report.longJumpFeet > 0) "${report.longJumpFeet} ft" else "15.5 ft", "-", "पास", ImprovementStatus.IMPROVED)
            }

            HorizontalDivider()

            // Action Buttons
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = onGeneratePdf,
                    modifier = Modifier.weight(1f).testTag("btn_student_share_pdf"),
                    colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("PDF शेयर करें")
                }

                OutlinedButton(
                    onClick = onPrintPdf,
                    modifier = Modifier.weight(1f).testTag("btn_student_print_pdf"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("प्रिंट करें")
                }
            }
        }
    }
}

@Composable
fun AttMetricItem(label: String, value: String, color: Color = MaterialTheme.colorScheme.onSurface) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontSize = 9.sp)
        Text(text = value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = color)
    }
}

@Composable
fun PerformanceMetricRow(
    title: String,
    latest: String,
    best: String,
    improvementText: String,
    status: ImprovementStatus
) {
    val statusColor = Color(status.badgeColorHex)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1.2f)) {
            Text(text = title, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
            Text(text = "वर्तमान: $latest | बेस्ट: $best", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontSize = 10.sp)
        }
        Surface(
            color = statusColor.copy(alpha = 0.15f),
            shape = RoundedCornerShape(4.dp)
        ) {
            Text(
                text = improvementText,
                style = MaterialTheme.typography.labelSmall,
                color = statusColor,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                fontSize = 10.sp
            )
        }
    }
}

@Composable
fun MonthlyVisualChartsSection(
    cadets: List<CadetMonthlyPhysicalPerformance>,
    summary: MonthlyDashboardSummary?
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Attendance Bar Chart
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "📊 उपस्थिति वितरण (Attendance Distribution)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                // Render simple custom Compose Canvas chart
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                ) {
                    val width = size.width
                    val height = size.height
                    val barWidth = (width / (cadets.take(8).size.coerceAtLeast(1) * 1.5f)).coerceIn(16f, 36f)

                    cadets.take(8).forEachIndexed { i, cadet ->
                        val pct = (cadet.attendance.attendancePercentage / 100f).coerceIn(0.0, 1.0).toFloat()
                        val barHeight = (height - 30f) * pct
                        val x = 20f + i * (barWidth * 1.4f)
                        val y = height - 20f - barHeight

                        drawRect(
                            color = if (pct >= 0.8f) Color(0xFF16A34A) else if (pct >= 0.6f) Color(0xFFF59E0B) else Color(0xFFDC2626),
                            topLeft = Offset(x, y),
                            size = Size(barWidth, barHeight)
                        )
                    }

                    // Baseline
                    drawLine(
                        color = Color.LightGray,
                        start = Offset(10f, height - 20f),
                        end = Offset(width - 10f, height - 20f),
                        strokeWidth = 2f
                    )
                }

                Text(
                    text = "शीर्ष 8 कैडेट्स की मासिक उपस्थिति प्रतिशत (हरी पट्टी = 80%+)",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }
        }

        // 1600m Running Progression Chart
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "⏱️ 1600m टाइमिंग तुलना (कम समय = बेहतर प्रदर्शन)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                cadets.take(6).forEach { cadet ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(cadet.studentName.take(14), style = MaterialTheme.typography.bodySmall, modifier = Modifier.width(100.dp))
                        LinearProgressIndicator(
                            progress = {
                                val sec = MonthlyPerformanceEngine.parseTimeToSeconds(cadet.time1600mBest)
                                if (sec < 600) (1.0f - (sec / 600f)).toFloat().coerceIn(0.1f, 1f) else 0.2f
                            },
                            modifier = Modifier.weight(1f).height(8.dp).clip(RoundedCornerShape(4.dp)),
                            color = Color(0xFFDC2626),
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(cadet.time1600mBest, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun RunningTrialsSummarySection(
    cadets: List<CadetMonthlyPhysicalPerformance>,
    summary: MonthlyDashboardSummary?
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "🏁 1600m ग्राउंड ट्रायल एवं रेस रिकॉर्ड्स",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("सत्र का सर्वश्रेष्ठ", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Text(summary?.best1600mFormatted ?: "-", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFFDC2626))
                    Text(summary?.best1600mCadetName ?: "-", style = MaterialTheme.typography.labelSmall)
                }

                Column {
                    Text("अकादमी औसत", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Text(summary?.average1600mFormatted ?: "-", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = SaffronPrimary)
                    Text("सभी कैडेट्स", style = MaterialTheme.typography.labelSmall)
                }

                Column {
                    Text("कुल रेस सत्र", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Text("${summary?.totalPhysicalTests ?: 0}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF16A34A))
                    Text("ट्रायल संपन्न", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

@Composable
fun CadetDetailReportDialog(
    report: CadetMonthlyPhysicalPerformance,
    monthLabel: String,
    onDismiss: () -> Unit,
    onSharePdf: () -> Unit,
    onPrintPdf: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.88f),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = report.studentName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = NavySecondary
                        )
                        Text(
                            text = "माह: $monthLabel | JBA: ${report.studentId}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "बंद करें")
                    }
                }

                HorizontalDivider()

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        // Attendance details
                        Text("1. उपस्थिति सारांश", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            AttMetricItem("कुल दिन", "${report.attendance.totalTrainingDays}")
                            AttMetricItem("उपस्थित", "${report.attendance.presentCount}", Color(0xFF16A34A))
                            AttMetricItem("अनुपस्थित", "${report.attendance.absentCount}", Color(0xFFDC2626))
                            AttMetricItem("उपस्थिति %", String.format(Locale.US, "%.1f%%", report.attendance.attendancePercentage), SaffronPrimary)
                            AttMetricItem("बेस्ट स्ट्रीक", "${report.attendance.bestStreak} दिन", Color(0xFF7C3AED))
                        }
                    }

                    item {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        Text("2. शारीरिक प्रदर्शन व मासिक सुधार", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            PerformanceMetricRow("1600m रनिंग", report.time1600mLatest, report.time1600mBest, report.time1600mImprovement.diffText, report.time1600mImprovement.status)
                            PerformanceMetricRow("100m स्प्रिंट", report.time100mLatest, report.time100mBest, "मानक गति", ImprovementStatus.SAME)
                            PerformanceMetricRow("पुश-अप्स", "${report.pushupsLatest} reps", "${report.pushupsBest} reps", report.pushupsImprovement.diffText, report.pushupsImprovement.status)
                            PerformanceMetricRow("बीम / पुल-अप्स", "${report.pullupsLatest} reps", "${report.pullupsBest} reps", report.pullupsImprovement.diffText, report.pullupsImprovement.status)
                            PerformanceMetricRow("लंबी कूद (Long Jump)", if (report.longJumpFeet > 0) "${report.longJumpFeet} ft" else "15.5 ft", "-", "क्वालिफाइड", ImprovementStatus.IMPROVED)
                        }
                    }

                    if (report.raceHistory.isNotEmpty()) {
                        item {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                            Text("3. 1600m ग्राउंड रेस ट्रायल (${report.raceHistory.size} सत्र)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                            report.raceHistory.forEach { r ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("1600m ट्रायल: ${r.timeFormatted}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                                    Text(if (r.rank > 0) "रैंक #${r.rank}" else r.status, style = MaterialTheme.typography.labelSmall, color = SaffronDark)
                                }
                            }
                        }
                    }
                }

                HorizontalDivider()

                // Dialog Action Buttons
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onSharePdf,
                        modifier = Modifier.weight(1f).testTag("btn_dialog_share_pdf"),
                        colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("PDF शेयर करें", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = onPrintPdf,
                        modifier = Modifier.weight(1f).testTag("btn_dialog_print_pdf"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("प्रिंट करें", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyStateCard(message: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 20.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.Assessment, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(44.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = message, style = MaterialTheme.typography.bodyMedium, color = Color.Gray, textAlign = TextAlign.Center)
        }
    }
}
