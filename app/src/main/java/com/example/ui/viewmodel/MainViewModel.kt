package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.AiCoachEngine
import com.example.data.ai.AiCoachInsight
import com.example.data.ai.PerformanceScoreBreakdown
import com.example.data.analytics.monthly.*
import com.example.data.db.AppDatabase
import com.example.data.model.*
import com.example.data.recruitment.*
import com.example.data.repository.AppRepository
import com.example.util.AdminSecurityManager
import com.example.util.RolePermissionManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AppRepository
    private val database: AppDatabase

    init {
        database = AppDatabase.getDatabase(application, viewModelScope)
        repository = AppRepository(database.appDao())
        viewModelScope.launch {
            repository.ensureDataSeeded()
        }
    }

    private val todayDateStr: String
        get() = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    // Admin PIN Security State
    private val _isAdminPinConfigured = MutableStateFlow(AdminSecurityManager.isPinConfigured(getApplication()))
    val isAdminPinConfigured: StateFlow<Boolean> = _isAdminPinConfigured.asStateFlow()

    fun refreshAdminPinStatus() {
        _isAdminPinConfigured.value = AdminSecurityManager.isPinConfigured(getApplication())
    }

    // Role state
    private val _currentRole = MutableStateFlow(RolePermissionManager.ROLE_STUDENT) // "STUDENT", "TRAINER", or "ADMIN"
    val currentRole: StateFlow<String> = _currentRole.asStateFlow()

    // Tracks authenticated staff role to prevent unauthorized self-elevation
    private var authenticatedStaffRole: String? = null

    // Login state
    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    // Selected Active Student ID
    private val _activeStudentId = MutableStateFlow("JBA-2026-001")
    val activeStudentId: StateFlow<String> = _activeStudentId.asStateFlow()

    fun loginAsStudent(studentIdInput: String, passwordInput: String): Boolean {
        val trimmedId = studentIdInput.trim()
        val trimmedPass = passwordInput.trim()
        if (trimmedId.isBlank() || trimmedPass.isBlank()) return false
        val match = allStudents.value.find {
            it.studentId.equals(trimmedId, ignoreCase = true) ||
            it.mobileNumber.equals(trimmedId, ignoreCase = true)
        }
        if (match != null) {
            val isVerified = com.example.util.StudentAuthManager.verifyStudentCredentials(match, trimmedPass)
            if (isVerified) {
                _activeStudentId.value = match.studentId
                _currentRole.value = RolePermissionManager.ROLE_STUDENT
                _isLoggedIn.value = true
                return true
            }
        }
        return false
    }

    /**
     * Direct login after verified registration or verified Phone OTP
     */
    fun loginAsStudentDirect(studentId: String) {
        _activeStudentId.value = studentId
        _currentRole.value = RolePermissionManager.ROLE_STUDENT
        _isLoggedIn.value = true
    }

    /**
     * Authenticates Admin using securely stored salted hash.
     * Rejects empty/default bypasses.
     */
    fun loginAsAdmin(pinInput: String): Boolean {
        val trimmed = pinInput.trim()
        val isConfigured = AdminSecurityManager.isPinConfigured(getApplication())
        if (!isConfigured) {
            return false
        }
        val isValid = AdminSecurityManager.verifyAdminPin(getApplication(), trimmed)
        if (isValid) {
            _currentRole.value = RolePermissionManager.ROLE_ADMIN
            authenticatedStaffRole = RolePermissionManager.ROLE_ADMIN
            _isLoggedIn.value = true
            return true
        }
        return false
    }

    /**
     * Authenticates Ground Coach / Trainer.
     */
    fun loginAsTrainer(pinInput: String): Boolean {
        val trimmed = pinInput.trim()
        val isValid = AdminSecurityManager.verifyTrainerPin(getApplication(), trimmed)
        if (isValid) {
            _currentRole.value = RolePermissionManager.ROLE_TRAINER
            authenticatedStaffRole = RolePermissionManager.ROLE_TRAINER
            _isLoggedIn.value = true
            return true
        }
        return false
    }

    /**
     * Initial one-time Admin PIN configuration.
     */
    fun setupInitialAdminPin(pin: String): Result<Unit> {
        val result = AdminSecurityManager.setupInitialPin(getApplication(), pin)
        if (result.isSuccess) {
            _isAdminPinConfigured.value = true
            _currentRole.value = RolePermissionManager.ROLE_ADMIN
            authenticatedStaffRole = RolePermissionManager.ROLE_ADMIN
            _isLoggedIn.value = true
        }
        return result
    }

    /**
     * Changes existing Admin PIN securely (Strictly Admin only).
     */
    fun changeAdminPin(currentPin: String, newPin: String, confirmPin: String): Result<Unit> {
        if (!RolePermissionManager.canManageAdminSecurity(_currentRole.value)) {
            return Result.failure(SecurityException("अनधिकृत: केवल मुख्य व्यवस्थापक पिन बदल सकते हैं! (Admin only)"))
        }
        val result = AdminSecurityManager.changeAdminPin(getApplication(), currentPin, newPin, confirmPin)
        if (result.isSuccess) {
            _isAdminPinConfigured.value = true
        }
        return result
    }

    /**
     * Registers a new student securely after validating mobile number uniqueness.
     */
    suspend fun registerStudentSecurely(student: StudentProfile): Result<StudentProfile> {
        val trimmedMobile = student.mobileNumber.trim()
        val digitsOnly = trimmedMobile.filter { it.isDigit() }
        if (digitsOnly.length != 10) {
            return Result.failure(IllegalArgumentException("कृपया 10 अंकों का वैध मोबाइल नंबर दर्ज करें! (10 digits required)"))
        }

        val isAlreadyRegistered = repository.isMobileRegistered(trimmedMobile)
        if (isAlreadyRegistered) {
            return Result.failure(IllegalArgumentException("यह मोबाइल नंबर ($trimmedMobile) पहले से पंजीकृत है! कृपया दूसरा नंबर दर्ज करें या अपनी आईडी से लॉगिन करें।"))
        }

        val studentToSave = student.copy(mobileNumber = trimmedMobile)
        repository.insertStudent(studentToSave)
        _activeStudentId.value = studentToSave.studentId
        _currentRole.value = RolePermissionManager.ROLE_STUDENT
        _isLoggedIn.value = true
        return Result.success(studentToSave)
    }

    fun logout() {
        _isLoggedIn.value = false
        authenticatedStaffRole = null
    }

    // Active Subject filter for study
    private val _selectedSubject = MutableStateFlow("Mathematics")
    val selectedSubject: StateFlow<String> = _selectedSubject.asStateFlow()

    // Flows from repository
    val allStudents: StateFlow<List<StudentProfile>> = repository.allStudents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeStudent: StateFlow<StudentProfile?> = _activeStudentId
        .flatMapLatest { id -> repository.getStudent(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val activeAttendance: StateFlow<List<AttendanceRecord>> = _activeStudentId
        .flatMapLatest { id -> repository.getAttendanceForStudent(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAttendanceRecords: StateFlow<List<AttendanceRecord>> = repository.allAttendance
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todayAttendance: StateFlow<AttendanceRecord?> = _activeStudentId
        .flatMapLatest { id -> repository.getTodayAttendance(id, todayDateStr) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val activeTrainingRecords: StateFlow<List<TrainingRecord>> = _activeStudentId
        .flatMapLatest { id -> repository.getTrainingRecordsForStudent(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todayTraining: StateFlow<TrainingRecord?> = _activeStudentId
        .flatMapLatest { id -> repository.getTodayTraining(id, todayDateStr) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allTrainingRecords: StateFlow<List<TrainingRecord>> = repository.allTrainingRecords
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeWorkouts: StateFlow<List<WorkoutRecord>> = _activeStudentId
        .flatMapLatest { id -> repository.getWorkoutsForStudent(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todayWorkout: StateFlow<WorkoutRecord?> = _activeStudentId
        .flatMapLatest { id -> repository.getTodayWorkout(id, todayDateStr) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val latestWorkoutPlan: StateFlow<DailyWorkoutPlan?> = repository.latestWorkoutPlan
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allChapters: StateFlow<List<Chapter>> = repository.allChapters
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val publishedChapters: StateFlow<List<Chapter>> = repository.publishedChapters
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todayStudyTargets: StateFlow<List<Chapter>> = repository.todayStudyTargets
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Phase 2B-1 Study & Question Bank Flows
    val allSubjects: StateFlow<List<StudySubject>> = repository.allSubjects
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeSubjects: StateFlow<List<StudySubject>> = repository.activeSubjects
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTopics: StateFlow<List<StudyTopic>> = repository.allTopics
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allQuestions: StateFlow<List<Question>> = repository.allQuestions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeQuestions: StateFlow<List<Question>> = repository.activeQuestions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allStudyAttempts: StateFlow<List<StudyAttempt>> = repository.allStudyAttempts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val studentStudyAttempts: StateFlow<List<StudyAttempt>> = _activeStudentId
        .flatMapLatest { id -> repository.getStudyAttemptsForStudent(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allMockTests: StateFlow<List<MockTest>> = repository.allMockTests
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTestAttempts: StateFlow<List<TestAttempt>> = repository.allTestAttempts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val studentTestAttempts: StateFlow<List<TestAttempt>> = _activeStudentId
        .flatMapLatest { id -> repository.getAttemptsForStudent(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allNotices: StateFlow<List<Notice>> = repository.allNotices
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allRecruitmentInfo: StateFlow<List<RecruitmentInfo>> = repository.allRecruitmentInfo
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Akhada Content Management Flows ---
    val allTrainers: StateFlow<List<Trainer>> = repository.allTrainers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allGalleryItems: StateFlow<List<GalleryItem>> = repository.allGalleryItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allSuccessStories: StateFlow<List<SuccessStory>> = repository.allSuccessStories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val contactInfo: StateFlow<ContactInfo?> = repository.contactInfo
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Phase 2B: Race Sessions & Live Results
    val allRaceSessions: StateFlow<List<RaceSession>> = repository.allRaceSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allRaceResults: StateFlow<List<RaceResult>> = repository.allRaceResults
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allWorkouts: StateFlow<List<WorkoutRecord>> = repository.allWorkouts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Phase 5B: Communication & Notifications
    val allNotifications: StateFlow<List<AppNotification>> = repository.allNotifications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val unreadNotificationsCount: StateFlow<Int> = allNotifications
        .map { list -> list.count { !it.isRead } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Phase 3: Monthly Cadet Performance & Analytics Engine
    private val _selectedMonthKey = MutableStateFlow(MonthlyPerformanceEngine.getCurrentMonthKey())
    val selectedMonthKey: StateFlow<String> = _selectedMonthKey.asStateFlow()

    fun setSelectedMonth(monthKey: String) {
        _selectedMonthKey.value = monthKey
    }

    fun selectPreviousMonth() {
        _selectedMonthKey.value = MonthlyPerformanceEngine.getPreviousMonthKey(_selectedMonthKey.value)
    }

    fun selectNextMonth() {
        _selectedMonthKey.value = MonthlyPerformanceEngine.getNextMonthKey(_selectedMonthKey.value)
    }

    fun selectCurrentMonth() {
        _selectedMonthKey.value = MonthlyPerformanceEngine.getCurrentMonthKey()
    }

    private data class MonthlyRawDataBundle(
        val training: List<TrainingRecord>,
        val workouts: List<WorkoutRecord>,
        val raceSessions: List<RaceSession>,
        val raceResults: List<RaceResult>
    )

    val monthlyPerformanceReport: StateFlow<MonthlyPerformanceReport?> = combine(
        combine(_selectedMonthKey, allStudents, allAttendanceRecords) { m, s, a ->
            Triple(m, s, a)
        },
        combine(allTrainingRecords, allWorkouts, allRaceSessions, allRaceResults) { t, w, rs, rr ->
            MonthlyRawDataBundle(t, w, rs, rr)
        }
    ) { (monthKey, students, attendance), rawBundle ->
        MonthlyPerformanceEngine.generateMonthlyReport(
            monthKey = monthKey,
            students = students,
            allAttendance = attendance,
            allTraining = rawBundle.training,
            allWorkouts = rawBundle.workouts,
            allRaceSessions = rawBundle.raceSessions,
            allRaceResults = rawBundle.raceResults
        )
    }.flowOn(kotlinx.coroutines.Dispatchers.Default)
     .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Phase 2B: Active Live Stopwatch State
    private val _activeLiveSession = MutableStateFlow<RaceSession?>(null)
    val activeLiveSession: StateFlow<RaceSession?> = _activeLiveSession.asStateFlow()

    private val _liveParticipants = MutableStateFlow<List<RaceResult>>(emptyList())
    val liveParticipants: StateFlow<List<RaceResult>> = _liveParticipants.asStateFlow()

    private val _isStopwatchRunning = MutableStateFlow(false)
    val isStopwatchRunning: StateFlow<Boolean> = _isStopwatchRunning.asStateFlow()

    private val _isStopwatchPaused = MutableStateFlow(false)
    val isStopwatchPaused: StateFlow<Boolean> = _isStopwatchPaused.asStateFlow()

    private val _stopwatchElapsedMillis = MutableStateFlow(0L)
    val stopwatchElapsedMillis: StateFlow<Long> = _stopwatchElapsedMillis.asStateFlow()

    private var stopwatchTickerJob: Job? = null
    private var stopwatchBaseTimeMillis: Long = 0L
    private var stopwatchAccumulatedMillis: Long = 0L
    private var nextRankCounter: Int = 1

    fun addTrainer(trainer: Trainer) {
        if (!RolePermissionManager.canManageContentCms(_currentRole.value)) return
        viewModelScope.launch { repository.addTrainer(trainer) }
    }

    fun updateTrainer(trainer: Trainer) {
        if (!RolePermissionManager.canManageContentCms(_currentRole.value)) return
        viewModelScope.launch { repository.updateTrainer(trainer) }
    }

    fun deleteTrainer(trainer: Trainer) {
        if (!RolePermissionManager.canManageContentCms(_currentRole.value)) return
        viewModelScope.launch { repository.deleteTrainer(trainer) }
    }

    fun addGalleryItem(item: GalleryItem) {
        if (!RolePermissionManager.canManageContentCms(_currentRole.value)) return
        viewModelScope.launch { repository.addGalleryItem(item) }
    }

    fun deleteGalleryItem(item: GalleryItem) {
        if (!RolePermissionManager.canManageContentCms(_currentRole.value)) return
        viewModelScope.launch { repository.deleteGalleryItem(item) }
    }

    fun addSuccessStory(story: SuccessStory) {
        if (!RolePermissionManager.canManageContentCms(_currentRole.value)) return
        viewModelScope.launch { repository.addSuccessStory(story) }
    }

    fun updateSuccessStory(story: SuccessStory) {
        if (!RolePermissionManager.canManageContentCms(_currentRole.value)) return
        viewModelScope.launch { repository.updateSuccessStory(story) }
    }

    fun deleteSuccessStory(story: SuccessStory) {
        if (!RolePermissionManager.canManageContentCms(_currentRole.value)) return
        viewModelScope.launch { repository.deleteSuccessStory(story) }
    }

    fun updateContactInfo(info: ContactInfo) {
        if (!RolePermissionManager.canManageContentCms(_currentRole.value)) return
        viewModelScope.launch { repository.updateContactInfo(info) }
    }

    // Overall Performance & AI Coach Insights
    val unifiedPerformanceState: StateFlow<com.example.data.analytics.UnifiedPerformanceState> = combine(
        listOf(
            activeStudent,
            activeSubjects,
            allTopics,
            activeQuestions,
            studentStudyAttempts,
            studentTestAttempts,
            activeAttendance,
            activeWorkouts,
            activeTrainingRecords
        )
    ) { array ->
        val student = array[0] as? StudentProfile
        @Suppress("UNCHECKED_CAST")
        val subjects = array[1] as List<StudySubject>
        @Suppress("UNCHECKED_CAST")
        val topics = array[2] as List<StudyTopic>
        @Suppress("UNCHECKED_CAST")
        val questions = array[3] as List<Question>
        @Suppress("UNCHECKED_CAST")
        val studyAtt = array[4] as List<StudyAttempt>
        @Suppress("UNCHECKED_CAST")
        val testAtt = array[5] as List<TestAttempt>
        @Suppress("UNCHECKED_CAST")
        val attendance = array[6] as List<AttendanceRecord>
        @Suppress("UNCHECKED_CAST")
        val workouts = array[7] as List<WorkoutRecord>
        @Suppress("UNCHECKED_CAST")
        val training = array[8] as List<TrainingRecord>

        com.example.data.analytics.PerformanceInsightEngine.buildUnifiedPerformanceState(
            student = student,
            subjects = subjects,
            topics = topics,
            questions = questions,
            studyAttempts = studyAtt,
            testAttempts = testAtt,
            attendanceRecords = attendance,
            workoutRecords = workouts,
            trainingRecords = training,
            currentDate = todayDateStr
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        com.example.data.analytics.PerformanceInsightEngine.buildUnifiedPerformanceState(
            student = null,
            subjects = emptyList(),
            topics = emptyList(),
            questions = emptyList(),
            studyAttempts = emptyList(),
            testAttempts = emptyList(),
            attendanceRecords = emptyList(),
            workoutRecords = emptyList(),
            trainingRecords = emptyList(),
            currentDate = todayDateStr
        )
    )

    val performanceScore: StateFlow<PerformanceScoreBreakdown> = combine(
        activeAttendance,
        activeWorkouts,
        allChapters,
        studentTestAttempts
    ) { att, wkt, chp, attm ->
        AiCoachEngine.calculateOverallPerformance(att, wkt, chp, attm)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        PerformanceScoreBreakdown(84, 18.0, 26.0, 16.0, 24.0, "A (बहुत अच्छा)", "उत्कृष्ट संतुलन")
    )

    val aiCoachInsights: StateFlow<List<AiCoachInsight>> = combine(
        listOf(
            activeStudent,
            activeAttendance,
            activeWorkouts,
            allChapters,
            studentTestAttempts,
            studentStudyAttempts,
            unifiedPerformanceState
        )
    ) { array ->
        val student = array[0] as? StudentProfile
        @Suppress("UNCHECKED_CAST")
        val att = array[1] as List<AttendanceRecord>
        @Suppress("UNCHECKED_CAST")
        val wkt = array[2] as List<WorkoutRecord>
        @Suppress("UNCHECKED_CAST")
        val chp = array[3] as List<Chapter>
        @Suppress("UNCHECKED_CAST")
        val testAttm = array[4] as List<TestAttempt>
        @Suppress("UNCHECKED_CAST")
        val studyAttm = array[5] as List<StudyAttempt>
        val uState = array[6] as? com.example.data.analytics.UnifiedPerformanceState

        if (student != null) {
            AiCoachEngine.generatePersonalizedInsights(
                student = student,
                attendanceRecords = att,
                workoutRecords = wkt,
                chapters = chp,
                testAttempts = testAttm,
                studyAttempts = studyAttm,
                unifiedState = uState
            )
        } else emptyList()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Phase 2C: Recruitment & Eligibility Intelligence State ---
    private val _selectedRecruitmentCategory = MutableStateFlow("ARMY_AGNIVEER_GD")
    val selectedRecruitmentCategory: StateFlow<String> = _selectedRecruitmentCategory.asStateFlow()

    val selectedRecruitmentProfile: StateFlow<RecruitmentCategoryProfile> = _selectedRecruitmentCategory
        .map { catId -> RecruitmentStandardRepository.findProfileByIdOrGoal(catId) }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            RecruitmentStandardRepository.allCategoryProfiles.first()
        )

    val studentEligibilityReport: StateFlow<StudentEligibilityReport> = combine(
        activeStudent,
        selectedRecruitmentProfile
    ) { student, profile ->
        EligibilityIntelligenceEngine.evaluateEligibility(student, profile)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        EligibilityIntelligenceEngine.evaluateEligibility(null, RecruitmentStandardRepository.allCategoryProfiles.first())
    )

    val physicalStandardComparison: StateFlow<List<PhysicalStandardComparisonItem>> = combine(
        activeStudent,
        selectedRecruitmentProfile
    ) { student, profile ->
        EligibilityIntelligenceEngine.comparePhysicalStandards(student, profile)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val personalizedRecruitmentRecommendations: StateFlow<List<String>> = combine(
        listOf(
            activeStudent,
            selectedRecruitmentProfile,
            activeWorkouts,
            studentTestAttempts,
            studentStudyAttempts
        )
    ) { array ->
        val student = array[0] as? StudentProfile
        val profile = array[1] as? RecruitmentCategoryProfile ?: RecruitmentStandardRepository.allCategoryProfiles.first()
        @Suppress("UNCHECKED_CAST")
        val wkt = array[2] as List<WorkoutRecord>
        @Suppress("UNCHECKED_CAST")
        val testAttm = array[3] as List<TestAttempt>
        @Suppress("UNCHECKED_CAST")
        val studyAttm = array[4] as List<StudyAttempt>

        EligibilityIntelligenceEngine.generatePersonalizedRecruitmentRecommendations(
            student = student,
            targetProfile = profile,
            workoutRecords = wkt,
            testAttempts = testAttm,
            studyAttempts = studyAttm
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Phase 2C: Notice Intelligence State ---
    private val _selectedNoticeCategory = MutableStateFlow("ALL")
    val selectedNoticeCategory: StateFlow<String> = _selectedNoticeCategory.asStateFlow()

    private val _noticeSearchQuery = MutableStateFlow("")
    val noticeSearchQuery: StateFlow<String> = _noticeSearchQuery.asStateFlow()

    val filteredSortedNotices: StateFlow<List<Notice>> = combine(
        allNotices,
        _selectedNoticeCategory,
        _noticeSearchQuery
    ) { notices, cat, query ->
        val filtered = EligibilityIntelligenceEngine.filterAndSortNotices(
            notices = notices,
            selectedCategory = cat,
            currentDate = todayDateStr
        )
        if (query.isBlank()) {
            filtered
        } else {
            val q = query.trim().lowercase()
            filtered.filter {
                it.title.lowercase().contains(q) ||
                it.content.lowercase().contains(q) ||
                it.category.lowercase().contains(q) ||
                it.author.lowercase().contains(q)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Active Quiz & Mock Test State ---
    private val _testTitle = MutableStateFlow("Daily Practice Quiz")
    val testTitle: StateFlow<String> = _testTitle.asStateFlow()

    private val _targetExam = MutableStateFlow("General Competition")
    val targetExam: StateFlow<String> = _targetExam.asStateFlow()

    private val _currentQuestions = MutableStateFlow<List<Question>>(emptyList())
    val currentQuestions: StateFlow<List<Question>> = _currentQuestions.asStateFlow()

    private val _currentQuestionIndex = MutableStateFlow(0)
    val currentQuestionIndex: StateFlow<Int> = _currentQuestionIndex.asStateFlow()

    private val _userAnswers = MutableStateFlow<Map<Int, Int>>(emptyMap()) // index -> selectedOption (0..3)
    val userAnswers: StateFlow<Map<Int, Int>> = _userAnswers.asStateFlow()

    private val _markedForReview = MutableStateFlow<Set<Int>>(emptySet())
    val markedForReview: StateFlow<Set<Int>> = _markedForReview.asStateFlow()

    private val _timeRemainingSeconds = MutableStateFlow(1200)
    val timeRemainingSeconds: StateFlow<Int> = _timeRemainingSeconds.asStateFlow()

    private val _isTestCompleted = MutableStateFlow(false)
    val isTestCompleted: StateFlow<Boolean> = _isTestCompleted.asStateFlow()

    private val _latestAttemptResult = MutableStateFlow<TestAttempt?>(null)
    val latestAttemptResult: StateFlow<TestAttempt?> = _latestAttemptResult.asStateFlow()

    private var timerJob: Job? = null
    private var testTotalDurationSeconds = 1200
    private var activeTestId: Long = 0

    fun switchRole(role: String) {
        val target = RolePermissionManager.sanitizeRole(role)
        // If attempting to switch to ADMIN, ensure user was authenticated with Admin privileges
        if (target == RolePermissionManager.ROLE_ADMIN && authenticatedStaffRole != RolePermissionManager.ROLE_ADMIN) {
            if (authenticatedStaffRole == RolePermissionManager.ROLE_TRAINER) {
                _currentRole.value = RolePermissionManager.ROLE_TRAINER
            } else {
                _currentRole.value = RolePermissionManager.ROLE_STUDENT
            }
            return
        }
        _currentRole.value = target
    }

    fun switchActiveStudent(studentId: String) {
        _activeStudentId.value = studentId
    }

    fun setSelectedSubject(subject: String) {
        _selectedSubject.value = subject
    }

    fun markTodayAttendance(status: String, remarks: String = "") {
        viewModelScope.launch {
            val record = AttendanceRecord(
                studentId = _activeStudentId.value,
                date = todayDateStr,
                status = status,
                remarks = remarks
            )
            repository.markAttendance(record)
        }
    }

    fun submitDailyWorkout(
        runningKm: Double,
        time1600Seconds: Int,
        pushups: Int,
        situps: Int,
        pullups: Int,
        squats: Int,
        notes: String
    ) {
        viewModelScope.launch {
            val prevWorkouts = activeWorkouts.value
            val prev1600 = prevWorkouts.firstOrNull()?.time1600mSeconds ?: (time1600Seconds + 20)
            val record = WorkoutRecord(
                studentId = _activeStudentId.value,
                date = todayDateStr,
                runningDistanceKm = runningKm,
                runningTargetKm = 3.0,
                time1600mSeconds = time1600Seconds,
                previous1600mSeconds = prev1600,
                pushupsDone = pushups,
                pushupsTarget = 50,
                situpsDone = situps,
                situpsTarget = 50,
                pullupsDone = pullups,
                pullupsTarget = 10,
                squatsDone = squats,
                squatsTarget = 50,
                plankSecondsDone = 90,
                notes = notes
            )
            repository.recordWorkout(record)
        }
    }

    fun submitTrainingRecord(
        studentId: String = _activeStudentId.value,
        date: String = todayDateStr,
        runningDistanceKm: Double,
        runningDuration: String,
        runningType: String,
        pushups: Int,
        situps: Int,
        pullups: Int,
        squats: Int,
        plankSeconds: Int,
        stretchingDone: Boolean = true,
        otherTraining: String = "",
        trainerNotes: String = ""
    ) {
        viewModelScope.launch {
            val record = TrainingRecord(
                studentId = studentId,
                date = date,
                runningDistanceKm = runningDistanceKm,
                runningDuration = runningDuration,
                runningType = runningType,
                pushups = pushups,
                situps = situps,
                pullups = pullups,
                squats = squats,
                plankSeconds = plankSeconds,
                stretchingDone = stretchingDone,
                otherTraining = otherTraining,
                trainerNotes = trainerNotes,
                timestamp = System.currentTimeMillis()
            )
            repository.recordTraining(record)
        }
    }

    fun saveTrainingRecord(record: TrainingRecord) {
        viewModelScope.launch {
            repository.recordTraining(record)
        }
    }

    fun updateTrainingRecord(record: TrainingRecord) {
        viewModelScope.launch {
            repository.updateTraining(record)
        }
    }

    fun deleteTrainingRecord(record: TrainingRecord) {
        viewModelScope.launch {
            repository.deleteTraining(record)
        }
    }

    fun getAttendanceForDate(date: String): Flow<List<AttendanceRecord>> = repository.getAttendanceByDate(date)

    fun toggleChapterCompletion(chapter: Chapter) {
        viewModelScope.launch {
            repository.toggleChapterCompletion(chapter)
        }
    }

    // --- Quiz & Mock Test Setup ---
    fun startChapterQuiz(chapter: Chapter, questionCount: Int = 10) {
        viewModelScope.launch {
            val questions = allQuestions.value.filter { it.chapterId == chapter.id }
                .ifEmpty { allQuestions.value.filter { it.subjectName == chapter.subjectName } }
                .ifEmpty { allQuestions.value }
                .take(questionCount)

            _testTitle.value = "${chapter.chapterName} - Quiz"
            _targetExam.value = chapter.subjectName
            _currentQuestions.value = questions
            _currentQuestionIndex.value = 0
            _userAnswers.value = emptyMap()
            _markedForReview.value = emptySet()
            _isTestCompleted.value = false
            _latestAttemptResult.value = null
            activeTestId = chapter.id
            testTotalDurationSeconds = questionCount * 60
            _timeRemainingSeconds.value = testTotalDurationSeconds

            startTimer()
        }
    }

    fun startMockTest(mockTest: MockTest) {
        viewModelScope.launch {
            val questions = allQuestions.value.shuffled().take(mockTest.totalQuestions)
                .ifEmpty { allQuestions.value }

            _testTitle.value = mockTest.title
            _targetExam.value = mockTest.targetExam
            _currentQuestions.value = questions
            _currentQuestionIndex.value = 0
            _userAnswers.value = emptyMap()
            _markedForReview.value = emptySet()
            _isTestCompleted.value = false
            _latestAttemptResult.value = null
            activeTestId = mockTest.id
            testTotalDurationSeconds = mockTest.durationMinutes * 60
            _timeRemainingSeconds.value = testTotalDurationSeconds

            startTimer()
        }
    }

    fun startCustomQuiz(subject: String, count: Int) {
        viewModelScope.launch {
            val questions = allQuestions.value
                .filter { if (subject == "All") true else it.subjectName == subject }
                .shuffled()
                .take(count)
                .ifEmpty { allQuestions.value }

            _testTitle.value = "$subject Speed Practice ($count Qs)"
            _targetExam.value = subject
            _currentQuestions.value = questions
            _currentQuestionIndex.value = 0
            _userAnswers.value = emptyMap()
            _markedForReview.value = emptySet()
            _isTestCompleted.value = false
            _latestAttemptResult.value = null
            activeTestId = 99
            testTotalDurationSeconds = count * 60
            _timeRemainingSeconds.value = testTotalDurationSeconds

            startTimer()
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_timeRemainingSeconds.value > 0 && !_isTestCompleted.value) {
                delay(1000)
                _timeRemainingSeconds.value -= 1
            }
            if (_timeRemainingSeconds.value <= 0 && !_isTestCompleted.value) {
                submitTest()
            }
        }
    }

    fun selectOption(questionIndex: Int, option: Int) {
        val current = _userAnswers.value.toMutableMap()
        current[questionIndex] = option
        _userAnswers.value = current
    }

    fun toggleMarkForReview(questionIndex: Int) {
        val current = _markedForReview.value.toMutableSet()
        if (current.contains(questionIndex)) {
            current.remove(questionIndex)
        } else {
            current.add(questionIndex)
        }
        _markedForReview.value = current
    }

    fun navigateToQuestion(index: Int) {
        if (index in 0 until _currentQuestions.value.size) {
            _currentQuestionIndex.value = index
        }
    }

    fun nextQuestion() {
        if (_currentQuestionIndex.value < _currentQuestions.value.size - 1) {
            _currentQuestionIndex.value += 1
        }
    }

    fun previousQuestion() {
        if (_currentQuestionIndex.value > 0) {
            _currentQuestionIndex.value -= 1
        }
    }

    fun submitTest() {
        timerJob?.cancel()
        val questions = _currentQuestions.value
        val answers = _userAnswers.value
        val totalQ = questions.size
        var correct = 0
        var wrong = 0
        var unattempted = 0

        var mathCorrect = 0
        var mathTotal = 0
        var reasoningCorrect = 0
        var reasoningTotal = 0
        var gkCorrect = 0
        var gkTotal = 0
        var hindiEngCorrect = 0
        var hindiEngTotal = 0

        questions.forEachIndexed { index, q ->
            val userAns = answers[index]
            val isMath = q.subjectName == "Mathematics"
            val isReasoning = q.subjectName == "Reasoning"
            val isGk = q.subjectName.contains("GK")
            val isLang = q.subjectName == "Hindi" || q.subjectName == "English"

            if (isMath) mathTotal++
            if (isReasoning) reasoningTotal++
            if (isGk) gkTotal++
            if (isLang) hindiEngTotal++

            if (userAns == null) {
                unattempted++
            } else if (userAns == q.correctOption) {
                correct++
                if (isMath) mathCorrect++
                if (isReasoning) reasoningCorrect++
                if (isGk) gkCorrect++
                if (isLang) hindiEngCorrect++
            } else {
                wrong++
            }
        }

        val marksPerQ = 2.0
        val negPerQ = 0.5
        val rawScore = (correct * marksPerQ) - (wrong * negPerQ)
        val maxScore = totalQ * marksPerQ
        val finalScore = rawScore.coerceAtLeast(0.0)
        val attempted = correct + wrong
        val accuracy = if (attempted > 0) (correct.toDouble() / attempted * 100.0) else 0.0
        val timeTaken = testTotalDurationSeconds - _timeRemainingSeconds.value

        val weakSubject = when {
            gkTotal > 0 && (gkCorrect.toDouble() / gkTotal) < 0.6 -> "GK / GS & Current Affairs"
            mathTotal > 0 && (mathCorrect.toDouble() / mathTotal) < 0.6 -> "Mathematics (गणित)"
            reasoningTotal > 0 && (reasoningCorrect.toDouble() / reasoningTotal) < 0.6 -> "Reasoning (तर्कशक्ति)"
            else -> "सामान्य अध्ययन व रिवीज़न"
        }

        val strongSubject = when {
            mathTotal > 0 && (mathCorrect.toDouble() / mathTotal) >= 0.7 -> "Mathematics (गणित)"
            reasoningTotal > 0 && (reasoningCorrect.toDouble() / reasoningTotal) >= 0.7 -> "Reasoning (तर्कशक्ति)"
            gkTotal > 0 && (gkCorrect.toDouble() / gkTotal) >= 0.7 -> "GK / GS"
            else -> "हिंदी व्याकरण व सामान्य ज्ञान"
        }

        val attempt = TestAttempt(
            testId = activeTestId,
            testTitle = _testTitle.value,
            targetExam = _targetExam.value,
            studentId = _activeStudentId.value,
            date = todayDateStr,
            totalQuestions = totalQ,
            attemptedCount = attempted,
            correctCount = correct,
            wrongCount = wrong,
            unattemptedCount = unattempted,
            score = finalScore,
            maxScore = maxScore,
            accuracyPercentage = Math.round(accuracy * 10.0) / 10.0,
            timeTakenSeconds = timeTaken,
            rank = if (finalScore >= maxScore * 0.8) 1 else 2,
            percentile = if (finalScore >= maxScore * 0.8) 95.5 else 82.0,
            mathScore = "$mathCorrect/${mathTotal.coerceAtLeast(1)}",
            reasoningScore = "$reasoningCorrect/${reasoningTotal.coerceAtLeast(1)}",
            gkScore = "$gkCorrect/${gkTotal.coerceAtLeast(1)}",
            hindiEnglishScore = "$hindiEngCorrect/${hindiEngTotal.coerceAtLeast(1)}",
            weakArea = weakSubject,
            strongArea = strongSubject
        )

        _latestAttemptResult.value = attempt
        _isTestCompleted.value = true

        viewModelScope.launch {
            repository.recordTestAttempt(attempt)
        }
    }

    // --- Admin & Trainer Actions ---
    fun addStudent(student: StudentProfile, onResult: (Result<Unit>) -> Unit = {}) {
        if (!RolePermissionManager.canEnrollStudent(_currentRole.value)) {
            onResult(Result.failure(SecurityException("अनधिकृत: केवल अधिकृत स्टाफ नया छात्र पंजीकृत कर सकता है")))
            return
        }
        viewModelScope.launch {
            val trimmedMobile = student.mobileNumber.trim()
            val digitsOnly = trimmedMobile.filter { it.isDigit() }
            if (digitsOnly.length != 10) {
                onResult(Result.failure(IllegalArgumentException("कृपया 10 अंकों का वैध मोबाइल नंबर दर्ज करें!")))
                return@launch
            }
            val exists = repository.isMobileRegistered(trimmedMobile)
            if (exists) {
                onResult(Result.failure(IllegalArgumentException("मोबाइल नंबर ($trimmedMobile) पहले से पंजीकृत है!")))
                return@launch
            }
            repository.insertStudent(student.copy(mobileNumber = trimmedMobile))
            onResult(Result.success(Unit))
        }
    }

    fun updateStudent(student: StudentProfile) {
        if (!RolePermissionManager.canEnrollStudent(_currentRole.value)) return
        viewModelScope.launch {
            repository.updateStudent(student)
        }
    }

    fun deleteStudent(student: StudentProfile) {
        // Strictly Admin only
        if (!RolePermissionManager.canDeleteStudent(_currentRole.value)) return
        viewModelScope.launch {
            repository.deleteStudent(student)
        }
    }

    fun selectRecruitmentCategory(catId: String) {
        _selectedRecruitmentCategory.value = catId
    }

    fun selectNoticeCategory(category: String) {
        _selectedNoticeCategory.value = category
    }

    fun setNoticeSearchQuery(query: String) {
        _noticeSearchQuery.value = query
    }

    fun publishNotice(title: String, content: String, category: String, isUrgent: Boolean) {
        if (!RolePermissionManager.canManageNotices(_currentRole.value)) return
        viewModelScope.launch {
            val notice = Notice(
                title = title,
                content = content,
                category = category,
                date = todayDateStr,
                author = "जय बजरंग अखाड़ा प्रशासक",
                isUrgent = isUrgent,
                priority = if (isUrgent) "URGENT" else "NORMAL"
            )
            repository.addNotice(notice)
        }
    }

    fun publishNoticeWithDetails(
        title: String,
        content: String,
        category: String,
        priority: String = "NORMAL",
        isPinned: Boolean = false,
        expiryDate: String = "",
        author: String = "मुख्य प्रशिक्षक (Head Trainer)"
    ) {
        if (!RolePermissionManager.canManageNotices(_currentRole.value)) return
        viewModelScope.launch {
            val notice = Notice(
                title = title,
                content = content,
                category = category,
                date = todayDateStr,
                author = author,
                isUrgent = priority.equals("URGENT", true) || priority.equals("HIGH", true),
                priority = priority,
                isPinned = isPinned,
                expiryDate = expiryDate
            )
            repository.addNotice(notice)
        }
    }

    fun updateNotice(notice: Notice) {
        if (!RolePermissionManager.canManageNotices(_currentRole.value)) return
        viewModelScope.launch {
            repository.updateNotice(notice)
        }
    }

    fun togglePinNotice(notice: Notice) {
        if (!RolePermissionManager.canManageNotices(_currentRole.value)) return
        viewModelScope.launch {
            repository.togglePinNotice(notice)
        }
    }

    fun toggleNoticePin(notice: Notice) {
        togglePinNotice(notice)
    }

    fun markNoticeRead(notice: Notice) {
        viewModelScope.launch {
            repository.markNoticeRead(notice)
        }
    }

    fun deleteNotice(notice: Notice) {
        if (!RolePermissionManager.canManageNotices(_currentRole.value)) return
        viewModelScope.launch {
            repository.deleteNotice(notice)
        }
    }

    fun createWorkoutPlan(plan: DailyWorkoutPlan) {
        if (!RolePermissionManager.canManageWorkoutPlan(_currentRole.value)) return
        viewModelScope.launch {
            repository.setWorkoutPlan(plan)
        }
    }

    fun addRecruitmentInfo(info: RecruitmentInfo) {
        if (!RolePermissionManager.canManageNotices(_currentRole.value)) return
        viewModelScope.launch {
            repository.addRecruitmentInfo(info)
        }
    }

    fun updateRecruitmentInfo(info: RecruitmentInfo) {
        if (!RolePermissionManager.canManageNotices(_currentRole.value)) return
        viewModelScope.launch {
            repository.updateRecruitmentInfo(info)
        }
    }

    fun deleteRecruitmentInfo(info: RecruitmentInfo) {
        if (!RolePermissionManager.canManageNotices(_currentRole.value)) return
        viewModelScope.launch {
            repository.deleteRecruitmentInfo(info)
        }
    }

    fun addQuestion(question: Question) {
        if (!RolePermissionManager.canManageQuestionBank(_currentRole.value)) return
        viewModelScope.launch {
            repository.addQuestion(question)
        }
    }

    fun updateQuestion(question: Question) {
        if (!RolePermissionManager.canManageQuestionBank(_currentRole.value)) return
        viewModelScope.launch {
            repository.updateQuestion(question)
        }
    }

    fun deleteQuestion(question: Question) {
        if (!RolePermissionManager.canManageQuestionBank(_currentRole.value)) return
        viewModelScope.launch {
            repository.deleteQuestion(question)
        }
    }

    fun toggleQuestionActive(question: Question) {
        if (!RolePermissionManager.canManageQuestionBank(_currentRole.value)) return
        viewModelScope.launch {
            repository.updateQuestion(question.copy(isActive = !question.isActive))
        }
    }

    fun addSubject(subject: StudySubject) {
        if (!RolePermissionManager.canManageStudyMaterials(_currentRole.value)) return
        viewModelScope.launch {
            repository.insertSubject(subject)
        }
    }

    fun updateSubject(subject: StudySubject) {
        if (!RolePermissionManager.canManageStudyMaterials(_currentRole.value)) return
        viewModelScope.launch {
            repository.updateSubject(subject)
        }
    }

    fun deleteSubject(subject: StudySubject) {
        if (!RolePermissionManager.canManageStudyMaterials(_currentRole.value)) return
        viewModelScope.launch {
            repository.deleteSubject(subject)
        }
    }

    fun addTopic(topic: StudyTopic) {
        if (!RolePermissionManager.canManageStudyMaterials(_currentRole.value)) return
        viewModelScope.launch {
            repository.insertTopic(topic)
        }
    }

    fun updateTopic(topic: StudyTopic) {
        if (!RolePermissionManager.canManageStudyMaterials(_currentRole.value)) return
        viewModelScope.launch {
            repository.updateTopic(topic)
        }
    }

    fun deleteTopic(topic: StudyTopic) {
        if (!RolePermissionManager.canManageStudyMaterials(_currentRole.value)) return
        viewModelScope.launch {
            repository.deleteTopic(topic)
        }
    }

    fun recordStudyQuestionAttempt(
        questionId: String,
        subjectId: String,
        topicId: String,
        selectedAnswer: String,
        isCorrect: Boolean,
        timeTakenSeconds: Int = 0,
        studentIdOverride: String? = null
    ) {
        viewModelScope.launch {
            val studentId = studentIdOverride ?: _activeStudentId.value
            val attempt = StudyAttempt(
                studentId = studentId,
                questionId = questionId,
                subjectId = subjectId,
                topicId = topicId,
                selectedAnswer = selectedAnswer,
                isCorrect = isCorrect,
                timeTakenSeconds = timeTakenSeconds,
                attemptDate = todayDateStr
            )
            repository.recordStudyAttempt(attempt)
        }
    }

    fun addChapter(chapter: Chapter, onResult: (Result<Long>) -> Unit = {}) {
        if (!RolePermissionManager.canManageStudyMaterials(_currentRole.value)) {
            onResult(Result.failure(SecurityException("अनधिकृत: केवल अधिकृत व्यवस्थापक/प्रशिक्षक ही स्टडी मटेरियल जोड़ सकते हैं!")))
            return
        }
        viewModelScope.launch {
            val result = repository.addChapter(chapter)
            onResult(result)
        }
    }

    fun updateChapter(chapter: Chapter, onResult: (Result<Unit>) -> Unit = {}) {
        if (!RolePermissionManager.canManageStudyMaterials(_currentRole.value)) {
            onResult(Result.failure(SecurityException("अनधिकृत: केवल अधिकृत व्यवस्थापक/प्रशिक्षक ही स्टडी मटेरियल संपादित कर सकते हैं!")))
            return
        }
        viewModelScope.launch {
            val result = repository.updateChapter(chapter)
            onResult(result)
        }
    }

    fun deleteChapter(chapter: Chapter) {
        if (!RolePermissionManager.canManageStudyMaterials(_currentRole.value)) return
        viewModelScope.launch {
            repository.deleteChapter(chapter)
        }
    }

    fun deleteChapterById(id: Long) {
        if (!RolePermissionManager.canManageStudyMaterials(_currentRole.value)) return
        viewModelScope.launch {
            repository.deleteChapterById(id)
        }
    }

    fun toggleChapterPublish(chapter: Chapter) {
        if (!RolePermissionManager.canManageStudyMaterials(_currentRole.value)) return
        viewModelScope.launch {
            repository.toggleChapterPublish(chapter)
        }
    }

    fun reorderChapters(chapters: List<Chapter>) {
        if (!RolePermissionManager.canManageStudyMaterials(_currentRole.value)) return
        viewModelScope.launch {
            chapters.forEachIndexed { index, chapter ->
                val updated = chapter.copy(
                    displayOrder = index + 1,
                    chapterOrder = index + 1,
                    updatedAt = "2026-08-30"
                )
                repository.updateChapter(updated)
            }
        }
    }

    fun addMockTest(mockTest: MockTest) {
        viewModelScope.launch {
            repository.addMockTest(mockTest)
        }
    }

    fun markAdminAttendanceBatch(studentStatusMap: Map<String, String>, date: String = todayDateStr) {
        viewModelScope.launch {
            val records = studentStatusMap.map { (sId, status) ->
                AttendanceRecord(
                    studentId = sId,
                    date = date,
                    status = status,
                    remarks = "प्रशिक्षक द्वारा सत्यापित"
                )
            }
            repository.markBatchAttendance(records)
        }
    }

    // ==========================================
    // Phase 3A: Firebase Cloud Sync & Auth Flows
    // ==========================================

    val cloudSyncStatus: StateFlow<com.example.data.cloud.CloudSyncStatus> = repository.cloudSyncStatus
    val cloudAuthState: StateFlow<com.example.data.cloud.CloudAuthState> = repository.cloudAuthState

    fun triggerCloudSync() {
        viewModelScope.launch {
            repository.syncWithCloud()
        }
    }

    fun signInWithCloudEmail(email: String, pinOrPass: String, onResult: (Boolean, String?) -> Unit = { _, _ -> }) {
        viewModelScope.launch {
            val result = repository.cloudAuthManager.signInWithEmail(email, pinOrPass)
            result.onSuccess { auth ->
                _currentRole.value = auth.role
                if (!auth.studentId.isNullOrBlank()) {
                    _activeStudentId.value = auth.studentId
                }
                _isLoggedIn.value = true
                onResult(true, null)
            }.onFailure { error ->
                onResult(false, error.message)
            }
        }
    }

    fun registerCloudUser(
        email: String,
        pass: String,
        name: String,
        role: String = com.example.data.cloud.FirestoreConstants.ROLE_STUDENT,
        studentId: String = "",
        onResult: (Boolean, String?) -> Unit = { _, _ -> }
    ) {
        viewModelScope.launch {
            val result = repository.cloudAuthManager.registerUser(email, pass, name, role, studentId)
            result.onSuccess { auth ->
                _currentRole.value = auth.role
                if (studentId.isNotBlank()) {
                    _activeStudentId.value = studentId
                }
                _isLoggedIn.value = true
                onResult(true, null)
            }.onFailure { error ->
                onResult(false, error.message)
            }
        }
    }

    fun signOutCloud() {
        repository.cloudAuthManager.signOut()
        _isLoggedIn.value = false
    }

    // ==========================================
    // Phase 2B: Live 1600m Batch Stopwatch Engine
    // ==========================================

    fun formatRaceTime(millis: Long): String {
        val totalSec = millis / 1000
        val min = totalSec / 60
        val sec = totalSec % 60
        val cs = (millis % 1000) / 10
        return String.format(Locale.getDefault(), "%02d:%02d.%02d", min, sec, cs)
    }

    /**
     * Initializes a new Race Session with selected participants.
     */
    fun setupNewRaceSession(
        batchName: String,
        distanceMeters: Int = 1600,
        participants: List<StudentProfile>,
        chestNumbers: Map<String, String> = emptyMap(),
        notes: String = ""
    ) {
        if (!RolePermissionManager.canOperateLiveStopwatch(_currentRole.value)) return

        resetMasterStopwatch()

        val raceId = "race_${UUID.randomUUID().toString().take(8)}"
        val staffName = if (RolePermissionManager.isAdmin(_currentRole.value)) "ADMIN" else "TRAINER"

        val session = RaceSession(
            raceId = raceId,
            batchName = batchName.ifBlank { "${distanceMeters}m दौड़ बैच - ${SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date())}" },
            distanceMeters = distanceMeters,
            date = todayDateStr,
            startTimeMillis = 0L,
            status = "READY",
            notes = notes,
            recordedBy = staffName,
            totalParticipants = participants.size,
            finishedCount = 0
        )

        val results = participants.mapIndexed { index, student ->
            val assignedChest = chestNumbers[student.studentId]?.ifBlank { (101 + index).toString() } ?: (101 + index).toString()
            RaceResult(
                raceId = raceId,
                studentId = student.studentId,
                studentName = student.fullName,
                chestNumber = assignedChest,
                village = student.village,
                status = "RUNNING",
                finishTimestamp = null,
                elapsedMillis = 0L,
                timeFormatted = "",
                rank = 0,
                isPersonalBest = false,
                notes = ""
            )
        }

        _activeLiveSession.value = session
        _liveParticipants.value = results
        nextRankCounter = 1
    }

    /**
     * Starts master batch stopwatch.
     */
    fun startMasterStopwatch() {
        if (!RolePermissionManager.canOperateLiveStopwatch(_currentRole.value)) return
        val currentSession = _activeLiveSession.value ?: return

        val now = System.currentTimeMillis()
        stopwatchBaseTimeMillis = now
        stopwatchAccumulatedMillis = 0L
        _stopwatchElapsedMillis.value = 0L
        _isStopwatchRunning.value = true
        _isStopwatchPaused.value = false

        _activeLiveSession.value = currentSession.copy(
            startTimeMillis = now,
            status = "RUNNING"
        )

        startStopwatchTicker()
    }

    /**
     * Pauses master stopwatch.
     */
    fun pauseMasterStopwatch() {
        if (!RolePermissionManager.canOperateLiveStopwatch(_currentRole.value)) return
        if (!_isStopwatchRunning.value || _isStopwatchPaused.value) return

        stopwatchTickerJob?.cancel()
        stopwatchAccumulatedMillis = _stopwatchElapsedMillis.value
        _isStopwatchPaused.value = true
        _activeLiveSession.value = _activeLiveSession.value?.copy(status = "PAUSED")
    }

    /**
     * Resumes master stopwatch.
     */
    fun resumeMasterStopwatch() {
        if (!RolePermissionManager.canOperateLiveStopwatch(_currentRole.value)) return
        if (!_isStopwatchRunning.value || !_isStopwatchPaused.value) return

        stopwatchBaseTimeMillis = System.currentTimeMillis()
        _isStopwatchPaused.value = false
        _activeLiveSession.value = _activeLiveSession.value?.copy(status = "RUNNING")
        startStopwatchTicker()
    }

    private fun startStopwatchTicker() {
        stopwatchTickerJob?.cancel()
        stopwatchTickerJob = viewModelScope.launch {
            while (_isStopwatchRunning.value && !_isStopwatchPaused.value) {
                val now = System.currentTimeMillis()
                _stopwatchElapsedMillis.value = stopwatchAccumulatedMillis + (now - stopwatchBaseTimeMillis)
                delay(30) // ~33 FPS smooth display
            }
        }
    }

    /**
     * Resets stopwatch and clears current live race session.
     */
    fun resetMasterStopwatch() {
        stopwatchTickerJob?.cancel()
        stopwatchTickerJob = null
        stopwatchBaseTimeMillis = 0L
        stopwatchAccumulatedMillis = 0L
        _stopwatchElapsedMillis.value = 0L
        _isStopwatchRunning.value = false
        _isStopwatchPaused.value = false
        _activeLiveSession.value = null
        _liveParticipants.value = emptyList()
        nextRankCounter = 1
    }

    /**
     * Records finish for an individual cadet.
     * High tactile speed, accurate milliseconds, rank assignment.
     */
    fun recordCadetFinish(studentId: String) {
        if (!RolePermissionManager.canOperateLiveStopwatch(_currentRole.value)) return
        if (!_isStopwatchRunning.value) return

        val currentElapsed = _stopwatchElapsedMillis.value
        val finishTimeFormatted = formatRaceTime(currentElapsed)
        val session = _activeLiveSession.value ?: return

        val currentList = _liveParticipants.value.toMutableList()
        val index = currentList.indexOfFirst { it.studentId == studentId }
        if (index == -1) return

        val existing = currentList[index]
        if (existing.status == "FINISHED") return // already finished

        // Check personal best
        val student = allStudents.value.find { it.studentId == studentId }
        val isPb = checkIfPersonalBest(student, session.distanceMeters, currentElapsed)

        val updatedResult = existing.copy(
            status = "FINISHED",
            finishTimestamp = System.currentTimeMillis(),
            elapsedMillis = currentElapsed,
            timeFormatted = finishTimeFormatted,
            rank = nextRankCounter++,
            isPersonalBest = isPb,
            updatedAt = System.currentTimeMillis()
        )

        currentList[index] = updatedResult
        _liveParticipants.value = currentList

        val finishedCount = currentList.count { it.status == "FINISHED" }
        _activeLiveSession.value = session.copy(
            finishedCount = finishedCount,
            updatedAt = System.currentTimeMillis()
        )
    }

    /**
     * Undoes accidental finish tap for a cadet.
     */
    fun undoCadetFinish(studentId: String) {
        if (!RolePermissionManager.canOperateLiveStopwatch(_currentRole.value)) return

        val currentList = _liveParticipants.value.toMutableList()
        val index = currentList.indexOfFirst { it.studentId == studentId }
        if (index == -1) return

        val existing = currentList[index]
        if (existing.status != "FINISHED") return

        val reverted = existing.copy(
            status = "RUNNING",
            finishTimestamp = null,
            elapsedMillis = 0L,
            timeFormatted = "",
            rank = 0,
            isPersonalBest = false,
            updatedAt = System.currentTimeMillis()
        )

        currentList[index] = reverted

        // Recalculate ranks for remaining finishers
        var rank = 1
        val sortedList = currentList.map { item ->
            if (item.status == "FINISHED") {
                item.copy(rank = rank++)
            } else {
                item
            }
        }

        nextRankCounter = rank
        _liveParticipants.value = sortedList

        val finishedCount = sortedList.count { it.status == "FINISHED" }
        _activeLiveSession.value = _activeLiveSession.value?.copy(
            finishedCount = finishedCount,
            updatedAt = System.currentTimeMillis()
        )
    }

    /**
     * Marks a cadet as DNF (Did Not Finish).
     */
    fun markCadetDnf(studentId: String) {
        if (!RolePermissionManager.canOperateLiveStopwatch(_currentRole.value)) return

        val currentList = _liveParticipants.value.toMutableList()
        val index = currentList.indexOfFirst { it.studentId == studentId }
        if (index == -1) return

        val updated = currentList[index].copy(
            status = "DNF",
            rank = 0,
            updatedAt = System.currentTimeMillis()
        )
        currentList[index] = updated
        _liveParticipants.value = currentList
    }

    /**
     * Allows coach to manually edit a finish time if needed.
     */
    fun editCadetFinishTime(studentId: String, newElapsedMillis: Long) {
        if (!RolePermissionManager.canOperateLiveStopwatch(_currentRole.value)) return

        val currentList = _liveParticipants.value.toMutableList()
        val index = currentList.indexOfFirst { it.studentId == studentId }
        if (index == -1) return

        val session = _activeLiveSession.value ?: return
        val student = allStudents.value.find { it.studentId == studentId }
        val isPb = checkIfPersonalBest(student, session.distanceMeters, newElapsedMillis)

        val updated = currentList[index].copy(
            status = "FINISHED",
            elapsedMillis = newElapsedMillis,
            timeFormatted = formatRaceTime(newElapsedMillis),
            isPersonalBest = isPb,
            updatedAt = System.currentTimeMillis()
        )
        currentList[index] = updated

        // Re-sort finishers by elapsedMillis and reassign ranks
        val finishers = currentList.filter { it.status == "FINISHED" }.sortedBy { it.elapsedMillis }
        val others = currentList.filter { it.status != "FINISHED" }

        val rankedFinishers = finishers.mapIndexed { idx, item -> item.copy(rank = idx + 1) }
        nextRankCounter = rankedFinishers.size + 1

        val finalParticipants = mutableListOf<RaceResult>()
        currentList.forEach { orig ->
            val ranked = rankedFinishers.find { it.studentId == orig.studentId }
            finalParticipants.add(ranked ?: orig)
        }

        _liveParticipants.value = finalParticipants
    }

    /**
     * Stops and saves the entire live race session into local Room database,
     * enqueues offline outbox sync, and automatically updates student training logs/records.
     */
    fun finishAndSaveLiveRace(
        autoUpdateCadetProfiles: Boolean = true,
        onSaved: () -> Unit = {}
    ) {
        if (!RolePermissionManager.canOperateLiveStopwatch(_currentRole.value)) return
        val session = _activeLiveSession.value ?: return

        stopwatchTickerJob?.cancel()
        _isStopwatchRunning.value = false
        _isStopwatchPaused.value = false

        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val participants = _liveParticipants.value.map { item ->
                if (item.status == "RUNNING") {
                    item.copy(status = "DNF", rank = 0, updatedAt = now)
                } else {
                    item.copy(updatedAt = now)
                }
            }

            val finishedCount = participants.count { it.status == "FINISHED" }
            val completedSession = session.copy(
                status = "COMPLETED",
                endTimeMillis = now,
                finishedCount = finishedCount,
                updatedAt = now
            )

            // 1. Save to Room database
            repository.saveRaceSession(completedSession)
            repository.saveRaceResults(participants)

            // 2. Queue for Cloud Sync
            repository.cloudSyncManager.queueSync(
                entityType = com.example.data.cloud.SyncEntityType.RACE_SESSION,
                localRecordId = completedSession.raceId,
                firestoreDocId = "race_${completedSession.raceId}"
            )

            participants.forEach { result ->
                repository.cloudSyncManager.queueSync(
                    entityType = com.example.data.cloud.SyncEntityType.RACE_RESULT,
                    localRecordId = result.id.toString(),
                    firestoreDocId = "result_${result.raceId}_${result.studentId}",
                    studentId = result.studentId
                )
            }

            // 3. Auto-update Cadet Profiles and Daily Training Records
            if (autoUpdateCadetProfiles) {
                participants.filter { it.status == "FINISHED" }.forEach { result ->
                    val student = allStudents.value.find { it.studentId == result.studentId }
                    if (student != null) {
                        // Create Training Record
                        val trainingRecord = TrainingRecord(
                            studentId = result.studentId,
                            date = todayDateStr,
                            runningDistanceKm = completedSession.distanceMeters / 1000.0,
                            runningDuration = result.timeFormatted.substringBefore("."), // e.g. "05:14"
                            runningType = "${completedSession.distanceMeters}m दौड़ ट्रायल (Rank #${result.rank})",
                            pushups = student.pushups,
                            situps = student.situps,
                            pullups = student.pullups,
                            squats = student.squats,
                            plankSeconds = student.plankSeconds,
                            stretchingDone = true,
                            trainerNotes = "बैच: ${completedSession.batchName}, रैंक: #${result.rank}, समय: ${result.timeFormatted}",
                            timestamp = now
                        )
                        repository.recordTraining(trainingRecord)

                        // Update StudentProfile best time if better
                        val updatedStudent = updateStudentBestTime(student, completedSession.distanceMeters, result.elapsedMillis, result.timeFormatted)
                        if (updatedStudent != null) {
                            repository.updateStudent(updatedStudent)
                        }
                    }
                }
            }

            // Reset in-memory session
            _activeLiveSession.value = null
            _liveParticipants.value = emptyList()
            _stopwatchElapsedMillis.value = 0L

            onSaved()
        }
    }

    /**
     * Deletes a race session and its results.
     */
    fun deleteRaceSession(session: RaceSession) {
        if (!RolePermissionManager.isAdmin(_currentRole.value) && !RolePermissionManager.isTrainer(_currentRole.value)) return
        viewModelScope.launch {
            repository.deleteRaceSession(session)
        }
    }

    private fun checkIfPersonalBest(student: StudentProfile?, distanceMeters: Int, elapsedMillis: Long): Boolean {
        if (student == null) return false
        val currentBestStr = when (distanceMeters) {
            400 -> student.time400m
            800 -> student.time800m
            1600 -> student.time1600m
            5000 -> student.time5km
            else -> student.time1600m
        }
        val currentBestMillis = parseTimeToMillis(currentBestStr)
        return currentBestMillis == 0L || elapsedMillis < currentBestMillis
    }

    private fun updateStudentBestTime(
        student: StudentProfile,
        distanceMeters: Int,
        elapsedMillis: Long,
        timeFormatted: String
    ): StudentProfile? {
        val shortTime = timeFormatted.substringBefore(".")
        return when (distanceMeters) {
            400 -> {
                val cur = parseTimeToMillis(student.time400m)
                if (cur == 0L || elapsedMillis < cur) student.copy(time400m = shortTime) else null
            }
            800 -> {
                val cur = parseTimeToMillis(student.time800m)
                if (cur == 0L || elapsedMillis < cur) student.copy(time800m = shortTime) else null
            }
            1600 -> {
                val cur = parseTimeToMillis(student.time1600m)
                if (cur == 0L || elapsedMillis < cur) student.copy(time1600m = shortTime) else null
            }
            5000 -> {
                val cur = parseTimeToMillis(student.time5km)
                if (cur == 0L || elapsedMillis < cur) student.copy(time5km = shortTime) else null
            }
            else -> null
        }
    }

    private fun parseTimeToMillis(timeStr: String): Long {
        if (timeStr.isBlank()) return 0L
        val parts = timeStr.trim().split(":")
        return try {
            if (parts.size == 2) {
                val min = parts[0].toLong()
                val secParts = parts[1].split(".")
                val sec = secParts[0].toLong()
                val ms = if (secParts.size > 1) secParts[1].padEnd(3, '0').take(3).toLong() else 0L
                (min * 60 + sec) * 1000 + ms
            } else 0L
        } catch (e: Exception) {
            0L
        }
    }

    // ==========================================
    // Phase 4A: Trainer -> Student Assignment & Batch Management
    // ==========================================
    fun assignTrainerAndBatch(
        studentId: String,
        trainerId: String,
        trainerName: String,
        batchName: String
    ) {
        if (!RolePermissionManager.isAdmin(_currentRole.value) && !RolePermissionManager.isTrainer(_currentRole.value)) return

        viewModelScope.launch {
            val student = allStudents.value.find { it.studentId == studentId } ?: return@launch
            val updated = student.copy(
                assignedTrainerId = trainerId,
                assignedTrainerName = trainerName,
                batchName = batchName
            )
            repository.updateStudent(updated)
            repository.cloudSyncManager.queueSync(
                entityType = com.example.data.cloud.SyncEntityType.STUDENT,
                localRecordId = studentId,
                firestoreDocId = studentId,
                studentId = studentId
            )
        }
    }

    // ==========================================
    // Phase 4A: Direct Ground Performance Test Recording by Coach
    // ==========================================
    fun recordGroundPhysicalPerformance(
        studentId: String,
        time1600m: String,
        pushups: Int,
        situps: Int,
        pullups: Int,
        longJumpFeet: Double,
        highJumpFeet: Double,
        shotPutMeters: Double,
        coachNotes: String
    ) {
        if (!RolePermissionManager.isAdmin(_currentRole.value) && !RolePermissionManager.isTrainer(_currentRole.value)) return

        viewModelScope.launch {
            val student = allStudents.value.find { it.studentId == studentId } ?: return@launch
            val updatedStudent = student.copy(
                time1600m = time1600m.ifBlank { student.time1600m },
                pushups = pushups,
                situps = situps,
                pullups = pullups,
                longJumpFeet = longJumpFeet,
                highJumpFeet = highJumpFeet,
                shotPutMeters = shotPutMeters
            )
            repository.updateStudent(updatedStudent)

            // Also record a daily training record
            val trainingRecord = TrainingRecord(
                studentId = studentId,
                date = todayDateStr,
                runningDistanceKm = 1.6,
                runningDuration = time1600m.ifBlank { "05:30" },
                runningType = "1600m ग्राउंड टेस्ट",
                pushups = pushups,
                situps = situps,
                pullups = pullups,
                squats = student.squats,
                plankSeconds = student.plankSeconds,
                stretchingDone = true,
                trainerNotes = coachNotes.ifBlank { "कोच द्वारा सत्यापित ग्राउंड शारीरिक परीक्षण" },
                timestamp = System.currentTimeMillis()
            )
            repository.recordTraining(trainingRecord)

            // Queue for cloud sync
            repository.cloudSyncManager.queueSync(
                entityType = com.example.data.cloud.SyncEntityType.STUDENT,
                localRecordId = studentId,
                firestoreDocId = studentId,
                studentId = studentId
            )
            repository.cloudSyncManager.queueSync(
                entityType = com.example.data.cloud.SyncEntityType.TRAINING_RECORD,
                localRecordId = "${studentId}_$todayDateStr",
                firestoreDocId = "tr_${studentId}_$todayDateStr",
                studentId = studentId
            )
        }
    }

    // ==========================================
    // PHASE 5B: COMMUNICATION & NOTIFICATION ACTIONS
    // ==========================================

    /**
     * Broadcasts / dispatches a new communication notification.
     * Enforces RBAC:
     * - Admin can send announcements, notices, recruitment alerts, training reminders, study reminders to all or targeted.
     * - Trainer can send to their assigned batch or student list.
     * - Students are restricted from creating broadcast communications.
     */
    fun sendCommunicationNotification(
        title: String,
        message: String,
        category: String,
        targetType: String,
        targetStudentIds: List<String> = emptyList(),
        targetBatch: String = "",
        targetTrainerId: String = "",
        actionRoute: String = "dashboard",
        isUrgent: Boolean = false,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        val currentRoleStr = _currentRole.value
        val isAdmin = RolePermissionManager.isAdmin(currentRoleStr)
        val isTrainer = RolePermissionManager.isTrainer(currentRoleStr)

        if (!isAdmin && !isTrainer) {
            onError("केवल एडमिन और प्रशिक्षक ही संदेश या सूचना प्रसारित कर सकते हैं (Unauthorized).")
            return
        }

        if (title.isBlank() || message.isBlank()) {
            onError("शीर्षक और संदेश विवरण अनिवार्य हैं।")
            return
        }

        val senderName = if (isAdmin) "अखाड़ा मुख्य संचालक (Admin)" else "कोच / प्रशिक्षक (Coach)"
        val senderRole = if (isAdmin) "ADMIN" else "TRAINER"

        // Restrict trainer targeting
        val finalTargetType = if (isTrainer && !isAdmin) {
            if (targetStudentIds.isNotEmpty()) AppNotification.TARGET_SELECTED_STUDENTS else AppNotification.TARGET_TRAINER_GROUP
        } else {
            targetType
        }

        val notifId = "notif_${System.currentTimeMillis()}_${(1000..9999).random()}"
        val newNotification = AppNotification(
            notificationId = notifId,
            title = title.trim(),
            message = message.trim(),
            category = category,
            senderId = if (isTrainer) "TR-001" else "ADMIN",
            senderName = senderName,
            senderRole = senderRole,
            targetType = finalTargetType,
            targetStudentIds = targetStudentIds.joinToString(","),
            targetBatch = targetBatch,
            targetTrainerId = targetTrainerId,
            actionRoute = actionRoute,
            isUrgent = isUrgent,
            timestamp = System.currentTimeMillis(),
            dateFormatted = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale("hi", "IN")).format(Date()),
            isRead = false
        )

        viewModelScope.launch {
            try {
                repository.insertNotification(newNotification)
                // Queue for cloud sync
                repository.cloudSyncManager.queueSync(
                    entityType = com.example.data.cloud.SyncEntityType.NOTIFICATION,
                    localRecordId = notifId,
                    firestoreDocId = notifId
                )
                // Attempt instant upload if online
                repository.uploadNotificationToCloud(newNotification)
                onSuccess()
            } catch (e: Exception) {
                onError(e.localizedMessage ?: "सूचना भेजने में त्रुटि हुई")
            }
        }
    }

    fun markNotificationAsRead(notifId: String) {
        viewModelScope.launch {
            repository.markNotificationAsRead(notifId)
        }
    }

    fun markAllNotificationsAsRead() {
        viewModelScope.launch {
            repository.markAllNotificationsAsRead()
        }
    }

    fun deleteNotification(notifId: String) {
        if (!RolePermissionManager.isAdmin(_currentRole.value)) return
        viewModelScope.launch {
            repository.deleteNotification(notifId)
            repository.cloudSyncManager.queueSync(
                entityType = com.example.data.cloud.SyncEntityType.NOTIFICATION,
                localRecordId = notifId,
                firestoreDocId = notifId,
                operation = com.example.data.cloud.SyncOperation.DELETE
            )
            repository.deleteNotificationFromCloud(notifId)
        }
    }

    // ==========================================
    // PHASE 5B: REAL PHONE OTP AUTHENTICATION
    // ==========================================

    fun sendPhoneOtp(
        activity: android.app.Activity,
        phoneNumber: String,
        onCodeSent: (verificationId: String) -> Unit,
        onAutoVerified: () -> Unit,
        onError: (String) -> Unit
    ) {
        repository.cloudAuthManager.sendPhoneVerificationCode(
            activity = activity,
            rawPhoneNumber = phoneNumber,
            onCodeSent = onCodeSent,
            onAutoVerified = { auth ->
                _isLoggedIn.value = true
                if (auth.studentId != null) {
                    _activeStudentId.value = auth.studentId
                }
                onAutoVerified()
            },
            onVerificationFailed = onError
        )
    }

    fun verifyPhoneOtp(
        verificationId: String,
        otpCode: String,
        fullName: String = "",
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            val result = repository.cloudAuthManager.signInWithPhoneOtp(verificationId, otpCode, fullName)
            result.onSuccess { auth ->
                _isLoggedIn.value = true
                if (auth.studentId != null) {
                    _activeStudentId.value = auth.studentId
                }
                onSuccess()
            }.onFailure { err ->
                onError(err.localizedMessage ?: "OTP सत्यापन विफल रहा।")
            }
        }
    }
}
