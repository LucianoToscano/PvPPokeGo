from pathlib import Path
import re

p = Path("app/src/main/java/com/lucianotoscano/pvppokego/overlay/BattleOverlayService.kt")
s = p.read_text()

eye = r'''    private fun attachHudToggleTouch() {
        val wm = windowManager ?: return
        if (hudToggleTouch != null) return
        val params = hudControlParams(hudToggleAnchor())
        hudToggleTouch = TextView(this).apply {
            // Transparent touch target only. The eye itself is drawn by BattleOverlayView Canvas.
            text = ""
            setTextColor(Color.TRANSPARENT)
            setBackgroundColor(Color.TRANSPARENT)
            contentDescription = "Ocultar ou mostrar HUD"
            isClickable = true
            isFocusable = false
            installHudControlDrag(this, params, BattleOverlayView.BLOCK_HUD_TOGGLE) {
                toggleHudCollapsed()
            }
        }.also { wm.addView(it, params) }
    }

'''
s, n = re.subn(r"(?s)    private fun attachHudToggleTouch() {.*?
    }

    private fun attachHudLockTouch", eye + "    private fun attachHudLockTouch", s, count=1)
if n != 1: raise SystemExit("eye replacement failed")

lock = r'''    private fun attachHudLockTouch() {
        val wm = windowManager ?: return
        if (hudLockTouch != null) return
        val params = hudControlParams(hudLockAnchor())
        hudLockTouch = TextView(this).apply {
            // Transparent touch target only. The lock itself is drawn by BattleOverlayView Canvas.
            text = ""
            setTextColor(Color.TRANSPARENT)
            setBackgroundColor(Color.TRANSPARENT)
            contentDescription = "Travar ou destravar HUD"
            isClickable = true
            isFocusable = false
            installHudControlDrag(this, params, BattleOverlayView.BLOCK_HUD_LOCK) {
                settingsRepo.layoutLocked = !settingsRepo.layoutLocked
                if (settingsRepo.layoutLocked) {
                    overlayView?.setEditTarget(null)
                    removeEditTouchLayer()
                } else {
                    dismissMoveMenu()
                }
                redraw()
            }
        }.also { wm.addView(it, params) }
    }

'''
s, n = re.subn(r"(?s)    private fun attachHudLockTouch() {.*?
    }

    private fun hudControlParams", lock + "    private fun hudControlParams", s, count=1)
if n != 1: raise SystemExit("lock replacement failed")

s = s.replace("val size = (52f * unit).toInt().coerceAtLeast(44)",
              "val size = (56f * unit).toInt().coerceAtLeast(52)")

touch = r'''    private fun installHudControlDrag(view: View, params: WindowManager.LayoutParams, block: String, onTap: () -> Unit) {
        val touchSlop = android.view.ViewConfiguration.get(this)
            .scaledTouchSlop.toFloat().coerceAtLeast(16f)
        var downX = 0f
        var downY = 0f
        var startX = 0
        var startY = 0
        var dragging = false
        view.isClickable = true
        view.isFocusable = false
        view.setOnTouchListener { v, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    downX = event.rawX
                    downY = event.rawY
                    startX = params.x
                    startY = params.y
                    dragging = false
                    v.parent?.requestDisallowInterceptTouchEvent(true)
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = event.rawX - downX
                    val dy = event.rawY - downY
                    if (!settingsRepo.layoutLocked &&
                        (abs(dx) >= touchSlop || abs(dy) >= touchSlop)) {
                        dragging = true
                    }
                    if (dragging) {
                        val maxX = (resources.displayMetrics.widthPixels - params.width).coerceAtLeast(0)
                        val maxY = (resources.displayMetrics.heightPixels - params.height).coerceAtLeast(0)
                        params.x = (startX + dx).toInt().coerceIn(0, maxX)
                        params.y = (startY + dy).toInt().coerceIn(-params.height / 2, maxY)
                        runCatching { windowManager?.updateViewLayout(view, params) }
                    }
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (dragging) {
                        val sx = resources.displayMetrics.widthPixels / 864f
                        val sy = resources.displayMetrics.heightPixels / 1536f
                        settingsRepo.setPosition(
                            block,
                            (params.x + params.width / 2f) / sx,
                            (params.y + params.height / 2f) / sy
                        )
                        redraw()
                    } else {
                        onTap()
                    }
                    true
                }
                MotionEvent.ACTION_CANCEL -> {
                    dragging = false
                    true
                }
                else -> true
            }
        }
    }

'''
s, n = re.subn(r"(?s)    private fun installHudControlDrag(view: View, params: WindowManager.LayoutParams, block: String, onTap: () -> Unit) {.*?
    }

    private fun toggleHudCollapsed", touch + "    private fun toggleHudCollapsed", s, count=1)
if n != 1: raise SystemExit("touch replacement failed")

s = re.sub(r'
s*(hudToggleTouch as? TextView)?.texts*=.*', '', s)
s = re.sub(r'
s*(hudLockTouch as? TextView)?.texts*=.*', '', s)
p.write_text(s)

g = Path("app/build.gradle.kts")
gs = g.read_text()
gs = re.sub(r"versionCode\s*=\s*\d+", "versionCode = 13", gs, count=1)
gs = re.sub(r'versionName\s*=\s*"[^"]+"', 'versionName = "0.5.8-canvas-lock-eye"', gs, count=1)
g.write_text(gs)
