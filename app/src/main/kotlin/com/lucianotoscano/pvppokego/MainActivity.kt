package com.lucianotoscano.pvppokego

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {
    private lateinit var status: TextView
    private val projectionManager by lazy {
        getSystemService(MediaProjectionManager::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = "PvPPokeGo"
        setContentView(buildUi())
        requestNotificationPermissionIfNeeded()
        updateStatus()
    }

    private fun buildUi(): LinearLayout {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(dp(24), dp(36), dp(24), dp(24))
            setBackgroundColor(Color.rgb(10, 22, 38))
        }

        root.addView(TextView(this).apply {
            text = "PvPPokeGo V02"
            textSize = 28f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
        }, fullWidth())

        root.addView(TextView(this).apply {
            text = "Overlay externo • contador de energia • OCR de carregados"
            textSize = 15f
            setTextColor(Color.LTGRAY)
            gravity = Gravity.CENTER
            setPadding(0, dp(8), 0, dp(24))
        }, fullWidth())

        status = TextView(this).apply {
            textSize = 15f
            setTextColor(Color.WHITE)
            setPadding(dp(12), dp(12), dp(12), dp(12))
            setBackgroundColor(Color.rgb(22, 45, 70))
        }
        root.addView(status, fullWidth())

        root.addView(Button(this).apply {
            text = "1. Permitir sobreposição"
            setOnClickListener {
                startActivity(
                    Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:$packageName")
                    )
                )
            }
        }, buttonParams())

        root.addView(Button(this).apply {
            text = "2. Iniciar PvP + OCR"
            setOnClickListener { startCapture() }
        }, buttonParams())

        root.addView(Button(this).apply {
            text = "Parar overlay"
            setOnClickListener {
                startService(Intent(this@MainActivity, OverlayService::class.java).apply {
                    action = OverlayService.ACTION_STOP
                })
                updateStatus("Overlay encerrado.")
            }
        }, buttonParams())

        root.addView(TextView(this).apply {
            text = "Na primeira execução o Android pedirá autorização para capturar a tela. O PvPPokeGo usa essa captura apenas para analisar visualmente a batalha e mantém a lógica separada do Pokémon GO."
            setTextColor(Color.LTGRAY)
            textSize = 13f
            setPadding(0, dp(22), 0, 0)
        }, fullWidth())

        return root
    }

    private fun startCapture() {
        if (!Settings.canDrawOverlays(this)) {
            updateStatus("Primeiro permita a sobreposição.")
            return
        }
        startActivityForResult(projectionManager.createScreenCaptureIntent(), REQ_CAPTURE)
    }

    @Suppress("DEPRECATION")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != REQ_CAPTURE) return
        if (resultCode != RESULT_OK || data == null) {
            updateStatus("Captura de tela não autorizada.")
            return
        }

        val service = Intent(this, OverlayService::class.java).apply {
            action = OverlayService.ACTION_START
            putExtra(OverlayService.EXTRA_RESULT_CODE, resultCode)
            putExtra(OverlayService.EXTRA_RESULT_DATA, data)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(service)
        } else {
            startService(service)
        }
        updateStatus("Overlay iniciado. Abra o Pokémon GO e entre na batalha.")
    }

    override fun onResume() {
        super.onResume()
        if (::status.isInitialized) updateStatus()
    }

    private fun updateStatus(extra: String? = null) {
        val overlay = if (Settings.canDrawOverlays(this)) "OK" else "PENDENTE"
        status.text = buildString {
            append("Permissão de overlay: $overlay")
            if (!extra.isNullOrBlank()) append("\n$extra")
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= 33 &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 100)
        }
    }

    private fun fullWidth() = LinearLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.WRAP_CONTENT
    )

    private fun buttonParams() = LinearLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.WRAP_CONTENT
    ).apply { topMargin = dp(14) }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    companion object {
        private const val REQ_CAPTURE = 5001
    }
}
