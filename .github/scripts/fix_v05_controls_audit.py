from pathlib import Path


def replace_once(path: Path, old: str, new: str, label: str) -> None:
    text = path.read_text()
    if old not in text:
        raise SystemExit(f'{label}: pattern not found in {path}')
    path.write_text(text.replace(old, new, 1))


# 1) Version this as a distinct repaired build.
build = Path('app/build.gradle.kts')
replace_once(
    build,
    '        versionCode = 5\n        versionName = "0.5.0-copilot-hardening"\n',
    '        versionCode = 6\n        versionName = "0.5.1-overlay-controls-fix"\n',
    'version bump',
)

# 2) Avoid the MediaProjection-result race leaving the overlay permanently marked as
#    hidden behind the app UI. The battle-presence gate already prevents the HUD from
#    appearing over the settings screen before a real battle is detected.
main = Path('app/src/main/java/com/lucianotoscano/pvppokego/MainActivity.kt')
replace_once(
    main,
    '                putExtra(BattleOverlayService.EXTRA_APP_VISIBLE, true)\n',
    '                putExtra(BattleOverlayService.EXTRA_APP_VISIBLE, false)\n',
    'projection app-visible race',
)

# 3) A full HUD reset must really return to a safe/default state. Previously scale,
#    opacity and the global edit/lock state survived the reset.
settings = Path('app/src/main/java/com/lucianotoscano/pvppokego/data/SettingsRepository.kt')
replace_once(
    settings,
    '''    fun resetHudCustomization() {
        prefs.edit().also { editor ->
            HUD_BLOCKS.forEach { block ->
                listOf("x", "y", "scale", "opacity", "hidden", "locked").forEach { suffix ->
                    editor.remove("${block}_$suffix")
                }
            }
        }.apply()
    }
''',
    '''    fun resetHudCustomization() {
        prefs.edit().also { editor ->
            HUD_BLOCKS.forEach { block ->
                listOf("x", "y", "scale", "opacity", "hidden", "locked").forEach { suffix ->
                    editor.remove("${block}_$suffix")
                }
            }
            // Full reset must also leave the overlay in a safe, non-editing state.
            editor.putBoolean("edit_mode", false)
            editor.putBoolean("layout_locked", true)
            editor.putFloat("overlay_scale", 0.8f)
            editor.putFloat("overlay_opacity", 0.9f)
        }.apply()
    }
''',
    'full HUD reset',
)

# 4) Harden service startup and manual mode.
service = Path('app/src/main/java/com/lucianotoscano/pvppokego/overlay/BattleOverlayService.kt')
replace_once(
    service,
    '''        settingsRepo = SettingsRepository(this)
        settingsRepo.ensureTopHudSafeV4()
        settingsRepo.overlayServiceRunning = true
''',
    '''        settingsRepo = SettingsRepository(this)
        settingsRepo.ensureTopHudSafeV4()
        // Never revive a stale edit/free-drag session after a service restart. A stale
        // fullscreen drag layer can otherwise consume battle input unexpectedly.
        settingsRepo.editMode = false
        settingsRepo.layoutLocked = true
        settingsRepo.overlayServiceRunning = true
''',
    'safe service startup state',
)
replace_once(
    service,
    '        val enabled = settingsRepo.overlayEnabled && !appUiVisible && battleActive\n',
    '''        // With automatic recognition disabled, keep the HUD available as a manual
        // overlay instead of making every control disappear because battleActive can no
        // longer receive visual/OCR evidence.
        val enabled = settingsRepo.overlayEnabled && !appUiVisible && (battleActive || !settingsRepo.autoRecognition)
''',
    'manual HUD visibility',
)
text = service.read_text()
text = text.replace('notification("V04 • ${leagueLabel(currentLeagueCp)} • HUD PvP ativo")', 'notification("V05 FIX • ${leagueLabel(currentLeagueCp)} • HUD PvP ativo")')
text = text.replace('notification("V04 • ${leagueLabel(cp)} • HUD PvP ativo")', 'notification("V05 FIX • ${leagueLabel(cp)} • HUD PvP ativo")')
text = text.replace('/** Full-screen touch layer exists ONLY in explicit edit mode. Battle input is blocked while editing. */', '/** Full-screen touch layer is temporary while editing/free-drag is active. Lock again before battling. */')
service.write_text(text)

# 5) Per-block lock/hidden state must be respected by every hit-test, not only by the
#    blocks that happen to use the near() helper.
view = Path('app/src/main/java/com/lucianotoscano/pvppokego/overlay/BattleOverlayView.kt')
replace_once(
    view,
    '''        val strong = state.strongTypesAgainstOpponent.take(4)
        if (strong.isNotEmpty()) {
''',
    '''        val strong = state.strongTypesAgainstOpponent.take(4)
        if (strong.isNotEmpty() && !settings.isBlockHidden(BLOCK_STRONG_TYPES) &&
            !(settings.isBlockLocked(BLOCK_STRONG_TYPES) && settings.layoutLocked)) {
''',
    'strong-types hit-test lock',
)
replace_once(
    view,
    '''        val (mx, my) = pos(BLOCK_ENEMY_MOVES, ENEMY_MOVE_CENTER_X, ENEMY_MOVE_FAST_Y)
        if (x in (mx-150f)..(mx+150f) && y in (my-55f)..(my+205f)) return BLOCK_ENEMY_MOVES

        val (r1x, r1y) = reservePos(0)
        if (x in (r1x-65f)..(r1x+175f) && y in (r1y-55f)..(r1y+125f)) return BLOCK_RESERVE_1
        val (r2x, r2y) = reservePos(1)
        if (x in (r2x-65f)..(r2x+175f) && y in (r2y-55f)..(r2y+125f)) return BLOCK_RESERVE_2
''',
    '''        val (mx, my) = pos(BLOCK_ENEMY_MOVES, ENEMY_MOVE_CENTER_X, ENEMY_MOVE_FAST_Y)
        if (!settings.isBlockHidden(BLOCK_ENEMY_MOVES) &&
            !(settings.isBlockLocked(BLOCK_ENEMY_MOVES) && settings.layoutLocked) &&
            x in (mx-150f)..(mx+150f) && y in (my-55f)..(my+205f)) return BLOCK_ENEMY_MOVES

        val (r1x, r1y) = reservePos(0)
        if (!settings.isBlockHidden(BLOCK_RESERVE_1) &&
            !(settings.isBlockLocked(BLOCK_RESERVE_1) && settings.layoutLocked) &&
            x in (r1x-65f)..(r1x+175f) && y in (r1y-55f)..(r1y+125f)) return BLOCK_RESERVE_1
        val (r2x, r2y) = reservePos(1)
        if (!settings.isBlockHidden(BLOCK_RESERVE_2) &&
            !(settings.isBlockLocked(BLOCK_RESERVE_2) && settings.layoutLocked) &&
            x in (r2x-65f)..(r2x+175f) && y in (r2y-55f)..(r2y+125f)) return BLOCK_RESERVE_2
''',
    'moves/reserves hit-test locks',
)

print('V05 audited overlay-control fixes applied')
