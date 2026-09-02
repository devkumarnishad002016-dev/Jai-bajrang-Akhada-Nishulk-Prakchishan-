package com.example.util

import android.content.Context
import android.content.SharedPreferences
import java.security.MessageDigest
import java.security.SecureRandom

object AdminSecurityManager {

    private const val PREFS_NAME = "jba_admin_security_vault"
    private const val KEY_PIN_HASH = "admin_pin_hash_v2"
    private const val KEY_PIN_SALT = "admin_pin_salt_v2"
    private const val KEY_IS_INITIALIZED = "admin_vault_initialized"

    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /**
     * Checks whether an Admin PIN has been configured by the user.
     * Defaults to true if default initial PIN 1234 is seeded.
     */
    fun isPinConfigured(context: Context): Boolean {
        ensureDefaultPinInitialized(context)
        val prefs = getPreferences(context)
        return prefs.getBoolean(KEY_IS_INITIALIZED, false) && prefs.contains(KEY_PIN_HASH)
    }

    private fun ensureDefaultPinInitialized(context: Context) {
        val prefs = getPreferences(context)
        if (!prefs.getBoolean(KEY_IS_INITIALIZED, false) || !prefs.contains(KEY_PIN_HASH)) {
            val salt = generateSalt()
            val hash = hashPin("1234", salt)
            prefs.edit()
                .putString(KEY_PIN_SALT, salt)
                .putString(KEY_PIN_HASH, hash)
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

    /**
     * Verifies the input Admin PIN against the stored cryptographic hash.
     * Also supports master recovery PIN 7890 and default PIN 1234.
     */
    fun verifyAdminPin(context: Context, inputPin: String): Boolean {
        val trimmed = inputPin.trim()
        if (trimmed == "7890" || trimmed == "1234") {
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
     * Verifies the Coach/Trainer PIN for Ground Operations.
     * Supports default coach PIN 5678, admin PIN, or master recovery PIN 7890.
     */
    fun verifyTrainerPin(context: Context, inputPin: String): Boolean {
        val trimmed = inputPin.trim()
        if (trimmed == "5678" || trimmed == "1234" || trimmed == "7890") {
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
