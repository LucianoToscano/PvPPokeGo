# PvPPokeGo — contexto mestre único

Atualizado em: 2026-09-26

## Regra de continuidade

A partir desta data, as decisões, ideias, progresso, builds e próximos passos de PvPPokeGo e PGLOT serão discutidos em um único tópico mestre de conversa. Os dois projetos continuam separados tecnicamente em seus repositórios, mas não devem mais depender de três chats paralelos para contexto.

Este documento serve como âncora técnica durável. Não armazena credenciais, tokens, dados privados de conta ou material sensível.

## Escopo PvPPokeGo

PvPPokeGo é o assistente visual externo para batalha PvP. Foco:
- captura/visão/OCR;
- reconhecimento de Pokémon e golpes;
- memória dos três adversários;
- energia e contagem de fast moves;
- charged move timing;
- PvPoke como fonte de dados/ranking;
- matchups, shield/switch/counter advice;
- overlay modular e não intrusivo;
- correção manual rápida;
- importação de coleção/battle parties via PGSStats;
- replay/diagnóstico local.

Ficam fora do PvPPokeGo: mapa/feed geográfico, spoofing, rotas/GPX, ninhos, farm, GO Plus/VGP e funções próprias do PGLOT.

## Decisões de UX já consolidadas

- Overlay deve poder ser ocultado manualmente.
- Deve existir modo compacto/bolha e modo somente alertas.
- Auto-hide quando não há informação útil.
- Ocultar a UI nunca apaga o estado da batalha.
- Golpes previstos pelo ranking devem ser visualmente diferentes dos golpes confirmados em batalha.
- Toque no fast/charged move deve abrir seletor apenas com golpes legais daquele Pokémon.
- Moveset inicial vem do top #1 de PvPoke quando disponível.
- OCR de banner de charged move deve converter nome localizado para move ID local.
- Primeiro charged observado substitui apenas uma previsão ainda não confirmada.
- Segundo charged diferente observado ocupa o outro slot; repetição do primeiro não deve sobrescrever nada.

## Core implementado / em evolução

Arquivos principais:
- `BattleEnergyTracker.kt`
- `BattleObservationProcessor.kt`
- `BattleTextEventParser.kt`
- `BattleConfidenceEngine.kt`
- `BattleAssistEngine.kt`
- `BattleReplayRecorder.kt`
- `BattleExperienceModels.kt`
- `BattleTrainingController.kt`

Conceitos implementados:
- `EvidenceLevel`: PREDICTED / PROBABLE / CONFIRMED;
- energia exata + `EnergyRangeEstimate`;
- autocorreção quando um charged observado prova que fast moves foram perdidos;
- confirmação persistente de charged moves;
- cálculo de readiness e double-charge respeitando o limite de 100 de energia;
- bait risk advisor;
- switch timer parametrizado por regra/copa;
- alert engine;
- overlay visibility policy;
- replay local normalizado;
- controlador de treino manual sem Pokémon GO aberto;
- perfis de regras/layout/debug.

## Próximas prioridades

1. validar todo o core com build/test runner;
2. integrar alertas/confiança/faixa de energia ao overlay Android;
3. implementar seletores touch-safe de fast/charged moves;
4. integrar switch timer ao reconhecimento de troca;
5. ligar replay/last action ao UI;
6. PGSStatsImporter + battle parties;
7. PvPIvRankEngine / MoveTiming / CounterAdvisor completos;
8. APK testável quando existir módulo Android/CI no repositório ativo.

## Regra de qualidade

Não considerar recurso pronto apenas porque existe em documentação. Para porcentagem do projeto, contar código implementado, integrado, compilado/testado e utilizável no APK.
