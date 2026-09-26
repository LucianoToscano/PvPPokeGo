# PvPPokeGo — Handoff completo para continuar em outra conta

Atualizado: 2026-09-26
Branch ativa: `android/apk-v02`
Repositório: `LucianoToscano/PvPPokeGo`

Este arquivo existe para que outro desenvolvedor, outra conta do ChatGPT ou outro agente consiga continuar o projeto sem depender da memória de uma conversa específica.

## 1. Objetivo e limite arquitetural

PvPPokeGo é um assistente Android EXTERNO para PvP do Pokémon GO.

Regras permanentes:
- não modificar o APK do Pokémon GO;
- não injetar código no processo do jogo;
- não usar root, concealment ou bypass;
- não ler memória interna do processo do jogo;
- usar `MediaProjection` + OCR/visão + dados locais;
- overlay via `WindowManager`;
- durante a batalha, usar dados empacotados localmente, sem depender de rede.

## 2. Fontes de dados escolhidas

### PvPoke
Usado como fonte atualizável para:
- espécies/formas;
- tipos;
- fast moves e charged moves;
- energia gerada/custo;
- turns/cooldown;
- rankings/movesets para Grande, Ultra e Mestre.

Script: `tools/update_pvpoke_data.py`

O moveset inicial do adversário deve usar o ranking #1 da liga selecionada. A realidade observada em batalha tem prioridade sobre esse default.

### Pokémon GO text resources / PokeMiners
Usado em release/build-time para gerar aliases completos de nomes localizados de golpes. Isso permite converter banners como `Plusle usou Relâmpago!` para o `moveId` usado pelo core/PvPoke.

Script: `tools/update_move_localization.py`

### Ícones de tipo
Base visual/licenciada: `partywhale/pokemon-type-icons` (MIT).

Script: `tools/sync_type_icons.py`

O script gera:
- `type_icons/`: ícone circular completo;
- `type_symbols/`: mesmo símbolo sem o círculo de fundo, para o círculo de carregamento ser desenhado pelo app.

Referência visual adicional fornecida pelo usuário: pacote Sketch/ZIP de ícones de tipos de Daniel Motta. Usar como referência de proporção/estética, mas os assets distribuídos devem permanecer os SVGs com licença clara enquanto não houver autorização explícita para redistribuir o outro pacote.

## 3. Liga selecionada no lobby

O app deve trabalhar com:
- Grande: 1500 CP;
- Ultra: 2500 CP;
- Mestra: sem cap prático no dataset (`10000`).

Arquivo: `app/.../BattleLeague.kt`

Comportamento:
1. modo padrão `AUTO` tenta ler a liga por OCR no lobby;
2. se detectar, cria a `BattleSession` com o CP cap correto;
3. se não detectar, overlay mostra `Liga? ▼` e o usuário escolhe;
4. MainActivity também tem seleção manual;
5. o rank #1/moveset padrão depende da liga selecionada.

## 4. Time do usuário antes da batalha

Quando o OCR encontra o lobby e seus Pokémon selecionados, `OverlayService.registerLobbyTeam()` tenta registrar até três espécies na `BattleSession`.

Objetivo final: sempre conhecer os três Pokémon do usuário antes da batalha para tipo, matchup, sugestão de troca e decisão de charged move.

## 5. Memória dos três Pokémon adversários

`BattleEnergyTracker` mantém estado independente para três slots.

Regras:
- trocar NÃO zera energia;
- retornar ao campo recupera energia/contagem anterior;
- desmaiar encerra o slot;
- nova batalha zera a sessão;
- cada inimigo mantém fast move, charged moves, energia, contagem e golpes já observados.

## 6. Contador de energia e bug do zero

Regra implementada:
- fast move detectado => soma `energyGain`;
- contador de cada charged = energia faltante / energia do fast move;
- quando charged é usado, subtrair SOMENTE o `energyCost`;
- preservar energia residual;
- recalcular imediatamente.

Exemplo: se havia 64 e o golpe custa 40, depois do golpe ficam 24. O contador NÃO pode permanecer travado em `0`.

## 7. Identificação automática de charged moves reais

Nunca assumir que só existe `Relâmpago`. O parser deve aceitar QUALQUER charged move legal daquele Pokémon.

Fluxo:
1. OCR extrai linha `X usou Y!` / `X used Y!`;
2. `BattleTextEventParser` resolve `Y` para um `moveId` local;
3. `BattleObservationProcessor` deduplica o mesmo banner em frames consecutivos;
4. `BattleEnergyTracker.onChargedMove()` gasta a energia e atualiza os dois slots de charged.

### Regra de descoberta progressiva dos dois charged moves

No início:
- usar os dois charged moves do rank #1 do PvPoke para a liga.

Quando um golpe real é observado:
- se já está entre os defaults, marcar como observado/verificado;
- se é diferente, substituir um slot que NÃO foi escolhido manualmente e ainda NÃO foi verificado.

Quando um segundo golpe diferente é observado:
- preservar o primeiro golpe já verificado;
- substituir o outro slot ainda não verificado.

Assim os dois botões convergem para os dois golpes reais do adversário.

Manual tem prioridade:
- se o usuário escolheu um botão manualmente, OCR não sobrescreve esse botão;
- long-press/`Automático (PvPoke/OCR)` libera o botão de volta para automação.

O estado usa `observedChargedMoveIds` e `manualChargedMoveButtonIndexes`.

## 8. IDs dos golpes

Como o app é externo, ele NÃO lê diretamente os IDs internos do processo do Pokémon GO.

O caminho permitido/implementado é:
- charged move: OCR do nome mostrado pelo próprio jogo -> tabela de localização -> `moveId` PvPoke;
- fast move: default rank #1 + seleção manual; a camada visual/motion conta ocorrências. Inferência automática de qual fast move está sendo usado por timing/visual pode ser melhorada depois, mas não deve depender de injeção ou leitura de memória do jogo.

## 9. Submenus ao tocar nos ícones

Arquivo: `OverlayService.kt` + `BattleOverlayViews.kt`.

Comportamento:
- tocar no fast move abre lista de todos os fast moves legais da espécie;
- tocar em charged esquerdo/direito abre lista de todos os charged moves legais;
- mostra energia do golpe no submenu;
- seleção manual fica preservada;
- opção `Automático (PvPoke/OCR)` devolve o slot ao controle automático.

## 10. Visual de carregamento

`ChargeProgressCircle` desenha:
- círculo cinza quando vazio;
- preenchimento gradual de baixo para cima com a cor do tipo;
- símbolo branco do tipo por cima;
- badge numérico com quantidade de fast moves restantes;
- `0`/ready quando atingiu energia suficiente.

O preenchimento é calculado por `currentEnergy / energyCost`, limitado a 100%.

## 11. Posição/layout decididos

Problemas anteriores:
- ícones de fraqueza no topo estavam soltos/desalinhados;
- painel de ataques chegava a ficar em cima do treinador humano adversário.

Nova regra:
- `weaknessBar`: `TOP|END`, logo abaixo do HUD nativo do adversário (`~8%` da altura), alinhado no topo e perto dos elementos originais;
- painel de ataques: `TOP|START`, deslocado para a região centro-esquerda (`~30%` da largura e `~18.5%` da altura), deixando espaço visível para o treinador inimigo à direita;
- usar proporções de tela, não coordenadas absolutas de um único aparelho.

Essas porcentagens são um baseline e devem ser calibradas com screenshots reais de diferentes resoluções.

## 12. OCR e captura

`ScreenCaptureAnalyzer.kt`:
- MediaProjection;
- ML Kit Text Recognition;
- OCR do header superior do adversário;
- extração separada de linhas que contenham `usou/used`;
- amostragem visual da região do adversário para pulsos de movimento/fast move.

## 13. Arquivos Android principais

- `app/src/main/AndroidManifest.xml`
- `app/src/main/kotlin/com/lucianotoscano/pvppokego/MainActivity.kt`
- `.../OverlayService.kt`
- `.../ScreenCaptureAnalyzer.kt`
- `.../BattleOverlayViews.kt`
- `.../TypeIconView.kt`
- `.../BattleLeague.kt`
- `.../LocalBattleData.kt`

## 14. Core principal

- `core/.../BattleEnergyTracker.kt`
- `core/.../BattleObservationProcessor.kt`
- `core/.../BattleSession.kt`
- `core/.../BattleDecisionEngine.kt`
- `core/.../BattleAutomationController.kt`
- `core/.../TypeMatchupAdvisor.kt`
- `core/.../BattleTextEventParser.kt`
- `core/.../PvpokeRepository.kt`

## 15. Build do APK

Workflow: `.github/workflows/build-apk.yml`

O CI:
1. baixa PvPoke e gera TSV local;
2. gera aliases pt-BR completos;
3. sincroniza os 18 tipos;
4. roda testes do core;
5. compila `:app:assembleDebug`;
6. publica artifact `PvPPokeGo-V02-debug` com APK e SHA-256.

## 16. Coisas a validar no aparelho

Prioridade de teste manual:
1. posição do `weaknessBar` em diferentes telas;
2. distância do painel de golpes para o treinador inimigo;
3. clique/submenu dos três ícones de golpes;
4. OCR de `X usou Y!` sem escudo e com escudo;
5. primeiro charged observado atualiza um slot;
6. segundo charged diferente atualiza o OUTRO slot;
7. contador sai de 0 depois do charged;
8. energia residual correta;
9. memória após troca/retorno;
10. detecção Grande/Ultra/Mestra no lobby;
11. reconhecimento dos três Pokémon próprios no lobby;
12. ajustar thresholds de motion para fast moves sem duplicar animações.

## 17. Prompt recomendado para outra conta ChatGPT

Cole este texto na outra conta após conectar o GitHub:

> Estou continuando o projeto Android `LucianoToscano/PvPPokeGo`. Use a branch `android/apk-v02`. Antes de alterar qualquer coisa, leia `docs/PROJECT_CONTINUATION_HANDOFF.md`, `docs/PROJECT_CONTEXT_MEMORY.md`, `README.md` e os testes do core. Preserve todas as decisões existentes. O app deve continuar como overlay externo, sem injeção/root/modificação do Pokémon GO. Continue a partir do estado atual, valide o GitHub Actions `Build PvPPokeGo APK`, corrija qualquer falha de CI e mantenha o handoff atualizado ao terminar cada mudança importante.

## 18. Regra de colaboração

A fonte da verdade compartilhável entre contas é o GitHub, especialmente este arquivo, os testes e os commits. Não depender de memória privada de uma conta do ChatGPT. Sempre atualizar este handoff quando uma regra de produto/arquitetura mudar.
