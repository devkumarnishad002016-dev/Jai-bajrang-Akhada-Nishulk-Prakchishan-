package com.example

import com.example.data.model.StudyTopic
import com.example.data.model.TopicDocument
import com.example.util.RolePermissionManager
import com.example.util.TopicFileUtils
import org.junit.Assert.*
import org.junit.Test

class TopicCmsAndFileUploadTest {

    @Test
    fun testRolePermissionsForTopicManagement() {
        // Admin must be permitted
        assertTrue(RolePermissionManager.canManageStudyMaterials("ADMIN"))
        assertTrue(RolePermissionManager.isAdmin("ADMIN"))

        // Trainer must also be permitted to manage study materials & topic documents
        assertTrue(RolePermissionManager.canManageStudyMaterials("TRAINER"))

        // Student must NOT have administrative rights
        assertFalse(RolePermissionManager.canManageStudyMaterials("STUDENT"))
    }

    @Test
    fun testTopicDocumentModelProperties() {
        val pdfDoc = TopicDocument(
            topicId = "TOPIC_NUMBER_SYSTEM",
            subjectId = "SUB_MATH",
            title = "संख्या पद्धति विस्तृत फॉर्मूला नोट्स",
            fileType = "PDF",
            fileName = "number_system_notes.pdf",
            filePath = "sample/path/number_system_notes.pdf",
            fileSize = "512 KB",
            description = "14 पृष्ठ विस्तृत नोट्स"
        )

        assertTrue(pdfDoc.isPdf)
        assertFalse(pdfDoc.isExcel)
        assertEquals("PDF", pdfDoc.fileType)
        assertEquals("512 KB", pdfDoc.fileSize)

        val excelDoc = TopicDocument(
            topicId = "TOPIC_PERCENTAGE",
            subjectId = "SUB_MATH",
            title = "प्रतिशत शॉर्टकट ट्रिक्स व अभ्यास शीट",
            fileType = "EXCEL",
            fileName = "percentage_shortcuts.xlsx",
            filePath = "sample/path/percentage_shortcuts.xlsx",
            fileSize = "128 KB",
            description = "45 पंक्तियां प्रश्न बैंक"
        )

        assertFalse(excelDoc.isPdf)
        assertTrue(excelDoc.isExcel)
        assertEquals("128 KB", excelDoc.fileSize)
    }

    @Test
    fun testStudyTopicCRUDModel() {
        val originalTopic = StudyTopic(
            topicId = "TOPIC_ANALOGY",
            subjectId = "SUB_REASONING",
            topicName = "सादृश्यता परीक्षण (Analogy)",
            displayOrder = 1,
            isActive = true
        )

        assertEquals("सादृश्यता परीक्षण (Analogy)", originalTopic.topicName)
        assertTrue(originalTopic.isActive)

        // Simulate Edit/Update
        val updatedTopic = originalTopic.copy(
            topicName = "सादृश्यता परीक्षण - ट्रिक्स व अभ्यास",
            isActive = false,
            displayOrder = 2
        )

        assertEquals("सादृश्यता परीक्षण - ट्रिक्स व अभ्यास", updatedTopic.topicName)
        assertFalse(updatedTopic.isActive)
        assertEquals(2, updatedTopic.displayOrder)
    }

    @Test
    fun testSpreadsheetRowParsing() {
        val csvData = """
            क्र.,प्रश्न / विषय,उत्तर / नियम,टिप्पणी
            1,संख्या पद्धति,स्थानीय मान व जातीय मान,महत्वपूर्ण
            2,इकाई अंक,चक्रीयता नियम (2,3,7,8),अवश्य याद रखें
        """.trimIndent()

        val parsedRows = TopicFileUtils.parseSpreadsheetText(csvData)
        assertEquals(3, parsedRows.size)
        assertEquals(4, parsedRows[0].size)
        assertEquals("संख्या पद्धति", parsedRows[1][1].trim())
        assertEquals("इकाई अंक", parsedRows[2][1].trim())
    }
}
