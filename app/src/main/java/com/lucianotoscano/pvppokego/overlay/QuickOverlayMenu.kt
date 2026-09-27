package com.lucianotoscano.pvppokego.overlay

import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.SystemClock
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import com.lucianotoscano.pvppokego.data.BattleLeagueMode
import com.lucianotoscano.pvppokego.data.SettingsRepository
import kotlin.math.abs
import kotlin.math.min

/**
 * Floating PGSharp-style quick menu for PvPPokeGo.
 *
 * It only changes PvPPokeGo overlay preferences. It never sends input to Pokemon GO.
 * The gear itself can be long-pressed and dragged anywhere on screen.
 */
class QuickOverlayMenu(
    private val context: Context,
    private val windowManager: WindowManager,
    private val settings: SettingsRepository,
    private val callbacks: Callbacks
) {
    interface Callbacks {
        fun onSettingsChanged(rebuildTouchZones: Boolean = false)
        fun onToggleHud()
        fun isHudCollapsed(): Boolean
        fun onResetHudPositions()
    }

    private var gearView: TextView? = null
    private var gearParams: WindowManager.LayoutParams? = null
    private var panelView: View? = null
    private var externallyVisible = false

    private val metrics get() = context.resources.displayMetrics
    private val density get() = metrics.density
    private fun dp(v: Int): Int = (v * density).toInt()

    fun attach() {
        if (gearView != null) return

        val gear = TextView(context).apply {
            text = "⚙"
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            textSize = 23f
            setShadowLayer(3f, 0f, 1f, Color.BLACK)
            background = roundedBackground(
                Color.argb(205, 40, 44, 52),
                Color.argb(225, 238, 241, 246),
                dp(28).toFloat(),
                dp(1)
            )
        }

        val params = gearLayoutParams()
        gearParams = params
        gear.visibility = View.GONE
        installDragAndTap(gear, params)
        gearView = gear
        windowManager.addView(gear, params)
    }

    fun setVisible(visible: Boolean) {
        externallyVisible = visible
        gearView?.visibility = if (visible) View.VISIBLE else View.GONE
        if (!visible) dismissPanel()
    }

    fun dismissPanel() {
        panelView?.let { runCatching { windowManager.removeView(it) } }
        panelView = null
    }

    fun destroy() {
        dismissPanel()
        gearView?.let { runCatching { windowManager.removeView(it) } }
        gearView = null
        gearParams = null
    }

    private fun togglePanel() {
        if (!externallyVisible) return
        if (panelView != null) dismissPanel() else showPanel()
    }

    private fun showPanel() {
        if (!externallyVisible) return
        dismissPanel()

        val labels = settings.quickMenuShowLabels
        val content = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(4), dp(4), dp(4), dp(4))
            background = roundedBackground(
                Color.argb(96, 18, 21, 27),
                Color.argb(105, 240, 242, 246),
                dp(12).toFloat(),
                dp(1)
            )
        }

        addToggle(content, "◉", "HUD", !callbacks.isHudCollapsed(), labels) {
            callbacks.onToggleHud()
            refreshPanel()
        }
        addToggle(content, "A", "Reconhecimento automático", settings.autoRecognition, labels) {
            settings.autoRecognition = !settings.autoRecognition
            changed()
        }
        addToggle(content, "★", "Modo Batalha Assistido", settings.battleAssistEnabled, labels) {
            settings.battleAssistEnabled = !settings.battleAssistEnabled
            changed()
        }
        addToggle(content, "◆", "Fraquezas do inimigo", settings.showStrongTypes, labels) {
            settings.showStrongTypes = !settings.showStrongTypes
            changed()
        }
        addToggle(content, "✓", "Indicador Pokémon atual", settings.showCurrentIndicator, labels) {
            settings.showCurrentIndicator = !settings.showCurrentIndicator
            changed()
        }
        addToggle(content, "2", "Analisar reservas", settings.analyzeReserves, labels) {
            settings.analyzeReserves = !settings.analyzeReserves
            changed()
        }
        addToggle(content, "T", "Tipos das reservas", settings.showReserveTypes, labels) {
            settings.showReserveTypes = !settings.showReserveTypes
            changed()
        }
        addToggle(content, "⚔", "Ataques do inimigo", settings.showEnemyMoves, labels) {
            settings.showEnemyMoves = !settings.showEnemyMoves
            changed()
        }
        addToggle(content, "•••", "Pontos de carregamento", settings.showChargedCounter, labels) {
            settings.showChargedCounter = !settings.showChargedCounter
            changed()
        }
        addToggle(content, "⚡", "Progresso de energia", settings.showEnergyProgress, labels) {
            settings.showEnergyProgress = !settings.showEnergyProgress
            changed()
        }
        addToggle(content, "⏱", "Timer de troca", settings.showSwitchTimer, labels) {
            settings.showSwitchTimer = !settings.showSwitchTimer
            changed()
        }
        addToggle(content, "Aa", "Textos do HUD", settings.showHudText, labels) {
            settings.showHudText = !settings.showHudText
            changed()
        }
        addToggle(content, "↔", "Modo edição", settings.editMode, labels) {
            settings.editMode = !settings.editMode
            callbacks.onSettingsChanged(true)
            if (settings.editMode) dismissPanel() else refreshPanel()
        }
        addToggle(content, "D", "DEBUG", settings.debugMode, labels) {
            settings.debugMode = !settings.debugMode
            changed()
        }

        addAction(content, "L", "Liga: ${leagueShort(settings.leagueMode)}", labels) {
            settings.leagueMode = nextLeague(settings.leagueMode)
            callbacks.onSettingsChanged(false)
            refreshPanel()
        }
        addAction(content, "S", "Escala: ${(settings.scale * 100).toInt()}%", labels) {
            settings.scale = if (settings.scale >= 1.19f) .5f else settings.scale + .1f
            callbacks.onSettingsChanged(true)
            refreshPanel()
        }
        addAction(content, "◐", "Opacidade: ${(settings.opacity * 100).toInt()}%", labels) {
            settings.opacity = if (settings.opacity >= .99f) .3f else settings.opacity + .1f
            callbacks.onSettingsChanged(false)
            refreshPanel()
        }
        addToggle(content, "≡", "Mostrar nomes neste menu", settings.quickMenuShowLabels, labels) {
            settings.quickMenuShowLabels = !settings.quickMenuShowLabels
            refreshPanel()
        }
        addAction(content, "↺", "Restaurar posições", labels) {
            settings.resetQuickMenuPosition()
            callbacks.onResetHudPositions()
            moveGearToStoredPosition()
            callbacks.onSettingsChanged(true)
            refreshPanel()
        }
        addAction(content, "×", "Fechar", labels) { dismissPanel() }

        val scroll = ScrollView(context).apply {
            setBackgroundColor(Color.TRANSPARENT)
            isFillViewport = false
            addView(content, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        }

        val gear = gearParams ?: return
        val screenW = metrics.widthPixels
        val screenH = metrics.heightPixels
        val gearSize = gear.width
        val width = if (labels) min((screenW * .58f).toInt(), dp(220)) else min((screenW * .18f).toInt(), dp(72))
        val height = min((screenH * .72f).toInt(), dp(530))
        val margin = (screenW * .012f).toInt().coerceAtLeast(4)
        val rightX = gear.x + gearSize + margin
        val x = if (rightX + width <= screenW) rightX else (gear.x - width - margin).coerceAtLeast(0)
        val y = gear.y.coerceIn(0, (screenH - height).coerceAtLeast(0))

        val params = WindowManager.LayoutParams(
            width,
            height,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            this.x = x
            this.y = y
        }

        panelView = scroll
        windowManager.addView(scroll, params)
    }

    private fun addToggle(
        parent: LinearLayout,
        icon: String,
        label: String,
        enabled: Boolean,
        labels: Boolean,
        onTap: () -> Unit
    ) = addRow(parent, icon, label, labels, enabled, onTap)

    private fun addAction(
        parent: LinearLayout,
        icon: String,
        label: String,
        labels: Boolean,
        onTap: () -> Unit
    ) = addRow(parent, icon, label, labels, null, onTap)

    private fun addRow(
        parent: LinearLayout,
        icon: String,
        label: String,
        labels: Boolean,
        enabled: Boolean?,
        onTap: () -> Unit
    ) {
        val active = enabled == true
        val border = when (enabled) {
            true -> Color.argb(245, 91, 196, 118)
            false -> Color.argb(125, 220, 224, 230)
            null -> Color.argb(155, 205, 211, 220)
        }
        val fill = when (enabled) {
            true -> Color.argb(205, 47, 67, 58)
            false -> Color.argb(188, 43, 47, 56)
            null -> Color.argb(190, 45, 49, 58)
        }
        val row = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(5), dp(3), dp(5), dp(3))
            background = roundedBackground(fill, border, dp(8).toFloat(), dp(1))
            isClickable = true
            setOnClickListener { onTap() }
        }

        row.addView(TextView(context).apply {
            text = icon
            gravity = Gravity.CENTER
            setTextColor(if (active) Color.rgb(133, 240, 155) else Color.WHITE)
            textSize = if (icon.length > 2) 12f else 17f
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            setShadowLayer(2f, 0f, 1f, Color.BLACK)
        }, LinearLayout.LayoutParams(dp(30), dp(30)))

        if (labels) {
            row.addView(TextView(context).apply {
                text = label
                setTextColor(Color.WHITE)
                textSize = 11.5f
                maxLines = 1
                setPadding(dp(6), 0, dp(2), 0)
                setShadowLayer(2f, 0f, 1f, Color.BLACK)
            }, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        }

        parent.addView(row, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(38)).apply {
            bottomMargin = dp(3)
        })
    }

    private fun changed(rebuildTouches: Boolean = false) {
        callbacks.onSettingsChanged(rebuildTouches)
        refreshPanel()
    }

    private fun refreshPanel() {
        if (panelView != null && externallyVisible && !settings.editMode) showPanel()
    }

    private fun nextLeague(mode: BattleLeagueMode): BattleLeagueMode = when (mode) {
        BattleLeagueMode.AUTO -> BattleLeagueMode.GREAT
        BattleLeagueMode.GREAT -> BattleLeagueMode.ULTRA
        BattleLeagueMode.ULTRA -> BattleLeagueMode.MASTER
        BattleLeagueMode.MASTER -> BattleLeagueMode.AUTO
    }

    private fun leagueShort(mode: BattleLeagueMode): String = when (mode) {
        BattleLeagueMode.AUTO -> "Auto"
        BattleLeagueMode.GREAT -> "Grande"
        BattleLeagueMode.ULTRA -> "Ultra"
        BattleLeagueMode.MASTER -> "Mestra"
    }

    private fun gearLayoutParams(): WindowManager.LayoutParams {
        val screenW = metrics.widthPixels
        val screenH = metrics.heightPixels
        val size = min(dp(46), (screenW * .105f).toInt()).coerceAtLeast(48)
        val maxX = (screenW - size).coerceAtLeast(0)
        val maxY = (screenH - size).coerceAtLeast(0)
        return WindowManager.LayoutParams(
            size,
            size,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = (settings.quickMenuXFraction * maxX).toInt().coerceIn(0, maxX)
            y = (settings.quickMenuYFraction * maxY).toInt().coerceIn(0, maxY)
        }
    }

    private fun moveGearToStoredPosition() {
        val gear = gearView ?: return
        val params = gearParams ?: return
        val screenW = metrics.widthPixels
        val screenH = metrics.heightPixels
        val maxX = (screenW - params.width).coerceAtLeast(0)
        val maxY = (screenH - params.height).coerceAtLeast(0)
        params.x = (settings.quickMenuXFraction * maxX).toInt().coerceIn(0, maxX)
        params.y = (settings.quickMenuYFraction * maxY).toInt().coerceIn(0, maxY)
        runCatching { windowManager.updateViewLayout(gear, params) }
    }

    private fun installDragAndTap(view: View, params: WindowManager.LayoutParams) {
        var downRawX = 0f
        var downRawY = 0f
        var startX = 0
        var startY = 0
        var downAt = 0L
        var dragging = false

        view.setOnTouchListener { _, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    downRawX = event.rawX
                    downRawY = event.rawY
                    startX = params.x
                    startY = params.y
                    downAt = SystemClock.uptimeMillis()
                    dragging = false
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = event.rawX - downRawX
                    val dy = event.rawY - downRawY
                    val held = SystemClock.uptimeMillis() - downAt >= 180L
                    val moved = abs(dx) + abs(dy) >= 10f
                    if (held && moved) {
                        dragging = true
                        dismissPanel()
                        val maxX = (metrics.widthPixels - params.width).coerceAtLeast(0)
                        val maxY = (metrics.heightPixels - params.height).coerceAtLeast(0)
                        params.x = (startX + dx).toInt().coerceIn(0, maxX)
                        params.y = (startY + dy).toInt().coerceIn(0, maxY)
                        runCatching { windowManager.updateViewLayout(view, params) }
                    }
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (dragging) {
                        val maxX = (metrics.widthPixels - params.width).coerceAtLeast(1)
                        val maxY = (metrics.heightPixels - params.height).coerceAtLeast(1)
                        settings.quickMenuXFraction = params.x.toFloat() / maxX.toFloat()
                        settings.quickMenuYFraction = params.y.toFloat() / maxY.toFloat()
                    } else {
                        togglePanel()
                    }
                    true
                }
                MotionEvent.ACTION_CANCEL -> true
                else -> true
            }
        }
    }

    private fun roundedBackground(color: Int, strokeColor: Int, radius: Float, strokeWidth: Int): GradientDrawable =
        GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            setColor(color)
            cornerRadius = radius
            setStroke(strokeWidth, strokeColor)
        }
}
