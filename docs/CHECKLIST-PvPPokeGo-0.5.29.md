# PvPPokeGo 0.5.29 — Recuperar reconhecimento dos aliados

Referência funcional dos aliados: [código 0.5.17](https://github.com/LucianoToscano/PvPPokeGo/tree/release/0.5.17). Referência visual do HUD: 0.5.17. Base de funções atual: 0.5.28.

- [x] Encontrada regressão de OCR nome/PC quando visual não tem match.
- [x] Recuperado fallback de nome + PC sem depender do atlas.
- [x] Recuperada leitura ampla 0.5.17 quando os recortes individuais não identificam os três Pokémon.
- [x] Exigidos três pares reconhecidos distintos no fallback amplo, sem inventar correspondências por coluna.
- [x] `needsOwnTeamScan` continua até 3 espécies/PCs confiáveis.
- [x] Cadência OCR de seleção ajustada para 1550 ms.
- [x] Testes unitários para reconhecimento OCR nome+PC, baixa confiança e dados ausentes.
- [x] Build Standard isolado configurado para 0.5.29.
- [x] Build 0.5.29 com testes de regressão concluídos.
- [x] APK validado/assinado (QA/debug) e SHA256 registrado.
- [ ] Teste em aparelho Samsung: os 3 Pokémon são encontrados ANTES de entrar na batalha.
- [ ] Teste em aparelho Samsung: reservas seguem corretamente após troca, sem misturar PC/espécie.
- [ ] Revisar log de diagnóstico caso algum dos três não seja reconhecido.

Build público: https://github.com/LucianoToscano/PvPPokeGo-APK-Build/actions/runs/37719025048

## Build Standard QA 0.5.29 — entregue

- Run público: https://github.com/LucianoToscano/PvPPokeGo-APK-Build/actions/runs/37719025048 — **SUCCESS**.
- Commit público compilado: `008993b11f9299fc4a88cfeb7fde9f8ff7e9efc2`.
- `:app:testStandardDebugUnitTest` + `:app:assembleStandardDebug`: PASS.
- Artefato `PvPPokeGo-0.5.29-QA-debug.apk`: **79.193.455 bytes**.
- SHA-256 `b79c41f58e6fe846f7a62497cd91e1d64f15e377bdee1d8dcc4e2369e1199e3a`, hash do arquivo baixado corresponde ao `SHA256SUMS.txt` do CI.
- `unzip -t`: PASS; `gamemaster.json`, `pvpoke_snapshot.json`, `pokemon_icon_atlas.png/json` presentes.
- `apksigner verify`: APK Signature Scheme v2 **true**, 1 signatário QA/debug (não é assinatura permanente).
- Launcher transparente enviado pelo usuário presente: SHA256 `f8d4f45922e6af0022e3eaad1d255fedd2b790650f542e62315042f8bd1dbf51`, idêntico à versão 0.5.27/0.5.28.
- Google Drive oficial, ZIP com APK + checksum: https://drive.google.com/file/d/1Pw9RuiruLHrP4f7tkCFDLG8NnGhMuxFO/view?usp=drivesdk
- PENDENTE: QA de detecção efetiva no aparelho Samsung, não inferir que todos os aliados aparecem a partir de BUILD SUCCESS.


## Arena — ranking, medalhas, campeonatos e recompensas (2026-10-08)

Planejamento canônico: [Arena — especificação e estado](ARENA_RANKING_TOURNAMENTS_REWARDS.md).

- [x] Inspecionar a branch 0.5.29, HEAD e documentação; não alterar a referência visual HUD 0.5.17.
- [x] Criar implementação isolada em `feature/arena-foundation-20261008`.
- [x] Inserir painel recolhível Arena na tela principal sem modificar overlay/captura/OCR.
- [x] Apelido de perfil **local** com gravação independente.
- [x] XP/faixas locais a partir de resultados explícitos do histórico disponível, evitando dupla contagem por ID.
- [x] Medalhas locais com regras de progresso visíveis.
- [x] Motor matemático para classificação Elo separada por liga e temporada.
- [x] Exigir duas confirmações complementares como condição necessária a considerar partida competitiva.
- [x] Criar gerador inicial de primeira rodada eliminatória por seeds para potências de dois.
- [x] Adicionar testes de unidade no código para XP, duplicatas, streak, validação bilateral, Elo, separação por liga e chaves.
- [x] CI Kotlin/Android: `:app:testStandardDebugUnitTest --tests 'com.lucianotoscano.pvppokego.arena.*'` — SUCCESS em [37873242217](https://github.com/LucianoToscano/PvPPokeGo-APK-Build/actions/runs/37873242217).
- [x] Suíte Android Standard completa, assemble e integridade SHA/ZIP/assinatura v2 PASS no [build 37873242187](https://github.com/LucianoToscano/PvPPokeGo-APK-Build/actions/runs/37873242187): **PvPPokeGo-0.5.30-QA-debug.apk**, 79.283.282 bytes, SHA-256 `4069ced7db03ea6e5d104b8d13826438802a671853b4b228784a3ad225ac5e7a`.
- [x] VersionName/versionCode 0.5.30 / 35 ajustados na branch de feature privada após o build público validado.
- [x] ZIP de QA enviado e verificado na pasta Drive oficial: [PvPPokeGo 0.5.30 Arena QA](https://drive.google.com/file/d/1lMFVEQrq6sc37iDdFrw2upCAfaZrDDp9/view?usp=drivesdk), tamanho 35.770.210 bytes.
- [ ] Testar APK de prévia em aparelho Samsung e aprovar UI, perfil e medalhas; a instalação não foi executada pela assistência.
- [ ] Validar UI real no Samsung: expansão, apelido, medalhas, legibilidade, rolagem e persistência.
- [ ] Manter acumuladores vitalícios independentes do histórico atual limitado a 50 partidas; definir política de migração.
- [ ] Implementar login e identidade estável, API, banco de dados, anti-fraude e privacidade/consentimento.
- [ ] Criar ranking ONLINE global/nacional/semanal/sazonal (não mostrar jogadores/posições fictícios).
- [ ] Criar inscrição real e calendário de torneios, fase de grupos, melhor de 3 e chaves completas com byes.
- [ ] Criar fluxos de disputa/mediador, auditoria de resultados e aprovação de torneios com prêmio.
- [ ] Criar inventário e concessão de medalhas/títulos/molduras/temas virtuais com validação server-side.
- [ ] Avaliar parcerias, regras legais, patrocínio, elegibilidade e meios autorizados antes de prêmios reais.
- [ ] Gerar e validar APK novo **somente** quando Standard compilar e artifact tiver versão/assinatura/SHA-256 verificados.

**Status:** FUNDAÇÃO NO CÓDIGO / PARCIAL; integração online e premiação NÃO implementadas. APK 0.5.29 QA anteriormente entregue permanece o último verificado. Não aumentar versionCode nem prometer 0.5.30 até build real.

## Continuação isolada 0.5.31 — cadastro manual pré-batalha

Especificação: [TEAM_BUILDER_0.5.31.md](TEAM_BUILDER_0.5.31.md). Branch `feature/team-builder-0.5.31-20261008` criada a partir da 0.5.30.

- [x] Base do Guia Mestre e HEAD de Arena verificados; release/0.5.29 não alterada.
- [x] Painel SUA EQUIPE integrado no topo da tela inicial, mantendo Arena e controles Batalha.
- [x] Três vagas editáveis, espécie e forma com busca no Game Master offline.
- [x] PC, IVs 0-15, HP máximo opcional, sexo, Mega/Sombroso, golpe ágil e dois carregados.
- [x] Sincronizar seletor de liga ao `SettingsRepository.leagueMode` existente, sem flag concorrente.
- [x] Persistência local + inclusão `manual_team_v1` no backup JSON existente.
- [x] Validação pura + testes JUnit para liga, campos inválidos, slots vazios e campos opcionais.
- [x] CI Kotlin `:app:testStandardDebugUnitTest --tests 'com.lucianotoscano.pvppokego.data.ManualTeamPolicyTest'` PASS: [run 37875124885](https://github.com/LucianoToscano/PvPPokeGo-APK-Build/actions/runs/37875124885) — BUILD SUCCESSFUL em 1m 4s.
- [x] CI Standard integral `:app:testStandardDebugUnitTest :app:assembleStandardDebug` PASS: [run 37875124890](https://github.com/LucianoToscano/PvPPokeGo-APK-Build/actions/runs/37875124890). APK 0.5.31 QA, ZIP e assinatura v2 verificados.
- [ ] QA real Samsung: editor de 3 slots, busca de formas, persistência, exportar/importar, troca de liga, rolagem, exibição do teclado.
- [ ] Integrar cadastro como sugestão/hint verificável de reconhecimento, jamais substituir automaticamente OCR com dados antigos.
- [x] Sprite fiel por `speciesId` usando `PokemonIconAtlas` existente, sem falsificar imagem de forma; carregamento assíncrono e fallback por nome.
- [ ] Validar visual real dos três cartões e legibilidade no Samsung.
- [ ] Exibir rank e HP inferidos apenas após cálculo confiável, versão de dados e critério de confiança explícito.
- [ ] Validar elegibilidade real de Mega/Sombroso por liga e forma; o toggle atual é só declaração do jogador.
- [x] APK `PvPPokeGo-0.5.31-QA-debug.apk` real (79.430.738 bytes), SHA-256 `07d769bdcbbe4a6400ca21fe38f1276ddbb946b3a682c46cb85648d9400f52a0`. ZIP assinado e com checksums [Drive oficial](https://drive.google.com/file/d/1BQP8Zffi9IlwzBHWbN5S0koxbCOtJwvY/view?usp=drivesdk), 35.822.814 bytes.
- [ ] Validar em Samsung antes de aprovar visual e PR.

**Status:** Código da funcionalidade implantado em branch de feature; sem confundir esse progresso com validação física.


## 0.5.32 — SUA EQUIPE: Automático x Manual — 2026-10-09 (SEM BUILD)

Especificação: [TEAM_RECOGNITION_MODES_0.5.32.md](TEAM_RECOGNITION_MODES_0.5.32.md).
Branch isolada `feature/team-recognition-modes-0.5.32`; `release/0.5.29` e APKs 0.5.30/0.5.31 permanecem preservados.

- [x] Conferir guia canônico `release/0.5.29`, histórico `release/0.5.26`, HEAD das branches e contrato visual.
- [x] Manter modo **Automático** como padrão, reutilizando detector de três aliados e correspondência de reservas existentes.
- [x] Adicionar escolha explícita de modo **Manual** no painel SUA EQUIPE.
- [x] Se os três Pokémon têm espécie válida + PC dentro da liga, associar roster declarado ao motor como equipe atual sem esperar escanear a tela de seleção.
- [x] Não presumir por ordem de cadastro qual está em campo; seguir verificando imagem/CP/OCR para ativo e reservas.
- [x] Impedir que um scan automático sobrescreva a composição selecionada manualmente.
- [x] Criar política para rejeitar visual forte e PC incompatíveis, espécie externa e correspondência ambígua.
- [x] Persistir `team_recognition_mode_v1` e incluir no export/import de configurações existente.
- [x] Proteger resets/continuidade do motor para reaplicar roster selecionado, sem reaproveitar energia/HP da equipe anterior.
- [x] Adicionar testes JUnit no código para CP, visual, contradição, espécie não cadastrada, duplicatas e trocas.
- [x] Registrar estado, referência de código, critérios de QA e limitações no Guia Mestre.
- [ ] Executar os testes do matcher **somente quando for autorizado compilar/testar**.
- [ ] Rodar build Android e validar APK **somente quando o usuário solicitar**.
- [ ] Verificar se modo Manual com OCR global OFF deixa aviso e não presume o Pokémon ativo.
- [ ] QA Samsung: seleção imediata, três aliados, ordem real dos cartões reservas, mudança de equipe, batalha nova, troca de liga e falha de captura.
- [ ] Exibir, também no card inicial, snapshot de equipe reconhecida automaticamente com confiança e origem dos dados, sem sobrescrever `manual_team_v1`.
- [ ] Adicionar testes de regressão de integração `BattleOverlayService`/`BattleEngine`/`BattleCorrespondenceTracker` em partidas consecutivas antes de liberar.
- [ ] Verificar compatibilidade de backup após importação de flag e roster.
- [ ] Polir o layout usando imagens originais do atlas, sem remover funcionalidades atuais.

**STATUS REAL: CÓDIGO ALTERADO + DOCUMENTAÇÃO REGISTRADA; NENHUM TESTE/COMPILAÇÃO/APK EXECUTADO NESTA ETAPA POR PEDIDO DO USUÁRIO.**


## Configurações do Treinador — adendo visual de 2026-10-09 (SEM BUILD)

Referência: captura de Configurações enviada pelo usuário, com **Gerais / PvP IV / Gerador de Nomes / Formato da Data Pego / Simulador de Batalha**, Nível 54 e Equipe Mystic. **54/Mystic são ilustração da captura, NÃO valores atribuídos à conta do jogador.** Contrato: [TRAINER_SETTINGS_0.5.32.md](TRAINER_SETTINGS_0.5.32.md).

- [x] Verificar Guia Mestre 0.5.29, histórico 0.5.26 e estado real da branch `feature/team-recognition-modes-0.5.32`.
- [x] Preservar modo Automático/Manual da Sua Equipe, Arena e opções de HUD/overlay sem criar outra preferência de liga.
- [x] Criar card recolhível Configurações do Treinador com cinco subseções sem sair da tela inicial.
- [x] Nível editável/opcional, equipe Mystic/Valor/Instinct ou Não informada; persistência local e nenhuma suposição de login.
- [x] Consultar IVs dos três Pokémon informados em Sua Equipe; campos ausentes ficam ausentes.
- [x] Gerador de nomes com formatos pré-estabelecidos e cópia real para área de transferência do aparelho, sem comandar Pokémon GO.
- [x] Escolher formato da data de captura, mostrar exemplo datado, sem fingir ter lido data real do Pokémon GO.
- [x] Adicionar prévia de vantagens por tipos entre aliado cadastrado e adversário do Game Master offline. **NÃO é simulação completa**.
- [x] Salvar preferências no arquivo original de configurações e incluir quatro chaves no backup JSON.
- [x] Criar testes JUnit de validação de nível, preview de nomes/IVs, formato de datas e valores não presumidos (código criado, **não executado**).
- [x] Registrar arquivos, limites e passos futuros no Guia Mestre + documento de detalhamento.
- [ ] Construir ranking **verdadeiro** de PvP IV por espécie, forma, nível e liga com algoritmo verificável.
- [ ] Implementar simulador de batalha por movimentos/energia/escudos/HP/turnos a partir de engine matemático testado.
- [ ] Integrar data real de captura somente com campo específico ou fonte verificada.
- [ ] QA Samsung e testes Kotlin/Gradle/Android Standard **adiados por solicitação do usuário**.
- [ ] QA backup e restore da equipe, preferências, HUD e modos, sem perda de posições.
- [ ] Polir proporções e ícones do card conforme referências clean antes de considerar o design final.

**STATUS: CÓDIGO E DOCUMENTAÇÃO ADICIONADOS À MESMA BRANCH 0.5.32; NENHUM TESTE/BUILD/APK EXECUTADO.**


## Referências PvPoke avançadas + exportação CSV de matriz — 2026-10-09 (SEM BUILD)

Documento técnico, fontes, nove imagens arquivadas e CSV original: [PVP_ANALYTICS_REFERENCES_2026-10-09.md](PVP_ANALYTICS_REFERENCES_2026-10-09.md).

- [x] Receber e organizar referências de Rankings, faixas CMP, simulação 1v1, estados de energia/HP e escudos, breakpoints, bulkpoints, timing, battle ratings, matriz, diferenças e Team Builder.
- [x] Conferir URLs públicas do PvPoke fornecidas e apontar ao repositório `pvpoke/pvpoke` (fonte de arquitetura/licenciamento, não projeto espelhado).
- [x] Preservar **nove imagens** e o CSV original no Google Drive do projeto, com links estáveis no documento técnico.
- [x] Inspecionar CSV `_vs .csv`: 3×3 confrontos, W/L/D e média por linha; os números assinados são diferenças de matriz, **não** rating/percentual/Elo/rank IV.
- [x] Confirmar reutilização de `GameDataRepository`, `GameMaster`, `TypeChart`, `BattleEngine` e painel de equipe/treinador já existentes, sem confundir a simulação offline com estado de batalha atual.
- [x] Descrever fases e critérios de QA no documento técnico, mantendo origem, versão do snapshot e incerteza explícita.
- [ ] Implementar cálculo verdadeiro e versionado de CP/IV/rank, timing, turnos/dano/energia, CMP, breakpoints e bulkpoints por algoritmo testado.
- [ ] Implementar simulador local 1×1 e matrizes com rankings/battle rating, relatórios e parser CSV (differences separados de ratings), com testes de equivalência.
- [ ] Criar UI clean Central de Consulta PvP com Rankings, Simular, Equipes/Matrix, CMP, Tipos e Golpes sem copiar UI de terceiro.
- [ ] Integrar análise simulada ao perfil de equipe **sem** substituir OCR/matcher e sem escrever palpites em histórico observado.
- [ ] Validar licença/atribuição, versões Game Master e regras atuais antes de redistribuir/adaptar códigos de terceiros.
- [ ] Executar testes e gerar APK **somente quando o usuário pedir**; evitar qualquer alteração de release/0.5.29 ou regressão do HUD V03.

**STATUS: REFERÊNCIAS E CSV ARQUIVADOS + ESPECIFICAÇÃO/CHECKLIST REGISTRADOS. NOVOS MÓDULOS NÃO IMPLEMENTADOS; NENHUM TESTE, BUILD OU APK.**


## Análise consolidada Calcy IV / PvPoke / Team Builder / Training / 2 CSV — 2026-10-09

**Documento de trabalho:** [ANALISE_CONSOLIDADA_PVP_2026-10-09.md](ANALISE_CONSOLIDADA_PVP_2026-10-09.md). Esta entrada é exclusivamente **ANÁLISE + DOCUMENTAÇÃO**, não uma nova implementação.

- [x] Reler Guia Mestre canônico `release/0.5.29`, histórico `release/0.5.26`, branch e HEAD real de `feature/team-recognition-modes-0.5.32` antes de registrar.
- [x] Inventariar código existente: `GameDataRepository`, `TypeChart`, `Models`, `BattleEngine`, `TeamBuilderPanel`, `TrainerSettingsPanel`; distinguir lógica já escrita do simulador completo ainda ausente.
- [x] Ler arquivo `Team vs. All Pokemon.csv`: **1.152 linhas de confronto**, 6 colunas, 0 linhas inválidas, 0 duplicatas, ratings de Altaria/Houndoom/Rillaboom + Threat Score/Overall Rating, 51.369 bytes e hash registrado.
- [x] Ler `_vs .csv`: grade 3×3 de diferenças assinadas, W/L/D, Average conferida, 370 bytes e hash registrado.
- [x] Armazenar cópias originais dos **dois CSVs** na pasta do projeto no Drive com links no documento.
- [x] Revisar prints e referências dos módulos de Team Builder (Coverage/Bulk/Safety/Consistency, amenazas/alternativas/scorecard), CMP, Rankings, simulador 1×1, Academy/Train IA, análise de dano/escudos/energia, top performers e filtros avançados.
- [x] Distinguir ranking de espécies, PvP IV Rank, Battle Rating 0–1000, Threat Score, diferença média CSV, Team Rating/Individual Rating de Training e Arena Elo. Nunca rotular como mesma métrica.
- [x] Distinguir PvPoke Tera Raid da mecânica GO: **Tera é de Scarlet/Violet** e fica excluída do PvP GO; ideia geral de counters pode inspirar futuro módulo independente de raides GO.
- [x] Definir três pilares e plano por prioridade: Assistente de Batalha, Laboratório PvP, Academia/Training.
- [x] Registrar limites de licenciamento, versão/validade de dados, amostragem de bots no Train, identidade OCR versus simulação e confidencialidade.
- [ ] **P0** Quando autorizado: testes/compilação da feature atual 0.5.32, isolamento da equipe manual/automática e regressão HUD/OCR/backup.
- [ ] **P1** Implementar importador de dois formatos CSV com fonte/versionamento e cópia simulada, sem inserir na Arena/histórico real.
- [ ] **P1** Criar botão "Analisar equipe" a partir da composição atual sem alterá-la; exibir relatório de ameaças/cobertura e incerteza, com métricas tipadas.
- [ ] **P1** Validar versão dos dados e implementar motor matemático puro (CPM, IV rank, dano, energia, escudos, buff, CMP, timing) com fixtures de regressão.
- [ ] **P2** Simulador real 1×1, 3×N Matrix/scorecard, breakpoints/bulkpoints, histograma e top counters; evitar uso no caminho crítico da captura.
- [ ] **P2** Academia PvP 3×3 contra IA offline, replay de decisões, gráficos de dano/escudos/energia, progressão em treino separada de batalhas verificadas.
- [ ] **P3** Rankings Arena/campeonatos/prêmios somente com backend, validação e antifraude; sem misturar indicadores de simulação/bot.
- [ ] **Fora do núcleo PvP:** qualquer cálculo específico de Tera/Scarlet/Violet; futura Raides/PvE GO deve ser uma ferramenta distinta.
- [ ] **Antes de publicar:** revisar licenças/atribuições, movimentos legados, regras de liga, dados válidos e QA Samsung.

**Nenhum código de produção, testes executados, build, APK ou release nesta rodada de análise.** 


## Exportações reais do aparelho — diagnóstico 0.5.31 — 2026-10-09 (SEM BUILD)

Documento técnico com hash de origem, evidências e plano conservador: [DIAGNOSTICO_EXPORTS_0.5.31_2026-10-09.md](DIAGNOSTICO_EXPORTS_0.5.31_2026-10-09.md).

- [x] Inspecionar 3 anexos 0.5.31: backup JSON de configurações, ZIP de diagnóstico e ZIP de histórico, **sem restaurar configurações**.
- [x] Verificar que os dois ZIPs incluem histórico JSON de bytes idênticos; 7 sessões, 702 eventos, CSV de 702 linhas compatível com JSON.
- [x] Registrar que 6/7 sessões não têm `result` verificável; nunca classificá-las como derrota/estimar win rate.
- [x] Detectar possível fragmentação de sessão: vitória de 3,7s sem Pokémon identificados nem OCR, próximas sessões longas, duas sessões sem participantes.
- [x] Detectar 34 trocas rotuladas `OCR/CONFIRMADO` e sessões com mais de 3 espécies do mesmo lado, exigindo auditoria de temporalidade/identidade.
- [x] Verificar ordens dos eventos: quatro das sete sessões possuem `elapsedMs` regressivo em pelo menos um par consecutivo.
- [x] Comparar configurações: arquivo isolado 34 chaves, ZIP diagnóstico 35; única chave adicional `auto_recognition=false` no diagnóstico. O novo `team_recognition_mode_v1` **não existe** no backup antigo 0.5.31, sem supor bug por isso.
- [x] Documentar mismatch entre roster `manual_team_v1` e espécies aliadas reconhecidas, ainda sem causa determinada; não inserir dados pessoais/arquivo bruto no repositório.
- [x] Revisar medidas existentes no código 0.5.32 para time Manual/Auto, sem alegar testes nem correção já verificada.
- [ ] **P0** Criar testes com cenários de sessao partida/resultado → partida subsequente; evitar fragmentos e juntar partidas reais erroneamente.
- [ ] **P0** Auditar confirmação OCR contra roster real: 3 aliados + 3 adversários, espécies/PC/forma, persistência, troca e pressão do matcher.
- [ ] **P0** Validar comportamento de `auto_recognition=false` e modo Manual separado, antes/depois do backup e na inicialização do serviço.
- [ ] **P1** Diferenciar evento confirmado por imagem, inferido e manual; exigir evidência para resultado e manter indeterminado quando necessário.
- [ ] **P1** Garantir ordem cronológica estável e consistente JSON/CSV/txt, sem afetar captura de batalha.
- [ ] **P1** Mostrar e permitir recuperar reservas desconhecidas sem inventar golpes.
- [ ] Executar QA em Samsung + testes Kotlin/Gradle/Android **somente quando usuário autorizar**.

**STATUS: somente inspeção estática e documentação sanitizada; nenhum código funcional, teste compilado ou APK modificado.**


## Caixa-Preta / Diagnóstico Avançado — proposta pós-exports 0.5.31 — 2026-10-09 (SOMENTE DOC)

Especificação: [PROPOSTA_CAIXA_PRETA_DIAGNOSTICO_0.5.32.md](PROPOSTA_CAIXA_PRETA_DIAGNOSTICO_0.5.32.md).

- [x] Conferir exportador atual na `MainActivity.kt`: ZIP básico com configurações, histórico, device, README; ZIP de histórico separado com JSON/TXT/CSV.
- [x] Registrar limitações Android: Logcat completo do sistema não é acessível silenciosamente por app comum; usar relatório de bugs oficial/ADB **externamente**, e telemetria própria do aplicativo.
- [x] Especificar Caixa-Preta: trace de OCR, rejeições por thresholds, matcher manual/automático, continuidade das sessões, captura/FPS/rotação, energia/escudos, resultados, exceções próprias e warnings de integridade.
- [x] Estabelecer proposta de ZIP versionado e compatível, linha do tempo com seq e tempo monotônico, privacidade opt-in, redação de PII e limites de armazenamento/performance.
- [ ] Implementar coleta interna **desligada por padrão**, sem screenshot automático nem coleta de outros apps.
- [ ] Implementar botão `Marcar problema agora` e exportador avançado SAF sem quebrar o exportador existente.
- [ ] Implementar alerta antirregressão de equipe 3×3, resultados sem evidência, regressão de tempos, frames stale e inconsistência OCR/PC/visual, sem auto-corrigir silenciosamente.
- [ ] Testes unitários/parsers, integridade ZIP, compatibilidade e desempenho Android; inspeção manual de vazamento de dados/PII.
- [ ] Validar com sessão anotada em Samsung e dois exports 0.5.31; testar modo manual/automático e `auto_recognition`.
- [ ] Não compilar nem gerar APK até ordem explícita.

**Estado: registrada PROPOSTA; nenhuma implementação/compilação/teste.**


## v0.5.33 Logcat + HP + controles OFF cinza — execução autorizada em 2026-10-09

Relatório exato de código/QA: [IMPLEMENTACAO_0.5.33_LOGCAT_HP_UI.md](IMPLEMENTACAO_0.5.33_LOGCAT_HP_UI.md).

- [x] Abrir branch `feature/diagnostics-hp-ui-0.5.33` derivada da feature 0.5.32, sem alterar a release antiga.
- [x] Implementar `AppDiagnosticLog.kt` com `Log.i("PvPPokeGo")`, buffer privado limitado, coleta **apenas do próprio processo** por Logcat best-effort.
- [x] Anexar `runtime-events.log`, `logcat-app.txt` e `integrity-warnings.txt` ao ZIP diagnóstico preexistente (sem remover JSON de histórico/settings/device).
- [x] Instrumentar eventos de serviço/reset/sessões com breadcrumbs sanitizados, sem registrar OCR bruto/terceiros.
- [x] Corrigir estados visuais: OFF cinza + label atenuado, ON ciano no menu rápido e no Switch da página de configurações.
- [x] Melhorar previsão de HP: intervalo sobrevivente sem escudo, aviso separado de escudo, fallback discreto quando ROI ausente, chave persistente `show_hp_remaining_forecast` incluída no backup.
- [x] Atualizar `versionName=0.5.33`, `versionCode=38` e adicionar CI de QA para geração de artefato APK com SHA256.
- [x] **Tentou-se compilar** no GitHub Actions, mas job encerrou como failure antes de steps/logs, sem executar Gradle. Primeira run: https://github.com/LucianoToscano/PvPPokeGo/actions/runs/37901931447 ; tentativa posterior: https://github.com/LucianoToscano/PvPPokeGo/actions/runs/37902276938.
- [ ] **BLOQUEADOR — APK**: verificar aviso da run GitHub Actions na interface (runner, faturamento/quota, Actions desabilitado) e restabelecer execução; não atribuir causa sem evidência.
- [ ] Testes unitários Kotlin/Android: 0 executados até desbloqueio de runner; exigir `testStandardDebugUnitTest` e `assembleStandardDebug` passando.
- [ ] Download e checksum do APK **0.5.33 real**; não oferecer cópia 0.5.31 renomeada.
- [ ] QA em Samsung/Android 16: Logcat próprio, ZIP, tema claro/escuro, QuickMenu ON/OFF, HP previsão com/sem ROI, capture stale, OCR/manual/auto e restauração de posições.
- [ ] Produzir artefato ao usuário quando compile/testes realmente funcionarem, indicando assinatura debug e necessidade de backup se exigida reinstalação.

**ATENÇÃO: IMPLEMENTADO EM CÓDIGO-FONTE; COMPILAÇÃO NÃO OCORREU; 0 APKs GERADOS.** 


## P0 — Recuperação do build público já existente (2026-10-09, v0.5.33)

- [x] Encontrar o procedimento HISTÓRICO já documentado no Guia Mestre (2026-10-07): espelho público `LucianoToscano/PvPPokeGo-APK-Build`, em vez de tentar repetidamente Actions privados.
- [x] Confirmar permissões e branch pública `preview/team-recognition-0.5.32-20261009`, usada como base.
- [x] Comparar SHA dos arquivos `app/` da fonte privada `3b316f6f...` com mirror 0.5.32: 20 arquivos diferentes/ausentes; criar snapshot 0.5.33 isolado e atualizado em um commit (`655514514f1cdfa4ce89b7848aa36994dc639f27`).
- [x] Adicionar workflow `.github/workflows/diagnostics-hp-ui-0.5.33.yml` com Standard+tests+SHA256, herdando fluxo comprovado da 0.5.31.
- [x] Confirmar criação da run pública: https://github.com/LucianoToscano/PvPPokeGo-APK-Build/actions/runs/37903983424 — inicialmente `in_progress`.
- [ ] Verificar run concluir `success`; se `failure` investigar stacktrace real e corrigir **primeiro fonte privada**, depois repetir sync por SHA no build público.
- [ ] Confirmar testStandardDebugUnitTest + assembleStandardDebug executados; extrair APK do artifact, validar arquivo e SHA256; não renomear build antigo.
- [ ] Entregar APK real 0.5.33 ao usuário após sucesso, com ressalva de assinatura QA/debug e QA físico ainda pendente.
- [ ] Registrar resultado final no Guia Mestre e Drive (run, commit, SHA256, testes, artifact, pendências).
- [x] Regra permanente introduzida no início do Guia Mestre: **consultar sempre o espelho público já aprovado antes de alegar que build é impossível**, manter original privado e sincronização revisada somente para compilação.


### Status final do APK 0.5.33 no espelho público — 2026-10-09
- [x] Workflow público `37903983424` **success**: https://github.com/LucianoToscano/PvPPokeGo-APK-Build/actions/runs/37903983424.
- [x] Gradle `:app:testStandardDebugUnitTest`, `:app:assembleStandardDebug` e validações de assets e assinatura executados com sucesso.
- [x] APK 0.5.33 descarregado de artifact `11603767382`, extraído e validado. Tamanho 79.561.814 bytes; SHA-256 `9de20e578d02e56dc11652d0b9f639293b99852cfa123f15aa600d0a0a6eb1d8` confere com checksum do CI.
- [x] Disponibilizar `PvPPokeGo-0.5.33-QA-debug.apk` real no chat, sem renomear builds antigos.
- [ ] QA físico Samsung: instalação/atualização compatível com chave de assinatura, botões de settings ativados/brancos e inativos/cinzas, funções HP, Logcat/exportação, overlay/reservas/reconhecimento/posição dos controles. Não atribuir status de QA físico antes dos testes.


## Próxima funcionalidade — Scanner de três vagas (proposta 0.5.34, 2026-10-09)

Especificação detalhada: [PROPOSTA_SCANNER_EQUIPE_0.5.34.md](PROPOSTA_SCANNER_EQUIPE_0.5.34.md).

- [x] Conferir código atual de SUA EQUIPE (3 vagas, PC/HP/IV/sexo/forma/golpes), ML Kit, MediaProjection e guia mestre 0.5.33.
- [x] Analisar **estaticamente** nomes de recursos/arquivos do APK Poke Genie enviado como referência. **Correção do usuário 2026-10-09:** "Premium" no filename foi acrescentado por ele mesmo; NÃO inferir modificação só pelo nome. Arquivo inspecionado sem executar e sem redistribuição.
- [x] Registrar exemplo Cramorant 1485 PC, 126 PS, Bicada, Mergulho, Voar, fêmea, tipos Voador/Água; IVs no apelido não validados.
- [x] Propor UI por vaga “Escanear ficha” + “Escanear avaliação” + “Revisar e aplicar” + “Usar time agora”, com confiança por campo.
- [ ] Implementar importação de screenshot consentida (Android Photo Picker), parser OCR de ficha, validação contra Game Master e merge conservador de duas capturas.
- [ ] Implementar IV appraisal de barras com tolerâncias/resolução e fallback manual, sem usar IV de apelido como verdade.
- [ ] Adicionar captura direta a partir de tela do Pokémon GO via MediaProjection ativa/consentida, sem tocar automaticamente no jogo e sem regressões no OCR de batalha.
- [ ] Rank IV PvP, calculadora de fortalecer/evoluir, moveset, histórico local, apelidos e casos de mudança de forma como Cramorant, com fontes licenciadas.
- [ ] Testes Android/JUnit, UX e QA do aparelho; depois build no espelho público existente e checksum do APK da versão nova **quando explicitamente pedido**.
- [ ] Não embutir/redistribuir o APK de referência do Poke Genie no PvPPokeGo ou no mirror sem confirmação de titularidade/licença; pesquisar livremente funcionalidades e implementar equivalentes de forma independente.

**Status: apenas documentação na branch `feature/team-scan-0.5.34-planning`; sem novo APK.**

- [x] **Retificação de registro 2026-10-09:** usuário informou que renomeou pessoalmente o arquivo com “Premium” por estética e que é uma versão normal. Removido o rótulo não comprovado “APK modificado” do planejamento. Permissão para usar em análise técnica; autenticidade da assinatura e licença de componentes não verificadas. Nenhum novo APK gerado nesta retificação.


## Auditoria técnica complementar do arquivo Poke Genie — 2026-10-09
- [x] Inspeção estática do APK real enviado: 8.19.0/build 416, pacote `com.cjin.pokegenie.standard`; manifesto, layouts, Tesseract e dados, container ZIP íntegro.
- [x] Registrar inventário técnico e diferenças em [ESTUDO_APK_POKE_GENIE_0.5.34.md](ESTUDO_APK_POKE_GENIE_0.5.34.md) — sem copiar código/dados de terceiros.
- [x] Distinguir comprovado por nome de recurso/manifesto vs algoritmo/funcionalidade não verificados; considerar APK aninhado `yrf.apk` evidência de camada de empacotamento, NÃO de adulteração.
- [x] Comparar código canônico atual: slots e OCR existem; OCR da ficha e Avaliar NÃO implementados.
- [ ] Implementar prioridade P0: screenshot(s) da ficha, appraisal, revisão/confiança e salvamento independente por vaga 1/2/3.
- [ ] P1: rank IV, golpes PvP/energia, team scorecard e leitura em overlay consentida com classificador de contexto.
- [ ] P2: histórico de scans e filtros, CP/poeira/doce e apelidos customizados.
- [ ] QA e eventual build somente depois de implementação autorizada; não misturar artefatos do Poke Genie ou de distribuidores terceiros no mirror público.
**Estado: estudo/documentação. Nenhum novo APK ou código Kotlin da versão 0.5.34 foi feito neste checkpoint.**


### Status de código da 0.5.34 — 2026-10-09
- [x] Criada branch `feature/team-scan-0.5.34-implementation`, preservar 0.5.33.
- [x] Adicionado TeamScanParser: espécies, PC/PS e movimentos por screenshot do Pokémon GO; nickname não é fonte de IV.
- [x] TeamScreenshotScanner ML Kit via GetContent: captura selecionada pelo usuário, sem gravação automática da imagem.
- [x] Interface SUA EQUIPE: scanner individual por vaga, prévia, confirmação/aplicar, segunda imagem e prevenção de conflito de espécies.
- [x] Rank de IV offline, estatísticas PC potencial e análise básica de energia/turnos/DPE de golpes por dados PvPoke.
- [x] Histórico local dos scans confirmados e inclusão no backup; sem armazenar captura bruta.
- [x] Testes JUnit adicionados para Cramorant OCR, rejeitar números do apelido, dados inválidos, fórmula de CP, rank e energia.
- [x] versão 0.5.34 code 39 com arquivos do scanner e testes espelhados no público para QA, workflow iniciado: https://github.com/LucianoToscano/PvPPokeGo-APK-Build/actions/runs/37917670306
- [ ] Confirmar testes/compilação Standard no Actions público e corregir falhas antes de declarar APK pronto.
- [ ] Scanner real das três barras de Avaliar 0–15: calibrar layout/dimensões com screenshots de aparelhos reais. A versão atual **NÃO preenche IVs automaticamente**.
- [ ] Captura ao vivo de ficha/avaliação sob consentimento usando MediaProjection com classificador de contexto, sem regredir overlay.
- [ ] Calculadora dos custos exatos de poeira/doces/evolução via fonte atualizada e testes.
- [ ] QA no celular: variabilidade de OCR, reserva/matcher, posições HUD, histórico e backup/restore.
- [ ] Publicar APK com checksum e assinatura verificados apenas se o CI passar.


### Conclusão de QA automatizada — Scanner 0.5.34 (2026-10-09)
- [x] GitHub Actions público run `37917670306` `success`; testes e Gradle Standard debug concluídos.
- [x] Artefato `11611096799` `PvPPokeGo-0.5.34-QA-debug` baixado; APK e ZIP íntegros, SHA256 conferido com arquivo do CI.
- [x] Assinatura Android v2 validada no CI; entregue APK concreto `PvPPokeGo-0.5.34-QA-debug.apk`, 79.660.114 bytes, SHA256 `45e24fdf5d6c9a3c899bda7e08cec8a826231201c1f07f30604c99d3f7bc6d3e`.
- [ ] QA Samsung e compatibilidade de assinatura com versão instalada; fazer backup das configurações antes de tentar atualização.
- [ ] Validar scanner com imagens reais, especialmente IV Avaliar, correção de nomes PT-BR, Auto/Manual, HP/overlay/reservas.
- [ ] Implementar scanner da avaliação numérico, live scan e custos exatos numa fase seguinte; *não* tratá-los como prontos no APK 0.5.34.


### Backlog da Comunidade de Treinadores / 0.5.35+ (proposta 2026-10-09)
- [x] Estudar screenshot do perfil e QR/código de amizade; confirmar oficialmente a separação entre código amizade, Battle Code e Party Play.
- [x] Registrar plano e critérios em `docs/PROPOSTA_COMUNIDADE_TREINADORES_0.5.35.md`.
- [ ] Área Meu Treinador: nick/código 12 dígitos, nível/facção, OCR de screenshot com prévia, opção compartilhar/copiar, dados privados por padrão.
- [ ] Cadastro de amigos e cartões PvPPokeGo locais; não sincronizar nem alegar adição automática dentro do jogo.
- [ ] Convites de coordenação e agenda para duelo PvP, reides PvE e eventos, com share sheet e regras.
- [ ] Conectar selecionador do time 1/2/3 e informações de tipagem para recomendações de duelo/raid.
- [ ] Debater/avaliar autenticação e backend próprios, moderação, consentimento, bloqueio e privacidade antes de ligar rede social online.
- [ ] Testar exportação/restore, scans sensíveis e regressão HUD/overlay/scanner antes de release.
- [ ] Build e APK com essas funções: NÃO iniciados; 0.5.34 QA entregue permanece inalterada.
