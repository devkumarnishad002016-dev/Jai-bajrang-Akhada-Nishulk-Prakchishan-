package com.example.ui.screens.workout

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DailyWorkoutPlan
import com.example.data.model.StudentProfile
import com.example.data.model.WorkoutRecord
import com.example.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun WorkoutScreen(
    student: StudentProfile?,
    latestPlan: DailyWorkoutPlan?,
    workoutRecords: List<WorkoutRecord>,
    todayWorkout: WorkoutRecord?,
    onSubmitWorkout: (runningKm: Double, time1600: Int, pushups: Int, situps: Int, pullups: Int, squats: Int, notes: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showRecordDialog by remember { mutableStateOf(false) }

    // Stopwatch helper state
    var stopwatchRunning by remember { mutableStateOf(false) }
    var stopwatchSeconds by remember { mutableIntStateOf(0) }

    LaunchedEffect(stopwatchRunning) {
        while (stopwatchRunning) {
            delay(1000)
            stopwatchSeconds += 1
        }
    }

    val latestRecord = workoutRecords.firstOrNull() ?: todayWorkout

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("workout_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Mission Training Header & Daily Plan
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
                                text = "दैनिक फिजिकल ट्रेनिंग (Daily Workout)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "जय बजरंग अखाड़ा • सेना-पुलिस ग्राउंड रूटीन",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Button(
                            onClick = { showRecordDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("record_performance_button")
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("स्कोर दर्ज करें")
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Daily Target Plan from Trainer
                    latestPlan?.let { plan ->
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.SportsScore,
                                        contentDescription = null,
                                        tint = SaffronPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = plan.title,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = plan.instructions,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = 18.sp
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    PlanTargetChip("रनिंग: ${plan.targetRunningKm} KM", SaffronPrimary)
                                    PlanTargetChip("पुश-अप्स: ${plan.targetPushups}", OliveTertiary)
                                    PlanTargetChip("सिट-अप्स: ${plan.targetSitups}", NavySecondary)
                                    PlanTargetChip("पुल-अप्स: ${plan.targetPullups}", Color(0xFF7C3AED))
                                }
                            }
                        }
                    }
                }
            }
        }

        // 2. Improvement & Comparison Card (Highlights 7:30 -> 7:10 improvement)
        item {
            latestRecord?.let { rec ->
                val prev = rec.previous1600mSeconds
                val curr = rec.time1600mSeconds
                val diff = prev - curr
                val isImproved = diff > 0

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isImproved) OliveContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceVariant
                    ),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.horizontalGradient(
                            if (isImproved) listOf(OliveTertiary, SaffronPrimary)
                            else listOf(MaterialTheme.colorScheme.outline, MaterialTheme.colorScheme.outline)
                        )
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "1600 मीटर टाइम सुधार (Improvement)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (isImproved) {
                                Surface(
                                    color = OliveTertiary,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "$diff सेकंड का सुधार! ⚡",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TimeBox("पिछला रिकॉर्ड (Previous)", "${prev / 60}:${String.format("%02d", prev % 60)} min", MaterialTheme.colorScheme.onSurfaceVariant)
                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = "Improvement",
                                tint = SaffronPrimary
                            )
                            TimeBox("आज का समय (Today)", "${curr / 60}:${String.format("%02d", curr % 60)} min", SaffronPrimary)
                        }

                        if (rec.notes.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "रिमार्क: ${rec.notes}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // 3. Ground Stopwatch / Timer Assistant
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
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "ग्राउंड स्टॉपवॉच (Training Timer)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        val mins = stopwatchSeconds / 60
                        val secs = stopwatchSeconds % 60
                        Text(
                            text = String.format("%02d:%02d", mins, secs),
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (stopwatchRunning) SaffronPrimary else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(
                            onClick = { stopwatchRunning = !stopwatchRunning },
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(if (stopwatchRunning) StatusAbsent else OliveTertiary)
                        ) {
                            Icon(
                                imageVector = if (stopwatchRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = "Start/Stop",
                                tint = Color.White
                            )
                        }

                        IconButton(
                            onClick = {
                                stopwatchRunning = false
                                stopwatchSeconds = 0
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Reset",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        // 4. Workout Categories & Exercises Cards
        item {
            Text(
                text = "ट्रेनिंग श्रेणियां (Workout Categories)",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
        }

        // Running Category
        item {
            ExerciseCategoryCard(
                categoryTitle = "रनिंग अभ्यास (Running Training)",
                icon = Icons.Default.DirectionsRun,
                color = SaffronPrimary,
                items = listOf(
                    "400 Meter Sprint" to "सर्वोत्तम समय: ${student?.time400m ?: "1:05"}",
                    "800 Meter Trial" to "सर्वोत्तम समय: ${student?.time800m ?: "2:20"}",
                    "1600 Meter Army/Police Trial" to "सर्वोत्तम समय: ${student?.time1600m ?: "5:15"}",
                    "5 KM Endurance Run" to "सर्वोत्तम समय: ${student?.time5km ?: "20:10"}",
                    "100m Sprint Intervals" to "4 सेट्स (विस्फोटक गति अभ्यास)"
                )
            )
        }

        // Strength Training Category
        item {
            ExerciseCategoryCard(
                categoryTitle = "स्ट्रेंथ व क्षमता अभ्यास (Strength Training)",
                icon = Icons.Default.FitnessCenter,
                color = OliveTertiary,
                items = listOf(
                    "Push-ups (पुश-अप्स)" to "दैनिक लक्ष्य: 50 | छात्र रिकॉर्ड: ${student?.pushups ?: 45}",
                    "Sit-ups (सिट-अप्स)" to "दैनिक लक्ष्य: 50 | छात्र रिकॉर्ड: ${student?.situps ?: 50}",
                    "Pull-ups (बीम / पुल-अप्स)" to "सेना लक्ष्य: 10 बीम (40 अंक) | रिकॉर्ड: ${student?.pullups ?: 12}",
                    "Squats (दंड / बैठक)" to "दैनिक लक्ष्य: 60 रेप्स | रिकॉर्ड: ${student?.squats ?: 60}",
                    "Plank (कोर स्टैमिना)" to "दैनिक लक्ष्य: 120 सेकंड | रिकॉर्ड: ${student?.plankSeconds ?: 120}s"
                )
            )
        }

        // 5. Workout Records History
        item {
            Text(
                text = "हालिया वर्कआउट इतिहास (Recent History)",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
        }

        items(workoutRecords) { rec ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "दिनांक: ${rec.date}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "रनिंग: ${rec.runningDistanceKm} KM",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = SaffronPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "1600m: ${rec.time1600mSeconds / 60}:${String.format("%02d", rec.time1600mSeconds % 60)}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "पुश-अप्स: ${rec.pushupsDone}/${rec.pushupsTarget}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "सिट-अप्स: ${rec.situpsDone}/${rec.situpsTarget}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "पुल-अप्स: ${rec.pullupsDone}/${rec.pullupsTarget}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }

    // Performance Entry Dialog
    if (showRecordDialog) {
        var runKm by remember { mutableStateOf("3.0") }
        var mins1600 by remember { mutableStateOf("7") }
        var secs1600 by remember { mutableStateOf("10") }
        var pushups by remember { mutableStateOf("35") }
        var situps by remember { mutableStateOf("45") }
        var pullups by remember { mutableStateOf("8") }
        var squats by remember { mutableStateOf("50") }
        var notes by remember { mutableStateOf("ग्राउंड टाइमिंग ट्रायल पूर्ण हुआ।") }

        AlertDialog(
            onDismissRequest = { showRecordDialog = false },
            title = {
                Text(
                    text = "दैनिक वर्कआउट दर्ज करें",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = runKm,
                        onValueChange = { runKm = it },
                        label = { Text("दौड़ी गई दूरी (KM)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = mins1600,
                            onValueChange = { mins1600 = it },
                            label = { Text("1600m मिनट") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = secs1600,
                            onValueChange = { secs1600 = it },
                            label = { Text("1600m सेकंड") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = pushups,
                            onValueChange = { pushups = it },
                            label = { Text("पुश-अप्स") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = situps,
                            onValueChange = { situps = it },
                            label = { Text("सिट-अप्स") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = pullups,
                            onValueChange = { pullups = it },
                            label = { Text("पुल-अप्स (बीम)") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = squats,
                            onValueChange = { squats = it },
                            label = { Text("बैठक (Squats)") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("टिप्पणी / रिमार्क") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val totalSecs = (mins1600.toIntOrNull() ?: 7) * 60 + (secs1600.toIntOrNull() ?: 10)
                        onSubmitWorkout(
                            runKm.toDoubleOrNull() ?: 3.0,
                            totalSecs,
                            pushups.toIntOrNull() ?: 35,
                            situps.toIntOrNull() ?: 45,
                            pullups.toIntOrNull() ?: 8,
                            squats.toIntOrNull() ?: 50,
                            notes
                        )
                        showRecordDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary)
                ) {
                    Text("सेव करें (Save)")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRecordDialog = false }) {
                    Text("रद्द करें")
                }
            }
        )
    }
}

@Composable
fun ExerciseCategoryCard(
    categoryTitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    items: List<Pair<String, String>>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(color.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = categoryTitle,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            items.forEach { (name, desc) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "• $name",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = desc,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun PlanTargetChip(label: String, color: Color) {
    Surface(
        color = color.copy(alpha = 0.12f),
        shape = RoundedCornerShape(6.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = color,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
        )
    }
}

@Composable
fun TimeBox(label: String, time: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = time,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            color = color
        )
    }
}
