package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "students",
    indices = [
        Index(value = ["mobileNumber"], unique = true),
        Index(value = ["studentId"], unique = true)
    ]
)
data class StudentProfile(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentId: String = "JBA-2026-001",
    val fullName: String = "",
    val fatherName: String = "",
    val mobileNumber: String = "",
    val village: String = "",
    val dob: String = "2004-05-15",
    val age: Int = 21,
    val gender: String = "Male",
    val education: String = "12th Pass",
    val recruitmentGoal: String = "Indian Army", // Indian Army, CG Police, SSC GD, CRPF, BSF, ITBP, CISF, Other
    val joinDate: String = "2025-01-10",
    val profilePhotoUri: String = "",
    // Physical Metrics
    val heightCm: Double = 171.0,
    val weightKg: Double = 63.5,
    val chestNormalCm: Double = 81.0,
    val chestExpandedCm: Double = 86.0,
    val time400m: String = "1:08",
    val time800m: String = "2:26",
    val time1600m: String = "5:35",
    val time5km: String = "21:30",
    val pushups: Int = 45,
    val situps: Int = 50,
    val pullups: Int = 12,
    val squats: Int = 60,
    val plankSeconds: Int = 120,
    val overallScore: Int = 84,
    val longJumpFeet: Double = 15.5,
    val highJumpFeet: Double = 4.2,
    val shotPutMeters: Double = 7.5,
    val attendanceStreakDays: Int = 7,
    val studyTargetPercentage: Int = 80,
    val assignedTrainerId: String = "TR-001",
    val assignedTrainerName: String = "देव कुमार निषाद (मुख्य कोच)",
    val batchName: String = "सुबह आर्मी स्पेशल बैच (Morning Army Batch)",
    val passwordHash: String = "",
    val passwordSalt: String = "",
    val isDeleted: Boolean = false,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
) {
    val joiningDate: String get() = joinDate
}
