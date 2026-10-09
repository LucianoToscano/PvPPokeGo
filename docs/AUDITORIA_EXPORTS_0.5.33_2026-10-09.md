# PvPPokeGo 0.5.33 — Auditoria dos arquivos reais de configuração, diagnóstico, histórico e APK
**Data 2026-10-09. Natureza:** análise estática, sem compilar ou modificar o APK/código. Usuário forneceu quatro anexos; nenhum deles, nem dados privados, foram copiados ao repositório. O relatório Markdown integral também foi disponibilizado como arquivo nesta conversa.

## Cadeia de evidência
- `PvPPokeGo-0.5.33-config.json`, SHA-256 `0ad7cfbcce150529aa949b7bba487ce0686f4bd407a50bdd6150c03f69f119d2`, JSON válido, **byte a byte igual** a `settings.json` do diagnóstico.
- `PvPPokeGo-0.5.33-diagnostico.zip`, SHA-256 `524786e2d77e44db3314940ebb899953c4c0f569584e0929979b2fadab0d2873`; ZIP válido, 7 entradas: settings, history, runtime-events, logcat-app, integrity-warnings, device e README.
- `PvPPokeGo-0.5.33-historico.zip`, SHA-256 `b0ca7a6f0d873da796eac082a64d50b17cdffa56c25c4448db5be951624245d6`; ZIP válido, 4 entradas; `historico.json` byte a byte idêntico a `battle_history.json`; `historico.csv` tem exatamente 464 linhas / 7 IDs. Não publicar os anexos brutos no GitHub, pois têm configurações pessoais, times e metadados de dispositivo.
- `PvPPokeGo_0.5.33.apk`, SHA-256 `9de20e578d02e56dc11652d0b9f639293b99852cfa123f15aa600d0a0a6eb1d8`; ZIP íntegro, 532 entradas, contém `assets/gamemaster.json`, `assets/pvpoke_cpms.json`, `assets/rankings-1500.json`, fingerprints/atlas e ML Kit OCR. **Não foi instalado nem executado e não se verificou assinatura APK/manifesto binário neste lote.**
- Origem do diagnóstico: Samsung Android 16 (SDK 36), resolução 1080×2340 e 420 dpi. Preservar privado os dados específicos do usuário.

## Métricas verificadas (não estimativas)
- **7 sessões e 464 eventos**: 141 estados próprios, 141 estados adversários, 84 decisões de assistente, 38 diagnósticos, 20 carregados inimigos, 11 eventos rápidos próprios (23 usos inferidos), **0 rápidos inimigos explícitos**, 7 trocas próprias, 6 trocas adversárias, 2 carregados próprios, 7 inícios e 7 fins.
- **Todos os 7 `result=null`**. Não computar como vitórias/derrotas nem alimentar Elo/ranking/medalhas; informação insuficiente.
- Términos: 4 por `Sem evidência de batalha por 28s`, 2 por `Nova partida confirmada por identidade visual/OCR`, 1 por `Encerramento solicitado`.
- **1 inversão temporal** na sessão mais antiga (índice sétimo da exportação): `elapsedMs 211911 -> 209790`, diferença **-2121 ms**. `integrity-warnings.txt` confirma a sessão com inversão, sem provar causa OCR.
- **14 de 20 ataques carregados inimigos (70%)** são `Ataque carregado (não identificado)`, `source=energia/faixa`: são inferidos e **não devem ser exibidos como golpes observados por OCR**. 6 com nome e `source=OCR`, ainda sem validação visual externa.
- **Todos os 282 snapshots de estado** (141 VOCÊ + 141 INIMIGO) têm `shields=2`; isso não prova ausência de escudos no jogo, mas o histórico **não registra sua redução**. Importante para aconselhamento sobre escudo e HP.
- Uma sessão de 39,5s tem player e opponent null e encerra por falta de evidência. Outra de ~192,5s registra poucos eventos discretos em relação à duração; investigar falso início/stale, sem diagnóstico definitivo.
- `logcat-app.txt` tem 1 linha referente à exportação e **nenhuma exceção registrada**; não atesta ausência de crash. `runtime-events.log` contém 7 `session|started` e 4 `session|ended`; faltam breadcrumbs de encerramento para outras vias, embora histórico tenha `PARTIDA/FIM` em todas.

## BUG P0 reproduzível por regra matemática: espécie + PC inválidos
O histórico menciona identidades **Wingull CP 1485** e **Vanillite CP 1485** como aliados. O próprio `gamemaster.json` do APK contém base stats Wingull `(106/61/120)` e Vanillite `(118/106/113)`. Aplicando IV 15/15/15 e `cpms[100]=0.845300018787384` (nível 51) à fórmula `floor((ATK+15)*sqrt(DEF+15)*sqrt(HP+15)*CPM^2/10)`: **Wingull PC máximo 875**, **Vanillite máximo 1182**. Logo, os pares nome+CP registrados são fisicamente impossíveis. O backup do mesmo lote inclui **Cramorant CP 1485**, espécie capaz de atingir esse PC (máximo nível 51 ~2450), tornando plausível uma confusão entre sprite/nome/CP ou persistência indevida de CP no switch; **não considerar a causa provada sem o quadro original**.

Recomendação: adicionar `SpeciesCpPlausibilityPolicy` que compare CP detectado com limites da espécie/forma/nivelamento, com tolerâncias bem definidas; rejeitar hipótese impossível e registrar evento de diagnóstico seguro. Conferir fallback a time manual e observações consistentes em múltiplos frames sem sobrescrever estado correto. Testes positivos Cramorant1485, negativos Wingull1485/Vanillite1485. Código atual em `detect/AllyOcrRecoveryPolicy.kt` branch 0.5.34 aplica apenas `cp in 10..10000` + confiança >=0.90 nesta política; o critério atual **não basta** para estes casos, sem descartar a existência de outros guardas no pipeline.

## Outras correções por prioridade
**P0 — Continuidade e fidelidade:** não registrar como batalha válida evento que não possui identidade nem evidência mínima; classificar como sessão observacional/incerta; guardar origem, razão, score e frames confirmadores de `NEW_BATTLE` e `SWITCH`; respeitar até 3 aliados e adversários distintos, sem inferir vitória. Completar eventos `session|ended(reason)` em todos os caminhos. Esse problema é semelhante ao estudo anterior de 0.5.31, mas **não declarar regressão nem melhora quantitativa apenas por amostras diferentes**.

**P0 — Carga inimiga/escudo/HP:** separar OCR confirmado de eventos `energia/faixa` inferidos; deduplicar por ciclo; no HUD marcar baixa confiança e impedir recomendações assertivas baseadas só em modelo incerto. Oponentes atingem 32 hipóteses com scores por estado às vezes ~0,10–0,20. Escudos invariantes em 2 exigem eventos de observação com fonte/confiança; HP distinguir observado/estimado/ausente/stale e não perpetuar 100% sem atualizar.

**P1 — Histórico, backup e caixa-preta:** eventos com sequência monotônica única, timestamps e ordenação estável na exportação CSV/JSON/TXT; não reescrever o arquivo original. Logcat do próprio PID muito limitado no Android 16; ampliar ring buffer de *eventos do próprio aplicativo* sem texto OCR/screenshot/nickname/coords nem dados de terceiros, especificando rejeições e estado do motor. Backup config tem 39 chaves persistidas e `manual_team_v1` como JSON codificado em string; validar importação/dados/versões e preservar posições HUD exatas, sem extrapolar estado Manual/Auto entre batalhas, pois `team_recognition_mode_v1` não consta no snapshot.

**P1 — Competição:** 0/7 resultados confirmados, portanto bloquear atribuição de pontuação, medalha, ranking ou recompensa a esse lote. Ranking de IV, ranking PvPoke meta e rating do usuário são classificações diferentes.

## Regressões a testar antes da próxima compilação
- Simular CP inválido para espécie em sprite visual e nome OCR; validação de forma/mega/sombra/buddy no limite; tratar incerteza e comparar roster.
- Duas partidas consecutivas e fase de seleção; troca verdadeira, retorno de espécie, falso aviso de troca, pausa/stale e encerramento sem evidência.
- 0/1/2 escudos e ataques OCR versus inferidos; HP baixo, queda de HP, HP do inimigo; não transformar contadores estimados em fatos.
- Android 16 Samsung e rotação; preservar menu, positions floats, reservas, HUD/ícones, scan de ficha e histórico.
- Unit/instrumented QA + APK assinado verificado, sem declarar pronto antes de sucesso de build e smoke test físico.

## Estado de execução
**Análise/documentação somente; nenhum código, teste Gradle, CI, commit de implementação ou APK novo.** O arquivo APK v0.5.33 foi realmente recebido neste chat (anterior documento de implementação da 0.5.33 dizia que na época não havia artifact; essa afirmação histórica não deve apagar a evidência do APK atual, cuja origem/assinatura ainda carecem de validação). Continuar a partir do Guia Mestre, versão 0.5.34 QA existente e requisitos de 0.5.35+ sem reiniciar.
