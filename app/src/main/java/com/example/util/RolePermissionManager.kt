package com.example.util

import com.example.data.cloud.FirestoreConstants

/**
 * Multi-layer Security and Role Permission Manager for Jai Bajrang Akhada.
 * Enforces role-based permissions across Layer 1 (UI), Layer 2 (ViewModel / Repository),
 * and defines the security matrix aligned with Layer 3 (Firestore Rules).
 *
 * Supported Roles:
 * 1. "ADMIN" - Full system, security, content, quiz, and operational control.
 * 2. "TRAINER" - Ground training, attendance, workout plans, and student metrics.
 * 3. "STUDENT" - Personal profile, workouts, study, exams, and attendance logging.
 */
object RolePermissionManager {

    const val ROLE_ADMIN = FirestoreConstants.ROLE_ADMIN
    const val ROLE_TRAINER = FirestoreConstants.ROLE_TRAINER
    const val ROLE_STUDENT = FirestoreConstants.ROLE_STUDENT

    /**
     * Checks if given role has full Administrator privileges.
     */
    fun isAdmin(role: String?): Boolean {
        if (role.isNullOrBlank()) return false
        return role.equals(ROLE_ADMIN, ignoreCase = true)
    }

    /**
     * Checks if given role has Coach/Trainer privileges.
     */
    fun isTrainer(role: String?): Boolean {
        if (role.isNullOrBlank()) return false
        return role.equals(ROLE_TRAINER, ignoreCase = true) || role.equals("COACH", ignoreCase = true)
    }

    /**
     * Checks if role is either Admin or Trainer (Academy Staff).
     */
    fun isStaff(role: String?): Boolean {
        return isAdmin(role) || isTrainer(role)
    }

    /**
     * Checks if role is Student / Cadet.
     */
    fun isStudent(role: String?): Boolean {
        if (role.isNullOrBlank()) return false
        return role.equals(ROLE_STUDENT, ignoreCase = true)
    }

    // ==========================================
    // Layer 2: Permission Enforcement Methods
    // ==========================================

    /**
     * Security & PIN Management: Strictly Admin only.
     */
    fun canManageAdminSecurity(role: String?): Boolean = isAdmin(role)

    /**
     * User Roles & Authorization Management: Strictly Admin only.
     */
    fun canManageUserRoles(role: String?): Boolean = isAdmin(role)

    /**
     * Student Deletion: Strictly Admin only.
     */
    fun canDeleteStudent(role: String?): Boolean = isAdmin(role)

    /**
     * Question Bank & Exam Configuration: Strictly Admin only.
     */
    fun canManageQuestionBank(role: String?): Boolean = isAdmin(role)

    /**
     * Notices & Recruitment Management: Strictly Admin only.
     */
    fun canManageNotices(role: String?): Boolean = isAdmin(role)

    /**
     * Website & Akhada CMS (Trainers, Gallery, Stories, Contact): Strictly Admin only.
     */
    fun canManageContentCms(role: String?): Boolean = isAdmin(role)

    /**
     * Study Materials & Subject Management: Strictly Admin only.
     */
    fun canManageStudyMaterials(role: String?): Boolean = isAdmin(role)

    // ==========================================
    // Ground Operations: Allowed for Admin & Trainer
    // ==========================================

    /**
     * Mark or update daily student attendance (batch or individual).
     */
    fun canMarkAttendance(role: String?): Boolean = isStaff(role)

    /**
     * Create or publish daily workout and training plans.
     */
    fun canManageWorkoutPlan(role: String?): Boolean = isStaff(role)

    /**
     * Record and update physical metrics (1600m run, 100m sprint, pull-ups, push-ups, jump).
     */
    fun canUpdatePhysicalPerformance(role: String?): Boolean = isStaff(role)

    /**
     * Enroll new student/cadet into the academy directory.
     */
    fun canEnrollStudent(role: String?): Boolean = isStaff(role)

    /**
     * Operate Live 1600m Batch Stopwatch and record finish times (Phase 2B).
     */
    fun canOperateLiveStopwatch(role: String?): Boolean = isStaff(role)

    /**
     * View full academy student directory and profile details.
     */
    fun canViewAllStudents(role: String?): Boolean = isStaff(role)

    /**
     * Phase 3: View academy-wide monthly performance analytics dashboard.
     * Admin & Trainer have full ground performance view.
     */
    fun canViewMonthlyAnalytics(role: String?): Boolean = isStaff(role)

    /**
     * Phase 3: Generate and export bulk PDF performance reports for all academy cadets.
     * Allowed for Staff (Admin & Trainer).
     */
    fun canGenerateBulkCadetReports(role: String?): Boolean = isStaff(role)

    /**
     * Phase 3: Checks whether current user is authorized to view or generate report for a specific cadet.
     * Staff can view any cadet; Students can only view/generate their own report.
     */
    fun canAccessCadetReport(role: String?, requesterStudentId: String?, targetStudentId: String): Boolean {
        if (isStaff(role)) return true
        if (isStudent(role) && requesterStudentId != null) {
            return requesterStudentId.equals(targetStudentId, ignoreCase = true)
        }
        return false
    }

    /**
     * Helper to safely sanitize role string, falling back to safest restricted "STUDENT" role.
     */
    fun sanitizeRole(role: String?): String {
        return when {
            isAdmin(role) -> ROLE_ADMIN
            isTrainer(role) -> ROLE_TRAINER
            else -> ROLE_STUDENT
        }
    }
}
