package com.example.ui.screens.progress

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ai.AiCoachInsight
import com.example.data.ai.PerformanceScoreBreakdown
import com.example.data.analytics.*
import com.example.data.model.StudentProfile
import com.example.data.model.StudyAttempt
import com.example.data.model.TestAttempt
import com.example.data.model.WorkoutRecord
import com.example.ui.theme.*
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressAndAiCoachScreen(
    student: StudentProfile?,
    performanceScore: PerformanceScoreBreakdown,
    aiCoachInsights: List<AiCoachInsight>,
    workouts: List<WorkoutRecord>,
    testAttempts: List<TestAttempt>,
    studyAttempts: List<StudyAttempt> = emptyList(),
    unifiedState: UnifiedPerformanceState? = null,
    onNavigateToPractice: ((String) -> Unit)? = null,
    onNavigateToMockTest: ((Long) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("समग्र रिपोर्ट", "विषय व टॉपिक", "ट्रेंड व इतिहास", "AI कोच व फिटनेस")

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("progress_ai_coach_screen")
    ) {
        // Top Navigation Tabs
        PrimaryTabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = SaffronPrimary,
            modifier = Modifier.fillMaxWidth()
        ) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = title,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 12.sp,
                            maxLines = 1
                        )
                    }
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (selectedTab) {
                0 -> {
                    // TAB 0: OVERALL PERFORMANCE & TODAY & WEEKLY
                    item {
                        UnifiedPerformanceScoreCard(
                            unifiedScore = unifiedState?.overallScore,
                            legacyScore = performanceScore
                        )
                    }

                    item {
                        QuickMetricsGrid(
                            unifiedState = unifiedState,
                            testAttempts = testAttempts,
                            studyAttempts = studyAttempts
                        )
                    }

                    item {
                        TodayProgressCard(
                            today = unifiedState?.todayProgress
                        )
                    }

                    item {
                        WeeklyConsistencyCard(
                            weekly = unifiedState?.weeklyProgress
                        )
                    }

                    item {
                        ActionableRecommendationsSection(
                            recommendations = unifiedState?.recommendations ?: emptyList()
                        )
                    }
                }

                1 -> {
                    // TAB 1: SUBJECTS & TOPIC WEAKNESS
                    item {
                        SubjectAnalyticsSection(
                            subjects = unifiedState?.subjectAnalyticsList ?: emptyList()
                        )
                    }

                    item {
                        TopicIntelligenceSection(
                            strongTopics = unifiedState?.strongTopics ?: emptyList(),
                            averageTopics = unifiedState?.averageTopics ?: emptyList(),
                            weakTopics = unifiedState?.weakTopics ?: emptyList(),
                            needPracticeTopics = unifiedState?.needPracticeTopics ?: emptyList(),
                            mostPracticedTopics = unifiedState?.mostPracticedTopics ?: emptyList()
                        )
                    }
                }

                2 -> {
                    // TAB 2: TRENDS & PERFORMANCE HISTORY
                    item {
                        QuizAndMockTrendsSection(
                            quizTrend = unifiedState?.quizTrend,
                            mockTrend = unifiedState?.mockTrend
                        )
                    }

                    item {
                        ChronologicalHistorySection(
                            testAttempts = testAttempts,
                            studyAttempts = studyAttempts
                        )
                    }
                }

                3 -> {
                    // TAB 3: AI COACH & PHYSICAL READINESS
                    item {
                        AiCoachHeaderCard()
                    }

                    items(aiCoachInsights) { insight ->
                        AiCoachInsightItemCard(insight = insight)
                    }

                    item {
                        PhysicalReadinessStandardsCard(
                            student = student,
                            workouts = workouts
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// SECTION 1: OVERALL SCORE & WEIGHTAGE CARD
// -------------------------------------------------------------
@Composable
fun UnifiedPerformanceScoreCard(
    unifiedScore: UnifiedPerformanceScore?,
    legacyScore: PerformanceScoreBreakdown
) {
    val totalScore = unifiedScore?.overallScore ?: legacyScore.totalScore
    val grade = unifiedScore?.grade ?: legacyScore.grade
    val remarks = unifiedScore?.remarks ?: legacyScore.remarks

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "समग्र प्रदर्शन स्कोर (Performance Score)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "ग्रेड: $grade",
                        style = MaterialTheme.typography.bodySmall,
                        color = SaffronPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Big Score Ring Badge
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(SaffronContainer, SaffronPrimary.copy(alpha = 0.2f))
                            )
                        )
                        .border(3.dp, SaffronPrimary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$totalScore",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = SaffronPrimary
                        )
                        Text(
                            text = "100 में से",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = remarks,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "स्कोर विभाजन एवं भार (Weightage)",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (unifiedScore?.isNormalized == true) {
                    Surface(
                        color = SaffronContainer.copy(alpha = 0.7f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "उपलब्ध डेटा अनुसार पुनर्संतुलित",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            color = SaffronPrimary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            val studyScore = unifiedScore?.studyComponentScore ?: legacyScore.studyScore
            val mockScore = unifiedScore?.mockTestComponentScore ?: legacyScore.mockTestScore
            val consScore = unifiedScore?.consistencyComponentScore ?: legacyScore.attendanceScore
            val physScore = unifiedScore?.physicalAttendanceComponentScore ?: legacyScore.physicalScore

            WeightageBar("अध्ययन प्रगति (Study - 30%)", String.format("%.1f / 30.0", studyScore), (studyScore / 30.0).toFloat(), NavySecondary)
            Spacer(modifier = Modifier.height(8.dp))
            WeightageBar("मॉक टेस्ट स्कोर (Mock Test - 35%)", String.format("%.1f / 35.0", mockScore), (mockScore / 35.0).toFloat(), Color(0xFF7C3AED))
            Spacer(modifier = Modifier.height(8.dp))
            WeightageBar("निरंतरता (Consistency - 20%)", String.format("%.1f / 20.0", consScore), (consScore / 20.0).toFloat(), OliveTertiary)
            Spacer(modifier = Modifier.height(8.dp))
            WeightageBar("शारीरिक व उपस्थिति (Physical - 15%)", String.format("%.1f / 15.0", physScore), (physScore / 15.0).toFloat(), SaffronPrimary)
        }
    }
}

// -------------------------------------------------------------
// SECTION 2: QUICK METRICS GRID
// -------------------------------------------------------------
@Composable
fun QuickMetricsGrid(
    unifiedState: UnifiedPerformanceState?,
    testAttempts: List<TestAttempt>,
    studyAttempts: List<StudyAttempt>
) {
    val totalQ = unifiedState?.totalQuestionsAttempted ?: studyAttempts.size
    val accuracy = unifiedState?.overallAccuracyPercentage ?: 0.0
    val totalMocks = unifiedState?.totalMockTests ?: testAttempts.size
    val streak = unifiedState?.currentStreakDays ?: 1

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        MetricTile("कुल प्रश्न", "$totalQ", "अभ्यास हल", NavySecondary, Modifier.weight(1f))
        MetricTile("सटीकता", if (totalQ > 0) "${accuracy.roundToInt()}%" else "N/A", "शुद्धता दर", OliveTertiary, Modifier.weight(1f))
        MetricTile("मॉक टेस्ट", "$totalMocks", "पूर्ण टेस्ट", Color(0xFF7C3AED), Modifier.weight(1f))
        MetricTile("स्ट्रीक", "$streak दिन", "निरंतरता", SaffronPrimary, Modifier.weight(1f))
    }
}

@Composable
fun MetricTile(title: String, value: String, subtitle: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = title, style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = color, maxLines = 1)
            Text(text = subtitle, style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp), color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
        }
    }
}

// -------------------------------------------------------------
// SECTION 3: TODAY'S PROGRESS CARD
// -------------------------------------------------------------
@Composable
fun TodayProgressCard(today: TodayProgressSummary?) {
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
                    Icon(Icons.Default.Today, contentDescription = null, tint = SaffronPrimary, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "आज की प्रगति (Today's Progress)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = today?.date ?: "आज",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (today == null || !today.hasActivity) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "आज का अभ्यास अभी शुरू नहीं हुआ है। आज का पहला क्विज़ हल करें!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    ProgressStatBox("प्रश्न हल", "${today.questionsAttempted}", "सही: ${today.correctCount}")
                    ProgressStatBox("आज की सटीकता", "${today.accuracyPercentage.roundToInt()}%", if (today.accuracyPercentage >= 70) "उत्कृष्ट" else "संतोषजनक")
                    ProgressStatBox("सत्र / टेस्ट", "${today.studySessionsCount} क्विज़ / ${today.mockTestsCount} टेस्ट", "सक्रिय")
                }
            }
        }
    }
}

@Composable
fun ProgressStatBox(title: String, value: String, sub: String) {
    Column(horizontalAlignment = Alignment.Start) {
        Text(text = title, style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = SaffronPrimary)
        Text(text = sub, style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = OliveTertiary)
    }
}

// -------------------------------------------------------------
// SECTION 4: WEEKLY CONSISTENCY & 7-DAY ACTIVITY
// -------------------------------------------------------------
@Composable
fun WeeklyConsistencyCard(weekly: WeeklyProgressSummary?) {
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
                    Icon(Icons.Default.DateRange, contentDescription = null, tint = NavySecondary, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "साप्ताहिक सक्रियता (Last 7 Days)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "${weekly?.activeDaysCount ?: 0}/7 दिन सक्रिय",
                    style = MaterialTheme.typography.labelSmall,
                    color = OliveTertiary,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 7 Days Chips Row
            val days = weekly?.last7Days ?: emptyList()
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                days.forEach { day ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.width(38.dp)
                    ) {
                        Text(
                            text = day.dayNameHindi,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(
                                    if (day.hasStudied) OliveTertiary else MaterialTheme.colorScheme.surfaceVariant
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (day.hasStudied) "${day.questionsAttempted}" else "-",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (day.hasStudied) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = weekly?.comparisonWithPreviousWeekText ?: "सप्ताहिक तुलना के लिए नियमित अभ्यास जारी रखें।",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// -------------------------------------------------------------
// SECTION 5: ACTIONABLE RECOMMENDATIONS (HINDI)
// -------------------------------------------------------------
@Composable
fun ActionableRecommendationsSection(recommendations: List<PerformanceRecommendation>) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "व्यक्तिगत अध्ययन सुझाव (Actionable Insights)",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Surface(
                color = SaffronContainer,
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = "नियम आधारित AI",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                    color = SaffronPrimary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }

        if (recommendations.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            ) {
                Text(
                    text = "डेटा पर्याप्त होने पर व्यक्तिगत सुझाव यहाँ दिखाई देंगे।",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(14.dp)
                )
            }
        } else {
            recommendations.forEach { rec ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(text = rec.iconEmoji, fontSize = 22.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = rec.title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (rec.isPositive) OliveTertiary else SaffronPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = rec.actionTextHindi,
                                style = MaterialTheme.typography.bodySmall,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// SECTION 6: SUBJECT-WISE PERFORMANCE SECTION
// -------------------------------------------------------------
@Composable
fun SubjectAnalyticsSection(subjects: List<SubjectAnalytics>) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "विषयवार प्रदर्शन विश्लेषण (Subject-Wise Analytics)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        if (subjects.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Text(
                    text = "अभी विषयवार डेटा उपलब्ध नहीं है। कृपया पहले प्रैक्टिस क्विज़ हल करें।",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(16.dp)
                )
            }
        } else {
            subjects.forEach { subject ->
                SubjectPerformanceCard(subject = subject)
            }
        }
    }
}

@Composable
fun SubjectPerformanceCard(subject: SubjectAnalytics) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
                    Text(text = subject.icon, fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = subject.subjectName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                val (badgeBg, badgeText) = when (subject.performanceLevel) {
                    PerformanceLevel.STRONG -> OliveTertiary.copy(alpha = 0.15f) to Pair(OliveTertiary, "मजबूत (Strong)")
                    PerformanceLevel.AVERAGE -> SaffronPrimary.copy(alpha = 0.15f) to Pair(SaffronPrimary, "औसत (Average)")
                    PerformanceLevel.WEAK -> MaterialTheme.colorScheme.error.copy(alpha = 0.15f) to Pair(MaterialTheme.colorScheme.error, "कमजोर (Weak)")
                    PerformanceLevel.NEED_MORE_PRACTICE -> MaterialTheme.colorScheme.surfaceVariant to Pair(MaterialTheme.colorScheme.onSurfaceVariant, "अभ्यास आवश्यक")
                }

                Surface(
                    color = badgeBg,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = badgeText.second,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = badgeText.first,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Accuracy Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "सटीकता दर",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = if (subject.totalAttempted > 0) "${subject.accuracyPercentage.roundToInt()}%" else "0%",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = when (subject.performanceLevel) {
                        PerformanceLevel.STRONG -> OliveTertiary
                        PerformanceLevel.AVERAGE -> SaffronPrimary
                        PerformanceLevel.WEAK -> MaterialTheme.colorScheme.error
                        PerformanceLevel.NEED_MORE_PRACTICE -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { (subject.accuracyPercentage / 100.0).toFloat().coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = when (subject.performanceLevel) {
                    PerformanceLevel.STRONG -> OliveTertiary
                    PerformanceLevel.AVERAGE -> SaffronPrimary
                    PerformanceLevel.WEAK -> MaterialTheme.colorScheme.error
                    PerformanceLevel.NEED_MORE_PRACTICE -> MaterialTheme.colorScheme.outlineVariant
                },
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "हल प्रश्न: ${subject.totalAttempted}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(text = "सही: ${subject.correctCount}", fontSize = 11.sp, color = OliveTertiary, fontWeight = FontWeight.Bold)
                Text(text = "गलत: ${subject.incorrectCount}", fontSize = 11.sp, color = MaterialTheme.colorScheme.error)
                Text(text = "सत्र: ${subject.practiceSessionsCount}", fontSize = 11.sp, color = NavySecondary)
            }
        }
    }
}

// -------------------------------------------------------------
// SECTION 7: TOPIC INTELLIGENCE (STRONG / WEAK / NEED PRACTICE)
// -------------------------------------------------------------
@Composable
fun TopicIntelligenceSection(
    strongTopics: List<TopicAnalytics>,
    averageTopics: List<TopicAnalytics>,
    weakTopics: List<TopicAnalytics>,
    needPracticeTopics: List<TopicAnalytics>,
    mostPracticedTopics: List<TopicAnalytics>
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text(
            text = "टॉपिकवार कमजोरी व मजबूती (Topic Intelligence)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        // Weak Topics Category
        if (weakTopics.isNotEmpty()) {
            TopicGroupCard(
                title = "कमजोर टॉपिक (Weak Topics - सुधार आवश्यक)",
                topics = weakTopics,
                badgeColor = MaterialTheme.colorScheme.error,
                containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)
            )
        }

        // Strong Topics Category
        if (strongTopics.isNotEmpty()) {
            TopicGroupCard(
                title = "मजबूत टॉपिक (Strong Topics - 75%+ Accuracy)",
                topics = strongTopics,
                badgeColor = OliveTertiary,
                containerColor = OliveTertiary.copy(alpha = 0.1f)
            )
        }

        // Topics Needing Practice
        if (needPracticeTopics.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "अभ्यास हेतु शेष टॉपिक (Need More Practice)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "सटीक मूल्यांकन हेतु न्यूनतम 5 प्रश्नों का प्रयास आवश्यक है।",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    needPracticeTopics.take(6).chunked(2).forEach { rowTopics ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowTopics.forEach { topic ->
                                Surface(
                                    modifier = Modifier.weight(1f),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Text(text = topic.topicName, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium, maxLines = 1)
                                        Text(text = "${topic.subjectName} • ${topic.totalAttempted}/5 हल", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                            if (rowTopics.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun TopicGroupCard(
    title: String,
    topics: List<TopicAnalytics>,
    badgeColor: Color,
    containerColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = badgeColor
            )
            Spacer(modifier = Modifier.height(8.dp))

            topics.forEach { topic ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = topic.topicName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        Text(text = "${topic.subjectName} • कुल ${topic.totalAttempted} प्रश्न", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text(
                        text = "${topic.accuracyPercentage.roundToInt()}%",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = badgeColor
                    )
                }
                HorizontalDivider(color = badgeColor.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 2.dp))
            }
        }
    }
}

// -------------------------------------------------------------
// SECTION 8: QUIZ & MOCK TEST IMPROVEMENT TRENDS
// -------------------------------------------------------------
@Composable
fun QuizAndMockTrendsSection(
    quizTrend: PracticeQuizTrend?,
    mockTrend: MockTestTrend?
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text(
            text = "प्रगति व सुधार ट्रेंड (Improvement Trends)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        // Practice Quiz Trend
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
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
                        Icon(Icons.Default.TrendingUp, contentDescription = null, tint = NavySecondary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("प्रैक्टिस क्विज़ ट्रेंड (Quiz Trend)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    }
                    if (quizTrend?.hasTrendData == true) {
                        val isPositive = quizTrend.improvementPercentage >= 0
                        Surface(
                            color = if (isPositive) OliveTertiary.copy(alpha = 0.15f) else SaffronPrimary.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "${if (isPositive) "+" else ""}${quizTrend.improvementPercentage.roundToInt()}% सुधार",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isPositive) OliveTertiary else SaffronPrimary,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (quizTrend == null || !quizTrend.hasTrendData) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "ट्रेंड देखने के लिए कम से कम 2 Quiz Attempts की आवश्यकता है।",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TrendMetric("प्रारंभिक सटीकता", "${quizTrend.firstRecordedAccuracy.roundToInt()}%")
                        TrendMetric("नवीनतम सटीकता", "${quizTrend.latestRecordedAccuracy.roundToInt()}%")
                        TrendMetric("औसत सटीकता", "${quizTrend.recentAverageAccuracy.roundToInt()}%")
                    }
                }
            }
        }

        // Mock Test Trend
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
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
                        Icon(Icons.Default.Quiz, contentDescription = null, tint = Color(0xFF7C3AED), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("मॉक टेस्ट स्कोर ट्रेंड (Mock Test Trend)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    }
                    if (mockTrend?.hasTrendData == true) {
                        Surface(
                            color = Color(0xFF7C3AED).copy(alpha = 0.15f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "सर्वश्रेष्ठ: ${mockTrend.bestScore.roundToInt()}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF7C3AED),
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (mockTrend == null || !mockTrend.hasTrendData) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "मॉक टेस्ट विश्लेषण के लिए अपना पहला या दूसरा Mock Test पूरा करें।",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TrendMetric("नवीनतम स्कोर", "${mockTrend.latestScore.roundToInt()} अंक")
                        TrendMetric("औसत स्कोर", "${mockTrend.averageScore.roundToInt()} अंक")
                        TrendMetric("टेस्ट सटीकता", "${mockTrend.recentAccuracy.roundToInt()}%")
                    }
                }
            }
        }
    }
}

@Composable
fun TrendMetric(label: String, value: String) {
    Column {
        Text(text = label, style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = SaffronPrimary)
    }
}

// -------------------------------------------------------------
// SECTION 9: CHRONOLOGICAL PERFORMANCE HISTORY
// -------------------------------------------------------------
@Composable
fun ChronologicalHistorySection(
    testAttempts: List<TestAttempt>,
    studyAttempts: List<StudyAttempt>
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "नवीनतम परीक्षा इतिहास (Recent Performance History)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        if (testAttempts.isEmpty() && studyAttempts.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            ) {
                Text(
                    text = "अभी कोई परीक्षा अथवा क्विज़ इतिहास दर्ज नहीं है।",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(16.dp)
                )
            }
        } else {
            // Mock test attempts
            testAttempts.take(5).forEach { attempt ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    color = Color(0xFF7C3AED).copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "MOCK TEST",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                                        color = Color(0xFF7C3AED),
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = attempt.testTitle,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "दिनांक: ${attempt.date} • समय: ${attempt.timeTakenSeconds / 60} मिनट",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${attempt.score.roundToInt()}/${attempt.maxScore.roundToInt()}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = SaffronPrimary
                            )
                            Text(
                                text = "सटीकता: ${attempt.accuracyPercentage.roundToInt()}%",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = OliveTertiary
                            )
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// SECTION 10: AI COACH HEADER & INSIGHTS & PHYSICAL STANDARDS
// -------------------------------------------------------------
@Composable
fun AiCoachHeaderCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = NavyContainer.copy(alpha = 0.5f)),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(listOf(SaffronPrimary, NavySecondary))
        )
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
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(SaffronPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = "Bajrang AI Coach",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Bajrang AI Coach",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = OnNavyContainer
                        )
                        Text(
                            text = "दैनिक प्रदर्शन विश्लेषण एवं व्यक्तिगत मार्गदर्शन",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    color = SaffronPrimary,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "LIVE AI",
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

@Composable
fun AiCoachInsightItemCard(insight: AiCoachInsight) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
                    val icon = when (insight.category) {
                        "Physical" -> Icons.Default.DirectionsRun
                        "Study" -> Icons.Default.MenuBook
                        "MockTest" -> Icons.Default.Quiz
                        "Attendance" -> Icons.Default.FactCheck
                        else -> Icons.Default.MilitaryTech
                    }
                    val color = when (insight.category) {
                        "Physical" -> SaffronPrimary
                        "Study" -> NavySecondary
                        "MockTest" -> Color(0xFF7C3AED)
                        "Attendance" -> OliveTertiary
                        else -> GoldAccent
                    }

                    Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = insight.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = insight.badgeLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = insight.messageHindi,
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 22.sp
            )
        }
    }
}

@Composable
fun PhysicalReadinessStandardsCard(
    student: StudentProfile?,
    workouts: List<WorkoutRecord>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "भर्ती मानक तैयारी (Physical Readiness Standards)",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                StandardBadge("1600m दौड़", student?.time1600m ?: "5:15 min", "लक्ष्य: 5:30", true)
                StandardBadge("बीम (पुल-अप्स)", "${student?.pullups ?: 12} Beam", "लक्ष्य: 10 (40 Marks)", true)
                StandardBadge("लंबी कूद (Long Jump)", "${student?.longJumpFeet ?: 15.5} Feet", "लक्ष्य: 14 Feet", true)
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                StandardBadge("ऊंची कूद (High Jump)", "${student?.highJumpFeet ?: 4.2} Feet", "लक्ष्य: 4.0 Feet", true)
                StandardBadge("गोला फेंक (Shot Put)", "${student?.shotPutMeters ?: 7.5} Meter", "लक्ष्य: 7.2 Meter", true)
                StandardBadge("सीना (Chest)", "${student?.chestNormalCm ?: 82}-${student?.chestExpandedCm ?: 87} cm", "लक्ष्य: 77-82 cm", true)
            }
        }
    }
}

@Composable
fun WeightageBar(title: String, scoreText: String, ratio: Float, color: Color) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = title, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
            Text(text = scoreText, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = color)
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { ratio.coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = color,
            trackColor = color.copy(alpha = 0.15f)
        )
    }
}

@Composable
fun StandardBadge(label: String, value: String, target: String, isPassed: Boolean) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
        modifier = Modifier.width(105.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = label, style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = if (isPassed) OliveTertiary else SaffronPrimary)
            Text(text = target, style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
