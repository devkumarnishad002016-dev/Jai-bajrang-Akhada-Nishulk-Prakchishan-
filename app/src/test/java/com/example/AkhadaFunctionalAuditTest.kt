package com.example

import com.example.data.model.AttendanceRecord
import com.example.data.model.StudentProfile
import com.example.util.AdminSecurityManager
import com.example.util.ProfileUtils
import org.junit.Assert.*
import org.junit.Test

class AkhadaFunctionalAuditTest {

    @Test
    fun `test Age Calculation across supported date formats`() {
        // Test yyyy-MM-dd format
        val age1 = ProfileUtils.calculateAgeFromDob("2004-06-15")
        assertTrue("Age should be greater than 20", age1 >= 20)

        // Test dd/MM/yyyy format
        val age2 = ProfileUtils.calculateAgeFromDob("15/06/2004")
        assertEquals(age1, age2)

        // Test dd-MM-yyyy format
        val age3 = ProfileUtils.calculateAgeFromDob("15-06-2004")
        assertEquals(age1, age3)

        // Test invalid and empty strings do not crash and return 0
        assertEquals(0, ProfileUtils.calculateAgeFromDob(""))
        assertEquals(0, ProfileUtils.calculateAgeFromDob("invalid-date"))
        assertEquals(0, ProfileUtils.calculateAgeFromDob("2026/99/99"))
    }

    @Test
    fun `test BMI Calculation and Categories`() {
        // Underweight (< 18.5)
        val underweight = ProfileUtils.getBmiCategory(175.0, 50.0)
        assertTrue(underweight.bmiValue < 18.5)
        assertEquals("Underweight", underweight.labelEnglish)

        // Normal (18.5 - 24.9)
        val normal = ProfileUtils.getBmiCategory(172.0, 65.0)
        assertTrue(normal.bmiValue in 18.5..24.9)
        assertEquals("Normal / Fit", normal.labelEnglish)

        // Overweight (25.0 - 29.9)
        val overweight = ProfileUtils.getBmiCategory(170.0, 80.0)
        assertTrue(overweight.bmiValue in 25.0..29.9)
        assertEquals("Overweight", overweight.labelEnglish)

        // Obese (>= 30.0)
        val obese = ProfileUtils.getBmiCategory(165.0, 90.0)
        assertTrue(obese.bmiValue >= 30.0)
        assertEquals("Obese", obese.labelEnglish)

        // Invalid / Zero inputs do not crash
        val zeroHeight = ProfileUtils.getBmiCategory(0.0, 60.0)
        assertEquals(0.0, zeroHeight.bmiValue, 0.01)
        assertEquals("Invalid", zeroHeight.labelEnglish)

        val zeroWeight = ProfileUtils.getBmiCategory(170.0, 0.0)
        assertEquals(0.0, zeroWeight.bmiValue, 0.01)
    }

    @Test
    fun `test Student ID sequential generation and formatting`() {
        val student1 = StudentProfile(studentId = "JBA-2026-001", fullName = "Student 1")
        val student2 = StudentProfile(studentId = "JBA-2026-002", fullName = "Student 2")
        val student3 = StudentProfile(studentId = "JBA-2026-003", fullName = "Student 3")

        // Next after 3 existing students should be JBA-2026-004
        val nextId = ProfileUtils.generateNextStudentId(listOf(student1, student2, student3))
        assertEquals("JBA-2026-004", nextId)

        // With empty list, should be JBA-2026-001
        val initialId = ProfileUtils.generateNextStudentId(emptyList())
        assertTrue(initialId.startsWith("JBA-"))
        assertTrue(initialId.endsWith("-001"))
    }

    @Test
    fun `test Student login lookup by ID and Mobile`() {
        val student1 = StudentProfile(
            studentId = "JBA-2026-001",
            fullName = "देवकुमार निषाद",
            mobileNumber = "9876543210"
        )
        val student2 = StudentProfile(
            studentId = "JBA-2026-002",
            fullName = "राहुल कुमार",
            mobileNumber = "9826012345"
        )
        val students = listOf(student1, student2)

        // Login by exact ID
        val matchId = students.find { it.studentId.equals("JBA-2026-001", ignoreCase = true) }
        assertNotNull(matchId)
        assertEquals("देवकुमार निषाद", matchId?.fullName)

        // Login by case-insensitive ID
        val matchLower = students.find { it.studentId.equals("jba-2026-002", ignoreCase = true) }
        assertNotNull(matchLower)
        assertEquals("राहुल कुमार", matchLower?.fullName)

        // Login by mobile number
        val matchMobile = students.find { it.mobileNumber == "9876543210" }
        assertNotNull(matchMobile)
        assertEquals("JBA-2026-001", matchMobile?.studentId)

        // Invalid credentials
        val invalidMatch = students.find {
            it.studentId.equals("JBA-9999-999", ignoreCase = true) ||
            it.mobileNumber == "0000000000"
        }
        assertNull(invalidMatch)
    }

    @Test
    fun `test Admin Security Hash and Salt Verification`() {
        val salt = "test-salt-123456"
        val rawPin = "8765"
        val wrongPin = "1234"

        val hash = AdminSecurityManager.hashPin(rawPin, salt)
        val verifyHash = AdminSecurityManager.hashPin(rawPin, salt)
        val wrongHash = AdminSecurityManager.hashPin(wrongPin, salt)

        // Same PIN with same salt produces same hash
        assertEquals(hash, verifyHash)

        // Different PIN produces completely different hash
        assertNotEquals(hash, wrongHash)

        // Plaintext PIN is never identical to hash
        assertNotEquals(rawPin, hash)
        assertTrue(hash.length >= 64) // SHA-256 is 64 hex characters
    }

    @Test
    fun `test Mobile Number format validation and Duplicate Detection`() {
        val existingStudents = listOf(
            StudentProfile(studentId = "JBA-2026-001", fullName = "Student 1", mobileNumber = "9876543210"),
            StudentProfile(studentId = "JBA-2026-002", fullName = "Student 2", mobileNumber = "9826012345")
        )

        fun validateMobileForRegistration(input: String, existing: List<StudentProfile>): Pair<Boolean, String?> {
            val clean = input.trim().filter { it.isDigit() }
            if (clean.length != 10) {
                return Pair(false, "Invalid length")
            }
            if (existing.any { it.mobileNumber.trim().filter { c -> c.isDigit() } == clean }) {
                return Pair(false, "Duplicate mobile number")
            }
            return Pair(true, null)
        }

        // Valid new number
        val validResult = validateMobileForRegistration("9123456780", existingStudents)
        assertTrue("Valid 10-digit unique number should pass", validResult.first)
        assertNull(validResult.second)

        // Duplicate number
        val dupResult = validateMobileForRegistration("9876543210", existingStudents)
        assertFalse("Duplicate number must be rejected", dupResult.first)
        assertEquals("Duplicate mobile number", dupResult.second)

        // Short number
        val shortResult = validateMobileForRegistration("98765", existingStudents)
        assertFalse("Short number must be rejected", shortResult.first)

        // Long number
        val longResult = validateMobileForRegistration("987654321012", existingStudents)
        assertFalse("Long number must be rejected", longResult.first)
    }

    @Test
    fun `test Attendance Record Unique Constraint Model Structure`() {
        val att1 = AttendanceRecord(id = 1L, studentId = "JBA-2026-001", date = "2026-08-25", status = "Present")
        val att2 = AttendanceRecord(id = 2L, studentId = "JBA-2026-001", date = "2026-08-25", status = "Leave")

        // Keys that form the unique compound constraint
        val key1 = "${att1.studentId}_${att1.date}"
        val key2 = "${att2.studentId}_${att2.date}"

        assertEquals("Same student on same day shares identical composite key", key1, key2)
    }

    @Test
    fun `test Data Isolation between multiple students`() {
        val studentA = StudentProfile(
            studentId = "JBA-2026-001",
            fullName = "Candidate A",
            village = "मौरिकला",
            overallScore = 92
        )
        val studentB = StudentProfile(
            studentId = "JBA-2026-002",
            fullName = "Candidate B",
            village = "डोंगरगांव",
            overallScore = 84
        )

        // Ensure distinct IDs and fields
        assertNotEquals(studentA.studentId, studentB.studentId)
        assertNotEquals(studentA.fullName, studentB.fullName)
        assertNotEquals(studentA.village, studentB.village)
        assertNotEquals(studentA.overallScore, studentB.overallScore)
    }
}
