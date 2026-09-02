package com.example

import android.app.Application
import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.db.AppDao
import com.example.data.db.AppDatabase
import com.example.data.model.RaceResult
import com.example.data.model.RaceSession
import com.example.data.model.StudentProfile
import com.example.data.model.TrainingRecord
import com.example.ui.viewmodel.MainViewModel
import com.example.util.RolePermissionManager
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
class Phase2BLiveBatchStopwatchTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: AppDao
    private lateinit var context: Context
    private lateinit var application: Application
    private lateinit var viewModel: MainViewModel

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        application = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.appDao()
        viewModel = MainViewModel(application)
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `test race session and race result room persistence`() = runBlocking {
        val session = RaceSession(
            raceId = "test_race_001",
            batchName = "1600m टेस्ट बैच",
            distanceMeters = 1600,
            date = "2026-08-27",
            startTimeMillis = 1756200000000L,
            endTimeMillis = 1756200330000L,
            status = "COMPLETED",
            notes = "गुफा ग्राउंड ट्रैक",
            recordedBy = "TRAINER",
            totalParticipants = 2,
            finishedCount = 2
        )

        dao.insertRaceSession(session)

        val retrievedSession = dao.getRaceSessionDirect("test_race_001")
        assertNotNull(retrievedSession)
        assertEquals("1600m टेस्ट बैच", retrievedSession?.batchName)
        assertEquals(1600, retrievedSession?.distanceMeters)

        val results = listOf(
            RaceResult(
                id = 1,
                raceId = "test_race_001",
                studentId = "JBA-2026-001",
                studentName = "विकास निषाद",
                chestNumber = "101",
                village = "मौरिकला",
                status = "FINISHED",
                finishTimestamp = 1756200315000L,
                elapsedMillis = 315000L,
                timeFormatted = "05:15.00",
                rank = 1,
                isPersonalBest = true,
                notes = "उत्कृष्ट पेस"
            ),
            RaceResult(
                id = 2,
                raceId = "test_race_001",
                studentId = "JBA-2026-002",
                studentName = "अजय यादव",
                chestNumber = "102",
                village = "सोनपुर",
                status = "FINISHED",
                finishTimestamp = 1756200328000L,
                elapsedMillis = 328000L,
                timeFormatted = "05:28.00",
                rank = 2,
                isPersonalBest = false,
                notes = "अच्छा प्रयास"
            )
        )

        dao.insertRaceResults(results)

        val sessionResults = dao.getRaceResultsForSessionDirect("test_race_001")
        assertEquals(2, sessionResults.size)
        assertEquals("101", sessionResults[0].chestNumber)
        assertEquals(1, sessionResults[0].rank)
        assertEquals("05:15.00", sessionResults[0].timeFormatted)
        assertEquals("102", sessionResults[1].chestNumber)
        assertEquals(2, sessionResults[1].rank)
    }

    @Test
    fun `test role permissions for live stopwatch`() {
        // Admin allowed
        assertTrue(RolePermissionManager.canOperateLiveStopwatch(RolePermissionManager.ROLE_ADMIN))
        // Trainer allowed
        assertTrue(RolePermissionManager.canOperateLiveStopwatch(RolePermissionManager.ROLE_TRAINER))
        // Student strictly denied
        assertFalse(RolePermissionManager.canOperateLiveStopwatch(RolePermissionManager.ROLE_STUDENT))
    }

    @Test
    fun `test stopwatch batch setup and finish recorder logic`() = runBlocking {
        // Login as Trainer
        viewModel.loginAsTrainer("5678")
        assertEquals(RolePermissionManager.ROLE_TRAINER, viewModel.currentRole.value)

        val students = listOf(
            StudentProfile(
                studentId = "JBA-T-001",
                fullName = "रोहित कुमार",
                fatherName = "श्री राम",
                mobileNumber = "9988776655",
                village = "मौरिकला",
                dob = "2004-01-01",
                age = 22,
                gender = "Male",
                education = "12th",
                recruitmentGoal = "Army GD",
                time1600m = "05:30"
            ),
            StudentProfile(
                studentId = "JBA-T-002",
                fullName = "अमित साहू",
                fatherName = "श्री श्याम",
                mobileNumber = "9988776656",
                village = "डोंगरगढ़",
                dob = "2004-02-02",
                age = 22,
                gender = "Male",
                education = "12th",
                recruitmentGoal = "CG Police",
                time1600m = "05:45"
            )
        )

        val chestMap = mapOf(
            "JBA-T-001" to "501",
            "JBA-T-002" to "502"
        )

        // Setup batch
        viewModel.setupNewRaceSession(
            batchName = "1600m ट्रायल बैच-1",
            distanceMeters = 1600,
            participants = students,
            chestNumbers = chestMap,
            notes = "ट्रैक ड्राई"
        )

        val session = viewModel.activeLiveSession.value
        assertNotNull(session)
        assertEquals("1600m ट्रायल बैच-1", session?.batchName)
        assertEquals(1600, session?.distanceMeters)
        assertEquals(2, session?.totalParticipants)

        val liveList = viewModel.liveParticipants.value
        assertEquals(2, liveList.size)
        assertEquals("501", liveList[0].chestNumber)
        assertEquals("502", liveList[1].chestNumber)
        assertEquals("RUNNING", liveList[0].status)

        // Start stopwatch
        viewModel.startMasterStopwatch()
        assertTrue(viewModel.isStopwatchRunning.value)

        // Record finish for cadet 1
        viewModel.recordCadetFinish("JBA-T-001")
        val afterFinish1 = viewModel.liveParticipants.value
        val cadet1Result = afterFinish1.find { it.studentId == "JBA-T-001" }
        assertEquals("FINISHED", cadet1Result?.status)
        assertEquals(1, cadet1Result?.rank)

        // Record finish for cadet 2
        viewModel.recordCadetFinish("JBA-T-002")
        val afterFinish2 = viewModel.liveParticipants.value
        val cadet2Result = afterFinish2.find { it.studentId == "JBA-T-002" }
        assertEquals("FINISHED", cadet2Result?.status)
        assertEquals(2, cadet2Result?.rank)

        // Test Undo cadet 1 finish
        viewModel.undoCadetFinish("JBA-T-001")
        val afterUndo = viewModel.liveParticipants.value
        val cadet1AfterUndo = afterUndo.find { it.studentId == "JBA-T-001" }
        assertEquals("RUNNING", cadet1AfterUndo?.status)
        assertEquals(0, cadet1AfterUndo?.rank)

        // Re-finish cadet 1
        viewModel.recordCadetFinish("JBA-T-001")
        val afterRefinish = viewModel.liveParticipants.value.find { it.studentId == "JBA-T-001" }
        assertEquals("FINISHED", afterRefinish?.status)
    }

    @Test
    fun `test time formatting utility`() {
        // 5 minutes, 14 seconds, 32 hundredths = 314320 ms
        val millis = 314320L
        val formatted = viewModel.formatRaceTime(millis)
        assertEquals("05:14.32", formatted)
    }
}
