# PvPPokeGo — contexto mestre único

Atualizado em: 2026-09-26

## Regra de continuidade

A partir desta data, as decisões, ideias, progresso, builds e próximos passos de PvPPokeGo e PGLOT serão discutidos em um único tópico mestre de conversa. Os dois projetos continuam separados tecnicamente em seus repositórios, mas não devem mais depender de três chats paralelos para contexto.

Este documento serve como âncora técnica durável. Não armazena credenciais, tokens, dados privados de conta ou material sensível.

## Migração dos chats antigos

O conteúdo recuperado dos tópicos **“APK pronto para baixar”** e **“Funções Para Dois APKs”** que pertence ao PvPPokeGo foi consolidado integralmente em:

- `docs/CHAT_MIGRATION_APK_PRONTO_E_FUNCOES_DOIS_APKS_2026-09-26.md`

Esse documento preserva:
- análise do APK-base histórico;
- arquitetura MediaProjection/OCR/overlay;
- especificação visual e controles `▼`;
- moveset PvPoke #1 + confirmação automática por OCR;
- memória dos três inimigos;
- energia residual/faixa/autocorreção/double-charge;
- Confidence Engine;
- Battle Brain, bait, shield/switch advice e timers;
- replay/treino/PGSStats;
- achados Poke Genie/PGSharp/iPogo/Pokemod/PGTools/Shungo;
- material Rocket/Raid/Max separado do core PvP;
- roadmap histórico F1–F10;
- implementado vs pendente;
- separação explícita do que pertence ao PGLOT.

A partir de agora não é necessário consultar os dois chats antigos para continuar o desenvolvimento.

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


<!-- PVPPG-CHAT-VISUAL-20261122 -->
## Consolidação de dados e referências visuais — 22/11/2026

Fontes públicas, pacotes ZIP, modelos, GIFs, auditorias, testes offline e proposta de Biblioteca Visual estão registrados em [CONSOLIDACAO_CHAT_BIBLIOTECA_VISUAL_DADOS_20261122.md](CONSOLIDACAO_CHAT_BIBLIOTECA_VISUAL_DADOS_20261122.md). No `pogo_pkm.json` fornecido, dos 1.755 registros há 1.451 pares `(id, form)`, **24 grupos divergentes** e 1.427 chaves sem conflito. Não usar variantes divergentes em cálculos PvP sem revisão de proveniência; `latest.json` tem 18.812 templates, mas snapshot datado de 28/08/2026. Separar dados de espécies/ataques de assets visuais e dados pessoais; testar regressão de Battle Engine, IVs e matchups antes da integração. O reconciliador offline só foi testado fora do repositório; não houve build, homologação em aparelho ou release PvPPokeGo.
