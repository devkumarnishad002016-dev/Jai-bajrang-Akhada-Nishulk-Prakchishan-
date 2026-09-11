package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.model.TopicDocument
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * File management utilities for Topic-wise PDF & Excel documents.
 * Handles storage, copying from device URI, opening via FileProvider, sharing,
 * in-app CSV/Excel preview data parsing, and sample file generation.
 */
object TopicFileUtils {

    private const val DOCUMENTS_DIR = "topic_documents"

    fun getDocumentsDirectory(context: Context): File {
        val dir = File(context.filesDir, DOCUMENTS_DIR)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    fun formatFileSize(bytes: Long): String {
        return when {
            bytes >= 1024 * 1024 -> String.format(Locale.US, "%.1f MB", bytes.toDouble() / (1024 * 1024))
            bytes >= 1024 -> "${bytes / 1024} KB"
            else -> "$bytes B"
        }
    }

    /**
     * Copies a file from a content Uri (picked by user via file picker) into internal app storage.
     * Returns Pair(absoluteFilePath, formattedSize)
     */
    fun savePickedUriToInternalStorage(
        context: Context,
        uri: Uri,
        originalName: String? = null
    ): Pair<String, String> {
        val dir = getDocumentsDirectory(context)
        val timestamp = System.currentTimeMillis()
        val safeName = (originalName ?: "document_${timestamp}").replace("[^a-zA-Z0-9._-]".toRegex(), "_")
        val destFile = File(dir, "${timestamp}_$safeName")

        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(destFile).use { output ->
                input.copyTo(output)
            }
        }

        val sizeFormatted = formatFileSize(destFile.length())
        return Pair(destFile.absolutePath, sizeFormatted)
    }

    /**
     * Ensures the document file exists on disk. If not, generates a functional
     * study content file (PDF format or CSV/Excel spreadsheet) so it can be opened/shared.
     */
    fun ensureFileExistsOnDisk(context: Context, doc: TopicDocument): File {
        if (doc.filePath.isNotBlank()) {
            val existing = File(doc.filePath)
            if (existing.exists() && existing.length() > 0) {
                return existing
            }
        }

        val dir = getDocumentsDirectory(context)
        val targetName = doc.fileName.ifBlank {
            if (doc.isPdf) "doc_${doc.topicId}.pdf" else "sheet_${doc.topicId}.csv"
        }
        val file = File(dir, targetName)
        if (!file.exists() || file.length() == 0L) {
            FileOutputStream(file).use { fos ->
                if (doc.isPdf) {
                    val pdfContent = generateSamplePdfBytes(doc)
                    fos.write(pdfContent)
                } else {
                    val csvContent = generateSampleSpreadsheetContent(doc)
                    fos.write(csvContent.toByteArray(Charsets.UTF_8))
                }
            }
        }
        return file
    }

    /**
     * Generates a basic compliant PDF byte array with Jai Bajrang Akhada watermark & study content.
     */
    private fun generateSamplePdfBytes(doc: TopicDocument): ByteArray {
        val contentText = """
            JAI BAJRANG AKHADA - DEFENCE & POLICE ACADEMY
            =======================================================
            अध्याय / टॉपिक: ${doc.title}
            टॉपिक कोड: ${doc.topicId} | विषय कोड: ${doc.subjectId}
            दिनांक: ${doc.uploadDate} | फ़ाइल: ${doc.fileName}
            
            विवरण एवं मुख्य बिंदु:
            ${doc.description.ifBlank { "सेना एवं पुलिस भर्ती लिखित परीक्षा हेतु महत्वपूर्ण अध्ययन सामग्री एवं हल प्रश्न।" }}
            
            1. मुख्य सूत्र एवं नियम (Key Concepts & Rules):
               - सभी परिभाषाएं, शॉर्टकट ट्रिक्स एवं अवधारणाओं का गहन अभ्यास करें।
               - विगत 10 वर्षों के हल प्रश्न-पत्रों के पैटर्न पर आधारित।
            
            2. अभ्यास निर्देश (Practice Instructions):
               - प्रतिदिन कम से कम 30 मिनट इस टॉपिक के प्रश्नों का अभ्यास करें।
               - समय प्रबंधन (Time Management) और एक्यूरेसी पर विशेष ध्यान दें।
               - किसी भी संशय के समाधान हेतु अखाड़ा मुख्य प्रशिक्षक से संपर्क करें।
            
            जय बजरंग अखाड़ा - लक्ष्य: भारतीय सेना एवं पुलिस सेवा में शत-प्रतिशत चयन!
        """.trimIndent()

        // Construct standard valid PDF 1.4 stream
        val sb = StringBuilder()
        sb.append("%PDF-1.4\n")
        sb.append("1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n")
        sb.append("2 0 obj\n<< /Type /Pages /Kids [3 0 R] /Count 1 >>\nendobj\n")
        sb.append("3 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 612 792] /Contents 4 0 R /Resources << /Font << /F1 5 0 R >> >> >>\nendobj\n")
        
        val escapedText = contentText
            .replace("\\", "\\\\")
            .replace("(", "\\(")
            .replace(")", "\\)")
            .lines()
            .take(25)
            .mapIndexed { i, line ->
                val asciiLine = line.filter { it.code < 128 }.ifBlank { "Jai Bajrang Akhada Study Material - Topic: ${doc.topicId}" }
                "1 0 0 1 50 ${720 - (i * 22)} Tm (${asciiLine}) Tj"
            }.joinToString("\n")

        val stream = "BT\n/F1 12 Tf\n$escapedText\nET"
        sb.append("4 0 obj\n<< /Length ${stream.toByteArray().size} >>\nstream\n$stream\nendstream\nendobj\n")
        sb.append("5 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>\nendobj\n")
        sb.append("xref\n0 6\n0000000000 65535 f \n0000000009 00000 n \n0000000058 00000 n \n0000000115 00000 n \n")
        val streamOffset = sb.length
        sb.append("trailer\n<< /Size 6 /Root 1 0 R >>\nstartxref\n$streamOffset\n%%EOF\n")
        return sb.toString().toByteArray(Charsets.ISO_8859_1)
    }

    /**
     * Generates a rich CSV / Spreadsheet with sample questions & table columns.
     */
    fun generateSampleSpreadsheetContent(doc: TopicDocument): String {
        val sb = StringBuilder()
        sb.append("क्रमांक (S.No.),टॉपिक (Topic),प्रश्न (Question),विकल्प A (Option A),विकल्प B (Option B),विकल्प C (Option C),विकल्प D (Option D),सही उत्तर (Correct),कठिनाई (Difficulty)\n")
        sb.append("1,\"${doc.title}\",\"सबसे छोटी अभाज्य संख्या कौन सी है?\",\"1\",\"2\",\"3\",\"0\",\"B\",\"सरल\"\n")
        sb.append("2,\"${doc.title}\",\"दो लगातार सम संख्याओं का म.स. (HCF) सदैव क्या होता है?\",\"1\",\"2\",\"4\",\"संख्याओं पर निर्भर\",\"B\",\"सरल\"\n")
        sb.append("3,\"${doc.title}\",\"प्रथम 10 प्राकृत संख्याओं का औसत क्या होगा?\",\"5.5\",\"5.0\",\"6.0\",\"4.5\",\"A\",\"मध्यम\"\n")
        sb.append("4,\"${doc.title}\",\"1 से 100 तक कुल कितनी अभाज्य संख्याएं (Prime Numbers) होती हैं?\",\"21\",\"25\",\"27\",\"30\",\"B\",\"मध्यम\"\n")
        sb.append("5,\"${doc.title}\",\"यदि किसी संख्या का 20% मान 50 है, तो वह संख्या क्या होगी?\",\"200\",\"250\",\"300\",\"150\",\"B\",\"मध्यम\"\n")
        sb.append("6,\"${doc.title}\",\"एक ट्रेन 72 किमी/घंटे की गति से चल रही है, उसकी मीटर/सेकंड में गति क्या होगी?\",\"15 m/s\",\"20 m/s\",\"25 m/s\",\"18 m/s\",\"B\",\"सरल\"\n")
        sb.append("7,\"${doc.title}\",\"साधारण ब्याज सूत्र I = (P × R × T) / 100 में P क्या दर्शाता है?\",\"दर\",\"समय\",\"मूलधन\",\"मिश्रधन\",\"C\",\"सरल\"\n")
        sb.append("8,\"${doc.title}\",\"कार्यक्षमता और समय में कैसा संबंध होता है?\",\"सीधा अनुपात\",\"व्युत्क्रमानुपात (उल्टा)\",\"कोई संबंध नहीं\",\"बराबर\",\"B\",\"मध्यम\"\n")
        return sb.toString()
    }

    /**
     * Parses spreadsheet / CSV data rows for in-app table preview.
     */
    fun parseSpreadsheetRows(context: Context, doc: TopicDocument): List<List<String>> {
        val file = ensureFileExistsOnDisk(context, doc)
        return try {
            val lines = file.readLines(Charsets.UTF_8)
            lines.map { line ->
                // Simple CSV split handling quotes
                parseCsvLine(line)
            }.filter { it.isNotEmpty() }
        } catch (e: Exception) {
            listOf(
                listOf("त्रुटि", "फ़ाइल पढ़ने में असमर्थ"),
                listOf("विवरण", e.localizedMessage ?: "Unknown error")
            )
        }
    }

    private fun parseCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        var cur = StringBuilder()
        var inQuotes = false
        for (char in line) {
            when {
                char == '\"' -> inQuotes = !inQuotes
                char == ',' && !inQuotes -> {
                    result.add(cur.toString().trim())
                    cur = StringBuilder()
                }
                else -> cur.append(char)
            }
        }
        result.add(cur.toString().trim())
        return result
    }

    /**
     * Opens document in external viewer via FileProvider Intent.
     */
    fun openDocument(context: Context, doc: TopicDocument) {
        val file = ensureFileExistsOnDisk(context, doc)
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val mimeType = if (doc.isPdf) "application/pdf" else "text/comma-separated-values"
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, mimeType)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(intent, "${doc.title} खोलें"))
        } catch (e: Exception) {
            Toast.makeText(context, "फ़ाइल खोलने हेतु उपयुक्त ऐप उपलब्ध नहीं है।", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Shares document with cadets via Intent.
     */
    fun shareDocument(context: Context, doc: TopicDocument) {
        val file = ensureFileExistsOnDisk(context, doc)
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val mimeType = if (doc.isPdf) "application/pdf" else "text/comma-separated-values"
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "स्टडी मटेरियल - ${doc.title} (जय बजरंग अखाड़ा)")
                putExtra(
                    Intent.EXTRA_TEXT,
                    "जय बजरंग अखाड़ा - ${doc.title}\nटॉपिक: ${doc.topicId}\nफ़ाइल: ${doc.fileName} (${doc.fileSize})\n${doc.description}"
                )
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "दस्तावेज़ शेयर करें"))
        } catch (e: Exception) {
            Toast.makeText(context, "दस्तावेज़ शेयर करने में समस्या आई।", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Deletes physical file from internal storage if present.
     */
    fun deleteLocalFile(context: Context, filePath: String) {
        if (filePath.isNotBlank()) {
            val f = File(filePath)
            if (f.exists()) {
                f.delete()
            }
        }
    }
}
