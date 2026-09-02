package com.example

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.db.AppDatabase
import com.example.data.model.AppNotification
import com.example.data.model.StudentProfile
import com.example.data.repository.AppRepository
import com.example.util.RolePermissionManager
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
class Phase5BCommunicationAndAuthTest {

    private lateinit var database: AppDatabase
    private lateinit var repository: AppRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = AppRepository(database.appDao())
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun testNotificationCreationAndPersistence() = runBlocking {
        val notification = AppNotification(
            notificationId = "notif_test_101",
            title = "1600m विशेष ट्रायल सूचना",
            message = "कल सुबह 5:30 बजे मुख्य ग्राउंड पर 1600 मीटर समय मापन ट्रायल आयोजित किया जाएगा।",
            category = AppNotification.CATEGORY_TRAINING_REMINDER,
            senderRole = "ADMIN",
            senderName = "मुख्य व्यवस्थापक",
            targetType = AppNotification.TARGET_ALL_STUDENTS,
            targetBatch = "सभी बैच",
            isUrgent = true,
            isRead = false
        )

        repository.insertNotification(notification)

        val allNotifs = repository.allNotifications.first()
        assertTrue("Notification must be stored in database", allNotifs.any { it.notificationId == "notif_test_101" })

        val fetched = allNotifs.first { it.notificationId == "notif_test_101" }
        assertEquals("1600m विशेष ट्रायल सूचना", fetched.title)
        assertEquals(AppNotification.CATEGORY_TRAINING_REMINDER, fetched.category)
        assertTrue(fetched.isUrgent)
        assertFalse(fetched.isRead)
    }

    @Test
    fun testMarkNotificationAsRead() = runBlocking {
        val notification = AppNotification(
            notificationId = "notif_unread_102",
            title = "स्टडी मटेरियल अपडेट",
            message = "गणित व सामान्य ज्ञान के नए अध्याय अपलोड किए गए हैं।",
            category = AppNotification.CATEGORY_STUDY_REMINDER,
            senderRole = "TRAINER",
            senderName = "देव कुमार निषाद (कोच)",
            targetType = AppNotification.TARGET_SELECTED_STUDENTS,
            targetStudentIds = "JBA-2026-001,JBA-2026-002",
            isRead = false
        )

        repository.insertNotification(notification)
        val unreadCount = repository.allNotifications.first().count { !it.isRead }
        assertTrue("Unread count should be at least 1", unreadCount >= 1)

        repository.markNotificationAsRead("notif_unread_102")
        val updated = repository.getNotificationByIdDirect("notif_unread_102")
        assertNotNull(updated)
        assertTrue("Notification should now be marked as read", updated!!.isRead)
    }

    @Test
    fun testDeleteNotificationByAdmin() = runBlocking {
        val notification = AppNotification(
            notificationId = "notif_to_delete",
            title = "अस्थायी सूचना",
            message = "यह सूचना हटाई जाएगी।",
            category = AppNotification.CATEGORY_NOTICE,
            senderRole = "ADMIN"
        )

        repository.insertNotification(notification)
        var exists = repository.allNotifications.first().any { it.notificationId == "notif_to_delete" }
        assertTrue(exists)

        repository.deleteNotification("notif_to_delete")
        exists = repository.allNotifications.first().any { it.notificationId == "notif_to_delete" }
        assertFalse("Notification must be deleted", exists)
    }

    @Test
    fun testRoleBasedAccessTargetingRules() {
        // Admin permissions
        assertTrue(RolePermissionManager.isAdmin("ADMIN"))
        assertTrue(RolePermissionManager.isStaff("ADMIN"))

        // Trainer permissions
        assertTrue(RolePermissionManager.isTrainer("TRAINER"))
        assertTrue(RolePermissionManager.isStaff("TRAINER"))
        assertFalse(RolePermissionManager.isAdmin("TRAINER"))

        // Student permissions
        assertTrue(RolePermissionManager.isStudent("STUDENT"))
        assertFalse(RolePermissionManager.isStaff("STUDENT"))
        assertFalse(RolePermissionManager.isAdmin("STUDENT"))
    }

    @Test
    fun testNotificationCategoriesSupported() {
        val categories = listOf(
            AppNotification.CATEGORY_ANNOUNCEMENT,
            AppNotification.CATEGORY_NOTICE,
            AppNotification.CATEGORY_RECRUITMENT_ALERT,
            AppNotification.CATEGORY_TRAINING_REMINDER,
            AppNotification.CATEGORY_STUDY_REMINDER
        )

        assertEquals(5, categories.size)
        assertTrue(categories.contains("ANNOUNCEMENT"))
        assertTrue(categories.contains("NOTICE"))
        assertTrue(categories.contains("RECRUITMENT_ALERT"))
        assertTrue(categories.contains("TRAINING_REMINDER"))
        assertTrue(categories.contains("STUDY_REMINDER"))
    }
}
