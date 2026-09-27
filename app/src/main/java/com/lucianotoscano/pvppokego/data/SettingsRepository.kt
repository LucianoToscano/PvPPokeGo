package com.lucianotoscano.pvppokego.data

import android.content.Context

class SettingsRepository(context: Context) {
    private val prefs = context.getSharedPreferences(FILE, Context.MODE_PRIVATE)

    var overlayEnabled: Boolean
        get() = prefs.getBoolean("overlay_enabled", true)
        set(v) = prefs.edit().putBoolean("overlay_enabled", v).apply()

    var autoRecognition: Boolean
        get() = prefs.getBoolean("auto_recognition", true)
        set(v) = prefs.edit().putBoolean("auto_recognition", v).apply()

    var battleAssistEnabled: Boolean
        get() = prefs.getBoolean("battle_assist_enabled", true)
        set(v) = prefs.edit().putBoolean("battle_assist_enabled", v).apply()

    var leagueMode: BattleLeagueMode
        get() = runCatching {
            BattleLeagueMode.valueOf(prefs.getString("league_mode", BattleLeagueMode.AUTO.name)!!)
        }.getOrDefault(BattleLeagueMode.AUTO)
        set(v) = prefs.edit().putString("league_mode", v.name).apply()

    var lastDetectedLeagueCp: Int
        get() = prefs.getInt("last_detected_league_cp", 1500)
        set(v) = prefs.edit().putInt("last_detected_league_cp", v).apply()

    fun configuredLeagueCp(): Int? = leagueMode.cpCap

    var showStrongTypes: Boolean
        get() = prefs.getBoolean("show_strong_types", true)
        set(v) = prefs.edit().putBoolean("show_strong_types", v).apply()

    var showCurrentIndicator: Boolean
        get() = prefs.getBoolean("show_current_indicator", true)
        set(v) = prefs.edit().putBoolean("show_current_indicator", v).apply()

    var analyzeReserves: Boolean
        get() = prefs.getBoolean("analyze_reserves", true)
        set(v) = prefs.edit().putBoolean("analyze_reserves", v).apply()

    var showReserveTypes: Boolean
        get() = prefs.getBoolean("show_reserve_types", true)
        set(v) = prefs.edit().putBoolean("show_reserve_types", v).apply()

    var showEnemyMoves: Boolean
        get() = prefs.getBoolean("show_enemy_moves", true)
        set(v) = prefs.edit().putBoolean("show_enemy_moves", v).apply()

    var showChargedCounter: Boolean
        get() = prefs.getBoolean("show_charged_counter", true)
        set(v) = prefs.edit().putBoolean("show_charged_counter", v).apply()

    var showEnergyProgress: Boolean
        get() = prefs.getBoolean("show_energy_progress", true)
        set(v) = prefs.edit().putBoolean("show_energy_progress", v).apply()

    var showHudText: Boolean
        get() = prefs.getBoolean("show_hud_text", true)
        set(v) = prefs.edit().putBoolean("show_hud_text", v).apply()

    var showEnemyHistory: Boolean
        get() = prefs.getBoolean("show_enemy_history", false)
        set(v) = prefs.edit().putBoolean("show_enemy_history", v).apply()

    var quickMenuAdvanced: Boolean
        get() = prefs.getBoolean("quick_menu_advanced", false)
        set(v) = prefs.edit().putBoolean("quick_menu_advanced", v).apply()

    /** Menu can switch between PGSharp-like icon+text rows and compact icons only. */
    var quickMenuShowLabels: Boolean
        get() = prefs.getBoolean("quick_menu_show_labels", true)
        set(v) = prefs.edit().putBoolean("quick_menu_show_labels", v).apply()

    var quickMenuIconsOnly: Boolean
        get() = !quickMenuShowLabels
        set(v) { quickMenuShowLabels = !v }

    var quickMenuXFraction: Float
        get() = prefs.getFloat("quick_menu_x_frac", 1f)
        set(v) = prefs.edit().putFloat("quick_menu_x_frac", v.coerceIn(0f, 1f)).apply()

    var quickMenuYFraction: Float
        get() = prefs.getFloat("quick_menu_y_frac", 1f)
        set(v) = prefs.edit().putFloat("quick_menu_y_frac", v.coerceIn(0f, 1f)).apply()

    /** Independent league badge position; it never follows the gear. */
    var leagueBadgeXFraction: Float
        get() = prefs.getFloat("league_badge_x_frac", .82f)
        set(v) = prefs.edit().putFloat("league_badge_x_frac", v.coerceIn(0f, 1f)).apply()

    var leagueBadgeYFraction: Float
        get() = prefs.getFloat("league_badge_y_frac", .91f)
        set(v) = prefs.edit().putFloat("league_badge_y_frac", v.coerceIn(0f, 1f)).apply()

    fun ensureQuickMenuBottomRightV2() {
        if (prefs.getInt("quick_menu_position_version", 0) >= 2) return
        prefs.edit()
            .putFloat("quick_menu_x_frac", 1f)
            .putFloat("quick_menu_y_frac", 1f)
            .putInt("quick_menu_position_version", 2)
            .apply()
    }

    /** Default is icon+text like the PGSharp reference; users can collapse to icons only. */
    fun ensureQuickMenuStyleV3() {
        if (prefs.getInt("quick_menu_style_version", 0) >= 4) return
        prefs.edit()
            .putBoolean("quick_menu_show_labels", true)
            .putBoolean("quick_menu_advanced", false)
            .putInt("quick_menu_style_version", 4)
            .apply()
    }

    /**
     * V6 stops forcing a fixed top Y. The detector now anchors against GO's actual white cards.
     * We only clear legacy auto-generated high positions once; manual edits made after V6 persist.
     */
    fun ensureTopHudSafeV4() {
        if (prefs.getInt("top_hud_position_version", 0) >= 6) return
        val editor = prefs.edit()
        listOf("hud_player_matchup", "hud_strong_types").forEach { block ->
            editor.remove("${block}_x")
            editor.remove("${block}_y")
        }
        editor.putInt("top_hud_position_version", 6).apply()
    }

    var showSwitchTimer: Boolean
        get() = prefs.getBoolean("show_switch_timer", false)
        set(v) = prefs.edit().putBoolean("show_switch_timer", v).apply()

    var debugMode: Boolean
        get() = prefs.getBoolean("debug_mode", false)
        set(v) = prefs.edit().putBoolean("debug_mode", v).apply()

    var editMode: Boolean
        get() = prefs.getBoolean("edit_mode", false)
        set(v) = prefs.edit().putBoolean("edit_mode", v).apply()

    /** Global HUD position lock. Unlocking enables direct drag without opening edit mode. */
    var layoutLocked: Boolean
        get() = prefs.getBoolean("layout_locked", true)
        set(v) = prefs.edit().putBoolean("layout_locked", v).apply()

    var scale: Float
        get() = prefs.getFloat("overlay_scale", 0.8f)
        set(v) = prefs.edit().putFloat("overlay_scale", v.coerceIn(.5f, 1.2f)).apply()

    var opacity: Float
        get() = prefs.getFloat("overlay_opacity", .9f)
        set(v) = prefs.edit().putFloat("overlay_opacity", v.coerceIn(.3f, 1f)).apply()

    var overlayServiceRunning: Boolean
        get() = prefs.getBoolean("overlay_service_running", false)
        set(v) = prefs.edit().putBoolean("overlay_service_running", v).apply()

    fun setPosition(block: String, x: Float, y: Float) {
        prefs.edit().putFloat("${block}_x", x).putFloat("${block}_y", y).apply()
    }

    fun getPosition(block: String, defaultX: Float, defaultY: Float): Pair<Float, Float> =
        prefs.getFloat("${block}_x", defaultX) to prefs.getFloat("${block}_y", defaultY)

    fun hasPosition(block: String): Boolean = prefs.contains("${block}_x") && prefs.contains("${block}_y")

    fun blockScale(block: String): Float = prefs.getFloat("${block}_scale", 1f).coerceIn(.55f, 1.6f)
    fun setBlockScale(block: String, value: Float) = prefs.edit().putFloat("${block}_scale", value.coerceIn(.55f, 1.6f)).apply()

    fun blockOpacity(block: String): Float = prefs.getFloat("${block}_opacity", 1f).coerceIn(.25f, 1f)
    fun setBlockOpacity(block: String, value: Float) = prefs.edit().putFloat("${block}_opacity", value.coerceIn(.25f, 1f)).apply()

    fun isBlockHidden(block: String): Boolean = prefs.getBoolean("${block}_hidden", false)
    fun setBlockHidden(block: String, hidden: Boolean) = prefs.edit().putBoolean("${block}_hidden", hidden).apply()

    fun isBlockLocked(block: String): Boolean = prefs.getBoolean("${block}_locked", false)
    fun setBlockLocked(block: String, locked: Boolean) = prefs.edit().putBoolean("${block}_locked", locked).apply()

    fun clearHudPositions() {
        val blocks = HUD_BLOCKS
        prefs.edit().also { editor ->
            blocks.forEach { block ->
                editor.remove("${block}_x")
                editor.remove("${block}_y")
            }
        }.apply()
    }

    fun resetHudCustomization() {
        prefs.edit().also { editor ->
            HUD_BLOCKS.forEach { block ->
                listOf("x", "y", "scale", "opacity", "hidden", "locked").forEach { suffix ->
                    editor.remove("${block}_$suffix")
                }
            }
        }.apply()
    }

    fun resetQuickMenuPosition() {
        prefs.edit()
            .putFloat("quick_menu_x_frac", 1f)
            .putFloat("quick_menu_y_frac", 1f)
            .putFloat("league_badge_x_frac", .82f)
            .putFloat("league_badge_y_frac", .91f)
            .putInt("quick_menu_position_version", 2)
            .apply()
    }

    companion object {
        private const val FILE = "pvppokego_prefs"
        val HUD_BLOCKS = listOf(
            "hud_player_matchup", "hud_strong_types", "hud_enemy_moves",
            "hud_reserve_1", "hud_reserve_2", "hud_reserves",
            "hud_toggle", "hud_lock", "hud_battle_assist", "hud_enemy_history"
        )
    }
}
