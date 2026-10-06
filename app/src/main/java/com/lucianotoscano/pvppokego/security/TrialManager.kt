package com.lucianotoscano.pvppokego.security

import android.content.Context
import android.content.SharedPreferences
import java.util.concurrent.TimeUnit

/**
 * Simple 15-day trial manager.
 * Stores the first launch timestamp in private SharedPreferences.
 * After 15 days the app is considered expired.
 */
object TrialManager {
    private const val PREFS_NAME = "pvppokego_trial_v1"
    private const val KEY_FIRST_LAUNCH = "fl_ts"
    private const val KEY_OBFUSCATED = "fl_chk"
    private const val TRIAL_DURATION_MS = 15L * 24 * 60 * 60 * 1000

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun isTrialValid(context: Context): Boolean {
        val prefs = prefs(context)
        val now = System.currentTimeMillis()
        var firstLaunch = prefs.getLong(KEY_FIRST_LAUNCH, 0L)

        if (firstLaunch == 0L) {
            firstLaunch = now
            prefs.edit()
                .putLong(KEY_FIRST_LAUNCH, firstLaunch)
                .putLong(KEY_OBFUSCATED, firstLaunch xor 0x5A5A5A5A5A5A5A5AL)
                .apply()
            return true
        }

        val check = prefs.getLong(KEY_OBFUSCATED, 0L)
        if (check != (firstLaunch xor 0x5A5A5A5A5A5A5A5AL)) return false

        val elapsed = now - firstLaunch
        return elapsed < TRIAL_DURATION_MS
    }

    fun remainingDays(context: Context): Long {
        val firstLaunch = prefs(context).getLong(KEY_FIRST_LAUNCH, 0L)
        if (firstLaunch == 0L) return 15
        val remainingMs = TRIAL_DURATION_MS - (System.currentTimeMillis() - firstLaunch)
        return TimeUnit.MILLISECONDS.toDays(remainingMs + (24 * 60 * 60 * 1000 - 1))
    }

    fun statusMessage(context: Context): String {
        if (!isTrialValid(context)) {
            return "Versão de teste expirada.\nEste APK só funciona por 15 dias."
        }
        val days = remainingDays(context)
        return when {
            days <= 0 -> "Versão de teste expirada."
            days == 1L -> "Resta 1 dia de teste."
            else -> "Restam $days dias de teste."
        }
    }
}
