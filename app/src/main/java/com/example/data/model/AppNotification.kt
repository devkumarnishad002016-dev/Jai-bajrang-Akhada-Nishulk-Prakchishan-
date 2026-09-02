package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * Represents a Communication & Notification Message in the Akhada System.
 * Supports targeted messaging, role restrictions, multi-category broadcasts,
 * and offline-first Room + Firestore bi-directional synchronization.
 */
@Entity(
    tableName = "app_notifications",
    indices = [
        Index(value = ["notificationId"], unique = true),
        Index(value = ["category"]),
        Index(value = ["targetType"]),
        Index(value = ["senderId"]),
        Index(value = ["timestamp"])
    ]
)
data class AppNotification(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val notificationId: String = UUID.randomUUID().toString(),
    val title: String,
    val message: String,
    val category: String = CATEGORY_ANNOUNCEMENT, // ANNOUNCEMENT, NOTICE, RECRUITMENT_ALERT, TRAINING_REMINDER, STUDY_REMINDER
    val senderId: String = "",
    val senderName: String = "मुख्य प्रशिक्षक (Head Trainer)",
    val senderRole: String = "ADMIN", // ADMIN, TRAINER
    val targetType: String = TARGET_ALL_STUDENTS, // ALL_STUDENTS, SELECTED_STUDENTS, TRAINERS, TRAINER_GROUP
    val targetStudentIds: String = "", // Comma-separated list of student IDs e.g. "JBA-2025-001,JBA-2025-002"
    val targetBatch: String = "", // Batch name if targeted to a specific batch
    val targetTrainerId: String = "", // Trainer ID if targeted to a specific trainer or trainer's cadets
    val actionRoute: String = "", // Deep-link screen destination e.g. "ground_training", "study_material", "mock_tests", "recruitment"
    val isUrgent: Boolean = false,
    val timestamp: Long = System.currentTimeMillis(),
    val dateFormatted: String = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale("hi", "IN")).format(Date()),
    val isRead: Boolean = false,
    val readByUids: String = "" // Comma-separated list of UIDs or studentIds who have marked as read
) {
    /**
     * Checks if a given student is an authorized recipient of this notification.
     */
    fun isRecipientStudent(studentId: String?, batchName: String? = null): Boolean {
        if (targetType == TARGET_ALL_STUDENTS || targetType == "ALL") return true
        if (targetType == TARGET_SELECTED_STUDENTS) {
            val list = targetStudentIds.split(",").map { it.trim() }
            return studentId != null && list.contains(studentId)
        }
        if (targetType == TARGET_TRAINER_GROUP) {
            if (targetBatch.isNotBlank() && batchName != null && targetBatch.equals(batchName, ignoreCase = true)) {
                return true
            }
            if (studentId != null && targetStudentIds.isNotBlank()) {
                val list = targetStudentIds.split(",").map { it.trim() }
                return list.contains(studentId)
            }
        }
        return false
    }

    /**
     * Checks if a given trainer is an authorized recipient of this notification.
     */
    fun isRecipientTrainer(trainerId: String?): Boolean {
        if (targetType == TARGET_TRAINERS || targetType == "ALL") return true
        if (targetType == TARGET_TRAINER_GROUP && targetTrainerId.isNotBlank()) {
            return trainerId != null && targetTrainerId == trainerId
        }
        return false
    }

    /**
     * Category Display title in Hindi & English
     */
    val categoryLabelHindi: String
        get() = when (category) {
            CATEGORY_ANNOUNCEMENT -> "📢 घोषणा (Announcement)"
            CATEGORY_NOTICE -> "📋 आधिकारिक नोटिस (Notice)"
            CATEGORY_RECRUITMENT_ALERT -> "🎖️ भर्ती अलर्ट (Recruitment Alert)"
            CATEGORY_TRAINING_REMINDER -> "🏃 ग्राउंड ट्रेनिंग स्मरण (Training Reminder)"
            CATEGORY_STUDY_REMINDER -> "📖 अध्ययन व टेस्ट स्मरण (Study Reminder)"
            else -> "🔔 सामान्य सूचना (General)"
        }

    val targetLabelHindi: String
        get() = when (targetType) {
            TARGET_ALL_STUDENTS -> "सभी पंजीकृत कैडेट्स (All Cadets)"
            TARGET_SELECTED_STUDENTS -> "चयनित कैडेट्स (${targetStudentIds.split(',').filter { it.isNotBlank() }.size} छात्र)"
            TARGET_TRAINERS -> "सभी कोच व ट्रेनर्स (All Trainers)"
            TARGET_TRAINER_GROUP -> if (targetBatch.isNotBlank()) "बैच: $targetBatch" else "कोच ग्रुप ($targetTrainerId)"
            else -> "सभी सदस्य"
        }

    companion object {
        const val CATEGORY_ANNOUNCEMENT = "ANNOUNCEMENT"
        const val CATEGORY_NOTICE = "NOTICE"
        const val CATEGORY_RECRUITMENT_ALERT = "RECRUITMENT_ALERT"
        const val CATEGORY_TRAINING_REMINDER = "TRAINING_REMINDER"
        const val CATEGORY_STUDY_REMINDER = "STUDY_REMINDER"

        const val TARGET_ALL_STUDENTS = "ALL_STUDENTS"
        const val TARGET_SELECTED_STUDENTS = "SELECTED_STUDENTS"
        const val TARGET_TRAINERS = "TRAINERS"
        const val TARGET_TRAINER_GROUP = "TRAINER_GROUP"
    }
}
