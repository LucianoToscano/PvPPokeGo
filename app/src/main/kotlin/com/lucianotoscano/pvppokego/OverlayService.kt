package com.lucianotoscano.pvppokego

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.NotificationCompat
import pvppokego.core.battle.BattleObservationProcessor
import pvppokego.core.battle.BattleSession
import pvppokego.core.battle.TypeMatchupAdvisor
import pvppokego.core.model.ChargedMoveProgress
import pvppokego.core.model.MoveData
import pvppokego.core.model.PokemonBattleStatus

/**
 * External PvP overlay. It never injects into or modifies Pokemon GO.
 *
 * V03 restores the compact/classic PvPPokeGo HUD while keeping the V02 energy
 * fill, OCR charged-move learning, PvPoke rank defaults and move submenus.
 */
class OverlayService : Service(), ScreenCaptureAnalyzer.Callback {
    private lateinit var windowManager: WindowManager
    private lateinit var data: LocalBattleData
    private lateinit var leaguePreference: LeaguePreference
    private val mainHandler = Handler(Looper.getMainLooper())

    private var analyzer: ScreenCaptureAnalyzer? = null
    private var currentLeague: BattleLeague? = null
    private var session: BattleSession? = null
    private var observationProcessor: BattleObservationProcessor? = null
    private var pendingEnemySpeciesId: String? = null

    private lateinit var weaknessBar: LinearLayout
    private lateinit var myMatchupChip: TextView
    private lateinit var reserveAdvisorBar: LinearLayout
    private lateinit var leagueChip: TextView
    private lateinit var movePanel: LinearLayout
    private lateinit var enemyLabel: TextView
    private lateinit var fastChip: FastMoveChip
    private lateinit var chargedLeft: ChargedMoveChip
    private lateinit var chargedRight: ChargedMoveChip
    private lateinit var correctionRow: LinearLayout
    private lateinit var correctionLabel: TextView

    private var motionArmed = true
    private var lastFastMoveAt = 0L

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WindowManager::class.java)
        data = LocalBattleData(this)
        leaguePreference = LeaguePreference(this)
        currentLeague = leaguePreference.configuredLeague()
        createNotificationChannel()
        createOverlayViews()
        currentLeague?.let(::createBattleSession)
        render()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopSelf()
            return START_NOT_STICKY
        }
        if (intent?.action != ACTION_START) return START_NOT_STICKY

        startForeground(
            NOTIFICATION_ID,
            NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_menu_view)
                .setContentTitle("PvPPokeGo ativo")
                .setContentText("OCR, contador de energia e overlay PvP")
                .setOngoing(true)
                .build()
        )

        val resultCode = intent.getIntExtra(EXTRA_RESULT_CODE, 0)
        @Suppress("DEPRECATION")
        val resultData = intent.getParcelableExtra<Intent>(EXTRA_RESULT_DATA)
        if (resultCode == 0 || resultData == null) {
            Toast.makeText(this, "Permissão de captura inválida.", Toast.LENGTH_LONG).show()
            stopSelf()
            return START_NOT_STICKY
        }

        analyzer?.stop()
        val projection = getSystemService(MediaProjectionManager::class.java)
            .getMediaProjection(resultCode, resultData)
        analyzer = ScreenCaptureAnalyzer(this, projection, this).also { it.start() }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        analyzer?.stop()
        analyzer = null
        removeOverlay(weaknessBar)
        removeOverlay(myMatchupChip)
        removeOverlay(reserveAdvisorBar)
        removeOverlay(leagueChip)
        removeOverlay(movePanel)
        stopForeground(STOP_FOREGROUND_REMOVE)
        super.onDestroy()
    }

    override fun onOcr(
        fullText: String,
        myHeaderText: String,
        enemyHeaderText: String,
        chargedBannerLines: List<String>,
        timestampMs: Long
    ) {
        mainHandler.post {
            val lobbyLeague = LobbyLeagueDetector.detect(fullText)
            if (leaguePreference.mode == LeagueSelectionMode.AUTO && lobbyLeague != null) {
                leaguePreference.lastDetectedLeague = lobbyLeague
                if (currentLeague != lobbyLeague || session == null) {
                    selectLeague(lobbyLeague, persistManualChoice = false)
                }
            }

            if (lobbyLeague != null) registerLobbyTeam(fullText)

            data.resolveSpeciesFromText(myHeaderText)?.let(::activateMyPokemon)
            data.resolveSpeciesFromText(enemyHeaderText)?.let(::activateEnemy)

            // Any legal charged move observed in "X usou Y"/"X used Y" updates
            // the corresponding enemy slot and learns the real moveset unless
            // that button was manually pinned by the user.
            chargedBannerLines.forEach { banner ->
                val result = observationProcessor?.onEnemyChargedMoveBanner(banner, timestampMs)
                if (result?.applied == true) {
                    motionArmed = false
                    lastFastMoveAt = timestampMs
                    render()
                }
            }
        }
    }

    override fun onMotion(score: Double, timestampMs: Long) {
        mainHandler.post {
            if (score <= MOTION_REARM_THRESHOLD) {
                motionArmed = true
                return@post
            }
            if (!motionArmed || score < MOTION_TRIGGER_THRESHOLD) return@post

            val battle = session ?: return@post
            val slot = battle.enemyTracker.activeEnemySlot() ?: return@post
            val state = battle.enemyTracker.state(slot)
            val fast = state.fastMoveId?.let(data.repository::move) ?: return@post
            val cooldown = when {
                fast.cooldownMs > 0 -> fast.cooldownMs.toLong()
                fast.turns > 0 -> fast.turns * 500L
                else -> 1000L
            }
            val refractory = (cooldown * 0.68).toLong().coerceAtLeast(320L)
            if (timestampMs - lastFastMoveAt < refractory) return@post

            runCatching { battle.enemyTracker.onFastMove(slot) }
                .onSuccess {
                    lastFastMoveAt = timestampMs
                    motionArmed = false
                    render()
                }
        }
    }

    private fun registerLobbyTeam(fullText: String) {
        val battle = session ?: return
        data.resolveAllSpeciesFromText(fullText)
            .take(3)
            .forEachIndexed { slot, speciesId ->
                runCatching { battle.registerMyPokemon(slot, speciesId) }
            }
    }

    private fun activateMyPokemon(speciesId: String) {
        val battle = session ?: return
        val snapshot = battle.snapshot()
        val known = snapshot.myTeam.firstOrNull { it.speciesId == speciesId }
        val slot = known?.slot ?: snapshot.myTeam.firstOrNull { it.speciesId == null }?.slot ?: return

        if (known == null) {
            runCatching { battle.registerMyPokemon(slot, speciesId) }.getOrElse { return }
        }
        runCatching { battle.setMyActivePokemon(slot) }
        render()
    }

    private fun createBattleSession(league: BattleLeague) {
        session = BattleSession(data.repository, league.cpCap)
        observationProcessor = BattleObservationProcessor(
            session = requireNotNull(session),
            repository = data.repository,
            moveAliases = data.moveAliases
        )
        pendingEnemySpeciesId?.let(::activateEnemy)
    }

    private fun selectLeague(league: BattleLeague, persistManualChoice: Boolean) {
        if (currentLeague == league && session != null) return
        currentLeague = league
        leaguePreference.lastDetectedLeague = league
        if (persistManualChoice) {
            leaguePreference.mode = when (league) {
                BattleLeague.GREAT -> LeagueSelectionMode.GREAT
                BattleLeague.ULTRA -> LeagueSelectionMode.ULTRA
                BattleLeague.MASTER -> LeagueSelectionMode.MASTER
            }
        }
        createBattleSession(league)
        render()
    }

    private fun activateEnemy(speciesId: String) {
        pendingEnemySpeciesId = speciesId
        val battle = session ?: run {
            render()
            return
        }

        val states = battle.enemyTracker.allStates()
        val knownSlot = states.firstOrNull { it.speciesId == speciesId }?.slot
        val emptySlot = states.firstOrNull { it.speciesId == null }?.slot
        val slot = knownSlot ?: emptySlot ?: battle.enemyTracker.activeEnemySlot() ?: 0

        if (battle.enemyTracker.state(slot).speciesId != speciesId) {
            battle.enemyTracker.identifyPokemon(slot, speciesId)
        }
        if (battle.enemyTracker.activeEnemySlot() != slot) battle.enemyTracker.switchTo(slot)
        render()
    }

    private fun createOverlayViews() {
        val metrics = DisplayMetrics().also {
            @Suppress("DEPRECATION")
            windowManager.defaultDisplay.getRealMetrics(it)
        }

        // Classic top-right type strip, positioned above/adjacent to the enemy
        // header instead of across the middle of the fight.
        weaknessBar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(dp(2), dp(1), dp(2), dp(1))
            background = null
            visibility = View.GONE
        }
        windowManager.addView(
            weaknessBar,
            overlayParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                Gravity.TOP or Gravity.END,
                touchable = false
            ).apply {
                x = dp(10)
                y = dp(8)
            }
        )

        // Current-player matchup indicator from the older HUD.
        myMatchupChip = TextView(this).apply {
            gravity = Gravity.CENTER
            textSize = 16f
            setTextColor(Color.WHITE)
            setShadowLayer(2f, 0f, 1f, Color.BLACK)
            visibility = View.GONE
        }
        windowManager.addView(
            myMatchupChip,
            overlayParams(dp(32), dp(32), Gravity.TOP or Gravity.START, touchable = false).apply {
                x = dp(8)
                y = dp(8)
            }
        )

        // Small reserve recommendations beside the game's reserve cards.
        reserveAdvisorBar = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.END
            visibility = View.GONE
        }
        windowManager.addView(
            reserveAdvisorBar,
            overlayParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                Gravity.END or Gravity.CENTER_VERTICAL,
                touchable = false
            ).apply {
                x = dp(5)
                y = dp(70)
            }
        )

        // Only appears when automatic lobby detection failed. Once a league is
        // known it disappears so it does not permanently alter the battle UI.
        leagueChip = TextView(this).apply {
            textSize = 11f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setPadding(dp(8), dp(4), dp(8), dp(4))
            background = rounded(Color.argb(175, 20, 34, 52), 18f)
            setOnClickListener { showLeagueMenu(this) }
            visibility = View.GONE
        }
        windowManager.addView(
            leagueChip,
            overlayParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                Gravity.TOP or Gravity.CENTER_HORIZONTAL,
                touchable = true
            ).apply { y = (metrics.heightPixels * 0.105f).toInt() }
        )

        // Classic transparent move HUD. No large dark rectangle over the trainer.
        movePanel = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dp(2), dp(2), dp(2), dp(2))
            background = null
            visibility = View.GONE
        }

        enemyLabel = TextView(this).apply {
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            textSize = 10f
            setShadowLayer(3f, 0f, 1f, Color.BLACK)
            setPadding(dp(6), dp(2), dp(6), dp(2))
            background = rounded(Color.argb(95, 0, 0, 0), 14f)
            setOnLongClickListener {
                correctionRow.visibility = if (correctionRow.visibility == View.VISIBLE) View.GONE else View.VISIBLE
                true
            }
        }
        movePanel.addView(
            enemyLabel,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )

        fastChip = FastMoveChip(this).apply {
            onSelectRequested = { anchor -> showFastMoveMenu(anchor) }
        }
        movePanel.addView(fastChip)

        val chargedRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }
        chargedLeft = ChargedMoveChip(this).apply {
            buttonIndex = 0
            onSelectRequested = { index, anchor -> showChargedMoveMenu(index, anchor) }
            setOnLongClickListener {
                clearChargedOverride(0)
                true
            }
        }
        chargedRight = ChargedMoveChip(this).apply {
            buttonIndex = 1
            onSelectRequested = { index, anchor -> showChargedMoveMenu(index, anchor) }
            setOnLongClickListener {
                clearChargedOverride(1)
                true
            }
        }
        chargedRow.addView(chargedLeft)
        chargedRow.addView(chargedRight)
        movePanel.addView(chargedRow)

        // Manual correction remains available but no longer clutters the default
        // design. Long-press the enemy name to show/hide it.
        correctionRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            visibility = View.GONE
            background = rounded(Color.argb(125, 0, 0, 0), 16f)
        }
        val minus = compactButton("−") { correctFastCount(-1) }
        correctionLabel = TextView(this).apply {
            text = "E 0"
            textSize = 9f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setPadding(dp(3), 0, dp(3), 0)
        }
        val plus = compactButton("+") { correctFastCount(1) }
        correctionRow.addView(minus, LinearLayout.LayoutParams(dp(32), dp(30)))
        correctionRow.addView(correctionLabel, LinearLayout.LayoutParams(dp(48), dp(30)))
        correctionRow.addView(plus, LinearLayout.LayoutParams(dp(32), dp(30)))
        movePanel.addView(correctionRow)

        windowManager.addView(
            movePanel,
            overlayParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                Gravity.TOP or Gravity.END,
                touchable = true
            ).apply {
                // Leaves a visible gap before the opponent trainer model.
                x = (metrics.widthPixels * 0.20f).toInt()
                y = (metrics.heightPixels * 0.205f).toInt()
            }
        )
    }

    private fun render() {
        val league = currentLeague
        leagueChip.text = league?.let { "${it.labelPtBr} ▼" } ?: "Escolher liga ▼"
        leagueChip.visibility = if (league == null && pendingEnemySpeciesId != null) View.VISIBLE else View.GONE

        val battle = session
        val slot = battle?.enemyTracker?.activeEnemySlot()
        if (battle == null || slot == null) {
            movePanel.visibility = View.GONE
            weaknessBar.visibility = View.GONE
            myMatchupChip.visibility = View.GONE
            reserveAdvisorBar.visibility = View.GONE
            return
        }

        movePanel.visibility = View.VISIBLE
        val state = battle.enemyTracker.state(slot)
        enemyLabel.text = data.displayName(state.speciesId)

        val fast = state.fastMoveId?.let(data.repository::move)
        fastChip.bind(fast, data.moveDisplayName(fast?.moveId))

        val progressById = battle.enemyTracker.chargedMoveProgress(slot)
            .associateBy { it.move.moveId }
        bindCharged(
            chargedLeft,
            state.selectedChargedMoveIds.getOrNull(0),
            progressById,
            state.observedChargedMoveIds,
            0 in state.manualChargedMoveButtonIndexes
        )
        bindCharged(
            chargedRight,
            state.selectedChargedMoveIds.getOrNull(1),
            progressById,
            state.observedChargedMoveIds,
            1 in state.manualChargedMoveButtonIndexes
        )

        correctionLabel.text = "E ${state.energy}"
        renderWeaknesses(state.speciesId)
        renderTeamAdvice(state.speciesId)
    }

    private fun bindCharged(
        chip: ChargedMoveChip,
        moveId: String?,
        progressById: Map<String, ChargedMoveProgress>,
        observed: Set<String>,
        manual: Boolean
    ) {
        val progress = moveId?.let(progressById::get)
        val move = moveId?.let(data.repository::move)
        val baseName = data.moveDisplayName(moveId) ?: move?.name
        val suffix = when {
            moveId != null && moveId in observed -> " ✓"
            manual -> " ✎"
            else -> ""
        }
        val display = baseName?.plus(suffix)
        if (progress != null) chip.bind(progress, display) else chip.bindMoveOnly(move, display)
    }

    private fun renderWeaknesses(speciesId: String?) {
        weaknessBar.removeAllViews()
        if (speciesId == null) {
            weaknessBar.visibility = View.GONE
            return
        }

        val types = data.strongTypesAgainst(speciesId).take(6)
        if (types.isEmpty()) {
            weaknessBar.visibility = View.GONE
            return
        }

        types.forEach { type ->
            val icon = TypeIconView(this).apply {
                setType(type)
                contentDescription = type
            }
            weaknessBar.addView(
                icon,
                LinearLayout.LayoutParams(dp(27), dp(27)).apply {
                    marginStart = dp(2)
                    marginEnd = dp(2)
                }
            )
        }
        weaknessBar.visibility = View.VISIBLE
    }

    private fun renderTeamAdvice(enemySpeciesId: String?) {
        val battle = session ?: return hideTeamAdvice()
        val enemyId = enemySpeciesId ?: return hideTeamAdvice()
        val snapshot = battle.snapshot()
        val ids = snapshot.myTeam.map { it.speciesId }
        val recommendations = TypeMatchupAdvisor(data.repository).rankMyTeam(enemyId, ids)
        if (recommendations.isEmpty()) return hideTeamAdvice()

        val activeSlot = snapshot.myTeam.firstOrNull { it.status == PokemonBattleStatus.ACTIVE }?.slot
        val activeRecommendation = activeSlot?.let { slot -> recommendations.firstOrNull { it.slot == slot } }
        if (activeRecommendation != null) {
            val (symbol, color) = matchupMarker(activeRecommendation.typeScore)
            myMatchupChip.text = symbol
            myMatchupChip.background = rounded(Color.argb(220, Color.red(color), Color.green(color), Color.blue(color)), 18f)
            myMatchupChip.visibility = View.VISIBLE
        } else {
            myMatchupChip.visibility = View.GONE
        }

        reserveAdvisorBar.removeAllViews()
        val reserves = snapshot.myTeam.filter { state ->
            state.speciesId != null && state.slot != activeSlot && state.status != PokemonBattleStatus.FAINTED
        }
        reserves.forEach { state ->
            val pokemon = state.speciesId?.let(data.repository::pokemon) ?: return@forEach
            val recommendation = recommendations.firstOrNull { it.slot == state.slot }
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL or Gravity.END
                setPadding(dp(2), dp(2), dp(2), dp(2))
            }
            pokemon.types.take(2).forEach { type ->
                row.addView(
                    TypeIconView(this).apply { setType(type) },
                    LinearLayout.LayoutParams(dp(22), dp(22)).apply { marginEnd = dp(2) }
                )
            }
            val score = recommendation?.typeScore ?: 1.0
            val (symbol, color) = matchupMarker(score)
            row.addView(TextView(this).apply {
                text = symbol
                textSize = 14f
                gravity = Gravity.CENTER
                setTextColor(Color.WHITE)
                setShadowLayer(2f, 0f, 1f, Color.BLACK)
                background = rounded(Color.argb(220, Color.red(color), Color.green(color), Color.blue(color)), 14f)
            }, LinearLayout.LayoutParams(dp(25), dp(25)))
            reserveAdvisorBar.addView(row)
        }
        reserveAdvisorBar.visibility = if (reserves.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun hideTeamAdvice() {
        myMatchupChip.visibility = View.GONE
        reserveAdvisorBar.visibility = View.GONE
    }

    private fun matchupMarker(score: Double): Pair<String, Int> = when {
        score >= 1.15 -> "✓" to Color.rgb(25, 180, 85)
        score <= 0.85 -> "×" to Color.rgb(215, 65, 65)
        else -> "•" to Color.rgb(115, 125, 135)
    }

    /** Tap the fast icon/name to choose any legal fast move for that species. */
    private fun showFastMoveMenu(anchor: View) {
        val battle = session ?: return
        val slot = battle.enemyTracker.activeEnemySlot() ?: return
        val moves = battle.enemyTracker.availableFastMoves(slot)
        if (moves.isEmpty()) return

        PopupMenu(this, anchor).apply {
            moves.forEach { move ->
                menu.add("${data.moveDisplayName(move.moveId) ?: move.name}  •  ${move.energyGain}E")
                    .setOnMenuItemClickListener {
                        runCatching { battle.enemyTracker.setFastMove(slot, move.moveId) }
                            .onSuccess { render() }
                        true
                    }
            }
            show()
        }
    }

    /**
     * Tap either charged icon to choose from all legal charged moves.
     * Long-press returns that button to automatic PvPoke/OCR control.
     */
    private fun showChargedMoveMenu(buttonIndex: Int, anchor: View) {
        val battle = session ?: return
        val slot = battle.enemyTracker.activeEnemySlot() ?: return
        val moves = battle.enemyTracker.availableChargedMoves(slot)
        if (moves.isEmpty()) return

        PopupMenu(this, anchor).apply {
            menu.add("Automático (PvPoke/OCR)").setOnMenuItemClickListener {
                battle.enemyTracker.clearChargedMoveManualOverride(slot, buttonIndex)
                render()
                true
            }
            moves.forEach { move ->
                val name = data.moveDisplayName(move.moveId) ?: move.name
                menu.add("$name  •  ${move.energyCost}E")
                    .setOnMenuItemClickListener {
                        runCatching { battle.enemyTracker.setChargedMove(slot, buttonIndex, move.moveId) }
                            .onSuccess { render() }
                        true
                    }
            }
            show()
        }
    }

    private fun clearChargedOverride(buttonIndex: Int) {
        val battle = session ?: return
        val slot = battle.enemyTracker.activeEnemySlot() ?: return
        battle.enemyTracker.clearChargedMoveManualOverride(slot, buttonIndex)
        Toast.makeText(this, "Golpe ${buttonIndex + 1} voltou para automático.", Toast.LENGTH_SHORT).show()
        render()
    }

    private fun showLeagueMenu(anchor: View) {
        PopupMenu(this, anchor).apply {
            BattleLeague.entries.forEach { league ->
                menu.add(league.labelPtBr).setOnMenuItemClickListener {
                    selectLeague(league, persistManualChoice = true)
                    true
                }
            }
            menu.add("Automático — ler lobby").setOnMenuItemClickListener {
                leaguePreference.mode = LeagueSelectionMode.AUTO
                currentLeague = null
                session = null
                observationProcessor = null
                render()
                true
            }
            show()
        }
    }

    private fun correctFastCount(delta: Int) {
        val battle = session ?: return
        val slot = battle.enemyTracker.activeEnemySlot() ?: return
        val state = battle.enemyTracker.state(slot)
        val fast = state.fastMoveId?.let(data.repository::move) ?: return
        val corrected = (state.energy + fast.energyGain * delta).coerceIn(0, 100)
        battle.enemyTracker.setEstimatedEnergy(slot, corrected)
        render()
    }

    private fun compactButton(textValue: String, click: () -> Unit): Button =
        Button(this).apply {
            text = textValue
            textSize = 13f
            minWidth = 0
            minimumWidth = 0
            minHeight = 0
            minimumHeight = 0
            setPadding(dp(3), 0, dp(3), 0)
            setOnClickListener { click() }
        }

    private fun overlayParams(
        width: Int,
        height: Int,
        gravity: Int,
        touchable: Boolean
    ): WindowManager.LayoutParams {
        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }
        var flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
        if (!touchable) flags = flags or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE

        return WindowManager.LayoutParams(
            width,
            height,
            type,
            flags,
            PixelFormat.TRANSLUCENT
        ).apply { this.gravity = gravity }
    }

    private fun rounded(color: Int, radiusDp: Float): GradientDrawable =
        GradientDrawable().apply {
            setColor(color)
            cornerRadius = radiusDp * resources.displayMetrics.density
        }

    private fun removeOverlay(view: View) {
        if (view.isAttachedToWindow) runCatching { windowManager.removeView(view) }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getSystemService(NotificationManager::class.java).createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID,
                    "PvPPokeGo batalha",
                    NotificationManager.IMPORTANCE_LOW
                )
            )
        }
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    companion object {
        const val ACTION_START = "com.lucianotoscano.pvppokego.START"
        const val ACTION_STOP = "com.lucianotoscano.pvppokego.STOP"
        const val EXTRA_RESULT_CODE = "result_code"
        const val EXTRA_RESULT_DATA = "result_data"

        private const val CHANNEL_ID = "pvppokego_battle"
        private const val NOTIFICATION_ID = 202
        private const val MOTION_TRIGGER_THRESHOLD = 19.0
        private const val MOTION_REARM_THRESHOLD = 8.0
    }
}
