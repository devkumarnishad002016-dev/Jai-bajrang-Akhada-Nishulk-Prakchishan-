package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "study_subjects",
    indices = [
        Index(value = ["subjectId"], unique = true)
    ]
)
data class StudySubject(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subjectId: String, // e.g. "SUB_MATH", "SUB_REASONING", "SUB_HINDI", "SUB_ENGLISH", "SUB_GK", "SUB_GS", "SUB_CURRENT_AFFAIRS"
    val name: String, // गणित, रीजनिंग, हिंदी, English, सामान्य ज्ञान, सामान्य विज्ञान, करेंट अफेयर्स
    val icon: String = "📚",
    val displayOrder: Int = 1,
    val isActive: Boolean = true
)

@Entity(
    tableName = "study_topics",
    indices = [
        Index(value = ["topicId"], unique = true),
        Index(value = ["subjectId"])
    ]
)
data class StudyTopic(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val topicId: String, // e.g. "TOPIC_MATH_NUMSYS"
    val subjectId: String, // references StudySubject.subjectId
    val topicName: String,
    val displayOrder: Int = 1,
    val isActive: Boolean = true,
    val description: String = ""
)

@Entity(
    tableName = "chapters",
    indices = [
        Index(value = ["subjectName", "chapterNumber", "chapterName"]),
        Index(value = ["subjectName", "isPublished"]),
        Index(value = ["displayOrder"])
    ]
)
data class Chapter(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subjectName: String, // e.g. "करेंट अफेयर्स / सॉल्व्ड पेपर", "सामान्य बुद्धिमत्ता एवं तर्कशक्ति", "सामान्य ज्ञान एवं सामान्य जागरूकता", "प्रारम्भिक अंकगणित", "सामान्य हिन्दी", "प्रैक्टिस सेट"
    val chapterNumber: Int = 1,
    val chapterName: String,
    val chapterOrder: Int = 1,
    val description: String = "",
    val notesContent: String = "",
    val fileUrl: String = "", // PDF / Document path or remote URL
    val fileName: String = "",
    val examType: String = "All Defence & Police Exams", // e.g. "Indian Army", "CG Police", "SSC GD", "All Defence & Police Exams"
    val language: String = "Hindi", // Hindi, Bilingual, English
    val isPublished: Boolean = true,
    val displayOrder: Int = 1,
    val createdAt: String = "2026-08-30",
    val updatedAt: String = "2026-08-30",
    val videoTitle: String = "",
    val videoUrl: String = "",
    val practiceQuestionsCount: Int = 20,
    val isCompleted: Boolean = false,
    val progressPercentage: Int = 0,
    val isTodayTarget: Boolean = false
) {
    val effectiveOrder: Int get() = if (displayOrder > 0) displayOrder else chapterNumber
}

object ChapterValidator {
    data class ValidationResult(val isValid: Boolean, val errorMessage: String = "")

    fun validate(
        subjectName: String,
        chapterNumber: Int,
        chapterName: String
    ): ValidationResult {
        if (subjectName.trim().isEmpty()) {
            return ValidationResult(false, "कृपया विषय (Subject) का चयन करें।")
        }
        if (chapterNumber <= 0) {
            return ValidationResult(false, "कृपया वैध अध्याय क्रमांक (Chapter Number) दर्ज करें।")
        }
        if (chapterName.trim().isEmpty()) {
            return ValidationResult(false, "कृपया अध्याय का नाम (Chapter Name) दर्ज करें।")
        }
        return ValidationResult(true)
    }

    fun validate(chapter: Chapter): ValidationResult =
        validate(chapter.subjectName, chapter.chapterNumber, chapter.chapterName)
}

@Entity(
    tableName = "questions",
    indices = [
        Index(value = ["questionId"]),
        Index(value = ["subjectId"]),
        Index(value = ["topicId"])
    ]
)
data class Question(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val questionId: String = "", // Unique e.g. "Q_MATH_001"
    val subjectId: String = "", // Foreign key/reference to StudySubject.subjectId
    val topicId: String = "", // Foreign key/reference to StudyTopic.topicId
    val chapterId: Long = 0,
    val subjectName: String = "",
    val chapterName: String = "",
    val questionText: String,
    val optionA: String,
    val optionB: String,
    val optionC: String,
    val optionD: String,
    val correctOption: Int = 0, // 0 = A, 1 = B, 2 = C, 3 = D
    val correctOptionLetter: String = "A", // "A", "B", "C", "D"
    val explanation: String = "",
    val difficulty: String = "मध्यम", // आसान, मध्यम, कठिन
    val language: String = "Hindi", // Hindi, English, Bilingual
    val isActive: Boolean = true,
    val createdDate: String = "",
    val timestamp: Long = System.currentTimeMillis()
) {
    val questionTextHindi: String get() = questionText
    val explanationHindi: String get() = explanation
    val correctLetter: String get() = if (correctOptionLetter.isNotBlank()) correctOptionLetter.trim().uppercase() else when (correctOption) {
        0 -> "A"
        1 -> "B"
        2 -> "C"
        3 -> "D"
        else -> "A"
    }
}

@Entity(
    tableName = "study_attempts",
    indices = [
        Index(value = ["studentId"]),
        Index(value = ["questionId"]),
        Index(value = ["subjectId"]),
        Index(value = ["topicId"]),
        Index(value = ["studentId", "attemptDate"])
    ]
)
data class StudyAttempt(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentId: String,
    val questionId: String,
    val subjectId: String,
    val topicId: String,
    val selectedAnswer: String, // "A", "B", "C", "D"
    val isCorrect: Boolean,
    val timeTakenSeconds: Int = 0,
    val attemptDate: String, // YYYY-MM-DD
    val timestamp: Long = System.currentTimeMillis()
)

object QuestionValidator {
    data class ValidationResult(val isValid: Boolean, val errorMessage: String = "")

    fun validate(
        questionText: String,
        optionA: String,
        optionB: String,
        optionC: String,
        optionD: String,
        correctOption: String,
        subjectId: String,
        topicId: String
    ): ValidationResult {
        if (questionText.trim().isEmpty()) {
            return ValidationResult(false, "प्रश्न विवरण (Question text) खाली नहीं हो सकता।")
        }
        if (optionA.trim().isEmpty() || optionB.trim().isEmpty() || optionC.trim().isEmpty() || optionD.trim().isEmpty()) {
            return ValidationResult(false, "चारों विकल्प (Options A, B, C, D) दर्ज करना अनिवार्य है।")
        }
        val validOptions = listOf("A", "B", "C", "D")
        if (correctOption.trim().uppercase() !in validOptions) {
            return ValidationResult(false, "सही उत्तर केवल A, B, C या D में से होना चाहिए।")
        }
        if (subjectId.trim().isEmpty()) {
            return ValidationResult(false, "कृपया संबंधित विषय (Subject) का चयन करें।")
        }
        if (topicId.trim().isEmpty()) {
            return ValidationResult(false, "कृपया संबंधित टॉपिक (Topic) का चयन करें।")
        }
        return ValidationResult(true)
    }
}

@Entity(tableName = "mock_tests")
data class MockTest(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val targetExam: String, // Indian Army, CG Police, SSC GD, General Competition
    val totalQuestions: Int = 20,
    val totalMarks: Int = 40,
    val durationMinutes: Int = 30,
    val negativeMarking: Double = 0.5,
    val description: String = ""
)

@Entity(tableName = "test_attempts")
data class TestAttempt(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val testId: Long,
    val testTitle: String,
    val targetExam: String,
    val studentId: String,
    val date: String,
    val totalQuestions: Int,
    val attemptedCount: Int,
    val correctCount: Int,
    val wrongCount: Int,
    val unattemptedCount: Int,
    val score: Double,
    val maxScore: Double,
    val accuracyPercentage: Double,
    val timeTakenSeconds: Int,
    val rank: Int = 1,
    val percentile: Double = 94.5,
    val mathScore: String = "8/10",
    val reasoningScore: String = "9/10",
    val gkScore: String = "4/10",
    val hindiEnglishScore: String = "8/10",
    val weakArea: String = "GK / Current Affairs",
    val strongArea: String = "Reasoning & Mathematics"
)

@Entity(tableName = "notices")
data class Notice(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val content: String,
    val category: String, // Training, Examination, Recruitment, Important Announcement
    val date: String,
    val author: String = "मुख्य प्रशिक्षक (Head Trainer)",
    val isUrgent: Boolean = false,
    val priority: String = "NORMAL", // NORMAL, HIGH, URGENT
    val isPinned: Boolean = false,
    val expiryDate: String = "", // YYYY-MM-DD
    val isRead: Boolean = false
)

@Entity(tableName = "recruitment_infos")
data class RecruitmentInfo(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val recruitmentName: String,
    val organization: String,
    val eligibility: String,
    val ageLimit: String,
    val heightRequirement: String,
    val chestRequirement: String,
    val physicalTest: String,
    val writtenExam: String,
    val syllabus: String,
    val importantDocuments: String,
    val importantDates: String,
    val officialWebsiteLink: String,
    val badgeLabel: String = "New Openings",
    val category: String = "All",
    val shortDescription: String = "",
    val selectionProcess: String = "",
    val medicalRequirements: String = "",
    val preparationTips: String = ""
) {
    val title: String get() = recruitmentName
    val postName: String get() = recruitmentName
    val totalPosts: String get() = organization
    val lastDate: String get() = importantDates
    val physicalStandards: String get() = physicalTest
    val salary: String get() = "Level-3 Pay Scale"
    val officialUrl: String get() = officialWebsiteLink
}
