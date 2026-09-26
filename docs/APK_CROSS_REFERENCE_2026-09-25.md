# Auditoria cruzada de APKs — 2026-09-25

Objetivo: registrar funções e padrões arquiteturais encontrados nos APKs fornecidos e decidir o que pode ser reimplementado de forma própria no PvPPokeGo.

> Regra do projeto: usar os APKs como referência comportamental/arquitetural. Não copiar código proprietário, não remover licenças/premium e não implementar ocultação/bypass de anti-cheat.

## Arquivos analisados

| Artefato | SHA-256 | Papel na análise |
|---|---|---|
| Pokemod 13.3.4 | `2eafb8a85c700addcf7fd4aa96dc478688bf782004d749b4f80cec140332fd01` | referência de launcher, root/injeção, vPGP, contas e notificações |
| iPogo 237 / Pokémon GO 0.429.1 | `561abbcd0ff23a343f5e85480f0736dea1ba71b4f55b0e512dc0472258e7d0e6` | referência de funções nativas integradas ao jogo |
| Poke Genie 8.15.0 | `32b8a0b98ccc8d16b79411e81fcac4ef8471e785a4bf444a3014273ad908969b` | referência de overlay, OCR, IV/PvP e simulador |
| Pokémon GO oficial 0.429.1 | `df4d00f24f4165063715eb7890fb331bc55acc04a12c090173e02c354832ac0c` | baseline para comparar o iPogo |
| PVPPokeGo Source Phase1 | `922b08752549e380ca8f74654595da47e8223c8606a92eeca29abfb45eabdb42` | base editável atual |

## Descobertas confirmadas

### Poke Genie — altamente aproveitável como referência para PvPPokeGo

O APK declara `SYSTEM_ALERT_WINDOW` e foreground service de `MEDIA_PROJECTION`, confirmando um modelo externo de captura + overlay semelhante ao adotado pelo PvPPokeGo.

Classes/módulos encontrados diretamente no APK:

- `IVisionHelper`, `IVisionView` e `ACTION_STOP_IVISION`;
- `OcrHelper`;
- `OverlayMenuView`;
- `BattleTeamOverlayView`;
- `BattleSimulatorManager`;
- `SimulationBestCounterActivity`, `SimulationAllMovesActivity`, `BreakpointActivity`;
- módulo `PvPIV` com candidatos, chart e settings;
- `MovesetActivity` e referência de efetividade de tipos;
- módulos de histórico, rename, Pokédex e backup.

### iPogo — funções nativas confirmadas

A comparação com o Pokémon GO oficial da mesma versão mostrou que o iPogo mantém praticamente a mesma estrutura Java e concentra alterações importantes no binário nativo `libmain.so`.

Símbolos/strings exclusivos encontrados no `libmain.so` do iPogo:

- `setLocationNative`;
- `set_autoconnectvgp`;
- `set_shinyscanner`;
- `set_skip_animations`;
- `set_skip_team_leader`;
- `set_skip_team_rocket`;
- `set_fastmap`;
- `set_boostspawn`;
- `set_freezepokemon`;
- `set_spin`;
- `set_throw`;
- `set_arplus`;
- `set_challenge`;
- `release_pokemon`;
- `update_shinies`;
- `get_player_stats`;
- `individual_attack`, `individual_defense`, `individual_stamina`;
- string `excellent`;
- string `iPogo`.

Interpretação: existem hooks/bridges nativos para localização, captura/arremesso, mapa/spawn, shiny scanner, vGP, animações e rotinas de Team GO Rocket/Líder.

### Pokemod — arquitetura de launcher/injetor confirmada

O APK contém:

- permissão `ACCESS_SUPERUSER`;
- `SYSTEM_ALERT_WINDOW`, `MANAGE_EXTERNAL_STORAGE`, `QUERY_ALL_PACKAGES`, `REQUEST_INSTALL_PACKAGES`;
- `assets/pokemod-injector`;
- `assets/busybox-arm64`;
- `assets/sqlite3-arm64`;
- `assets/nostatusbar.zip`;
- `libconceal.so` e `libfancier.so`;
- serviço `com.pokemod.app.injector.HALService`;
- `DataReceiver`, `InjectionResult` e `VpgpTelemetryData`.

Strings de interface confirmam:

- Account Manager;
- troca rápida de contas;
- vPGP / Virtual Pokémon GO Plus;
- Injection Settings / Injection Type / Injection Time;
- Shiny Scanner notifications;
- Teleport;
- gravação/logs;
- atualização automática do Pokémon GO;
- Discord Rich Presence;
- configurações avançadas/root/SELinux.

Observação: o launcher não contém necessariamente todo o código das funções de gameplay; parte pode ser entregue/carregada na etapa de injeção.

## O que incorporar no PvPPokeGo

### Prioridade A — encaixa diretamente na arquitetura atual

1. **Pipeline de captura com fallback**
   - manter `MediaProjection` como principal;
   - adicionar modo screenshot/manual quando a captura contínua falhar;
   - diagnóstico claro de captura e permissão.

2. **OCR especializado por tela/ROI**
   - separar detector de nome/CP, detector de moveset, detector de energia/eventos e detector de reserva;
   - normalização OCR números↔letras para nomes/CP;
   - confiança por campo em vez de uma confiança global.

3. **Overlay modular**
   - menu pequeno independente do overlay passivo;
   - módulos minimizáveis: inimigo, golpes, energia, reservas, tipos e debug;
   - posição/escala persistentes.

4. **Battle Simulator local**
   - engine independente da captura;
   - comparar todos os movesets possíveis;
   - breakpoint/bulkpoint básico;
   - estimar dano/energia/turnos;
   - servir tanto para recomendação pré-batalha quanto para overlay.

5. **PvP IV / candidatos**
   - cálculo de rank por liga;
   - melhor evolução elegível;
   - nível/CP alvo;
   - custo de poeira/doces;
   - marcador de candidato PvP.

6. **Moveset reference**
   - lista de golpes possíveis por Pokémon;
   - legado/Elite quando aplicável;
   - energia, dano, duração e DPE/DPT;
   - type effectiveness local.

### Prioridade B — útil depois da detecção principal

7. **Histórico de batalha/diagnóstico**
   - registrar apenas eventos derivados da captura: troca detectada, fast contado, charged confirmado, confiança e correções manuais;
   - export JSON para depuração.

8. **Modo de correção rápida**
   - botões `+1/-1 Fast`;
   - seleção manual de Fast/Charged quando detector estiver inseguro;
   - botão para confirmar charged usado;
   - restauração do estado por inimigo ao retornar de uma troca.

9. **Perfis/layouts**
   - salvar tamanho, posição, opacidade e módulos visíveis;
   - perfil compacto para PvP e perfil detalhado para análise.

### Fora do núcleo PvPPokeGo

As funções abaixo pertencem ao projeto de recuperação/mod completo, não ao assistente visual PvP:

- alteração de localização/teleporte;
- injeção/root/SELinux/concealment;
- Virtual GO Plus interno;
- fast map/boost spawn/freeze Pokémon;
- skip de animações/Team Rocket/Líder via hook interno;
- modificação do arremesso interno (`set_throw`).

O PvPPokeGo deve continuar funcional sem modificar o processo do Pokémon GO.

## Mapa de reutilização

| Função/padrão | Poke Genie | iPogo | Pokemod | PvPPokeGo destino |
|---|---:|---:|---:|---|
| MediaProjection + overlay | confirmado | n/a | overlay launcher | **incorporar** |
| OCR de tela | confirmado | n/a | n/a | **incorporar** |
| IV/PvP rank | confirmado | stats nativas | n/a | **incorporar** |
| Battle simulator | confirmado | dados nativos | n/a | **incorporar** |
| Moveset/type chart | confirmado | dados nativos | n/a | **incorporar** |
| Shiny scanner | n/a | confirmado | notificações confirmadas | projeto recuperação |
| vPGP | n/a | `set_autoconnectvgp` | confirmado | projeto recuperação |
| Skip Rocket/Líder | n/a | confirmado | provável payload | projeto recuperação |
| Throw helper | n/a | `set_throw`, `excellent` | provável payload | projeto recuperação |
| Localização/teleporte | n/a | `setLocationNative` | Teleport UI | projeto recuperação |
| Account Manager | n/a | n/a | confirmado | projeto recuperação |
| Root/injetor | n/a | patch nativo | confirmado | **não incorporar no PvPPokeGo** |

## Próximas implementações recomendadas no PvPPokeGo

1. `CaptureHealthMonitor` + fallback screenshot.
2. `BattleEventDetector` separado do OCR de nome/CP.
3. `MoveCandidateResolver` com confiança e correção manual.
4. `BattleSimulator` puro Kotlin, testável fora do Android.
5. `PvPIvRankEngine` e tela de candidatos.
6. `BattleHistoryStore` + export de diagnóstico.
