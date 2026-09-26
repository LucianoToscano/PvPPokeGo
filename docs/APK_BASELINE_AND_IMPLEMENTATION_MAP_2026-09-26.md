# Baseline oficial e mapa de implementação — PvPPokeGo

Data: 2026-09-26

## Objetivo

Separar recursos do Pokémon GO oficial das funções adicionadas pelos APKs de referência e transformar somente os conceitos úteis ao PvPPokeGo em componentes próprios, externos e testáveis.

## Baseline oficial confirmado

Pacote oficial recebido: `com.nianticlabs.pokemongo_0.429.1-2026091602_1arch_a062dcee45ae577b522007ccc2bae93f_apkmirror.com.apkm`

- package: `com.nianticlabs.pokemongo`
- versão: `0.429.1`
- versionCode: `2026091602`
- ABI: `arm64-v8a`
- min API: 28
- APKM SHA-256: `af397f568a929d37e8c46d3cd9f6bdcf1a1a908bc18c4b85cebf17cf78251ad9`
- `base.apk` SHA-256: `5c6cc61a6752c42f6114845b45cb6d4d2568dbb9a7eab34af290706a86f8d29e`
- `split_config.arm64_v8a.apk` SHA-256: `70364d9968ce14c4c5f450a99e76ca9f9491ba7732db7df2a5df540c58fd90c4`

O split ARM64 oficial possui 25 bibliotecas nativas. Contra PGSharp 1.271 sem root e iPogo 237, 24 são byte-idênticas; apenas `libmain.so` difere.

| Build | `libmain.so` |
|---|---:|
| Pokémon GO oficial | 6.696 bytes |
| PGSharp 1.271 | 38.247.680 bytes |
| iPogo 237 | 16.234.824 bytes |

Isso ajuda a separar recursos do jogo base de hooks/bridges adicionados por terceiros.

## Correção importante

O SHA `df4d00f24f4165063715eb7890fb331bc55acc04a12c090173e02c354832ac0c` pertence ao PGSharp 1.271 sem root e ao arquivo `Pokémon GO_0.429.1 LOT.apk`, que são byte-idênticos. Ele não é o hash do Pokémon GO oficial.

## Referências úteis para o PvPPokeGo

### Poke Genie — referência principal

Confirmado no APK:

- Tesseract OCR (`libtess.so`) e modelos de idioma/números;
- bancos `move_list.txt`, `move_pvp_data.txt`, `move_stats.txt` e `moveset_ranking_genie_v3.txt`;
- layouts de `battle_team_overlay`, `ivision_overlay`, PvP IV e Battle Simulator;
- estrutura clara de moveset, raid, PvP IV e análise visual.

Aplicação no PvPPokeGo:

- captura de tela/MediaProjection;
- OCR por ROI e por tipo de campo;
- confiança independente por leitura;
- overlay modular;
- identificação de Pokémon/moves;
- simulador/counter engine;
- PvP IV e ranking;
- fallback por screenshot/manual.

### Data Hub

Assets confirmados:

- `pokemons.json`;
- `gym_team.json`;
- `raidRatings.json`;
- `maxBattleRatings.json`.

Aplicação: referência de schema e organização de dados. O PvPPokeGo deve preferir fontes próprias/atualizáveis, como o pipeline PvPoke já adotado.

### PGTools

Aplicação indireta:

- modelos de filtros;
- configurações por módulo;
- stats/logs;
- regras e prioridades;
- diagnóstico de sessões.

Farm, teleporte e execução automática permanecem fora do core PvP.

### PGSharp / iPogo

O baseline oficial confirmou que strings como `battleAssistant`, `enhancedThrowExcellent`, `instantRocket`, `set_shinyscanner`, `set_skip_team_rocket`, `set_fastmap` e `set_autoconnectvgp` pertencem a extensões/mods e não ao jogo oficial. Para o PvPPokeGo, usar apenas conceitos de leitura, estado de batalha e assistência visual.

### Pokemod / PGSRoot / Shungo

Servem somente como referência de separação entre app, serviço e módulos privilegiados. O PvPPokeGo deve continuar externo ao processo do Pokémon GO e não depender de root, injeção, concealment ou bypass.

## Componentes a implementar

### P0

1. `BattleFrameObservation`: resultado bruto de um frame, com confiança por campo.
2. `StableObservationReducer`: estabiliza leituras entre frames antes de alterar o estado da batalha.
3. ROI específica para nome/slot/move/energia/eventos.
4. detector de troca e charged move como eventos separados do OCR textual.
5. overlay modular que recebe apenas estado estabilizado.
6. logs de observações e correções manuais.

### P1

7. `MoveCandidateResolver` com candidatos possíveis + confiança.
8. integração com `BattleEnergyTracker` para estimar energia e fast restantes.
9. `ShieldAdvisor` e recomendação de charged move.
10. `BattleSimulator` puro Kotlin.
11. PvP IV rank engine e candidatos.

### P2

12. replay/debug de sessão a partir de JSON de observações.
13. perfis de layout/ROI por resolução e densidade.
14. calibração assistida de ROI.

## Regra de arquitetura

O pipeline deve ser:

`captura -> ROI -> observações com confiança -> estabilização -> eventos de batalha -> BattleSession/BattleEnergyTracker -> decisão -> overlay`

Nenhum detector deve alterar diretamente o estado de batalha. Isso permite testar OCR/visão, tracking e decisão separadamente.

## Limites clean-room

Não copiar código, DEX, SO, modelos proprietários ou bancos proprietários dos APKs analisados para distribuição. Os arquivos servem como referência funcional e arquitetural. Não remover licenças/premium e não incorporar root/injeção/concealment/anti-detection/bypass.
