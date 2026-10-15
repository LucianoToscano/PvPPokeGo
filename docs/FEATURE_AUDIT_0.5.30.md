# PvPPokeGo 0.5.30 Hybrid QA — mapa verificável de funcionalidades

Data: 2026-10-15. Esta matriz separa código existente, testes automatizados e pendências externas. **Não indica lançamento de produção.** Versão estável `release/0.5.29` permanece intacta. Desenvolvimento canônico: `feature/hybrid-data-sync-0.5.29`; espelho público: `preview/hybrid-data-sync-20261015`.

| Área | Estado no código | Evidência e limites |
| --- | --- | --- |
| App Android nativo, captura e HUD | Implementado anteriormente | `MainActivity.kt`, `BattleOverlayService.kt`, `BattleOverlayView.kt`; Android 35 smoke 0.5.29 passou; teste em batalha real pendente |
| Reconhecimento aliado OCR + CP + visual | Implementado anteriormente | `BattleOcrDetector.kt`, `PokemonVisualRecognizer.kt`, fallback `AllyOcrRecoveryPolicy` e testes; Samsung real pendente |
| Liga Grande/Ultra/Mestra | Implementado anteriormente | `BattleLeague`, rankings offline 1500/2500/10000 |
| Reservas aliadas, correspondência e continuidade | Implementado anteriormente | `ReserveCardMatcher`, `BattleCorrespondenceTracker`, `BattleContinuityPolicy`, respectivos testes unitários; confirmação física pendente |
| Ataques, energia adversária/aliada, escudos e buffs | Implementado anteriormente | `BattleEngine`, `EnemyEnergyTracker`, `ShieldDecisionEngine`, `StatStageTracker`, múltiplos testes unitários |
| Dano projetado no HP / previsão carregada | Implementado anteriormente | `DamageForecastEngine`, `ObservedDamageTracker`, opção `showHpAssist` |
| Estratégia local / previsão, troca e CMP | Implementado anteriormente | `TacticalForecastEngine`, `CmpForecastEngine`, `BattleAssistAdvisor`, histórico; não é IA treinada online |
| Histórico detalhado / eventos / exportações | Implementado anteriormente | `BattleHistoryRepository.kt` e `MainActivity.kt`: CSV, JSON, TXT e diagnóstico ZIP |
| Exportação/importação de configurações e posições HUD | Implementado anteriormente | `SettingsRepository.exportBackupJson`, `importBackupJson`; agora inclui híbrido |
| Três slots do time adversário | Implementado na branch 0.5.30 | `OpponentTeamTracker.kt`; guarda slots 1–3 sem inventar identidade; preserva CP, HP, estado e chave de energia através das trocas; opção no HUD; testes JUnit novos |
| Dados offline-first e atualização diária | Implementado na branch híbrida | `HybridSnapshotStore`, `HybridSyncScheduler`, `GameDataRepository`; staging SHA-256 e ativação no próximo início |
| Wi-Fi real, atualização manual e indicação de base | Implementado na branch 0.5.30 | WorkManager, `SettingsScreen` com status, opção Wi-Fi e disparo de verificação manual |
| Backup do agendamento e diagnóstico de sincronização | Implementado na branch 0.5.30 | Campo `hybridSync` no backup JSON e `hybrid_sync.txt` no ZIP de diagnóstico |
| Feed público versionado e deduplicado | Validado em CI anterior | branch pública `hybrid-feed`, manifesto com 7 arquivos e SHA-256; SHA dos dados idêntico aos assets do APK anterior |
| APK 0.5.30 e ZIP de código editável | Em execução no CI público | Pipeline para gerar APK debug e ZIP com fontes, assets gerados, testes, Gradle Wrapper; só marcar aprovado após job real |
| Instalação/abertura Android 35 | Confirmado em 0.5.29 | Execução `38802898336` aprovada; CI 0.5.30 em andamento |
| Launcher transparente/ícones fielmente preservados | Recursos existentes conservados | Pipeline verifica recursos e atlas; aprovação estética em Samsung não pode ser presumida |
| Captura e reconhecimento durante batalha em Samsung | **PENDENTE EXTERNO** | Requer gravação ou acesso autorizado a dispositivo físico real; emulador não substitui |
| Mudança diária em rede Wi-Fi do telefone e retomada | **PENDENTE EXTERNO** | Requer teste de campo Wi-Fi/desligamento/reabertura/processo; CI testa compilação e algoritmo, não condições físicas |
| Assinatura independente do manifesto | **PENDENTE SEGURANÇA** | SHA-256 detecta corrupção mas não autentica o manifesto; precisa chave privada mantida em Secret seguro e public key pinned no app |
| APK definitivo com assinatura persistente | **PENDENTE CREDENCIAL** | Requer keystore permanente do proprietário fornecido de forma segura. APK debug assinado pelo CI não é release. Nunca versionar keystore em fonte pública |
| Treinamento remoto de IA por dados pessoais de jogadores | **NÃO IMPLEMENTADO** | Existem previsão e aprendizado estatístico local; servidor, política de consentimento, anonimização, métricas e infraestrutura de treinamento não foram fornecidos |
| Layout dos fundos e nova interface artística final | **PENDENTE ESPECIFICAÇÃO/QA** | Layout original 0.5.29 não foi substituído nesta branch; alterações de imagem aprovadas exigem os arquivos originais e validação visual |

## Ordem do próximo teste físico

1. Exportar configurações antes de trocar um APK assinado com chave debug diferente; não desinstalar sem backup.
2. Instalar APK QA, conferir permissões de overlay/captura e preservação visual do HUD.
3. Entrar na seleção pré-batalha: verificar reconhecimento de três aliados e forms/CP; depois trocas e retorno.
4. Confirmar 3 slots adversários, energia, HP, escudos, histórico e HUD sem encobrir controles nativos.
5. No Wi-Fi real, pedir verificação manual, fechar/reabrir app e confirmar versão ativa; repetir sem rede e com atualização rejeitada.
6. Conferir arquivo de diagnóstico exportado antes/depois dos testes e comparar com checklist canônico.

## Regra de divulgação

Não dizer que **TUDO** foi validado no aparelho se houve apenas build e emulator smoke. Documentar sempre os itens que exigem a instalação física e o fornecimento seguro de chaves. Versão 0.5.30 **QA/debug**, PR #37 permanece draft e branch estável não deve receber merge automático.
