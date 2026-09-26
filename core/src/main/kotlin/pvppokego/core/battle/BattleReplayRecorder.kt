package pvppokego.core.battle

enum class BattleReplayEventType {
    BATTLE_START,
    BATTLE_END,
    ENEMY_IDENTIFIED,
    FAST_MOVE,
    CHARGED_MOVE,
    MOVE_CONFIRMED,
    SWITCH,
    SHIELD_RECOMMENDATION,
    ENERGY_CORRECTION,
    MANUAL_CORRECTION,
    NOTE
}

data class BattleReplayEvent(
    val timestampMs: Long,
    val type: BattleReplayEventType,
    val slot: Int? = null,
    val speciesId: String? = null,
    val moveId: String? = null,
    val detail: String = "",
    val metadata: Map<String, String> = emptyMap()
)

/**
 * Local-only diagnostic timeline. It intentionally records normalized battle
 * events instead of screenshots or account credentials.
 */
class BattleReplayRecorder(
    private val maxEvents: Int = 2_000
) {
    private val events = ArrayDeque<BattleReplayEvent>()

    init {
        require(maxEvents > 0)
    }

    fun record(event: BattleReplayEvent) {
        while (events.size >= maxEvents) events.removeFirst()
        events.addLast(event)
    }

    fun all(): List<BattleReplayEvent> = events.toList()

    fun clear() {
        events.clear()
    }

    fun exportText(): String = events.joinToString("\n") { event ->
        buildString {
            append(event.timestampMs)
            append('\t')
            append(event.type.name)
            event.slot?.let { append("\tslot=").append(it) }
            event.speciesId?.let { append("\tspecies=").append(it) }
            event.moveId?.let { append("\tmove=").append(it) }
            if (event.detail.isNotBlank()) append("\tdetail=").append(event.detail.replace('\n', ' '))
            if (event.metadata.isNotEmpty()) {
                append("\t")
                append(event.metadata.entries.joinToString(",") { "${it.key}=${it.value}" })
            }
        }
    }
}
