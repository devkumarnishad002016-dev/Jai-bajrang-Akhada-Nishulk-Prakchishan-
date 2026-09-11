package com.example

import com.example.data.db.SyllabusStudyMaterialData
import com.example.data.model.Chapter
import com.example.data.model.ChapterValidator
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class StudyMaterialSystemTest {

    @Test
    fun testAllSyllabusChaptersCountAndCoverage() {
        val syllabusChapters = SyllabusStudyMaterialData.getAllSyllabusChapters()

        // Verify total exact chapters count = 78 (with 15 English chapters added)
        assertEquals("Syllabus must contain exact 78 chapters", 78, syllabusChapters.size)

        // Verify all 6 subjects are present
        val subjects = syllabusChapters.map { it.subjectName }.distinct()
        assertTrue(subjects.contains("Mathematics"))
        assertTrue(subjects.contains("Reasoning"))
        assertTrue(subjects.contains("Hindi"))
        assertTrue(subjects.contains("GK / GS"))
        assertTrue(subjects.contains("English"))
        assertTrue(subjects.contains("Computer"))

        // Verify mathematics count = 15
        val mathCount = syllabusChapters.count { it.subjectName == "Mathematics" }
        assertEquals(15, mathCount)

        // Verify reasoning count = 15
        val reasoningCount = syllabusChapters.count { it.subjectName == "Reasoning" }
        assertEquals(15, reasoningCount)

        // Verify hindi count = 12
        val hindiCount = syllabusChapters.count { it.subjectName == "Hindi" }
        assertEquals(12, hindiCount)

        // Verify GK / GS count = 16
        val gkCount = syllabusChapters.count { it.subjectName == "GK / GS" }
        assertEquals(16, gkCount)

        // Verify English count = 15
        val englishCount = syllabusChapters.count { it.subjectName == "English" }
        assertEquals(15, englishCount)

        // Verify computer count = 5
        val computerCount = syllabusChapters.count { it.subjectName == "Computer" }
        assertEquals(5, computerCount)

        // Total = 15 + 15 + 12 + 16 + 15 + 5 = 78
        assertEquals(78, mathCount + reasoningCount + hindiCount + gkCount + englishCount + computerCount)
    }

    @Test
    fun testChapterValidationRules() {
        // Valid chapter
        val validChapter = Chapter(
            subjectName = "Mathematics",
            chapterNumber = 1,
            chapterName = "संख्या पद्धति (Number System)"
        )
        val validResult = ChapterValidator.validate(validChapter)
        assertTrue(validResult.isValid)
        assertTrue(validResult.errorMessage.isEmpty())

        // Invalid subject
        val emptySubjectChapter = Chapter(
            subjectName = "",
            chapterNumber = 1,
            chapterName = "संख्या पद्धति"
        )
        val emptySubjectResult = ChapterValidator.validate(emptySubjectChapter)
        assertFalse(emptySubjectResult.isValid)
        assertTrue(emptySubjectResult.errorMessage.contains("विषय"))

        // Invalid chapter number <= 0
        val zeroChapterNum = Chapter(
            subjectName = "Mathematics",
            chapterNumber = 0,
            chapterName = "संख्या पद्धति"
        )
        val zeroNumResult = ChapterValidator.validate(zeroChapterNum)
        assertFalse(zeroNumResult.isValid)
        assertTrue(zeroNumResult.errorMessage.contains("अध्याय क्रमांक"))

        // Invalid empty chapter name
        val emptyNameChapter = Chapter(
            subjectName = "Mathematics",
            chapterNumber = 2,
            chapterName = "   "
        )
        val emptyNameResult = ChapterValidator.validate(emptyNameChapter)
        assertFalse(emptyNameResult.isValid)
        assertTrue(emptyNameResult.errorMessage.contains("अध्याय का नाम"))
    }

    @Test
    fun testDuplicateChapterDetection() {
        val existingList = listOf(
            Chapter(id = 1L, subjectName = "Mathematics", chapterNumber = 1, chapterName = "संख्या पद्धति (Number System)"),
            Chapter(id = 2L, subjectName = "Mathematics", chapterNumber = 2, chapterName = "लघुत्तम समापवर्त्य और महत्तम समापवर्तक (LCM & HCF)")
        )

        // Duplicate by number
        val dupByNumber = existingList.any {
            it.subjectName.equals("Mathematics", ignoreCase = true) && it.chapterNumber == 1
        }
        assertTrue(dupByNumber)

        // Duplicate by name
        val dupByName = existingList.any {
            it.subjectName.equals("Mathematics", ignoreCase = true) && it.chapterName.equals("संख्या पद्धति (Number System)", ignoreCase = true)
        }
        assertTrue(dupByName)

        // Same chapter number in different subject is allowed
        val reasoningCh1 = existingList.any {
            it.subjectName.equals("Reasoning", ignoreCase = true) && it.chapterNumber == 1
        }
        assertFalse(reasoningCh1)
    }

    @Test
    fun testAllSyllabusChaptersArePublishedByDefault() {
        val chapters = SyllabusStudyMaterialData.getAllSyllabusChapters()
        assertTrue(chapters.all { it.isPublished })
        assertTrue(chapters.all { it.chapterNumber > 0 })
        assertTrue(chapters.all { it.chapterName.isNotBlank() })
    }
}
