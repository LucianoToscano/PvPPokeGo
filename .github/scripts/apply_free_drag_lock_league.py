from pathlib import Path

ROOT = Path('.')

def patch(path, old, new, count=1):
    p = ROOT / path
    s = p.read_text()
    if old not in s:
        raise SystemExit(f'pattern not found in {path}: {old[:120]!r}')
    s2 = s.replace(old, new, count)
    p.write_text(s2)

# ---------- SettingsRepository: global layout lock + independent lock block ----------
path='app/src/main/java/com/lucianotoscano/pvppokego/data/SettingsRepository.kt'
patch(path,
'''    var editMode: Boolean
        get() = prefs.getBoolean("edit_mode", false)
        set(v) = prefs.edit().putBoolean("edit_mode", v).apply()
''',
'''    var editMode: Boolean
        get() = prefs.getBoolean("edit_mode", false)
        set(v) = prefs.edit().putBoolean("edit_mode", v).apply()

    /** Global HUD position lock. Unlocking enables direct drag without opening edit mode. */
    var layoutLocked: Boolean
        get() = prefs.getBoolean("layout_locked", true)
        set(v) = prefs.edit().putBoolean("layout_locked", v).apply()
''')
patch(path,
'''            "hud_reserve_1", "hud_reserve_2", "hud_reserves",
            "hud_toggle", "hud_battle_assist", "hud_enemy_history"
''',
'''            "hud_reserve_1", "hud_reserve_2", "hud_reserves",
            "hud_toggle", "hud_lock", "hud_battle_assist", "hud_enemy_history"
''')

# ---------- BattleOverlayView: lock icon + unlocked direct drag ----------
path='app/src/main/java/com/lucianotoscano/pvppokego/overlay/BattleOverlayView.kt'
patch(path,
'''        BLOCK_BATTLE_ASSIST -> pos(block, BATTLE_ASSIST_X, BATTLE_ASSIST_Y)
        BLOCK_HUD_TOGGLE -> pos(block, HUD_TOGGLE_CENTER_X, HUD_TOGGLE_CENTER_Y)
        BLOCK_ENEMY_HISTORY -> pos(block, ENEMY_HISTORY_X, ENEMY_HISTORY_Y)
''',
'''        BLOCK_BATTLE_ASSIST -> pos(block, BATTLE_ASSIST_X, BATTLE_ASSIST_Y)
        BLOCK_HUD_TOGGLE -> pos(block, HUD_TOGGLE_CENTER_X, HUD_TOGGLE_CENTER_Y)
        BLOCK_HUD_LOCK -> pos(block, HUD_LOCK_CENTER_X, HUD_LOCK_CENTER_Y)
        BLOCK_ENEMY_HISTORY -> pos(block, ENEMY_HISTORY_X, ENEMY_HISTORY_Y)
''')
patch(path,
'''        if (!settings.editMode || collapsed || width <= 0 || height <= 0) return null
''',
'''        if ((settings.layoutLocked && !settings.editMode) || collapsed || width <= 0 || height <= 0) return null
''')
patch(path,
'''        if (near(BLOCK_HUD_TOGGLE, HUD_TOGGLE_CENTER_X, HUD_TOGGLE_CENTER_Y, 32f, 32f)) return BLOCK_HUD_TOGGLE
        if (near(BLOCK_PLAYER_MATCHUP, PLAYER_MATCHUP_X, TOP_ICON_Y, 34f, 30f)) return BLOCK_PLAYER_MATCHUP
''',
'''        if (near(BLOCK_HUD_TOGGLE, HUD_TOGGLE_CENTER_X, HUD_TOGGLE_CENTER_Y, 32f, 32f)) return BLOCK_HUD_TOGGLE
        if (near(BLOCK_HUD_LOCK, HUD_LOCK_CENTER_X, HUD_LOCK_CENTER_Y, 32f, 32f)) return BLOCK_HUD_LOCK
        if (near(BLOCK_PLAYER_MATCHUP, PLAYER_MATCHUP_X, TOP_ICON_Y, 34f, 30f)) return BLOCK_PLAYER_MATCHUP
''')
patch(path,
'''        val (r2x, r2y) = reservePos(1)
        if (x in (r2x-65f)..(r2x+175f) && y in (r2y-55f)..(r2y+125f)) return BLOCK_RESERVE_2
        return null
''',
'''        val (r2x, r2y) = reservePos(1)
        if (x in (r2x-65f)..(r2x+175f) && y in (r2y-55f)..(r2y+125f)) return BLOCK_RESERVE_2
        if (near(BLOCK_ENEMY_HISTORY, ENEMY_HISTORY_X, ENEMY_HISTORY_Y, 180f, 80f)) return BLOCK_ENEMY_HISTORY
        return null
''')
patch(path,
'''        if (!settings.editMode || width <= 0 || height <= 0) return
''',
'''        if ((settings.layoutLocked && !settings.editMode) || width <= 0 || height <= 0) return
''')
patch(path,
'''        if (settings.debugMode) drawDebug(canvas)
        drawVisibilityToggle(canvas, false)
    }

    private fun drawVisibilityToggle(canvas: Canvas, hidden: Boolean) {
''',
'''        if (settings.debugMode) drawDebug(canvas)
        drawLayoutLock(canvas)
        drawVisibilityToggle(canvas, false)
    }

    private fun drawLayoutLock(canvas: Canvas) {
        val (refCx, refCy) = pos(BLOCK_HUD_LOCK, HUD_LOCK_CENTER_X, HUD_LOCK_CENTER_Y)
        val cx = rx(refCx); val cy = ry(refCy); val radius = 16f*u()
        paint.style = Paint.Style.FILL
        paint.color = hudColor(if (settings.layoutLocked) Color.rgb(72,76,82) else Color.rgb(45,112,82), .88f)
        canvas.drawCircle(cx, cy, radius, paint)
        stroke.style = Paint.Style.STROKE
        stroke.strokeWidth = 1.5f*u(); stroke.color = hudColor(Color.WHITE, .72f)
        canvas.drawCircle(cx, cy, radius, stroke)
        val body = RectF(cx-7f*u(), cy-1.5f*u(), cx+7f*u(), cy+9f*u())
        stroke.strokeWidth = 1.8f*u(); stroke.color = hudColor(Color.WHITE)
        canvas.drawRoundRect(body, 2.2f*u(), 2.2f*u(), stroke)
        val shackle = RectF(cx-5.2f*u(), cy-8f*u(), cx+5.2f*u(), cy+3.2f*u())
        canvas.drawArc(shackle, 190f, 160f, false, stroke)
        if (!settings.layoutLocked) {
            // Open shackle: short break/offset on the right makes unlocked state obvious.
            paint.style = Paint.Style.FILL; paint.color = hudColor(if (settings.layoutLocked) Color.rgb(72,76,82) else Color.rgb(45,112,82))
            canvas.drawRect(cx+3.8f*u(), cy-7.5f*u(), cx+8.2f*u(), cy-1f*u(), paint)
            stroke.color = hudColor(Color.WHITE); stroke.strokeWidth = 1.8f*u()
            canvas.drawLine(cx+4.7f*u(), cy-7.0f*u(), cx+7.4f*u(), cy-9.0f*u(), stroke)
        }
        drawEditSelection(canvas, BLOCK_HUD_LOCK, cx, cy, 25f*u(), 25f*u())
    }

    private fun drawVisibilityToggle(canvas: Canvas, hidden: Boolean) {
''')
patch(path,
'''        const val BLOCK_HUD_TOGGLE = "hud_toggle"
        const val BLOCK_BATTLE_ASSIST = "hud_battle_assist"
''',
'''        const val BLOCK_HUD_TOGGLE = "hud_toggle"
        const val BLOCK_HUD_LOCK = "hud_lock"
        const val BLOCK_BATTLE_ASSIST = "hud_battle_assist"
''')
patch(path,
'''        const val HUD_TOGGLE_CENTER_X=432f
        const val HUD_TOGGLE_CENTER_Y=66f
''',
'''        const val HUD_TOGGLE_CENTER_X=432f
        const val HUD_TOGGLE_CENTER_Y=66f
        const val HUD_LOCK_CENTER_X=382f
        const val HUD_LOCK_CENTER_Y=66f
''')

# ---------- BattleOverlayService: draggable eye/lock, global unlock, eye hides ALL ----------
path='app/src/main/java/com/lucianotoscano/pvppokego/overlay/BattleOverlayService.kt'
patch(path,
'''import com.lucianotoscano.pvppokego.data.BattleLeagueMode
''',
'''import com.lucianotoscano.pvppokego.data.BattleLeagueMode
import com.lucianotoscano.pvppokego.data.BattleDetection
''')
patch(path,
'''    private var hudToggleTouch: View? = null
    private var editTouchLayer: View? = null
''',
'''    private var hudToggleTouch: View? = null
    private var hudLockTouch: View? = null
    private var editTouchLayer: View? = null
''')
patch(path,
'''        attachMoveTouchZones()
        attachHudToggleTouch()
        quickMenu = QuickOverlayMenu(
''',
'''        attachMoveTouchZones()
        attachHudToggleTouch()
        attachHudLockTouch()
        quickMenu = QuickOverlayMenu(
''')
patch(path,
'''                override fun isHudCollapsed(): Boolean = hudCollapsed

                override fun onResetHudPositions() {
''',
'''                override fun isHudCollapsed(): Boolean = hudCollapsed
                override fun isLayoutLocked(): Boolean = settingsRepo.layoutLocked

                override fun onResetHudPositions() {
''')
patch(path,
'''    private fun hudToggleAnchor(): Pair<Float, Float> = settingsRepo.getPosition(
        BattleOverlayView.BLOCK_HUD_TOGGLE,
        BattleOverlayView.HUD_TOGGLE_CENTER_X,
        BattleOverlayView.HUD_TOGGLE_CENTER_Y
    )
''',
'''    private fun hudToggleAnchor(): Pair<Float, Float> = settingsRepo.getPosition(
        BattleOverlayView.BLOCK_HUD_TOGGLE,
        BattleOverlayView.HUD_TOGGLE_CENTER_X,
        BattleOverlayView.HUD_TOGGLE_CENTER_Y
    )

    private fun hudLockAnchor(): Pair<Float, Float> = settingsRepo.getPosition(
        BattleOverlayView.BLOCK_HUD_LOCK,
        BattleOverlayView.HUD_LOCK_CENTER_X,
        BattleOverlayView.HUD_LOCK_CENTER_Y
    )
''')
# Replace eye touch implementation and params section with draggable eye + lock.
start='''    private fun attachHudToggleTouch() {
        val wm = windowManager ?: return
        if (hudToggleTouch != null) return
        hudToggleTouch = View(this).apply {
            setBackgroundColor(Color.TRANSPARENT)
            isClickable = true
            setOnClickListener { toggleHudCollapsed() }
        }.also { wm.addView(it, hudToggleParams()) }
    }

    private fun hudToggleParams(): WindowManager.LayoutParams {
        val metrics = resources.displayMetrics
        val sx = metrics.widthPixels / 864f
        val sy = metrics.heightPixels / 1536f
        val unit = min(sx, sy) * (settingsRepo.scale / .8f).coerceIn(.62f, 1.5f)
        val size = (52f * unit).toInt().coerceAtLeast(44)
        val (refX, refY) = hudToggleAnchor()
        val centerX = refX * sx
        val centerY = refY * sy
        return smallTouchableParams(size, size).apply {
            x = (centerX - size / 2f).toInt()
            y = (centerY - size / 2f).toInt()
        }
    }
'''
replacement='''    private fun attachHudToggleTouch() {
        val wm = windowManager ?: return
        if (hudToggleTouch != null) return
        val params = hudControlParams(hudToggleAnchor())
        hudToggleTouch = View(this).apply {
            setBackgroundColor(Color.TRANSPARENT)
            isClickable = true
            installHudControlDrag(this, params, BattleOverlayView.BLOCK_HUD_TOGGLE) { toggleHudCollapsed() }
        }.also { wm.addView(it, params) }
    }

    private fun attachHudLockTouch() {
        val wm = windowManager ?: return
        if (hudLockTouch != null) return
        val params = hudControlParams(hudLockAnchor())
        hudLockTouch = View(this).apply {
            setBackgroundColor(Color.TRANSPARENT)
            isClickable = true
            installHudControlDrag(this, params, BattleOverlayView.BLOCK_HUD_LOCK) {
                settingsRepo.layoutLocked = !settingsRepo.layoutLocked
                if (settingsRepo.layoutLocked) overlayView?.setEditTarget(null)
                redraw()
            }
        }.also { wm.addView(it, params) }
    }

    private fun hudControlParams(anchor: Pair<Float, Float>): WindowManager.LayoutParams {
        val metrics = resources.displayMetrics
        val sx = metrics.widthPixels / 864f
        val sy = metrics.heightPixels / 1536f
        val unit = min(sx, sy) * (settingsRepo.scale / .8f).coerceIn(.62f, 1.5f)
        val size = (52f * unit).toInt().coerceAtLeast(44)
        val centerX = anchor.first * sx
        val centerY = anchor.second * sy
        return smallTouchableParams(size, size).apply {
            x = (centerX - size / 2f).toInt()
            y = (centerY - size / 2f).toInt()
        }
    }

    private fun installHudControlDrag(view: View, params: WindowManager.LayoutParams, block: String, onTap: () -> Unit) {
        var downX = 0f; var downY = 0f; var startX = 0; var startY = 0; var dragging = false
        view.setOnTouchListener { _, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    downX = event.rawX; downY = event.rawY; startX = params.x; startY = params.y; dragging = false; true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = event.rawX - downX; val dy = event.rawY - downY
                    if (!settingsRepo.layoutLocked && abs(dx) + abs(dy) >= 6f) dragging = true
                    if (dragging) {
                        val maxX = (resources.displayMetrics.widthPixels - params.width).coerceAtLeast(0)
                        val maxY = (resources.displayMetrics.heightPixels - params.height).coerceAtLeast(0)
                        params.x = (startX + dx).toInt().coerceIn(0, maxX)
                        params.y = (startY + dy).toInt().coerceIn(0, maxY)
                        runCatching { windowManager?.updateViewLayout(view, params) }
                    }
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (dragging) {
                        val sx = resources.displayMetrics.widthPixels / 864f
                        val sy = resources.displayMetrics.heightPixels / 1536f
                        settingsRepo.setPosition(block, (params.x + params.width/2f) / sx, (params.y + params.height/2f) / sy)
                        redraw()
                    } else onTap()
                    true
                }
                MotionEvent.ACTION_CANCEL -> true
                else -> true
            }
        }
    }
'''
patch(path,start,replacement)
patch(path,
'''        editTouchLayer = layer
        wm.addView(layer, params)
        attachEditToolbar()
        quickMenu?.bringToFront()
''',
'''        editTouchLayer = layer
        wm.addView(layer, params)
        if (settingsRepo.editMode) attachEditToolbar() else removeEditToolbar()
        bringHudControlsToFront()
        quickMenu?.bringToFront()
''')
patch(path,
'''    private fun syncEditTouchLayer(enabled: Boolean) {
        val shouldEdit = enabled && settingsRepo.editMode && !hudCollapsed
        if (shouldEdit) attachEditTouchLayer() else removeEditTouchLayer()
    }
''',
'''    private fun syncEditTouchLayer(enabled: Boolean) {
        val shouldDrag = enabled && !hudCollapsed && (settingsRepo.editMode || !settingsRepo.layoutLocked)
        if (shouldDrag) {
            attachEditTouchLayer()
            if (settingsRepo.editMode) attachEditToolbar() else removeEditToolbar()
            bringHudControlsToFront()
            quickMenu?.bringToFront()
        } else removeEditTouchLayer()
    }

    private fun bringHudControlsToFront() {
        fun readd(view: View?, params: WindowManager.LayoutParams) {
            if (view == null) return
            runCatching { windowManager?.removeView(view) }
            runCatching { windowManager?.addView(view, params) }
        }
        readd(hudToggleTouch, hudControlParams(hudToggleAnchor()))
        if (!hudCollapsed) readd(hudLockTouch, hudControlParams(hudLockAnchor()))
    }
''')
patch(path,
'''        runCatching { hudToggleTouch?.let { windowManager?.removeView(it) } }
        fastTouch = null
        charged1Touch = null
        charged2Touch = null
        hudToggleTouch = null
        attachMoveTouchZones()
        attachHudToggleTouch()
''',
'''        runCatching { hudToggleTouch?.let { windowManager?.removeView(it) } }
        runCatching { hudLockTouch?.let { windowManager?.removeView(it) } }
        fastTouch = null
        charged1Touch = null
        charged2Touch = null
        hudToggleTouch = null
        hudLockTouch = null
        attachMoveTouchZones()
        attachHudToggleTouch()
        attachHudLockTouch()
''')
# In OCR loop infer league from live cards before applying detection.
patch(path,
'''                            if (d != null && e != null) {
                                val pairedCards = d.player != null && d.opponent != null
''',
'''                            if (d != null && e != null) {
                                if (settingsRepo.leagueMode == BattleLeagueMode.AUTO) {
                                    inferLeagueCpFromCards(d)?.let { inferredCp ->
                                        if (inferredCp != currentLeagueCp) {
                                            settingsRepo.lastDetectedLeagueCp = inferredCp
                                            reloadLeague(inferredCp)
                                            e = engine
                                        }
                                    }
                                }
                                val pairedCards = d.player != null && d.opponent != null
''')
# Add inference helper before reloadLeague.
patch(path,
'''    private suspend fun reloadLeague(cp: Int) {
''',
'''    /** Fallback when lobby OCR misses the league: live card CPs identify the cap reliably in normal GBL. */
    private fun inferLeagueCpFromCards(d: BattleDetection): Int? {
        val cps = listOfNotNull(d.player?.cp, d.opponent?.cp).filter { it > 0 }
        if (cps.isEmpty()) return null
        val maxCp = cps.maxOrNull() ?: return null
        return when {
            maxCp > 2500 -> 10000
            maxCp > 1500 -> 2500
            cps.size >= 2 -> 1500
            else -> null
        }
    }

    private suspend fun reloadLeague(cp: Int) {
''')
# Redraw: eye remains, everything else truly hides.
patch(path,
'''        hudToggleTouch?.visibility = if (enabled && !settingsRepo.editMode) View.VISIBLE else View.GONE
        quickMenu?.setVisible(enabled)
        quickMenu?.refreshStatusBadge()
''',
'''        // Eye is the single restore control when HUD is collapsed. Lock/menu/league all disappear.
        hudToggleTouch?.visibility = if (enabled) View.VISIBLE else View.GONE
        hudLockTouch?.visibility = if (enabled && !hudCollapsed) View.VISIBLE else View.GONE
        quickMenu?.setVisible(enabled && !hudCollapsed)
        if (!hudCollapsed) quickMenu?.refreshStatusBadge()
''')
patch(path,
'''        val selectorsEnabled = enabled && !hudCollapsed && !settingsRepo.editMode
''',
'''        val selectorsEnabled = enabled && !hudCollapsed && settingsRepo.layoutLocked && !settingsRepo.editMode
''')
patch(path,
'''        runCatching { hudToggleTouch?.let { windowManager?.removeView(it) } }
        overlayView = null
''',
'''        runCatching { hudToggleTouch?.let { windowManager?.removeView(it) } }
        runCatching { hudLockTouch?.let { windowManager?.removeView(it) } }
        overlayView = null
''')
patch(path,
'''        hudToggleTouch = null
        detector?.close()
''',
'''        hudToggleTouch = null
        hudLockTouch = null
        detector?.close()
''')

# ---------- QuickOverlayMenu: lock-aware dragging + reference-style league badges ----------
path='app/src/main/java/com/lucianotoscano/pvppokego/overlay/QuickOverlayMenu.kt'
patch(path,
'''        fun isHudCollapsed(): Boolean
        fun onResetHudPositions()
''',
'''        fun isHudCollapsed(): Boolean
        fun isLayoutLocked(): Boolean
        fun onResetHudPositions()
''')
patch(path,
'''        installDrag(view, params, onMoved = { xFrac, yFrac ->
            settings.quickMenuXFraction = xFrac
            settings.quickMenuYFraction = yFrac
        }, onTap = { togglePanel() })
''',
'''        installDrag(view, params, canDrag = { !callbacks.isLayoutLocked() }, onMoved = { xFrac, yFrac ->
            settings.quickMenuXFraction = xFrac
            settings.quickMenuYFraction = yFrac
        }, onTap = { togglePanel() })
''')
patch(path,
'''        installDrag(view, params, onMoved = { xFrac, yFrac ->
            settings.leagueBadgeXFraction = xFrac
            settings.leagueBadgeYFraction = yFrac
        }, onTap = { /* display only; league changes from the menu */ })
''',
'''        installDrag(view, params, canDrag = { !callbacks.isLayoutLocked() }, onMoved = { xFrac, yFrac ->
            settings.leagueBadgeXFraction = xFrac
            settings.leagueBadgeYFraction = yFrac
        }, onTap = { /* display only; league changes from the menu */ })
''')
patch(path,
'''    private fun installDrag(
        view: View,
        params: WindowManager.LayoutParams,
        onMoved: (Float, Float) -> Unit,
        onTap: () -> Unit
    ) {
''',
'''    private fun installDrag(
        view: View,
        params: WindowManager.LayoutParams,
        canDrag: () -> Boolean,
        onMoved: (Float, Float) -> Unit,
        onTap: () -> Unit
    ) {
''')
patch(path,
'''                    if (abs(dx) + abs(dy) >= 6f) dragging = true
''',
'''                    if (canDrag() && abs(dx) + abs(dy) >= 6f) dragging = true
''')
# Slightly larger league badge for the recognizable triangular art.
patch(path,
'''        val size = dp(34).coerceAtLeast(36)
''',
'''        val size = dp(42).coerceAtLeast(44)
''')
# Replace LeagueBadgeView implementation.
old='''    private inner class LeagueBadgeView(ctx: Context) : View(ctx) {
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
new='''    /** Vector recreation of the league emblems supplied as the visual reference. */
    private inner class LeagueBadgeView(ctx: Context) : View(ctx) {
        private val p = Paint(Paint.ANTI_ALIAS_FLAG)
        private var kind: LeagueKind = LeagueKind.AUTO
        fun setKind(value: LeagueKind) { kind=value; invalidate() }
        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            val w=width.toFloat(); val h=height.toFloat(); val pad=2.5f*density
            val tri=Path().apply { moveTo(pad,pad); lineTo(w-pad,pad); lineTo(w/2f,h-pad); close() }
            canvas.save(); canvas.clipPath(tri)
            p.style=Paint.Style.FILL
            p.color=when(kind){
                LeagueKind.GREAT->Color.rgb(18,55,142)
                LeagueKind.ULTRA->Color.rgb(18,23,35)
                LeagueKind.MASTER->Color.rgb(105,55,151)
                LeagueKind.AUTO->Color.rgb(235,45,49)
            }
            canvas.drawRect(0f,0f,w,h,p)
            p.style=Paint.Style.STROKE; p.strokeCap=Paint.Cap.SQUARE
            when(kind){
                LeagueKind.GREAT->{
                    p.color=Color.rgb(235,53,58); p.strokeWidth=5.2f*density
                    canvas.drawLine(w*.30f,h*.50f,w*.90f,h*.18f,p)
                }
                LeagueKind.ULTRA->{
                    p.color=Color.rgb(250,207,26); p.strokeWidth=4.6f*density
                    canvas.drawLine(w*.22f,h*.48f,w*.92f,h*.12f,p)
                    canvas.drawLine(w*.32f,h*.66f,w*.92f,h*.34f,p)
                }
                LeagueKind.MASTER->{
                    p.color=Color.rgb(232,64,178); p.strokeWidth=4.1f*density
                    canvas.drawLine(w*.20f,h*.44f,w*.90f,h*.10f,p)
                    canvas.drawLine(w*.27f,h*.59f,w*.91f,h*.27f,p)
                    canvas.drawLine(w*.35f,h*.73f,w*.82f,h*.49f,p)
                }
                LeagueKind.AUTO->{
                    // Fourth supplied emblem: red field with pale sweeping lower section.
                    p.style=Paint.Style.FILL; p.color=Color.rgb(224,239,249)
                    val sweep=Path().apply { moveTo(w*.40f,h*.25f); cubicTo(w*.58f,h*.18f,w*.78f,h*.17f,w,h*.18f); lineTo(w,h); lineTo(w*.5f,h); close() }
                    canvas.drawPath(sweep,p)
                }
            }
            canvas.restore()
            // Gray outer and white inner triangle borders.
            p.style=Paint.Style.STROKE; p.strokeJoin=Paint.Join.ROUND
            p.strokeWidth=4.2f*density; p.color=Color.rgb(96,96,96); canvas.drawPath(tri,p)
            val innerPad=5.2f*density
            val inner=Path().apply { moveTo(innerPad,innerPad); lineTo(w-innerPad,innerPad); lineTo(w/2f,h-innerPad); close() }
            p.strokeWidth=1.8f*density; p.color=Color.WHITE; canvas.drawPath(inner,p)
            // White Pokeball mark in the upper-left, matching the supplied emblems.
            val cx=w*.31f; val cy=h*.25f; val r=w*.105f
            p.style=Paint.Style.FILL; p.color=Color.WHITE; canvas.drawCircle(cx,cy,r,p)
            p.style=Paint.Style.STROKE; p.strokeWidth=1.6f*density; p.color=when(kind){LeagueKind.GREAT->Color.rgb(18,55,142);LeagueKind.ULTRA->Color.rgb(18,23,35);LeagueKind.MASTER->Color.rgb(105,55,151);LeagueKind.AUTO->Color.rgb(210,45,49)}
            canvas.drawLine(cx-r,cy,cx+r,cy,p); canvas.drawCircle(cx,cy,r*.30f,p)
        }
    }
'''
patch(path,old,new)

# ---------- League OCR: accept current pt-BR wording and imperfect punctuation ----------
path='app/src/main/java/com/lucianotoscano/pvppokego/data/BattleLeague.kt'
patch(path,
'''        return when {
            containsAny(text,
                "liga grande", "great league", "pc max 1500", "max pc 1500",
                "cp 1500", "1500 cp", "limite 1500", "ate 1500"
            ) -> 1500
            containsAny(text,
                "liga ultra", "ultra league", "pc max 2500", "max pc 2500",
                "cp 2500", "2500 cp", "limite 2500", "ate 2500"
            ) -> 2500
            containsAny(text,
                "liga mestra", "liga mestre", "master league", "sem limite de pc",
                "sem limite de cp", "no cp limit"
            ) -> 10000
            else -> null
        }
''',
'''        return when {
            containsAny(text,
                "liga mestra", "liga mestre", "master league", "sem limite de pc",
                "sem limite de cp", "no cp limit", "pc maximo por pokemon nao ha",
                "pc maximo nao ha", "cp max no limit"
            ) || (text.contains("pc maximo") && text.contains("nao ha")) -> 10000
            containsAny(text,
                "liga ultra", "ultra league", "pc max 2500", "max pc 2500",
                "cp 2500", "2500 cp", "limite 2500", "ate 2500", "pc maximo por pokemon 2500"
            ) || (text.contains("2500") && (text.contains("pc max") || text.contains("cp max") || text.contains("limite"))) -> 2500
            containsAny(text,
                "liga grande", "great league", "pc max 1500", "max pc 1500",
                "cp 1500", "1500 cp", "limite 1500", "ate 1500", "pc maximo por pokemon 1500"
            ) || (text.contains("1500") && (text.contains("pc max") || text.contains("cp max") || text.contains("limite") || text.contains("copa"))) -> 1500
            else -> null
        }
''')

print('free-drag + global lock + eye-hide-all + league badge/detection patch applied')
