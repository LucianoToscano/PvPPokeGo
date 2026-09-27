from pathlib import Path
import re


def must_replace(text: str, old: str, new: str, label: str) -> str:
    if old not in text:
        raise SystemExit(f'missing marker: {label}')
    return text.replace(old, new, 1)

# -----------------------------------------------------------------------------
# BattleOverlayService: confidence fusion, own-energy events, dynamic native top anchor,
# and richer edit toolbar.
# -----------------------------------------------------------------------------
p = Path('app/src/main/java/com/lucianotoscano/pvppokego/overlay/BattleOverlayService.kt')
s = p.read_text()
s = must_replace(s,
    'import com.lucianotoscano.pvppokego.detect.BattleOcrDetector\n',
    'import com.lucianotoscano.pvppokego.detect.BattleOcrDetector\nimport com.lucianotoscano.pvppokego.detect.BattlePresenceTracker\n',
    'presence import')
s = must_replace(s,
    '    private var frameDetector: BattleFrameEventDetector? = null\n',
    '    private var frameDetector: BattleFrameEventDetector? = null\n    private val presenceTracker = BattlePresenceTracker()\n    private var lastNativeTopCardY: Int? = null\n    private var lastEditSnapshot: Triple<String, Float, Float>? = null\n',
    'presence fields')
s = s.replace('                frameDetector = BattleFrameEventDetector()\n', '                frameDetector = BattleFrameEventDetector()\n                presenceTracker.reset()\n')
s = s.replace('                battleActive = false\n                lastBattleEvidenceAtMs = 0L\n', '                battleActive = false\n                presenceTracker.reset()\n                lastNativeTopCardY = null\n                lastBattleEvidenceAtMs = 0L\n')

old_analyze = '''                            val event = frameDetector?.analyze(frame, e.currentFastDurationMs(), now)
                            if (event != null) {
                                if (event.battleUiVisible) markBattleEvidence(now)
                                if (battleActive || event.battleUiVisible) {
                                    e.onReserveSlotsDetected(event.reserve1Visible, event.reserve2Visible)
                                }
                                if (event.fastMoveCount > 0 && (battleActive || event.battleUiVisible)) {
                                    e.onFastDetected(event.fastMoveCount, event.confidence, event.source, event.hpRatio, event.motion)
                                }
                            }
'''
new_analyze = '''                            val event = frameDetector?.analyze(
                                frame,
                                e.currentFastDurationMs(),
                                e.currentOwnFastDurationMs(),
                                now
                            )
                            if (event != null) {
                                lastNativeTopCardY = event.nativeTopCardY ?: lastNativeTopCardY
                                battleActive = presenceTracker.update(
                                    visualConfidence = event.battleConfidence,
                                    nowMs = now
                                )
                                e.onBattleConfidence(presenceTracker.confidence)
                                if (event.battleUiVisible) markBattleEvidence(now)
                                if (battleActive || event.battleUiVisible) {
                                    e.onReserveSlotsDetected(event.reserve1Visible, event.reserve2Visible)
                                }
                                if (event.fastMoveCount > 0 && (battleActive || event.battleUiVisible)) {
                                    e.onFastDetected(event.fastMoveCount, event.confidence, event.source, event.hpRatio, event.motion)
                                }
                                if (event.ownFastMoveCount > 0 && (battleActive || event.battleUiVisible)) {
                                    e.onOwnFastDetected(event.ownFastMoveCount, event.confidence, event.source, event.opponentHpRatio)
                                }
                            }
'''
s = must_replace(s, old_analyze, new_analyze, 'frame analyze block')

old_ocr_evidence = '''                                if ((d.player != null && d.opponent != null) || battleTextLooksActive(d.battleText)) {
                                    markBattleEvidence(now)
                                }
                                if (switchChoicePromptDetected(d.battleText)) {
                                    switchChoicePromptUntilMs = now + SWITCH_CHOICE_PROMPT_GRACE_MS
                                    markBattleEvidence(now)
                                }
'''
new_ocr_evidence = '''                                val pairedCards = d.player != null && d.opponent != null
                                val textEvidence = battleTextLooksActive(d.battleText)
                                val switchPrompt = switchChoicePromptDetected(d.battleText)
                                if (pairedCards || textEvidence || switchPrompt) {
                                    battleActive = presenceTracker.update(
                                        pairedPokemonCards = pairedCards,
                                        battleTextEvidence = textEvidence,
                                        switchPrompt = switchPrompt,
                                        nowMs = now
                                    )
                                    e.onBattleConfidence(presenceTracker.confidence)
                                    markBattleEvidence(now)
                                }
                                if (switchPrompt) {
                                    switchChoicePromptUntilMs = now + SWITCH_CHOICE_PROMPT_GRACE_MS
                                }
'''
s = must_replace(s, old_ocr_evidence, new_ocr_evidence, 'ocr evidence block')

old_hide = '''                        if (battleActive && now - lastBattleEvidenceAtMs > BATTLE_VISIBILITY_GRACE_MS) {
                            battleActive = false
                            switchChoicePromptUntilMs = 0L
                        }
'''
new_hide = '''                        battleActive = presenceTracker.tick(now)
                        engine?.onBattleConfidence(presenceTracker.confidence)
                        if (!battleActive) switchChoicePromptUntilMs = 0L
'''
s = must_replace(s, old_hide, new_hide, 'battle hide block')

s = must_replace(s,
    '        view.setCollapsed(hudCollapsed)\n',
    '        view.setCollapsed(hudCollapsed)\n        view.setNativeTopCardY(lastNativeTopCardY)\n',
    'native top card redraw')

s = s.replace('        frameDetector?.reset()\n        battleActive = false\n', '        frameDetector?.reset()\n        presenceTracker.reset()\n        lastNativeTopCardY = null\n        battleActive = false\n')
s = s.replace('        frameDetector?.reset()\n        switchChoicePromptUntilMs = 0L\n', '        frameDetector?.reset()\n        presenceTracker.reset()\n        lastNativeTopCardY = null\n        switchChoicePromptUntilMs = 0L\n')

# Snapshot selected block before dragging, to support one-step undo.
s = must_replace(s,
    '''                        candidate = overlayView?.dragTargetAt(event.x, event.y)
                        overlayView?.setEditTarget(candidate)
''',
    '''                        candidate = overlayView?.dragTargetAt(event.x, event.y)
                        overlayView?.setEditTarget(candidate)
                        candidate?.let { block ->
                            overlayView?.blockPosition(block)?.let { pos ->
                                lastEditSnapshot = Triple(block, pos.first, pos.second)
                            }
                        }
''',
    'edit snapshot')

# Replace toolbar with compact icon controls for selected block plus save/reset.
start = s.index('    /** Always-on edit controls above the full-screen drag layer, so edit mode can be saved/reset. */')
end = s.index('    private fun removeEditToolbar()', start)
new_toolbar = r'''    /** Compact edit controls. Select a HUD block, then resize/fade/lock/hide/undo or save. */
    private fun attachEditToolbar() {
        val wm = windowManager ?: return
        if (editToolbarView != null) return
        val d = resources.displayMetrics.density
        fun dp(v: Int): Int = (v * d).toInt()

        fun button(textValue: String, action: () -> Unit) = TextView(this).apply {
            text = textValue
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            textSize = 13f
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            setShadowLayer(2f, 0f, 1f, Color.BLACK)
            background = roundedBackground(Color.argb(210, 24, 29, 37), Color.argb(210, 238, 241, 246), dp(8).toFloat(), dp(1))
            isClickable = true
            setOnClickListener { action() }
        }

        val bar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(dp(3), dp(3), dp(3), dp(3))
            background = roundedBackground(Color.argb(170, 15, 18, 24), Color.argb(150, 255, 255, 255), dp(10).toFloat(), dp(1))
        }

        fun selected(): String? = overlayView?.selectedEditTarget()
        fun refresh() { redraw() }

        val actions = listOf<Pair<String, () -> Unit>>(
            "−" to { selected()?.let { settingsRepo.setBlockScale(it, settingsRepo.blockScale(it) - .10f); refresh() } },
            "+" to { selected()?.let { settingsRepo.setBlockScale(it, settingsRepo.blockScale(it) + .10f); refresh() } },
            "◐" to { selected()?.let { val o=settingsRepo.blockOpacity(it); settingsRepo.setBlockOpacity(it, if (o <= .36f) 1f else o - .25f); refresh() } },
            "L" to { selected()?.let { settingsRepo.setBlockLocked(it, !settingsRepo.isBlockLocked(it)); refresh() } },
            "H" to { selected()?.let { settingsRepo.setBlockHidden(it, !settingsRepo.isBlockHidden(it)); refresh() } },
            "↶" to { lastEditSnapshot?.let { (block,x,y) -> settingsRepo.setPosition(block,x,y); overlayView?.setEditTarget(block); rebuildTouchableZones(); refresh() } },
            "✓" to { settingsRepo.editMode=false; overlayView?.setEditTarget(null); rebuildTouchableZones(); redraw() },
            "↺" to { settingsRepo.resetHudCustomization(); overlayView?.setEditTarget(null); rebuildTouchableZones(); redraw() }
        )
        actions.forEach { (label, action) ->
            bar.addView(button(label, action), LinearLayout.LayoutParams(0, dp(34), 1f).apply { marginStart=dp(2); marginEnd=dp(2) })
        }

        val screenW = resources.displayMetrics.widthPixels
        val screenH = resources.displayMetrics.heightPixels
        val width = min((screenW * .92f).toInt(), dp(390))
        val height = dp(42)
        val params = smallTouchableParams(width, height).apply {
            x = (screenW - width) / 2
            y = (screenH - height - dp(62)).coerceAtLeast(0)
        }
        editToolbarView = bar
        wm.addView(bar, params)
    }

'''
s = s[:start] + new_toolbar + s[end:]
p.write_text(s)

# -----------------------------------------------------------------------------
# BattleOverlayView: dynamic native anchor, per-block customization, range labels/history.
# -----------------------------------------------------------------------------
p = Path('app/src/main/java/com/lucianotoscano/pvppokego/overlay/BattleOverlayView.kt')
s = p.read_text()
s = s.replace('import com.lucianotoscano.pvppokego.data.GameDataRepository\n', 'import com.lucianotoscano.pvppokego.data.GameDataRepository\nimport com.lucianotoscano.pvppokego.data.EnergyConfidence\n')
s = s.replace('import kotlin.math.min\n', 'import kotlin.math.min\nimport kotlin.math.max\n')
s = must_replace(s,
    '    private var switchChoicePromptVisible = false\n',
    '    private var switchChoicePromptVisible = false\n    private var nativeTopCardYpx: Int? = null\n    private var blockAlphaMultiplier = 1f\n',
    'view fields')
s = must_replace(s,
    '    fun showMoveMenu(kind: MoveKind, options: List<MoveDef>) = Unit\n',
    '''    fun setNativeTopCardY(y: Int?) { if (y != null) nativeTopCardYpx = y; invalidate() }
    fun selectedEditTarget(): String? = editTarget
    fun blockPosition(block: String): Pair<Float, Float>? = when (block) {
        BLOCK_PLAYER_MATCHUP -> topPos(block, PLAYER_MATCHUP_X)
        BLOCK_STRONG_TYPES -> topPos(block, STRONG_TYPES_RIGHT_X)
        BLOCK_RESERVE_1 -> reservePos(0)
        BLOCK_RESERVE_2 -> reservePos(1)
        BLOCK_ENEMY_MOVES -> pos(block, ENEMY_MOVE_CENTER_X, ENEMY_MOVE_FAST_Y)
        BLOCK_BATTLE_ASSIST -> pos(block, BATTLE_ASSIST_X, BATTLE_ASSIST_Y)
        BLOCK_HUD_TOGGLE -> pos(block, HUD_TOGGLE_CENTER_X, HUD_TOGGLE_CENTER_Y)
        BLOCK_ENEMY_HISTORY -> pos(block, ENEMY_HISTORY_X, ENEMY_HISTORY_Y)
        else -> null
    }
    fun showMoveMenu(kind: MoveKind, options: List<MoveDef>) = Unit
''',
    'view public helpers')

# Add topPos helpers after pos().
marker = '''    private fun pos(block: String, defaultX: Float, defaultY: Float): Pair<Float, Float> =
        settings.getPosition(block, defaultX, defaultY)
'''
replacement = marker + r'''

    private fun dynamicTopIconRefY(): Float {
        val cardTop = nativeTopCardYpx ?: return TOP_ICON_Y
        val statusId = resources.getIdentifier("status_bar_height", "dimen", "android")
        val statusPx = if (statusId > 0) resources.getDimensionPixelSize(statusId) else (24f * resources.displayMetrics.density).toInt()
        val radiusPx = 15f * u()
        val preferred = cardTop.toFloat() - radiusPx - 5f * u()
        val safe = max(statusPx + radiusPx + 2f * u(), preferred)
        return refY(safe)
    }

    private fun topPos(block: String, defaultX: Float): Pair<Float, Float> =
        if (settings.hasPosition(block)) settings.getPosition(block, defaultX, TOP_ICON_Y)
        else defaultX to dynamicTopIconRefY()

    private inline fun withBlockAlpha(block: String, draw: () -> Unit) {
        if (settings.isBlockHidden(block)) return
        val old = blockAlphaMultiplier
        blockAlphaMultiplier = settings.blockOpacity(block)
        try { draw() } finally { blockAlphaMultiplier = old }
    }
'''
s = must_replace(s, marker, replacement, 'topPos helpers')

# Make drag lock-aware and dynamic-top-aware.
s = s.replace('''        fun near(block: String, dx: Float, dy: Float, halfW: Float, halfH: Float): Boolean {
            val (cx, cy) = pos(block, dx, dy)
            return x in (cx-halfW)..(cx+halfW) && y in (cy-halfH)..(cy+halfH)
        }
''', '''        fun near(block: String, dx: Float, dy: Float, halfW: Float, halfH: Float): Boolean {
            if (settings.isBlockLocked(block) || settings.isBlockHidden(block)) return false
            val (cx, cy) = if (block == BLOCK_PLAYER_MATCHUP || block == BLOCK_STRONG_TYPES) topPos(block, dx) else pos(block, dx, dy)
            return x in (cx-halfW)..(cx+halfW) && y in (cy-halfH)..(cy+halfH)
        }
''')
s = s.replace('''            val (right, cy) = pos(BLOCK_STRONG_TYPES, STRONG_TYPES_RIGHT_X, TOP_ICON_Y)
''', '''            val (right, cy) = topPos(BLOCK_STRONG_TYPES, STRONG_TYPES_RIGHT_X)
''')
s = s.replace('''        settings.setPosition(block, refX(screenX), refY(screenY))
''', '''        if (settings.isBlockLocked(block)) return
        settings.setPosition(block, refX(screenX), refY(screenY))
''')

# Replace the main drawing dispatch with block-alpha wrappers and history.
old_draw = '''        if (settings.showCurrentIndicator) {
            val (x, y) = pos(BLOCK_PLAYER_MATCHUP, PLAYER_MATCHUP_X, TOP_ICON_Y)
            drawMatchupIndicator(canvas, rx(x), ry(y), state.playerMatchup, 14f*u())
        }
        if (settings.showStrongTypes) drawStrongTypes(canvas)
        if (settings.showEnemyMoves) drawMoveTriangle(canvas)
        if (state.chargedIncoming && settings.showEnemyMoves && !settings.battleAssistEnabled) drawShieldAdvice(canvas)
        if (settings.analyzeReserves) {
            if (switchChoicePromptVisible) drawSwitchChoiceIndicators(canvas) else drawReserves(canvas)
        }
        if (settings.battleAssistEnabled) drawBattleAssist(canvas)
'''
new_draw = '''        if (settings.showCurrentIndicator) withBlockAlpha(BLOCK_PLAYER_MATCHUP) {
            val (x, y) = topPos(BLOCK_PLAYER_MATCHUP, PLAYER_MATCHUP_X)
            drawMatchupIndicator(canvas, rx(x), ry(y), state.playerMatchup, 14f*u()*settings.blockScale(BLOCK_PLAYER_MATCHUP))
        }
        if (settings.showStrongTypes) withBlockAlpha(BLOCK_STRONG_TYPES) { drawStrongTypes(canvas) }
        if (settings.showEnemyMoves) withBlockAlpha(BLOCK_ENEMY_MOVES) { drawMoveTriangle(canvas) }
        if (state.chargedIncoming && settings.showEnemyMoves && !settings.battleAssistEnabled) drawShieldAdvice(canvas)
        if (settings.analyzeReserves) {
            if (switchChoicePromptVisible) drawSwitchChoiceIndicators(canvas) else drawReserves(canvas)
        }
        if (settings.battleAssistEnabled) withBlockAlpha(BLOCK_BATTLE_ASSIST) { drawBattleAssist(canvas) }
        if (settings.showEnemyHistory) withBlockAlpha(BLOCK_ENEMY_HISTORY) { drawEnemyHistory(canvas) }
'''
s = must_replace(s, old_draw, new_draw, 'main draw dispatch')

# Dynamic top references inside player edit and strong types.
s = s.replace('''            val (mx, my) = pos(BLOCK_PLAYER_MATCHUP, PLAYER_MATCHUP_X, TOP_ICON_Y)
''', '''            val (mx, my) = topPos(BLOCK_PLAYER_MATCHUP, PLAYER_MATCHUP_X)
''')
s = s.replace('''        val radius = 14f*u()
        val (rightRef, yRef) = pos(BLOCK_STRONG_TYPES, STRONG_TYPES_RIGHT_X, TOP_ICON_Y)
''', '''        val bs = settings.blockScale(BLOCK_STRONG_TYPES)
        val radius = 14f*u()*bs
        val (rightRef, yRef) = topPos(BLOCK_STRONG_TYPES, STRONG_TYPES_RIGHT_X)
''')
s = s.replace('''        val spacing = rx(35f)
''', '''        val spacing = rx(35f) * bs
''', 1)

# Enemy move block scale and confidence label.
s = s.replace('''        val centerX=rx(centerRefX); val fastY=ry(fastRefY); val chargedY=ry(fastRefY+123f)
        val spread=rx(74f); val fastRadius=31f*u(); val chargedRadius=41f*u()
''', '''        val bs = settings.blockScale(BLOCK_ENEMY_MOVES)
        val centerX=rx(centerRefX); val fastY=ry(fastRefY); val chargedY=fastY+ry(123f)*bs
        val spread=rx(74f)*bs; val fastRadius=31f*u()*bs; val chargedRadius=41f*u()*bs
''')
s = s.replace('''        drawEditSelection(canvas, BLOCK_ENEMY_MOVES, centerX, (fastY+chargedY)/2f, 132f*u(), 116f*u())
    }
''', '''        if (settings.showHudText) {
            val p = state.charged1 ?: state.charged2
            if (p != null) {
                val energyText = when (p.confidence) {
                    EnergyConfidence.CONFIRMED -> "ENERGIA ${p.currentEnergy} • CONFIRMADA"
                    EnergyConfidence.ESTIMATED -> "ENERGIA ~${p.currentEnergy} • ESTIMADA"
                    EnergyConfidence.RANGE -> "ENERGIA ${p.minEnergy}-${p.maxEnergy} • FAIXA"
                }
                paint.color=hudColor(Color.WHITE,.82f); paint.textAlign=Paint.Align.CENTER; paint.textSize=9.5f*u()*bs; paint.typeface=Typeface.DEFAULT_BOLD
                paint.setShadowLayer(2f,0f,1f,Color.BLACK); canvas.drawText(energyText,centerX,chargedY+chargedRadius+54f*u()*bs,paint); paint.clearShadowLayer()
            }
        }
        drawEditSelection(canvas, BLOCK_ENEMY_MOVES, centerX, (fastY+chargedY)/2f, 138f*u()*bs, 122f*u()*bs)
    }
''', 1)

# Reserve blocks: independent customization + recommendation labels.
old_reserve_start = '''            val block = if (index == 0) BLOCK_RESERVE_1 else BLOCK_RESERVE_2
            val (indicatorRefX, cardRefY) = reservePos(index)
            val indicatorX = rx(indicatorRefX)
            val cardCenterY = ry(cardRefY)
            drawMatchupIndicator(canvas,indicatorX,cardCenterY,reserve.matchup,13.5f*u())
            if(settings.showReserveTypes && reserve.types.isNotEmpty()){
                val typeRadius=12f*u(); val spacing=rx(31f); val centerX=rx(indicatorRefX+98f)
'''
new_reserve_start = '''            val block = if (index == 0) BLOCK_RESERVE_1 else BLOCK_RESERVE_2
            if (settings.isBlockHidden(block)) return@forEachIndexed
            val bs = settings.blockScale(block)
            val oldAlpha = blockAlphaMultiplier
            blockAlphaMultiplier = settings.blockOpacity(block)
            val (indicatorRefX, cardRefY) = reservePos(index)
            val indicatorX = rx(indicatorRefX)
            val cardCenterY = ry(cardRefY)
            drawMatchupIndicator(canvas,indicatorX,cardCenterY,reserve.matchup,13.5f*u()*bs)
            if(settings.showReserveTypes && reserve.types.isNotEmpty()){
                val typeRadius=12f*u()*bs; val spacing=rx(31f)*bs; val centerX=rx(indicatorRefX+98f)
'''
s = must_replace(s, old_reserve_start, new_reserve_start, 'reserve block start')
s = s.replace('''            drawEditSelection(canvas, block, indicatorX+rx(49f), cardCenterY+ry(41f), 112f*u(), 72f*u())
        }
''', '''            if (settings.showHudText && !reserve.recommendation.isNullOrBlank()) {
                paint.color=hudColor(when(reserve.recommendation){"MELHOR"->Color.rgb(46,188,105);"EVITAR"->Color.rgb(232,61,72);else->Color.WHITE})
                paint.textAlign=Paint.Align.CENTER; paint.textSize=9.2f*u()*bs; paint.typeface=Typeface.DEFAULT_BOLD
                paint.setShadowLayer(2f,0f,1f,Color.BLACK); canvas.drawText(reserve.recommendation!!,indicatorX,cardCenterY+27f*u()*bs,paint); paint.clearShadowLayer()
            }
            drawEditSelection(canvas, block, indicatorX+rx(49f), cardCenterY+ry(41f), 112f*u()*bs, 72f*u()*bs)
            blockAlphaMultiplier = oldAlpha
        }
''', 1)

# Switch choice labels.
s = s.replace('''        candidates.take(xs.size).forEachIndexed { index, reserve ->
            drawMatchupIndicator(canvas, xs[index], y, reserve.matchup, 16f*u())
        }
''', '''        candidates.take(xs.size).forEachIndexed { index, reserve ->
            drawMatchupIndicator(canvas, xs[index], y, reserve.matchup, 16f*u())
            if (settings.showHudText && !reserve.recommendation.isNullOrBlank()) {
                paint.color=hudColor(when(reserve.recommendation){"MELHOR"->Color.rgb(46,188,105);"EVITAR"->Color.rgb(232,61,72);else->Color.WHITE})
                paint.textAlign=Paint.Align.CENTER; paint.textSize=10f*u(); paint.typeface=Typeface.DEFAULT_BOLD
                paint.setShadowLayer(2f,0f,1f,Color.BLACK); canvas.drawText(reserve.recommendation!!,xs[index],y+30f*u(),paint); paint.clearShadowLayer()
            }
        }
''')

# Battle assist scale.
s = s.replace('''        val halfW = 176f*u()
        val halfH = 30f*u()
''', '''        val bs = settings.blockScale(BLOCK_BATTLE_ASSIST)
        val halfW = 176f*u()*bs
        val halfH = 30f*u()*bs
''', 1)

# Add history drawer before timers.
marker = '    private fun drawTimers(canvas: Canvas) {'
history = r'''    private fun drawEnemyHistory(canvas: Canvas) {
        if (state.enemyHistory.isEmpty()) return
        val (refX, refY) = pos(BLOCK_ENEMY_HISTORY, ENEMY_HISTORY_X, ENEMY_HISTORY_Y)
        val bs = settings.blockScale(BLOCK_ENEMY_HISTORY)
        val x = rx(refX); val y = ry(refY)
        paint.textAlign=Paint.Align.RIGHT; paint.typeface=Typeface.DEFAULT_BOLD; paint.textSize=9.4f*u()*bs
        state.enemyHistory.take(3).forEachIndexed { i, line ->
            paint.color=hudColor(Color.WHITE,.86f); paint.setShadowLayer(2f,0f,1f,Color.BLACK)
            canvas.drawText(line,x,y+i*14f*u()*bs,paint)
        }
        paint.clearShadowLayer()
        drawEditSelection(canvas,BLOCK_ENEMY_HISTORY,x-rx(85f)*bs,y+12f*u()*bs,rx(95f)*bs,28f*u()*bs)
    }

'''
s = must_replace(s, marker, history + marker, 'history drawer')

# hudColor includes per-block alpha.
s = s.replace('''    private fun hudColor(color:Int,multiplier:Float=1f):Int{val a=(hudAlpha*multiplier).toInt().coerceIn(0,255);return Color.argb(a,Color.red(color),Color.green(color),Color.blue(color))}
''', '''    private fun hudColor(color:Int,multiplier:Float=1f):Int{val a=(hudAlpha*multiplier*blockAlphaMultiplier).toInt().coerceIn(0,255);return Color.argb(a,Color.red(color),Color.green(color),Color.blue(color))}
''')

# Add history constants and keep dynamic-top default in the safe region.
s = s.replace('''        const val BLOCK_BATTLE_ASSIST = "hud_battle_assist"
''', '''        const val BLOCK_BATTLE_ASSIST = "hud_battle_assist"
        const val BLOCK_ENEMY_HISTORY = "hud_enemy_history"
''')
s = s.replace('''        const val BATTLE_ASSIST_Y=770f
''', '''        const val BATTLE_ASSIST_Y=770f
        const val ENEMY_HISTORY_X=846f
        const val ENEMY_HISTORY_Y=650f
''')
p.write_text(s)

# -----------------------------------------------------------------------------
# QuickOverlayMenu: true basic/advanced menu and triangle league badge.
# -----------------------------------------------------------------------------
p = Path('app/src/main/java/com/lucianotoscano/pvppokego/overlay/QuickOverlayMenu.kt')
s = p.read_text()
s = s.replace('import android.graphics.Color\n', 'import android.graphics.Color\nimport android.graphics.Canvas\nimport android.graphics.Paint\nimport android.graphics.Path\n')
s = s.replace('    private var leagueView: TextView? = null\n', '    private var leagueView: LeagueBadgeView? = null\n')
old_league_attach = '''        val league = TextView(context).apply {
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            textSize = 17f
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            setShadowLayer(2f, 0f, 1f, Color.BLACK)
            contentDescription = "Liga atual"
            visibility = View.GONE
        }
'''
new_league_attach = '''        val league = LeagueBadgeView(context).apply {
            contentDescription = "Liga atual"
            visibility = View.GONE
        }
'''
s = must_replace(s, old_league_attach, new_league_attach, 'league badge attach')
old_refresh = '''        leagueView?.apply {
            text = when (kind) {
                LeagueKind.GREAT -> "◇"
                LeagueKind.ULTRA -> "◆"
                LeagueKind.MASTER -> "♛"
                LeagueKind.AUTO -> "◎"
            }
            background = leagueBackground(kind)
        }
'''
new_refresh = '''        leagueView?.setKind(kind)
'''
s = must_replace(s, old_refresh, new_refresh, 'league refresh')

# Replace menu builders with basic/advanced split.
start = s.index('    private fun buildIconMenu(parent: LinearLayout) {')
end = s.index('    private fun addIconCell(', start)
new_builders = r'''    private fun buildIconMenu(parent: LinearLayout) {
        fun row(vararg items: IconItem) {
            val line = LinearLayout(context).apply { orientation=LinearLayout.HORIZONTAL; gravity=Gravity.START }
            items.forEach { addIconCell(line,it) }
            while(line.childCount<4) line.addView(View(context),LinearLayout.LayoutParams(0,dp(43),1f))
            parent.addView(line,LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,dp(47)))
        }
        if (!settings.quickMenuAdvanced) {
            row(
                IconItem("◉","HUD",!callbacks.isHudCollapsed()){callbacks.onToggleHud();refreshPanel()},
                IconItem("★","Assistido",settings.battleAssistEnabled){settings.battleAssistEnabled=!settings.battleAssistEnabled;changed()},
                IconItem("△","Liga",null){cycleLeague()},
                IconItem("↔","Editar HUD",settings.editMode){settings.editMode=!settings.editMode;callbacks.onSettingsChanged(true);if(settings.editMode)dismissPanel()else refreshPanel()}
            )
            row(
                IconItem("T","Textos HUD",settings.showHudText){settings.showHudText=!settings.showHudText;changed()},
                IconItem("…","Avançado",null){settings.quickMenuAdvanced=true;refreshPanel()},
                IconItem("≡","Mostrar textos",null){settings.quickMenuShowLabels=true;refreshPanel()},
                IconItem("×","Fechar",null){dismissPanel()}
            )
        } else {
            row(
                IconItem("A","Reconhecimento",settings.autoRecognition){settings.autoRecognition=!settings.autoRecognition;changed()},
                IconItem("◇","Fraquezas",settings.showStrongTypes){settings.showStrongTypes=!settings.showStrongTypes;changed()},
                IconItem("✓","Matchup",settings.showCurrentIndicator){settings.showCurrentIndicator=!settings.showCurrentIndicator;changed()},
                IconItem("2","Reservas",settings.analyzeReserves){settings.analyzeReserves=!settings.analyzeReserves;changed()}
            )
            row(
                IconItem("T","Tipos reservas",settings.showReserveTypes){settings.showReserveTypes=!settings.showReserveTypes;changed()},
                IconItem("⚔","Ataques",settings.showEnemyMoves){settings.showEnemyMoves=!settings.showEnemyMoves;changed()},
                IconItem("••","Contagem",settings.showChargedCounter){settings.showChargedCounter=!settings.showChargedCounter;changed()},
                IconItem("⚡","Energia",settings.showEnergyProgress){settings.showEnergyProgress=!settings.showEnergyProgress;changed()}
            )
            row(
                IconItem("↻","Timer",settings.showSwitchTimer){settings.showSwitchTimer=!settings.showSwitchTimer;changed()},
                IconItem("H","Histórico",settings.showEnemyHistory){settings.showEnemyHistory=!settings.showEnemyHistory;changed()},
                IconItem("◐","Opacidade",null){settings.opacity=if(settings.opacity>=.99f).3f else settings.opacity+.1f;callbacks.onSettingsChanged(false);refreshPanel()},
                IconItem("D","Debug",settings.debugMode){settings.debugMode=!settings.debugMode;changed()}
            )
            row(
                IconItem("‹","Básico",null){settings.quickMenuAdvanced=false;refreshPanel()},
                IconItem("↺","Restaurar",null){resetPositions()},
                IconItem("▦","Textos",null){settings.quickMenuShowLabels=true;refreshPanel()},
                IconItem("×","Fechar",null){dismissPanel()}
            )
        }
    }

    /** PGSharp-like list: compact icon cell at left and text at right. */
    private fun buildLabeledMenu(parent: LinearLayout) {
        if (!settings.quickMenuAdvanced) {
            addRow(parent,"◉","HUD",!callbacks.isHudCollapsed()){callbacks.onToggleHud();refreshPanel()}
            addRow(parent,"★","Modo Batalha Assistido",settings.battleAssistEnabled){settings.battleAssistEnabled=!settings.battleAssistEnabled;changed()}
            addRow(parent,"△","Liga: ${leagueShort(settings.leagueMode)}",null){cycleLeague()}
            addRow(parent,"↔","Modo edição",settings.editMode){settings.editMode=!settings.editMode;callbacks.onSettingsChanged(true);if(settings.editMode)dismissPanel()else refreshPanel()}
            addRow(parent,"T","Textos do HUD",settings.showHudText){settings.showHudText=!settings.showHudText;changed()}
            addRow(parent,"…","Configurações avançadas",null){settings.quickMenuAdvanced=true;refreshPanel()}
            addRow(parent,"▦","Somente ícones",null){settings.quickMenuShowLabels=false;refreshPanel()}
            addRow(parent,"×","Fechar",null){dismissPanel()}
        } else {
            addRow(parent,"A","Reconhecimento automático",settings.autoRecognition){settings.autoRecognition=!settings.autoRecognition;changed()}
            addRow(parent,"◇","Fraquezas do inimigo",settings.showStrongTypes){settings.showStrongTypes=!settings.showStrongTypes;changed()}
            addRow(parent,"✓","Indicador Pokémon atual",settings.showCurrentIndicator){settings.showCurrentIndicator=!settings.showCurrentIndicator;changed()}
            addRow(parent,"2","Analisar reservas",settings.analyzeReserves){settings.analyzeReserves=!settings.analyzeReserves;changed()}
            addRow(parent,"T","Tipos das reservas",settings.showReserveTypes){settings.showReserveTypes=!settings.showReserveTypes;changed()}
            addRow(parent,"⚔","Ataques do inimigo",settings.showEnemyMoves){settings.showEnemyMoves=!settings.showEnemyMoves;changed()}
            addRow(parent,"••","Pontos de carregamento",settings.showChargedCounter){settings.showChargedCounter=!settings.showChargedCounter;changed()}
            addRow(parent,"⚡","Progresso de energia",settings.showEnergyProgress){settings.showEnergyProgress=!settings.showEnergyProgress;changed()}
            addRow(parent,"↻","Timer de troca",settings.showSwitchTimer){settings.showSwitchTimer=!settings.showSwitchTimer;changed()}
            addRow(parent,"H","Histórico do inimigo",settings.showEnemyHistory){settings.showEnemyHistory=!settings.showEnemyHistory;changed()}
            addRow(parent,"S","Escala ${(settings.scale*100).toInt()}%",null){settings.scale=if(settings.scale>=1.19f).5f else settings.scale+.1f;callbacks.onSettingsChanged(true);refreshPanel()}
            addRow(parent,"◐","Opacidade ${(settings.opacity*100).toInt()}%",null){settings.opacity=if(settings.opacity>=.99f).3f else settings.opacity+.1f;callbacks.onSettingsChanged(false);refreshPanel()}
            addRow(parent,"D","DEBUG",settings.debugMode){settings.debugMode=!settings.debugMode;changed()}
            addRow(parent,"U","Mostrar todos os blocos",null){SettingsRepository.HUD_BLOCKS.forEach{settings.setBlockHidden(it,false)};changed(true)}
            addRow(parent,"‹","Voltar ao menu básico",null){settings.quickMenuAdvanced=false;refreshPanel()}
            addRow(parent,"↺","Restaurar posições/layout",null){settings.resetHudCustomization();resetPositions()}
            addRow(parent,"▦","Somente ícones",null){settings.quickMenuShowLabels=false;refreshPanel()}
            addRow(parent,"×","Fechar",null){dismissPanel()}
        }
    }

'''
s = s[:start] + new_builders + s[end:]

# Replace leagueBackground helper with custom drawable class. Keep helper if harmless but add class before enums.
insert = r'''
    private inner class LeagueBadgeView(ctx: Context) : View(ctx) {
        private val fill = Paint(Paint.ANTI_ALIAS_FLAG)
        private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style=Paint.Style.STROKE; strokeWidth=dp(2).toFloat(); color=Color.argb(235,240,240,240) }
        private var kind: LeagueKind = LeagueKind.AUTO
        fun setKind(value: LeagueKind) { kind=value; invalidate() }
        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            val w=width.toFloat(); val h=height.toFloat(); val pad=3f*density
            val tri=Path().apply { moveTo(pad,pad); lineTo(w-pad,pad); lineTo(w/2f,h-pad); close() }
            fill.style=Paint.Style.FILL
            fill.color=when(kind){
                LeagueKind.GREAT->Color.rgb(45,99,190)
                LeagueKind.ULTRA->Color.rgb(58,72,78)
                LeagueKind.MASTER->Color.rgb(82,37,145)
                LeagueKind.AUTO->Color.rgb(66,72,84)
            }
            canvas.drawPath(tri,fill)
            canvas.drawPath(tri,strokePaint)
            fill.color=when(kind){LeagueKind.GREAT->Color.rgb(235,55,62);LeagueKind.ULTRA->Color.rgb(245,202,34);LeagueKind.MASTER->Color.rgb(237,66,183);LeagueKind.AUTO->Color.rgb(120,190,240)}
            fill.strokeWidth=dp(2).toFloat(); fill.style=Paint.Style.STROKE
            canvas.drawLine(w*.28f,h*.35f,w*.73f,h*.22f,fill)
            canvas.drawLine(w*.34f,h*.52f,w*.79f,h*.36f,fill)
            fill.style=Paint.Style.FILL; fill.color=Color.WHITE
            canvas.drawCircle(w*.35f,h*.25f,w*.085f,fill)
            strokePaint.strokeWidth=dp(1).toFloat(); strokePaint.color=Color.rgb(40,45,55)
            canvas.drawCircle(w*.35f,h*.25f,w*.032f,strokePaint)
        }
    }
'''
idx = s.index('    private enum class LeagueKind')
s = s[:idx] + insert + '\n' + s[idx:]
p.write_text(s)
