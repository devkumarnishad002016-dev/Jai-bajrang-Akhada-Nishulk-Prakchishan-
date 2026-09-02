package com.example.ui.screens.study

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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.StudySubject
import com.example.data.model.StudyTopic
import com.example.ui.theme.*
import com.example.ui.viewmodel.StudySubjectViewModel

/**
 * StudyDashboard composable that retrieves and displays study subjects from the Room database
 * via the StudySubjectViewModel, allowing users to select a subject to explore its topics.
 */
@Composable
fun StudyDashboard(
    modifier: Modifier = Modifier,
    viewModel: StudySubjectViewModel = viewModel(),
    onTopicSelected: (StudySubject, StudyTopic) -> Unit = { _, _ -> },
    onPracticeSubject: (StudySubject) -> Unit = {}
) {
    val subjects by viewModel.allSubjects.collectAsState()
    val allTopics by viewModel.allTopics.collectAsState()
    val selectedSubjectId by viewModel.selectedSubjectId.collectAsState()

    StudyDashboardContent(
        subjects = subjects,
        allTopics = allTopics,
        selectedSubjectId = selectedSubjectId,
        onSubjectSelect = { subject ->
            viewModel.selectSubject(if (selectedSubjectId == subject.subjectId) null else subject.subjectId)
        },
        onTopicClick = { subject, topic ->
            viewModel.selectTopic(topic.topicId)
            onTopicSelected(subject, topic)
        },
        onPracticeSubject = onPracticeSubject,
        modifier = modifier
    )
}

/**
 * Stateless presentation composable for the Study Dashboard.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyDashboardContent(
    subjects: List<StudySubject>,
    allTopics: List<StudyTopic>,
    selectedSubjectId: String?,
    onSubjectSelect: (StudySubject) -> Unit,
    onTopicClick: (StudySubject, StudyTopic) -> Unit,
    onPracticeSubject: (StudySubject) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredSubjects = remember(subjects, searchQuery) {
        if (searchQuery.isBlank()) {
            subjects.filter { it.isActive }
        } else {
            subjects.filter {
                it.isActive && (it.name.contains(searchQuery, ignoreCase = true) ||
                        it.subjectId.contains(searchQuery, ignoreCase = true))
            }
        }
    }

    val selectedSubject = remember(subjects, selectedSubjectId) {
        subjects.find { it.subjectId == selectedSubjectId }
    }

    val topicsForSelected = remember(allTopics, selectedSubjectId) {
        if (selectedSubjectId == null) emptyList()
        else allTopics.filter { it.subjectId == selectedSubjectId && it.isActive }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("study_dashboard_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Dashboard Header Banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("study_dashboard_header"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "📖 परीक्षा अध्ययन डैशबोर्ड",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = SaffronPrimary
                            )
                            Text(
                                text = "विषय चुनें और संबंधित सभी टॉपिक्स का अध्ययन करें",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Surface(
                            shape = CircleShape,
                            color = SaffronPrimary.copy(alpha = 0.12f),
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.MenuBook,
                                    contentDescription = null,
                                    tint = SaffronPrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Quick Search Bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("study_dashboard_search_input"),
                        placeholder = { Text("विषय खोजें (उदा. गणित, तर्कशक्ति, हिंदी)...", fontSize = 13.sp) },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = "Search", tint = SaffronPrimary)
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SaffronPrimary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Quick Summary Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AssistChip(
                            onClick = {},
                            label = { Text("कुल विषय: ${subjects.count { it.isActive }}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                            leadingIcon = {
                                Icon(Icons.Default.AutoStories, contentDescription = null, modifier = Modifier.size(16.dp), tint = NavySecondary)
                            }
                        )
                        AssistChip(
                            onClick = {},
                            label = { Text("कुल टॉपिक्स: ${allTopics.count { it.isActive }}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                            leadingIcon = {
                                Icon(Icons.Default.ListAlt, contentDescription = null, modifier = Modifier.size(16.dp), tint = OliveTertiary)
                            }
                        )
                    }
                }
            }
        }

        // 2. Horizontal Subject Selector Pills for fast switching
        if (subjects.isNotEmpty()) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "त्वरित विषय चयन (Quick Select):",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(vertical = 4.dp),
                        modifier = Modifier.fillMaxWidth().testTag("study_quick_subject_row")
                    ) {
                        items(subjects.filter { it.isActive }) { subject ->
                            val isSelected = subject.subjectId == selectedSubjectId
                            FilterChip(
                                selected = isSelected,
                                onClick = { onSubjectSelect(subject) },
                                label = {
                                    Text(
                                        text = "${subject.icon} ${subject.name}",
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 13.sp
                                    )
                                },
                                leadingIcon = if (isSelected) {
                                    {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                            tint = SaffronPrimary
                                        )
                                    }
                                } else null,
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SaffronPrimary.copy(alpha = 0.15f),
                                    selectedLabelColor = SaffronPrimary
                                ),
                                modifier = Modifier.testTag("filter_chip_${subject.subjectId}")
                            )
                        }
                    }
                }
            }
        }

        // 3. Subjects & Topic Exploration List
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "उपलब्ध विषय सूची (Study Subjects)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${filteredSubjects.size} विषय",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (filteredSubjects.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "🔍", fontSize = 36.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "कोई विषय नहीं मिला",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "खोज शब्द बदलें या नया विषय जोड़ें।",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(filteredSubjects, key = { it.subjectId }) { subject ->
                val isSelected = selectedSubjectId == subject.subjectId
                val subjectTopics = allTopics.filter { it.subjectId == subject.subjectId && it.isActive }

                StudySubjectCard(
                    subject = subject,
                    topics = subjectTopics,
                    isSelected = isSelected,
                    onToggleSelect = { onSubjectSelect(subject) },
                    onTopicClick = { topic -> onTopicClick(subject, topic) },
                    onPracticeSubject = { onPracticeSubject(subject) }
                )
            }
        }
    }
}

/**
 * Individual Subject Card displaying topic count, icon, and expandable list of topics.
 */
@Composable
fun StudySubjectCard(
    subject: StudySubject,
    topics: List<StudyTopic>,
    isSelected: Boolean,
    onToggleSelect: () -> Unit,
    onTopicClick: (StudyTopic) -> Unit,
    onPracticeSubject: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("study_subject_card_${subject.subjectId}")
            .clickable { onToggleSelect() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.surface
            else
                MaterialTheme.colorScheme.surface
        ),
        border = if (isSelected) CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(SaffronPrimary),
            width = 1.5.dp
        ) else null,
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) SaffronPrimary.copy(alpha = 0.2f)
                                else NavySecondary.copy(alpha = 0.08f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = subject.icon, fontSize = 22.sp)
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = subject.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) SaffronPrimary else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${topics.size} टॉपिक्स उपलब्ध",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(
                    onClick = onToggleSelect,
                    modifier = Modifier.testTag("toggle_subject_btn_${subject.subjectId}")
                ) {
                    Icon(
                        imageVector = if (isSelected) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (isSelected) "Collapse" else "Expand",
                        tint = if (isSelected) SaffronPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onToggleSelect,
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text = if (isSelected) "टॉपिक्स बंद करें ▲" else "टॉपिक्स देखें (${topics.size}) ▼",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = NavySecondary
                    )
                }

                Button(
                    onClick = onPracticeSubject,
                    colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("practice_btn_${subject.subjectId}")
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("अभ्यास शुरू करें", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Expandable Topics List
            AnimatedVisibility(
                visible = isSelected,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                        .testTag("topics_section_${subject.subjectId}")
                ) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                        thickness = 1.dp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "📋 ${subject.name} के टॉपिक्स:",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    if (topics.isEmpty()) {
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            Text(
                                text = "इस विषय में अभी कोई टॉपिक नहीं जोड़ा गया है।",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            topics.forEachIndexed { index, topic ->
                                TopicItemRow(
                                    index = index + 1,
                                    topic = topic,
                                    onTopicClick = { onTopicClick(topic) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Topic row item within a subject.
 */
@Composable
fun TopicItemRow(
    index: Int,
    topic: StudyTopic,
    onTopicClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("topic_row_${topic.topicId}")
            .clickable { onTopicClick() },
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = NavySecondary.copy(alpha = 0.1f),
                    modifier = Modifier.size(28.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "$index",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = NavySecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = topic.topicName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (topic.description.isNotBlank()) {
                        Text(
                            text = topic.description,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2
                        )
                    }
                }
            }

            IconButton(
                onClick = onTopicClick,
                modifier = Modifier.size(32.dp).testTag("select_topic_btn_${topic.topicId}")
            ) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Open topic",
                    tint = SaffronPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
