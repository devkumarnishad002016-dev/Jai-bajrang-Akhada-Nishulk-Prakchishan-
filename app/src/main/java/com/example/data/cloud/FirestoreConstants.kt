package com.example.data.cloud

/**
 * Constants for Firestore Cloud Backend Collections and Fields.
 * Ensures consistent naming across Android client and Cloud Security Rules.
 */
object FirestoreConstants {
    // Top-Level Collections
    const val COLLECTION_USERS = "users"
    const val COLLECTION_STUDENTS = "students"
    const val COLLECTION_ATTENDANCE = "attendance_records"
    const val COLLECTION_TRAINING = "training_records"
    const val COLLECTION_WORKOUTS = "workout_records"
    const val COLLECTION_WORKOUT_PLANS = "workout_plans"
    const val COLLECTION_NOTICES = "notices"
    const val COLLECTION_RECRUITMENT = "recruitment_info"
    const val COLLECTION_SUBJECTS = "study_subjects"
    const val COLLECTION_TOPICS = "study_topics"
    const val COLLECTION_QUESTIONS = "questions"
    const val COLLECTION_STUDY_ATTEMPTS = "study_attempts"
    const val COLLECTION_MOCK_TESTS = "mock_tests"
    const val COLLECTION_TEST_ATTEMPTS = "test_attempts"
    const val COLLECTION_RACE_SESSIONS = "race_sessions"
    const val COLLECTION_RACE_RESULTS = "race_results"
    const val COLLECTION_CHAPTERS = "chapters"
    const val COLLECTION_NOTIFICATIONS = "app_notifications"

    // Roles
    const val ROLE_STUDENT = "STUDENT"
    const val ROLE_ADMIN = "ADMIN"
    const val ROLE_TRAINER = "TRAINER"
    const val ROLE_COACH = "TRAINER" // backward compat alias for TRAINER

    // Metadata fields
    const val FIELD_UPDATED_AT = "updatedAt"
    const val FIELD_CREATED_AT = "createdAt"
    const val FIELD_SYNCED_BY = "syncedBy"
    const val FIELD_STUDENT_ID = "studentId"
    const val FIELD_UID = "uid"
    const val FIELD_ROLE = "role"
    const val FIELD_IS_ACTIVE = "isActive"
    const val FIELD_APP_VERSION = "appVersion"
}
