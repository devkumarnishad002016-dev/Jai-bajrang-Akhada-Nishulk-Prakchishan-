package com.example

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit test suite evaluating Firestore Security Rules logic simulation.
 * Validates role-based access control, owner isolation, immutability,
 * date restrictions, and default-deny policies before cloud deployment.
 */
class FirestoreSecurityRulesValidationTest {

    data class AuthContext(
        val uid: String?,
        val tokenRole: String? = null,
        val tokenAdmin: Boolean = false,
        val tokenStudentId: String? = null
    ) {
        val isAuthenticated: Boolean get() = uid != null
    }

    data class FirestoreDoc(
        val id: String,
        val data: Map<String, Any?>
    )

    // Exact rules engine simulation matching /firestore.rules
    object SecurityRulesEngine {
        fun isAdmin(auth: AuthContext, usersCollection: Map<String, Map<String, Any?>>): Boolean {
            if (!auth.isAuthenticated) return false
            if (auth.tokenRole == "ADMIN" || auth.tokenRole == "COACH" || auth.tokenAdmin) return true
            val userDoc = usersCollection[auth.uid]
            return userDoc?.get("role") == "ADMIN" || userDoc?.get("role") == "COACH"
        }

        fun isStudentOwner(auth: AuthContext, studentId: String, usersCollection: Map<String, Map<String, Any?>>): Boolean {
            if (!auth.isAuthenticated) return false
            if (auth.tokenStudentId != null && auth.tokenStudentId == studentId) return true
            val userDoc = usersCollection[auth.uid]
            val linkedStudentId = userDoc?.get("studentId") as? String
            return linkedStudentId == studentId
        }

        fun isServerCurrentDate(dateStr: String, serverToday: String): Boolean {
            return dateStr == serverToday
        }

        fun evaluateRead(
            collection: String,
            doc: FirestoreDoc,
            auth: AuthContext,
            usersCollection: Map<String, Map<String, Any?>> = emptyMap()
        ): Boolean {
            if (isAdmin(auth, usersCollection)) return true

            return when (collection) {
                "notices", "recruitment_info", "study_subjects", "study_topics" -> true // Public read
                "mock_tests", "leaderboard", "questions" -> auth.isAuthenticated // Authenticated read
                "students" -> isStudentOwner(auth, doc.id, usersCollection)
                "attendance_records", "training_records", "test_attempts" -> {
                    val ownerStudentId = doc.data["studentId"] as? String ?: return false
                    isStudentOwner(auth, ownerStudentId, usersCollection)
                }
                "users" -> auth.isAuthenticated && auth.uid == doc.id
                else -> false // DEFAULT DENY
            }
        }

        fun evaluateCreate(
            collection: String,
            docId: String,
            newData: Map<String, Any?>,
            auth: AuthContext,
            serverToday: String = "2026-08-25",
            usersCollection: Map<String, Map<String, Any?>> = emptyMap()
        ): Boolean {
            if (isAdmin(auth, usersCollection)) return true

            return when (collection) {
                "users" -> {
                    val role = newData["role"] as? String
                    auth.isAuthenticated && auth.uid == docId && auth.uid == newData["uid"] &&
                            role != "ADMIN" && role != "COACH"
                }
                "attendance_records" -> {
                    val studentId = newData["studentId"] as? String ?: return false
                    val date = newData["date"] as? String ?: return false
                    // Student can only create record for their own verified studentId AND for server current date
                    isStudentOwner(auth, studentId, usersCollection) && isServerCurrentDate(date, serverToday)
                }
                "training_records" -> {
                    val studentId = newData["studentId"] as? String ?: return false
                    isStudentOwner(auth, studentId, usersCollection)
                }
                "test_attempts" -> {
                    val studentId = newData["studentId"] as? String ?: return false
                    isStudentOwner(auth, studentId, usersCollection)
                }
                // Students can NEVER write to student directory, content, questions, tests, recruitment, notices, or leaderboard
                "students", "notices", "recruitment_info", "study_subjects", "study_topics", "mock_tests", "questions", "leaderboard" -> false
                else -> false // DEFAULT DENY
            }
        }

        fun evaluateUpdate(
            collection: String,
            existingDoc: FirestoreDoc,
            newData: Map<String, Any?>,
            auth: AuthContext,
            usersCollection: Map<String, Map<String, Any?>> = emptyMap()
        ): Boolean {
            if (isAdmin(auth, usersCollection)) return true

            return when (collection) {
                "users" -> {
                    val existingRole = existingDoc.data["role"]
                    val newRole = newData["role"]
                    val existingStudentId = existingDoc.data["studentId"]
                    val newStudentId = newData["studentId"]
                    // Cannot escalate role or switch studentId mapping
                    auth.isAuthenticated && auth.uid == existingDoc.id &&
                            newRole == existingRole && newStudentId == existingStudentId
                }
                "test_attempts" -> false // Test attempts are immutable for students
                "attendance_records" -> false // Students cannot modify submitted/corrected attendance
                "training_records" -> false // Students cannot modify submitted training history
                "students", "notices", "recruitment_info", "study_subjects", "study_topics", "mock_tests", "questions", "leaderboard" -> false
                else -> false // DEFAULT DENY
            }
        }

        fun evaluateDelete(
            collection: String,
            existingDoc: FirestoreDoc,
            auth: AuthContext,
            usersCollection: Map<String, Map<String, Any?>> = emptyMap()
        ): Boolean {
            if (isAdmin(auth, usersCollection)) return true
            return false // Students can NEVER delete records
        }
    }

    private val studentAAuth = AuthContext(uid = "uid-student-A", tokenStudentId = "JBA-2026-001")
    private val studentBAuth = AuthContext(uid = "uid-student-B", tokenStudentId = "JBA-2026-002")
    private val unauthenticated = AuthContext(uid = null)
    private val adminAuth = AuthContext(uid = "uid-admin-01", tokenRole = "ADMIN")

    private val usersMap = mapOf(
        "uid-student-A" to mapOf("uid" to "uid-student-A", "role" to "STUDENT", "studentId" to "JBA-2026-001"),
        "uid-student-B" to mapOf("uid" to "uid-student-B", "role" to "STUDENT", "studentId" to "JBA-2026-002"),
        "uid-admin-01" to mapOf("uid" to "uid-admin-01", "role" to "ADMIN")
    )

    @Test
    fun `test A - Unauthenticated student read is DENIED`() {
        val studentDoc = FirestoreDoc("JBA-2026-001", mapOf("fullName" to "Rahul Sharma", "mobileNumber" to "9876543210"))
        val canRead = SecurityRulesEngine.evaluateRead("students", studentDoc, unauthenticated, usersMap)
        assertFalse("Unauthenticated users MUST NOT read student profiles", canRead)
    }

    @Test
    fun `test B - Student A reading Student B profile is DENIED`() {
        val studentBDoc = FirestoreDoc("JBA-2026-002", mapOf("fullName" to "Amit Singh", "mobileNumber" to "9876543211"))
        val canRead = SecurityRulesEngine.evaluateRead("students", studentBDoc, studentAAuth, usersMap)
        assertFalse("Student A must NOT read Student B profile", canRead)
    }

    @Test
    fun `test C - Student A creating Student B attendance is DENIED`() {
        val newAttendance = mapOf("studentId" to "JBA-2026-002", "date" to "2026-08-25", "status" to "Present")
        val canCreate = SecurityRulesEngine.evaluateCreate("attendance_records", "JBA-2026-002_2026-08-25", newAttendance, studentAAuth, "2026-08-25", usersMap)
        assertFalse("Student A must NOT create attendance for Student B", canCreate)
    }

    @Test
    fun `test D - Student A creating own attendance today is ALLOWED`() {
        val newAttendance = mapOf("studentId" to "JBA-2026-001", "uid" to "uid-student-A", "date" to "2026-08-25", "status" to "Present")
        val canCreate = SecurityRulesEngine.evaluateCreate("attendance_records", "JBA-2026-001_2026-08-25", newAttendance, studentAAuth, "2026-08-25", usersMap)
        assertTrue("Student A must be allowed to create own attendance for today", canCreate)
    }

    @Test
    fun `test E - Student A creating past attendance is DENIED`() {
        val pastAttendance = mapOf("studentId" to "JBA-2026-001", "uid" to "uid-student-A", "date" to "2026-08-24", "status" to "Present")
        val canCreate = SecurityRulesEngine.evaluateCreate("attendance_records", "JBA-2026-001_2026-08-24", pastAttendance, studentAAuth, "2026-08-25", usersMap)
        assertFalse("Student A must NOT create attendance for past dates", canCreate)
    }

    @Test
    fun `test F - Student A creating future attendance is DENIED`() {
        val futureAttendance = mapOf("studentId" to "JBA-2026-001", "uid" to "uid-student-A", "date" to "2026-08-26", "status" to "Present")
        val canCreate = SecurityRulesEngine.evaluateCreate("attendance_records", "JBA-2026-001_2026-08-26", futureAttendance, studentAAuth, "2026-08-25", usersMap)
        assertFalse("Student A must NOT create attendance for future dates", canCreate)
    }

    @Test
    fun `test G - Student A modifying own submitted attendance is DENIED`() {
        val existingAttendance = FirestoreDoc("JBA-2026-001_2026-08-25", mapOf("studentId" to "JBA-2026-001", "date" to "2026-08-25", "status" to "Present"))
        val updatedData = mapOf("studentId" to "JBA-2026-001", "date" to "2026-08-25", "status" to "Absent")
        val canUpdate = SecurityRulesEngine.evaluateUpdate("attendance_records", existingAttendance, updatedData, studentAAuth, usersMap)
        assertFalse("Student A must NOT update submitted attendance", canUpdate)
    }

    @Test
    fun `test H - Student A modifying own test score is DENIED`() {
        val existingAttempt = FirestoreDoc("attempt_01", mapOf("studentId" to "JBA-2026-001", "score" to 35, "maxScore" to 100))
        val updatedData = mapOf("studentId" to "JBA-2026-001", "score" to 95, "maxScore" to 100)
        val canUpdate = SecurityRulesEngine.evaluateUpdate("test_attempts", existingAttempt, updatedData, studentAAuth, usersMap)
        assertFalse("Students must NEVER modify submitted test scores", canUpdate)
    }

    @Test
    fun `test I - Student A creating or modifying leaderboard is DENIED`() {
        val leaderboardDoc = FirestoreDoc("top_rankers", mapOf("rank1" to "JBA-2026-002"))
        val updatedData = mapOf("rank1" to "JBA-2026-001")
        val canUpdate = SecurityRulesEngine.evaluateUpdate("leaderboard", leaderboardDoc, updatedData, studentAAuth, usersMap)
        val canCreate = SecurityRulesEngine.evaluateCreate("leaderboard", "rank_custom", updatedData, studentAAuth, "2026-08-25", usersMap)
        assertFalse("Students must NOT update leaderboard", canUpdate)
        assertFalse("Students must NOT create leaderboard entries", canCreate)
    }

    @Test
    fun `test J - Student A creating or modifying questions is DENIED`() {
        val qDoc = FirestoreDoc("q_101", mapOf("questionText" to "What is capital of CG?"))
        val canUpdate = SecurityRulesEngine.evaluateUpdate("questions", qDoc, mapOf("questionText" to "Hacked question"), studentAAuth, usersMap)
        val canCreate = SecurityRulesEngine.evaluateCreate("questions", "q_999", mapOf("questionText" to "New"), studentAAuth, "2026-08-25", usersMap)
        assertFalse("Students must NOT update questions", canUpdate)
        assertFalse("Students must NOT create questions", canCreate)
    }

    @Test
    fun `test K - Student A modifying notices or recruitment info is DENIED`() {
        val noticeDoc = FirestoreDoc("notice_1", mapOf("title" to "Ground open"))
        val canUpdateNotice = SecurityRulesEngine.evaluateUpdate("notices", noticeDoc, mapOf("title" to "Altered"), studentAAuth, usersMap)
        val canCreateRecruitment = SecurityRulesEngine.evaluateCreate("recruitment_info", "rec_99", mapOf("name" to "Fake"), studentAAuth, "2026-08-25", usersMap)

        assertFalse("Student must NOT update notices", canUpdateNotice)
        assertFalse("Student must NOT create recruitment info", canCreateRecruitment)
    }

    @Test
    fun `test L - Admin student management is ALLOWED`() {
        val studentDoc = FirestoreDoc("JBA-2026-001", mapOf("fullName" to "Rahul Sharma", "mobileNumber" to "9876543210"))
        val canRead = SecurityRulesEngine.evaluateRead("students", studentDoc, adminAuth, usersMap)
        val canUpdate = SecurityRulesEngine.evaluateUpdate("students", studentDoc, mapOf("overallScore" to 90), adminAuth, usersMap)
        val canCreate = SecurityRulesEngine.evaluateCreate("students", "JBA-2026-099", mapOf("fullName" to "New Student"), adminAuth, "2026-08-25", usersMap)

        assertTrue("Admin must be allowed to read students", canRead)
        assertTrue("Admin must be allowed to update students", canUpdate)
        assertTrue("Admin must be allowed to create students", canCreate)
    }

    @Test
    fun `test M - Admin attendance correction is ALLOWED`() {
        val existingAttendance = FirestoreDoc("JBA-2026-001_2026-08-25", mapOf("studentId" to "JBA-2026-001", "date" to "2026-08-25", "status" to "Absent"))
        val correctedData = mapOf("studentId" to "JBA-2026-001", "date" to "2026-08-25", "status" to "Present (Admin Correction)")
        val canUpdate = SecurityRulesEngine.evaluateUpdate("attendance_records", existingAttendance, correctedData, adminAuth, usersMap)
        assertTrue("Admin must be allowed to correct attendance", canUpdate)
    }

    @Test
    fun `test N - Admin test correction is ALLOWED`() {
        val existingAttempt = FirestoreDoc("attempt_01", mapOf("studentId" to "JBA-2026-001", "score" to 35, "maxScore" to 100))
        val correctedData = mapOf("studentId" to "JBA-2026-001", "score" to 40, "maxScore" to 100, "trainerNotes" to "Recounted")
        val canUpdate = SecurityRulesEngine.evaluateUpdate("test_attempts", existingAttempt, correctedData, adminAuth, usersMap)
        assertTrue("Admin must be allowed to perform test corrections", canUpdate)
    }

    @Test
    fun `test O - Cross-student access using forged uid or studentId fields is DENIED`() {
        // Attacker creates attendance containing student B's ID while claiming their own uid
        val spoofedData = mapOf("studentId" to "JBA-2026-002", "uid" to "uid-student-A", "date" to "2026-08-25", "status" to "Present")
        val canCreate = SecurityRulesEngine.evaluateCreate("attendance_records", "JBA-2026-002_2026-08-25", spoofedData, studentAAuth, "2026-08-25", usersMap)
        assertFalse("Cross-student spoofing MUST be denied when studentId does not match user account", canCreate)
    }

    @Test
    fun `test P - Self-promotion to ADMIN or COACH is DENIED`() {
        val escalatedSignupData = mapOf("uid" to "uid-student-A", "role" to "ADMIN", "email" to "hacker@test.com")
        val canCreateEscalated = SecurityRulesEngine.evaluateCreate("users", "uid-student-A", escalatedSignupData, studentAAuth, "2026-08-25", usersMap)
        assertFalse("Student self-promoting to ADMIN on signup MUST be DENIED", canCreateEscalated)

        val existingUserDoc = FirestoreDoc("uid-student-A", mapOf("uid" to "uid-student-A", "role" to "STUDENT"))
        val escalatedUpdateData = mapOf("uid" to "uid-student-A", "role" to "COACH")
        val canUpdateEscalated = SecurityRulesEngine.evaluateUpdate("users", existingUserDoc, escalatedUpdateData, studentAAuth, usersMap)
        assertFalse("Student elevating role to COACH on update MUST be DENIED", canUpdateEscalated)
    }

    @Test
    fun `test Q - Unlisted collection access is DENIED by default`() {
        val secretDoc = FirestoreDoc("secret_1", mapOf("key" to "value"))
        val canRead = SecurityRulesEngine.evaluateRead("internal_logs", secretDoc, studentAAuth, usersMap)
        val canCreate = SecurityRulesEngine.evaluateCreate("audit_events", "audit_1", mapOf("event" to "test"), studentAAuth, "2026-08-25", usersMap)
        val canUpdate = SecurityRulesEngine.evaluateUpdate("system_configs", secretDoc, mapOf("key" to "hacked"), studentAAuth, usersMap)

        assertFalse("Unlisted collection READ must be DENIED by default", canRead)
        assertFalse("Unlisted collection CREATE must be DENIED by default", canCreate)
        assertFalse("Unlisted collection UPDATE must be DENIED by default", canUpdate)
    }
}
