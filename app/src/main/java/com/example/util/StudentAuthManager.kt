package com.example.util

import com.example.data.model.StudentProfile
import java.security.MessageDigest
import java.security.SecureRandom

object StudentAuthManager {

    private const val PEPPER = "JBA_AKHADA_STUDENT_AUTH_PEPPER_2026"

    /**
     * Generates a cryptographically secure random 16-byte hex salt.
     */
    fun generateSalt(): String {
        val random = SecureRandom()
        val saltBytes = ByteArray(16)
        random.nextBytes(saltBytes)
        return bytesToHex(saltBytes)
    }

    /**
     * Computes salted SHA-256 hash of the student's password.
     */
    fun hashPassword(password: String, saltHex: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val combined = "$saltHex:${password.trim()}:$PEPPER".toByteArray(Charsets.UTF_8)
        val hashBytes = md.digest(combined)
        return bytesToHex(hashBytes)
    }

    /**
     * Creates new password credentials (hash, salt) for a student.
     */
    fun createPasswordCredentials(password: String): Pair<String, String> {
        val salt = generateSalt()
        val hash = hashPassword(password, salt)
        return Pair(hash, salt)
    }

    /**
     * Verifies an entered password against the student's stored hash and salt.
     * Uses constant-time comparison to protect against timing attacks.
     */
    fun verifyPassword(inputPassword: String, storedHash: String, storedSalt: String): Boolean {
        if (storedHash.isBlank() || storedSalt.isBlank()) {
            return false
        }
        val computedHash = hashPassword(inputPassword, storedSalt)
        return slowEquals(storedHash, computedHash)
    }

    /**
     * Verifies student credentials against their StudentProfile.
     */
    fun verifyStudentCredentials(student: StudentProfile, inputPassword: String): Boolean {
        val trimmed = inputPassword.trim()
        if (trimmed.isEmpty()) return false

        // If student profile has configured hash and salt, verify cryptographically
        if (student.passwordHash.isNotBlank() && student.passwordSalt.isNotBlank()) {
            return verifyPassword(trimmed, student.passwordHash, student.passwordSalt)
        }

        return false
    }

    /**
     * Validates password strength for registration and password changes.
     */
    fun validatePasswordPolicy(password: String): String? {
        val trimmed = password.trim()
        if (trimmed.length < 6) {
            return "पासवर्ड कम से कम 6 अक्षरों का होना चाहिए! (Minimum 6 characters)"
        }
        return null
    }

    // Default seeded credentials for the 2 real accounts
    const val DEFAULT_UJALA_SALT = "a1f94c7b2e38d0158c49bf63e21074a9"
    const val DEFAULT_JANESHWARI_SALT = "b8e42d7f1a905c339d62fe81c47385b2"

    val DEFAULT_UJALA_HASH: String by lazy {
        hashPassword("Ujala@JBA2026", DEFAULT_UJALA_SALT)
    }

    val DEFAULT_JANESHWARI_HASH: String by lazy {
        hashPassword("Janeshwari@JBA2026", DEFAULT_JANESHWARI_SALT)
    }

    private fun bytesToHex(bytes: ByteArray): String {
        val sb = StringBuilder()
        for (b in bytes) {
            sb.append(String.format("%02x", b))
        }
        return sb.toString()
    }

    /**
     * Constant-time comparison to prevent timing attacks.
     */
    private fun slowEquals(a: String, b: String): Boolean {
        var diff = a.length xor b.length
        val minLen = minOf(a.length, b.length)
        for (i in 0 until minLen) {
            diff = diff or (a[i].code xor b[i].code)
        }
        return diff == 0
    }
}
