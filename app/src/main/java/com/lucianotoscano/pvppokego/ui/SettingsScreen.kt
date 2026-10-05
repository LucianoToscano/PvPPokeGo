package com.lucianotoscano.pvppokego.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lucianotoscano.pvppokego.data.BattleHistoryEntry
import com.lucianotoscano.pvppokego.data.BattleHistoryRepository
import com.lucianotoscano.pvppokego.data.BattleLeagueMode
import com.lucianotoscano.pvppokego.data.SettingsRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SettingsScreen(
    repository: SettingsRepository,
    historyRepository: BattleHistoryRepository,
    historyRevision: Int,
    overlayPermissionGranted: Boolean,
    onRequestOverlayPermission: () -> Unit,
    onStartOverlay: () -> Unit,
    onStopOverlay: () -> Unit,
    onResetBattle: () -> Unit,
    onExportSettings: () -> Unit,
    onImportSettings: () -> Unit
) {
    var overlay by remember { mutableStateOf(repository.overlayEnabled) }
    var auto by remember { mutableStateOf(repository.autoRecognition) }
    var assist by remember { mutableStateOf(repository.battleAssistEnabled) }
    var league by remember { mutableStateOf(repository.leagueMode) }
    var strong by remember { mutableStateOf(repository.showStrongTypes) }
    var current by remember { mutableStateOf(repository.showCurrentIndicator) }
    var reserves by remember { mutableStateOf(repository.analyzeReserves) }
    var reserveTypes by remember { mutableStateOf(repository.showReserveTypes) }
    var moves by remember { mutableStateOf(repository.showEnemyMoves) }
    var counter by remember { mutableStateOf(repository.showChargedCounter) }
    var progress by remember { mutableStateOf(repository.showEnergyProgress) }
    var timer by remember { mutableStateOf(repository.showSwitchTimer) }
    var debug by remember { mutableStateOf(repository.debugMode) }
    var edit by remember { mutableStateOf(repository.editMode) }
    var scale by remember { mutableFloatStateOf(repository.scale) }
    var opacity by remember { mutableFloatStateOf(repository.opacity) }
    var history by remember(historyRevision) { mutableStateOf(historyRepository.loadHistory()) }
    var expandedBattleId by remember { mutableStateOf<Long?>(null) }

    MaterialTheme {
        Scaffold { padding ->
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 18.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Spacer(Modifier.height(8.dp))
                Text("PvPPokeGo", style = MaterialTheme.typography.headlineMedium)
                Text("Assistente visual externo de PvP. Não modifica nem envia comandos ao Pokémon GO.")

                if (!overlayPermissionGranted) {
                    Button(
                        onClick = onRequestOverlayPermission,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Permitir sobreposição")
                    }
                }

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onStartOverlay,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Iniciar overlay")
                    }
                    Button(
                        onClick = onStopOverlay,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Parar")
                    }
                }

                Button(
                    onClick = onResetBattle,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("RESET BATTLE")
                }

                Text(
                    "Controles do overlay:\n" +
                        "• 🔓 Cadeado aberto → arrasta os blocos do HUD (toques do jogo passam fora dos blocos)\n" +
                        "• 🔒 Cadeado fechado → layout travado\n" +
                        "• ◉ → mostrar / ocultar HUD\n" +
                        "• ⚙ → menu rápido (liga, edição, opções)\n" +
                        "• Editar HUD → barra ✓ (salvar) e ↺ (reset)\n" +
                        "Versão 0.5.11",
                    style = MaterialTheme.typography.bodySmall
                )

                HorizontalDivider(Modifier.padding(vertical = 8.dp))
                Toggle("Overlay", overlay) {
                    overlay = it
                    repository.overlayEnabled = it
                }
                Toggle("Reconhecimento automático", auto) {
                    auto = it
                    repository.autoRecognition = it
                }
                Toggle("Modo Batalha Assistido", assist) {
                    assist = it
                    repository.battleAssistEnabled = it
                }
                if (assist) {
                    Text(
                        "Copiloto visual: recomenda CONTINUE FAST, TROQUE, ESCUDO, NÃO ESCUDE e alerta de Charged. " +
                            "Ele não toca nem envia comandos ao Pokémon GO."
                    )
                }

                Text("Liga / ranking PvP", style = MaterialTheme.typography.titleMedium)
                Text(
                    if (league == BattleLeagueMode.AUTO) {
                        val detected = when (repository.lastDetectedLeagueCp) {
                            2500 -> "Liga Ultra"
                            10000 -> "Liga Mestra"
                            else -> "Liga Grande"
                        }
                        "Automático: ler o lobby. Última detectada: $detected"
                    } else {
                        league.label
                    }
                )

                LeagueSelectorRow(
                    left = BattleLeagueMode.AUTO,
                    right = BattleLeagueMode.GREAT,
                    selected = league
                ) { selected ->
                    league = selected
                    repository.leagueMode = selected
                }

                LeagueSelectorRow(
                    left = BattleLeagueMode.ULTRA,
                    right = BattleLeagueMode.MASTER,
                    selected = league
                ) { selected ->
                    league = selected
                    repository.leagueMode = selected
                }

                Toggle("Fraquezas do inimigo no topo", strong) {
                    strong = it
                    repository.showStrongTypes = it
                }
                Toggle("Indicador do Pokémon atual", current) {
                    current = it
                    repository.showCurrentIndicator = it
                }
                Toggle("Analisar reservas", reserves) {
                    reserves = it
                    repository.analyzeReserves = it
                }
                Toggle("Mostrar tipos das reservas", reserveTypes) {
                    reserveTypes = it
                    repository.showReserveTypes = it
                }
                Toggle("Ataques do inimigo", moves) {
                    moves = it
                    repository.showEnemyMoves = it
                }
                Toggle("Pontos de carregamento", counter) {
                    counter = it
                    repository.showChargedCounter = it
                }
                Toggle("Progresso de energia", progress) {
                    progress = it
                    repository.showEnergyProgress = it
                }
                Toggle("Timer de troca", timer) {
                    timer = it
                    repository.showSwitchTimer = it
                }
                Toggle("Modo edição — segure e arraste", edit) {
                    edit = it
                    repository.editMode = it
                }
                if (edit) {
                    Text(
                        "Na batalha, segure um bloco do HUD por um instante e arraste. " +
                            "As posições são salvas automaticamente e o movimento é livre pela tela."
                    )
                    OutlinedButton(
                        onClick = { repository.clearHudPositions() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Restaurar posições padrão do HUD")
                    }
                }
                Toggle("DEBUG", debug) {
                    debug = it
                    repository.debugMode = it
                }

                Text("Escala: ${(scale * 100).toInt()}%")
                Slider(
                    value = scale,
                    onValueChange = {
                        scale = it
                        repository.scale = it
                    },
                    valueRange = .5f..1.2f,
                    steps = 6
                )

                Text("Opacidade: ${(opacity * 100).toInt()}%")
                Slider(
                    value = opacity,
                    onValueChange = {
                        opacity = it
                        repository.opacity = it
                    },
                    valueRange = .3f..1f,
                    steps = 6
                )

                HorizontalDivider(Modifier.padding(vertical = 8.dp))
                Text("Backup de configurações e posições", style = MaterialTheme.typography.titleMedium)
                Text(
                    "Salva opções, escala, opacidade e as posições personalizadas dos ícones, blocos e menus do HUD. " +
                        "Ao importar, o layout volta para o backup e fica travado por segurança.",
                    style = MaterialTheme.typography.bodySmall
                )
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(onClick = onExportSettings, modifier = Modifier.weight(1f)) {
                        Text("Exportar")
                    }
                    OutlinedButton(onClick = onImportSettings, modifier = Modifier.weight(1f)) {
                        Text("Importar")
                    }
                }

                HorizontalDivider(Modifier.padding(vertical = 8.dp))
                Text("Histórico de batalhas", style = MaterialTheme.typography.titleMedium)
                Text(
                    "Mostra Pokémon, PC, ataques, nível de confiança, decisões do assistente e o motivo de cada recomendação. " +
                        "Use a linha do tempo para rever contagem de energia, trocas e possíveis erros de identificação.",
                    style = MaterialTheme.typography.bodySmall
                )
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { history = historyRepository.loadHistory() },
                        modifier = Modifier.weight(1f)
                    ) { Text("Atualizar") }
                    OutlinedButton(
                        onClick = {
                            historyRepository.clear()
                            history = emptyList()
                            expandedBattleId = null
                        },
                        modifier = Modifier.weight(1f),
                        enabled = history.isNotEmpty()
                    ) { Text("Limpar") }
                }

                if (history.isEmpty()) {
                    Text("Nenhuma batalha registrada ainda.", style = MaterialTheme.typography.bodySmall)
                } else {
                    history.take(10).forEach { battle ->
                        BattleHistoryCard(
                            battle = battle,
                            expanded = expandedBattleId == battle.id,
                            onToggle = {
                                expandedBattleId = if (expandedBattleId == battle.id) null else battle.id
                            }
                        )
                    }
                }

                HorizontalDivider(Modifier.padding(vertical = 8.dp))
                Text("Sobre", style = MaterialTheme.typography.titleMedium)
                Text("Desenvolvimento PvPPokGo by: LucianoToscano\n(84) 99131-9021")
                Text("Versão 0.5.14")
                Text(
                    "Base visual V03 preservada. O HUD protege a interface nativa do Pokémon GO, " +
                        "permite reposicionar blocos no modo edição e mantém o botão de olho no topo."
                )
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun BattleHistoryCard(
    battle: BattleHistoryEntry,
    expanded: Boolean,
    onToggle: () -> Unit
) {
    val date = remember(battle.startedAtEpochMs) {
        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(battle.startedAtEpochMs))
    }
    val league = when (battle.leagueCp) {
        2500 -> "Ultra"
        10000 -> "Mestra"
        else -> "Grande"
    }
    val player = battle.playerName ?: "Seu Pokémon"
    val enemy = battle.opponentName ?: "Inimigo"
    val playerCp = battle.playerCp?.let { " PC $it" }.orEmpty()
    val enemyCp = battle.opponentCp?.let { " PC $it" }.orEmpty()
    val duration = ((battle.endedAtEpochMs - battle.startedAtEpochMs).coerceAtLeast(0L) / 1000L)
    val ownCharged = battle.events.count { it.actor == "VOCÊ" && it.category == "CARREGADO" }
    val enemyCharged = battle.events.count { it.actor == "INIMIGO" && it.category == "CARREGADO" }
    val decisions = battle.events.count { it.category == "DECISÃO" }

    Column(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Text("$date • Liga $league • ${duration}s", style = MaterialTheme.typography.labelMedium)
        Text("$player$playerCp  ×  $enemy$enemyCp")
        Text(
            "Carregados: você $ownCharged • inimigo $enemyCharged • decisões $decisions • ${battle.events.size} eventos",
            style = MaterialTheme.typography.bodySmall
        )
        OutlinedButton(onClick = onToggle, modifier = Modifier.fillMaxWidth()) {
            Text(if (expanded) "Ocultar detalhes" else "Ver ataques e tempos")
        }
        if (expanded) {
            if (battle.events.isEmpty()) {
                Text("Nenhum ataque identificado nesta batalha.", style = MaterialTheme.typography.bodySmall)
            } else {
                battle.events.forEach { event ->
                    val seconds = event.elapsedMs / 1000.0
                    val count = if (event.count > 1) " ×${event.count}" else ""
                    val pokemon = event.pokemon?.let { " • $it" }.orEmpty()
                    val confidence = event.confidence?.let { " • $it" }.orEmpty()
                    val source = event.source?.let { " • fonte: $it" }.orEmpty()
                    Text(
                        "[+${"%.1f".format(Locale.US, seconds)}s] ${event.actor} • ${event.category} • ${event.moveName}$count$pokemon$confidence$source",
                        style = MaterialTheme.typography.bodySmall
                    )
                    event.reason?.takeIf { it.isNotBlank() }?.let { reason ->
                        Text("↳ $reason", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

@Composable
private fun LeagueSelectorRow(
    left: BattleLeagueMode,
    right: BattleLeagueMode,
    selected: BattleLeagueMode,
    onSelect: (BattleLeagueMode) -> Unit
) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        LeagueButton(left, selected == left, Modifier.weight(1f), onSelect)
        LeagueButton(right, selected == right, Modifier.weight(1f), onSelect)
    }
}

@Composable
private fun LeagueButton(
    mode: BattleLeagueMode,
    selected: Boolean,
    modifier: Modifier,
    onSelect: (BattleLeagueMode) -> Unit
) {
    if (selected) {
        Button(
            onClick = { onSelect(mode) },
            modifier = modifier
        ) {
            Text(mode.label)
        }
    } else {
        OutlinedButton(
            onClick = { onSelect(mode) },
            modifier = modifier
        ) {
            Text(mode.label)
        }
    }
}

@Composable
private fun Toggle(
    label: String,
    value: Boolean,
    onChange: (Boolean) -> Unit
) {
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, modifier = Modifier.weight(1f))
        Switch(checked = value, onCheckedChange = onChange)
    }
}
