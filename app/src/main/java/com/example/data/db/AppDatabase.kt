package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        StudentProfile::class,
        AttendanceRecord::class,
        TrainingRecord::class,
        WorkoutRecord::class,
        DailyWorkoutPlan::class,
        StudySubject::class,
        StudyTopic::class,
        Chapter::class,
        Question::class,
        StudyAttempt::class,
        MockTest::class,
        TestAttempt::class,
        Notice::class,
        RecruitmentInfo::class,
        Trainer::class,
        GalleryItem::class,
        SuccessStory::class,
        ContactInfo::class,
        RaceSession::class,
        RaceResult::class,
        AppNotification::class
    ],
    version = 11,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Ensure unique index on student mobile numbers
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_students_mobileNumber` ON `students` (`mobileNumber`)")
            }
        }

        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE students ADD COLUMN assignedTrainerId TEXT NOT NULL DEFAULT 'TR-001'")
                db.execSQL("ALTER TABLE students ADD COLUMN assignedTrainerName TEXT NOT NULL DEFAULT 'देव कुमार निषाद (मुख्य कोच)'")
                db.execSQL("ALTER TABLE students ADD COLUMN batchName TEXT NOT NULL DEFAULT 'सुबह आर्मी स्पेशल बैच (Morning Army Batch)'")
            }
        }

        val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `app_notifications` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `notificationId` TEXT NOT NULL,
                        `title` TEXT NOT NULL,
                        `message` TEXT NOT NULL,
                        `category` TEXT NOT NULL,
                        `senderId` TEXT NOT NULL,
                        `senderName` TEXT NOT NULL,
                        `senderRole` TEXT NOT NULL,
                        `targetType` TEXT NOT NULL,
                        `targetStudentIds` TEXT NOT NULL,
                        `targetBatch` TEXT NOT NULL,
                        `targetTrainerId` TEXT NOT NULL,
                        `actionRoute` TEXT NOT NULL,
                        `isUrgent` INTEGER NOT NULL,
                        `timestamp` INTEGER NOT NULL,
                        `dateFormatted` TEXT NOT NULL,
                        `isRead` INTEGER NOT NULL,
                        `readByUids` TEXT NOT NULL
                    )
                """.trimIndent())
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_app_notifications_notificationId` ON `app_notifications` (`notificationId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_app_notifications_category` ON `app_notifications` (`category`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_app_notifications_targetType` ON `app_notifications` (`targetType`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_app_notifications_senderId` ON `app_notifications` (`senderId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_app_notifications_timestamp` ON `app_notifications` (`timestamp`)")
            }
        }

        val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE students ADD COLUMN passwordHash TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE students ADD COLUMN passwordSalt TEXT NOT NULL DEFAULT ''")
            }
        }

        val MIGRATION_10_11 = object : Migration(10, 11) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE trainers ADD COLUMN coachId TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE trainers ADD COLUMN achievement TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE trainers ADD COLUMN role TEXT NOT NULL DEFAULT 'TRAINER'")
                db.execSQL("ALTER TABLE trainers ADD COLUMN passwordHash TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE trainers ADD COLUMN passwordSalt TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE trainers ADD COLUMN forcePasswordChange INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE trainers ADD COLUMN active INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE trainers ADD COLUMN createdAt INTEGER NOT NULL DEFAULT 0")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_trainers_coachId` ON `trainers` (`coachId`)")
            }
        }

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "jai_bajrang_akhada.db"
                )
                .addMigrations(MIGRATION_5_6, MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10, MIGRATION_10_11)
                .fallbackToDestructiveMigration()
                .addCallback(DatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database.appDao())
                    }
                }
            }

            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        com.example.util.CoachAuthManager.seedInitialCoaches(database.appDao())
                    }
                }
            }
        }

        suspend fun populateInitialData(dao: AppDao) {
            com.example.util.CoachAuthManager.seedInitialCoaches(dao)
            dao.insertStudents(DemoDataGenerator.getSampleStudents())
            dao.insertAttendanceList(DemoDataGenerator.getSampleAttendance())
            dao.insertTrainingRecords(DemoDataGenerator.getSampleTrainingRecords())
            dao.insertWorkoutRecords(DemoDataGenerator.getSampleWorkoutRecords())
            dao.insertWorkoutPlan(DemoDataGenerator.getSampleWorkoutPlan())
            dao.insertSubjects(DemoDataGenerator.getSampleStudySubjects())
            dao.insertTopics(DemoDataGenerator.getSampleStudyTopics())
            dao.insertChapters(DemoDataGenerator.getSampleChapters())
            dao.insertQuestions(DemoDataGenerator.getSampleQuestions())
            dao.insertStudyAttempts(DemoDataGenerator.getSampleStudyAttempts())
            dao.insertMockTests(DemoDataGenerator.getSampleMockTests())
            DemoDataGenerator.getSampleTestAttempts().forEach { dao.insertTestAttempt(it) }
            dao.insertNotices(DemoDataGenerator.getSampleNotices())
            dao.insertRecruitmentInfos(DemoDataGenerator.getSampleRecruitmentInfo())
            dao.insertRaceSessions(DemoDataGenerator.getSampleRaceSessions())
            dao.insertRaceResults(DemoDataGenerator.getSampleRaceResults())
            dao.insertNotifications(DemoDataGenerator.getSampleNotifications())
        }
    }
}
