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

## Regra de implementação

- Reimplementar comportamento e UX com código próprio.
- Não copiar bytecode, DEX, SO, JAR, modelos OCR ou bancos proprietários de terceiros para distribuição.
- Não remover licenças/premium.
- Não incorporar concealment, anti-detection ou bypass de anti-cheat.
- Manter o core de batalha separado de Android/OCR/gestos para ser testável.

## Relação com PGLOT

Podem ser compartilhados somente componentes próprios/genéricos: dados Pokémon/tipos/moves, PvP IV, efetividade, battle engine, filtros, OCR/vision, logs/stats, notificações e modelos de configuração.

Localização, mapa, Virtual GO Plus, farm, hunter por coordenadas, root e automações de gameplay permanecem isolados no PGLOT.
