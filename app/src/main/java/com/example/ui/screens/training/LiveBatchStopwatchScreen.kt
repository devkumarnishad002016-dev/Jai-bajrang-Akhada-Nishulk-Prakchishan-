package com.example.ui.screens.training

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.RaceResult
import com.example.data.model.RaceSession
import com.example.data.model.StudentProfile
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel
import com.example.util.RolePermissionManager
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveBatchStopwatchScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentRole by viewModel.currentRole.collectAsState()
    val canOperate = RolePermissionManager.canOperateLiveStopwatch(currentRole)

    if (!canOperate) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("1600m लाइव बैच स्टॉपवॉच") },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Restricted",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "अनधिकृत पहुंच (Access Denied)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Text(
                            text = "लाइव 1600m बैच स्टॉपवॉच और फिनिश-लाइन टाइमर केवल ग्राउंड कोच एवं एडमिन के लिए उपलब्ध है।",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Button(
                            onClick = onNavigateBack,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("वापस जाएं")
                        }
                    }
                }
            }
        }
        return
    }

    val activeSession by viewModel.activeLiveSession.collectAsState()
    val liveParticipants by viewModel.liveParticipants.collectAsState()
    val isRunning by viewModel.isStopwatchRunning.collectAsState()
    val isPaused by viewModel.isStopwatchPaused.collectAsState()
    val elapsedMillis by viewModel.stopwatchElapsedMillis.collectAsState()
    val allStudents by viewModel.allStudents.collectAsState()
    val allRaceSessions by viewModel.allRaceSessions.collectAsState()
    val allRaceResults by viewModel.allRaceResults.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Live Recorder, 1: History
    var showSetupDialog by remember { mutableStateOf(false) }
    var showFinishConfirmDialog by remember { mutableStateOf(false) }
    var showResetConfirmDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var editingCadetResult by remember { mutableStateOf<RaceResult?>(null) }
    var selectedSessionForDetail by remember { mutableStateOf<RaceSession?>(null) }

    Scaffold(
        modifier = modifier.testTag("live_batch_stopwatch_screen"),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "1600m लाइव बैच टाइमर",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = if (RolePermissionManager.isAdmin(currentRole)) OliveTertiary else SaffronPrimary,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = if (RolePermissionManager.isAdmin(currentRole)) "ADMIN" else "COACH",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = if (activeSession != null) activeSession!!.batchName else "ग्राउंड ट्रायल रिकॉर्डर",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("stopwatch_back_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (activeSession == null) {
                        IconButton(
                            onClick = { showSetupDialog = true },
                            modifier = Modifier.testTag("btn_new_race_batch")
                        ) {
                            Icon(Icons.Default.AddCircle, contentDescription = "New Batch", tint = SaffronPrimary)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Tab Selector
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = SaffronPrimary
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (isRunning) Color.Red else Color.Gray)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("लाइव रिकॉर्डर", fontWeight = FontWeight.Bold)
                        }
                    },
                    modifier = Modifier.testTag("tab_live_recorder")
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("बैच इतिहास (${allRaceSessions.size})", fontWeight = FontWeight.Bold)
                        }
                    },
                    modifier = Modifier.testTag("tab_race_history")
                )
            }

            if (selectedTab == 0) {
                // Live Recorder Tab
                if (activeSession == null) {
                    // Empty State: Prompt to create new race batch
                    EmptyRaceSetupView(
                        onStartSetup = { showSetupDialog = true }
                    )
                } else {
                    // Active Race Session View
                    ActiveRaceStopwatchView(
                        session = activeSession!!,
                        participants = liveParticipants,
                        isRunning = isRunning,
                        isPaused = isPaused,
                        elapsedMillis = elapsedMillis,
                        searchQuery = searchQuery,
                        onSearchChange = { searchQuery = it },
                        onStart = { viewModel.startMasterStopwatch() },
                        onPause = { viewModel.pauseMasterStopwatch() },
                        onResume = { viewModel.resumeMasterStopwatch() },
                        onFinishCadet = { sId -> viewModel.recordCadetFinish(sId) },
                        onUndoCadet = { sId -> viewModel.undoCadetFinish(sId) },
                        onDnfCadet = { sId -> viewModel.markCadetDnf(sId) },
                        onEditCadet = { cadet -> editingCadetResult = cadet },
                        onCompleteBatch = { showFinishConfirmDialog = true },
                        onResetBatch = { showResetConfirmDialog = true },
                        formatTime = { ms -> viewModel.formatRaceTime(ms) }
                    )
                }
            } else {
                // Race History Tab
                RaceHistoryView(
                    sessions = allRaceSessions,
                    allResults = allRaceResults,
                    onSelectSession = { session -> selectedSessionForDetail = session },
                    onDeleteSession = { session -> viewModel.deleteRaceSession(session) }
                )
            }
        }
    }

    // Dialog: Setup New Batch
    if (showSetupDialog) {
        NewRaceBatchDialog(
            allStudents = allStudents,
            onDismiss = { showSetupDialog = false },
            onConfirm = { batchName, distanceMeters, selectedCadets, chestMap, notes ->
                viewModel.setupNewRaceSession(
                    batchName = batchName,
                    distanceMeters = distanceMeters,
                    participants = selectedCadets,
                    chestNumbers = chestMap,
                    notes = notes
                )
                showSetupDialog = false
                selectedTab = 0
            }
        )
    }

    // Dialog: Finish & Save Confirmation
    if (showFinishConfirmDialog) {
        val runningCount = liveParticipants.count { it.status == "RUNNING" }
        val finishedCount = liveParticipants.count { it.status == "FINISHED" }

        AlertDialog(
            onDismissRequest = { showFinishConfirmDialog = false },
            icon = { Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SaffronPrimary, modifier = Modifier.size(36.dp)) },
            title = { Text("बैच दौड़ समाप्त एवं सुरक्षित करें?", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("• कुल धावक: ${liveParticipants.size}")
                    Text("• फिनिश किए: $finishedCount धावक")
                    if (runningCount > 0) {
                        Text(
                            "• शेष $runningCount धावकों को DNF (Did Not Finish) दर्ज किया जाएगा।",
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Text(
                        "परिणाम स्थानीय Room SQLite डेटाबेस में सहेजे जाएंगे, क्लाउड सिंक आउटबॉक्स में दर्ज होंगे तथा छात्रों के ट्रेनिंग प्रोफाइल में ऑटो-अपडेट होंगे।",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showFinishConfirmDialog = false
                        viewModel.finishAndSaveLiveRace(autoUpdateCadetProfiles = true)
                        selectedTab = 1 // Switch to history tab to view saved results
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary),
                    modifier = Modifier.testTag("btn_confirm_save_race")
                ) {
                    Text("हां, सुरक्षित करें (Save)")
                }
            },
            dismissButton = {
                TextButton(onClick = { showFinishConfirmDialog = false }) {
                    Text("रद्द करें")
                }
            }
        )
    }

    // Dialog: Reset Confirmation
    if (showResetConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showResetConfirmDialog = false },
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("दौड़ सत्र रीसेट करें?", fontWeight = FontWeight.Bold) },
            text = { Text("क्या आप वर्तमान लाइव स्टॉपवॉच को रीसेट करना चाहते हैं? इससे बिना सेव किए गए सभी फिनिश टाइम मिट जाएंगे।") },
            confirmButton = {
                Button(
                    onClick = {
                        showResetConfirmDialog = false
                        viewModel.resetMasterStopwatch()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("रीसेट करें")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirmDialog = false }) {
                    Text("वापस")
                }
            }
        )
    }

    // Dialog: Edit Finish Time
    if (editingCadetResult != null) {
        EditCadetTimeDialog(
            result = editingCadetResult!!,
            onDismiss = { editingCadetResult = null },
            onSave = { newMillis ->
                viewModel.editCadetFinishTime(editingCadetResult!!.studentId, newMillis)
                editingCadetResult = null
            }
        )
    }

    // Dialog / Bottom Sheet: Session Breakdown
    if (selectedSessionForDetail != null) {
        RaceSessionDetailDialog(
            session = selectedSessionForDetail!!,
            results = allRaceResults.filter { it.raceId == selectedSessionForDetail!!.raceId },
            onDismiss = { selectedSessionForDetail = null }
        )
    }
}

// ==========================================
// Empty State: Prompt to Create Batch
// ==========================================
@Composable
private fun EmptyRaceSetupView(
    onStartSetup: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(SaffronPrimary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = "Stopwatch",
                        tint = SaffronPrimary,
                        modifier = Modifier.size(40.dp)
                    )
                }

                Text(
                    text = "1600m लाइव बैच स्टॉपवॉच",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "ग्राउंड पर 10 से 50 कैडेट्स की एक साथ टाइमिंग लें। मास्टर स्टॉपवॉच शुरू करें और फिनिश लाइन पर प्रत्येक कैडेट के चेस्ट नंबर पर टैप करके सटीक मिलीसेकंड टाइमिंग रिकॉर्ड करें।",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    FeaturePill(icon = Icons.Default.Bolt, label = "मिलीसेकंड प्रिसिजन")
                    FeaturePill(icon = Icons.Default.WifiOff, label = "100% ऑफलाइन")
                    FeaturePill(icon = Icons.Default.EmojiEvents, label = "ऑटो रैंक व PB")
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = onStartSetup,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("btn_start_new_race_batch"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("नया रनिंग बैच शुरू करें", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun FeaturePill(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(14.dp), tint = SaffronPrimary)
        Spacer(modifier = Modifier.width(4.dp))
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.Medium)
    }
}

// ==========================================
// Active Race Stopwatch View
// ==========================================
@Composable
private fun ActiveRaceStopwatchView(
    session: RaceSession,
    participants: List<RaceResult>,
    isRunning: Boolean,
    isPaused: Boolean,
    elapsedMillis: Long,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onFinishCadet: (String) -> Unit,
    onUndoCadet: (String) -> Unit,
    onDnfCadet: (String) -> Unit,
    onEditCadet: (RaceResult) -> Unit,
    onCompleteBatch: () -> Unit,
    onResetBatch: () -> Unit,
    formatTime: (Long) -> String,
    modifier: Modifier = Modifier
) {
    val finishedCount = participants.count { it.status == "FINISHED" }
    val totalCount = participants.size

    val filteredParticipants = remember(participants, searchQuery) {
        if (searchQuery.isBlank()) {
            participants
        } else {
            val q = searchQuery.trim().lowercase()
            participants.filter {
                it.chestNumber.lowercase().contains(q) ||
                it.studentName.lowercase().contains(q) ||
                it.studentId.lowercase().contains(q) ||
                it.village.lowercase().contains(q)
            }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("active_race_stopwatch_view"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Master Timer Display (Extra Large Digital Clock)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("master_stopwatch_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)), // Dark tactical theme
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = SaffronPrimary,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "${session.distanceMeters}m दौड़",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            isRunning && !isPaused -> Color(0xFF22C55E)
                                            isPaused -> Color(0xFFF59E0B)
                                            else -> Color(0xFF94A3B8)
                                        }
                                    )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = when {
                                    isRunning && !isPaused -> "दौड़ जारी (RUNNING)"
                                    isPaused -> "विराम (PAUSED)"
                                    else -> "तैयार (READY)"
                                },
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Digital Clock Display
                    Text(
                        text = formatTime(elapsedMillis),
                        fontSize = 46.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        color = when {
                            isRunning && !isPaused -> Color(0xFF4ADE80) // vibrant neon green
                            isPaused -> Color(0xFFFDE047) // warning yellow
                            else -> Color.White
                        },
                        letterSpacing = 2.sp,
                        modifier = Modifier.testTag("digital_clock_text")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Progress Stats
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF0F172A))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "फिनिशर्स: $finishedCount / $totalCount",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "शेष: ${totalCount - finishedCount}",
                            color = Color(0xFF94A3B8),
                            fontSize = 13.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Master Control Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (!isRunning) {
                            // START BUTTON
                            Button(
                                onClick = onStart,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("btn_master_start"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A))
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("दौड़ शुरू करें (START)", fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        } else {
                            if (!isPaused) {
                                // PAUSE BUTTON
                                Button(
                                    onClick = onPause,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .testTag("btn_master_pause"),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706))
                                ) {
                                    Icon(Icons.Default.Pause, contentDescription = null, tint = Color.White)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("विराम (Pause)", fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            } else {
                                // RESUME BUTTON
                                Button(
                                    onClick = onResume,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .testTag("btn_master_resume"),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A))
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("जारी रखें (Resume)", fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }

                            // FINISH BATCH BUTTON
                            Button(
                                onClick = onCompleteBatch,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("btn_master_save_batch"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary)
                            ) {
                                Icon(Icons.Default.Save, contentDescription = null, tint = Color.White)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("बैच सेव करें", fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }

                        // RESET ICON BUTTON
                        IconButton(
                            onClick = onResetBatch,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF334155))
                                .testTag("btn_master_reset")
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Reset", tint = Color.White)
                        }
                    }
                }
            }
        }

        // 2. Search / Filter Box for Quick Tactile Access during busy races
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_cadet_field"),
                placeholder = { Text("चेस्ट नंबर या नाम से खोजें...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchChange("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
        }

        // 3. Section Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "फिनिश लाइन रिकॉर्डर (Finish Line Tapper)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "टैप करें और समय दर्ज करें",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // 4. Cadets Grid / List
        items(filteredParticipants, key = { it.studentId }) { result ->
            CadetFinishRecorderCard(
                result = result,
                isTimerRunning = isRunning,
                onFinish = { onFinishCadet(result.studentId) },
                onUndo = { onUndoCadet(result.studentId) },
                onDnf = { onDnfCadet(result.studentId) },
                onEdit = { onEditCadet(result) }
            )
        }
    }
}

// ==========================================
// Individual Cadet Finish Line Recorder Card
// ==========================================
@Composable
private fun CadetFinishRecorderCard(
    result: RaceResult,
    isTimerRunning: Boolean,
    onFinish: () -> Unit,
    onUndo: () -> Unit,
    onDnf: () -> Unit,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isFinished = result.status == "FINISHED"
    val isDnf = result.status == "DNF"

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("cadet_card_${result.studentId}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isFinished -> Color(0xFFF0FDF4) // soft green
                isDnf -> Color(0xFFFEF2F2) // soft red
                else -> MaterialTheme.colorScheme.surface
            }
        ),
        border = when {
            isFinished -> androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF22C55E))
            isDnf -> androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444))
            else -> androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: Large Chest Number Badge
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        when {
                            isFinished -> Color(0xFF16A34A)
                            isDnf -> Color(0xFFDC2626)
                            else -> SaffronPrimary
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = result.chestNumber,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Middle: Name, Village, Status, Time
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = result.studentName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (result.isPersonalBest && isFinished) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Surface(
                            color = Color(0xFFEF4444),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "🔥 PB",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                }

                Text(
                    text = "${result.village} • ${result.studentId}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (isFinished) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        // Rank Badge
                        Surface(
                            color = when (result.rank) {
                                1 -> Color(0xFFEAB308) // Gold
                                2 -> Color(0xFF94A3B8) // Silver
                                3 -> Color(0xFFB45309) // Bronze
                                else -> Color(0xFF3B82F6) // Blue
                            },
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "रैंक #${result.rank}",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = result.timeFormatted,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFF15803D)
                        )
                    }
                } else if (isDnf) {
                    Text(
                        text = "DNF (Did Not Finish)",
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Right: Tactile Action Button
            if (!isFinished && !isDnf) {
                // Large High-Tactility FINISH BUTTON
                Button(
                    onClick = onFinish,
                    enabled = isTimerRunning,
                    modifier = Modifier
                        .height(48.dp)
                        .testTag("btn_finish_${result.studentId}"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A))
                ) {
                    Icon(Icons.Default.Flag, contentDescription = "Finish", tint = Color.White)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("फिनिश", fontWeight = FontWeight.Bold, color = Color.White)
                }
            } else if (isFinished) {
                // Options for Finished Cadet: Undo or Edit
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Time", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(
                        onClick = onUndo,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.Undo, contentDescription = "Undo Finish", tint = MaterialTheme.colorScheme.error)
                    }
                }
            } else {
                // DNF: Allow Undo
                IconButton(
                    onClick = onUndo,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(Icons.Default.Undo, contentDescription = "Undo DNF", tint = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

// ==========================================
// Dialog: Setup New Race Batch
// ==========================================
@Composable
private fun NewRaceBatchDialog(
    allStudents: List<StudentProfile>,
    onDismiss: () -> Unit,
    onConfirm: (batchName: String, distanceMeters: Int, selectedCadets: List<StudentProfile>, chestMap: Map<String, String>, notes: String) -> Unit
) {
    var batchName by remember { mutableStateOf("1600m ट्रायल बैच - " + SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date())) }
    var distanceMeters by remember { mutableIntStateOf(1600) }
    val selectedStudents = remember { mutableStateMapOf<String, Boolean>() }
    val chestNumbers = remember { mutableStateMapOf<String, String>() }
    var notes by remember { mutableStateOf("") }
    var searchQuery by remember { mutableStateOf("") }

    // Initialize all selected by default
    LaunchedEffect(allStudents) {
        allStudents.forEachIndexed { idx, s ->
            selectedStudents[s.studentId] = true
            chestNumbers[s.studentId] = (101 + idx).toString()
        }
    }

    val filteredList = remember(allStudents, searchQuery) {
        if (searchQuery.isBlank()) allStudents
        else {
            val q = searchQuery.trim().lowercase()
            allStudents.filter { it.fullName.lowercase().contains(q) || it.studentId.lowercase().contains(q) || it.village.lowercase().contains(q) }
        }
    }

    val selectedCount = selectedStudents.values.count { it }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("नया रनिंग बैच सेटअप करें", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 480.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Batch Name
                OutlinedTextField(
                    value = batchName,
                    onValueChange = { batchName = it },
                    label = { Text("बैच का नाम") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Distance Selector
                Text("दूरी (Distance):", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(400, 800, 1600, 5000).forEach { dist ->
                        FilterChip(
                            selected = distanceMeters == dist,
                            onClick = { distanceMeters = dist },
                            label = { Text("${dist}m", fontWeight = FontWeight.Bold) }
                        )
                    }
                }

                // Notes
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("ट्रैक / ग्राउंड नोट्स (वैकल्पिक)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Cadets Selector Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "धावक चयन ($selectedCount / ${allStudents.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    TextButton(
                        onClick = {
                            val allSel = selectedStudents.values.all { it }
                            allStudents.forEach { s -> selectedStudents[s.studentId] = !allSel }
                        }
                    ) {
                        Text(if (selectedStudents.values.all { it }) "Deselect All" else "Select All")
                    }
                }

                // Search Cadets
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("छात्र खोजें...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Cadet Selection List with Chest Number Input
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(filteredList, key = { it.studentId }) { student ->
                        val isChecked = selectedStudents[student.studentId] ?: false
                        val chest = chestNumbers[student.studentId] ?: ""

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isChecked) SaffronPrimary.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .clickable { selectedStudents[student.studentId] = !isChecked }
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = { selectedStudents[student.studentId] = it }
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(student.fullName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("${student.studentId} • ${student.village}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            if (isChecked) {
                                OutlinedTextField(
                                    value = chest,
                                    onValueChange = { chestNumbers[student.studentId] = it },
                                    label = { Text("चेस्ट #") },
                                    singleLine = true,
                                    modifier = Modifier.width(80.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val chosen = allStudents.filter { selectedStudents[it.studentId] == true }
                    if (chosen.isNotEmpty()) {
                        onConfirm(batchName, distanceMeters, chosen, chestNumbers, notes)
                    }
                },
                enabled = selectedCount > 0,
                colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary),
                modifier = Modifier.testTag("btn_confirm_setup_batch")
            ) {
                Text("बैच तैयार करें ($selectedCount)")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("रद्द करें")
            }
        }
    )
}

// ==========================================
// Dialog: Edit Cadet Finish Time
// ==========================================
@Composable
private fun EditCadetTimeDialog(
    result: RaceResult,
    onDismiss: () -> Unit,
    onSave: (Long) -> Unit
) {
    var minStr by remember {
        val totalSec = result.elapsedMillis / 1000
        val min = totalSec / 60
        mutableStateOf(min.toString())
    }
    var secStr by remember {
        val totalSec = result.elapsedMillis / 1000
        val sec = totalSec % 60
        mutableStateOf(sec.toString())
    }
    var csStr by remember {
        val cs = (result.elapsedMillis % 1000) / 10
        mutableStateOf(cs.toString())
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("फिनिश समय संपादित करें", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("धावक: ${result.studentName} (चेस्ट #${result.chestNumber})")
                Text("वर्तमान समय: ${result.timeFormatted}", fontWeight = FontWeight.Bold, color = SaffronPrimary)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = minStr,
                        onValueChange = { if (it.length <= 2) minStr = it.filter { c -> c.isDigit() } },
                        label = { Text("मिनट (Min)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = secStr,
                        onValueChange = { if (it.length <= 2) secStr = it.filter { c -> c.isDigit() } },
                        label = { Text("सेकंड (Sec)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = csStr,
                        onValueChange = { if (it.length <= 2) csStr = it.filter { c -> c.isDigit() } },
                        label = { Text("मि.से. (CS)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val m = minStr.toLongOrNull() ?: 0L
                    val s = secStr.toLongOrNull() ?: 0L
                    val cs = csStr.toLongOrNull() ?: 0L
                    val totalMs = (m * 60 + s) * 1000 + (cs * 10)
                    if (totalMs > 0) {
                        onSave(totalMs)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary)
            ) {
                Text("अपडेट करें")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("रद्द करें")
            }
        }
    )
}

// ==========================================
// Race History View
// ==========================================
@Composable
private fun RaceHistoryView(
    sessions: List<RaceSession>,
    allResults: List<RaceResult>,
    onSelectSession: (RaceSession) -> Unit,
    onDeleteSession: (RaceSession) -> Unit,
    modifier: Modifier = Modifier
) {
    if (sessions.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.History, contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(48.dp))
                Text("कोई पुराना दौड़ बैच रिकॉर्ड नहीं मिला", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("नया रनिंग बैच शुरू करके फिनिश लाइन टाइमिंग रिकॉर्ड करें।", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        return
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("race_history_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(sessions.sortedByDescending { it.createdAt }, key = { it.raceId }) { session ->
            val sessionResults = allResults.filter { it.raceId == session.raceId }.sortedBy { it.rank.takeIf { r -> r > 0 } ?: 999 }
            val topFinisher = sessionResults.firstOrNull { it.status == "FINISHED" }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectSession(session) }
                    .testTag("race_session_item_${session.raceId}"),
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
                        Surface(
                            color = SaffronPrimary,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "${session.distanceMeters}m",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }

                        Text(
                            text = session.date,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = session.batchName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    if (session.notes.isNotBlank()) {
                        Text(
                            text = session.notes,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "धावक: ${session.finishedCount}/${session.totalParticipants}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (topFinisher != null) {
                            Text(
                                text = "🥇 ${topFinisher.studentName} (${topFinisher.timeFormatted})",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = SaffronPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// Dialog: Race Session Detail
// ==========================================
@Composable
private fun RaceSessionDetailDialog(
    session: RaceSession,
    results: List<RaceResult>,
    onDismiss: () -> Unit
) {
    val sortedResults = results.sortedWith(
        compareBy<RaceResult> { if (it.status == "FINISHED") 0 else 1 }
            .thenBy { it.rank }
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(session.batchName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Text("${session.distanceMeters}m दौड़ • ${session.date} • रिकॉर्डकर्ता: ${session.recordedBy}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("रैंक व धावक", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text("चेस्ट # / समय", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(sortedResults, key = { it.studentId }) { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (item.status == "FINISHED") Color(0xFFF0FDF4) else Color(0xFFFEF2F2))
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (item.status == "FINISHED") {
                                    Surface(
                                        color = when (item.rank) {
                                            1 -> Color(0xFFEAB308)
                                            2 -> Color(0xFF94A3B8)
                                            3 -> Color(0xFFB45309)
                                            else -> Color(0xFF3B82F6)
                                        },
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = "#${item.rank}",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                } else {
                                    Surface(
                                        color = Color(0xFFEF4444),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = "DNF",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(item.studentName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        if (item.isPersonalBest) {
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("🔥", fontSize = 10.sp)
                                        }
                                    }
                                    Text(item.village, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = if (item.status == "FINISHED") item.timeFormatted else "DNF",
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 13.sp,
                                    color = if (item.status == "FINISHED") Color(0xFF15803D) else Color(0xFFDC2626)
                                )
                                Text("चेस्ट: ${item.chestNumber}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("बंद करें")
            }
        }
    )
}
