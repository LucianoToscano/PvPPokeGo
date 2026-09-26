# Auditoria cruzada de APKs — atualizada em 2026-09-26

Objetivo: registrar funções e padrões arquiteturais encontrados nos APKs fornecidos e decidir o que pode ser reimplementado de forma própria no PvPPokeGo.

> Regra do projeto: usar os APKs como referência comportamental/arquitetural. Não copiar código proprietário, não remover licenças/premium e não implementar ocultação/bypass de anti-cheat.

## Baseline oficial correto

Pokémon GO oficial 0.429.1 foi recebido em APKM, package `com.nianticlabs.pokemongo`, versionCode `2026091602`, ARM64, Android 9+.

- APKM SHA-256: `af397f568a929d37e8c46d3cd9f6bdcf1a1a908bc18c4b85cebf17cf78251ad9`
- `base.apk`: `5c6cc61a6752c42f6114845b45cb6d4d2568dbb9a7eab34af290706a86f8d29e`
- split ARM64: `70364d9968ce14c4c5f450a99e76ca9f9491ba7732db7df2a5df540c58fd90c4`

Correção: o SHA `df4d00f24f4165063715eb7890fb331bc55acc04a12c090173e02c354832ac0c` pertence ao PGSharp 1.271 sem root e ao arquivo LOT atual, que são byte-idênticos. Não é o hash do Pokémon GO oficial.

## Comparação nativa

O split ARM64 oficial possui 25 bibliotecas. Comparando com PGSharp 1.271 e iPogo 237, 24 são byte-idênticas e somente `libmain.so` difere.

| Build | `libmain.so` |
|---|---:|
| Pokémon GO oficial | 6.696 bytes |
| PGSharp 1.271 | 38.247.680 bytes |
| iPogo 237 | 16.234.824 bytes |

Isso permite separar melhor funções oficiais de extensões adicionadas por mods.

## Descobertas relevantes

### Poke Genie — principal referência para PvPPokeGo

Confirmados no APK:

- Tesseract OCR (`libtess.so`);
- modelos OCR para texto e números;
- `move_list.txt`, `move_pvp_data.txt`, `move_stats.txt`, `moveset_ranking_genie_v3.txt`;
- layouts de `ivision_overlay`, `battle_team_overlay`, PvP IV e Battle Simulator;
- estrutura de movesets, counters, raids e análise visual.

Uso no projeto: captura/MediaProjection, OCR por ROI, confiança por campo, overlay modular, simulador, PvP IV, movesets e fallback por screenshot, todos por implementação própria.

### PGSharp / iPogo

A comparação com o oficial confirma que recursos como `battleAssistant`, `enhancedThrowExcellent`, `instantRocket`, `set_shinyscanner`, `set_fastmap`, `set_skip_team_rocket` e `set_autoconnectvgp` pertencem às extensões/mods, não ao jogo base.

No PvPPokeGo, aproveitar apenas conceitos de observação, estado e assistência visual. Localização, VGP, farm e automação invasiva ficam fora do core.

### PGTools

Confirmou arquitetura modular de regras/filtros/stats. Aproveitar somente componentes genéricos: configuração, filtros, logging e modelos de estado.

### Data Hub

Confirmados `pokemons.json`, `gym_team.json`, `raidRatings.json` e `maxBattleRatings.json`. Usar como referência de schema, não como dependência binária.

### Pokemod / PGSRoot / Shungo

Servem como referência de separação entre app/serviço/módulos privilegiados. O PvPPokeGo permanece externo ao processo do jogo, sem root, injeção ou concealment.

## O que incorporar

### Prioridade A

1. pipeline de captura com fallback;
2. OCR por ROI e por tipo de campo;
3. confiança independente por leitura;
4. estabilização multi-frame;
5. overlay modular;
6. Battle Simulator local;
7. PvP IV/rank;
8. moveset/type reference;
9. histórico/debug;
10. correção manual rápida.

### Implementação já iniciada

`core/src/main/kotlin/pvppokego/core/vision/VisionObservation.kt` adiciona:

- `FieldObservation<T>`;
- `BattleFrameObservation`;
- `StableObservation<T>`;
- `StableObservationReducer<T>`.

O objetivo é impedir que uma única leitura OCR ruim altere imediatamente o estado da batalha.

## Fora do núcleo PvPPokeGo

- teleporte/localização;
- injeção/root/concealment;
- Virtual GO Plus interno;
- farm/hunt;
- fast map/boost spawn/freeze;
- skip de animações/Rocket por hook;
- modificação interna de arremesso.

## Pipeline alvo

`captura -> ROI -> observações com confiança -> estabilização -> eventos -> BattleSession/BattleEnergyTracker -> decisão -> overlay`

Mais detalhes em `docs/APK_BASELINE_AND_IMPLEMENTATION_MAP_2026-09-26.md`.
