package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.StudentProfile
import com.example.ui.theme.*
import com.example.util.RolePermissionManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopHeader(
    title: String = "जय बजरंग अखाड़ा",
    subtitle: String = "स्वस्थ युवा – सशक्त राष्ट्र",
    currentRole: String,
    activeStudent: StudentProfile?,
    allStudents: List<StudentProfile>,
    onRoleToggle: (String) -> Unit,
    onStudentSelect: (String) -> Unit,
    onLogout: (() -> Unit)? = null,
    unreadNotificationsCount: Int = 0,
    onNotificationsClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val isAdmin = RolePermissionManager.isAdmin(currentRole)
    val isTrainer = RolePermissionManager.isTrainer(currentRole)
    val isStudent = RolePermissionManager.isStudent(currentRole)
    var showStudentPicker by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: App Logo & Name
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .weight(1f, fill = false)
                    .padding(end = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(SaffronPrimary, SaffronDark)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = "Mission Emblem",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "मौरीकला (गुफा)",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = SaffronPrimary,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1
                    )
                }
            }

            // Right: Role Badge & Action Icons in single compact row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                // Role Toggle Pill
                Surface(
                    onClick = {
                        val nextRole = if (isStudent) "TRAINER" else "STUDENT"
                        onRoleToggle(nextRole)
                    },
                    shape = RoundedCornerShape(8.dp),
                    color = if (isStudent) SaffronContainer.copy(alpha = 0.6f) else OliveContainer.copy(alpha = 0.6f),
                    border = BorderStroke(1.dp, if (isStudent) SaffronPrimary.copy(alpha = 0.4f) else OliveTertiary.copy(alpha = 0.4f)),
                    modifier = Modifier.testTag("role_switcher_chip")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = when {
                                isAdmin -> Icons.Default.AdminPanelSettings
                                isTrainer -> Icons.Default.Sports
                                else -> Icons.Default.Person
                            },
                            contentDescription = "Role Icon",
                            modifier = Modifier.size(13.dp),
                            tint = if (isStudent) SaffronDark else OliveTertiary
                        )
                        Text(
                            text = when {
                                isAdmin -> "Admin"
                                isTrainer -> "कोच"
                                else -> "छात्र"
                            },
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            fontWeight = FontWeight.Bold,
                            color = if (isStudent) SaffronDark else OliveTertiary
                        )
                    }
                }

                // Student switch icon (only in student mode)
                if (isStudent) {
                    IconButton(
                        onClick = { showStudentPicker = !showStudentPicker },
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
                            .testTag("switch_student_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwitchAccount,
                            contentDescription = "Switch Student",
                            modifier = Modifier.size(15.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Notification Hub Icon with unread badge
                if (onNotificationsClick != null) {
                    BadgedBox(
                        badge = {
                            if (unreadNotificationsCount > 0) {
                                Badge(
                                    containerColor = SaffronPrimary,
                                    contentColor = Color.White,
                                    modifier = Modifier.size(14.dp)
                                ) {
                                    Text(
                                        text = if (unreadNotificationsCount > 9) "9+" else "$unreadNotificationsCount",
                                        fontSize = 9.sp
                                    )
                                }
                            }
                        }
                    ) {
                        IconButton(
                            onClick = onNotificationsClick,
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
                                .testTag("btn_top_notifications")
                        ) {
                            Icon(
                                imageVector = if (unreadNotificationsCount > 0) Icons.Default.NotificationsActive else Icons.Outlined.Notifications,
                                contentDescription = "Notifications",
                                modifier = Modifier.size(15.dp),
                                tint = if (unreadNotificationsCount > 0) SaffronPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Logout / Switch Account button
                if (onLogout != null) {
                    IconButton(
                        onClick = onLogout,
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
                            .testTag("logout_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Logout,
                            contentDescription = "लॉगआउट / स्विच",
                            modifier = Modifier.size(15.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }

    // Student Switcher Dropdown Dialog
    if (showStudentPicker) {
        AlertDialog(
            onDismissRequest = { showStudentPicker = false },
            title = {
                Text(
                    text = "छात्र प्रोफाइल चुनें (Select Student)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    allStudents.forEach { student ->
                        val isSelected = student.studentId == activeStudent?.studentId
                        Card(
                            onClick = {
                                onStudentSelect(student.studentId)
                                showStudentPicker = false
                            },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) SaffronContainer else MaterialTheme.colorScheme.surfaceVariant
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
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
                                        text = student.fullName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) OnSaffronContainer else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${student.studentId} • ${student.village} • लक्ष्य: ${student.recruitmentGoal}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (isSelected) SaffronDark else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Selected",
                                        tint = SaffronPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showStudentPicker = false }) {
                    Text("बंद करें (Close)")
                }
            }
        )
    }
}

@Composable
fun AppBottomNavigationBar(
    currentScreen: String,
    currentRole: String,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val isAdmin = RolePermissionManager.isAdmin(currentRole)
    val isTrainer = RolePermissionManager.isTrainer(currentRole)

    NavigationBar(
        modifier = modifier
            .windowInsetsPadding(WindowInsets.navigationBars)
            .testTag("main_bottom_nav"),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp
    ) {
        if (isTrainer) {
            // Coach / Trainer bottom items
            val trainerItems = listOf(
                Triple("coach_dashboard", "कोच डैशबोर्ड", Icons.Default.Dashboard),
                Triple("admin_attendance", "हाजिरी", Icons.Default.FactCheck),
                Triple("admin_workout", "वर्कआउट", Icons.Default.SportsScore),
                Triple("training", "ग्राउंड ड्रिल", Icons.Default.FitnessCenter),
                Triple("leaderboard", "रैंकिंग", Icons.Default.EmojiEvents)
            )

            trainerItems.forEach { (route, label, icon) ->
                val isSelected = currentScreen == route
                NavigationBarItem(
                    selected = isSelected,
                    onClick = { onNavigate(route) },
                    icon = {
                        Icon(
                            imageVector = icon,
                            contentDescription = label,
                            tint = if (isSelected) SaffronPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    label = {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) SaffronPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = SaffronContainer
                    ),
                    modifier = Modifier.testTag("trainer_nav_tab_$route")
                )
            }
        } else if (isAdmin) {
            // Admin bottom items
            val adminItems = listOf(
                Triple("admin_dashboard", "डैशबोर्ड", Icons.Default.Dashboard),
                Triple("admin_attendance", "उपस्थिति", Icons.Default.FactCheck),
                Triple("admin_workout", "ट्रेनिंग प्लान", Icons.Default.SportsScore),
                Triple("admin_study", "प्रश्न व स्टडी", Icons.Default.MenuBook),
                Triple("admin_notices", "नोटिस व भर्ती", Icons.Default.Campaign)
            )

            adminItems.forEach { (route, label, icon) ->
                val isSelected = currentScreen == route
                NavigationBarItem(
                    selected = isSelected,
                    onClick = { onNavigate(route) },
                    icon = {
                        Icon(
                            imageVector = icon,
                            contentDescription = label,
                            tint = if (isSelected) OliveTertiary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    label = {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) OliveTertiary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = OliveContainer
                    ),
                    modifier = Modifier.testTag("admin_nav_tab_$route")
                )
            }
        } else {
            // Student bottom items
            val items = listOf(
                Triple("dashboard", "होम", Icons.Default.Home),
                Triple("workout", "वर्कआउट", Icons.Default.FitnessCenter),
                Triple("study", "अध्ययन", Icons.Default.MenuBook),
                Triple("mocktest", "मॉक टेस्ट", Icons.Default.Quiz),
                Triple("progress", "प्रगति", Icons.Default.TrendingUp),
                Triple("profile", "प्रोफाइल", Icons.Default.Person)
            )

            items.forEach { (route, label, icon) ->
                val isSelected = currentScreen == route
                NavigationBarItem(
                    selected = isSelected,
                    onClick = { onNavigate(route) },
                    icon = {
                        Icon(
                            imageVector = icon,
                            contentDescription = label,
                            tint = if (isSelected) SaffronPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    label = {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) SaffronPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = SaffronContainer
                    ),
                    modifier = Modifier.testTag("nav_tab_$route")
                )
            }
        }
    }
}

@Composable
fun MetricStatCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    iconColor: Color,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    progress: Float? = null,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(iconColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = iconColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (progress != null) {
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { progress.coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = iconColor,
                    trackColor = iconColor.copy(alpha = 0.2f),
                )
            }
        }
    }
}

@Composable
fun StatusBadge(
    status: String,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor) = when (status) {
        "Present", "उपस्थित" -> StatusPresent.copy(alpha = 0.15f) to StatusPresent
        "Absent", "अनुपस्थित" -> StatusAbsent.copy(alpha = 0.15f) to StatusAbsent
        "Late", "विलंब" -> StatusLate.copy(alpha = 0.15f) to StatusLate
        "Leave", "अवकाश" -> StatusLeave.copy(alpha = 0.15f) to StatusLeave
        else -> SaffronPrimary.copy(alpha = 0.15f) to SaffronPrimary
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 3.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = status,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}
