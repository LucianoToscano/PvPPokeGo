from pathlib import Path
import re

p = Path("app/src/main/java/com/lucianotoscano/pvppokego/overlay/BattleOverlayService.kt")
s = p.read_text()

def replace_between(src, start_marker, end_marker, replacement):
    a = src.find(start_marker)
    if a < 0:
        raise SystemExit("missing start marker: " + start_marker)
    b = src.find(end_marker, a)
    if b < 0:
        raise SystemExit("missing end marker: " + end_marker)
    return src[:a] + replacement + src[b:]

eye_start = "    private fun attachHudToggleTouch() {"
eye_end = "    private fun attachHudLockTouch() {"
eye = """    private fun attachHudToggleTouch() {
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

"""
s = replace_between(s, eye_start, eye_end, eye + eye_end)

lock_start = "    private fun attachHudLockTouch() {"
lock_end = "    private fun hudControlParams"
lock = """    private fun attachHudLockTouch() {
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

"""
s = replace_between(s, lock_start, lock_end, lock + lock_end)

s = s.replace("val size = (52f * unit).toInt().coerceAtLeast(44)",
              "val size = (56f * unit).toInt().coerceAtLeast(52)")

touch_start = "    private fun installHudControlDrag("
touch_end = "    private fun toggleHudCollapsed()"
touch = """    private fun installHudControlDrag(view: View, params: WindowManager.LayoutParams, block: String, onTap: () -> Unit) {
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

"""
s = replace_between(s, touch_start, touch_end, touch + touch_end)

# Remove any legacy emoji assignment from redraw; icons remain Canvas-only.
s = re.sub(r'(?m)^\s*\(hudToggleTouch as\? TextView\)\?\.text\s*=.*$', '', s)
s = re.sub(r'(?m)^\s*\(hudLockTouch as\? TextView\)\?\.text\s*=.*$', '', s)

p.write_text(s)

g = Path("app/build.gradle.kts")
gs = g.read_text()
gs = re.sub(r"versionCode\s*=\s*\d+", "versionCode = 13", gs, count=1)
gs = re.sub(r'versionName\s*=\s*"[^"]+"', 'versionName = "0.5.8-canvas-lock-eye"', gs, count=1)
g.write_text(gs)
