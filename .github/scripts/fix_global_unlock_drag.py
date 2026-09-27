from pathlib import Path

# Apply the free-drag repair after the workflow is present on the branch.
service = Path('app/src/main/java/com/lucianotoscano/pvppokego/overlay/BattleOverlayService.kt')
text = service.read_text()
old = '''            installHudControlDrag(this, params, BattleOverlayView.BLOCK_HUD_LOCK) {
                settingsRepo.layoutLocked = !settingsRepo.layoutLocked
                if (settingsRepo.layoutLocked) overlayView?.setEditTarget(null)
                redraw()
            }
'''
new = '''            installHudControlDrag(this, params, BattleOverlayView.BLOCK_HUD_LOCK) {
                settingsRepo.layoutLocked = !settingsRepo.layoutLocked
                // Global padlock is the single source of truth for free dragging.
                // Rebuild the drag layer on the next UI turn so unlocking immediately
                // makes every HUD block draggable without opening the legacy edit mode.
                if (settingsRepo.layoutLocked) {
                    overlayView?.setEditTarget(null)
                    removeEditTouchLayer()
                } else {
                    dismissMoveMenu()
                }
                hudLockTouch?.post { redraw() }
            }
'''
if old not in text:
    raise SystemExit('lock toggle block not found')
service.write_text(text.replace(old, new, 1))

view = Path('app/src/main/java/com/lucianotoscano/pvppokego/overlay/BattleOverlayView.kt')
text = view.read_text()
old = '''        fun near(block: String, dx: Float, dy: Float, halfW: Float, halfH: Float): Boolean {
            if (settings.isBlockLocked(block) || settings.isBlockHidden(block)) return false
'''
new = '''        fun near(block: String, dx: Float, dy: Float, halfW: Float, halfH: Float): Boolean {
            // When the global padlock is open, ALL HUD blocks must be movable.
            // Per-block locks only apply inside the legacy fine-edit mode while the
            // global layout itself is locked.
            if ((settings.isBlockLocked(block) && settings.layoutLocked) || settings.isBlockHidden(block)) return false
'''
if old not in text:
    raise SystemExit('near block not found')
text = text.replace(old, new, 1)
old = '''        if (settings.isBlockLocked(block)) return
        settings.setPosition(block, refX(screenX), refY(screenY))
'''
new = '''        if (settings.isBlockLocked(block) && settings.layoutLocked) return
        settings.setPosition(block, refX(screenX), refY(screenY))
'''
if old not in text:
    raise SystemExit('move block lock check not found')
view.write_text(text.replace(old, new, 1))
