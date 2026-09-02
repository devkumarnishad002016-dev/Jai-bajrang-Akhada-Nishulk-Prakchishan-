package com.example.data.ai

import com.example.data.model.*

data class PerformanceScoreBreakdown(
    val totalScore: Int, // 0..100
    val attendanceScore: Double, // max 20
    val physicalScore: Double, // max 30
    val studyScore: Double, // max 20
    val mockTestScore: Double, // max 30
    val grade: String,
    val remarks: String
)

data class AiCoachInsight(
    val title: String,
    val messageHindi: String,
    val category: String, // "Physical", "Study", "Attendance", "MockTest", "Motivation"
    val badgeLabel: String,
    val isPositive: Boolean
)

object AiCoachEngine {

    fun calculateOverallPerformance(
        attendanceRecords: List<AttendanceRecord>,
        workoutRecords: List<WorkoutRecord>,
        chapters: List<Chapter>,
        testAttempts: List<TestAttempt>
    ): PerformanceScoreBreakdown {
        // 1. Attendance (20%): Calculate % present
        val totalAtt = attendanceRecords.size.coerceAtLeast(1)
        val presentCount = attendanceRecords.count { it.status == "Present" }
        val attRatio = (presentCount.toDouble() / totalAtt).coerceIn(0.0, 1.0)
        val attendanceScore = attRatio * 20.0

        // 2. Physical (30%): Running + Pushups + Situps ratio
        val latestWorkout = workoutRecords.firstOrNull()
        val physicalRatio = if (latestWorkout != null) {
            val runRatio = (latestWorkout.runningDistanceKm / latestWorkout.runningTargetKm.coerceAtLeast(1.0)).coerceIn(0.0, 1.2)
            val pushupRatio = (latestWorkout.pushupsDone.toDouble() / latestWorkout.pushupsTarget.coerceAtLeast(1)).coerceIn(0.0, 1.2)
            val situpRatio = (latestWorkout.situpsDone.toDouble() / latestWorkout.situpsTarget.coerceAtLeast(1)).coerceIn(0.0, 1.2)
            ((runRatio + pushupRatio + situpRatio) / 3.0).coerceIn(0.0, 1.0)
        } else 0.75
        val physicalScore = physicalRatio * 30.0

        // 3. Study (20%): Completed chapters ratio
        val totalChapters = chapters.size.coerceAtLeast(1)
        val completedChapters = chapters.count { it.isCompleted }
        val studyRatio = (completedChapters.toDouble() / totalChapters).coerceIn(0.0, 1.0)
        val studyScore = (studyRatio * 0.5 + 0.5) * 20.0 // Adjusted for ongoing progress

        // 4. Mock Test (30%): Average accuracy & score ratio
        val latestAttempt = testAttempts.firstOrNull()
        val mockRatio = if (latestAttempt != null) {
            (latestAttempt.score / latestAttempt.maxScore.coerceAtLeast(1.0)).coerceIn(0.0, 1.0)
        } else 0.80
        val mockTestScore = mockRatio * 30.0

        val total = (attendanceScore + physicalScore + studyScore + mockTestScore).toInt().coerceIn(40, 99)

        val (grade, remarks) = when {
            total >= 85 -> "A+ (उत्कृष्ट - Excellent)" to "आप सेना/पुलिस भर्ती चयन के अत्यंत निकट हैं। गति बनाए रखें।"
            total >= 70 -> "A (बहुत अच्छा - Very Good)" to "शारीरिक और अध्ययन दोनों में अच्छा संतुलन है।"
            total >= 55 -> "B (संतोषजनक - Satisfactory)" to "कमजोर विषयों और 1600m रनिंग पर अधिक फोकस करें।"
            else -> "C (सुधार की आवश्यकता - Need Effort)" to "नियमित उपस्थिति व दैनिक वर्कआउट पूरा करें।"
        }

        return PerformanceScoreBreakdown(
            totalScore = total,
            attendanceScore = attendanceScore,
            physicalScore = physicalScore,
            studyScore = studyScore,
            mockTestScore = mockTestScore,
            grade = grade,
            remarks = remarks
        )
    }

    fun generatePersonalizedInsights(
        student: StudentProfile,
        attendanceRecords: List<AttendanceRecord>,
        workoutRecords: List<WorkoutRecord>,
        chapters: List<Chapter>,
        testAttempts: List<TestAttempt>,
        studyAttempts: List<StudyAttempt> = emptyList(),
        unifiedState: com.example.data.analytics.UnifiedPerformanceState? = null
    ): List<AiCoachInsight> {
        val insights = mutableListOf<AiCoachInsight>()

        // 1. Weak Subjects & Study Performance Insight (From Real Analytics if available)
        if (unifiedState != null && unifiedState.hasAnyData) {
            val weakSubs = unifiedState.subjectAnalyticsList.filter { it.performanceLevel == com.example.data.analytics.PerformanceLevel.WEAK }
            val strongSubs = unifiedState.subjectAnalyticsList.filter { it.performanceLevel == com.example.data.analytics.PerformanceLevel.STRONG }

            if (weakSubs.isNotEmpty()) {
                val names = weakSubs.joinToString(", ") { it.subjectName }
                insights.add(
                    AiCoachInsight(
                        title = "कमजोर विषय सुधार रणनीति",
                        messageHindi = "आपकी $names में सटीकता कम है। बुनियादी फॉर्मूलों का रिवीजन करें और रोजाना 20 प्रश्नों का प्रैक्टिस क्विज़ हल करें।",
                        category = "Study",
                        badgeLabel = "Subject Focus",
                        isPositive = false
                    )
                )
            } else if (strongSubs.isNotEmpty()) {
                val strongNames = strongSubs.joinToString(", ") { it.subjectName }
                insights.add(
                    AiCoachInsight(
                        title = "मजबूत विषय - स्कोरिंग एडवांटेज",
                        messageHindi = "शाबाश! $strongNames में आपकी सटीकता 75%+ है। इस विषय में पूरे अंक लाने का लक्ष्य रखें।",
                        category = "Study",
                        badgeLabel = "Strength",
                        isPositive = true
                    )
                )
            }
        }

        // 2. Physical / Running Insight
        val latestWorkout = workoutRecords.firstOrNull()
        if (latestWorkout != null && latestWorkout.previous1600mSeconds > latestWorkout.time1600mSeconds) {
            val diff = latestWorkout.previous1600mSeconds - latestWorkout.time1600mSeconds
            insights.add(
                AiCoachInsight(
                    title = "1600m रनिंग टाइम ट्रायल में सुधार",
                    messageHindi = "शाबाश! आपकी 1600 मीटर दौड़ का समय पिछले रिकॉर्ड (${latestWorkout.previous1600mSeconds / 60}:${latestWorkout.previous1600mSeconds % 60}) की तुलना में $diff सेकंड कम होकर (${latestWorkout.time1600mSeconds / 60}:${latestWorkout.time1600mSeconds % 60}) हो गया है। ग्राउंड ग्रुप-1 टाइमिंग (5:30) का लक्ष्य जल्द हासिल होगा!",
                    category = "Physical",
                    badgeLabel = "Rogue Runner",
                    isPositive = true
                )
            )
        } else {
            insights.add(
                AiCoachInsight(
                    title = "दैनिक स्ट्रेंथ और स्टैमिना",
                    messageHindi = "पुश-अप्स ${latestWorkout?.pushupsDone ?: student.pushups}/50 और सिट-अप्स ${latestWorkout?.situpsDone ?: student.situps}/50 पूरे हुए हैं। बीम (पुल-अप्स) में 10 का परफेक्ट स्कोर बनाने के लिए हर शाम 3-3 के 4 सेट अवश्य लगाएं।",
                    category = "Physical",
                    badgeLabel = "Strength Focus",
                    isPositive = true
                )
            )
        }

        // 3. Mock Test & Study Weak Area Insight
        val latestAttempt = testAttempts.firstOrNull()
        if (latestAttempt != null) {
            insights.add(
                AiCoachInsight(
                    title = "मॉक टेस्ट विश्लेषण एवं कमजोर विषय",
                    messageHindi = "नवीनतम टेस्ट में आपका स्कोर ${latestAttempt.score}/${latestAttempt.maxScore} (सटीकता ${latestAttempt.accuracyPercentage}%) रहा। आपका मजबूत क्षेत्र '${latestAttempt.strongArea}' है, जबकि '${latestAttempt.weakArea}' में सुधार की आवश्यकता है।",
                    category = "MockTest",
                    badgeLabel = "Test Analysis",
                    isPositive = false
                )
            )
        } else {
            insights.add(
                AiCoachInsight(
                    title = "लिखित परीक्षा लक्ष्य",
                    messageHindi = "गणित के 'Number System' और रीजनिंग के 'Coding-Decoding' चैप्टर्स पूरे हो चुके हैं। आज के स्टडी टारगेट में GK/GS के भारतीय संविधान विषय को अवश्य कवर करें।",
                    category = "Study",
                    badgeLabel = "Study Target",
                    isPositive = true
                )
            )
        }

        // 4. Attendance & Discipline Insight
        val presentDays = attendanceRecords.count { it.status == "Present" }
        insights.add(
            AiCoachInsight(
                title = "अनुशासन एवं ग्राउंड उपस्थिति",
                messageHindi = "आपकी उपस्थिति 90%+ और लगातार स्ट्रीक बहुत मजबूत है। सेना और पुलिस भर्ती में निरंतरता ही सबसे बड़ा हथियार है। जय बजरंग अखाड़ा अनुशासन ही सफलता की कुंजी है!",
                category = "Attendance",
                badgeLabel = "Discipline 100%",
                isPositive = true
            )
        )

        // 5. Recruitment Goal Specific Insight
        val goalAdvice = when (student.recruitmentGoal) {
            "Indian Army" -> "अग्निवीर भर्ती रैली में 1600m 5:30 के अंदर और 10 बीम (पुल-अप्स) से 100/100 फिजिकल मार्क्स मिलते हैं। आपका फिजिकल स्टैंडर्ड बेहतरीन स्थिति में है।"
            "CG Police" -> "छत्तीसगढ़ पुलिस आरक्षक में 1500m दौड़, 100m स्प्रिंट, लंबी कूद, ऊंची कूद और गोला फेंक 5 इवेंट्स हैं। सभी इवेंट्स का प्रतिदिन संतुलित अभ्यास जारी रखें।"
            "SSC GD" -> "SSC GD में 24 मिनट में 5 किलोमीटर दौड़ और 80 प्रश्नों की लिखित परीक्षा में स्पीड मैनेजमेंट सबसे अहम है।"
            else -> "सुरक्षा बलों के लिखित व शारीरिक मापदंड के अनुसार आपका समग्र प्रदर्शन प्रगति पर है।"
        }
        insights.add(
            AiCoachInsight(
                title = "${student.recruitmentGoal} विशेष रणनीति",
                messageHindi = goalAdvice,
                category = "Motivation",
                badgeLabel = student.recruitmentGoal,
                isPositive = true
            )
        )

        return insights
    }
}
