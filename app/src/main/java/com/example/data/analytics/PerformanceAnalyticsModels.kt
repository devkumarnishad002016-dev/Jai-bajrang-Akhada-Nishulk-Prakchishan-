package com.example.data.analytics

import com.example.data.model.StudySubject
import com.example.data.model.StudyTopic

object AnalyticsConstants {
    const val MINIMUM_TOPIC_ATTEMPTS = 5
    const val MINIMUM_TREND_RECORDS = 2
    const val STRONG_ACCURACY_THRESHOLD = 75.0
    const val AVERAGE_ACCURACY_THRESHOLD = 50.0

    // Transparent Default Performance Score weights (sum = 1.00)
    const val WEIGHT_STUDY = 0.30
    const val WEIGHT_MOCK_TEST = 0.35
    const val WEIGHT_CONSISTENCY = 0.20
    const val WEIGHT_PHYSICAL_ATTENDANCE = 0.15
}

enum class PerformanceLevel(val labelHindi: String, val badgeColorHex: Long) {
    STRONG("मजबूत (Strong)", 0xFF2E7D32),
    AVERAGE("औसत (Average)", 0xFFF57C00),
    WEAK("कमजोर (Weak)", 0xFFD32F2F),
    NEED_MORE_PRACTICE("अधिक अभ्यास आवश्यक", 0xFF757575)
}

data class SubjectAnalytics(
    val subjectId: String,
    val subjectName: String,
    val icon: String,
    val totalAttempted: Int,
    val correctCount: Int,
    val incorrectCount: Int,
    val accuracyPercentage: Double,
    val averageScore: Double,
    val practiceSessionsCount: Int,
    val mockQuestionsCount: Int,
    val performanceLevel: PerformanceLevel
)

data class TopicAnalytics(
    val topicId: String,
    val topicName: String,
    val subjectId: String,
    val subjectName: String,
    val totalAttempted: Int,
    val correctCount: Int,
    val incorrectCount: Int,
    val accuracyPercentage: Double,
    val performanceLevel: PerformanceLevel,
    val hasSufficientData: Boolean
)

data class TodayProgressSummary(
    val date: String,
    val questionsAttempted: Int,
    val correctCount: Int,
    val accuracyPercentage: Double,
    val studySessionsCount: Int,
    val mockTestsCount: Int,
    val timeSpentSeconds: Int,
    val hasActivity: Boolean
)

data class DailyActivity(
    val date: String, // YYYY-MM-DD
    val dayNameHindi: String, // सोम, मंगल, बुध...
    val questionsAttempted: Int,
    val correctCount: Int,
    val accuracyPercentage: Double,
    val hasStudied: Boolean
)

data class WeeklyProgressSummary(
    val last7Days: List<DailyActivity>,
    val totalQuestions7Days: Int,
    val averageAccuracy7Days: Double,
    val activeDaysCount: Int,
    val consistencyPercentage: Double,
    val comparisonWithPreviousWeekText: String,
    val hasSufficientData: Boolean
)

data class PracticeQuizTrend(
    val totalAttempts: Int,
    val recentAccuracy: Double,
    val firstRecordedAccuracy: Double,
    val latestRecordedAccuracy: Double,
    val improvementPercentage: Double,
    val recentAverageAccuracy: Double,
    val hasTrendData: Boolean
)

data class MockTestTrend(
    val totalAttempts: Int,
    val recentScores: List<Double>,
    val recentAccuracy: Double,
    val scoreImprovement: Double,
    val bestScore: Double,
    val latestScore: Double,
    val averageScore: Double,
    val hasTrendData: Boolean
)

data class UnifiedPerformanceScore(
    val overallScore: Int, // 0..100
    val grade: String,
    val remarks: String,
    val studyComponentScore: Double, // calculated value
    val mockTestComponentScore: Double,
    val consistencyComponentScore: Double,
    val physicalAttendanceComponentScore: Double,
    val weightsUsedExplanation: String,
    val isNormalized: Boolean
)

data class PerformanceRecommendation(
    val id: String,
    val title: String,
    val actionTextHindi: String,
    val category: String, // "Subject", "Topic", "MockTest", "Consistency", "General"
    val priority: Int, // 1 = high, 2 = medium, 3 = low
    val isPositive: Boolean,
    val iconEmoji: String
)

data class UnifiedPerformanceState(
    val studentId: String,
    val studentName: String,
    val overallScore: UnifiedPerformanceScore,
    val todayProgress: TodayProgressSummary,
    val weeklyProgress: WeeklyProgressSummary,
    val subjectAnalyticsList: List<SubjectAnalytics>,
    val strongTopics: List<TopicAnalytics>,
    val averageTopics: List<TopicAnalytics>,
    val weakTopics: List<TopicAnalytics>,
    val needPracticeTopics: List<TopicAnalytics>,
    val mostPracticedTopics: List<TopicAnalytics>,
    val quizTrend: PracticeQuizTrend,
    val mockTrend: MockTestTrend,
    val recommendations: List<PerformanceRecommendation>,
    val totalQuestionsAttempted: Int,
    val totalCorrect: Int,
    val totalIncorrect: Int,
    val overallAccuracyPercentage: Double,
    val totalStudySessions: Int,
    val totalMockTests: Int,
    val currentStreakDays: Int,
    val hasAnyData: Boolean
)
