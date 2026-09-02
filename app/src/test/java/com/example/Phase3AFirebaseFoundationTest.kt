package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.cloud.*
import com.example.data.db.AppDao
import com.example.data.db.AppDatabase
import com.example.data.model.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class Phase3AFirebaseFoundationTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: AppDao
    private lateinit var context: Context
    private lateinit var syncManager: FirestoreDataSyncManager

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.appDao()
        syncManager = FirestoreDataSyncManager(dao, firestore = null)
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun testFirestoreConstants_CorrectCollectionNames() {
        assertEquals("users", FirestoreConstants.COLLECTION_USERS)
        assertEquals("students", FirestoreConstants.COLLECTION_STUDENTS)
        assertEquals("attendance_records", FirestoreConstants.COLLECTION_ATTENDANCE)
        assertEquals("training_records", FirestoreConstants.COLLECTION_TRAINING)
        assertEquals("notices", FirestoreConstants.COLLECTION_NOTICES)
        assertEquals("recruitment_info", FirestoreConstants.COLLECTION_RECRUITMENT)
        assertEquals("STUDENT", FirestoreConstants.ROLE_STUDENT)
        assertEquals("ADMIN", FirestoreConstants.ROLE_ADMIN)
    }

    @Test
    fun testCloudUserProfile_ToAndFromMap() {
        val original = CloudUserProfile(
            uid = "test-uid-12345",
            email = "vikram.rathore@akhada.org",
            displayName = "Vikram Rathore",
            phoneNumber = "+919876543210",
            role = FirestoreConstants.ROLE_STUDENT,
            linkedStudentId = "JBA-2026-001",
            createdAt = 1756000000000L,
            lastLoginAt = 1756001000000L,
            isActive = true
        )

        val map = original.toMap()
        assertEquals("test-uid-12345", map["uid"])
        assertEquals("vikram.rathore@akhada.org", map["email"])
        assertEquals("STUDENT", map["role"])
        assertEquals("JBA-2026-001", map["linkedStudentId"])

        val deserialized = CloudUserProfile.fromMap(map)
        assertEquals(original.uid, deserialized.uid)
        assertEquals(original.email, deserialized.email)
        assertEquals(original.displayName, deserialized.displayName)
        assertEquals(original.role, deserialized.role)
        assertEquals(original.linkedStudentId, deserialized.linkedStudentId)
        assertEquals(original.isActive, deserialized.isActive)
    }

    @Test
    fun testFirestoreDataSyncManager_StudentMapping() {
        val student = StudentProfile(
            id = 1,
            studentId = "JBA-2026-042",
            fullName = "Rohit Verma",
            fatherName = "Shri R. Verma",
            mobileNumber = "9823456789",
            village = "Kawardha",
            dob = "2005-08-14",
            age = 20,
            gender = "Male",
            education = "12th Pass",
            recruitmentGoal = "CG Police",
            heightCm = 174.5,
            weightKg = 67.0,
            chestNormalCm = 83.0,
            chestExpandedCm = 89.0,
            time1600m = "5:18",
            pushups = 48,
            pullups = 14
        )

        val map = syncManager.studentToMap(student)
        assertEquals("JBA-2026-042", map["studentId"])
        assertEquals("Rohit Verma", map["fullName"])
        assertEquals("CG Police", map["recruitmentGoal"])
        assertEquals(174.5, map["heightCm"])

        val reconstructed = syncManager.mapToStudent("JBA-2026-042", map)
        assertNotNull(reconstructed)
        assertEquals(student.studentId, reconstructed?.studentId)
        assertEquals(student.fullName, reconstructed?.fullName)
        assertEquals(student.village, reconstructed?.village)
        assertEquals(student.time1600m, reconstructed?.time1600m)
    }

    @Test
    fun testAttendanceAndTrainingRecordMapping() {
        val attendance = AttendanceRecord(
            studentId = "JBA-2026-001",
            date = "2026-08-24",
            status = "Present",
            remarks = "समय पर उपस्थित"
        )
        val attMap = syncManager.attendanceToMap(attendance)
        assertEquals("JBA-2026-001", attMap["studentId"])
        assertEquals("Present", attMap["status"])

        val training = TrainingRecord(
            studentId = "JBA-2026-001",
            date = "2026-08-24",
            runningDistanceKm = 1.6,
            runningDuration = "5:20",
            runningType = "1600m Practice",
            pushups = 45,
            situps = 50,
            pullups = 12,
            squats = 60,
            plankSeconds = 120,
            stretchingDone = true
        )
        val trMap = syncManager.trainingRecordToMap(training)
        assertEquals("JBA-2026-001", trMap["studentId"])
        assertEquals("5:20", trMap["runningDuration"])
        assertEquals(45, trMap["pushups"])
    }

    @Test
    fun testNoticeAndRecruitmentMapping() {
        val notice = Notice(
            id = 5,
            title = "ग्राउंड ट्रायल दिनांक",
            content = "सभी छात्र रविवार सुबह 5:30 बजे पहुंचें",
            date = "2026-08-25",
            category = "प्रशिक्षण",
            isPinned = true,
            isRead = false
        )
        val noticeMap = syncManager.noticeToMap(notice)
        val reconstructedNotice = syncManager.mapToNotice("notice_5", noticeMap)
        assertNotNull(reconstructedNotice)
        assertEquals(5L, reconstructedNotice?.id)
        assertEquals("ग्राउंड ट्रायल दिनांक", reconstructedNotice?.title)
        assertTrue(reconstructedNotice?.isPinned == true)

        val recruitment = RecruitmentInfo(
            id = 10,
            recruitmentName = "SSC GD CAPF 2026",
            organization = "Staff Selection Commission",
            eligibility = "10th Pass",
            ageLimit = "18-23 वर्ष",
            heightRequirement = "170 cm",
            chestRequirement = "80-85 cm",
            physicalTest = "5 KM Run in 24 min",
            writtenExam = "Online CBT 80 Questions",
            syllabus = "Math, Reasoning, GK, Hindi",
            importantDocuments = "10th Marksheet, Aadhar, Domicile",
            importantDates = "Last Date: 15 Oct 2026",
            officialWebsiteLink = "https://ssc.gov.in"
        )
        val recMap = syncManager.recruitmentToMap(recruitment)
        val reconstructedRec = syncManager.mapToRecruitment("recruitment_10", recMap)
        assertNotNull(reconstructedRec)
        assertEquals(10L, reconstructedRec?.id)
        assertEquals("SSC GD CAPF 2026", reconstructedRec?.recruitmentName)
    }

    @Test
    fun testCloudSyncStatus_Transitions() {
        val idle: CloudSyncStatus = CloudSyncStatus.Idle
        assertEquals(CloudSyncStatus.Idle, idle)

        val syncing: CloudSyncStatus = CloudSyncStatus.Syncing("डेटा लोड हो रहा है...")
        assertTrue(syncing is CloudSyncStatus.Syncing)

        val success: CloudSyncStatus = CloudSyncStatus.Success(recordsSynced = 15)
        assertEquals(15, (success as CloudSyncStatus.Success).recordsSynced)

        val error: CloudSyncStatus = CloudSyncStatus.Error("ऑफलाइन मोड", isOffline = true)
        assertTrue((error as CloudSyncStatus.Error).isOffline)
    }
}
