package com.example.data.db

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {

    // --- Students ---
    @Query("SELECT * FROM students ORDER BY id ASC")
    fun getAllStudents(): Flow<List<StudentProfile>>

    @Query("SELECT * FROM students ORDER BY id ASC")
    suspend fun getAllStudentsDirect(): List<StudentProfile>

    @Query("SELECT * FROM students WHERE studentId = :studentId LIMIT 1")
    fun getStudentById(studentId: String): Flow<StudentProfile?>

    @Query("SELECT * FROM students WHERE studentId = :studentId LIMIT 1")
    suspend fun getStudentDirect(studentId: String): StudentProfile?

    @Query("SELECT * FROM students WHERE mobileNumber = :mobileNumber LIMIT 1")
    suspend fun getStudentByMobile(mobileNumber: String): StudentProfile?

    @Query("SELECT COUNT(*) FROM students WHERE mobileNumber = :mobileNumber")
    suspend fun countStudentsWithMobile(mobileNumber: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: StudentProfile): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudents(students: List<StudentProfile>)

    @Update
    suspend fun updateStudent(student: StudentProfile)

    @Delete
    suspend fun deleteStudent(student: StudentProfile)

    // --- Attendance ---
    @Query("SELECT * FROM attendance_records ORDER BY date DESC, id DESC")
    fun getAllAttendance(): Flow<List<AttendanceRecord>>

    @Query("SELECT * FROM attendance_records ORDER BY date DESC, id DESC")
    suspend fun getAllAttendanceDirect(): List<AttendanceRecord>

    @Query("SELECT * FROM attendance_records WHERE studentId = :studentId ORDER BY date DESC")
    fun getAttendanceForStudent(studentId: String): Flow<List<AttendanceRecord>>

    @Query("SELECT * FROM attendance_records WHERE studentId = :studentId AND date = :date LIMIT 1")
    fun getTodayAttendance(studentId: String, date: String): Flow<AttendanceRecord?>

    @Query("SELECT * FROM attendance_records WHERE date = :date")
    fun getAttendanceByDate(date: String): Flow<List<AttendanceRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendance(record: AttendanceRecord): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendanceList(records: List<AttendanceRecord>)

    // --- Daily Training Records (Phase 2A) ---
    @Query("SELECT * FROM training_records ORDER BY date DESC, id DESC")
    fun getAllTrainingRecords(): Flow<List<TrainingRecord>>

    @Query("SELECT * FROM training_records ORDER BY date DESC, id DESC")
    suspend fun getAllTrainingRecordsDirect(): List<TrainingRecord>

    @Query("SELECT * FROM training_records WHERE studentId = :studentId ORDER BY date DESC, id DESC")
    fun getTrainingRecordsForStudent(studentId: String): Flow<List<TrainingRecord>>

    @Query("SELECT * FROM training_records WHERE studentId = :studentId ORDER BY date DESC, id DESC")
    suspend fun getTrainingRecordsForStudentDirect(studentId: String): List<TrainingRecord>

    @Query("SELECT * FROM training_records WHERE studentId = :studentId AND date = :date ORDER BY id DESC LIMIT 1")
    fun getTodayTrainingRecord(studentId: String, date: String): Flow<TrainingRecord?>

    @Query("SELECT * FROM training_records WHERE studentId = :studentId AND date = :date ORDER BY id DESC LIMIT 1")
    suspend fun getTrainingRecordDirect(studentId: String, date: String): TrainingRecord?

    @Query("SELECT * FROM training_records WHERE id = :id LIMIT 1")
    fun getTrainingRecordById(id: Long): Flow<TrainingRecord?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrainingRecord(record: TrainingRecord): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrainingRecords(records: List<TrainingRecord>)

    @Update
    suspend fun updateTrainingRecord(record: TrainingRecord)

    @Delete
    suspend fun deleteTrainingRecord(record: TrainingRecord)

    // --- Workouts ---
    @Query("SELECT * FROM workout_records WHERE studentId = :studentId ORDER BY date DESC")
    fun getWorkoutsForStudent(studentId: String): Flow<List<WorkoutRecord>>

    @Query("SELECT * FROM workout_records ORDER BY date DESC")
    fun getAllWorkouts(): Flow<List<WorkoutRecord>>

    @Query("SELECT * FROM workout_records ORDER BY date DESC")
    suspend fun getAllWorkoutsDirect(): List<WorkoutRecord>

    @Query("SELECT * FROM workout_records WHERE studentId = :studentId AND date = :date LIMIT 1")
    fun getTodayWorkout(studentId: String, date: String): Flow<WorkoutRecord?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkoutRecord(record: WorkoutRecord): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkoutRecords(records: List<WorkoutRecord>)

    @Query("SELECT * FROM daily_workout_plans ORDER BY id DESC LIMIT 1")
    fun getLatestWorkoutPlan(): Flow<DailyWorkoutPlan?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkoutPlan(plan: DailyWorkoutPlan): Long

    // --- Subjects (Phase 2B-1) ---
    @Query("SELECT * FROM study_subjects ORDER BY displayOrder ASC, id ASC")
    fun getAllSubjects(): Flow<List<StudySubject>>

    @Query("SELECT * FROM study_subjects ORDER BY displayOrder ASC, id ASC")
    suspend fun getAllSubjectsDirect(): List<StudySubject>

    @Query("SELECT * FROM study_subjects WHERE isActive = 1 ORDER BY displayOrder ASC")
    fun getActiveSubjects(): Flow<List<StudySubject>>

    @Query("SELECT * FROM study_subjects WHERE subjectId = :subjectId LIMIT 1")
    fun getSubjectById(subjectId: String): Flow<StudySubject?>

    @Query("SELECT * FROM study_subjects WHERE subjectId = :subjectId LIMIT 1")
    suspend fun getSubjectDirect(subjectId: String): StudySubject?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubject(subject: StudySubject): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubjects(subjects: List<StudySubject>)

    @Update
    suspend fun updateSubject(subject: StudySubject)

    @Delete
    suspend fun deleteSubject(subject: StudySubject)

    // --- Topics (Phase 2B-1) ---
    @Query("SELECT * FROM study_topics ORDER BY displayOrder ASC, id ASC")
    fun getAllTopics(): Flow<List<StudyTopic>>

    @Query("SELECT * FROM study_topics ORDER BY displayOrder ASC, id ASC")
    suspend fun getAllTopicsDirect(): List<StudyTopic>

    @Query("SELECT * FROM study_topics WHERE subjectId = :subjectId ORDER BY displayOrder ASC")
    fun getTopicsForSubject(subjectId: String): Flow<List<StudyTopic>>

    @Query("SELECT * FROM study_topics WHERE subjectId = :subjectId AND isActive = 1 ORDER BY displayOrder ASC")
    fun getActiveTopicsForSubject(subjectId: String): Flow<List<StudyTopic>>

    @Query("SELECT * FROM study_topics WHERE topicId = :topicId LIMIT 1")
    fun getTopicById(topicId: String): Flow<StudyTopic?>

    @Query("SELECT * FROM study_topics WHERE topicId = :topicId LIMIT 1")
    suspend fun getTopicDirect(topicId: String): StudyTopic?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTopic(topic: StudyTopic): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTopics(topics: List<StudyTopic>)

    @Update
    suspend fun updateTopic(topic: StudyTopic)

    @Delete
    suspend fun deleteTopic(topic: StudyTopic)

    // --- Chapters / Study ---
    @Query("SELECT * FROM chapters ORDER BY displayOrder ASC, chapterNumber ASC, id ASC")
    fun getAllChapters(): Flow<List<Chapter>>

    @Query("SELECT * FROM chapters ORDER BY displayOrder ASC, chapterNumber ASC, id ASC")
    suspend fun getAllChaptersDirect(): List<Chapter>

    @Query("SELECT * FROM chapters WHERE isPublished = 1 ORDER BY displayOrder ASC, chapterNumber ASC, id ASC")
    fun getPublishedChapters(): Flow<List<Chapter>>

    @Query("SELECT * FROM chapters WHERE isPublished = 1 ORDER BY displayOrder ASC, chapterNumber ASC, id ASC")
    suspend fun getPublishedChaptersDirect(): List<Chapter>

    @Query("SELECT * FROM chapters WHERE subjectName = :subject ORDER BY displayOrder ASC, chapterNumber ASC, id ASC")
    fun getChaptersBySubject(subject: String): Flow<List<Chapter>>

    @Query("SELECT * FROM chapters WHERE subjectName = :subject AND isPublished = 1 ORDER BY displayOrder ASC, chapterNumber ASC, id ASC")
    fun getPublishedChaptersBySubject(subject: String): Flow<List<Chapter>>

    @Query("SELECT * FROM chapters WHERE isTodayTarget = 1")
    fun getTodayStudyTargets(): Flow<List<Chapter>>

    @Query("SELECT * FROM chapters WHERE id = :id LIMIT 1")
    fun getChapterById(id: Long): Flow<Chapter?>

    @Query("SELECT * FROM chapters WHERE id = :id LIMIT 1")
    suspend fun getChapterByIdDirect(id: Long): Chapter?

    @Query("SELECT * FROM chapters WHERE LOWER(TRIM(subjectName)) = LOWER(TRIM(:subjectName)) AND chapterNumber = :chapterNumber AND LOWER(TRIM(chapterName)) = LOWER(TRIM(:chapterName)) LIMIT 1")
    suspend fun findDuplicateChapter(subjectName: String, chapterNumber: Int, chapterName: String): Chapter?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChapters(chapters: List<Chapter>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChapter(chapter: Chapter): Long

    @Update
    suspend fun updateChapter(chapter: Chapter)

    @Delete
    suspend fun deleteChapter(chapter: Chapter)

    @Query("DELETE FROM chapters WHERE id = :id")
    suspend fun deleteChapterById(id: Long)

    // --- Questions (Question Bank - Phase 2B-1) ---
    @Query("SELECT * FROM questions ORDER BY id DESC")
    fun getAllQuestions(): Flow<List<Question>>

    @Query("SELECT * FROM questions ORDER BY id DESC")
    suspend fun getAllQuestionsDirect(): List<Question>

    @Query("SELECT * FROM questions WHERE isActive = 1 ORDER BY id DESC")
    fun getActiveQuestions(): Flow<List<Question>>

    @Query("SELECT * FROM questions WHERE subjectId = :subjectId ORDER BY id DESC")
    fun getQuestionsBySubjectId(subjectId: String): Flow<List<Question>>

    @Query("SELECT * FROM questions WHERE topicId = :topicId ORDER BY id DESC")
    fun getQuestionsByTopicId(topicId: String): Flow<List<Question>>

    @Query("SELECT * FROM questions WHERE subjectId = :subjectId AND topicId = :topicId ORDER BY id DESC")
    fun getQuestionsBySubjectAndTopic(subjectId: String, topicId: String): Flow<List<Question>>

    @Query("SELECT * FROM questions WHERE questionId = :questionId LIMIT 1")
    fun getQuestionByQuestionId(questionId: String): Flow<Question?>

    @Query("SELECT * FROM questions WHERE questionId = :questionId LIMIT 1")
    suspend fun getQuestionByQuestionIdDirect(questionId: String): Question?

    @Query("SELECT * FROM questions WHERE id = :id LIMIT 1")
    fun getQuestionById(id: Long): Flow<Question?>

    @Query("SELECT * FROM questions WHERE chapterId = :chapterId")
    fun getQuestionsByChapter(chapterId: Long): Flow<List<Question>>

    @Query("SELECT * FROM questions WHERE subjectName = :subject")
    fun getQuestionsBySubject(subject: String): Flow<List<Question>>

    @Query("SELECT * FROM questions ORDER BY RANDOM() LIMIT :limit")
    suspend fun getRandomQuestions(limit: Int): List<Question>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestions(questions: List<Question>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestion(question: Question): Long

    @Update
    suspend fun updateQuestion(question: Question)

    @Delete
    suspend fun deleteQuestion(question: Question)

    // --- Study Attempts (Phase 2B-1) ---
    @Query("SELECT * FROM study_attempts WHERE studentId = :studentId ORDER BY id DESC")
    fun getStudyAttemptsForStudent(studentId: String): Flow<List<StudyAttempt>>

    @Query("SELECT * FROM study_attempts WHERE studentId = :studentId AND subjectId = :subjectId ORDER BY id DESC")
    fun getStudyAttemptsForStudentAndSubject(studentId: String, subjectId: String): Flow<List<StudyAttempt>>

    @Query("SELECT * FROM study_attempts WHERE studentId = :studentId AND topicId = :topicId ORDER BY id DESC")
    fun getStudyAttemptsForStudentAndTopic(studentId: String, topicId: String): Flow<List<StudyAttempt>>

    @Query("SELECT * FROM study_attempts ORDER BY id DESC")
    fun getAllStudyAttempts(): Flow<List<StudyAttempt>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudyAttempt(attempt: StudyAttempt): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudyAttempts(attempts: List<StudyAttempt>)

    // --- Mock Tests & Attempts ---
    @Query("SELECT * FROM mock_tests ORDER BY id ASC")
    fun getAllMockTests(): Flow<List<MockTest>>

    @Query("SELECT * FROM mock_tests ORDER BY id ASC")
    suspend fun getAllMockTestsDirect(): List<MockTest>

    @Query("SELECT * FROM mock_tests WHERE id = :id LIMIT 1")
    fun getMockTestById(id: Long): Flow<MockTest?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMockTests(tests: List<MockTest>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMockTest(test: MockTest): Long

    @Query("SELECT * FROM test_attempts WHERE studentId = :studentId ORDER BY id DESC")
    fun getAttemptsForStudent(studentId: String): Flow<List<TestAttempt>>

    @Query("SELECT * FROM test_attempts ORDER BY id DESC")
    fun getAllTestAttempts(): Flow<List<TestAttempt>>

    @Query("SELECT * FROM test_attempts ORDER BY id DESC")
    suspend fun getAllTestAttemptsDirect(): List<TestAttempt>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTestAttempt(attempt: TestAttempt): Long

    // --- Notices ---
    @Query("SELECT * FROM notices ORDER BY isPinned DESC, isUrgent DESC, id DESC")
    fun getAllNotices(): Flow<List<Notice>>

    @Query("SELECT * FROM notices ORDER BY isPinned DESC, isUrgent DESC, id DESC")
    suspend fun getAllNoticesDirect(): List<Notice>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotices(notices: List<Notice>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotice(notice: Notice): Long

    @Update
    suspend fun updateNotice(notice: Notice)

    @Delete
    suspend fun deleteNotice(notice: Notice)

    // --- Recruitment Info ---
    @Query("SELECT * FROM recruitment_infos ORDER BY id ASC")
    fun getAllRecruitmentInfo(): Flow<List<RecruitmentInfo>>

    @Query("SELECT * FROM recruitment_infos ORDER BY id ASC")
    suspend fun getAllRecruitmentInfosDirect(): List<RecruitmentInfo>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecruitmentInfos(infos: List<RecruitmentInfo>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecruitmentInfo(info: RecruitmentInfo): Long

    @Update
    suspend fun updateRecruitmentInfo(info: RecruitmentInfo)

    @Delete
    suspend fun deleteRecruitmentInfo(info: RecruitmentInfo)

    // --- Trainers (Content Management) ---
    @Query("SELECT * FROM trainers ORDER BY displayOrder ASC, id ASC")
    fun getAllTrainers(): Flow<List<Trainer>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrainer(trainer: Trainer): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrainers(trainers: List<Trainer>)

    @Update
    suspend fun updateTrainer(trainer: Trainer)

    @Delete
    suspend fun deleteTrainer(trainer: Trainer)

    // --- Gallery Items ---
    @Query("SELECT * FROM gallery_items ORDER BY id DESC")
    fun getAllGalleryItems(): Flow<List<GalleryItem>>

    @Query("SELECT * FROM gallery_items WHERE category = :category ORDER BY id DESC")
    fun getGalleryItemsByCategory(category: String): Flow<List<GalleryItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGalleryItem(item: GalleryItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGalleryItems(items: List<GalleryItem>)

    @Delete
    suspend fun deleteGalleryItem(item: GalleryItem)

    // --- Success Stories ---
    @Query("SELECT * FROM success_stories ORDER BY year DESC, id DESC")
    fun getAllSuccessStories(): Flow<List<SuccessStory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSuccessStory(story: SuccessStory): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSuccessStories(stories: List<SuccessStory>)

    @Update
    suspend fun updateSuccessStory(story: SuccessStory)

    @Delete
    suspend fun deleteSuccessStory(story: SuccessStory)

    // --- Contact Info ---
    @Query("SELECT * FROM contact_info WHERE id = 1 LIMIT 1")
    fun getContactInfo(): Flow<ContactInfo?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateContactInfo(info: ContactInfo): Long

    // --- Phase 2B: Live 1600m Batch Stopwatch & Race Sessions ---
    @Query("SELECT * FROM race_sessions ORDER BY date DESC, startTimeMillis DESC")
    fun getAllRaceSessions(): Flow<List<RaceSession>>

    @Query("SELECT * FROM race_sessions ORDER BY date DESC, startTimeMillis DESC")
    suspend fun getAllRaceSessionsDirect(): List<RaceSession>

    @Query("SELECT * FROM race_sessions WHERE raceId = :raceId LIMIT 1")
    fun getRaceSessionById(raceId: String): Flow<RaceSession?>

    @Query("SELECT * FROM race_sessions WHERE raceId = :raceId LIMIT 1")
    suspend fun getRaceSessionDirect(raceId: String): RaceSession?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRaceSession(session: RaceSession): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRaceSessions(sessions: List<RaceSession>)

    @Update
    suspend fun updateRaceSession(session: RaceSession)

    @Delete
    suspend fun deleteRaceSession(session: RaceSession)

    @Query("DELETE FROM race_sessions WHERE raceId = :raceId")
    suspend fun deleteRaceSessionById(raceId: String)

    // --- Phase 2B: Race Results ---
    @Query("SELECT * FROM race_results ORDER BY rank ASC, elapsedMillis ASC")
    fun getAllRaceResults(): Flow<List<RaceResult>>

    @Query("SELECT * FROM race_results ORDER BY rank ASC, elapsedMillis ASC")
    suspend fun getAllRaceResultsDirect(): List<RaceResult>

    @Query("SELECT * FROM race_results WHERE raceId = :raceId ORDER BY CASE WHEN rank = 0 THEN 9999 ELSE rank END ASC, elapsedMillis ASC")
    fun getRaceResultsForSession(raceId: String): Flow<List<RaceResult>>

    @Query("SELECT * FROM race_results WHERE raceId = :raceId ORDER BY CASE WHEN rank = 0 THEN 9999 ELSE rank END ASC, elapsedMillis ASC")
    suspend fun getRaceResultsForSessionDirect(raceId: String): List<RaceResult>

    @Query("SELECT * FROM race_results WHERE studentId = :studentId ORDER BY createdAt DESC")
    fun getRaceResultsForStudent(studentId: String): Flow<List<RaceResult>>

    @Query("SELECT * FROM race_results WHERE raceId = :raceId AND studentId = :studentId LIMIT 1")
    suspend fun getRaceResultForStudent(raceId: String, studentId: String): RaceResult?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRaceResult(result: RaceResult): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRaceResults(results: List<RaceResult>)

    @Update
    suspend fun updateRaceResult(result: RaceResult)

    @Delete
    suspend fun deleteRaceResult(result: RaceResult)

    @Query("DELETE FROM race_results WHERE raceId = :raceId")
    suspend fun deleteRaceResultsBySession(raceId: String)

    // --- Phase 5B: Communication & Notifications ---
    @Query("SELECT * FROM app_notifications ORDER BY timestamp DESC")
    fun getAllNotifications(): Flow<List<AppNotification>>

    @Query("SELECT * FROM app_notifications ORDER BY timestamp DESC")
    suspend fun getAllNotificationsDirect(): List<AppNotification>

    @Query("SELECT * FROM app_notifications WHERE notificationId = :notifId LIMIT 1")
    fun getNotificationById(notifId: String): Flow<AppNotification?>

    @Query("SELECT * FROM app_notifications WHERE notificationId = :notifId LIMIT 1")
    suspend fun getNotificationByIdDirect(notifId: String): AppNotification?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: AppNotification): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotifications(notifications: List<AppNotification>)

    @Update
    suspend fun updateNotification(notification: AppNotification)

    @Query("UPDATE app_notifications SET isRead = 1 WHERE notificationId = :notifId")
    suspend fun markNotificationAsRead(notifId: String)

    @Query("UPDATE app_notifications SET isRead = 1")
    suspend fun markAllNotificationsAsRead()

    @Query("DELETE FROM app_notifications WHERE notificationId = :notifId")
    suspend fun deleteNotificationByNotifId(notifId: String)

    @Query("DELETE FROM app_notifications WHERE id = :id")
    suspend fun deleteNotification(id: Long)
}

