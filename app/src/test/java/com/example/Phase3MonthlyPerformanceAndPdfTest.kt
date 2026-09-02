package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.analytics.monthly.*
import com.example.data.model.*
import com.example.util.RolePermissionManager
import com.example.util.pdf.PdfReportCardGenerator
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class Phase3MonthlyPerformanceAndPdfTest {

    private lateinit var context: Context

    private val sampleStudents = listOf(
        StudentProfile(
            id = 1,
            studentId = "JBA-2026-001",
            fullName = "Vikram Singh Gurjar",
            fatherName = "Shri R. S. Gurjar",
            village = "Mauri Kalan",
            mobileNumber = "9876543210",
            recruitmentGoal = "Indian Army",
            time1600m = "05:15"
        ),
        StudentProfile(
            id = 2,
            studentId = "JBA-2026-002",
            fullName = "Amit Kumar Sharma",
            fatherName = "Shri M. L. Sharma",
            village = "Kherli",
            mobileNumber = "9876543211",
            recruitmentGoal = "CG Police",
            time1600m = "05:30"
        ),
        StudentProfile(
            id = 3,
            studentId = "JBA-2026-003",
            fullName = "Rajesh Verma",
            fatherName = "Shri D. P. Verma",
            village = "Rampur",
            mobileNumber = "9876543212",
            recruitmentGoal = "SSC GD",
            time1600m = "05:45"
        )
    )

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun testAttendanceAnalytics_PresentAbsentStreakCalculation() {
        val studentId = "JBA-2026-001"
        val attendanceList = listOf(
            AttendanceRecord(studentId = studentId, date = "2026-08-01", status = "Present"),
            AttendanceRecord(studentId = studentId, date = "2026-08-02", status = "Present"),
            AttendanceRecord(studentId = studentId, date = "2026-08-03", status = "Present"),
            AttendanceRecord(studentId = studentId, date = "2026-08-04", status = "Absent"),
            AttendanceRecord(studentId = studentId, date = "2026-08-05", status = "Leave"),
            AttendanceRecord(studentId = studentId, date = "2026-08-06", status = "Present")
        )

        val report = MonthlyPerformanceEngine.generateMonthlyReport(
            monthKey = "2026-08",
            students = listOf(sampleStudents[0]),
            allAttendance = attendanceList,
            allTraining = emptyList(),
            allWorkouts = emptyList(),
            allRaceSessions = emptyList(),
            allRaceResults = emptyList()
        )

        val cadet = report.cadetReports.first()
        assertEquals(6, cadet.attendance.totalTrainingDays)
        assertEquals(4, cadet.attendance.presentCount)
        assertEquals(1, cadet.attendance.absentCount)
        assertEquals(1, cadet.attendance.leaveCount)
        assertEquals(66.7, cadet.attendance.attendancePercentage, 0.5)
        assertEquals(3, cadet.attendance.bestStreak)
    }

    @Test
    fun testAttendanceAnalytics_ZeroAndHundredPercentScenarios() {
        val st1 = sampleStudents[0]
        val st2 = sampleStudents[1]

        val attendance100 = listOf(
            AttendanceRecord(studentId = st1.studentId, date = "2026-08-01", status = "Present"),
            AttendanceRecord(studentId = st1.studentId, date = "2026-08-02", status = "Present")
        )
        val attendance0 = listOf(
            AttendanceRecord(studentId = st2.studentId, date = "2026-08-01", status = "Absent"),
            AttendanceRecord(studentId = st2.studentId, date = "2026-08-02", status = "Absent")
        )

        val report = MonthlyPerformanceEngine.generateMonthlyReport(
            monthKey = "2026-08",
            students = listOf(st1, st2),
            allAttendance = attendance100 + attendance0,
            allTraining = emptyList(),
            allWorkouts = emptyList(),
            allRaceSessions = emptyList(),
            allRaceResults = emptyList()
        )

        val cadet100 = report.cadetReports.find { it.studentId == st1.studentId }!!
        val cadet0 = report.cadetReports.find { it.studentId == st2.studentId }!!

        assertEquals(100.0, cadet100.attendance.attendancePercentage, 0.01)
        assertEquals(0.0, cadet0.attendance.attendancePercentage, 0.01)
    }

    @Test
    fun testRunningAnalytics_1600mBestLatestAndRanking() {
        val raceSession = RaceSession(
            raceId = "race_10",
            date = "2026-08-15",
            batchName = "1600m Monthly Time Trial",
            distanceMeters = 1600,
            totalParticipants = 3
        )

        val raceResults = listOf(
            RaceResult(id = 1, raceId = "race_10", studentId = "JBA-2026-001", studentName = "Vikram", chestNumber = "01", timeFormatted = "05:10.00", elapsedMillis = 310000L, rank = 1, status = "FINISHED"),
            RaceResult(id = 2, raceId = "race_10", studentId = "JBA-2026-002", studentName = "Amit", chestNumber = "02", timeFormatted = "05:25.00", elapsedMillis = 325000L, rank = 2, status = "FINISHED"),
            RaceResult(id = 3, raceId = "race_10", studentId = "JBA-2026-003", studentName = "Rajesh", chestNumber = "03", timeFormatted = "DNF", elapsedMillis = 0L, rank = 0, status = "DNF")
        )

        val report = MonthlyPerformanceEngine.generateMonthlyReport(
            monthKey = "2026-08",
            students = sampleStudents,
            allAttendance = emptyList(),
            allTraining = emptyList(),
            allWorkouts = emptyList(),
            allRaceSessions = listOf(raceSession),
            allRaceResults = raceResults
        )

        val vikram = report.cadetReports.find { it.studentId == "JBA-2026-001" }!!
        val amit = report.cadetReports.find { it.studentId == "JBA-2026-002" }!!
        val rajesh = report.cadetReports.find { it.studentId == "JBA-2026-003" }!!

        assertEquals("05:10.00", vikram.time1600mBest)
        assertEquals("05:25.00", amit.time1600mBest)
        assertEquals(1, vikram.rank1600m)
        assertEquals(2, amit.rank1600m)
        assertEquals(0, rajesh.rank1600m)
        assertTrue(rajesh.isDnfOnly)
    }

    @Test
    fun testImprovementTracking_RunningAndStrength() {
        // July Records (Previous month)
        val julyTraining = listOf(
            TrainingRecord(studentId = "JBA-2026-001", date = "2026-07-20", runningDuration = "05:30", runningType = "1600m Practice", pushups = 30, pullups = 8),
            TrainingRecord(studentId = "JBA-2026-002", date = "2026-07-20", runningDuration = "05:15", runningType = "1600m Practice", pushups = 40, pullups = 10)
        )

        // August Records (Current month)
        val augTraining = listOf(
            TrainingRecord(studentId = "JBA-2026-001", date = "2026-08-20", runningDuration = "05:10", runningType = "1600m Practice", pushups = 40, pullups = 10), // Faster & More pushups = Improved
            TrainingRecord(studentId = "JBA-2026-002", date = "2026-08-20", runningDuration = "05:35", runningType = "1600m Practice", pushups = 35, pullups = 8)   // Slower & Fewer pushups = Declined
        )

        val report = MonthlyPerformanceEngine.generateMonthlyReport(
            monthKey = "2026-08",
            students = sampleStudents.take(2),
            allAttendance = emptyList(),
            allTraining = julyTraining + augTraining,
            allWorkouts = emptyList(),
            allRaceSessions = emptyList(),
            allRaceResults = emptyList()
        )

        val vikram = report.cadetReports.find { it.studentId == "JBA-2026-001" }!!
        val amit = report.cadetReports.find { it.studentId == "JBA-2026-002" }!!

        // Vikram ran faster (05:10 vs 05:30) -> IMPROVED
        assertEquals(ImprovementStatus.IMPROVED, vikram.time1600mImprovement.status)
        assertTrue(vikram.time1600mImprovement.diffText.contains("तेज") || vikram.time1600mImprovement.diffText.contains("Faster", ignoreCase = true))
        // Vikram did more pushups (40 vs 30) -> IMPROVED
        assertEquals(ImprovementStatus.IMPROVED, vikram.pushupsImprovement.status)

        // Amit ran slower (05:35 vs 05:15) -> DECLINED
        assertEquals(ImprovementStatus.DECLINED, amit.time1600mImprovement.status)
        assertTrue(amit.time1600mImprovement.diffText.contains("धीमा") || amit.time1600mImprovement.diffText.contains("Slower", ignoreCase = true))
        // Amit did fewer pushups (35 vs 40) -> DECLINED
        assertEquals(ImprovementStatus.DECLINED, amit.pushupsImprovement.status)
    }

    @Test
    fun testTieBreaker_EqualTimingRanking() {
        val raceSession = RaceSession(
            raceId = "race_20",
            date = "2026-08-25",
            batchName = "Batch Trial",
            distanceMeters = 1600,
            totalParticipants = 2
        )

        val raceResults = listOf(
            RaceResult(id = 11, raceId = "race_20", studentId = "JBA-2026-001", studentName = "Vikram", chestNumber = "01", timeFormatted = "05:20.00", elapsedMillis = 320000L, rank = 1, status = "FINISHED"),
            RaceResult(id = 12, raceId = "race_20", studentId = "JBA-2026-002", studentName = "Amit", chestNumber = "02", timeFormatted = "05:20.00", elapsedMillis = 320000L, rank = 1, status = "FINISHED")
        )

        val report = MonthlyPerformanceEngine.generateMonthlyReport(
            monthKey = "2026-08",
            students = sampleStudents.take(2),
            allAttendance = emptyList(),
            allTraining = emptyList(),
            allWorkouts = emptyList(),
            allRaceSessions = listOf(raceSession),
            allRaceResults = raceResults
        )

        val vikram = report.cadetReports.find { it.studentId == "JBA-2026-001" }!!
        val amit = report.cadetReports.find { it.studentId == "JBA-2026-002" }!!

        // Both finished with equal time, both should have a valid top rank (1)
        assertEquals(1, vikram.rank1600m)
        assertEquals(1, amit.rank1600m)
    }

    @Test
    fun testPdfReportCardGeneration_ProducesValidFile() {
        val report = MonthlyPerformanceEngine.generateMonthlyReport(
            monthKey = "2026-08",
            students = sampleStudents,
            allAttendance = listOf(
                AttendanceRecord(studentId = "JBA-2026-001", date = "2026-08-01", status = "Present"),
                AttendanceRecord(studentId = "JBA-2026-001", date = "2026-08-02", status = "Present")
            ),
            allTraining = listOf(
                TrainingRecord(studentId = "JBA-2026-001", date = "2026-08-01", runningDuration = "05:12", runningType = "1600m Practice", pushups = 45, pullups = 12)
            ),
            allWorkouts = emptyList(),
            allRaceSessions = emptyList(),
            allRaceResults = emptyList()
        )

        val cadet = report.cadetReports.first()
        val pdfFile = PdfReportCardGenerator.generateCadetReportPdf(
            context = context,
            report = cadet,
            monthLabel = report.monthLabel,
            monthKey = report.monthKey
        )

        assertNotNull(pdfFile)
        assertTrue(pdfFile.exists())
        assertTrue("PDF file size should be > 0 bytes", pdfFile.length() > 0)
    }

    @Test
    fun testBulkPdfGeneration_ProducesValidAcademyReport() {
        val report = MonthlyPerformanceEngine.generateMonthlyReport(
            monthKey = "2026-08",
            students = sampleStudents,
            allAttendance = emptyList(),
            allTraining = emptyList(),
            allWorkouts = emptyList(),
            allRaceSessions = emptyList(),
            allRaceResults = emptyList()
        )

        val bulkPdfFile = PdfReportCardGenerator.generateBulkAcademyReportPdf(
            context = context,
            monthlyReport = report
        )

        assertNotNull(bulkPdfFile)
        assertTrue(bulkPdfFile.exists())
        assertTrue("Bulk PDF size should be > 0 bytes", bulkPdfFile.length() > 0)
    }

    @Test
    fun testRbacPermissions_AdminTrainerStudent() {
        // Admin
        assertTrue(RolePermissionManager.isAdmin("ADMIN"))
        assertTrue(RolePermissionManager.isStaff("ADMIN"))
        assertTrue(RolePermissionManager.canGenerateBulkCadetReports("ADMIN"))
        assertTrue(RolePermissionManager.canViewMonthlyAnalytics("ADMIN"))

        // Trainer
        assertTrue(RolePermissionManager.isTrainer("TRAINER"))
        assertTrue(RolePermissionManager.isStaff("TRAINER"))
        assertTrue(RolePermissionManager.canGenerateBulkCadetReports("TRAINER"))
        assertTrue(RolePermissionManager.canViewMonthlyAnalytics("TRAINER"))
        assertFalse(RolePermissionManager.canManageAdminSecurity("TRAINER")) // Trainer cannot change Admin PIN

        // Student
        assertTrue(RolePermissionManager.isStudent("STUDENT"))
        assertFalse(RolePermissionManager.isStaff("STUDENT"))
        assertFalse(RolePermissionManager.canViewMonthlyAnalytics("STUDENT")) // Student cannot see full academy analytics
        assertTrue(RolePermissionManager.canAccessCadetReport("STUDENT", "JBA-2026-001", "JBA-2026-001"))
        assertFalse(RolePermissionManager.canAccessCadetReport("STUDENT", "JBA-2026-001", "JBA-2026-002"))
    }
}
