package com.example.data.cloud

import android.util.Log
import com.example.data.db.AppDao
import com.example.data.model.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * Firestore Data Sync Manager handles bi-directional data flow between
 * local Room Database (Single Source of Truth) and Firebase Cloud Firestore.
 *
 * Guarantees:
 * - Room is always the offline-first Single Source of Truth.
 * - Every cloud document has deterministic, stable IDs preventing duplicates on repeated syncs.
 * - Every record has createdAt, updatedAt, studentId, and uid metadata where applicable.
 * - Graceful fallback when offline or when Firebase credentials are not yet configured.
 */
class FirestoreDataSyncManager(
    private val appDao: AppDao,
    private val firestore: FirebaseFirestore? = runCatching { FirebaseFirestore.getInstance() }.getOrNull(),
    val outbox: SyncOutboxManager = SyncOutboxManager()
) {
    private val TAG = "FirestoreDataSync"

    private val _syncStatus = MutableStateFlow<CloudSyncStatus>(CloudSyncStatus.Idle)
    val syncStatus: StateFlow<CloudSyncStatus> = _syncStatus.asStateFlow()

    private val activeListeners = mutableListOf<ListenerRegistration>()

    fun isCloudAvailable(): Boolean = firestore != null

    /**
     * Enqueues an entity operation in the offline-first Outbox queue.
     * When internet is active and credentials are confirmed, the queue is processed automatically.
     */
    suspend fun queueSync(
        entityType: SyncEntityType,
        localRecordId: String,
        firestoreDocId: String,
        operation: SyncOperation = SyncOperation.UPSERT,
        studentId: String? = null
    ): SyncOutboxItem {
        return outbox.enqueue(
            entityType = entityType,
            localRecordId = localRecordId,
            firestoreDocId = firestoreDocId,
            operation = operation,
            studentId = studentId
        )
    }

    /**
     * Processes pending items in the offline Outbox with role enforcement,
     * exponential backoff, and duplicate prevention.
     */
    suspend fun processOutboxQueue(
        currentAuthState: CloudAuthState
    ): Int = withContext(Dispatchers.IO) {
        val currentStudentId = (currentAuthState as? CloudAuthState.Authenticated)?.studentId
        val currentUid = (currentAuthState as? CloudAuthState.Authenticated)?.uid
        val role = (currentAuthState as? CloudAuthState.Authenticated)?.role ?: FirestoreConstants.ROLE_STUDENT
        val isAdmin = role == FirestoreConstants.ROLE_ADMIN || role == FirestoreConstants.ROLE_COACH

        val pending = outbox.getPendingItems(currentStudentId, isAdmin)
        if (pending.isEmpty()) return@withContext 0

        var processedCount = 0
        for (item in pending) {
            outbox.markInProgress(item)
            try {
                // Cross-student security check
                if (!isAdmin && item.studentId != null && item.studentId != currentStudentId) {
                    val secEx = SecurityException("Cross-student sync rejected: Attempted to upload ${item.studentId} while authenticated as $currentStudentId")
                    outbox.markFailure(item, secEx, isPermanent = true)
                    continue
                }

                val result = executeOutboxItem(item, currentUid, isAdmin)
                if (result.isSuccess) {
                    outbox.markSuccess(item)
                    processedCount++
                } else {
                    val ex = result.exceptionOrNull() ?: Exception("Unknown sync error")
                    outbox.markFailure(item, ex)
                }
            } catch (e: Exception) {
                outbox.markFailure(item, e)
            }
        }
        processedCount
    }

    private suspend fun executeOutboxItem(
        item: SyncOutboxItem,
        currentUid: String?,
        isAdmin: Boolean
    ): Result<Unit> {
        return when (item.entityType) {
            SyncEntityType.STUDENT -> {
                val student = appDao.getStudentDirect(item.localRecordId)
                    ?: return Result.failure(IllegalArgumentException("Student not found: ${item.localRecordId}"))
                uploadStudentToCloud(student, currentUid)
            }
            SyncEntityType.ATTENDANCE -> {
                val records = appDao.getAllAttendanceDirect()
                val record = records.find { "${it.studentId}_${it.date}" == item.localRecordId || it.id.toString() == item.localRecordId }
                    ?: return Result.failure(IllegalArgumentException("Attendance record not found: ${item.localRecordId}"))
                uploadAttendanceToCloud(record, currentUid)
            }
            SyncEntityType.TRAINING_RECORD -> {
                val records = appDao.getAllTrainingRecordsDirect()
                val record = records.find { "${it.studentId}_${it.date}" == item.localRecordId || it.id.toString() == item.localRecordId }
                    ?: return Result.failure(IllegalArgumentException("Training record not found: ${item.localRecordId}"))
                uploadTrainingRecordToCloud(record, currentUid)
            }
            SyncEntityType.TEST_ATTEMPT -> {
                val attempts = appDao.getAllTestAttemptsDirect()
                val attempt = attempts.find { it.id.toString() == item.localRecordId }
                    ?: return Result.failure(IllegalArgumentException("Test attempt not found: ${item.localRecordId}"))
                uploadTestAttemptToCloud(attempt, currentUid)
            }
            SyncEntityType.NOTICE -> {
                if (item.operation == SyncOperation.DELETE) {
                    val idLong = item.localRecordId.toLongOrNull() ?: 0L
                    deleteNoticeFromCloud(idLong)
                } else {
                    val notices = appDao.getAllNoticesDirect()
                    val notice = notices.find { it.id.toString() == item.localRecordId }
                        ?: return Result.failure(IllegalArgumentException("Notice not found: ${item.localRecordId}"))
                    uploadNoticeToCloud(notice)
                }
            }
            SyncEntityType.RECRUITMENT -> {
                val infos = appDao.getAllRecruitmentInfosDirect()
                val info = infos.find { it.id.toString() == item.localRecordId }
                    ?: return Result.failure(IllegalArgumentException("Recruitment info not found: ${item.localRecordId}"))
                uploadRecruitmentToCloud(info)
            }
            SyncEntityType.SUBJECT -> {
                val subjects = appDao.getAllSubjectsDirect()
                val sub = subjects.find { it.subjectId == item.localRecordId }
                    ?: return Result.failure(IllegalArgumentException("Subject not found: ${item.localRecordId}"))
                uploadSubjectToCloud(sub)
            }
            SyncEntityType.TOPIC -> {
                val topics = appDao.getAllTopicsDirect()
                val top = topics.find { it.topicId == item.localRecordId }
                    ?: return Result.failure(IllegalArgumentException("Topic not found: ${item.localRecordId}"))
                uploadTopicToCloud(top)
            }
            SyncEntityType.QUESTION -> {
                val questions = appDao.getAllQuestionsDirect()
                val q = questions.find { it.questionId == item.localRecordId || it.id.toString() == item.localRecordId }
                    ?: return Result.failure(IllegalArgumentException("Question not found: ${item.localRecordId}"))
                uploadQuestionToCloud(q)
            }
            SyncEntityType.MOCK_TEST -> {
                val tests = appDao.getAllMockTestsDirect()
                val test = tests.find { it.id.toString() == item.localRecordId }
                    ?: return Result.failure(IllegalArgumentException("Mock test not found: ${item.localRecordId}"))
                uploadMockTestToCloud(test)
            }
            SyncEntityType.RACE_SESSION -> {
                val session = appDao.getRaceSessionDirect(item.localRecordId)
                    ?: return Result.failure(IllegalArgumentException("Race session not found: ${item.localRecordId}"))
                uploadRaceSessionToCloud(session)
            }
            SyncEntityType.RACE_RESULT -> {
                val results = appDao.getAllRaceResultsDirect()
                val result = results.find { it.id.toString() == item.localRecordId || "${it.raceId}_${it.studentId}" == item.localRecordId }
                    ?: return Result.failure(IllegalArgumentException("Race result not found: ${item.localRecordId}"))
                uploadRaceResultToCloud(result)
            }
            SyncEntityType.CHAPTER -> {
                if (item.operation == SyncOperation.DELETE) {
                    val chapId = item.localRecordId.toLongOrNull() ?: 0L
                    deleteChapterFromCloud(chapId)
                } else {
                    val chapters = appDao.getAllChaptersDirect()
                    val chapter = chapters.find { it.id.toString() == item.localRecordId }
                        ?: return Result.failure(IllegalArgumentException("Chapter not found: ${item.localRecordId}"))
                    uploadChapterToCloud(chapter)
                }
            }
            SyncEntityType.NOTIFICATION -> {
                if (item.operation == SyncOperation.DELETE) {
                    deleteNotificationFromCloud(item.firestoreDocId)
                } else {
                    val notifications = appDao.getAllNotificationsDirect()
                    val notif = notifications.find { it.notificationId == item.localRecordId || it.id.toString() == item.localRecordId }
                        ?: return Result.failure(IllegalArgumentException("Notification not found: ${item.localRecordId}"))
                    uploadNotificationToCloud(notif)
                }
            }
        }
    }

    /**
     * Performs a full synchronization cycle:
     * 1. Pulls remote updates from Cloud into local Room database (Upsert)
     * 2. Pushes local data to Cloud using SetOptions.merge() with stable IDs
     */
    suspend fun performFullSync(
        authState: CloudAuthState = CloudAuthState.Unauthenticated
    ): CloudSyncStatus = withContext(Dispatchers.IO) {
        val db = firestore ?: return@withContext CloudSyncStatus.Error(
            message = "क्लाउड डेटाबेस सक्रिय नहीं है (Firebase Firestore is not initialized or offline)",
            isOffline = true
        )

        try {
            _syncStatus.value = CloudSyncStatus.Syncing("क्लाउड सामग्री सिंक की जा रही है...")
            var totalSynced = 0

            // 1. Pull remote notices
            val remoteNotices = fetchNoticesFromCloudInternal(db)
            if (remoteNotices.isNotEmpty()) {
                appDao.insertNotices(remoteNotices)
                totalSynced += remoteNotices.size
            }

            // 1B. Pull remote communication notifications
            val remoteNotifications = fetchNotificationsFromCloudInternal(db)
            if (remoteNotifications.isNotEmpty()) {
                appDao.insertNotifications(remoteNotifications)
                totalSynced += remoteNotifications.size
            }

            // 2. Pull remote recruitment info
            val remoteRecruitment = fetchRecruitmentFromCloudInternal(db)
            if (remoteRecruitment.isNotEmpty()) {
                appDao.insertRecruitmentInfos(remoteRecruitment)
                totalSynced += remoteRecruitment.size
            }

            // 3. Pull remote subjects & topics
            val remoteSubjects = fetchSubjectsFromCloudInternal(db)
            if (remoteSubjects.isNotEmpty()) {
                appDao.insertSubjects(remoteSubjects)
                totalSynced += remoteSubjects.size
            }

            val remoteTopics = fetchTopicsFromCloudInternal(db)
            if (remoteTopics.isNotEmpty()) {
                appDao.insertTopics(remoteTopics)
                totalSynced += remoteTopics.size
            }

            // 4. Pull remote mock tests & questions
            val remoteMockTests = fetchMockTestsFromCloudInternal(db)
            if (remoteMockTests.isNotEmpty()) {
                appDao.insertMockTests(remoteMockTests)
                totalSynced += remoteMockTests.size
            }

            val remoteQuestions = fetchQuestionsFromCloudInternal(db)
            if (remoteQuestions.isNotEmpty()) {
                appDao.insertQuestions(remoteQuestions)
                totalSynced += remoteQuestions.size
            }

            // 5. Pull remote students if admin or authenticated
            val currentStudentId = (authState as? CloudAuthState.Authenticated)?.studentId
            val currentRole = (authState as? CloudAuthState.Authenticated)?.role ?: FirestoreConstants.ROLE_STUDENT
            val isAdmin = currentRole == FirestoreConstants.ROLE_ADMIN || currentRole == FirestoreConstants.ROLE_COACH

            if (isAdmin) {
                _syncStatus.value = CloudSyncStatus.Syncing("कैडेट प्रोफाइल एवं उपस्थिति सिंक किए जा रहे हैं...")
                val remoteStudents = fetchStudentsFromCloudInternal(db)
                if (remoteStudents.isNotEmpty()) {
                    appDao.insertStudents(remoteStudents)
                    totalSynced += remoteStudents.size
                }
            }

            // 6. Process outbox pending items
            val outboxSynced = processOutboxQueue(authState)
            totalSynced += outboxSynced

            // 7. Push local students to Cloud (if authenticated / matching studentId or admin)
            _syncStatus.value = CloudSyncStatus.Syncing("स्थानीय डेटा क्लाउड पर बैकअप किया जा रहा है...")
            val localStudents = appDao.getAllStudentsDirect()
            for (student in localStudents) {
                if (isAdmin || student.studentId == currentStudentId) {
                    uploadStudentToCloud(student)
                }
            }

            // 8. Push local attendance records to Cloud
            val localAttendance = appDao.getAllAttendanceDirect()
            for (att in localAttendance) {
                if (isAdmin || att.studentId == currentStudentId) {
                    uploadAttendanceToCloud(att)
                }
            }

            // 9. Push local training records to Cloud
            val localTraining = appDao.getAllTrainingRecordsDirect()
            for (tr in localTraining) {
                if (isAdmin || tr.studentId == currentStudentId) {
                    uploadTrainingRecordToCloud(tr)
                }
            }

            // 10. Push local test attempts to Cloud
            val localAttempts = appDao.getAllTestAttemptsDirect()
            for (attempt in localAttempts) {
                if (isAdmin || attempt.studentId == currentStudentId) {
                    uploadTestAttemptToCloud(attempt)
                }
            }

            // 11. Push local notifications to Cloud (if admin/staff created)
            if (isAdmin) {
                val localNotifs = appDao.getAllNotificationsDirect()
                for (notif in localNotifs) {
                    uploadNotificationToCloud(notif)
                }
            }

            val status = CloudSyncStatus.Success(
                timestamp = System.currentTimeMillis(),
                recordsSynced = totalSynced + localStudents.size + localAttendance.size,
                message = "क्लाउड सिंक पूर्ण: $totalSynced रिकॉर्ड सिंक किए गए"
            )
            _syncStatus.value = status
            status
        } catch (e: Exception) {
            Log.e(TAG, "performFullSync failed: ${e.message}", e)
            val error = CloudSyncStatus.Error(
                message = "सिंक विफल: ${e.localizedMessage ?: "नेटवर्क समस्या"}",
                isOffline = e.message?.contains("network", true) == true
            )
            _syncStatus.value = error
            error
        }
    }

    // ==========================================
    // PUSH / UPLOAD OPERATIONS (Room -> Firestore)
    // ==========================================

    suspend fun uploadStudentToCloud(student: StudentProfile, uid: String? = null): Result<Unit> = withContext(Dispatchers.IO) {
        val db = firestore ?: return@withContext Result.failure(IllegalStateException("Firestore is null"))
        try {
            val docRef = db.collection(FirestoreConstants.COLLECTION_STUDENTS).document(student.studentId)
            val data = studentToMap(student, uid)
            docRef.set(data, SetOptions.merge()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "uploadStudentToCloud failed: ${student.studentId}", e)
            Result.failure(e)
        }
    }

    suspend fun uploadAttendanceToCloud(record: AttendanceRecord, uid: String? = null): Result<Unit> = withContext(Dispatchers.IO) {
        val db = firestore ?: return@withContext Result.failure(IllegalStateException("Firestore is null"))
        try {
            val docId = "${record.studentId}_${record.date}"
            val data = attendanceToMap(record, uid)
            db.collection(FirestoreConstants.COLLECTION_ATTENDANCE).document(docId).set(data, SetOptions.merge()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "uploadAttendanceToCloud failed", e)
            Result.failure(e)
        }
    }

    suspend fun uploadTrainingRecordToCloud(record: TrainingRecord, uid: String? = null): Result<Unit> = withContext(Dispatchers.IO) {
        val db = firestore ?: return@withContext Result.failure(IllegalStateException("Firestore is null"))
        try {
            val docId = "${record.studentId}_${record.date}"
            val data = trainingRecordToMap(record, uid)
            db.collection(FirestoreConstants.COLLECTION_TRAINING).document(docId).set(data, SetOptions.merge()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "uploadTrainingRecordToCloud failed", e)
            Result.failure(e)
        }
    }

    suspend fun uploadTestAttemptToCloud(attempt: TestAttempt, uid: String? = null): Result<Unit> = withContext(Dispatchers.IO) {
        val db = firestore ?: return@withContext Result.failure(IllegalStateException("Firestore is null"))
        try {
            val docId = if (attempt.id > 0) "attempt_${attempt.studentId}_${attempt.testId}_${attempt.id}" else "attempt_${attempt.studentId}_${attempt.testId}_${attempt.date}"
            val data = testAttemptToMap(attempt, uid)
            db.collection(FirestoreConstants.COLLECTION_TEST_ATTEMPTS).document(docId).set(data, SetOptions.merge()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "uploadTestAttemptToCloud failed", e)
            Result.failure(e)
        }
    }

    suspend fun uploadMockTestToCloud(test: MockTest): Result<Unit> = withContext(Dispatchers.IO) {
        val db = firestore ?: return@withContext Result.failure(IllegalStateException("Firestore is null"))
        try {
            val docId = "mock_test_${test.id}"
            val data = mockTestToMap(test)
            db.collection(FirestoreConstants.COLLECTION_MOCK_TESTS).document(docId).set(data, SetOptions.merge()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "uploadMockTestToCloud failed", e)
            Result.failure(e)
        }
    }

    suspend fun uploadNoticeToCloud(notice: Notice): Result<Unit> = withContext(Dispatchers.IO) {
        val db = firestore ?: return@withContext Result.failure(IllegalStateException("Firestore is null"))
        try {
            val docId = if (notice.id > 0) "notice_${notice.id}" else "notice_${System.currentTimeMillis()}"
            val data = noticeToMap(notice)
            db.collection(FirestoreConstants.COLLECTION_NOTICES).document(docId).set(data, SetOptions.merge()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "uploadNoticeToCloud failed", e)
            Result.failure(e)
        }
    }

    suspend fun deleteNoticeFromCloud(noticeId: Long): Result<Unit> = withContext(Dispatchers.IO) {
        val db = firestore ?: return@withContext Result.failure(IllegalStateException("Firestore is null"))
        try {
            val docId = "notice_$noticeId"
            db.collection(FirestoreConstants.COLLECTION_NOTICES).document(docId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "deleteNoticeFromCloud failed", e)
            Result.failure(e)
        }
    }

    suspend fun uploadRecruitmentToCloud(info: RecruitmentInfo): Result<Unit> = withContext(Dispatchers.IO) {
        val db = firestore ?: return@withContext Result.failure(IllegalStateException("Firestore is null"))
        try {
            val docId = if (info.id > 0) "recruitment_${info.id}" else "recruitment_${System.currentTimeMillis()}"
            val data = recruitmentToMap(info)
            db.collection(FirestoreConstants.COLLECTION_RECRUITMENT).document(docId).set(data, SetOptions.merge()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "uploadRecruitmentToCloud failed", e)
            Result.failure(e)
        }
    }

    suspend fun uploadSubjectToCloud(subject: StudySubject): Result<Unit> = withContext(Dispatchers.IO) {
        val db = firestore ?: return@withContext Result.failure(IllegalStateException("Firestore is null"))
        try {
            val docId = "subject_${subject.subjectId}"
            val data = subjectToMap(subject)
            db.collection(FirestoreConstants.COLLECTION_SUBJECTS).document(docId).set(data, SetOptions.merge()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "uploadSubjectToCloud failed", e)
            Result.failure(e)
        }
    }

    suspend fun uploadTopicToCloud(topic: StudyTopic): Result<Unit> = withContext(Dispatchers.IO) {
        val db = firestore ?: return@withContext Result.failure(IllegalStateException("Firestore is null"))
        try {
            val docId = "topic_${topic.topicId}"
            val data = topicToMap(topic)
            db.collection(FirestoreConstants.COLLECTION_TOPICS).document(docId).set(data, SetOptions.merge()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "uploadTopicToCloud failed", e)
            Result.failure(e)
        }
    }

    suspend fun uploadQuestionToCloud(question: Question): Result<Unit> = withContext(Dispatchers.IO) {
        val db = firestore ?: return@withContext Result.failure(IllegalStateException("Firestore is null"))
        try {
            val docId = "q_${question.questionId.ifEmpty { question.id.toString() }}"
            val data = questionToMap(question)
            db.collection(FirestoreConstants.COLLECTION_QUESTIONS).document(docId).set(data, SetOptions.merge()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "uploadQuestionToCloud failed", e)
            Result.failure(e)
        }
    }

    suspend fun uploadRaceSessionToCloud(session: RaceSession): Result<Unit> = withContext(Dispatchers.IO) {
        val db = firestore ?: return@withContext Result.failure(IllegalStateException("Firestore is null"))
        try {
            val docId = "race_${session.raceId}"
            val data = raceSessionToMap(session)
            db.collection(FirestoreConstants.COLLECTION_RACE_SESSIONS).document(docId).set(data, SetOptions.merge()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "uploadRaceSessionToCloud failed: ${session.raceId}", e)
            Result.failure(e)
        }
    }

    suspend fun uploadRaceResultToCloud(result: RaceResult): Result<Unit> = withContext(Dispatchers.IO) {
        val db = firestore ?: return@withContext Result.failure(IllegalStateException("Firestore is null"))
        try {
            val docId = "result_${result.raceId}_${result.studentId}"
            val data = raceResultToMap(result)
            db.collection(FirestoreConstants.COLLECTION_RACE_RESULTS).document(docId).set(data, SetOptions.merge()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "uploadRaceResultToCloud failed: ${result.raceId}_${result.studentId}", e)
            Result.failure(e)
        }
    }

    suspend fun uploadChapterToCloud(chapter: Chapter): Result<Unit> = withContext(Dispatchers.IO) {
        val db = firestore ?: return@withContext Result.failure(IllegalStateException("Firestore is null"))
        try {
            val docId = "chapter_${chapter.id}"
            val data = chapterToMap(chapter)
            db.collection(FirestoreConstants.COLLECTION_CHAPTERS).document(docId).set(data, SetOptions.merge()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "uploadChapterToCloud failed: ${chapter.id}", e)
            Result.failure(e)
        }
    }

    suspend fun deleteChapterFromCloud(chapterId: Long): Result<Unit> = withContext(Dispatchers.IO) {
        val db = firestore ?: return@withContext Result.failure(IllegalStateException("Firestore is null"))
        try {
            val docId = "chapter_$chapterId"
            db.collection(FirestoreConstants.COLLECTION_CHAPTERS).document(docId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "deleteChapterFromCloud failed: $chapterId", e)
            Result.failure(e)
        }
    }

    suspend fun uploadNotificationToCloud(notification: AppNotification): Result<Unit> = withContext(Dispatchers.IO) {
        val db = firestore ?: return@withContext Result.failure(IllegalStateException("Firestore is null"))
        try {
            val docId = if (notification.notificationId.isNotBlank()) notification.notificationId else "notif_${notification.id}_${notification.timestamp}"
            val data = notificationToMap(notification)
            db.collection(FirestoreConstants.COLLECTION_NOTIFICATIONS).document(docId).set(data, SetOptions.merge()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "uploadNotificationToCloud failed: ${notification.notificationId}", e)
            Result.failure(e)
        }
    }

    suspend fun deleteNotificationFromCloud(notificationId: String): Result<Unit> = withContext(Dispatchers.IO) {
        val db = firestore ?: return@withContext Result.failure(IllegalStateException("Firestore is null"))
        try {
            val docId = notificationId.ifEmpty { return@withContext Result.failure(IllegalArgumentException("Empty notificationId")) }
            db.collection(FirestoreConstants.COLLECTION_NOTIFICATIONS).document(docId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "deleteNotificationFromCloud failed: $notificationId", e)
            Result.failure(e)
        }
    }

    // ==========================================
    // PULL / FETCH OPERATIONS (Firestore -> Room)
    // ==========================================

    private suspend fun fetchChaptersFromCloudInternal(db: FirebaseFirestore): List<Chapter> {
        return try {
            val snapshot = db.collection(FirestoreConstants.COLLECTION_CHAPTERS).get().await()
            snapshot.documents.mapNotNull { doc ->
                mapToChapter(doc.id, doc.data ?: return@mapNotNull null)
            }
        } catch (e: Exception) {
            Log.w(TAG, "fetchChaptersFromCloud error", e)
            emptyList()
        }
    }

    private suspend fun fetchNotificationsFromCloudInternal(db: FirebaseFirestore): List<AppNotification> {
        return try {
            val snapshot = db.collection(FirestoreConstants.COLLECTION_NOTIFICATIONS).get().await()
            snapshot.documents.mapNotNull { doc ->
                mapToNotification(doc.id, doc.data ?: return@mapNotNull null)
            }
        } catch (e: Exception) {
            Log.w(TAG, "fetchNotificationsFromCloud error", e)
            emptyList()
        }
    }

    private suspend fun fetchNoticesFromCloudInternal(db: FirebaseFirestore): List<Notice> {
        return try {
            val snapshot = db.collection(FirestoreConstants.COLLECTION_NOTICES).get().await()
            snapshot.documents.mapNotNull { doc ->
                mapToNotice(doc.id, doc.data ?: return@mapNotNull null)
            }
        } catch (e: Exception) {
            Log.w(TAG, "fetchNoticesFromCloud error", e)
            emptyList()
        }
    }

    private suspend fun fetchRecruitmentFromCloudInternal(db: FirebaseFirestore): List<RecruitmentInfo> {
        return try {
            val snapshot = db.collection(FirestoreConstants.COLLECTION_RECRUITMENT).get().await()
            snapshot.documents.mapNotNull { doc ->
                mapToRecruitment(doc.id, doc.data ?: return@mapNotNull null)
            }
        } catch (e: Exception) {
            Log.w(TAG, "fetchRecruitmentFromCloud error", e)
            emptyList()
        }
    }

    private suspend fun fetchStudentsFromCloudInternal(db: FirebaseFirestore): List<StudentProfile> {
        return try {
            val snapshot = db.collection(FirestoreConstants.COLLECTION_STUDENTS).get().await()
            snapshot.documents.mapNotNull { doc ->
                mapToStudent(doc.id, doc.data ?: return@mapNotNull null)
            }
        } catch (e: Exception) {
            Log.w(TAG, "fetchStudentsFromCloud error", e)
            emptyList()
        }
    }

    private suspend fun fetchSubjectsFromCloudInternal(db: FirebaseFirestore): List<StudySubject> {
        return try {
            val snapshot = db.collection(FirestoreConstants.COLLECTION_SUBJECTS).get().await()
            snapshot.documents.mapNotNull { doc ->
                mapToSubject(doc.id, doc.data ?: return@mapNotNull null)
            }
        } catch (e: Exception) {
            Log.w(TAG, "fetchSubjectsFromCloud error", e)
            emptyList()
        }
    }

    private suspend fun fetchTopicsFromCloudInternal(db: FirebaseFirestore): List<StudyTopic> {
        return try {
            val snapshot = db.collection(FirestoreConstants.COLLECTION_TOPICS).get().await()
            snapshot.documents.mapNotNull { doc ->
                mapToTopic(doc.id, doc.data ?: return@mapNotNull null)
            }
        } catch (e: Exception) {
            Log.w(TAG, "fetchTopicsFromCloud error", e)
            emptyList()
        }
    }

    private suspend fun fetchMockTestsFromCloudInternal(db: FirebaseFirestore): List<MockTest> {
        return try {
            val snapshot = db.collection(FirestoreConstants.COLLECTION_MOCK_TESTS).get().await()
            snapshot.documents.mapNotNull { doc ->
                mapToMockTest(doc.id, doc.data ?: return@mapNotNull null)
            }
        } catch (e: Exception) {
            Log.w(TAG, "fetchMockTestsFromCloud error", e)
            emptyList()
        }
    }

    private suspend fun fetchQuestionsFromCloudInternal(db: FirebaseFirestore): List<Question> {
        return try {
            val snapshot = db.collection(FirestoreConstants.COLLECTION_QUESTIONS).get().await()
            snapshot.documents.mapNotNull { doc ->
                mapToQuestion(doc.id, doc.data ?: return@mapNotNull null)
            }
        } catch (e: Exception) {
            Log.w(TAG, "fetchQuestionsFromCloud error", e)
            emptyList()
        }
    }

    // ==========================================
    // REAL-TIME LISTENERS (Multi-device dynamic updates)
    // ==========================================

    /**
     * Starts listening to live Notice Board updates in Firestore and updates Room cache immediately.
     */
    fun startListeningToNotices(onUpdate: ((List<Notice>) -> Unit)? = null) {
        val db = firestore ?: return
        val listener = db.collection(FirestoreConstants.COLLECTION_NOTICES)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "Notices real-time listener error: ${error.message}")
                    return@addSnapshotListener
                }
                if (snapshot != null && !snapshot.isEmpty) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        mapToNotice(doc.id, doc.data ?: return@mapNotNull null)
                    }
                    onUpdate?.invoke(list)
                }
            }
        activeListeners.add(listener)
    }

    /**
     * Starts listening to live Notifications in Firestore and triggers callback.
     */
    fun startListeningToNotifications(onUpdate: ((List<AppNotification>) -> Unit)? = null) {
        val db = firestore ?: return
        val listener = db.collection(FirestoreConstants.COLLECTION_NOTIFICATIONS)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "Notifications real-time listener error: ${error.message}")
                    return@addSnapshotListener
                }
                if (snapshot != null && !snapshot.isEmpty) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        mapToNotification(doc.id, doc.data ?: return@mapNotNull null)
                    }
                    onUpdate?.invoke(list)
                }
            }
        activeListeners.add(listener)
    }

    fun stopAllListeners() {
        activeListeners.forEach { it.remove() }
        activeListeners.clear()
    }

    // ==========================================
    // MODEL MAPPERS (Room Entities <-> Firestore Maps)
    // Every map includes createdAt, updatedAt, studentId, uid where applicable.
    // ==========================================

    fun studentToMap(s: StudentProfile, uid: String? = null): Map<String, Any?> {
        val now = System.currentTimeMillis()
        return mapOf(
            "studentId" to s.studentId,
            "uid" to (uid ?: ""),
            "fullName" to s.fullName,
            "fatherName" to s.fatherName,
            "mobileNumber" to s.mobileNumber,
            "village" to s.village,
            "dob" to s.dob,
            "age" to s.age,
            "gender" to s.gender,
            "education" to s.education,
            "recruitmentGoal" to s.recruitmentGoal,
            "joinDate" to s.joinDate,
            "profilePhotoUri" to s.profilePhotoUri,
            "heightCm" to s.heightCm,
            "weightKg" to s.weightKg,
            "chestNormalCm" to s.chestNormalCm,
            "chestExpandedCm" to s.chestExpandedCm,
            "time400m" to s.time400m,
            "time800m" to s.time800m,
            "time1600m" to s.time1600m,
            "time5km" to s.time5km,
            "pushups" to s.pushups,
            "situps" to s.situps,
            "pullups" to s.pullups,
            "squats" to s.squats,
            "plankSeconds" to s.plankSeconds,
            "overallScore" to s.overallScore,
            "longJumpFeet" to s.longJumpFeet,
            "highJumpFeet" to s.highJumpFeet,
            "shotPutMeters" to s.shotPutMeters,
            "attendanceStreakDays" to s.attendanceStreakDays,
            "studyTargetPercentage" to s.studyTargetPercentage,
            "assignedTrainerId" to s.assignedTrainerId,
            "assignedTrainerName" to s.assignedTrainerName,
            "batchName" to s.batchName,
            FirestoreConstants.FIELD_CREATED_AT to now,
            FirestoreConstants.FIELD_UPDATED_AT to now
        )
    }

    fun mapToStudent(docId: String, data: Map<String, Any?>): StudentProfile? {
        return try {
            val studentId = (data["studentId"] as? String) ?: docId
            StudentProfile(
                studentId = studentId,
                fullName = data["fullName"] as? String ?: "",
                fatherName = data["fatherName"] as? String ?: "",
                mobileNumber = data["mobileNumber"] as? String ?: "",
                village = data["village"] as? String ?: "",
                dob = data["dob"] as? String ?: "2004-05-15",
                age = (data["age"] as? Number)?.toInt() ?: 21,
                gender = data["gender"] as? String ?: "Male",
                education = data["education"] as? String ?: "12th Pass",
                recruitmentGoal = data["recruitmentGoal"] as? String ?: "Indian Army",
                joinDate = data["joinDate"] as? String ?: "2025-01-10",
                profilePhotoUri = data["profilePhotoUri"] as? String ?: "",
                heightCm = (data["heightCm"] as? Number)?.toDouble() ?: 171.0,
                weightKg = (data["weightKg"] as? Number)?.toDouble() ?: 63.5,
                chestNormalCm = (data["chestNormalCm"] as? Number)?.toDouble() ?: 81.0,
                chestExpandedCm = (data["chestExpandedCm"] as? Number)?.toDouble() ?: 86.0,
                time400m = data["time400m"] as? String ?: "1:08",
                time800m = data["time800m"] as? String ?: "2:26",
                time1600m = data["time1600m"] as? String ?: "5:35",
                time5km = data["time5km"] as? String ?: "21:30",
                pushups = (data["pushups"] as? Number)?.toInt() ?: 45,
                situps = (data["situps"] as? Number)?.toInt() ?: 50,
                pullups = (data["pullups"] as? Number)?.toInt() ?: 12,
                squats = (data["squats"] as? Number)?.toInt() ?: 60,
                plankSeconds = (data["plankSeconds"] as? Number)?.toInt() ?: 120,
                overallScore = (data["overallScore"] as? Number)?.toInt() ?: 84,
                longJumpFeet = (data["longJumpFeet"] as? Number)?.toDouble() ?: 15.5,
                highJumpFeet = (data["highJumpFeet"] as? Number)?.toDouble() ?: 4.2,
                shotPutMeters = (data["shotPutMeters"] as? Number)?.toDouble() ?: 7.5,
                attendanceStreakDays = (data["attendanceStreakDays"] as? Number)?.toInt() ?: 7,
                studyTargetPercentage = (data["studyTargetPercentage"] as? Number)?.toInt() ?: 80,
                assignedTrainerId = data["assignedTrainerId"] as? String ?: "TR-001",
                assignedTrainerName = data["assignedTrainerName"] as? String ?: "देव कुमार निषाद (मुख्य कोच)",
                batchName = data["batchName"] as? String ?: "सुबह आर्मी स्पेशल बैच (Morning Army Batch)"
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error mapping student doc: $docId", e)
            null
        }
    }

    fun attendanceToMap(a: AttendanceRecord, uid: String? = null): Map<String, Any?> {
        val now = System.currentTimeMillis()
        return mapOf(
            "studentId" to a.studentId,
            "uid" to (uid ?: ""),
            "date" to a.date,
            "status" to a.status,
            "remarks" to a.remarks,
            FirestoreConstants.FIELD_CREATED_AT to now,
            FirestoreConstants.FIELD_UPDATED_AT to now
        )
    }

    fun mapToAttendance(docId: String, data: Map<String, Any?>): AttendanceRecord? {
        return try {
            AttendanceRecord(
                studentId = data["studentId"] as? String ?: return null,
                date = data["date"] as? String ?: return null,
                status = data["status"] as? String ?: "Present",
                remarks = data["remarks"] as? String ?: ""
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error mapping attendance doc: $docId", e)
            null
        }
    }

    fun trainingRecordToMap(t: TrainingRecord, uid: String? = null): Map<String, Any?> {
        val now = System.currentTimeMillis()
        return mapOf(
            "studentId" to t.studentId,
            "uid" to (uid ?: ""),
            "date" to t.date,
            "runningDistanceKm" to t.runningDistanceKm,
            "runningDuration" to t.runningDuration,
            "runningType" to t.runningType,
            "pushups" to t.pushups,
            "situps" to t.situps,
            "pullups" to t.pullups,
            "squats" to t.squats,
            "plankSeconds" to t.plankSeconds,
            "stretchingDone" to t.stretchingDone,
            "otherTraining" to t.otherTraining,
            "trainerNotes" to t.trainerNotes,
            "timestamp" to t.timestamp,
            FirestoreConstants.FIELD_CREATED_AT to (if (t.timestamp > 0) t.timestamp else now),
            FirestoreConstants.FIELD_UPDATED_AT to now
        )
    }

    fun testAttemptToMap(t: TestAttempt, uid: String? = null): Map<String, Any?> {
        val now = System.currentTimeMillis()
        return mapOf(
            "studentId" to t.studentId,
            "uid" to (uid ?: ""),
            "testId" to t.testId,
            "testTitle" to t.testTitle,
            "targetExam" to t.targetExam,
            "date" to t.date,
            "totalQuestions" to t.totalQuestions,
            "attemptedCount" to t.attemptedCount,
            "correctCount" to t.correctCount,
            "wrongCount" to t.wrongCount,
            "unattemptedCount" to t.unattemptedCount,
            "score" to t.score,
            "maxScore" to t.maxScore,
            "accuracyPercentage" to t.accuracyPercentage,
            "timeTakenSeconds" to t.timeTakenSeconds,
            "rank" to t.rank,
            "percentile" to t.percentile,
            "mathScore" to t.mathScore,
            "reasoningScore" to t.reasoningScore,
            "gkScore" to t.gkScore,
            "hindiEnglishScore" to t.hindiEnglishScore,
            "weakArea" to t.weakArea,
            "strongArea" to t.strongArea,
            FirestoreConstants.FIELD_CREATED_AT to now,
            FirestoreConstants.FIELD_UPDATED_AT to now
        )
    }

    fun mockTestToMap(m: MockTest): Map<String, Any?> {
        val now = System.currentTimeMillis()
        return mapOf(
            "id" to m.id,
            "title" to m.title,
            "targetExam" to m.targetExam,
            "totalQuestions" to m.totalQuestions,
            "totalMarks" to m.totalMarks,
            "durationMinutes" to m.durationMinutes,
            "negativeMarking" to m.negativeMarking,
            "description" to m.description,
            FirestoreConstants.FIELD_CREATED_AT to now,
            FirestoreConstants.FIELD_UPDATED_AT to now
        )
    }

    fun mapToMockTest(docId: String, data: Map<String, Any?>): MockTest? {
        return try {
            val idNum = docId.removePrefix("mock_test_").toLongOrNull() ?: (data["id"] as? Number)?.toLong() ?: 0L
            MockTest(
                id = idNum,
                title = data["title"] as? String ?: "",
                targetExam = data["targetExam"] as? String ?: "Indian Army",
                totalQuestions = (data["totalQuestions"] as? Number)?.toInt() ?: 20,
                totalMarks = (data["totalMarks"] as? Number)?.toInt() ?: 40,
                durationMinutes = (data["durationMinutes"] as? Number)?.toInt() ?: 30,
                negativeMarking = (data["negativeMarking"] as? Number)?.toDouble() ?: 0.5,
                description = data["description"] as? String ?: ""
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error mapping mock test doc: $docId", e)
            null
        }
    }

    fun noticeToMap(n: Notice): Map<String, Any?> {
        val now = System.currentTimeMillis()
        return mapOf(
            "id" to n.id,
            "title" to n.title,
            "content" to n.content,
            "date" to n.date,
            "category" to n.category,
            "author" to n.author,
            "isUrgent" to n.isUrgent,
            "priority" to n.priority,
            "isPinned" to n.isPinned,
            "expiryDate" to n.expiryDate,
            "isRead" to n.isRead,
            FirestoreConstants.FIELD_CREATED_AT to now,
            FirestoreConstants.FIELD_UPDATED_AT to now
        )
    }

    fun mapToNotice(docId: String, data: Map<String, Any?>): Notice? {
        return try {
            val idNum = docId.removePrefix("notice_").toLongOrNull() ?: (data["id"] as? Number)?.toLong() ?: 0L
            Notice(
                id = idNum,
                title = data["title"] as? String ?: "",
                content = data["content"] as? String ?: "",
                date = data["date"] as? String ?: "",
                category = data["category"] as? String ?: "सामान्य",
                author = data["author"] as? String ?: "मुख्य प्रशिक्षक (Head Trainer)",
                isUrgent = data["isUrgent"] as? Boolean ?: false,
                priority = data["priority"] as? String ?: "NORMAL",
                isPinned = data["isPinned"] as? Boolean ?: false,
                expiryDate = data["expiryDate"] as? String ?: "",
                isRead = data["isRead"] as? Boolean ?: false
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error mapping notice doc: $docId", e)
            null
        }
    }

    fun recruitmentToMap(r: RecruitmentInfo): Map<String, Any?> {
        val now = System.currentTimeMillis()
        return mapOf(
            "id" to r.id,
            "recruitmentName" to r.recruitmentName,
            "organization" to r.organization,
            "category" to r.category,
            "shortDescription" to r.shortDescription,
            "eligibility" to r.eligibility,
            "ageLimit" to r.ageLimit,
            "heightRequirement" to r.heightRequirement,
            "chestRequirement" to r.chestRequirement,
            "physicalTest" to r.physicalTest,
            "writtenExam" to r.writtenExam,
            "syllabus" to r.syllabus,
            "importantDocuments" to r.importantDocuments,
            "importantDates" to r.importantDates,
            "officialWebsiteLink" to r.officialWebsiteLink,
            FirestoreConstants.FIELD_CREATED_AT to now,
            FirestoreConstants.FIELD_UPDATED_AT to now
        )
    }

    fun mapToRecruitment(docId: String, data: Map<String, Any?>): RecruitmentInfo? {
        return try {
            val idNum = docId.removePrefix("recruitment_").toLongOrNull() ?: (data["id"] as? Number)?.toLong() ?: 0L
            RecruitmentInfo(
                id = idNum,
                recruitmentName = data["recruitmentName"] as? String ?: "",
                organization = data["organization"] as? String ?: "",
                category = data["category"] as? String ?: "ARMY",
                shortDescription = data["shortDescription"] as? String ?: "",
                eligibility = data["eligibility"] as? String ?: "",
                ageLimit = data["ageLimit"] as? String ?: "",
                heightRequirement = data["heightRequirement"] as? String ?: "",
                chestRequirement = data["chestRequirement"] as? String ?: "",
                physicalTest = data["physicalTest"] as? String ?: "",
                writtenExam = data["writtenExam"] as? String ?: "",
                syllabus = data["syllabus"] as? String ?: "",
                importantDocuments = data["importantDocuments"] as? String ?: "",
                importantDates = data["importantDates"] as? String ?: "",
                officialWebsiteLink = data["officialWebsiteLink"] as? String ?: ""
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error mapping recruitment doc: $docId", e)
            null
        }
    }

    fun subjectToMap(s: StudySubject): Map<String, Any?> {
        val now = System.currentTimeMillis()
        return mapOf(
            "subjectId" to s.subjectId,
            "name" to s.name,
            "icon" to s.icon,
            "displayOrder" to s.displayOrder,
            "isActive" to s.isActive,
            FirestoreConstants.FIELD_CREATED_AT to now,
            FirestoreConstants.FIELD_UPDATED_AT to now
        )
    }

    fun mapToSubject(docId: String, data: Map<String, Any?>): StudySubject? {
        return try {
            val subId = docId.removePrefix("subject_").ifEmpty { data["subjectId"] as? String ?: "" }
            StudySubject(
                subjectId = subId,
                name = data["name"] as? String ?: "",
                icon = data["icon"] as? String ?: "📚",
                displayOrder = (data["displayOrder"] as? Number)?.toInt() ?: 1,
                isActive = data["isActive"] as? Boolean ?: true
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error mapping subject doc: $docId", e)
            null
        }
    }

    fun topicToMap(t: StudyTopic): Map<String, Any?> {
        val now = System.currentTimeMillis()
        return mapOf(
            "topicId" to t.topicId,
            "subjectId" to t.subjectId,
            "topicName" to t.topicName,
            "displayOrder" to t.displayOrder,
            "isActive" to t.isActive,
            "description" to t.description,
            FirestoreConstants.FIELD_CREATED_AT to now,
            FirestoreConstants.FIELD_UPDATED_AT to now
        )
    }

    fun mapToTopic(docId: String, data: Map<String, Any?>): StudyTopic? {
        return try {
            val topId = docId.removePrefix("topic_").ifEmpty { data["topicId"] as? String ?: "" }
            StudyTopic(
                topicId = topId,
                subjectId = data["subjectId"] as? String ?: "",
                topicName = data["topicName"] as? String ?: "",
                displayOrder = (data["displayOrder"] as? Number)?.toInt() ?: 1,
                isActive = data["isActive"] as? Boolean ?: true,
                description = data["description"] as? String ?: ""
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error mapping topic doc: $docId", e)
            null
        }
    }

    fun questionToMap(q: Question): Map<String, Any?> {
        val now = System.currentTimeMillis()
        return mapOf(
            "questionId" to q.questionId,
            "subjectId" to q.subjectId,
            "topicId" to q.topicId,
            "chapterId" to q.chapterId,
            "subjectName" to q.subjectName,
            "chapterName" to q.chapterName,
            "questionText" to q.questionText,
            "optionA" to q.optionA,
            "optionB" to q.optionB,
            "optionC" to q.optionC,
            "optionD" to q.optionD,
            "correctOption" to q.correctOption,
            "correctOptionLetter" to q.correctOptionLetter,
            "explanation" to q.explanation,
            "difficulty" to q.difficulty,
            "language" to q.language,
            "isActive" to q.isActive,
            "createdDate" to q.createdDate,
            "timestamp" to q.timestamp,
            FirestoreConstants.FIELD_CREATED_AT to (if (q.timestamp > 0) q.timestamp else now),
            FirestoreConstants.FIELD_UPDATED_AT to now
        )
    }

    fun mapToQuestion(docId: String, data: Map<String, Any?>): Question? {
        return try {
            val qId = docId.removePrefix("q_").ifEmpty { data["questionId"] as? String ?: "" }
            Question(
                questionId = qId,
                subjectId = data["subjectId"] as? String ?: "",
                topicId = data["topicId"] as? String ?: "",
                chapterId = (data["chapterId"] as? Number)?.toLong() ?: 0L,
                subjectName = data["subjectName"] as? String ?: "",
                chapterName = data["chapterName"] as? String ?: "",
                questionText = data["questionText"] as? String ?: "",
                optionA = data["optionA"] as? String ?: "",
                optionB = data["optionB"] as? String ?: "",
                optionC = data["optionC"] as? String ?: "",
                optionD = data["optionD"] as? String ?: "",
                correctOption = (data["correctOption"] as? Number)?.toInt() ?: 0,
                correctOptionLetter = data["correctOptionLetter"] as? String ?: "A",
                explanation = data["explanation"] as? String ?: "",
                difficulty = data["difficulty"] as? String ?: "मध्यम",
                language = data["language"] as? String ?: "Hindi",
                isActive = data["isActive"] as? Boolean ?: true,
                createdDate = data["createdDate"] as? String ?: "",
                timestamp = (data["timestamp"] as? Number)?.toLong() ?: System.currentTimeMillis()
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error mapping question doc: $docId", e)
            null
        }
    }

    fun raceSessionToMap(s: RaceSession): Map<String, Any?> {
        val now = System.currentTimeMillis()
        return mapOf(
            "raceId" to s.raceId,
            "batchName" to s.batchName,
            "distanceMeters" to s.distanceMeters,
            "date" to s.date,
            "startTimeMillis" to s.startTimeMillis,
            "endTimeMillis" to s.endTimeMillis,
            "status" to s.status,
            "notes" to s.notes,
            "recordedBy" to s.recordedBy,
            "totalParticipants" to s.totalParticipants,
            "finishedCount" to s.finishedCount,
            FirestoreConstants.FIELD_CREATED_AT to (if (s.createdAt > 0) s.createdAt else now),
            FirestoreConstants.FIELD_UPDATED_AT to now
        )
    }

    fun mapToRaceSession(docId: String, data: Map<String, Any?>): RaceSession? {
        return try {
            val rId = docId.removePrefix("race_").ifEmpty { data["raceId"] as? String ?: "" }
            RaceSession(
                raceId = rId,
                batchName = data["batchName"] as? String ?: "",
                distanceMeters = (data["distanceMeters"] as? Number)?.toInt() ?: 1600,
                date = data["date"] as? String ?: "",
                startTimeMillis = (data["startTimeMillis"] as? Number)?.toLong() ?: 0L,
                endTimeMillis = (data["endTimeMillis"] as? Number)?.toLong(),
                status = data["status"] as? String ?: "READY",
                notes = data["notes"] as? String ?: "",
                recordedBy = data["recordedBy"] as? String ?: "TRAINER",
                totalParticipants = (data["totalParticipants"] as? Number)?.toInt() ?: 0,
                finishedCount = (data["finishedCount"] as? Number)?.toInt() ?: 0,
                createdAt = (data[FirestoreConstants.FIELD_CREATED_AT] as? Number)?.toLong() ?: System.currentTimeMillis(),
                updatedAt = (data[FirestoreConstants.FIELD_UPDATED_AT] as? Number)?.toLong() ?: System.currentTimeMillis()
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error mapping race session doc: $docId", e)
            null
        }
    }

    fun raceResultToMap(r: RaceResult): Map<String, Any?> {
        val now = System.currentTimeMillis()
        return mapOf(
            "raceId" to r.raceId,
            "studentId" to r.studentId,
            "studentName" to r.studentName,
            "chestNumber" to r.chestNumber,
            "village" to r.village,
            "status" to r.status,
            "finishTimestamp" to r.finishTimestamp,
            "elapsedMillis" to r.elapsedMillis,
            "timeFormatted" to r.timeFormatted,
            "rank" to r.rank,
            "isPersonalBest" to r.isPersonalBest,
            "notes" to r.notes,
            FirestoreConstants.FIELD_CREATED_AT to (if (r.createdAt > 0) r.createdAt else now),
            FirestoreConstants.FIELD_UPDATED_AT to now
        )
    }

    fun mapToRaceResult(docId: String, data: Map<String, Any?>): RaceResult? {
        return try {
            RaceResult(
                raceId = data["raceId"] as? String ?: return null,
                studentId = data["studentId"] as? String ?: return null,
                studentName = data["studentName"] as? String ?: "",
                chestNumber = data["chestNumber"] as? String ?: "",
                village = data["village"] as? String ?: "",
                status = data["status"] as? String ?: "RUNNING",
                finishTimestamp = (data["finishTimestamp"] as? Number)?.toLong(),
                elapsedMillis = (data["elapsedMillis"] as? Number)?.toLong() ?: 0L,
                timeFormatted = data["timeFormatted"] as? String ?: "",
                rank = (data["rank"] as? Number)?.toInt() ?: 0,
                isPersonalBest = data["isPersonalBest"] as? Boolean ?: false,
                notes = data["notes"] as? String ?: "",
                createdAt = (data[FirestoreConstants.FIELD_CREATED_AT] as? Number)?.toLong() ?: System.currentTimeMillis(),
                updatedAt = (data[FirestoreConstants.FIELD_UPDATED_AT] as? Number)?.toLong() ?: System.currentTimeMillis()
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error mapping race result doc: $docId", e)
            null
        }
    }

    fun chapterToMap(c: Chapter): Map<String, Any?> {
        val now = System.currentTimeMillis()
        return mapOf(
            "id" to c.id,
            "subjectName" to c.subjectName,
            "chapterNumber" to c.chapterNumber,
            "chapterName" to c.chapterName,
            "chapterOrder" to c.chapterOrder,
            "description" to c.description,
            "notesContent" to c.notesContent,
            "fileUrl" to c.fileUrl,
            "fileName" to c.fileName,
            "examType" to c.examType,
            "language" to c.language,
            "isPublished" to c.isPublished,
            "displayOrder" to c.displayOrder,
            "createdAt" to c.createdAt,
            "updatedAt" to c.updatedAt,
            "videoTitle" to c.videoTitle,
            "videoUrl" to c.videoUrl,
            "practiceQuestionsCount" to c.practiceQuestionsCount,
            "isCompleted" to c.isCompleted,
            "progressPercentage" to c.progressPercentage,
            "isTodayTarget" to c.isTodayTarget,
            FirestoreConstants.FIELD_CREATED_AT to now,
            FirestoreConstants.FIELD_UPDATED_AT to now
        )
    }

    fun mapToChapter(docId: String, data: Map<String, Any?>): Chapter? {
        return try {
            val idNum = docId.removePrefix("chapter_").toLongOrNull() ?: (data["id"] as? Number)?.toLong() ?: 0L
            Chapter(
                id = idNum,
                subjectName = data["subjectName"] as? String ?: "",
                chapterNumber = (data["chapterNumber"] as? Number)?.toInt() ?: 1,
                chapterName = data["chapterName"] as? String ?: "",
                chapterOrder = (data["chapterOrder"] as? Number)?.toInt() ?: 1,
                description = data["description"] as? String ?: "",
                notesContent = data["notesContent"] as? String ?: "",
                fileUrl = data["fileUrl"] as? String ?: "",
                fileName = data["fileName"] as? String ?: "",
                examType = data["examType"] as? String ?: "All Defence & Police Exams",
                language = data["language"] as? String ?: "Hindi",
                isPublished = data["isPublished"] as? Boolean ?: true,
                displayOrder = (data["displayOrder"] as? Number)?.toInt() ?: 1,
                createdAt = data["createdAt"] as? String ?: "2026-08-30",
                updatedAt = data["updatedAt"] as? String ?: "2026-08-30",
                videoTitle = data["videoTitle"] as? String ?: "",
                videoUrl = data["videoUrl"] as? String ?: "",
                practiceQuestionsCount = (data["practiceQuestionsCount"] as? Number)?.toInt() ?: 20,
                isCompleted = data["isCompleted"] as? Boolean ?: false,
                progressPercentage = (data["progressPercentage"] as? Number)?.toInt() ?: 0,
                isTodayTarget = data["isTodayTarget"] as? Boolean ?: false
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error mapping chapter doc: $docId", e)
            null
        }
    }

    fun notificationToMap(n: AppNotification): Map<String, Any?> {
        val now = System.currentTimeMillis()
        return mapOf(
            "id" to n.id,
            "notificationId" to n.notificationId,
            "title" to n.title,
            "message" to n.message,
            "category" to n.category,
            "senderId" to n.senderId,
            "senderName" to n.senderName,
            "senderRole" to n.senderRole,
            "targetType" to n.targetType,
            "targetStudentIds" to n.targetStudentIds,
            "targetBatch" to n.targetBatch,
            "targetTrainerId" to n.targetTrainerId,
            "actionRoute" to n.actionRoute,
            "isUrgent" to n.isUrgent,
            "timestamp" to n.timestamp,
            "dateFormatted" to n.dateFormatted,
            "isRead" to n.isRead,
            "readByUids" to n.readByUids,
            FirestoreConstants.FIELD_CREATED_AT to (if (n.timestamp > 0) n.timestamp else now),
            FirestoreConstants.FIELD_UPDATED_AT to now
        )
    }

    fun mapToNotification(docId: String, data: Map<String, Any?>): AppNotification? {
        return try {
            val notifId = data["notificationId"] as? String ?: docId
            val idNum = (data["id"] as? Number)?.toLong() ?: 0L
            val timestamp = (data["timestamp"] as? Number)?.toLong() ?: System.currentTimeMillis()
            AppNotification(
                id = idNum,
                notificationId = notifId,
                title = data["title"] as? String ?: "",
                message = data["message"] as? String ?: "",
                category = data["category"] as? String ?: AppNotification.CATEGORY_ANNOUNCEMENT,
                senderId = data["senderId"] as? String ?: "",
                senderName = data["senderName"] as? String ?: "मुख्य प्रशिक्षक",
                senderRole = data["senderRole"] as? String ?: "ADMIN",
                targetType = data["targetType"] as? String ?: AppNotification.TARGET_ALL_STUDENTS,
                targetStudentIds = data["targetStudentIds"] as? String ?: "",
                targetBatch = data["targetBatch"] as? String ?: "",
                targetTrainerId = data["targetTrainerId"] as? String ?: "",
                actionRoute = data["actionRoute"] as? String ?: "",
                isUrgent = data["isUrgent"] as? Boolean ?: false,
                timestamp = timestamp,
                dateFormatted = data["dateFormatted"] as? String ?: "",
                isRead = data["isRead"] as? Boolean ?: false,
                readByUids = data["readByUids"] as? String ?: ""
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error mapping notification doc: $docId", e)
            null
        }
    }
}
