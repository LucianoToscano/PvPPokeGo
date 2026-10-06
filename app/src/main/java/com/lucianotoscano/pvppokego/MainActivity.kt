package com.lucianotoscano.pvppokego

import android.Manifest
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.core.content.ContextCompat
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.lucianotoscano.pvppokego.data.BattleHistoryRepository
import com.lucianotoscano.pvppokego.data.SettingsRepository
import com.lucianotoscano.pvppokego.overlay.BattleOverlayService
import com.lucianotoscano.pvppokego.ui.SettingsScreen

class MainActivity : ComponentActivity() {
    private lateinit var settingsRepo: SettingsRepository
    private lateinit var historyRepo: BattleHistoryRepository
    private var overlayGranted by mutableStateOf(false)
    private var historyRevision by mutableStateOf(0)
    private var pendingHistoryExportBattleId: Long? = null
    /** True after capture permission succeeds until the service-running flag catches up. */
    private var overlayStarting = false
    private val projectionManager by lazy {
        getSystemService(MediaProjectionManager::class.java)
    }

    private val notificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    private val exportSettingsLauncher =
        registerForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
            if (uri == null || !::settingsRepo.isInitialized) return@registerForActivityResult
            runCatching {
                contentResolver.openOutputStream(uri)?.bufferedWriter()?.use { writer ->
                    writer.write(settingsRepo.exportBackupJson())
                } ?: error("Não foi possível abrir o arquivo")
            }.onSuccess {
                Toast.makeText(this, "Configurações exportadas", Toast.LENGTH_SHORT).show()
            }.onFailure {
                Toast.makeText(this, "Falha ao exportar: ${it.message}", Toast.LENGTH_LONG).show()
            }
        }

    private val exportDiagnosticsLauncher =
        registerForActivityResult(ActivityResultContracts.CreateDocument("application/zip")) { uri ->
            if (uri == null || !::settingsRepo.isInitialized || !::historyRepo.isInitialized) return@registerForActivityResult
            runCatching {
                contentResolver.openOutputStream(uri)?.use { raw ->
                    ZipOutputStream(raw.buffered()).use { zip ->
                        fun entry(name: String, text: String) {
                            zip.putNextEntry(ZipEntry(name))
                            zip.write(text.toByteArray(Charsets.UTF_8))
                            zip.closeEntry()
                        }
                        entry("settings.json", settingsRepo.exportBackupJson())
                        entry("battle_history.json", historyRepo.exportJson())
                        entry(
                            "device.txt",
                            buildString {
                                appendLine("PvPPokeGo=0.5.18")
                                appendLine("package=" + packageName)
                                appendLine("manufacturer=" + android.os.Build.MANUFACTURER)
                                appendLine("model=" + android.os.Build.MODEL)
                                appendLine("android=" + android.os.Build.VERSION.RELEASE)
                                appendLine("sdk=" + android.os.Build.VERSION.SDK_INT)
                                val dm = resources.displayMetrics
                                appendLine("display=" + dm.widthPixels + "x" + dm.heightPixels)
                                appendLine("densityDpi=" + dm.densityDpi)
                            }
                        )
                        entry(
                            "README.txt",
                            "Diagnóstico PvPPokeGo 0.5.18\n" +
                                "Inclui configurações, posições, histórico de batalhas, fontes/confiança dos eventos e dados do aparelho.\n"
                        )
                    }
                } ?: error("Não foi possível criar o ZIP")
            }.onSuccess {
                Toast.makeText(this, "Diagnóstico exportado", Toast.LENGTH_LONG).show()
            }.onFailure {
                Toast.makeText(this, "Falha ao exportar diagnóstico: " + it.message, Toast.LENGTH_LONG).show()
            }
        }

    private val exportHistoryLauncher =
        registerForActivityResult(ActivityResultContracts.CreateDocument("application/zip")) { uri ->
            val selectedId = pendingHistoryExportBattleId
            pendingHistoryExportBattleId = null
            if (uri == null || !::historyRepo.isInitialized) return@registerForActivityResult

            runCatching {
                val entries = if (selectedId != null) {
                    listOfNotNull(historyRepo.battleById(selectedId))
                } else {
                    historyRepo.loadHistory()
                }
                require(entries.isNotEmpty()) { "Nenhuma batalha para exportar" }

                contentResolver.openOutputStream(uri)?.use { raw ->
                    ZipOutputStream(raw.buffered()).use { zip ->
                        fun entry(name: String, text: String) {
                            zip.putNextEntry(ZipEntry(name))
                            zip.write(text.toByteArray(Charsets.UTF_8))
                            zip.closeEntry()
                        }
                        entry("historico.json", historyRepo.exportJson(entries))
                        entry("historico-legivel.txt", historyRepo.exportAnalysisText(entries))
                        entry("historico.csv", historyRepo.exportCsv(entries))
                        entry(
                            "README.txt",
                            buildString {
                                appendLine("PvPPokeGo 0.5.18 — exportação de histórico")
                                appendLine()
                                appendLine("Envie este ZIP diretamente para análise.")
                                appendLine("historico-legivel.txt: leitura humana, separada por VOCÊ / INIMIGO / ASSISTENTE / DIAGNÓSTICO.")
                                appendLine("historico.json: dados completos e estruturados.")
                                appendLine("historico.csv: eventos em formato tabular.")
                                appendLine("Batalhas exportadas: " + entries.size)
                            }
                        )
                    }
                } ?: error("Não foi possível criar o ZIP")
            }.onSuccess {
                Toast.makeText(this, "Histórico exportado para análise", Toast.LENGTH_LONG).show()
            }.onFailure {
                Toast.makeText(this, "Falha ao exportar histórico: " + it.message, Toast.LENGTH_LONG).show()
            }
        }

    private val importSettingsLauncher =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri == null || !::settingsRepo.isInitialized) return@registerForActivityResult
            runCatching {
                val raw = contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                    ?: error("Não foi possível abrir o arquivo")
                settingsRepo.importBackupJson(raw)
            }.onSuccess { restored ->
                Toast.makeText(this, "$restored configurações restauradas", Toast.LENGTH_SHORT).show()
                recreate()
            }.onFailure {
                Toast.makeText(this, "Backup inválido: ${it.message}", Toast.LENGTH_LONG).show()
            }
        }

    private val projectionLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val data = result.data ?: return@registerForActivityResult
            if (result.resultCode != RESULT_OK) return@registerForActivityResult

            overlayStarting = true
            val service = Intent(this, BattleOverlayService::class.java).apply {
                putExtra(BattleOverlayService.EXTRA_RESULT_CODE, result.resultCode)
                putExtra(BattleOverlayService.EXTRA_RESULT_DATA, data)
                // Do NOT mark the app UI as visible here. The projection dialog returns to this
                // Activity, but the user is about to switch to Pokemon GO. Starting with
                // appUiVisible=true hides lock/eye/HUD until BACKGROUND arrives and was a
                // common cause of "untappable" controls.
                putExtra(BattleOverlayService.EXTRA_APP_VISIBLE, false)
            }
            ContextCompat.startForegroundService(this, service)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        settingsRepo = SettingsRepository(this)
        historyRepo = BattleHistoryRepository(this)
        overlayGranted = Settings.canDrawOverlays(this)

        if (Build.VERSION.SDK_INT >= 33) {
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        setContent {
            SettingsScreen(
                repository = settingsRepo,
                historyRepository = historyRepo,
                historyRevision = historyRevision,
                overlayPermissionGranted = overlayGranted,
                onRequestOverlayPermission = ::requestOverlayPermission,
                onStartOverlay = ::startOverlay,
                onStopOverlay = ::stopOverlay,
                onResetBattle = ::resetBattle,
                onExportSettings = ::exportSettings,
                onImportSettings = ::importSettings,
                onExportDiagnostics = ::exportDiagnostics,
                onExportHistory = ::exportHistory,
                onExportBattle = ::exportBattle,
                onToggleRecording = ::toggleRecording
            )
        }
    }

    override fun onResume() {
        super.onResume()
        overlayGranted = Settings.canDrawOverlays(this)
        historyRevision++
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

    private fun exportSettings() {
        exportSettingsLauncher.launch("PvPPokeGo-0.5.18-config.json")
    }

    private fun exportDiagnostics() {
        exportDiagnosticsLauncher.launch("PvPPokeGo-0.5.18-diagnostico.zip")
    }

    private fun exportHistory() {
        pendingHistoryExportBattleId = null
        exportHistoryLauncher.launch("PvPPokeGo-0.5.18-historico.zip")
    }

    private fun exportBattle(id: Long) {
        pendingHistoryExportBattleId = id
        exportHistoryLauncher.launch("PvPPokeGo-0.5.18-batalha-$id.zip")
    }

    private fun toggleRecording() {
        if (!settingsRepo.overlayServiceRunning) {
            Toast.makeText(this, "Inicie o overlay antes de gravar", Toast.LENGTH_LONG).show()
            return
        }
        startService(
            Intent(this, BattleOverlayService::class.java)
                .setAction(BattleOverlayService.ACTION_TOGGLE_RECORDING)
        )
    }

    private fun importSettings() {
        importSettingsLauncher.launch(arrayOf("application/json", "text/plain", "*/*"))
    }

    private fun resetBattle() {
        startService(
            Intent(this, BattleOverlayService::class.java)
                .setAction(BattleOverlayService.ACTION_RESET)
        )
    }
}
