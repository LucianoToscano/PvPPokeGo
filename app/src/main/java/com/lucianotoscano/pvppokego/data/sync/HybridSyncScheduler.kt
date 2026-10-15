package com.lucianotoscano.pvppokego.data.sync

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkerParameters
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

/**
 * Safe default: check daily on unmetered connectivity, without modifying battle data.
 * Remote downloads must not be enabled until a trusted signed/versioned manifest exists.
 */
object HybridSyncScheduler {
    private const val UNIQUE_WORK = "pvppokego-daily-hybrid-data-sync"
    private const val PREFS = "hybrid_data_sync"
    private const val AUTO = "auto"
    private const val WIFI = "wifi_only"

    fun install(context: Context) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val enabled = prefs.getBoolean(AUTO, true)
        val wifiOnly = prefs.getBoolean(WIFI, true)
        if (!enabled) {
            WorkManager.getInstance(context).cancelUniqueWork(UNIQUE_WORK)
            return
        }
        val request = PeriodicWorkRequestBuilder<HybridSyncWorker>(1, TimeUnit.DAYS)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(if (wifiOnly) NetworkType.UNMETERED else NetworkType.CONNECTED)
                    .build()
            )
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            UNIQUE_WORK, ExistingPeriodicWorkPolicy.KEEP, request
        )
    }

    fun configure(context: Context, automatic: Boolean, wifiOnly: Boolean) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putBoolean(AUTO, automatic).putBoolean(WIFI, wifiOnly).apply()
        WorkManager.getInstance(context).cancelUniqueWork(UNIQUE_WORK)
        install(context)
    }
}

class HybridSyncWorker(context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        // No trusted remote manifest endpoint has been configured yet.
        // Fail closed: never download arbitrary data or change the active snapshot.
        applicationContext.getSharedPreferences("hybrid_data_sync", Context.MODE_PRIVATE)
            .edit().putLong("last_scheduler_run_ms", System.currentTimeMillis()).apply()
        return Result.success()
    }
}
