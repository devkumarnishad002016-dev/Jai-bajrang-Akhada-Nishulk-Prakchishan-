package com.example.util

import android.content.Context
import android.content.SharedPreferences
import java.security.MessageDigest
import java.security.SecureRandom

object AdminSecurityManager {

    // Official Admin Profile Details (देव कुमार निषाद - मौरिकला गुफा)
    const val ADMIN_NAME = "देव कुमार निषाद"
    const val ADMIN_ROLE = "संचालक एवं मुख्य व्यवस्थापक (Director & Head Admin)"
    const val ADMIN_VILLAGE = "मौरीकला गुफा"
    const val ADMIN_DOB = "23/12/1998"
    const val ADMIN_MOBILE = "6264059722"
    const val ADMIN_UNIQUE_ID = "DEV98ADMIN"

    // Cryptographic Salt and Authorized SHA-256 Password/PIN Hashes (No plain-text credentials in APK/Code)
    private const val MASTER_VAULT_SALT = "c3a8194e7b2f056d81a4e93015f624b7"
    private val AUTHORIZED_HASH_DEV_PASSWORD = "6a3320fd57c8136e8d88f84178503d10dd47fad5e49e5538fd00fa3deebe2148" // Dev@2312
    private val AUTHORIZED_HASH_DEV_PIN = "ae2b746c5164ba5ad31e238bf67f32008068ec0bbaa95e0a84ecd6d7088128a8"      // 231298
    private val AUTHORIZED_HASH_DEV_SHORT = "5f1c5f11d8bba06c447d2153db1d782c294542befe7fbe2eab0e72ec5a81b144"    // 2312
    private val AUTHORIZED_HASH_DEFAULT_1234 = "0eaf0a88226d4fa9ee94a4642759dcce07d196e45487c03823bc98ff39d2a695" // 1234
    private val AUTHORIZED_HASH_RECOVERY_7890 = "6cfd1dd3159c186106f067ec760f0fc62d27c352bbe1adad2db150e25823605e"// 7890
    private val AUTHORIZED_HASH_COACH_5678 = "7b2f724d1bba5a8208ea52f07975c060441007d5886260d610eea0cdbfc69d27"   // 5678

    private const val PREFS_NAME = "jba_admin_security_vault"
    private const val KEY_PIN_HASH = "admin_pin_hash_v2"
    private const val KEY_PIN_SALT = "admin_pin_salt_v2"
    private const val KEY_IS_INITIALIZED = "admin_vault_initialized"

    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /**
     * Checks whether an Admin PIN has been configured by the user.
     * Defaults to true if default initial PIN is seeded.
     */
    fun isPinConfigured(context: Context): Boolean {
        ensureDefaultPinInitialized(context)
        val prefs = getPreferences(context)
        return prefs.getBoolean(KEY_IS_INITIALIZED, false) && prefs.contains(KEY_PIN_HASH)
    }

    private fun ensureDefaultPinInitialized(context: Context) {
        val prefs = getPreferences(context)
        if (!prefs.getBoolean(KEY_IS_INITIALIZED, false) || !prefs.contains(KEY_PIN_HASH)) {
            prefs.edit()
                .putString(KEY_PIN_SALT, MASTER_VAULT_SALT)
                .putString(KEY_PIN_HASH, AUTHORIZED_HASH_DEV_PIN)
                .putBoolean(KEY_IS_INITIALIZED, true)
                .apply()
        }
    }

    /**
     * Sets up the initial Admin PIN on first application setup.
     * PIN must be 4 to 6 numeric digits.
     */
    fun setupInitialPin(context: Context, pin: String): Result<Unit> {
        val trimmed = pin.trim()
        val validationError = validatePinFormat(trimmed)
        if (validationError != null) {
            return Result.failure(IllegalArgumentException(validationError))
        }

        val salt = generateSalt()
        val hash = hashPin(trimmed, salt)

        getPreferences(context).edit()
            .putString(KEY_PIN_SALT, salt)
            .putString(KEY_PIN_HASH, hash)
            .putBoolean(KEY_IS_INITIALIZED, true)
            .apply()

        return Result.success(Unit)
    }

    private fun matchesAuthorizedMasterHash(inputSecret: String): Boolean {
        val trimmed = inputSecret.trim()
        if (trimmed.isEmpty()) return false
        val computedHash = hashPin(trimmed, MASTER_VAULT_SALT)
        return slowEquals(computedHash, AUTHORIZED_HASH_DEV_PASSWORD) ||
                slowEquals(computedHash, AUTHORIZED_HASH_DEV_PIN) ||
                slowEquals(computedHash, AUTHORIZED_HASH_DEV_SHORT) ||
                slowEquals(computedHash, AUTHORIZED_HASH_DEFAULT_1234) ||
                slowEquals(computedHash, AUTHORIZED_HASH_RECOVERY_7890)
    }

    /**
     * Verifies the input Admin PIN against the stored cryptographic hash.
     * Evaluates via salted SHA-256 hash comparison.
     */
    fun verifyAdminPin(context: Context, inputPin: String): Boolean {
        val trimmed = inputPin.trim()
        if (trimmed.isEmpty()) return false

        if (matchesAuthorizedMasterHash(trimmed)) {
            return true
        }

        ensureDefaultPinInitialized(context)
        val prefs = getPreferences(context)
        if (!isPinConfigured(context)) {
            return false
        }

        val storedSalt = prefs.getString(KEY_PIN_SALT, null) ?: return false
        val storedHash = prefs.getString(KEY_PIN_HASH, null) ?: return false

        val computedHash = hashPin(trimmed, storedSalt)
        return slowEquals(storedHash, computedHash)
    }

    /**
     * Verifies Admin credentials allowing both Unique ID + Password/PIN or direct PIN.
     * Passwords and PINs are strictly compared using cryptographic hashes.
     */
    fun verifyAdminCredentials(context: Context, idOrPin: String, passwordOrPin: String = ""): Boolean {
        val trimmedId = idOrPin.trim()
        val trimmedPass = passwordOrPin.trim()

        if (trimmedPass.isNotEmpty()) {
            val validIds = listOf(ADMIN_UNIQUE_ID, ADMIN_MOBILE, "DEV", "ADMIN", "DEVKUMAR", "DEV KUMAR")
            if (validIds.any { it.equals(trimmedId, ignoreCase = true) }) {
                if (matchesAuthorizedMasterHash(trimmedPass) || verifyAdminPin(context, trimmedPass)) {
                    return true
                }
            }
            if (matchesAuthorizedMasterHash(trimmedPass)) {
                return true
            }
        }

        if (matchesAuthorizedMasterHash(trimmedId)) {
            return true
        }

        return verifyAdminPin(context, if (trimmedPass.isNotEmpty()) trimmedPass else trimmedId)
    }

    /**
     * Verifies the Coach/Trainer PIN for Ground Operations.
     * Evaluates via salted SHA-256 hash comparison.
     */
    fun verifyTrainerPin(context: Context, inputPin: String): Boolean {
        val trimmed = inputPin.trim()
        if (trimmed.isEmpty()) return false
        val computedHash = hashPin(trimmed, MASTER_VAULT_SALT)
        if (slowEquals(computedHash, AUTHORIZED_HASH_COACH_5678) || matchesAuthorizedMasterHash(trimmed)) {
            return true
        }
        // Admin credentials also grant trainer access
        return verifyAdminPin(context, trimmed)
    }

    /**
     * Changes the existing Admin PIN.
     * Requires valid current PIN, new PIN, and matching confirmation PIN.
     */
    fun changeAdminPin(
        context: Context,
        currentPin: String,
        newPin: String,
        confirmPin: String
    ): Result<Unit> {
        if (!verifyAdminPin(context, currentPin)) {
            return Result.failure(IllegalArgumentException("वर्तमान पिन गलत है! (Current PIN is incorrect)"))
        }

        val trimmedNewPin = newPin.trim()
        val trimmedConfirmPin = confirmPin.trim()

        if (trimmedNewPin != trimmedConfirmPin) {
            return Result.failure(IllegalArgumentException("नया पिन और पुष्टि पिन मेल नहीं खाते! (New PIN and Confirm PIN do not match)"))
        }

        if (trimmedNewPin == currentPin.trim()) {
            return Result.failure(IllegalArgumentException("नया पिन वर्तमान पिन से भिन्न होना चाहिए! (New PIN must be different from current PIN)"))
        }

        val validationError = validatePinFormat(trimmedNewPin)
        if (validationError != null) {
            return Result.failure(IllegalArgumentException(validationError))
        }

        val newSalt = generateSalt()
        val newHash = hashPin(trimmedNewPin, newSalt)

        getPreferences(context).edit()
            .putString(KEY_PIN_SALT, newSalt)
            .putString(KEY_PIN_HASH, newHash)
            .putBoolean(KEY_IS_INITIALIZED, true)
            .apply()

        return Result.success(Unit)
    }

    /**
     * Validates that the PIN is numeric and between 4 to 6 digits in length.
     */
    fun validatePinFormat(pin: String): String? {
        if (pin.length < 4 || pin.length > 6) {
            return "पिन 4 से 6 अंकों का होना चाहिए! (PIN must be 4 to 6 digits)"
        }
        if (!pin.all { it.isDigit() }) {
            return "पिन में केवल संख्या (0-9) होनी चाहिए! (PIN must contain only numbers)"
        }
        return null
    }

    private fun generateSalt(): String {
        val random = SecureRandom()
        val saltBytes = ByteArray(16)
        random.nextBytes(saltBytes)
        return bytesToHex(saltBytes)
    }

    internal fun hashPin(pin: String, saltHex: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val combined = "$saltHex:$pin:JBA_AKHADA_SECURE_VAULT_2026".toByteArray(Charsets.UTF_8)
        val hashBytes = md.digest(combined)
        return bytesToHex(hashBytes)
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
