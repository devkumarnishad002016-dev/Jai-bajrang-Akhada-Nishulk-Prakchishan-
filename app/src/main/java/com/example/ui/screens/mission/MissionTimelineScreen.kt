package com.example.ui.screens.mission

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.mission.MissionDashboardState
import com.example.data.mission.MissionDuration
import com.example.data.mission.MissionEngine
import com.example.data.model.Chapter
import com.example.data.model.StudentProfile
import com.example.data.model.StudyAttempt
import com.example.data.model.TestAttempt
import com.example.data.model.TrainingRecord
import com.example.data.model.WorkoutRecord
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MissionTimelineScreen(
    student: StudentProfile?,
    todayWorkout: WorkoutRecord?,
    todayTraining: TrainingRecord?,
    todayChapters: List<Chapter>,
    studentStudyAttempts: List<StudyAttempt>,
    studentTestAttempts: List<TestAttempt>,
    onNavigateToWorkout: () -> Unit,
    onNavigateToStudy: () -> Unit,
    onNavigateToMockTest: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedDuration by remember { mutableStateOf(MissionDuration.DAYS_60) }

    val missionState: MissionDashboardState = remember(
        student,
        selectedDuration,
        todayWorkout,
        todayTraining,
        todayChapters,
        studentStudyAttempts,
        studentTestAttempts
    ) {
        MissionEngine.calculateMissionState(
            student = student,
            duration = selectedDuration,
            todayWorkout = todayWorkout,
            todayTraining = todayTraining,
            todayChapters = todayChapters,
            studentStudyAttempts = studentStudyAttempts,
            studentTestAttempts = studentTestAttempts
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "30/60/90 दिन मिशन",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "लक्ष्य: ${missionState.recruitmentGoal} • दैनिक लक्ष्य व प्रगति",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("mission_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "पीछे जाएं"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        modifier = modifier.testTag("mission_timeline_screen")
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Duration Selector Tabs
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "मिशन अवधि चुनें (Select Mission Plan):",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            MissionDuration.values().forEach { plan ->
                                val isSelected = plan == selectedDuration
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable { selectedDuration = plan }
                                        .testTag("mission_plan_${plan.days}"),
                                    color = if (isSelected) SaffronPrimary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "${plan.days} दिन",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = if (plan.days == 30) "क्रैश" else if (plan.days == 60) "मानक" else "फाउंडेशन",
                                            fontSize = 11.sp,
                                            color = if (isSelected) Color.White.copy(alpha = 0.9f) else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Mission Status Overview Banner
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = NavySecondary),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "दिन ${missionState.currentDayNumber} / ${missionState.totalDays}",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = missionState.selectedDuration.titleHindi,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SaffronLight
                                )
                            }

                            Surface(
                                color = SaffronPrimary,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocalFireDepartment,
                                        contentDescription = "Streak",
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${missionState.streakDays} दिन स्ट्रीक",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Progress Bar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "मिशन पूर्णता: ${missionState.completionPercentage}%",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White
                            )
                            Text(
                                text = "${missionState.totalDays - missionState.currentDayNumber} दिन शेष",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { missionState.completionPercentage / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = SaffronPrimary,
                            trackColor = Color.White.copy(alpha = 0.2f)
                        )

                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = missionState.phaseTitleHindi,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                }
            }

            // Motivational Quote
            item {
                Surface(
                    color = SaffronPrimary.copy(alpha = 0.10f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = SaffronPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = missionState.motivationalQuoteHindi,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = SaffronDark
                        )
                    }
                }
            }

            // Today's Daily Target Checklist Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "आज के 3 मुख्य लक्ष्य (Daily Mission Targets)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Target 1: Workout / Running Target
            item {
                MissionTargetCard(
                    titleHindi = "ग्राउंड रनिंग व फिजिकल वर्कआउट",
                    subtitleHindi = if (missionState.dailyWorkoutTargetDone) "आज का फिजिकल लक्ष्य पूर्ण! ✓" else "1600m रनिंग व ग्राउंड एक्सरसाइज बाकी है",
                    categoryIcon = Icons.Default.DirectionsRun,
                    categoryColor = SaffronPrimary,
                    isDone = missionState.dailyWorkoutTargetDone,
                    actionText = if (missionState.dailyWorkoutTargetDone) "देखें" else "वर्कआउट भरें",
                    onActionClick = onNavigateToWorkout,
                    testTag = "target_workout_card"
                )
            }

            // Target 2: Study Target
            item {
                MissionTargetCard(
                    titleHindi = "लिखित परीक्षा अध्याय व नोट्स",
                    subtitleHindi = if (missionState.dailyStudyTargetDone) "आज का अध्ययन लक्ष्य पूर्ण! ✓" else "गणित/जीके/रीजनिंग अध्याय अध्ययन करें",
                    categoryIcon = Icons.Default.MenuBook,
                    categoryColor = NavySecondary,
                    isDone = missionState.dailyStudyTargetDone,
                    actionText = if (missionState.dailyStudyTargetDone) "अध्ययन करें" else "अध्याय खोलें",
                    onActionClick = onNavigateToStudy,
                    testTag = "target_study_card"
                )
            }

            // Target 3: Practice / Mock Target
            item {
                MissionTargetCard(
                    titleHindi = "स्पीड प्रैक्टिस एवं मॉक टेस्ट",
                    subtitleHindi = if (missionState.dailyPracticeTargetDone) "आज का मॉक टेस्ट पूर्ण! ✓" else "20 प्रश्नों का प्रैक्टिस सेट हल करें",
                    categoryIcon = Icons.Default.Quiz,
                    categoryColor = Color(0xFF7C3AED),
                    isDone = missionState.dailyPracticeTargetDone,
                    actionText = if (missionState.dailyPracticeTargetDone) "रिजल्ट देखें" else "टेस्ट दें",
                    onActionClick = onNavigateToMockTest,
                    testTag = "target_mock_card"
                )
            }

            // Detailed Tasks Breakdown for Selected Goal
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "विस्तृत टास्क चेकलिस्ट (${missionState.recruitmentGoal})",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            items(missionState.tasks) { task ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (task.isCompleted) OliveTertiary.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
                    ),
                    border = CardDefaults.outlinedCardBorder()
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
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (task.isCompleted) OliveTertiary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (task.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                    contentDescription = null,
                                    tint = if (task.isCompleted) OliveTertiary else Color.Gray,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = task.titleHindi,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (task.isCompleted) OliveTertiary else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = task.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Surface(
                            color = if (task.isCompleted) OliveTertiary.copy(alpha = 0.15f) else SaffronPrimary.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = task.targetMetric,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (task.isCompleted) OliveTertiary else SaffronDark,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MissionTargetCard(
    titleHindi: String,
    subtitleHindi: String,
    categoryIcon: androidx.compose.ui.graphics.vector.ImageVector,
    categoryColor: Color,
    isDone: Boolean,
    actionText: String,
    onActionClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag(testTag),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(if (isDone) OliveTertiary.copy(alpha = 0.15f) else categoryColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isDone) Icons.Default.CheckCircle else categoryIcon,
                        contentDescription = null,
                        tint = if (isDone) OliveTertiary else categoryColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = titleHindi,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = subtitleHindi,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isDone) OliveTertiary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Button(
                onClick = onActionClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isDone) OliveTertiary else categoryColor
                ),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(text = actionText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
