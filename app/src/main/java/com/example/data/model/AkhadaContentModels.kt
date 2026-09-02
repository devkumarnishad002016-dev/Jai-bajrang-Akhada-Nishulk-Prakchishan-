package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trainers")
data class Trainer(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val photoUri: String = "",
    val experience: String, // e.g. "अनुभव: 8+ वर्ष"
    val serviceBackground: String, // e.g. "पूर्व सैन्य / खेल प्रशिक्षक"
    val specialization: String, // e.g. "1600m रनिंग, फिजिकल एंड्योरेंस, ग्राउंड ट्रेनिंग"
    val introduction: String, // e.g. "मौरिकला गुफा अखाड़े में युवाओं को अनुशासित प्रशिक्षण"
    val contactNumber: String = "",
    val displayOrder: Int = 1
)

@Entity(tableName = "gallery_items")
data class GalleryItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val photoUri: String = "",
    val category: String = "Physical Training", // Physical Training, Running, Ground Training, Written Classes, Events, Awareness Campaign, Other
    val caption: String = "",
    val date: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "success_stories")
data class SuccessStory(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentName: String,
    val village: String,
    val recruitmentExam: String, // e.g. "Indian Army GD", "State Police"
    val year: String, // e.g. "2025"
    val photoUri: String = "",
    val story: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "contact_info")
data class ContactInfo(
    @PrimaryKey val id: Long = 1, // Single active record
    val organisation: String = "जय बजरंग अखाड़ा – गांव से सेना/पुलिस भर्ती अभियान",
    val trainingCentre: String = "जय बजरंग अखाड़ा – मौरीकला गुफा",
    val contactPerson: String = "मुख्य प्रशिक्षक / अखाड़ा संचालक",
    val mobile: String = "9876543210",
    val address: String = "मौरिकला गुफा, ग्राम मौरिकला, ब्लॉक व जिला",
    val whatsapp: String = "9876543210",
    val workingHours: String = "प्रातः 05:00 - 08:30 | सायं 04:30 - 07:30"
)
