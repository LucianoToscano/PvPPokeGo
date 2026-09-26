# Project Context Memory — PvPPokeGo

Atualizado em: 2026-09-26

## Objetivo

O PvPPokeGo é o projeto de assistência de batalha PvP. Ele concentra leitura visual, overlay, contagem de energia/golpes, análise de matchup, identificação de moveset, suporte a escudos e recomendações em batalha.

## Fontes analisadas

Referências funcionais/arquiteturais fornecidas pelo proprietário do projeto:

- Pokémon GO oficial 0.429.1 APKM;
- PGSharp 1.271 sem root e Android 8;
- `Pokémon GO_0.429.1 LOT.apk`;
- iPogo 237;
- Poke Genie 8.15.0;
- Pokemod 13.3.4;
- PGSRoot 1.0.15;
- PGTools 6.0.6 e PGTools TEST 6.0.6;
- Shungo 1.7.0;
- Data Hub 2.5.2;
- Pokecoord 1.2.2;
- Pokemon Coords WebAPK;
- dados PvPoke já adotados pelo projeto.

## Baseline oficial confirmado

Pokémon GO 0.429.1, package `com.nianticlabs.pokemongo`, versionCode `2026091602`, ARM64, Android 9+.

- APKM SHA-256: `af397f568a929d37e8c46d3cd9f6bdcf1a1a908bc18c4b85cebf17cf78251ad9`
- `base.apk`: `5c6cc61a6752c42f6114845b45cb6d4d2568dbb9a7eab34af290706a86f8d29e`
- split ARM64: `70364d9968ce14c4c5f450a99e76ca9f9491ba7732db7df2a5df540c58fd90c4`

O split ARM64 oficial contém 25 bibliotecas nativas. Comparando com PGSharp 1.271 e iPogo 237, 24 são byte-idênticas e apenas `libmain.so` muda:

- oficial: 6.696 bytes;
- PGSharp: 38.247.680 bytes;
- iPogo: 16.234.824 bytes.

O hash `df4d00f24f4165063715eb7890fb331bc55acc04a12c090173e02c354832ac0c` pertence ao PGSharp 1.271 sem root e ao arquivo LOT atual, que são byte-idênticos; não é o hash do Pokémon GO oficial.

## Descobertas que interessam ao PvPPokeGo

### Poke Genie — principal referência

- overlay Android;
- captura de tela/MediaProjection;
- OCR por regiões;
- Tesseract e modelos específicos de idioma/número;
- PvP IV e candidatos;
- banco de movesets;
- Battle Simulator/counters;
- fallback por screenshot;
- layouts de `battle_team_overlay` e `ivision_overlay`.

Aplicação: reproduzir a arquitetura com código próprio, sem copiar código/modelos proprietários.

### Data Hub / PvPoke

Data Hub confirmou organização de dados em `pokemons.json`, `gym_team.json`, `raidRatings.json` e `maxBattleRatings.json`. Usar apenas como referência de schema. O projeto mantém PvPoke/fonte própria atualizável como base principal de dados de batalha.

### PGTools

Aproveitar conceitos genéricos de filtros, stats, modelos de configuração e logs. Farm, teleporte, raid automation e Rocket automation não fazem parte do core PvP.

### PGSharp / iPogo

A comparação com o baseline oficial separou claramente funções adicionadas como `battleAssistant`, helpers de throw, Rocket, shiny scanner, fast map e VGP. Para o PvPPokeGo, aproveitar somente conceitos de observação, estado e assistência visual.

### Pokemod / PGSRoot / Shungo

Servem como referência de separação entre app/serviço/módulos. O PvPPokeGo permanece externo ao processo do Pokémon GO, sem root, injeção, concealment ou bypass.

## Pesquisa web/GitHub — 2026-09-26

### Referências PvP prioritárias

- `pvpoke/pvpoke`: fonte canônica e principal referência open source para battle simulation, rankings, team builder, matchups e dados PvP. Continuar como base de dados/conferência do projeto.
- `sikora-pawel/pvpoke`: fork orientado a disponibilizar dados/matrizes/SQLite para consumo móvel. Útil para estudar pipeline de dados offline e atualização, sem depender do site PvPoke em runtime.
- `cartolanofacundo/pokego-pvp`: referência extremamente próxima do objetivo do PvPPokeGo. Possui segunda tela de combate 3x3, carregamento de aliados/inimigos, força de ataques, turnos até charged move, recomendação de troca, dados PvPoke, persistência do time e rankeador de IV.
- O rankeador de `pokego-pvp` percorre as 4096 combinações de IV, calcula nível/CP/stat product, ranks por liga, evoluções/Megas, custos de Stardust/candy/XL e posição da espécie no PvPoke. Usar como referência de comportamento/testes para nosso `PvPIvRankEngine`.
- `mglerner/gopvpsim`: simulador PvP em Python baseado em PvPoke, com foco em breakpoints/bulkpoints, IV e movesets; excelente oracle de testes para um `BattleSimulator`/`BreakpointEngine` próprio em Kotlin.
- `Mygod/ohbem`: biblioteca otimizada para rank de IV PvP, com Little/Great/Ultra, níveis 50/51, evoluções/Megas, empates e functional perfect. Excelente referência algorítmica para `PvPIvRankEngine`.
- `pedrosancao/pokemon-go-pvp-rank`, `jgmcelwain/go-iv`, `Sigafoos/iv` e `DeathbyToast/PvP_IVs`: referências menores para validar resultados, UX e casos de borda de rank/IV.

### OCR e reconhecimento visual

- `ingimar-eyfjord/iv-pokemon-scanner`: referência atual para importar CSV/screenshot/vídeo, OCR de Pokémon, resolução de IV oculto por CP/HP/nível e classificação para PvP/raids/gyms. Relevante para pipeline de coleção e validação do OCR.
- `AlansApps/PGO-Scanner`: referência de scanner por screenshot/vídeo para espécie/forma/CP/IV. Útil para ROI, estabilização entre frames e extração visual.
- `PGSS`/`TheRaidMapper`: referências históricas de OCR de screenshots de raids; aproveitar apenas arquitetura de detecção/recorte/classificação.
- `Brohaffey/pokemon-GO-counter-finder`: gerador simples de strings de busca usando fraqueza/tipo, slots `@1/@2/@3` e favoritos. Pode originar um `StorageSearchGenerator` compartilhado com PGLOT.

### Dados e atualização

- `PokeMiners/game_masters`: referência prioritária para atualização automatizada de espécies, formas, moves e parâmetros do jogo; criar conversão própria e versionada para os modelos do core.
- `PokeMiners/pogo_assets`: pode auxiliar nomes/localizações/sprites durante desenvolvimento, respeitando direitos/licença dos assets. Não embutir conteúdo de terceiros sem autorização/licença adequada.
- `JP-study-list/poke-change`: boa referência de pipeline de rebuild de dex/formas/costumes a partir de PokeMiners e de import/export local.

### Referências analíticas auxiliares

- `psoren/pokemon-go-odds`: modelo estatístico de shiny/Hundo/Shundo com IV floors e fontes de encontro. Não entra no motor de batalha, mas pode fornecer ideias para analytics compartilhado no futuro.
- `EduHunterDev/PokemonGo-Calculadora-Stardust`: utilitário simples; apenas inspiração para calculadoras de custos/recompensas, baixa prioridade.

### O que permanece fora do core PvPPokeGo

Mesmo quando encontrados em Golbat/PoracleNG/ReactMap/PGSharp/iPogo/PGTools, ficam exclusivos do PGLOT: coordenadas, mapa global, ninhos, localização salva para teleporte, farm, hunter, Rocket automation, raid automation, GO Plus/VGP e scanner de mundo. O PvPPokeGo pode compartilhar somente modelos de dados e filtros genéricos.

## Arquitetura de visão incorporada

Novo componente: `core/src/main/kotlin/pvppokego/core/vision/VisionObservation.kt`.

Ele introduz:

- `ObservationSource`;
- `FieldObservation<T>` com confiança independente por campo;
- `BattleFrameObservation` para leitura bruta de um frame;
- `StableObservation<T>`;
- `StableObservationReducer<T>` para exigir confirmações entre frames antes de alterar o estado da batalha.

Pipeline alvo:

`captura -> ROI -> observações com confiança -> estabilização -> eventos de batalha -> BattleSession/BattleEnergyTracker -> decisão -> overlay`

## Componentes alvo derivados das novas referências

- `BattleSimulator`: simulação local desacoplada de Android/OCR, comparável contra PvPoke/gopvpsim.
- `BreakpointEngine`: dano, breakpoints, bulkpoints e cenários de IV.
- `PvPIvRankEngine`: 4096 combinações, CP cap, level cap, stat product, ranks empatados, evoluções/formas/Megas e IV floors configuráveis.
- `MoveTimingEngine`: energia do fast, custo do charged, número de fasts e turnos/tempo restante.
- `CounterAdvisor`: matchup/rating + tipos + movesets + energia/escudos.
- `CollectionVisionScanner`: reconhecimento de Pokémon/CP/HP/IV a partir de screenshot/vídeo, separado do detector de batalha ao vivo.
- `StorageSearchGenerator`: strings oficiais de busca por tipos/golpes/favoritos.
- suíte de testes cruzados usando resultados conhecidos de PvPoke, gopvpsim e bibliotecas open source como oracles, sem copiar implementação.

## Funções prioritárias

1. OCR/visão por ROI para identificar Pokémon e elementos da batalha.
2. Overlay modular e reposicionável.
3. Contagem de Fast Moves e energia por Pokémon inimigo.
4. Memória independente dos três Pokémon adversários.
5. Identificação/seleção dos Charged Moves possíveis.
6. Quantidade de Fast Moves restante para cada Charged Move.
7. Recomendação de shield baseada em ameaça, tipo, energia e HP.
8. Recomendação do melhor Charged Move entre os dois botões.
9. Matchup, fraquezas, resistências e tipos favoráveis.
10. Simulador/counter engine local.
11. PvP IV rank engine e candidatos.
12. Logs/replay/debug de reconhecimento.
13. Correção manual rápida de detecções.
14. Perfis de ROI/layout por resolução.
15. Breakpoint/bulkpoint engine e validação cruzada.
16. Scanner de coleção por screenshot/vídeo como módulo separado.

## Regra de implementação

- Reimplementar comportamento e UX com código próprio.
- Não copiar bytecode, DEX, SO, JAR, modelos OCR ou bancos proprietários de terceiros para distribuição.
- Não remover licenças/premium.
- Não incorporar concealment, anti-detection ou bypass de anti-cheat.
- Manter o core de batalha separado de Android/OCR/gestos para ser testável.
- Para código open source, conferir licença individual e manter atribuições/avisos exigidos; quando possível, usar projetos externos como oracles de teste e portar a lógica de forma independente.

## Relação com PGLOT

Podem ser compartilhados somente componentes próprios/genéricos: dados Pokémon/tipos/moves, PvP IV, efetividade, battle engine, filtros, OCR/vision, logs/stats, notificações, modelos de configuração e `StorageSearchGenerator`.

Localização, mapa, Virtual GO Plus, farm, hunter por coordenadas, root e automações de gameplay permanecem isolados no PGLOT.
