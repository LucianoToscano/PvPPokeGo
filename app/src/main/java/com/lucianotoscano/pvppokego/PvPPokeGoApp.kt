package com.lucianotoscano.pvppokego

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.widget.Toast
import com.lucianotoscano.pvppokego.security.TrialManager

class PvPPokeGoApp : Application() {
    override fun onCreate() {
        super.onCreate()

        // Check trial as early as possible.
        if (!TrialManager.isTrialValid(this)) {
            Toast.makeText(
                this,
                TrialManager.statusMessage(this),
                Toast.LENGTH_LONG
            ).show()
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "PvPPokeGo Overlay",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Captura visual e overlay do PvPPokeGo"
                setShowBadge(false)
            }
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    companion object {
        const val CHANNEL_ID = "pvppokego_overlay"
    }
}
