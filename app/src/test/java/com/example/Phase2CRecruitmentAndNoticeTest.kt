package com.example

import com.example.data.model.Notice
import com.example.data.model.RecruitmentInfo
import com.example.data.model.StudentProfile
import com.example.data.recruitment.*
import org.junit.Assert.*
import org.junit.Test

class Phase2CRecruitmentAndNoticeTest {

    private fun createSampleStudent(
        age: Int = 19,
        dob: String = "2007-02-15",
        heightCm: Double = 172.0,
        chestNormal: Double = 81.0,
        chestExpanded: Double = 87.0,
        gender: String = "Male",
        education: String = "12th Pass",
        time1600: String = "5:20",
        pullups: Int = 10,
        goal: String = "Indian Army"
    ): StudentProfile {
        return StudentProfile(
            studentId = "JBA-TEST-001",
            fullName = "Vikram Rathore",
            fatherName = "Shri R. Rathore",
            village = "Mauri Kala",
            age = age,
            dob = dob,
            gender = gender,
            education = education,
            mobileNumber = "9876543210",
            recruitmentGoal = goal,
            heightCm = heightCm,
            weightKg = 64.0,
            chestNormalCm = chestNormal,
            chestExpandedCm = chestExpanded,
            time1600m = time1600,
            time400m = "1:05",
            time800m = "2:20",
            time5km = "21:00",
            pushups = 40,
            situps = 45,
            pullups = pullups,
            squats = 50,
            plankSeconds = 90,
            longJumpFeet = 15.0,
            highJumpFeet = 4.2,
            shotPutMeters = 7.8,
            attendanceStreakDays = 12,
            studyTargetPercentage = 85,
            overallScore = 90
        )
    }

    @Test
    fun testArmyGDEligibilityEvaluation_ValidStudent_ReturnsLikelyEligible() {
        val student = createSampleStudent(
            age = 19,
            heightCm = 172.0,
            chestNormal = 81.0,
            chestExpanded = 87.0,
            education = "12th Pass"
        )
        val armyProfile = RecruitmentStandardRepository.findProfileByIdOrGoal("ARMY_AGNIVEER_GD")

        val report = EligibilityIntelligenceEngine.evaluateEligibility(student, armyProfile)

        assertEquals("Vikram Rathore", report.studentId.let { student.fullName })
        assertEquals(EligibilityStatus.LIKELY_ELIGIBLE, report.overallStatus)
        assertTrue("Score should be high", report.overallScorePercentage >= 80)
        assertTrue(report.criteria.isNotEmpty())
        assertTrue(report.disclaimerText.contains("आधिकारिक भर्ती अधिसूचना"))
    }

    @Test
    fun testArmyGDEligibilityEvaluation_OverAgeStudent_FlagsAgeRequirement() {
        // Age 28 is well above maxAge + 5.0 (21 + 5 = 26)
        val student = createSampleStudent(
            age = 28,
            dob = "1998-01-01"
        )
        val armyProfile = RecruitmentStandardRepository.findProfileByIdOrGoal("ARMY_AGNIVEER_GD")

        val report = EligibilityIntelligenceEngine.evaluateEligibility(student, armyProfile)

        val ageCriterion = report.criteria.find { it.criterionName.contains("आयु") }
        assertNotNull(ageCriterion)
        assertEquals(false, ageCriterion?.isMet)
        assertEquals(EligibilityStatus.NEEDS_IMPROVEMENT, report.overallStatus)
    }

    @Test
    fun testPhysicalStandardComparison_ReturnsDetailedMetrics() {
        val student = createSampleStudent(
            time1600 = "5:15",
            pullups = 10
        )
        val armyProfile = RecruitmentStandardRepository.findProfileByIdOrGoal("ARMY_AGNIVEER_GD")

        val comparisons = EligibilityIntelligenceEngine.comparePhysicalStandards(student, armyProfile)

        assertTrue(comparisons.isNotEmpty())
        val run1600 = comparisons.find { it.metricName.contains("1600") }
        assertNotNull(run1600)
        assertEquals(StandardStatus.ACHIEVED, run1600?.status)

        val pullupsItem = comparisons.find { it.metricName.contains("बीम") || it.metricName.contains("पुल-अप्स") || it.metricName.contains("Pull-ups") }
        assertNotNull(pullupsItem)
        assertEquals(StandardStatus.ACHIEVED, pullupsItem?.status)
    }

    @Test
    fun testRecruitmentStandardRepository_FindsProfileByKeyword() {
        val army = RecruitmentStandardRepository.findProfileByIdOrGoal("Indian Army GD")
        assertEquals("ARMY_AGNIVEER_GD", army.id)

        val police = RecruitmentStandardRepository.findProfileByIdOrGoal("CG Police Constable")
        assertEquals("CG_STATE_POLICE", police.id)

        val ssc = RecruitmentStandardRepository.findProfileByIdOrGoal("SSC GD 2026")
        assertEquals("SSC_GD_CAPF", ssc.id)
    }

    @Test
    fun testNoticeModelWithPhase2CProperties() {
        val notice = Notice(
            id = 1,
            title = "अग्निवीर रैली रजिस्ट्रेशन",
            content = "सभी कैडेट समय पर दस्तावेज तैयार करें",
            date = "2026-08-24",
            category = "भर्ती अपडेट",
            author = "उस्ताद राम सिंह",
            isUrgent = true,
            priority = "URGENT",
            isPinned = true,
            expiryDate = "2026-09-15",
            isRead = false
        )

        assertTrue(notice.isPinned)
        assertTrue(notice.isUrgent)
        assertEquals("URGENT", notice.priority)
        assertEquals("2026-09-15", notice.expiryDate)
    }

    @Test
    fun testRecruitmentInfoModelWithPhase2CProperties() {
        val rec = RecruitmentInfo(
            id = 1,
            recruitmentName = "Indian Army Agniveer GD 2026",
            organization = "Indian Army",
            category = "ARMY",
            shortDescription = "10वीं पास युवाओं के लिए सेना में सुनहरा अवसर",
            eligibility = "10th Pass (45%)",
            ageLimit = "17.5 - 21 वर्ष",
            heightRequirement = "169 cm",
            chestRequirement = "77-82 cm",
            physicalTest = "1600m Running, 10 Pull-ups",
            writtenExam = "CEE Online Computer Test",
            syllabus = "GK, GS, Maths, Reasoning",
            importantDocuments = "10th Marksheet, Domicile, Caste, Character",
            importantDates = "अंतिम तिथि: 30 सितंबर 2026",
            officialWebsiteLink = "https://joinindianarmy.nic.in"
        )

        assertEquals("ARMY", rec.category)
        assertEquals("10th Pass (45%)", rec.eligibility)
        assertTrue(rec.title.contains("Army"))
        assertTrue(rec.officialUrl.startsWith("https://"))
    }
}
