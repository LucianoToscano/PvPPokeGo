# PvPPokeGo — ESTADO CANÔNICO ATUALIZADO EM 13/10/2026

> **Leia esta seção antes de registros históricos.** GitHub é a fonte de código/documentação. O Google Drive abaixo é o NOVO destino oficial dos binários. Registros anteriores de versão e links da conta antiga são preservados apenas como histórico — não usar como entrega corrente.

## 0. Estado atual — entrega 0.5.36 e novo Google Drive

- **Novo Drive PvPPokeGo (propriedade LucianoToscano):** https://drive.google.com/drive/folders/1ybogVRRnaOe0KtWjlovxS56bJOi_JewI
- **GitHub código (privado):** https://github.com/LucianoToscano/PvPPokeGo
- **Branch de desenvolvimento mais recente verificada:** `feature/battle-correspondence-0.5.36-20261009`. HEAD base anterior à atualização documental: `8b32a050390e8b0475ca3d1579c7759424c54480`. Sempre conferir HEAD antes de alterar.
- **Índice único de links atuais:** [docs/PROJECT_LINKS.md](PROJECT_LINKS.md).
- **Checklist 0.5.36 atual:** [docs/CHECKLIST-PvPPokeGo-0.5.36.md](CHECKLIST-PvPPokeGo-0.5.36.md); preservar checklist de 80 itens da 0.5.26 como base histórica.
- **APK Standard QA/debug 0.5.36:** https://drive.google.com/file/d/14wzZDCx0RMCFtH-olGYlFb9FDnpaATHu/view?usp=drivesdk
- **ZIP com APK e comprovantes:** https://drive.google.com/file/d/1Y_ngMw12VcEPKMvBWVynMHnp6WQTyFOa/view?usp=drivesdk
- **Espelho DOCX migrado do Guia Mestre:** https://docs.google.com/document/d/1CfSJ8o0r3qnoq3MSsfozYD1M5C07A1pL/edit?usp=drivesdk (cópia histórica da versão antiga, NÃO substitui esta página atualizada).
- **CI definitivo validado:** https://github.com/LucianoToscano/PvPPokeGo-APK-Build/actions/runs/37992150961 — `completed/success`, artifact `11645731162`. Versão `0.5.36`, versionCode 41.
- **SHA-256 APK verificado:** `e1ff7e2ac9e0a5f6e940f1d334bb2c7722409d7018bc3781253652a0679c1a94`; tamanho 79.676.502 bytes, ZIP 35.916.907 bytes. `unzip -t` APK/ZIP PASS e assets preservados; relatório de CI confirma assinatura v2 e testes Standard.
- **Status real:** build e entrega **CONCLUÍDOS**, aceitação visual Samsung Android 16 e partidas reais **PENDENTES**. Preservar backup de HUD, equipe e histórico antes da troca; assinatura debug pode ser diferente.
- **Código:** nenhuma mudança de produção nesta atualização; continuidade/matcher e testes da 0.5.36 preservados.

**Regra obrigatória:** novos builds e documentação devem usar o novo Drive; não republicar links de `12g6IITrn64y0D40YsIdzl-sN9alZty8Q` como destino atual. O link da pasta anterior fica somente nos registros históricos para rastreabilidade. Links `sandbox:` de chats anteriores não são entrega permanente.

---


## CONCLUSÃO DO CI / APK 0.5.36 — 2026-10-09

**Run definitiva:** https://github.com/LucianoToscano/PvPPokeGo-APK-Build/actions/runs/37992092526 — `completed/success` e **15 etapas aprovadas**, incluindo Android SDK36, geração do Game Master offline e atlas, `:app:testStandardDebugUnitTest`, `:app:assembleStandardDebug`, integridade, assinatura e upload.

**Snapshot público do código canônico:** `LucianoToscano/PvPPokeGo-APK-Build@preview/battle-correspondence-0.5.36-20261009`, commit `41a83803e213503af19d99342acd0b80d1be0d2e`; comparação por blob SHA: 129 arquivos necessários à compilação idênticos aos da fonte privada. Mirror não inclui dados pessoais/backups privados.

**Artifact verificado:** `PvPPokeGo-0.5.36-QA-debug` (ID `11644874837`), com `PvPPokeGo-0.5.36-QA-debug.apk`, `SHA256SUMS.txt` e `signature-verification.txt`. ZIP tem integridade válida; APK tem 79.676.502 bytes, SHA256 **`0afeb9c9f1039dc0f4cfe34f2939f5c70d8c46561bab68b62c373bb736e4c36b`**, conferido com o SHA256SUMS gerado no CI e recalculado no arquivo extraído. O APK extraído também passou `ZipFile.testzip()`; possui gamemaster.json, pokemon_icon_atlas.png/json e DEX. `apksigner verify` no CI: v2 true, 1 signatário (QA debug). Links exatos da sessão: `sandbox:/mnt/data/PvPPokeGo-0.5.36-QA-debug.apk` e `sandbox:/mnt/data/PvPPokeGo-0.5.36-QA-debug.zip`.

**Limitação não resolvida no CI:** teste automatizado de Kotlin/Android e integridade do APK não substitui uso real no Samsung Android 16. OCR de escudos/HP, resultado vitória/derrota, capturas reais e persistência dos slots precisam de QA físico; não inventar teste em celular. Pode haver incompatibilidade de assinatura com o APK instalado: exportar backup do HUD/equipe/histórico antes de reinstalar. A continuação de funcionalidades sociais/outros APKs não foi implementada nesta versão específica.

**Pendência operacional:** pedir retorno com screenshots e export de diagnóstico 0.5.36 após batalhas de teste. Não redistribuir backup sensível em repositório público. A versão prévia 0.5.35 permanece preservada.

## 2026-10-09 — 0.5.36 APK QA PRONTO E VERIFICADO

Build FINAL aprovado: https://github.com/LucianoToscano/PvPPokeGo-APK-Build/actions/runs/37992150961 (**completed/success**). Branch pública isolada `preview/battle-correspondence-offline-0.5.36-20261009`, commit `7fd5532cac539fc81559fee44079f88e8d297f33`; dados PvPoke locais e 3 assets de atlas/fingerprints preservados da 0.5.35 **com SHA verificado**. Correções do matcher de batalha, distinção de forma, time e reservas, desambiguação do par de candidatos, confirmação de troca e integridade histórica com testes do núcleo aprovados. Teste Kotlin de `BattleHistorySegmentMatcherTest.kt` corrigido quanto aos quatro parâmetros de evento; `BattleCorrespondenceTrackerTest.kt` agora confirma comportamento conservador de duas evidências em troca simultânea. Não afetar as versões anteriores nem misturar com funcionalidades ainda não implementadas.

**Artifact:** `PvPPokeGo-0.5.36-QA-debug.apk`, 79.676.502 bytes, SHA256 `e1ff7e2ac9e0a5f6e940f1d334bb2c7722409d7018bc3781253652a0679c1a94`, artifact `11645731162`, APK e ZIP validados, assinatura v2=true, 1 signer, versão 0.5.36 code41, link na sessão `sandbox:/mnt/data/PvPPokeGo-0.5.36-QA-debug.apk`. `testStandardDebugUnitTest` e `assembleStandardDebug` **SUCCESS**. **Não houve QA físico Samsung**: salvar backup antes de trocar APK QA/debug, conferir chave de assinatura e testar reconhecimento 3x3, troca de formas regionais, continuidade, HUD, HP, escudos e histórico.

**Documentação detalhada:** [BUILD_0.5.36_OFFLINE_ALTERNATIVE.md](BUILD_0.5.36_OFFLINE_ALTERNATIVE.md). O mirror público é apenas compilação; projeto original fica PRIVADO, sem transferir dados do jogador. **Pendente** módulo Comunidade completo/backend, scanner Avaliar calibrado e correções com evidência física para escudos/charged OCR; não alegar essas funcionalidades como prontas.

---

## 2026-10-09 — Continuação da correspondência 0.5.36 com fallback de build

**Não reiniciar trabalho nem compilar de mirror novo:** o estudo/patch de correspondência está em `feature/battle-correspondence-0.5.36-20261009`. Para o espelho de build, a versão `preview/battle-correspondence-0.5.36-20261009` teve atraso na tarefa de baixar sprites de PokeMiners. Foi derivado o mirror auxiliar `preview/battle-correspondence-offline-0.5.36-20261009` **no repositório público de build já existente**, usando apenas `gh run download` e o SHA256 conhecido do APK QA 0.5.35 para recuperar atlas/fingerprints e preservar todos os ícones. Fonte e espelho privado/público continuam separados.

- Primeira execução offline: [37991507117](https://github.com/LucianoToscano/PvPPokeGo-APK-Build/actions/runs/37991507117). Chegou ao Gradle e revelou erro **somente de teste** em `BattleHistorySegmentMatcherTest.kt` linha 91: `BattleHistoryEvent` exigia quarto argumento. Corrigido de `BattleHistoryEvent(2_000, "PARTIDA", "INÍCIO")` para `BattleHistoryEvent(2_000, "SISTEMA", "PARTIDA", "INÍCIO")`; código do app não teve que ser revertido.
- Commit fonte de teste corrigido: `bd67821e44bf19f21e7effe5343d086e9cfab90a`; mirror offline `a19644936b8b9c8dad0324b3e9ffed5b11e0e934`.
- Segunda execução offline: [37991837778](https://github.com/LucianoToscano/PvPPokeGo-APK-Build/actions/runs/37991837778), verificar status final, testes, SHA256/assinatura e APK. Não informar que o APK está pronto enquanto não existir artifact íntegro verificado.
- Documentação integral: [BUILD_0.5.36_OFFLINE_ALTERNATIVE.md](BUILD_0.5.36_OFFLINE_ALTERNATIVE.md).
- Pendências: teste físico Samsung Android16, checagem de escudos/HP/ativação manual e compatibilidade de assinatura. Não misturar futuro módulo Comunidade com funções já implementadas.

---

## 2026-10-09 — Continuação executada: correspondência de batalhas e reservas 0.5.36

**NÃO REINICIAR.** Pedido expresso: "Continue exatamente do ponto em que você parou — Resolvendo lógica de correspondência de batalhas"; preservar tudo e compilar com testes. Fonte canônica: `feature/battle-correspondence-0.5.36-20261009`, derivada da `feature/verified-core-0.5.35-20261009`. Versão 0.5.36 versionCode41. Detalhes: [`IMPLEMENTACAO_CORRESPONDENCIA_BATALHAS_0.5.36.md`](IMPLEMENTACAO_CORRESPONDENCIA_BATALHAS_0.5.36.md).

**ARQUIVOS EM CÓDIGO alterados:** `engine/BattleCorrespondenceTracker.kt`, `engine/ReserveCardMatcher.kt`, `engine/BattleEngine.kt`, `overlay/BattleOverlayService.kt`, `data/BattleHistorySegmentMatcher.kt`, `data/BattleHistoryRepository.kt`, `app/build.gradle.kts` e 3 testes: `BattleCorrespondenceTrackerTest.kt`, `ReserveCardMatcherTest.kt`, `BattleHistorySegmentMatcherTest.kt`.

**CORREÇÕES VERIFICÁVEIS POR REGRA:** (a) novas partidas só após gap efetivo + dois frames incompatíveis estáveis; latch do gap entre detecções consecutivas enquanto o serviço renova `lastBattleEvidenceAtMs`; (b) apenas membro confiável ancora continuidade do próprio time; (c) reserva do card usa CP, forma/Dex coerentes e identidade exata do ativo, conflito não é confirmado nem por eliminação; não cadastrar membro com CP impossível ou pertencente ao slot de outra espécie; (d) evento OCR abaixo 0.78 não muta motor nem histórico; (e) histórico fragmentado requer interrupção e retomada + identidade overlap, sem fundir partidas vazias ou rematches; leitura do histórico não regrava material bruto. Testes originais de CP impossível Wingull/Vanillite e novo teste Cramorant preservados, com 3 suítes ampliadas.

**BUILD:** espelho público existente `LucianoToscano/PvPPokeGo-APK-Build`, branch `preview/battle-correspondence-0.5.36-20261009`, HEAD `4e9dbfd7057001989fb9b6d61b14d554d0abc283` (arquivos compiláveis conferidos por hashes com source privado). Run definitiva do último snapshot: https://github.com/LucianoToscano/PvPPokeGo-APK-Build/actions/runs/37991096113 . A run `37990812354` é anterior à correção de latch/CP reserva; **não usar seu APK para entrega final**. Acompanhar até teste + compilação + artefato + checksum. Não dizer que passou antes de concluir.

**CONTINUAM PENDENTES:** QA físico Android16 Samsung; OCR de escudos/resultados e previsão HP com video/log; Avaliar IV 0–15 calibrado, scanner live, comunidade/torneios/reides/trocas, backlog de outros APKs. Esses itens não foram incorporados no patch de confiabilidade e não devem ser anunciados como concluídos. Preservar todos os backups/posições do HUD, scanner 0.5.34, ícones, previsão HP 0.5.33 e configurações. Nenhum dado pessoal do usuário ou APK de terceiros copiado ao repositório público.

---

## 2026-10-09 — PvPPokeGo 0.5.35 COMPILADO, TESTADO E ENTREGUE

**CONFIRMAÇÃO REAL:** espelho isolado [run 37974565481](https://github.com/LucianoToscano/PvPPokeGo-APK-Build/actions/runs/37974565481), job 113969447354, **completed/success**. Android SDK 36, Gradle 8.11.1 e Java 17, fingerprints/atlas offline, testes `testStandardDebugUnitTest` e `assembleStandardDebug`, integridade ZIP APK, assinatura v2 e upload do artifact **TODOS PASSARAM**.

**Artefato:** `PvPPokeGo-0.5.35-QA-debug.apk`, 79.660.114 bytes, SHA256 **`4b52dc60ea7ee29da7363a3e061bb72d44aa1377cc1a1106d25b6a54f9257172`**, extraído e hash revalidado contra `SHA256SUMS.txt`. Artifact ID 11638566198, [GitHub Actions URL](https://github.com/LucianoToscano/PvPPokeGo-APK-Build/actions/runs/37974565481), expira 2026-11-08. O APK anterior 0.5.34 é diferente (hash `45e24fdf5d6c9a3c899bda7e08cec8a826231201c1f07f30604c99d3f7bc6d3e`), mas preservado.

**Correções nesta versão:** guard matemático PC x espécie que rejeita Wingull/Vanillite PC1485, aceita Cramorant1485; filtros para batalha, time e fallback OCR; logging anônimo `species_cp_impossible`; cronologia monotônica dos eventos; descarte de sessões sem identidade e sem combate; logs de motivos de encerramento. Scanner 0.5.34, layout/icons/HUD/HP/config backup permanecem. Ver detalhes em [IMPLEMENTACAO_0.5.35_CORE_STABILIZATION.md](IMPLEMENTACAO_0.5.35_CORE_STABILIZATION.md).

**Limites:** build/testes automatizados aprovados **não equivalem a QA físico**. Ainda pendente testar scanner/escudos e OCR ao vivo em Samsung Android16. Biblioteca pública não inclui material sensível. Ideias de apps terceiros continuam backlog quando não houver implementação real; comunidade e backend ainda ausentes. APK debug pode ter assinatura diferente da instalada: backup config, posições HUD e histórico antes de desinstalar.

---

## 2026-10-09 — IMPLEMENTAÇÃO 0.5.35: VALIDAÇÃO PC/ESPÉCIE + HISTÓRICO ÍNTEGRO (BUILD QA EM ANDAMENTO)

**Guia operacional:** [IMPLEMENTACAO_0.5.35_CORE_STABILIZATION.md](IMPLEMENTACAO_0.5.35_CORE_STABILIZATION.md). Branch privada separada `feature/verified-core-0.5.35-20261009`, a partir de 0.5.34 que continua preservada. Espelho público isolado para build `LucianoToscano/PvPPokeGo-APK-Build@preview/verified-core-0.5.35-20261009`. Código versionName=0.5.35 e code40.

**ALTERAÇÕES REALMENTE APLICADAS:** (1) `SpeciesCpPlausibilityPolicy` verifica PC matematicamente permitido pelas estatísticas locais, IV15/15/15 e CPM até nível51, com fallback conservador para catálogo ausente; (2) `GameDataRepository.isSpeciesCpPlausible`; (3) cards de batalha, equipe selecionada, fallback OCR 3 slots filtram falsos pares identidade+CP sem alterar o motor, deixam log anônimo `species_cp_impossible`; (4) `BattleHistoryRepository` nunca grava `elapsedMs` regressivo após OCR assíncrono, registra motivo do finish em todas as rotas, descarta fragmento sem identidade nem combate observável; (5) testes `SpeciesCpPlausibilityPolicyTest` com casos reais Wingull1485 impossível, Vanillite1485 impossível e Cramorant1485 possível; (6) Gradle 0.5.35 /40.

**BUILD/TESTES:** GitHub Actions público isolado [run 37974565481](https://github.com/LucianoToscano/PvPPokeGo-APK-Build/actions/runs/37974565481). Em execução quando este bloco foi criado; verificar status **antes** de declarar APK compilado. O source do espelho foi sincronizado só para arquivos necessários; nenhum arquivo pessoal/backup do jogador foi publicado. Não confundir build/CI com QA físico Samsung Android16.

**NÃO IMPLEMENTADO NESTE PATCH:** comunidade, backend de amigos/clãs, convites de reide, comércio, calibração automática barras IV Avaliar, login/account, injeções de outros APKs; estudos Calcy/Poke Genie/PokeRaid/GO Hub/Regibot seguem como backlog, não chamar de recursos entregues. P0 ainda precisa validar escudos e Charged OCR x energia (auditoria 0.5.33 encontrou todos shields2 e 14/20 charged inferidos). Preservados HUD, controles, iconografia, scanner 0.5.34, overlay, HP fantasma, modos de equipe, backup/posições.

---

## 2026-10-09 — AUDITORIA DOS EXPORTS REAIS 0.5.33 (CONFIG / DIAGNÓSTICO / HISTÓRICO / APK)

**Documento integral:** [AUDITORIA_EXPORTS_0.5.33_2026-10-09.md](AUDITORIA_EXPORTS_0.5.33_2026-10-09.md). O usuário enviou 4 arquivos físicos e todos foram lidos/validados. ZIPs e APK íntegros; config do ZIP de diagnóstico idêntico byte a byte ao JSON individual; JSON de histórico idêntico entre ZIPs; CSV possui exatamente **464 eventos e 7 sessões**. Não copiar anexos brutos ao repositório por privacidade. Hashes/proveniência constam no estudo.

**ACHADOS OBJETIVOS:** todos os resultados das 7 sessões são null — não atribuir vitórias, derrotas ou Elo. 4 sessões acabam sem evidência há 28s, 2 por detecção de nova partida, 1 por parada voluntária; 1 sessão sem identidades Pokémon. 1 inversão temporal do histórico (211911->209790ms, diferença -2121ms). 20 carregados adversários, 14 (70%) sem nome e inferidos por energia/faixa — NÃO equivalem a golpes confirmados. Nenhum rápido adversário explicitamente registrado. Os 282 snapshots de estado têm shields=2 (não demonstra uso real ou ausência de uso de escudo, apenas carência de atualização no log). Logcat-app contém somente evento de exportação, insuficiente para testar crash. Runtime lista 7 session|started e 4 session|ended, sem fechamento de todas as vias.

**BUG P0 DE CONSISTÊNCIA:** o histórico atribui Wingull **PC 1485** e Vanillite **PC 1485**, ambos fisicamente impossíveis conforme gamemaster e CPM do próprio APK (máximo no nível 51: Wingull PC 875; Vanillite PC 1182). Config manual contém Cramorant PC 1485, espécie válida para esse PC. É plausível erro de identificação/associação com CP de Cramorant, mas SEM VÍDEO NÃO PROVAR CAUSA. Código de AllyOcrRecoveryPolicy na branch 0.5.34 valida somente cp entre 10..10000 e OCR confidence >=0.90 nessa política. Exigir validação CP x espécie/forma e comparação com time manual antes de aceitar identidade/troca; registrar rejeições com confiança e motivo. Testes necessários antes de release.

**P0 PENDENTE:** corrigir inicio e continuidade das sessões, distinguir charged inferido vs OCR observado, HP/escudos com observações confiáveis e estados desconhecidos, reduzir avisos assertivos sob baixa confiança. **P1:** ordenação monotônica CSV/JSON, breadcrumbs em todos fechamentos, ring buffer interno sem dados privados, backup preservando posições HUD e evitando inferir estado Manual/Auto a partir do snapshot atual. Código fonte e APK 0.5.34 QA preservados.

**STATUS:** auditoria estática e documentação somente; nenhum Kotlin modificado, sem compilação, APK novo, Gradle ou QA físico nesta etapa. Arquivo APK 0.5.33 agora foi realmente recebido; status histórico de CI bloqueado deve ser entendido como uma fotografia do momento anterior, não ausência do anexo atual. Verificação de assinatura Android permanece pendente.

---

## 2026-10-09 — Confirmação importante: "Projeto do Regibot_v2.3.3.0 Criação Original.apk" é IDENTICO a Regibot_v2.3.32.apk

Upload descrito como compilação original Grok foi comparado localmente com Regibot_v2.3.32.apk já analisado. **Tamanho dos dois: 52.937.403 bytes; SHA-256 idêntico `6f08a94c19b7d84b42e412d51cbe5f583732606f3681bee5d7b323b344bdb6b7`; comparação binária `cmp -s` exit 0; ZIP íntegro, 940 itens.** É O MESMO APK com nome diferente; package `com.regitools.regibot` e versionName interno **2.3.32**, apesar do nome da cópia citar `2.3.3.0`. Isto **NÃO é entrega de código-fonte original**. Não analisar como versão separada, nem inferir mudanças inexistentes. Estudo de recursos e salvaguardas já registrado em [`ESTUDO_REGIBOT_2.3.32_PVPPokeGo.md`](ESTUDO_REGIBOT_2.3.32_PVPPokeGo.md), com adendo de identidade binária. **Nenhuma alteração funcional nem APK do PvPPokeGo**. Documentação privada apenas.

---

## 2026-10-09 — REGIBOT 2.3.32 (APK DO PRÓPRIO USUÁRIO) INSPECIONADO — PESQUISA E ROADMAP, SEM BUILD

Usuário informou que criou o `Regibot_v2.3.32.apk` e autorizou análise. Arquivo comprovadamente examinado na sessão: `52.937.403` bytes, SHA256 `6f08a94c19b7d84b42e412d51cbe5f583732606f3681bee5d7b323b344bdb6b7`, ZIP integralmente testado, Android manifest package `com.regitools.regibot` versão 2.3.32 code45, minSdk26/target36. Análise **ESTÁTICA SEM INSTALAÇÃO OU EXECUÇÃO**; nenhum segredo, token, binário, asset ou código do Regibot copiado para repo.

**DOCUMENTO CANÔNICO COMPLETO:** [`ESTUDO_REGIBOT_2.3.32_PVPPokeGo.md`](ESTUDO_REGIBOT_2.3.32_PVPPokeGo.md). Arquitetura com 5 esquemas JSON (`catchemall` 15 seções/119 campos, `grunt` 11/95, `raid` 10/71, `regiplay` 10/71, `sniper` 10/38); 394 campos / 344 não-nota, sem IDs duplicados por arquivo, mode/feature/sections/typed fields/defaults/file. APK contém 31 activities, serviços de foreground/boot, classes ConfigManager/SaveLists/SaveFilterSets/WorldClock/Pokedex/RegiPlayScanner, asset de catálogo Pokémon (1250 linhas, 1025 IDs únicos), mapa 1473 formas bidirecional, Leaflet 1.9.4 BSD-2-Clause com obrigações de licença. `pokemon.json.type` significa categoria ('normal', 'form', 'legendary', 'mythical'), **NÃO O TIPO ELEMENTAL**; não importar para matemática PvP. Existem ELF arm64 de injeção e scripts/bridges que **NÃO devem ser executados nem transferidos**.

**O QUE VALE ADAPTAR AO PvPPokeGo:** configuração schema-driven versionada + ON/OFF cinza/colorido, validação e rollback de backup, perfis de filtro/time e import-export, scanner por espécie/forma/IV/sexo/ligas + ajuste de exibição, preparação de reide/Rocket/Dinamax puramente consultiva, calendário/relógio mundial e mapa de eventos opt-in, diagnóstico do próprio app e checklists. NÃO transpor teleporte, injeção, auto-captura, automação de batalha, autenticação arbitrária, permissões desnecessárias. O Pokémon GO proíbe localização falsa, software modificado/acesso não autorizado; fonte na documentação. Mesmo sendo do autor, revisar licenças dos assets e dependências.

**STATUS REAL:** apenas relatório/documentos privados nesta branch, **SEM MUDANÇAS DE CÓDIGO/APK**. A versão 0.5.34 QA e o Guia Mestre anterior são preservados. Propostas dependem de autorização posterior e validação do HEAD antes de codificar. Integrar com `ESTUDO_CALCY_POKERAID_GOHUB_0.5.35.md` e `PROPOSTA_COMUNIDADE_TREINADORES_0.5.35.md`.

---


## 2026-10-09 — ESTUDO DOS XAPK CALCY IV 3.44, POKERAID 0.48.16 E GO HUB (SOMENTE PESQUISA)

O usuário forneceu os dois arquivos XAPK originais deste chat e seis screenshots de PokeRaid (Amizade, Seus Clãs, Trocar/lista desejos/ofertas, scanner). Os pacotes foram inspecionados **ESTATICAMENTE (sem instalar/executar)** e o site https://db.pokemongohub.net/pt e páginas de tier lists, DPS/TDO, bônus Mega e disclaimer foram consultadas online. **Relatório completo obrigatório:** [`ESTUDO_CALCY_POKERAID_GOHUB_0.5.35.md`](ESTUDO_CALCY_POKERAID_GOHUB_0.5.35.md). Integra-se ao backlog [`PROPOSTA_COMUNIDADE_TREINADORES_0.5.35.md`](PROPOSTA_COMUNIDADE_TREINADORES_0.5.35.md).

**Fatos verificados:** Calcy `tesmath.calcy` v3.44 XAPK SHA256 `fe98aa69446630bf526326bdc723de67db678f2e90055aff6e30235586dabeb5`, splits base/arm64/xxhdpi; contém OCR Tesseract, tela/overlay de scan, PvP IV/meta e SQLite `gamestats.db` com tabelas de espécies, movimentos PvE/PvP, evoluções, efeitos, shadow/mega e traduções (inspecionado SOMENTE o esquema). PokeRaid `me.pokeraid` v0.48.16 SHA256 `2dced3bd0a7400acd2cfa7e4b228c8af58c630144f4e01f99f084050d23e241c`, splits base/arm64/pt/xxhdpi; contém recursos ML Kit OCR, layouts de matchmaking PvP, salas/coordenador de reides, clãs, amizade, ofertas/trocas, histórico, notificações e smart IV. Nomes de recursos NÃO comprovam funcionamento e nenhum APK/asset/modelo/dado de terceiros foi copiado para o PvPPokeGo.

**GO Hub:** https://db.pokemongohub.net/pt oferece PvP/PvE tier lists, Pokédex, tipos, moves, CP calculator, Hundo odds, DPS/TDO, Megas e Dinamax. Copyright/aviso legal `https://db.pokemongohub.net/pt/legal/disclaimer` com direitos reservados e créditos PvPoke/Dialgadex; **não interpretar como licença para copiar/scrapear/republicar dados**. Priorizar links/fonte atribuídos, API autorizada e datasets licenciados auditados; sempre distingua Rank IV de tier meta e PvP/PvE.

**NOVOS ITENS do backlog 0.5.35+:** P0 calibrar barras IV de avaliação e corrigir confiança; completar custos de fortalecer/evoluir com fontes autorizadas; enriquecer golpes e efeitos; listas de ofertas/desejos LOCAIS com compartilhamento; P1 guia GO Hub com links, preparador de reide DPS/TDO, duelos/agenda; P2 clãs/chat/salas/torneios/trocas online exigem backend, privacidade, moderação, antiabuso. Não confundir códigos de amizade/Battle Code/grupo. Interface deve evitar títulos cortados do PokeRaid e permanecer clean.

**ESTADO:** o APK 0.5.34 QA e branch de implementação permanecem intocados em código; registro nesta branch privada apenas em `docs/`. Nenhuma nova compilação ou APK feito. O usuário não autorizou implementação integral das ideias nesta mensagem, pediu estudo e sugestões. Preservar toda versão 0.5.34 e anteriores.

---

## 2026-10-09 — Nova proposta: comunidade, treinador, duelos, reides PvE e eventos (PENDENTE)

Usuário compartilhou capturas reais das telas de perfil e código QR de treinador e pediu aproveitar perfil, código de amigos, duelos, eventos e PvE no PvPPokeGo. **Não confundir com recurso já implementado:** a compilação entregue 0.5.34 **NÃO contém essas funções sociais**.

**Proposta técnica/análise funcional completa:** [`docs/PROPOSTA_COMUNIDADE_TREINADORES_0.5.35.md`](PROPOSTA_COMUNIDADE_TREINADORES_0.5.35.md), criada na branch privada `feature/team-scan-0.5.34-implementation` e não enviada ao espelho público (somente documentação). Proposta candidata a uma próxima fase 0.5.35+ conforme validações.

**Conceito:** tela "Comunidade" com Meu Treinador (nickname, nível, facção, código 12 dígitos e QR/carta de apresentação compartilhável), cadastro voluntário de amigos/treinadores no PvPPokeGo, convites **de coordenação** para duelo PvP, reides PvE e eventos, equipes recomendadas, inscrição/agenda e histórico de resultados. Prioridade de fase 1 offline/local com Android Share Sheet + OCR via imagem selecionada e revisão do código, reaproveitando `TrainerPreferencesRepository` existente. Fases sociais online (confirmação entre usuários, lobby, push, moderação) dependem de backend seguro e autorização/consentimento.

**LIMITES oficiais:** amizade por Trainer Code/QR nativo de 12 dígitos; duelo próximo usa Battle Code QR diferente; duelo remoto entre amigos acontece **dentro do Pokémon GO**; reides dependem de convite nativo, passes e regras da Niantic; Party Play usa QR/código temporário diferente. Sem API confirmada de integração, o app NÃO pode alegar ter efetivamente adicionado amigo, iniciado duelo/raid ou entrado no lobby do jogo. Fontes oficiais vinculadas na proposta.

**SEGURANÇA:** usuário mostrou QR/código pessoal em imagem; NUNCA transcrever código real, QR, nome da conta nem compartilhá-los em GitHub público/diagnósticos/logs. Perfil social privado por padrão, compartilhamento opt-in; locais aproximados e regras LGPD; moderação/bloqueio para futuro componente online. Não copiar assets originais do jogo. Preservar 0.5.34 e todas as funções anteriores do scanner/HUD/overlay, sem alterações ao APK nesta etapa.

---

## 2026-10-09 — 0.5.34 CI SUCCESS e APK QA verificado

**Compilação da fase 1 do Scanner de Equipe 0.5.34 concluída em run real:** https://github.com/LucianoToscano/PvPPokeGo-APK-Build/actions/runs/37917670306 `completed/success` job `113777712647`. Repo público compilador existente `LucianoToscano/PvPPokeGo-APK-Build` branch `preview/team-scan-0.5.34-20261009`, SHA `1dc7a4ba2d9e073f53523392cbd4fb5356bbd8ea`, snapshot sanitizado da fonte privada commit `286523513cc0d6fc7c310b2cb06027e79124f966`. `:app:testStandardDebugUnitTest`, `:app:assembleStandardDebug`, assets offline, atlas, testes de integridade e assinatura passaram.

**APK real entregue:** `PvPPokeGo-0.5.34-QA-debug.apk`, **79.660.114 bytes**, SHA-256 `45e24fdf5d6c9a3c899bda7e08cec8a826231201c1f07f30604c99d3f7bc6d3e` (confere com SHA256SUMS.txt do Actions), assinatura v2 válida, único assinante; artifact ID `11611096799`, extraído após download. Link da sessão: `sandbox:/mnt/data/PvPPokeGo-0.5.34-QA-debug.apk` e backup ZIP de artifact também acessível durante sessão.

**CÓDIGO realmente incluído:** importação da foto da ficha para cada vaga, OCR de espécie/PC/PS/golpes com confirmação manual, cálculo IV rank 4096 combinações + PC nível 50, análise energética dos golpes e histórico de scan local/backup. **Não concluído:** interpretação geométrica efetiva das três barras de Avaliar (botão existe mas não produz IV confiável), captura automática ao vivo do Pokémon GO, custos de poeira/doces, QA físico; não afirmar que todos os cinco subprojetos estão completos. Revisar [`IMPLEMENTACAO_SCANNER_EQUIPE_0.5.34.md`](IMPLEMENTACAO_SCANNER_EQUIPE_0.5.34.md) e checklist. Preservar base e layout 0.5.33 e continuar em branch de implementação, não repetir CI sem necessidade.

---

## 2026-10-09 — Implementação REAL iniciada: Scanner de Equipe 0.5.34 (não confundir com planejamento)

**Branch de implementação isolada:** `feature/team-scan-0.5.34-implementation` derivada de `feature/team-scan-0.5.34-planning`. Fontes modificados/criados: `ui/TeamBuilderPanel.kt`, `data/TeamScanParser.kt`, `detect/TeamScreenshotScanner.kt`, `data/TeamPvpTools.kt`, `data/TeamScanHistoryRepository.kt`, `data/SettingsRepository.kt`, `app/build.gradle.kts`, 2 testes unitários.

**FUNÇÕES IMPLEMENTADAS (primeira fase):** importar screenshot da ficha para vaga Pokémon 1/2/3 mediante seleção explícita, reconhecer com ML Kit espécie, PC, PS e golpes legais do catálogo e nomes PT-BR, revisar/confirmar antes de salvar, impedir conflito de espécie, mesclar campos apenas na edição da vaga, reusar scans confirmados pelo histórico local (sem reter screenshot), incluir histórico no backup, mostrar rank de IV por liga via 4096 combinações usando CPM do PvPoke offline, PC potencial no nível 50 e eficiência dos golpes ágil/carregado (tempo, energia e DPE). Nenhuma alteração automática da equipe ativa nem do motor de batalha, e não houve inclusão do APK Poke Genie.

**PENDÊNCIAS importantes:** botão Avaliar aceita screenshot e OCR, mas leitura real das barras gráficas IV **AINDA NÃO ESTÁ CALIBRADA**: por segurança, IVs não são inferidos automaticamente, precisando entrada manual/QA com imagens reais. Captura AO VIVO do jogo via MediaProjection e custos EXATOS de fortalecimento também são futuras fases. Ranking IV não é ranking competitivo do meta. Nunca dizer que tudo isso foi totalmente finalizado.

**BUILD DE QA acionado no espelho público existente** `LucianoToscano/PvPPokeGo-APK-Build`, branch `preview/team-scan-0.5.34-20261009`, commit `1dc7a4ba2d9e073f53523392cbd4fb5356bbd8ea` a partir do private code commit `286523513cc0d6fc7c310b2cb06027e79124f966` (futuras mudanças DOCS no HEAD não alteram esse snapshot de compilação). Workflow `.github/workflows/team-scan-0.5.34.yml`, run https://github.com/LucianoToscano/PvPPokeGo-APK-Build/actions/runs/37917670306 ; status inicial EM PROGRESSO, resultados finais e link do APK só após verificação real da CI.

**DETALHES / LIMITES / TESTES:** [`docs/IMPLEMENTACAO_SCANNER_EQUIPE_0.5.34.md`](IMPLEMENTACAO_SCANNER_EQUIPE_0.5.34.md). Preservar totalmente 0.5.33 e HUD/posições, reconhecimento aliado/reservas, logcat/HP, histórico de batalha; corrigir primeiro na branch canônica se build falhar. Para próxima geração APK, revisar assinatura e SHA256. Manter Google Drive atualizado no resultado.

---


## 2026-10-09 — Análise estática aprofundada do APK Poke Genie concluída (planejamento 0.5.34)
Após o usuário pedir “estude ele então. Veja o que podemos aproveitar dele”, foi analisado o arquivo APK real enviado, não apenas as imagens. Hash SHA256 da amostra: `0c668336c534361aaad76021f9ab52b2bd9bd625df3b0ff83f524ab7f5eb347d`, manifesto `com.cjin.pokegenie.standard` versão 8.19.0/build 416. Evidências: telas/activities de scan, iVision, Avaliar IV, PvP IV/ranking gráfico, golpes, evolução e custos, histórico/merge, nomes personalizados, tipos, simulação e raids; bibliotecas `libtesseract`, `libleptonica` e tessdata eng/números/idiomas; datasets `assets/plists/*` (movesets PvP, CPM, evolução, poeira e tipagem). Permissões declaradas de overlay/MediaProjection. O ZIP externo também contém `assets/yrf.apk` (~84 MB), um APK ZIP aninhado. **Não presumir originalidade, modificação, malware ou funcionalidades plenamente operantes somente pelos nomes**; o usuário esclareceu que ele próprio inseriu “Premium” no filename, e esta explicação permanece respeitada.

**RELATÓRIO TÉCNICO COMPLETO e plano auditável:** [`ESTUDO_APK_POKE_GENIE_0.5.34.md`](ESTUDO_APK_POKE_GENIE_0.5.34.md). Funcionalidades escolhidas: (P0) Scanner de três slots por screenshot de ficha + Avaliar, validação de confiança e confirmação manual; (P1) scanner overlay consentido com classificador de contexto, rank IV por liga, golpes/energia; (P2) histórico scans/custos/apelidos. Reusar `TeamBuilderPanel`, `TeamSetupRepository`, `BattleOcrDetector` ML Kit, `PokemonVisualRecognizer` e Game Master — **sem copiar** binários, interfaces, dados e algoritmos proprietários. Prioridade de preservar modo Auto/Manual, histórico/diagnósticos, overlay/reservas, HUD/ícones/posições. Não construir funcionalidades de lobby remoto/loja/raids como pré-requisito de scanner PvP.

**Estado:** documentos atualizados na branch privada `feature/team-scan-0.5.34-planning`; não há implementação Kotlin do scanner nem APK 0.5.34. Base última compilada/validada em CI é 0.5.33. Prosseguir em branch funcional separada e usar mirror público de APK existente só quando implementar/compilar.


## 2026-10-09 — Solicitação de Scanner 1/2/3 e referência ao APK Poke Genie (v0.5.34, somente planejamento)

O usuário apresentou duas capturas da ficha do **Cramorant** e um APK de terceiros nomeado “Poke Genie ... Premium ... Fixed ... TekMods.com.apk”, pedindo scanner similar para as vagas Pokémon 1, 2 e 3 da tela SUA EQUIPE, com preenchimento automático dos campos, e mais ideias funcionais do Genie.

**Decisão técnica:** a primeira fase deve reutilizar **ML Kit**, `BattleOcrDetector`, `TeamBuilderPanel`, `TeamSetupRepository`, `ManualTeamPolicy`, `gamemaster.json`, nomes de golpes PT-BR e o modo Manual/Automático existentes, sem sobreposição direta de captura em outros apps sem consentimento. Fluxo previsto: selecionar vaga -> importar 1 ou 2 screenshots -> extrair campos com confiança/fonte -> opcional screenshot “Avaliar” para IV -> prévia editável -> salvar vaga -> escolher equipe atual depois dos 3. Captura em tempo real por MediaProjection consentida fica fase posterior, após QA e preservação do HUD.

**Evidência exemplo das imagens:** Cramorant PC 1485, PS 126/126, feminino, Voador/Água, Bicada, Mergulho, Voar. Indicadores 6/9/14 parecem parte do apelido, não comprovam IVs; requerem avaliação oficial ou confirmação manual. As formas Engolidora/Devoradora são efeitos transitórios de batalha e NÃO duas espécies diferentes no cadastro permanente.

**APK de referência fornecido pelo usuário:** ZIP inspecionado de modo estático apenas, sem execução/instalação. Contém nomes de recursos `ivision_overlay`, `scan_details`, `dialog_input_appraisal`, `activity_pvp_iv_settings`, `move_list`, `battle_team_overlay`, OCR Tesseract e tabelas locais; estes nomes não provam funcionamento nem segurança. **RETIFICAÇÃO DO USUÁRIO 2026-10-09:** o nome "Premium" foi acrescentado pelo próprio usuário, por estética, e ele declara tratar-se da versão normal. Assim, NÃO afirmar que o APK foi modificado somente pelo filename. Seguir autorizado a analisar o arquivo tecnicamente e usar conceitos funcionais como referência, sem assumir licença sobre código/assets/datasets de terceiros. Implementação própria compatível com o PvPPokeGo, preservando o repositório privado e o espelho público de compilação separados.

**Documentação integral de critérios/arquitetura/testes:** [`docs/PROPOSTA_SCANNER_EQUIPE_0.5.34.md`](PROPOSTA_SCANNER_EQUIPE_0.5.34.md).

**Estado real:** criada branch isolada `feature/team-scan-0.5.34-planning` a partir de `feature/diagnostics-hp-ui-0.5.33`. Só documentação; nenhum código Kotlin do scanner, testes nem novo APK foram feitos. A 0.5.33 compilada em 2026-10-09 continua base de uso; não alegar que o scanner já está na 0.5.33. Implementar em fases; preservar posições HUD, histórico, Logcat, HP, overlay, reservas/matcher e switch Auto/Manual. Se build futuro for autorizado, usar repositório público de compilação previamente validado.

## REGRA PRIORITÁRIA — BUILD/APK POR ESPELHO PÚBLICO EXISTENTE

**ANTES de tentar GitHub Actions no repositório original privado `LucianoToscano/PvPPokeGo`, consulte ESTA seção e os checkpoints históricos de 2026-10-07.** O GitHub Actions desse repositório já foi bloqueado por problema de faturamento/conta em 2026-10-07 e 2026-10-09; repetir workflows privados pode falhar antes de iniciar o Gradle.

**Solução comprovada e autorizada anteriormente:** repositório público *isolado exclusivamente para compilar*, https://github.com/LucianoToscano/PvPPokeGo-APK-Build (não criar outro sem motivo). Ele contém snapshots isolados por `preview/...`, com workflows Standard que já geraram os APKs QA da 0.5.29 e 0.5.31. O repositório original permanece privado e canônico.

**Procedimento obrigatório em futuras versões:**
1. Conferir a branch, o HEAD e o Guia Mestre **do repositório privado**.
2. Conferir o repositório público existente, HEAD da última branch preview, workflow, runs e artefatos. **Nunca afirmar que não há alternativa de build antes dessa verificação.**
3. Criar branch `preview/<versão>` no repositório público, comparando SHA de blobs com a fonte canônica; copiar apenas a lista revisada de código/recursos necessários para build, **sem histórico privado, tokens, keystore, dados de usuários, backups, arquivos confidenciais ou credenciais**.
4. Adaptar workflow de build Standard testado para versão/branch, incluir testes Gradle `:app:testStandardDebugUnitTest`, `:app:assembleStandardDebug`, empacotamento dos assets e verificações de APK, assinatura e SHA256.
5. Só declarar APK pronto após **run success**, confirmação de SHA256 e identificação do artefato gerado; jamais renomear APK antigo, presumir sucesso porque o workflow foi iniciado, ou afirmar testes físicos no Samsung sem executá-los.
6. Atualizar este Guia Mestre, checklist e espelho Google Drive com branches, commits privado/público, URL do run, checksum, nome de artefato, correções e QA pendente. Não sincronizar o mirror automaticamente nem tornar o repositório canônico público.

**Situação nova 0.5.33 (2026-10-09):**
- Origem privada: `feature/diagnostics-hp-ui-0.5.33`, HEAD de fonte `3b316f6fcaf7cf7726ca324548d9ce2427be70c5` (guia pode alterar HEAD posteriormente).
- Branch pública criada: `preview/diagnostics-hp-ui-0.5.33-20261009`, HEAD `655514514f1cdfa4ce89b7848aa36994dc639f27`.
- Snapshot derivado do `preview/team-recognition-0.5.32-20261009`, commit `11600081071b78348ae9fedf8620421c194cd3b8`; comparação dos arquivos `app/` revelou 20 diferenças (código Kotlin, testes, recurso XML e novo logger), sincronizadas por blobs idênticos ao Git original. Novo workflow público `.github/workflows/diagnostics-hp-ui-0.5.33.yml` adaptado do workflow da 0.5.31 aprovado.
- **Run público iniciado**: https://github.com/LucianoToscano/PvPPokeGo-APK-Build/actions/runs/37903983424 ; resultado e APK DEVEM ser conferidos, não presumidos.
- Histórico do espelho público já constava neste Guia desde 2026-10-07 nos registros `Espelho público isolado de compilação Standard`, versões 0.5.26–0.5.31. **Erro de processo identificado:** na preparação da 0.5.33, não foi relida/seguida essa seção, levando à tentativa redundante de Actions privados e instruções desnecessárias ao usuário para resolver billing.

---

# PvPPokeGo — Guia Mestre de Continuidade e Registro

> **ARQUIVO CANÔNICO DO PROJETO**
>
> Este documento é a fonte principal de continuidade do PvPPokeGo.
> Toda nova conversa, correção, melhoria, build, teste ou entrega deve começar por aqui.
> Quando houver divergência entre documentos antigos e este guia, primeiro verificar o estado real do GitHub e então atualizar este arquivo.

## 1. Regra permanente de continuidade

**MELHORAR E ORGANIZAR, NUNCA EMPOBRECER.**

Antes de qualquer trabalho:

1. abrir este documento;
2. verificar a branch real em desenvolvimento;
3. verificar o HEAD atual;
4. conferir PRs e workflows relevantes;
5. ler o checklist da versão atual;
6. identificar o último passo realmente concluído;
7. continuar dali, sem reiniciar a análise;
8. preservar código, testes, histórico, documentação e decisões anteriores;
9. não voltar para uma versão antiga apenas porque foi citada em uma conversa;
10. nunca afirmar build/APK concluído sem artefato real e validação correspondente.

Proibido como rotina:
- `git reset --hard` ou equivalente destrutivo sem autorização explícita;
- renomear APK antigo como versão nova;
- marcar item como concluído sem evidência;
- repetir indefinidamente a mesma pesquisa/validação sem novo sinal;
- criar implementação paralela quando já existe módulo que pode ser evoluído.

## 2. Regra obrigatória de documentação

**Toda mudança relevante deve ser registrada neste guia antes de considerar a etapa encerrada.**

Isso inclui:
- correção de bug;
- alteração de UI/HUD;
- mudança de ícone/arte;
- nova feature;
- mudança de lógica matemática;
- mudança de banco/dataset;
- alteração de arquitetura;
- novo teste;
- mudança de workflow/build;
- APK gerado;
- upload para Drive;
- mudança de branch/versão;
- decisão de adiar ou remover algo;
- bloqueio conhecido.

Cada registro deve conter, quando aplicável:

- data;
- versão;
- branch;
- commit;
- problema/objetivo;
- o que foi alterado;
- arquivos principais;
- testes executados;
- resultado;
- status: `CONCLUÍDO`, `PARCIAL`, `BLOQUEADO` ou `ADIADO`;
- pendências;
- próximo passo.

**Não apagar registros antigos.**
Quando algo mudar, adicionar um novo registro explicando a correção/decisão.

## 3. Fonte de verdade atual

> **Atenção:** Os campos abaixo registram o estado HISTÓRICO 0.5.26. O estado atual e os links oficiais da 0.5.36 são os da seção 0 no início deste arquivo. Não tratar os campos antigos como status vigente.


### Fonte canônica

GitHub:
https://github.com/LucianoToscano/PvPPokeGo/blob/feature/battle-correspondence-0.5.36-20261009/docs/PROJECT_CONTINUITY_GUIDE.md

Espelho no Google Drive:
https://docs.google.com/document/d/1CfSJ8o0r3qnoq3MSsfozYD1M5C07A1pL/edit?usp=drivesdk

**Regra:** o GitHub é a fonte canônica versionada; o Google Drive é um espelho de consulta/continuidade. Quando houver divergência, verificar o estado real do repositório e atualizar os dois.

Projeto: **PvPPokeGo**  
Repositório: https://github.com/LucianoToscano/PvPPokeGo  
Branch em desenvolvimento: `release/0.5.26`  
PR: **#34**  
Pacote: `com.lucianotoscano.pvppokego`  
Versão: `0.5.26`  
versionCode: `31`

Checklist atual:
- `docs/CHECKLIST-PvPPokeGo-0.5.26.md`

Handoff da release:
- `docs/RELEASE-0.5.26-HANDOFF.md`

Pesquisa externa:
- `docs/research/PVP-EXTERNAL-REFERENCES.md`
- `docs/research/pvp-sources.lock.json`

Último checkpoint de lógica validado antes dos commits de documentação:
- `356f35cf11008921f500ad8fffdcfb891dc344fc`

Commits recentes de documentação:
- `8a38570e04d5ab1512167ab90b117637fb1ec423` — checklist 1–80;
- `34f22208f4a27fa84d0b96e96fe7eb29b839418e` — checklist complementar PBE.

> Sempre confirmar o HEAD real da branch antes de trabalhar. Os hashes acima são checkpoints documentados, não substituem a verificação ao vivo.

## 4. Estado do build

Variante prioritária: **Standard**.

Recorder Compat/gravação:
- código preservado;
- etapa adiada por decisão do projeto;
- não deve bloquear a entrega Standard.

Última validação lógica documentada:
- **132 PASS / 0 FAIL** no harness JVM atual;
- validação isolada do `BattleAssistAdvisor`: PASS.

Importante:
- ainda não existe APK legítimo `0.5.26`;
- runs recentes do GitHub Actions terminaram antes de receber runner (`steps: null`);
- nunca renomear o APK 0.5.22 como 0.5.26.

## 5. Bugs prioritários atuais

### 5.1 Ícones dos Pokémon de reserva — PARCIAL

Sintomas observados em aparelho real:
- cards laterais podem ficar em `?` / `IDENTIFICANDO`;
- Pokémon 2/3 podem não exibir a imagem correta;
- tela de troca também pode ficar sem o ícone correto.

Arquivos prioritários:
- `BattleOverlayView.kt`
- `BattleOverlayService.kt`
- `BattleOcrDetector.kt`
- `PokemonIconAtlas.kt`
- `ReserveCardMatcher.kt`
- `Models.kt`

Fallback desejado:
1. espécie/forma exata no atlas;
2. Dex quando não ambíguo;
3. retrato estável da seleção da equipe;
4. retrato/crop do card nativo;
5. placeholder somente em último caso.

Regra:
**não esconder o ícone apenas porque matchup/recomendação ainda está ANALISANDO.**

Status: **PARCIAL — busca de cards ambíguos e fallback do retrato estável corrigidos; validação em APK real pendente**.

### 5.2 Ícone oficial do aplicativo — ABERTO/PARCIAL

Arte oficial aprovada:
Poké Ball com setas vermelho/laranja e azul/ciano em moldura triangular.

A arte já existiu no projeto, mas o aparelho mostrou novamente o ícone genérico do Android.

Não considerar concluído até validar:
- `android:icon`;
- `android:roundIcon`;
- `@mipmap/ic_launcher`;
- `@mipmap/ic_launcher_round`;
- adaptive icon foreground/background;
- launcher Samsung;
- APK realmente instalado.

Status: **PARCIAL / NÃO VALIDADO NO APK FINAL**.

## 6. Motor matemático/preditivo

O projeto já possui base para:
- energia min/likely/max;
- Fast Move hypotheses;
- residual;
- overfarm;
- back-to-back Charged;
- ChargedMovePredictor;
- DamageForecastEngine;
- ShieldDecisionEngine;
- FastMovePhaseTracker;
- TacticalForecastEngine;
- CMP por range;
- Consistency Score;
- recuperação após frames perdidos;
- evidência observada × matemática × inferência.

Regra:
**probabilidade comportamental nunca substitui matemática determinística.**

Documento detalhado no checklist:
- seção PBE em `docs/CHECKLIST-PvPPokeGo-0.5.26.md`.

## 7. Fontes externas já auditadas

Não repetir a pesquisa sem necessidade.

- PvPoke: `pvpoke/pvpoke@f627e89e53c0c7b903fff097df7a0ad0ac95decc`
- GoPvPSim: `mglerner/gopvpsim@0952963f04bbc313c042a422a3d042c066678639`
- PokeMoves: `jamiehumphries/pokemoves@bd9ac2e2bb4df8dd72337d1aab299e04d42a3093`
- PoGO GBL Analyzer: `crsiebler/pogo-gbl-analyzer@d4dd848f29ff6b929eca5efe69b419aeefffef6d`

Uso:
`upstream -> atualização offline -> validação/diff -> assets locais -> engine Kotlin`

Durante a batalha:
`frames/OCR -> eventos -> estado matemático -> predictor -> overlay`

Sem dependência web no hot path.

## 8. Checklist permanente para encerrar qualquer alteração

Antes de dizer que uma mudança terminou:

- [ ] código atual verificado;
- [ ] alteração aplicada;
- [ ] funcionalidades existentes preservadas;
- [ ] teste relevante executado;
- [ ] checklist atualizado;
- [ ] este Guia Mestre atualizado;
- [ ] commit registrado;
- [ ] limitações/pendências registradas;
- [ ] se houve APK: build, Manifest, assets, ícone, assinatura e SHA-256 validados;
- [ ] se houve entrega: GitHub/Drive atualizados e link final registrado.

## 9. Template obrigatório de registro

Copiar este bloco ao final da seção **Registro cronológico**:

```text
### AAAA-MM-DD — Título curto

Versão:
Branch:
Commit:

OBJETIVO/PROBLEMA:
-

ALTERAÇÕES:
-

ARQUIVOS PRINCIPAIS:
-

TESTES:
-

RESULTADO:
-

STATUS:
CONCLUÍDO / PARCIAL / BLOQUEADO / ADIADO

PENDÊNCIAS:
-

PRÓXIMO PASSO:
-
```

## 10. Prompt padrão para iniciar um novo chat

Usar este texto:

```text
CONTINUAÇÃO DO PROJETO PVPPokeGo — NÃO REINICIAR.

Antes de qualquer alteração, leia o Guia Mestre canônico do projeto:
https://github.com/LucianoToscano/PvPPokeGo/blob/feature/battle-correspondence-0.5.36-20261009/docs/PROJECT_CONTINUITY_GUIDE.md

Depois:
1. verifique a branch/HEAD reais;
2. leia o checklist da versão atual;
3. identifique o último registro cronológico realmente concluído;
4. continue exatamente dali;
5. não repita pesquisas já documentadas;
6. não volte para versões antigas;
7. preserve funcionalidades existentes;
8. registre neste Guia Mestre toda correção, mudança, teste, build e entrega feita nesta conversa.

Regra principal:
MELHORAR E ORGANIZAR, NUNCA EMPOBRECER.

Se houver divergência entre conversa antiga e repositório, o código/estado real deve ser verificado primeiro e a divergência documentada.
```

## 11. Registro cronológico

### 2026-10-07 — Checklist 1–80 transformado em acompanhamento oficial

Versão: 0.5.26  
Branch: `release/0.5.26`  
Commit: `8a38570e04d5ab1512167ab90b117637fb1ec423`

OBJETIVO/PROBLEMA:
- transformar ideias/requisitos em checklist rastreável.

ALTERAÇÕES:
- criado `docs/CHECKLIST-PvPPokeGo-0.5.26.md`;
- 80 itens classificados como feito/parcial/pendente/adiado.

RESULTADO:
- checklist passou a ser referência de planejamento da 0.5.26.

STATUS:
CONCLUÍDO.

### 2026-10-07 — PBE incorporado ao checklist

Versão: 0.5.26  
Branch: `release/0.5.26`  
Commit: `34f22208f4a27fa84d0b96e96fe7eb29b839418e`

OBJETIVO/PROBLEMA:
- incorporar o Predictive Battle Engine ao planejamento sem criar uma engine paralela.

ALTERAÇÕES:
- adicionado checklist PBE com 24 componentes;
- adicionadas prioridades PBE-A até PBE-J;
- formalizadas regras para separar matemática, possibilidade, previsão e comportamento.

RESULTADO:
- PBE documentado como evolução da engine Kotlin existente.

STATUS:
CONCLUÍDO.

### 2026-10-07 — Regra canônica de documentação e continuidade

Versão: 0.5.26  
Branch: `release/0.5.26`  
Commits:
- `13650e1d1ac5190078aedd6433d284d940173d16` — criação do Guia Mestre;
- `f2b20b8ad84521785a3d9301a2cbbde59d4735e1` — START_HERE redirecionado;
- `0f0288ea7ecca3f927c2bf4646bfa4b661526597` — handoff 0.5.20 arquivado;
- `5bd0cdf45aec1d80140f889ac27776c6abff9f28` — handoff atual redirecionado.

OBJETIVO/PROBLEMA:
- evitar perda de contexto entre chats, duplicação de trabalho e decisões contraditórias.

ALTERAÇÕES:
- criado este Guia Mestre canônico;
- definido protocolo obrigatório de atualização;
- definido template de registro;
- definido prompt padrão para novo chat;
- `START_HERE_NEW_CHAT.md` agora aponta para este documento;
- o handoff antigo da 0.5.20 foi preservado em `docs/archive/CURRENT_DEVELOPMENT_HANDOFF-0.5.20.md`;
- `docs/CURRENT_DEVELOPMENT_HANDOFF.md` agora aponta para o Guia Mestre;
- criado espelho no Google Drive dentro da pasta oficial do projeto.

GOOGLE DRIVE:
- Documento: `PvPPokeGo — GUIA MESTRE DE CONTINUIDADE E REGISTRO`
- ID: `178P8tWol8mjv_juy5qhyitEvE6yl1fzPnuLWGDV1cXw`
- Link: https://docs.google.com/document/d/1CfSJ8o0r3qnoq3MSsfozYD1M5C07A1pL/edit?usp=drivesdk

RESULTADO:
- existe uma referência principal versionada no GitHub e um espelho persistente no Drive;
- novos chats devem começar pelo Guia Mestre, não por resumos antigos.

STATUS:
CONCLUÍDO.

### 2026-10-07 — Reservas: continuar busca ambígua e reutilizar retrato estável

Versão: 0.5.26  
Branch: `release/0.5.26`  
Commit de código: `fd66d553fc37148c6ab53d96c040b047e01c3bb4`  
HEAD de entrada conferido: `fdd78ab2998e1ed9ead6d55cf71f436763add5ba`

OBJETIVO/PROBLEMA:
- Retomar o bug prioritário 5.1 a partir do estado real, após o último registro de continuidade concluído.
- A leitura dos dois CPs encerrava a busca visual mesmo com associação ambígua/sem correspondência.
- O fallback de retrato da equipe exigia confirmação da posição nativa, embora o retrato já pertencesse ao slot estável.

ALTERAÇÕES:
- ReserveScanPolicy exige confirmação do mapeamento de cada card para interromper a busca; BattleEngine fornece o resultado do matcher existente.
- BattleOverlayView permite o retrato do slot estável mesmo enquanto a posição nativa está pendente, mantendo atlas exato/Dex como prioridade.
- Preservado o requisito de mapeamento confirmado para promover crops nativos ao cache da equipe e posicionar indicadores no seletor de troca.
- Acrescentados testes de CP duplicado com formas do mesmo Dex, CP sem correspondência e segundo mapeamento ausente.
- Checklist atualizado sem promover reconhecimento visual a concluído; espelho do Guia Mestre no Drive sincronizado.

ARQUIVOS PRINCIPAIS:
- app/src/main/java/com/lucianotoscano/pvppokego/engine/ReserveScanPolicy.kt
- app/src/main/java/com/lucianotoscano/pvppokego/engine/BattleEngine.kt
- app/src/main/java/com/lucianotoscano/pvppokego/overlay/BattleOverlayView.kt
- app/src/test/java/com/lucianotoscano/pvppokego/engine/ReserveScanPolicyTest.kt
- docs/CHECKLIST-PvPPokeGo-0.5.26.md
- docs/PROJECT_CONTINUITY_GUIDE.md

TESTES:
- Kotlin/JVM + JUnit 4.13.2 reais: ReserveScanPolicyTest (8) e ReserveCardMatcherTest (10), **18 PASS / 0 FAIL**.
- Suíte histórica de 132 testes não reexecutada nesta sessão.
- Compilação completa Android, renderização de Bitmap/Canvas e APK instalado não validados nesta sessão.
- PR #34 conferido aberto/draft na branch correta.
- Run do HEAD de entrada: 37697150072, job 113051559680, failure com steps=[], runner_id=0; nenhum comando de build executado.
- Ambiente desta sessão possui JDK/Kotlin no Android Studio e SDK android-37.0; isso corrige apenas a descrição do ambiente atual, sem invalidar o bloqueio histórico.
- Git local sem remote-https; download público do repositório retornou 404. Fontes relevantes obtidas pelo conector autenticado, fixadas no HEAD de entrada; nenhuma cópia parcial foi apresentada como checkout completo.
- Leitura/escrita do espelho feita pelo conector nativo: o helper de leitura em arquivo rejeitou o caminho absoluto Windows. Estrutura de uma aba inspecionada e preservada.

RESULTADO:
- Corrigidas duas condições que impediam a recuperação/exibição das reservas; lógica de identificação existente preservada.
- Nenhum APK gerado; ícone do launcher não alterado.

STATUS:
PARCIAL — correção de código e testes direcionados concluídos; aceite visual depende de APK real.

PENDÊNCIAS:
- Build/test Android Standard e validação em aparelho: atlas presente/ausente, CP duplicado, início no meio da batalha, troca/desmaio e seletor de troca.
- Validar ícone oficial instalado e launcher Samsung.
- Medir custo das novas tentativas de busca quando a evidência continuar ambígua; intervalo de OCR existente preservado.

PRÓXIMO PASSO:
- Executar o build Standard em ambiente com acesso ao repositório/dependências e runner funcional; validar os cenários visuais acima antes de encerrar o bug 5.1.
- Depois retomar replay/fixtures e a ordem PBE já documentada, sem reiniciar pesquisa externa.

### 2026-10-07 — Verificação de continuidade e referências do launcher

Versão: 0.5.26  
Branch: `release/0.5.26`  
HEAD verificado antes deste registro: `4b3932d18e9574977f7d5db9eaf1f17aa9273359`  
PR: #34 (aberto, draft)

OBJETIVO/PROBLEMA:
- Retomar do Guia Mestre sem reiniciar nem descartar trabalho; conferir pendências de APK Standard, reservas e launcher.

VERIFICAÇÕES:
- Guia Mestre, checklist 0.5.26 e handoff lidos pelo conector GitHub autenticado.
- Confirmada branch release/0.5.26 e PR #34 aberto/draft.
- Conferido `app/src/main/AndroidManifest.xml`: `android:icon=@mipmap/ic_launcher` e `android:roundIcon=@mipmap/ic_launcher_round` declarados.
- Conferidos `mipmap-anydpi-v26/ic_launcher.xml` e `ic_launcher_round.xml`: ambos apontam para `@drawable/pvppokego_launcher_foreground` e `@color/pvppokego_icon_background`.
- O registro anterior de reservas (`fd66d553`, 18 PASS / 0 FAIL) permanece referência; nenhuma suíte foi reexecutada nesta verificação.

ALTERAÇÕES:
- Somente este registro cronológico; nenhuma alteração em código, assets, configuração de build ou checklist.

TESTES:
- Inspeção estática das referências de Manifest e adaptive icons; nenhum build Android ou teste em aparelho nesta etapa.

RESULTADO:
- Referências do ícone presentes nos arquivos conferidos, sem comprovação da arte visual efetivamente empacotada/instalada.
- Não há APK 0.5.26 validado nesta etapa. Bugs 5.1 e 5.2 permanecem PARCIAIS.

STATUS:
PARCIAL.

PENDÊNCIAS:
- Validar existência e conteúdo visual do asset final, empacotamento, APK Standard e launcher Samsung.
- Executar QA real dos cards de reserva e seletor de troca após build.

PRÓXIMO PASSO:
- Retomar build Standard em ambiente funcional, validar assets/Manifest e testar reservas em aparelho, preservando a implementação atual.

### 2026-10-07 — Fallback de ícone condicionado à correspondência do card

Versão: 0.5.26  
Branch: `release/0.5.26`  
Commit de código: `8dbf9f47e1f35a4b9ff8dc5f1da6141cdafda8bb`

OBJETIVO/PROBLEMA:
- Impedir que o retrato recortado de outro Pokémon apareça no HUD ao reordenar os cards de reserva durante troca/continuidade.

ALTERAÇÕES:
- `BattleOverlayView.drawReservePokemonIcon`: atlas exato/Dex segue prioritário; retrato estável do slot segue disponível; crop temporário indexado pelo card só aparece quando `cardMappingConfirmed` é verdadeiro.
- A política de identificação e o armazenamento de energia não foram alterados.

ARQUIVOS PRINCIPAIS:
- `app/src/main/java/com/lucianotoscano/pvppokego/overlay/BattleOverlayView.kt`

TESTES:
- Inspeção estática do trecho antes da mudança; substituição de trecho exato efetuada no GitHub.
- Build Android e testes de interface física NÃO executados. O run anterior `37701733198` falhou com `steps=null`, antes do runner executar comandos.
- Sem APK produzido; ambiente desta sessão não tem checkout completo autenticado, Android SDK ou Gradle acessíveis; clone HTTPS não resolve `github.com`.

RESULTADO:
- Fallback de crop nativo agora exige mapeamento confirmado, evitando um ícone de espécie errada quando o card ainda é ambíguo.
- Não resolve sozinho placeholders onde espécie e retrato permanecem desconhecidos; validação em aparelho continua obrigatória.

STATUS:
PARCIAL.

PENDÊNCIAS:
- Corrigir/validar os casos reais de `?`/`IDENTIFICANDO` em reservas e seletor; validar arte oficial instalada no launcher Samsung.
- Executar build Standard com runner/SDK operacional; conferir Manifest, recursos, assinatura e SHA-256 antes de entregar APK 0.5.26.

PRÓXIMO PASSO:
- Diagnosticar o pipeline de build com runner efetivamente iniciado; testar reservas/trocas e ícone no APK gerado.

### 2026-10-07 — Compatibilidade de ícones do launcher e nova tentativa de APK

Versão: 0.5.26  
Branch: `release/0.5.26`  
Commits:
- `5b939efb4bddf8303952001ca248ba03c45a64eb` — fallback legacy `ic_launcher`;
- `b3605eeab9829f308de98319f1024d742dab562a` — fallback legacy `ic_launcher_round`;
- `7fe575e689c681778ab391bcf1b906bac9b242eb` — invariantes de build para ambos os recursos.

OBJETIVO/PROBLEMA:
- Corrigir a ausência de recursos de launcher para Android pré-API 26 e tentar gerar APK Standard da versão real.

ALTERAÇÕES:
- Confirmada no GitHub a arte oficial `drawable-nodpi/pvppokego_launcher.png` (Poké Ball com setas vermelha/laranja e azul/ciano).
- Confirmado que os dois adaptive icons (`mipmap-anydpi-v26`) apontam para o foreground oficial.
- Criados `app/src/main/res/mipmap-anydpi/ic_launcher.xml` e `ic_launcher_round.xml`, ambos usando a arte oficial nos Androids anteriores ao API 26.
- `.github/workflows/release-0.5.26.yml` agora verifica a existência e referência dos dois fallbacks legacy além dos adaptive icons.
- Nenhum código de batalha ou função existente removido.

ARQUIVOS PRINCIPAIS:
- `app/src/main/res/mipmap-anydpi/ic_launcher.xml`
- `app/src/main/res/mipmap-anydpi/ic_launcher_round.xml`
- `.github/workflows/release-0.5.26.yml`

TESTES:
- Inspeção estática da arte original, Manifest, foreground e recursos adaptive/legacy.
- Reexecutado o run `37701971801`: job `113068723701` falhou sem steps.
- Run `37702512658`: job `113069070074` falhou com `steps=null`.
- Run `37702558022`: job `113069219336` falhou com `steps=null`.
- Nenhuma compilação, teste Gradle ou validação em celular ocorreu: o GitHub não atribuiu runner efetivamente executável; ambiente local sem Android SDK/Gradle e sem acesso DNS aos repositórios de dependências.

RESULTADO:
- Recurso ausente de launcher legacy corrigido no código; ainda depende de APK real para validar em dispositivo.
- Não foi criado APK 0.5.26 nesta etapa; não há evidência de erro de compilação do novo código.
- A falha pré-execução pode envolver cobrança/limites ou indisponibilidade da execução hospedada; verificar a anotação exibida pelo GitHub Actions e as configurações da conta antes de diagnosticar como causa confirmada.

STATUS:
PARCIAL / BLOQUEADO (build).

PENDÊNCIAS:
- Desbloquear GitHub-hosted runner (verificação da conta `LucianoToscano`: Settings > Billing & licensing > Actions/budgets/limites e anotação do workflow).
- Rodar `:app:testStandardDebugUnitTest`, `:app:assembleStandardDebug`, validar APK, ícone adaptativo e legado, assinatura e SHA-256.
- Validar no Samsung os ícones do launcher e os retratos dos Pokémon de reserva.

PRÓXIMO PASSO:
- Com runner funcional, deixar o workflow Standard concluir e baixar o artefato efetivamente produzido; registrar checksum, teste e link na documentação.


### Adendo ao registro anterior — checklist e espelho sincronizados

- Checklist 0.5.26 atualizado no commit `e7348403c5d4b19f3c5c5191b423662c2955f2d7`.
- Espelho do Guia Mestre no Google Drive (`178P8tWol8mjv_juy5qhyitEvE6yl1fzPnuLWGDV1cXw`) atualizado e conferido via conector nativo.
- Último run `37702558022` não gerou artefatos (0), com job `113069219336` em failure e `steps=null`.
- Nenhum APK renomeado, publicado ou entregue indevidamente.


### 2026-10-07 — Diagnóstico confirmado: faturamento/limites bloqueiam Actions

Versão: 0.5.26  
Branch: `release/0.5.26`  
Status: BLOQUEADO (infraestrutura/conta GitHub; não é falha de compilação demonstrada)

EVIDÊNCIA:
- Usuário enviou captura do workflow `PvPPokeGo 0.5.26`, execução #58, aba Anotações.
- Erro explícito exibido pelo GitHub: "O serviço não foi iniciado porque houve falhas nos pagamentos recentes da sua conta ou porque seu limite de gastos precisa ser aumentado. Verifique a seção 'Faturamento e planos' nas suas configurações."
- O aviso separado de migração futura de `ubuntu-latest` para Ubuntu 26 não é a causa apontada para esta falha.
- Isso explica as falhas de jobs sem steps/log; o runner foi bloqueado antes de executar código. Ainda não se sabe se o motivo específico é pagamento pendente ou orçamento/limite de Actions.

ALTERAÇÕES:
- Somente documentação do diagnóstico. Código e recursos do aplicativo preservados.
- É necessário que o proprietário da conta verifique GitHub > Settings > Billing & licensing / Budgets and alerts e regularize o impedimento; autorização ao assistente não concede capacidade para alterar pagamentos ou limites da conta.

TESTES:
- Anotação analisada visualmente; nenhum novo Gradle/assemble executado.

RESULTADO:
- Bloqueio de conta/limite identificado na anotação do GitHub; não adianta repetir build até haver liberação do runner.
- APK Standard 0.5.26 permanece NÃO GERADO; nenhum arquivo anterior deve ser renomeado como novo.

PRÓXIMO PASSO:
- Após o GitHub indicar faturamento/budget normalizado, reexecutar o workflow de build Standard, verificar execução real de steps, testes, manifest, ícones, assinatura e SHA-256, e só então entregar o APK.


### 2026-10-07 — Espelho público isolado de compilação Standard

Versão: 0.5.26  
Branch privada canônica: `release/0.5.26`  
Repositório de build público: https://github.com/LucianoToscano/PvPPokeGo-APK-Build  
Snapshot original: `ec8610ba16ea8528e2f9c0268170d5a869d0387c`

OBJETIVO:
- Contornar bloqueio do GitHub Actions em repositório privado sem contratar serviço pago, preservando histórico e acesso privado ao projeto original.

ALTERAÇÕES:
- Foi confirmado repositório público de build separado `LucianoToscano/PvPPokeGo-APK-Build`, com permissões de edição.
- Criado snapshot novo sem histórico privado, contendo 104 arquivos pertinentes ao app Standard, testes, workflow, assets essenciais e avisos de licença.
- Excluídos scripts antigos, `.build/`, documentos internos, Recorder Compat, histórico e chaves de assinatura/credenciais.
- Arte oficial PNG copiada com SHA Git idêntico ao original; lógica de reservas preservada.
- Commit inicial público de código: `21ed3da78944407563e546280715ea950394261c`; trigger README: `9d265d8c807c89a56e27936cfe0510ffae4a6eb1`.
- Workflow público `.github/workflows/build-standard-apk.yml`: empacotar PvPoke offline e atlas, executar testes, gerar Standard debug, validar integridade e SHA-256, publicar artifact QA.
- Primeira tentativa pública `37706678401` executou runner (faturamento não bloqueou) mas falhou na ação `android-actions/setup-android@v3`, que tentou instalar pacote SDK obsoleto `tools`.
- Corrigido workflow para usar `sdkmanager` do runner; commit `1139a2fc256dddcaa05e0d3695db432a34725349`, nova tentativa `37706765046`.

STATUS:
PARCIAL — repositório público e workflow criados, build novo em andamento. Não afirmar APK concluído antes de teste e artifact real.

PENDÊNCIAS:
- Verificar desfecho da execução pública, corrigir eventuais falhas reais de compilação e confirmar artifact APK Standard e assinatura debug.
- A cópia pública é snapshot manual, não sincroniza alterações automaticamente com branch privada.
- O código dos créditos do aplicativo preserva contato previamente definido pelo projeto; código exposto na cópia pública.
- Registrar SHA-256 e link de entrega quando efetivamente disponíveis.

PRÓXIMO PASSO:
- Concluir build público e realizar validação de artefato sem modificar a branch privada de código.


### 2026-10-07 — Erros Kotlin de compilação Standard revelados pelo runner público

Versão: 0.5.26  
Branch privada: `release/0.5.26`
Compilação pública: `LucianoToscano/PvPPokeGo-APK-Build`

PROBLEMA:
- Primeiro build completo com Gradle na cópia pública (run `37706823681`) passou pelo SDK, dependências PvPoke e atlas de sprites; falhou em `:app:compileStandardDebugKotlin`.
- Logs mostram 3 causas concretas: retorno Boolean onde se esperava `HpFill?`, expressão de pontuação misturando tipos numéricos e falta de import `CaptureHealth` no serviço do overlay.

CORREÇÕES APLICADAS NO CÓDIGO CANÔNICO:
1. `BattleFrameEventDetector.kt`: ROI inválida do HP das reservas retorna `null`, não `false`; commit `9e395f5c8492bfac0ea4ce79e1eaa45ab174966b`.
2. `BattleEngine.kt`: `hpAdjustment: Double` calculado com conversão explícita para preservar aritmética do reserveScore sem ambiguidades; commit `c9295962d7d2caac67869dffc36b123ccaa442f5`.
3. `BattleOverlayService.kt`: import de `com.lucianotoscano.pvppokego.data.CaptureHealth` para chamadas já existentes; commit `2a98de5621de5f80e4ed1bc22a4c7db3f9f92b89`.

BUILD PÚBLICO:
- Mesmos três arquivos copiados do código canônico, em um único commit público `0b1c453add6e838c4de06eb07e5792481bb884d2`.
- Nova execução pública: `37707859527`.

TESTES:
- Erros fundamentados no log real da execução `37706823681`. Não houve ainda novo resultado de compilação confirmando as três correções.
- Sem regressão intencional: funções e lógica de batalha preservadas.

RESULTADO:
- Código-fonte atualizado nas duas branches e build público disparado.
- Não existe ainda APK 0.5.26 validado.

STATUS:
PARCIAL — aguardando resultado do teste e compile Android da nova execução.

PRÓXIMO PASSO:
- Revisar `37707859527`; em sucesso verificar integrity, assinatura, SHA-256 e disponibilizar artifact, registrar link/checksum. Se falhar, corrigir apenas o erro real indicado pelo novo log.


### 2026-10-07 — APK Standard 0.5.26 QA/debug REAL entregue

Versão: `0.5.26` (versionCode 31)  
Branch privada canônica: `release/0.5.26`  
Build público isolado: https://github.com/LucianoToscano/PvPPokeGo-APK-Build
Commit compilado: `0b1c453add6e838c4de06eb07e5792481bb884d2`
Run: https://github.com/LucianoToscano/PvPPokeGo-APK-Build/actions/runs/37707859527

OBJETIVO:
- Gerar pela primeira vez o APK 0.5.26 real com os recursos de launcher/reserva e correções Kotlin, preservando repositório original privado e Recorder Compat adiado.

ALTERAÇÕES E RESULTADOS:
- SDK Android 36, Gradle, PvPoke empacotado offline e banco visual de fingerprints/atlas executados com sucesso no runner público.
- Os três erros Kotlin revelados na execução anterior foram corrigidos primeiro no original privado (`9e395f5c`, `c9295962`, `2a98de56`) e depois copiados sem divergências para a branch pública (`0b1c453`).
- Workflow `37707859527` terminou `success`: `:app:testStandardDebugUnitTest`, `:app:assembleStandardDebug`, validação de integridade e assinatura.
- Artefato produzido: `PvPPokeGo-0.5.26-QA-debug.apk`, 78.046.047 bytes.
- Assinatura APK v2 debug verificada. **Não é certificado permanente/release**, e pode não atualizar um APK antigo assinado de outra forma.
- ZIP GitHub artifact extraído em ambiente de entrega, `unzip -t` do APK passou; foram confirmados `assets/gamemaster.json`, `assets/pokemon_icon_atlas.png`, `assets/pvpoke_snapshot.json`, `res/drawable-nodpi-v4/pvppokego_launcher.png`.
- SHA-256 publicado pelo workflow e reconfirmado após download: `461815c2b8c9078f8fcd617145f1abeefb3a0be03f2376114227f44594708932` (match exato).
- Upload para Google Drive concluído e verificado via metadados (tamanho idêntico):
  https://drive.google.com/file/d/1bcbP8WJ7sWgd9lUTvqb0NzV9MSXq_nCH/view?usp=drivesdk
  Pasta: `12g6IITrn64y0D40YsIdzl-sN9alZty8Q`.
- Checklist atualizado com entrega QA no commit `f3d5ab0c7d86f9d8d9a5e77f20d2de67603dd3af`.

ARQUIVOS PRINCIPAIS:
- `BattleFrameEventDetector.kt`, `BattleEngine.kt`, `BattleOverlayService.kt` (correções Kotlin)
- `docs/CHECKLIST-PvPPokeGo-0.5.26.md`
- Na cópia pública: `.github/workflows/build-standard-apk.yml`, 104 arquivos de snapshot de build (sem histórico privado, segredos e docs internas)

TESTES:
- Compilação Android Standard e testes unitários de Gradle concluídos no CI (workflow SUCCESS).
- Validação de assinatura, integridade ZIP e checksums: PASS.
- Sem QA visual em aparelho nesta etapa.

STATUS:
CONCLUÍDO — geração e entrega QA/debug do APK Standard 0.5.26.  
PARCIAL — validação física dos ícones de reservas e launcher Samsung e assinatura permanente.

PENDÊNCIAS:
- Usuário instalar e verificar reservas 2/3, troca, ícone Samsung, telas com CP duplicado, e informar eventuais regressões.
- Se o certificado instalado diferir, exportar ajustes antes de desinstalar versão anterior.
- Release permanente só com keystore conhecida; não renomear QA/debug como release final.
- Cópia pública build-only é snapshot e requer sincronização explícita das alterações futuras.

PRÓXIMO PASSO:
- Reproduzir visualmente os bugs em dispositivo usando o APK entregue; aplicar correção na branch privada canônica, transferir snapshot revisado ao build público, registrar novo checkpoint e subir numeração para próxima entrega.


### 2026-10-07 — 0.5.27: reversão do visual poluído das reservas e ícone oficial transparente

BASE: 0.5.26 validado no Actions `37707859527`.  
BRANCH CANÔNICA: `release/0.5.27`, criada de `release/0.5.26` no commit `0faf11d2227c4a4ddc17990d6fd476832526d2b8`.  
COMMIT DA CORREÇÃO: `cb00b81607706df99fcd9837dd27fc6e4fc4c4bb`.  
ESPÊLHO PÚBLICO: `LucianoToscano/PvPPokeGo-APK-Build`, commit `71a849787cfad9aead891cabd6e48b7772dce4e8`.

PROBLEMAS REPORTADOS PELO USUÁRIO NA CAPTURA DA VERSÃO 0.5.26:
- Cards das duas reservas na direita visualmente poluídos, retratos com halos/círculos, textos e CP muito comprimidos.
- Elementos de HUD pareciam desaparecer, especialmente durante troca em que a identidade dos dois cards nem sempre está confirmada.
- Launcher instalado diferente da arte transparente enviada explicitamente.

ALTERAÇÕES:
- `BattleOverlayView.kt`: cards com fundo escuro arredondado compacto, indicador menor, retrato quadrado arredondado sem halo circular, nome/CP/status em linhas separadas, texto limitado à largura disponível, tipos menores e HP barra discreta, mantendo posições personalizáveis individuais das duas reservas.
- Em switch chooser só são desenhados indicadores especiais quando ambos os cards têm correspondência confirmada; nos demais casos o HUD de reservas continua visível sem esconder os elementos estabelecidos.
- Prioridade de sprite por speciesId/form do atlas, depois retrato de slot estável e só então crop card confirmado; a correção de correspondência não foi removida.
- `app/src/main/res/drawable-nodpi/pvppokego_launcher.png`: substituído pelos bytes EXATOS do PNG RGBA transparente enviado pelo usuário (1254×1254, 1.453.830 bytes, Git blob `95797dfe8b1fe4e4db0fe07d1f35c2a4e49ae695`).
- `colors.xml`: fundo do adaptive icon transparente `#00000000`; foreground mantém a logo original com inset de 16dp para evitar corte pela máscara; `mipmap-anydpi` e `mipmap-anydpi-v26` existentes permanecem referindo o mesmo recurso sem criar ícone alternativo.
- `app/build.gradle.kts`: versionCode 32, versionName 0.5.27.
- Repositório privado permanece privado; cópia pública isolada atualizada e configurada para build 0.5.27.

TESTES/BUILD:
- GitHub Actions do espelho público: `37712804512` disparado; status ainda por confirmar neste registro.
- Não foi executado teste visual em Samsung nesta etapa; layout novo requer QA físico.

STATUS: IMPLEMENTADO NO CÓDIGO; APK/QA EM ANDAMENTO.
PENDÊNCIAS: confirmar `:app:testStandardDebugUnitTest`, `:app:assembleStandardDebug`, artefato APK 0.5.27 com hash/assinatura, e testar launcher/reservas/troca no Samsung. Não declarar reparo visual validado sem prova.
PRÓXIMO PASSO: conferir o resultado do run público, ajustar qualquer erro de compilação, entregar APK QA se passar e registrar checksum/URL no Guia Mestre.


### 2026-10-07 — Entrega QA 0.5.27 concluída e hash da arte conferido

BRANCH CANÔNICA: `release/0.5.27`  
REPO ORIGINAL: privado; espelho público apenas para build, sem histórico privado.  
COMMIT FONTE COM CORREÇÕES: `cb00b81607706df99fcd9837dd27fc6e4fc4c4bb`.  
COMMIT DE BUILD: `71a849787cfad9aead891cabd6e48b7772dce4e8`.  
EXECUÇÃO SUCCESS: https://github.com/LucianoToscano/PvPPokeGo-APK-Build/actions/runs/37712804512

RESULTADO:
- `PvPPokeGo-0.5.27-QA-debug.apk` gerado, 79.193.451 bytes (versionCode 32, versionName 0.5.27).
- Testes e build `:app:testStandardDebugUnitTest` + `:app:assembleStandardDebug`: **PASS**.
- ZIP APK verificado sem erros, assinatura APK Signature Scheme v2 **PASS** (certificado QA/debug, não release permanente).
- SHA-256 do APK: `cbf336cddfa31bf0639df41491b4e1cc0019b3bc4403a6c394a3bfa5966d3025` (arquivo baixado bate exatamente com SHA256SUMS do CI).
- `res/drawable-nodpi-v4/pvppokego_launcher.png` extraído do APK tem 1.453.830 bytes e SHA-256 `f8d4f45922e6af0022e3eaad1d255fedd2b790650f542e62315042f8bd1dbf51`, **exatamente igual ao PNG RGBA transparente enviado pelo usuário**.
- `pokemon_icon_atlas.png` e `gamemaster.json` presentes dentro do APK.
- Arquivo ZIP de distribuição contendo APK + SHA256SUMS e verificação de assinatura foi armazenado e conferido na pasta oficial do Google Drive:
  https://drive.google.com/file/d/1KIvivodofc3QA72oS0zeUTJPplFUkUb3/view?usp=drivesdk
- Checklist 0.5.27 atualizado: commit `c12bdec830797e21009ebaa0951d15b7d285d040`.

STATUS:
- CONCLUÍDO: correção no código, build e validação técnica do APK QA 0.5.27.
- PENDENTE: teste visual no Samsung; não afirmar que aparência do launcher ou cards foi aprovada pelo usuário.
- IMPORTANTE: assinatura debug nova pode impedir instalação por cima da 0.5.26 devido a certificado diferente. Exportar configurações antes de desinstalar, se necessário.

PRÓXIMO PASSO:
- Instalar, validar o PNG oficial na tela inicial, reservas (PC, tipos, retratos e HP), modo de troca/continuidade e ícones de controle no Samsung. Registrar feedback e evoluir para próximo versionCode em nova entrega.


### 2026-10-07 — 0.5.28: restauração visual baseada no APK 0.5.17

RELATO:
- Usuário disse que 0.5.27 estava "tudo bugado" e enviou o APK **PvPPokeGo-0.5.17.apk** como referência visual aprovada.
- O arquivo fornecido foi verificado como ZIP/APK íntegro (9 dex, 27 assets, SHA-256 `928fa6cfb64be14842721ce248a055f7aee1a7f9c40d7d3eaeb7b1045ddfbbd6`). Não foi executado em emulador/celular; a análise de HUD usa também a branch fonte preservada `release/0.5.17` (HEAD `d5c3be748b54eb10cd73bb116d6eda1e8acdbf85`).
- Comparação com 0.5.27 provou mudança de apresentação: a 0.5.17 tinha indicadores pequenos de matchup, tipos e recomendação, sem cards retangulares grandes, retratos ou barra de HP no bloco de reservas. A 0.5.27 inseriu esses cards e textos, alterando o layout original.
- Outro desvio: a seção de ataques 0.5.27 mostrava previsão extensa e indicadores de dano mesmo no HUD normal; 0.5.17 mostrava círculos dos moves, nomes, eficácia e texto simples de energia.

DECISÃO:
- A **0.5.17 é referência canônica para a aparência do HUD**, não para a lógica de batalhas.
- Preservar toda a implementação avançada de engine/capture/reconhecimento e ícone do app transparente fornecido pelo usuário em 0.5.27; NÃO fazer rollback geral.

ALTERAÇÕES REALIZADAS:
- Criada branch privada `release/0.5.28` baseada em `release/0.5.27` (`09d12f524652cd8fd106fda029a24f8c4bee2ced`).
- `BattleOverlayView.kt` em `33878a17a904953c2ce91aa1e50390bccbb31d5b`: restauradas as rotinas `drawReserves` e `drawSwitchChoiceIndicators` da branch 0.5.17; removidos os cards grandes, retratos decorativos e texto extra do HUD normal. Única salvaguarda adicional: não ocultar automaticamente o indicador quando OCR não encontra nome; manter "IDENTIFICANDO". Indicador de switch mantém ícones pequenos da 0.5.17 e fallback quando dados do chooser estão incompletos.
- `BattleOverlayView.kt` em `c03ff271a9bf727f9df8973ac775d738c7d8c27d`: restaurado desenho tradicional de `drawMoveTriangle` (nome dos 3 ataques, counter, energia resumida, espaçamento original); dados e funções preditivas continuam na engine e as anotações avançadas ainda podem ser vistas com `settings.debugMode`.
- `app/build.gradle.kts` em `0ad039f11b7deb39c35ef564dcbcab15c0df9900`: `versionName = 0.5.28`, `versionCode = 33`.
- Ícone do launcher **não revertido** ao arquivo antigo: mantido exatamente o PNG oficial transparente de 0.5.27 solicitado pelo usuário.
- A cópia pública isolada para compilação foi atualizada no commit `c1432f2b316c06baf40083db5d3f40f94a03bef5`, sem exposição do histórico privado.
- Workflow: https://github.com/LucianoToscano/PvPPokeGo-APK-Build/actions/runs/37715432893

ARQUIVOS:
- `app/src/main/java/com/lucianotoscano/pvppokego/overlay/BattleOverlayView.kt`
- `app/build.gradle.kts`
- Espelho de build público: `.github/workflows/build-standard-apk.yml`, `README.md`.

VALIDAÇÃO:
- Integridade ZIP do APK enviado 0.5.17: PASS.
- Revisão estática do código fonte 0.5.17 vs 0.5.27: o traçado de olho/cadeado, indicadores, chips de tipo e posições padrão já eram idênticos; não houve razão para substituir esses controles.
- A versão Standard 0.5.28 aguardava build/tests ao escrever este registro. NÃO marcar APK disponível sem artefato e assinatura reais.

STATUS:
- Código 0.5.28 com visual legacy restaurado, build em andamento.
- QA real de tela em Samsung ainda obrigatório; nenhuma garantia de que efeitos de layouts personalizados antigos/PGSharp sejam resolvidos apenas pelo desenho.

PENDÊNCIAS:
- Verificar `:app:testStandardDebugUnitTest` + `:app:assembleStandardDebug`; conferir assinatura v2, hash e artefato.
- Testar em dispositivo: olho, cadeado, ataque inimigo, indicador de reservas, opção de troca, posições salvas e ícone transparente do launcher.
- Se o usuário ainda reportar diferença, pedir captura da 0.5.17 executando, além do APK, para comparar áreas e dimensões reais.

PRÓXIMO PASSO:
- Finalizar QA/debug 0.5.28, entregar APK verdadeiro e registrar entrega no Guia Mestre/checklist.

### 2026-10-07 — Correção de compilação no HUD restaurado

- Run público `37715432893`: falhou na compilação Kotlin; log apontou `BattleOverlayView.kt:873:22 Unresolved reference 'stableBattleAdvice'`.
- Causa: no recorte de código visual da 0.5.17, o helper novo `stableBattleAdvice` da 0.5.27 foi removido junto com o bloco da UI moderna, mas `drawBattleAssist` ainda depende dele.
- Corrigido no privado `f1de2771f5a0e88a840d8864134ea5424cdd8d5a`: recuperado exatamente o helper, mantendo a estabilização dos conselhos.
- Espelhado no repo público `491106a928efd810c9351c8e356d3513ad03c6f2`; novo run `37716045867` iniciado.
- Comparados os métodos removidos da versão 0.5.27: `drawReservePokemonIcon`, `reserveMatchupLabel`, `reserveMatchupColor` e `fitReserveText` não têm chamadas pendentes após a restauração.
- Status: compilação e entrega APK 0.5.28 ainda pendentes neste checkpoint.


### 2026-10-07 — APK Standard QA 0.5.28 gerado e verificado

CONTINUIDADE DO RELATO "VEJA AQUI COMO ERA": usamos a fonte `release/0.5.17` e o APK fornecido para restaurar visual sem rollback de engine.

RESULTADO REAL:
- Novo APK `PvPPokeGo-0.5.28-QA-debug.apk`, 79.193.451 bytes, com versionCode 33/versionName 0.5.28.
- Run final: https://github.com/LucianoToscano/PvPPokeGo-APK-Build/actions/runs/37716045867 — **SUCCESS**.
- Commit público compilado: `491106a928efd810c9351c8e356d3513ad03c6f2`.
- Testes Standard unitários `:app:testStandardDebugUnitTest`, assemble APK `:app:assembleStandardDebug`, verificação do launcher e dados PvPoke/atlas: PASS.
- `unzip -t` dentro do APK: PASS; `assets/gamemaster.json`, `assets/pvpoke_snapshot.json`, `assets/pokemon_icon_atlas.png`, `assets/pokemon_icon_atlas.json` e `res/drawable-nodpi-v4/pvppokego_launcher.png` presentes.
- Assinatura `apksigner verify`: v2 **true**, 1 signatário QA/debug, não assinatura permanente de release.
- SHA-256 APK: `504643fe67f08ab599f35d74b8cf3cf1f7f079324badae9024712d59c54c359d`, idêntico ao manifest de checksums do runner.
- PNG oficial transparente no APK: SHA-256 `f8d4f45922e6af0022e3eaad1d255fedd2b790650f542e62315042f8bd1dbf51`, idêntico ao fornecido pelo usuário.
- Artefato público: https://github.com/LucianoToscano/PvPPokeGo-APK-Build/actions/runs/37716045867
- ZIP de distribuição e verificação, armazenado na pasta oficial Drive: https://drive.google.com/file/d/1sPMZZMsKpWukN0J3yxhBYVGYeJHu4RYe/view?usp=drivesdk
- Checklist final 0.5.28 atualizado no commit `d24c905f7487035f4658a33ad0a3402797ce7cd3`.

COMPROMISSO DE QUALIDADE:
- Foram recuperados traçados HUD 0.5.17 de reservas e escolhas de troca (pequenos badges e ícones de tipo, sem grandes cards) e disposição clássica de ataques/energia. Debug mantém novas previsões. Nem a engine nem o ícone transparente oficial foram revertidos.
- **Não houve teste visual no Samsung**. A restauração está validada como código/build, NÃO como correspondência perfeita no dispositivo.
- APK 0.5.28 QA/debug pode não atualizar 0.5.27 devido a certificado de debug; recomendar exportar dados/posições antes de desinstalação.

PENDÊNCIAS:
- Usuário instalar e validar a aparência da 0.5.28 lado a lado com a 0.5.17, incluindo reservas, trocas, tipos, cadeado, olho, ataque inimigo e posição dos controles.
- Ajustar com evidência visual qualquer diferença residual, sem nova reformulação desnecessária.


### 2026-10-07 — Correção funcional do reconhecimento dos três aliados (0.5.29)

CONTEXTO:
- Após entregar 0.5.28 com HUD visual restaurado de 0.5.17, usuário informou que o SISTEMA não funcionava; detalhou que o reconhecimento dos Pokémon aliados piorou comparado com 0.5.17.
- Branch base privada `release/0.5.28` preservada; nova branch `release/0.5.29` criada, versionCode 34.
- A análise confirmou uma regressão concreta: `BattleOcrDetector.fuseTeamSlotEvidence` da 0.5.28 retornava null para nome e PC válidos quando não havia match de imagem, ao contrário do detector 0.5.17 que reconhecia aliados por OCR.
- A 0.5.17 varria uma área ampla 4–96% largura / 10–93% altura. A nova implementação só usava três regiões fixas estreitas, potencialmente perdendo outras proporções de layout.
- O motor interrompia buscas se havia três slots com PC, ainda que identidade fosse incerta.

MUDANÇAS:
- `BattleOcrDetector.kt` commit `d37d3fe484bda3a8d120d47ed9763530f85a3c97`: OCR puro nome válido + PC 10..10000 e confiança ≥0.90 recuperado independentemente da existência de sprite. Não substitui identificação visual para formas/variantes quando disponível. Se per-slot identifica menos que os 3, usa leitura ampla da tela de seleção 0.5.17. Este fallback só retorna um time completo de 3 pares distintos de espécie/PC reconhecidos, evitando introduzir falsas identidades de tela sem time.
- `AllyOcrRecoveryPolicy.kt` commit `17f68e42a663e6fcf8b8e895ff789749ef0ca11f`: regra pura para reconhecimento OCR.
- `AllyOcrRecoveryPolicyTest.kt` commit `062cbb3b62059bf48eaf8822582aa7d025d1534d`: regressões de nome+PC com visual ausente e rejeição de PC/nome inválidos e baixa confiança.
- `BattleEngine.kt` commit `73029729981e308bac1c6990e91ca796da997e5d`: `needsOwnTeamScan` só interrompe depois que os três slots têm PC, espécie e confiança ≥0.90, evitando que palpites incompletos congelem o time.
- `BattleOverlayService.kt` commit `ba1668ac8244911fec9c22272b84eadb0e0b99be`: cadência OCR da equipe 1550ms, próxima ao detector amplo da 0.5.17, para reduzir carga no aparelho.
- `app/build.gradle.kts` commit `1e45dec7dfc80eebc1a6eddffda3df78b7a0bdc2`: versionName 0.5.29, versionCode 34.
- Copiado só o snapshot necessário para `LucianoToscano/PvPPokeGo-APK-Build` público, commit `008993b11f9299fc4a88cfeb7fde9f8ff7e9efc2`, preservando original privado e visual de 0.5.17 restaurado na 0.5.28.

VALIDAÇÃO:
- Build Standard CI iniciado: https://github.com/LucianoToscano/PvPPokeGo-APK-Build/actions/runs/37719025048
- STATUS NESTE CHECKPOINT: teste compilação em andamento. Ainda não declarar reconhecimento validado no Samsung.
- Sem captura bruta de tela / arquivo de diagnóstico do usuário para testar a exatidão da identificação de espécies e PC no dispositivo real.
- A regra OCR+PC recupera comportamento antigo, mas a leitura ampla é deliberadamente conservadora e não resolve automaticamente todos os layouts.

PENDÊNCIAS:
- Confirmar testes unitários, compile e APK 0.5.29. Verificar assinatura v2/sha256/ícone ao entregar.
- QA físico na tela de seleção 3 aliados e continuidade das reservas durante trocas; solicitar exportação de diagnóstico se continuar falhando.
- Não alterar ícone transparente e layout de referência 0.5.17 nem perder motor de batalha.


### 2026-10-07 — APK 0.5.29 com recuperação de aliados: entrega técnica concluída

COMMIT DO ESPELHO DE BUILD: `008993b11f9299fc4a88cfeb7fde9f8ff7e9efc2`  
RUN: https://github.com/LucianoToscano/PvPPokeGo-APK-Build/actions/runs/37719025048

RESULTADO:
- Run público concluiu com **success** em `:app:testStandardDebugUnitTest`, `:app:assembleStandardDebug` e validação do APK.
- APK de QA: `PvPPokeGo-0.5.29-QA-debug.apk`, 79.193.455 bytes, versão 0.5.29, versionCode 34.
- SHA-256 do APK validado localmente contra `SHA256SUMS.txt` do build: `b79c41f58e6fe846f7a62497cd91e1d64f15e377bdee1d8dcc4e2369e1199e3a`.
- `unzip -t` verificou arquivo sem erros; atlas, gamemaster e snapshot PvPoke encontrados no APK.
- Assinatura v2 debug verificou **true** (1 signer); não confundir com certificado de release/permanente.
- Logo PNG transparente oficial no APK: SHA256 `f8d4f45922e6af0022e3eaad1d255fedd2b790650f542e62315042f8bd1dbf51`.
- Pacote ZIP armazenado no Google Drive oficial, com APK, assinatura e hashes:
  https://drive.google.com/file/d/1Pw9RuiruLHrP4f7tkCFDLG8NnGhMuxFO/view?usp=drivesdk
- Checklist atualizado no commit `3862b9b4ca9285f0e4e7f1c3bfd08076483fd9b4`.

STATUS:
- CONCLUÍDO: correção-fonte do fallback OCR nome/PC + região ampla da versão 0.5.17, testes automatizados, APK Standard QA 0.5.29 gerado/assinado e distribuído.
- PENDENTE: confirmação de reconhecimento correto dos 3 aliados na seleção pré-batalha no Samsung e preservação após trocas. Nenhum teste em aparelho foi executado pelo assistente.
- Se continuar falhando, obter screenshot e `Configurações > Exportar diagnóstico` para analisar layout/captura/OCR e não voltar a modificar apenas a aparência.
- Se a assinatura instalada divergir, exportar configurações antes de desinstalar para testar o APK QA/debug.

PRÓXIMO PASSO: QA de reconhecimento no aparelho; investigar código de captura e pipeline usando diagnóstico caso espécies/PC não sejam detectados.


### 2026-10-08 — Adendo canônico: inventário visual, OCR, energia, HP e previsões da batalha

**Natureza:** decisão de produto + especificação funcional e visual derivada das capturas e correções feitas pelo usuário nesta conversa. **Registro documental, NÃO alegação de implementação no APK**. Base visual historicamente aprovada: HUD compacto no estilo 0.5.17/0.5.28, preservando o motor evoluído; não fazer rollback de código. Regra permanente: **MELHORAR E ORGANIZAR, NUNCA EMPOBRECER**.

**Referências e branch/HEAD verificados em 2026-10-08, antes deste adendo:**
- Repositório privado: https://github.com/LucianoToscano/PvPPokeGo ; branch padrão main, não confundir com branch ativa.
- Guia solicitado: release/0.5.26, HEAD observado 88d675f516e370dd0f684bd16c4f2c9c6fb5210d; essa branch é histórica, preservada.
- release/0.5.27, HEAD observado d88ccb84e7bc582aed49bb99a7c223fc903567f1; histórica.
- release/0.5.28, HEAD observado c184c3b39318eb00d6ede6bed6b32fd998291d80; referência visual mais recente, histórica.
- **Branch ativa real release/0.5.29**, HEAD observado 60f5a87ed71886831e676d7b1b1f0745a67d3754. Guia ativo: https://github.com/LucianoToscano/PvPPokeGo/blob/release/0.5.29/docs/PROJECT_CONTINUITY_GUIDE.md . Não voltar para 0.5.26 apenas porque o link foi citado.
- Objetivo desta edição: acrescentar registro integral tanto ao guia histórico explicitamente apontado quanto ao guia ativo; código, versão, workflow, release e APK não alterados. Os HEAD acima são **pré-edição**, não se confundem com os commits documentais novos.

#### A. Limites de propriedade e integridade da interface — REGRA INVIOLÁVEL

1. **Interface original do Pokémon GO é intocável**. Jamais modificar, reposicionar, redimensionar, substituir, tapar ou interferir nos controles originais. O PvPPokeGo pode observá-los por OCR/visão, construir estado e desenhar um overlay independente que não bloqueie toques necessários.
2. Fontes NATIVAS, **não contam como ícones/funções próprios do PvPPokeGo**: cartões superiores de cada Pokémon com nome, PC, Pokébolas e escudos; barras originais de HP; personagens, cena e animações; botão nativo de desistir/trocar; cartões nativos dos dois aliados reservas com sprites/PC/HP; grandes botões dos ataques carregados com seu preenchimento e nomes; textos nativos de eficácia (SUPER EFICAZ!, NÃO É MUITO EFICAZ); mensagens de ataque (ex.: “Sneasel usou Jogo Sujo!”), BLOQUEADO!, ATAQUE/DEFESA AUMENTOU ou DIMINUIU; tela de escolha de escudo, botão AGORA NÃO, contador de decisão; modal de substituição obrigatória e contador; telas pré-batalha, escolha de liga/equipe e seus emblemas. **São entradas de leitura, não componentes a redesenhar**.
3. **PGSharp não é PvPPokeGo.** Excluir do inventário próprio: estrela amarela e barra cinza/botões “Ataques ágeis”, “Ataques carregados” e “Shield”, assim como outros controles atribuídos ao PGSharp pelo usuário. Não transformar supostos botões externos em funcionalidades implementadas.
4. Distinguir explicitamente o texto nativo de eficácia perto do HP/botões próprios do Pokémon GO do **indicador adicional de eficácia do inimigo desenhado junto dos círculos do PvPPokeGo** (item #15 das marcações). Nunca documentar esses dois como o mesmo recurso.
5. Ícones circulares de tipos com aparência no estilo Pokémon GO, cor por tipo, contorno fino, escala pequena e alinhamento horizontal **já foram aprovados** pelo usuário; **preservar exatamente o conceito e evitar redesenho gratuito**.

#### B. Inventário visual consolidado do PvPPokeGo (numeração das capturas e correções do usuário)

A numeração corresponde às anotações manuais das capturas, não necessariamente a IDs de código. Ícones sobre cards nativos são overlays informativos; cards/HP/PC nativos NÃO são propriedade nossa.

| # | Elemento próprio ou leitura | Função / estados / observações |
|---|---|---|
| 1 | Olho riscado / olho aberto | Alternar visibilidade da HUD; com HUD oculta permanece controle mínimo para reexibir. Dois estados visuais observados. |
| 2 | Cadeado fechado / aberto verde | Travar posições / destravar para editar e arrastar HUD. Não interceptar controles do jogo. |
| 3 | Chave inglesa / engrenagem | Acesso ao menu rápido/visual/sistema do overlay; controle visível dentro e fora da batalha. |
| 4 | Badge acima do emblema: ✓ verde, círculo cinza, × vermelho | Matchup do Pokémon ATIVO: favorável, neutro ou desfavorável (tipos, golpes e contexto; não só PC). **Não é botão de fechar**. |
| 5 | Emblema triangular do PvPPokeGo | Identificação visual do overlay e referência para status de matchup; não presumir ação ao toque sem confirmar no código. |
| 6 | Caixa de decisão tática | Estados registrados: CONTINUE FAST, ATENÇÃO, ESCUDO, TROQUE → Pokémon, PREPARE-SE, ANALISANDO ATAQUE. Explicações resumidas: pressão, golpe possível, proteção, alternativa de troca, ataques/tempo até carregado, dano sem/com escudo, faixa de HP/KO; estimativa, não certeza. |
| 7 | Triângulo de três círculos dos golpes do INIMIGO | Superior = ágil identificado/provável; dois inferiores = carregados prováveis; nomes, tipo, setas de seleção, eficácia, contador/pontos, preenchimento energético compartilhado e texto ENERGIA ... ESTIMADA. |
| 8 | Chips pequenos de tipos no topo | Exibem tipos próprios, tipos adversários e/ou tipos ofensivos relevantes/supereficazes conforme posição; manter os símbolos/cores aprovados e rotular a semântica sem confundir tipo real com fraqueza calculada. |
| 9 | Badge na reserva aliada 1 | MELHOR ✓, NEUTRO ○ ou EVITAR × para matchup de troca com Pokémon inimigo ativo; é overlay separado do card nativo. |
| 10 | Badge na reserva aliada 2 | Mesma análise independente do slot 1, com atualização em mudanças de oponente/estado. |
| 11 | Identificação do primeiro Pokémon reserva | O card com sprite, PC e barra HP é **nativo Pokémon GO e só pode ser lido**; o PvPPokeGo adiciona apenas avaliação/ícones próprios adjacentes. |
| 12 | Pequenos indicadores de tipo da segunda reserva | Overlay de tipos/matchup associado ao segundo slot sem alterar o card nativo. |
| 13 | Pequenos valores numéricos laterais (ex.: 8, 25, 32, 46) | **Hipótese a verificar**, possivelmente temporizador de troca/reentrada após switch e memória de inimigo vivo. Não classificar como nível ou contador de turnos sem auditar código e capturas sequenciais. |
| 14 | Telemetria compacta perto da arena | Textos por espécie/golpe e abreviações F/E; possível contagem de Fast e energia estimada. Decodificar unidades no código antes de afirmar sua precisão. Considerar modo diagnóstico recolhível. |
| 15 | SUPER EFICAZ! **próprio sob os carregados do inimigo** | Classificação de eficácia do golpe adversário contra Pokémon ativo; diferente do texto do Pokémon GO. Pode alimentar ameaça, dano e ESCUDO. Ex.: Dive (Água) contra Houndoom (Sombrio/Fogo) é supereficaz. |
| 16 | Setas ▼ ao lado dos nomes dos golpes inimigos | Abrem seletor manual AUTO · PvPoke/OCR: lista #1...#5 de possíveis carregados, tipo, dano base, custo E e ✓ do selecionado. Capturas: Vaporeon Surfar/Pancada Corporal/Raio Aurora/Pulso d’Água/Retorno; Sealeo Avalanche/Jogo Sujo/Soco de Gelo/Pinote Triplo/Retorno. Seleção manual substitui hipótese de move, sem zerar automaticamente energia armazenada. |
| 17 | Pequeno monitor HP 100% acima do centro | Indicação de percentual HP própria do overlay; **qual lado ele representa e acurácia dependem de auditoria**, não assumir que seja valor exato. |
| 18 | PREVIEW textual/numérico de dano à esquerda | Ex.: SEM ESC, dano em faixa percentual, HP sobrevivente, COM ESC, KO POSSÍVEL e MODELO. Previsão deve exibir faixa/confiança, não inventar certeza. |

**Outros elementos de overlay observados, fora da numeração:** cronômetro de sessão 0:00:00 e ícone pessoa/engrenagem associados (finalidade exata pendente); botão de fechar lista de golpes; lista de movimentos no modo AUTO · PvPoke/OCR; textos de tipo/energia/dano; pequenas bolinhas indicando geração/cargas e energia restante; indicação de seleção de golpe; menu de configuração; modo editar HUD; menu de apenas ícones; estados de gravação/visão. NÃO incluir barra PGSharp “Ataques ágeis / Ataques carregados / Shield” nem estrela PGSharp.

**Menu rápido global (capturas fora da batalha também):** Editar posições do HUD; Textos do HUD; Escala (ex. 80%); Opacidade (ex. 90%); Restaurar posições/layout; Mostrar opções avançadas; Gravar tela com PvPPokeGo; Pausar visão/análise; Remover sobreposição; Parar serviço PvPPokeGo; Modo somente ícones; Fechar menu. Em edição, controles inferiores observados: –, +, ◐, L, H, desfazer, ✓ confirmar, restaurar/reciclar; significado exato de ◐/L/H exige leitura do código. Não atribuir a esses controles ações não verificadas.

**Preferência visual:** HUD clean, profissional, minimalista, pequeno, translúcido, ajustável/travável, sem texto redundante, indicadores de tipo originais aprovados, desenhos de círculos compactos, componentes expansíveis só sob demanda, alertas temporários; exibir previsões sem poluir nem obstruir telas nativas de escudo/troca. Visual de reserva 0.5.17/0.5.28 como referência, mantendo todas as capacidades atuais.

#### C. Nova função especificada — três slots inteligentes do time AD-versário (PENDENTE DE IMPLEMENTAÇÃO/QA)

- Exibir **três slots fixos “? ? ?”** inicialmente; à medida que Pokémon adversários forem reconhecidos por nome/OCR/sprite/PC/troca, substituir somente o slot correspondente pelo **sprite correto da espécie/forma**, sem inventar identidade. Manter slots fixos por identidade/revelação; não reordenar a cada troca.
- Slot ativo tem borda/destaque discreto; reserva viva permanece visível com HP/energia armazenados; desmaiado tem tratamento cinza/X, com histórico guardado. Estado desconhecido não recebe nome inferido como fato.
- O usuário aprovou **brilho de energia**: sem brilho = sem carregado pronto confirmado/estimado; brilho suave/cor 1 = energia estimada para **ao menos uma** utilização de carregado; brilho cor 2 = energia estimada para **dois** carregados consecutivos. Pode complementar com • / •• para acessibilidade e evitar ambiguidade. Indicar confiança se move ainda não confirmado.
- **Não confundir dois carregados diferentes equipados com energia para executar duas vezes um carregado.** Cálculo de prontidão baseado na única energia compartilhada, custo de cada golpe e quantidade de utilizações consecutivas realmente possível até limite 100E. Não brilhar se o Pokémon estiver nocauteado, mesmo com energia residual histórica.
- Ícone continua armazenando seus dados quando adversário troca, e retorna ao estado anterior quando ele reaparece; energia e HP **não resetam na troca**.
- Não criar cartões grandes adicionais sobre Pokémon GO; preferir faixa compacta independente sem cobrir componentes críticos.

#### D. Motor de energia e turnos: equações e regras determinísticas

- Um turno de PvP = **0,5 s**; cada ágil tem duração em turnos, dano e energia por utilização próprios. **Não generalizar 10E por ágil nem converter valores de Ginásios/Reides para PvP**.
- Cada Pokémon dos dois lados precisa de registro independente: ID de slot/forma, ativo/reserva/desmaiado, espécie/PC, moves ágeis/carregados e confiança, intervalo de HP, escudos relevantes, energia como intervalo min/provável/max, último evento/turno, buffs/debuffs ativos, histórico.
- Atualização ao reconhecer ágil uma vez: E_nova = min(100, E_anterior + geração_do_golpe); atualizar AMBOS os medidores carregados conforme a **mesma reserva**; primeira cor até custo de uma utilização, segunda cor quando continua guardando para segunda. Se o golpe custar 40E, uma utilização fica disponível em 40E e duas em 80E; se custar 55E, duas não cabem em 100E.
- Ao reconhecer carregado usado: E_nova = max(0, E_anterior − custo_identificado), uma única vez por evento, inclusive se bloqueado. Nunca zerar automaticamente saldo, e nunca descontar em dobro por OCR repetido/câmera/escudo; em evento de golpe desconhecido, preservar intervalo de hipóteses até identificação, não subtrair custo arbitrário.
- Exemplo HIPOTÉTICO de ágil +10E, carregado 40E: 4 usos → 40E, anel cor 1; mais quatro → 80E, segunda cor; gastar 40 → 40E restante. Exemplo REAL a auditar no dataset PvP vigente: Raichu com Trovoada de Choques não deve assumir +10E sem olhar a tabela correta; valores da aba “Ginásios e Reides” não são os do PvP.
- HP caindo na barra nativa é **sinal de dano, não prova isolada de um ágil**: sincronizar duração/animação do golpe, intervalo entre impactos, dano sem outros eventos, fase da batalha e OCR. Dano pode vir de carregado, lag, frames perdidos ou mais de um golpe entre leituras. Priorizar estado e deduplicação de evento.
- A câmera mudar/focar defensor, tela de escudo, texto “X usou Y!” e retorno de câmera são pistas de **fase de carregado**. Mudança de câmera sozinha não prova golpe/custo; texto confirmado + eventos associados permitem descontar e voltar à contagem de ágeis.
- Troca de inimigo: preservar energia e HP individuais, moves, histórico, percentuais de confiança; restaurar no retorno; **resetar estágios temporários de Ataque/Defesa ao sair de campo**, de acordo com regra PvP, sem apagar energia. Pokémon nocauteado é marcado inativo até fim da luta e não reassumido por OCR incorreto.
- Corrigir/aprender a partir dos resultados observados; matemática determinística tem precedência sobre probabilidade de comportamento. Evitar suposições de intenção (“vai usar carregado” significa **pode usar** se energia suficiente).

#### E. OCR/visão e máquina de estados da batalha (somente leitura)

**Telas pré-batalha/seleção de liga/equipe:** ler liga atual (Grande, Ultra, Mestra), variações como Megaedição/copas, limite PC 1500/2500/sem limite, nome da equipe, seleção recomendada/manual, sprites, espécies, CP e ordem dos 3 aliados. Esses componentes são NATIVOS e não se redesenham. Preparar time próprio e regra de ranking/moves, mas validar disponibilidade real do formato.

**Durante batalha, ROIs adaptativas por posição:** cabeçalho sup. esquerdo/direito (nomes, PC, shields e Pokébolas); HP nativo esquerdo/direito (preenchimento, cores, perdas e desmaio); dois cartões nativos aliados à direita (sprite, PC e HP); botões nativos carregados inferiores (nome, tipo, disponibilidade/preenchimento, eficácia exibida); centro (mensagens de ataques/status); contador superior (contexto e tempo); modais de escudo e troca forçada. Suportar proporções/resoluções diferentes e overlays externos sem confundir controles do PGSharp.

**Eventos observáveis:** “Sneasel usou Jogo Sujo!”, “BLOQUEADO!”, “ATAQUE AUMENTOU!”, “ATAQUE DIMINUIU!”, “DEFESA AUMENTOU!”, “DEFESA DIMINUIU!”, qualificadores “MUITO”, “SUPER EFICAZ!”, “NÃO É MUITO EFICAZ”, tela “Quer usar um escudo protetor?”, “AGORA NÃO”, contadores, modal “Quer trocar por um Pokémon novo?”, troca voluntária, nocaute, HP e opções de time. Associar sempre ator, alvo, timestamp/turno, confiabilidade, efeito do escudo e estágio exato quando suportado. Alterações de status podem ocorrer **mesmo se o carregado for bloqueado**; registrar golpe, shield, dano, modificador e resultado em eventos separados.
  
**FSM recomendada:** PRE_BATTLE → BATTLE_FAST → CHARGED_PENDING → SHIELD_DECISION → CHARGED_RESOLUTION → BATTLE_FAST; ramo FAINT → FORCED_SWITCH → BATTLE_FAST; SWITCH → sincronizar estado e atualizar matchup. Os contadores de escudo, substituição obrigatória e cooldown da troca comum são temporizadores DIFERENTES. Eventos OCR mantidos em frames consecutivos não devem ser contabilizados múltiplas vezes.

**Prioridades de leitura:** 1) golpe identificado/HP/shield/status/troca, 2) CP/espécie/energia/contadores e equipe, 3) metadados auxiliares. Sempre distinguir CONFIRMADO vs ESTIMADO vs INCERTO; permitir correção manual de moves sem apagar a energia corretamente acumulada. OCR observa elementos nativos; **sem comandos automáticos/toques nos controles do jogo**.

#### F. HP real, estimativa de dano, bônus e fontes

- **HP exato do aliado via tela de detalhes** quando disponível: captura do Houndoom PC 1499 mostrava **120/120 PS = 120 HP máximos**, 100%. Vincular espécie/forma/PC/IV/nível/identidade do indivíduo, não apenas nome da espécie, e aproveitar na seleção da equipe. HP em batalha = leitura do preenchimento × HP máximo conhecido, com tolerância de pixels/arredondamento; calibrar por perdas sucessivas.
- Observação do overlay em outra captura: **Houndoom HP: 35**, **Sealeo HP: 245**; valores não devem ser assumidos exatos só por aparecerem. Se 245 for HP real do Sealeo, **provável inconsistência** com limites válidos da espécie/nível; investigar associação ao Pokémon errado, stats desatualizados, OCR, unidades e fórmula; validar contra Game Master atual.
- Para inimigos com CP conhecido e IVs/nível desconhecidos, gerar candidatos de nível+IV (e forma) compatíveis, obter faixa de HP máximo e inferir HP atual pela barra. Não afirmar HP exato por CP isolado; exemplos anteriores com “Raichu 1499 ~104–118 HP” são **ilustrativos e devem ser revalidados** com stats, níveis/CPM e forma atuais. Valores “ginásio/reides” na tela de golpes (por exemplo Raichu Trovão de Choques, Soco Trovoada, Desbravar) NÃO são diretamente o dano PvP nem o custo de energia PvP.
- Fórmulas básicas de referência, sob dados vigentes do jogo: HP_max = floor((Stamina_base_GO + IV_stamina) × CPM_nível), com piso aplicável; CP = floor[(Atk_base+IV_A) × sqrt(Def_base+IV_D) × sqrt(Stamina_base+IV_S) × CPM² / 10]. Usar níveis, formas, CP multipliers, buffs e tabelas atuais; não copiar HP-base das gerações principais como se fosse HP no GO.
- Dano PvP exige poder PvP, Ataque efetivo do atacante, Defesa efetiva do defensor, bônus STAB (~1,2), eficácia de tipos (por multiplicadores oficiais; supereficaz ~1,6; pouco eficaz ~0,625), Sombroso/clima/condições aplicáveis e estágios de Ataque/Defesa (aprox. +1 ×1,25; +2 ×1,5; −1 ×0,8; demais via fórmula oficial), com floor/arredondamento do jogo. **Poder base 50 ou 60 mostrado fora da aba PvP NÃO equivale a −50/−60 HP.** Clima geralmente não modifica dano em batalhas de treinador GBL: validar modo antes de aplicar “bônus do momento”.
- Comparar dano observado (ΔHP) com dano previsto, atualizar intervalos de estatísticas possíveis e confiança; não inferir um ágil apenas de cada variação da barra. Preview nº 18: faixas de dano/HP com escudo e sem escudo, aviso possível KO, plano de mitigação; nº 17 percentual de HP com origem clara. Mostrar “aprox.” e confiança na previsão.
- Diferenciar matchup e eficácia do golpe específico: ✓ verde/○ neutro/× vermelho refere-se à avaliação do confronto; chips de tipos/círculos individuais mostram eficácia dos golpes adversários e indicadores nos carregados do próprio jogador são nativos e apenas lidos.

**Fontes fornecidas para auditoria de HP/CP/moves e comentários (verificar qualidade/data, não adotar fórmulas antigas sem revisão):**
- Reddit curto: https://www.reddit.com/r/TheSilphRoad/s/LnqUPTcmfI
- Reddit curto: https://www.reddit.com/r/TheSilphRoad/s/NUhsqaEbna
- Reddit curto: https://www.reddit.com/r/TheSilphRoad/s/SsREfbHowZ
- Reddit curto: https://www.reddit.com/r/TheSilphRoad/s/96212SI7pC
- Reddit curto: https://www.reddit.com/r/TheSilphRoad/s/hzvOylwaIB
- Bulbapedia fornecida: https://bulbapedia.bulbagarden.net/wiki/List_of_Pok%C3%A9mon_by_base_stats_(Generation_VI-present))_(as_%60HP%60 (URL a normalizar/conferir; stats dos jogos principais ≠ base de GO).
- Planilha histórica: https://docs.google.com/spreadsheets/d/1xZ1daunztOgvOZxO7ABfJPF41-2mqMDzY1Qs_0OWwq8/edit#gid=399231321 ; abas: Pokemon CP/HP, Leveling Info, Go Base Stats, Type Advantage Chart, Move sets, Pokemon Science 2.0 data e outras. São dados históricos, NÃO Game Master atual.
- https://pokemongo.gishan.net/o/hp
- https://db.pokemongohub.net/tools/cp-calculator
- Referências históricas listadas na própria planilha: Reddit “how hp/maxstamina is calculated”, “exact pokemon cp formula”, “pokemon hp tier list” etc. Não afirmar ter lido todos os comentários de links curtos quando inacessíveis; validar diretamente antes de citar achados como fatos.
- Bancos por versão com Game Master GO + snapshot PvPoke, tipagem/forma/IV/CPM e data de atualização, com testes e licenças.

#### G. Recomendações de UX/QA e estados de entrega

**Fluxo de HUD preferido:** sempre visível e compacto apenas matchup principal, chips de tipos aprovados, monitor de golpes/energia, conselho tático, badges de reservas e (futuramente) três slots inimigos; mostrar PREPARE-SE/ESCUDO/KO/dano como alertas transitórios; listas de movimentos, telemetria, OCR confidence/histórico, cronômetro e configurações apenas sob demanda. Durante modais NATIVOS de escudo e escolha forçada, recolher menus grandes para não encobrir botões; permitir que o jogo mantenha todos os toques.

**Critérios de aceitação futuros:**
1. Diferenciar visualmente componentes GO × PGSharp × PvPPokeGo em capturas, sem alterar região nativa.
2. Estados olho/cadeado/ferramenta e modo edição funcionam dentro e fora de batalha, com escala/posição/opacidade persistidos.
3. Matchup ativo 3 estados, chips de tipo e eficácia inimiga nº 15 corretos em trocas.
4. Monitor ágil+2 carregados utiliza energia compartilhada, custo e geração PvP corretos, dupla carga, desconto residual e deduplicação.
5. Pokémon adversário saiu e voltou: mantém energia/HP/moves; buffs/debuffs resetam em troca; após KO ele fica desmaiado.
6. Reconhecimento de tela de escudo, OCR de golpe, BLOQUEADO, buff/debuff, tela de troca, contadores e câmera, com confiança/estados coerentes.
7. Novo painel de três inimigos começa ???, revela sem swap de identidade, tem 0/1/2 brilhos matematicamente possíveis, ativo/reserva/desmaiado e mantém posição fixa.
8. HP do aliado exato vindo de ficha quando conhecido; inimigo por faixa; rejeitar HP físico impossível; avaliar exemplo Houndoom 120/120 e Sealeo 245 suspeito.
9. Preview de dano/sobrevivência com ranges; sem confundir poder de golpe, energia e HP, nem aplicar modificadores PvE ao PvP.
10. Verificar legibilidade e ausência de sobreposição em aparelho Android/Samsung, modal de escudo, escolha de equipe, troca forçada, configurações PGSharp presentes e diferentes resoluções.
11. Não remover funções existentes, não reintroduzir cards de reserva grandes nem UI carregada; comparar com referência visual 0.5.17.
12. Escrever testes unitários de estado/energia/dano + testes com sequência de screenshots/vídeo reais; não marcar como concluído só pelas capturas.

**STATUS EM 2026-10-08:** documentação de requisitos visuais, mecanismos e sugestões = **CONCLUÍDO (neste guia)**. **Implementação, teste de regressão, APK e QA visual no Samsung para os novos requisitos = NÃO VERIFICADOS/PENDENTES**. A entrega técnica anterior 0.5.29 permanece o último build conhecido no histórico, sem confundir este adendo com novo build. Não alterar código/Gradle/versionCode/workflows nem sincronizar APK sem pedido específico.

**PRÓXIMO PASSO:** ler Guia Mestre/Checklist em release/0.5.29 e auditar as implementações existentes contra esta lista, classificando cada função como EXISTENTE-VALIDADA / EXISTENTE-COM-FALHA / PARCIAL / NOVA / BLOQUEADA. Implementar apenas deltas necessários, testes e documentação incremental na branch ativa, preservando visual e recursos anteriores. Somente depois compilar/QA físico/publicar artefato real e registrar commit, SHA-256 e link.



### 2026-10-08 — Inventário complementar dos MENUS: menu rápido na batalha + configurações da tela inicial (0.5.29)

**Origem:** sequência de capturas de tela do usuário da versão 0.5.29, seguida de inventários textuais aprovados durante a conversa. Este trecho complementa (não substitui) o adendo anterior de 2026-10-08 sobre a interface de batalha, energia e OCR. **Estado de evidência: identificado VISUALMENTE; não confundir controle visível/toggle ligado com função tecnicamente validada no APK.** Nenhum código foi alterado como consequência desta especificação.

**HEADs vistos antes da edição documental:** release/0.5.26 = `550fa08c8be1beb20bce4884d3a4c8c650c1dd62` e release/0.5.29 = `df26f33ed7dbdc6282eec5e65b9d77a0071aba52`. Branch default `main` não é a release ativa. A entrega técnica está identificada como 0.5.29 nas capturas. Atualizar as duas cópias do guia para evitar perda de contexto, mantendo 0.5.26 como referência histórica e 0.5.29 como continuação ativa. Estes SHA são PRÉ-COMMIT e deverão ser substituídos, na conferência final, pelos HEAD novos.

#### H. Menu rápido ABERTO sobre o Pokémon GO (HUD / batalha; inventário completo observado)

**Acesso:** pelo controle do PvPPokeGo (chave inglesa/engrenagem). O menu pode aparecer sobre a tela normal do mapa fora de batalha e deve manter o Pokémon GO completamente funcional. Capturas mostram segmentos diferentes da mesma lista rolável e visualização com opções avançadas expandidas/recolhidas. Não confundir o ícone de estrela **da opção textual no nosso MENU** com a **estrela amarela do PGSharp sobre a cena de batalha**, que NÃO é nossa.

| Grupo | Ícone observado | Rótulo literal / estado | Contrato funcional para auditar |
|---|---|---|---|
| BATALHA | ◉ | Ocultar HUD | Esconde HUD de batalha e mantém mecanismo seguro de recuperação/mostrar. Diferenciar da opção global Overlay no app. |
| BATALHA | ★ | Modo batalha assistido | Alterna recomendações táticas e alertas visuais; NÃO envia cliques/comandos ao Pokémon GO. |
| BATALHA | △ | Liga: Automático | Alterna/seleciona modo de liga; respetivo valor deve sincronizar com escolha da tela inicial. |
| BATALHA | ↔ | Editar posições do HUD | Abre modo de reposicionamento do HUD e controles de edição; não mover controles nativos. |
| HUD / VISUAL | T | Textos do HUD | Alternar legendas/textos visíveis do HUD, respeitando modo somente ícones. |
| HUD / VISUAL | S | Escala 80% ou 90% (observada) | Ajuste de escala; dois valores observados em capturas diferentes, não duas opções distintas. Persistir mudanças. |
| HUD / VISUAL | ◐ | Opacidade 90% ou 100% (observada) | Ajuste de opacidade; valores variáveis, não duplicatas. Persistir mudanças. |
| HUD / VISUAL | ↺ | Restaurar posições/layout | Restabelecer layout padrão com confirmação adequada e possibilidade de recuperar backup; evitar reset acidental. |
| ANÁLISE | A | Reconhecimento automático | Liga/desliga pipeline de visão/OCR sem alterar o jogo. |
| ANÁLISE | ◇ | Fraquezas do inimigo | Chips/indicadores dos tipos e suas fraquezas no topo do overlay. |
| ANÁLISE | ✓ | Indicador Pokémon atual | Avaliação do confronto do Pokémon ativo (melhor, neutro, evitar) com 3 estados. |
| ANÁLISE | 2 | Analisar reservas | Avaliação dos dois aliados de reserva, separada dos cards nativos. |
| ANÁLISE | T | Tipos das reservas | Tipos/ícones dos aliados reservas; não modificar sprites/cards originais. |
| ANÁLISE | ⚔ | Ataques do inimigo | Monitor com golpe ágil, até dois carregados e seleção manual/OCR. |
| ANÁLISE | •• | Contagem de carregados | Exibir pontos/cargas/contagem para ataques do inimigo; sinônimo potencial de “Pontos de carregamento” na tela inicial; validar se a configuração compartilhada é a mesma. |
| ANÁLISE | ⚡ | Progresso de energia | Energia residual prevista por Pokémon adversário, duas camadas visuais no carregamento e conservação após troca. |
| ANÁLISE | HP | HP e previsão de dano | HP conhecido/estimado e prévia de dano provável, respeitando incerteza e estado de escudo. |
| ANÁLISE | ↻ | Timer de troca | Cronômetro de cooldown de troca, separado de temporizador de escudo e troca forçada. |
| ANÁLISE | H | Histórico do inimigo | Informação resumida das observações/moves/energia dos inimigos; distinta do Histórico de batalhas completo na tela inicial. |
| ANÁLISE | D | Diagnóstico / DEBUG | Telemetria técnica / OCR para depuração (sem poluir interface padrão). |
| EXPANSÃO | ••• / ‹ | Mostrar opções avançadas / Ocultar opções avançadas | **UM** controle de dois estados; expande/recolhe opções do menu e modifica quais grupos ficam visíveis. |
| SISTEMA | ● | Gravar tela com PvPPokeGo | Capturar gravação interna com permissões adequadas, sem interferir na captura usada para OCR. |
| SISTEMA | Ⅱ | Pausar visão/análise | Congela/pausa processamento; deve distinguir de ocultar HUD, remover overlay e parar serviço. |
| SISTEMA | × | Remover sobreposição | Retira somente overlay sem fingir que todo serviço foi interrompido; funcionalidade real a validar. |
| SISTEMA | ■ | Parar serviço PvPPokeGo | Interrompe serviço e recursos/captura necessários; diferente de remover apenas a HUD. |
| SISTEMA | ▦ | Modo somente ícones | Oculta legendas/textos extensos mantendo ícones corretos. |
| SISTEMA | × | Fechar menu | Fecha apenas menu rápido, não encerra app ou batalha. |

**Contagem do inventário do MENU:** 4 (Batalha) + 4 (HUD/Visual) + 12 (Análise) + 1 (controle de expansão com dois estados) + 6 (Sistema) = **27 opções/controles distintos** nas capturas combinadas; não necessariamente todos aparecem simultaneamente devido a rolagem, modo expandido e contexto. **Elementos auxiliares do próprio HUD** também vistos com menu aberto: olho ocultar/mostrar e cadeado travar/destravar, além do status HP% e marcador `?`; não contabilizar esses como novas opções da lista. O ícone cadeado VERDE circulado pelo usuário representa o estado do CONTROLE DO PvPPokeGo, e não um ícone original do Pokémon GO.

**Comportamentos especiais e erros a evitar:** não atribuir “fechar aplicativo” ao × de “Fechar menu”; não confundir remover overlay com parar serviço; não trocar a arte oficial delicada do olho/cadeado por emojis; preservar áreas de toque associadas aos próprios ícones, sem hitboxes invisíveis distantes; menu deve ser exibido e editável com ou sem batalha; controles não devem cobrir o toque no jogo; exportação de posições antes de “restaurar layout”.

#### I. Tela INICIAL do aplicativo PvPPokeGo 0.5.29 — inventário integral das quatro capturas

**Título/descrição visível:** PvPPokeGo — “Assistente visual de batalhas PvP”; “Analisa a tela e mostra informações em tempo real sem enviar comandos ao Pokémon GO”. A tela mostra status “Pronto para iniciar o HUD”, instrução resumida “HUD: 🔓 mover • 🔒 travar • ◉ mostrar/ocultar • ⚙ menu rápido”, pasta interna de gravações `Movies/PvPPokeGo` e versão **0.5.29**. O texto de apresentação é a intenção de funcionamento: ausência de comandos automáticos ao jogo permanece regra inviolável.

**Seção BATALHA — 4 botões visíveis:**

| Controle | Texto | Função pretendida / validação necessária |
|---|---|---|
| 1 | Iniciar | Iniciar serviço/HUD, obter permissões necessárias e sincronizar toggles sem iniciar duas sessões em duplicidade. |
| 2 | Parar | Encerrar serviço/HUD; verificar diferenças em relação a Pausar ou Remover sobreposição. |
| 3 | Gravar | Começar/parar gravação local conforme estado real; revisar permissões e localização `Movies/PvPPokeGo`. |
| 4 | Nova batalha | Inicializar novo registro cronológico e estado de combate, com política explícita para sessão anterior. |

**Seção ASSISTÊNCIA E HUD — 3 toggles:**

1. **Overlay** — liga/desliga camada sobre outras aplicações; nas capturas aparece LIGADO (roxo).
2. **Reconhecimento automático** — liga/desliga reconhecimento visual/OCR; na captura aparece LIGADO.
3. **Modo Batalha Assistido** — mostra previsões/recomendações “CONTINUE FAST”, “TROQUE”, “ESCUDO”, “NÃO ESCUDE” e alerta de Charged, **sem tocar/enviar comandos** ao Pokémon GO; na captura aparece LIGADO.

**Seção LIGA E ANÁLISE — 4 botões de seleção de liga + 11 toggles:**

**Seleção “Liga / ranking PvP”:** `Automático` (selecionado na captura) / `Liga Grande` / `Liga Ultra` / `Liga Mestra`. Texto de status: “Automático: ler o lobby. Última detectada: Liga Grande”. A seleção manual/automática deve sincronizar com a opção `Liga: Automático` do menu rápido. Identificação de copas/edições especiais exige regra própria, não inferir só por PC.

| # | Toggle literal da tela inicial | Status visual no print | Mapeamento desejado ao menu rápido |
|---|---|---|---|
| 1 | Fraquezas do inimigo no topo | LIGADO | “Fraquezas do inimigo” |
| 2 | Indicador do Pokémon atual | LIGADO | “Indicador Pokémon atual” |
| 3 | Analisar reservas | LIGADO | “Analisar reservas” |
| 4 | Mostrar tipos das reservas | LIGADO | “Tipos das reservas” |
| 5 | Ataques do inimigo | LIGADO | “Ataques do inimigo” |
| 6 | Pontos de carregamento | LIGADO | “Contagem de carregados” — confirmar flag compartilhada |
| 7 | Progresso de energia | LIGADO | “Progresso de energia” |
| 8 | HP e previsão de dano sem escudo | LIGADO | “HP e previsão de dano” — texto pode ser mais completo no menu rápido |
| 9 | Timer de troca | DESLIGADO | “Timer de troca” |
| 10 | Modo edição — segure e arraste | DESLIGADO | “Editar posições do HUD” — verificar semântica/segurança do bloqueio |
| 11 | DEBUG | DESLIGADO | “Diagnóstico / DEBUG” |

Textos de ajuda nas capturas:
- Energia: “A previsão inimiga usa turnos de 0,5 s, energia residual e hipóteses de Fast Move. Quando há incerteza, mostra faixa/READY? em vez de inventar um valor exato.”
- HP: “Mostra HP estimado e uma faixa fantasma sobre a barra quando um carregado inimigo pode chegar. Dano observado tem prioridade; sem amostras, usa o modelo offline PvPoke como estimativa.” A parte final da descrição aparece parcialmente cortada nas capturas, então auditar implementação antes de ampliar a promessa.

**Seção APARÊNCIA / PERSONALIZAÇÃO — 2 sliders dentro de LIGA E ANÁLISE/aba inicial:**

1. `Escala: 80%` — no exemplo, controle deslizante; outras capturas do menu rápido exibem 90%. A escala é ajustável, não duas funções separadas.
2. `Opacidade: 90%` — slider; outros prints do menu rápido exibem 100%. A opacidade é ajustável, não duas funções separadas.
Ambos devem persistir, exportar/importar e atualizar imediatamente o menu rápido; limites reais exigem auditoria.

**Seção BACKUP E DIAGNÓSTICO — 3 ações visíveis:**

1. `Exportar` (configurações e posições): salva toggles/opções, escala, opacidade e posições personalizadas de ícones, blocos e menus do HUD. Evitar perda de posições já aprovadas.
2. `Importar` (configurações e posições): restaura um backup; texto original informa que **o layout fica travado por segurança ao importar**. Verificar versão de esquema/backward compatibility antes de sobrescrever.
3. `Exportar diagnóstico (.zip)`: pacote contendo configurações, posições, histórico, confiança/origem dos eventos e dados do aparelho. Consentimento e minimização de dados importantes ao compartilhar.

**Seção HISTÓRICO DE BATALHAS — 3 controles visíveis:**

1. `Atualizar`: atualizar/recarregar a lista.
2. `Exportar ZIP`: exportar histórico quando houver batalhas. Formatos anunciados no próprio texto: **.txt legível, .json completo e .csv em tabela**.
3. `Limpar histórico`: apagar registros, idealmente confirmar ação destrutiva. Na captura, exportar e limpar aparecem **desabilitados**, com “Nenhuma batalha registrada ainda”.
- Modelo de apresentação especificado: **conversa cronológica** com balões separados para “você”, “inimigo” e “assistente”; validar ligação aos registros reais antes de alegar funcionalidade.

**Seção SOBRE — informações, não controles configuráveis:**
- Crédito exibido no app: “Desenvolvimento PvPPokGo by: LucianoToscano”; contato de suporte **(84) 99131-9021**; versão 0.5.29.
- Texto: “Base visual V03 preservada. O HUD protege a interface nativa do Pokémon GO, permite reposicionar blocos no modo edição e mantém o botão de olho no topo.”
- Conferir ortografia e consistência entre marca `PvPPokeGo` e créditos `PvPPokGo`, sem remover crédito do desenvolvedor.

**Contagem da TELA INICIAL:** 4 (Batalha) + 3 (Assistência e HUD) + 4 (Seleção da liga) + 11 (Ligações de análise) + 2 (Escala/Opacidade) + 3 (Backup e diagnóstico) + 3 (Histórico) = **30 controles/opções** visíveis nas capturas, além de textos de descrição e seção “Sobre”. Valores ou estados roxos/cinzas são evidência visual da captura, NÃO teste automatizado de toggle nem garantia de persistência.

#### J. Regra de sincronização dos 2 caminhos de configuração e checklist de QA

- **Uma única configuração persistida por funcionalidade**, independentemente do caminho (tela inicial versus menu rápido). Ajustar escala/opacity, liga, reconhecer automático, modo assistido, fraquezas, matchup ativo, reservas, tipos, ataques, carga, energia, HP/previsão, timer, edição e DEBUG em um lugar deve refletir no outro sem reiniciar tudo.
- Normalizar rótulos inconsistentes SEM mudar semântica: `Pontos de carregamento` ↔ `Contagem de carregados`; `HP e previsão de dano sem escudo` ↔ `HP e previsão de dano`; `Reconhecimento automático` aparece nas duas interfaces; `Editar posições do HUD` ↔ `Modo edição — segure e arraste`; `Fraquezas do inimigo no topo` ↔ `Fraquezas do inimigo`.
- Garantir diferenciação de estado e comandos: `Overlay` global ≠ `Ocultar HUD` temporariamente ≠ `Modo somente ícones` ≠ `Pausar visão/análise` ≠ `Remover sobreposição` ≠ `Parar serviço`. Mostrar a ação correta e não quebrar o fluxo de retomada.
- Na interface inicial, botões `Iniciar`, `Parar`, `Gravar`, `Nova batalha` devem refletir estado real e permissões; evitar botões semanticamente ativos mas inoperantes. História/diagnóstico devem oferecer exportações realmente legíveis; exportar/limpar desabilitados quando não houver histórico.
- Ao importar backup de configurações, manter locks seguros e reconstituir posições tanto fora como dentro da batalha; não deslocar olhinho/cadeado/engrenagem. Validar formato e migrations em upgrades; restaurar só com ação explícita.
- Funcionalidade desligada no menu inicial não deve reaparecer por padrão no HUD; e configuração escondida por “Opções avançadas” não deve perder valor ao recolher menu.
- Respeitar a distinção já documentada entre **controles externos do PGSharp**, **elementos nativos do Pokémon GO somente para leitura**, e controles desenhados pelo próprio PvPPokeGo.
- Para QA: capturar (1) tela inicial inteira com scroll e modos on/off, (2) menu rápido completo e recolhido, (3) transição para edição/layout, (4) backup/restauração com posições personalizadas, (5) OCR em batalha real, (6) comportamento quando outro overlay está presente, (7) exportação do histórico com e sem registros; comparar resultado com prints antes de considerar “funcionando”.

**STATUS:** este inventário/contrato visual está **REGISTRADO NO GUIA**, mas a auditoria dos toggles, sincronização, testes de Android e APK com novas correções **NÃO FOI FEITA NESTA ETAPA**. Este commit é SOMENTE de documentação; não é build/release nova. Toda mudança futura nos menus deve gerar novo registro cronológico no guia da branch ativa e (quando relevante) no espelho histórico solicitado, incluindo branch/HEAD real, arquivos alterados, teste, pendências e links de artefatos verificados.



### 2026-10-08 — Arena: fundação de ranking, campeonato, medalhas e recompensas

Versão-base: `0.5.29` (`versionCode 34`, **inalterados**)  
Branch de implementação isolada: `feature/arena-foundation-20261008`  
Branch base histórica/ativa conferida: `release/0.5.29` (HEAD inicial `952ff14e5f97fb461619946473a42c144cdc2caa`)  
Referência histórica lida: `release/0.5.26`, sem rollback nem edição nesta etapa.  
Commits de implementação: `9aba3959`, `23e2235e`, `b15cce50`, `869144ca`, `8f62fad7`, `b3d18557`.  
Especificação: `docs/ARENA_RANKING_TOURNAMENTS_REWARDS.md`.

OBJETIVO/PROBLEMA:
- Solicitação expressa do usuário para incorporar ranking do PvPPokeGo, XP, perfis, torneios, recompensas e medalhas tomando o mockup recebido como inspiração.
- Preservar a aparência compacta do HUD da 0.5.17, a recuperação OCR 0.5.29, launcher, backups e todas as funções atuais.

ALTERAÇÕES:
- Adicionado painel `ArenaDashboard` expansível na tela inicial do app, sem modificar nenhuma rotina do HUD, OCR, captura, reconhecimento, histórico ou motor de batalha.
- Criados apelido editável local, resumo do histórico **disponível**, XP cosmético e faixas de atividade Bronze/Prata/Ouro/Platina/Diamante/Mestre e cinco medalhas com progresso real. Não confundir essas faixas com ranking global.
- Criadas regras puras de aprovação **provisória** de resultados com dois relatos complementares, Elo inicial K=32/base 1000 por temporada/liga, rejeição de duplicatas, gerador de primeira rodada eliminatória para número de inscritos potência de dois.
- Seções Ranking, Campeonatos e Recompensas identificam explicitamente funções online não ativadas; sem usuários fictícios, vitórias imaginárias, promessas de PokéCoins/passes, prêmios distribuídos ou upload implícito.
- Criado documento de regras, etapas, integridade competitiva, moderação e critérios de aceite; checklist atualizado.
- Mudanças feitas somente na branch de feature para não interferir em trabalhos/branches de releases em paralelo.

ARQUIVOS PRINCIPAIS:
- `app/src/main/java/com/lucianotoscano/pvppokego/arena/ArenaProgress.kt`
- `app/src/main/java/com/lucianotoscano/pvppokego/arena/ArenaCompetitionEngine.kt`
- `app/src/main/java/com/lucianotoscano/pvppokego/ui/ArenaDashboard.kt`
- `app/src/main/java/com/lucianotoscano/pvppokego/ui/SettingsScreen.kt`
- `app/src/test/java/com/lucianotoscano/pvppokego/arena/ArenaProgressTest.kt`
- `app/src/test/java/com/lucianotoscano/pvppokego/arena/ArenaCompetitionEngineTest.kt`
- `docs/ARENA_RANKING_TOURNAMENTS_REWARDS.md`
- `docs/CHECKLIST-PvPPokeGo-0.5.29.md`
- `docs/PROJECT_CONTINUITY_GUIDE.md`

TESTES:
- Arquivos de teste JUnit adicionados, cobrindo resultado explícito, XP/idempotência, streak, três ligas, relatos conflitantes e únicos, rating e chave.
- **Não afirmar que passaram até executar**. Build Android, renderização Compose e Samsung/QA físico ainda não realizados nesta etapa.
- APK não foi produzido e `versionName/versionCode` não foram alterados.

RESULTADO:
- MVP local de perfil/progresso/medalhas e fundação matemática do ranking/torneios, isolados do HUD existente; backend competitivo e prêmios permanecem por fazer.

STATUS:
**PARCIAL** — código e especificação no branch de feature; sem Android QA/APK.

PENDÊNCIAS:
- Testar compilar e tela real, revisar medidas no celular.
- Online exige autenticação, servidor autoritativo, banco, monitor antifraude, moderação e consentimento.
- Histórico existente é limitado a 50 partidas: números locais são referentes aos registros ainda salvos e não são contadores vitalícios.
- Implementar campeonatos operacionais, desafios, medalhas persistentes, inventário e premiação conforme checklist; requisitos legais/fornecedores para prêmios reais.

PRÓXIMO PASSO:
- Rodar testes/assemble Standard e QA da interface; corrigir falhas antes de merge; só então versionar novo APK legítimo.
- Evoluir backend e eventos verificados em etapa separada; a captura proposta pelo usuário é referência visual de produto, não evidência de funções já operantes.


### 2026-10-08 — Arena: testes Kotlin direcionados aprovados

Branch de fonte: `feature/arena-foundation-20261008` (base `release/0.5.29`).  
Branch de build isolada: `LucianoToscano/PvPPokeGo-APK-Build:preview/arena-0.5.30-20261008`.  
PR: https://github.com/LucianoToscano/PvPPokeGo/pull/35 (draft).  
Run direcionado: https://github.com/LucianoToscano/PvPPokeGo-APK-Build/actions/runs/37873242217 .

RESULTADO:
- Arquivos de código/testes da Arena e SettingsScreen conferidos idênticos por SHA de blob entre fonte privada e snapshot público.
- Android SDK 36 + Gradle 8.11.1 + JDK 17, tarefa `:app:testStandardDebugUnitTest --tests 'com.lucianotoscano.pvppokego.arena.*'`: **SUCCESS**.
- Painel Arena movido para perto do topo da tela inicial, antes do card Batalha; resumo de faixa/XP visível mesmo recolhido. Nenhuma alteração em tela nativa do Pokémon GO nem HUD de batalha.
- Checklist e especificação atualizados para refletir evidência real; testes novos não apenas adicionados, agora executados.
- Build completo de preview 0.5.30 permanece PENDENTE. Não afirmar APK entregável, assinatura ou teste Samsung.

STATUS: **PARCIAL — testes direcionados concluídos; APK/QA aguardando.**

PRÓXIMO PASSO: conferir `:app:testStandardDebugUnitTest` integral, `:app:assembleStandardDebug`, `apksigner`, SHA-256 e resultado do artifact; corrigir erros de CI se houver.

### 2026-10-08/09 — PvPPokeGo Arena 0.5.30: APK Standard QA gerado e verificado

Versão de prévia: `0.5.30` (`versionCode = 35`)  
Branch privada de desenvolvimento: `feature/arena-foundation-20261008` (originada em `release/0.5.29`, HEAD inicial `952ff14e5f97fb461619946473a42c144cdc2caa`)  
PR de integração: https://github.com/LucianoToscano/PvPPokeGo/pull/35 (**draft**, sem merge para a release)  
Branch pública de CI: `LucianoToscano/PvPPokeGo-APK-Build:preview/arena-0.5.30-20261008`  
HEAD compilado da cópia de build: `8fc87cce058bfe23e505e5b9dffb18162f65a651`  
Run completo: https://github.com/LucianoToscano/PvPPokeGo-APK-Build/actions/runs/37873242187  
Run testes isolados: https://github.com/LucianoToscano/PvPPokeGo-APK-Build/actions/runs/37873242217

OBJETIVO/PROBLEMA:
- Entregar QA local da primeira implementação de perfil, XP/medalhas, regras de pontuação e torneios PvPPokeGo sem interferir em HUD e OCR da 0.5.29.
- Corrigir o versionamento da branch privada para corresponder ao APK efetivamente compilado.

ALTERAÇÕES:
- Introduzidos `ArenaProgress`, `ArenaCompetitionEngine`, `ArenaDashboard`, integração mínima no topo de `SettingsScreen`, testes JUnit e documentação Arena/Checklist.
- Alterado `app/build.gradle.kts` na branch de feature para `versionName 0.5.30` e `versionCode 35` após êxito do snapshot público.
- Registro espelhado no Google Drive do Guia Mestre (espelho, não substituto do arquivo GitHub).

ARQUIVOS:
- `app/src/main/java/com/lucianotoscano/pvppokego/arena/ArenaProgress.kt`
- `app/src/main/java/com/lucianotoscano/pvppokego/arena/ArenaCompetitionEngine.kt`
- `app/src/main/java/com/lucianotoscano/pvppokego/ui/ArenaDashboard.kt`
- `app/src/main/java/com/lucianotoscano/pvppokego/ui/SettingsScreen.kt`
- `app/src/test/java/com/lucianotoscano/pvppokego/arena/ArenaProgressTest.kt`
- `app/src/test/java/com/lucianotoscano/pvppokego/arena/ArenaCompetitionEngineTest.kt`
- `app/build.gradle.kts`
- `docs/ARENA_RANKING_TOURNAMENTS_REWARDS.md`
- `docs/CHECKLIST-PvPPokeGo-0.5.29.md`
- `docs/PROJECT_CONTINUITY_GUIDE.md`

TESTES:
- `:app:testStandardDebugUnitTest --tests 'com.lucianotoscano.pvppokego.arena.*'` no run 37873242217: **SUCCESS**.
- `:app:testStandardDebugUnitTest :app:assembleStandardDebug` no run 37873242187: **SUCCESS / BUILD SUCCESSFUL in 46s**.
- Pacote offline PvPoke e atlas visual gerados pelo workflow: PASS.
- ZIP do artifact e arquivo APK: integridade `unzip -t` PASS; assinatura APK v2 debug: true, um signatário.
- SHA-256 computado no CI e reconfirmado após extração: **`4069ced7db03ea6e5d104b8d13826438802a671853b4b228784a3ad225ac5e7a`**.
- Tamanho real do APK: **79.283.282 bytes**.
- Artifact GitHub `PvPPokeGo-0.5.30-QA-debug`, ID `11591126817`; inclui APK, `SHA256SUMS.txt` e relatório `apksigner`.
- ZIP final de distribuição no Google Drive oficial, upload aceito e metadados verificados (35.770.210 bytes): https://drive.google.com/file/d/1lMFVEQrq6sc37iDdFrw2upCAfaZrDDp9/view?usp=drivesdk
- **Nenhum teste visual/físico em Samsung nesta etapa**. Assinatura QA/debug pode divergir do APK instalado; exportar configurações antes de desinstalar e não sugerir update in-place garantido.

RESULTADO:
- Primeira implementação local de Arena com APK de QA disponível, mantendo exclusivamente na feature branch até aceite visual; OCR/energia/HUD/launcher base não modificados pela feature.

STATUS:
**CONCLUÍDO** para código inicial, testes, build, integridade, assinatura v2 e disponibilização do artifact QA.  
**PARCIAL** para o módulo completo: falta backend, contas, ranking vivo, competições operacionais, histórico vitalício, premiações virtuais persistidas, prêmios reais e QA Samsung.

PENDÊNCIAS:
- Instalar e testar APK no Samsung: botão Arena no topo, expansão, apelido após reinício, medalhas, velocidade de rolagem, histórico e funções anteriores; preservar exportação de configurações/HUD.
- Não conceder pontos competitivos por OCR/histórico autoestimado; histórico de batalhas atual limitado a 50 registros.
- Futuro serviço autoritativo e aprovação contra fraudes antes de posicionamento em ranking e resgate de prêmio.

PRÓXIMO PASSO:
- Receber captura e relato de QA do usuário, corrigir defeitos em branch, repetir CI, finalizar PR#35 somente após validação.
- Evoluir infraestrutura de torneios/ranking e recompensas em etapas rastreáveis no Guia Mestre e checklist, sem substituir matemáticas de batalha nem modificar controles nativos.



### 2026-10-08/09 — Proposta Sua Equipe: editor manual pré-batalha 0.5.31

Versão: `0.5.31`, versionCode `36` (apenas branch de feature isolada)  
Base: `feature/arena-foundation-20261008` (0.5.30, HEAD de criação `5ce6f0398d36455294b1300e142aaad3a4f6528e`)  
Branch de implementação: `feature/team-builder-0.5.31-20261008`  
Branch de CI isolado: `LucianoToscano/PvPPokeGo-APK-Build:preview/team-builder-0.5.31-20261008`  
Referências visuais: duas capturas/mockups fornecidos pelo usuário nesta conversa, com página inicial, logo/overlay/coplay, cartão SUA EQUIPE, Altaria/Houndoom/Rillaboom, PC/IVs/tipos/golpes/sexo/Mega/Sombroso/liga. Não presumir nomes/golpes/IV/rank desses exemplos como dados reais do usuário.

OBJETIVO:
- Incluir na página inicial cadastro manual de três Pokémon e seleção de liga sem perder Arena 0.5.30 nem regressar motor, OCR, reserva, launcher, HUD.
- Não introduzir falsos ranks, medição HP não verificada ou dados inventados, nem interferir no jogo nativo.

ALTERAÇÕES:
- `TeamBuilderPanel.kt`: componente Compose compacto recolhível no início da tela, três vagas, busca de espécie/forma no Game Master offline, PC, IVs, HP máximo, sexo, Mega, Sombroso, e seleção de golpes legais da forma quando a espécie está presente no banco. Campos opcionais vazios em vez de adivinhar.
- `ManualTeamPokemon`, `ManualTeamRoster`, `ManualTeamPolicy`: modelo e regras puras sanitizadas para 3 vagas, IVs, PC, HP, gênero e teto da liga.
- `TeamSetupRepository.kt`: salvar/carregar equipe no mesmo arquivo persistente das configurações, chave `manual_team_v1`.
- `SettingsRepository.kt`: autorizar a nova chave de equipe na exportação/importação JSON existente, sem migrar/resetar posições do HUD.
- `SettingsScreen.kt`: painel antes da Arena, seletor usando a MESMA `leagueMode` de configurações e menu rápido.
- `app/build.gradle.kts`: 0.5.31 / code 36 **somente nesta feature**.
- `ManualTeamPolicyTest.kt`: testes de time incompleto, 3 vagas, IV/PC inválidos, limite da liga e opcionais.
- `docs/TEAM_BUILDER_0.5.31.md` e checklist atualizados.

INTEGRIDADE/ESCOPO:
- Entrada manual é **declaração do jogador**, não OCR confirmado. Nenhum dado manual foi injetado no matcher e engine em combate. Integração da sugestão precisa de política por confiança e replay para evitar a regressão de aliados relatada na 0.5.29.
- Sem alterar `BattleEngine`, `BattleOverlayService`, `BattleOverlayView`, assets do launcher ou lógica da Arena.
- RANK e porcentagem de HP não são atribuídos por esses campos; são requisitos futuros.
- UI visual dos sprites dos três Pokémon do mockup ainda não implementada. Priorizar funcionamento e dados consistentes, depois reproduzir mockup.

TESTES:
- Fonte/modelo, editor e testes JUnit adicionados. Execução Standard/CI aguardando resultado real; sem promessa de APK nesta data do registro.
- Pendente validação visual no Samsung, teclado/scroll, reentrada do app e backup/restauração.

STATUS: **PARCIAL** — código de editor e persistência, sem QA físico e sem integrar OCR.

PRÓXIMO PASSO:
- Conferir pipeline `0.5.31`, corrigir falhas de compilação/testes, só depois entregar APK de QA assinado e registrar SHA-256/Drive. Validar no aparelho antes de merge para a release.
- Integrar manual com reconhecedor como preferência verificável, mantendo maior prioridade em evidência visual de espécie/forma correta.


### 2026-10-08/09 — Sua Equipe 0.5.31: testes direcionados aprovados, sprites originais reutilizados

Branch: `feature/team-builder-0.5.31-20261008`, baseada em `feature/arena-foundation-20261008` sem alterar a release 0.5.29.  
PR draft: https://github.com/LucianoToscano/PvPPokeGo/pull/36  
CI targeted: https://github.com/LucianoToscano/PvPPokeGo-APK-Build/actions/runs/37875124885

ALTERAÇÕES:
- Cartões dos três Pokémon mostram sprite exato do atlas local `PokemonIconAtlas` por espécie/forma quando carregado, em vez de imagens decorativas inventadas; fallback seguro por nome e PC.
- A leitura/decodificação do atlas para a UI é assíncrona; classe original no overlay foi reutilizada sem alterações.
- Sete arquivos de código, UI e testes comparados por hash Git (mesmo SHA de blob) entre branches privada e build público.

TESTES:
- `:app:testStandardDebugUnitTest --tests 'com.lucianotoscano.pvppokego.data.ManualTeamPolicyTest'` resultou **SUCCESS / BUILD SUCCESSFUL in 1m 4s**.
- Versão completa do APK e teste Samsung ainda aguardando; não afirmar entrega final com base neste teste direcionado.

STATUS: **PARCIAL** — código/testes direcionados concluídos, build completo e QA visual pendentes.


### 2026-10-09 — Entrega técnica APK Standard QA 0.5.31 "Sua Equipe" concluída

Versão: **0.5.31**, versionCode **36**  
Branch de código: `feature/team-builder-0.5.31-20261008` (feature baseada na Arena 0.5.30, NÃO merged na release/0.5.29).  
PR draft: https://github.com/LucianoToscano/PvPPokeGo/pull/36  
Cópia pública build-only isolada: `LucianoToscano/PvPPokeGo-APK-Build:preview/team-builder-0.5.31-20261008`.  
Commit SHA compilado da cópia pública: `fd17818758bb35f54c31a9cd379a42c1af373594`.

OBJETIVO:
- Entregar o cadastro manual de três Pokémon na página inicial com seleção da liga, busca de espécie/forma, PC, IV, HP, sexo, flags e golpes, sem regressar OCR, HUD ou Arena.

ALTERAÇÕES/ARQUIVOS:
- `data/TeamSetupModels.kt`: DTO de 3 vagas + validação/sanitização.
- `data/TeamSetupRepository.kt`: persistência própria dentro de `pvppokego_prefs`.
- `ui/TeamBuilderPanel.kt`: editor Compose recolhível, três cards com sprite fiel via atlas existente carregado assíncrono, busca no Game Master local, seleção de golpes e saves por slot.
- `ui/SettingsScreen.kt`: painel antes da Arena/Batalha e seleção compartilhada `SettingsRepository.leagueMode`.
- `data/SettingsRepository.kt`: backup JSON inclui `manual_team_v1`.
- `app/build.gradle.kts`: `versionName 0.5.31`, `versionCode 36`.
- `app/src/test/java/com/lucianotoscano/pvppokego/data/ManualTeamPolicyTest.kt`.
- `docs/TEAM_BUILDER_0.5.31.md`, `docs/CHECKLIST-PvPPokeGo-0.5.29.md`, `docs/PROJECT_CONTINUITY_GUIDE.md`.

TESTES:
- Teste Kotlin `ManualTeamPolicyTest`, run https://github.com/LucianoToscano/PvPPokeGo-APK-Build/actions/runs/37875124885 — **SUCCESS**.
- Testes Standard completos `:app:testStandardDebugUnitTest` e compilação `:app:assembleStandardDebug`, run https://github.com/LucianoToscano/PvPPokeGo-APK-Build/actions/runs/37875124890 — **SUCCESS**, `BUILD SUCCESSFUL in 1m 17s`.
- Snapshot privado/público dos sete arquivos de código comparados por Git blob SHA, coincidindo exatamente.
- APK QA: `PvPPokeGo-0.5.31-QA-debug.apk`, tamanho **79.430.738 bytes**, SHA-256 **`07d769bdcbbe4a6400ca21fe38f1276ddbb946b3a682c46cb85648d9400f52a0`**, reconfirmado após download e comparado ao `SHA256SUMS.txt`.
- ZIP artefato ID **11592506273**, tamanho **35.822.814 bytes**. `unzip -t` ZIP/APK PASS; assinatura APK Signature Scheme **v2 true**, 1 signatário QA/debug; **não** é certificado permanente.
- Assets `gamemaster.json`, `move_names_ptbr.json`, `pokemon_icon_atlas.json/png`, `pvpoke_snapshot.json`, launcher original transparente confirmados presentes.
- ZIP no Google Drive oficial, upload e metadados verificados:
  https://drive.google.com/file/d/1BQP8Zffi9IlwzBHWbN5S0koxbCOtJwvY/view?usp=drivesdk

RESULTADO:
- Editor manual de três Pokémon com dados reais do Game Master, sincronização com liga já existente, persistência/backup e cartões com sprites do atlas; compile/testes/build/arquivo APK QA reais.

STATUS:
**CONCLUÍDO** — código inicial, testes Kotlin, build Standard e verificações técnicas do APK/Drive.  
**PARCIAL** — aceite de aparência no Samsung, integração segura das dicas manuais ao reconhecimento em batalha, verificações de elegibilidade por copa, ranking PvPoke real e acabamento visual igual ao mockup.

PENDÊNCIAS E GARANTIAS:
- Cadastro manual é **dica do jogador**, nunca substitui sozinho OCR/identidade/CP confiável do detector existente. Não alterar `BattleEngine`, correspondência do inimigo ou posicionamento HUD sem regressões e fixtures.
- Instalar no Samsung, testar busca por nomes/formas, três vagas, ícones do atlas, ligas, restauração do backup, edição e sensores, além de telas de batalha, troca e launcher.
- O certificado QA/debug pode ser diferente do APK já instalado; exportar backup antes de desinstalar se Android rejeitar a atualização.
- Não declarar esta prévia "aprovada no celular" sem prova visual e feedback.
- Não mergear PR #36 na branch da Arena/release até validação no dispositivo.

PRÓXIMO PASSO:
- Receber capturas reais do layout e relato do comportamento dos três slots em 0.5.31.
- Corrigir eventuais falhas ou diferenças visuais, depois desenvolver uso dos dados manuais como hints sujeitos a confirmação por OCR/matcher e incorporar pontuação PvPoke somente com dados validados.


### 2026-10-09 — SUA EQUIPE: modos Automático/Manual integrados em código; SEM APK/SEM BUILD

VERSÃO DE DESENVOLVIMENTO: **0.5.32**, `versionCode 37`, **não compilada** nesta etapa.  
GUIA BASE LIDO: `release/0.5.29` `docs/PROJECT_CONTINUITY_GUIDE.md` e histórico `release/0.5.26`.  
BRANCH ORIGEM: `feature/team-builder-0.5.31-20261008` no HEAD `79d3372c77a3d1cff31c21847e648ea50b03725e`.  
BRANCH ATUAL: `feature/team-recognition-modes-0.5.32` (preservar sem merge automático).  
REFERÊNCIA DE GUIA/REGRAS: `docs/TEAM_RECOGNITION_MODES_0.5.32.md`.  
REFERÊNCIAS VISUAIS: mockups enviados pelo usuário da homepage e 3 cartões Sua Equipe, mantendo HUD V03/0.5.17 e ícones do jogo.

OBJETIVO DO PEDIDO:
- Dar escolha entre **Automático** (o detector tenta reconhecer/aplicar os membros reais do time) e **Manual** (jogador cadastra/seleciona seus três Pokémon e eles passam imediatamente a ser a equipe atual declarada para o motor).
- Em Manual, a câmera segue identificando qual dos três está ativo e quais são reservas; cadastro não deve falsificar evidência OCR.
- Preservar tudo de Arena 0.5.30, cadastro 0.5.31 e rotina OCR/contadores/HUD anteriores; **não compilar nem gerar APK agora**.

ALTERAÇÕES EFETIVADAS EM ARQUIVOS:
- `app/src/main/java/com/lucianotoscano/pvppokego/data/TeamSetupRepository.kt`: `TeamRecognitionMode.AUTOMATIC/MANUAL`, preferência `team_recognition_mode_v1`, seleção apenas com time completo e CP da liga.
- `app/src/main/java/com/lucianotoscano/pvppokego/data/SettingsRepository.kt`: incluir nova preferência no backup JSON sem perder configurações e posições anteriores.
- `app/src/main/java/com/lucianotoscano/pvppokego/ui/TeamBuilderPanel.kt`: botões de modo, rótulo de equipe atual declarada e explicação quando o reconhecimento automático global está OFF. Três vagas e opções do editor 0.5.31 preservadas.
- `app/src/main/java/com/lucianotoscano/pvppokego/ui/SettingsScreen.kt`: informar estado do reconhecimento geral ao novo card, mantendo seletor de liga único.
- `app/src/main/java/com/lucianotoscano/pvppokego/data/ManualTeamMatchPolicy.kt`: matcher puro por imagem de espécie confiável/CP/OCR, rejeitando contradição de identidade e ambiguidades.
- `app/src/main/java/com/lucianotoscano/pvppokego/engine/BattleEngine.kt`: equipe manual no próprio motor; não sobrescrever com scan de roster; pré-carregar três membros sem presumir ativo; preservar identidade conservadora no tracker de continuidade; impedir reutilização automática de HP/energia do ativo anterior ao trocar equipe.
- `app/src/main/java/com/lucianotoscano/pvppokego/overlay/BattleOverlayService.kt`: sincronizar o modo/time do painel ao motor vivo e reaplicar seleção após reset/término de batalha, mantendo OCR de adversário e análise de combate separados.
- `app/src/test/java/com/lucianotoscano/pvppokego/data/ManualTeamMatchPolicyTest.kt`: casos de correspondência por CP, visual, contradições, espécie ausente, duplicidade e nova composição; arquivo existe, **ainda não foi executado**.
- `app/build.gradle.kts`: versão de desenvolvimento `0.5.32` (code37), NÃO implica APK entregue.
- `docs/TEAM_RECOGNITION_MODES_0.5.32.md` e `docs/CHECKLIST-PvPPokeGo-0.5.29.md`: escopo, riscos, etapas e QA pendente.
- `docs/PROJECT_CONTINUITY_GUIDE.md`: este novo registro cronológico, preservando todos os anteriores.

REGRAS E CASOS IMPORTANTES:
- Automático segue sendo padrão e usa a lógica já existente de time, trocas e reservas; sem confiança suficiente, resultado é incerto, não inventado.
- Manual completo e compatível com a liga significa `equipe atual declarada`, não `detectada visualmente/confirmada`. Ao voltar para Automático, manter cadastro salvo para o futuro mas retirar sobreposição manual do motor.
- A ordem dos slots não é necessariamente a ordem das reservas no jogo; CP e imagem confirmam o slot de cada carta.
- Conflito imagem confiável x CP não deve piná-los ao Pokémon errado. Evitar copiar HP, energia ou histórico da espécie anterior.
- O toggle de equipe Manual é distinto do toggle global de reconhecimento de imagem: se global estiver OFF, time declarado pode estar pré-carregado mas o ativo não pode ser afirmado visualmente.
- Sem perda/alteração de launcher, olho, cadeado, tamanho/posição de elementos HUD, Arena ou reconhecimento inimigo.
- Mostrar o time detectado AUTOMATICAMENTE na homepage continua como tarefa de UI pendente; não armazenar palpite de OCR como cadastro manual.

TESTES E BUILD:
- **NÃO foram rodados testes JUnit, Gradle, Android, compilação Kotlin, APK, assinatura, instalação nem QA Samsung relativos a 0.5.32**, seguindo ordem expressa do usuário. Não marcar sucesso, não usar APK de versões anteriores renomeado.
- Branch `PvPPokeGo-APK-Build:preview/team-recognition-0.5.32-20261009` havia sido criada parcialmente durante preparação interrompida; **não é snapshot validado nem fonte de APK**. Não iniciar pipeline.
- 0.5.31 continua sendo a última versão com APK QA efetivamente compilado e checksum registrado, sem afirmar homologação Samsung.

STATUS: **PARCIAL: código em branch + guia atualizado; testes/QA/compilação conscientemente adiados.**

PRÓXIMO PASSO:
- Continuar planejando/aplicando alterações em branch com documentação cronológica, sem compilar até pedido explícito.
- Ao autorizar testes: executar `ManualTeamMatchPolicyTest`, casos de continuidade/mesma espécie/PC, suíte Android, inspeção de UI com time real, backup import/export e validade de forma; depois avaliar novo APK e somente então considerar merge.
- Sempre reler este guia, checklist e último HEAD real antes de mexer em código, preservar histórico em releases anteriores.


### 2026-10-09 — Referência Configurações do Treinador: perfil, PvP IV, nomes, datas e simulador (sem build)

ORIGEM:
- Novo print fornecido pelo usuário: tela de "Configurações" contendo **Gerais, PvP IV, Gerador de Nomes, Formato da Data Pego, Simulador de Batalha**, campo **Nível do Treinador** e seleção **Equipe**.
- Captura preservada para consultas futuras no Google Drive oficial: https://drive.google.com/file/d/14EgQT-TBmqzOC0vdIThI9nuzcTa1_P0L/view?usp=drivesdk
- Os valores `54` e `Mystic` no print **não são necessariamente informações do usuário**; não copiá-los como padrão para o perfil.
- Consulta prévia do guia canônico `release/0.5.29` e referência histórica `release/0.5.26`. Contrato principal: preservar tudo, implementar em branch, registrar, **não compilar/não gerar APK até nova ordem**.

BRANCH/ESTADO DE PARTIDA:
- Repositório: `LucianoToscano/PvPPokeGo`.
- Feature corrente: `feature/team-recognition-modes-0.5.32`, continuando o modo Manual/Automático de Sua Equipe (HEAD anteriormente registrado `54d34253d711523cccd1a9d0f2a592152fc9605e`; HEAD deve ser consultado novamente no próximo chat).
- A `release/0.5.29` e as branches de Arena `0.5.30` e equipe `0.5.31` **não foram mescladas nem substituídas**.
- A base visual HUD V03/0.5.17, ícones do atlas, OCR, matcher, energia e posições permanecem intocados por este adendo.

MUDANÇAS EM CÓDIGO:
1. `app/src/main/java/com/lucianotoscano/pvppokego/data/TrainerTools.kt`: entidades `TrainerFaction`, `CatchDateDisplay`, `PokemonNameStyle`, política pura de validação e geração de nomes, IVs registrados e formato de data.
2. `app/src/main/java/com/lucianotoscano/pvppokego/data/TrainerPreferencesRepository.kt`: guarda nível opcional do treinador, facção (Mystic/Valor/Instinct ou não informada), estilo de nomes e formato de data no mesmo arquivo local de configurações; nenhum login/conta oficial.
3. `app/src/main/java/com/lucianotoscano/pvppokego/ui/TrainerSettingsPanel.kt`: novo card recolhível no app com cinco opções:
   - **Gerais**: nível do treinador editável e equipe pessoal (não sincronizados com Pokémon GO);
   - **PvP IV**: mostra apenas IVs que o jogador já cadastrou nos três Pokémon em Sua Equipe; **não reivindica rank PvP calculado**;
   - **Gerador de Nomes**: configura estilo, gera prévia com dados conhecidos e botão que copia para clipboard do Android, sem modificar nomes dentro do jogo;
   - **Formato da Data Pego**: `dd/MM/yyyy`, `yyyy-MM-dd`, `dd/MM/yy` e prévia ilustrativa de hoje; **não usa data real de captura ainda**;
   - **Simulador de Batalha**: primeira funcionalidade é comparador OFFLINE de vantagens de tipos entre um dos três aliados cadastrados e Pokémon adversário selecionado via `gamemaster.json`, utilizando `TypeChart` existente. Rotulado como **prévia de tipos e NÃO simulação de combate por turnos ou resultado**.
4. `app/src/main/java/com/lucianotoscano/pvppokego/ui/SettingsScreen.kt`: integra o novo card após Sua Equipe/Arena sem duplicar controles do HUD e sem alterar os modos de reconhecimento.
5. `app/src/main/java/com/lucianotoscano/pvppokego/data/SettingsRepository.kt`: incluir `trainer_profile_level_v1`, `trainer_profile_faction_v1`, `trainer_pokemon_name_style_v1`, `trainer_catch_date_display_v1` no allowlist de import/export JSON, mantendo posições e outros dados.
6. `app/src/test/java/com/lucianotoscano/pvppokego/data/TrainerToolsTest.kt`: testes Kotlin escritos para valores opcionais, formato, nomes/IVs, sem execução.
7. `docs/TRAINER_SETTINGS_0.5.32.md` e `docs/CHECKLIST-PvPPokeGo-0.5.29.md` com implementação, QA pendente e roadmap.

LIMITES:
- Ranking PvP IV por combinação exata de espécie/nível/forma/IV/PC/limite de liga é trabalho futuro; não confundir soma de IVs com classificação.
- Simulador real deve modelar fast/charged moves, turnos, escudos, estados, HP e energia, com motor auditado/testes; a comparação de tipos atual é **somente apoio exploratório**, não diz quem ganha.
- Não importar dados do Pokémon GO nem inferir nível/facção/data de captura a partir de imagem de outro app, a menos que fonte confiável e consentimento.
- Nenhuma alteração deve preencher identificação OCR, histórico de vitórias, ranking Arena ou dados de adversário.
- Manter UX clean: opções recolhidas na tela inicial, e eventual futura aba Configurações exige validação de navegação e nenhuma perda de funções.

TESTES E ENTREGA:
- **Nenhum teste Kotlin, Android, CI, Gradle, build, APK, assinatura, instalação ou QA Samsung executado para este adendo.** Os testes acima são código criado e documentado, não passaram nem falharam.
- APK QA mais recente realmente entregue permanece `0.5.31`; `0.5.32` é somente código de desenvolvimento.
- Não disparar workflows, não copiar/renomear APK antigo e não criar nova release até autorização explícita.

STATUS:
**IMPLEMENTAÇÃO DE INTERFACE + PERSISTÊNCIA/PRÉVIA INICIAL REGISTRADAS NO GITHUB E DOCUMENTAÇÃO. TESTES/BUILD PENDENTES A PEDIDO DO USUÁRIO.**

PRÓXIMOS PASSOS:
- Continuar acumulando mudanças e registrando no Guia Mestre.
- Quando autorizado: rodar testes `TrainerToolsTest`, modo de equipe, backup, Compose/Android e QA real em dispositivo, depois considerar build.
- Em outra etapa criar classificador real de PvP IV e simulador por turnos, sem regressões nos mecanismos existentes.

### 2026-10-09 — Nova leva de referências PvPoke: rankings, CMP, simulação, timing, matrizes e CSV (SEM BUILD)

ORIGEM:
- Usuário forneceu uma leva com 9 arquivos de imagem, um CSV e links explícitos do PvPoke. Mantida intenção anterior: **seguir acumulando mudanças e registrando; NÃO compilar nem gerar APK agora**.
- Guias `release/0.5.29` e histórico `release/0.5.26` consultados; `release/0.5.29` HEAD confirmado `952ff14e5f97fb461619946473a42c144cdc2caa`.
- Branch de desenvolvimento confirmada antes da alteração: `feature/team-recognition-modes-0.5.32`, HEAD anterior `75f380fc8ac801350bcbf9be65e95e35cefce3ea`. Não houve merge, build, alteração em APK, HUD ou código Kotlin nesta leva.

NOVO DOCUMENTO COMPLETO:
- `docs/PVP_ANALYTICS_REFERENCES_2026-10-09.md`
- Nove capturas, URLs individuais do Drive e matriz CSV original estão preservados e indexados nele; **não depender só da disponibilidade futura das imagens deste chat**.
- Arquivo CSV original no Drive: https://drive.google.com/file/d/1sYxNDXV0RszjN922ALwIARmmxu8dtDnf/view?usp=drivesdk
- CSV verificado: 370 bytes, SHA-256 `bfc98fe13870a4b795466454a1bdfd114143c6f21ed10ff49d3206e09fdf3dc1`; 8 colunas, 4 linhas incluindo cabeçalho, grade 3×3 e W/L/D, `Average`; **valores assinados são diferenças de matriz, NÃO battle rating (0–1000), rank IV ou Elo de jogador**.
- Links externos úteis: https://pvpoke.com/rankings/ · https://pvpoke.com/battle/ · https://pvpoke.com/team-builder/all/1500/ · https://pvpoke.com/attack-cmp-chart/all/1500/froslass/ · https://pvpoke.com/battle/1500/moltres_galarian/glalie/22/0-3-2/3-1-4/ · https://github.com/pvpoke/pvpoke
- Comparar fonte/versionamento, respeitar licenças e direitos de marca/arte; tomar como referência funcional, não copiar interface ou afirmar resultados atuais dos screenshots.

CONCEITOS E REQUISITOS CAPTURADOS:
1. **Rankings PvP** por liga/papel (Overall, Leads, Closers, Switches, Chargers, Attackers, Consistency), notas como resultados de *simulações versionadas*; distintos de ranking público da Arena e rank IV.
2. **CMP** por estatística real de Ataque e faixas de nível/IV/CP da liga, respeitando empate, timing de golpes ágeis e casos de “falso CMP”.
3. **Simulador 1×1** com formas/IV/nível/golpes, HP/energia, escudos 0/1/2, buffs/debuffs, baiting, troca, timeline e cenários, com explicação de hipóteses e sem prometer previsão garantida.
4. **Matrix Team Builder** para grupos offline, battle ratings 0–1000, W/L/D, diferenças, médias, cobertura, import/export CSV e cenários de 0/1/2 escudos, sem alterar a lista de três aliados *atuais* cadastrada no motor.
5. **Optimal move timing** por turnos ágeis, **breakpoints/bulkpoints** por dano inteiro, **CMP chart**, condições de vitória e tradeoffs de energia.
6. Continuar utilizando base `GameDataRepository` (GM e CP multipliers), `Models`, `TypeChart`, `BattleEngine` e módulos de equipe; motor de simulação deve ter estado puro/isolado do serviço OCR para preservar as partidas reais.
7. Primeira etapa segura: inventariar modelos/snapshots/licenças e produzir algoritmo reproduzível e testes/fixtures; a UI clean e integração ao HUD vêm depois de validação.

ARQUIVOS ALTERADOS NESTA LEVA:
- `docs/PVP_ANALYTICS_REFERENCES_2026-10-09.md` (novo, referências + mapa técnico).
- `docs/CHECKLIST-PvPPokeGo-0.5.29.md` (etapas, status e próximos testes).
- `docs/PROJECT_CONTINUITY_GUIDE.md` (registro atual).
- Espelho de continuidade do Google Drive oficial atualizado com este checkpoint; arquivos originais arquivados na pasta.

STATUS / TESTES / PENDÊNCIAS:
- **Somente documentação/arquivo de referências** nesta leva. **NÃO foram implementados** ranking simulado, CMP, matriz CSV ou simulador completo nesta etapa.
- **NÃO executar testes/Gradle, disparar workflows, gerar APK, assinatura ou release** até solicitação expressa do usuário. A `0.5.31` permanece a última versão QA realmente compilada; `0.5.32` segue desenvolvimento em branch isolada.
- Em próximos turnos, iniciar pela leitura do guia e checklist, HEAD real, inventário de snapshot e critérios de validade dos cálculos; desenvolver em etapas sem perda de OCR/HUD e documentar resultados reais.

### 2026-10-09 — Análise consolidada das imagens PvPoke/Calcy/Team Builder/Train e DOIS CSV; documentação, SEM CÓDIGO/SEM APK

PEDIDO: após diversas levas de referências visuais e URLs, o usuário pediu "Ok pode analisar". A instrução anterior de **não compilar nem gerar APK até novo pedido** permanece válida. Esta rodada analisa e documenta; não muda funcionalidade.

ESTADO CONFIRMADO ANTES DAS ALTERAÇÕES:
- Guia canônico lido em `release/0.5.29`, histórico em `release/0.5.26`.
- Branch autoritativa em desenvolvimento `feature/team-recognition-modes-0.5.32`, HEAD inicial `7480d7b340784b48416ac7d72f10ba1290da38e2`. `release/0.5.29` em `952ff14e5f97fb461619946473a42c144cdc2caa`, intacta.
- Reutilizar o que já existe em `GameDataRepository` (Game Master, CP multipliers, rankings), `Models`, `TypeChart`, `BattleEngine`, `TeamBuilderPanel`, `TrainerSettingsPanel`; evitar duplicatas e não afirmar simulador completo já implementado.

DOCUMENTO INTEGRAL: `docs/ANALISE_CONSOLIDADA_PVP_2026-10-09.md`.
DOIS CSV REAIS INSPECIONADOS (bytes originais, sem conversão):
1. `Team vs. All Pokemon.csv` → 51.369 bytes, **1.152 adversários**, 6 colunas, 0 linhas inválidas, 0 rótulos repetidos; Altaria/Houndoom/Rillaboom, Threat Score, Overall Rating. Hash SHA-256 `d704a831d54ccad2fb4818c1767131fc17adedf29ef1bb8e2468fc3cf066b1a1`. Original preservado no Drive: https://drive.google.com/file/d/1bUjTtGHH36wyojFrLqlG7JI4kOgC40K7/view?usp=drivesdk
2. `_vs .csv` → 370 bytes, 3×3, 8 colunas, W/L/D e diferenças médias verificadas; hash SHA-256 `bfc98fe13870a4b795466454a1bdfd114143c6f21ed10ff49d3206e09fdf3dc1`. Original também preservado no Drive: https://drive.google.com/file/d/1W1XYj4gOAOjG9-Gz1GsA_fgk-Lo9SMpd/view?usp=drivesdk
- Scores do Team Builder/Threat Score e `Overall Rating` NÃO são Elo de jogador, PvP IV Rank nem porcentagem de vitória. Differences do outro CSV também NÃO são Battle Rating. Importação futura deve armazenar proveniência, versão e cenários (liga, golpes, shields, IV), ausentes nos arquivos.

CONCLUSÕES:
- Pilar 1 Assistente de Batalha: preservar OCR/overlay vivo, CP/HP/energia/escudos, reclassificação conservadora, recomendações apenas com confiança e sem toque automático no jogo.
- Pilar 2 Laboratório PvP: botão `Analisar equipe` sobre CÓPIA do time atual, cobertura/bulk/safety/consistency, scorecard 3×N, threats, substitutes, counters, simulação por turnos offline, CMP/timing/breakpoints/bulkpoints, rankings/pesquisa, estatísticas distintas corretamente rotuladas.
- Pilar 3 Academia PvP: batalha SIMULADA 3×3 contra IA, escolhas próprias, dificuldades, escudos, troca, HP/energia, replay, erros e acertos, gráficos Damage/Shields/Energy, evolução de treino SEPARADA de ranking Arena/campeonato.
- *Top Performers* PvPoke Train usa amostra de jogadores e bots; estatística externa não equivale a partida GO verificada. Leaderboard competitivo/prêmios exigem antifraude e consentimento.
- *Tera Raid Calculator* https://pvpoke.com/tera/altaria/dragon/dragon-flying corresponde a Scarlet/Violet e **NÃO deve entrar como mecânica PvP do Pokémon GO**. Somente ideia de seleção de counters pode inspirar futuro módulo separado Raides/PvE do GO.
- Licença MIT do código PvPoke não deve ser confundida com direitos sobre marcas/arte/sprites de terceiros. Visual do PvPPokeGo deve permanecer próprio, clean, sem clonagem.

PRIORIZAÇÃO / NÃO EXECUTADO:
- P0 integridade da branch 0.5.32 e QA antes de build (apenas quando autorizado).
- P1 versionamento de dados, parser seguro dos dois formatos CSV, relatórios de equipes e motor de cálculo puro validado.
- P2 simulador 1×1/matriz/CMP/Academia offline depois dos testes de matemática, sem interferência no OCR.
- P3 Arena pública, premiação/campeonatos, raides GO separadas, se autorizados e tecnicamente sustentáveis.
- **Nenhum código de produção alterado neste turno; nenhum teste, Gradle, Android, CI, assinatura ou APK executado**.
- Arquivos desta rodada: NOVO `docs/ANALISE_CONSOLIDADA_PVP_2026-10-09.md`; ATUALIZADO `docs/CHECKLIST-PvPPokeGo-0.5.29.md`; ATUALIZADO este Guia Mestre; Drive recebeu CSV originais, e registro de continuidade deve conter link para esta análise.

PRÓXIMO CHAT: abrir esta análise e o último registro do Guia, verificar HEAD real, preservar todas as versões e NÃO iniciar build/apk espontaneamente.

### 2026-10-09 — Novo diagnóstico REAL da 0.5.31 (3 anexos JSON/ZIP), sem build e sem copiar dados brutos

Pedido contextual: usuário anexou `PvPPokeGo-0.5.31-config.json`, `PvPPokeGo-0.5.31-diagnostico.zip` e `PvPPokeGo-0.5.31-historico.zip` após análise do projeto. Feita inspeção estática sem restaurar, ativar, compilar ou gerar APK.

**RELATÓRIO DE ORIGEM E PROVAS:** `docs/DIAGNOSTICO_EXPORTS_0.5.31_2026-10-09.md` (hashes, formato, indicadores, QA pendente). Os arquivos de entrada contêm dados de jogo, aparelho e ajustes de HUD: NÃO foram publicados em bruto no repositório/Drive, somente métricas para depuração. Preserve as posições de HUD e não regrave preferências.

EVIDÊNCIAS REALMENTE CONFERIDAS:
- Ambos os ZIPs contêm `battle_history.json`/`historico.json` idênticos byte a byte, SHA256 `f395abee65fc32cb508c6279a1923a3ffca57e7ba0cc92f9abaa6bb717682242`. ZIP de histórico contém também versão legível e CSV.
- **7 sessões, 702 eventos**; CSV exatamente 702 registros e 7 IDs. **1 resultado Vitória, 6 sem resultado** (`null`). Não usar 1/7 como taxa de vitórias, nem contabilizar `null` como derrota.
- Uma vitória de somente **3,7s** não contém identificação de Pokémon nem OCR; duas sessões adicionais sem identidades e sem OCR. Isso sugere investigar a segmentação/associação de resultado, não prova bug único.
- Três encerramentos por 28s sem evidência; dois por seleção de time; um por novo OCR; um por resultado. Há sessões de ~843s e ~507s com mais espécies atribuídas a cada lado do que o limite de três da batalha; podem incluir OCR errado, retenção temporal ou fragmentos indevidamente agrupados.
- **34 trocas** têm fonte OCR e rótulo `CONFIRMADO`; porém a consistência de identidades em múltiplos minutos não está verificada por vídeo. Em **4 sessões** os eventos não estão ordenados crescentemente por `elapsedMs`.
- `auto_recognition=false` está no `settings.json` do diagnóstico (35 chaves), mas ausente no backup separado (34 chaves). Isso **não demonstra** a configuração efetiva durante cada partida. O novo `team_recognition_mode_v1` da feature 0.5.32 naturalmente não aparece no backup 0.5.31.
- A equipe salva `manual_team_v1` difere de parte dos nomes vistos no histórico; o modo de equipe não estava implementado em 0.5.31. Não imputar definitivamente um erro ou afirmar que 0.5.32 já corrigiu antes de QA.

AÇÕES PENDENTES:
1. **P0** QA e testes de segmentação início/fim, telas de resultado, seleção e reinício, rosters 3×3 e equipe divergente.
2. **P0** integração do modo manual/automático com OCR/slots e preferência global, confirmando regra de não sobrescrever equipe declarada e não inventar ativo.
3. **P1** distinguir `OCR CONFIRMADO` verdadeiro de evento candidato/estimado; preservar fonte e relógio; ordenar exportação e verificar motivos de reservas `DESCONHECIDO`.
4. Validar em Samsung com captura conhecida e cenário rotulado; só então alterar/compilar, se o usuário autorizar.
5. Manter `release/0.5.29` e último APK 0.5.31 sem alterações. Nesta etapa: **DOCUMENTAÇÃO SOMENTE — ZERO BUILD/TESTES KOTLIN/APK**.

Documentos alterados: `docs/DIAGNOSTICO_EXPORTS_0.5.31_2026-10-09.md` (novo), checklist e este Guia Mestre. Estado da branch de feature deve ser verificado ao continuar.

### 2026-10-09 — Proposta nova "Caixa-Preta PvPPokeGo" / Logcat / relatório antibug (SEM BUILD)

ORIGEM: após o diagnóstico dos três anexos da v0.5.31, usuário perguntou se havia mais registros ou "cat Android" que pudessem ser incorporados ao app para exportar e trazer para análise. **Resposta: priorizar telemetria interna estruturada, e usar Logcat/Bugreport externos quando precisar investigar falhas de sistema**.

ANÁLISE DIRETA DO CÓDIGO NA FEATURE 0.5.32:
- `MainActivity.kt` linhas 57–96 contém exportador básico ZIP existente (`settings.json`, `battle_history.json`, `device.txt`, `README.txt`). ZIP de histórico é separado (linhas 98–135) com JSON/TXT/CSV.
- `BattleHistoryRepository.kt` já dispõe de `recordDiagnostic`; `SettingsScreen.kt` já possui botões; `BattleOverlayService.kt` já emite eventos de diagnóstico e captura. Preservá-los, acrescentar somente trilha de *por que* uma decisão foi tomada (candidatos OCR, thresholds, matcher, confiança, rejeições, tempos).
- Um APK comum Android 16 **não pode ler todo Logcat do SO ou de terceiros** por permissão `READ_LOGS`; não prometer isso nem solicitar root. Relatório oficial externo: Opções do desenvolvedor → Criar relatório de bugs (sujeito a dados sensíveis); ou computador/ADB. Fontes oficiais listadas no documento novo.

ESPECIFICAÇÃO:
- `docs/PROPOSTA_CAIXA_PRETA_DIAGNOSTICO_0.5.32.md` detalha UX recolhível, `Marcar problema agora`, export ZIP compatível com manifest + versionamento + eventos de timestamp monotônico, traces do OCR/espécie/forma/PC/confiança, rejeições, drift de captura/FPS, motor/estado, erros próprios, integridade da sessão e warnings 3×3.
- Coleta OFF por padrão, registro circular limitado e amostrado, armazenamento local, imagem apenas **opt-in manual**, limpeza de identificadores/PII; nenhum envio remoto automático, evitar excesso no HUD e travamento do overlay por escrita em disco.
- **Distinguir observação e correção:** alertas antibug não devem forçar troca de Pokémon nem fundir sessões sem evidência; devem manter estado válido/inconsistência visível, gerar diagnóstico e apoiar testes.
- Evitar duplicar exportador básico ou modificar settings import/backups; manter HUD/painel/ícones e posições sem mudança.
- Casos de aceite extraídos da v0.5.31: 7 sessões, 702 eventos, 1 vitória sem OCR, outros seis resultados ausentes, troca OCR aparentemente inconsistente, 4 sessões com ordem temporal não monotônica, divergência flag `auto_recognition` entre exportações.
- DOCUMENTAÇÃO apenas, para futura implementação quando user aprovar; **nenhum código de produção, build, teste, APK ou release foi alterado**.

Registro no checklist: `docs/CHECKLIST-PvPPokeGo-0.5.29.md`. Próximo chat deve começar pelo Guia Mestre e verificar HEAD real de `feature/team-recognition-modes-0.5.32`, sem retomar versão antiga por engano.

### 2026-10-09 — Implementação 0.5.33 autorizada: Logcat restrito ao app + OFF cinza + HP restante — APK PENDENTE por CI

O usuário explicitamente pediu **implementar Logcat na nova versão, corrigir ícones/controles brancos quando desligados (OFF cinza), adicionar novas funções de HP e enviar novo APK**. Esta ordem supera o bloqueio anterior contra compilar/APK (a geração agora está AUTORIZADA). Foi criada uma branch isolada, sem afetar release/0.5.29 nem o código da feature 0.5.32:
- Branch: `feature/diagnostics-hp-ui-0.5.33` derivada de `feature/team-recognition-modes-0.5.32`.
- Versionamento: `app/build.gradle.kts`: 0.5.33, versionCode 38.

Implementação realizada em fonte:
1. Novo `data/AppDiagnosticLog.kt`: trail privado rotativo de até 128 KiB e Logcat próprio processo/Tag `PvPPokeGo`, com timeout, sem permissão READ_LOGS, sem dados OCR crus.
2. `MainActivity.kt`: mantém exportador ZIP legado e acrescenta `runtime-events.log`, `logcat-app.txt` e `integrity-warnings.txt`; registros de sessões sem resultado/eventos regressivos **sem mutação do histórico**. O Logcat de outros apps/sistema NÃO é acessível por APK comum; usar bugreport do Android/ADB externamente se necessário.
3. `BattleOverlayService.kt` + `BattleHistoryRepository.kt`: breadcrumb de início/fim de sessão, stop/reset e diagnósticos do motor com rótulo genérico para evitar vazamento de identificação.
4. `QuickOverlayMenu.kt`: ON azul/ciano; OFF cinza escuro com texto e ícone também acinzentados, tanto em lista quanto modo somente ícones. Nenhuma mudança nas posições dos controles ou hitboxes.
5. `SettingsScreen.kt`: switches de configuração OFF cinza e ON ciano, com cartão leve/texto atenuado, além de opção de HP restante previsto e exportação informando Logcat próprio.
6. `SettingsRepository.kt`: chave booleana `show_hp_remaining_forecast`, padrão true, import/export preservados.
7. `BattleOverlayView.kt`: faixa de dano existente preservada; prévia de HP após golpe sem escudo (intervalo em %) com mensagem de escudo, KO possível/provável e fallback discreto perto do indicador quando ROI nativa não é localizada. Sem desenhar sob captura STALE/PAUSED.
8. Novo workflow `.github/workflows/pvppokego-0533-qa.yml` para testes `testStandardDebugUnitTest`, build `assembleStandardDebug`, checksum SHA256 e artefato `PvPPokeGo-0.5.33-APK`.

**BLOQUEADOR CRÍTICO DOCUMENTADO**: GitHub Actions runs https://github.com/LucianoToscano/PvPPokeGo/actions/runs/37901931447 e https://github.com/LucianoToscano/PvPPokeGo/actions/runs/37902276938 terminaram `failure` em segundos e com job sem steps disponíveis, **antes de iniciar compilador/Gradle**. Outros workflows do repo apresentaram falha igual. Não inventar erro de Kotlin, não atribuir definitivamente ao pagamento sem UI de erro. Execução local no ambiente disponível também não tem Android SDK, Gradle, jar Android e não tem acesso de rede a github.com; portanto não é possível compilar nem fornecer arquivo de APK verdadeiro agora. O antigo `PvPPokeGo-0.5.31-QA-debug.apk` **NÃO** contém estas mudanças e não deve ser rebatizado. NÃO afirmar que a compilação/QA passou.

Registro técnico: `docs/IMPLEMENTACAO_0.5.33_LOGCAT_HP_UI.md` e `docs/CHECKLIST-PvPPokeGo-0.5.29.md`.

Pendência:
- Conferir alerta específico de bloqueio na execução GitHub Actions, habilitar runner/quota/faturamento conforme o caso com ação do proprietário, rerun workflow, verificar testes, checksum e depois enviar APK.
- QA do Samsung, capturas/cores em light/dark, Logcat PID, previsões HP com ausência de ROI e integridade de estado; log privado, sem capturar dados de terceiros. Somente concluir quando existir build REAL.
- Preservar HUD, ícones oficiais, backup de posições; sem permissão para tornar repo público/espelhar código privado somente para evitar CI.

**STATUS: 0.5.33 alterada em código-fonte + documentação; NENHUM APK 0.5.33 VALIDADO/Gerado. A última versão de APK QA continua 0.5.31.**

## 12. Regra final

Sempre que o usuário pedir:
- “me manda o texto para novo chat”;
- “continue em outro chat”;
- “não quero perder o contexto”;

a resposta deve incluir obrigatoriamente o link para este arquivo e instruir o novo chat a lê-lo primeiro.

**Este documento deve crescer junto com o projeto. Não é descartável e não deve ser substituído silenciosamente por um resumo novo.**

### 2026-10-09 — Diagnóstico confirmado pelo print da execução do GitHub Actions

O usuário enviou screenshot da execução nº 7 do workflow `PvPPokeGo 0.5.33 QA APK`, com anotação vermelha **"O serviço não foi iniciado porque houve falhas nos pagamentos recentes da sua conta o..."** (o restante só aparece em "Mostrar mais"). Este é um **bloqueio administrativo de faturamento/limite no GitHub Actions antes do job**, não um erro demonstrado de Gradle ou Kotlin; NÃO é possível inferir do print se há cobrança efetivamente pendente ou só orçamento/limite esgotado. A anotação Ubuntu 26.04 é apenas um aviso informativo e não explica o bloqueio.

Próximos passos sem contratar serviço automaticamente: perfil GitHub > Settings > Billing & Licensing > consultar Payment history, Payment information, Usage e Budgets and alerts. Verificar erro completo com "Mostrar mais". Não adicionar cartão nem elevar limites automaticamente: ambas são decisões do proprietário. Se a conta for regularizada e o runner iniciar, reexecutar `.github/workflows/pvppokego-0533-qa.yml` e conferir `testStandardDebugUnitTest`, `assembleStandardDebug`, SHA256 e APK real. Alternativa sem GitHub-hosted runner: build local com Android SDK/Gradle em ambiente devidamente configurado e com acesso autorizado ao repositório privado. NÃO tornar código privado público sem autorização específica.


### 2026-10-09 — CONCLUSÃO DO BUILD PÚBLICO 0.5.33, TESTES E APK VERIFICADO

- Fonte canônica: `LucianoToscano/PvPPokeGo`, branch `feature/diagnostics-hp-ui-0.5.33`. Commit de código compilado: `3b316f6fcaf7cf7726ca324548d9ce2427be70c5`.
- Mirror público **isolado somente para build**: `LucianoToscano/PvPPokeGo-APK-Build`, branch `preview/diagnostics-hp-ui-0.5.33-20261009`, commit `655514514f1cdfa4ce89b7848aa36994dc639f27`.
- Comparação por SHA da árvore Git confirmou equivalência exata de todos os arquivos `app/`, `gradle/`, `tools/`, `settings.gradle.kts`, `build.gradle.kts`, `gradle.properties` com os da fonte privada compilada (excluindo histórico privado e documentação restrita). Nenhum arquivo de fonte necessário ao build ficou divergente.
- RUN FINAL: https://github.com/LucianoToscano/PvPPokeGo-APK-Build/actions/runs/37903983424, `completed/success`; job `113732891222` **success**. Passaram checkout, JDK17, SDK Android36, Gradle8.11.1, verificação de versão/launcher, assets offline PvPoke, atlas de ícones, `:app:testStandardDebugUnitTest`, `:app:assembleStandardDebug`, inspeção do APK, `apksigner verify` e upload do artefato.
- Artifact GitHub (id `11603767382`): `PvPPokeGo-0.5.33-QA-debug` (ZIP), contendo `PvPPokeGo-0.5.33-QA-debug.apk`, `SHA256SUMS.txt` e `signature-verification.txt`.
- APK real: **79.561.814 bytes**; SHA-256: `9de20e578d02e56dc11652d0b9f639293b99852cfa123f15aa600d0a0a6eb1d8` (checksum gerado no CI e recalculado após download, correspondência exata). ZIP testado sem erros; APK testado como ZIP sem erros, `assets/gamemaster.json`, `assets/pokemon_icon_atlas.png`, `assets/pokemon_icon_atlas.json` presentes; assinatura v2 validada no CI, 1 assinante.
- APK disponibilizado para download no chat via `sandbox:/mnt/data/PvPPokeGo-0.5.33-QA-debug.apk`, após download autenticado do artefato de GitHub Actions e inspeção.
- Nota obrigatória: **QA/debug, não assinatura release permanente**. Android pode impedir atualização sobre APK assinado com outra chave; orientar exportar dados/configurações antes de instalar/desinstalar. **Sem teste físico no Samsung neste checkpoint**: confirmar estado visual dos botões ativo/desativado, previsão HP, diagnósticos/Logcat, reconhecimento e overlay com o usuário. Nunca marcar essas validações como concluídas apenas porque o Gradle passou.
- Erro de processo corrigido: prioridade ao mirror público de compilação documentado desde 2026-10-07, com regra permanente no topo do Guia Mestre. Não criar mirror adicional nem repetir CI privado bloqueado sem necessidade.

**Retificação adicional (2026-10-09, scanner 0.5.34):** O usuário esclareceu que ele próprio renomeou o APK inserindo “Premium” apenas por estética; declara ser versão normal e permite usá-lo como referência. **Não classificá-lo mais como APK modificado/crack apenas pelo nome.** Autenticidade/assinatura original não foram verificadas, e o consentimento para analisar não equivale à propriedade do código do Poke Genie. Usar análise estática/requisitos e elaborar implementação própria. Branch de planejamento `feature/team-scan-0.5.34-planning`. Sem APK/build novo; documentação apenas.

**ENTREGA GOOGLE DRIVE 0.5.35 (2026-10-09):** APK `PvPPokeGo-0.5.35-QA-debug.apk` na pasta oficial [arquivo direto](https://drive.google.com/file/d/1oZ4srqfqZG-gi0OLudI6_6yS91Vn8Tfi/view?usp=drivesdk), ZIP de QA [arquivo](https://drive.google.com/file/d/19fh30lTLh4clFi8pSreXkFcN946VYYEG/view?usp=drivesdk); upload retornou sucesso e os dois IDs pertencem à pasta `12g6IITrn64y0D40YsIdzl-sN9alZty8Q`. APK também anexado no chat em `/mnt/data/PvPPokeGo-0.5.35-QA-debug.apk`, SHA256 `4b52dc60ea7ee29da7363a3e061bb72d44aa1377cc1a1106d25b6a54f9257172`. Não alterar a versão anterior nem mesclar branches sem revisar QA.



**Atualização real de CI 2026-10-09:** A primeira execução 37990812354 chegou à compilação Kotlin dos testes e falhou com erro verificável em `BattleHistorySegmentMatcherTest.kt:91`: construção de `BattleHistoryEvent` com três argumentos quando `moveName` é obrigatório. O teste foi corrigido na fonte canônica, e também foi corrigida a expectativa do teste de troca simultânea (agora exige dois frames). Runs intermediárias descartadas: 37991096113, 37992007040. **Run definitiva do source compilável atual:** https://github.com/LucianoToscano/PvPPokeGo-APK-Build/actions/runs/37992092526, mirror HEAD `41a83803e213503af19d99342acd0b80d1be0d2e`. Comparação real da árvore Git: **129 arquivos de código/recursos de build comparáveis iguais em SHA** entre fonte privada e mirror, zero divergências. Aguardar resultado desta run antes de declarar APK pronto; o CI antigo não comprova sucesso dos testes atualizados.


### 2026-10-13 — Drive oficial migrado, APK QA 0.5.36 recuperado e checklist atualizado

Versão: 0.5.36 — Standard QA/debug; versionCode 41.
Branch: `feature/battle-correspondence-0.5.36-20261009`.
HEAD base do código: `8b32a050390e8b0475ca3d1579c7759424c54480` (commits desta atualização são apenas documentação).

**OBJETIVO:**
- Corrigir referências antigas à pasta Drive de outra conta e entregar APK verificado com checklist, preservando o histórico e sem conflito com funcionalidades desenvolvidas em paralelo.

**ALTERAÇÕES:**
- Identificada nova pasta oficial do projeto no Google Drive (ID `1ybogVRRnaOe0KtWjlovxS56bJOi_JewI`, conta proprietária LucianoToscano).
- Download do artifact GitHub `11645731162` da run pública com sucesso `37992150961`; APK extraído do ZIP original, sem alterar binário nem renomear versão antiga.
- Arquivos oficiais `PvPPokeGo-0.5.36-QA-debug.apk` e `PvPPokeGo-0.5.36-QA-debug-APK-e-verificacao.zip` enviados ao novo Drive e conferidos por leitura de metadados (IDs `14wzZDCx0RMCFtH-olGYlFb9FDnpaATHu` e `1Y_ngMw12VcEPKMvBWVynMHnp6WQTyFOa`).
- Criados `docs/PROJECT_LINKS.md` e `docs/CHECKLIST-PvPPokeGo-0.5.36.md`; atualizados `START_HERE_NEW_CHAT.md`, `docs/CURRENT_DEVELOPMENT_HANDOFF.md`, `README.md` e este Guia Mestre.
- Registros de versões anteriores e links de entrega históricos preservados; antigos links não devem ser utilizados como referência atual.

**VERIFICAÇÃO/TESTES:**
- SHA-256 do arquivo extraído recalculado e idêntico a `SHA256SUMS.txt`: `e1ff7e2ac9e0a5f6e940f1d334bb2c7722409d7018bc3781253652a0679c1a94`.
- `unzip -t` ZIP original e APK: PASS. Recursos `gamemaster.json`, `pokemon_icon_atlas.json/png`, `pokemon_visual_fingerprints.json`: presentes.
- Relatório CI `signature-verification.txt` confirma v2=true, 1 assinante. `testStandardDebugUnitTest` e `assembleStandardDebug` concluídos com sucesso na run pública anterior; **não reexecutados nesta atualização de documentação**.
- Metadados Drive dos dois arquivos conferidos após upload: bytes/parentes corretos.

**RESULTADO:**
- APK verdadeiro da versão mais recente de código compilado disponível na nova pasta e nesta sessão. Checklist e links do projeto documentados.
- **Não houve nova compilação nesta data**: reutilizado e verificado o build legítimo de 09/10/2026.

**STATUS:** CONCLUÍDO para migração de links centrais, entrega do APK e documentação; PARCIAL para homologação funcional.

**PENDÊNCIAS:**
- QA físico em Samsung Android16: OCR 3 slots, formas e PCs, reservas, continuidade da batalha, escudos/HP, ícones, histórico, assinatura de instalação/backup.
- Referências históricas datadas continuam no documento por rastreabilidade. Futuras branches devem consultar `docs/PROJECT_LINKS.md` antes de publicar URLs.

**PRÓXIMO PASSO:**
- Receber export de diagnóstico e vídeo de batalha do APK 0.5.36; validar defeitos restantes sem mexer em HUD ou posições desnecessariamente.
