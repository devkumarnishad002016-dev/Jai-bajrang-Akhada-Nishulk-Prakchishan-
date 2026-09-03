package com.example.data.mission

import com.example.data.model.Chapter
import com.example.data.model.StudentProfile
import com.example.data.model.StudyAttempt
import com.example.data.model.TestAttempt
import com.example.data.model.TrainingRecord
import com.example.data.model.WorkoutRecord

enum class MissionDuration(val days: Int, val titleHindi: String, val subtitleHindi: String) {
    DAYS_30(30, "30-दिवसीय क्रैश मिशन", "अंतिम अभ्यास व त्वरित तैयारी (Target Crash Course)"),
    DAYS_60(60, "60-दिवसीय मानक मिशन", "संतुलित फिजिकल व लिखित परीक्षा तैयारी (Standard Mission)"),
    DAYS_90(90, "90-दिवसीय बुनियादी मिशन", "शून्य से अंतिम चयन तक सम्पूर्ण तैयारी (Foundation Mission)")
}

data class DailyMissionTask(
    val id: String,
    val titleHindi: String,
    val category: String, // STUDY, WORKOUT, PRACTICE
    val description: String,
    val targetMetric: String,
    val isCompleted: Boolean = false
)

data class MissionDashboardState(
    val studentName: String,
    val recruitmentGoal: String,
    val selectedDuration: MissionDuration,
    val currentDayNumber: Int,
    val totalDays: Int,
    val completionPercentage: Int,
    val streakDays: Int,
    val dailyStudyTargetDone: Boolean,
    val dailyWorkoutTargetDone: Boolean,
    val dailyPracticeTargetDone: Boolean,
    val tasks: List<DailyMissionTask>,
    val motivationalQuoteHindi: String,
    val phaseTitleHindi: String
)

object MissionEngine {

    fun calculateMissionState(
        student: StudentProfile?,
        duration: MissionDuration = MissionDuration.DAYS_60,
        todayWorkout: WorkoutRecord? = null,
        todayTraining: TrainingRecord? = null,
        todayChapters: List<Chapter> = emptyList(),
        studentStudyAttempts: List<StudyAttempt> = emptyList(),
        studentTestAttempts: List<TestAttempt> = emptyList()
    ): MissionDashboardState {
        val goal = student?.recruitmentGoal ?: "CG Police"
        val totalDays = duration.days

        // Calculate active days since joining or capped to duration
        val streak = student?.attendanceStreakDays?.coerceAtLeast(0) ?: 0
        val dayNumber = ((streak % totalDays) + 1).coerceIn(1, totalDays)

        // Workout Target Done: today workout recorded or running distance >= 1.6km
        val workoutDone = (todayWorkout != null && todayWorkout.runningDistanceKm > 0) ||
                (todayTraining != null && todayTraining.runningDistanceKm > 0)

        // Study Target Done: any chapter marked today or completed
        val studyDone = todayChapters.any { it.isCompleted } || studentStudyAttempts.isNotEmpty()

        // Practice / Mock Target Done: student has at least one attempt or test
        val practiceDone = studentTestAttempts.isNotEmpty() || studentStudyAttempts.size >= 5

        // Completed targets today
        var completedCount = 0
        if (workoutDone) completedCount++
        if (studyDone) completedCount++
        if (practiceDone) completedCount++

        val baseProgressFromDays = ((dayNumber.toDouble() / totalDays.toDouble()) * 85.0).toInt()
        val dailyBonus = (completedCount * 5)
        val completionPercentage = (baseProgressFromDays + dailyBonus).coerceIn(0, 100)

        val tasks = when (goal) {
            "Indian Army" -> listOf(
                DailyMissionTask(
                    id = "task_run",
                    titleHindi = "1600m रनिंग ट्रायल एवं टाइमिंग चेक",
                    category = "WORKOUT",
                    description = "लक्ष्य: 5 मिनट 30 सेकंड (ग्रुप-1 के 60 अंक)",
                    targetMetric = "1600m",
                    isCompleted = workoutDone
                ),
                DailyMissionTask(
                    id = "task_beam",
                    titleHindi = "बीम (Pull-ups) 10 रेप्स & 40 पुशअप्स",
                    category = "WORKOUT",
                    description = "10 बीम = 40 अंक (आर्मी फिजिकल मेरिट हेतु)",
                    targetMetric = "10 Pull-ups",
                    isCompleted = (todayWorkout?.pullupsDone ?: 0) >= 8 || (todayTraining?.pullups ?: 0) >= 8
                ),
                DailyMissionTask(
                    id = "task_study",
                    titleHindi = "सामान्य ज्ञान व सामान्य विज्ञान 30 प्रश्न",
                    category = "STUDY",
                    description = "आर्मी जीडी परीक्षा हेतु विज्ञान सूत्र एवं भारतीय इतिहास",
                    targetMetric = "30 प्रश्न",
                    isCompleted = studyDone
                ),
                DailyMissionTask(
                    id = "task_mock",
                    titleHindi = "दैनिक 20-प्रश्नों का स्पीड टेस्ट",
                    category = "PRACTICE",
                    description = "समय प्रबंधन एवं 0.50 नेगेटिव मार्किंग का अभ्यास",
                    targetMetric = "1 टेस्ट",
                    isCompleted = practiceDone
                )
            )
            "CG Police" -> listOf(
                DailyMissionTask(
                    id = "task_cg_run",
                    titleHindi = "800m रनिंग एवं 100m स्प्रिंट अभ्यास",
                    category = "WORKOUT",
                    description = "800m समय: < 2 मिनट 30 सेकंड (14-20 अंक)",
                    targetMetric = "800m + 100m",
                    isCompleted = workoutDone
                ),
                DailyMissionTask(
                    id = "task_cg_jump",
                    titleHindi = "लंबी कूद (Long Jump) & गोला फेंक (Shot Put)",
                    category = "WORKOUT",
                    description = "लंबी कूद लक्ष्य: 5.00+ मीटर | गोला फेंक: 8.5+ मीटर",
                    targetMetric = "5m Jump / 8.5m Throw",
                    isCompleted = (student?.longJumpFeet ?: 0.0) >= 14.0
                ),
                DailyMissionTask(
                    id = "task_cg_study",
                    titleHindi = "छत्तीसगढ़ सामान्य ज्ञान एवं समसामयिकी",
                    category = "STUDY",
                    description = "छत्तीसगढ़ इतिहास, भूगोल, जनजाति व समसामयिक घटनाएं",
                    targetMetric = "25 प्रश्न",
                    isCompleted = studyDone
                ),
                DailyMissionTask(
                    id = "task_cg_practice",
                    titleHindi = "गणित व तर्कशक्ति 20 बहुविकल्पीय प्रश्न",
                    category = "PRACTICE",
                    description = "प्रतिशत, लाभ-हानि, दिशा ज्ञान व कोडिंग-डिकोडिंग",
                    targetMetric = "20 प्रश्न",
                    isCompleted = practiceDone
                )
            )
            else -> listOf(
                DailyMissionTask(
                    id = "task_ssc_run",
                    titleHindi = "5 KM एंड्योरेंस रनिंग (SSC GD / BSF / CRPF)",
                    category = "WORKOUT",
                    description = "24 मिनट में 5 किलोमीटर की नियमित तैयारी",
                    targetMetric = "5 KM",
                    isCompleted = workoutDone
                ),
                DailyMissionTask(
                    id = "task_ssc_math",
                    titleHindi = "प्रारम्भिक अंकगणित (Arithmetic Core)",
                    category = "STUDY",
                    description = "संख्या पद्धति, सरलीकरण व कार्य-समय",
                    targetMetric = "30 मिनट नोट्स",
                    isCompleted = studyDone
                ),
                DailyMissionTask(
                    id = "task_ssc_reasoning",
                    titleHindi = "तर्कशक्ति (Reasoning) अभ्यास सेट",
                    category = "STUDY",
                    description = "सादृश्यता, श्रृंखला एवं रक्त संबंध",
                    targetMetric = "20 प्रश्न",
                    isCompleted = studyDone
                ),
                DailyMissionTask(
                    id = "task_ssc_mock",
                    titleHindi = "दैनिक मॉक टेस्ट एवं कमजोर क्षेत्रों का विश्लेषण",
                    category = "PRACTICE",
                    description = "मॉक टेस्ट स्कोर 75%+ का लक्ष्य रखें",
                    targetMetric = "1 टेस्ट",
                    isCompleted = practiceDone
                )
            )
        }

        val phaseTitle = when {
            dayNumber <= totalDays / 3 -> "चरण 1: बुनियादी स्टैमिना एवं मूलभूत ज्ञान (Foundation)"
            dayNumber <= (totalDays * 2) / 3 -> "चरण 2: स्पीड, स्ट्रेंथ एवं टॉपिक-वार महारत (Speed & Strength)"
            else -> "चरण 3: परीक्षा अनुरूप फुल सिमुलेशन एवं अंतिम मेरिट (Peak Performance)"
        }

        val quote = when ((dayNumber % 4)) {
            0 -> "‘पसीना ग्राउंड पर बहेगा तो परीक्षा और फिजिकल में आंसू नहीं बहेंगे!’"
            1 -> "‘अनुशासन ही वह पुल है जो लक्ष्य को सफलता में बदलता है।’"
            2 -> "‘मौरिकला गुफा के अखाड़े से निकलकर देश की रक्षा का संकल्प पूरा करना है।’"
            else -> "‘हर सुबह का 1600 मीटर का चक्कर तुम्हें वर्दी के एक कदम और करीब लाता है।’"
        }

        return MissionDashboardState(
            studentName = student?.fullName ?: "कैडेट",
            recruitmentGoal = goal,
            selectedDuration = duration,
            currentDayNumber = dayNumber,
            totalDays = totalDays,
            completionPercentage = completionPercentage,
            streakDays = streak,
            dailyStudyTargetDone = studyDone,
            dailyWorkoutTargetDone = workoutDone,
            dailyPracticeTargetDone = practiceDone,
            tasks = tasks,
            motivationalQuoteHindi = quote,
            phaseTitleHindi = phaseTitle
        )
    }
}
