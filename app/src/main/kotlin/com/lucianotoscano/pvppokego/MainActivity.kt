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
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView

class MainActivity : Activity() {
    private lateinit var status: TextView
    private lateinit var leaguePreference: LeaguePreference
    private val projectionManager by lazy { getSystemService(MediaProjectionManager::class.java) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = "PvPPokeGo"
        leaguePreference = LeaguePreference(this)
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
            text = "PvPPokeGo V03"
            textSize = 28f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
        }, fullWidth())

        root.addView(TextView(this).apply {
            text = "Overlay clássico • contador de energia • OCR de carregados"
            textSize = 15f
            setTextColor(Color.LTGRAY)
            gravity = Gravity.CENTER
            setPadding(0, dp(8), 0, dp(20))
        }, fullWidth())

        status = TextView(this).apply {
            textSize = 15f
            setTextColor(Color.WHITE)
            setPadding(dp(12), dp(12), dp(12), dp(12))
            setBackgroundColor(Color.rgb(22, 45, 70))
        }
        root.addView(status, fullWidth())

        root.addView(TextView(this).apply {
            text = "Liga / ranking PvP"
            setTextColor(Color.WHITE)
            textSize = 14f
            setPadding(0, dp(18), 0, dp(4))
        }, fullWidth())

        val modes = listOf(
            "Automático — ler lobby",
            "Liga Grande — 1500 PC",
            "Liga Ultra — 2500 PC",
            "Liga Mestra — sem limite"
        )
        val spinner = Spinner(this).apply {
            adapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_spinner_dropdown_item, modes)
            setSelection(
                when (leaguePreference.mode) {
                    LeagueSelectionMode.AUTO -> 0
                    LeagueSelectionMode.GREAT -> 1
                    LeagueSelectionMode.ULTRA -> 2
                    LeagueSelectionMode.MASTER -> 3
                }
            )
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    leaguePreference.mode = when (position) {
                        1 -> LeagueSelectionMode.GREAT
                        2 -> LeagueSelectionMode.ULTRA
                        3 -> LeagueSelectionMode.MASTER
                        else -> LeagueSelectionMode.AUTO
                    }
                    updateStatus()
                }
                override fun onNothingSelected(parent: AdapterView<*>?) = Unit
            }
        }
        root.addView(spinner, fullWidth())

        root.addView(TextView(this).apply {
            text = "No Automático, o app lê Grande/Ultra/Mestra no lobby. Se não conseguir detectar, o overlay mostra um seletor para você escolher. O moveset inicial usa o rank #1 do PvPoke para a liga selecionada."
            textSize = 12f
            setTextColor(Color.LTGRAY)
            setPadding(0, dp(5), 0, dp(6))
        }, fullWidth())

        root.addView(Button(this).apply {
            text = "1. Permitir sobreposição"
            setOnClickListener {
                startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
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
            text = "A captura é usada para leitura visual da batalha. O PvPPokeGo continua externo ao processo do Pokémon GO."
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
        @Suppress("DEPRECATION")
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) startForegroundService(service) else startService(service)
        updateStatus("Overlay iniciado. Abra o Pokémon GO e entre na batalha.")
    }

    override fun onResume() {
        super.onResume()
        if (::status.isInitialized) updateStatus()
    }

    private fun updateStatus(extra: String? = null) {
        val overlay = if (Settings.canDrawOverlays(this)) "OK" else "PENDENTE"
        val leagueText = when (leaguePreference.mode) {
            LeagueSelectionMode.AUTO -> leaguePreference.lastDetectedLeague?.let { "AUTO (${it.labelPtBr})" } ?: "AUTO"
            LeagueSelectionMode.GREAT -> BattleLeague.GREAT.labelPtBr
            LeagueSelectionMode.ULTRA -> BattleLeague.ULTRA.labelPtBr
            LeagueSelectionMode.MASTER -> BattleLeague.MASTER.labelPtBr
        }
        status.text = buildString {
            append("Permissão de overlay: $overlay\nLiga: $leagueText")
            if (!extra.isNullOrBlank()) append("\n$extra")
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= 33 &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 100)
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

    companion object { private const val REQ_CAPTURE = 5001 }
}
