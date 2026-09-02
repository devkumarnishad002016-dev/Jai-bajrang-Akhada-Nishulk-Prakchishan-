package com.example.ui.screens.leaderboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.StudentProfile
import com.example.ui.theme.*

@Composable
fun LeaderboardScreen(
    allStudents: List<StudentProfile>,
    currentStudentId: String,
    onSelectStudent: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedCategory by remember { mutableStateOf("Overall") } // "Overall", "1600m Running", "Pushups", "Score"

    val sortedStudents: List<StudentProfile> = remember(allStudents, selectedCategory) {
        when (selectedCategory) {
            "1600m Running" -> allStudents.sortedBy { it.time1600m }
            "Pushups" -> allStudents.sortedByDescending { it.pushups }
            "Score" -> allStudents.sortedByDescending { it.studyTargetPercentage }
            else -> allStudents.sortedByDescending { it.studyTargetPercentage + it.pushups }
        }
    }

    val top3 = sortedStudents.take(3)
    val remaining = sortedStudents.drop(3)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("leaderboard_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
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
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = "Trophy",
                            tint = GoldAccent,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "अखाड़ा लीडरबोर्ड (Leaderboard)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "गाँव से सेना–पुलिस भर्ती अभियान • शीर्ष युवा प्रतिभा",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Category Filter Chips
        item {
            val categories = listOf(
                "Overall" to "समग्र रैंक (Overall)",
                "1600m Running" to "1600m रनिंग",
                "Pushups" to "पुश-अप्स & बीम",
                "Score" to "स्टडी & टेस्ट स्कोर"
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(categories.size) { idx ->
                    val (key, label) = categories[idx]
                    val isSelected = selectedCategory == key
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategory = key },
                        label = { Text(label, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SaffronContainer,
                            selectedLabelColor = OnSaffronContainer
                        )
                    )
                }
            }
        }

        // Top 3 Podium
        if (top3.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "शीर्ष 3 विजेता (Top 3 Performers)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            // 2nd Place
                            if (top3.size > 1) {
                                PodiumColumn(
                                    student = top3[1],
                                    rank = 2,
                                    badgeColor = SilverAccent,
                                    height = 100.dp,
                                    onSelect = onSelectStudent
                                )
                            }

                            // 1st Place (Center & Highest)
                            if (top3.isNotEmpty()) {
                                PodiumColumn(
                                    student = top3[0],
                                    rank = 1,
                                    badgeColor = GoldAccent,
                                    height = 130.dp,
                                    onSelect = onSelectStudent
                                )
                            }

                            // 3rd Place
                            if (top3.size > 2) {
                                PodiumColumn(
                                    student = top3[2],
                                    rank = 3,
                                    badgeColor = BronzeAccent,
                                    height = 80.dp,
                                    onSelect = onSelectStudent
                                )
                            }
                        }
                    }
                }
            }
        }

        // Remaining Students List
        item {
            Text(
                text = "सभी छात्र रैंकिंग (Full Rankings)",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
        }

        itemsIndexed(sortedStudents) { index, student ->
            val rank = index + 1
            val isCurrent = student.studentId == currentStudentId

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectStudent(student.studentId) },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isCurrent) SaffronContainer else MaterialTheme.colorScheme.surface
                ),
                border = CardDefaults.outlinedCardBorder().let {
                    if (isCurrent) androidx.compose.foundation.BorderStroke(1.5.dp, SaffronPrimary)
                    else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        // Rank Circle
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(
                                    when (rank) {
                                        1 -> GoldAccent.copy(alpha = 0.2f)
                                        2 -> SilverAccent.copy(alpha = 0.2f)
                                        3 -> BronzeAccent.copy(alpha = 0.2f)
                                        else -> MaterialTheme.colorScheme.surfaceVariant
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "#$rank",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = when (rank) {
                                    1 -> GoldAccent
                                    2 -> SilverAccent
                                    3 -> BronzeAccent
                                    else -> MaterialTheme.colorScheme.onSurface
                                }
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = student.fullName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isCurrent) OnSaffronContainer else MaterialTheme.colorScheme.onSurface
                                )
                                if (isCurrent) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(color = SaffronPrimary, shape = RoundedCornerShape(4.dp)) {
                                        Text("YOU", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = Color.White, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                                    }
                                }
                            }
                            Text(
                                text = "गाँव: ${student.village} • लक्ष्य: ${student.recruitmentGoal}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Key Stat
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "1600m: ${student.time1600m}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = SaffronPrimary
                        )
                        Text(
                            text = "पुश-अप्स: ${student.pushups} | बीम: ${student.pullups}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PodiumColumn(
    student: StudentProfile,
    rank: Int,
    badgeColor: Color,
    height: androidx.compose.ui.unit.Dp,
    onSelect: (String) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(90.dp)
            .clickable { onSelect(student.studentId) }
    ) {
        // Avatar + Crown
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(badgeColor.copy(alpha = 0.2f))
                .border(2.dp, badgeColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = badgeColor,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = student.fullName.split(" ").firstOrNull() ?: student.fullName,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            textAlign = TextAlign.Center
        )
        Text(
            text = student.time1600m,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = SaffronPrimary,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Pedestal Box
        Box(
            modifier = Modifier
                .width(80.dp)
                .height(height)
                .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(badgeColor.copy(alpha = 0.4f), badgeColor.copy(alpha = 0.15f))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$rank",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = badgeColor
            )
        }
    }
}
