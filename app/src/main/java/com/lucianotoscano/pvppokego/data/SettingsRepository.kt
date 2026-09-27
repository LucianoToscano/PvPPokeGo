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
        get() = prefs.getFloat("league_badge_x_frac", .86f)
        set(v) = prefs.edit().putFloat("league_badge_x_frac", v.coerceIn(0f, 1f)).apply()

    var leagueBadgeYFraction: Float
        get() = prefs.getFloat("league_badge_y_frac", .84f)
        set(v) = prefs.edit().putFloat("league_badge_y_frac", v.coerceIn(0f, 1f)).apply()

    fun ensureQuickMenuBottomRightV2() {
        if (prefs.getInt("quick_menu_position_version", 0) >= 2) return
        prefs.edit()
            .putFloat("quick_menu_x_frac", 1f)
            .putFloat("quick_menu_y_frac", 1f)
            .putInt("quick_menu_position_version", 2)
            .apply()
    }

    /** V4 restores icon+text as the default menu style; users can still switch to icons only. */
    fun ensureQuickMenuStyleV3() {
        if (prefs.getInt("quick_menu_style_version", 0) >= 4) return
        prefs.edit()
            .putBoolean("quick_menu_show_labels", true)
            .putInt("quick_menu_style_version", 4)
            .apply()
    }

    /**
     * Android keeps TYPE_APPLICATION_OVERLAY below the system status bar. Therefore the correct fix
     * is to keep our top chips in Pokemon GO's free strip below the status bar, not try to cover it.
     * Only legacy positions that are high enough to be clipped are migrated.
     */
    fun ensureTopHudSafeV4() {
        if (prefs.getInt("top_hud_position_version", 0) >= 5) return
        val editor = prefs.edit()
        listOf("hud_player_matchup", "hud_strong_types", "hud_toggle").forEach { block ->
            val key = "${block}_y"
            val current = prefs.getFloat(key, Float.NaN)
            if (current.isNaN() || current < 54f) editor.putFloat(key, 66f)
        }
        editor.putInt("top_hud_position_version", 5).apply()
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

    fun clearHudPositions() {
        val blocks = listOf(
            "hud_player_matchup", "hud_strong_types", "hud_enemy_moves",
            "hud_reserve_1", "hud_reserve_2", "hud_reserves",
            "hud_toggle", "hud_battle_assist"
        )
        prefs.edit().also { editor ->
            blocks.forEach { block ->
                editor.remove("${block}_x")
                editor.remove("${block}_y")
            }
        }.apply()
    }

    fun resetQuickMenuPosition() {
        prefs.edit()
            .putFloat("quick_menu_x_frac", 1f)
            .putFloat("quick_menu_y_frac", 1f)
            .putFloat("league_badge_x_frac", .86f)
            .putFloat("league_badge_y_frac", .84f)
            .putInt("quick_menu_position_version", 2)
            .apply()
    }

    companion object {
        private const val FILE = "pvppokego_prefs"
    }
}
