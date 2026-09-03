package com.example.ui.screens.notices

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.AppNotification
import com.example.data.model.Notice
import com.example.data.model.StudentProfile
import com.example.ui.theme.*

/**
 * Enhanced Student & Cadet Notice & Inbox Screen.
 * Provides unified, crystal-clear access to:
 * 1. 📋 आधिकारिक नोटिस बोर्ड (Official Academy Notices)
 * 2. 🔔 डायरेक्ट अलर्ट्स व संदेश (Targeted Cadet Broadcasts & Reminders)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentNotificationsScreen(
    studentProfile: StudentProfile?,
    allNotifications: List<AppNotification>,
    allNotices: List<Notice> = emptyList(),
    onMarkAsRead: (String) -> Unit = {},
    onMarkAllAsRead: () -> Unit = {},
    onNavigateToRoute: (String) -> Unit = {},
    onBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) } // 0: Official Notices, 1: Direct Alerts
    var selectedNoticeCategory by remember { mutableStateOf("ALL") }
    var selectedNotifCategory by remember { mutableStateOf("ALL") }
    var searchQuery by remember { mutableStateOf("") }
    var selectedNoticeForDetail by remember { mutableStateOf<Notice?>(null) }
    var selectedNotifForDetail by remember { mutableStateOf<AppNotification?>(null) }

    // Filter Direct Notifications
    val studentNotifications = remember(allNotifications, studentProfile, selectedNotifCategory, searchQuery) {
        val studentId = studentProfile?.studentId
        val batchName = studentProfile?.batchName

        allNotifications.filter { notif ->
            val isTargeted = notif.isRecipientStudent(studentId, batchName)
            val matchesCategory = if (selectedNotifCategory == "ALL") true else notif.category == selectedNotifCategory
            val matchesSearch = if (searchQuery.isBlank()) true else {
                val q = searchQuery.trim().lowercase()
                notif.title.lowercase().contains(q) || notif.message.lowercase().contains(q) || notif.senderName.lowercase().contains(q)
            }
            isTargeted && matchesCategory && matchesSearch
        }
    }

    // Filter Official Notices
    val filteredNotices = remember(allNotices, selectedNoticeCategory, searchQuery) {
        allNotices.filter { notice ->
            val matchesCategory = when (selectedNoticeCategory) {
                "ALL" -> true
                "जरूरी" -> notice.isUrgent || notice.priority.equals("URGENT", true)
                else -> notice.category.contains(selectedNoticeCategory, ignoreCase = true)
            }
            val matchesSearch = if (searchQuery.isBlank()) true else {
                val q = searchQuery.trim().lowercase()
                notice.title.lowercase().contains(q) ||
                notice.content.lowercase().contains(q) ||
                notice.author.lowercase().contains(q) ||
                notice.category.lowercase().contains(q)
            }
            matchesCategory && matchesSearch
        }.sortedWith(
            compareByDescending<Notice> { it.isPinned }
                .thenByDescending { it.isUrgent || it.priority.equals("URGENT", true) }
                .thenByDescending { it.id }
        )
    }

    val unreadCount = remember(studentNotifications) {
        studentNotifications.count { !it.isRead }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "सूचना एवं नोटिस बोर्ड",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("btn_back_student_notifs")) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (selectedTabIndex == 1 && unreadCount > 0) {
                        TextButton(onClick = onMarkAllAsRead, modifier = Modifier.testTag("btn_mark_all_read_student")) {
                            Text("सब पढ़ा", color = SaffronPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 1. Primary Tab Switcher (Notices vs Inbox)
            item {
                PrimaryTabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                ) {
                    Tab(
                        selected = selectedTabIndex == 0,
                        onClick = { selectedTabIndex = 0 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Campaign, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "नोटिस बोर्ड (${filteredNotices.size})",
                                    fontWeight = if (selectedTabIndex == 0) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 13.sp
                                )
                            }
                        },
                        selectedContentColor = SaffronPrimary,
                        unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Tab(
                        selected = selectedTabIndex == 1,
                        onClick = { selectedTabIndex = 1 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Notifications, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (unreadCount > 0) "इनबॉक्स ($unreadCount नया)" else "इनबॉक्स अलर्ट्स",
                                    fontWeight = if (selectedTabIndex == 1) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 13.sp
                                )
                            }
                        },
                        selectedContentColor = SaffronPrimary,
                        unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // 2. Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            text = if (selectedTabIndex == 0) "नोटिस शीर्षक या विवरण खोजें..." else "सूचनाओं में खोजें...",
                            fontSize = 13.sp
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_student_notif_search"),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = SaffronPrimary) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SaffronPrimary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    )
                )
            }

            // 3. Category Filter Chips (Dynamic according to selected tab)
            item {
                if (selectedTabIndex == 0) {
                    val noticeCategories = listOf(
                        "ALL" to "सभी सूचनाएं",
                        "प्रशिक्षण" to "🏃 प्रशिक्षण (Training)",
                        "परीक्षा" to "📖 परीक्षा (Exam)",
                        "भर्ती अपडेट" to "🎖️ भर्ती (Recruitment)",
                        "जरूरी" to "⚡ अति महत्वपूर्ण",
                        "उपलब्धि" to "🏆 अखाड़ा गौरव"
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(noticeCategories) { (key, label) ->
                            val isSelected = selectedNoticeCategory == key
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedNoticeCategory = key },
                                label = { Text(label, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SaffronPrimary,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                } else {
                    val notifCategories = listOf(
                        Pair("ALL", "सभी संदेश"),
                        Pair(AppNotification.CATEGORY_RECRUITMENT_ALERT, "🎖️ भर्ती अलर्ट"),
                        Pair(AppNotification.CATEGORY_TRAINING_REMINDER, "🏃 ग्राउंड ट्रेनिंग"),
                        Pair(AppNotification.CATEGORY_STUDY_REMINDER, "📖 स्टडी व टेस्ट"),
                        Pair(AppNotification.CATEGORY_ANNOUNCEMENT, "📢 घोषणाएं"),
                        Pair(AppNotification.CATEGORY_NOTICE, "📋 नोटिस")
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(notifCategories) { (catKey, catLabel) ->
                            val isSelected = selectedNotifCategory == catKey
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedNotifCategory = catKey },
                                label = { Text(catLabel, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SaffronPrimary,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }
            }

            // 4. Tab 0: OFFICIAL NOTICES LIST
            if (selectedTabIndex == 0) {
                if (filteredNotices.isEmpty()) {
                    item {
                        EmptyNoticeState(
                            title = "कोई आधिकारिक नोटिस उपलब्ध नहीं है",
                            subtitle = "नया नोटिस जारी होने पर यहाँ प्रदर्शित किया जाएगा।"
                        )
                    }
                } else {
                    items(filteredNotices, key = { "notice_${it.id}" }) { notice ->
                        OfficialNoticeCard(
                            notice = notice,
                            onClick = { selectedNoticeForDetail = notice },
                            onNavigateToRoute = onNavigateToRoute
                        )
                    }
                }
            } else {
                // 5. Tab 1: DIRECT ALERTS & INBOX NOTIFICATIONS LIST
                if (studentNotifications.isEmpty()) {
                    item {
                        EmptyNoticeState(
                            title = "कोई नई सूचना या अलर्ट नहीं है",
                            subtitle = "प्रशिक्षकों द्वारा नया प्रसारण होने पर यहाँ प्रदर्शित होगा।"
                        )
                    }
                } else {
                    items(studentNotifications, key = { "notif_${it.notificationId.ifEmpty { it.id.toString() }}" }) { notif ->
                        StudentNotificationCard(
                            notification = notif,
                            onCardClick = {
                                if (!notif.isRead) {
                                    onMarkAsRead(notif.notificationId)
                                }
                                selectedNotifForDetail = notif
                            },
                            onActionClick = {
                                if (!notif.isRead) {
                                    onMarkAsRead(notif.notificationId)
                                }
                                if (notif.actionRoute.isNotBlank() && notif.actionRoute != "dashboard") {
                                    onNavigateToRoute(notif.actionRoute)
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    // Detail Dialogs
    selectedNoticeForDetail?.let { notice ->
        NoticeFullDialog(
            notice = notice,
            onDismiss = { selectedNoticeForDetail = null },
            onNavigateToRoute = { route ->
                selectedNoticeForDetail = null
                onNavigateToRoute(route)
            }
        )
    }

    selectedNotifForDetail?.let { notif ->
        NotificationFullDialog(
            notification = notif,
            onDismiss = { selectedNotifForDetail = null },
            onNavigateToRoute = { route ->
                selectedNotifForDetail = null
                onNavigateToRoute(route)
            }
        )
    }
}

/**
 * Rich Card for Official Academy Notices (1600m Trials, Recruitment, Camps, etc.)
 */
@Composable
fun OfficialNoticeCard(
    notice: Notice,
    onClick: () -> Unit,
    onNavigateToRoute: (String) -> Unit
) {
    val isUrgent = notice.isUrgent || notice.priority.equals("URGENT", true)

    val categoryColor = when {
        isUrgent -> StatusAbsent
        notice.category.contains("प्रशिक्षण") -> OliveTertiary
        notice.category.contains("परीक्षा") -> DeepNavy
        notice.category.contains("भर्ती") -> SaffronSecondary
        notice.category.contains("उपलब्धि") -> GoldAccent
        else -> SaffronPrimary
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("official_notice_card_${notice.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (notice.isPinned) SaffronContainer.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            width = if (notice.isPinned || isUrgent) 1.5.dp else 1.dp,
            color = when {
                notice.isPinned -> SaffronPrimary
                isUrgent -> StatusAbsent
                else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (notice.isPinned) 3.dp else 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Badges and Date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (notice.isPinned) {
                        Surface(
                            color = SaffronDark,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            ) {
                                Icon(Icons.Default.PushPin, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text("पिन नोटिस", style = MaterialTheme.typography.labelSmall, color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Surface(
                        color = categoryColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = if (isUrgent) "⚡ अति महत्वपूर्ण (Urgent)" else notice.category,
                            style = MaterialTheme.typography.labelSmall,
                            color = categoryColor,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Text(
                    text = notice.date,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Notice Title
            Text(
                text = notice.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Notice Content Body
            Text(
                text = notice.content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 20.sp,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            Spacer(modifier = Modifier.height(8.dp))

            // Footer: Author & Read Full Action
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "जारीकर्ता: ${notice.author}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onClick() }
                ) {
                    Text(
                        text = "विस्तार से पढ़ें",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = SaffronDark
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        Icons.Default.ArrowForward,
                        contentDescription = null,
                        tint = SaffronDark,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

/**
 * Card for Targeted Broadcast Messages & Alerts
 */
@Composable
fun StudentNotificationCard(
    notification: AppNotification,
    onCardClick: () -> Unit,
    onActionClick: () -> Unit
) {
    val categoryColor = when (notification.category) {
        AppNotification.CATEGORY_ANNOUNCEMENT -> OliveTertiary
        AppNotification.CATEGORY_NOTICE -> SaffronPrimary
        AppNotification.CATEGORY_RECRUITMENT_ALERT -> SaffronSecondary
        AppNotification.CATEGORY_TRAINING_REMINDER -> ArmyGreen
        AppNotification.CATEGORY_STUDY_REMINDER -> DeepNavy
        else -> MaterialTheme.colorScheme.primary
    }

    val actionButtonText = when (notification.actionRoute) {
        "ground_training", "training" -> "🏃 ग्राउंड टाइमर खोलें"
        "mock_tests", "mocktest" -> "📖 मॉक टेस्ट दें"
        "study_material", "study" -> "📚 नोट्स व विषय देखें"
        "recruitment" -> "🎖️ भर्ती जानकारी देखें"
        "admin_notices", "notices" -> "📋 आधिकारिक नोटिस देखें"
        else -> null
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCardClick() }
            .testTag("card_student_notif_${notification.notificationId}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (!notification.isRead) categoryColor.copy(alpha = 0.06f) else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            width = if (!notification.isRead) 1.5.dp else 1.dp,
            color = if (!notification.isRead) categoryColor.copy(alpha = 0.6f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (!notification.isRead) 2.dp else 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Category & Status Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
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

                if (!notification.isRead) {
                    Surface(
                        color = SaffronPrimary,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "नया (New)",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Notification Title
            Text(
                text = notification.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = if (!notification.isRead) FontWeight.ExtraBold else FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Message Body
            Text(
                text = notification.message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 20.sp
            )

            // Action Button (Deep-link)
            if (actionButtonText != null) {
                Spacer(modifier = Modifier.height(12.dp))
                FilledTonalButton(
                    onClick = onActionClick,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = categoryColor.copy(alpha = 0.15f),
                        contentColor = categoryColor
                    )
                ) {
                    Text(actionButtonText, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(Icons.Default.ChevronRight, contentDescription = null, modifier = Modifier.size(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            Spacer(modifier = Modifier.height(8.dp))

            // Footer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "जारीकर्ता: ${notification.senderName}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = notification.dateFormatted,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Full Screen / Dialog view for reading a complete Official Notice cleanly
 */
@Composable
fun NoticeFullDialog(
    notice: Notice,
    onDismiss: () -> Unit,
    onNavigateToRoute: (String) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Top Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = SaffronContainer,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = notice.category,
                            style = MaterialTheme.typography.labelMedium,
                            color = OnSaffronContainer,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = notice.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 26.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "📅 दिनांक: ${notice.date}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "👤 ${notice.author}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))
                Divider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(14.dp))

                // Full Content
                Text(
                    text = notice.content,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 24.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (notice.category.contains("भर्ती")) {
                        Button(
                            onClick = { onNavigateToRoute("recruitment") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary)
                        ) {
                            Text("भर्ती विवरण देखें", fontWeight = FontWeight.Bold)
                        }
                    } else if (notice.category.contains("प्रशिक्षण")) {
                        Button(
                            onClick = { onNavigateToRoute("training") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = OliveTertiary)
                        ) {
                            Text("ग्राउंड ट्रेनिंग देखें", fontWeight = FontWeight.Bold)
                        }
                    }

                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("बंद करें")
                    }
                }
            }
        }
    }
}

/**
 * Full Dialog for targeted app notification
 */
@Composable
fun NotificationFullDialog(
    notification: AppNotification,
    onDismiss: () -> Unit,
    onNavigateToRoute: (String) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = SaffronContainer,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = notification.categoryLabelHindi,
                            style = MaterialTheme.typography.labelMedium,
                            color = OnSaffronContainer,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = notification.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 26.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "${notification.dateFormatted} • प्रेषक: ${notification.senderName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(14.dp))
                Divider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = notification.message,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 24.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                if (notification.actionRoute.isNotBlank() && notification.actionRoute != "dashboard") {
                    Button(
                        onClick = { onNavigateToRoute(notification.actionRoute) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary)
                    ) {
                        Text("संबंधित स्क्रीन खोलें", fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("बंद करें")
                }
            }
        }
    }
}

@Composable
fun EmptyNoticeState(
    title: String,
    subtitle: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.NotificationsNone,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(54.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
