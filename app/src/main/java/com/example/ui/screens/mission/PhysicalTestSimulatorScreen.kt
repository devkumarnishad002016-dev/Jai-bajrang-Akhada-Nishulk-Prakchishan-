package com.example.ui.screens.mission

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.mission.PhysicalSimulatorInput
import com.example.data.mission.PhysicalTestSimulatorEngine
import com.example.data.model.StudentProfile
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhysicalTestSimulatorScreen(
    student: StudentProfile?,
    onSaveResultToRecord: ((PhysicalSimulatorInput, Int) -> Unit)? = null,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedExam by remember {
        mutableStateOf(student?.recruitmentGoal?.takeIf { it in listOf("Indian Army", "CG Police", "SSC GD") } ?: "Indian Army")
    }

    // Input States
    var min1600 by remember { mutableStateOf("5") }
    var sec1600 by remember { mutableStateOf("32") }
    var sec100 by remember { mutableStateOf("13.2") }
    var min800 by remember { mutableStateOf("2") }
    var sec800 by remember { mutableStateOf("15") }
    var pullups by remember { mutableStateOf(student?.pullups?.takeIf { it > 0 }?.toString() ?: "10") }
    var pushups by remember { mutableStateOf(student?.pushups?.takeIf { it > 0 }?.toString() ?: "40") }
    var longJumpMeters by remember { mutableStateOf("5.2") }
    var highJumpMeters by remember { mutableStateOf("1.45") }
    var shotPutMeters by remember { mutableStateOf("8.8") }
    var ditchPassed by remember { mutableStateOf(true) }
    var balancePassed by remember { mutableStateOf(true) }
    var showSavedSnackbar by remember { mutableStateOf(false) }

    val simulatorInput = remember(
        selectedExam, min1600, sec1600, sec100, min800, sec800,
        pullups, pushups, longJumpMeters, highJumpMeters, shotPutMeters,
        ditchPassed, balancePassed
    ) {
        PhysicalSimulatorInput(
            examTarget = selectedExam,
            time1600mMinutes = min1600.toIntOrNull() ?: 5,
            time1600mSeconds = sec1600.toIntOrNull() ?: 30,
            time100mSeconds = sec100.toDoubleOrNull() ?: 13.0,
            time800mMinutes = min800.toIntOrNull() ?: 2,
            time800mSeconds = sec800.toIntOrNull() ?: 15,
            pullupsBeam = pullups.toIntOrNull() ?: 10,
            pushupsCount = pushups.toIntOrNull() ?: 40,
            longJumpMeters = longJumpMeters.toDoubleOrNull() ?: 5.0,
            highJumpMeters = highJumpMeters.toDoubleOrNull() ?: 1.40,
            shotPutMeters = shotPutMeters.toDoubleOrNull() ?: 8.5,
            ditchJumpPassed = ditchPassed,
            zigZagBalancePassed = balancePassed
        )
    }

    val report = remember(simulatorInput, student) {
        PhysicalTestSimulatorEngine.simulate(simulatorInput, student)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "फिजिकल टेस्ट सिम्युलेटर",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "भर्ती मानक, स्कोरिंग एवं सुधार तुलना",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("simulator_back_button")
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
        snackbarHost = {
            if (showSavedSnackbar) {
                Snackbar(
                    modifier = Modifier.padding(16.dp),
                    action = {
                        TextButton(onClick = { showSavedSnackbar = false }) {
                            Text("ठीक है", color = Color.White)
                        }
                    }
                ) {
                    Text("सिम्युलेटर परिणाम रिकॉर्ड में सुरक्षित किया गया!")
                }
            }
        },
        modifier = modifier.testTag("physical_test_simulator_screen")
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Target Exam Selector
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "लक्षित भर्ती परीक्षा चुनें (Target Exam Standard):",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(
                                "Indian Army" to "आर्मी GD",
                                "CG Police" to "CG पुलिस",
                                "SSC GD" to "SSC GD"
                            ).forEach { (examKey, label) ->
                                val isSelected = selectedExam == examKey
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable { selectedExam = examKey }
                                        .testTag("simulator_exam_$examKey"),
                                    color = if (isSelected) SaffronPrimary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = label,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = if (examKey == "Indian Army") "100 अंक मेरिट" else if (examKey == "CG Police") "5 स्पर्धा 100 अंक" else "क्वालिफाइंग",
                                            fontSize = 10.sp,
                                            color = if (isSelected) Color.White.copy(alpha = 0.9f) else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Real-Time Simulation Result Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("simulator_result_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                    border = BorderStroke(1.dp, Color(report.statusBadgeColorHex).copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = report.examTarget,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "कुल फिजिकल स्कोर: ${report.totalPhysicalMarks} / ${report.maxPhysicalMarks}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Surface(
                                color = Color(report.statusBadgeColorHex),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = report.qualificationStatusHindi,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Score percentage indicator
                        LinearProgressIndicator(
                            progress = { report.percentage / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(RoundedCornerShape(5.dp)),
                            color = Color(report.statusBadgeColorHex),
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lightbulb,
                                    contentDescription = null,
                                    tint = SaffronPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = report.coachGuidanceHindi,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            // Metric Breakdown Table
            item {
                Text(
                    text = "इवेंट-वार स्कोरिंग विवरण (Event Breakdown)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            items(report.metricBreakdown) { metric ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (metric.isQualified) OliveTertiary.copy(alpha = 0.06f) else MaterialTheme.colorScheme.surface
                    ),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = metric.eventNameHindi,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "आपका समय / माप: ${metric.userValueFormatted}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = metric.benchmarkDescription,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Gray
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Surface(
                                color = if (metric.isQualified) OliveTertiary else Color(0xFFD32F2F),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = if (metric.maxMarks > 0) "${metric.marksAwarded}/${metric.maxMarks} अंक" else if (metric.isQualified) "पास" else "फेल",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = metric.statusHindi,
                                fontSize = 10.sp,
                                color = if (metric.isQualified) OliveTertiary else Color(0xFFD32F2F)
                            )
                        }
                    }
                }
            }

            // Comparison with Previous Records
            if (report.comparisonWithPrevious.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "पूर्व रिकॉर्ड से सुधार तुलना (Comparison with Best)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                items(report.comparisonWithPrevious) { delta ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = CardDefaults.outlinedCardBorder()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = delta.metricName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "वर्तमान: ${delta.currentValue} • पूर्व: ${delta.previousValue}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Surface(
                                color = if (delta.isBetter) OliveTertiary.copy(alpha = 0.15f) else Color.Gray.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = delta.improvementText,
                                    color = if (delta.isBetter) OliveTertiary else Color.DarkGray,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Data Entry Inputs Form
            item {
                Spacer(modifier = Modifier.height(6.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "ग्राउंड ट्रायल परिणाम प्रविष्टि (Enter Trial Values)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = SaffronDark
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        // 1600m Running Input
                        Text("1600m रनिंग टाइम:", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = min1600,
                                onValueChange = { min1600 = it },
                                label = { Text("मिनट (Min)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("simulator_1600m_min")
                            )
                            OutlinedTextField(
                                value = sec1600,
                                onValueChange = { sec1600 = it },
                                label = { Text("सेकंड (Sec)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("simulator_1600m_sec")
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        if (selectedExam == "Indian Army") {
                            // Pullups
                            OutlinedTextField(
                                value = pullups,
                                onValueChange = { pullups = it },
                                label = { Text("बीम / पुल-अप्स संख्या (10 लक्ष्य)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("simulator_pullups_input")
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Ditch & Zig-Zag Balance Checks
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("9-फीट गड्ढा कूद (Ditch Jump):", fontSize = 13.sp)
                                Switch(
                                    checked = ditchPassed,
                                    onCheckedChange = { ditchPassed = it },
                                    modifier = Modifier.testTag("simulator_ditch_switch")
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("ज़िग-ज़ैग बैलेंस (Balance Beam):", fontSize = 13.sp)
                                Switch(
                                    checked = balancePassed,
                                    onCheckedChange = { balancePassed = it },
                                    modifier = Modifier.testTag("simulator_balance_switch")
                                )
                            }
                        } else if (selectedExam == "CG Police") {
                            // 100m sprint & 800m
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = sec100,
                                    onValueChange = { sec100 = it },
                                    label = { Text("100m समय (Sec)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = sec800,
                                    onValueChange = { sec800 = it },
                                    label = { Text("800m सेकंड") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Long Jump & High Jump & Shotput
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = longJumpMeters,
                                    onValueChange = { longJumpMeters = it },
                                    label = { Text("लंबी कूद (मीटर)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = highJumpMeters,
                                    onValueChange = { highJumpMeters = it },
                                    label = { Text("ऊंची कूद (मीटर)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = shotPutMeters,
                                onValueChange = { shotPutMeters = it },
                                label = { Text("गोला फेंक दूरी (मीटर)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.fillMaxWidth()
                            )
                        } else {
                            // SSC GD
                            OutlinedTextField(
                                value = pushups,
                                onValueChange = { pushups = it },
                                label = { Text("पुश-अप्स संख्या") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Save Result Button
                        Button(
                            onClick = {
                                onSaveResultToRecord?.invoke(simulatorInput, report.totalPhysicalMarks)
                                showSavedSnackbar = true
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("simulator_save_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("यह ट्रायल परिणाम सुरक्षित करें (Save Result)")
                        }
                    }
                }
            }
        }
    }
}
