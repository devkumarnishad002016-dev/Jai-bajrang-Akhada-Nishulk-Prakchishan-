package com.example.ui.screens.study

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Question
import com.example.data.model.StudySubject
import com.example.data.model.StudyTopic
import com.example.ui.theme.*

/**
 * State class for managing interactive practice quiz session.
 */
data class PracticeQuizState(
    val subject: StudySubject? = null,
    val topic: StudyTopic? = null,
    val questions: List<Question> = emptyList(),
    val currentIndex: Int = 0,
    val selectedAnswers: Map<Int, String> = emptyMap(), // questionIndex -> "A", "B", "C", "D"
    val submittedQuestions: Set<Int> = emptySet(), // indices of questions where answer has been submitted
    val isFinished: Boolean = false,
    val timeSpentSeconds: Int = 0
) {
    val currentQuestion: Question? get() = questions.getOrNull(currentIndex)
    val totalQuestions: Int get() = questions.size
    val currentAnswer: String? get() = selectedAnswers[currentIndex]
    val isCurrentSubmitted: Boolean get() = submittedQuestions.contains(currentIndex)

    val correctCount: Int get() = submittedQuestions.count { idx ->
        val q = questions.getOrNull(idx) ?: return@count false
        val ans = selectedAnswers[idx] ?: return@count false
        ans.equals(q.correctLetter, ignoreCase = true)
    }

    val incorrectCount: Int get() = submittedQuestions.count { idx ->
        val q = questions.getOrNull(idx) ?: return@count false
        val ans = selectedAnswers[idx] ?: return@count false
        !ans.equals(q.correctLetter, ignoreCase = true)
    }

    val accuracyPercentage: Int get() = if (submittedQuestions.isNotEmpty()) {
        ((correctCount * 100) / submittedQuestions.size)
    } else 0
}

/**
 * Full-screen Interactive Practice Quiz Screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PracticeQuizScreen(
    subject: StudySubject,
    topic: StudyTopic?,
    questions: List<Question>,
    onRecordAttempt: (questionId: String, subjectId: String, topicId: String, selectedAnswer: String, isCorrect: Boolean, timeTaken: Int) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var quizState by remember(subject, topic, questions) {
        mutableStateOf(
            PracticeQuizState(
                subject = subject,
                topic = topic,
                questions = questions
            )
        )
    }

    var showExitDialog by remember { mutableStateOf(false) }

    if (quizState.isFinished) {
        PracticeQuizResultScreen(
            state = quizState,
            onReattempt = {
                quizState = PracticeQuizState(
                    subject = subject,
                    topic = topic,
                    questions = questions.shuffled()
                )
            },
            onBackToDashboard = onBack,
            modifier = modifier
        )
        return
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("practice_quiz_screen"),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "${subject.icon} ${subject.name}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        Text(
                            text = topic?.topicName ?: "संपूर्ण विषय अभ्यास (Subject Practice)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { showExitDialog = true },
                        modifier = Modifier.testTag("quiz_back_btn")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Real-time live score counters
                    Row(
                        modifier = Modifier.padding(end = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            color = StatusPresent.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = StatusPresent, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = "${quizState.correctCount}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = StatusPresent,
                                    modifier = Modifier.testTag("quiz_correct_count")
                                )
                            }
                        }

                        Surface(
                            color = StatusAbsent.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Close, contentDescription = null, tint = StatusAbsent, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = "${quizState.incorrectCount}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = StatusAbsent,
                                    modifier = Modifier.testTag("quiz_incorrect_count")
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = {
            PracticeQuizBottomBar(
                state = quizState,
                onPrevious = {
                    if (quizState.currentIndex > 0) {
                        quizState = quizState.copy(currentIndex = quizState.currentIndex - 1)
                    }
                },
                onSubmitCurrent = {
                    val currentQ = quizState.currentQuestion
                    val selectedAns = quizState.currentAnswer
                    if (currentQ != null && selectedAns != null && !quizState.isCurrentSubmitted) {
                        val isCorrect = selectedAns.equals(currentQ.correctLetter, ignoreCase = true)
                        onRecordAttempt(
                            currentQ.questionId,
                            currentQ.subjectId.ifEmpty { subject.subjectId },
                            currentQ.topicId.ifEmpty { topic?.topicId ?: "" },
                            selectedAns,
                            isCorrect,
                            15
                        )
                        quizState = quizState.copy(
                            submittedQuestions = quizState.submittedQuestions + quizState.currentIndex
                        )
                    }
                },
                onNext = {
                    if (quizState.currentIndex < quizState.totalQuestions - 1) {
                        quizState = quizState.copy(currentIndex = quizState.currentIndex + 1)
                    } else {
                        quizState = quizState.copy(isFinished = true)
                    }
                }
            )
        }
    ) { innerPadding ->
        if (questions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                    Text("📚", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "इस विषय/टॉपिक में कोई प्रश्न उपलब्ध नहीं है।",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "कृपया एडमिन पैनल से नए प्रश्न जोड़ें या अन्य विषय चुनें।",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onBack,
                        colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary)
                    ) {
                        Text("वापस जाएं")
                    }
                }
            }
        } else {
            PracticeQuizQuestionContent(
                state = quizState,
                onSelectOption = { letter ->
                    if (!quizState.isCurrentSubmitted) {
                        val newAnswers = quizState.selectedAnswers.toMutableMap()
                        newAnswers[quizState.currentIndex] = letter
                        quizState = quizState.copy(selectedAnswers = newAnswers)
                    }
                },
                onNavigateQuestion = { idx ->
                    quizState = quizState.copy(currentIndex = idx)
                },
                modifier = Modifier.padding(innerPadding)
            )
        }
    }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text("अभ्यास छोड़ें?", fontWeight = FontWeight.Bold) },
            text = { Text("क्या आप इस अभ्यास सत्र को छोड़ना चाहते हैं? आपका अब तक का हल किया गया डेटा सुरक्षित रहेगा।") },
            confirmButton = {
                Button(
                    onClick = {
                        showExitDialog = false
                        onBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusAbsent)
                ) {
                    Text("हां, बाहर निकलें")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) {
                    Text("अभ्यास जारी रखें")
                }
            }
        )
    }
}

/**
 * Question content view with options and instant explanation.
 */
@Composable
fun PracticeQuizQuestionContent(
    state: PracticeQuizState,
    onSelectOption: (String) -> Unit,
    onNavigateQuestion: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentQ = state.currentQuestion ?: return
    val selectedOption = state.currentAnswer
    val isSubmitted = state.isCurrentSubmitted

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Progress Bar & Step Indicator
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "प्रश्न ${state.currentIndex + 1} / ${state.totalQuestions}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = SaffronPrimary,
                        modifier = Modifier.testTag("quiz_progress_text")
                    )
                    Text(
                        text = "कठिनाई: ${currentQ.difficulty}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                LinearProgressIndicator(
                    progress = { ((state.currentIndex + 1).toFloat() / state.totalQuestions.toFloat()).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .testTag("quiz_progress_bar"),
                    color = SaffronPrimary,
                    trackColor = SaffronPrimary.copy(alpha = 0.15f)
                )
            }
        }

        // 2. Question Number Strip Palette
        item {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(state.totalQuestions) { qIdx ->
                    val isCurr = qIdx == state.currentIndex
                    val isSub = state.submittedQuestions.contains(qIdx)
                    val qItem = state.questions.getOrNull(qIdx)
                    val ans = state.selectedAnswers[qIdx]
                    val isCorrect = isSub && qItem != null && ans != null && ans.equals(qItem.correctLetter, ignoreCase = true)
                    val isWrong = isSub && qItem != null && ans != null && !ans.equals(qItem.correctLetter, ignoreCase = true)

                    val pillColor = when {
                        isCorrect -> StatusPresent
                        isWrong -> StatusAbsent
                        isCurr -> SaffronPrimary
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }

                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(pillColor)
                            .border(
                                width = if (isCurr) 2.dp else 0.dp,
                                color = if (isCurr) Color.Black.copy(alpha = 0.4f) else Color.Transparent,
                                shape = CircleShape
                            )
                            .clickable { onNavigateQuestion(qIdx) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${qIdx + 1}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isCorrect || isWrong || isCurr) Color.White else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // 3. Question Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("practice_question_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Surface(
                        color = NavySecondary.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = if (currentQ.chapterName.isNotBlank()) currentQ.chapterName else currentQ.subjectName,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = NavySecondary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = currentQ.questionText,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 24.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // 4. Four MCQ Options (A, B, C, D)
        val optionList = listOf(
            "A" to currentQ.optionA,
            "B" to currentQ.optionB,
            "C" to currentQ.optionC,
            "D" to currentQ.optionD
        )

        items(optionList) { (letter, text) ->
            val isSelected = selectedOption.equals(letter, ignoreCase = true)
            val isCorrectOption = currentQ.correctLetter.equals(letter, ignoreCase = true)

            val (bgColor, borderColor, textColor) = when {
                !isSubmitted && isSelected -> Triple(
                    SaffronPrimary.copy(alpha = 0.15f),
                    SaffronPrimary,
                    SaffronPrimary
                )
                isSubmitted && isCorrectOption -> Triple(
                    StatusPresent.copy(alpha = 0.18f),
                    StatusPresent,
                    StatusPresent
                )
                isSubmitted && isSelected && !isCorrectOption -> Triple(
                    StatusAbsent.copy(alpha = 0.18f),
                    StatusAbsent,
                    StatusAbsent
                )
                else -> Triple(
                    MaterialTheme.colorScheme.surface,
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f),
                    MaterialTheme.colorScheme.onSurface
                )
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("quiz_option_$letter")
                    .clickable(enabled = !isSubmitted) { onSelectOption(letter) },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = bgColor),
                border = androidx.compose.foundation.BorderStroke(
                    width = if (isSelected || (isSubmitted && isCorrectOption)) 2.dp else 1.dp,
                    color = borderColor
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(borderColor.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = letter,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = text,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (isSelected || (isSubmitted && isCorrectOption)) FontWeight.Bold else FontWeight.Normal,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )

                    if (isSubmitted) {
                        if (isCorrectOption) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Correct",
                                tint = StatusPresent,
                                modifier = Modifier.size(22.dp)
                            )
                        } else if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Cancel,
                                contentDescription = "Incorrect",
                                tint = StatusAbsent,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    } else if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.RadioButtonChecked,
                            contentDescription = "Selected",
                            tint = SaffronPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // 5. Instant Explanation Box (Visible after submission)
        if (isSubmitted) {
            val isUserCorrect = selectedOption.equals(currentQ.correctLetter, ignoreCase = true)

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("quiz_explanation_card"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isUserCorrect) StatusPresent.copy(alpha = 0.12f) else StatusAbsent.copy(alpha = 0.12f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isUserCorrect) Icons.Default.CheckCircle else Icons.Default.Info,
                                contentDescription = null,
                                tint = if (isUserCorrect) StatusPresent else StatusAbsent,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isUserCorrect) "उत्कृष्ट! सही उत्तर (Correct)" else "गलत उत्तर! (Correct Option: ${currentQ.correctLetter})",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isUserCorrect) StatusPresent else StatusAbsent
                            )
                        }

                        if (currentQ.explanation.isNotBlank()) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                            Text(
                                text = "💡 व्याख्या (Explanation):",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = currentQ.explanation,
                                style = MaterialTheme.typography.bodyMedium,
                                lineHeight = 22.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Bottom navigation bar with Prev, Submit Answer, Next Question buttons.
 */
@Composable
fun PracticeQuizBottomBar(
    state: PracticeQuizState,
    onPrevious: () -> Unit,
    onSubmitCurrent: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Previous Button
            OutlinedButton(
                onClick = onPrevious,
                enabled = state.currentIndex > 0,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("quiz_prev_btn")
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("पिछला")
            }

            // Middle Action: Submit or Next
            if (!state.isCurrentSubmitted) {
                Button(
                    onClick = onSubmitCurrent,
                    enabled = state.currentAnswer != null,
                    colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("quiz_submit_answer_btn")
                ) {
                    Icon(Icons.Default.Done, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("उत्तर जांचें (Submit)")
                }
            } else {
                Button(
                    onClick = onNext,
                    colors = ButtonDefaults.buttonColors(containerColor = OliveTertiary),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("quiz_next_btn")
                ) {
                    Text(
                        if (state.currentIndex < state.totalQuestions - 1) "अगला प्रश्न" else "परिणाम देखें 🏁"
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next", modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

/**
 * Result Screen at the completion of Practice Quiz session.
 */
@Composable
fun PracticeQuizResultScreen(
    state: PracticeQuizState,
    onReattempt: () -> Unit,
    onBackToDashboard: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .testTag("quiz_result_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        color = SaffronPrimary.copy(alpha = 0.15f),
                        shape = CircleShape,
                        modifier = Modifier.size(72.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = null,
                                tint = SaffronPrimary,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "अभ्यास सत्र पूर्ण हुआ! 🎉",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = "${state.subject?.name ?: "विषय"} • ${state.topic?.topicName ?: "सभी टॉपिक्स"}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Accuracy Big Badge
                    Text(
                        text = "${state.accuracyPercentage}%",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (state.accuracyPercentage >= 70) StatusPresent else SaffronPrimary,
                        modifier = Modifier.testTag("result_accuracy_text")
                    )

                    Text(
                        text = "सटीकता दर (Accuracy Rate)",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Stats row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        QuizSummaryStatPill("कुल प्रश्न", "${state.totalQuestions}", MaterialTheme.colorScheme.onSurface)
                        QuizSummaryStatPill("सही (Correct)", "${state.correctCount}", StatusPresent)
                        QuizSummaryStatPill("गलत (Wrong)", "${state.incorrectCount}", StatusAbsent)
                        val unattempted = state.totalQuestions - state.submittedQuestions.size
                        QuizSummaryStatPill("छूटे हुए", "$unattempted", MaterialTheme.colorScheme.outline)
                    }
                }
            }
        }

        // Performance evaluation message
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (state.accuracyPercentage >= 75) StatusPresent.copy(alpha = 0.12f)
                    else SaffronPrimary.copy(alpha = 0.12f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (state.accuracyPercentage >= 75) Icons.Default.Stars else Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = if (state.accuracyPercentage >= 75) StatusPresent else SaffronPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = if (state.accuracyPercentage >= 75) "उत्कृष्ट प्रदर्शन (Excellent)" else "नियमित अभ्यास से और सुधार करें",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (state.accuracyPercentage >= 75) StatusPresent else SaffronPrimary
                        )
                        Text(
                            text = if (state.accuracyPercentage >= 75)
                                "आपकी सटीकता सेना/पुलिस परीक्षा स्तर के अनुकूल है। रिवीजन जारी रखें।"
                            else
                                "कमजोर टॉपिक्स के नोट्स पुनः पढ़ें और फॉर्मूलों का दोबारा अभ्यास करें।",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Action Buttons
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onReattempt,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors()
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("पुनः अभ्यास करें")
                }

                Button(
                    onClick = onBackToDashboard,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Dashboard, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("डैशबोर्ड पर जाएं")
                }
            }
        }
    }
}

@Composable
fun QuizSummaryStatPill(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = color)
        Text(text = label, style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
