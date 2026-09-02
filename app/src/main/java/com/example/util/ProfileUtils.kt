package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.ui.graphics.Color
import com.example.data.model.StudentProfile
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object ProfileUtils {

    /**
     * Calculates age automatically from Date of Birth string.
     * Supports formats: yyyy-MM-dd, dd/MM/yyyy, dd-MM-yyyy, yyyy/MM/dd.
     */
    fun calculateAgeFromDob(dobStr: String): Int {
        if (dobStr.isBlank()) return 0
        val trimmed = dobStr.trim()
        val formats = listOf(
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()),
            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()),
            SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()),
            SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
        )
        for (sdf in formats) {
            try {
                sdf.isLenient = false
                val parsedDate = sdf.parse(trimmed)
                if (parsedDate != null) {
                    val dobCal = Calendar.getInstance().apply { time = parsedDate }
                    val today = Calendar.getInstance()
                    var age = today.get(Calendar.YEAR) - dobCal.get(Calendar.YEAR)
                    if (today.get(Calendar.DAY_OF_YEAR) < dobCal.get(Calendar.DAY_OF_YEAR)) {
                        age--
                    }
                    return age.coerceAtLeast(0)
                }
            } catch (_: Exception) {
                // Try next pattern
            }
        }
        return 0
    }

    /**
     * Calculates BMI from Height (cm) and Weight (kg).
     * Formula: weight / (heightInMeters * heightInMeters)
     */
    fun calculateBmi(heightCm: Double, weightKg: Double): Double {
        if (heightCm <= 0 || weightKg <= 0) return 0.0
        val heightMeters = heightCm / 100.0
        val bmi = weightKg / (heightMeters * heightMeters)
        return Math.round(bmi * 10.0) / 10.0
    }

    data class BmiCategory(
        val bmiValue: Double,
        val labelHindi: String,
        val labelEnglish: String,
        val descriptionHindi: String,
        val color: Color
    )

    /**
     * Returns full BMI assessment category and color.
     */
    fun getBmiCategory(heightCm: Double, weightKg: Double): BmiCategory {
        val bmi = calculateBmi(heightCm, weightKg)
        return when {
            bmi <= 0.0 -> BmiCategory(
                bmiValue = 0.0,
                labelHindi = "अमान्य माप",
                labelEnglish = "Invalid",
                descriptionHindi = "कृपया सही ऊंचाई और वजन दर्ज करें",
                color = Color.Gray
            )
            bmi < 18.5 -> BmiCategory(
                bmiValue = bmi,
                labelHindi = "कम वजन (Underweight)",
                labelEnglish = "Underweight",
                descriptionHindi = "भर्ती मानक हेतु संतुलित पोषण व वजन बढ़ाना आवश्यक",
                color = Color(0xFFF59E0B) // Amber
            )
            bmi in 18.5..24.9 -> BmiCategory(
                bmiValue = bmi,
                labelHindi = "सामान्य एवं फिट (Normal)",
                labelEnglish = "Normal / Fit",
                descriptionHindi = "आदर्श शारीरिक बनावट – सेना/पुलिस फिजिकल हेतु उपयुक्त",
                color = Color(0xFF10B981) // Green
            )
            bmi in 25.0..29.9 -> BmiCategory(
                bmiValue = bmi,
                labelHindi = "अधिक वजन (Overweight)",
                labelEnglish = "Overweight",
                descriptionHindi = "दौड़ व कार्डियो वर्कआउट बढ़ाकर वजन नियंत्रित करें",
                color = Color(0xFFF97316) // Orange
            )
            else -> BmiCategory(
                bmiValue = bmi,
                labelHindi = "मोटापा (Obese)",
                labelEnglish = "Obese",
                descriptionHindi = "सख्त डाइट व अत्यधिक कैलोरी बर्न ट्रेनिंग आवश्यक",
                color = Color(0xFFEF4444) // Red
            )
        }
    }

    /**
     * Generates next sequential unique Student ID in format JBA-2026-001, JBA-2026-002, etc.
     */
    fun generateNextStudentId(existingStudents: List<StudentProfile>): String {
        val year = Calendar.getInstance().get(Calendar.YEAR).coerceAtLeast(2026)
        var maxSequence = 0
        for (student in existingStudents) {
            val id = student.studentId.trim()
            if (id.startsWith("JBA-")) {
                val parts = id.split("-")
                if (parts.size >= 3) {
                    val seq = parts[2].toIntOrNull() ?: 0
                    if (seq > maxSequence) {
                        maxSequence = seq
                    }
                }
            }
        }
        val nextSeq = maxSequence + 1
        return String.format(Locale.getDefault(), "JBA-%d-%03d", year, nextSeq)
    }

    /**
     * Saves a camera bitmap locally to app internal storage and returns the local file URI string.
     */
    fun saveBitmapToInternalStorage(context: Context, bitmap: Bitmap, studentId: String): String {
        return try {
            val photoDir = File(context.filesDir, "student_photos").apply { mkdirs() }
            val cleanId = studentId.replace("[^a-zA-Z0-9]".toRegex(), "_")
            val photoFile = File(photoDir, "photo_${cleanId}_${System.currentTimeMillis()}.jpg")
            FileOutputStream(photoFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            }
            Uri.fromFile(photoFile).toString()
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    /**
     * Copies a gallery content URI locally to app internal storage to avoid permission expiration.
     */
    fun saveUriToInternalStorage(context: Context, sourceUri: Uri, studentId: String): String {
        return try {
            val photoDir = File(context.filesDir, "student_photos").apply { mkdirs() }
            val cleanId = studentId.replace("[^a-zA-Z0-9]".toRegex(), "_")
            val photoFile = File(photoDir, "photo_${cleanId}_${System.currentTimeMillis()}.jpg")
            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                FileOutputStream(photoFile).use { output ->
                    input.copyTo(output)
                }
            }
            Uri.fromFile(photoFile).toString()
        } catch (e: Exception) {
            e.printStackTrace()
            sourceUri.toString()
        }
    }
}
