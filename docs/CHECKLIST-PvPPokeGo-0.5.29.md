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
