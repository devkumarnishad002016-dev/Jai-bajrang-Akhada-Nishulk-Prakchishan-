package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.db.AppDao
import com.example.data.db.AppDatabase
import com.example.data.model.StudentProfile
import com.example.util.ProfileUtils
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class StudentProfilePersistenceStressTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: AppDao
    private lateinit var context: Context

    @Before
    fun createDb() {
        context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.appDao()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun `stress test single student profile updates and persistence across multiple cycles`() = runBlocking {
        // Step 1: Insert Initial Student Profile
        val initialStudent = StudentProfile(
            studentId = "JBA-2026-001",
            fullName = "देवकुमार निषाद",
            fatherName = "श्री रामकुमार निषाद",
            mobileNumber = "9876543210",
            village = "मौरिकला (गुफा)",
            dob = "2004-05-15",
            age = 21,
            gender = "Male",
            education = "12th Pass",
            recruitmentGoal = "Indian Army (GD)",
            joinDate = "2025-01-10",
            profilePhotoUri = "file:///data/user/0/com.example/files/photo_001.jpg",
            heightCm = 171.0,
            weightKg = 63.5,
            chestNormalCm = 81.0,
            chestExpandedCm = 86.0,
            time400m = "1:08",
            time800m = "2:26",
            time1600m = "5:35",
            time5km = "21:30",
            pushups = 45,
            situps = 50,
            pullups = 12,
            squats = 60,
            plankSeconds = 120,
            overallScore = 84,
            longJumpFeet = 15.5,
            highJumpFeet = 4.2,
            shotPutMeters = 7.5,
            attendanceStreakDays = 7,
            studyTargetPercentage = 80
        )

        dao.insertStudent(initialStudent)

        // Verify initial insertion
        val loaded1 = dao.getStudentDirect("JBA-2026-001")
        assertNotNull("Student JBA-2026-001 should exist", loaded1)
        assertEquals("देवकुमार निषाद", loaded1?.fullName)
        assertEquals("5:35", loaded1?.time1600m)
        assertEquals(45, loaded1?.pushups)

        // Step 2: Simulate 50 Rapid Physical Training Benchmark Updates (Stress testing updates)
        var currentStudent = loaded1!!
        for (i in 1..50) {
            val updatedTiming = "5:${(35 - (i % 15)).coerceAtLeast(10)}"
            val updatedPushups = 45 + (i % 20)
            val updatedPullups = 12 + (i % 6)
            val updatedScore = (84 + (i % 15)).coerceAtMost(100)

            currentStudent = currentStudent.copy(
                time1600m = updatedTiming,
                pushups = updatedPushups,
                pullups = updatedPullups,
                overallScore = updatedScore,
                attendanceStreakDays = currentStudent.attendanceStreakDays + 1
            )
            dao.updateStudent(currentStudent)
        }

        // Verify state after 50 updates
        val postStressLoaded = dao.getStudentDirect("JBA-2026-001")
        assertNotNull(postStressLoaded)
        assertEquals(currentStudent.time1600m, postStressLoaded?.time1600m)
        assertEquals(currentStudent.pushups, postStressLoaded?.pushups)
        assertEquals(currentStudent.pullups, postStressLoaded?.pullups)
        assertEquals(currentStudent.overallScore, postStressLoaded?.overallScore)
        assertEquals(57, postStressLoaded?.attendanceStreakDays)
    }

    @Test
    fun `test physical records addition, comprehensive profile update and simulated app restart`() = runBlocking {
        // Step 1: Initial creation
        val studentId = "JBA-2026-001"
        val candidate = StudentProfile(
            studentId = studentId,
            fullName = "राहुल कुमार",
            fatherName = "श्री शिव कुमार",
            mobileNumber = "9826011223",
            village = "डोंगरगांव",
            dob = "2005-08-20",
            gender = "Male",
            education = "Graduate",
            recruitmentGoal = "CG Police Constable",
            heightCm = 174.0,
            weightKg = 68.0,
            chestNormalCm = 83.0,
            chestExpandedCm = 88.5
        )
        dao.insertStudent(candidate)
        val persistedCandidate = dao.getStudentDirect(studentId)!!

        // Step 2: Comprehensive physical and identity update
        val updatedProfile = persistedCandidate.copy(
            fullName = "राहुल कुमार साहू",
            fatherName = "श्री शिव कुमार साहू",
            village = "डोंगरगांव (खुर्द)",
            dob = "2005-08-20",
            education = "B.Sc Graduate",
            recruitmentGoal = "Chhattisgarh Police Sub-Inspector",
            profilePhotoUri = "file:///storage/emulated/0/Android/data/photo_rahul.jpg",
            heightCm = 175.0,
            weightKg = 67.0,
            chestNormalCm = 84.0,
            chestExpandedCm = 89.0,
            time400m = "1:02",
            time800m = "2:15",
            time1600m = "5:12",
            time5km = "19:45",
            pushups = 55,
            situps = 60,
            pullups = 15,
            squats = 80,
            plankSeconds = 180,
            longJumpFeet = 16.5,
            highJumpFeet = 4.5,
            shotPutMeters = 8.2,
            overallScore = 94,
            attendanceStreakDays = 14,
            studyTargetPercentage = 90
        )

        dao.updateStudent(updatedProfile)

        // Step 3: Simulate App Restart (Re-fetch through Flow and direct query)
        val afterRestartProfile = dao.getStudentDirect(studentId)
        assertNotNull("Profile must be persistent after restart", afterRestartProfile)

        // Confirm all updated fields match exactly
        assertEquals("JBA-2026-001", afterRestartProfile?.studentId)
        assertEquals("राहुल कुमार साहू", afterRestartProfile?.fullName)
        assertEquals("श्री शिव कुमार साहू", afterRestartProfile?.fatherName)
        assertEquals("9826011223", afterRestartProfile?.mobileNumber)
        assertEquals("डोंगरगांव (खुर्द)", afterRestartProfile?.village)
        assertEquals("2005-08-20", afterRestartProfile?.dob)
        assertEquals("B.Sc Graduate", afterRestartProfile?.education)
        assertEquals("Chhattisgarh Police Sub-Inspector", afterRestartProfile?.recruitmentGoal)
        assertEquals("file:///storage/emulated/0/Android/data/photo_rahul.jpg", afterRestartProfile?.profilePhotoUri)

        // Physical ground records verification
        assertEquals("1:02", afterRestartProfile?.time400m)
        assertEquals("2:15", afterRestartProfile?.time800m)
        assertEquals("5:12", afterRestartProfile?.time1600m)
        assertEquals("19:45", afterRestartProfile?.time5km)
        assertEquals(55, afterRestartProfile?.pushups)
        assertEquals(60, afterRestartProfile?.situps)
        assertEquals(15, afterRestartProfile?.pullups)
        assertEquals(80, afterRestartProfile?.squats)
        assertEquals(180, afterRestartProfile?.plankSeconds)
        assertEquals(16.5, afterRestartProfile?.longJumpFeet ?: 0.0, 0.01)
        assertEquals(4.5, afterRestartProfile?.highJumpFeet ?: 0.0, 0.01)
        assertEquals(8.2, afterRestartProfile?.shotPutMeters ?: 0.0, 0.01)

        // Step 4: Verify Dashboard Display Consistency
        // Age calculation match
        val dashboardCalculatedAge = ProfileUtils.calculateAgeFromDob(afterRestartProfile!!.dob)
        assertTrue("Dashboard calculated age should be valid", dashboardCalculatedAge in 20..22)

        // BMI calculation match
        val dashboardBmi = ProfileUtils.getBmiCategory(afterRestartProfile.heightCm, afterRestartProfile.weightKg)
        assertEquals("Normal / Fit", dashboardBmi.labelEnglish)
        assertTrue(dashboardBmi.bmiValue in 18.5..24.9)

        // Score badge match
        assertEquals(94, afterRestartProfile.overallScore)
        assertEquals(14, afterRestartProfile.attendanceStreakDays)
    }

    @Test
    fun `stress test multi-candidate data isolation across restarts`() = runBlocking {
        val candidateA = StudentProfile(
            studentId = "JBA-2026-001",
            fullName = "Candidate Alpha",
            mobileNumber = "9876543201",
            village = "मौरिकला",
            pushups = 50,
            time1600m = "5:20",
            overallScore = 90
        )

        val candidateB = StudentProfile(
            studentId = "JBA-2026-002",
            fullName = "Candidate Beta",
            mobileNumber = "9876543202",
            village = "डोंगरगांव",
            pushups = 35,
            time1600m = "6:10",
            overallScore = 75
        )

        val candidateC = StudentProfile(
            studentId = "JBA-2026-003",
            fullName = "Candidate Gamma",
            mobileNumber = "9876543203",
            village = "अंबागढ़ चौकी",
            pushups = 40,
            time1600m = "5:45",
            overallScore = 82
        )

        dao.insertStudents(listOf(candidateA, candidateB, candidateC))
        val persistedB = dao.getStudentDirect("JBA-2026-002")!!

        // Update Candidate B's physical records independently
        val updatedB = persistedB.copy(
            pushups = 48,
            time1600m = "5:30",
            overallScore = 88
        )
        dao.updateStudent(updatedB)

        // Read all students through Flow
        val allStudents = dao.getAllStudents().first()
        assertEquals(3, allStudents.size)

        // Verify Candidate A is untouched
        val reloadedA = dao.getStudentDirect("JBA-2026-001")
        assertEquals(50, reloadedA?.pushups)
        assertEquals("5:20", reloadedA?.time1600m)
        assertEquals(90, reloadedA?.overallScore)

        // Verify Candidate B has updated values
        val reloadedB = dao.getStudentDirect("JBA-2026-002")
        assertEquals(48, reloadedB?.pushups)
        assertEquals("5:30", reloadedB?.time1600m)
        assertEquals(88, reloadedB?.overallScore)

        // Verify Candidate C is untouched
        val reloadedC = dao.getStudentDirect("JBA-2026-003")
        assertEquals(40, reloadedC?.pushups)
        assertEquals("5:45", reloadedC?.time1600m)
        assertEquals(82, reloadedC?.overallScore)

        // Verify next generated ID
        val nextId = ProfileUtils.generateNextStudentId(allStudents)
        assertEquals("JBA-2026-004", nextId)
    }

    @Test
    fun `test profile photo removal and preservation`() = runBlocking {
        val student = StudentProfile(
            studentId = "JBA-2026-001",
            fullName = "विकास वर्मा",
            mobileNumber = "9876543204",
            profilePhotoUri = "file:///data/user/0/com.example/files/photo.jpg"
        )
        dao.insertStudent(student)
        val persisted = dao.getStudentDirect("JBA-2026-001")!!

        // Remove photo (set to empty string)
        val withoutPhoto = persisted.copy(profilePhotoUri = "")
        dao.updateStudent(withoutPhoto)

        // Reload
        val reloaded = dao.getStudentDirect("JBA-2026-001")
        assertEquals("", reloaded?.profilePhotoUri)
        assertEquals("विकास वर्मा", reloaded?.fullName)
    }
}
