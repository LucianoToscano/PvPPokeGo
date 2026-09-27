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

/** Floating battle menu: icon-only launcher, PGSharp-like icon+text panel, independent league badge. */
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
    private var leagueView: TextView? = null
    private var leagueParams: WindowManager.LayoutParams? = null
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
            textSize = 19f
            contentDescription = "Menu PvPPokeGo"
            setShadowLayer(2f, 0f, 1f, Color.BLACK)
            background = roundLauncherBackground()
            visibility = View.GONE
        }
        val gp = gearLayoutParams()
        gearParams = gp
        installGearDragAndTap(gear, gp)
        gearView = gear
        windowManager.addView(gear, gp)

        val league = TextView(context).apply {
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            textSize = 17f
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            setShadowLayer(2f, 0f, 1f, Color.BLACK)
            contentDescription = "Liga atual"
            visibility = View.GONE
        }
        val lp = leagueLayoutParams()
        leagueParams = lp
        installLeagueDrag(league, lp)
        leagueView = league
        windowManager.addView(league, lp)
        refreshStatusBadge()
    }

    fun setVisible(visible: Boolean) {
        externallyVisible = visible
        gearView?.visibility = if (visible) View.VISIBLE else View.GONE
        leagueView?.visibility = if (visible) View.VISIBLE else View.GONE
        if (!visible) removePanel(false)
        if (visible) refreshStatusBadge()
    }

    /** Shows the active league as a standalone icon, not attached to the gear. */
    fun refreshStatusBadge() {
        val label = callbacks.currentBattleModeLabel()
        val kind = when {
            label.contains("ULTRA") || label.contains("AUTO-U") -> LeagueKind.ULTRA
            label.contains("MESTRA") || label.contains("AUTO-M") -> LeagueKind.MASTER
            label.contains("GRANDE") || label.contains("AUTO-G") -> LeagueKind.GREAT
            else -> when (settings.leagueMode) {
                BattleLeagueMode.ULTRA -> LeagueKind.ULTRA
                BattleLeagueMode.MASTER -> LeagueKind.MASTER
                BattleLeagueMode.GREAT -> LeagueKind.GREAT
                BattleLeagueMode.AUTO -> LeagueKind.AUTO
            }
        }
        leagueView?.apply {
            text = when (kind) {
                LeagueKind.GREAT -> "◇"
                LeagueKind.ULTRA -> "◆"
                LeagueKind.MASTER -> "♛"
                LeagueKind.AUTO -> "◎"
            }
            background = leagueBackground(kind)
        }
    }

    fun bringToFront() {
        gearView?.let { v -> gearParams?.let { p -> runCatching { windowManager.removeView(v) }; runCatching { windowManager.addView(v, p) } } }
        leagueView?.let { v -> leagueParams?.let { p -> runCatching { windowManager.removeView(v) }; runCatching { windowManager.addView(v, p) } } }
    }

    fun dismissPanel() {
        panelScrollY = 0
        removePanel(false)
    }

    fun destroy() {
        removePanel(false)
        leagueView?.let { runCatching { windowManager.removeView(it) } }
        gearView?.let { runCatching { windowManager.removeView(it) } }
        leagueView = null
        leagueParams = null
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
            setPadding(dp(2), dp(3), dp(2), dp(3))
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
        val width = if (labels) min((screenW * .62f).toInt(), dp(245)) else min((screenW * .46f).toInt(), dp(176))
        val desiredHeight = if (labels) dp(470) else dp(245)
        val height = min((screenH * if (labels) .64f else .36f).toInt(), desiredHeight)
        val margin = dp(5)
        val rightX = gear.x + gear.width + margin
        val x = if (rightX + width <= screenW) rightX else (gear.x - width - margin).coerceAtLeast(0)
        val y = (gear.y - height + gear.height).coerceIn(0, (screenH - height).coerceAtLeast(0))

        val params = WindowManager.LayoutParams(
            width, height,
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
                gravity = Gravity.START
            }
            items.forEach { addIconCell(line, it) }
            while (line.childCount < 4) line.addView(View(context), LinearLayout.LayoutParams(0, dp(43), 1f))
            parent.addView(line, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(47)))
        }

        if (settings.editMode) row(
            IconItem("✓", "Salvar edição", true) { settings.editMode = false; callbacks.onSettingsChanged(true); dismissPanel() },
            IconItem("↺", "Resetar posições", null) { callbacks.onResetHudPositions(); callbacks.onSettingsChanged(true); refreshPanel() }
        )
        row(
            IconItem("◉", "HUD", !callbacks.isHudCollapsed()) { callbacks.onToggleHud(); refreshPanel() },
            IconItem("A", "Reconhecimento", settings.autoRecognition) { settings.autoRecognition = !settings.autoRecognition; changed() },
            IconItem("★", "Assistido", settings.battleAssistEnabled) { settings.battleAssistEnabled = !settings.battleAssistEnabled; changed() },
            IconItem("♜", "Liga", null) { cycleLeague() }
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
            IconItem("↺", "Restaurar tudo", null) { resetPositions() },
            IconItem("×", "Fechar", null) { dismissPanel() }
        )
    }

    /** PGSharp-like list: square icon cell at left, plain text at right, no large rounded panel. */
    private fun buildLabeledMenu(parent: LinearLayout) {
        if (settings.editMode) {
            addRow(parent, "✓", "SALVAR EDIÇÃO", true) { settings.editMode = false; callbacks.onSettingsChanged(true); dismissPanel() }
            addRow(parent, "↺", "RESETAR POSIÇÕES", null) { callbacks.onResetHudPositions(); callbacks.onSettingsChanged(true); refreshPanel() }
        }
        addRow(parent, "◉", "HUD", !callbacks.isHudCollapsed()) { callbacks.onToggleHud(); refreshPanel() }
        addRow(parent, "A", "Reconhecimento automático", settings.autoRecognition) { settings.autoRecognition = !settings.autoRecognition; changed() }
        addRow(parent, "★", "Modo Batalha Assistido", settings.battleAssistEnabled) { settings.battleAssistEnabled = !settings.battleAssistEnabled; changed() }
        addRow(parent, "♜", "Liga: ${leagueShort(settings.leagueMode)}", null) { cycleLeague() }
        addRow(parent, "◇", "Fraquezas do inimigo", settings.showStrongTypes) { settings.showStrongTypes = !settings.showStrongTypes; changed() }
        addRow(parent, "✓", "Indicador do Pokémon atual", settings.showCurrentIndicator) { settings.showCurrentIndicator = !settings.showCurrentIndicator; changed() }
        addRow(parent, "2", "Analisar reservas", settings.analyzeReserves) { settings.analyzeReserves = !settings.analyzeReserves; changed() }
        addRow(parent, "T", "Tipos das reservas", settings.showReserveTypes) { settings.showReserveTypes = !settings.showReserveTypes; changed() }
        addRow(parent, "⚔", "Ataques do inimigo", settings.showEnemyMoves) { settings.showEnemyMoves = !settings.showEnemyMoves; changed() }
        addRow(parent, "••", "Pontos de carregamento", settings.showChargedCounter) { settings.showChargedCounter = !settings.showChargedCounter; changed() }
        addRow(parent, "⚡", "Progresso de energia", settings.showEnergyProgress) { settings.showEnergyProgress = !settings.showEnergyProgress; changed() }
        addRow(parent, "↻", "Timer de troca", settings.showSwitchTimer) { settings.showSwitchTimer = !settings.showSwitchTimer; changed() }
        addRow(parent, "↔", "Modo edição", settings.editMode) { settings.editMode = !settings.editMode; callbacks.onSettingsChanged(true); if (settings.editMode) dismissPanel() else refreshPanel() }
        addRow(parent, "S", "Escala ${(settings.scale * 100).toInt()}%", null) { settings.scale = if (settings.scale >= 1.19f) .5f else settings.scale + .1f; callbacks.onSettingsChanged(true); refreshPanel() }
        addRow(parent, "◐", "Opacidade ${(settings.opacity * 100).toInt()}%", null) { settings.opacity = if (settings.opacity >= .99f) .3f else settings.opacity + .1f; callbacks.onSettingsChanged(false); refreshPanel() }
        addRow(parent, "D", "DEBUG", settings.debugMode) { settings.debugMode = !settings.debugMode; changed() }
        addRow(parent, "▦", "Somente ícones", null) { settings.quickMenuShowLabels = false; refreshPanel() }
        addRow(parent, "↺", "Restaurar posições", null) { resetPositions() }
        addRow(parent, "×", "Fechar", null) { dismissPanel() }
    }

    private fun addIconCell(parent: LinearLayout, item: IconItem) {
        val button = TextView(context).apply {
            text = item.icon
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            textSize = if (item.icon.length > 1) 12f else 17f
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            setShadowLayer(1.6f, 0f, 1f, Color.BLACK)
            background = squareIconBackground(item.enabled)
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
            setPadding(0, 0, dp(6), 0)
            isClickable = true
            setOnClickListener { onTap() }
        }
        row.addView(TextView(context).apply {
            text = icon
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            textSize = 16f
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            background = squareIconBackground(enabled)
        }, LinearLayout.LayoutParams(dp(38), dp(38)).apply { marginEnd = dp(8) })
        row.addView(TextView(context).apply {
            text = label
            setTextColor(Color.WHITE)
            textSize = 13f
            maxLines = 1
            setShadowLayer(2f, 0f, 1f, Color.BLACK)
        }, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        parent.addView(row, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(41)))
    }

    private fun changed(rebuildTouches: Boolean = false) {
        callbacks.onSettingsChanged(rebuildTouches)
        refreshStatusBadge()
        refreshPanel()
    }

    /** Rebuild without jumping to the top after every selection. */
    private fun refreshPanel() {
        val current = panelView ?: return
        panelScrollY = current.scrollY
        showPanel()
    }

    private fun cycleLeague() {
        settings.leagueMode = when (settings.leagueMode) {
            BattleLeagueMode.AUTO -> BattleLeagueMode.GREAT
            BattleLeagueMode.GREAT -> BattleLeagueMode.ULTRA
            BattleLeagueMode.ULTRA -> BattleLeagueMode.MASTER
            BattleLeagueMode.MASTER -> BattleLeagueMode.AUTO
        }
        callbacks.onSettingsChanged(false)
        refreshStatusBadge()
        refreshPanel()
    }

    private fun leagueShort(mode: BattleLeagueMode): String = when (mode) {
        BattleLeagueMode.AUTO -> "Automático"
        BattleLeagueMode.GREAT -> "Grande"
        BattleLeagueMode.ULTRA -> "Ultra"
        BattleLeagueMode.MASTER -> "Mestra"
    }

    private fun resetPositions() {
        settings.resetQuickMenuPosition()
        callbacks.onResetHudPositions()
        moveGearToStoredPosition()
        moveLeagueToStoredPosition()
        callbacks.onSettingsChanged(true)
        refreshPanel()
    }

    private fun gearLayoutParams(): WindowManager.LayoutParams {
        val size = min(dp(38), (metrics.widthPixels * .088f).toInt()).coerceAtLeast(40)
        return floatingParams(size, size).apply {
            val maxX = (metrics.widthPixels - size).coerceAtLeast(0)
            val maxY = (metrics.heightPixels - size).coerceAtLeast(0)
            x = (settings.quickMenuXFraction * maxX).toInt().coerceIn(0, maxX)
            y = (settings.quickMenuYFraction * maxY).toInt().coerceIn(0, maxY)
        }
    }

    private fun leagueLayoutParams(): WindowManager.LayoutParams {
        val size = dp(34).coerceAtLeast(36)
        return floatingParams(size, size).apply {
            val maxX = (metrics.widthPixels - size).coerceAtLeast(0)
            val maxY = (metrics.heightPixels - size).coerceAtLeast(0)
            x = (settings.leagueBadgeXFraction * maxX).toInt().coerceIn(0, maxX)
            y = (settings.leagueBadgeYFraction * maxY).toInt().coerceIn(0, maxY)
        }
    }

    private fun floatingParams(w: Int, h: Int) = WindowManager.LayoutParams(
        w, h,
        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
        PixelFormat.TRANSLUCENT
    ).apply { gravity = Gravity.TOP or Gravity.START }

    private fun moveGearToStoredPosition() {
        val view = gearView ?: return
        val p = gearParams ?: return
        val maxX = (metrics.widthPixels - p.width).coerceAtLeast(0)
        val maxY = (metrics.heightPixels - p.height).coerceAtLeast(0)
        p.x = (settings.quickMenuXFraction * maxX).toInt().coerceIn(0, maxX)
        p.y = (settings.quickMenuYFraction * maxY).toInt().coerceIn(0, maxY)
        runCatching { windowManager.updateViewLayout(view, p) }
    }

    private fun moveLeagueToStoredPosition() {
        val view = leagueView ?: return
        val p = leagueParams ?: return
        val maxX = (metrics.widthPixels - p.width).coerceAtLeast(0)
        val maxY = (metrics.heightPixels - p.height).coerceAtLeast(0)
        p.x = (settings.leagueBadgeXFraction * maxX).toInt().coerceIn(0, maxX)
        p.y = (settings.leagueBadgeYFraction * maxY).toInt().coerceIn(0, maxY)
        runCatching { windowManager.updateViewLayout(view, p) }
    }

    private fun installGearDragAndTap(view: View, params: WindowManager.LayoutParams) {
        installDrag(view, params, onMoved = { xFrac, yFrac ->
            settings.quickMenuXFraction = xFrac
            settings.quickMenuYFraction = yFrac
        }, onTap = { togglePanel() })
    }

    private fun installLeagueDrag(view: View, params: WindowManager.LayoutParams) {
        installDrag(view, params, onMoved = { xFrac, yFrac ->
            settings.leagueBadgeXFraction = xFrac
            settings.leagueBadgeYFraction = yFrac
        }, onTap = { /* display only; league changes from the menu */ })
    }

    private fun installDrag(
        view: View,
        params: WindowManager.LayoutParams,
        onMoved: (Float, Float) -> Unit,
        onTap: () -> Unit
    ) {
        var downRawX = 0f; var downRawY = 0f; var startX = 0; var startY = 0; var dragging = false
        view.setOnTouchListener { _, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    downRawX = event.rawX; downRawY = event.rawY; startX = params.x; startY = params.y; dragging = false; true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = event.rawX - downRawX; val dy = event.rawY - downRawY
                    if (abs(dx) + abs(dy) >= 6f) dragging = true
                    if (dragging) {
                        removePanel(false)
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
                        onMoved(params.x.toFloat() / maxX, params.y.toFloat() / maxY)
                        callbacks.onSettingsChanged(false)
                    } else onTap()
                    true
                }
                MotionEvent.ACTION_CANCEL -> true
                else -> true
            }
        }
    }

    private fun roundLauncherBackground(): GradientDrawable = GradientDrawable().apply {
        shape = GradientDrawable.OVAL
        setColor(Color.argb(150, 30, 35, 44))
        setStroke(dp(1), Color.argb(225, 238, 241, 246))
    }

    private fun squareIconBackground(enabled: Boolean?): GradientDrawable = GradientDrawable().apply {
        shape = GradientDrawable.RECTANGLE
        cornerRadius = dp(2).toFloat()
        setColor(Color.argb(165, 23, 27, 33))
        setStroke(dp(1), when (enabled) {
            true -> Color.rgb(111, 220, 146)
            false -> Color.argb(120, 170, 177, 188)
            null -> Color.argb(150, 235, 238, 244)
        })
    }

    private fun leagueBackground(kind: LeagueKind): GradientDrawable = GradientDrawable().apply {
        shape = GradientDrawable.OVAL
        setColor(when (kind) {
            LeagueKind.GREAT -> Color.argb(215, 52, 111, 190)
            LeagueKind.ULTRA -> Color.argb(215, 194, 145, 43)
            LeagueKind.MASTER -> Color.argb(215, 111, 76, 169)
            LeagueKind.AUTO -> Color.argb(195, 73, 80, 91)
        })
        setStroke(dp(1), Color.argb(235, 245, 247, 250))
    }

    private enum class LeagueKind { AUTO, GREAT, ULTRA, MASTER }
    private data class IconItem(val icon: String, val label: String, val enabled: Boolean?, val onTap: () -> Unit)
}
