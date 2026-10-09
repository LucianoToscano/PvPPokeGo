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
