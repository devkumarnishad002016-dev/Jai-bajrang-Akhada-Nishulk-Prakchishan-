package com.example.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminQuestionBankScreen(
    allQuestions: List<Question>,
    allSubjects: List<StudySubject>,
    allTopics: List<StudyTopic>,
    onAddQuestion: (Question) -> Unit,
    onUpdateQuestion: (Question) -> Unit,
    onDeleteQuestion: (Question) -> Unit,
    onToggleActive: (Question) -> Unit,
    onAddSubject: (StudySubject) -> Unit,
    onAddTopic: (StudyTopic) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedSubjectId by remember { mutableStateOf<String?>(null) }
    var selectedTopicId by remember { mutableStateOf<String?>(null) }
    var selectedDifficulty by remember { mutableStateOf<String?>(null) }
    var showOnlyActive by remember { mutableStateOf(false) }

    var showAddQuestionDialog by remember { mutableStateOf(false) }
    var questionToEdit by remember { mutableStateOf<Question?>(null) }
    var questionToDelete by remember { mutableStateOf<Question?>(null) }
    var showSubjectTopicDialog by remember { mutableStateOf(false) }

    val availableTopics = remember(selectedSubjectId, allTopics) {
        if (selectedSubjectId == null) allTopics else allTopics.filter { it.subjectId == selectedSubjectId }
    }

    val filteredQuestions = remember(allQuestions, searchQuery, selectedSubjectId, selectedTopicId, selectedDifficulty, showOnlyActive) {
        allQuestions.filter { q ->
            val matchesSearch = searchQuery.isBlank() ||
                q.questionText.contains(searchQuery, ignoreCase = true) ||
                q.explanation.contains(searchQuery, ignoreCase = true) ||
                q.questionId.contains(searchQuery, ignoreCase = true)
            val matchesSubject = selectedSubjectId == null || q.subjectId == selectedSubjectId
            val matchesTopic = selectedTopicId == null || q.topicId == selectedTopicId
            val matchesDifficulty = selectedDifficulty == null || q.difficulty == selectedDifficulty
            val matchesActive = !showOnlyActive || q.isActive
            matchesSearch && matchesSubject && matchesTopic && matchesDifficulty && matchesActive
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize().testTag("admin_question_bank_screen"),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("प्रश्न बैंक प्रबंधन (Question Bank)", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("कुल प्रश्न: ${allQuestions.size} | प्रदर्शित: ${filteredQuestions.size}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("admin_qb_back_button")) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showSubjectTopicDialog = true }, modifier = Modifier.testTag("admin_manage_subjects_button")) {
                        Icon(imageVector = Icons.Default.Category, contentDescription = "Manage Subjects/Topics", tint = SaffronPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddQuestionDialog = true },
                containerColor = SaffronPrimary,
                contentColor = Color.White,
                icon = { Icon(Icons.Default.Add, contentDescription = "Add") },
                text = { Text("नया प्रश्न जोड़ें", fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("admin_add_question_fab")
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Search field
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth().testTag("admin_qb_search_input"),
                    placeholder = { Text("प्रश्न, ID या व्याख्या खोजें...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear")
                            }
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }

            // Subject Filter Chips
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("विषय अनुसार फ़िल्टर (Filter by Subject):", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        item {
                            FilterChip(
                                selected = selectedSubjectId == null,
                                onClick = {
                                    selectedSubjectId = null
                                    selectedTopicId = null
                                },
                                label = { Text("सभी विषय (${allSubjects.size})") },
                                modifier = Modifier.testTag("admin_qb_filter_all_subjects")
                            )
                        }
                        items(allSubjects) { subject ->
                            FilterChip(
                                selected = selectedSubjectId == subject.subjectId,
                                onClick = {
                                    selectedSubjectId = if (selectedSubjectId == subject.subjectId) null else subject.subjectId
                                    selectedTopicId = null
                                },
                                label = { Text("${subject.icon} ${subject.name}") },
                                modifier = Modifier.testTag("admin_qb_filter_subject_${subject.subjectId}")
                            )
                        }
                    }
                }
            }

            // Topic Filter Chips (if available)
            if (availableTopics.isNotEmpty()) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("टॉपिक फ़िल्टर (Filter by Topic):", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            item {
                                FilterChip(
                                    selected = selectedTopicId == null,
                                    onClick = { selectedTopicId = null },
                                    label = { Text("सभी टॉपिक") },
                                    modifier = Modifier.testTag("admin_qb_filter_all_topics")
                                )
                            }
                            items(availableTopics) { topic ->
                                FilterChip(
                                    selected = selectedTopicId == topic.topicId,
                                    onClick = {
                                        selectedTopicId = if (selectedTopicId == topic.topicId) null else topic.topicId
                                    },
                                    label = { Text(topic.topicName) },
                                    modifier = Modifier.testTag("admin_qb_filter_topic_${topic.topicId}")
                                )
                            }
                        }
                    }
                }
            }

            // Difficulty & Active status row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                        listOf("सभी", "आसान", "मध्यम", "कठिन").forEach { diff ->
                            val isSelected = (diff == "सभी" && selectedDifficulty == null) || selectedDifficulty == diff
                            SuggestionChip(
                                onClick = { selectedDifficulty = if (diff == "सभी") null else diff },
                                label = { Text(diff, style = MaterialTheme.typography.labelSmall) },
                                colors = SuggestionChipDefaults.suggestionChipColors(
                                    containerColor = if (isSelected) SaffronPrimary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface
                                ),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) SaffronPrimary else MaterialTheme.colorScheme.outlineVariant
                                )
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = showOnlyActive,
                            onCheckedChange = { showOnlyActive = it },
                            modifier = Modifier.testTag("admin_qb_active_only_checkbox")
                        )
                        Text("सक्रिय ही", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            // Empty state
            if (filteredQuestions.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(top = 20.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.Quiz, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("कोई प्रश्न नहीं मिला", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            Text("कृपया फ़िल्टर बदलें या नया प्रश्न जोड़ें।", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            // Questions list
            items(filteredQuestions, key = { it.id }) { q ->
                QuestionAdminCard(
                    question = q,
                    onEdit = { questionToEdit = q },
                    onDelete = { questionToDelete = q },
                    onToggleActive = { onToggleActive(q) }
                )
            }
        }
    }

    // Add Question Dialog
    if (showAddQuestionDialog) {
        QuestionFormDialog(
            title = "नया प्रश्न जोड़ें (Add Question)",
            allSubjects = allSubjects,
            allTopics = allTopics,
            initialQuestion = null,
            onDismiss = { showAddQuestionDialog = false },
            onSave = { newQ ->
                onAddQuestion(newQ)
                showAddQuestionDialog = false
            }
        )
    }

    // Edit Question Dialog
    questionToEdit?.let { q ->
        QuestionFormDialog(
            title = "प्रश्न संपादित करें (Edit Question)",
            allSubjects = allSubjects,
            allTopics = allTopics,
            initialQuestion = q,
            onDismiss = { questionToEdit = null },
            onSave = { updatedQ ->
                onUpdateQuestion(updatedQ)
                questionToEdit = null
            }
        )
    }

    // Delete Confirmation Dialog
    questionToDelete?.let { q ->
        AlertDialog(
            onDismissRequest = { questionToDelete = null },
            title = { Text("प्रश्न हटाएं (Delete Question)", fontWeight = FontWeight.Bold) },
            text = { Text("क्या आप निश्चित हैं कि आप इस प्रश्न को हटाना चाहते हैं?\n\n'${q.questionText}'") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteQuestion(q)
                        questionToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusAbsent),
                    modifier = Modifier.testTag("admin_confirm_delete_question")
                ) {
                    Text("हटाएं", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { questionToDelete = null }) { Text("रद्द करें") }
            }
        )
    }

    // Subject & Topic Quick Management Dialog
    if (showSubjectTopicDialog) {
        SubjectTopicManagementDialog(
            allSubjects = allSubjects,
            allTopics = allTopics,
            onAddSubject = onAddSubject,
            onAddTopic = onAddTopic,
            onDismiss = { showSubjectTopicDialog = false }
        )
    }
}

@Composable
fun QuestionAdminCard(
    question: Question,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleActive: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("admin_question_card_${question.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Header Tags Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = SaffronPrimary.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = question.subjectName.ifEmpty { question.subjectId },
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = SaffronPrimary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    if (question.chapterName.isNotBlank() || question.topicId.isNotBlank()) {
                        Surface(
                            color = NavySecondary.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = question.chapterName.ifEmpty { question.topicId },
                                style = MaterialTheme.typography.labelSmall,
                                color = NavySecondary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    val diffColor = when (question.difficulty) {
                        "आसान" -> StatusPresent
                        "कठिन" -> StatusAbsent
                        else -> SaffronPrimary
                    }
                    Surface(
                        color = diffColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = question.difficulty,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = diffColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (question.isActive) "सक्रिय" else "निष्क्रिय",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (question.isActive) StatusPresent else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Switch(
                        checked = question.isActive,
                        onCheckedChange = { onToggleActive() },
                        modifier = Modifier.testTag("admin_toggle_active_${question.id}")
                    )
                }
            }

            // Question Text
            Text(
                text = question.questionText,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )

            // Options
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                listOf(
                    "A" to question.optionA,
                    "B" to question.optionB,
                    "C" to question.optionC,
                    "D" to question.optionD
                ).forEach { (optLetter, optText) ->
                    val isCorrect = question.correctLetter.equals(optLetter, ignoreCase = true)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                if (isCorrect) StatusPresent.copy(alpha = 0.12f)
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(if (isCorrect) StatusPresent else MaterialTheme.colorScheme.outlineVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = optLetter,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isCorrect) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = optText,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = if (isCorrect) FontWeight.Bold else FontWeight.Normal,
                            color = if (isCorrect) StatusPresent else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Explanation if present
            if (question.explanation.isNotBlank()) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.Top) {
                        Icon(
                            imageVector = Icons.Default.Lightbulb,
                            contentDescription = "Explanation",
                            tint = SaffronPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "व्याख्या: ${question.explanation}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Actions Row (Edit / Delete)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onEdit, modifier = Modifier.testTag("admin_edit_question_${question.id}")) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", tint = NavySecondary)
                }
                IconButton(onClick = onDelete, modifier = Modifier.testTag("admin_delete_question_${question.id}")) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = StatusAbsent)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestionFormDialog(
    title: String,
    allSubjects: List<StudySubject>,
    allTopics: List<StudyTopic>,
    initialQuestion: Question?,
    onDismiss: () -> Unit,
    onSave: (Question) -> Unit
) {
    var qText by remember { mutableStateOf(initialQuestion?.questionText ?: "") }
    var optA by remember { mutableStateOf(initialQuestion?.optionA ?: "") }
    var optB by remember { mutableStateOf(initialQuestion?.optionB ?: "") }
    var optC by remember { mutableStateOf(initialQuestion?.optionC ?: "") }
    var optD by remember { mutableStateOf(initialQuestion?.optionD ?: "") }
    var correctOpt by remember { mutableStateOf(initialQuestion?.correctLetter ?: "A") }
    var expl by remember { mutableStateOf(initialQuestion?.explanation ?: "") }
    var diff by remember { mutableStateOf(initialQuestion?.difficulty ?: "मध्यम") }
    var lang by remember { mutableStateOf(initialQuestion?.language ?: "Hindi") }
    var isAct by remember { mutableStateOf(initialQuestion?.isActive ?: true) }

    var selectedSubId by remember {
        mutableStateOf(
            initialQuestion?.subjectId?.ifEmpty { null }
                ?: allSubjects.firstOrNull()?.subjectId ?: "SUB_MATH"
        )
    }

    val availableTopics = remember(selectedSubId, allTopics) {
        allTopics.filter { it.subjectId == selectedSubId }
    }

    var selectedTopId by remember {
        mutableStateOf(
            initialQuestion?.topicId?.ifEmpty { null }
                ?: availableTopics.firstOrNull()?.topicId ?: "TOPIC_MATH_NUMSYS"
        )
    }

    var validationError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().heightIn(max = 500.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Validation Error Banner
                validationError?.let { err ->
                    item {
                        Surface(
                            color = StatusAbsent.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "⚠️ $err",
                                color = StatusAbsent,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                }

                // Subject Selection Chips
                item {
                    Text("विषय (Subject) चयन करें:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        allSubjects.forEach { sub ->
                            FilterChip(
                                selected = selectedSubId == sub.subjectId,
                                onClick = {
                                    selectedSubId = sub.subjectId
                                    selectedTopId = allTopics.firstOrNull { it.subjectId == sub.subjectId }?.topicId ?: ""
                                },
                                label = { Text("${sub.icon} ${sub.name}") }
                            )
                        }
                    }
                }

                // Topic Selection Chips
                if (availableTopics.isNotEmpty()) {
                    item {
                        Text("टॉपिक (Topic) चयन करें:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            availableTopics.forEach { top ->
                                FilterChip(
                                    selected = selectedTopId == top.topicId,
                                    onClick = { selectedTopId = top.topicId },
                                    label = { Text(top.topicName) }
                                )
                            }
                        }
                    }
                }

                // Question Text
                item {
                    OutlinedTextField(
                        value = qText,
                        onValueChange = { qText = it; validationError = null },
                        label = { Text("प्रश्न विवरण (Question Text) *") },
                        modifier = Modifier.fillMaxWidth().testTag("admin_input_question_text"),
                        minLines = 2
                    )
                }

                // Options A & B
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = optA,
                            onValueChange = { optA = it; validationError = null },
                            label = { Text("Option A *") },
                            modifier = Modifier.weight(1f).testTag("admin_input_option_a")
                        )
                        OutlinedTextField(
                            value = optB,
                            onValueChange = { optB = it; validationError = null },
                            label = { Text("Option B *") },
                            modifier = Modifier.weight(1f).testTag("admin_input_option_b")
                        )
                    }
                }

                // Options C & D
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = optC,
                            onValueChange = { optC = it; validationError = null },
                            label = { Text("Option C *") },
                            modifier = Modifier.weight(1f).testTag("admin_input_option_c")
                        )
                        OutlinedTextField(
                            value = optD,
                            onValueChange = { optD = it; validationError = null },
                            label = { Text("Option D *") },
                            modifier = Modifier.weight(1f).testTag("admin_input_option_d")
                        )
                    }
                }

                // Correct Option Selector
                item {
                    Text("सही उत्तर (Correct Option):", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        listOf("A", "B", "C", "D").forEach { opt ->
                            val isSelected = correctOpt.equals(opt, ignoreCase = true)
                            FilterChip(
                                selected = isSelected,
                                onClick = { correctOpt = opt; validationError = null },
                                label = { Text("विकल्प $opt", fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                modifier = Modifier.testTag("admin_select_correct_opt_$opt")
                            )
                        }
                    }
                }

                // Difficulty Selector
                item {
                    Text("कठिनाई स्तर (Difficulty):", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("आसान", "मध्यम", "कठिन").forEach { level ->
                            FilterChip(
                                selected = diff == level,
                                onClick = { diff = level },
                                label = { Text(level) },
                                modifier = Modifier.testTag("admin_select_diff_$level")
                            )
                        }
                    }
                }

                // Language Selector
                item {
                    Text("भाषा (Language):", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Hindi", "English", "Bilingual").forEach { l ->
                            FilterChip(
                                selected = lang == l,
                                onClick = { lang = l },
                                label = { Text(l) }
                            )
                        }
                    }
                }

                // Explanation
                item {
                    OutlinedTextField(
                        value = expl,
                        onValueChange = { expl = it },
                        label = { Text("विस्तृत व्याख्या (Explanation)") },
                        modifier = Modifier.fillMaxWidth().testTag("admin_input_explanation"),
                        minLines = 2
                    )
                }

                // Active Switch
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = isAct, onCheckedChange = { isAct = it })
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("सक्रिय रखें (Active in Quiz & Study)")
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val validation = QuestionValidator.validate(
                        questionText = qText,
                        optionA = optA,
                        optionB = optB,
                        optionC = optC,
                        optionD = optD,
                        correctOption = correctOpt,
                        subjectId = selectedSubId,
                        topicId = selectedTopId
                    )
                    if (!validation.isValid) {
                        validationError = validation.errorMessage
                        return@Button
                    }

                    val chosenSubject = allSubjects.find { it.subjectId == selectedSubId }
                    val chosenTopic = allTopics.find { it.topicId == selectedTopId }

                    val qId = initialQuestion?.questionId?.ifEmpty { null }
                        ?: "Q_${selectedSubId.replace("SUB_", "")}_${System.currentTimeMillis().toString().takeLast(4)}"

                    val q = Question(
                        id = initialQuestion?.id ?: 0,
                        questionId = qId,
                        subjectId = selectedSubId,
                        topicId = selectedTopId,
                        chapterId = initialQuestion?.chapterId ?: 0,
                        subjectName = chosenSubject?.name ?: selectedSubId,
                        chapterName = chosenTopic?.topicName ?: selectedTopId,
                        questionText = qText.trim(),
                        optionA = optA.trim(),
                        optionB = optB.trim(),
                        optionC = optC.trim(),
                        optionD = optD.trim(),
                        correctOption = when (correctOpt.uppercase()) {
                            "A" -> 0
                            "B" -> 1
                            "C" -> 2
                            "D" -> 3
                            else -> 0
                        },
                        correctOptionLetter = correctOpt.uppercase(),
                        explanation = expl.trim(),
                        difficulty = diff,
                        language = lang,
                        isActive = isAct,
                        createdDate = initialQuestion?.createdDate?.ifEmpty { null }
                            ?: SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                    )
                    onSave(q)
                },
                colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary),
                modifier = Modifier.testTag("admin_save_question_button")
            ) {
                Text("सुरक्षित करें")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("रद्द करें") }
        }
    )
}

@Composable
fun SubjectTopicManagementDialog(
    allSubjects: List<StudySubject>,
    allTopics: List<StudyTopic>,
    onAddSubject: (StudySubject) -> Unit,
    onAddTopic: (StudyTopic) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    var newSubName by remember { mutableStateOf("") }
    var newSubId by remember { mutableStateOf("") }
    var newSubIcon by remember { mutableStateOf("📚") }

    var newTopName by remember { mutableStateOf("") }
    var newTopId by remember { mutableStateOf("") }
    var selectedSubForTopic by remember { mutableStateOf(allSubjects.firstOrNull()?.subjectId ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("विषय व टॉपिक प्रबंधन", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth().heightIn(max = 450.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                TabRow(selectedTabIndex = selectedTab) {
                    Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("विषय (${allSubjects.size})") })
                    Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("टॉपिक (${allTopics.size})") })
                }

                if (selectedTab == 0) {
                    LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(allSubjects) { sub ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(8.dp).fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("${sub.icon} ${sub.name} (${sub.subjectId})", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodySmall)
                                    Text(if (sub.isActive) "सक्रिय" else "निष्क्रिय", color = if (sub.isActive) StatusPresent else MaterialTheme.colorScheme.outline, style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("नया विषय जोड़ें:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            OutlinedTextField(value = newSubName, onValueChange = { newSubName = it }, label = { Text("विषय नाम") }, modifier = Modifier.weight(1f))
                            OutlinedTextField(value = newSubIcon, onValueChange = { newSubIcon = it }, label = { Text("इमोजी") }, modifier = Modifier.width(80.dp))
                        }
                        Button(
                            onClick = {
                                if (newSubName.isNotBlank()) {
                                    val autoId = "SUB_" + newSubName.uppercase().replace(" ", "_").take(8)
                                    onAddSubject(
                                        StudySubject(
                                            subjectId = autoId,
                                            name = newSubName.trim(),
                                            icon = newSubIcon.ifBlank { "📚" },
                                            displayOrder = allSubjects.size + 1,
                                            isActive = true
                                        )
                                    )
                                    newSubName = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("विषय जोड़ें")
                        }
                    }
                } else {
                    LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(allTopics) { top ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text(top.topicName, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodySmall)
                                    Text("विषय ID: ${top.subjectId} | टॉपिक ID: ${top.topicId}", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("नया टॉपिक जोड़ें:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                        OutlinedTextField(value = newTopName, onValueChange = { newTopName = it }, label = { Text("टॉपिक नाम") }, modifier = Modifier.fillMaxWidth())
                        Button(
                            onClick = {
                                if (newTopName.isNotBlank() && selectedSubForTopic.isNotBlank()) {
                                    val autoTopId = "TOPIC_" + newTopName.uppercase().replace(" ", "_").take(8)
                                    onAddTopic(
                                        StudyTopic(
                                            topicId = autoTopId,
                                            subjectId = selectedSubForTopic,
                                            topicName = newTopName.trim(),
                                            displayOrder = allTopics.size + 1,
                                            isActive = true
                                        )
                                    )
                                    newTopName = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("टॉपिक जोड़ें")
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) { Text("पूर्ण") }
        }
    )
}
