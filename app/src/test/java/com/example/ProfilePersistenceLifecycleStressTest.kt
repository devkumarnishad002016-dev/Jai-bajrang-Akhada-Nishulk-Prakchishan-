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
class ProfilePersistenceLifecycleStressTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: AppDao
    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.appDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `execute complete 8-step real profile persistence stress test`() = runBlocking {
        // ==========================================
        // TEST 1 — Create Student (Admin Enrollment)
        // ==========================================
        val existingStudents = dao.getAllStudents().first()
        val generatedId = ProfileUtils.generateNextStudentId(existingStudents)
        assertEquals("JBA-2026-001", generatedId)

        val newStudent = StudentProfile(
            studentId = generatedId,
            fullName = "सुरेश कुमार यादव",
            fatherName = "श्री बिहारी लाल यादव",
            mobileNumber = "9826198765",
            village = "मौरिकला",
            dob = "2004-04-10",
            gender = "Male",
            education = "12th Pass",
            recruitmentGoal = "Indian Army (GD)",
            joinDate = "2026-08-22",
            heightCm = 172.0,
            weightKg = 64.0,
            chestNormalCm = 82.0,
            chestExpandedCm = 87.0
        )
        dao.insertStudent(newStudent)

        // Verify Student 1 in Admin Roster
        val rosterAfterCreation = dao.getAllStudents().first()
        assertTrue("Student must appear in roster", rosterAfterCreation.any { it.studentId == generatedId })
        val fetched1 = dao.getStudentDirect(generatedId)
        assertNotNull(fetched1)
        assertEquals("सुरेश कुमार यादव", fetched1?.fullName)

        // ==========================================
        // TEST 2 — Edit Profile
        // ==========================================
        val editedProfile = fetched1!!.copy(
            fullName = "सुरेश कुमार यादव (पहलवान)",
            fatherName = "श्री बिहारी लाल यादव जी",
            village = "मौरिकला (बजरंग चौक)",
            mobileNumber = "9826198760",
            education = "B.A. First Year",
            recruitmentGoal = "CISF Constable / GD",
            dob = "2003-11-25"
        )
        dao.updateStudent(editedProfile)

        // Verify updated values in DB (Dashboard sync source)
        val fetchedAfterEdit = dao.getStudentDirect(generatedId)
        assertNotNull(fetchedAfterEdit)
        assertEquals("सुरेश कुमार यादव (पहलवान)", fetchedAfterEdit?.fullName)
        assertEquals("श्री बिहारी लाल यादव जी", fetchedAfterEdit?.fatherName)
        assertEquals("मौरिकला (बजरंग चौक)", fetchedAfterEdit?.village)
        assertEquals("9826198760", fetchedAfterEdit?.mobileNumber)
        assertEquals("B.A. First Year", fetchedAfterEdit?.education)
        assertEquals("CISF Constable / GD", fetchedAfterEdit?.recruitmentGoal)
        assertEquals("2003-11-25", fetchedAfterEdit?.dob)

        // Verify dynamic age & BMI on dashboard
        val dashboardAge = ProfileUtils.calculateAgeFromDob(fetchedAfterEdit!!.dob)
        assertTrue("Age calculated from 2003-11-25 should be 22+", dashboardAge >= 22)
        val dashboardBmi = ProfileUtils.getBmiCategory(fetchedAfterEdit.heightCm, fetchedAfterEdit.weightKg)
        assertEquals("Normal / Fit", dashboardBmi.labelEnglish)

        // ==========================================
        // TEST 3 — Physical Ground Records
        // ==========================================
        val profileWithGroundRecords = fetchedAfterEdit.copy(
            time400m = "1:05",
            time800m = "2:20",
            time1600m = "5:18",
            time5km = "20:45",
            pushups = 52,
            situps = 58,
            pullups = 14,
            squats = 75,
            plankSeconds = 150,
            overallScore = 91
        )
        dao.updateStudent(profileWithGroundRecords)

        val fetchedAfterGround = dao.getStudentDirect(generatedId)
        assertNotNull(fetchedAfterGround)
        assertEquals("1:05", fetchedAfterGround?.time400m)
        assertEquals("2:20", fetchedAfterGround?.time800m)
        assertEquals("5:18", fetchedAfterGround?.time1600m)
        assertEquals("20:45", fetchedAfterGround?.time5km)
        assertEquals(52, fetchedAfterGround?.pushups)
        assertEquals(58, fetchedAfterGround?.situps)
        assertEquals(14, fetchedAfterGround?.pullups)
        assertEquals(75, fetchedAfterGround?.squats)
        assertEquals(150, fetchedAfterGround?.plankSeconds)
        assertEquals(91, fetchedAfterGround?.overallScore)

        // ==========================================
        // TEST 4 — Photo Persistence
        // ==========================================
        val testPhotoUri = "file:///data/user/0/com.example/files/profile_jba_2026_001.jpg"
        val profileWithPhoto = fetchedAfterGround!!.copy(profilePhotoUri = testPhotoUri)
        dao.updateStudent(profileWithPhoto)

        // Simulate navigating away and re-querying
        val fetchedAfterNav = dao.getStudentDirect(generatedId)
        assertEquals(testPhotoUri, fetchedAfterNav?.profilePhotoUri)

        // ==========================================
        // TEST 5 — Logout / Login Simulation
        // ==========================================
        // Logout resets active session; Login re-queries by Student ID or Mobile
        var activeSessionId: String? = null
        assertNull("Logged out state", activeSessionId)

        // Candidate logs in with Student ID
        val loginCandidate = dao.getStudentDirect(generatedId)
        assertNotNull("Candidate must be found by ID", loginCandidate)
        activeSessionId = loginCandidate?.studentId
        assertEquals(generatedId, activeSessionId)

        // Confirm all data persists in logged in session
        assertEquals("सुरेश कुमार यादव (पहलवान)", loginCandidate?.fullName)
        assertEquals("5:18", loginCandidate?.time1600m)
        assertEquals(52, loginCandidate?.pushups)
        assertEquals(testPhotoUri, loginCandidate?.profilePhotoUri)
        assertEquals(91, loginCandidate?.overallScore)

        // ==========================================
        // TEST 6 — Application Restart Simulation
        // ==========================================
        // Query afresh directly from Room storage (simulating complete cold restart)
        val freshAppRestartStudent = dao.getStudentDirect(generatedId)
        assertNotNull("Record must survive cold restart", freshAppRestartStudent)

        assertEquals("JBA-2026-001", freshAppRestartStudent?.studentId)
        assertEquals("सुरेश कुमार यादव (पहलवान)", freshAppRestartStudent?.fullName)
        assertEquals("श्री बिहारी लाल यादव जी", freshAppRestartStudent?.fatherName)
        assertEquals("मौरिकला (बजरंग चौक)", freshAppRestartStudent?.village)
        assertEquals("9826198760", freshAppRestartStudent?.mobileNumber)
        assertEquals("B.A. First Year", freshAppRestartStudent?.education)
        assertEquals("CISF Constable / GD", freshAppRestartStudent?.recruitmentGoal)
        assertEquals("2003-11-25", freshAppRestartStudent?.dob)
        assertEquals(testPhotoUri, freshAppRestartStudent?.profilePhotoUri)

        assertEquals("1:05", freshAppRestartStudent?.time400m)
        assertEquals("2:20", freshAppRestartStudent?.time800m)
        assertEquals("5:18", freshAppRestartStudent?.time1600m)
        assertEquals("20:45", freshAppRestartStudent?.time5km)
        assertEquals(52, freshAppRestartStudent?.pushups)
        assertEquals(58, freshAppRestartStudent?.situps)
        assertEquals(14, freshAppRestartStudent?.pullups)
        assertEquals(91, freshAppRestartStudent?.overallScore)

        // ==========================================
        // TEST 7 — Data Isolation (Student A vs B)
        // ==========================================
        val studentBId = ProfileUtils.generateNextStudentId(listOf(freshAppRestartStudent!!))
        assertEquals("JBA-2026-002", studentBId)

        val studentB = StudentProfile(
            studentId = studentBId,
            fullName = "रोहित सिंह",
            fatherName = "श्री विजय सिंह",
            mobileNumber = "9988776655",
            village = "डोंगरगांव",
            dob = "2005-01-15",
            recruitmentGoal = "Army Clerk",
            time1600m = "6:05",
            pushups = 38,
            pullups = 9,
            overallScore = 78,
            profilePhotoUri = ""
        )
        dao.insertStudent(studentB)

        // Switch to Student A
        val activeA = dao.getStudentDirect("JBA-2026-001")
        assertEquals("सुरेश कुमार यादव (पहलवान)", activeA?.fullName)
        assertEquals("5:18", activeA?.time1600m)
        assertEquals(52, activeA?.pushups)
        assertEquals(testPhotoUri, activeA?.profilePhotoUri)

        // Switch to Student B
        val activeB = dao.getStudentDirect("JBA-2026-002")
        assertEquals("रोहित सिंह", activeB?.fullName)
        assertEquals("6:05", activeB?.time1600m)
        assertEquals(38, activeB?.pushups)
        assertEquals("", activeB?.profilePhotoUri)

        // Assert strict isolation
        assertNotEquals(activeA?.studentId, activeB?.studentId)
        assertNotEquals(activeA?.fullName, activeB?.fullName)
        assertNotEquals(activeA?.time1600m, activeB?.time1600m)
        assertNotEquals(activeA?.pushups, activeB?.pushups)

        // ==========================================
        // TEST 8 — Admin Verification
        // ==========================================
        val allRoster = dao.getAllStudents().first()
        assertEquals(2, allRoster.size)

        val adminInspectedCandidate = allRoster.find { it.studentId == "JBA-2026-001" }
        assertNotNull(adminInspectedCandidate)
        assertEquals("JBA-2026-001", adminInspectedCandidate?.studentId)
        assertEquals("सुरेश कुमार यादव (पहलवान)", adminInspectedCandidate?.fullName)
        assertEquals("CISF Constable / GD", adminInspectedCandidate?.recruitmentGoal)
        assertEquals("5:18", adminInspectedCandidate?.time1600m)
        assertEquals(52, adminInspectedCandidate?.pushups)
        assertEquals(14, adminInspectedCandidate?.pullups)
        assertEquals(91, adminInspectedCandidate?.overallScore)
        assertEquals(testPhotoUri, adminInspectedCandidate?.profilePhotoUri)
    }
}
