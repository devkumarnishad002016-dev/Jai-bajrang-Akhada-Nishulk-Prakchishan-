package com.example.ui.screens.notices

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

/**
 * Phase 5B — Student & Cadet Inbox Screen.
 * Displays targeted communications, announcements, recruitment alerts,
 * training reminders, and study reminders with read/unread tracking and deep links.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentNotificationsScreen(
    studentProfile: StudentProfile?,
    allNotifications: List<AppNotification>,
    onMarkAsRead: (String) -> Unit = {},
    onMarkAllAsRead: () -> Unit = {},
    onNavigateToRoute: (String) -> Unit = {},
    onBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedCategoryFilter by remember { mutableStateOf("ALL") }
    var searchQuery by remember { mutableStateOf("") }

    // Filter notifications relevant to current student
    val studentNotifications = remember(allNotifications, studentProfile, selectedCategoryFilter, searchQuery) {
        val studentId = studentProfile?.studentId
        val batchName = studentProfile?.batchName

        allNotifications.filter { notif ->
            val isTargeted = notif.isRecipientStudent(studentId, batchName)
            val matchesCategory = if (selectedCategoryFilter == "ALL") true else notif.category == selectedCategoryFilter
            val matchesSearch = if (searchQuery.isBlank()) true else {
                val q = searchQuery.trim().lowercase()
                notif.title.lowercase().contains(q) || notif.message.lowercase().contains(q) || notif.senderName.lowercase().contains(q)
            }
            isTargeted && matchesCategory && matchesSearch
        }
    }

    val unreadCount = remember(studentNotifications) {
        studentNotifications.count { !it.isRead }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "सूचना एवं संदेश इनबॉक्स (Inbox)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (unreadCount > 0) "$unreadCount नए अपठित संदेश" else "सभी सूचनाएं अद्यतित हैं",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (unreadCount > 0) SaffronPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("btn_back_student_notifs")) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (unreadCount > 0) {
                        TextButton(onClick = onMarkAllAsRead, modifier = Modifier.testTag("btn_mark_all_read_student")) {
                            Text("सब पढ़ा (Read All)", color = SaffronPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Search Bar & Filter Chips
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("सूचनाओं में खोजें...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_student_notif_search"),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    }
                )

                val filterOptions = listOf(
                    Pair("ALL", "सभी संदेश"),
                    Pair(AppNotification.CATEGORY_ANNOUNCEMENT, "📢 घोषणाएं"),
                    Pair(AppNotification.CATEGORY_NOTICE, "📋 नोटिस"),
                    Pair(AppNotification.CATEGORY_RECRUITMENT_ALERT, "🎖️ भर्ती अलर्ट"),
                    Pair(AppNotification.CATEGORY_TRAINING_REMINDER, "🏃 ट्रेनिंग"),
                    Pair(AppNotification.CATEGORY_STUDY_REMINDER, "📖 स्टडी व टेस्ट")
                )

                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(filterOptions) { (catKey, catLabel) ->
                        FilterChip(
                            selected = selectedCategoryFilter == catKey,
                            onClick = { selectedCategoryFilter = catKey },
                            label = { Text(catLabel, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SaffronPrimary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }

            if (studentNotifications.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.NotificationsNone,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "कोई नई सूचना उपलब्ध नहीं है",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "अखाड़ा प्रशिक्षकों द्वारा नया प्रसारण होने पर यहाँ प्रदर्शित होगा।",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 24.dp, top = 4.dp)
                ) {
                    items(studentNotifications, key = { it.notificationId.ifEmpty { it.id.toString() } }) { notif ->
                        StudentNotificationCard(
                            notification = notif,
                            onCardClick = {
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
}

@Composable
fun StudentNotificationCard(
    notification: AppNotification,
    onCardClick: () -> Unit
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
        "ground_training" -> "🏃 ग्राउंड टाइमर खोलें"
        "mock_tests" -> "📖 मॉक टेस्ट दें"
        "study_material" -> "📚 नोट्स देखें"
        "recruitment" -> "🎖️ भर्ती जानकारी देखें"
        "admin_notices" -> "📋 आधिकारिक नोटिस देखें"
        else -> null
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCardClick() }
            .testTag("card_student_notif_${notification.notificationId}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (!notification.isRead) categoryColor.copy(alpha = 0.05f) else MaterialTheme.colorScheme.surface
        ),
        border = if (!notification.isRead) androidx.compose.foundation.BorderStroke(1.5.dp, categoryColor.copy(alpha = 0.5f)) else null,
        elevation = CardDefaults.cardElevation(defaultElevation = if (!notification.isRead) 3.dp else 1.dp)
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

                if (!notification.isRead) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(SaffronPrimary)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = notification.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = if (!notification.isRead) FontWeight.Bold else FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = notification.message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (actionButtonText != null) {
                Spacer(modifier = Modifier.height(10.dp))
                FilledTonalButton(
                    onClick = onCardClick,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = categoryColor.copy(alpha = 0.15f),
                        contentColor = categoryColor
                    )
                ) {
                    Text(actionButtonText, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(Icons.Default.ChevronRight, contentDescription = null, modifier = Modifier.size(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(6.dp))

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
