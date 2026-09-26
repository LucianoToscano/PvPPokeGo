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
import com.lucianotoscano.pvppokego.data.BattleLeagueMode
import com.lucianotoscano.pvppokego.data.SettingsRepository

@Composable
fun SettingsScreen(
    repository: SettingsRepository,
    overlayPermissionGranted: Boolean,
    onRequestOverlayPermission: () -> Unit,
    onStartOverlay: () -> Unit,
    onStopOverlay: () -> Unit,
    onResetBattle: () -> Unit
) {
    var overlay by remember { mutableStateOf(repository.overlayEnabled) }
    var auto by remember { mutableStateOf(repository.autoRecognition) }
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

    MaterialTheme {
        Scaffold { padding ->
            Column(
                Modifier.fillMaxSize().padding(padding).padding(horizontal = 18.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Spacer(Modifier.height(8.dp))
                Text("PvPPokeGo", style = MaterialTheme.typography.headlineMedium)
                Text("Assistente visual externo de PvP. Não modifica nem envia comandos ao Pokémon GO.")

                if (!overlayPermissionGranted) {
                    Button(onClick = onRequestOverlayPermission, modifier = Modifier.fillMaxWidth()) {
                        Text("Permitir sobreposição")
                    }
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = onStartOverlay, modifier = Modifier.weight(1f)) { Text("Iniciar overlay") }
                    Button(onClick = onStopOverlay, modifier = Modifier.weight(1f)) { Text("Parar") }
                }
                Button(onClick = onResetBattle, modifier = Modifier.fillMaxWidth()) { Text("RESET BATTLE") }

                HorizontalDivider(Modifier.padding(vertical = 8.dp))
                Toggle("Overlay", overlay) { overlay = it; repository.overlayEnabled = it }
                Toggle("Reconhecimento automático", auto) { auto = it; repository.autoRecognition = it }

                Text("Liga / ranking PvP", style = MaterialTheme.typography.titleMedium)
                Text(
                    if (league == BattleLeagueMode.AUTO) {
                        val detected = when (repository.lastDetectedLeagueCp) {
                            2500 -> "Liga Ultra"
                            10000 -> "Liga Mestra"
                            else -> "Liga Grande"
                        }
                        "Automático: ler o lobby. Última detectada: $detected"
                    } else league.label
                )
                LeagueSelectorRow(
                    left = BattleLeagueMode.AUTO,
                    right = BattleLeagueMode.GREAT,
                    selected = league
                ) { selected -> league = selected; repository.leagueMode = selected }
                LeagueSelectorRow(
                    left = BattleLeagueMode.ULTRA,
                    right = BattleLeagueMode.MASTER,
                    selected = league
                ) { selected -> league = selected; repository.leagueMode = selected }

                Toggle("Tipos fortes contra inimigo", strong) { strong = it; repository.showStrongTypes = it }
                Toggle("Indicador do Pokémon atual", current) { current = it; repository.showCurrentIndicator = it }
                Toggle("Analisar reservas", reserves) { reserves = it; repository.analyzeReserves = it }
                Toggle("Mostrar tipos das reservas", reserveTypes) { reserveTypes = it; repository.showReserveTypes = it }
                Toggle("Ataques do inimigo", moves) { moves = it; repository.showEnemyMoves = it }
                Toggle("Contador de carregados", counter) { counter = it; repository.showChargedCounter = it }
                Toggle("Progresso de energia", progress) { progress = it; repository.showEnergyProgress = it }
                Toggle("Timer de troca", timer) { timer = it; repository.showSwitchTimer = it }
                Toggle("Modo edição", edit) { edit = it; repository.editMode = it }
                Toggle("DEBUG", debug) { debug = it; repository.debugMode = it }

                Text("Escala: ${(scale * 100).toInt()}%")
                Slider(value = scale, onValueChange = { scale = it; repository.scale = it }, valueRange = .5f..1.2f, steps = 6)
                Text("Opacidade: ${(opacity * 100).toInt()}%")
                Slider(value = opacity, onValueChange = { opacity = it; repository.opacity = it }, valueRange = .3f..1f, steps = 6)

                HorizontalDivider(Modifier.padding(vertical = 8.dp))
                Text("Sobre", style = MaterialTheme.typography.titleMedium)
                Text("Desenvolvimento PvPPokGo by: LucianoToscano\n(84) 99131-9021")
                Text("Base visual V03 preservada. A evolução adiciona leitura da liga, memória de energia/moves e confirmação automática dos carregados reais sem redesenhar o HUD.")
                Spacer(Modifier.height(24.dp))
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
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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
        Button(onClick = { onSelect(mode) }, modifier = modifier) { Text(mode.label) }
    } else {
        OutlinedButton(onClick = { onSelect(mode) }, modifier = modifier) { Text(mode.label) }
    }
}

@Composable
private fun Toggle(label: String, value: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, modifier = Modifier.weight(1f))
        Switch(checked = value, onCheckedChange = onChange)
    }
}
