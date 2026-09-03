package com.example

import com.example.data.mission.*
import com.example.data.model.*
import org.junit.Assert.*
import org.junit.Test

class Phase4GraminDigitalMissionTest {

    // ==========================================
    // 1. 30/60/90 DAY MISSION ENGINE TESTS
    // ==========================================

    @Test
    fun testMissionEngine_30_60_90_DaysDurations() {
        val student = StudentProfile(
            studentId = "JBA-2026-TEST",
            fullName = "राहुल निषाद",
            recruitmentGoal = "Indian Army"
        )

        // Test 30 Days
        val state30 = MissionEngine.calculateMissionState(
            student = student,
            duration = MissionDuration.DAYS_30,
            todayWorkout = null,
            todayTraining = null,
            todayChapters = emptyList(),
            studentStudyAttempts = emptyList(),
            studentTestAttempts = emptyList()
        )
        assertEquals(30, state30.totalDays)
        assertTrue(state30.tasks.isNotEmpty())

        // Test 60 Days
        val state60 = MissionEngine.calculateMissionState(
            student = student,
            duration = MissionDuration.DAYS_60,
            todayWorkout = null,
            todayTraining = null,
            todayChapters = emptyList(),
            studentStudyAttempts = emptyList(),
            studentTestAttempts = emptyList()
        )
        assertEquals(60, state60.totalDays)

        // Test 90 Days
        val state90 = MissionEngine.calculateMissionState(
            student = student,
            duration = MissionDuration.DAYS_90,
            todayWorkout = null,
            todayTraining = null,
            todayChapters = emptyList(),
            studentStudyAttempts = emptyList(),
            studentTestAttempts = emptyList()
        )
        assertEquals(90, state90.totalDays)
    }

    @Test
    fun testMissionEngine_ProgressAndStreakCalculation() {
        val student = StudentProfile(
            studentId = "JBA-2026-TEST",
            fullName = "सुरेश यादव",
            attendanceStreakDays = 8
        )
        val workout = WorkoutRecord(
            studentId = "JBA-2026-TEST",
            date = "2026-09-03",
            runningDistanceKm = 5.0,
            pushupsDone = 45
        )
        val training = TrainingRecord(
            studentId = "JBA-2026-TEST",
            date = "2026-09-03",
            runningDistanceKm = 2.0,
            runningDuration = "5:20",
            pushups = 45,
            pullups = 10
        )
        val studyAttempt = StudyAttempt(
            studentId = "JBA-2026-TEST",
            questionId = "q1",
            subjectId = "s1",
            topicId = "t1",
            selectedAnswer = "A",
            isCorrect = true,
            attemptDate = "2026-09-03"
        )
        val testAttempt = TestAttempt(
            testId = 1L,
            testTitle = "आर्मी GD मॉक टेस्ट #1",
            targetExam = "Indian Army",
            studentId = "JBA-2026-TEST",
            date = "2026-09-03",
            totalQuestions = 50,
            attemptedCount = 48,
            correctCount = 45,
            wrongCount = 3,
            unattemptedCount = 2,
            score = 87.0,
            maxScore = 100.0,
            accuracyPercentage = 93.75,
            timeTakenSeconds = 2400
        )

        val state = MissionEngine.calculateMissionState(
            student = student,
            duration = MissionDuration.DAYS_60,
            todayWorkout = workout,
            todayTraining = training,
            todayChapters = emptyList(),
            studentStudyAttempts = listOf(studyAttempt),
            studentTestAttempts = listOf(testAttempt)
        )

        assertTrue("Running target should be accomplished", state.dailyWorkoutTargetDone)
        assertTrue("Study target should be completed", state.dailyStudyTargetDone)
        assertTrue("Mock test target should be accomplished", state.dailyPracticeTargetDone)
        assertTrue("Overall mission completion should be calculated", state.completionPercentage >= 0)
        assertEquals(8, state.streakDays)
    }

    // ==========================================
    // 2. PHYSICAL TEST SIMULATOR ENGINE TESTS
    // ==========================================

    @Test
    fun testPhysicalSimulator_Army1600mScoring_Group1AndGroup2() {
        // Group 1: 5 min 20 sec (<= 330 sec) = 60 Marks, 10 pullups = 40 Marks -> Total 100 Marks
        val inputGroup1 = PhysicalSimulatorInput(
            examTarget = "Indian Army",
            time1600mMinutes = 5,
            time1600mSeconds = 20,
            pullupsBeam = 10,
            pushupsCount = 45,
            situpsCount = 45
        )
        val result1 = PhysicalTestSimulatorEngine.simulate(inputGroup1)
        val run1600 = result1.metricBreakdown.find { it.eventNameHindi.contains("1600m") }
        assertNotNull(run1600)
        assertEquals(60, run1600!!.marksAwarded)
        assertTrue(run1600.statusHindi.contains("ग्रुप 1"))

        val pullups = result1.metricBreakdown.find { it.eventNameHindi.contains("बीम") }
        assertNotNull(pullups)
        assertEquals(40, pullups!!.marksAwarded)
        assertEquals(100, result1.totalPhysicalMarks)
        assertTrue(result1.qualificationStatusHindi.contains("योग्य"))

        // Group 2: 5 min 40 sec (340 sec) = 48 Marks
        val inputGroup2 = PhysicalSimulatorInput(
            examTarget = "Indian Army",
            time1600mMinutes = 5,
            time1600mSeconds = 40,
            pullupsBeam = 8
        )
        val result2 = PhysicalTestSimulatorEngine.simulate(inputGroup2)
        val runGroup2 = result2.metricBreakdown.find { it.eventNameHindi.contains("1600m") }
        assertNotNull(runGroup2)
        assertEquals(48, runGroup2!!.marksAwarded)
        assertTrue(runGroup2.statusHindi.contains("ग्रुप 2"))

        // Fail: Above 5 min 45 sec (345s) -> 0 Marks
        val inputFail = PhysicalSimulatorInput(
            examTarget = "Indian Army",
            time1600mMinutes = 6,
            time1600mSeconds = 10,
            pullupsBeam = 5
        )
        val resultFail = PhysicalTestSimulatorEngine.simulate(inputFail)
        val runFail = resultFail.metricBreakdown.find { it.eventNameHindi.contains("1600m") }
        assertNotNull(runFail)
        assertFalse(runFail!!.isQualified)
        assertEquals(0, runFail.marksAwarded)
    }

    @Test
    fun testPhysicalSimulator_ComparisonWithPriorRecord() {
        val priorStudent = StudentProfile(
            studentId = "JBA-2026-COMP",
            fullName = "विकास वर्मा",
            time1600m = "5:50",
            pushups = 35,
            pullups = 7,
            longJumpFeet = 13.0
        )

        // Current improved input: 5:28 (improved vs 5:50)
        val currentInput = PhysicalSimulatorInput(
            examTarget = "Indian Army",
            time1600mMinutes = 5,
            time1600mSeconds = 28,
            pullupsBeam = 10,
            pushupsCount = 45,
            longJumpMeters = 4.8
        )

        val result = PhysicalTestSimulatorEngine.simulate(currentInput, priorStudent)
        assertTrue("Comparison deltas should be generated", result.comparisonWithPrevious.isNotEmpty())

        val runDelta = result.comparisonWithPrevious.find { it.metricName.contains("1600m") }
        assertNotNull(runDelta)
        assertTrue("Running time delta should show improvement", runDelta!!.isBetter)

        val pullupsDelta = result.comparisonWithPrevious.find { it.metricName.contains("बीम") }
        assertNotNull(pullupsDelta)
        assertTrue(pullupsDelta!!.isBetter)
    }

    // ==========================================
    // 3. VILLAGE DIGITAL LIBRARY TESTS (100% OFFLINE)
    // ==========================================

    @Test
    fun testVillageDigitalLibrary_OfflineContentIntegrity() {
        val allItems = VillageDigitalLibraryData.getAllLibraryItems()
        assertTrue("Library must contain curated items", allItems.size >= 8)

        // Verify categories coverage
        val categories = allItems.map { it.category }.toSet()
        assertTrue(categories.contains(LibraryCategory.NOTES))
        assertTrue(categories.contains(LibraryCategory.PYQ))
        assertTrue(categories.contains(LibraryCategory.PRACTICE_SET))
        assertTrue(categories.contains(LibraryCategory.GK_GS))
        assertTrue(categories.contains(LibraryCategory.RECRUITMENT_GUIDE))

        // Verify that each item has offline content and formulas
        allItems.forEach { item ->
            assertTrue("Title cannot be blank: ${item.id}", item.titleHindi.isNotBlank())
            assertTrue("Markdown content must be offline readable: ${item.id}", item.contentMarkdownHindi.isNotBlank())
            assertTrue("Estimated read time must be positive: ${item.id}", item.estimatedReadMinutes > 0)
            assertTrue("Target exam must be specified: ${item.id}", item.targetExam.isNotBlank())
            assertTrue("Item must be marked offline available", item.isOfflineAvailable)
        }
    }

    // ==========================================
    // 4. RECRUITMENT ROADMAP TESTS (8 STAGES)
    // ==========================================

    @Test
    fun testRecruitmentRoadmaps_Contains8StagesForEachExam() {
        val roadmaps = RecruitmentRoadmapData.getAllRoadmaps()
        assertTrue("Must include Army GD, CG Police, and SSC GD", roadmaps.size >= 3)

        roadmaps.forEach { roadmap ->
            assertEquals("Roadmap must contain exactly 8 sequential stages for ${roadmap.examNameHindi}", 8, roadmap.stages.size)
            for (step in 1..8) {
                val stage = roadmap.stages.find { it.stepNumber == step }
                assertNotNull("Missing step $step in ${roadmap.examNameHindi}", stage)
                assertTrue(stage!!.titleHindi.isNotBlank())
                assertTrue("Stage must have checklists", stage.keyChecklistHindi.isNotEmpty())
                assertTrue("Stage must have coach tips", stage.tipsHindi.isNotBlank())
            }
        }
    }

    // ==========================================
    // 5. PARENT PROGRESS REPORT ENGINE TESTS
    // ==========================================

    @Test
    fun testParentProgressEngine_GeneratesCompleteReport() {
        val student = StudentProfile(
            studentId = "JBA-2026-PARENT",
            fullName = "दीपक कुमार",
            fatherName = "रामप्रसाद कुमार",
            village = "मौरिकला",
            recruitmentGoal = "CG Police Constable",
            attendanceStreakDays = 12
        )
        val attendance = listOf(
            AttendanceRecord(studentId = student.studentId, date = "2026-09-01", status = "Present"),
            AttendanceRecord(studentId = student.studentId, date = "2026-09-02", status = "Present"),
            AttendanceRecord(studentId = student.studentId, date = "2026-09-03", status = "Present")
        )
        val training = listOf(
            TrainingRecord(studentId = student.studentId, date = "2026-09-02", runningDuration = "5:35", pushups = 42, pullups = 9)
        )
        val testAttempts = listOf(
            TestAttempt(
                testId = 101L,
                testTitle = "CG Police Mock #1",
                targetExam = "CG Police",
                studentId = student.studentId,
                date = "2026-09-02",
                totalQuestions = 100,
                attemptedCount = 95,
                correctCount = 82,
                wrongCount = 13,
                unattemptedCount = 5,
                score = 82.0,
                maxScore = 100.0,
                accuracyPercentage = 86.3,
                timeTakenSeconds = 5400
            )
        )

        val report = ParentProgressEngine.generateReport(
            student = student,
            attendanceRecords = attendance,
            workoutRecords = emptyList(),
            trainingRecords = training,
            testAttempts = testAttempts
        )

        assertEquals("दीपक कुमार", report.studentName)
        assertEquals("रामप्रसाद कुमार", report.fatherName)
        assertEquals("मौरिकला", report.village)
        assertTrue(report.attendanceDaysCount >= 3)
        assertTrue("Discipline grade should be calculated", report.overallDisciplineGradeHindi.isNotBlank())
        assertTrue("Report must have coach message to parents", report.coachMessageToParentsHindi.contains("दीपक"))
        assertTrue("Parent advice tips must be provided", report.parentAdviceTipsHindi.isNotEmpty())
    }

    // ==========================================
    // 6. TRAINER ATTENTION ENGINE TESTS
    // ==========================================

    @Test
    fun testTrainerAttentionEngine_DetectsAbsentAndWeakCadets() {
        val student1 = StudentProfile(
            studentId = "JBA-2026-ATT1",
            fullName = "अमित साहू",
            time1600m = "6:30" // weak running
        )
        val student2 = StudentProfile(
            studentId = "JBA-2026-ATT2",
            fullName = "रोहित बघेल",
            studyTargetPercentage = 25 // low progress
        )

        val todayDateStr = "2026-09-03"

        val attendance = listOf(
            AttendanceRecord(studentId = student1.studentId, date = todayDateStr, status = "Absent"),
            AttendanceRecord(studentId = student2.studentId, date = todayDateStr, status = "Present")
        )

        val attentionItems = TrainerAttentionEngine.generateAttentionList(
            allStudents = listOf(student1, student2),
            allAttendance = attendance,
            allTraining = emptyList(),
            allTestAttempts = emptyList(),
            todayDateStr = todayDateStr
        )

        assertTrue("Should detect attention issues", attentionItems.isNotEmpty())

        // Check absent detection
        val absentIssue = attentionItems.find { it.student.studentId == student1.studentId && it.issueType == CadetAttentionIssueType.ABSENT_TODAY }
        assertNotNull("Absent cadet must be flagged", absentIssue)
        assertTrue(absentIssue!!.suggestedCoachActionHindi.contains("अभिभावक") || absentIssue.suggestedCoachActionHindi.contains("कॉल"))

        // Check weak physical or study detection
        val weakIssue = attentionItems.find { it.student.studentId == student1.studentId && it.issueType == CadetAttentionIssueType.WEAK_PHYSICAL }
        assertNotNull("Weak 1600m runner must be flagged", weakIssue)
    }
}
