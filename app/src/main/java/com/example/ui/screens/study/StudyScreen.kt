package com.example.ui.screens.study

import android.content.Intent
import android.net.Uri
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.components.MetricStatCard
import com.example.ui.theme.*
import com.example.util.TopicFileUtils
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun StudyScreen(
    allChapters: List<Chapter>,
    allSubjects: List<StudySubject>,
    allTopics: List<StudyTopic>,
    allQuestions: List<Question>,
    studentStudyAttempts: List<StudyAttempt>,
    allTopicDocuments: List<TopicDocument> = emptyList(),
    selectedSubject: String,
    onSelectSubject: (String) -> Unit,
    onToggleChapter: (Chapter) -> Unit,
    onStartChapterQuiz: (Chapter) -> Unit,
    onRecordAttempt: (questionId: String, subjectId: String, topicId: String, selectedAnswer: String, isCorrect: Boolean, timeTaken: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var studyTab by remember { mutableStateOf(0) } // 0: विषयवार अभ्यास (Subjects & Topics), 1: पाठ्यक्रम व नोट्स (Chapters)
    var selectedSubjectDetail by remember { mutableStateOf<StudySubject?>(null) }
    var practiceTopic by remember { mutableStateOf<StudyTopic?>(null) }
    var practiceSubject by remember { mutableStateOf<StudySubject?>(null) }
    var practiceInitialPartIndex by remember { mutableIntStateOf(0) }
    var selectedChapterForDetail by remember { mutableStateOf<Chapter?>(null) }
    var chapterSearchQuery by remember { mutableStateOf("") }

    val todayDateStr = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }

    // Overall Study Stats calculation
    val totalAttempts = studentStudyAttempts.size
    val correctAttempts = studentStudyAttempts.count { it.isCorrect }
    val overallAccuracy = if (totalAttempts > 0) ((correctAttempts * 100) / totalAttempts) else 0
    val todayAttemptsCount = studentStudyAttempts.count { it.attemptDate == todayDateStr }
    val activeSubjectsCount = allSubjects.count { it.isActive }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("study_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Screen Header & Mode Toggle
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
                                text = "📚 मेरी पढ़ाई (Study Material & Question Bank)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "गाँव से सेना–पुलिस भर्ती मिशन • विषयवार प्रश्न बैंक एवं नोट्स",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Tab selector
                    TabRow(
                        selectedTabIndex = studyTab,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        contentColor = SaffronPrimary,
                        modifier = Modifier.clip(RoundedCornerShape(10.dp))
                    ) {
                        Tab(
                            selected = studyTab == 0,
                            onClick = { studyTab = 0 },
                            text = { Text("विषय व टॉपिक अभ्यास", fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                            modifier = Modifier.testTag("study_tab_subjects")
                        )
                        Tab(
                            selected = studyTab == 1,
                            onClick = { studyTab = 1 },
                            text = { Text("पाठ्यक्रम व नोट्स", fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                            modifier = Modifier.testTag("study_tab_chapters")
                        )
                    }
                }
            }
        }

        if (studyTab == 0) {
            // --- TAB 0: SUBJECTS & TOPICS (PHASE 2B-1) ---

            // Overview Study Stats
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MetricStatCard(
                        title = "कुल प्रश्न हल",
                        value = "$totalAttempts",
                        subtitle = "अभ्यास किए गए",
                        icon = Icons.Default.CheckCircle,
                        iconColor = OliveTertiary,
                        modifier = Modifier.weight(1f).testTag("stat_total_questions_attempted")
                    )
                    MetricStatCard(
                        title = "सटीकता (Accuracy)",
                        value = "$overallAccuracy%",
                        subtitle = "$correctAttempts सही",
                        icon = Icons.Default.Percent,
                        iconColor = SaffronPrimary,
                        modifier = Modifier.weight(1f).testTag("stat_overall_accuracy")
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MetricStatCard(
                        title = "आज के प्रश्न",
                        value = "$todayAttemptsCount",
                        subtitle = "दैनिक लक्ष्य",
                        icon = Icons.Default.Today,
                        iconColor = NavySecondary,
                        modifier = Modifier.weight(1f).testTag("stat_today_attempts")
                    )
                    MetricStatCard(
                        title = "सक्रिय विषय",
                        value = "$activeSubjectsCount",
                        subtitle = "उपलब्ध कोर्स",
                        icon = Icons.Default.MenuBook,
                        iconColor = Color(0xFF7C3AED),
                        modifier = Modifier.weight(1f).testTag("stat_active_subjects")
                    )
                }
            }

            // Subject Cards Section Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "विषय अनुसार अध्ययन (Subjects)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${allSubjects.size} विषय उपलब्ध",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Subject Cards List
            items(allSubjects.filter { it.isActive }) { subject ->
                val subjectTopics = allTopics.filter { it.subjectId == subject.subjectId && it.isActive }
                val subjectQuestions = allQuestions.filter { (it.subjectId == subject.subjectId || it.subjectName.equals(subject.name, ignoreCase = true)) && it.isActive }
                val subjectAttempts = studentStudyAttempts.filter { it.subjectId == subject.subjectId }
                val subjectCorrect = subjectAttempts.count { it.isCorrect }
                val subjectAccuracy = if (subjectAttempts.isNotEmpty()) ((subjectCorrect * 100) / subjectAttempts.size) else 0

                val isExpanded = selectedSubjectDetail?.subjectId == subject.subjectId

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("study_subject_card_${subject.subjectId}")
                        .clickable {
                            selectedSubjectDetail = if (isExpanded) null else subject
                        },
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
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(CircleShape)
                                        .background(SaffronPrimary.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = subject.icon, fontSize = 22.sp)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = subject.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${subjectTopics.size} टॉपिक्स • ${subjectQuestions.size} प्रश्न उपलब्ध",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Surface(
                                    color = if (subjectAccuracy >= 70) StatusPresent.copy(alpha = 0.15f)
                                    else if (subjectAccuracy > 0) SaffronPrimary.copy(alpha = 0.15f)
                                    else MaterialTheme.colorScheme.surfaceVariant,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = if (subjectAttempts.isNotEmpty()) "$subjectAccuracy% सही" else "नया",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (subjectAccuracy >= 70) StatusPresent
                                        else if (subjectAccuracy > 0) SaffronPrimary
                                        else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(
                                onClick = {
                                    selectedSubjectDetail = if (isExpanded) null else subject
                                },
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text(
                                    text = if (isExpanded) "टॉपिक्स छुपाएं ▲" else "टॉपिक्स देखें (${subjectTopics.size}) ▼",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = NavySecondary,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Button(
                                    onClick = {
                                        practiceSubject = subject
                                        practiceTopic = null
                                        practiceInitialPartIndex = 0
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                    modifier = Modifier.testTag("practice_subject_${subject.subjectId}")
                                ) {
                                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("संपूर्ण विषय अभ्यास", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // 50-50 Part Quick Launch for Subject (If > 50 questions)
                        val subjectParts = subjectQuestions.chunked(50)
                        if (subjectParts.size > 1) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "📑 50-50 प्रश्न अभ्यास भाग (${subjectParts.size} भाग):",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = SaffronPrimary
                                        )
                                        Text(
                                            text = "कुल ${subjectQuestions.size} प्रश्न",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    LazyRow(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        items(subjectParts.size) { pIdx ->
                                            val startQ = pIdx * 50 + 1
                                            val endQ = minOf((pIdx + 1) * 50, subjectQuestions.size)
                                            FilledTonalButton(
                                                onClick = {
                                                    practiceSubject = subject
                                                    practiceTopic = null
                                                    practiceInitialPartIndex = pIdx
                                                },
                                                shape = RoundedCornerShape(6.dp),
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                modifier = Modifier.testTag("practice_sub_${subject.subjectId}_part_$pIdx")
                                            ) {
                                                Text("भाग ${pIdx + 1} ($startQ-$endQ)", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Expanded Topic List inside Subject Card
                        if (isExpanded) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "${subject.name} के महत्वपूर्ण टॉपिक्स:",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                subjectTopics.forEach { topic ->
                                    val topicQuestions = allQuestions.filter { (it.topicId == topic.topicId || it.chapterName.equals(topic.topicName, ignoreCase = true)) && it.isActive }
                                    val topicAttempts = studentStudyAttempts.filter { it.topicId == topic.topicId }
                                    val topicCorrect = topicAttempts.count { it.isCorrect }
                                    val topicAcc = if (topicAttempts.isNotEmpty()) ((topicCorrect * 100) / topicAttempts.size) else 0

                                    val topicDocs = remember(allTopicDocuments, topic.topicId) {
                                        allTopicDocuments.filter { it.topicId == topic.topicId }
                                    }

                                    val topicParts = topicQuestions.chunked(50)

                                    Surface(
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(10.dp),
                                            verticalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = topic.topicName,
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        fontWeight = FontWeight.SemiBold
                                                    )
                                                    Text(
                                                        text = "${topicQuestions.size} प्रश्न • हल: ${topicAttempts.size} (सटीकता: $topicAcc%)" +
                                                            if (topicParts.size > 1) " • ${topicParts.size} भाग (50-50)" else "",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }

                                                if (topicParts.size <= 1) {
                                                    Button(
                                                        onClick = {
                                                            practiceSubject = subject
                                                            practiceTopic = topic
                                                            practiceInitialPartIndex = 0
                                                        },
                                                        colors = ButtonDefaults.buttonColors(containerColor = OliveTertiary),
                                                        shape = RoundedCornerShape(6.dp),
                                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                                        modifier = Modifier.testTag("practice_topic_${topic.topicId}")
                                                    ) {
                                                        Text("अभ्यास करें", style = MaterialTheme.typography.labelSmall)
                                                    }
                                                }
                                            }

                                            // If topic has > 50 questions, show 50-50 part buttons
                                            if (topicParts.size > 1) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = "50-50 भाग:",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = FontWeight.Bold,
                                                        color = OliveTertiary,
                                                        modifier = Modifier.padding(end = 6.dp)
                                                    )
                                                    LazyRow(
                                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                        modifier = Modifier.fillMaxWidth()
                                                    ) {
                                                        items(topicParts.size) { pIdx ->
                                                            val startQ = pIdx * 50 + 1
                                                            val endQ = minOf((pIdx + 1) * 50, topicQuestions.size)
                                                            Button(
                                                                onClick = {
                                                                    practiceSubject = subject
                                                                    practiceTopic = topic
                                                                    practiceInitialPartIndex = pIdx
                                                                },
                                                                colors = ButtonDefaults.buttonColors(containerColor = OliveTertiary),
                                                                shape = RoundedCornerShape(6.dp),
                                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                                modifier = Modifier.testTag("practice_topic_${topic.topicId}_part_$pIdx")
                                                            ) {
                                                                Text("भाग ${pIdx + 1} ($startQ-$endQ)", style = MaterialTheme.typography.labelSmall)
                                                            }
                                                        }
                                                    }
                                                }
                                            }

                                            // Topic-Wise PDF and Excel materials uploaded by Admin
                                            if (topicDocs.isNotEmpty()) {
                                                Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                                                Text(
                                                    text = "📂 अध्ययन सामग्री (PDF व Excel शीट्स):",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = NavySecondary
                                                )
                                                LazyRow(
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    items(topicDocs) { doc ->
                                                        val isPdf = doc.isPdf
                                                        Surface(
                                                            color = if (isPdf) StatusAbsent.copy(alpha = 0.12f) else Color(0xFF059669).copy(alpha = 0.12f),
                                                            shape = RoundedCornerShape(8.dp),
                                                            modifier = Modifier.clickable {
                                                                TopicFileUtils.openDocument(context, doc)
                                                            }
                                                        ) {
                                                            Row(
                                                                verticalAlignment = Alignment.CenterVertically,
                                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                            ) {
                                                                Text(if (isPdf) "📕" else "📗", fontSize = 12.sp)
                                                                Spacer(modifier = Modifier.width(4.dp))
                                                                Text(
                                                                    text = doc.title,
                                                                    fontSize = 11.sp,
                                                                    fontWeight = FontWeight.Medium,
                                                                    color = if (isPdf) StatusAbsent else Color(0xFF059669)
                                                                )
                                                                Spacer(modifier = Modifier.width(4.dp))
                                                                Icon(
                                                                    imageVector = Icons.Default.OpenInNew,
                                                                    contentDescription = "Open",
                                                                    modifier = Modifier.size(12.dp),
                                                                    tint = if (isPdf) StatusAbsent else Color(0xFF059669)
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // --- TAB 1: CHAPTERS & SYLLABUS (EXISTING COMPATIBILITY) ---
            val subjectsList = listOf("Mathematics", "Reasoning", "Hindi", "English", "GK / GS", "Computer")
            val filteredChapters = allChapters.filter { chapter ->
                val matchSubject = chapter.subjectName.equals(selectedSubject, ignoreCase = true)
                val matchSearch = chapterSearchQuery.isBlank() ||
                        chapter.chapterName.contains(chapterSearchQuery, ignoreCase = true) ||
                        chapter.description.contains(chapterSearchQuery, ignoreCase = true) ||
                        chapter.chapterNumber.toString() == chapterSearchQuery.trim()
                matchSubject && matchSearch
            }.sortedWith(compareBy({ it.displayOrder }, { it.chapterNumber }))

            val completedCount = filteredChapters.count { it.isCompleted }
            val totalCount = filteredChapters.size.coerceAtLeast(1)
            val subjectProgress = (completedCount.toFloat() / totalCount.toFloat())

            // Subject Progress Bar
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
                            Text(
                                text = "$selectedSubject प्रगति",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "$completedCount / $totalCount चैप्टर्स पूर्ण (${(subjectProgress * 100).toInt()}%)",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = SaffronPrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        LinearProgressIndicator(
                            progress = { subjectProgress.coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = SaffronPrimary,
                            trackColor = SaffronPrimary.copy(alpha = 0.15f)
                        )
                    }
                }
            }

            // Search Bar for Chapters
            item {
                OutlinedTextField(
                    value = chapterSearchQuery,
                    onValueChange = { chapterSearchQuery = it },
                    placeholder = { Text("$selectedSubject में अध्याय खोजें...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                    trailingIcon = {
                        if (chapterSearchQuery.isNotEmpty()) {
                            IconButton(onClick = { chapterSearchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().testTag("student_chapter_search_input")
                )
            }

            // Subject Filter Chips
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(subjectsList) { subject ->
                        val isSelected = subject.equals(selectedSubject, ignoreCase = true)
                        FilterChip(
                            selected = isSelected,
                            onClick = { onSelectSubject(subject) },
                            label = {
                                Text(
                                    text = when (subject) {
                                        "Mathematics" -> "गणित (Maths)"
                                        "Reasoning" -> "रीजनिंग (Reasoning)"
                                        "Hindi" -> "सामान्य हिंदी"
                                        "English" -> "General English"
                                        "GK / GS" -> "GK / GS व संविधान"
                                        "Computer" -> "कंप्यूटर (Computer)"
                                        else -> subject
                                    },
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SaffronContainer,
                                selectedLabelColor = OnSaffronContainer
                            ),
                            modifier = Modifier.testTag("subject_tab_$subject")
                        )
                    }
                }
            }

            // Chapters List
            item {
                Text(
                    text = "$selectedSubject के अध्याय (${filteredChapters.size})",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            items(filteredChapters) { chapter ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedChapterForDetail = chapter },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = chapter.isCompleted,
                                onCheckedChange = { onToggleChapter(chapter) },
                                colors = CheckboxDefaults.colors(checkedColor = OliveTertiary)
                            )

                            Spacer(modifier = Modifier.width(6.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${chapter.chapterNumber}. ${chapter.chapterName}",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    if (chapter.isTodayTarget) {
                                        Surface(
                                            color = SaffronPrimary,
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = "Today Target",
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }

                                if (chapter.description.isNotEmpty()) {
                                    Text(
                                        text = chapter.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 2
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                if (chapter.fileUrl.isNotBlank()) {
                                    Surface(
                                        color = Color(0xFFFEE2E2),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                Icons.Default.PictureAsPdf,
                                                contentDescription = null,
                                                tint = Color(0xFFDC2626),
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text(
                                                "PDF नोट्स",
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                                color = Color(0xFF991B1B),
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }

                                Text(
                                    text = "${chapter.practiceQuestionsCount} प्रश्न",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                OutlinedButton(
                                    onClick = { selectedChapterForDetail = chapter },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text("नोट्स (Notes)", style = MaterialTheme.typography.labelSmall)
                                }

                                Button(
                                    onClick = { onStartChapterQuiz(chapter) },
                                    colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.testTag("start_quiz_btn_${chapter.id}")
                                ) {
                                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text("क्विज़ दें", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Direct Interactive Practice Screen (Phase 2B-2)
    val currentPracticeSubject = practiceSubject
    if (currentPracticeSubject != null) {
        val targetQuestions = remember(currentPracticeSubject, practiceTopic, allQuestions) {
            val sId = currentPracticeSubject.subjectId
            val tId = practiceTopic?.topicId
            allQuestions.filter { q ->
                val matchesSubject = q.subjectId == sId || q.subjectName.equals(currentPracticeSubject.name, ignoreCase = true)
                val matchesTopic = tId == null || q.topicId == tId || q.chapterName.equals(practiceTopic?.topicName, ignoreCase = true)
                matchesSubject && matchesTopic && q.isActive
            }.ifEmpty {
                // If specific filter yields empty, provide subject's questions
                allQuestions.filter { q ->
                    (q.subjectId == sId || q.subjectName.equals(currentPracticeSubject.name, ignoreCase = true)) && q.isActive
                }
            }
        }

        PracticeQuizScreen(
            subject = currentPracticeSubject,
            topic = practiceTopic,
            questions = targetQuestions,
            initialPartIndex = practiceInitialPartIndex,
            onRecordAttempt = onRecordAttempt,
            onBack = {
                practiceSubject = null
                practiceTopic = null
                practiceInitialPartIndex = 0
            },
            modifier = modifier
        )
        return
    }

    // Chapter Detail & Notes Dialog (from Phase 1 / 2A)
    selectedChapterForDetail?.let { ch ->
        AlertDialog(
            onDismissRequest = { selectedChapterForDetail = null },
            title = {
                Text(
                    text = ch.chapterName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Surface(
                            color = SaffronContainer.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "विषय: ${ch.subjectName} • क्रम: #${ch.chapterOrder}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = OnSaffronContainer,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }

                    item {
                        Text(
                            text = "विवरण एवं मुख्य बिंदु:",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = ch.notesContent.ifEmpty { "इस अध्याय के संपूर्ण नोट्स एवं सूत्र उपलब्ध हैं।" },
                            style = MaterialTheme.typography.bodyMedium,
                            lineHeight = 22.sp
                        )
                    }

                    if (ch.fileUrl.isNotBlank()) {
                        item {
                            Card(
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFEE2E2)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        try {
                                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(ch.fileUrl))
                                            context.startActivity(intent)
                                        } catch (e: Exception) {
                                            // Fallback
                                        }
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PictureAsPdf,
                                        contentDescription = "PDF Notes",
                                        tint = Color(0xFFDC2626),
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = ch.fileName.ifBlank { "अध्याय स्टडी नोट्स (PDF)" },
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF7F1D1D)
                                        )
                                        Text(
                                            text = "PDF नोट्स खोलें व डाउनलोड करें",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color(0xFF991B1B)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (ch.videoTitle.isNotEmpty()) {
                        item {
                            Card(
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        try {
                                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(ch.videoUrl))
                                            context.startActivity(intent)
                                        } catch (e: Exception) {
                                            // Fallback
                                        }
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.SmartDisplay,
                                        contentDescription = "Video",
                                        tint = Color.Red,
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = ch.videoTitle,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "वीडियो क्लास देखें (Watch on YouTube)",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val chapterToQuiz = ch
                        selectedChapterForDetail = null
                        onStartChapterQuiz(chapterToQuiz)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary)
                ) {
                    Text("इस अध्याय का टेस्ट शुरू करें")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedChapterForDetail = null }) {
                    Text("बंद करें")
                }
            }
        )
    }
}

@Composable
fun TopicPracticeDialog(
    subject: StudySubject,
    topic: StudyTopic?,
    questions: List<Question>,
    onRecordAttempt: (questionId: String, subjectId: String, topicId: String, selectedAnswer: String, isCorrect: Boolean, timeTaken: Int) -> Unit,
    onDismiss: () -> Unit
) {
    var currentIndex by remember { mutableStateOf(0) }
    var selectedOption by remember { mutableStateOf<String?>(null) }
    var isSubmitted by remember { mutableStateOf(false) }
    var correctAnswersCount by remember { mutableStateOf(0) }
    var isFinished by remember { mutableStateOf(false) }

    val currentQ = questions.getOrNull(currentIndex)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "${subject.icon} ${subject.name}" + (if (topic != null) " • ${topic.topicName}" else ""),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (questions.isNotEmpty() && !isFinished) {
                    Text(
                        text = "प्रश्न ${currentIndex + 1} / ${questions.size}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            if (questions.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("इस टॉपिक/विषय में अभी कोई सक्रिय प्रश्न उपलब्ध नहीं है।", style = MaterialTheme.typography.bodyMedium)
                }
            } else if (isFinished) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.EmojiEvents, contentDescription = null, tint = SaffronPrimary, modifier = Modifier.size(54.dp))
                    Text("अभ्यास पूर्ण हुआ! 🎉", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("कुल प्रश्न: ${questions.size}", style = MaterialTheme.typography.bodyMedium)
                    Text("सही उत्तर: $correctAnswersCount / ${questions.size}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = StatusPresent)
                    val percent = ((correctAnswersCount * 100) / questions.size)
                    Text("सटीकता (Accuracy): $percent%", style = MaterialTheme.typography.titleSmall, color = SaffronPrimary, fontWeight = FontWeight.Bold)
                }
            } else if (currentQ != null) {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Question text
                    item {
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = currentQ.questionText,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    // Options list
                    items(
                        listOf(
                            "A" to currentQ.optionA,
                            "B" to currentQ.optionB,
                            "C" to currentQ.optionC,
                            "D" to currentQ.optionD
                        )
                    ) { (optLetter, optText) ->
                        val isSelected = selectedOption == optLetter
                        val isCorrectOption = currentQ.correctLetter.equals(optLetter, ignoreCase = true)

                        val backgroundColor = when {
                            !isSubmitted && isSelected -> SaffronPrimary.copy(alpha = 0.2f)
                            isSubmitted && isCorrectOption -> StatusPresent.copy(alpha = 0.2f)
                            isSubmitted && isSelected && !isCorrectOption -> StatusAbsent.copy(alpha = 0.2f)
                            else -> MaterialTheme.colorScheme.surface
                        }

                        val borderColor = when {
                            !isSubmitted && isSelected -> SaffronPrimary
                            isSubmitted && isCorrectOption -> StatusPresent
                            isSubmitted && isSelected && !isCorrectOption -> StatusAbsent
                            else -> MaterialTheme.colorScheme.outlineVariant
                        }

                        Surface(
                            color = backgroundColor,
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = !isSubmitted) {
                                    selectedOption = optLetter
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(borderColor.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = optLetter, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = optText,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = if (isSelected || (isSubmitted && isCorrectOption)) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }

                    // Explanation once submitted
                    if (isSubmitted) {
                        item {
                            Surface(
                                color = if (selectedOption.equals(currentQ.correctLetter, ignoreCase = true)) StatusPresent.copy(alpha = 0.1f) else StatusAbsent.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = if (selectedOption.equals(currentQ.correctLetter, ignoreCase = true)) Icons.Default.CheckCircle else Icons.Default.Cancel,
                                            contentDescription = null,
                                            tint = if (selectedOption.equals(currentQ.correctLetter, ignoreCase = true)) StatusPresent else StatusAbsent,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (selectedOption.equals(currentQ.correctLetter, ignoreCase = true)) "सही उत्तर! (Correct)" else "गलत उत्तर! (Incorrect - सही: ${currentQ.correctLetter})",
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = if (selectedOption.equals(currentQ.correctLetter, ignoreCase = true)) StatusPresent else StatusAbsent
                                        )
                                    }
                                    if (currentQ.explanation.isNotBlank()) {
                                        Text(
                                            text = "व्याख्या: ${currentQ.explanation}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (questions.isNotEmpty() && !isFinished) {
                if (!isSubmitted) {
                    Button(
                        onClick = {
                            if (selectedOption != null && currentQ != null) {
                                val isCorrect = selectedOption.equals(currentQ.correctLetter, ignoreCase = true)
                                if (isCorrect) correctAnswersCount++
                                isSubmitted = true
                                onRecordAttempt(
                                    currentQ.questionId,
                                    currentQ.subjectId.ifEmpty { subject.subjectId },
                                    currentQ.topicId.ifEmpty { topic?.topicId ?: "" },
                                    selectedOption ?: "",
                                    isCorrect,
                                    15
                                )
                            }
                        },
                        enabled = selectedOption != null,
                        colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary)
                    ) {
                        Text("जांचें (Submit)")
                    }
                } else {
                    Button(
                        onClick = {
                            if (currentIndex < questions.size - 1) {
                                currentIndex++
                                selectedOption = null
                                isSubmitted = false
                            } else {
                                isFinished = true
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary)
                    ) {
                        Text(if (currentIndex < questions.size - 1) "अगला प्रश्न ▶" else "परिणाम देखें 🏁")
                    }
                }
            } else {
                Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary)) {
                    Text("समाप्त")
                }
            }
        },
        dismissButton = {
            if (!isFinished) {
                TextButton(onClick = onDismiss) { Text("बंद करें") }
            }
        }
    )
}
