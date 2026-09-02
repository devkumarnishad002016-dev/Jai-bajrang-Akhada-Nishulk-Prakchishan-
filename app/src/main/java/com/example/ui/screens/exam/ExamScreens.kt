package com.example.ui.screens.exam

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
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
import com.example.data.model.MockTest
import com.example.data.model.Question
import com.example.data.model.TestAttempt
import com.example.ui.theme.*

@Composable
fun MockTestScreen(
    mockTests: List<MockTest>,
    recentAttempts: List<TestAttempt>,
    onStartTest: (MockTest) -> Unit,
    onStartCustomQuiz: (subject: String, count: Int) -> Unit,
    onViewAttemptResult: (TestAttempt) -> Unit,
    modifier: Modifier = Modifier
) {
    var showCustomQuizDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("mock_test_screen"),
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
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "मॉक टेस्ट सीरीज (Mock Test Series)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Army GD • SSC GD • Police • CRPF • BSF",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Button(
                            onClick = { showCustomQuizDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(imageVector = Icons.Default.FlashOn, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("स्पीड क्विज", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }

        // Available Full Mock Tests
        item {
            Text(
                text = "उपलब्ध संपूर्ण मॉक टेस्ट (Available Full Tests)",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
        }

        items(mockTests) { test ->
            Card(
                modifier = Modifier.fillMaxWidth(),
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
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = test.title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "लक्षित परीक्षा: ${test.targetExam}",
                                style = MaterialTheme.typography.labelSmall,
                                color = SaffronDark,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Surface(
                            color = SaffronContainer,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "${test.durationMinutes} मिनट",
                                style = MaterialTheme.typography.labelSmall,
                                color = OnSaffronContainer,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${test.totalQuestions} प्रश्न • ${test.totalMarks} अंक • -${test.negativeMarking} नेगेटिव",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Button(
                            onClick = { onStartTest(test) },
                            colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("start_test_btn_${test.id}")
                        ) {
                            Text("टेस्ट शुरू करें")
                        }
                    }
                }
            }
        }

        // Recent Test Attempts & Performance History
        if (recentAttempts.isNotEmpty()) {
            item {
                Text(
                    text = "हालिया टेस्ट प्रयास परिणाम (Recent Test Attempts)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            items(recentAttempts) { attempt ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onViewAttemptResult(attempt) },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = attempt.testTitle,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "दिनांक: ${attempt.date} • परीक्षा: ${attempt.targetExam}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Surface(
                                color = OliveContainer,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "${attempt.score}/${attempt.maxScore}",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = OnOliveContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "सटीकता: ${attempt.accuracyPercentage}%",
                                style = MaterialTheme.typography.bodySmall,
                                color = OliveTertiary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "कमजोर विषय: ${attempt.weakArea}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }

    if (showCustomQuizDialog) {
        var selectedSub by remember { mutableStateOf("All") }
        var questionCount by remember { mutableIntStateOf(10) }

        AlertDialog(
            onDismissRequest = { showCustomQuizDialog = false },
            title = {
                Text("स्पीड प्रैक्टिस क्विज (Speed Quiz)", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("विषय चुनें:")
                    listOf("All", "Mathematics", "Reasoning", "GK / GS", "Hindi", "English").forEach { sub ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedSub = sub }
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(selected = selectedSub == sub, onClick = { selectedSub = sub })
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (sub == "All") "सभी विषय (All Subjects)" else sub)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showCustomQuizDialog = false
                        onStartCustomQuiz(selectedSub, questionCount)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary)
                ) {
                    Text("क्विज शुरू करें")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomQuizDialog = false }) {
                    Text("रद्द करें")
                }
            }
        )
    }
}

@Composable
fun ActiveTestScreen(
    testTitle: String,
    targetExam: String,
    questions: List<Question>,
    currentIndex: Int,
    userAnswers: Map<Int, Int>,
    markedForReview: Set<Int>,
    timeRemainingSeconds: Int,
    onSelectOption: (Int, Int) -> Unit,
    onToggleReview: (Int) -> Unit,
    onNavigateQuestion: (Int) -> Unit,
    onNextQuestion: () -> Unit,
    onPrevQuestion: () -> Unit,
    onSubmitTest: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showSubmitConfirmDialog by remember { mutableStateOf(false) }
    var showPaletteSheet by remember { mutableStateOf(false) }

    if (questions.isEmpty()) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = SaffronPrimary)
        }
        return
    }

    val currentQ = questions.getOrNull(currentIndex) ?: questions.first()
    val selectedOption = userAnswers[currentIndex]
    val isMarked = markedForReview.contains(currentIndex)

    val mins = timeRemainingSeconds / 60
    val secs = timeRemainingSeconds % 60
    val timeFormatted = String.format("%02d:%02d", mins, secs)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .testTag("active_test_screen")
    ) {
        // 1. Top Bar: Timer, Question Count, Palette Button
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 3.dp,
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = testTitle,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    Text(
                        text = "प्रश्न ${currentIndex + 1} / ${questions.size}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Timer & Palette Button
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        color = if (timeRemainingSeconds < 180) StatusAbsent.copy(alpha = 0.15f) else SaffronContainer,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = "Timer",
                                tint = if (timeRemainingSeconds < 180) StatusAbsent else SaffronPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = timeFormatted,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (timeRemainingSeconds < 180) StatusAbsent else OnSaffronContainer
                            )
                        }
                    }

                    IconButton(onClick = { showPaletteSheet = !showPaletteSheet }) {
                        Icon(
                            imageVector = Icons.Default.GridView,
                            contentDescription = "Question Palette",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // 2. Question Content Body
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Subject & Marks Pill
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = NavyContainer,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = currentQ.subjectName,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = OnNavyContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Text(
                        text = "+2.0 Marks | -0.5 Negative",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Question Text
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Text(
                        text = "Q.${currentIndex + 1}  ${currentQ.questionTextHindi}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(16.dp),
                        lineHeight = 24.sp
                    )
                }
            }

            // Options A, B, C, D
            val options = listOf(
                0 to currentQ.optionA,
                1 to currentQ.optionB,
                2 to currentQ.optionC,
                3 to currentQ.optionD
            )

            items(options) { (optIndex, optText) ->
                val isSelected = selectedOption == optIndex
                val optLetter = when (optIndex) {
                    0 -> "A"
                    1 -> "B"
                    2 -> "C"
                    3 -> "D"
                    else -> ""
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectOption(currentIndex, optIndex) },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) SaffronContainer else MaterialTheme.colorScheme.surface
                    ),
                    border = CardDefaults.outlinedCardBorder().let {
                        if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, SaffronPrimary)
                        else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    }
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
                                .background(if (isSelected) SaffronPrimary else MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = optLetter,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Text(
                            text = optText,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) OnSaffronContainer else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )

                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = SaffronPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }

        // 3. Question Palette Horizontal preview
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(questions.size) { qIdx ->
                val isAns = userAnswers.containsKey(qIdx)
                val isRev = markedForReview.contains(qIdx)
                val isCurr = qIdx == currentIndex

                val boxColor = when {
                    isRev -> Color(0xFF7C3AED)
                    isAns -> OliveTertiary
                    else -> MaterialTheme.colorScheme.surfaceVariant
                }

                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(boxColor)
                        .border(
                            if (isCurr) 2.dp else 0.dp,
                            if (isCurr) SaffronPrimary else Color.Transparent,
                            CircleShape
                        )
                        .clickable { onNavigateQuestion(qIdx) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${qIdx + 1}",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isAns || isRev) Color.White else MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // 4. Bottom Control Bar: Prev, Review, Next, Submit
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onPrevQuestion,
                    enabled = currentIndex > 0,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Prev")
                }

                IconButton(onClick = { onToggleReview(currentIndex) }) {
                    Icon(
                        imageVector = if (isMarked) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                        contentDescription = "Review",
                        tint = if (isMarked) Color(0xFF7C3AED) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = { showSubmitConfirmDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = OliveTertiary),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("जमा करें (Submit)")
                }

                Button(
                    onClick = onNextQuestion,
                    enabled = currentIndex < questions.size - 1,
                    colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next")
                }
            }
        }
    }

    if (showPaletteSheet) {
        AlertDialog(
            onDismissRequest = { showPaletteSheet = false },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("प्रश्न पैलेट (Question Palette)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    IconButton(onClick = { showPaletteSheet = false }) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Status Legend
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(OliveTertiary))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("हल किए (${userAnswers.size})", fontSize = 11.sp)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(Color(0xFF7C3AED)))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("रिव्यू (${markedForReview.size})", fontSize = 11.sp)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("छूटे (${questions.size - userAnswers.size})", fontSize = 11.sp)
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    // Grid of question buttons
                    val chunkedQuestions = questions.indices.chunked(5)
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        chunkedQuestions.forEach { rowIndices ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                rowIndices.forEach { qIdx ->
                                    val isAns = userAnswers.containsKey(qIdx)
                                    val isRev = markedForReview.contains(qIdx)
                                    val isCurr = qIdx == currentIndex

                                    val bgColor = when {
                                        isRev -> Color(0xFF7C3AED)
                                        isAns -> OliveTertiary
                                        else -> MaterialTheme.colorScheme.surfaceVariant
                                    }

                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(bgColor)
                                            .border(
                                                width = if (isCurr) 2.dp else 0.dp,
                                                color = if (isCurr) SaffronPrimary else Color.Transparent,
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .clickable {
                                                onNavigateQuestion(qIdx)
                                                showPaletteSheet = false
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "${qIdx + 1}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = if (isAns || isRev) Color.White else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPaletteSheet = false }) {
                    Text("बंद करें")
                }
            }
        )
    }

    if (showSubmitConfirmDialog) {
        val attempted = userAnswers.size
        val unattempted = questions.size - attempted

        AlertDialog(
            onDismissRequest = { showSubmitConfirmDialog = false },
            title = {
                Text("टेस्ट सबमिट करें?", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("कुल प्रश्न: ${questions.size}")
                    Text("हल किए गए (Attempted): $attempted", color = OliveTertiary, fontWeight = FontWeight.Bold)
                    Text("छूटे हुए (Unattempted): $unattempted", color = StatusAbsent, fontWeight = FontWeight.Bold)
                    Text("रिव्यू हेतु मार्क: ${markedForReview.size}", color = Color(0xFF7C3AED))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("क्या आप टेस्ट समाप्त कर परिणाम देखना चाहते हैं?")
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSubmitConfirmDialog = false
                        onSubmitTest()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary)
                ) {
                    Text("हां, सबमिट करें")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSubmitConfirmDialog = false }) {
                    Text("वापस टेस्ट पर जाएं")
                }
            }
        )
    }
}

@Composable
fun TestResultScreen(
    attempt: TestAttempt,
    questions: List<Question>,
    userAnswers: Map<Int, Int>,
    onReattempt: () -> Unit,
    onGoToDashboard: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showSolutions by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("test_result_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Result Score Header Card
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
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        color = SaffronContainer,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = attempt.testTitle,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = OnSaffronContainer,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "${attempt.score} / ${attempt.maxScore}",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = SaffronPrimary
                    )

                    Text(
                        text = "कुल प्राप्त अंक (Total Score)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Stats row: Accuracy, Time Taken, Rank
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        ResultSummaryPill("सटीकता (Accuracy)", "${attempt.accuracyPercentage}%", OliveTertiary)
                        ResultSummaryPill("समय (Time)", "${attempt.timeTakenSeconds / 60}m ${attempt.timeTakenSeconds % 60}s", NavySecondary)
                        ResultSummaryPill("अखाड़ा रैंक", "#${attempt.rank}", GoldAccent)
                        ResultSummaryPill("परसेंटाइल", "${attempt.percentile}%", Color(0xFF7C3AED))
                    }
                }
            }
        }

        // 2. Questions Attempt Breakdown
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    ResultCountBadge("सही (Correct)", attempt.correctCount, StatusPresent)
                    ResultCountBadge("गलत (Wrong)", attempt.wrongCount, StatusAbsent)
                    ResultCountBadge("छूटे (Unattempted)", attempt.unattemptedCount, MaterialTheme.colorScheme.outline)
                }
            }
        }

        // 3. Subject-wise Analysis & Weak/Strong Areas
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "विषयवार विश्लेषण (Subject Breakdown)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("गणित (Maths):", fontWeight = FontWeight.Medium)
                        Text(attempt.mathScore, fontWeight = FontWeight.Bold, color = SaffronPrimary)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("तर्कशक्ति (Reasoning):", fontWeight = FontWeight.Medium)
                        Text(attempt.reasoningScore, fontWeight = FontWeight.Bold, color = OliveTertiary)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("GK / GS व संविधान:", fontWeight = FontWeight.Medium)
                        Text(attempt.gkScore, fontWeight = FontWeight.Bold, color = NavySecondary)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("हिंदी / English:", fontWeight = FontWeight.Medium)
                        Text(attempt.hindiEnglishScore, fontWeight = FontWeight.Bold, color = Color(0xFF7C3AED))
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            color = OliveContainer.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("मजबूत क्षेत्र (Strong)", style = MaterialTheme.typography.labelSmall, color = OnOliveContainer)
                                Text(attempt.strongArea, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = OliveTertiary)
                            }
                        }

                        Surface(
                            color = StatusAbsent.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("सुधार की आवश्यकता (Weak)", style = MaterialTheme.typography.labelSmall, color = StatusAbsent)
                                Text(attempt.weakArea, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = StatusAbsent)
                            }
                        }
                    }
                }
            }
        }

        // 4. Action Buttons
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = { showSolutions = !showSolutions },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(if (showSolutions) "सॉल्यूशंस छुपाएं" else "हल व्याख्या देखें (Solutions)")
                }

                Button(
                    onClick = onGoToDashboard,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("डैशबोर्ड पर जाएं")
                }
            }
        }

        // 5. Detailed Solutions & Explanations
        if (showSolutions && questions.isNotEmpty()) {
            item {
                Text(
                    text = "विस्तृत प्रश्न हल एवं व्याख्या (Solutions)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            items(questions.size) { idx ->
                val q = questions[idx]
                val userAns = userAnswers[idx]
                val isCorrect = userAns == q.correctOption
                val isUnattempted = userAns == null

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Q.${idx + 1} (${q.subjectName})",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = SaffronPrimary
                            )

                            val (statusText, statusColor) = when {
                                isCorrect -> "सही (+2.0)" to StatusPresent
                                isUnattempted -> "छूटा (0.0)" to MaterialTheme.colorScheme.outline
                                else -> "गलत (-0.5)" to StatusAbsent
                            }

                            Text(
                                text = statusText,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = statusColor
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = q.questionTextHindi,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        val optList = listOf(q.optionA, q.optionB, q.optionC, q.optionD)
                        optList.forEachIndexed { optIdx, optTxt ->
                            val letter = when (optIdx) { 0 -> "A"; 1 -> "B"; 2 -> "C"; else -> "D" }
                            val isCorrectOpt = optIdx == q.correctOption
                            val isUserChosen = optIdx == userAns

                            val optBg = when {
                                isCorrectOpt -> StatusPresent.copy(alpha = 0.15f)
                                isUserChosen -> StatusAbsent.copy(alpha = 0.15f)
                                else -> Color.Transparent
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(optBg, RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "$letter. $optTxt",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isCorrectOpt) StatusPresent else if (isUserChosen) StatusAbsent else MaterialTheme.colorScheme.onSurface,
                                    fontWeight = if (isCorrectOpt || isUserChosen) FontWeight.Bold else FontWeight.Normal
                                )
                                if (isCorrectOpt) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("✓ सही उत्तर", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = StatusPresent)
                                }
                            }
                        }

                        if (q.explanationHindi.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "व्याख्या: ${q.explanationHindi}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ResultSummaryPill(title: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = color)
        Text(text = title, style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun ResultCountBadge(label: String, count: Int, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(color))
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = "$label: $count", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = color)
    }
}
