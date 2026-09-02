package com.example.ui.screens.admin

import androidx.compose.animation.*
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppNotification
import com.example.data.model.StudentProfile
import com.example.ui.theme.*
import com.example.util.RolePermissionManager

/**
 * Phase 5B — Admin & Trainer Secure Communication & Broadcast Hub.
 * Supports: Announcements, Notices, Recruitment Alerts, Training Reminders, Study Reminders,
 * Targeted cadet/trainer dispatch, Quick Presets, and Full Notification History.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminCommunicationScreen(
    allNotifications: List<AppNotification>,
    allStudents: List<StudentProfile>,
    currentRole: String = "ADMIN",
    onSendNotification: (
        title: String,
        message: String,
        category: String,
        targetType: String,
        targetStudentIds: List<String>,
        targetBatch: String,
        targetTrainerId: String,
        actionRoute: String,
        isUrgent: Boolean,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) -> Unit,
    onDeleteNotification: (notifId: String) -> Unit = {},
    onMarkAllRead: () -> Unit = {},
    onBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isAdmin = RolePermissionManager.isAdmin(currentRole)
    val isTrainer = RolePermissionManager.isTrainer(currentRole)

    var selectedTab by remember { mutableStateOf(0) } // 0 = Compose Broadcast, 1 = History & Logs
    var selectedCategory by remember { mutableStateOf(AppNotification.CATEGORY_ANNOUNCEMENT) }
    var title by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var targetType by remember { mutableStateOf(if (isTrainer && !isAdmin) AppNotification.TARGET_TRAINER_GROUP else AppNotification.TARGET_ALL_STUDENTS) }
    var selectedBatch by remember { mutableStateOf("सुबह आर्मी स्पेशल बैच (Morning Army Batch)") }
    var isUrgent by remember { mutableStateOf(false) }
    var actionRoute by remember { mutableStateOf("dashboard") }

    // Multi-select for target students
    var selectedStudentIds by remember { mutableStateOf(setOf<String>()) }
    var showStudentPickerSheet by remember { mutableStateOf(false) }
    var studentSearchQuery by remember { mutableStateOf("") }

    // Feedback SnackBar
    var snackbarMessage by remember { mutableStateOf<String?>(null) }
    var snackbarIsError by remember { mutableStateOf(false) }

    // History Filters
    var historyFilterCategory by remember { mutableStateOf("ALL") }
    var historySearchQuery by remember { mutableStateOf("") }

    val batches = listOf(
        "सुबह आर्मी स्पेशल बैच (Morning Army Batch)",
        "शाम पुलिस कांस्टेबल बैच (Evening Police Batch)",
        "SSC GD स्पेशल मिशन बैच (SSC GD Mission Batch)",
        "ऑल इंडिया फिजिकल ट्रायल्स बैच (All India Trials Batch)"
    )

    val actionRoutes = listOf(
        Pair("dashboard", "🏠 होम डैशबोर्ड"),
        Pair("ground_training", "🏃 ग्राउंड 1600m ट्रेनिंग व टाइमर"),
        Pair("mock_tests", "📖 ऑल इंडिया मॉक टेस्ट"),
        Pair("study_material", "📚 स्टडी मटेरियल व नोट्स"),
        Pair("recruitment", "🎖️ भर्ती सूचना व फॉर्म लिंक"),
        Pair("admin_notices", "📋 आधिकारिक नोटिस बोर्ड")
    )

    // Quick Templates
    val quickTemplates = listOf(
        QuickTemplate(
            label = "🏃 1600m ट्रायल रिमाइंडर",
            title = "कल प्रातः 1600m स्पीड ट्रायल व बीम टेस्ट",
            message = "कल सुबह ठीक 05:15 बजे मौरिकला ग्राउंड पर 1600m का टाइम ट्रायल होगा। सभी कैडेट्स समय पर ग्राउंड पर उपस्थित रहें।",
            category = AppNotification.CATEGORY_TRAINING_REMINDER,
            route = "ground_training",
            urgent = true
        ),
        QuickTemplate(
            label = "🎖️ अग्निवीर भर्ती अलर्ट",
            title = "इंडियन आर्मी अग्निवीर रैली 2026 ऑनलाइन फॉर्म शुरू",
            message = "अग्निवीर भर्ती 2026 के लिए ऑनलाइन रजिस्ट्रेशन प्रारंभ हो चुका है। आवश्यक डॉक्यूमेंट्स तैयार कर अखाड़ा कार्यालय से संपर्क करें।",
            category = AppNotification.CATEGORY_RECRUITMENT_ALERT,
            route = "recruitment",
            urgent = true
        ),
        QuickTemplate(
            label = "📖 संडे मॉक टेस्ट",
            title = "साप्ताहिक फुल-लेंथ ऑल इंडिया टेस्ट लाइव",
            message = "50 प्रश्नों का लाइव टेस्ट ऐप में उपलब्ध है। सभी कैडेट्स 60 मिनट की समय सीमा में टेस्ट पूरा कर अपनी ऑल इंडिया रैंक देखें।",
            category = AppNotification.CATEGORY_STUDY_REMINDER,
            route = "mock_tests",
            urgent = false
        ),
        QuickTemplate(
            label = "📢 अनुशासन व डाइट निर्देश",
            title = "अनुशासन एवं डाइट चार्ट संबंधी आवश्यक निर्देश",
            message = "ग्राउंड वर्कआउट के बाद स्प्राउट्स (चना-मूंग) व गुड़ का सेवन अनिवार्य है। बिना सूचना अनुपस्थित रहने पर दंड देय होगा।",
            category = AppNotification.CATEGORY_ANNOUNCEMENT,
            route = "dashboard",
            urgent = false
        )
    )

    val filteredHistory = remember(allNotifications, historyFilterCategory, historySearchQuery) {
        allNotifications.filter { notif ->
            val matchCat = if (historyFilterCategory == "ALL") true else notif.category == historyFilterCategory
            val matchSearch = if (historySearchQuery.isBlank()) true else {
                val q = historySearchQuery.trim().lowercase()
                notif.title.lowercase().contains(q) || notif.message.lowercase().contains(q) || notif.senderName.lowercase().contains(q)
            }
            matchCat && matchSearch
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (isAdmin) "संचार एवं सूचना केंद्र (Broadcast Hub)" else "कोच संचार पोर्टल (Coach Communication)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isAdmin) "कैडेट्स एवं कोचों को आधिकारिक संदेश व अलर्ट भेजें" else "अपने बैच के कैडेट्स को प्रशिक्षण स्मरण भेजें",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("btn_back_comm")) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (selectedTab == 1 && allNotifications.isNotEmpty()) {
                        IconButton(onClick = onMarkAllRead, modifier = Modifier.testTag("btn_mark_all_read")) {
                            Icon(Icons.Default.DoneAll, contentDescription = "Mark All Read", tint = SaffronPrimary)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
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
                            Icon(Icons.Default.Campaign, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("नया संदेश भेजें (Compose)", fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal)
                        }
                    },
                    modifier = Modifier.testTag("tab_compose_comm")
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("संदेश इतिहास (${allNotifications.size})", fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal)
                        }
                    },
                    modifier = Modifier.testTag("tab_history_comm")
                )
            }

            // SnackBar Feedback Banner
            snackbarMessage?.let { msg ->
                Surface(
                    color = if (snackbarIsError) MaterialTheme.colorScheme.errorContainer else ArmySuccess.copy(alpha = 0.15f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = msg,
                            color = if (snackbarIsError) MaterialTheme.colorScheme.error else ArmySuccess,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { snackbarMessage = null }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Close", modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            if (selectedTab == 0) {
                // ==========================================
                // COMPOSE BROADCAST TAB
                // ==========================================
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    // Quick Presets Carousel
                    item {
                        Text(
                            text = "त्वरित टेम्पलेट्स (Quick Presets)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(quickTemplates) { tmpl ->
                                SuggestionChip(
                                    onClick = {
                                        title = tmpl.title
                                        message = tmpl.message
                                        selectedCategory = tmpl.category
                                        actionRoute = tmpl.route
                                        isUrgent = tmpl.urgent
                                    },
                                    label = { Text(tmpl.label, fontSize = 12.sp) },
                                    colors = SuggestionChipDefaults.suggestionChipColors(
                                        containerColor = SaffronPrimary.copy(alpha = 0.08f),
                                        labelColor = SaffronPrimary
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.testTag("preset_${tmpl.category.lowercase()}")
                                )
                            }
                        }
                    }

                    // 1. Category Selector
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "1. सूचना श्रेणी चुनें (Select Category)",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(10.dp))

                                val categories = listOf(
                                    Triple(AppNotification.CATEGORY_ANNOUNCEMENT, "📢 घोषणा (Announcement)", OliveTertiary),
                                    Triple(AppNotification.CATEGORY_NOTICE, "📋 आधिकारिक नोटिस (Notice)", SaffronPrimary),
                                    Triple(AppNotification.CATEGORY_RECRUITMENT_ALERT, "🎖️ भर्ती अलर्ट (Recruitment Alert)", SaffronSecondary),
                                    Triple(AppNotification.CATEGORY_TRAINING_REMINDER, "🏃 ग्राउंड ट्रेनिंग स्मरण (Training)", ArmyGreen),
                                    Triple(AppNotification.CATEGORY_STUDY_REMINDER, "📖 अध्ययन व टेस्ट स्मरण (Study)", DeepNavy)
                                )

                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    categories.forEach { (catKey, catLabel, catColor) ->
                                        Surface(
                                            color = if (selectedCategory == catKey) catColor.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                            shape = RoundedCornerShape(10.dp),
                                            border = if (selectedCategory == catKey) androidx.compose.foundation.BorderStroke(1.5.dp, catColor) else null,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { selectedCategory = catKey }
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                RadioButton(
                                                    selected = selectedCategory == catKey,
                                                    onClick = { selectedCategory = catKey },
                                                    colors = RadioButtonDefaults.colors(selectedColor = catColor)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = catLabel,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = if (selectedCategory == catKey) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (selectedCategory == catKey) catColor else MaterialTheme.colorScheme.onSurface
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 2. Audience / Target Selector
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "2. संदेश प्राप्तकर्ता लक्ष्य (Target Audience)",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(10.dp))

                                val targetOptions = if (isAdmin) {
                                    listOf(
                                        Pair(AppNotification.TARGET_ALL_STUDENTS, "👥 सभी छात्र / कैडेट्स (All Students)"),
                                        Pair(AppNotification.TARGET_SELECTED_STUDENTS, "🎯 चयनित कैडेट्स (Selected Cadets - ${selectedStudentIds.size} चुने गए)"),
                                        Pair(AppNotification.TARGET_TRAINERS, "🥋 केवल सभी कोच व प्रशिक्षक (All Trainers)"),
                                        Pair(AppNotification.TARGET_TRAINER_GROUP, "🏷️ विशिष्ट बैच (Specific Batch)")
                                    )
                                } else {
                                    listOf(
                                        Pair(AppNotification.TARGET_TRAINER_GROUP, "🏷️ मेरे अधिकृत बैच के कैडेट्स (Assigned Batch)"),
                                        Pair(AppNotification.TARGET_SELECTED_STUDENTS, "🎯 मेरे चयनित छात्र (Selected Cadets - ${selectedStudentIds.size} चुने गए)")
                                    )
                                }

                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    targetOptions.forEach { (tKey, tLabel) ->
                                        Surface(
                                            color = if (targetType == tKey) SaffronPrimary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                            shape = RoundedCornerShape(10.dp),
                                            border = if (targetType == tKey) androidx.compose.foundation.BorderStroke(1.5.dp, SaffronPrimary) else null,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { targetType = tKey }
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                RadioButton(
                                                    selected = targetType == tKey,
                                                    onClick = { targetType = tKey },
                                                    colors = RadioButtonDefaults.colors(selectedColor = SaffronPrimary)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = tLabel,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = if (targetType == tKey) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (targetType == tKey) SaffronPrimary else MaterialTheme.colorScheme.onSurface
                                                )
                                            }
                                        }
                                    }
                                }

                                // If selected students, show picker button
                                if (targetType == AppNotification.TARGET_SELECTED_STUDENTS) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    OutlinedButton(
                                        onClick = { showStudentPickerSheet = true },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("btn_open_student_picker"),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(if (selectedStudentIds.isEmpty()) "छात्र चुनें (Select Students)" else "चयनित छात्र बदलें (${selectedStudentIds.size} कैडेट्स)")
                                    }
                                }

                                // If batch targeted, show batch selector dropdown/chips
                                if (targetType == AppNotification.TARGET_TRAINER_GROUP) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text("बैच चुनें:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        batches.forEach { bName ->
                                            FilterChip(
                                                selected = selectedBatch == bName,
                                                onClick = { selectedBatch = bName },
                                                label = { Text(bName, fontSize = 12.sp) },
                                                colors = FilterChipDefaults.filterChipColors(
                                                    selectedContainerColor = SaffronPrimary,
                                                    selectedLabelColor = Color.White
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 3. Message Content Fields
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "3. संदेश विवरण (Message Content)",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(12.dp))

                                OutlinedTextField(
                                    value = title,
                                    onValueChange = { title = it },
                                    label = { Text("संदेश शीर्षक (Title) *") },
                                    placeholder = { Text("उदा. कल 1600m ट्रायल एवं बीम टेस्ट...") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("input_comm_title"),
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp),
                                    leadingIcon = { Icon(Icons.Default.Title, contentDescription = null) }
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                OutlinedTextField(
                                    value = message,
                                    onValueChange = { message = it },
                                    label = { Text("विस्तृत संदेश (Detailed Message Body) *") },
                                    placeholder = { Text("सूचना, समय, स्थान एवं आवश्यक निर्देशों का विस्तृत विवरण यहाँ लिखें...") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(130.dp)
                                        .testTag("input_comm_message"),
                                    maxLines = 5,
                                    shape = RoundedCornerShape(10.dp),
                                    leadingIcon = { Icon(Icons.Default.Message, contentDescription = null) }
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                // Urgent Priority Switch
                                Surface(
                                    color = if (isUrgent) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                Icons.Default.PriorityHigh,
                                                contentDescription = null,
                                                tint = if (isUrgent) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column {
                                                Text(
                                                    text = "अत्यावश्यक अलर्ट (Urgent Alert)",
                                                    fontWeight = FontWeight.Bold,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = if (isUrgent) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    text = "कैडेट्स को हाई-प्रायोरिटी नोटिफिकेशन प्रदर्शित करेगा",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                        Switch(
                                            checked = isUrgent,
                                            onCheckedChange = { isUrgent = it },
                                            modifier = Modifier.testTag("switch_comm_urgent")
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Deep Link Destination Selector
                                Text("क्लिक करने पर गंतव्य स्क्रीन (Action Route):", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(6.dp))
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    items(actionRoutes) { (routeKey, routeLabel) ->
                                        FilterChip(
                                            selected = actionRoute == routeKey,
                                            onClick = { actionRoute = routeKey },
                                            label = { Text(routeLabel, fontSize = 11.sp) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = OliveTertiary,
                                                selectedLabelColor = Color.White
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 4. Send Button
                    item {
                        Button(
                            onClick = {
                                if (title.isBlank() || message.isBlank()) {
                                    snackbarMessage = "कृपया शीर्षक और संदेश दोनों दर्ज करें।"
                                    snackbarIsError = true
                                    return@Button
                                }
                                if (targetType == AppNotification.TARGET_SELECTED_STUDENTS && selectedStudentIds.isEmpty()) {
                                    snackbarMessage = "कृपया कम से कम एक छात्र चुनें।"
                                    snackbarIsError = true
                                    return@Button
                                }

                                onSendNotification(
                                    title,
                                    message,
                                    selectedCategory,
                                    targetType,
                                    selectedStudentIds.toList(),
                                    if (targetType == AppNotification.TARGET_TRAINER_GROUP) selectedBatch else "",
                                    if (isTrainer) "TR-001" else "",
                                    actionRoute,
                                    isUrgent,
                                    {
                                        snackbarMessage = "✅ सूचना सफलतापूर्वक प्रसारित व सिंक कर दी गई!"
                                        snackbarIsError = false
                                        title = ""
                                        message = ""
                                        selectedStudentIds = emptySet()
                                    },
                                    { err ->
                                        snackbarMessage = "त्रुटि: $err"
                                        snackbarIsError = true
                                    }
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("btn_send_broadcast"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary)
                        ) {
                            Icon(Icons.Default.Send, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "सूचना व संदेश प्रसारित करें (Broadcast Now)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            } else {
                // ==========================================
                // NOTIFICATION HISTORY & LOGS TAB
                // ==========================================
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    // Search and Category Filter
                    item {
                        OutlinedTextField(
                            value = historySearchQuery,
                            onValueChange = { historySearchQuery = it },
                            placeholder = { Text("इतिहास में खोजें (शीर्षक / संदेश)...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_history_search"),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                            trailingIcon = {
                                if (historySearchQuery.isNotEmpty()) {
                                    IconButton(onClick = { historySearchQuery = "" }) {
                                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                                    }
                                }
                            }
                        )
                    }

                    item {
                        val filterCats = listOf(
                            Pair("ALL", "सभी (All)"),
                            Pair(AppNotification.CATEGORY_ANNOUNCEMENT, "घोषणाएं"),
                            Pair(AppNotification.CATEGORY_NOTICE, "नोटिस"),
                            Pair(AppNotification.CATEGORY_RECRUITMENT_ALERT, "भर्ती अलर्ट"),
                            Pair(AppNotification.CATEGORY_TRAINING_REMINDER, "ट्रेनिंग"),
                            Pair(AppNotification.CATEGORY_STUDY_REMINDER, "स्टडी")
                        )

                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(filterCats) { (catKey, catName) ->
                                FilterChip(
                                    selected = historyFilterCategory == catKey,
                                    onClick = { historyFilterCategory = catKey },
                                    label = { Text(catName, fontSize = 12.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = SaffronPrimary,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }
                    }

                    if (filteredHistory.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 40.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        Icons.Default.NotificationsNone,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(56.dp)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "कोई संदेश इतिहास नहीं मिला",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "नया प्रसारण करने के लिए 'नया संदेश भेजें' टैब पर जाएं।",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    } else {
                        items(filteredHistory, key = { it.notificationId.ifEmpty { it.id.toString() } }) { notif ->
                            NotificationHistoryItem(
                                notification = notif,
                                isAdmin = isAdmin,
                                onDelete = { onDeleteNotification(notif.notificationId) }
                            )
                        }
                    }
                }
            }
        }
    }

    // ==========================================
    // MULTI-SELECT CADET PICKER SHEET
    // ==========================================
    if (showStudentPickerSheet) {
        ModalBottomSheet(
            onDismissRequest = { showStudentPickerSheet = false },
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "लक्षित कैडेट्स चुनें (${selectedStudentIds.size} चयनित)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(
                        onClick = {
                            selectedStudentIds = if (selectedStudentIds.size == allStudents.size) emptySet() else allStudents.map { it.studentId }.toSet()
                        }
                    ) {
                        Text(if (selectedStudentIds.size == allStudents.size) "सभी हटाएं" else "सभी चुनें")
                    }
                }

                OutlinedTextField(
                    value = studentSearchQuery,
                    onValueChange = { studentSearchQuery = it },
                    placeholder = { Text("कैडेट का नाम या चेस्ट नंबर...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) }
                )

                Spacer(modifier = Modifier.height(10.dp))

                val filteredCadets = allStudents.filter { s ->
                    if (studentSearchQuery.isBlank()) true else {
                        val q = studentSearchQuery.trim().lowercase()
                        s.fullName.lowercase().contains(q) || s.studentId.lowercase().contains(q) || s.village.lowercase().contains(q)
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 350.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(filteredCadets) { student ->
                        val isChecked = selectedStudentIds.contains(student.studentId)
                        Surface(
                            color = if (isChecked) SaffronPrimary.copy(alpha = 0.10f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedStudentIds = if (isChecked) {
                                        selectedStudentIds - student.studentId
                                    } else {
                                        selectedStudentIds + student.studentId
                                    }
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = isChecked,
                                    onCheckedChange = { checked ->
                                        selectedStudentIds = if (checked) {
                                            selectedStudentIds + student.studentId
                                        } else {
                                            selectedStudentIds - student.studentId
                                        }
                                    },
                                    colors = CheckboxDefaults.colors(checkedColor = SaffronPrimary)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${student.fullName} (${student.studentId})",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${student.village} • ${student.recruitmentGoal}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { showStudentPickerSheet = false },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary)
                ) {
                    Text("चयन की पुष्टि करें (${selectedStudentIds.size} कैडेट्स)", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun NotificationHistoryItem(
    notification: AppNotification,
    isAdmin: Boolean,
    onDelete: () -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val categoryColor = when (notification.category) {
        AppNotification.CATEGORY_ANNOUNCEMENT -> OliveTertiary
        AppNotification.CATEGORY_NOTICE -> SaffronPrimary
        AppNotification.CATEGORY_RECRUITMENT_ALERT -> SaffronSecondary
        AppNotification.CATEGORY_TRAINING_REMINDER -> ArmyGreen
        AppNotification.CATEGORY_STUDY_REMINDER -> DeepNavy
        else -> MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("notif_history_${notification.notificationId}"),
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
                    Surface(
                        color = categoryColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = notification.categoryLabelHindi,
                            style = MaterialTheme.typography.labelSmall,
                            color = categoryColor,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    if (notification.isUrgent) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.errorContainer,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "⚡ अति महत्वपूर्ण",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }
                }

                if (isAdmin) {
                    IconButton(
                        onClick = { showDeleteConfirm = true },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            Icons.Default.DeleteOutline,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = notification.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = notification.message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "प्रेषक: ${notification.senderName}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "लक्ष्य: ${notification.targetLabelHindi}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = notification.dateFormatted,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("संदेश इतिहास हटाएं?") },
            text = { Text("क्या आप इस अधिसूचना को डेटाबेस एवं क्लाउड इतिहास से हटाना चाहते हैं?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        onDelete()
                    }
                ) {
                    Text("हटाएं", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("रद्द करें")
                }
            }
        )
    }
}

private data class QuickTemplate(
    val label: String,
    val title: String,
    val message: String,
    val category: String,
    val route: String,
    val urgent: Boolean
)
