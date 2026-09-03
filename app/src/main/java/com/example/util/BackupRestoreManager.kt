package com.example.util

import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.example.data.db.AppDao
import com.example.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Handles full database offline backup to JSON and restores data safely into Room DB.
 */
object BackupRestoreManager {

    /**
     * Generates a complete JSON backup of the entire local database.
     */
    suspend fun createBackupJson(dao: AppDao): String = withContext(Dispatchers.IO) {
        val root = JSONObject()
        root.put("app", "Jai Bajrang Akhada")
        root.put("version", "1.0.0")
        root.put("backupDate", SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()))

        // 1. Students
        val students = dao.getAllStudentsDirect()
        val studentArray = JSONArray()
        for (s in students) {
            val obj = JSONObject().apply {
                put("studentId", s.studentId)
                put("fullName", s.fullName)
                put("fatherName", s.fatherName)
                put("mobileNumber", s.mobileNumber)
                put("village", s.village)
                put("dob", s.dob)
                put("age", s.age)
                put("gender", s.gender)
                put("education", s.education)
                put("recruitmentGoal", s.recruitmentGoal)
                put("batchName", s.batchName)
                put("assignedTrainerName", s.assignedTrainerName)
                put("assignedTrainerId", s.assignedTrainerId)
                put("heightCm", s.heightCm)
                put("weightKg", s.weightKg)
                put("chestNormalCm", s.chestNormalCm)
                put("chestExpandedCm", s.chestExpandedCm)
                put("time1600m", s.time1600m)
                put("time5km", s.time5km)
                put("pushups", s.pushups)
                put("situps", s.situps)
                put("pullups", s.pullups)
                put("squats", s.squats)
                put("plankSeconds", s.plankSeconds)
                put("overallScore", s.overallScore)
                put("longJumpFeet", s.longJumpFeet)
                put("highJumpFeet", s.highJumpFeet)
                put("shotPutMeters", s.shotPutMeters)
            }
            studentArray.put(obj)
        }
        root.put("students", studentArray)

        // 2. Attendance
        val attendance = dao.getAllAttendanceDirect()
        val attArray = JSONArray()
        for (a in attendance) {
            val obj = JSONObject().apply {
                put("studentId", a.studentId)
                put("date", a.date)
                put("status", a.status)
                put("remarks", a.remarks)
            }
            attArray.put(obj)
        }
        root.put("attendance", attArray)

        // 3. Training
        val training = dao.getAllTrainingRecordsDirect()
        val trainingArray = JSONArray()
        for (t in training) {
            val obj = JSONObject().apply {
                put("studentId", t.studentId)
                put("date", t.date)
                put("runningDistanceKm", t.runningDistanceKm)
                put("runningDuration", t.runningDuration)
                put("runningType", t.runningType)
                put("pushups", t.pushups)
                put("situps", t.situps)
                put("pullups", t.pullups)
                put("squats", t.squats)
                put("plankSeconds", t.plankSeconds)
                put("trainerNotes", t.trainerNotes)
                put("timestamp", t.timestamp)
            }
            trainingArray.put(obj)
        }
        root.put("training", trainingArray)

        // 4. Notices
        val notices = dao.getAllNoticesDirect()
        val noticeArray = JSONArray()
        for (n in notices) {
            val obj = JSONObject().apply {
                put("title", n.title)
                put("content", n.content)
                put("category", n.category)
                put("date", n.date)
                put("author", n.author)
                put("isUrgent", n.isUrgent)
                put("priority", n.priority)
                put("isPinned", n.isPinned)
            }
            noticeArray.put(obj)
        }
        root.put("notices", noticeArray)

        root.toString(2)
    }

    /**
     * Shares the JSON backup string to external storage, WhatsApp, Drive, or Email.
     */
    fun shareBackup(context: Context, jsonBackup: String) {
        val dateStr = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "जय बजरंग अखाड़ा - डेटाबेस बैकअप ($dateStr)")
            putExtra(Intent.EXTRA_TEXT, jsonBackup)
        }
        context.startActivity(Intent.createChooser(intent, "डेटाबेस बैकअप फ़ाइल शेयर / सहेजें"))
    }

    /**
     * Safely restores students and attendance from a JSON backup.
     */
    suspend fun restoreDatabase(dao: AppDao, jsonString: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val root = JSONObject(jsonString)
            var restoredStudents = 0
            var restoredAttendance = 0

            // Restore Students
            if (root.has("students")) {
                val array = root.getJSONArray("students")
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val student = StudentProfile(
                        studentId = obj.optString("studentId", "JBA-2026-999"),
                        fullName = obj.optString("fullName", "कैडेट"),
                        fatherName = obj.optString("fatherName", ""),
                        mobileNumber = obj.optString("mobileNumber", "9876543210"),
                        village = obj.optString("village", "मौरिकला"),
                        dob = obj.optString("dob", "2004-05-15"),
                        age = obj.optInt("age", 21),
                        gender = obj.optString("gender", "Male"),
                        education = obj.optString("education", "12th Pass"),
                        recruitmentGoal = obj.optString("recruitmentGoal", "CG Police"),
                        batchName = obj.optString("batchName", "सुबह आर्मी स्पेशल बैच"),
                        assignedTrainerName = obj.optString("assignedTrainerName", "देव कुमार निषाद (मुख्य कोच)"),
                        assignedTrainerId = obj.optString("assignedTrainerId", "TR-001"),
                        heightCm = obj.optDouble("heightCm", 168.0),
                        weightKg = obj.optDouble("weightKg", 62.0),
                        chestNormalCm = obj.optDouble("chestNormalCm", 81.0),
                        chestExpandedCm = obj.optDouble("chestExpandedCm", 86.0),
                        time1600m = obj.optString("time1600m", "5:45"),
                        time5km = obj.optString("time5km", "22:30"),
                        pushups = obj.optInt("pushups", 40),
                        situps = obj.optInt("situps", 45),
                        pullups = obj.optInt("pullups", 10),
                        squats = obj.optInt("squats", 50),
                        plankSeconds = obj.optInt("plankSeconds", 90),
                        overallScore = obj.optInt("overallScore", 80),
                        longJumpFeet = obj.optDouble("longJumpFeet", 14.5),
                        highJumpFeet = obj.optDouble("highJumpFeet", 4.2),
                        shotPutMeters = obj.optDouble("shotPutMeters", 7.0)
                    )
                    dao.insertStudent(student)
                    restoredStudents++
                }
            }

            // Restore Attendance
            if (root.has("attendance")) {
                val array = root.getJSONArray("attendance")
                val attList = mutableListOf<AttendanceRecord>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    attList.add(
                        AttendanceRecord(
                            studentId = obj.optString("studentId"),
                            date = obj.optString("date"),
                            status = obj.optString("status", "Present"),
                            remarks = obj.optString("remarks", "")
                        )
                    )
                }
                if (attList.isNotEmpty()) {
                    dao.insertAttendanceList(attList)
                    restoredAttendance = attList.size
                }
            }

            Result.success("डेटा सफलतापूर्वक पुनर्स्थापित हुआ! ($restoredStudents छात्र, $restoredAttendance हाजिरी रिकॉर्ड)")
        } catch (e: Exception) {
            Result.failure(Exception("बैकअप पढ़ने में त्रुटि: ${e.localizedMessage ?: "अमान्य JSON प्रारूप"}"))
        }
    }
}
