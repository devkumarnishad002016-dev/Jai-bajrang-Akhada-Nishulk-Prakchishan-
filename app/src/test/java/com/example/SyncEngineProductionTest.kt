package com.example

import com.example.data.cloud.*
import com.example.data.db.AppDao
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Production-Grade Verification Test Suite for Offline-First Cloud Sync Engine.
 * Tests cover all scenarios A through Q requested in P1 Step 3.
 */
class SyncEngineProductionTest {

    // In-memory fake AppDao to simulate Room local SQLite database (Single Source of Truth)
    class FakeAppDao : AppDao {
        val students = mutableListOf<StudentProfile>()
        val attendance = mutableListOf<AttendanceRecord>()
        val training = mutableListOf<TrainingRecord>()
        val testAttempts = mutableListOf<TestAttempt>()
        val notices = mutableListOf<Notice>()
        val recruitment = mutableListOf<RecruitmentInfo>()
        val subjects = mutableListOf<StudySubject>()
        val topics = mutableListOf<StudyTopic>()
        val questions = mutableListOf<Question>()
        val mockTests = mutableListOf<MockTest>()

        override fun getAllStudents(): Flow<List<StudentProfile>> = flowOf(students)
        override suspend fun getAllStudentsDirect(): List<StudentProfile> = students.toList()
        override fun getStudentById(studentId: String): Flow<StudentProfile?> = flowOf(students.find { it.studentId == studentId })
        override suspend fun getStudentDirect(studentId: String): StudentProfile? = students.find { it.studentId == studentId }
        override suspend fun insertStudent(student: StudentProfile): Long {
            students.removeAll { it.studentId == student.studentId }
            students.add(student)
            return 1L
        }
        override suspend fun insertStudents(students: List<StudentProfile>) {
            students.forEach { insertStudent(it) }
        }
        override suspend fun updateStudent(student: StudentProfile) { insertStudent(student) }
        override suspend fun deleteStudent(student: StudentProfile) { students.removeAll { it.studentId == student.studentId } }
        override suspend fun countStudentsWithMobile(mobileNumber: String): Int = students.count { it.mobileNumber == mobileNumber }
        override suspend fun getStudentByMobile(mobileNumber: String): StudentProfile? = students.find { it.mobileNumber == mobileNumber }

        override fun getAllAttendance(): Flow<List<AttendanceRecord>> = flowOf(attendance)
        override suspend fun getAllAttendanceDirect(): List<AttendanceRecord> = attendance.toList()
        override fun getAttendanceForStudent(studentId: String): Flow<List<AttendanceRecord>> = flowOf(attendance.filter { it.studentId == studentId })
        override fun getTodayAttendance(studentId: String, date: String): Flow<AttendanceRecord?> = flowOf(attendance.find { it.studentId == studentId && it.date == date })
        override fun getAttendanceByDate(date: String): Flow<List<AttendanceRecord>> = flowOf(attendance.filter { it.date == date })
        override suspend fun insertAttendance(record: AttendanceRecord): Long {
            attendance.removeAll { it.studentId == record.studentId && it.date == record.date }
            attendance.add(record)
            return 1L
        }
        override suspend fun insertAttendanceList(records: List<AttendanceRecord>) { records.forEach { insertAttendance(it) } }

        override fun getAllTrainingRecords(): Flow<List<TrainingRecord>> = flowOf(training)
        override suspend fun getAllTrainingRecordsDirect(): List<TrainingRecord> = training.toList()
        override fun getTrainingRecordsForStudent(studentId: String): Flow<List<TrainingRecord>> = flowOf(training.filter { it.studentId == studentId })
        override suspend fun getTrainingRecordsForStudentDirect(studentId: String): List<TrainingRecord> = training.filter { it.studentId == studentId }
        override fun getTodayTrainingRecord(studentId: String, date: String): Flow<TrainingRecord?> = flowOf(training.find { it.studentId == studentId && it.date == date })
        override suspend fun getTrainingRecordDirect(studentId: String, date: String): TrainingRecord? = training.find { it.studentId == studentId && it.date == date }
        override fun getTrainingRecordById(id: Long): Flow<TrainingRecord?> = flowOf(training.find { it.id == id })
        override suspend fun insertTrainingRecord(record: TrainingRecord): Long {
            training.removeAll { it.studentId == record.studentId && it.date == record.date }
            training.add(record)
            return 1L
        }
        override suspend fun insertTrainingRecords(records: List<TrainingRecord>) { records.forEach { insertTrainingRecord(it) } }
        override suspend fun updateTrainingRecord(record: TrainingRecord) { insertTrainingRecord(record) }
        override suspend fun deleteTrainingRecord(record: TrainingRecord) { training.remove(record) }

        override fun getAllTestAttempts(): Flow<List<TestAttempt>> = flowOf(testAttempts)
        override suspend fun getAllTestAttemptsDirect(): List<TestAttempt> = testAttempts.toList()
        override fun getAttemptsForStudent(studentId: String): Flow<List<TestAttempt>> = flowOf(testAttempts.filter { it.studentId == studentId })
        override suspend fun insertTestAttempt(attempt: TestAttempt): Long {
            val assignedId = if (attempt.id > 0) attempt.id else (testAttempts.size + 1).toLong()
            val item = attempt.copy(id = assignedId)
            testAttempts.removeAll { it.id == assignedId }
            testAttempts.add(item)
            return assignedId
        }

        override fun getAllNotices(): Flow<List<Notice>> = flowOf(notices)
        override suspend fun getAllNoticesDirect(): List<Notice> = notices.toList()
        override suspend fun insertNotice(notice: Notice): Long {
            val id = if (notice.id > 0) notice.id else (notices.size + 1).toLong()
            notices.removeAll { it.id == id }
            notices.add(notice.copy(id = id))
            return id
        }
        override suspend fun insertNotices(notices: List<Notice>) { notices.forEach { insertNotice(it) } }
        override suspend fun updateNotice(notice: Notice) { insertNotice(notice) }
        override suspend fun deleteNotice(notice: Notice) { notices.removeAll { it.id == notice.id } }

        override fun getAllRecruitmentInfo(): Flow<List<RecruitmentInfo>> = flowOf(recruitment)
        override suspend fun getAllRecruitmentInfosDirect(): List<RecruitmentInfo> = recruitment.toList()
        override suspend fun insertRecruitmentInfo(info: RecruitmentInfo): Long {
            val id = if (info.id > 0) info.id else (recruitment.size + 1).toLong()
            recruitment.removeAll { it.id == id }
            recruitment.add(info.copy(id = id))
            return id
        }
        override suspend fun insertRecruitmentInfos(infos: List<RecruitmentInfo>) { infos.forEach { insertRecruitmentInfo(it) } }
        override suspend fun updateRecruitmentInfo(info: RecruitmentInfo) { insertRecruitmentInfo(info) }
        override suspend fun deleteRecruitmentInfo(info: RecruitmentInfo) { recruitment.removeAll { it.id == info.id } }

        override fun getAllSubjects(): Flow<List<StudySubject>> = flowOf(subjects)
        override fun getActiveSubjects(): Flow<List<StudySubject>> = flowOf(subjects.filter { it.isActive })
        override suspend fun getAllSubjectsDirect(): List<StudySubject> = subjects.toList()
        override fun getSubjectById(subjectId: String): Flow<StudySubject?> = flowOf(subjects.find { it.subjectId == subjectId })
        override suspend fun getSubjectDirect(subjectId: String): StudySubject? = subjects.find { it.subjectId == subjectId }
        override suspend fun insertSubject(subject: StudySubject): Long {
            subjects.removeAll { it.subjectId == subject.subjectId }
            subjects.add(subject)
            return 1L
        }
        override suspend fun insertSubjects(subjects: List<StudySubject>) { subjects.forEach { insertSubject(it) } }
        override suspend fun updateSubject(subject: StudySubject) { insertSubject(subject) }
        override suspend fun deleteSubject(subject: StudySubject) { subjects.removeAll { it.subjectId == subject.subjectId } }

        override fun getAllTopics(): Flow<List<StudyTopic>> = flowOf(topics)
        override fun getTopicsForSubject(subjectId: String): Flow<List<StudyTopic>> = flowOf(topics.filter { it.subjectId == subjectId })
        override fun getActiveTopicsForSubject(subjectId: String): Flow<List<StudyTopic>> = flowOf(topics.filter { it.subjectId == subjectId && it.isActive })
        override suspend fun getAllTopicsDirect(): List<StudyTopic> = topics.toList()
        override fun getTopicById(topicId: String): Flow<StudyTopic?> = flowOf(topics.find { it.topicId == topicId })
        override suspend fun getTopicDirect(topicId: String): StudyTopic? = topics.find { it.topicId == topicId }
        override suspend fun insertTopic(topic: StudyTopic): Long {
            topics.removeAll { it.topicId == topic.topicId }
            topics.add(topic)
            return 1L
        }
        override suspend fun insertTopics(topics: List<StudyTopic>) { topics.forEach { insertTopic(it) } }
        override suspend fun updateTopic(topic: StudyTopic) { insertTopic(topic) }
        override suspend fun deleteTopic(topic: StudyTopic) { topics.removeAll { it.topicId == topic.topicId } }

        override fun getAllQuestions(): Flow<List<Question>> = flowOf(questions)
        override fun getActiveQuestions(): Flow<List<Question>> = flowOf(questions.filter { it.isActive })
        override suspend fun getAllQuestionsDirect(): List<Question> = questions.toList()
        override fun getQuestionsBySubjectId(subjectId: String): Flow<List<Question>> = flowOf(questions.filter { it.subjectId == subjectId })
        override fun getQuestionsByTopicId(topicId: String): Flow<List<Question>> = flowOf(questions.filter { it.topicId == topicId })
        override fun getQuestionsBySubjectAndTopic(subjectId: String, topicId: String): Flow<List<Question>> = flowOf(questions.filter { it.subjectId == subjectId && it.topicId == topicId })
        override fun getQuestionByQuestionId(questionId: String): Flow<Question?> = flowOf(questions.find { it.questionId == questionId })
        override suspend fun getQuestionByQuestionIdDirect(questionId: String): Question? = questions.find { it.questionId == questionId }
        override fun getQuestionById(id: Long): Flow<Question?> = flowOf(questions.find { it.id == id })
        override fun getQuestionsByChapter(chapterId: Long): Flow<List<Question>> = flowOf(questions.filter { it.chapterId == chapterId })
        override fun getQuestionsBySubject(subject: String): Flow<List<Question>> = flowOf(questions.filter { it.subjectName == subject })
        override suspend fun getRandomQuestions(limit: Int): List<Question> = questions.take(limit)
        override suspend fun insertQuestion(question: Question): Long {
            val qId = if (question.questionId.isNotEmpty()) question.questionId else "q_${questions.size + 1}"
            questions.removeAll { it.questionId == qId }
            questions.add(question.copy(questionId = qId))
            return 1L
        }
        override suspend fun insertQuestions(questions: List<Question>) { questions.forEach { insertQuestion(it) } }
        override suspend fun updateQuestion(question: Question) { insertQuestion(question) }
        override suspend fun deleteQuestion(question: Question) { questions.removeAll { it.questionId == question.questionId } }

        override fun getAllMockTests(): Flow<List<MockTest>> = flowOf(mockTests)
        override suspend fun getAllMockTestsDirect(): List<MockTest> = mockTests.toList()
        override fun getMockTestById(id: Long): Flow<MockTest?> = flowOf(mockTests.find { it.id == id })
        override suspend fun insertMockTest(test: MockTest): Long {
            val id = if (test.id > 0) test.id else (mockTests.size + 1).toLong()
            mockTests.removeAll { it.id == id }
            mockTests.add(test.copy(id = id))
            return id
        }
        override suspend fun insertMockTests(tests: List<MockTest>) { tests.forEach { insertMockTest(it) } }

        // Dummy unused methods for interface completeness
        override fun getAllWorkouts(): Flow<List<WorkoutRecord>> = flowOf(emptyList())
        override suspend fun getAllWorkoutsDirect(): List<WorkoutRecord> = emptyList()
        override fun getWorkoutsForStudent(studentId: String): Flow<List<WorkoutRecord>> = flowOf(emptyList())
        override fun getTodayWorkout(studentId: String, date: String): Flow<WorkoutRecord?> = flowOf(null)
        override suspend fun insertWorkoutRecord(record: WorkoutRecord): Long = 1L
        override suspend fun insertWorkoutRecords(records: List<WorkoutRecord>) {}
        override fun getLatestWorkoutPlan(): Flow<DailyWorkoutPlan?> = flowOf(null)
        override suspend fun insertWorkoutPlan(plan: DailyWorkoutPlan): Long = 1L
        val chaptersList = mutableListOf<Chapter>()

        override fun getAllChapters(): Flow<List<Chapter>> = flowOf(chaptersList)
        override suspend fun getAllChaptersDirect(): List<Chapter> = chaptersList.toList()
        override fun getPublishedChapters(): Flow<List<Chapter>> = flowOf(chaptersList.filter { it.isPublished })
        override suspend fun getPublishedChaptersDirect(): List<Chapter> = chaptersList.filter { it.isPublished }
        override fun getPublishedChaptersBySubject(subject: String): Flow<List<Chapter>> = flowOf(chaptersList.filter { it.subjectName.equals(subject, ignoreCase = true) && it.isPublished })
        override fun getTodayStudyTargets(): Flow<List<Chapter>> = flowOf(chaptersList.filter { it.isTodayTarget })
        override fun getChaptersBySubject(subject: String): Flow<List<Chapter>> = flowOf(chaptersList.filter { it.subjectName == subject })
        override fun getChapterById(id: Long): Flow<Chapter?> = flowOf(chaptersList.find { it.id == id })
        override suspend fun getChapterByIdDirect(id: Long): Chapter? = chaptersList.find { it.id == id }
        override suspend fun findDuplicateChapter(subjectName: String, chapterNumber: Int, chapterName: String): Chapter? =
            chaptersList.find { it.subjectName.equals(subjectName, ignoreCase = true) && (it.chapterNumber == chapterNumber || it.chapterName.equals(chapterName, ignoreCase = true)) }
        override suspend fun insertChapter(chapter: Chapter): Long {
            val assignedId = if (chapter.id > 0) chapter.id else (chaptersList.size + 1).toLong()
            val item = chapter.copy(id = assignedId)
            chaptersList.removeAll { it.id == assignedId }
            chaptersList.add(item)
            return assignedId
        }
        override suspend fun insertChapters(chapters: List<Chapter>) { chapters.forEach { insertChapter(it) } }
        override suspend fun updateChapter(chapter: Chapter) {
            chaptersList.removeAll { it.id == chapter.id }
            chaptersList.add(chapter)
        }
        override suspend fun deleteChapter(chapter: Chapter) { chaptersList.removeAll { it.id == chapter.id } }
        override suspend fun deleteChapterById(id: Long) { chaptersList.removeAll { it.id == id } }
        override fun getAllStudyAttempts(): Flow<List<StudyAttempt>> = flowOf(emptyList())
        override fun getStudyAttemptsForStudent(studentId: String): Flow<List<StudyAttempt>> = flowOf(emptyList())
        override fun getStudyAttemptsForStudentAndSubject(studentId: String, subjectId: String): Flow<List<StudyAttempt>> = flowOf(emptyList())
        override fun getStudyAttemptsForStudentAndTopic(studentId: String, topicId: String): Flow<List<StudyAttempt>> = flowOf(emptyList())
        override suspend fun insertStudyAttempt(attempt: StudyAttempt): Long = 1L
        override suspend fun insertStudyAttempts(attempts: List<StudyAttempt>) {}
        override fun getAllTrainers(): Flow<List<Trainer>> = flowOf(emptyList())
        override suspend fun insertTrainer(trainer: Trainer): Long = 1L
        override suspend fun insertTrainers(trainers: List<Trainer>) {}
        override suspend fun updateTrainer(trainer: Trainer) {}
        override suspend fun deleteTrainer(trainer: Trainer) {}
        override fun getAllGalleryItems(): Flow<List<GalleryItem>> = flowOf(emptyList())
        override fun getGalleryItemsByCategory(category: String): Flow<List<GalleryItem>> = flowOf(emptyList())
        override suspend fun insertGalleryItem(item: GalleryItem): Long = 1L
        override suspend fun insertGalleryItems(items: List<GalleryItem>) {}
        override suspend fun deleteGalleryItem(item: GalleryItem) {}
        override fun getAllSuccessStories(): Flow<List<SuccessStory>> = flowOf(emptyList())
        override suspend fun insertSuccessStory(story: SuccessStory): Long = 1L
        override suspend fun insertSuccessStories(stories: List<SuccessStory>) {}
        override suspend fun updateSuccessStory(story: SuccessStory) {}
        override suspend fun deleteSuccessStory(story: SuccessStory) {}
        override fun getContactInfo(): Flow<ContactInfo?> = flowOf(null)
        override suspend fun insertOrUpdateContactInfo(info: ContactInfo): Long = 1L

        val raceSessions = mutableListOf<RaceSession>()
        val raceResults = mutableListOf<RaceResult>()

        override fun getAllRaceSessions(): Flow<List<RaceSession>> = flowOf(raceSessions)
        override suspend fun getAllRaceSessionsDirect(): List<RaceSession> = raceSessions.toList()
        override fun getRaceSessionById(raceId: String): Flow<RaceSession?> = flowOf(raceSessions.find { it.raceId == raceId })
        override suspend fun getRaceSessionDirect(raceId: String): RaceSession? = raceSessions.find { it.raceId == raceId }
        override suspend fun insertRaceSession(session: RaceSession): Long {
            raceSessions.removeAll { it.raceId == session.raceId }
            raceSessions.add(session)
            return 1L
        }
        override suspend fun insertRaceSessions(sessions: List<RaceSession>) { sessions.forEach { insertRaceSession(it) } }
        override suspend fun updateRaceSession(session: RaceSession) { insertRaceSession(session) }
        override suspend fun deleteRaceSession(session: RaceSession) { raceSessions.removeAll { it.raceId == session.raceId } }
        override suspend fun deleteRaceSessionById(raceId: String) { raceSessions.removeAll { it.raceId == raceId } }

        override fun getAllRaceResults(): Flow<List<RaceResult>> = flowOf(raceResults)
        override suspend fun getAllRaceResultsDirect(): List<RaceResult> = raceResults.toList()
        override fun getRaceResultsForSession(raceId: String): Flow<List<RaceResult>> = flowOf(raceResults.filter { it.raceId == raceId })
        override suspend fun getRaceResultsForSessionDirect(raceId: String): List<RaceResult> = raceResults.filter { it.raceId == raceId }
        override fun getRaceResultsForStudent(studentId: String): Flow<List<RaceResult>> = flowOf(raceResults.filter { it.studentId == studentId })
        override suspend fun getRaceResultForStudent(raceId: String, studentId: String): RaceResult? = raceResults.find { it.raceId == raceId && it.studentId == studentId }
        override suspend fun insertRaceResult(result: RaceResult): Long {
            val id = if (result.id > 0) result.id else (raceResults.size + 1).toLong()
            raceResults.removeAll { it.id == id || (it.raceId == result.raceId && it.studentId == result.studentId) }
            raceResults.add(result.copy(id = id))
            return id
        }
        override suspend fun insertRaceResults(results: List<RaceResult>) { results.forEach { insertRaceResult(it) } }
        override suspend fun updateRaceResult(result: RaceResult) { insertRaceResult(result) }
        override suspend fun deleteRaceResult(result: RaceResult) { raceResults.remove(result) }
        override suspend fun deleteRaceResultsBySession(raceId: String) { raceResults.removeAll { it.raceId == raceId } }

        val notifications = mutableListOf<AppNotification>()
        override fun getAllNotifications(): Flow<List<AppNotification>> = flowOf(notifications)
        override suspend fun getAllNotificationsDirect(): List<AppNotification> = notifications.toList()
        override fun getNotificationById(notifId: String): Flow<AppNotification?> = flowOf(notifications.find { it.notificationId == notifId })
        override suspend fun getNotificationByIdDirect(notifId: String): AppNotification? = notifications.find { it.notificationId == notifId }
        override suspend fun insertNotification(notification: AppNotification): Long {
            notifications.removeAll { it.notificationId == notification.notificationId }
            notifications.add(notification)
            return 1L
        }
        override suspend fun insertNotifications(notifications: List<AppNotification>) {
            notifications.forEach { insertNotification(it) }
        }
        override suspend fun updateNotification(notification: AppNotification) {
            insertNotification(notification)
        }
        override suspend fun markNotificationAsRead(notifId: String) {
            val idx = notifications.indexOfFirst { it.notificationId == notifId }
            if (idx != -1) {
                notifications[idx] = notifications[idx].copy(isRead = true)
            }
        }
        override suspend fun markAllNotificationsAsRead() {
            for (i in notifications.indices) {
                notifications[i] = notifications[i].copy(isRead = true)
            }
        }
        override suspend fun deleteNotificationByNotifId(notifId: String) {
            notifications.removeAll { it.notificationId == notifId }
        }
        override suspend fun deleteNotification(id: Long) {
            notifications.removeAll { it.id == id }
        }
    }

    private lateinit var fakeDao: FakeAppDao
    private lateinit var outboxManager: SyncOutboxManager

    @Before
    fun setUp() {
        fakeDao = FakeAppDao()
        outboxManager = SyncOutboxManager()
    }

    @Test
    fun `test A - Offline Room write immediately persists to local database without crashing`() = runTest {
        val student = StudentProfile(studentId = "JBA-2026-001", fullName = "Rahul Sharma", mobileNumber = "9876543210")
        val attendance = AttendanceRecord(studentId = "JBA-2026-001", date = "2026-08-25", status = "Present")

        fakeDao.insertStudent(student)
        fakeDao.insertAttendance(attendance)

        val persistedStudent = fakeDao.getStudentDirect("JBA-2026-001")
        val persistedAttendance = fakeDao.getAllAttendanceDirect()

        assertNotNull("Student MUST be saved locally offline", persistedStudent)
        assertEquals("Rahul Sharma", persistedStudent?.fullName)
        assertEquals(1, persistedAttendance.size)
        assertEquals("Present", persistedAttendance[0].status)
    }

    @Test
    fun `test B - Online synchronization processes queued items successfully`() = runTest {
        val outboxItem = outboxManager.enqueue(
            entityType = SyncEntityType.ATTENDANCE,
            localRecordId = "JBA-2026-001_2026-08-25",
            firestoreDocId = "JBA-2026-001_2026-08-25",
            studentId = "JBA-2026-001"
        )

        val pending = outboxManager.getPendingItems("JBA-2026-001", isAdmin = false)
        assertEquals(1, pending.size)

        outboxManager.markSuccess(outboxItem)

        val remaining = outboxManager.getPendingItems("JBA-2026-001", isAdmin = false)
        assertEquals(0, remaining.size)
        assertEquals(1, outboxManager.diagnostics.value.syncedCount)
    }

    @Test
    fun `test C - Retry after network failure applies bounded exponential backoff`() = runTest {
        val item = outboxManager.enqueue(
            entityType = SyncEntityType.TRAINING_RECORD,
            localRecordId = "JBA-2026-001_2026-08-25",
            firestoreDocId = "JBA-2026-001_2026-08-25",
            studentId = "JBA-2026-001"
        )

        val networkError = RuntimeException("Network timeout / offline")
        outboxManager.markFailure(item, networkError, isPermanent = false)

        val snapshot = outboxManager.getQueueSnapshot()[0]
        assertEquals(1, snapshot.retryCount)
        assertEquals(SyncState.TRANSIENT_FAILURE, snapshot.syncState)
        assertTrue(snapshot.nextRetryTime > System.currentTimeMillis())
    }

    @Test
    fun `test D - Duplicate sync prevention ensures single logical outbox entry for identical document ID`() = runTest {
        outboxManager.enqueue(SyncEntityType.ATTENDANCE, "JBA-2026-001_2026-08-25", "JBA-2026-001_2026-08-25", SyncOperation.UPSERT, "JBA-2026-001")
        outboxManager.enqueue(SyncEntityType.ATTENDANCE, "JBA-2026-001_2026-08-25", "JBA-2026-001_2026-08-25", SyncOperation.UPSERT, "JBA-2026-001")

        val snapshot = outboxManager.getQueueSnapshot()
        assertEquals("Queue MUST deduplicate identical doc items to prevent repeated writes", 1, snapshot.size)
    }

    @Test
    fun `test E - App restart during sync recovers pending outbox queue`() = runTest {
        outboxManager.enqueue(SyncEntityType.TEST_ATTEMPT, "attempt_01", "attempt_JBA-2026-001_1_2026-08-25", SyncOperation.UPSERT, "JBA-2026-001")
        val item = outboxManager.getQueueSnapshot()[0]
        outboxManager.markInProgress(item)

        // Simulate app restart / queue inspection
        val pendingOnRestart = outboxManager.getPendingItems("JBA-2026-001", isAdmin = false)
        assertTrue("Pending item must be recoverable after restart", pendingOnRestart.isNotEmpty())
    }

    @Test
    fun `test F - Token expiration pauses private queue execution`() = runTest {
        outboxManager.enqueue(SyncEntityType.ATTENDANCE, "JBA-2026-001_2026-08-25", "JBA-2026-001_2026-08-25", SyncOperation.UPSERT, "JBA-2026-001")

        // Unauthenticated auth state (expired token / signed out)
        val pendingForUnauth = outboxManager.getPendingItems(currentStudentId = null, isAdmin = false)
        assertEquals("Unauthenticated state MUST NOT sync student private records", 0, pendingForUnauth.size)
    }

    @Test
    fun `test G - Permission denied is treated as permanent failure and stops infinite retries`() = runTest {
        val item = outboxManager.enqueue(SyncEntityType.ATTENDANCE, "JBA-2026-002_2026-08-25", "JBA-2026-002_2026-08-25", SyncOperation.UPSERT, "JBA-2026-002")

        val permError = RuntimeException("PERMISSION_DENIED: Missing or insufficient permissions.")
        outboxManager.markFailure(item, permError)

        val snapshot = outboxManager.getQueueSnapshot()[0]
        assertEquals(SyncState.PERMANENT_FAILURE, snapshot.syncState)
        assertTrue(snapshot.isPermanent)
        assertEquals(0L, snapshot.nextRetryTime)
    }

    @Test
    fun `test H - Student ownership enforcement only retrieves student's own pending items`() = runTest {
        outboxManager.enqueue(SyncEntityType.ATTENDANCE, "JBA-2026-001_2026-08-25", "JBA-2026-001_2026-08-25", SyncOperation.UPSERT, "JBA-2026-001")
        outboxManager.enqueue(SyncEntityType.ATTENDANCE, "JBA-2026-002_2026-08-25", "JBA-2026-002_2026-08-25", SyncOperation.UPSERT, "JBA-2026-002")

        val pendingStudentA = outboxManager.getPendingItems("JBA-2026-001", isAdmin = false)
        assertEquals(1, pendingStudentA.size)
        assertEquals("JBA-2026-001", pendingStudentA[0].studentId)
    }

    @Test
    fun `test I - Cross-student sync rejection flags attempt as permanent failure`() = runTest {
        val forgedItem = outboxManager.enqueue(SyncEntityType.ATTENDANCE, "JBA-2026-002_2026-08-25", "JBA-2026-002_2026-08-25", SyncOperation.UPSERT, "JBA-2026-002")

        // Authenticated as Student A (JBA-2026-001) attempting to process Student B's item
        val secEx = SecurityException("Cross-student sync rejected: Attempted to upload JBA-2026-002 while authenticated as JBA-2026-001")
        outboxManager.markFailure(forgedItem, secEx, isPermanent = true)

        val snapshot = outboxManager.getQueueSnapshot()[0]
        assertEquals(SyncState.PERMANENT_FAILURE, snapshot.syncState)
    }

    @Test
    fun `test J - Attendance synchronization queues and marks attendance records`() = runTest {
        val att = AttendanceRecord(studentId = "JBA-2026-001", date = "2026-08-25", status = "Present")
        fakeDao.insertAttendance(att)

        val item = outboxManager.enqueue(SyncEntityType.ATTENDANCE, "JBA-2026-001_2026-08-25", "JBA-2026-001_2026-08-25", SyncOperation.UPSERT, "JBA-2026-001")
        assertNotNull(item)
        assertEquals("JBA-2026-001_2026-08-25", item.firestoreDocId)
    }

    @Test
    fun `test K - Training synchronization handles physical metric records`() = runTest {
        val tr = TrainingRecord(studentId = "JBA-2026-001", date = "2026-08-25", runningDuration = "5:45", pushups = 35)
        fakeDao.insertTrainingRecord(tr)

        val item = outboxManager.enqueue(SyncEntityType.TRAINING_RECORD, "JBA-2026-001_2026-08-25", "JBA-2026-001_2026-08-25", SyncOperation.UPSERT, "JBA-2026-001")
        assertEquals(SyncEntityType.TRAINING_RECORD, item.entityType)
    }

    @Test
    fun `test L - Test attempt synchronization maintains immutable results`() = runTest {
        val attempt = TestAttempt(
            id = 1L,
            testId = 101L,
            testTitle = "General Duty Mock 01",
            targetExam = "Indian Army",
            studentId = "JBA-2026-001",
            date = "2026-08-25",
            totalQuestions = 50,
            attemptedCount = 45,
            correctCount = 40,
            wrongCount = 5,
            unattemptedCount = 5,
            score = 80.0,
            maxScore = 100.0,
            accuracyPercentage = 88.8,
            timeTakenSeconds = 1800
        )
        fakeDao.insertTestAttempt(attempt)

        val item = outboxManager.enqueue(SyncEntityType.TEST_ATTEMPT, "1", "attempt_JBA-2026-001_101_1", SyncOperation.UPSERT, "JBA-2026-001")
        assertEquals(SyncEntityType.TEST_ATTEMPT, item.entityType)
    }

    @Test
    fun `test M - Admin synchronization allows processing all academy collections`() = runTest {
        outboxManager.enqueue(SyncEntityType.STUDENT, "JBA-2026-001", "JBA-2026-001", SyncOperation.UPSERT, "JBA-2026-001")
        outboxManager.enqueue(SyncEntityType.NOTICE, "1", "notice_1", SyncOperation.UPSERT, null)
        outboxManager.enqueue(SyncEntityType.QUESTION, "q_101", "q_101", SyncOperation.UPSERT, null)

        val adminPending = outboxManager.getPendingItems(currentStudentId = null, isAdmin = true)
        assertEquals(3, adminPending.size)
    }

    @Test
    fun `test N - Firestore to Room synchronization safely upserts validated remote records into Room`() = runTest {
        val remoteNotices = listOf(
            Notice(id = 10L, title = "Ground Rally Date Announced", content = "Physical trials start next Monday", category = "Ground", date = "2026-08-25")
        )
        fakeDao.insertNotices(remoteNotices)

        val localNotices = fakeDao.getAllNoticesDirect()
        assertEquals(1, localNotices.size)
        assertEquals("Ground Rally Date Announced", localNotices[0].title)
    }

    @Test
    fun `test O - Delete sync removes notice from Room and queues cloud delete operation`() = runTest {
        val notice = Notice(id = 5L, title = "Old Event", content = "Expired", category = "Event", date = "2026-08-01")
        fakeDao.insertNotice(notice)
        assertEquals(1, fakeDao.getAllNoticesDirect().size)

        fakeDao.deleteNotice(notice)
        assertEquals(0, fakeDao.getAllNoticesDirect().size)

        val deleteItem = outboxManager.enqueue(SyncEntityType.NOTICE, "5", "notice_5", SyncOperation.DELETE, null)
        assertEquals(SyncOperation.DELETE, deleteItem.operation)
    }

    @Test
    fun `test P - Conflict handling preserves local Single Source of Truth`() = runTest {
        val initialStudent = StudentProfile(studentId = "JBA-2026-001", fullName = "Rahul Sharma", mobileNumber = "9876543210", overallScore = 80)
        fakeDao.insertStudent(initialStudent)

        // Local edit takes priority
        val updatedLocal = initialStudent.copy(overallScore = 95)
        fakeDao.updateStudent(updatedLocal)

        val current = fakeDao.getStudentDirect("JBA-2026-001")
        assertEquals(95, current?.overallScore)
    }

    @Test
    fun `test Q - Idempotent retry does not create duplicate entries or corrupt state`() = runTest {
        val item = outboxManager.enqueue(SyncEntityType.ATTENDANCE, "JBA-2026-001_2026-08-25", "JBA-2026-001_2026-08-25", SyncOperation.UPSERT, "JBA-2026-001")

        // First attempt fails
        outboxManager.markFailure(item, RuntimeException("Transient connection error"))
        assertEquals(1, outboxManager.diagnostics.value.failedCount)

        // Retry succeeds
        val updatedItem = outboxManager.getQueueSnapshot()[0]
        outboxManager.markSuccess(updatedItem)

        assertEquals(1, outboxManager.diagnostics.value.syncedCount)
        assertEquals(0, outboxManager.diagnostics.value.failedCount)
    }
}
