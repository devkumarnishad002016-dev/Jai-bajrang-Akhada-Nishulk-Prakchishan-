package com.example.data.analytics.monthly

import com.example.data.model.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

/**
 * Pure Kotlin analytics computation engine for monthly cadet performance.
 * Aggregates local Room data (Students, Attendance, Training, Workouts, Races)
 * into structured reports, statistical summaries, rankings, and improvement metrics.
 */
object MonthlyPerformanceEngine {

    private val monthFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())
    private val fullDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    val HINDI_MONTHS = mapOf(
        1 to "जनवरी (January)",
        2 to "फरवरी (February)",
        3 to "मार्च (March)",
        4 to "अप्रैल (April)",
        5 to "मई (May)",
        6 to "जून (June)",
        7 to "जुलाई (July)",
        8 to "अगस्त (August)",
        9 to "सितंबर (September)",
        10 to "अक्टूबर (October)",
        11 to "नवंबर (November)",
        12 to "दिसंबर (December)"
    )

    fun formatMonthLabel(monthKey: String): String {
        val parts = monthKey.split("-")
        if (parts.size == 2) {
            val year = parts[0]
            val monthNum = parts[1].toIntOrNull() ?: 1
            val monthName = HINDI_MONTHS[monthNum] ?: "माह $monthNum"
            return "$monthName $year"
        }
        return monthKey
    }

    fun getPreviousMonthKey(monthKey: String): String {
        val parts = monthKey.split("-")
        if (parts.size == 2) {
            var year = parts[0].toIntOrNull() ?: 2026
            var month = parts[1].toIntOrNull() ?: 8
            month -= 1
            if (month < 1) {
                month = 12
                year -= 1
            }
            return String.format(Locale.US, "%04d-%02d", year, month)
        }
        return "2026-07"
    }

    fun getNextMonthKey(monthKey: String): String {
        val parts = monthKey.split("-")
        if (parts.size == 2) {
            var year = parts[0].toIntOrNull() ?: 2026
            var month = parts[1].toIntOrNull() ?: 8
            month += 1
            if (month > 12) {
                month = 1
                year += 1
            }
            return String.format(Locale.US, "%04d-%02d", year, month)
        }
        return "2026-09"
    }

    fun getCurrentMonthKey(): String {
        return monthFormat.format(Date())
    }

    /**
     * Converts a running time string (e.g. "05:15.00", "5:35", "05:12") to total seconds.
     * Returns Double.MAX_VALUE if invalid or unparseable.
     */
    fun parseTimeToSeconds(timeStr: String?): Double {
        if (timeStr.isNullOrBlank() || timeStr == "-" || timeStr.equals("DNF", ignoreCase = true) || timeStr.equals("DNS", ignoreCase = true)) {
            return Double.MAX_VALUE
        }
        val clean = timeStr.trim()
        val colonParts = clean.split(":")
        return try {
            if (colonParts.size == 2) {
                val mins = colonParts[0].toDoubleOrNull() ?: return Double.MAX_VALUE
                val secs = colonParts[1].toDoubleOrNull() ?: return Double.MAX_VALUE
                mins * 60.0 + secs
            } else if (colonParts.size == 3) {
                val hrs = colonParts[0].toDoubleOrNull() ?: return Double.MAX_VALUE
                val mins = colonParts[1].toDoubleOrNull() ?: return Double.MAX_VALUE
                val secs = colonParts[2].toDoubleOrNull() ?: return Double.MAX_VALUE
                hrs * 3600.0 + mins * 60.0 + secs
            } else {
                clean.toDoubleOrNull() ?: Double.MAX_VALUE
            }
        } catch (_: Exception) {
            Double.MAX_VALUE
        }
    }

    /**
     * Formats seconds (e.g. 315.0) to "05:15" or "05:15.20"
     */
    fun formatSecondsToTime(seconds: Double, includeDecimals: Boolean = false): String {
        if (seconds <= 0 || seconds >= Double.MAX_VALUE - 1000) return "-"
        val mins = (seconds / 60).toInt()
        val secs = seconds % 60
        return if (includeDecimals) {
            val secInt = secs.toInt()
            val centi = ((secs - secInt) * 100).toInt()
            String.format(Locale.US, "%02d:%02d.%02d", mins, secInt, centi)
        } else {
            String.format(Locale.US, "%02d:%02d", mins, secs.roundToInt())
        }
    }

    /**
     * Computes the complete monthly performance report.
     */
    fun generateMonthlyReport(
        monthKey: String,
        students: List<StudentProfile>,
        allAttendance: List<AttendanceRecord>,
        allTraining: List<TrainingRecord>,
        allWorkouts: List<WorkoutRecord>,
        allRaceSessions: List<RaceSession>,
        allRaceResults: List<RaceResult>
    ): MonthlyPerformanceReport {
        val prevMonthKey = getPreviousMonthKey(monthKey)
        val monthLabel = formatMonthLabel(monthKey)

        // Filter records for Current Month & Previous Month
        val currentAttendance = allAttendance.filter { it.date.startsWith(monthKey) }
        val prevAttendance = allAttendance.filter { it.date.startsWith(prevMonthKey) }

        val currentTraining = allTraining.filter { it.date.startsWith(monthKey) }
        val prevTraining = allTraining.filter { it.date.startsWith(prevMonthKey) }

        val currentWorkouts = allWorkouts.filter { it.date.startsWith(monthKey) }
        val prevWorkouts = allWorkouts.filter { it.date.startsWith(prevMonthKey) }

        // Filter race sessions & results
        val raceSessionsByMonth = allRaceSessions.filter { it.date.startsWith(monthKey) }.associateBy { it.raceId }
        val prevRaceSessionsByMonth = allRaceSessions.filter { it.date.startsWith(prevMonthKey) }.associateBy { it.raceId }

        val currentRaceResults = allRaceResults.filter { result ->
            raceSessionsByMonth.containsKey(result.raceId) ||
            SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date(result.createdAt)).startsWith(monthKey)
        }

        val prevRaceResults = allRaceResults.filter { result ->
            prevRaceSessionsByMonth.containsKey(result.raceId) ||
            SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date(result.createdAt)).startsWith(prevMonthKey)
        }

        // Build individual cadet reports
        val rawCadetReports = students.map { student ->
            buildCadetReport(
                student = student,
                monthKey = monthKey,
                prevMonthKey = prevMonthKey,
                currentAttendance = currentAttendance.filter { it.studentId == student.studentId },
                prevAttendance = prevAttendance.filter { it.studentId == student.studentId },
                currentTraining = currentTraining.filter { it.studentId == student.studentId },
                prevTraining = prevTraining.filter { it.studentId == student.studentId },
                currentWorkouts = currentWorkouts.filter { it.studentId == student.studentId },
                prevWorkouts = prevWorkouts.filter { it.studentId == student.studentId },
                currentRaceResults = currentRaceResults.filter { it.studentId == student.studentId },
                prevRaceResults = prevRaceResults.filter { it.studentId == student.studentId }
            )
        }

        // Calculate 1600m dense ranking (fastest time gets Rank 1)
        val rankedReports = assignRankings(rawCadetReports)

        // Build high-level Academy Summary
        val summary = buildDashboardSummary(
            monthKey = monthKey,
            monthLabel = monthLabel,
            prevMonthKey = prevMonthKey,
            students = students,
            cadetReports = rankedReports,
            currentAttendance = currentAttendance,
            currentTraining = currentTraining,
            currentRaceResults = currentRaceResults,
            currentRaceSessions = raceSessionsByMonth.values.toList()
        )

        return MonthlyPerformanceReport(
            monthKey = monthKey,
            monthLabel = monthLabel,
            previousMonthKey = prevMonthKey,
            summary = summary,
            cadetReports = rankedReports
        )
    }

    private fun buildCadetReport(
        student: StudentProfile,
        monthKey: String,
        prevMonthKey: String,
        currentAttendance: List<AttendanceRecord>,
        prevAttendance: List<AttendanceRecord>,
        currentTraining: List<TrainingRecord>,
        prevTraining: List<TrainingRecord>,
        currentWorkouts: List<WorkoutRecord>,
        prevWorkouts: List<WorkoutRecord>,
        currentRaceResults: List<RaceResult>,
        prevRaceResults: List<RaceResult>
    ): CadetMonthlyPhysicalPerformance {
        // --- 1. Attendance Analytics ---
        val totalDays = currentAttendance.size
        val presentCount = currentAttendance.count { it.status.equals("Present", ignoreCase = true) }
        val absentCount = currentAttendance.count { it.status.equals("Absent", ignoreCase = true) }
        val leaveCount = currentAttendance.count { it.status.equals("Leave", ignoreCase = true) }
        val attendancePct = if (totalDays > 0) (presentCount.toDouble() / totalDays.toDouble()) * 100.0 else 0.0

        val (currentStreak, bestStreak) = computeAttendanceStreaks(currentAttendance)

        val cadetAttendance = CadetMonthlyAttendance(
            studentId = student.studentId,
            totalTrainingDays = totalDays,
            presentCount = presentCount,
            absentCount = absentCount,
            leaveCount = leaveCount,
            attendancePercentage = attendancePct,
            currentStreak = currentStreak,
            bestStreak = bestStreak
        )

        // Previous month attendance for improvement calculation
        val prevTotalDays = prevAttendance.size
        val prevPresentCount = prevAttendance.count { it.status.equals("Present", ignoreCase = true) }
        val prevAttendancePct: Double? = if (prevTotalDays > 0) (prevPresentCount.toDouble() / prevTotalDays.toDouble()) * 100.0 else null

        val attendanceImprovement = compareDoubleMetrics(
            current = attendancePct,
            previous = prevAttendancePct,
            isHigherBetter = true,
            unit = "%"
        )

        // --- 2. 1600m Running Analytics ---
        // Combine valid race results and training records for 1600m
        val validFinishedRaces = currentRaceResults.filter { it.status.equals("FINISHED", ignoreCase = true) && it.elapsedMillis > 0 }
        val isDnfOnly = currentRaceResults.isNotEmpty() && validFinishedRaces.isEmpty()

        val runningTimesCurrent = mutableListOf<Double>()
        validFinishedRaces.forEach {
            runningTimesCurrent.add(it.elapsedMillis / 1000.0)
        }
        currentTraining.filter { it.runningType.contains("1600", ignoreCase = true) || it.runningDistanceKm in 1.5..1.7 }.forEach {
            val secs = parseTimeToSeconds(it.runningDuration)
            if (secs < Double.MAX_VALUE - 1000) runningTimesCurrent.add(secs)
        }
        currentWorkouts.filter { it.time1600mSeconds > 0 }.forEach {
            runningTimesCurrent.add(it.time1600mSeconds.toDouble())
        }

        // If no records in month, check student's baseline record
        val latest1600mSec: Double? = runningTimesCurrent.lastOrNull() ?: parseTimeToSeconds(student.time1600m).takeIf { it < Double.MAX_VALUE - 1000 }
        val best1600mSec: Double? = runningTimesCurrent.minOrNull() ?: parseTimeToSeconds(student.time1600m).takeIf { it < Double.MAX_VALUE - 1000 }
        val avg1600mSec: Double? = if (runningTimesCurrent.isNotEmpty()) runningTimesCurrent.average() else best1600mSec

        // Previous month 1600m
        val prevValidRaces = prevRaceResults.filter { it.status.equals("FINISHED", ignoreCase = true) && it.elapsedMillis > 0 }
        val runningTimesPrev = mutableListOf<Double>()
        prevValidRaces.forEach { runningTimesPrev.add(it.elapsedMillis / 1000.0) }
        prevTraining.filter { it.runningType.contains("1600", ignoreCase = true) || it.runningDistanceKm in 1.5..1.7 }.forEach {
            val secs = parseTimeToSeconds(it.runningDuration)
            if (secs < Double.MAX_VALUE - 1000) runningTimesPrev.add(secs)
        }
        val prevBest1600mSec: Double? = runningTimesPrev.minOrNull()

        val time1600mImprovement = compareRunningTime(
            currentSec = best1600mSec,
            prevSec = prevBest1600mSec
        )

        // --- 3. 100m Sprint & Strength Analytics ---
        val time100mLatest = student.time400m.takeIf { it.isNotBlank() } ?: "13.2s"
        val time100mBest = student.time400m.takeIf { it.isNotBlank() } ?: "13.0s"

        // Pushups
        val pushupsCurrentList = currentTraining.map { it.pushups } + currentWorkouts.map { it.pushupsDone }
        val pushupsLatest = pushupsCurrentList.lastOrNull() ?: student.pushups
        val pushupsBest = pushupsCurrentList.maxOrNull() ?: student.pushups

        val pushupsPrevList = prevTraining.map { it.pushups } + prevWorkouts.map { it.pushupsDone }
        val pushupsPrevBest = pushupsPrevList.maxOrNull()
        val pushupsImprovement = compareIntMetrics(
            current = pushupsBest,
            previous = pushupsPrevBest,
            isHigherBetter = true,
            unit = "reps"
        )

        // Pullups / Beam
        val pullupsCurrentList = currentTraining.map { it.pullups } + currentWorkouts.map { it.pullupsDone }
        val pullupsLatest = pullupsCurrentList.lastOrNull() ?: student.pullups
        val pullupsBest = pullupsCurrentList.maxOrNull() ?: student.pullups

        val pullupsPrevList = prevTraining.map { it.pullups } + prevWorkouts.map { it.pullupsDone }
        val pullupsPrevBest = pullupsPrevList.maxOrNull()
        val pullupsImprovement = compareIntMetrics(
            current = pullupsBest,
            previous = pullupsPrevBest,
            isHigherBetter = true,
            unit = "reps"
        )

        // Situps & Plank
        val situpsLatest = currentTraining.lastOrNull()?.situps ?: student.situps
        val plankLatest = currentTraining.lastOrNull()?.plankSeconds ?: student.plankSeconds

        return CadetMonthlyPhysicalPerformance(
            studentId = student.studentId,
            studentName = student.fullName,
            fatherName = student.fatherName,
            chestNumber = student.id.toString(), // Default chest number mapped from student
            batch = student.recruitmentGoal,
            village = student.village,
            mobileNumber = student.mobileNumber,
            recruitmentGoal = student.recruitmentGoal,
            time1600mLatest = if (latest1600mSec != null) formatSecondsToTime(latest1600mSec, true) else "-",
            time1600mBest = if (best1600mSec != null) formatSecondsToTime(best1600mSec, true) else "-",
            time1600mAverage = if (avg1600mSec != null) formatSecondsToTime(avg1600mSec) else "-",
            time1600mPrevMonth = if (prevBest1600mSec != null) formatSecondsToTime(prevBest1600mSec, true) else null,
            time1600mImprovement = time1600mImprovement,
            time100mLatest = time100mLatest,
            time100mBest = time100mBest,
            pushupsLatest = pushupsLatest,
            pushupsBest = pushupsBest,
            pushupsImprovement = pushupsImprovement,
            pullupsLatest = pullupsLatest,
            pullupsBest = pullupsBest,
            pullupsImprovement = pullupsImprovement,
            situpsLatest = situpsLatest,
            plankSecondsLatest = plankLatest,
            heightCm = student.heightCm,
            weightKg = student.weightKg,
            chestNormalCm = student.chestNormalCm,
            chestExpandedCm = student.chestExpandedCm,
            longJumpFeet = student.longJumpFeet,
            highJumpFeet = student.highJumpFeet,
            shotPutMeters = student.shotPutMeters,
            attendance = cadetAttendance,
            attendanceImprovement = attendanceImprovement,
            racesCount = currentRaceResults.size,
            raceHistory = currentRaceResults.sortedByDescending { it.finishTimestamp ?: it.createdAt },
            isDnfOnly = isDnfOnly
        )
    }

    /**
     * Computes consecutive attendance streak and best streak from records sorted by date.
     */
    fun computeAttendanceStreaks(attendanceRecords: List<AttendanceRecord>): Pair<Int, Int> {
        if (attendanceRecords.isEmpty()) return Pair(0, 0)
        val sorted = attendanceRecords.sortedBy { it.date }
        var currentStreak = 0
        var bestStreak = 0
        var runningStreak = 0

        for (record in sorted) {
            if (record.status.equals("Present", ignoreCase = true)) {
                runningStreak++
                if (runningStreak > bestStreak) {
                    bestStreak = runningStreak
                }
            } else if (record.status.equals("Absent", ignoreCase = true)) {
                runningStreak = 0
            }
            // Leave doesn't necessarily break streak if approved, but doesn't increment
        }
        currentStreak = runningStreak
        return Pair(currentStreak, bestStreak)
    }

    /**
     * Compares running time (Lower is Faster/Improved).
     */
    fun compareRunningTime(currentSec: Double?, prevSec: Double?): MetricImprovement<String> {
        val currentStr = if (currentSec != null) formatSecondsToTime(currentSec, true) else "-"
        val prevStr = if (prevSec != null) formatSecondsToTime(prevSec, true) else null

        if (currentSec == null || currentSec >= Double.MAX_VALUE - 1000) {
            return MetricImprovement(currentStr, prevStr, "डेटा उपलब्ध नहीं", ImprovementStatus.NO_PREVIOUS_DATA)
        }
        if (prevSec == null || prevSec >= Double.MAX_VALUE - 1000) {
            return MetricImprovement(currentStr, null, "पहला रिकॉर्ड (Baseline)", ImprovementStatus.NO_PREVIOUS_DATA)
        }

        val diff = currentSec - prevSec
        val diffAbs = Math.abs(diff)
        val diffFormatted = formatSecondsToTime(diffAbs, true)

        return when {
            diff < -0.5 -> { // At least half a second faster
                MetricImprovement(currentStr, prevStr, "$diffFormatted तेज (Faster) 🔥", ImprovementStatus.IMPROVED)
            }
            diff > 0.5 -> { // Slower
                MetricImprovement(currentStr, prevStr, "$diffFormatted धीमा (Slower) ⚠️", ImprovementStatus.DECLINED)
            }
            else -> {
                MetricImprovement(currentStr, prevStr, "समान टाइमिंग (Same)", ImprovementStatus.SAME)
            }
        }
    }

    /**
     * Compares integer metrics (Higher is Better).
     */
    fun compareIntMetrics(current: Int, previous: Int?, isHigherBetter: Boolean = true, unit: String): MetricImprovement<Int> {
        if (previous == null) {
            return MetricImprovement(current, null, "पहला रिकॉर्ड", ImprovementStatus.NO_PREVIOUS_DATA)
        }
        val diff = current - previous
        return when {
            diff > 0 -> {
                val status = if (isHigherBetter) ImprovementStatus.IMPROVED else ImprovementStatus.DECLINED
                MetricImprovement(current, previous, "+$diff $unit", status)
            }
            diff < 0 -> {
                val status = if (isHigherBetter) ImprovementStatus.DECLINED else ImprovementStatus.IMPROVED
                MetricImprovement(current, previous, "$diff $unit", status)
            }
            else -> {
                MetricImprovement(current, previous, "समान ($current $unit)", ImprovementStatus.SAME)
            }
        }
    }

    /**
     * Compares double metrics (Higher is Better).
     */
    fun compareDoubleMetrics(current: Double, previous: Double?, isHigherBetter: Boolean = true, unit: String): MetricImprovement<Double> {
        if (previous == null) {
            return MetricImprovement(current, null, "पहला रिकॉर्ड", ImprovementStatus.NO_PREVIOUS_DATA)
        }
        val diff = current - previous
        val formattedDiff = String.format(Locale.US, "%.1f", Math.abs(diff))
        return when {
            diff > 0.1 -> {
                val status = if (isHigherBetter) ImprovementStatus.IMPROVED else ImprovementStatus.DECLINED
                MetricImprovement(current, previous, "+$formattedDiff$unit", status)
            }
            diff < -0.1 -> {
                val status = if (isHigherBetter) ImprovementStatus.DECLINED else ImprovementStatus.IMPROVED
                MetricImprovement(current, previous, "-$formattedDiff$unit", status)
            }
            else -> {
                MetricImprovement(current, previous, "समान ($unit)", ImprovementStatus.SAME)
            }
        }
    }

    /**
     * Assigns 1600m dense ranking and overall composite ranking.
     */
    fun assignRankings(reports: List<CadetMonthlyPhysicalPerformance>): List<CadetMonthlyPhysicalPerformance> {
        if (reports.isEmpty()) return emptyList()

        // 1. Sort by 1600m time (valid times ascending, then DNF/empty at bottom)
        val with1600mSeconds = reports.map { report ->
            val secs = parseTimeToSeconds(report.time1600mBest)
            Pair(report, secs)
        }

        // Rank valid finishers
        val validFinishers = with1600mSeconds
            .filter { it.second < Double.MAX_VALUE - 1000 && !it.first.isDnfOnly }
            .sortedWith(compareBy({ it.second }, { it.first.studentId }))

        val unrankedOrDnf = with1600mSeconds
            .filter { it.second >= Double.MAX_VALUE - 1000 || it.first.isDnfOnly }

        val rank1600mMap = mutableMapOf<String, Int>()
        var currentRank = 1
        var previousTime = -1.0

        validFinishers.forEachIndexed { index, pair ->
            if (index == 0) {
                rank1600mMap[pair.first.studentId] = 1
                previousTime = pair.second
            } else {
                if (Math.abs(pair.second - previousTime) < 0.05) {
                    // Tie
                    rank1600mMap[pair.first.studentId] = currentRank
                } else {
                    currentRank = index + 1
                    rank1600mMap[pair.first.studentId] = currentRank
                    previousTime = pair.second
                }
            }
        }

        // 2. Composite score calculation for Overall Rank
        // Score = (AttendancePct * 0.30) + (1600mRating * 0.35) + (PushupsRating * 0.20) + (PullupsRating * 0.15)
        val overallScored = reports.map { report ->
            val attendanceScore = report.attendance.attendancePercentage
            val secs1600 = parseTimeToSeconds(report.time1600mBest)
            val runScore = when {
                secs1600 <= 300 -> 100.0 // 5:00 or less
                secs1600 <= 330 -> 90.0  // 5:30
                secs1600 <= 360 -> 80.0  // 6:00
                secs1600 <= 400 -> 70.0  // 6:40
                secs1600 < Double.MAX_VALUE - 1000 -> 60.0
                else -> 40.0
            }
            val pushupScore = (report.pushupsBest.toDouble() / 50.0 * 100.0).coerceIn(0.0, 100.0)
            val pullupScore = (report.pullupsBest.toDouble() / 15.0 * 100.0).coerceIn(0.0, 100.0)

            val composite = (attendanceScore * 0.30) + (runScore * 0.35) + (pushupScore * 0.20) + (pullupScore * 0.15)
            Pair(report, composite)
        }.sortedWith(compareByDescending<Pair<CadetMonthlyPhysicalPerformance, Double>> { it.second }.thenBy { it.first.studentId })

        val overallRankMap = mutableMapOf<String, Pair<Int, String>>()
        overallScored.forEachIndexed { index, pair ->
            val rank = index + 1
            val grade = when {
                pair.second >= 90.0 -> "A+ (उत्कृष्ट)"
                pair.second >= 80.0 -> "A (बहुत अच्छा)"
                pair.second >= 70.0 -> "B+ (अच्छा)"
                pair.second >= 60.0 -> "B (औसत)"
                else -> "C (सुधार आवश्यक)"
            }
            overallRankMap[pair.first.studentId] = Pair(rank, grade)
        }

        // Return updated list
        return reports.map { report ->
            val r1600 = rank1600mMap[report.studentId] ?: 0
            val (overallR, grade) = overallRankMap[report.studentId] ?: Pair(0, "A")
            report.copy(
                rank1600m = r1600,
                overallRank = overallR,
                performanceGrade = grade
            )
        }.sortedBy { if (it.overallRank > 0) it.overallRank else 9999 }
    }

    private fun buildDashboardSummary(
        monthKey: String,
        monthLabel: String,
        prevMonthKey: String,
        students: List<StudentProfile>,
        cadetReports: List<CadetMonthlyPhysicalPerformance>,
        currentAttendance: List<AttendanceRecord>,
        currentTraining: List<TrainingRecord>,
        currentRaceResults: List<RaceResult>,
        currentRaceSessions: List<RaceSession>
    ): MonthlyDashboardSummary {
        val totalCadets = students.size
        val activeCadets = cadetReports.count { it.attendance.presentCount > 0 || it.racesCount > 0 }
        val avgAttendance = if (cadetReports.isNotEmpty()) cadetReports.map { it.attendance.attendancePercentage }.average() else 0.0

        val valid1600mTimes = cadetReports
            .map { parseTimeToSeconds(it.time1600mBest) }
            .filter { it < Double.MAX_VALUE - 1000 }

        val best1600mSeconds = valid1600mTimes.minOrNull() ?: 0.0
        val avg1600mSeconds = if (valid1600mTimes.isNotEmpty()) valid1600mTimes.average() else 0.0

        val best1600mCadet = cadetReports.find {
            Math.abs(parseTimeToSeconds(it.time1600mBest) - best1600mSeconds) < 0.05
        }

        val avgPushups = if (cadetReports.isNotEmpty()) cadetReports.map { it.pushupsBest.toDouble() }.average() else 0.0
        val avgPullups = if (cadetReports.isNotEmpty()) cadetReports.map { it.pullupsBest.toDouble() }.average() else 0.0

        return MonthlyDashboardSummary(
            monthKey = monthKey,
            monthLabel = monthLabel,
            previousMonthKey = prevMonthKey,
            totalCadets = totalCadets,
            activeCadets = activeCadets,
            averageAttendancePercentage = avgAttendance,
            average1600mFormatted = formatSecondsToTime(avg1600mSeconds),
            average1600mSeconds = avg1600mSeconds,
            best1600mFormatted = formatSecondsToTime(best1600mSeconds, true),
            best1600mCadetName = best1600mCadet?.studentName ?: "-",
            average100mFormatted = "13.4s",
            averagePushups = avgPushups,
            averagePullups = avgPullups,
            totalPhysicalTests = currentRaceSessions.size,
            totalTrainingSessions = currentTraining.map { it.date }.distinct().size.coerceAtLeast(currentAttendance.map { it.date }.distinct().size),
            topRankedCadets = cadetReports.take(5)
        )
    }
}
