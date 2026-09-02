package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.db.AppDao
import com.example.data.db.AppDatabase
import com.example.data.db.DemoDataGenerator
import com.example.data.model.*
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
class Phase2BStudyAndQuestionBankTest {

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
    fun `test creating and querying study subjects`() = runBlocking {
        val subject1 = StudySubject(subjectId = "SUB_MATH", name = "गणित (Mathematics)", icon = "📐", displayOrder = 1, isActive = true)
        val subject2 = StudySubject(subjectId = "SUB_GK", name = "सामान्य ज्ञान (GK / GS)", icon = "🇮🇳", displayOrder = 2, isActive = true)
        val subject3 = StudySubject(subjectId = "SUB_TEST", name = "Test Subject (Inactive)", icon = "🧪", displayOrder = 3, isActive = false)

        dao.insertSubject(subject1)
        dao.insertSubject(subject2)
        dao.insertSubject(subject3)

        val allSubjects = dao.getAllSubjects().first()
        assertEquals(3, allSubjects.size)

        val activeSubjects = dao.getActiveSubjects().first()
        assertEquals(2, activeSubjects.size)
        assertTrue(activeSubjects.any { it.subjectId == "SUB_MATH" })
        assertTrue(activeSubjects.any { it.subjectId == "SUB_GK" })
        assertFalse(activeSubjects.any { it.subjectId == "SUB_TEST" })
    }

    @Test
    fun `test creating and querying study topics by subjectId`() = runBlocking {
        val topic1 = StudyTopic(topicId = "TOPIC_NUMSYS", subjectId = "SUB_MATH", topicName = "संख्या पद्धति", displayOrder = 1, isActive = true)
        val topic2 = StudyTopic(topicId = "TOPIC_PERCENT", subjectId = "SUB_MATH", topicName = "प्रतिशत", displayOrder = 2, isActive = true)
        val topic3 = StudyTopic(topicId = "TOPIC_HIST", subjectId = "SUB_GK", topicName = "भारतीय इतिहास", displayOrder = 1, isActive = true)

        dao.insertTopic(topic1)
        dao.insertTopic(topic2)
        dao.insertTopic(topic3)

        val mathTopics = dao.getTopicsForSubject("SUB_MATH").first()
        assertEquals(2, mathTopics.size)
        assertTrue(mathTopics.any { it.topicId == "TOPIC_NUMSYS" })
        assertTrue(mathTopics.any { it.topicId == "TOPIC_PERCENT" })

        val gkTopics = dao.getTopicsForSubject("SUB_GK").first()
        assertEquals(1, gkTopics.size)
        assertEquals("TOPIC_HIST", gkTopics[0].topicId)
    }

    @Test
    fun `test question bank CRUD and activation toggle`() = runBlocking {
        val question = Question(
            questionId = "Q_MATH_001",
            subjectId = "SUB_MATH",
            topicId = "TOPIC_NUMSYS",
            chapterId = 1,
            subjectName = "गणित",
            chapterName = "संख्या पद्धति",
            questionText = "प्रथम 10 प्राकृतिक संख्याओं का योग क्या है?",
            optionA = "45",
            optionB = "55",
            optionC = "50",
            optionD = "60",
            correctOption = 1,
            correctOptionLetter = "B",
            explanation = "n(n+1)/2 = 10 * 11 / 2 = 55",
            difficulty = "आसान",
            language = "Hindi",
            isActive = true,
            createdDate = "2026-03-30"
        )

        val rowId = dao.insertQuestion(question)
        assertTrue(rowId > 0)

        val activeQuestions = dao.getActiveQuestions().first()
        assertEquals(1, activeQuestions.size)
        assertEquals("Q_MATH_001", activeQuestions[0].questionId)
        assertEquals("B", activeQuestions[0].correctLetter)

        // Toggle activation
        val updatedQ = activeQuestions[0].copy(isActive = false)
        dao.updateQuestion(updatedQ)

        val activeAfterToggle = dao.getActiveQuestions().first()
        assertEquals(0, activeAfterToggle.size)

        val allQuestions = dao.getAllQuestions().first()
        assertEquals(1, allQuestions.size)
        assertFalse(allQuestions[0].isActive)

        // Delete question
        dao.deleteQuestion(allQuestions[0])
        val afterDelete = dao.getAllQuestions().first()
        assertEquals(0, afterDelete.size)
    }

    @Test
    fun `test question validation helper logic`() {
        val valid = QuestionValidator.validate(
            questionText = "भारत की राजधानी क्या है?",
            optionA = "मुंबई",
            optionB = "नई दिल्ली",
            optionC = "कोलकाता",
            optionD = "चेन्नई",
            correctOption = "B",
            subjectId = "SUB_GK",
            topicId = "TOPIC_GEO"
        )
        assertTrue(valid.isValid)
        assertTrue(valid.errorMessage.isEmpty())

        // Missing question text
        val invalidText = QuestionValidator.validate(
            questionText = "   ",
            optionA = "A",
            optionB = "B",
            optionC = "C",
            optionD = "D",
            correctOption = "A",
            subjectId = "SUB_GK",
            topicId = "TOPIC_GEO"
        )
        assertFalse(invalidText.isValid)
        assertEquals("प्रश्न विवरण (Question text) खाली नहीं हो सकता।", invalidText.errorMessage)

        // Missing option C
        val invalidOpt = QuestionValidator.validate(
            questionText = "Valid question text?",
            optionA = "A",
            optionB = "B",
            optionC = "",
            optionD = "D",
            correctOption = "A",
            subjectId = "SUB_GK",
            topicId = "TOPIC_GEO"
        )
        assertFalse(invalidOpt.isValid)
        assertEquals("चारों विकल्प (Options A, B, C, D) दर्ज करना अनिवार्य है।", invalidOpt.errorMessage)

        // Invalid correct option letter
        val invalidLetter = QuestionValidator.validate(
            questionText = "Valid question text?",
            optionA = "A",
            optionB = "B",
            optionC = "C",
            optionD = "D",
            correctOption = "E",
            subjectId = "SUB_GK",
            topicId = "TOPIC_GEO"
        )
        assertFalse(invalidLetter.isValid)
        assertEquals("सही उत्तर केवल A, B, C या D में से होना चाहिए।", invalidLetter.errorMessage)
    }

    @Test
    fun `test study attempts tracking and accuracy calculations per student`() = runBlocking {
        val student1 = "JBA-2026-001"
        val student2 = "JBA-2026-002"

        val attempt1 = StudyAttempt(
            studentId = student1,
            questionId = "Q_MATH_001",
            subjectId = "SUB_MATH",
            topicId = "TOPIC_NUMSYS",
            selectedAnswer = "B",
            isCorrect = true,
            timeTakenSeconds = 12,
            attemptDate = "2026-03-30"
        )
        val attempt2 = StudyAttempt(
            studentId = student1,
            questionId = "Q_MATH_002",
            subjectId = "SUB_MATH",
            topicId = "TOPIC_NUMSYS",
            selectedAnswer = "A",
            isCorrect = false,
            timeTakenSeconds = 25,
            attemptDate = "2026-03-30"
        )
        val attemptStudent2 = StudyAttempt(
            studentId = student2,
            questionId = "Q_MATH_001",
            subjectId = "SUB_MATH",
            topicId = "TOPIC_NUMSYS",
            selectedAnswer = "B",
            isCorrect = true,
            timeTakenSeconds = 15,
            attemptDate = "2026-03-30"
        )

        dao.insertStudyAttempt(attempt1)
        dao.insertStudyAttempt(attempt2)
        dao.insertStudyAttempt(attemptStudent2)

        val s1Attempts = dao.getStudyAttemptsForStudent(student1).first()
        assertEquals(2, s1Attempts.size)
        val s1Correct = s1Attempts.count { it.isCorrect }
        assertEquals(1, s1Correct)

        val s2Attempts = dao.getStudyAttemptsForStudent(student2).first()
        assertEquals(1, s2Attempts.size)
        assertTrue(s2Attempts[0].isCorrect)
    }

    @Test
    fun `test seed data population includes subjects, topics, and question bank`() = runBlocking {
        AppDatabase.populateInitialData(dao)

        val subjects = dao.getAllSubjects().first()
        assertTrue("Subjects count should be >= 5", subjects.size >= 5)

        val topics = dao.getAllTopics().first()
        assertTrue("Topics count should be >= 10", topics.size >= 10)

        val questions = dao.getAllQuestions().first()
        assertTrue("Questions count should be >= 20", questions.size >= 20)
    }

    @Test
    fun `test StudySubjectViewModel manages subjects and topics`() = runBlocking {
        val app = ApplicationProvider.getApplicationContext<android.app.Application>()
        val repo = com.example.data.repository.AppRepository(dao)
        val viewModel = com.example.ui.viewmodel.StudySubjectViewModel(app, repo)

        viewModel.addSubject(
            subjectId = "SUB_HINDI_TEST",
            name = "हिंदी व्याकरण (Hindi)",
            icon = "📖",
            displayOrder = 1,
            isActive = true
        )

        val subjects = repo.allSubjects.first()
        assertTrue(subjects.any { it.subjectId == "SUB_HINDI_TEST" })

        viewModel.addTopic(
            topicId = "TOPIC_HINDI_SANDHI",
            subjectId = "SUB_HINDI_TEST",
            topicName = "संधि एवं समास",
            description = "हिंदी व्याकरण के महत्वपूर्ण नियम",
            displayOrder = 1,
            isActive = true
        )

        val topics = repo.getTopicsForSubject("SUB_HINDI_TEST").first()
        assertTrue(topics.any { it.topicId == "TOPIC_HINDI_SANDHI" })

        val addedTopic = topics.first { it.topicId == "TOPIC_HINDI_SANDHI" }
        viewModel.toggleTopicActive(addedTopic)

        val updatedTopics = repo.getTopicsForSubject("SUB_HINDI_TEST").first()
        val toggledTopic = updatedTopics.first { it.topicId == "TOPIC_HINDI_SANDHI" }
        assertFalse(toggledTopic.isActive)
    }

    @Test
    fun `test StudyDashboard retrieves subjects and selects subject to view topics`() = runBlocking {
        AppDatabase.populateInitialData(dao)
        val app = ApplicationProvider.getApplicationContext<android.app.Application>()
        val repo = com.example.data.repository.AppRepository(dao)
        val viewModel = com.example.ui.viewmodel.StudySubjectViewModel(app, repo)

        val subjects = repo.allSubjects.first()
        assertTrue("Subjects should not be empty", subjects.isNotEmpty())

        val mathSubject = subjects.first { it.subjectId == "SUB_MATH" }
        viewModel.selectSubject(mathSubject.subjectId)

        val mathTopics = repo.getTopicsForSubject("SUB_MATH").first()
        assertTrue("Maths should have topics in Room DB", mathTopics.isNotEmpty())
        assertTrue("Should contain percentage topic", mathTopics.any { it.topicId == "TOPIC_MATH_PERCENT" })
    }
}



