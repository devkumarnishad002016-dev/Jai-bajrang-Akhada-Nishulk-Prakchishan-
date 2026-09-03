package com.example.data.mission

import com.example.data.model.AttendanceRecord
import com.example.data.model.StudentProfile
import com.example.data.model.TestAttempt
import com.example.data.model.TrainingRecord
import com.example.data.model.WorkoutRecord

data class ParentProgressReport(
    val studentName: String,
    val fatherName: String,
    val studentId: String,
    val village: String,
    val targetGoalHindi: String,
    val attendanceSummaryHindi: String,
    val attendanceStatusColorHex: Long,
    val attendanceDaysCount: Int,
    val attendancePercentage: Int,
    val physicalFitnessSummaryHindi: String,
    val runningTime1600m: String,
    val pushupsBeamHindi: String,
    val physicalGradeHindi: String,
    val studyProgressSummaryHindi: String,
    val mockTestAverageScoreHindi: String,
    val studyGradeHindi: String,
    val overallDisciplineGradeHindi: String, // "उत्कृष्ट (Grade A+)", "बहुत अच्छा (Grade A)", "संतोषजनक (Grade B)"
    val coachMessageToParentsHindi: String,
    val parentAdviceTipsHindi: List<String>,
    val reportDateFormatted: String
)

object ParentProgressEngine {

    fun generateReport(
        student: StudentProfile?,
        attendanceRecords: List<AttendanceRecord>,
        workoutRecords: List<WorkoutRecord>,
        trainingRecords: List<TrainingRecord>,
        testAttempts: List<TestAttempt>
    ): ParentProgressReport {
        val name = student?.fullName ?: "कैडेट"
        val father = student?.fatherName?.ifBlank { "अभिभावक" } ?: "अभिभावक"
        val village = student?.village?.ifBlank { "मौरिकला गुफा" } ?: "मौरिकला गुफा"
        val goal = when (student?.recruitmentGoal) {
            "Indian Army" -> "भारतीय थल सेना (Indian Army Agniveer GD)"
            "CG Police" -> "छत्तीसगढ़ पुलिस आरक्षक भर्ती"
            "SSC GD" -> "एसएससी जीडी / केंद्रीय सुरक्षा बल"
            else -> student?.recruitmentGoal ?: "सेना एवं पुलिस भर्ती"
        }

        // Attendance stats
        val totalDays = attendanceRecords.size.coerceAtLeast(1)
        val presentCount = attendanceRecords.count { it.status.equals("Present", ignoreCase = true) }
        val attendancePercent = if (attendanceRecords.isNotEmpty()) (presentCount * 100) / totalDays else student?.studyTargetPercentage?.coerceIn(70, 95) ?: 80

        val (attText, attColor) = when {
            attendancePercent >= 85 -> Pair("नियमित उपस्थिति (${attendancePercent}%) - बहुत अनुशासित", 0xFF2E7D32)
            attendancePercent >= 70 -> Pair("संतोषजनक उपस्थिति (${attendancePercent}%) - निरंतरता बनाए रखें", 0xFFF59E0B)
            else -> Pair("अनियमित उपस्थिति (${attendancePercent}%) - अखाड़े में नियमित भेजना सुनिश्चित करें", 0xFFD32F2F)
        }

        // Physical Fitness
        val run1600 = student?.time1600m?.ifBlank { "5:35" } ?: "5:35"
        val push = student?.pushups ?: 35
        val pull = student?.pullups ?: 8

        val physicalGrade = when {
            run1600 <= "5:30" && pull >= 8 -> "उत्कृष्ट (Grade A+) - भर्ती मेरिट हेतु तैयार"
            run1600 <= "5:45" && pull >= 6 -> "बहुत अच्छा (Grade A) - निरंतर सुधार में"
            else -> "संतोषजनक (Grade B) - सुबह की दौड़ में नियमितता आवश्यक"
        }

        val physicalText = "1600 मीटर दौड़: $run1600 मिनट | पुश-अप्स: $push रेप्स | बीम (पुल-अप्स): $pull"

        // Study stats
        val avgMockScore = if (testAttempts.isNotEmpty()) {
            val avg = testAttempts.map { it.score }.average()
            String.format("%.1f अंक", avg)
        } else {
            "72% (नियमित अभ्यास जारी)"
        }

        val studyGrade = if (testAttempts.isNotEmpty()) {
            val acc = testAttempts.map { it.accuracyPercentage }.average()
            if (acc >= 75) "उत्कृष्ट (Grade A)" else "सामान्य (Grade B)"
        } else {
            "संतोषजनक (Grade B+)"
        }

        val overallGrade = if (attendancePercent >= 80 && run1600 <= "5:45") "उत्कृष्ट (Grade A+)" else "प्रशंसनीय (Grade A)"

        val coachMsg = """
            आदरणीय ${father} जी,
            जय बजरंग अखाड़ा, मौरिकला गुफा में आपके सुपुत्र/सुपुत्री '${name}' का प्रशिक्षण निरंतर जारी है।
            कैडेट में वर्दी पहनने का गहरा जज्बा और मेहनत का उत्साह है।
            घर पर समय पर खान-पान (चना, गुड़, दूध) और रात को समय पर सोने का विशेष ध्यान रखें ताकि ग्राउंड पर शरीर को पूरी ऊर्जा मिल सके।
        """.trimIndent()

        val tips = listOf(
            "घर का पौष्टिक आहार दें: भीगे चने, गुड़, सोयाबीन और मौसमी फल।",
            "रात्रि में 10:00 बजे तक सोने के लिए कहें ताकि प्रातः 4:30 बजे ग्राउंड पहुँच सकें।",
            "कैडेट का मनोबल बढ़ाएं और अखाड़े की क्लास व ग्राउंड ट्रेनिंग में एक भी दिन अनुपस्थित न होने दें।"
        )

        val currentDateStr = java.text.SimpleDateFormat("dd MMMM yyyy", java.util.Locale("hi", "IN")).format(java.util.Date())

        return ParentProgressReport(
            studentName = name,
            fatherName = father,
            studentId = student?.studentId ?: "JBA-2026-001",
            village = village,
            targetGoalHindi = goal,
            attendanceSummaryHindi = attText,
            attendanceStatusColorHex = attColor,
            attendanceDaysCount = presentCount,
            attendancePercentage = attendancePercent,
            physicalFitnessSummaryHindi = physicalText,
            runningTime1600m = run1600,
            pushupsBeamHindi = "$push पुश-अप्स, $pull बीम",
            physicalGradeHindi = physicalGrade,
            studyProgressSummaryHindi = "लिखित परीक्षा के अध्यायों का अध्ययन व नियमित अभ्यास टेस्ट",
            mockTestAverageScoreHindi = avgMockScore,
            studyGradeHindi = studyGrade,
            overallDisciplineGradeHindi = overallGrade,
            coachMessageToParentsHindi = coachMsg,
            parentAdviceTipsHindi = tips,
            reportDateFormatted = currentDateStr
        )
    }
}
