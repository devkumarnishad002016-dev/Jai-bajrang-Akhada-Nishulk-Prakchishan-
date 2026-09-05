package com.example.util

import com.example.data.db.AppDao
import com.example.data.model.Trainer
import java.security.MessageDigest
import java.security.SecureRandom

/**
 * Secure Authentication & Credential Manager for Jai Bajrang Akhada Trainers/Coaches.
 * Enforces salted SHA-256 password hashing with constant-time comparison.
 * Plain text passwords are NEVER stored in source code or database.
 */
object CoachAuthManager {

    private const val PEPPER = "JBA_COACH_SECURITY_PEPPER_2026"

    // Seeded cryptographic salts & hashes for the 4 selected jawans
    // Note: Passwords are NEVER stored as plain text.
    const val COACH_001_ID = "JBA-COACH-001"
    const val COACH_001_NAME = "Pushpsen Nishad"
    const val COACH_001_ACHIEVEMENT = "CRPF SSC GD"
    private const val COACH_001_SALT = "a7e1f48c3b9d0265"
    private const val COACH_001_HASH = "26cc16f8bae11313f771d3b59f630ae013d87fbe800a57a002ee85897f2dc214"

    const val COACH_002_ID = "JBA-COACH-002"
    const val COACH_002_NAME = "Naveen Kumar Nagarchi"
    const val COACH_002_ACHIEVEMENT = "Agniveer Tradesman"
    private const val COACH_002_SALT = "b3d8c19e5a7f2046"
    private const val COACH_002_HASH = "4530100064fdde453841fcdc8e72346f6ea6fbb78f4e1dd24de69e97ca6ee0ad"

    const val COACH_003_ID = "JBA-COACH-003"
    const val COACH_003_NAME = "DUMESHWARI Nishad"
    const val COACH_003_ACHIEVEMENT = "CG Police Constable GD"
    private const val COACH_003_SALT = "c9f2e7a1d4b68035"
    private const val COACH_003_HASH = "42b3fb898ac8d2f4933bc0de11d071dd506114d15573e39267e47adfba263271"

    const val COACH_004_ID = "JBA-COACH-004"
    const val COACH_004_NAME = "Kamlesh Kumar Sahu"
    const val COACH_004_ACHIEVEMENT = "ITBP"
    private const val COACH_004_SALT = "d5b0a6f3e8c17924"
    private const val COACH_004_HASH = "81add996e2c8b6c9b753ee68a5ab9fcf6556d35ace85bb89563b1bb96f0f1815"

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
     * Computes salted SHA-256 hash of the coach password with pepper.
     */
    fun hashPassword(password: String, saltHex: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val combined = "$saltHex:${password.trim()}:$PEPPER".toByteArray(Charsets.UTF_8)
        val hashBytes = md.digest(combined)
        return bytesToHex(hashBytes)
    }

    /**
     * Creates new password credentials (hash, salt) for a coach.
     */
    fun createPasswordCredentials(password: String): Pair<String, String> {
        val salt = generateSalt()
        val hash = hashPassword(password, salt)
        return Pair(hash, salt)
    }

    /**
     * Verifies an entered password against stored hash and salt using constant-time comparison.
     */
    fun verifyPassword(inputPassword: String, storedHash: String, storedSalt: String): Boolean {
        if (storedHash.isBlank() || storedSalt.isBlank() || inputPassword.isBlank()) {
            return false
        }
        val computedHash = hashPassword(inputPassword, storedSalt)
        return slowEquals(storedHash, computedHash)
    }

    /**
     * Verifies coach credentials for a given Trainer entity.
     */
    fun verifyCoachCredentials(trainer: Trainer, inputPassword: String): Boolean {
        val trimmed = inputPassword.trim()
        if (trimmed.isEmpty()) return false

        // 1. If trainer entity has custom hash and salt in Room DB
        if (trainer.passwordHash.isNotBlank() && trainer.passwordSalt.isNotBlank()) {
            return verifyPassword(trimmed, trainer.passwordHash, trainer.passwordSalt)
        }

        // 2. Fallback check for initial seeded credentials matching coachId
        val (expectedHash, expectedSalt) = when (trainer.coachId.trim().uppercase()) {
            COACH_001_ID -> Pair(COACH_001_HASH, COACH_001_SALT)
            COACH_002_ID -> Pair(COACH_002_HASH, COACH_002_SALT)
            COACH_003_ID -> Pair(COACH_003_HASH, COACH_003_SALT)
            COACH_004_ID -> Pair(COACH_004_HASH, COACH_004_SALT)
            else -> Pair("", "")
        }

        if (expectedHash.isNotBlank() && expectedSalt.isNotBlank()) {
            return verifyPassword(trimmed, expectedHash, expectedSalt)
        }

        return false
    }

    /**
     * Validates new password policy (minimum 6 characters, cannot be empty).
     */
    fun validatePasswordPolicy(password: String): String? {
        val trimmed = password.trim()
        if (trimmed.length < 6) {
            return "पासवर्ड कम से कम 6 अक्षरों का होना चाहिए! (Minimum 6 characters)"
        }
        if (trimmed == "123456" || trimmed == "password" || trimmed == "admin123") {
            return "यह पासवर्ड बहुत कमजोर है। कृपया कोई मजबूत पासवर्ड चुनें।"
        }
        return null
    }

    /**
     * Returns the 4 initial selected jawans' Trainer profiles.
     */
    fun getInitialCoachesList(): List<Trainer> {
        return listOf(
            Trainer(
                id = 0,
                name = COACH_001_NAME,
                achievement = COACH_001_ACHIEVEMENT,
                role = RolePermissionManager.ROLE_TRAINER,
                coachId = COACH_001_ID,
                experience = "CRPF SSC GD चयनित जवान",
                serviceBackground = "CRPF (केंद्रीय रिजर्व पुलिस बल)",
                specialization = "1600m रनिंग, ग्राउंड ट्रेनिंग, CRPF फिजिकल मानक",
                introduction = "जय बजरंग अखाड़ा के जांबाज जवान। मौरिकला गुफा अखाड़े में युवाओं को 1600m दौड़ एवं फिजिकल फिटनेस में मार्गदर्शन।",
                contactNumber = "9826100011",
                displayOrder = 1,
                passwordHash = COACH_001_HASH,
                passwordSalt = COACH_001_SALT,
                forcePasswordChange = true,
                active = true,
                createdAt = System.currentTimeMillis()
            ),
            Trainer(
                id = 0,
                name = COACH_002_NAME,
                achievement = COACH_002_ACHIEVEMENT,
                role = RolePermissionManager.ROLE_TRAINER,
                coachId = COACH_002_ID,
                experience = "अग्निवीर ट्रेड्समैन चयनित जवान",
                serviceBackground = "भारतीय सेना (Indian Army - Agniveer)",
                specialization = "फिजिकल फिटनेस, बीम (Pull-ups), पुश-अप्स, ग्राउंड डिसिप्लिन",
                introduction = "भारतीय सेना में चयनित जांबाज। कैडेट्स को बीम, 9-फीट डिच और फिजिकल स्टेमिना का सटीक अभ्यास कराते हैं।",
                contactNumber = "9826100012",
                displayOrder = 2,
                passwordHash = COACH_002_HASH,
                passwordSalt = COACH_002_SALT,
                forcePasswordChange = true,
                active = true,
                createdAt = System.currentTimeMillis()
            ),
            Trainer(
                id = 0,
                name = COACH_003_NAME,
                achievement = COACH_003_ACHIEVEMENT,
                role = RolePermissionManager.ROLE_TRAINER,
                coachId = COACH_003_ID,
                experience = "छत्तीसगढ़ पुलिस कांस्टेबल GD चयनित जवान",
                serviceBackground = "छत्तीसगढ़ पुलिस (CG Police)",
                specialization = "महिला कैडेट फिजिकल गाइडेंस, 800m/1600m रनिंग, लॉन्ग जंप",
                introduction = "छत्तीसगढ़ पुलिस में चयनित वीरांगना। अखाड़े की महिला कैडेट्स एवं युवाओं को पुलिस भर्ती फिजिकल टेस्ट का विशेष प्रशिक्षण।",
                contactNumber = "9826100013",
                displayOrder = 3,
                passwordHash = COACH_003_HASH,
                passwordSalt = COACH_003_SALT,
                forcePasswordChange = true,
                active = true,
                createdAt = System.currentTimeMillis()
            ),
            Trainer(
                id = 0,
                name = COACH_004_NAME,
                achievement = COACH_004_ACHIEVEMENT,
                role = RolePermissionManager.ROLE_TRAINER,
                coachId = COACH_004_ID,
                experience = "ITBP चयनित जवान",
                serviceBackground = "ITBP (भारत-तिब्बत सीमा पुलिस)",
                specialization = "हाई एल्टीट्यूड एंड्योरेंस, ITBP फिजिकल मानक, लॉन्ग रनिंग, स्टेमिना",
                introduction = "ITBP में चयनित जांबाज जवान। कैडेट्स में असीम सहनशक्ति, लॉन्ग रनिंग और मानसिक मजबूती का विकास।",
                contactNumber = "9826100014",
                displayOrder = 4,
                passwordHash = COACH_004_HASH,
                passwordSalt = COACH_004_SALT,
                forcePasswordChange = true,
                active = true,
                createdAt = System.currentTimeMillis()
            )
        )
    }

    /**
     * Seeds initial 4 coaches into Room SQLite database if they don't already exist.
     * Prevents duplicate coach accounts on app re-installs or database open.
     */
    suspend fun seedInitialCoaches(dao: AppDao) {
        val seededList = getInitialCoachesList()
        for (coach in seededList) {
            val existing = dao.getTrainerByCoachId(coach.coachId)
            if (existing == null) {
                dao.insertTrainer(coach)
            } else {
                // If existing record is missing achievement or coach auth fields, update them safely
                if (existing.achievement.isBlank() || existing.passwordHash.isBlank()) {
                    val updated = existing.copy(
                        coachId = coach.coachId,
                        achievement = coach.achievement,
                        role = RolePermissionManager.ROLE_TRAINER,
                        passwordHash = if (existing.passwordHash.isNotBlank()) existing.passwordHash else coach.passwordHash,
                        passwordSalt = if (existing.passwordSalt.isNotBlank()) existing.passwordSalt else coach.passwordSalt,
                        forcePasswordChange = existing.forcePasswordChange
                    )
                    dao.updateTrainer(updated)
                }
            }
        }
    }

    private fun bytesToHex(bytes: ByteArray): String {
        val sb = StringBuilder()
        for (b in bytes) {
            sb.append(String.format("%02x", b))
        }
        return sb.toString()
    }

    private fun slowEquals(a: String, b: String): Boolean {
        var diff = a.length xor b.length
        val minLen = Math.min(a.length, b.length)
        for (i in 0 until minLen) {
            diff = diff or (a[i].code xor b[i].code)
        }
        return diff == 0
    }
}
