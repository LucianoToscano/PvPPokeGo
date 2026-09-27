from pathlib import Path
p=Path('app/src/main/java/com/lucianotoscano/pvppokego/overlay/BattleOverlayService.kt')
s=p.read_text()
old='''    private fun bringHudControlsToFront() {
        fun readd(view: View?, params: WindowManager.LayoutParams) {
            if (view == null) return
            runCatching { windowManager?.removeView(view) }
            runCatching { windowManager?.addView(view, params) }
        }
        readd(hudToggleTouch, hudControlParams(hudToggleAnchor()))
        if (!hudCollapsed) readd(hudLockTouch, hudControlParams(hudLockAnchor()))
    }
'''
new='''    private fun bringHudControlsToFront() {
        fun readd(view: View?) {
            if (view == null) return
            val params = view.layoutParams as? WindowManager.LayoutParams ?: return
            // Reuse the exact LayoutParams captured by the drag listener; creating a new object
            // here would make the visible control move independently from its touch coordinates.
            runCatching { windowManager?.removeView(view) }
            runCatching { windowManager?.addView(view, params) }
        }
        readd(hudToggleTouch)
        if (!hudCollapsed) readd(hudLockTouch)
    }
'''
if old not in s: raise SystemExit('bringHudControlsToFront pattern not found')
p.write_text(s.replace(old,new,1))
