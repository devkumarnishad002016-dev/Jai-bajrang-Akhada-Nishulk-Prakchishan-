package com.example.ui.screens.mission

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import com.example.data.mission.CadetAttentionIssueType
import com.example.data.mission.CadetAttentionItem
import com.example.data.mission.TrainerAttentionEngine
import com.example.data.model.AttendanceRecord
import com.example.data.model.StudentProfile
import com.example.data.model.TestAttempt
import com.example.data.model.TrainingRecord
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrainerAlertsScreen(
    allStudents: List<StudentProfile>,
    allAttendance: List<AttendanceRecord>,
    allTraining: List<TrainingRecord>,
    allTestAttempts: List<TestAttempt>,
    onSendAlertToStudent: (StudentProfile, String) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val todayDateStr = remember {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    val attentionList = remember(allStudents, allAttendance, allTraining, allTestAttempts, todayDateStr) {
        TrainerAttentionEngine.generateAttentionList(
            allStudents = allStudents,
            allAttendance = allAttendance,
            allTraining = allTraining,
            allTestAttempts = allTestAttempts,
            todayDateStr = todayDateStr
        )
    }

    var selectedFilter by remember { mutableStateOf<CadetAttentionIssueType?>(null) }
    var alertSentStudentId by remember { mutableStateOf<String?>(null) }

    val filteredList = remember(attentionList, selectedFilter) {
        if (selectedFilter == null) attentionList else attentionList.filter { it.issueType == selectedFilter }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "ट्रेनर अटेंशन हब",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "ध्यान देने योग्य कैडेट्स • अनुपस्थिति, कमजोर फिजिकल व स्कोर",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("trainer_alerts_back_button")
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
            if (alertSentStudentId != null) {
                Snackbar(
                    modifier = Modifier.padding(16.dp),
                    action = {
                        TextButton(onClick = { alertSentStudentId = null }) {
                            Text("ठीक है", color = Color.White)
                        }
                    }
                ) {
                    Text("कैडेट को व्यक्तिगत अलर्ट व मार्गदर्शन भेजा गया!")
                }
            }
        },
        modifier = modifier.testTag("trainer_alerts_screen")
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Attention Summary Banner
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = NavySecondary),
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
                                text = "कुल ${attentionList.size} कैडेट्स पर विशेष ध्यान आवश्यक",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "आज अनुपस्थित: ${attentionList.count { it.issueType == CadetAttentionIssueType.ABSENT_TODAY }} • कमजोर फिजिकल: ${attentionList.count { it.issueType == CadetAttentionIssueType.WEAK_PHYSICAL }}",
                                style = MaterialTheme.typography.bodySmall,
                                color = SaffronLight
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.NotificationImportant,
                            contentDescription = null,
                            tint = SaffronPrimary,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }

            // Filter Chips
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        FilterChip(
                            selected = selectedFilter == null,
                            onClick = { selectedFilter = null },
                            label = { Text("सभी (${attentionList.size})") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SaffronPrimary,
                                selectedLabelColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }

                    items(CadetAttentionIssueType.values()) { type ->
                        val count = attentionList.count { it.issueType == type }
                        if (count > 0) {
                            FilterChip(
                                selected = selectedFilter == type,
                                onClick = { selectedFilter = type },
                                label = { Text("${type.titleHindi} ($count)") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(type.badgeColorHex),
                                    selectedLabelColor = Color.White
                                ),
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                    }
                }
            }

            // Empty State
            if (filteredList.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = OliveTertiary,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "सभी कैडेट्स सही प्रगति पर हैं!",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = OliveTertiary
                            )
                        }
                    }
                }
            }

            // Attention Items List
            items(filteredList) { item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("attention_card_${item.student.studentId}"),
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
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(Color(item.issueType.badgeColorHex).copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = item.student.fullName.take(1),
                                        fontWeight = FontWeight.Bold,
                                        color = Color(item.issueType.badgeColorHex),
                                        fontSize = 16.sp
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = item.student.fullName,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "ग्राम: ${item.student.village} • लक्ष्य: ${item.student.recruitmentGoal}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Surface(
                                color = Color(item.issueType.badgeColorHex),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = item.issueType.titleHindi,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = item.issueDescriptionHindi,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "हाइलाइट: ${item.metricHighlight}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "कार्रवाई: ${item.suggestedCoachActionHindi}",
                                style = MaterialTheme.typography.labelSmall,
                                color = SaffronDark,
                                modifier = Modifier.weight(1f)
                            )

                            Button(
                                onClick = {
                                    onSendAlertToStudent(item.student, item.suggestedCoachActionHindi)
                                    alertSentStudentId = item.student.studentId
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(item.issueType.badgeColorHex)
                                ),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("अलर्ट भेजें", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
