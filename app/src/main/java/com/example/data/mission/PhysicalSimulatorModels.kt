package com.example.data.mission

import com.example.data.model.StudentProfile

data class PhysicalSimulatorInput(
    val examTarget: String = "Indian Army", // "Indian Army", "CG Police", "SSC GD"
    val time1600mMinutes: Int = 5,
    val time1600mSeconds: Int = 32,
    val time100mSeconds: Double = 13.2,
    val time800mMinutes: Int = 2,
    val time800mSeconds: Int = 18,
    val pullupsBeam: Int = 10,
    val pushupsCount: Int = 40,
    val situpsCount: Int = 45,
    val longJumpMeters: Double = 5.2,
    val highJumpMeters: Double = 1.45,
    val shotPutMeters: Double = 8.8,
    val ditchJumpPassed: Boolean = true,
    val zigZagBalancePassed: Boolean = true,
    val gender: String = "Male"
) {
    val time1600mTotalSeconds: Int get() = (time1600mMinutes * 60) + time1600mSeconds
    val time800mTotalSeconds: Int get() = (time800mMinutes * 60) + time800mSeconds
    val time1600mFormatted: String get() = String.format("%d:%02d", time1600mMinutes, time1600mSeconds)
    val time800mFormatted: String get() = String.format("%d:%02d", time800mMinutes, time800mSeconds)
}

data class MetricScoreResult(
    val eventNameHindi: String,
    val eventNameEnglish: String,
    val userValueFormatted: String,
    val marksAwarded: Int,
    val maxMarks: Int,
    val statusHindi: String,
    val benchmarkDescription: String,
    val isQualified: Boolean
)

data class MetricImprovementDelta(
    val metricName: String,
    val currentValue: String,
    val previousValue: String,
    val improvementText: String,
    val isBetter: Boolean
)

data class PhysicalSimulationReport(
    val examTarget: String,
    val totalPhysicalMarks: Int,
    val maxPhysicalMarks: Int,
    val percentage: Int,
    val qualificationStatusHindi: String, // "योग्य (QUALIFIED)", "बॉर्डरलाइन (BORDERLINE)", "अयोग्य (NEEDS WORK)"
    val statusBadgeColorHex: Long,
    val metricBreakdown: List<MetricScoreResult>,
    val comparisonWithPrevious: List<MetricImprovementDelta>,
    val coachGuidanceHindi: String,
    val nextActionHindi: String
)

object PhysicalTestSimulatorEngine {

    fun simulate(
        input: PhysicalSimulatorInput,
        student: StudentProfile? = null
    ): PhysicalSimulationReport {
        return when (input.examTarget) {
            "CG Police" -> simulateCgPolice(input, student)
            "SSC GD" -> simulateSscGd(input, student)
            else -> simulateIndianArmy(input, student)
        }
    }

    private fun simulateIndianArmy(
        input: PhysicalSimulatorInput,
        student: StudentProfile?
    ): PhysicalSimulationReport {
        val total1600s = input.time1600mTotalSeconds

        // 1600m Group 1: <= 5m30s (330s) -> 60 marks
        // Group 2: 5m31s to 5m45s (345s) -> 48 marks
        // > 5m45s -> 0 marks (Fail)
        val run1600Marks: Int
        val run1600Status: String
        val run1600Qualified: Boolean

        when {
            total1600s <= 330 -> {
                run1600Marks = 60
                run1600Status = "ग्रुप 1 (शानदार) - 60/60 अंक"
                run1600Qualified = true
            }
            total1600s <= 345 -> {
                run1600Marks = 48
                run1600Status = "ग्रुप 2 (पास) - 48/60 अंक"
                run1600Qualified = true
            }
            else -> {
                run1600Marks = 0
                run1600Status = "समय सीमा से बाहर (> 5:45) - 0 अंक"
                run1600Qualified = false
            }
        }

        // Pullups (Beam): 10 reps = 40m, 9 = 33m, 8 = 27m, 7 = 21m, 6 = 16m, < 6 = 0m (Fail)
        val pullupsMarks: Int
        val pullupsStatus: String
        val pullupsQualified: Boolean

        when {
            input.pullupsBeam >= 10 -> {
                pullupsMarks = 40
                pullupsStatus = "10 बीम (उत्कृष्ट) - 40/40 अंक"
                pullupsQualified = true
            }
            input.pullupsBeam == 9 -> {
                pullupsMarks = 33
                pullupsStatus = "9 बीम - 33/40 अंक"
                pullupsQualified = true
            }
            input.pullupsBeam == 8 -> {
                pullupsMarks = 27
                pullupsStatus = "8 बीम - 27/40 अंक"
                pullupsQualified = true
            }
            input.pullupsBeam == 7 -> {
                pullupsMarks = 21
                pullupsStatus = "7 बीम - 21/40 अंक"
                pullupsQualified = true
            }
            input.pullupsBeam == 6 -> {
                pullupsMarks = 16
                pullupsStatus = "6 बीम (न्यूनतम पास) - 16/40 अंक"
                pullupsQualified = true
            }
            else -> {
                pullupsMarks = 0
                pullupsStatus = "अनुत्तीर्ण (< 6 बीम) - 0 अंक"
                pullupsQualified = false
            }
        }

        val totalMarks = run1600Marks + pullupsMarks
        val isOverallPass = run1600Qualified && pullupsQualified && input.ditchJumpPassed && input.zigZagBalancePassed

        val qualStatusHindi = when {
            isOverallPass && totalMarks >= 80 -> "योग्य (QUALIFIED - EXCELLENT)"
            isOverallPass -> "योग्य (QUALIFIED - PASS)"
            else -> "सुधार आवश्यक (NEEDS WORK)"
        }

        val badgeColor = when {
            isOverallPass && totalMarks >= 80 -> 0xFF2E7D32 // Green
            isOverallPass -> 0xFFF59E0B // Amber
            else -> 0xFFD32F2F // Red
        }

        val breakdown = listOf(
            MetricScoreResult(
                eventNameHindi = "1600 मीटर दौड़",
                eventNameEnglish = "1600m Running",
                userValueFormatted = input.time1600mFormatted,
                marksAwarded = run1600Marks,
                maxMarks = 60,
                statusHindi = run1600Status,
                benchmarkDescription = "ग्रुप-1: <= 5:30 (60 अंक) | ग्रुप-2: <= 5:45 (48 अंक)",
                isQualified = run1600Qualified
            ),
            MetricScoreResult(
                eventNameHindi = "बीम (पुल-अप्स)",
                eventNameEnglish = "Pull-ups (Beam)",
                userValueFormatted = "${input.pullupsBeam} बीम",
                marksAwarded = pullupsMarks,
                maxMarks = 40,
                statusHindi = pullupsStatus,
                benchmarkDescription = "10 बीम = 40 अंक, 9 = 33, 8 = 27, 7 = 21, 6 = 16 अंक",
                isQualified = pullupsQualified
            ),
            MetricScoreResult(
                eventNameHindi = "9-फीट गड्ढा कूद",
                eventNameEnglish = "9-Feet Ditch Jump",
                userValueFormatted = if (input.ditchJumpPassed) "उत्तीर्ण (PASS)" else "अनुत्तीर्ण (FAIL)",
                marksAwarded = if (input.ditchJumpPassed) 0 else 0,
                maxMarks = 0,
                statusHindi = if (input.ditchJumpPassed) "क्वालिफाइड (मानक पूर्ण)" else "अयोग्य",
                benchmarkDescription = "केवल क्वालिफाइंग (Qualifying Only)",
                isQualified = input.ditchJumpPassed
            ),
            MetricScoreResult(
                eventNameHindi = "ज़िग-ज़ैग बैलेंस",
                eventNameEnglish = "Zig-Zag Balance",
                userValueFormatted = if (input.zigZagBalancePassed) "उत्तीर्ण (PASS)" else "अनुत्तीर्ण (FAIL)",
                marksAwarded = if (input.zigZagBalancePassed) 0 else 0,
                maxMarks = 0,
                statusHindi = if (input.zigZagBalancePassed) "क्वालिफाइड (संतुलन सही)" else "अयोग्य",
                benchmarkDescription = "केवल क्वालिफाइंग (Qualifying Only)",
                isQualified = input.zigZagBalancePassed
            )
        )

        // Comparison with previous student records
        val comparison = buildComparison(input, student)

        val advice = when {
            totalMarks == 100 -> "अद्भुत प्रदर्शन! आप 100/100 फिजिकल मेरिट में हैं। इसी गति और फॉर्म को रैली तक बनाए रखें।"
            run1600Marks < 60 && pullupsMarks == 40 -> "बीम में पूरे 40 अंक हैं! 1600m में केवल कुछ सेकंड घटाकर ग्रुप-1 (5:30) का लक्ष्य पूरा करें।"
            pullupsMarks < 40 -> "बीम की संख्या बढ़ाएं। नियमित ग्रिप स्ट्रेंथ और लटकने का अभ्यास करें ताकि पूरे 40 अंक मिलें।"
            else -> "प्रतिदिन सुबह 1600m पेस रनिंग और शाम को कोर वर्कआउट जारी रखें।"
        }

        return PhysicalSimulationReport(
            examTarget = "Indian Army Agniveer GD",
            totalPhysicalMarks = totalMarks,
            maxPhysicalMarks = 100,
            percentage = totalMarks,
            qualificationStatusHindi = qualStatusHindi,
            statusBadgeColorHex = badgeColor,
            metricBreakdown = breakdown,
            comparisonWithPrevious = comparison,
            coachGuidanceHindi = advice,
            nextActionHindi = if (totalMarks >= 80) "लिखित परीक्षा की तैयारी पर विशेष ध्यान दें।" else "ग्राउंड पर 1600m इंटरवल ट्रेनिंग करें।"
        )
    }

    private fun simulateCgPolice(
        input: PhysicalSimulatorInput,
        student: StudentProfile?
    ): PhysicalSimulationReport {
        // CG Police Constable 5 Events of 20 marks each = 100 Marks
        // 1. 100m Sprint: <= 12s -> 20m; <= 14s -> 14m; <= 16s -> 7m; > 16s -> 0m
        val marks100m = when {
            input.time100mSeconds <= 12.0 -> 20
            input.time100mSeconds <= 14.0 -> 14
            input.time100mSeconds <= 16.0 -> 7
            else -> 0
        }

        // 2. 800m Run: <= 2:00 (120s) -> 20m; <= 2:30 (150s) -> 14m; <= 3:00 (180s) -> 7m; > 3:00 -> 0m
        val marks800m = when {
            input.time800mTotalSeconds <= 120 -> 20
            input.time800mTotalSeconds <= 150 -> 14
            input.time800mTotalSeconds <= 180 -> 7
            else -> 0
        }

        // 3. Long Jump: >= 5.50m -> 20m; >= 5.00m -> 14m; >= 4.50m -> 7m; < 4.50m -> 0m
        val marksLongJump = when {
            input.longJumpMeters >= 5.50 -> 20
            input.longJumpMeters >= 5.00 -> 14
            input.longJumpMeters >= 4.50 -> 7
            else -> 0
        }

        // 4. High Jump: >= 1.50m -> 20m; >= 1.40m -> 14m; >= 1.30m -> 7m; < 1.30m -> 0m
        val marksHighJump = when {
            input.highJumpMeters >= 1.50 -> 20
            input.highJumpMeters >= 1.40 -> 14
            input.highJumpMeters >= 1.30 -> 7
            else -> 0
        }

        // 5. Shot Put: >= 9.00m -> 20m; >= 8.00m -> 14m; >= 7.00m -> 7m; < 7.00m -> 0m
        val marksShotPut = when {
            input.shotPutMeters >= 9.00 -> 20
            input.shotPutMeters >= 8.00 -> 14
            input.shotPutMeters >= 7.00 -> 7
            else -> 0
        }

        val total = marks100m + marks800m + marksLongJump + marksHighJump + marksShotPut
        val isPass = total >= 50 // Minimum 50% physical standard

        val breakdown = listOf(
            MetricScoreResult("100 मीटर दौड़", "100m Sprint", "${input.time100mSeconds}s", marks100m, 20, "$marks100m / 20 अंक", "<=12s (20), <=14s (14), <=16s (7)", marks100m > 0),
            MetricScoreResult("800 मीटर दौड़", "800m Run", input.time800mFormatted, marks800m, 20, "$marks800m / 20 अंक", "<=2:00 (20), <=2:30 (14), <=3:00 (7)", marks800m > 0),
            MetricScoreResult("लंबी कूद (Long Jump)", "Long Jump", "${input.longJumpMeters}m", marksLongJump, 20, "$marksLongJump / 20 अंक", ">=5.50m (20), >=5.00m (14), >=4.50m (7)", marksLongJump > 0),
            MetricScoreResult("ऊंची कूद (High Jump)", "High Jump", "${input.highJumpMeters}m", marksHighJump, 20, "$marksHighJump / 20 अंक", ">=1.50m (20), >=1.40m (14), >=1.30m (7)", marksHighJump > 0),
            MetricScoreResult("गोला फेंक (Shot Put)", "Shot Put", "${input.shotPutMeters}m", marksShotPut, 20, "$marksShotPut / 20 अंक", ">=9.00m (20), >=8.00m (14), >=7.00m (7)", marksShotPut > 0)
        )

        val comparison = buildComparison(input, student)

        return PhysicalSimulationReport(
            examTarget = "CG Police Constable",
            totalPhysicalMarks = total,
            maxPhysicalMarks = 100,
            percentage = total,
            qualificationStatusHindi = if (total >= 70) "योग्य (उत्कृष्ट मेरिट)" else if (isPass) "योग्य (संतोषजनक)" else "सुधार आवश्यक (कम स्कोर)",
            statusBadgeColorHex = if (total >= 70) 0xFF2E7D32 else if (isPass) 0xFFF59E0B else 0xFFD32F2F,
            metricBreakdown = breakdown,
            comparisonWithPrevious = comparison,
            coachGuidanceHindi = "छत्तीसगढ़ पुलिस भर्ती में 5 स्पर्धाओं का योग मेरिट तय करता है। कमजोर स्पर्धा पर फोकस करें।",
            nextActionHindi = "800m एवं लंबी कूद के टेक-ऑफ का विशेष अभ्यास करें।"
        )
    }

    private fun simulateSscGd(
        input: PhysicalSimulatorInput,
        student: StudentProfile?
    ): PhysicalSimulationReport {
        // SSC GD PET:
        // Male: 5 KM in 24:00 minutes (Qualifying)
        // Female: 1.6 KM in 8:30 minutes (Qualifying)
        val isMale = input.gender.equals("Male", ignoreCase = true)
        val targetDescription = if (isMale) "5 KM दौड़ 24:00 मिनट में" else "1.6 KM दौड़ 8:30 मिनट में"

        val runQualified = if (isMale) {
            // Using student or entered 1600m estimated to 5km
            input.time1600mTotalSeconds <= (24 * 60)
        } else {
            input.time1600mTotalSeconds <= (8 * 60 + 30)
        }

        val totalMarks = if (runQualified) 100 else 0

        val breakdown = listOf(
            MetricScoreResult(
                eventNameHindi = if (isMale) "5 किलोमीटर दौड़" else "1.6 किलोमीटर दौड़",
                eventNameEnglish = if (isMale) "5 KM Run" else "1.6 KM Run",
                userValueFormatted = input.time1600mFormatted,
                marksAwarded = if (runQualified) 100 else 0,
                maxMarks = 100,
                statusHindi = if (runQualified) "क्वालिफाइड (QUALIFIED)" else "समय सीमा से बाहर",
                benchmarkDescription = targetDescription,
                isQualified = runQualified
            ),
            MetricScoreResult(
                eventNameHindi = "पुश-अप्स एवं एंड्योरेंस",
                eventNameEnglish = "Push-ups & Endurance",
                userValueFormatted = "${input.pushupsCount} रेप्स",
                marksAwarded = 0,
                maxMarks = 0,
                statusHindi = "संतोषजनक स्टैमिना",
                benchmarkDescription = "ग्राउंड स्ट्रेंथ रखरखाव हेतु",
                isQualified = true
            )
        )

        val comparison = buildComparison(input, student)

        return PhysicalSimulationReport(
            examTarget = "SSC GD / BSF / CRPF / CISF",
            totalPhysicalMarks = totalMarks,
            maxPhysicalMarks = 100,
            percentage = totalMarks,
            qualificationStatusHindi = if (runQualified) "योग्य (PET QUALIFIED)" else "अयोग्य (समय सीमा से अधिक)",
            statusBadgeColorHex = if (runQualified) 0xFF2E7D32 else 0xFFD32F2F,
            metricBreakdown = breakdown,
            comparisonWithPrevious = comparison,
            coachGuidanceHindi = "एसएससी जीडी में फिजिकल केवल क्वालिफाइंग है। लंबी दूरी की लगातार रनिंग से स्टैमिना मजबूत करें।",
            nextActionHindi = "सप्ताह में कम से कम 3 दिन 5 किमी की पेस रनिंग करें।"
        )
    }

    private fun buildComparison(
        input: PhysicalSimulatorInput,
        student: StudentProfile?
    ): List<MetricImprovementDelta> {
        val list = mutableListOf<MetricImprovementDelta>()
        if (student == null) return list

        // 1600m comparison
        val prev1600 = student.time1600m.trim()
        if (prev1600.isNotBlank() && prev1600 != "--") {
            val curr1600 = input.time1600mFormatted
            val isBetter = curr1600 < prev1600
            val deltaText = if (isBetter) "सुधार: समय कम हुआ (Faster)" else "समान / पूर्व समय"
            list.add(
                MetricImprovementDelta(
                    metricName = "1600m रनिंग टाइम",
                    currentValue = curr1600,
                    previousValue = prev1600,
                    improvementText = deltaText,
                    isBetter = isBetter
                )
            )
        }

        // Pushups comparison
        val prevPushups = student.pushups
        val currPushups = input.pushupsCount
        val pushDiff = currPushups - prevPushups
        list.add(
            MetricImprovementDelta(
                metricName = "पुश-अप्स (Push-ups)",
                currentValue = "$currPushups",
                previousValue = "$prevPushups",
                improvementText = if (pushDiff >= 0) "+$pushDiff सुधार" else "$pushDiff कम",
                isBetter = pushDiff >= 0
            )
        )

        // Pullups comparison
        val prevPullups = student.pullups
        val currPullups = input.pullupsBeam
        val pullDiff = currPullups - prevPullups
        list.add(
            MetricImprovementDelta(
                metricName = "बीम / पुल-अप्स (Beam)",
                currentValue = "$currPullups",
                previousValue = "$prevPullups",
                improvementText = if (pullDiff >= 0) "+$pullDiff बीम" else "$pullDiff बीम",
                isBetter = pullDiff >= 0
            )
        )

        return list
    }
}
