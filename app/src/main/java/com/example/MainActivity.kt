package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.components.AppBottomNavigationBar
import com.example.ui.components.AppTopHeader
import com.example.ui.screens.auth.LoginScreen
import com.example.ui.screens.admin.*
import com.example.ui.screens.attendance.AttendanceScreen
import com.example.ui.screens.dashboard.DashboardScreen
import com.example.ui.screens.exam.ActiveTestScreen
import com.example.ui.screens.exam.MockTestScreen
import com.example.ui.screens.exam.TestResultScreen
import com.example.ui.screens.leaderboard.LeaderboardScreen
import com.example.ui.screens.notices.NoticeBoardScreen
import com.example.ui.screens.notices.RecruitmentInfoScreen
import com.example.ui.screens.notices.StudentNotificationsScreen
import com.example.ui.screens.monthly.MonthlyPerformanceScreen
import com.example.ui.screens.profile.StudentProfileScreen
import com.example.ui.screens.progress.ProgressAndAiCoachScreen
import com.example.ui.screens.study.StudyScreen
import com.example.ui.screens.content.*
import com.example.ui.screens.training.*
import com.example.ui.screens.workout.WorkoutScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.MainViewModel
import com.example.util.RolePermissionManager

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val isLoggedIn by viewModel.isLoggedIn.collectAsState()
                val allStudents by viewModel.allStudents.collectAsState()

                if (!isLoggedIn) {
                    LoginScreen(
                        allStudents = allStudents,
                        onStudentLogin = { id, password -> viewModel.loginAsStudent(id, password) },
                        onAdminLogin = { pin -> viewModel.loginAsAdmin(pin) },
                        onTrainerLogin = { pin -> viewModel.loginAsTrainer(pin) },
                        onRegisterStudent = { newStudent ->
                            viewModel.addStudent(newStudent)
                            viewModel.loginAsStudentDirect(newStudent.studentId)
                        },
                        onSendPhoneOtp = { activity, phone, onCodeSent, onAutoVerified, onError ->
                            viewModel.sendPhoneOtp(activity, phone, onCodeSent, onAutoVerified, onError)
                        },
                        onVerifyPhoneOtp = { vId, otp, name, onSuccess, onError ->
                            viewModel.verifyPhoneOtp(vId, otp, name, onSuccess, onError)
                        }
                    )
                } else {
                    MainApp(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun MainApp(viewModel: MainViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: "dashboard"

    val currentRole by viewModel.currentRole.collectAsState()
    val activeStudent by viewModel.activeStudent.collectAsState()
    val allStudents by viewModel.allStudents.collectAsState()
    val todayAttendance by viewModel.todayAttendance.collectAsState()
    val activeAttendance by viewModel.activeAttendance.collectAsState()
    val todayTraining by viewModel.todayTraining.collectAsState()
    val activeTrainingRecords by viewModel.activeTrainingRecords.collectAsState()
    val todayWorkout by viewModel.todayWorkout.collectAsState()
    val activeWorkouts by viewModel.activeWorkouts.collectAsState()
    val latestWorkoutPlan by viewModel.latestWorkoutPlan.collectAsState()
    val allChapters by viewModel.allChapters.collectAsState()
    val publishedChapters by viewModel.publishedChapters.collectAsState()
    val todayStudyTargets by viewModel.todayStudyTargets.collectAsState()
    val selectedSubject by viewModel.selectedSubject.collectAsState()
    val allSubjects by viewModel.allSubjects.collectAsState()
    val allTopics by viewModel.allTopics.collectAsState()
    val allQuestions by viewModel.allQuestions.collectAsState()
    val studentStudyAttempts by viewModel.studentStudyAttempts.collectAsState()
    val allMockTests by viewModel.allMockTests.collectAsState()
    val studentTestAttempts by viewModel.studentTestAttempts.collectAsState()
    val allNotices by viewModel.allNotices.collectAsState()
    val allRecruitmentInfo by viewModel.allRecruitmentInfo.collectAsState()
    val allTrainers by viewModel.allTrainers.collectAsState()
    val allGalleryItems by viewModel.allGalleryItems.collectAsState()
    val allSuccessStories by viewModel.allSuccessStories.collectAsState()
    val contactInfo by viewModel.contactInfo.collectAsState()
    val performanceScore by viewModel.performanceScore.collectAsState()
    val aiCoachInsights by viewModel.aiCoachInsights.collectAsState()
    val unifiedPerformanceState by viewModel.unifiedPerformanceState.collectAsState()
    val cloudSyncStatus by viewModel.cloudSyncStatus.collectAsState()
    val allAttendanceRecords by viewModel.allAttendanceRecords.collectAsState()
    val allTrainingRecords by viewModel.allTrainingRecords.collectAsState()
    val allNotifications by viewModel.allNotifications.collectAsState()
    val unreadNotificationsCount by viewModel.unreadNotificationsCount.collectAsState()

    // Test specific state
    val testTitle by viewModel.testTitle.collectAsState()
    val targetExam by viewModel.targetExam.collectAsState()
    val currentQuestions by viewModel.currentQuestions.collectAsState()
    val currentQuestionIndex by viewModel.currentQuestionIndex.collectAsState()
    val userAnswers by viewModel.userAnswers.collectAsState()
    val markedForReview by viewModel.markedForReview.collectAsState()
    val timeRemainingSeconds by viewModel.timeRemainingSeconds.collectAsState()
    val isTestCompleted by viewModel.isTestCompleted.collectAsState()
    val latestAttemptResult by viewModel.latestAttemptResult.collectAsState()

    val isFullScreenTest = currentRoute == "active_test"

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            if (!isFullScreenTest) {
                AppTopHeader(
                    currentRole = currentRole,
                    activeStudent = activeStudent,
                    allStudents = allStudents,
                    onRoleToggle = { role ->
                        viewModel.switchRole(role)
                        if (role == "ADMIN" || role == "TRAINER") {
                            navController.navigate("admin_dashboard") {
                                popUpTo("dashboard") { inclusive = false }
                            }
                        } else {
                            navController.navigate("dashboard") {
                                popUpTo("admin_dashboard") { inclusive = true }
                            }
                        }
                    },
                    onStudentSelect = { id ->
                        viewModel.switchActiveStudent(id)
                    },
                    onLogout = {
                        viewModel.logout()
                    },
                    unreadNotificationsCount = unreadNotificationsCount,
                    onNotificationsClick = {
                        if (currentRole == "ADMIN" || currentRole == "TRAINER") {
                            navController.navigate("admin_communication")
                        } else {
                            navController.navigate("notifications")
                        }
                    }
                )
            }
        },
        bottomBar = {
            if (!isFullScreenTest) {
                AppBottomNavigationBar(
                    currentScreen = currentRoute,
                    currentRole = currentRole,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(if (currentRole == "STUDENT") "dashboard" else "admin_dashboard") {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = if (currentRole == "STUDENT") "dashboard" else "admin_dashboard",
            modifier = Modifier.padding(innerPadding)
        ) {
            // --- Student Screens ---
            composable("dashboard") {
                DashboardScreen(
                    student = activeStudent,
                    todayAttendance = todayAttendance,
                    todayWorkout = todayWorkout,
                    latestWorkoutPlan = latestWorkoutPlan,
                    todayStudyTargets = todayStudyTargets,
                    performanceScore = performanceScore,
                    aiCoachInsights = aiCoachInsights,
                    latestNotices = allNotices,
                    latestAttempt = studentTestAttempts.firstOrNull(),
                    onNavigate = { route -> navController.navigate(route) },
                    onToggleChapter = { ch -> viewModel.toggleChapterCompletion(ch) },
                    onMarkAttendance = { st -> viewModel.markTodayAttendance(st) },
                    todayTraining = todayTraining,
                    activeAttendance = activeAttendance,
                    trainingRecordsCount = activeTrainingRecords.size
                )
            }

            composable("training") {
                TrainingScreen(
                    student = activeStudent,
                    trainingRecords = activeTrainingRecords,
                    todayTraining = todayTraining,
                    latestPlan = latestWorkoutPlan,
                    attendanceRecords = activeAttendance,
                    todayAttendance = todayAttendance,
                    onSaveTrainingRecord = { record -> viewModel.saveTrainingRecord(record) }
                )
            }

            composable("attendance") {
                AttendanceScreen(
                    student = activeStudent,
                    attendanceRecords = activeAttendance,
                    todayAttendance = todayAttendance,
                    onMarkTodayStatus = { st -> viewModel.markTodayAttendance(st) }
                )
            }

            composable("workout") {
                WorkoutScreen(
                    student = activeStudent,
                    latestPlan = latestWorkoutPlan,
                    workoutRecords = activeWorkouts,
                    todayWorkout = todayWorkout,
                    onSubmitWorkout = { runKm, time1600, push, sit, pull, sq, notes ->
                        viewModel.submitDailyWorkout(runKm, time1600, push, sit, pull, sq, notes)
                    }
                )
            }

            composable("study") {
                StudyScreen(
                    allChapters = if (RolePermissionManager.isAdmin(currentRole)) allChapters else publishedChapters,
                    allSubjects = allSubjects,
                    allTopics = allTopics,
                    allQuestions = allQuestions,
                    studentStudyAttempts = studentStudyAttempts,
                    selectedSubject = selectedSubject,
                    onSelectSubject = { sub -> viewModel.setSelectedSubject(sub) },
                    onToggleChapter = { ch -> viewModel.toggleChapterCompletion(ch) },
                    onStartChapterQuiz = { ch ->
                        viewModel.startChapterQuiz(ch)
                        navController.navigate("active_test")
                    },
                    onRecordAttempt = { qId, sId, tId, sel, isCorr, time ->
                        viewModel.recordStudyQuestionAttempt(qId, sId, tId, sel, isCorr, time)
                    }
                )
            }

            composable("mocktest") {
                MockTestScreen(
                    mockTests = allMockTests,
                    recentAttempts = studentTestAttempts,
                    onStartTest = { test ->
                        viewModel.startMockTest(test)
                        navController.navigate("active_test")
                    },
                    onStartCustomQuiz = { sub, count ->
                        viewModel.startCustomQuiz(sub, count)
                        navController.navigate("active_test")
                    },
                    onViewAttemptResult = { attempt ->
                        navController.navigate("test_result")
                    }
                )
            }

            composable("active_test") {
                ActiveTestScreen(
                    testTitle = testTitle,
                    targetExam = targetExam,
                    questions = currentQuestions,
                    currentIndex = currentQuestionIndex,
                    userAnswers = userAnswers,
                    markedForReview = markedForReview,
                    timeRemainingSeconds = timeRemainingSeconds,
                    onSelectOption = { qIdx, opt -> viewModel.selectOption(qIdx, opt) },
                    onToggleReview = { qIdx -> viewModel.toggleMarkForReview(qIdx) },
                    onNavigateQuestion = { qIdx -> viewModel.navigateToQuestion(qIdx) },
                    onNextQuestion = { viewModel.nextQuestion() },
                    onPrevQuestion = { viewModel.previousQuestion() },
                    onSubmitTest = {
                        viewModel.submitTest()
                        navController.navigate("test_result") {
                            popUpTo("active_test") { inclusive = true }
                        }
                    }
                )
            }

            composable("test_result") {
                val attempt = latestAttemptResult ?: studentTestAttempts.firstOrNull()
                if (attempt != null) {
                    TestResultScreen(
                        attempt = attempt,
                        questions = currentQuestions,
                        userAnswers = userAnswers,
                        onReattempt = {
                            navController.navigate("mocktest") {
                                popUpTo("test_result") { inclusive = true }
                            }
                        },
                        onGoToDashboard = {
                            navController.navigate("dashboard") {
                                popUpTo("test_result") { inclusive = true }
                            }
                        }
                    )
                } else {
                    DashboardScreen(
                        student = activeStudent,
                        todayAttendance = todayAttendance,
                        todayWorkout = todayWorkout,
                        latestWorkoutPlan = latestWorkoutPlan,
                        todayStudyTargets = todayStudyTargets,
                        performanceScore = performanceScore,
                        aiCoachInsights = aiCoachInsights,
                        latestNotices = allNotices,
                        latestAttempt = null,
                        onNavigate = { route -> navController.navigate(route) },
                        onToggleChapter = { ch -> viewModel.toggleChapterCompletion(ch) },
                        onMarkAttendance = { st -> viewModel.markTodayAttendance(st) }
                    )
                }
            }

            composable("progress") {
                ProgressAndAiCoachScreen(
                    student = activeStudent,
                    performanceScore = performanceScore,
                    aiCoachInsights = aiCoachInsights,
                    workouts = activeWorkouts,
                    testAttempts = studentTestAttempts,
                    studyAttempts = studentStudyAttempts,
                    unifiedState = unifiedPerformanceState,
                    onNavigateToPractice = { subId ->
                        navController.navigate("practice_quiz/$subId/ALL")
                    },
                    onNavigateToMockTest = { testId ->
                        val test = allMockTests.find { it.id == testId }
                        if (test != null) {
                            viewModel.startMockTest(test)
                            navController.navigate("active_test")
                        }
                    }
                )
            }

            composable("aicoach") {
                ProgressAndAiCoachScreen(
                    student = activeStudent,
                    performanceScore = performanceScore,
                    aiCoachInsights = aiCoachInsights,
                    workouts = activeWorkouts,
                    testAttempts = studentTestAttempts,
                    studyAttempts = studentStudyAttempts,
                    unifiedState = unifiedPerformanceState,
                    onNavigateToPractice = { subId ->
                        navController.navigate("practice_quiz/$subId/ALL")
                    },
                    onNavigateToMockTest = { testId ->
                        val test = allMockTests.find { it.id == testId }
                        if (test != null) {
                            viewModel.startMockTest(test)
                            navController.navigate("active_test")
                        }
                    }
                )
            }

            composable("leaderboard") {
                LeaderboardScreen(
                    allStudents = allStudents,
                    currentStudentId = activeStudent?.studentId ?: "JBA-2026-001",
                    onSelectStudent = { sId ->
                        viewModel.switchActiveStudent(sId)
                        navController.navigate("dashboard")
                    }
                )
            }

            composable("notices") {
                NoticeBoardScreen(
                    notices = allNotices,
                    onTogglePin = { n -> viewModel.toggleNoticePin(n) },
                    onMarkRead = { n -> viewModel.markNoticeRead(n) }
                )
            }

            composable("recruitment") {
                RecruitmentInfoScreen(
                    recruitmentList = allRecruitmentInfo,
                    student = activeStudent
                )
            }

            composable("profile") {
                StudentProfileScreen(
                    student = activeStudent,
                    onUpdateProfile = { s -> viewModel.updateStudent(s) }
                )
            }

            // --- Akhada Awareness & Public Screens ---
            composable("about_akhada") {
                AboutAkhadaScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onJoinCampaign = { navController.navigate("profile") }
                )
            }

            composable("mission") {
                MissionScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable("training_centre") {
                TrainingCentreScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onJoin = { navController.navigate("profile") }
                )
            }

            composable("trainers") {
                TrainersScreen(
                    trainers = allTrainers,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable("training_program") {
                TrainingProgramScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onGoToTraining = { navController.navigate("training") },
                    onGoToStudy = { navController.navigate("study") }
                )
            }

            composable("gallery") {
                GalleryScreen(
                    galleryItems = allGalleryItems,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable("success_stories") {
                SuccessStoriesScreen(
                    stories = allSuccessStories,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable("contact") {
                ContactScreen(
                    contactInfo = contactInfo,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // --- Admin Screens ---
            composable("admin_dashboard") {
                AdminDashboardScreen(
                    allStudents = allStudents,
                    allNotices = allNotices,
                    latestWorkoutPlan = latestWorkoutPlan,
                    currentRole = currentRole,
                    cloudSyncStatus = cloudSyncStatus,
                    allAttendanceRecords = allAttendanceRecords,
                    allTrainingRecords = allTrainingRecords,
                    onTriggerSync = { viewModel.triggerCloudSync() },
                    onAssignTrainer = { studentId, trainerId, trainerName, batchName ->
                        viewModel.assignTrainerAndBatch(studentId, trainerId, trainerName, batchName)
                    },
                    onRecordGroundTest = { studentId, time1600m, pushups, situps, pullups, longJumpFeet, highJumpFeet, shotPutMeters, coachNotes ->
                        viewModel.recordGroundPhysicalPerformance(
                            studentId, time1600m, pushups, situps, pullups, longJumpFeet, highJumpFeet, shotPutMeters, coachNotes
                        )
                    },
                    onNavigate = { route -> navController.navigate(route) },
                    onAddStudent = { student -> viewModel.addStudent(student) }
                )
            }

            composable("admin_content_cms") {
                if (RolePermissionManager.isAdmin(currentRole)) {
                    AdminContentManagementScreen(
                        trainers = allTrainers,
                        galleryItems = allGalleryItems,
                        successStories = allSuccessStories,
                        contactInfo = contactInfo,
                        onAddTrainer = { t -> viewModel.addTrainer(t) },
                        onDeleteTrainer = { t -> viewModel.deleteTrainer(t) },
                        onAddGalleryItem = { g -> viewModel.addGalleryItem(g) },
                        onDeleteGalleryItem = { g -> viewModel.deleteGalleryItem(g) },
                        onAddSuccessStory = { s -> viewModel.addSuccessStory(s) },
                        onDeleteSuccessStory = { s -> viewModel.deleteSuccessStory(s) },
                        onUpdateContactInfo = { c -> viewModel.updateContactInfo(c) },
                        onNavigateBack = { navController.popBackStack() }
                    )
                } else {
                    LaunchedEffect(Unit) {
                        navController.popBackStack()
                    }
                }
            }

            composable("admin_attendance") {
                AdminAttendanceBatchScreen(
                    allStudents = allStudents,
                    onSaveBatchAttendance = { map, date -> viewModel.markAdminAttendanceBatch(map, date) },
                    onBack = { navController.popBackStack() }
                )
            }

            composable("admin_live_stopwatch") {
                LiveBatchStopwatchScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable("admin_workout") {
                AdminWorkoutPlanScreen(
                    currentPlan = latestWorkoutPlan,
                    onSavePlan = { plan -> viewModel.createWorkoutPlan(plan) }
                )
            }

            composable("admin_study") {
                StudyScreen(
                    allChapters = allChapters,
                    allSubjects = allSubjects,
                    allTopics = allTopics,
                    allQuestions = allQuestions,
                    studentStudyAttempts = studentStudyAttempts,
                    selectedSubject = selectedSubject,
                    onSelectSubject = { sub -> viewModel.setSelectedSubject(sub) },
                    onToggleChapter = { ch -> viewModel.toggleChapterCompletion(ch) },
                    onStartChapterQuiz = { ch ->
                        viewModel.startChapterQuiz(ch)
                        navController.navigate("active_test")
                    },
                    onRecordAttempt = { qId, sId, tId, sel, isCorr, time ->
                        viewModel.recordStudyQuestionAttempt(qId, sId, tId, sel, isCorr, time)
                    }
                )
            }

            composable("admin_question_bank") {
                if (RolePermissionManager.isAdmin(currentRole)) {
                    AdminQuestionBankScreen(
                        allQuestions = allQuestions,
                        allSubjects = allSubjects,
                        allTopics = allTopics,
                        onAddQuestion = { q -> viewModel.addQuestion(q) },
                        onUpdateQuestion = { q -> viewModel.updateQuestion(q) },
                        onDeleteQuestion = { q -> viewModel.deleteQuestion(q) },
                        onToggleActive = { q -> viewModel.toggleQuestionActive(q) },
                        onAddSubject = { s -> viewModel.addSubject(s) },
                        onAddTopic = { t -> viewModel.addTopic(t) },
                        onBack = { navController.popBackStack() }
                    )
                } else {
                    LaunchedEffect(Unit) {
                        navController.popBackStack()
                    }
                }
            }

            composable("admin_study_material") {
                if (RolePermissionManager.isAdmin(currentRole)) {
                    AdminStudyMaterialScreen(
                        allChapters = allChapters,
                        onAddChapter = { ch -> viewModel.addChapter(ch) },
                        onUpdateChapter = { ch -> viewModel.updateChapter(ch) },
                        onDeleteChapter = { ch -> viewModel.deleteChapter(ch) },
                        onTogglePublish = { ch -> viewModel.toggleChapterPublish(ch) },
                        onNavigateBack = { navController.popBackStack() }
                    )
                } else {
                    LaunchedEffect(Unit) {
                        navController.popBackStack()
                    }
                }
            }

            composable("admin_notices") {
                if (RolePermissionManager.isAdmin(currentRole)) {
                    AdminNoticeAndRecruitmentScreen(
                        notices = allNotices,
                        recruitmentList = allRecruitmentInfo,
                        onPublishNotice = { title, content, cat, isUrg -> viewModel.publishNotice(title, content, cat, isUrg) },
                        onDeleteNotice = { n -> viewModel.deleteNotice(n) },
                        onAddRecruitment = { r -> viewModel.addRecruitmentInfo(r) },
                        onDeleteRecruitment = { r -> viewModel.deleteRecruitmentInfo(r) },
                        onUpdateNotice = { n -> viewModel.updateNotice(n) },
                        onTogglePinNotice = { n -> viewModel.toggleNoticePin(n) },
                        onUpdateRecruitment = { r -> viewModel.updateRecruitmentInfo(r) }
                    )
                } else {
                    LaunchedEffect(Unit) {
                        navController.popBackStack()
                    }
                }
            }

            composable("admin_security") {
                if (RolePermissionManager.isAdmin(currentRole)) {
                    AdminSecurityScreen(
                        onChangePin = { currentPin, newPin, confirmPin ->
                            viewModel.changeAdminPin(currentPin, newPin, confirmPin)
                        },
                        onBack = { navController.popBackStack() }
                    )
                } else {
                    LaunchedEffect(Unit) {
                        navController.popBackStack()
                    }
                }
            }

            // --- Phase 3: Monthly Cadet Performance, Analytics & PDF Report Card ---
            composable("monthly_performance") {
                val monthlyReport by viewModel.monthlyPerformanceReport.collectAsState()
                val selectedMonthKey by viewModel.selectedMonthKey.collectAsState()
                val activeStudentId by viewModel.activeStudentId.collectAsState()

                MonthlyPerformanceScreen(
                    monthlyReport = monthlyReport,
                    currentRole = currentRole,
                    activeStudentId = activeStudentId,
                    selectedMonthKey = selectedMonthKey,
                    onSelectMonth = { mKey -> viewModel.setSelectedMonth(mKey) },
                    onSelectPreviousMonth = { viewModel.selectPreviousMonth() },
                    onSelectNextMonth = { viewModel.selectNextMonth() },
                    onSelectCurrentMonth = { viewModel.selectCurrentMonth() },
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // --- Phase 5B: Communication & Notification Hub ---
            composable("admin_communication") {
                AdminCommunicationScreen(
                    allNotifications = allNotifications,
                    allStudents = allStudents,
                    currentRole = currentRole,
                    onSendNotification = { title, msg, cat, tType, tIds, batch, trId, route, isUrg, onSucc, onErr ->
                        viewModel.sendCommunicationNotification(
                            title = title,
                            message = msg,
                            category = cat,
                            targetType = tType,
                            targetStudentIds = tIds,
                            targetBatch = batch,
                            targetTrainerId = trId,
                            actionRoute = route,
                            isUrgent = isUrg,
                            onSuccess = onSucc,
                            onError = onErr
                        )
                    },
                    onDeleteNotification = { notifId -> viewModel.deleteNotification(notifId) },
                    onMarkAllRead = { viewModel.markAllNotificationsAsRead() },
                    onBack = { navController.popBackStack() }
                )
            }

            composable("notifications") {
                StudentNotificationsScreen(
                    studentProfile = activeStudent,
                    allNotifications = allNotifications,
                    onMarkAsRead = { notifId -> viewModel.markNotificationAsRead(notifId) },
                    onMarkAllAsRead = { viewModel.markAllNotificationsAsRead() },
                    onNavigateToRoute = { route ->
                        navController.navigate(route) {
                            popUpTo("dashboard") { saveState = true }
                            launchSingleTop = true
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
