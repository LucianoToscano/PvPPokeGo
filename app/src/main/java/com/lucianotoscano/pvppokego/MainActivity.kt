package com.lucianotoscano.pvppokego

import android.Manifest
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.core.content.ContextCompat
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.lucianotoscano.pvppokego.data.SettingsRepository
import com.lucianotoscano.pvppokego.overlay.BattleOverlayService
import com.lucianotoscano.pvppokego.ui.SettingsScreen

class MainActivity : ComponentActivity() {
    private lateinit var settingsRepo: SettingsRepository
    private var overlayGranted by mutableStateOf(false)
    /** True after capture permission succeeds until the service-running flag catches up. */
    private var overlayStarting = false
    private val projectionManager by lazy {
        getSystemService(MediaProjectionManager::class.java)
    }

    private val notificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    private val projectionLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val data = result.data ?: return@registerForActivityResult
            if (result.resultCode != RESULT_OK) return@registerForActivityResult

            overlayStarting = true
            val service = Intent(this, BattleOverlayService::class.java).apply {
                putExtra(BattleOverlayService.EXTRA_RESULT_CODE, result.resultCode)
                putExtra(BattleOverlayService.EXTRA_RESULT_DATA, data)
                // The permission result returns to this Activity. Keep the overlay hidden here;
                // onPause below will reliably notify the service when the user goes back to GO.
                putExtra(BattleOverlayService.EXTRA_APP_VISIBLE, true)
            }
            ContextCompat.startForegroundService(this, service)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        settingsRepo = SettingsRepository(this)
        overlayGranted = Settings.canDrawOverlays(this)

        if (Build.VERSION.SDK_INT >= 33) {
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        setContent {
            SettingsScreen(
                repository = settingsRepo,
                overlayPermissionGranted = overlayGranted,
                onRequestOverlayPermission = ::requestOverlayPermission,
                onStartOverlay = ::startOverlay,
                onStopOverlay = ::stopOverlay,
                onResetBattle = ::resetBattle
            )
        }
    }

    override fun onResume() {
        super.onResume()
        overlayGranted = Settings.canDrawOverlays(this)
        if (::settingsRepo.isInitialized && settingsRepo.overlayServiceRunning) {
            overlayStarting = false
            startService(
                Intent(this, BattleOverlayService::class.java)
                    .setAction(BattleOverlayService.ACTION_APP_FOREGROUND)
            )
        }
    }

    override fun onPause() {
        // Do not rely only on the SharedPreferences running flag: on a fresh start the
        // service may not have reached onCreate yet when this Activity is backgrounded.
        if (::settingsRepo.isInitialized && (settingsRepo.overlayServiceRunning || overlayStarting)) {
            startService(
                Intent(this, BattleOverlayService::class.java)
                    .setAction(BattleOverlayService.ACTION_APP_BACKGROUND)
            )
        }
        super.onPause()
    }

    private fun requestOverlayPermission() {
        startActivity(
            Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
        )
    }

    private fun startOverlay() {
        if (!Settings.canDrawOverlays(this)) {
            requestOverlayPermission()
            return
        }
        projectionLauncher.launch(projectionManager.createScreenCaptureIntent())
    }

    private fun stopOverlay() {
        overlayStarting = false
        startService(
            Intent(this, BattleOverlayService::class.java)
                .setAction(BattleOverlayService.ACTION_STOP)
        )
    }

    private fun resetBattle() {
        startService(
            Intent(this, BattleOverlayService::class.java)
                .setAction(BattleOverlayService.ACTION_RESET)
        )
    }
}
