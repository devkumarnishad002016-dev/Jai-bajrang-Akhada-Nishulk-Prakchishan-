package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "attendance_records",
    indices = [Index(value = ["studentId", "date"], unique = true)]
)
data class AttendanceRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentId: String,
    val date: String, // YYYY-MM-DD
    val status: String, // "Present", "Absent", "Leave", "Late"
    val remarks: String = ""
)

@Entity(
    tableName = "training_records",
    indices = [Index(value = ["studentId", "date"])]
)
data class TrainingRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentId: String,
    val date: String, // YYYY-MM-DD
    // Running
    val runningDistanceKm: Double = 1.6,
    val runningDuration: String = "5:38", // e.g. "5:38" or "25:00"
    val runningType: String = "1600m Practice", // Easy Run, Long Run, Sprint, Interval, 400m Practice, 800m Practice, 1600m Practice, 5 KM Practice, Other
    // Strength
    val pushups: Int = 40,
    val situps: Int = 45,
    val pullups: Int = 10,
    val squats: Int = 50,
    val plankSeconds: Int = 120,
    // Additional
    val stretchingDone: Boolean = true,
    val otherTraining: String = "",
    val trainerNotes: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "workout_records")
data class WorkoutRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentId: String,
    val date: String,
    val runningDistanceKm: Double = 3.0,
    val runningTargetKm: Double = 3.0,
    val time1600mSeconds: Int = 430, // 7 min 10 sec = 430s
    val previous1600mSeconds: Int = 450, // 7 min 30 sec = 450s
    val pushupsDone: Int = 35,
    val pushupsTarget: Int = 50,
    val situpsDone: Int = 45,
    val situpsTarget: Int = 50,
    val pullupsDone: Int = 8,
    val pullupsTarget: Int = 10,
    val squatsDone: Int = 50,
    val squatsTarget: Int = 50,
    val plankSecondsDone: Int = 90,
    val notes: String = ""
)

@Entity(tableName = "daily_workout_plans")
data class DailyWorkoutPlan(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String,
    val title: String,
    val instructions: String,
    val targetRunningKm: Double = 3.0,
    val targetPushups: Int = 50,
    val targetSitups: Int = 50,
    val targetPullups: Int = 10,
    val targetSquats: Int = 60,
    val sprintDetails: String = "4 x 100m Sprint Intervals"
)
