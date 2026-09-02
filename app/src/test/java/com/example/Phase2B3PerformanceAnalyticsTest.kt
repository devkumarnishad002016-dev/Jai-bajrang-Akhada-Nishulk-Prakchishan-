package com.example

import com.example.data.analytics.*
import com.example.data.model.*
import org.junit.Assert.*
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.*

class Phase2B3PerformanceAnalyticsTest {

    private val sampleSubject = StudySubject(
        id = 1,
        subjectId = "SUB_MATH",
        name = "सामान्य गणित (Mathematics)",
        icon = "🔢",
        displayOrder = 1
    )

    private val sampleTopic1 = StudyTopic(
        id = 1,
        topicId = "TOPIC_MATH_NUMSYS",
        subjectId = "SUB_MATH",
        topicName = "संख्या पद्धति (Number System)",
        displayOrder = 1
    )

    private val sampleQuestions = listOf(
        Question(id = 1, questionId = "Q_MATH_01", subjectId = "SUB_MATH", topicId = "TOPIC_MATH_NUMSYS", questionText = "Q1", optionA = "A", optionB = "B", optionC = "C", optionD = "D", correctOption = 0, correctOptionLetter = "A"),
        Question(id = 2, questionId = "Q_MATH_02", subjectId = "SUB_MATH", topicId = "TOPIC_MATH_NUMSYS", questionText = "Q2", optionA = "A", optionB = "B", optionC = "C", optionD = "D", correctOption = 0, correctOptionLetter = "A"),
        Question(id = 3, questionId = "Q_MATH_03", subjectId = "SUB_MATH", topicId = "TOPIC_MATH_NUMSYS", questionText = "Q3", optionA = "A", optionB = "B", optionC = "C", optionD = "D", correctOption = 0, correctOptionLetter = "A"),
        Question(id = 4, questionId = "Q_MATH_04", subjectId = "SUB_MATH", topicId = "TOPIC_MATH_NUMSYS", questionText = "Q4", optionA = "A", optionB = "B", optionC = "C", optionD = "D", correctOption = 0, correctOptionLetter = "A"),
        Question(id = 5, questionId = "Q_MATH_05", subjectId = "SUB_MATH", topicId = "TOPIC_MATH_NUMSYS", questionText = "Q5", optionA = "A", optionB = "B", optionC = "C", optionD = "D", correctOption = 0, correctOptionLetter = "A")
    )

    @Test
    fun testSubjectAnalytics_NeedMorePracticeWhenUnderThreshold() {
        val attempts = listOf(
            StudyAttempt(studentId = "JBA-2026-001", questionId = "Q_MATH_01", subjectId = "SUB_MATH", topicId = "TOPIC_MATH_NUMSYS", selectedAnswer = "A", isCorrect = true, timeTakenSeconds = 10, attemptDate = "2026-08-23"),
            StudyAttempt(studentId = "JBA-2026-001", questionId = "Q_MATH_02", subjectId = "SUB_MATH", topicId = "TOPIC_MATH_NUMSYS", selectedAnswer = "A", isCorrect = true, timeTakenSeconds = 12, attemptDate = "2026-08-23")
        )

        val analytics = PerformanceInsightEngine.calculateSubjectAnalytics(
            subjects = listOf(sampleSubject),
            studyAttempts = attempts,
            questions = sampleQuestions,
            testAttempts = emptyList()
        )

        assertEquals(1, analytics.size)
        val mathAnalytics = analytics.first()
        assertEquals(2, mathAnalytics.totalAttempted)
        assertEquals(2, mathAnalytics.correctCount)
        assertEquals(100.0, mathAnalytics.accuracyPercentage, 0.01)
        // Under 5 attempts -> NEED_MORE_PRACTICE
        assertEquals(PerformanceLevel.NEED_MORE_PRACTICE, mathAnalytics.performanceLevel)
    }

    @Test
    fun testSubjectAnalytics_Classifications() {
        // 10 attempts, 8 correct = 80% accuracy -> STRONG
        val attemptsStrong = (1..10).map { i ->
            StudyAttempt(
                studentId = "JBA-2026-001",
                questionId = "Q_MATH_0${i % 5 + 1}",
                subjectId = "SUB_MATH",
                topicId = "TOPIC_MATH_NUMSYS",
                selectedAnswer = if (i <= 8) "A" else "B",
                isCorrect = i <= 8,
                timeTakenSeconds = 15,
                attemptDate = "2026-08-23"
            )
        }

        val analyticsStrong = PerformanceInsightEngine.calculateSubjectAnalytics(
            subjects = listOf(sampleSubject),
            studyAttempts = attemptsStrong,
            questions = sampleQuestions,
            testAttempts = emptyList()
        )

        assertEquals(PerformanceLevel.STRONG, analyticsStrong.first().performanceLevel)
        assertEquals(80.0, analyticsStrong.first().accuracyPercentage, 0.01)

        // 10 attempts, 6 correct = 60% accuracy -> AVERAGE
        val attemptsAvg = (1..10).map { i ->
            StudyAttempt(
                studentId = "JBA-2026-001",
                questionId = "Q_MATH_0${i % 5 + 1}",
                subjectId = "SUB_MATH",
                topicId = "TOPIC_MATH_NUMSYS",
                selectedAnswer = if (i <= 6) "A" else "B",
                isCorrect = i <= 6,
                timeTakenSeconds = 15,
                attemptDate = "2026-08-23"
            )
        }

        val analyticsAvg = PerformanceInsightEngine.calculateSubjectAnalytics(
            subjects = listOf(sampleSubject),
            studyAttempts = attemptsAvg,
            questions = sampleQuestions,
            testAttempts = emptyList()
        )

        assertEquals(PerformanceLevel.AVERAGE, analyticsAvg.first().performanceLevel)
        assertEquals(60.0, analyticsAvg.first().accuracyPercentage, 0.01)

        // 10 attempts, 4 correct = 40% accuracy -> WEAK
        val attemptsWeak = (1..10).map { i ->
            StudyAttempt(
                studentId = "JBA-2026-001",
                questionId = "Q_MATH_0${i % 5 + 1}",
                subjectId = "SUB_MATH",
                topicId = "TOPIC_MATH_NUMSYS",
                selectedAnswer = if (i <= 4) "A" else "B",
                isCorrect = i <= 4,
                timeTakenSeconds = 15,
                attemptDate = "2026-08-23"
            )
        }

        val analyticsWeak = PerformanceInsightEngine.calculateSubjectAnalytics(
            subjects = listOf(sampleSubject),
            studyAttempts = attemptsWeak,
            questions = sampleQuestions,
            testAttempts = emptyList()
        )

        assertEquals(PerformanceLevel.WEAK, analyticsWeak.first().performanceLevel)
        assertEquals(40.0, analyticsWeak.first().accuracyPercentage, 0.01)
    }

    @Test
    fun testWeightRedistributionWhenOnlyStudyAttempts() {
        val attempts = (1..10).map { i ->
            StudyAttempt(
                studentId = "JBA-2026-001",
                questionId = "Q_MATH_01",
                subjectId = "SUB_MATH",
                topicId = "TOPIC_MATH_NUMSYS",
                selectedAnswer = "A",
                isCorrect = true,
                timeTakenSeconds = 10,
                attemptDate = "2026-08-23"
            )
        }

        val score = PerformanceInsightEngine.calculateUnifiedPerformanceScore(
            studyAttempts = attempts,
            testAttempts = emptyList(),
            attendanceRecords = emptyList(),
            workoutRecords = emptyList(),
            trainingRecords = emptyList()
        )

        assertTrue(score.overallScore in 50..100)
        assertNotNull(score.grade)
        assertNotNull(score.remarks)
    }

    @Test
    fun testPracticeQuizTrendCalculation() {
        val df = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val cal = Calendar.getInstance()

        cal.add(Calendar.DAY_OF_YEAR, -5)
        val date1 = df.format(cal.time)
        cal.add(Calendar.DAY_OF_YEAR, 2)
        val date2 = df.format(cal.time)
        cal.add(Calendar.DAY_OF_YEAR, 2)
        val date3 = df.format(cal.time)

        // 6 attempts
        val attempts = listOf(
            StudyAttempt(studentId = "JBA-2026-001", questionId = "Q_MATH_01", subjectId = "SUB_MATH", topicId = "TOPIC_MATH_NUMSYS", selectedAnswer = "A", isCorrect = true, attemptDate = date1),
            StudyAttempt(studentId = "JBA-2026-001", questionId = "Q_MATH_02", subjectId = "SUB_MATH", topicId = "TOPIC_MATH_NUMSYS", selectedAnswer = "B", isCorrect = false, attemptDate = date1),
            StudyAttempt(studentId = "JBA-2026-001", questionId = "Q_MATH_01", subjectId = "SUB_MATH", topicId = "TOPIC_MATH_NUMSYS", selectedAnswer = "A", isCorrect = true, attemptDate = date2),
            StudyAttempt(studentId = "JBA-2026-001", questionId = "Q_MATH_02", subjectId = "SUB_MATH", topicId = "TOPIC_MATH_NUMSYS", selectedAnswer = "A", isCorrect = true, attemptDate = date2),
            StudyAttempt(studentId = "JBA-2026-001", questionId = "Q_MATH_01", subjectId = "SUB_MATH", topicId = "TOPIC_MATH_NUMSYS", selectedAnswer = "A", isCorrect = true, attemptDate = date3),
            StudyAttempt(studentId = "JBA-2026-001", questionId = "Q_MATH_02", subjectId = "SUB_MATH", topicId = "TOPIC_MATH_NUMSYS", selectedAnswer = "A", isCorrect = true, attemptDate = date3)
        )

        val trend = PerformanceInsightEngine.calculatePracticeQuizTrend(attempts)
        assertTrue(trend.hasTrendData)
        assertTrue(trend.latestRecordedAccuracy >= trend.firstRecordedAccuracy)
    }

    @Test
    fun testMockTestTrendCalculation() {
        val tests = listOf(
            TestAttempt(
                id = 1,
                testId = 1,
                testTitle = "Army Mock Test 1",
                targetExam = "Indian Army",
                studentId = "JBA-2026-001",
                date = "2026-08-10",
                totalQuestions = 50,
                attemptedCount = 45,
                correctCount = 30,
                wrongCount = 15,
                unattemptedCount = 5,
                score = 60.0,
                maxScore = 100.0,
                accuracyPercentage = 60.0,
                timeTakenSeconds = 1800
            ),
            TestAttempt(
                id = 2,
                testId = 2,
                testTitle = "Army Mock Test 2",
                targetExam = "Indian Army",
                studentId = "JBA-2026-001",
                date = "2026-08-15",
                totalQuestions = 50,
                attemptedCount = 50,
                correctCount = 40,
                wrongCount = 10,
                unattemptedCount = 0,
                score = 80.0,
                maxScore = 100.0,
                accuracyPercentage = 80.0,
                timeTakenSeconds = 1750
            )
        )

        val trend = PerformanceInsightEngine.calculateMockTestTrend(tests)
        assertTrue(trend.hasTrendData)
        assertEquals(80.0, trend.latestScore, 0.01)
        assertEquals(80.0, trend.bestScore, 0.01)
        assertEquals(70.0, trend.averageScore, 0.01)
        assertEquals(20.0, trend.scoreImprovement, 0.01)
    }
}
