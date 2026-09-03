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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.theme.*

/**
 * Dedicated Study & Exam Management Hub for Admin and Coaches.
 * Gives coaches and admins control over:
 * 1. Question Bank (Add/Edit/Delete questions & manage subjects)
 * 2. Syllabus & Notes (Add/Edit/Delete study chapters & materials)
 * 3. Cadet Mock Test Results (Track cadet test attempts, scores & accuracy)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminStudyHubScreen(
    allQuestions: List<Question>,
    allSubjects: List<StudySubject>,
    allTopics: List<StudyTopic>,
    allChapters: List<Chapter>,
    allStudents: List<StudentProfile>,
    allTestAttempts: List<TestAttempt>,
    onAddQuestion: (Question) -> Unit,
    onUpdateQuestion: (Question) -> Unit,
    onDeleteQuestion: (Question) -> Unit,
    onToggleActive: (Question) -> Unit,
    onAddSubject: (StudySubject) -> Unit,
    onAddTopic: (StudyTopic) -> Unit,
    onAddChapter: (Chapter) -> Unit,
    onUpdateChapter: (Chapter) -> Unit,
    onDeleteChapter: (Chapter) -> Unit,
    onTogglePublishChapter: (Chapter) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedHubTab by remember { mutableStateOf(0) } // 0: प्रश्न बैंक, 1: पाठ्यक्रम व नोट्स, 2: कैडेट्स टेस्ट परिणाम

    // Question Bank State
    var qSearchQuery by remember { mutableStateOf("") }
    var qSubjectFilter by remember { mutableStateOf<String?>(null) }
    var showAddQuestionDialog by remember { mutableStateOf(false) }
    var questionToEdit by remember { mutableStateOf<Question?>(null) }
    var questionToDelete by remember { mutableStateOf<Question?>(null) }

    // Chapters State
    var chSearchQuery by remember { mutableStateOf("") }
    var chSubjectFilter by remember { mutableStateOf("ALL") }
    var showAddChapterDialog by remember { mutableStateOf(false) }
    var chapterToEdit by remember { mutableStateOf<Chapter?>(null) }
    var chapterToDelete by remember { mutableStateOf<Chapter?>(null) }

    val filteredQuestions = remember(allQuestions, qSearchQuery, qSubjectFilter) {
        allQuestions.filter { q ->
            val matchesSearch = qSearchQuery.isBlank() ||
                    q.questionText.contains(qSearchQuery, ignoreCase = true) ||
                    q.explanation.contains(qSearchQuery, ignoreCase = true)
            val matchesSubject = qSubjectFilter == null || q.subjectId == qSubjectFilter
            matchesSearch && matchesSubject
        }
    }

    val filteredChapters = remember(allChapters, chSearchQuery, chSubjectFilter) {
        allChapters.filter { ch ->
            val matchesSubject = chSubjectFilter == "ALL" || ch.subjectName.equals(chSubjectFilter, ignoreCase = true)
            val matchesSearch = chSearchQuery.isBlank() ||
                    ch.chapterName.contains(chSearchQuery, ignoreCase = true) ||
                    ch.description.contains(chSearchQuery, ignoreCase = true)
            matchesSubject && matchesSearch
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("admin_study_hub_screen")
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Top Header Card for Coach / Admin
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(OliveTertiary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MenuBook,
                                    contentDescription = null,
                                    tint = OliveTertiary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "स्टडी व प्रश्न बैंक प्रबंधन",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "प्रशिक्षकों व एडमिन हेतु प्रश्न निर्माण, नोट्स व टेस्ट समीक्षा",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 3-Tab Navigation Bar
                    TabRow(
                        selectedTabIndex = selectedHubTab,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        contentColor = SaffronPrimary,
                        modifier = Modifier.clip(RoundedCornerShape(10.dp))
                    ) {
                        Tab(
                            selected = selectedHubTab == 0,
                            onClick = { selectedHubTab = 0 },
                            text = {
                                Text(
                                    "प्रश्न बैंक (${allQuestions.size})",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (selectedHubTab == 0) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            icon = { Icon(Icons.Default.Quiz, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        )
                        Tab(
                            selected = selectedHubTab == 1,
                            onClick = { selectedHubTab = 1 },
                            text = {
                                Text(
                                    "पाठ्यक्रम व नोट्स (${allChapters.size})",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (selectedHubTab == 1) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            icon = { Icon(Icons.Default.AutoStories, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        )
                        Tab(
                            selected = selectedHubTab == 2,
                            onClick = { selectedHubTab = 2 },
                            text = {
                                Text(
                                    "कैडेट्स टेस्ट परिणाम",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (selectedHubTab == 2) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            icon = { Icon(Icons.Default.Analytics, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        )
                    }
                }
            }

            // Tab Content
            when (selectedHubTab) {
                0 -> {
                    // TAB 0: Question Bank Manager
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(vertical = 12.dp)
                    ) {
                        item {
                            Button(
                                onClick = { showAddQuestionDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.AddCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("नया प्रश्न जोड़ें (Add Question)", style = MaterialTheme.typography.labelSmall)
                            }
                        }

                        // Search & Subject Filter
                        item {
                            OutlinedTextField(
                                value = qSearchQuery,
                                onValueChange = { qSearchQuery = it },
                                placeholder = { Text("प्रश्न खोजें...") },
                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = SaffronPrimary) },
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        item {
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                item {
                                    FilterChip(
                                        selected = qSubjectFilter == null,
                                        onClick = { qSubjectFilter = null },
                                        label = { Text("सभी विषय (${allQuestions.size})", style = MaterialTheme.typography.labelSmall) }
                                    )
                                }
                                items(allSubjects) { subj ->
                                    val count = allQuestions.count { it.subjectId == subj.subjectId }
                                    FilterChip(
                                        selected = qSubjectFilter == subj.subjectId,
                                        onClick = { qSubjectFilter = if (qSubjectFilter == subj.subjectId) null else subj.subjectId },
                                        label = { Text("${subj.name} ($count)", style = MaterialTheme.typography.labelSmall) }
                                    )
                                }
                            }
                        }

                        // Question Items
                        items(filteredQuestions) { q ->
                            val correctLetter = q.correctLetter
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (q.isActive) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                ),
                                border = CardDefaults.outlinedCardBorder()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Surface(
                                                color = SaffronPrimary.copy(alpha = 0.15f),
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    text = q.subjectName.ifBlank { q.subjectId.replace("SUB_", "") },
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = SaffronPrimary,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                color = when (q.difficulty) {
                                                    "कठिन", "HARD" -> Color(0xFFDC2626).copy(alpha = 0.15f)
                                                    "मध्यम", "MEDIUM" -> SaffronPrimary.copy(alpha = 0.15f)
                                                    else -> OliveTertiary.copy(alpha = 0.15f)
                                                },
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    text = q.difficulty,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = when (q.difficulty) {
                                                        "कठिन", "HARD" -> Color(0xFFDC2626)
                                                        "मध्यम", "MEDIUM" -> SaffronPrimary
                                                        else -> OliveTertiary
                                                    },
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            IconButton(onClick = { questionToEdit = q }, modifier = Modifier.size(32.dp)) {
                                                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = SaffronPrimary, modifier = Modifier.size(18.dp))
                                            }
                                            IconButton(onClick = { questionToDelete = q }, modifier = Modifier.size(32.dp)) {
                                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = StatusAbsent, modifier = Modifier.size(18.dp))
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = q.questionText,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))
                                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Text("A. ${q.optionA}", style = MaterialTheme.typography.bodySmall, color = if (correctLetter == "A") OliveTertiary else MaterialTheme.colorScheme.onSurface, fontWeight = if (correctLetter == "A") FontWeight.Bold else FontWeight.Normal)
                                        Text("B. ${q.optionB}", style = MaterialTheme.typography.bodySmall, color = if (correctLetter == "B") OliveTertiary else MaterialTheme.colorScheme.onSurface, fontWeight = if (correctLetter == "B") FontWeight.Bold else FontWeight.Normal)
                                        Text("C. ${q.optionC}", style = MaterialTheme.typography.bodySmall, color = if (correctLetter == "C") OliveTertiary else MaterialTheme.colorScheme.onSurface, fontWeight = if (correctLetter == "C") FontWeight.Bold else FontWeight.Normal)
                                        Text("D. ${q.optionD}", style = MaterialTheme.typography.bodySmall, color = if (correctLetter == "D") OliveTertiary else MaterialTheme.colorScheme.onSurface, fontWeight = if (correctLetter == "D") FontWeight.Bold else FontWeight.Normal)
                                    }

                                    if (q.explanation.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Surface(
                                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                            shape = RoundedCornerShape(6.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = "💡 हल: ${q.explanation}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.padding(6.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                1 -> {
                    // TAB 1: Syllabus & Notes Manager
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(vertical = 12.dp)
                    ) {
                        item {
                            Button(
                                onClick = { showAddChapterDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = OliveTertiary),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.PostAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("नया अध्याय / नोट्स जोड़ें (Add Chapter Notes)", style = MaterialTheme.typography.labelSmall)
                            }
                        }

                        item {
                            OutlinedTextField(
                                value = chSearchQuery,
                                onValueChange = { chSearchQuery = it },
                                placeholder = { Text("अध्याय या नोट्स खोजें...") },
                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = OliveTertiary) },
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        item {
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                val subjectsList = listOf("ALL", "गणित", "रीजनिंग", "सामान्य ज्ञान", "सामान्य विज्ञान", "हिंदी", "English")
                                items(subjectsList) { subj ->
                                    FilterChip(
                                        selected = chSubjectFilter == subj,
                                        onClick = { chSubjectFilter = subj },
                                        label = { Text(if (subj == "ALL") "सभी विषय" else subj, style = MaterialTheme.typography.labelSmall) }
                                    )
                                }
                            }
                        }

                        items(filteredChapters) { chapter ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = CardDefaults.outlinedCardBorder()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Surface(
                                                color = SaffronContainer,
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    text = chapter.subjectName,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = SaffronDark,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                color = if (chapter.isPublished) OliveTertiary.copy(alpha = 0.15f) else Color(0xFFDC2626).copy(alpha = 0.15f),
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    text = if (chapter.isPublished) "प्रकाशित" else "ड्राफ्ट",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = if (chapter.isPublished) OliveTertiary else Color(0xFFDC2626),
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }

                                        Row {
                                            IconButton(onClick = { chapterToEdit = chapter }, modifier = Modifier.size(32.dp)) {
                                                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = OliveTertiary, modifier = Modifier.size(18.dp))
                                            }
                                            IconButton(onClick = { chapterToDelete = chapter }, modifier = Modifier.size(32.dp)) {
                                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = StatusAbsent, modifier = Modifier.size(18.dp))
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = chapter.chapterName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = chapter.description.ifBlank { "अध्याय विवरण व महत्वपूर्ण सूत्र" },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    if (chapter.fileUrl.isNotBlank() || chapter.videoUrl.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            if (chapter.fileUrl.isNotBlank()) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(14.dp))
                                                    Spacer(modifier = Modifier.width(2.dp))
                                                    Text("PDF नोट्स", style = MaterialTheme.typography.labelSmall, color = Color(0xFFDC2626))
                                                }
                                            }
                                            if (chapter.videoUrl.isNotBlank()) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(Icons.Default.PlayCircle, contentDescription = null, tint = SaffronPrimary, modifier = Modifier.size(14.dp))
                                                    Spacer(modifier = Modifier.width(2.dp))
                                                    Text("वीडियो क्लास", style = MaterialTheme.typography.labelSmall, color = SaffronPrimary)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                2 -> {
                    // TAB 2: Cadet Test Results & Evaluation
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(vertical = 12.dp)
                    ) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("कैडेट्स मॉक टेस्ट मूल्यांकन सारांश", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                    Text("छात्रों द्वारा दिए गए टेस्ट्स एवं स्कोर की लाइव सूची", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceAround
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("${allTestAttempts.size}", fontWeight = FontWeight.Bold, color = SaffronPrimary)
                                            Text("कुल टेस्ट दिए", style = MaterialTheme.typography.labelSmall)
                                        }
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            val avgScore = if (allTestAttempts.isNotEmpty()) allTestAttempts.map { it.accuracyPercentage }.average().toInt() else 0
                                            Text("$avgScore%", fontWeight = FontWeight.Bold, color = OliveTertiary)
                                            Text("औसत प्राप्तांक %", style = MaterialTheme.typography.labelSmall)
                                        }
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("${allStudents.size}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                            Text("पंजीकृत कैडेट्स", style = MaterialTheme.typography.labelSmall)
                                        }
                                    }
                                }
                            }
                        }

                        item {
                            Text("हाल ही में दिए गए टेस्ट स्कोर (${allTestAttempts.size})", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                        }

                        if (allTestAttempts.isEmpty()) {
                            item {
                                Surface(
                                    color = MaterialTheme.colorScheme.surface,
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(24.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(Icons.Default.Quiz, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(36.dp))
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text("कैडेट्स द्वारा अभी कोई मॉक टेस्ट नहीं दिया गया है।", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        } else {
                            items(allTestAttempts) { attempt ->
                                val student = allStudents.find { it.studentId == attempt.studentId }
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
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = student?.fullName ?: "कैडेट (${attempt.studentId})",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "टेस्ट: ${attempt.testId} • दिनांक: ${attempt.date}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = "सही: ${attempt.score} | गलत: ${attempt.wrongCount} | समय: ${attempt.timeTakenSeconds / 60}m",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }

                                        Surface(
                                            color = if (attempt.accuracyPercentage >= 60) OliveTertiary.copy(alpha = 0.15f) else Color(0xFFDC2626).copy(alpha = 0.15f),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Text(
                                                    text = "${attempt.accuracyPercentage}%",
                                                    fontWeight = FontWeight.Bold,
                                                    style = MaterialTheme.typography.titleMedium,
                                                    color = if (attempt.accuracyPercentage >= 60) OliveTertiary else Color(0xFFDC2626)
                                                )
                                                Text(
                                                    text = if (attempt.accuracyPercentage >= 60) "पास (Pass)" else "सुधार योग्य",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontSize = 10.sp,
                                                    color = if (attempt.accuracyPercentage >= 60) OliveTertiary else Color(0xFFDC2626)
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

    // Add Question Dialog
    if (showAddQuestionDialog) {
        var qText by remember { mutableStateOf("") }
        var optA by remember { mutableStateOf("") }
        var optB by remember { mutableStateOf("") }
        var optC by remember { mutableStateOf("") }
        var optD by remember { mutableStateOf("") }
        var correctOpt by remember { mutableStateOf("A") }
        var explanation by remember { mutableStateOf("") }
        var selectedSubjectId by remember { mutableStateOf(allSubjects.firstOrNull()?.subjectId ?: "SUB_MATH") }
        var selectedDifficulty by remember { mutableStateOf("मध्यम") }

        AlertDialog(
            onDismissRequest = { showAddQuestionDialog = false },
            title = { Text("नया प्रश्न जोड़ें (Add Question)", fontWeight = FontWeight.Bold) },
            text = {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        OutlinedTextField(
                            value = qText,
                            onValueChange = { qText = it },
                            label = { Text("प्रश्न (Question)") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2
                        )
                    }
                    item {
                        OutlinedTextField(value = optA, onValueChange = { optA = it }, label = { Text("विकल्प A") }, modifier = Modifier.fillMaxWidth())
                    }
                    item {
                        OutlinedTextField(value = optB, onValueChange = { optB = it }, label = { Text("विकल्प B") }, modifier = Modifier.fillMaxWidth())
                    }
                    item {
                        OutlinedTextField(value = optC, onValueChange = { optC = it }, label = { Text("विकल्प C") }, modifier = Modifier.fillMaxWidth())
                    }
                    item {
                        OutlinedTextField(value = optD, onValueChange = { optD = it }, label = { Text("विकल्प D") }, modifier = Modifier.fillMaxWidth())
                    }
                    item {
                        Text("सही विकल्प चुनें (Correct Option):", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("A", "B", "C", "D").forEach { opt ->
                                FilterChip(
                                    selected = correctOpt == opt,
                                    onClick = { correctOpt = opt },
                                    label = { Text(opt, fontWeight = FontWeight.Bold) }
                                )
                            }
                        }
                    }
                    item {
                        OutlinedTextField(
                            value = explanation,
                            onValueChange = { explanation = it },
                            label = { Text("व्याख्या / हल (Explanation)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (qText.isNotBlank() && optA.isNotBlank() && optB.isNotBlank()) {
                            val correctIndex = when (correctOpt) {
                                "A" -> 0
                                "B" -> 1
                                "C" -> 2
                                "D" -> 3
                                else -> 0
                            }
                            val newQ = Question(
                                questionId = "Q_CUSTOM_${System.currentTimeMillis()}",
                                subjectId = selectedSubjectId,
                                topicId = "TOPIC_CUSTOM",
                                questionText = qText.trim(),
                                optionA = optA.trim(),
                                optionB = optB.trim(),
                                optionC = optC.trim().ifEmpty { "None" },
                                optionD = optD.trim().ifEmpty { "None" },
                                correctOption = correctIndex,
                                correctOptionLetter = correctOpt,
                                explanation = explanation.trim(),
                                difficulty = selectedDifficulty,
                                isActive = true
                            )
                            onAddQuestion(newQ)
                            showAddQuestionDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary)
                ) {
                    Text("सहेजें (Save)")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddQuestionDialog = false }) { Text("रद्द करें") }
            }
        )
    }

    // Add Chapter Dialog
    if (showAddChapterDialog) {
        var subjectName by remember { mutableStateOf("सामान्य ज्ञान") }
        var chapterName by remember { mutableStateOf("") }
        var desc by remember { mutableStateOf("") }
        var pdfLink by remember { mutableStateOf("") }
        var videoLink by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddChapterDialog = false },
            title = { Text("नया अध्याय व नोट्स जोड़ें", fontWeight = FontWeight.Bold) },
            text = {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        OutlinedTextField(value = subjectName, onValueChange = { subjectName = it }, label = { Text("विषय का नाम (Subject)") }, modifier = Modifier.fillMaxWidth())
                    }
                    item {
                        OutlinedTextField(value = chapterName, onValueChange = { chapterName = it }, label = { Text("अध्याय का नाम (Chapter Name)") }, modifier = Modifier.fillMaxWidth())
                    }
                    item {
                        OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("अध्याय विवरण व सारांश") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
                    }
                    item {
                        OutlinedTextField(value = pdfLink, onValueChange = { pdfLink = it }, label = { Text("PDF नोट्स फ़ाइल लिंक (वैकल्पिक)") }, modifier = Modifier.fillMaxWidth())
                    }
                    item {
                        OutlinedTextField(value = videoLink, onValueChange = { videoLink = it }, label = { Text("वीडियो क्लास URL (वैकल्पिक)") }, modifier = Modifier.fillMaxWidth())
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (chapterName.isNotBlank() && subjectName.isNotBlank()) {
                            val newCh = Chapter(
                                subjectName = subjectName.trim(),
                                chapterName = chapterName.trim(),
                                description = desc.trim(),
                                fileUrl = pdfLink.trim(),
                                videoUrl = videoLink.trim(),
                                isPublished = true
                            )
                            onAddChapter(newCh)
                            showAddChapterDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = OliveTertiary)
                ) {
                    Text("जोड़ें (Save)")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddChapterDialog = false }) { Text("रद्द करें") }
            }
        )
    }

    // Delete Confirmation Dialog for Question
    questionToDelete?.let { q ->
        AlertDialog(
            onDismissRequest = { questionToDelete = null },
            title = { Text("प्रश्न हटाएं?", fontWeight = FontWeight.Bold) },
            text = { Text("क्या आप इस प्रश्न को प्रश्न बैंक से हटाना चाहते हैं?") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteQuestion(q)
                        questionToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusAbsent)
                ) {
                    Text("हटाएं (Delete)")
                }
            },
            dismissButton = {
                TextButton(onClick = { questionToDelete = null }) { Text("रद्द करें") }
            }
        )
    }

    // Delete Confirmation Dialog for Chapter
    chapterToDelete?.let { ch ->
        AlertDialog(
            onDismissRequest = { chapterToDelete = null },
            title = { Text("अध्याय हटाएं?", fontWeight = FontWeight.Bold) },
            text = { Text("क्या आप '${ch.chapterName}' अध्याय को हटाना चाहते हैं?") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteChapter(ch)
                        chapterToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusAbsent)
                ) {
                    Text("हटाएं (Delete)")
                }
            },
            dismissButton = {
                TextButton(onClick = { chapterToDelete = null }) { Text("रद्द करें") }
            }
        )
    }
}
