package com.example.data.analytics

import com.example.data.model.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

object PerformanceInsightEngine {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    fun calculateSubjectAnalytics(
        subjects: List<StudySubject>,
        studyAttempts: List<StudyAttempt>,
        questions: List<Question>,
        testAttempts: List<TestAttempt>
    ): List<SubjectAnalytics> {
        val questionMap = questions.associateBy { it.questionId }

        return subjects.map { subject ->
            // Filter study attempts matching this subject by subjectId or through question relation
            val subjectAttempts = studyAttempts.filter { attempt ->
                attempt.subjectId == subject.subjectId ||
                        questionMap[attempt.questionId]?.subjectId == subject.subjectId ||
                        questionMap[attempt.questionId]?.subjectName.equals(subject.name, ignoreCase = true)
            }

            val totalAttempted = subjectAttempts.size
            val correctCount = subjectAttempts.count { it.isCorrect }
            val incorrectCount = totalAttempted - correctCount
            val accuracy = if (totalAttempted > 0) {
                (correctCount.toDouble() / totalAttempted.toDouble()) * 100.0
            } else 0.0

            // Estimated score based on practice
            val averageScore = if (totalAttempted > 0) {
                ((correctCount * 2.0) - (incorrectCount * 0.5)).coerceAtLeast(0.0) / totalAttempted.coerceAtLeast(1) * 10.0
            } else 0.0

            // Distinct days or sessions
            val distinctSessions = subjectAttempts.map { it.attemptDate.ifEmpty { "2026-08-24" } }.distinct().size

            val level = when {
                totalAttempted < AnalyticsConstants.MINIMUM_TOPIC_ATTEMPTS -> PerformanceLevel.NEED_MORE_PRACTICE
                accuracy >= AnalyticsConstants.STRONG_ACCURACY_THRESHOLD -> PerformanceLevel.STRONG
                accuracy >= AnalyticsConstants.AVERAGE_ACCURACY_THRESHOLD -> PerformanceLevel.AVERAGE
                else -> PerformanceLevel.WEAK
            }

            SubjectAnalytics(
                subjectId = subject.subjectId,
                subjectName = subject.name,
                icon = subject.icon,
                totalAttempted = totalAttempted,
                correctCount = correctCount,
                incorrectCount = incorrectCount,
                accuracyPercentage = accuracy,
                averageScore = averageScore,
                practiceSessionsCount = distinctSessions,
                mockQuestionsCount = 0,
                performanceLevel = level
            )
        }
    }

    fun calculateTopicAnalytics(
        topics: List<StudyTopic>,
        subjects: List<StudySubject>,
        studyAttempts: List<StudyAttempt>,
        questions: List<Question>
    ): List<TopicAnalytics> {
        val subjectMap = subjects.associateBy { it.subjectId }
        val questionMap = questions.associateBy { it.questionId }

        return topics.map { topic ->
            val topicAttempts = studyAttempts.filter { attempt ->
                attempt.topicId == topic.topicId ||
                        questionMap[attempt.questionId]?.topicId == topic.topicId
            }

            val totalAttempted = topicAttempts.size
            val correctCount = topicAttempts.count { it.isCorrect }
            val incorrectCount = totalAttempted - correctCount
            val accuracy = if (totalAttempted > 0) {
                (correctCount.toDouble() / totalAttempted.toDouble()) * 100.0
            } else 0.0

            val hasSufficientData = totalAttempted >= AnalyticsConstants.MINIMUM_TOPIC_ATTEMPTS
            val level = when {
                !hasSufficientData -> PerformanceLevel.NEED_MORE_PRACTICE
                accuracy >= AnalyticsConstants.STRONG_ACCURACY_THRESHOLD -> PerformanceLevel.STRONG
                accuracy >= AnalyticsConstants.AVERAGE_ACCURACY_THRESHOLD -> PerformanceLevel.AVERAGE
                else -> PerformanceLevel.WEAK
            }

            val subName = subjectMap[topic.subjectId]?.name ?: "सामान्य"

            TopicAnalytics(
                topicId = topic.topicId,
                topicName = topic.topicName,
                subjectId = topic.subjectId,
                subjectName = subName,
                totalAttempted = totalAttempted,
                correctCount = correctCount,
                incorrectCount = incorrectCount,
                accuracyPercentage = accuracy,
                performanceLevel = level,
                hasSufficientData = hasSufficientData
            )
        }
    }

    fun calculateTodayProgress(
        targetDate: String,
        studyAttempts: List<StudyAttempt>,
        testAttempts: List<TestAttempt>
    ): TodayProgressSummary {
        val todayStudy = studyAttempts.filter { it.attemptDate == targetDate }
        val todayTests = testAttempts.filter { it.date == targetDate }

        val totalQuestions = todayStudy.size
        val correct = todayStudy.count { it.isCorrect }
        val accuracy = if (totalQuestions > 0) (correct.toDouble() / totalQuestions.toDouble()) * 100.0 else 0.0
        val studySessions = if (totalQuestions > 0) 1 else 0
        val timeSpent = todayStudy.sumOf { it.timeTakenSeconds } + todayTests.sumOf { it.timeTakenSeconds }

        return TodayProgressSummary(
            date = targetDate,
            questionsAttempted = totalQuestions,
            correctCount = correct,
            accuracyPercentage = accuracy,
            studySessionsCount = studySessions,
            mockTestsCount = todayTests.size,
            timeSpentSeconds = timeSpent,
            hasActivity = totalQuestions > 0 || todayTests.isNotEmpty()
        )
    }

    fun calculateWeeklyProgress(
        calendar: Calendar = Calendar.getInstance(),
        studyAttempts: List<StudyAttempt>,
        testAttempts: List<TestAttempt>
    ): WeeklyProgressSummary {
        val daysList = mutableListOf<DailyActivity>()
        val hindiDayNames = listOf("रवि", "सोम", "मंगल", "बुध", "गुरु", "शुक्र", "शनि")

        val tempCal = calendar.clone() as Calendar
        var totalQuestions = 0
        var totalCorrect = 0
        var activeDays = 0

        for (i in 6 downTo 0) {
            val checkCal = calendar.clone() as Calendar
            checkCal.add(Calendar.DAY_OF_YEAR, -i)
            val dateStr = dateFormat.format(checkCal.time)
            val dayOfWeek = checkCal.get(Calendar.DAY_OF_WEEK) - 1 // 0 = Sunday
            val dayName = hindiDayNames.getOrElse(dayOfWeek) { "दिन" }

            val attemptsOnDate = studyAttempts.filter { it.attemptDate == dateStr }
            val qCount = attemptsOnDate.size
            val cCount = attemptsOnDate.count { it.isCorrect }
            val acc = if (qCount > 0) (cCount.toDouble() / qCount.toDouble()) * 100.0 else 0.0

            if (qCount > 0) {
                activeDays++
                totalQuestions += qCount
                totalCorrect += cCount
            }

            daysList.add(
                DailyActivity(
                    date = dateStr,
                    dayNameHindi = dayName,
                    questionsAttempted = qCount,
                    correctCount = cCount,
                    accuracyPercentage = acc,
                    hasStudied = qCount > 0
                )
            )
        }

        val avgAccuracy = if (totalQuestions > 0) (totalCorrect.toDouble() / totalQuestions.toDouble()) * 100.0 else 0.0
        val consistency = (activeDays.toDouble() / 7.0) * 100.0
        val hasSufficientData = totalQuestions >= 5

        val comparisonText = when {
            !hasSufficientData -> "सप्ताहिक तुलना के लिए पर्याप्त डेटा उपलब्ध नहीं है। नियमित अभ्यास जारी रखें।"
            consistency >= 70.0 -> "शानदार सातत्य! पिछले 7 दिनों में से $activeDays दिन सक्रिय रहकर आपने बेहतरीन निरंतरता बनाई है।"
            consistency >= 40.0 -> "मध्यम सक्रियता ($activeDays/7 दिन)। भर्ती चयन हेतु प्रतिदिन कम से कम एक अभ्यास सत्र पूरा करें।"
            else -> "सक्रियता कम है। अनुशासन ही सेना/पुलिस परीक्षा में सफलता की पहली सीढ़ी है।"
        }

        return WeeklyProgressSummary(
            last7Days = daysList,
            totalQuestions7Days = totalQuestions,
            averageAccuracy7Days = avgAccuracy,
            activeDaysCount = activeDays,
            consistencyPercentage = consistency,
            comparisonWithPreviousWeekText = comparisonText,
            hasSufficientData = hasSufficientData
        )
    }

    fun calculatePracticeQuizTrend(studyAttempts: List<StudyAttempt>): PracticeQuizTrend {
        if (studyAttempts.size < AnalyticsConstants.MINIMUM_TREND_RECORDS) {
            val singleAcc = if (studyAttempts.isNotEmpty()) {
                val c = studyAttempts.count { it.isCorrect }
                (c.toDouble() / studyAttempts.size) * 100.0
            } else 0.0

            return PracticeQuizTrend(
                totalAttempts = studyAttempts.size,
                recentAccuracy = singleAcc,
                firstRecordedAccuracy = singleAcc,
                latestRecordedAccuracy = singleAcc,
                improvementPercentage = 0.0,
                recentAverageAccuracy = singleAcc,
                hasTrendData = false
            )
        }

        // Group attempts by date or into chronological batches
        val sortedAttempts = studyAttempts.sortedBy { it.timestamp }
        val halfSize = sortedAttempts.size / 2
        val firstHalf = sortedAttempts.take(halfSize)
        val secondHalf = sortedAttempts.drop(halfSize)

        val firstAcc = if (firstHalf.isNotEmpty()) (firstHalf.count { it.isCorrect }.toDouble() / firstHalf.size) * 100.0 else 0.0
        val secondAcc = if (secondHalf.isNotEmpty()) (secondHalf.count { it.isCorrect }.toDouble() / secondHalf.size) * 100.0 else 0.0
        val improvement = secondAcc - firstAcc

        val overallCorrect = sortedAttempts.count { it.isCorrect }
        val overallAcc = (overallCorrect.toDouble() / sortedAttempts.size) * 100.0

        return PracticeQuizTrend(
            totalAttempts = studyAttempts.size,
            recentAccuracy = secondAcc,
            firstRecordedAccuracy = firstAcc,
            latestRecordedAccuracy = secondAcc,
            improvementPercentage = improvement,
            recentAverageAccuracy = overallAcc,
            hasTrendData = true
        )
    }

    fun calculateMockTestTrend(testAttempts: List<TestAttempt>): MockTestTrend {
        if (testAttempts.size < AnalyticsConstants.MINIMUM_TREND_RECORDS) {
            val latest = testAttempts.firstOrNull()
            return MockTestTrend(
                totalAttempts = testAttempts.size,
                recentScores = testAttempts.map { it.score },
                recentAccuracy = latest?.accuracyPercentage ?: 0.0,
                scoreImprovement = 0.0,
                bestScore = latest?.score ?: 0.0,
                latestScore = latest?.score ?: 0.0,
                averageScore = latest?.score ?: 0.0,
                hasTrendData = false
            )
        }

        val sorted = testAttempts.sortedBy { it.id }
        val firstAttempt = sorted.first()
        val latestAttempt = sorted.last()
        val improvement = latestAttempt.score - firstAttempt.score
        val best = sorted.maxOf { it.score }
        val avg = sorted.map { it.score }.average()
        val recentAcc = latestAttempt.accuracyPercentage

        return MockTestTrend(
            totalAttempts = testAttempts.size,
            recentScores = sorted.takeLast(5).map { it.score },
            recentAccuracy = recentAcc,
            scoreImprovement = improvement,
            bestScore = best,
            latestScore = latestAttempt.score,
            averageScore = avg,
            hasTrendData = true
        )
    }

    /**
     * Unified Performance Score calculation with proportional weight redistribution
     * so missing/empty categories do NOT penalize the student unfairly.
     */
    fun calculateUnifiedPerformanceScore(
        studyAttempts: List<StudyAttempt>,
        testAttempts: List<TestAttempt>,
        attendanceRecords: List<AttendanceRecord>,
        workoutRecords: List<WorkoutRecord>,
        trainingRecords: List<TrainingRecord>
    ): UnifiedPerformanceScore {
        val hasStudyData = studyAttempts.isNotEmpty()
        val hasMockData = testAttempts.isNotEmpty()
        val hasAttendanceData = attendanceRecords.isNotEmpty()
        val hasPhysicalData = workoutRecords.isNotEmpty() || trainingRecords.isNotEmpty()

        // 1. Raw scores (0..100 scale for each component)
        val rawStudyScore: Double = if (hasStudyData) {
            val correct = studyAttempts.count { it.isCorrect }
            ((correct.toDouble() / studyAttempts.size.toDouble()) * 100.0).coerceIn(0.0, 100.0)
        } else -1.0

        val rawMockScore: Double = if (hasMockData) {
            val avgPercentage = testAttempts.map { (it.score / it.maxScore.coerceAtLeast(1.0)) * 100.0 }.average()
            avgPercentage.coerceIn(0.0, 100.0)
        } else -1.0

        val rawConsistencyScore: Double = if (hasAttendanceData || hasStudyData) {
            val presentCount = attendanceRecords.count { it.status == "Present" }
            val attRate = if (attendanceRecords.isNotEmpty()) (presentCount.toDouble() / attendanceRecords.size.toDouble()) * 100.0 else 80.0
            val studyActiveDays = studyAttempts.map { it.attemptDate }.distinct().size
            val studyRate = (studyActiveDays.toDouble() / 7.0).coerceIn(0.0, 1.0) * 100.0
            ((attRate * 0.6) + (studyRate * 0.4)).coerceIn(0.0, 100.0)
        } else -1.0

        val rawPhysicalScore: Double = if (hasPhysicalData) {
            val latestWorkout = workoutRecords.firstOrNull()
            val latestTraining = trainingRecords.firstOrNull()
            if (latestWorkout != null) {
                val runRatio = (latestWorkout.runningDistanceKm / latestWorkout.runningTargetKm.coerceAtLeast(1.0)).coerceIn(0.0, 1.2)
                val pushupRatio = (latestWorkout.pushupsDone.toDouble() / latestWorkout.pushupsTarget.coerceAtLeast(1)).coerceIn(0.0, 1.2)
                (((runRatio + pushupRatio) / 2.0) * 100.0).coerceIn(0.0, 100.0)
            } else if (latestTraining != null) {
                val pushupRatio = (latestTraining.pushups.toDouble() / 40.0).coerceIn(0.0, 1.2)
                (pushupRatio * 100.0).coerceIn(0.0, 100.0)
            } else 80.0
        } else -1.0

        // 2. Proportional Weight Redistribution
        val availableComponents = mutableMapOf<String, Pair<Double, Double>>() // category -> Pair(rawScore, defaultWeight)
        if (rawStudyScore >= 0) availableComponents["Study"] = Pair(rawStudyScore, AnalyticsConstants.WEIGHT_STUDY)
        if (rawMockScore >= 0) availableComponents["MockTest"] = Pair(rawMockScore, AnalyticsConstants.WEIGHT_MOCK_TEST)
        if (rawConsistencyScore >= 0) availableComponents["Consistency"] = Pair(rawConsistencyScore, AnalyticsConstants.WEIGHT_CONSISTENCY)
        if (rawPhysicalScore >= 0) availableComponents["Physical"] = Pair(rawPhysicalScore, AnalyticsConstants.WEIGHT_PHYSICAL_ATTENDANCE)

        if (availableComponents.isEmpty()) {
            // Default initial state when completely fresh
            return UnifiedPerformanceScore(
                overallScore = 0,
                grade = "N/A (डेटा आवश्यक)",
                remarks = "प्रदर्शन स्कोर के लिए कृपया कम से कम 5 प्रश्नों का अभ्यास या मॉक टेस्ट पूरा करें।",
                studyComponentScore = 0.0,
                mockTestComponentScore = 0.0,
                consistencyComponentScore = 0.0,
                physicalAttendanceComponentScore = 0.0,
                weightsUsedExplanation = "डेटा उपलब्ध होने पर स्कोर की गणना की जाएगी।",
                isNormalized = false
            )
        }

        val totalAvailableWeight = availableComponents.values.sumOf { it.second }
        var weightedSum = 0.0

        var studyComponent = 0.0
        var mockComponent = 0.0
        var consistencyComponent = 0.0
        var physicalComponent = 0.0

        availableComponents.forEach { (key, pair) ->
            val normalizedWeight = pair.second / totalAvailableWeight
            val contribution = pair.first * normalizedWeight
            weightedSum += contribution

            when (key) {
                "Study" -> studyComponent = (pair.first * AnalyticsConstants.WEIGHT_STUDY)
                "MockTest" -> mockComponent = (pair.first * AnalyticsConstants.WEIGHT_MOCK_TEST)
                "Consistency" -> consistencyComponent = (pair.first * AnalyticsConstants.WEIGHT_CONSISTENCY)
                "Physical" -> physicalComponent = (pair.first * AnalyticsConstants.WEIGHT_PHYSICAL_ATTENDANCE)
            }
        }

        val finalScore = weightedSum.roundToInt().coerceIn(0, 100)

        val (grade, remarks) = when {
            finalScore >= 85 -> "A+ (उत्कृष्ट - Excellent)" to "आप सेना/पुलिस भर्ती परीक्षा चयन के अत्यंत निकट हैं। गति और शुद्धता बनाए रखें।"
            finalScore >= 70 -> "A (बहुत अच्छा - Very Good)" to "अध्ययन, मॉक टेस्ट और शारीरिक फिटनेस में अच्छा संतुलन है।"
            finalScore >= 50 -> "B (संतोषजनक - Satisfactory)" to "कमजोर टॉपिक्स के अभ्यास और टाइम मैनेजमेंट पर विशेष ध्यान दें।"
            else -> "C (सुधार की आवश्यकता - Need Effort)" to "नियमित अभ्यास सत्र पूरा करें और बुनियादी सूत्रों को दोहराएं।"
        }

        val weightsExplanation = "अध्ययन (30%), मॉक टेस्ट (35%), निरंतरता (20%) एवं शारीरिक/उपस्थिति (15%) के उपलब्ध वास्तविक डेटा पर आधारित।"

        return UnifiedPerformanceScore(
            overallScore = finalScore,
            grade = grade,
            remarks = remarks,
            studyComponentScore = studyComponent,
            mockTestComponentScore = mockComponent,
            consistencyComponentScore = consistencyComponent,
            physicalAttendanceComponentScore = physicalComponent,
            weightsUsedExplanation = weightsExplanation,
            isNormalized = totalAvailableWeight < 1.0
        )
    }

    fun generatePerformanceRecommendations(
        subjectAnalytics: List<SubjectAnalytics>,
        topicAnalytics: List<TopicAnalytics>,
        overallAccuracy: Double,
        quizTrend: PracticeQuizTrend,
        mockTrend: MockTestTrend,
        consistencyPercentage: Double,
        hasSufficientData: Boolean
    ): List<PerformanceRecommendation> {
        val recommendations = mutableListOf<PerformanceRecommendation>()

        if (!hasSufficientData) {
            recommendations.add(
                PerformanceRecommendation(
                    id = "REC_INIT",
                    title = "प्रारंभिक अभ्यास आवश्यक",
                    actionTextHindi = "सटीक विश्लेषण और व्यक्तिगत सुझाव पाने के लिए कम से कम 5 प्रश्नों का अभ्यास क्विज़ या एक मॉक टेस्ट पूरा करें।",
                    category = "General",
                    priority = 1,
                    isPositive = true,
                    iconEmoji = "🎯"
                )
            )
            return recommendations
        }

        // 1. Weak Subjects Focus
        val weakSubjects = subjectAnalytics.filter { it.performanceLevel == PerformanceLevel.WEAK }
        if (weakSubjects.isNotEmpty()) {
            val names = weakSubjects.joinToString(", ") { it.subjectName }
            recommendations.add(
                PerformanceRecommendation(
                    id = "REC_WEAK_SUB",
                    title = "कमजोर विषयों पर विशेष ध्यान",
                    actionTextHindi = "आपकी $names में सटीकता 50% से कम है। पहले बुनियादी सूत्र और थ्योरी पढ़कर रोज 20 प्रश्नों का अभ्यास करें।",
                    category = "Subject",
                    priority = 1,
                    isPositive = false,
                    iconEmoji = "⚠️"
                )
            )
        }

        // 2. Weak Topics Focus
        val weakTopics = topicAnalytics.filter { it.performanceLevel == PerformanceLevel.WEAK }
        if (weakTopics.isNotEmpty()) {
            val topWeak = weakTopics.take(2).joinToString(", ") { it.topicName }
            recommendations.add(
                PerformanceRecommendation(
                    id = "REC_WEAK_TOPIC",
                    title = "कमजोर टॉपिक सुधारें",
                    actionTextHindi = "टॉपिक '$topWeak' में गलत उत्तरों की संख्या अधिक है। इन टॉपिक्स के वीडियो/नोट्स दोबारा देखें।",
                    category = "Topic",
                    priority = 2,
                    isPositive = false,
                    iconEmoji = "📚"
                )
            )
        }

        // 3. Accuracy Trend Feedback
        if (quizTrend.hasTrendData) {
            if (quizTrend.improvementPercentage > 5.0) {
                recommendations.add(
                    PerformanceRecommendation(
                        id = "REC_TREND_UP",
                        title = "सटीकता में लगातार सुधार",
                        actionTextHindi = "शानदार! आपकी अभ्यास सटीकता में +${quizTrend.improvementPercentage.roundToInt()}% का सुधार हुआ है। अब मध्यम से कठिन प्रश्नों का अभ्यास शुरू करें।",
                        category = "General",
                        priority = 2,
                        isPositive = true,
                        iconEmoji = "📈"
                    )
                )
            } else if (quizTrend.improvementPercentage < -5.0) {
                recommendations.add(
                    PerformanceRecommendation(
                        id = "REC_TREND_DOWN",
                        title = "सटीकता में गिरावट",
                        actionTextHindi = "प्रश्नों को हल करने में जल्दबाजी न करें। प्रश्न को ध्यान से पढ़कर विकल्प चुनें ताकि नेगेटिव मार्किंग से बचा जा सके।",
                        category = "General",
                        priority = 1,
                        isPositive = false,
                        iconEmoji = "⏱️"
                    )
                )
            }
        }

        // 4. Mock Test Score Recommendation
        if (mockTrend.hasTrendData) {
            if (mockTrend.scoreImprovement > 0) {
                recommendations.add(
                    PerformanceRecommendation(
                        id = "REC_MOCK_GOOD",
                        title = "मॉक टेस्ट स्कोर प्रगति",
                        actionTextHindi = "मॉक टेस्ट में स्कोर बढ़ रहा है। अब परीक्षा हॉल जैसे दबाव में टाइम मैनेजमेंट और स्पीड पर ध्यान केंद्रित करें।",
                        category = "MockTest",
                        priority = 2,
                        isPositive = true,
                        iconEmoji = "🏆"
                    )
                )
            } else {
                recommendations.add(
                    PerformanceRecommendation(
                        id = "REC_MOCK_TIME",
                        title = "मॉक टेस्ट टाइम मैनेजमेंट",
                        actionTextHindi = "मॉक टेस्ट में कठिन प्रश्नों पर अधिक समय व्यर्थ न करें। पहले आसान और मध्यम प्रश्नों को हल करें।",
                        category = "MockTest",
                        priority = 2,
                        isPositive = false,
                        iconEmoji = "⏳"
                    )
                )
            }
        }

        // 5. Consistency Advice
        if (consistencyPercentage < 50.0) {
            recommendations.add(
                PerformanceRecommendation(
                    id = "REC_CONSISTENCY",
                    title = "नियमितता (Consistency) बढ़ाएं",
                    actionTextHindi = "भर्ती परीक्षा में सफलता निरंतर अभ्यास से ही संभव है। प्रतिदिन कम से कम एक 15-मिनट का अभ्यास सत्र अवश्य पूरा करें।",
                    category = "Consistency",
                    priority = 1,
                    isPositive = false,
                    iconEmoji = "🔥"
                )
            )
        } else {
            recommendations.add(
                PerformanceRecommendation(
                    id = "REC_CONSISTENCY_GOOD",
                    title = "उत्कृष्ट अध्ययन अनुशासन",
                    actionTextHindi = "आपकी दैनिक अध्ययन स्ट्रीक बहुत अच्छी है। इसी लय के साथ अंतिम चयन तक जुटे रहें। जय बजरंग अखाड़ा!",
                    category = "Consistency",
                    priority = 3,
                    isPositive = true,
                    iconEmoji = "💪"
                )
            )
        }

        return recommendations
    }

    fun buildUnifiedPerformanceState(
        student: StudentProfile?,
        subjects: List<StudySubject>,
        topics: List<StudyTopic>,
        questions: List<Question>,
        studyAttempts: List<StudyAttempt>,
        testAttempts: List<TestAttempt>,
        attendanceRecords: List<AttendanceRecord>,
        workoutRecords: List<WorkoutRecord>,
        trainingRecords: List<TrainingRecord>,
        currentDate: String = dateFormat.format(Date())
    ): UnifiedPerformanceState {
        val sId = student?.studentId ?: "JBA-2026-001"
        val sName = student?.fullName ?: "जय बजरंग कैडेट"

        val subjectAnalytics = calculateSubjectAnalytics(subjects, studyAttempts, questions, testAttempts)
        val topicAnalytics = calculateTopicAnalytics(topics, subjects, studyAttempts, questions)

        val strongTopics = topicAnalytics.filter { it.performanceLevel == PerformanceLevel.STRONG }
        val avgTopics = topicAnalytics.filter { it.performanceLevel == PerformanceLevel.AVERAGE }
        val weakTopics = topicAnalytics.filter { it.performanceLevel == PerformanceLevel.WEAK }
        val needPractice = topicAnalytics.filter { it.performanceLevel == PerformanceLevel.NEED_MORE_PRACTICE }
        val mostPracticed = topicAnalytics.filter { it.totalAttempted > 0 }.sortedByDescending { it.totalAttempted }.take(5)

        val todayProg = calculateTodayProgress(currentDate, studyAttempts, testAttempts)
        val weeklyProg = calculateWeeklyProgress(Calendar.getInstance(), studyAttempts, testAttempts)
        val quizTrend = calculatePracticeQuizTrend(studyAttempts)
        val mockTrend = calculateMockTestTrend(testAttempts)

        val totalQuestions = studyAttempts.size
        val totalCorrect = studyAttempts.count { it.isCorrect }
        val totalIncorrect = totalQuestions - totalCorrect
        val overallAcc = if (totalQuestions > 0) (totalCorrect.toDouble() / totalQuestions.toDouble()) * 100.0 else 0.0

        val hasAnyData = totalQuestions > 0 || testAttempts.isNotEmpty() || attendanceRecords.isNotEmpty()

        val overallScore = calculateUnifiedPerformanceScore(
            studyAttempts = studyAttempts,
            testAttempts = testAttempts,
            attendanceRecords = attendanceRecords,
            workoutRecords = workoutRecords,
            trainingRecords = trainingRecords
        )

        val recommendations = generatePerformanceRecommendations(
            subjectAnalytics = subjectAnalytics,
            topicAnalytics = topicAnalytics,
            overallAccuracy = overallAcc,
            quizTrend = quizTrend,
            mockTrend = mockTrend,
            consistencyPercentage = weeklyProg.consistencyPercentage,
            hasSufficientData = totalQuestions >= 5 || testAttempts.isNotEmpty()
        )

        val distinctDays = (studyAttempts.map { it.attemptDate } + attendanceRecords.filter { it.status == "Present" }.map { it.date }).distinct().size

        return UnifiedPerformanceState(
            studentId = sId,
            studentName = sName,
            overallScore = overallScore,
            todayProgress = todayProg,
            weeklyProgress = weeklyProg,
            subjectAnalyticsList = subjectAnalytics,
            strongTopics = strongTopics,
            averageTopics = avgTopics,
            weakTopics = weakTopics,
            needPracticeTopics = needPractice,
            mostPracticedTopics = mostPracticed,
            quizTrend = quizTrend,
            mockTrend = mockTrend,
            recommendations = recommendations,
            totalQuestionsAttempted = totalQuestions,
            totalCorrect = totalCorrect,
            totalIncorrect = totalIncorrect,
            overallAccuracyPercentage = overallAcc,
            totalStudySessions = if (totalQuestions > 0) distinctDays.coerceAtLeast(1) else 0,
            totalMockTests = testAttempts.size,
            currentStreakDays = if (distinctDays > 0) distinctDays.coerceAtMost(30) else 0,
            hasAnyData = hasAnyData
        )
    }
}
