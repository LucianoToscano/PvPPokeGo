from pathlib import Path
p = Path('app/src/main/java/com/lucianotoscano/pvppokego/overlay/BattleOverlayService.kt')
s = p.read_text()
old = '        if (hudCollapsed || appUiVisible || !battleActive || settingsRepo.editMode) return\n'
new = '''        // In manual HUD mode (automatic recognition disabled), battleActive has no
        // visual/OCR evidence by design; move selection must still remain usable.
        if (hudCollapsed || appUiVisible || (settingsRepo.autoRecognition && !battleActive) || settingsRepo.editMode) return
'''
if old not in s:
    raise SystemExit('manual move-menu guard pattern not found')
p.write_text(s.replace(old, new, 1))
print('manual move-menu guard fixed')
