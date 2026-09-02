package com.example.ui.screens.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import coil.compose.AsyncImage
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.ai.AiCoachInsight
import com.example.data.ai.PerformanceScoreBreakdown
import com.example.data.model.*
import com.example.ui.components.MetricStatCard
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DashboardScreen(
    student: StudentProfile?,
    todayAttendance: AttendanceRecord?,
    todayWorkout: WorkoutRecord?,
    latestWorkoutPlan: DailyWorkoutPlan?,
    todayStudyTargets: List<Chapter>,
    performanceScore: PerformanceScoreBreakdown,
    aiCoachInsights: List<AiCoachInsight>,
    latestNotices: List<Notice>,
    latestAttempt: TestAttempt?,
    onNavigate: (String) -> Unit,
    onToggleChapter: (Chapter) -> Unit,
    onMarkAttendance: (String) -> Unit,
    todayTraining: TrainingRecord? = null,
    activeAttendance: List<AttendanceRecord> = emptyList(),
    trainingRecordsCount: Int = 0,
    modifier: Modifier = Modifier
) {
    val currentDateStr = remember {
        SimpleDateFormat("EEEE, dd MMMM yyyy", Locale("hi", "IN")).format(Date())
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("student_dashboard_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Hero Banner with Mission Tagline
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                shape = RoundedCornerShape(18.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Image(
                        painter = painterResource(id = R.drawable.hero_training_banner),
                        contentDescription = "Training Ground Banner",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    // Gradient Overlay for text contrast
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        Color.Transparent,
                                        Color.Black.copy(alpha = 0.85f)
                                    )
                                )
                            )
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = SaffronPrimary,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "जय बजरंग अखाड़ा",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }

                            Text(
                                text = currentDateStr,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }

                        Column {
                            Text(
                                text = "स्वस्थ युवा – सशक्त राष्ट्र",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                            Text(
                                text = "गाँव से सेना–पुलिस भर्ती अभियान • मौरिकला गुफा",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        }
                    }
                }
            }
        }

        // 1.1. Quick Access Bar: About & Mission (अखाड़ा परिचय एवं उद्देश्य)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = SaffronContainer.copy(alpha = 0.5f)),
                border = BorderStroke(1.dp, SaffronPrimary.copy(alpha = 0.35f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onNavigate("about_akhada") },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(SaffronPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Info, contentDescription = "हमारे बारे में", tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("हमारे बारे में", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = SaffronDark)
                            Text("About Akhada", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .height(28.dp)
                            .width(1.dp)
                            .background(SaffronPrimary.copy(alpha = 0.3f))
                    )

                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 12.dp)
                            .clickable { onNavigate("mission") },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(NavySecondary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Visibility, contentDescription = "हमारा मिशन / उद्देश्य", tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("हमारा उद्देश्य", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = NavySecondary)
                            Text("Mission & Vision", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }

        // 2. Student Profile Card & Welcome Header
        item {
            student?.let { s ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Avatar Badge / Photo
                        if (s.profilePhotoUri.isNotBlank()) {
                            AsyncImage(
                                model = s.profilePhotoUri,
                                contentDescription = "Student Photo",
                                modifier = Modifier
                                    .size(58.dp)
                                    .clip(CircleShape)
                                    .border(2.dp, SaffronPrimary, CircleShape)
                                    .clickable { onNavigate("profile") },
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(58.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(listOf(NavySecondary, SaffronPrimary))
                                    )
                                    .border(2.dp, SaffronPrimary, CircleShape)
                                    .clickable { onNavigate("profile") },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = s.fullName.take(1),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            val computedAge = com.example.util.ProfileUtils.calculateAgeFromDob(s.dob).let { if (it > 0) it else s.age }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = s.fullName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                StatusBadge(status = todayAttendance?.status ?: "Not Marked")
                            }

                            Spacer(modifier = Modifier.height(2.dp))

                            Text(
                                text = "आईडी: ${s.studentId} • उम्र: ${computedAge} वर्ष • गाँव: ${s.village}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    color = SaffronContainer,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "लक्ष्य: ${s.recruitmentGoal}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = OnSaffronContainer,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }

                                Surface(
                                    color = OliveContainer,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "स्कोर: ${performanceScore.totalScore}/100",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = OnOliveContainer,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. Quick Action Buttons
        item {
            Column {
                Text(
                    text = "त्वरित कार्य (Quick Actions)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    QuickActionButton(
                        title = "ट्रेनिंग",
                        subtitle = "Training",
                        icon = Icons.Default.DirectionsRun,
                        color = SaffronPrimary,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate("training") }
                    )
                    QuickActionButton(
                        title = "उपस्थिति",
                        subtitle = "Attendance",
                        icon = Icons.Default.FactCheck,
                        color = OliveTertiary,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate("attendance") }
                    )
                    QuickActionButton(
                        title = "अध्ययन",
                        subtitle = "Study",
                        icon = Icons.Default.MenuBook,
                        color = NavySecondary,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate("study") }
                    )
                    QuickActionButton(
                        title = "मॉक टेस्ट",
                        subtitle = "Mock Test",
                        icon = Icons.Default.Quiz,
                        color = Color(0xFF7C3AED),
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate("mocktest") }
                    )
                }
            }
        }

        // 3.4 Monthly Performance Card for Cadets (Phase 3)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigate("monthly_performance") }
                    .testTag("dashboard_monthly_report_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF0D9488).copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Assessment,
                                contentDescription = "मासिक रिपोर्ट कार्ड",
                                tint = Color(0xFF0D9488),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "मासिक प्रदर्शन व रिपोर्ट कार्ड",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "उपस्थिति %, 1600m ट्रायल, सुधार व PDF रिपोर्ट",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // 3.4B Notifications & Communication Inbox Card (Phase 5B)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigate("notifications") }
                    .testTag("dashboard_notifications_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(SaffronPrimary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = "सूचनाएं एवं अलर्ट",
                                tint = SaffronPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "संदेश एवं सूचना केंद्र (Notifications)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "भर्ती अलर्ट, ट्रेनिंग व अध्ययन रिमाइंडर, घोषणाएं",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // 3.5 Dedicated 'मेरी ट्रेनिंग' (My Daily Training) Card (PART 6)
        item {
            val streak = remember(activeAttendance) {
                var count = 0
                for (rec in activeAttendance.sortedByDescending { it.date }) {
                    if (rec.status == "Present") count++ else break
                }
                count.coerceAtLeast(1)
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dashboard_my_training_section"),
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
                                imageVector = Icons.Default.DirectionsRun,
                                contentDescription = null,
                                tint = SaffronPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "🏃 मेरी ट्रेनिंग (My Training)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        TextButton(onClick = { onNavigate("training") }) {
                            Text("सभी देखें", color = SaffronPrimary, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Training Status
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            color = if (todayTraining != null) OliveContainer.copy(alpha = 0.6f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = if (todayTraining != null) "✓ पूर्ण" else "लंबित",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (todayTraining != null) OliveTertiary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "आज की ट्रेनिंग",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Streak
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            color = SaffronContainer.copy(alpha = 0.6f)
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "🔥 $streak दिन",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = SaffronDark
                                )
                                Text(
                                    text = "हाजिरी स्ट्रीक",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = OnSaffronContainer
                                )
                            }
                        }

                        // Total Sessions
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            color = NavyContainer.copy(alpha = 0.6f)
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "${trainingRecordsCount.coerceAtLeast(if (todayTraining != null) 1 else 0)} सत्र",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = NavySecondary
                                )
                                Text(
                                    text = "कुल ट्रेनिंग",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = OnNavyContainer
                                )
                            }
                        }
                    }

                    if (todayTraining != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "🏃 ${todayTraining.runningDistanceKm} KM (${todayTraining.runningType})",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = SaffronPrimary
                                )
                                Text(
                                    text = "💪 पुश: ${todayTraining.pushups} • सिट: ${todayTraining.situps} • बीम: ${todayTraining.pullups}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }

        // 4. Daily Progress & Overall Score Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "दैनिक प्रगति (Daily Progress)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "आज का अभ्यास और लक्ष्य स्टेटस",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Circular Performance Score Badge
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .clip(CircleShape)
                                .background(SaffronPrimary.copy(alpha = 0.12f))
                                .border(2.dp, SaffronPrimary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${performanceScore.totalScore}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = SaffronPrimary
                                )
                                Text(
                                    text = "/100",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Progress Rows
                    val runDist = todayWorkout?.runningDistanceKm ?: 2.0
                    val runTarget = todayWorkout?.runningTargetKm ?: 3.0
                    val pushDone = todayWorkout?.pushupsDone ?: 35
                    val pushTarget = todayWorkout?.pushupsTarget ?: 50
                    val completedChapters = todayStudyTargets.count { it.isCompleted }
                    val totalTargets = todayStudyTargets.size.coerceAtLeast(4)

                    // Running
                    ProgressItemRow(
                        title = "दौड़ (Running)",
                        valueText = "${String.format(Locale.US, "%.1f", runDist)} KM / ${String.format(Locale.US, "%.1f", runTarget)} KM",
                        progress = (runDist / runTarget).toFloat(),
                        color = SaffronPrimary,
                        icon = Icons.Default.DirectionsRun
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Push-ups
                    ProgressItemRow(
                        title = "पुश-अप्स (Push-ups)",
                        valueText = "$pushDone / $pushTarget",
                        progress = (pushDone.toFloat() / pushTarget.toFloat()),
                        color = OliveTertiary,
                        icon = Icons.Default.FitnessCenter
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Study
                    ProgressItemRow(
                        title = "स्टडी चैप्टर्स (Study)",
                        valueText = "$completedChapters / $totalTargets पूर्ण",
                        progress = if (totalTargets > 0) completedChapters.toFloat() / totalTargets.toFloat() else 0.5f,
                        color = NavySecondary,
                        icon = Icons.Default.MenuBook
                    )
                }
            }
        }

        // 5. Today's Study Target Checklist
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Checklist,
                                contentDescription = "Targets",
                                tint = SaffronPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "आज का स्टडी टारगेट (Today's Targets)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        TextButton(onClick = { onNavigate("study") }) {
                            Text("सभी देखें", color = SaffronPrimary, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    if (todayStudyTargets.isEmpty()) {
                        Text(
                            text = "आज के सभी लक्ष्य पूर्ण हो चुके हैं!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = OliveTertiary
                        )
                    } else {
                        todayStudyTargets.take(4).forEach { chapter ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onToggleChapter(chapter) }
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = chapter.isCompleted,
                                    onCheckedChange = { onToggleChapter(chapter) },
                                    colors = CheckboxDefaults.colors(checkedColor = OliveTertiary)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = chapter.chapterName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (chapter.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "विषय: ${chapter.subjectName}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 6. Bajrang AI Coach Recommendation Card
        item {
            val topInsight = aiCoachInsights.firstOrNull()
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigate("aicoach") },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = NavyContainer.copy(alpha = 0.7f)
                ),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.horizontalGradient(listOf(SaffronPrimary, NavySecondary))
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(SaffronPrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Psychology,
                                    contentDescription = "AI Coach",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Bajrang AI Coach",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = OnNavyContainer
                            )
                        }

                        Surface(
                            color = SaffronPrimary,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "AI विश्लेषण",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = topInsight?.messageHindi ?: "आपकी 1600 मीटर दौड़ का समय पिछले रिकॉर्ड की तुलना में 20 सेकंड बेहतर हुआ है! अनुशासन और निरंतरता बनाए रखें।",
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnNavyContainer,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text(
                            text = "पूरा AI परामर्श देखें →",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = SaffronPrimary
                        )
                    }
                }
            }
        }

        // 7. Navigation Quick Cards (Leaderboard, Notices, Recruitment)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigate("leaderboard") },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = "Leaderboard",
                            tint = GoldAccent,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "लीडरबोर्ड",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "अखाड़ा टॉपर्स",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigate("recruitment") },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Icon(
                            imageVector = Icons.Default.MilitaryTech,
                            contentDescription = "Recruitment",
                            tint = OliveTertiary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "भर्ती जानकारी",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Army, Police, SSC",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigate("notices") },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Icon(
                            imageVector = Icons.Default.Campaign,
                            contentDescription = "Notices",
                            tint = SaffronPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "नोटिस बोर्ड",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "महत्वपूर्ण सूचनाएं",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // 9. जय बजरंग अखाड़ा अभियान एवं जानकारी (Akhada Campaign & Info Section)
        item {
            Column {
                Text(
                    text = "🚩 जय बजरंग अखाड़ा अभियान (मौरीकला गुफा)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
                )

                // Row 1: About, Mission, Training Centre
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AkhadaInfoCard(
                        title = "हमारे बारे में",
                        subtitle = "About Akhada",
                        icon = Icons.Default.Info,
                        color = SaffronPrimary,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate("about_akhada") }
                    )
                    AkhadaInfoCard(
                        title = "हमारा मिशन",
                        subtitle = "Our Mission",
                        icon = Icons.Default.Visibility,
                        color = NavySecondary,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate("mission") }
                    )
                    AkhadaInfoCard(
                        title = "प्रशिक्षण केंद्र",
                        subtitle = "Maurikala Gufa",
                        icon = Icons.Default.LocationOn,
                        color = OliveTertiary,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate("training_centre") }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Row 2: Trainers, Program, Gallery
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AkhadaInfoCard(
                        title = "हमारे प्रशिक्षक",
                        subtitle = "Trainers",
                        icon = Icons.Default.SportsScore,
                        color = Color(0xFFD97706),
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate("trainers") }
                    )
                    AkhadaInfoCard(
                        title = "पाठ्यक्रम",
                        subtitle = "Program",
                        icon = Icons.Default.FormatListNumbered,
                        color = Color(0xFF0284C7),
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate("training_program") }
                    )
                    AkhadaInfoCard(
                        title = "गैलरी",
                        subtitle = "Gallery",
                        icon = Icons.Default.PhotoLibrary,
                        color = Color(0xFF7C3AED),
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate("gallery") }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Row 3: Success Stories & Contact
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AkhadaInfoCard(
                        title = "सफलताएं (उपलब्धियां)",
                        subtitle = "Success Stories",
                        icon = Icons.Default.EmojiEvents,
                        color = GoldAccent,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate("success_stories") }
                    )
                    AkhadaInfoCard(
                        title = "संपर्क एवं सहायता",
                        subtitle = "Contact & Help",
                        icon = Icons.Default.Phone,
                        color = SaffronDark,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate("contact") }
                    )
                }
            }
        }
    }
}

@Composable
fun AkhadaInfoCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(fontSize = 12.sp),
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}


@Composable
fun QuickActionButton(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = color,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ProgressItemRow(
    title: String,
    valueText: String,
    progress: Float,
    color: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                text = valueText,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { progress.coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = color,
            trackColor = color.copy(alpha = 0.15f)
        )
    }
}
