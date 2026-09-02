package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.db.AppDao
import com.example.data.db.AppDatabase
import com.example.data.model.AttendanceRecord
import com.example.data.model.StudentProfile
import com.example.data.model.TrainingRecord
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
class Phase2ATrainingAndAttendanceTest {

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
    fun `test creating daily training record linked to studentId`() = runBlocking {
        val student = StudentProfile(
            studentId = "JBA-2026-001",
            fullName = "देवकुमार निषाद",
            fatherName = "श्री रामकुमार",
            mobileNumber = "9876543210",
            village = "मौरिकला",
            dob = "2004-05-15",
            age = 21,
            gender = "Male",
            education = "12th Pass",
            recruitmentGoal = "Indian Army (GD)",
            joinDate = "2025-01-10"
        )
        dao.insertStudent(student)

        val training = TrainingRecord(
            studentId = "JBA-2026-001",
            date = "2026-03-30",
            runningDistanceKm = 1.6,
            runningDuration = "05:38",
            runningType = "1600m Practice",
            pushups = 45,
            situps = 50,
            pullups = 12,
            squats = 60,
            plankSeconds = 120,
            stretchingDone = true,
            otherTraining = "टायर ड्रैग और स्प्रिंट",
            trainerNotes = "उत्कृष्ट प्रदर्शन, टाइमिंग में सुधार"
        )
        val id = dao.insertTrainingRecord(training)
        assertTrue(id > 0)

        val records = dao.getTrainingRecordsForStudent("JBA-2026-001").first()
        assertEquals(1, records.size)
        assertEquals("JBA-2026-001", records[0].studentId)
        assertEquals(1.6, records[0].runningDistanceKm, 0.001)
        assertEquals(45, records[0].pushups)
        assertEquals("1600m Practice", records[0].runningType)
    }

    @Test
    fun `test historical training records persistence without overwriting`() = runBlocking {
        val studentId = "JBA-2026-001"
        val recordDay1 = TrainingRecord(
            studentId = studentId,
            date = "2026-03-28",
            runningDistanceKm = 3.0,
            runningDuration = "14:10",
            runningType = "Long Run",
            pushups = 40,
            situps = 45,
            pullups = 10,
            squats = 50,
            plankSeconds = 90
        )
        val recordDay2 = TrainingRecord(
            studentId = studentId,
            date = "2026-03-29",
            runningDistanceKm = 1.6,
            runningDuration = "05:40",
            runningType = "1600m Practice",
            pushups = 42,
            situps = 50,
            pullups = 12,
            squats = 55,
            plankSeconds = 100
        )
        val recordDay3 = TrainingRecord(
            studentId = studentId,
            date = "2026-03-30",
            runningDistanceKm = 5.0,
            runningDuration = "22:15",
            runningType = "5 KM Practice",
            pushups = 45,
            situps = 55,
            pullups = 14,
            squats = 60,
            plankSeconds = 120
        )

        dao.insertTrainingRecord(recordDay1)
        dao.insertTrainingRecord(recordDay2)
        dao.insertTrainingRecord(recordDay3)

        val history = dao.getTrainingRecordsForStudent(studentId).first()
        assertEquals(3, history.size)
        // Verify newest first ordering
        assertEquals("2026-03-30", history[0].date)
        assertEquals("2026-03-29", history[1].date)
        assertEquals("2026-03-28", history[2].date)
    }

    @Test
    fun `test student training data isolation between Student A and Student B`() = runBlocking {
        val recordA1 = TrainingRecord(studentId = "STUDENT-A", date = "2026-03-30", runningDistanceKm = 1.6, pushups = 45)
        val recordA2 = TrainingRecord(studentId = "STUDENT-A", date = "2026-03-29", runningDistanceKm = 3.0, pushups = 40)
        val recordB1 = TrainingRecord(studentId = "STUDENT-B", date = "2026-03-30", runningDistanceKm = 5.0, pushups = 30)

        dao.insertTrainingRecord(recordA1)
        dao.insertTrainingRecord(recordA2)
        dao.insertTrainingRecord(recordB1)

        val listA = dao.getTrainingRecordsForStudent("STUDENT-A").first()
        val listB = dao.getTrainingRecordsForStudent("STUDENT-B").first()

        assertEquals(2, listA.size)
        assertEquals(1, listB.size)
        assertTrue(listA.all { it.studentId == "STUDENT-A" })
        assertTrue(listB.all { it.studentId == "STUDENT-B" })
    }

    @Test
    fun `test attendance unique constraint per student and date upsert`() = runBlocking {
        val studentId = "JBA-2026-001"
        val date = "2026-03-30"

        val att1 = AttendanceRecord(studentId = studentId, date = date, status = "Present")
        dao.insertAttendance(att1)

        var records = dao.getAttendanceForStudent(studentId).first()
        assertEquals(1, records.size)
        assertEquals("Present", records[0].status)

        // Trainer modifies attendance on the same date to Leave
        val att2 = AttendanceRecord(studentId = studentId, date = date, status = "Leave")
        dao.insertAttendance(att2)

        records = dao.getAttendanceForStudent(studentId).first()
        assertEquals("Duplicate attendance for same student and date must replace cleanly", 1, records.size)
        assertEquals("Leave", records[0].status)
    }

    @Test
    fun `test trainer batch attendance marking across multiple students`() = runBlocking {
        val date = "2026-03-30"
        val batch = listOf(
            AttendanceRecord(studentId = "JBA-001", date = date, status = "Present"),
            AttendanceRecord(studentId = "JBA-002", date = date, status = "Present"),
            AttendanceRecord(studentId = "JBA-003", date = date, status = "Absent"),
            AttendanceRecord(studentId = "JBA-004", date = date, status = "Leave")
        )

        dao.insertAttendanceList(batch)

        val dateAttendance = dao.getAttendanceByDate(date).first()
        assertEquals(4, dateAttendance.size)

        val presentCount = dateAttendance.count { it.status == "Present" }
        val absentCount = dateAttendance.count { it.status == "Absent" }
        val leaveCount = dateAttendance.count { it.status == "Leave" }

        assertEquals(2, presentCount)
        assertEquals(1, absentCount)
        assertEquals(1, leaveCount)
    }
}
