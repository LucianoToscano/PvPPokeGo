# PvPPokeGo — proposta de Caixa-Preta e Diagnóstico Avançado (v0.5.32)

**Data:** 2026-10-09. **Natureza:** sugestão de evolução a pedido do usuário, após revisão dos exportadores e dos 3 backups 0.5.31. **Não implementado em código; nenhum teste ou build executado.** Não mudar APK, permissões, HUD, reconhecimentos nem arquivos salvos do usuário sem próximo passo autorizado.

## Referências e continuidade

- Guia Mestre canônico de release: https://github.com/LucianoToscano/PvPPokeGo/blob/release/0.5.29/docs/PROJECT_CONTINUITY_GUIDE.md
- Histórico: https://github.com/LucianoToscano/PvPPokeGo/blob/release/0.5.26/docs/PROJECT_CONTINUITY_GUIDE.md
- Feature de desenvolvimento: `feature/team-recognition-modes-0.5.32`.
- Caso real: [DIAGNOSTICO_EXPORTS_0.5.31_2026-10-09.md](DIAGNOSTICO_EXPORTS_0.5.31_2026-10-09.md).
- Android oficial: https://developer.android.com/studio/debug/bug-report?hl=pt-br ; https://developer.android.com/privacy-and-security/risks/log-info-disclosure?hl=pt-br

## Confirmado no código atual (não duplicar)

- `MainActivity.kt`, linhas 57–96: **Exportar diagnóstico (.zip)** já existe e gera `settings.json`, `battle_history.json`, `device.txt`, `README.txt` pelo Storage Access Framework. **Não** inclui Logcat completo, quadros OCR, relatórios ANR do SO, métricas de captura contínuas ou log de candidatos/rejeições.
- `MainActivity.kt`, linhas 98–135: **Exportar histórico (.zip)** gera `historico.json`, `historico-legivel.txt`, `historico.csv` e `README.txt`; continuar funcionando sem mudança de nomes/colunas.
- `BattleHistoryRepository.kt` já grava eventos de histórico e diagnósticos com `recordDiagnostic`. O objetivo é **adicionar um log técnico limitado de decisões e falhas**, não duplicar toda a persistência.
- `SettingsRepository.kt` já permite exportar/importar configuração e posições; NUNCA restaurar configuração automaticamente durante geração do relatório e não mudar posição de HUD.
- O 0.5.31 mostrou divergência `auto_recognition=false`, sessão vitória de 3,7s sem OCR, 4 sessões com `elapsedMs` fora de ordem, e trocas OCR repetidas que justificam uma trilha de auditoria.

## Limitações reais do Android

- Um app Android comum **não consegue capturar todo o Logcat do sistema/terceiros silenciosamente**. A permissão `READ_LOGS` não é destinada a apps comuns. Não tentar contornar proteção com root, Accessibility ou permissões indevidas.
- **Bug Report oficial do Android** pode ser criado pelo próprio usuário em `Opções do desenvolvedor → Criar relatório de bugs`, sem atualização do nosso APK; contém dados sensíveis de todo aparelho, deve ser revisado/limpo antes de compartilhar.
- Com um computador e Android SDK Platform Tools, `adb logcat -d -v threadtime > pvppokego-logcat.txt` obtém registro de depuração (pode incluir apps/sistema, dados sensíveis) e `adb bugreport` gera diagnóstico do dispositivo; são **procedimentos externos**, não botões de exportação que o PvPPokeGo possa executar sozinho.

## Proposta de implementação (com opt-in explícito)

### UI

Em `Configurações → Diagnóstico` (seção recolhível, **sem novo botão sobre HUD durante gameplay**):
- `Diagnóstico detalhado` — OFF por padrão; liga coleta em buffer circular; indicar impacto potencial e espaço máximo.
- `Marcar problema agora` — marca `incidentId`, timestamp monotônico, últimos 30–60 s e próximos 10–20 s de **eventos técnicos**, sem screenshot automático. Deve funcionar por botão no menu fora dos controles essenciais ou notificação de diagnóstico, sem interceptar toques do jogo.
- `Exportar diagnóstico básico (.zip)` — exportação existente preservada.
- `Exportar diagnóstico avançado (.zip)` — arquivos extras, dados anonimizados por padrão, tamanho e período informados, sem envio automático para servidores.
- `Anexar imagem do erro` — opcional, seleção manual e aprovação antes de incluir; **nenhuma gravação/screenshot do jogo, de apps terceiros, de notificações ou do teclado por padrão**.
- `Limpar registros técnicos` — opção com confirmação, **não apaga histórico de batalhas nem posições**.

### Formato do ZIP novo (compatível com anterior)

- `README.txt`: como interpretar, regras de confidencialidade, versão schema e limitações.
- `manifest.json`: versão app/build/hash git se disponível, sdk/modelo simplificado, `diagnosticSchemaVersion`, engine/GM snapshot version, sessão técnica UUID local aleatório, timezone/relógio, opções de coleta e lista SHA256 dos arquivos; sem IMEI, telefone, ID de conta ou identificador global.
- `settings-redacted.json`: apenas toggles e medidas necessários; configurações sensíveis mascaradas. **O ZIP básico antigo pode manter settings.json e não ser alterado sem política de migração.**
- `battle_history.json`: estrutura existente quando opção permitir, opcionalmente filtrado por partida selecionada.
- `events.ndjson`: registros ordenados por `seq` e `elapsedRealtimeNanos`, com `wallTimeUtc` informativo, `battleSessionId`, `incidentId`, `category`, `stateBefore`/ `stateAfter`, `decision`, `rejectReason`, `source`, `confidence`, `frameId`; nunca armazenar OCR bruto de texto livre/nomes pessoais por padrão.
- `ocr_trace.ndjson`: candidatos de espécies/formas, PC, caixa relativa, confidence, thresholds e rejeições; codificar nome de jogador por token removível; limitar número de candidatos e não guardar imagem do frame.
- `team_matcher_trace.ndjson`: origem MANUAL_DECLARED/AUTO_OBSERVED/INFERRED, conflitos CP vs visual, status de cada slot, razão para negar/confirmar troca e motivo de substituição do time.
- `capture_health.csv`: agregados por segundo/frame (FPS, frames recebidos/processados/descartados, atraso, dimensões, rotação, canal MediaProjection/Accessibility, timestamp do frame, status da permissão), sem pixel/imagem.
- `battle_timeline.csv`: golpes ágeis, carregados, escudos, energia estimada, HP aproximado e reinícios de estado; incluir fonte/confiança e distinguir `observado` de `calculado`; não duplicar o histórico verdadeiro.
- `exceptions.txt`: stack traces **apenas do próprio app** com saneamento e sem PII; erros não fatais, OOM/ANR inferidos, info de thread em caso de congelamento controlado; app não promete coletar ANR completo do SO.
- `integrity_warnings.json`: violações de invariantes, alvos e sequência sem tentar corrigir silenciosamente.
- `device-summary.json`: versão OS, permissões, saúde do serviço, escala/orientação/resolução, bateria apenas percentual quando relevante; evitar modelo/identificação excessiva por padrão.

### Guardas antibug (primeiro observar, corrigir somente após validação)

1. Nenhum conjunto de aliados ou inimigos da mesma partida pode conter **mais de três identidades simultaneamente confirmadas**. Uma quarta identidade não deve ser imediatamente esquecida: registrar conflito e evidência, investigar se houve união indevida de sessões.
2. Sequência/timestamps monotônicos não podem regredir; em alteração do relógio civil preferir `elapsedRealtimeNanos` + contador `seq` e ordenar eventos sem perder o tempo real.
3. Equipe manual **declarada** nunca vira automaticamente `OCR_CONFIRMADO`; o ativo deve ser selecionado por espécie/CP/evidência confiável. Não sobrescrever os slots manuais por scan incompleto.
4. Registrar frame obsoleto, captura preta, queda de permissão, interrupção do MediaProjection, divergência rotação/tamanho e atraso do detector.
5. Energia, escudos, HP e turnos devem obedecer limites da engine e recomeçar após transição confirmada de batalha; registrar discrepâncias sem inventar certeza.
6. Resultado Vitória/Derrota só após confirmação suficiente; sessão sem evidência deve permanecer `indeterminado`, não ser automaticamente classificada como perda.
7. Sistema trava/lenta: manter ring buffer pequeno e tarefas em background; nenhuma escrita em disco por frame na thread principal; exportar preferencialmente fora da batalha.

### Privacidade, desempenho e QA

- **Armazenamento somente local** e compartilhamento iniciado pelo usuário, sem coleta automática remota. Entrada com consentimento expresso para nível avançado; OFF por padrão. Não publicar dados brutos em repositório público ou Drive por padrão.
- Buffer circular com limites configuráveis (proposta inicial 5–10 MB e 15–30 minutos); coleta por amostragem e gatilho de incidentes; descarte automático; exportação SAF; check de espaço, Zip Slip, travamentos, integridade e schemas.
- Sanitizar nomes do treinador, IDs, contatos, IP, URLs/tokens, dados de outras apps, e textos OCR livres; avisar antes de compartilhar bugreport/logcat externo.
- QA com 7 sessões/702 eventos de referência: resultado em 3,7s sem OCR, sessões longas, retornos à seleção, mudança auto/manual, `auto_recognition=false`, trocas inconsistentes, ordem reversa e travamentos.
- Testes futuros: carga/limites de memória, regra de não logging de PII, merge incorreto, migração/backward-compat da exportação, ZIP canônico e corrupção parcial, Android 16/Samsung, múltiplas telas e versões.
- Considerar `adb bugreport` apenas **opção externa** para falhas de sistema/ANR; não alegar coleta de logs completos feita pelo APK.

## Critério de entrega

- Primeiro: revisão e concordância do escopo.
- Depois: implementar biblioteca interna de telemetria, exportação avançada e testes em branch isolada, mantendo botões atuais; **não iniciar compilação nem gerar APK sem autorização explícita**.
- Nenhuma mudança de código realizada nesta rodada. Documento de requisitos apenas.
