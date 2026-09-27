package com.lucianotoscano.pvppokego.overlay

import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
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

/** Compact floating menu inspired by Pokemon GO's own small circular HUD controls. */
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
        fun currentBattleModeLabel(): String
    }

    private var gearView: TextView? = null
    private var gearParams: WindowManager.LayoutParams? = null
    private var modeView: TextView? = null
    private var modeParams: WindowManager.LayoutParams? = null
    private var panelView: ScrollView? = null
    private var externallyVisible = false
    private var panelScrollY = 0

    private val metrics get() = context.resources.displayMetrics
    private val density get() = metrics.density
    private fun dp(v: Int): Int = (v * density).toInt()

    fun attach() {
        if (gearView != null) return
        settings.ensureQuickMenuBottomRightV2()
        settings.ensureQuickMenuStyleV3()

        val gear = TextView(context).apply {
            text = "⚙"
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            textSize = 20f
            contentDescription = "Menu PvPPokeGo"
            setShadowLayer(2.4f, 0f, 1f, Color.BLACK)
            background = circleBackground(false)
            visibility = View.GONE
        }
        val gp = gearLayoutParams()
        gearParams = gp
        installDragAndTap(gear, gp)
        gearView = gear
        windowManager.addView(gear, gp)

        val mode = TextView(context).apply {
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            textSize = 11f
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            setShadowLayer(2f, 0f, 1f, Color.BLACK)
            background = smallModeBackground()
            visibility = View.GONE
        }
        val mp = modeLayoutParams(gp)
        modeView = mode
        modeParams = mp
        windowManager.addView(mode, mp)
        refreshStatusBadge()
    }

    fun setVisible(visible: Boolean) {
        externallyVisible = visible
        gearView?.visibility = if (visible) View.VISIBLE else View.GONE
        modeView?.visibility = if (visible) View.VISIBLE else View.GONE
        if (!visible) removePanel(false)
        if (visible) refreshStatusBadge()
    }

    /** Kept for service compatibility. This is now only a tiny one-letter league chip. */
    fun refreshStatusBadge() {
        val label = callbacks.currentBattleModeLabel()
        modeView?.text = when {
            label.contains("ULTRA") || label.contains("AUTO-U") -> "U"
            label.contains("MESTRA") || label.contains("AUTO-M") -> "M"
            else -> "G"
        }
        modeView?.background = smallModeBackground()
        updateModePosition()
    }

    fun bringToFront() {
        val gear = gearView ?: return
        val gp = gearParams ?: return
        runCatching { windowManager.removeView(gear) }
        runCatching { windowManager.addView(gear, gp) }
        val mode = modeView
        val mp = modeParams
        if (mode != null && mp != null) {
            runCatching { windowManager.removeView(mode) }
            runCatching { windowManager.addView(mode, mp) }
        }
    }

    fun dismissPanel() {
        panelScrollY = 0
        removePanel(false)
    }

    fun destroy() {
        removePanel(false)
        modeView?.let { runCatching { windowManager.removeView(it) } }
        gearView?.let { runCatching { windowManager.removeView(it) } }
        modeView = null
        modeParams = null
        gearView = null
        gearParams = null
    }

    private fun togglePanel() {
        if (!externallyVisible) return
        if (panelView != null) dismissPanel() else showPanel()
    }

    private fun removePanel(preserveScroll: Boolean) {
        val view = panelView ?: return
        if (preserveScroll) panelScrollY = view.scrollY
        runCatching { windowManager.removeView(view) }
        panelView = null
    }

    private fun showPanel() {
        if (!externallyVisible) return
        val restoreY = panelScrollY
        removePanel(true)

        val labels = settings.quickMenuShowLabels
        val content = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(3), dp(3), dp(3), dp(3))
            setBackgroundColor(Color.TRANSPARENT)
        }

        if (labels) buildLabeledMenu(content) else buildIconMenu(content)

        val scroll = ScrollView(context).apply {
            setBackgroundColor(Color.TRANSPARENT)
            isFillViewport = false
            clipToPadding = false
            addView(content, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        }

        val gear = gearParams ?: return
        val screenW = metrics.widthPixels
        val screenH = metrics.heightPixels
        val width = if (labels) min((screenW * .54f).toInt(), dp(205)) else min((screenW * .52f).toInt(), dp(194))
        val desiredHeight = if (labels) dp(420) else dp(245)
        val height = min((screenH * if (labels) .58f else .34f).toInt(), desiredHeight)
        val margin = dp(5)
        val rightX = gear.x + gear.width + margin
        val x = if (rightX + width <= screenW) rightX else (gear.x - width - margin).coerceAtLeast(0)
        val y = (gear.y - height + gear.height).coerceIn(0, (screenH - height).coerceAtLeast(0))

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
        scroll.post { scroll.scrollTo(0, restoreY.coerceAtLeast(0)) }
    }

    private fun buildIconMenu(parent: LinearLayout) {
        fun row(vararg items: IconItem) {
            val line = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER
            }
            items.forEach { item -> addIconCell(line, item) }
            while (line.childCount < 4) line.addView(View(context), LinearLayout.LayoutParams(0, dp(43), 1f))
            parent.addView(line, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(47)))
        }

        if (settings.editMode) {
            row(
                IconItem("✓", "Salvar edição", true) {
                    settings.editMode = false
                    callbacks.onSettingsChanged(true)
                    dismissPanel()
                },
                IconItem("↺", "Resetar posições", null) {
                    callbacks.onResetHudPositions(); callbacks.onSettingsChanged(true); refreshPanel()
                }
            )
        }

        row(
            IconItem("◉", "HUD", !callbacks.isHudCollapsed()) { callbacks.onToggleHud(); refreshPanel() },
            IconItem("A", "Reconhecimento", settings.autoRecognition) { settings.autoRecognition = !settings.autoRecognition; changed() },
            IconItem("★", "Assistido", settings.battleAssistEnabled) { settings.battleAssistEnabled = !settings.battleAssistEnabled; changed() },
            IconItem(leagueGlyph(), "Liga", null) { settings.leagueMode = nextLeague(settings.leagueMode); callbacks.onSettingsChanged(false); refreshStatusBadge(); refreshPanel() }
        )
        row(
            IconItem("◇", "Fraquezas", settings.showStrongTypes) { settings.showStrongTypes = !settings.showStrongTypes; changed() },
            IconItem("✓", "Matchup atual", settings.showCurrentIndicator) { settings.showCurrentIndicator = !settings.showCurrentIndicator; changed() },
            IconItem("2", "Reservas", settings.analyzeReserves) { settings.analyzeReserves = !settings.analyzeReserves; changed() },
            IconItem("T", "Tipos reservas", settings.showReserveTypes) { settings.showReserveTypes = !settings.showReserveTypes; changed() }
        )
        row(
            IconItem("⚔", "Ataques inimigos", settings.showEnemyMoves) { settings.showEnemyMoves = !settings.showEnemyMoves; changed() },
            IconItem("••", "Contagem", settings.showChargedCounter) { settings.showChargedCounter = !settings.showChargedCounter; changed() },
            IconItem("⚡", "Energia", settings.showEnergyProgress) { settings.showEnergyProgress = !settings.showEnergyProgress; changed() },
            IconItem("↻", "Timer troca", settings.showSwitchTimer) { settings.showSwitchTimer = !settings.showSwitchTimer; changed() }
        )
        row(
            IconItem("↔", "Editar HUD", settings.editMode) { settings.editMode = !settings.editMode; callbacks.onSettingsChanged(true); if (settings.editMode) dismissPanel() else refreshPanel() },
            IconItem("S", "Escala", null) { settings.scale = if (settings.scale >= 1.19f) .5f else settings.scale + .1f; callbacks.onSettingsChanged(true); refreshPanel() },
            IconItem("◐", "Opacidade", null) { settings.opacity = if (settings.opacity >= .99f) .3f else settings.opacity + .1f; callbacks.onSettingsChanged(false); refreshPanel() },
            IconItem("D", "Debug", settings.debugMode) { settings.debugMode = !settings.debugMode; changed() }
        )
        row(
            IconItem("≡", "Mostrar textos", null) { settings.quickMenuShowLabels = true; refreshPanel() },
            IconItem("↺", "Restaurar tudo", null) { settings.resetQuickMenuPosition(); callbacks.onResetHudPositions(); moveGearToStoredPosition(); callbacks.onSettingsChanged(true); refreshPanel() },
            IconItem("×", "Fechar", null) { dismissPanel() }
        )
    }

    private fun buildLabeledMenu(parent: LinearLayout) {
        if (settings.editMode) {
            addRow(parent, "✓", "SALVAR EDIÇÃO", true) { settings.editMode = false; callbacks.onSettingsChanged(true); dismissPanel() }
            addRow(parent, "↺", "RESETAR POSIÇÕES", null) { callbacks.onResetHudPositions(); callbacks.onSettingsChanged(true); refreshPanel() }
        }
        addRow(parent, "◉", "HUD", !callbacks.isHudCollapsed()) { callbacks.onToggleHud(); refreshPanel() }
        addRow(parent, "A", "Reconhecimento", settings.autoRecognition) { settings.autoRecognition = !settings.autoRecognition; changed() }
        addRow(parent, "★", "Assistido", settings.battleAssistEnabled) { settings.battleAssistEnabled = !settings.battleAssistEnabled; changed() }
        addRow(parent, leagueGlyph(), "Liga: ${leagueShort(settings.leagueMode)}", null) { settings.leagueMode = nextLeague(settings.leagueMode); callbacks.onSettingsChanged(false); refreshStatusBadge(); refreshPanel() }
        addRow(parent, "◇", "Fraquezas", settings.showStrongTypes) { settings.showStrongTypes = !settings.showStrongTypes; changed() }
        addRow(parent, "✓", "Matchup atual", settings.showCurrentIndicator) { settings.showCurrentIndicator = !settings.showCurrentIndicator; changed() }
        addRow(parent, "2", "Reservas", settings.analyzeReserves) { settings.analyzeReserves = !settings.analyzeReserves; changed() }
        addRow(parent, "T", "Tipos reservas", settings.showReserveTypes) { settings.showReserveTypes = !settings.showReserveTypes; changed() }
        addRow(parent, "⚔", "Ataques inimigos", settings.showEnemyMoves) { settings.showEnemyMoves = !settings.showEnemyMoves; changed() }
        addRow(parent, "••", "Contagem", settings.showChargedCounter) { settings.showChargedCounter = !settings.showChargedCounter; changed() }
        addRow(parent, "⚡", "Energia", settings.showEnergyProgress) { settings.showEnergyProgress = !settings.showEnergyProgress; changed() }
        addRow(parent, "↻", "Timer de troca", settings.showSwitchTimer) { settings.showSwitchTimer = !settings.showSwitchTimer; changed() }
        addRow(parent, "↔", "Modo edição", settings.editMode) { settings.editMode = !settings.editMode; callbacks.onSettingsChanged(true); if (settings.editMode) dismissPanel() else refreshPanel() }
        addRow(parent, "S", "Escala ${(settings.scale * 100).toInt()}%", null) { settings.scale = if (settings.scale >= 1.19f) .5f else settings.scale + .1f; callbacks.onSettingsChanged(true); refreshPanel() }
        addRow(parent, "◐", "Opacidade ${(settings.opacity * 100).toInt()}%", null) { settings.opacity = if (settings.opacity >= .99f) .3f else settings.opacity + .1f; callbacks.onSettingsChanged(false); refreshPanel() }
        addRow(parent, "D", "DEBUG", settings.debugMode) { settings.debugMode = !settings.debugMode; changed() }
        addRow(parent, "□", "Somente ícones", null) { settings.quickMenuShowLabels = false; refreshPanel() }
        addRow(parent, "↺", "Restaurar tudo", null) { settings.resetQuickMenuPosition(); callbacks.onResetHudPositions(); moveGearToStoredPosition(); callbacks.onSettingsChanged(true); refreshPanel() }
        addRow(parent, "×", "Fechar", null) { dismissPanel() }
    }

    private fun addIconCell(parent: LinearLayout, item: IconItem) {
        val button = TextView(context).apply {
            text = item.icon
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            textSize = if (item.icon.length > 1) 12.5f else 16.5f
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            setShadowLayer(2f, 0f, 1f, Color.BLACK)
            background = circleBackground(item.enabled == true, item.enabled == false)
            contentDescription = item.label
            isClickable = true
            setOnClickListener { item.onTap() }
        }
        parent.addView(button, LinearLayout.LayoutParams(0, dp(39), 1f).apply {
            marginStart = dp(3); marginEnd = dp(3); topMargin = dp(2); bottomMargin = dp(2)
        })
    }

    private fun addRow(parent: LinearLayout, icon: String, label: String, enabled: Boolean?, onTap: () -> Unit) {
        val row = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(4), dp(2), dp(6), dp(2))
            background = roundedBackground(Color.argb(145, 35, 40, 49), borderColor(enabled), dp(18).toFloat(), dp(1))
            isClickable = true
            setOnClickListener { onTap() }
        }
        row.addView(TextView(context).apply {
            text = icon
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            textSize = 15f
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }, LinearLayout.LayoutParams(dp(30), dp(30)))
        row.addView(TextView(context).apply {
            text = label
            setTextColor(Color.WHITE)
            textSize = 10.5f
            maxLines = 1
            setPadding(dp(4), 0, 0, 0)
        }, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        parent.addView(row, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(34)).apply { bottomMargin = dp(3) })
    }

    private fun changed(rebuildTouches: Boolean = false) {
        callbacks.onSettingsChanged(rebuildTouches)
        refreshStatusBadge()
        refreshPanel()
    }

    /** Rebuild while retaining ScrollView position instead of jumping back to the top. */
    private fun refreshPanel() {
        val current = panelView ?: return
        panelScrollY = current.scrollY
        showPanel()
    }

    private fun nextLeague(mode: BattleLeagueMode): BattleLeagueMode = when (mode) {
        BattleLeagueMode.AUTO -> BattleLeagueMode.GREAT
        BattleLeagueMode.GREAT -> BattleLeagueMode.ULTRA
        BattleLeagueMode.ULTRA -> BattleLeagueMode.MASTER
        BattleLeagueMode.MASTER -> BattleLeagueMode.AUTO
    }

    private fun leagueGlyph(): String = when (settings.leagueMode) {
        BattleLeagueMode.AUTO -> "A"
        BattleLeagueMode.GREAT -> "G"
        BattleLeagueMode.ULTRA -> "U"
        BattleLeagueMode.MASTER -> "M"
    }

    private fun leagueShort(mode: BattleLeagueMode): String = when (mode) {
        BattleLeagueMode.AUTO -> "Auto"
        BattleLeagueMode.GREAT -> "Grande"
        BattleLeagueMode.ULTRA -> "Ultra"
        BattleLeagueMode.MASTER -> "Mestra"
    }

    private fun gearLayoutParams(): WindowManager.LayoutParams {
        val size = min(dp(40), (metrics.widthPixels * .092f).toInt()).coerceAtLeast(42)
        val maxX = (metrics.widthPixels - size).coerceAtLeast(0)
        val maxY = (metrics.heightPixels - size).coerceAtLeast(0)
        return WindowManager.LayoutParams(
            size, size,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = (settings.quickMenuXFraction * maxX).toInt().coerceIn(0, maxX)
            y = (settings.quickMenuYFraction * maxY).toInt().coerceIn(0, maxY)
        }
    }

    private fun modeLayoutParams(gear: WindowManager.LayoutParams): WindowManager.LayoutParams {
        val size = dp(25).coerceAtLeast(27)
        return WindowManager.LayoutParams(
            size, size,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply { gravity = Gravity.TOP or Gravity.START }.also { positionMode(it, gear) }
    }

    private fun updateModePosition() {
        val gp = gearParams ?: return
        val mp = modeParams ?: return
        val mode = modeView ?: return
        positionMode(mp, gp)
        runCatching { windowManager.updateViewLayout(mode, mp) }
    }

    private fun positionMode(mode: WindowManager.LayoutParams, gear: WindowManager.LayoutParams) {
        val gap = dp(4)
        val leftX = gear.x - mode.width - gap
        mode.x = if (leftX >= 0) leftX else (gear.x + gear.width + gap).coerceAtMost((metrics.widthPixels - mode.width).coerceAtLeast(0))
        mode.y = (gear.y + (gear.height - mode.height) / 2).coerceIn(0, (metrics.heightPixels - mode.height).coerceAtLeast(0))
    }

    private fun moveGearToStoredPosition() {
        val gear = gearView ?: return
        val params = gearParams ?: return
        val maxX = (metrics.widthPixels - params.width).coerceAtLeast(0)
        val maxY = (metrics.heightPixels - params.height).coerceAtLeast(0)
        params.x = (settings.quickMenuXFraction * maxX).toInt().coerceIn(0, maxX)
        params.y = (settings.quickMenuYFraction * maxY).toInt().coerceIn(0, maxY)
        runCatching { windowManager.updateViewLayout(gear, params) }
        updateModePosition()
    }

    /** Direct drag: no long press and no relationship with the separate eye control. */
    private fun installDragAndTap(view: View, params: WindowManager.LayoutParams) {
        var downRawX = 0f
        var downRawY = 0f
        var startX = 0
        var startY = 0
        var dragging = false
        view.setOnTouchListener { _, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    downRawX = event.rawX; downRawY = event.rawY
                    startX = params.x; startY = params.y
                    dragging = false
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = event.rawX - downRawX
                    val dy = event.rawY - downRawY
                    if (abs(dx) + abs(dy) >= 6f) dragging = true
                    if (dragging) {
                        removePanel(false)
                        val maxX = (metrics.widthPixels - params.width).coerceAtLeast(0)
                        val maxY = (metrics.heightPixels - params.height).coerceAtLeast(0)
                        params.x = (startX + dx).toInt().coerceIn(0, maxX)
                        params.y = (startY + dy).toInt().coerceIn(0, maxY)
                        runCatching { windowManager.updateViewLayout(view, params) }
                        updateModePosition()
                    }
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (dragging) {
                        val maxX = (metrics.widthPixels - params.width).coerceAtLeast(1)
                        val maxY = (metrics.heightPixels - params.height).coerceAtLeast(1)
                        settings.quickMenuXFraction = params.x.toFloat() / maxX
                        settings.quickMenuYFraction = params.y.toFloat() / maxY
                        callbacks.onSettingsChanged(false)
                    } else togglePanel()
                    true
                }
                MotionEvent.ACTION_CANCEL -> true
                else -> true
            }
        }
    }

    private fun circleBackground(active: Boolean, disabled: Boolean = false): GradientDrawable = GradientDrawable().apply {
        shape = GradientDrawable.OVAL
        setColor(Color.argb(if (active) 205 else 160, 46, 51, 60))
        setStroke(dp(1), when {
            active -> Color.rgb(118, 224, 150)
            disabled -> Color.argb(150, 190, 196, 205)
            else -> Color.argb(220, 238, 241, 246)
        })
    }

    private fun smallModeBackground(): GradientDrawable = GradientDrawable().apply {
        shape = GradientDrawable.OVAL
        setColor(Color.argb(180, 35, 40, 49))
        val border = if (settings.battleAssistEnabled) Color.rgb(118, 224, 150) else Color.argb(220, 238, 241, 246)
        setStroke(dp(1), border)
    }

    private fun borderColor(enabled: Boolean?): Int = when (enabled) {
        true -> Color.rgb(118, 224, 150)
        false -> Color.argb(150, 190, 196, 205)
        null -> Color.argb(210, 238, 241, 246)
    }

    private fun roundedBackground(color: Int, strokeColor: Int, radius: Float, strokeWidth: Int): GradientDrawable =
        GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            setColor(color)
            cornerRadius = radius
            setStroke(strokeWidth, strokeColor)
        }

    private data class IconItem(
        val icon: String,
        val label: String,
        val enabled: Boolean?,
        val onTap: () -> Unit
    )
}
