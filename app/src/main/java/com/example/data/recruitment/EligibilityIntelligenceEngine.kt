package com.example.data.recruitment

import com.example.data.model.Notice
import com.example.data.model.StudentProfile
import com.example.data.model.StudyAttempt
import com.example.data.model.TestAttempt
import com.example.data.model.WorkoutRecord

object EligibilityIntelligenceEngine {

    /**
     * Evaluates the student's eligibility against a specific recruitment category.
     */
    fun evaluateEligibility(
        student: StudentProfile?,
        targetProfile: RecruitmentCategoryProfile
    ): StudentEligibilityReport {
        if (student == null) {
            return StudentEligibilityReport(
                studentId = "",
                recruitmentName = targetProfile.nameHindi,
                category = targetProfile.id,
                overallStatus = EligibilityStatus.INSUFFICIENT_DATA,
                overallScorePercentage = 0,
                criteria = emptyList(),
                actionAdviceHindi = "अपनी Profile पूरी करें ताकि सटीक Eligibility Guidance दी जा सके।"
            )
        }

        val criteriaList = mutableListOf<EligibilityCriterionResult>()
        var missingDataCount = 0
        var failCount = 0
        var partialCount = 0
        var passCount = 0

        // 1. Age Verification
        val studentAge = if (student.age > 0) student.age.toDouble() else calculateAgeFromDob(student.dob)
        if (studentAge <= 0) {
            missingDataCount++
            criteriaList.add(
                EligibilityCriterionResult(
                    criterionName = "आयु सीमा (Age Criteria)",
                    isMet = null,
                    studentValue = "दर्ज नहीं",
                    requiredValue = "${targetProfile.minAge} से ${targetProfile.maxAge} वर्ष",
                    statusTextHindi = "डेटा अपूर्ण",
                    guidanceHindi = "प्रोफाइल में जन्मतिथि (DOB) दर्ज करें।"
                )
            )
        } else if (studentAge in targetProfile.minAge..targetProfile.maxAge) {
            passCount++
            criteriaList.add(
                EligibilityCriterionResult(
                    criterionName = "आयु सीमा (Age Criteria)",
                    isMet = true,
                    studentValue = "${studentAge.toInt()} वर्ष",
                    requiredValue = "${targetProfile.minAge} से ${targetProfile.maxAge} वर्ष",
                    statusTextHindi = "आयु मानक अनुसार योग्य",
                    guidanceHindi = "आपकी आयु सामान्य वर्ग की सीमा में बिल्कुल सही है।"
                )
            )
        } else if (studentAge > targetProfile.maxAge && studentAge <= targetProfile.maxAge + 5.0) {
            partialCount++
            criteriaList.add(
                EligibilityCriterionResult(
                    criterionName = "आयु सीमा (Age Criteria)",
                    isMet = null,
                    studentValue = "${studentAge.toInt()} वर्ष",
                    requiredValue = "${targetProfile.minAge} से ${targetProfile.maxAge} वर्ष (+छूट)",
                    statusTextHindi = "आरक्षण छूट पर निर्भर",
                    guidanceHindi = "आपकी आयु सामान्य सीमा से अधिक है, लेकिन OBC/SC/ST छूट के तहत आप पात्र हो सकते हैं। ऑफिशियल नोटिफिकेशन जांचें।"
                )
            )
        } else {
            failCount++
            criteriaList.add(
                EligibilityCriterionResult(
                    criterionName = "आयु सीमा (Age Criteria)",
                    isMet = false,
                    studentValue = "${studentAge.toInt()} वर्ष",
                    requiredValue = "${targetProfile.minAge} से ${targetProfile.maxAge} वर्ष",
                    statusTextHindi = "आयु सीमा से बाहर",
                    guidanceHindi = "आपकी आयु इस पद की निर्धारित सीमा से बाहर प्रतीत होती है।"
                )
            )
        }

        // 2. Educational Qualification
        val studentEdu = student.education.trim()
        val studentEduRank = parseEducationRank(studentEdu)
        if (studentEdu.isBlank()) {
            missingDataCount++
            criteriaList.add(
                EligibilityCriterionResult(
                    criterionName = "शैक्षणिक योग्यता (Education)",
                    isMet = null,
                    studentValue = "दर्ज नहीं",
                    requiredValue = targetProfile.requiredEducationHindi,
                    statusTextHindi = "डेटा अपूर्ण",
                    guidanceHindi = "प्रोफाइल में अपनी उच्चतम शैक्षणिक योग्यता दर्ज करें।"
                )
            )
        } else if (studentEduRank >= targetProfile.minEducationLevelRank) {
            passCount++
            criteriaList.add(
                EligibilityCriterionResult(
                    criterionName = "शैक्षणिक योग्यता (Education)",
                    isMet = true,
                    studentValue = studentEdu,
                    requiredValue = targetProfile.requiredEducationHindi,
                    statusTextHindi = "शैक्षणिक योग्यता पूर्ण",
                    guidanceHindi = "आपकी शैक्षणिक योग्यता इस पद के आवश्यक न्यूनतम स्तर को पूरा करती है।"
                )
            )
        } else {
            failCount++
            criteriaList.add(
                EligibilityCriterionResult(
                    criterionName = "शैक्षणिक योग्यता (Education)",
                    isMet = false,
                    studentValue = studentEdu,
                    requiredValue = targetProfile.requiredEducationHindi,
                    statusTextHindi = "योग्यता अपूर्ण",
                    guidanceHindi = "इस पद हेतु न्यूनतम ${targetProfile.requiredEducationHindi} अनिवार्य है।"
                )
            )
        }

        // 3. Height Requirement
        val isFemale = student.gender.equals("Female", ignoreCase = true) || student.gender.contains("महिला")
        val requiredHeight = if (isFemale) targetProfile.minHeightFemaleCm else targetProfile.minHeightMaleCm
        val studentHeight = student.heightCm

        if (studentHeight <= 0) {
            missingDataCount++
            criteriaList.add(
                EligibilityCriterionResult(
                    criterionName = "शारीरिक ऊंचाई (Height)",
                    isMet = null,
                    studentValue = "दर्ज नहीं",
                    requiredValue = "न्यूनतम ${requiredHeight} cm",
                    statusTextHindi = "डेटा अपूर्ण",
                    guidanceHindi = "प्रोफाइल में अपनी सही ऊंचाई (cm) दर्ज करें।"
                )
            )
        } else if (studentHeight >= requiredHeight) {
            passCount++
            criteriaList.add(
                EligibilityCriterionResult(
                    criterionName = "शारीरिक ऊंचाई (Height)",
                    isMet = true,
                    studentValue = "${studentHeight} cm",
                    requiredValue = "न्यूनतम ${requiredHeight} cm",
                    statusTextHindi = "ऊंचाई मानक पूर्ण",
                    guidanceHindi = "आपकी ऊंचाई निर्धारित मानक के अनुसार पूर्णतः योग्य है।"
                )
            )
        } else if (studentHeight >= requiredHeight - 3.0) {
            partialCount++
            criteriaList.add(
                EligibilityCriterionResult(
                    criterionName = "शारीरिक ऊंचाई (Height)",
                    isMet = null,
                    studentValue = "${studentHeight} cm",
                    requiredValue = "न्यूनतम ${requiredHeight} cm (ST छूट)",
                    statusTextHindi = "क्षेत्रीय / वर्ग छूट देखें",
                    guidanceHindi = "आपकी ऊंचाई सामान्य मानक से थोड़ी कम है। ST या पहाड़ी क्षेत्र छूट के तहत पात्रता जांचें।"
                )
            )
        } else {
            failCount++
            criteriaList.add(
                EligibilityCriterionResult(
                    criterionName = "शारीरिक ऊंचाई (Height)",
                    isMet = false,
                    studentValue = "${studentHeight} cm",
                    requiredValue = "न्यूनतम ${requiredHeight} cm",
                    statusTextHindi = "ऊंचाई कम है",
                    guidanceHindi = "आपकी ऊंचाई निर्धारित मानक से कम है।"
                )
            )
        }

        // 4. Chest Requirement (Male only)
        if (!isFemale) {
            val normalChest = student.chestNormalCm
            val expandedChest = student.chestExpandedCm
            val expansion = (expandedChest - normalChest).coerceAtLeast(0.0)

            if (normalChest <= 0) {
                missingDataCount++
                criteriaList.add(
                    EligibilityCriterionResult(
                        criterionName = "सीना माप (Chest Measurement)",
                        isMet = null,
                        studentValue = "दर्ज नहीं",
                        requiredValue = "${targetProfile.minChestMaleCm} cm (+${targetProfile.minChestExpansionCm} cm फुलाव)",
                        statusTextHindi = "डेटा अपूर्ण",
                        guidanceHindi = "प्रोफाइल में सामान्य व फुलाया हुआ सीना माप दर्ज करें।"
                    )
                )
            } else if (normalChest >= targetProfile.minChestMaleCm && expansion >= targetProfile.minChestExpansionCm) {
                passCount++
                criteriaList.add(
                    EligibilityCriterionResult(
                        criterionName = "सीना माप (Chest Measurement)",
                        isMet = true,
                        studentValue = "$normalChest cm (फुलाव: +${expansion.toInt()} cm)",
                        requiredValue = "${targetProfile.minChestMaleCm} cm (+${targetProfile.minChestExpansionCm.toInt()} cm फुलाव)",
                        statusTextHindi = "सीना मानक पूर्ण",
                        guidanceHindi = "आपका सीना एवं न्यूनतम 5 सेमी फुलाव मानक के अनुरूप उत्कृष्ट है।"
                    )
                )
            } else if (normalChest >= targetProfile.minChestMaleCm - 2.0 || expansion < targetProfile.minChestExpansionCm) {
                partialCount++
                criteriaList.add(
                    EligibilityCriterionResult(
                        criterionName = "सीना माप (Chest Measurement)",
                        isMet = null,
                        studentValue = "$normalChest cm (फुलाव: +${expansion.toInt()} cm)",
                        requiredValue = "${targetProfile.minChestMaleCm} cm (+${targetProfile.minChestExpansionCm.toInt()} cm फुलाव)",
                        statusTextHindi = "फुलाव / चेस्ट में सुधार आवश्यक",
                        guidanceHindi = "पुश-अप्स और डीप ब्रीदिंग व्यायाम से सीने का 5 सेमी फुलाव बेहतर करें।"
                    )
                )
            } else {
                failCount++
                criteriaList.add(
                    EligibilityCriterionResult(
                        criterionName = "सीना माप (Chest Measurement)",
                        isMet = false,
                        studentValue = "$normalChest cm",
                        requiredValue = "${targetProfile.minChestMaleCm} cm (+5 cm)",
                        statusTextHindi = "सीना मानक से कम",
                        guidanceHindi = "चेस्ट साइज बढ़ाने हेतु नियमित पुश-अप्स और डिप्स का अभ्यास करें।"
                    )
                )
            }
        }

        // 5. Weight & BMI Metric
        if (student.weightKg > 0 && student.heightCm > 0) {
            val heightM = student.heightCm / 100.0
            val bmi = student.weightKg / (heightM * heightM)
            val bmiText = String.format(java.util.Locale.US, "%.1f", bmi)
            if (bmi in 18.5..25.0) {
                passCount++
                criteriaList.add(
                    EligibilityCriterionResult(
                        criterionName = "शारीरिक वजन एवं BMI",
                        isMet = true,
                        studentValue = "${student.weightKg} kg (BMI: $bmiText)",
                        requiredValue = "अनुपातिक वजन (BMI: 18.5 - 25.0)",
                        statusTextHindi = "आदर्श शारीरिक वजन",
                        guidanceHindi = "आपकी ऊंचाई के अनुपात में वजन एकदम संतुलित है।"
                    )
                )
            } else {
                partialCount++
                criteriaList.add(
                    EligibilityCriterionResult(
                        criterionName = "शारीरिक वजन एवं BMI",
                        isMet = null,
                        studentValue = "${student.weightKg} kg (BMI: $bmiText)",
                        requiredValue = "अनुपातिक वजन (BMI: 18.5 - 25.0)",
                        statusTextHindi = if (bmi < 18.5) "वजन कम (Underweight)" else "वजन अधिक (Overweight)",
                        guidanceHindi = "मेडिकल परीक्षण हेतु डाइट और वर्कआउट से वजन संतुलित करें।"
                    )
                )
            }
        }

        // Determine Overall Status
        val totalCriteria = criteriaList.size
        val scorePercent = if (totalCriteria > 0) ((passCount * 100) / totalCriteria) else 0

        val overallStatus = when {
            missingDataCount >= 2 -> EligibilityStatus.INSUFFICIENT_DATA
            failCount > 0 -> EligibilityStatus.NEEDS_IMPROVEMENT
            partialCount > 0 -> EligibilityStatus.MAY_BE_ELIGIBLE
            passCount == totalCriteria -> EligibilityStatus.LIKELY_ELIGIBLE
            else -> EligibilityStatus.MAY_BE_ELIGIBLE
        }

        val advice = when (overallStatus) {
            EligibilityStatus.LIKELY_ELIGIBLE ->
                "शानदार! आपकी प्रोफाइल इस भर्ती के सभी बुनियादी मापदंडों को पूरा करती है। फिजिकल ट्रेनिंग व रिटन एग्जाम की तैयारी पर ध्यान दें।"
            EligibilityStatus.MAY_BE_ELIGIBLE ->
                "आप संभवतः पात्र हैं। आयु या शारीरिक छूट के लिए नवीनतम आधिकारिक नोटिफिकेशन अवश्य देखें।"
            EligibilityStatus.NEEDS_IMPROVEMENT ->
                "कुछ मापदंडों में सुधार की आवश्यकता है। अपनी कमजोरियों को दूर करने के लिए अखाड़ा कोच के निर्देशों का पालन करें।"
            EligibilityStatus.INSUFFICIENT_DATA ->
                "अधिक सटीक Eligibility Guidance के लिए कृपया अपनी प्रोफाइल में आयु, ऊंचाई एवं शिक्षा का विवरण पूरा करें।"
        }

        return StudentEligibilityReport(
            studentId = student.studentId,
            recruitmentName = targetProfile.nameHindi,
            category = targetProfile.id,
            overallStatus = overallStatus,
            overallScorePercentage = scorePercent,
            criteria = criteriaList,
            actionAdviceHindi = advice
        )
    }

    /**
     * Compares the student's physical metrics with the target recruitment standards.
     */
    fun comparePhysicalStandards(
        student: StudentProfile?,
        targetProfile: RecruitmentCategoryProfile
    ): List<PhysicalStandardComparisonItem> {
        if (student == null) return emptyList()

        val results = mutableListOf<PhysicalStandardComparisonItem>()

        // 1. 1600m Running Time
        val runSeconds = parseTimeToSeconds(student.time1600m)
        val targetRunSec = targetProfile.target1600mSeconds
        if (runSeconds <= 0) {
            results.add(
                PhysicalStandardComparisonItem(
                    metricName = "1600 मीटर दौड़ (Running)",
                    iconEmoji = "🏃",
                    studentCurrentValue = "डेटा नहीं",
                    targetRequirement = formatSecondsToTime(targetRunSec),
                    status = StandardStatus.NO_DATA,
                    progressRatio = 0f,
                    coachingAdviceHindi = "दैनिक ट्रेनिंग में 1600m दौड़ का समय दर्ज करें।"
                )
            )
        } else {
            val status = if (runSeconds <= targetRunSec) StandardStatus.ACHIEVED else StandardStatus.NEEDS_IMPROVEMENT
            val ratio = ((targetRunSec.toFloat() / runSeconds.toFloat())).coerceIn(0f, 1.2f)
            val diffSec = runSeconds - targetRunSec
            val advice = if (status == StandardStatus.ACHIEVED) {
                "उत्कृष्ट! आपकी टाइमिंग भर्ती लक्ष्य (${formatSecondsToTime(targetRunSec)}) के अनुरूप बेहतरीन है।"
            } else {
                "लक्ष्य से $diffSec सेकंड पीछे। पेस रनिंग और 400m स्प्रिंट ड्रिल्स पर जोर दें।"
            }
            results.add(
                PhysicalStandardComparisonItem(
                    metricName = "1600 मीटर दौड़ (Running)",
                    iconEmoji = "🏃",
                    studentCurrentValue = student.time1600m,
                    targetRequirement = "≤ ${formatSecondsToTime(targetRunSec)}",
                    status = status,
                    progressRatio = ratio,
                    coachingAdviceHindi = advice
                )
            )
        }

        // 2. Beam Pull-ups (if applicable)
        if (targetProfile.targetPullups > 0) {
            val pullups = student.pullups
            val target = targetProfile.targetPullups
            val status = if (pullups >= target) StandardStatus.ACHIEVED else if (pullups > 0) StandardStatus.NEEDS_IMPROVEMENT else StandardStatus.NO_DATA
            val ratio = (pullups.toFloat() / target.toFloat()).coerceIn(0f, 1.2f)
            val advice = if (status == StandardStatus.ACHIEVED) {
                "10 बीम पूर्ण! आपको पूरे 40 अंक प्राप्त होंगे।"
            } else if (pullups > 0) {
                "वर्तमान में $pullups बीम। 10 बीम तक पहुंचने हेतु रोज 3 सेट एक्स्ट्रा लगाएं।"
            } else {
                "पुल-अप्स का डेटा दर्ज करें।"
            }
            results.add(
                PhysicalStandardComparisonItem(
                    metricName = "बीम / पुल-अप्स (Beam Pull-ups)",
                    iconEmoji = "💪",
                    studentCurrentValue = if (pullups > 0) "$pullups बीम" else "डेटा नहीं",
                    targetRequirement = "$target बीम (40 अंक)",
                    status = status,
                    progressRatio = ratio,
                    coachingAdviceHindi = advice
                )
            )
        }

        // 3. Long Jump (if applicable)
        if (targetProfile.targetLongJumpFeet > 0.0) {
            val lj = student.longJumpFeet
            val target = targetProfile.targetLongJumpFeet
            val status = if (lj >= target) StandardStatus.ACHIEVED else if (lj > 0.0) StandardStatus.NEEDS_IMPROVEMENT else StandardStatus.NO_DATA
            val ratio = if (target > 0) (lj.toFloat() / target.toFloat()).coerceIn(0f, 1.2f) else 1f
            val advice = if (status == StandardStatus.ACHIEVED) {
                "लंबी कूद मानक अनुसार पूर्ण।"
            } else if (lj > 0) {
                "टेक-ऑफ लेग की मजबूती और स्पीड अप्रोच पर अभ्यास करें।"
            } else {
                "लंबी कूद का डेटा दर्ज करें।"
            }
            results.add(
                PhysicalStandardComparisonItem(
                    metricName = "लंबी कूद (Long Jump)",
                    iconEmoji = "👟",
                    studentCurrentValue = if (lj > 0) "$lj फीट" else "डेटा नहीं",
                    targetRequirement = "≥ $target फीट",
                    status = status,
                    progressRatio = ratio,
                    coachingAdviceHindi = advice
                )
            )
        }

        // 4. High Jump (if applicable)
        if (targetProfile.targetHighJumpFeet > 0.0) {
            val hj = student.highJumpFeet
            val target = targetProfile.targetHighJumpFeet
            val status = if (hj >= target) StandardStatus.ACHIEVED else if (hj > 0.0) StandardStatus.NEEDS_IMPROVEMENT else StandardStatus.NO_DATA
            val ratio = if (target > 0) (hj.toFloat() / target.toFloat()).coerceIn(0f, 1.2f) else 1f
            val advice = if (status == StandardStatus.ACHIEVED) {
                "ऊंची कूद मानक अनुरूप।"
            } else if (hj > 0) {
                "सीजर कट तकनीक और कोर स्ट्रेंथ पर काम करें।"
            } else {
                "ऊंची कूद का डेटा दर्ज करें।"
            }
            results.add(
                PhysicalStandardComparisonItem(
                    metricName = "ऊंची कूद (High Jump)",
                    iconEmoji = "🦘",
                    studentCurrentValue = if (hj > 0) "$hj फीट" else "डेटा नहीं",
                    targetRequirement = "≥ $target फीट",
                    status = status,
                    progressRatio = ratio,
                    coachingAdviceHindi = advice
                )
            )
        }

        // 5. Shot Put (if applicable)
        if (targetProfile.targetShotPutMeters > 0.0) {
            val sp = student.shotPutMeters
            val target = targetProfile.targetShotPutMeters
            val status = if (sp >= target) StandardStatus.ACHIEVED else if (sp > 0.0) StandardStatus.NEEDS_IMPROVEMENT else StandardStatus.NO_DATA
            val ratio = if (target > 0) (sp.toFloat() / target.toFloat()).coerceIn(0f, 1.2f) else 1f
            val advice = if (status == StandardStatus.ACHIEVED) {
                "गोला फेंक दूरी मानक अनुसार।"
            } else if (sp > 0) {
                "शोल्डर स्ट्रेंथ और 45-डिग्री रिलीज एंगल का अभ्यास करें।"
            } else {
                "गोला फेंक का डेटा दर्ज करें।"
            }
            results.add(
                PhysicalStandardComparisonItem(
                    metricName = "गोला फेंक (Shot Put)",
                    iconEmoji = "☄️",
                    studentCurrentValue = if (sp > 0) "$sp मीटर" else "डेटा नहीं",
                    targetRequirement = "≥ $target मीटर",
                    status = status,
                    progressRatio = ratio,
                    coachingAdviceHindi = advice
                )
            )
        }

        // 6. Height (PST Standard)
        val isFemale = student.gender.equals("Female", ignoreCase = true)
        val targetHeight = if (isFemale) targetProfile.minHeightFemaleCm else targetProfile.minHeightMaleCm
        val height = student.heightCm
        val hStatus = if (height >= targetHeight) StandardStatus.ACHIEVED else if (height > 0) StandardStatus.NEEDS_IMPROVEMENT else StandardStatus.NO_DATA
        results.add(
            PhysicalStandardComparisonItem(
                metricName = "शारीरिक ऊंचाई (Height PST)",
                iconEmoji = "📏",
                studentCurrentValue = if (height > 0) "$height cm" else "डेटा नहीं",
                targetRequirement = "न्यूनतम $targetHeight cm",
                status = hStatus,
                progressRatio = if (height > 0) (height.toFloat() / targetHeight.toFloat()).coerceIn(0f, 1.1f) else 0f,
                coachingAdviceHindi = if (hStatus == StandardStatus.ACHIEVED) "हाइट मानक पूर्ण।" else "हाइट सुधार व सही पोस्चर बनाए रखें।"
            )
        )

        // 7. Chest (PST Standard - Male)
        if (!isFemale) {
            val chestNorm = student.chestNormalCm
            val chestExp = student.chestExpandedCm
            val expDiff = (chestExp - chestNorm).coerceAtLeast(0.0)
            val cStatus = if (chestNorm >= targetProfile.minChestMaleCm && expDiff >= 5.0) {
                StandardStatus.ACHIEVED
            } else if (chestNorm > 0) {
                StandardStatus.NEEDS_IMPROVEMENT
            } else {
                StandardStatus.NO_DATA
            }
            results.add(
                PhysicalStandardComparisonItem(
                    metricName = "सीना माप (Chest PST)",
                    iconEmoji = "🫁",
                    studentCurrentValue = if (chestNorm > 0) "$chestNorm cm (+${expDiff.toInt()} cm)" else "डेटा नहीं",
                    targetRequirement = "${targetProfile.minChestMaleCm} cm (+5 cm फुलाव)",
                    status = cStatus,
                    progressRatio = if (chestNorm > 0) (chestNorm.toFloat() / targetProfile.minChestMaleCm.toFloat()).coerceIn(0f, 1.1f) else 0f,
                    coachingAdviceHindi = if (cStatus == StandardStatus.ACHIEVED) "सीना और फुलाव दोनों सही हैं।" else "पुश-अप्स से 5 सेमी फुलाव सुनिश्चित करें।"
                )
            )
        }

        return results
    }

    /**
     * Generates personalized recruitment recommendations from real student data.
     */
    fun generatePersonalizedRecruitmentRecommendations(
        student: StudentProfile?,
        targetProfile: RecruitmentCategoryProfile,
        workoutRecords: List<WorkoutRecord> = emptyList(),
        testAttempts: List<TestAttempt> = emptyList(),
        studyAttempts: List<StudyAttempt> = emptyList()
    ): List<String> {
        val recommendations = mutableListOf<String>()

        if (student == null) {
            return listOf("अधिक सटीक Recruitment Guidance के लिए Training और Study data पूरा करें।")
        }

        // Profile Completeness Check
        if (student.dob.isBlank() || student.heightCm <= 0 || student.education.isBlank()) {
            recommendations.add("Eligibility guidance बेहतर बनाने के लिए अपनी Profile पूरी करें।")
        }

        // Running Performance Check
        val runSeconds = parseTimeToSeconds(student.time1600m)
        if (runSeconds > 0 && runSeconds > targetProfile.target1600mSeconds) {
            val diff = runSeconds - targetProfile.target1600mSeconds
            recommendations.add("आपके चयनित लक्ष्य (${targetProfile.nameEnglish}) के लिए 1600m Running पर अधिक ध्यान देने की आवश्यकता है (लक्ष्य से $diff सेकंड पीछे)।")
        }

        // Pullups Check for Army
        if (targetProfile.id.contains("ARMY") && student.pullups < 10 && student.pullups > 0) {
            recommendations.add("आर्मी में पूरे 40 अंक पाने हेतु बीम (पुल-अप्स) संख्या 10 तक ले जाएं।")
        }

        // Mock Test / Study Weakness Check
        if (testAttempts.isEmpty() && studyAttempts.size < 10) {
            recommendations.add("Physical preparation के साथ Written Exam practice भी बढ़ाएं। दैनिक क्विज हल करें।")
        } else if (testAttempts.isNotEmpty()) {
            val latest = testAttempts.first()
            if (latest.accuracyPercentage < 65.0) {
                recommendations.add("लिखित परीक्षा में सटीकता (${latest.accuracyPercentage.toInt()}%) कम है। कमजोर विषयों का पुनरावलोकन करें।")
            }
        }

        // Attendance & Ground Consistency
        if (student.attendanceStreakDays < 3) {
            recommendations.add("ग्राउंड प्रशिक्षण में निरंतरता बनाएं रखें। दैनिक उपस्थिति दर्ज करें।")
        }

        if (recommendations.isEmpty()) {
            recommendations.add("आपका शारीरिक और शैक्षणिक संतुलन उत्कृष्ट है। आगामी भर्ती रैली हेतु मॉक टेस्ट और पेस रनिंग जारी रखें।")
        }

        return recommendations
    }

    /**
     * Filters active notices and sorts them: Pinned first, then Urgent, then newest date first.
     */
    fun filterAndSortNotices(
        notices: List<Notice>,
        selectedCategory: String = "ALL",
        currentDate: String = ""
    ): List<Notice> {
        val nonExpired = notices.filter { notice ->
            if (notice.expiryDate.isNotBlank() && currentDate.isNotBlank()) {
                notice.expiryDate >= currentDate
            } else {
                true
            }
        }

        val categoryFiltered = if (selectedCategory == "ALL" || selectedCategory == "सभी") {
            nonExpired
        } else {
            nonExpired.filter {
                it.category.equals(selectedCategory, ignoreCase = true) ||
                (selectedCategory == "जरूरी सूचना" && (it.isUrgent || it.priority.equals("URGENT", true))) ||
                (selectedCategory == "प्रशिक्षण" && it.category.contains("Training", true)) ||
                (selectedCategory == "परीक्षा" && it.category.contains("Exam", true)) ||
                (selectedCategory == "भर्ती" && it.category.contains("Recruitment", true))
            }
        }

        return categoryFiltered.sortedWith(
            compareByDescending<Notice> { it.isPinned }
                .thenByDescending { it.isUrgent || it.priority.equals("URGENT", true) || it.priority.equals("HIGH", true) }
                .thenByDescending { it.date }
                .thenByDescending { it.id }
        )
    }

    // Helper functions
    private fun calculateAgeFromDob(dob: String): Double {
        if (dob.isBlank()) return 0.0
        return try {
            val parts = dob.trim().split("-")
            if (parts.size == 3) {
                val birthYear = parts[0].toInt()
                val currentYear = 2026 // App temporal anchor
                (currentYear - birthYear).toDouble()
            } else 0.0
        } catch (e: Exception) {
            0.0
        }
    }

    private fun parseEducationRank(edu: String): Int {
        val lower = edu.lowercase()
        return when {
            lower.contains("post graduate") || lower.contains("m.") || lower.contains("ma") || lower.contains("msc") -> 5
            lower.contains("graduate") || lower.contains("ba") || lower.contains("b.sc") || lower.contains("b.tech") || lower.contains("b.com") -> 4
            lower.contains("12th") || lower.contains("12") || lower.contains("barahvi") || lower.contains("intermediate") -> 3
            lower.contains("10th") || lower.contains("10") || lower.contains("dasvi") || lower.contains("matric") || lower.contains("high school") -> 2
            lower.contains("8th") || lower.contains("8") || lower.contains("aathvi") -> 1
            else -> 2 // default to 10th equivalent if general
        }
    }

    private fun parseTimeToSeconds(timeStr: String): Int {
        if (timeStr.isBlank()) return 0
        return try {
            val clean = timeStr.trim().replace("min", "").replace("m", "").trim()
            if (clean.contains(":")) {
                val parts = clean.split(":")
                val min = parts[0].trim().toIntOrNull() ?: 0
                val sec = parts[1].trim().toIntOrNull() ?: 0
                min * 60 + sec
            } else {
                clean.toIntOrNull() ?: 0
            }
        } catch (e: Exception) {
            0
        }
    }

    private fun formatSecondsToTime(seconds: Int): String {
        val min = seconds / 60
        val sec = seconds % 60
        return String.format(java.util.Locale.US, "%d:%02d", min, sec)
    }
}
