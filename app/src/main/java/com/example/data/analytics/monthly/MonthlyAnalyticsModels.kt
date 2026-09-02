package com.example.data.analytics.monthly

import com.example.data.model.RaceResult

/**
 * Improvement status compared to the previous month.
 */
enum class ImprovementStatus(val labelHindi: String, val badgeColorHex: Long) {
    IMPROVED("सुधार हुआ (Improved)", 0xFF16A34A),       // Green
    SAME("समान रहा (Same)", 0xFF2563EB),               // Blue
    DECLINED("गिरावट (Declined)", 0xFFDC2626),          // Red / Orange
    NO_PREVIOUS_DATA("पिछला डेटा नहीं", 0xFF6B7280)    // Gray
}

/**
 * Represents comparison difference and status for a single metric.
 */
data class MetricImprovement<T>(
    val currentVal: T,
    val previousVal: T?,
    val diffText: String,
    val status: ImprovementStatus
)

/**
 * Cadet monthly attendance analytics computed from existing AttendanceRecords.
 */
data class CadetMonthlyAttendance(
    val studentId: String,
    val totalTrainingDays: Int = 0,
    val presentCount: Int = 0,
    val absentCount: Int = 0,
    val leaveCount: Int = 0,
    val attendancePercentage: Double = 0.0,
    val currentStreak: Int = 0,
    val bestStreak: Int = 0
)

/**
 * Comprehensive monthly physical performance & analytics for an individual cadet.
 */
data class CadetMonthlyPhysicalPerformance(
    val studentId: String,
    val studentName: String,
    val fatherName: String = "",
    val chestNumber: String = "",
    val batch: String = "",
    val village: String = "",
    val mobileNumber: String = "",
    val recruitmentGoal: String = "Indian Army",
    
    // Running Metrics
    val time1600mLatest: String = "-",
    val time1600mBest: String = "-",
    val time1600mAverage: String = "-",
    val time1600mPrevMonth: String? = null,
    val time1600mImprovement: MetricImprovement<String>,
    val time100mLatest: String = "-",
    val time100mBest: String = "-",
    
    // Strength Metrics
    val pushupsLatest: Int = 0,
    val pushupsBest: Int = 0,
    val pushupsImprovement: MetricImprovement<Int>,
    val pullupsLatest: Int = 0,
    val pullupsBest: Int = 0,
    val pullupsImprovement: MetricImprovement<Int>,
    val situpsLatest: Int = 0,
    val plankSecondsLatest: Int = 0,
    
    // Field / Physical Body Metrics
    val heightCm: Double = 0.0,
    val weightKg: Double = 0.0,
    val chestNormalCm: Double = 0.0,
    val chestExpandedCm: Double = 0.0,
    val longJumpFeet: Double = 0.0,
    val highJumpFeet: Double = 0.0,
    val shotPutMeters: Double = 0.0,
    
    // Attendance Analytics
    val attendance: CadetMonthlyAttendance,
    val attendanceImprovement: MetricImprovement<Double>,
    
    // Monthly Academy Rankings
    val rank1600m: Int = 0,         // 1st, 2nd, 3rd... (0 if unranked / DNF)
    val overallRank: Int = 0,       // Composite ranking
    val performanceGrade: String = "A", // A+, A, B+, B, C
    
    // 1600m Race Trial History in the selected month
    val racesCount: Int = 0,
    val raceHistory: List<RaceResult> = emptyList(),
    val isDnfOnly: Boolean = false
)

/**
 * Academy-wide high level summary for the selected month.
 */
data class MonthlyDashboardSummary(
    val monthKey: String, // e.g. "2026-08"
    val monthLabel: String, // e.g. "अगस्त 2026 (August 2026)"
    val previousMonthKey: String, // e.g. "2026-07"
    val totalCadets: Int = 0,
    val activeCadets: Int = 0,
    val averageAttendancePercentage: Double = 0.0,
    val average1600mFormatted: String = "-",
    val average1600mSeconds: Double = 0.0,
    val best1600mFormatted: String = "-",
    val best1600mCadetName: String = "-",
    val average100mFormatted: String = "-",
    val averagePushups: Double = 0.0,
    val averagePullups: Double = 0.0,
    val totalPhysicalTests: Int = 0,
    val totalTrainingSessions: Int = 0,
    val topRankedCadets: List<CadetMonthlyPhysicalPerformance> = emptyList()
)

/**
 * Top-level container representing the full monthly performance state.
 */
data class MonthlyPerformanceReport(
    val monthKey: String,
    val monthLabel: String,
    val previousMonthKey: String,
    val summary: MonthlyDashboardSummary,
    val cadetReports: List<CadetMonthlyPhysicalPerformance> = emptyList(),
    val generatedTimestamp: Long = System.currentTimeMillis()
)
