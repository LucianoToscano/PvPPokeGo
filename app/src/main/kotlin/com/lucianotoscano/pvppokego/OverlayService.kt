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
import pvppokego.core.model.ChargedMoveProgress
import pvppokego.core.model.MoveData

/**
 * External PvP overlay. It never injects into or modifies Pokemon GO.
 *
 * Flow:
 * MediaProjection -> OCR/motion -> local PvPoke IDs -> BattleSession -> overlay.
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
    private lateinit var leagueChip: TextView
    private lateinit var movePanel: LinearLayout
    private lateinit var enemyLabel: TextView
    private lateinit var fastChip: FastMoveChip
    private lateinit var chargedLeft: ChargedMoveChip
    private lateinit var chargedRight: ChargedMoveChip
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
        removeOverlay(leagueChip)
        removeOverlay(movePanel)
        stopForeground(STOP_FOREGROUND_REMOVE)
        super.onDestroy()
    }

    override fun onOcr(
        fullText: String,
        enemyHeaderText: String,
        chargedBannerLines: List<String>,
        timestampMs: Long
    ) {
        mainHandler.post {
            val lobbyLeague = LobbyLeagueDetector.detect(fullText)
            if (leaguePreference.mode == LeagueSelectionMode.AUTO && currentLeague == null) {
                lobbyLeague?.let { detected ->
                    leaguePreference.lastDetectedLeague = detected
                    selectLeague(detected, persistManualChoice = false)
                }
            }

            // The lobby normally shows the player's three selected Pokemon.
            if (lobbyLeague != null) registerLobbyTeam(fullText)

            data.resolveSpeciesFromText(enemyHeaderText)?.let(::activateEnemy)

            // Exact charged-move text is mapped to the local PvPoke move ID.
            // This works for ANY legal charged move represented in the local data,
            // not only Thunderbolt/Relampago.
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

        // Top weakness row: directly under the game's enemy header/shield area.
        // It is intentionally not centered over the opponent trainer model.
        weaknessBar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(dp(5), dp(3), dp(5), dp(3))
            background = rounded(Color.argb(105, 0, 0, 0), 18f)
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
                x = (metrics.widthPixels * 0.025f).toInt()
                y = (metrics.heightPixels * 0.080f).toInt()
            }
        )

        leagueChip = TextView(this).apply {
            textSize = 12f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setPadding(dp(9), dp(5), dp(9), dp(5))
            background = rounded(Color.argb(165, 20, 34, 52), 18f)
            setOnClickListener { showLeagueMenu(this) }
        }
        windowManager.addView(
            leagueChip,
            overlayParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                Gravity.TOP or Gravity.CENTER_HORIZONTAL,
                touchable = true
            ).apply { y = (metrics.heightPixels * 0.118f).toInt() }
        )

        movePanel = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dp(5), dp(4), dp(5), dp(5))
            background = rounded(Color.argb(105, 0, 0, 0), 16f)
        }

        enemyLabel = TextView(this).apply {
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            textSize = 12f
            setPadding(dp(3), dp(2), dp(3), dp(2))
        }
        movePanel.addView(
            enemyLabel,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
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

        val correctionRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }
        val minus = compactButton("−") { correctFastCount(-1) }
        correctionLabel = TextView(this).apply {
            text = "energia"
            textSize = 10f
            setTextColor(Color.LTGRAY)
            gravity = Gravity.CENTER
            setPadding(dp(5), 0, dp(5), 0)
        }
        val plus = compactButton("+") { correctFastCount(1) }
        correctionRow.addView(minus, LinearLayout.LayoutParams(dp(38), dp(34)))
        correctionRow.addView(correctionLabel, LinearLayout.LayoutParams(dp(72), dp(34)))
        correctionRow.addView(plus, LinearLayout.LayoutParams(dp(38), dp(34)))
        movePanel.addView(correctionRow)

        windowManager.addView(
            movePanel,
            overlayParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                Gravity.TOP or Gravity.START,
                touchable = true
            ).apply {
                // Deliberately keep the move panel away from the opponent trainer.
                // In portrait battles this places it left of the trainer with a
                // visible gap instead of drawing icons over the human character.
                x = (metrics.widthPixels * 0.30f).toInt()
                y = (metrics.heightPixels * 0.185f).toInt()
            }
        )
    }

    private fun render() {
        val league = currentLeague
        leagueChip.text = league?.let { "${it.labelPtBr} ▼" } ?: "Liga? ▼"
        leagueChip.setTextColor(if (league == null) Color.rgb(255, 210, 80) else Color.WHITE)

        val battle = session
        val slot = battle?.enemyTracker?.activeEnemySlot()
        if (battle == null || slot == null) {
            enemyLabel.text = if (league == null) "Selecione a liga" else "Aguardando inimigo…"
            fastChip.bind(null)
            chargedLeft.bind(null)
            chargedRight.bind(null)
            correctionLabel.text = "energia"
            weaknessBar.visibility = View.GONE
            return
        }

        val state = battle.enemyTracker.state(slot)
        enemyLabel.text = data.displayName(state.speciesId)

        val fast = state.fastMoveId?.let(data.repository::move)
        fastChip.bind(fast, data.moveDisplayName(fast?.moveId))

        val progressById = battle.enemyTracker.chargedMoveProgress(slot)
            .associateBy { it.move.moveId }
        bindCharged(chargedLeft, state.selectedChargedMoveIds.getOrNull(0), progressById, state.observedChargedMoveIds, 0 in state.manualChargedMoveButtonIndexes)
        bindCharged(chargedRight, state.selectedChargedMoveIds.getOrNull(1), progressById, state.observedChargedMoveIds, 1 in state.manualChargedMoveButtonIndexes)

        correctionLabel.text = "E ${state.energy}"
        renderWeaknesses(state.speciesId)
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
                LinearLayout.LayoutParams(dp(28), dp(28)).apply {
                    marginStart = dp(2)
                    marginEnd = dp(2)
                }
            )
        }
        weaknessBar.visibility = View.VISIBLE
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
        Toast.makeText(this, "Golpe $buttonIndex voltou para automático.", Toast.LENGTH_SHORT).show()
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
            textSize = 15f
            minWidth = 0
            minimumWidth = 0
            minHeight = 0
            minimumHeight = 0
            setPadding(dp(5), 0, dp(5), 0)
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
