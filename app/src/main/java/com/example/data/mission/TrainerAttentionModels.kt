package com.example.data.mission

import com.example.data.model.AttendanceRecord
import com.example.data.model.StudentProfile
import com.example.data.model.TestAttempt
import com.example.data.model.TrainingRecord

enum class CadetAttentionIssueType(val titleHindi: String, val badgeColorHex: Long) {
    ABSENT_TODAY("आज अनुपस्थित (Absent)", 0xFFD32F2F),
    WEAK_PHYSICAL("कमजोर फिजिकल (Weak 1600m/Beam)", 0xFFEA580C),
    LOW_MOCK_SCORE("कम मॉक टेस्ट स्कोर (<50%)", 0xFF9333EA),
    LOW_STUDY_PROGRESS("अध्ययन में पिछड़ा हुआ (<40%)", 0xFFD97706),
    HIGH_PERFORMER("स्टार परफॉर्मर (Star Performer)", 0xFF16A34A)
}

data class CadetAttentionItem(
    val student: StudentProfile,
    val issueType: CadetAttentionIssueType,
    val issueDescriptionHindi: String,
    val metricHighlight: String,
    val suggestedCoachActionHindi: String
)

object TrainerAttentionEngine {

    fun generateAttentionList(
        allStudents: List<StudentProfile>,
        allAttendance: List<AttendanceRecord>,
        allTraining: List<TrainingRecord>,
        allTestAttempts: List<TestAttempt>,
        todayDateStr: String
    ): List<CadetAttentionItem> {
        val attentionList = mutableListOf<CadetAttentionItem>()

        val todayAttendanceMap = allAttendance
            .filter { it.date == todayDateStr }
            .associateBy { it.studentId }

        for (student in allStudents) {
            // 1. Check Attendance: Absent or Unmarked today
            val todayRecord = todayAttendanceMap[student.studentId]
            if (todayRecord != null && todayRecord.status.equals("Absent", ignoreCase = true)) {
                attentionList.add(
                    CadetAttentionItem(
                        student = student,
                        issueType = CadetAttentionIssueType.ABSENT_TODAY,
                        issueDescriptionHindi = "आज ग्राउंड ट्रेनिंग में अनुपस्थित रहे।",
                        metricHighlight = "आज की उपस्थिति: गैरहाजिर",
                        suggestedCoachActionHindi = "अभिभावक को कॉल करें या ऐप से अनुपस्थिति अलर्ट भेजें।"
                    )
                )
            } else if (todayRecord == null && allAttendance.isNotEmpty()) {
                // Not marked
                attentionList.add(
                    CadetAttentionItem(
                        student = student,
                        issueType = CadetAttentionIssueType.ABSENT_TODAY,
                        issueDescriptionHindi = "आज की हाजिरी अभी तक दर्ज नहीं हुई है।",
                        metricHighlight = "उपस्थिति: पेंडिंग",
                        suggestedCoachActionHindi = "चेक करें कि कैडेट ग्राउंड पर उपस्थित हैं या नहीं।"
                    )
                )
            }

            // 2. Check Physical Performance: 1600m time > 5:45 or Beam < 6
            val time = student.time1600m.trim()
            val isSlowRunning = time.isNotBlank() && time != "--" && time > "5:45"
            val isLowBeam = student.pullups > 0 && student.pullups < 6
            val isLowPushups = student.pushups > 0 && student.pushups < 25

            if (isSlowRunning || isLowBeam || isLowPushups) {
                val issue = when {
                    isSlowRunning && isLowBeam -> "1600m समय ($time) अधिक है एवं बीम (${student.pullups}) न्यूनतम से कम हैं।"
                    isSlowRunning -> "1600m समय ($time) ग्रुप-2 मानक (>5:45) से बाहर है।"
                    else -> "बीम (${student.pullups}) या पुशअप्स (${student.pushups}) में अतिरिक्त स्ट्रेंथ की आवश्यकता है।"
                }
                attentionList.add(
                    CadetAttentionItem(
                        student = student,
                        issueType = CadetAttentionIssueType.WEAK_PHYSICAL,
                        issueDescriptionHindi = issue,
                        metricHighlight = "1600m: $time | बीम: ${student.pullups}",
                        suggestedCoachActionHindi = "शाम के सत्र में विशेष 400m इंटरवल एवं बीम होल्डिंग कराएं।"
                    )
                )
            }

            // 3. Check Mock Test Scores: Latest score < 50%
            val studentAttempts = allTestAttempts.filter { it.studentId == student.studentId }
            if (studentAttempts.isNotEmpty()) {
                val latest = studentAttempts.maxByOrNull { it.id }
                if (latest != null && latest.score < (latest.maxScore * 0.50)) {
                    attentionList.add(
                        CadetAttentionItem(
                            student = student,
                            issueType = CadetAttentionIssueType.LOW_MOCK_SCORE,
                            issueDescriptionHindi = "हालिया मॉक टेस्ट '${latest.testTitle}' में स्कोर ${String.format("%.1f", latest.score)}/${latest.maxScore.toInt()} (<50%) रहा।",
                            metricHighlight = "कमजोर विषय: ${latest.weakArea}",
                            suggestedCoachActionHindi = "कमजोर विषय (${latest.weakArea}) के शॉर्ट नोट्स पढ़ने का निर्देश दें।"
                        )
                    )
                }
            }

            // 4. Check Low Study Progress
            if (student.studyTargetPercentage in 1..40) {
                attentionList.add(
                    CadetAttentionItem(
                        student = student,
                        issueType = CadetAttentionIssueType.LOW_STUDY_PROGRESS,
                        issueDescriptionHindi = "अध्ययन सिलेबस का केवल ${student.studyTargetPercentage}% पूरा हुआ है।",
                        metricHighlight = "सिलेबस प्रगति: ${student.studyTargetPercentage}%",
                        suggestedCoachActionHindi = "दैनिक 30 मिनट डिजिटल लाइब्रेरी से अध्ययन करने हेतु प्रेरित करें।"
                    )
                )
            }
        }

        return attentionList
    }
}
