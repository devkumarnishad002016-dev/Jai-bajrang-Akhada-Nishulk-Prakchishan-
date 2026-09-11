package com.example

import android.content.Context
import android.graphics.Bitmap
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.db.AppDao
import com.example.data.db.AppDatabase
import com.example.data.model.StudentProfile
import com.example.util.AdminSecurityManager
import com.example.util.ProfileUtils
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File
import java.io.FileOutputStream

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class StudentAndAdminProfilePhotoPersistenceTest {

    private lateinit var context: Context
    private lateinit var db: AppDatabase
    private lateinit var dao: AppDao

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.appDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun testStudentAndAdminProfilePhotoDistinctPersistenceAndLifecycle() = runBlocking {
        // ----------------------------------------------------
        // Step 1: Create sample bitmaps for Student and Admin
        // ----------------------------------------------------
        val studentBitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        val adminBitmap = Bitmap.createBitmap(120, 120, Bitmap.Config.ARGB_8888)

        val studentId = "JBA-2026-001"
        val adminId = AdminSecurityManager.ADMIN_UNIQUE_ID

        // ----------------------------------------------------
        // Step 2: Save Student photo to internal storage
        // ----------------------------------------------------
        val studentSavedUri = ProfileUtils.saveBitmapToInternalStorage(context, studentBitmap, studentId)
        assertTrue("Student photo URI must not be empty", studentSavedUri.isNotBlank())
        assertTrue("Student photo must point to file:// URI", studentSavedUri.startsWith("file://"))

        // ----------------------------------------------------
        // Step 3: Save Admin photo to internal storage
        // ----------------------------------------------------
        val adminSavedUri = ProfileUtils.saveBitmapToInternalStorage(context, adminBitmap, adminId)
        assertTrue("Admin photo URI must not be empty", adminSavedUri.isNotBlank())
        assertTrue("Admin photo must point to file:// URI", adminSavedUri.startsWith("file://"))

        // Assert that student and admin have different, non-colliding file paths
        assertNotEquals("Student and Admin photos must be stored in distinct files", studentSavedUri, adminSavedUri)

        // ----------------------------------------------------
        // Step 4: Persist Student photo in Room Database
        // ----------------------------------------------------
        val student = StudentProfile(
            studentId = studentId,
            fullName = "देवकुमार निषाद",
            fatherName = "श्री रामकुमार निषाद",
            mobileNumber = "9876543210",
            village = "मौरीकला (गुफा)",
            dob = "2004-05-15",
            age = 21,
            gender = "Male",
            education = "12th Pass",
            recruitmentGoal = "Indian Army (GD)",
            joinDate = "2025-01-10",
            profilePhotoUri = studentSavedUri
        )
        dao.insertStudent(student)

        val loadedStudent = dao.getStudentDirect(studentId)
        assertNotNull("Student should be persisted in Room", loadedStudent)
        assertEquals("Student photo URI should match persisted URI", studentSavedUri, loadedStudent?.profilePhotoUri)

        // ----------------------------------------------------
        // Step 5: Persist Admin photo in Admin Security Vault
        // ----------------------------------------------------
        AdminSecurityManager.setAdminPhotoUri(context, adminSavedUri)
        val loadedAdminPhotoUri = AdminSecurityManager.getAdminPhotoUri(context)
        assertEquals("Admin photo URI should match persisted URI in security vault", adminSavedUri, loadedAdminPhotoUri)

        // ----------------------------------------------------
        // Step 6: Simulate App Restart / Role Switch / Re-login
        // ----------------------------------------------------
        // Read Student photo directly from Room
        val reloadedStudent = dao.getStudentDirect(studentId)
        assertNotNull(reloadedStudent)
        assertEquals(studentSavedUri, reloadedStudent?.profilePhotoUri)

        // Read Admin photo directly from Security Vault
        val reloadedAdminPhoto = AdminSecurityManager.getAdminPhotoUri(context)
        assertEquals(adminSavedUri, reloadedAdminPhoto)

        // Verify that physical image files exist on disk
        val studentFile = File(android.net.Uri.parse(studentSavedUri).path ?: "")
        val adminFile = File(android.net.Uri.parse(adminSavedUri).path ?: "")
        assertTrue("Physical student photo file must exist", studentFile.exists())
        assertTrue("Physical admin photo file must exist", adminFile.exists())

        // ----------------------------------------------------
        // Step 7: Update Student photo (e.g. Gallery upload)
        // ----------------------------------------------------
        val tempGalleryFile = File(context.cacheDir, "gallery_temp.jpg").apply {
            FileOutputStream(this).use { out ->
                studentBitmap.compress(Bitmap.CompressFormat.JPEG, 80, out)
            }
        }
        val galleryContentUri = android.net.Uri.fromFile(tempGalleryFile)
        val updatedStudentUri = ProfileUtils.saveUriToInternalStorage(context, galleryContentUri, studentId)
        assertTrue(updatedStudentUri.isNotBlank())

        val updatedStudent = loadedStudent!!.copy(profilePhotoUri = updatedStudentUri)
        dao.updateStudent(updatedStudent)

        val finalStudent = dao.getStudentDirect(studentId)
        assertEquals(updatedStudentUri, finalStudent?.profilePhotoUri)
        // Admin photo should remain completely untouched
        assertEquals(adminSavedUri, AdminSecurityManager.getAdminPhotoUri(context))

        // ----------------------------------------------------
        // Step 8: Remove Photo flows
        // ----------------------------------------------------
        // Remove Admin photo
        AdminSecurityManager.clearAdminPhotoUri(context)
        assertEquals("", AdminSecurityManager.getAdminPhotoUri(context))

        // Remove Student photo
        val studentNoPhoto = finalStudent!!.copy(profilePhotoUri = "")
        dao.updateStudent(studentNoPhoto)
        assertEquals("", dao.getStudentDirect(studentId)?.profilePhotoUri)
    }
}
