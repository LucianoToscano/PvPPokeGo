from pathlib import Path

path = Path('app/src/main/java/com/lucianotoscano/pvppokego/overlay/BattleOverlayService.kt')
text = path.read_text(encoding='utf-8')

replacements = [
    (
        '    private var moveMenuView: View? = null\n    private var hudCollapsed = false\n',
        '    private var moveMenuView: View? = null\n    private var quickMenu: QuickOverlayMenu? = null\n    private var hudCollapsed = false\n'
    ),
    (
        '        attachMoveTouchZones()\n        attachHudToggleTouch()\n        redraw()\n',
        '''        attachMoveTouchZones()\n        quickMenu = QuickOverlayMenu(\n            this,\n            windowManager!!,\n            settingsRepo,\n            object : QuickOverlayMenu.Callbacks {\n                override fun onSettingsChanged(rebuildTouchZones: Boolean) {\n                    if (rebuildTouchZones) rebuildTouchableZones()\n                    redraw()\n                }\n\n                override fun onToggleHud() {\n                    toggleHudCollapsed()\n                }\n\n                override fun isHudCollapsed(): Boolean = hudCollapsed\n\n                override fun onResetHudPositions() {\n                    settingsRepo.clearHudPositions()\n                    rebuildTouchableZones()\n                    redraw()\n                }\n            }\n        ).also { it.attach() }\n        redraw()\n'''
    ),
    (
        '        attachMoveTouchZones()\n        attachHudToggleTouch()\n    }\n',
        '        attachMoveTouchZones()\n    }\n'
    ),
    (
        '        hudToggleTouch?.visibility = if (enabled && !settingsRepo.editMode) View.VISIBLE else View.GONE\n        if (!selectorsEnabled) dismissMoveMenu()\n',
        '        hudToggleTouch?.visibility = View.GONE\n        quickMenu?.setVisible(enabled && !settingsRepo.editMode)\n        if (!selectorsEnabled) dismissMoveMenu()\n'
    ),
    (
        '        dismissMoveMenu()\n        removeEditTouchLayer()\n',
        '        dismissMoveMenu()\n        quickMenu?.destroy()\n        quickMenu = null\n        removeEditTouchLayer()\n'
    ),
]

for old, new in replacements:
    if old not in text:
        raise SystemExit(f'Expected snippet not found:\n{old[:180]}')
    text = text.replace(old, new, 1)

path.write_text(text, encoding='utf-8')
print('BattleOverlayService.kt patched with QuickOverlayMenu integration')
