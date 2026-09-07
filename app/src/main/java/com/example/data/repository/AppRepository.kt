package com.example.data.repository

import com.example.data.cloud.CloudAuthManager
import com.example.data.cloud.CloudAuthState
import com.example.data.cloud.CloudSyncStatus
import com.example.data.cloud.FirestoreDataSyncManager
import com.example.data.cloud.SyncDiagnostics
import com.example.data.cloud.SyncEntityType
import com.example.data.cloud.SyncOperation
import com.example.data.db.AppDao
import com.example.data.db.AppDatabase
import com.example.data.db.DemoDataGenerator
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull

class AppRepository(
    private val dao: AppDao,
    val cloudAuthManager: CloudAuthManager = CloudAuthManager(),
    val cloudSyncManager: FirestoreDataSyncManager = FirestoreDataSyncManager(dao)
) {

    val cloudSyncStatus: StateFlow<CloudSyncStatus> get() = cloudSyncManager.syncStatus
    val cloudAuthState: StateFlow<CloudAuthState> get() = cloudAuthManager.authState
    val syncDiagnostics: StateFlow<SyncDiagnostics> get() = cloudSyncManager.outbox.diagnostics

    suspend fun syncWithCloud(): CloudSyncStatus = cloudSyncManager.performFullSync(cloudAuthManager.authState.value)

    val allStudents: Flow<List<StudentProfile>> = dao.getAllStudents()
    val allAttendance: Flow<List<AttendanceRecord>> = dao.getAllAttendance()
    val allChapters: Flow<List<Chapter>> = dao.getAllChapters()
    val publishedChapters: Flow<List<Chapter>> = dao.getPublishedChapters()
    val todayStudyTargets: Flow<List<Chapter>> = dao.getTodayStudyTargets()
    val allSubjects: Flow<List<StudySubject>> = dao.getAllSubjects()
    val activeSubjects: Flow<List<StudySubject>> = dao.getActiveSubjects()
    val allTopics: Flow<List<StudyTopic>> = dao.getAllTopics()
    val allQuestions: Flow<List<Question>> = dao.getAllQuestions()
    val activeQuestions: Flow<List<Question>> = dao.getActiveQuestions()
    val allStudyAttempts: Flow<List<StudyAttempt>> = dao.getAllStudyAttempts()
    val allMockTests: Flow<List<MockTest>> = dao.getAllMockTests()
    val allTestAttempts: Flow<List<TestAttempt>> = dao.getAllTestAttempts()
    val allNotices: Flow<List<Notice>> = dao.getAllNotices()
    val allRecruitmentInfo: Flow<List<RecruitmentInfo>> = dao.getAllRecruitmentInfo()
    val latestWorkoutPlan: Flow<DailyWorkoutPlan?> = dao.getLatestWorkoutPlan()
    val allTrainingRecords: Flow<List<TrainingRecord>> = dao.getAllTrainingRecords()
    val allWorkouts: Flow<List<WorkoutRecord>> = dao.getAllWorkouts()

    fun getTopicsForSubject(subjectId: String): Flow<List<StudyTopic>> =
        dao.getTopicsForSubject(subjectId)

    fun getActiveTopicsForSubject(subjectId: String): Flow<List<StudyTopic>> =
        dao.getActiveTopicsForSubject(subjectId)

    fun getQuestionsBySubjectId(subjectId: String): Flow<List<Question>> =
        dao.getQuestionsBySubjectId(subjectId)

    fun getQuestionsByTopicId(topicId: String): Flow<List<Question>> =
        dao.getQuestionsByTopicId(topicId)

    fun getQuestionsBySubjectAndTopic(subjectId: String, topicId: String): Flow<List<Question>> =
        dao.getQuestionsBySubjectAndTopic(subjectId, topicId)

    fun getStudyAttemptsForStudent(studentId: String): Flow<List<StudyAttempt>> =
        dao.getStudyAttemptsForStudent(studentId)

    fun getStudyAttemptsForStudentAndSubject(studentId: String, subjectId: String): Flow<List<StudyAttempt>> =
        dao.getStudyAttemptsForStudentAndSubject(studentId, subjectId)

    suspend fun insertSubject(subject: StudySubject) = dao.insertSubject(subject)
    suspend fun updateSubject(subject: StudySubject) = dao.updateSubject(subject)
    suspend fun deleteSubject(subject: StudySubject) = dao.deleteSubject(subject)

    suspend fun insertTopic(topic: StudyTopic) = dao.insertTopic(topic)
    suspend fun updateTopic(topic: StudyTopic) = dao.updateTopic(topic)
    suspend fun deleteTopic(topic: StudyTopic) = dao.deleteTopic(topic)

    suspend fun addQuestion(question: Question) = dao.insertQuestion(question)
    suspend fun updateQuestion(question: Question) = dao.updateQuestion(question)
    suspend fun deleteQuestion(question: Question) = dao.deleteQuestion(question)

    suspend fun recordStudyAttempt(attempt: StudyAttempt) = dao.insertStudyAttempt(attempt)

    fun getStudent(studentId: String): Flow<StudentProfile?> = dao.getStudentById(studentId)
    suspend fun getStudentByMobile(mobile: String): StudentProfile? = dao.getStudentByMobile(mobile)
    suspend fun isMobileRegistered(mobile: String): Boolean = dao.countStudentsWithMobile(mobile) > 0
    suspend fun getAllStudentsDirect(): List<StudentProfile> = dao.getAllStudentsDirect()
    suspend fun getAllStudentsIncludingDeletedDirect(): List<StudentProfile> = dao.getAllStudentsIncludingDeletedDirect()

    fun getAttendanceForStudent(studentId: String): Flow<List<AttendanceRecord>> =
        dao.getAttendanceForStudent(studentId)

    fun getTodayAttendance(studentId: String, date: String): Flow<AttendanceRecord?> =
        dao.getTodayAttendance(studentId, date)

    fun getAttendanceByDate(date: String): Flow<List<AttendanceRecord>> =
        dao.getAttendanceByDate(date)

    fun getTrainingRecordsForStudent(studentId: String): Flow<List<TrainingRecord>> =
        dao.getTrainingRecordsForStudent(studentId)

    fun getTodayTraining(studentId: String, date: String): Flow<TrainingRecord?> =
        dao.getTodayTrainingRecord(studentId, date)

    fun getWorkoutsForStudent(studentId: String): Flow<List<WorkoutRecord>> =
        dao.getWorkoutsForStudent(studentId)

    fun getTodayWorkout(studentId: String, date: String): Flow<WorkoutRecord?> =
        dao.getTodayWorkout(studentId, date)

    fun getChaptersBySubject(subject: String): Flow<List<Chapter>> =
        dao.getChaptersBySubject(subject)

    fun getChapterById(id: Long): Flow<Chapter?> = dao.getChapterById(id)

    fun getQuestionsByChapter(chapterId: Long): Flow<List<Question>> =
        dao.getQuestionsByChapter(chapterId)

    fun getQuestionsBySubject(subject: String): Flow<List<Question>> =
        dao.getQuestionsBySubject(subject)

    suspend fun getRandomQuestions(limit: Int): List<Question> =
        dao.getRandomQuestions(limit)

    fun getMockTestById(id: Long): Flow<MockTest?> = dao.getMockTestById(id)

    fun getAttemptsForStudent(studentId: String): Flow<List<TestAttempt>> =
        dao.getAttemptsForStudent(studentId)

    suspend fun insertStudent(student: StudentProfile) = dao.insertStudent(student)
    suspend fun updateStudent(student: StudentProfile) = dao.updateStudent(student)
    suspend fun softDeleteStudent(studentId: String) {
        dao.softDeleteStudent(studentId)
        cloudSyncManager.queueSync(
            entityType = SyncEntityType.STUDENT,
            localRecordId = studentId,
            firestoreDocId = studentId,
            operation = SyncOperation.DELETE,
            studentId = studentId
        )
    }
    suspend fun deleteStudent(student: StudentProfile) {
        dao.deleteStudent(student)
        cloudSyncManager.queueSync(
            entityType = SyncEntityType.STUDENT,
            localRecordId = student.studentId,
            firestoreDocId = student.studentId,
            operation = SyncOperation.DELETE,
            studentId = student.studentId
        )
    }

    suspend fun markAttendance(record: AttendanceRecord) = dao.insertAttendance(record)
    suspend fun markBatchAttendance(records: List<AttendanceRecord>) = dao.insertAttendanceList(records)

    suspend fun recordWorkout(record: WorkoutRecord) = dao.insertWorkoutRecord(record)
    suspend fun recordTraining(record: TrainingRecord) = dao.insertTrainingRecord(record)
    suspend fun updateTraining(record: TrainingRecord) = dao.updateTrainingRecord(record)
    suspend fun deleteTraining(record: TrainingRecord) = dao.deleteTrainingRecord(record)
    suspend fun setWorkoutPlan(plan: DailyWorkoutPlan) = dao.insertWorkoutPlan(plan)

    fun getPublishedChaptersBySubject(subject: String): Flow<List<Chapter>> =
        dao.getPublishedChaptersBySubject(subject)

    suspend fun addChapter(chapter: Chapter): Result<Long> {
        val validation = ChapterValidator.validate(
            subjectName = chapter.subjectName,
            chapterNumber = chapter.chapterNumber,
            chapterName = chapter.chapterName
        )
        if (!validation.isValid) {
            return Result.failure(IllegalArgumentException(validation.errorMessage))
        }

        val duplicate = dao.findDuplicateChapter(
            subjectName = chapter.subjectName,
            chapterNumber = chapter.chapterNumber,
            chapterName = chapter.chapterName
        )
        if (duplicate != null && duplicate.id != chapter.id) {
            return Result.failure(IllegalStateException("यह Chapter पहले से मौजूद है (Subject: ${chapter.subjectName}, Chapter: ${chapter.chapterNumber}. ${chapter.chapterName})"))
        }

        val id = dao.insertChapter(chapter)
        return Result.success(id)
    }

    suspend fun updateChapter(chapter: Chapter): Result<Unit> {
        val validation = ChapterValidator.validate(
            subjectName = chapter.subjectName,
            chapterNumber = chapter.chapterNumber,
            chapterName = chapter.chapterName
        )
        if (!validation.isValid) {
            return Result.failure(IllegalArgumentException(validation.errorMessage))
        }

        val duplicate = dao.findDuplicateChapter(
            subjectName = chapter.subjectName,
            chapterNumber = chapter.chapterNumber,
            chapterName = chapter.chapterName
        )
        if (duplicate != null && duplicate.id != chapter.id) {
            return Result.failure(IllegalStateException("यह Chapter पहले से मौजूद है (Subject: ${chapter.subjectName}, Chapter: ${chapter.chapterNumber}. ${chapter.chapterName})"))
        }

        dao.updateChapter(chapter)
        return Result.success(Unit)
    }

    suspend fun deleteChapter(chapter: Chapter) {
        dao.deleteChapter(chapter)
    }

    suspend fun deleteChapterById(id: Long) {
        dao.deleteChapterById(id)
    }

    suspend fun toggleChapterPublish(chapter: Chapter) {
        val updated = chapter.copy(
            isPublished = !chapter.isPublished,
            updatedAt = "2026-08-30"
        )
        dao.updateChapter(updated)
    }

    suspend fun insertChapter(chapter: Chapter) = dao.insertChapter(chapter)

    suspend fun toggleChapterCompletion(chapter: Chapter) {
        val newStatus = !chapter.isCompleted
        val newProgress = if (newStatus) 100 else 0
        dao.updateChapter(chapter.copy(isCompleted = newStatus, progressPercentage = newProgress))
    }

    suspend fun addMockTest(mockTest: MockTest) = dao.insertMockTest(mockTest)
    suspend fun recordTestAttempt(attempt: TestAttempt) = dao.insertTestAttempt(attempt)

    suspend fun addNotice(notice: Notice) = dao.insertNotice(notice)
    suspend fun updateNotice(notice: Notice) = dao.updateNotice(notice)
    suspend fun deleteNotice(notice: Notice) = dao.deleteNotice(notice)
    suspend fun togglePinNotice(notice: Notice) {
        dao.updateNotice(notice.copy(isPinned = !notice.isPinned))
    }
    suspend fun markNoticeRead(notice: Notice) {
        if (!notice.isRead) {
            dao.updateNotice(notice.copy(isRead = true))
        }
    }

    suspend fun addRecruitmentInfo(info: RecruitmentInfo) = dao.insertRecruitmentInfo(info)
    suspend fun updateRecruitmentInfo(info: RecruitmentInfo) = dao.updateRecruitmentInfo(info)
    suspend fun deleteRecruitmentInfo(info: RecruitmentInfo) = dao.deleteRecruitmentInfo(info)

    // --- Trainers (Content) ---
    val allTrainers: Flow<List<Trainer>> = dao.getAllTrainers()
    suspend fun addTrainer(trainer: Trainer) = dao.insertTrainer(trainer)
    suspend fun updateTrainer(trainer: Trainer) = dao.updateTrainer(trainer)
    suspend fun deleteTrainer(trainer: Trainer) = dao.deleteTrainer(trainer)
    suspend fun getTrainerByCoachId(coachId: String): Trainer? = dao.getTrainerByCoachId(coachId)
    fun getTrainerByCoachIdFlow(coachId: String): Flow<Trainer?> = dao.getTrainerByCoachIdFlow(coachId)
    suspend fun seedInitialCoachesIfMissing() = com.example.util.CoachAuthManager.seedInitialCoaches(dao)

    // --- Gallery ---
    val allGalleryItems: Flow<List<GalleryItem>> = dao.getAllGalleryItems()
    fun getGalleryItemsByCategory(category: String): Flow<List<GalleryItem>> = dao.getGalleryItemsByCategory(category)
    suspend fun addGalleryItem(item: GalleryItem) = dao.insertGalleryItem(item)
    suspend fun deleteGalleryItem(item: GalleryItem) = dao.deleteGalleryItem(item)

    // --- Success Stories ---
    val allSuccessStories: Flow<List<SuccessStory>> = dao.getAllSuccessStories()
    suspend fun addSuccessStory(story: SuccessStory) = dao.insertSuccessStory(story)
    suspend fun updateSuccessStory(story: SuccessStory) = dao.updateSuccessStory(story)
    suspend fun deleteSuccessStory(story: SuccessStory) = dao.deleteSuccessStory(story)

    // --- Cloud Sync Delegation Helpers ---
    suspend fun uploadStudentToCloud(student: StudentProfile) = cloudSyncManager.uploadStudentToCloud(student)
    suspend fun uploadAttendanceToCloud(record: AttendanceRecord) = cloudSyncManager.uploadAttendanceToCloud(record)
    suspend fun uploadTrainingRecordToCloud(record: TrainingRecord) = cloudSyncManager.uploadTrainingRecordToCloud(record)
    suspend fun uploadTestAttemptToCloud(attempt: TestAttempt) = cloudSyncManager.uploadTestAttemptToCloud(attempt)
    suspend fun uploadMockTestToCloud(test: MockTest) = cloudSyncManager.uploadMockTestToCloud(test)
    suspend fun uploadNoticeToCloud(notice: Notice) = cloudSyncManager.uploadNoticeToCloud(notice)
    suspend fun uploadRecruitmentToCloud(info: RecruitmentInfo) = cloudSyncManager.uploadRecruitmentToCloud(info)
    suspend fun uploadSubjectToCloud(subject: StudySubject) = cloudSyncManager.uploadSubjectToCloud(subject)
    suspend fun uploadTopicToCloud(topic: StudyTopic) = cloudSyncManager.uploadTopicToCloud(topic)
    suspend fun uploadQuestionToCloud(question: Question) = cloudSyncManager.uploadQuestionToCloud(question)

    // --- Contact Info ---
    val contactInfo: Flow<ContactInfo?> = dao.getContactInfo()
    suspend fun updateContactInfo(info: ContactInfo) = dao.insertOrUpdateContactInfo(info)

    // --- Phase 2B: Live 1600m Batch Stopwatch & Race Sessions ---
    val allRaceSessions: Flow<List<RaceSession>> = dao.getAllRaceSessions()
    val allRaceResults: Flow<List<RaceResult>> = dao.getAllRaceResults()

    fun getRaceSessionById(raceId: String): Flow<RaceSession?> = dao.getRaceSessionById(raceId)
    suspend fun getRaceSessionDirect(raceId: String): RaceSession? = dao.getRaceSessionDirect(raceId)
    fun getRaceResultsForSession(raceId: String): Flow<List<RaceResult>> = dao.getRaceResultsForSession(raceId)
    suspend fun getRaceResultsForSessionDirect(raceId: String): List<RaceResult> = dao.getRaceResultsForSessionDirect(raceId)
    fun getRaceResultsForStudent(studentId: String): Flow<List<RaceResult>> = dao.getRaceResultsForStudent(studentId)

    suspend fun saveRaceSession(session: RaceSession) = dao.insertRaceSession(session)
    suspend fun updateRaceSession(session: RaceSession) = dao.updateRaceSession(session)
    suspend fun deleteRaceSession(session: RaceSession) {
        dao.deleteRaceResultsBySession(session.raceId)
        dao.deleteRaceSession(session)
    }

    suspend fun saveRaceResult(result: RaceResult) = dao.insertRaceResult(result)
    suspend fun saveRaceResults(results: List<RaceResult>) = dao.insertRaceResults(results)
    suspend fun updateRaceResult(result: RaceResult) = dao.updateRaceResult(result)
    suspend fun deleteRaceResult(result: RaceResult) = dao.deleteRaceResult(result)

    suspend fun uploadRaceSessionToCloud(session: RaceSession) = cloudSyncManager.uploadRaceSessionToCloud(session)
    suspend fun uploadRaceResultToCloud(result: RaceResult) = cloudSyncManager.uploadRaceResultToCloud(result)

    // --- Phase 5B: Communication & Notifications ---
    val allNotifications: Flow<List<AppNotification>> = dao.getAllNotifications()
    fun getNotificationById(notifId: String): Flow<AppNotification?> = dao.getNotificationById(notifId)
    suspend fun getNotificationByIdDirect(notifId: String): AppNotification? = dao.getNotificationByIdDirect(notifId)
    suspend fun insertNotification(notification: AppNotification) = dao.insertNotification(notification)
    suspend fun updateNotification(notification: AppNotification) = dao.updateNotification(notification)
    suspend fun markNotificationAsRead(notifId: String) = dao.markNotificationAsRead(notifId)
    suspend fun markAllNotificationsAsRead() = dao.markAllNotificationsAsRead()
    suspend fun deleteNotification(notifId: String) = dao.deleteNotificationByNotifId(notifId)
    suspend fun uploadNotificationToCloud(notification: AppNotification) = cloudSyncManager.uploadNotificationToCloud(notification)
    suspend fun deleteNotificationFromCloud(notifId: String) = cloudSyncManager.deleteNotificationFromCloud(notifId)

    suspend fun ensureDataSeeded() {
        seedInitialCoachesIfMissing()
        val existingStudents = dao.getAllStudentsDirect()
        if (existingStudents.isEmpty()) {
            AppDatabase.populateInitialData(dao)
        } else {
            // Ensure foundational initial students (Ujala & Janeshwari) exist if missing,
            // but NEVER delete or purge any newly registered students!
            val initialStudents = DemoDataGenerator.getSampleStudents()
            val existingIds = existingStudents.map { it.studentId }.toSet()
            val existingMobiles = existingStudents.map { it.mobileNumber }.toSet()
            for (defaultStudent in initialStudents) {
                if (defaultStudent.studentId !in existingIds && defaultStudent.mobileNumber !in existingMobiles) {
                    dao.insertStudent(defaultStudent)
                }
            }

            val existingChapters = dao.getAllChaptersDirect()
            if (existingChapters.isEmpty() || existingChapters.size < 63) {
                dao.insertChapters(com.example.data.db.SyllabusStudyMaterialData.getAllSyllabusChapters())
                dao.insertSubjects(com.example.data.db.SyllabusStudyMaterialData.getSyllabusSubjects())
            }
            val existingNotifs = dao.getAllNotificationsDirect()
            if (existingNotifs.isEmpty()) {
                dao.insertNotifications(DemoDataGenerator.getSampleNotifications())
            }
        }
    }
}
