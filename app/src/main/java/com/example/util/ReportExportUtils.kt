package com.example.util

import android.content.Context
import android.content.Intent
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import com.example.data.model.AttendanceRecord
import com.example.data.model.StudentProfile
import com.example.data.model.TestAttempt
import com.example.data.model.TrainingRecord
import com.example.data.model.WorkoutRecord
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Report Generation and Export Utilities for Jai Bajrang Akhada.
 * Generates:
 * 1. Printable & PDF Report Cards via Android PrintManager.
 * 2. Formatted WhatsApp / Social Shareable Text Summaries.
 * 3. CSV/Excel Batch Attendance & Performance Sheets.
 */
object ReportExportUtils {

    /**
     * Generates a rich HTML Cadet Performance Card and triggers Android PrintManager (Save as PDF / Print).
     */
    fun printStudentReportCard(
        context: Context,
        student: StudentProfile?,
        attendanceRecords: List<AttendanceRecord>,
        workoutRecords: List<WorkoutRecord>,
        trainingRecords: List<TrainingRecord>,
        testAttempts: List<TestAttempt>
    ) {
        if (student == null) {
            Toast.makeText(context, "कृपया पहले छात्र का चयन करें!", Toast.LENGTH_SHORT).show()
            return
        }

        val totalAtt = attendanceRecords.size.coerceAtLeast(1)
        val presentCount = attendanceRecords.count { it.status == "Present" }
        val absentCount = attendanceRecords.count { it.status == "Absent" }
        val attPct = (presentCount * 100) / totalAtt

        val best1600m = workoutRecords.map { it.time1600mSeconds }.filter { it > 0 }.minOrNull() ?: 360
        val best1600mStr = "${best1600m / 60} मिनट ${best1600m % 60} सेकंड"

        val avgPushups = if (workoutRecords.isNotEmpty()) workoutRecords.map { it.pushupsDone }.average().toInt() else 35
        val avgSitups = if (workoutRecords.isNotEmpty()) workoutRecords.map { it.situpsDone }.average().toInt() else 40

        val totalTests = testAttempts.size
        val avgTestScore = if (totalTests > 0) testAttempts.map { it.accuracyPercentage }.average().toInt() else 75

        val printDate = SimpleDateFormat("dd MMMM yyyy", Locale("hi", "IN")).format(Date())

        val htmlContent = """
            <!DOCTYPE html>
            <html lang="hi">
            <head>
                <meta charset="UTF-8">
                <title>प्रगति पत्रक - ${student.fullName}</title>
                <style>
                    body {
                        font-family: 'Helvetica Neue', Arial, sans-serif;
                        color: #1a1a1a;
                        margin: 20px;
                        line-height: 1.5;
                    }
                    .header-box {
                        text-align: center;
                        border-bottom: 3px solid #E65100;
                        padding-bottom: 12px;
                        margin-bottom: 20px;
                    }
                    .title {
                        color: #E65100;
                        font-size: 24px;
                        font-weight: bold;
                        margin: 0;
                    }
                    .subtitle {
                        color: #4A6B32;
                        font-size: 14px;
                        font-weight: bold;
                        margin: 4px 0;
                    }
                    .tagline {
                        font-size: 12px;
                        color: #666;
                    }
                    .student-card {
                        background-color: #FFF3E0;
                        border: 1px solid #FFE0B2;
                        border-radius: 8px;
                        padding: 14px;
                        margin-bottom: 20px;
                    }
                    .grid-2 {
                        display: grid;
                        grid-template-columns: 1fr 1fr;
                        gap: 8px;
                    }
                    .grid-col {
                        font-size: 13px;
                    }
                    .section-title {
                        font-size: 16px;
                        font-weight: bold;
                        color: #E65100;
                        border-bottom: 1px solid #ddd;
                        padding-bottom: 4px;
                        margin: 16px 0 10px 0;
                    }
                    table {
                        width: 100%;
                        border-collapse: collapse;
                        margin-bottom: 14px;
                    }
                    th, td {
                        border: 1px solid #ccc;
                        padding: 8px 10px;
                        font-size: 12px;
                        text-align: left;
                    }
                    th {
                        background-color: #f2f2f2;
                        font-weight: bold;
                    }
                    .badge {
                        display: inline-block;
                        padding: 3px 8px;
                        border-radius: 4px;
                        font-size: 11px;
                        font-weight: bold;
                    }
                    .badge-green { background-color: #E8F5E9; color: #2E7D32; }
                    .badge-orange { background-color: #FFF3E0; color: #E65100; }
                    .footer-seal {
                        display: flex;
                        justify-content: space-between;
                        margin-top: 40px;
                        padding-top: 20px;
                    }
                    .sign-box {
                        text-align: center;
                        font-size: 12px;
                        color: #555;
                    }
                </style>
            </head>
            <body>
                <div class="header-box">
                    <h1 class="title">जय बजरंग अखाड़ा, मौरिकला गुफा</h1>
                    <div class="subtitle">गाँव से सेना–पुलिस भर्ती अभियान • शारीरिक व लिखित मूल्यांकन पत्रक</div>
                    <div class="tagline">दिनांक: $printDate | स्थान: मौरिकला गुफा ग्राउंड, आरंग-महासमुंद रोड</div>
                </div>

                <div class="student-card">
                    <div class="grid-2">
                        <div class="grid-col"><strong>कैडेट नाम:</strong> ${student.fullName}</div>
                        <div class="grid-col"><strong>कैडेट ID:</strong> ${student.studentId}</div>
                        <div class="grid-col"><strong>लक्ष्य भर्ती:</strong> ${student.recruitmentGoal}</div>
                        <div class="grid-col"><strong>मोबाइल नंबर:</strong> +91 ${student.mobileNumber}</div>
                        <div class="grid-col"><strong>ऊंचाई / वजन:</strong> ${student.heightCm} cm / ${student.weightKg} kg</div>
                        <div class="grid-col"><strong>प्रशिक्षक (Coach):</strong> ${student.assignedTrainerName}</div>
                    </div>
                </div>

                <div class="section-title">१. उपस्थिति एवं अनुशासन रिकॉर्ड (Attendance & Discipline)</div>
                <table>
                    <tr>
                        <th>कुल दर्ज दिवस</th>
                        <th>उपस्थित (Present)</th>
                        <th>अनुपस्थित (Absent)</th>
                        <th>मासिक उपस्थिति %</th>
                        <th>स्थिति</th>
                    </tr>
                    <tr>
                        <td>$totalAtt दिन</td>
                        <td>$presentCount दिन</td>
                        <td>$absentCount दिन</td>
                        <td><strong>$attPct%</strong></td>
                        <td><span class="badge ${if (attPct >= 80) "badge-green" else "badge-orange"}">${if (attPct >= 80) "उत्कृष्ट" else "सुधार योग्य"}</span></td>
                    </tr>
                </table>

                <div class="section-title">२. शारीरिक क्षमता व ग्राउंड प्रदर्शन (Physical Fitness)</div>
                <table>
                    <tr>
                        <th>इवेंट / टेस्ट</th>
                        <th>श्रेष्ठ / औसत रिकॉर्ड</th>
                        <th>भर्ती मानक स्तर</th>
                    </tr>
                    <tr>
                        <td>1600 मीटर दौड़ (1600m Run)</td>
                        <td><strong>$best1600mStr</strong></td>
                        <td>${if (best1600m <= 345) "उत्कृष्ट (Excellence - 60 Marks)" else "संतोषजनक (Pass Level)"}</td>
                    </tr>
                    <tr>
                        <td>पुश-अप्स (Pushups)</td>
                        <td>$avgPushups रेप्स</td>
                        <td>मानक 40+ पूर्ण</td>
                    </tr>
                    <tr>
                        <td>सिट-अप्स (Situps)</td>
                        <td>$avgSitups रेप्स</td>
                        <td>मानक 40+ पूर्ण</td>
                    </tr>
                </table>

                <div class="section-title">३. लिखित परीक्षा व मॉक टेस्ट मूल्यांकन (Written Exam)</div>
                <table>
                    <tr>
                        <th>कुल मॉक टेस्ट</th>
                        <th>औसत प्राप्तांक %</th>
                        <th>अध्ययन प्रगति</th>
                    </tr>
                    <tr>
                        <td>$totalTests टेस्ट दिए गए</td>
                        <td><strong>$avgTestScore%</strong></td>
                        <td>${if (avgTestScore >= 70) "सफल श्रेणी (Strong Readiness)" else "निरंतर अभ्यास जारी"}</td>
                    </tr>
                </table>

                <div class="footer-seal">
                    <div class="sign-box">
                        <br><br>
                        __________________________<br>
                        <strong>अभिभावक के हस्ताक्षर</strong>
                    </div>
                    <div class="sign-box">
                        <br><br>
                        __________________________<br>
                        <strong>मुख्य कोच / संचालक (जय बजरंग अखाड़ा)</strong>
                    </div>
                </div>
            </body>
            </html>
        """.trimIndent()

        val webView = WebView(context)
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
                val printAdapter = webView.createPrintDocumentAdapter("Cadet_Report_${student.studentId}")
                val builder = PrintAttributes.Builder()
                builder.setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                printManager?.print("Jai_Bajrang_Akhada_${student.fullName}", printAdapter, builder.build())
            }
        }
        webView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
    }

    /**
     * Shares a text summary of student progress via WhatsApp or other messaging apps.
     */
    fun shareStudentProgressSummary(
        context: Context,
        student: StudentProfile?,
        attendanceRecords: List<AttendanceRecord>,
        workoutRecords: List<WorkoutRecord>,
        testAttempts: List<TestAttempt>
    ) {
        if (student == null) {
            Toast.makeText(context, "कृपया पहले छात्र का चयन करें!", Toast.LENGTH_SHORT).show()
            return
        }

        val totalAtt = attendanceRecords.size.coerceAtLeast(1)
        val presentCount = attendanceRecords.count { it.status == "Present" }
        val attPct = (presentCount * 100) / totalAtt
        val best1600m = workoutRecords.map { it.time1600mSeconds }.filter { it > 0 }.minOrNull() ?: 360
        val best1600Str = "${best1600m / 60}m ${best1600m % 60}s"

        val avgAccuracy = if (testAttempts.isNotEmpty()) {
            testAttempts.map { it.accuracyPercentage }.average().toInt()
        } else {
            75
        }

        val summary = """
            🚩 *जय बजरंग अखाड़ा, मौरिकला गुफा* 🚩
            *कैडेट मासिक प्रगति पत्रक (Progress Report)*
            ------------------------------------
            👤 *नाम:* ${student.fullName}
            🆔 *ID:* ${student.studentId}
            🎖️ *लक्ष्य भर्ती:* ${student.recruitmentGoal}
            📞 *मोबाइल:* ${student.mobileNumber}
            
            📊 *उपस्थिति (Attendance):* $attPct% ($presentCount/$totalAtt दिन उपस्थित)
            🏃 *1600m रनिंग टाइम:* $best1600Str
            📝 *मॉक टेस्ट औसत:* $avgAccuracy%
            
            *प्रशिक्षक टिप्पणी:* नियमित अभ्यास और अनुशासन सराहनीय है। 
            जय हिन्द! 🇮🇳
        """.trimIndent()

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "प्रगति पत्रक - ${student.fullName}")
            putExtra(Intent.EXTRA_TEXT, summary)
        }
        context.startActivity(Intent.createChooser(intent, "प्रगति रिपोर्ट शेयर करें"))
    }

    /**
     * Generates CSV String for batch attendance.
     */
    fun generateAttendanceCsv(
        selectedDate: String,
        students: List<StudentProfile>,
        allAttendance: List<AttendanceRecord>
    ): String {
        val sb = StringBuilder()
        sb.append("Student ID,Full Name,Mobile,Target Goal,Date,Status,Remarks\n")

        val attMap = allAttendance.filter { it.date == selectedDate }.associateBy { it.studentId }

        for (student in students) {
            val rec = attMap[student.studentId]
            val status = rec?.status ?: "Not Marked"
            val remarks = rec?.remarks ?: ""
            sb.append("\"${student.studentId}\",\"${student.fullName}\",\"${student.mobileNumber}\",\"${student.recruitmentGoal}\",\"$selectedDate\",\"$status\",\"$remarks\"\n")
        }

        return sb.toString()
    }

    /**
     * Exports Batch Attendance into CSV format and shares it via Intent.
     */
    fun exportAttendanceCsv(
        context: Context,
        selectedDate: String,
        students: List<StudentProfile>,
        allAttendance: List<AttendanceRecord>
    ) {
        val csvData = generateAttendanceCsv(selectedDate, students, allAttendance)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/comma-separated-values"
            putExtra(Intent.EXTRA_SUBJECT, "उपस्थिति रिपोर्ट - जय बजरंग अखाड़ा ($selectedDate)")
            putExtra(Intent.EXTRA_TEXT, csvData)
        }
        context.startActivity(Intent.createChooser(intent, "उपस्थिति CSV एक्सपोर्ट करें (Excel / Sheets)"))
    }
}
