package com.example

import com.example.data.model.Question
import com.example.data.model.StudySubject
import com.example.data.model.StudyTopic
import com.example.data.model.TestAttempt
import com.example.ui.screens.study.PracticeQuizState
import org.junit.Assert.*
import org.junit.Test

class Phase2B2PracticeQuizAndExamTest {

    private val sampleSubject = StudySubject(
        subjectId = "SUB_MATH",
        name = "गणित (Mathematics)",
        icon = "📐"
    )

    private val sampleTopic = StudyTopic(
        topicId = "TOPIC_MATH_PERCENT",
        subjectId = "SUB_MATH",
        topicName = "प्रतिशत (Percentage)"
    )

    private val sampleQuestions = listOf(
        Question(
            questionId = "Q_MATH_P1",
            subjectId = "SUB_MATH",
            topicId = "TOPIC_MATH_PERCENT",
            questionText = "250 का 20% क्या होगा?",
            optionA = "40",
            optionB = "50",
            optionC = "60",
            optionD = "25",
            correctOption = 1,
            correctOptionLetter = "B",
            explanation = "250 * 20 / 100 = 50"
        ),
        Question(
            questionId = "Q_MATH_P2",
            subjectId = "SUB_MATH",
            topicId = "TOPIC_MATH_PERCENT",
            questionText = "यदि किसी संख्या का 30%, 60 है, तो वह संख्या क्या है?",
            optionA = "150",
            optionB = "180",
            optionC = "200",
            optionD = "220",
            correctOption = 2,
            correctOptionLetter = "C",
            explanation = "संख्या = (60 / 30) * 100 = 200"
        )
    )

    @Test
    fun testPracticeQuizState_InitialState() {
        val state = PracticeQuizState(
            subject = sampleSubject,
            topic = sampleTopic,
            questions = sampleQuestions
        )

        assertEquals(2, state.totalQuestions)
        assertEquals(0, state.currentIndex)
        assertNotNull(state.currentQuestion)
        assertEquals("250 का 20% क्या होगा?", state.currentQuestion?.questionText)
        assertEquals(0, state.correctCount)
        assertEquals(0, state.incorrectCount)
        assertFalse(state.isFinished)
    }

    @Test
    fun testPracticeQuizState_CorrectAnswerSubmission() {
        var state = PracticeQuizState(
            subject = sampleSubject,
            topic = sampleTopic,
            questions = sampleQuestions
        )

        // Select "B" (Correct)
        state = state.copy(selectedAnswers = mapOf(0 to "B"))
        assertEquals("B", state.currentAnswer)
        assertFalse(state.isCurrentSubmitted)

        // Submit current question
        state = state.copy(submittedQuestions = setOf(0))
        assertTrue(state.isCurrentSubmitted)
        assertEquals(1, state.correctCount)
        assertEquals(0, state.incorrectCount)
        assertEquals(100, state.accuracyPercentage)
    }

    @Test
    fun testPracticeQuizState_IncorrectAnswerSubmission() {
        var state = PracticeQuizState(
            subject = sampleSubject,
            topic = sampleTopic,
            questions = sampleQuestions
        )

        // Select "A" (Incorrect, correct is "B")
        state = state.copy(selectedAnswers = mapOf(0 to "A"))
        state = state.copy(submittedQuestions = setOf(0))

        assertEquals(0, state.correctCount)
        assertEquals(1, state.incorrectCount)
        assertEquals(0, state.accuracyPercentage)
    }

    @Test
    fun testPracticeQuizState_MultiQuestionProgressionAndAccuracy() {
        var state = PracticeQuizState(
            subject = sampleSubject,
            topic = sampleTopic,
            questions = sampleQuestions
        )

        // Q0: Correct ("B")
        state = state.copy(
            selectedAnswers = mapOf(0 to "B"),
            submittedQuestions = setOf(0),
            currentIndex = 1
        )

        // Q1: Incorrect ("A" instead of "C")
        state = state.copy(
            selectedAnswers = mapOf(0 to "B", 1 to "A"),
            submittedQuestions = setOf(0, 1),
            isFinished = true
        )

        assertEquals(1, state.correctCount)
        assertEquals(1, state.incorrectCount)
        assertEquals(50, state.accuracyPercentage)
        assertTrue(state.isFinished)
    }

    @Test
    fun testMockExam_ScoreAndNegativeMarkingCalculation() {
        val totalQuestions = 20
        val correct = 15
        val wrong = 4
        val unattempted = 1

        val marksPerQuestion = 2.0
        val negativePerWrong = 0.5

        val rawScore = (correct * marksPerQuestion) - (wrong * negativePerWrong)
        val maxScore = totalQuestions * marksPerQuestion
        val attempted = correct + wrong
        val accuracy = (correct.toDouble() / attempted.toDouble()) * 100.0

        assertEquals(28.0, rawScore, 0.01)
        assertEquals(40.0, maxScore, 0.01)
        assertEquals(78.94, accuracy, 0.1)

        val attempt = TestAttempt(
            testId = 1,
            testTitle = "Indian Army GD Mock Test 01",
            targetExam = "Indian Army",
            studentId = "JBA-2026-001",
            date = "2026-08-24",
            totalQuestions = totalQuestions,
            attemptedCount = attempted,
            correctCount = correct,
            wrongCount = wrong,
            unattemptedCount = unattempted,
            score = rawScore,
            maxScore = maxScore,
            accuracyPercentage = accuracy,
            timeTakenSeconds = 1200
        )

        assertEquals(28.0, attempt.score, 0.01)
        assertEquals(19, attempt.attemptedCount)
        assertEquals(1, attempt.unattemptedCount)
    }
}
