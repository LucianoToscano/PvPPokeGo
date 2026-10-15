# PvPPokeGo 0.5.38 — Auditoria incremental e preservação de versões (2026-10-15)

**STATUS: PARCIAL — NÃO É RELEASE DEFINITIVA NEM FUSÃO DE TODAS AS BRANCHES.**

## Fontes e backups imutáveis
- Fonte Android mais completa nesta trilha: `feature/pokemon-image-db-1.0.0-20261015`, HEAD inicial `6ea367a9fa52c4198a7398aa6417b8b8d3c10d16`, incluindo ImageDB e Home 0.5.37.
- Backup separadamente preservado: `backup/pre-audit-imagedb-0.5.37-20261015`, no HEAD original acima.
- Branch desta correção: `feature/definitive-imagedb-audit-0.5.38-20261015`, versionName 0.5.38 / versionCode 43.
- Backup da Home 0.5.37 anterior à descoberta ImageDB: `backup/pre-definitive-0.5.37-20261015` e branch de testes anterior `feature/definitive-audit-0.5.38-20261015` (não usar esta como definitiva; não possui ImageDB).
- Repositório público usado **somente para build**: `LucianoToscano/PvPPokeGo-APK-Build`, branch `preview/definitive-imagedb-audit-0.5.38-20261015`; sempre verificar igualdade do blob SHA dos arquivos espelhados e resultado do Actions.

## Problemas comprovados e diffs conceituais

1. **CP OCR transitório de batalha/zoom** — `app/src/main/java/com/lucianotoscano/pvppokego/engine/StableCpTracker.kt:24-31`. O tracker antigo mantinha candidate/streak após OCR nulo ou CP impossível. Corrigido para zerar `candidate` e `streak` nesses frames, SEM alterar `accepted`. Evidência: novo teste `missingOrInvalidFramesCannotCompleteInterruptedCpCandidate` e `missingFrameInterruptsFirstCpConfirmation` em `StableCpTrackerTest.kt`. Não substitui validação espécie+CP, já existente desde 0.5.35.

2. **Buff/debuff de duplo alvo** — `app/src/main/java/com/lucianotoscano/pvppokego/engine/StatStageTracker.kt:23-36`. Quando Game Master fornece apenas `buffsSelf` e `buffsOpponent` e `buffTarget=both`, `buffs` podia ficar vazio e o código retornava antes de aplicar os estágios. A detecção agora aceita esse formato sem remover o cap -4..+4. Teste `dualTargetOnlyExplicitBuffsStillAffectBothSides` em `StatStageTrackerTest.kt`.

3. **Travamento na montagem da View por atlas de sprites** — `app/src/main/java/com/lucianotoscano/pvppokego/overlay/PokemonIconAtlas.kt:35+`, `BattleOverlayView.kt:111`. A construção síncrona decodificava bitmap e JSON no thread da UI, elevando latência do HUD e pressão de memória. Construção de atlas/index em `Dispatchers.IO`, publicação no main thread, callback de redraw e `release()` descartando atlas se o overlay fecha durante carregamento. **Preservados** `PokemonImageStore`, escolha de forma exata, lógica de Mega/HOME e fallback de atlas da implementação ImageDB. PENDENTE: teste instrumental de memória/UI, imagens em aparelho e possíveis recursos do `PokemonImageStore` em reciclagem de bitmap.

4. **Versão isolada** — `app/build.gradle.kts`: 0.5.37/code42 -> 0.5.38/code43. Apenas candidata de QA; não altera Android package `com.lucianotoscano.pvppokego`, flavors ou regras de overlay externo.

## Divergências estruturais e funcionalidades pendentes
- **O build 0.5.30 Hybrid QA não está linearmente incorporado na branch ImageDB/Home 0.5.37.** SHA público original `d838a44a3bf6ded5110e82589f2961cc205f14af` existe em `LucianoToscano/PvPPokeGo-APK-Build`, mas NÃO no GitHub privado citado inicialmente. Ainda falta integração segura de `OpponentTeamTracker`, `HybridSnapshotStore`, `tools/generate_hybrid_feed.py`, `tools/test_generate_hybrid_feed.py` com o BattleEngine/Settings atuais.
- O build público Hybrid da branch `preview/hybrid-data-sync-20261015` passou a CI `38821812163` e contém APK e ZIP de fonte editável. Esse sucesso valida a trilha **Hybrid 0.5.30 separada**, não a presença do Hybrid na candidata ImageDB 0.5.38.
- **BattleEngine.kt** ~2.475 linhas. Há módulos EnergyTracker, EnemyEnergyTracker, FastMovePhaseTracker, ChargedAnimationCycleGate, DamageForecastEngine, ShieldDecisionEngine, AdviceStabilityGate, etc. A reextração completa da API ainda não foi executada; não tentar modularizar sem regressões abrangentes.
- `EnemyEnergyTracker.observeCharged` e `observeUnknownCharged` já usam filtro por energia suficiente e fallback intervalar (sem energia negativa por subtração). Provar integridade com replay e OCR real ainda é pendente.
- `BattleOverlayService.onDestroy` já cancela scanJob/scope, fecha OCR, gravação e captura; há liberação de frames em finally. Nenhum vazamento específico comprovado nessa rotina; teste instrumental de bitmap ainda pendente.
- `DamageForecastEngine` aplica faixa de estágios; ShieldDecision usa a faixa calculada via ChargedMovePredictor. Falta QA com replay de buffs e escudos em Android.
- A política Standard **sem Accessibility Service**, o overlay externo que nunca injeta/clique no jogo e posições HUD aprovadas foram preservados.
- `tools/package_offline_pvpoke_assets.py` usa PvPoke REF fixo, coleta dados e registra SHA; `tools/update_pvpoke_data.py` por padrão aceita `master` mutável. Auditoria de propagação/hashes e snapshot completo está PENDENTE.
- `./gradlew testDebugUnitTest` e `pytest tools/test_generate_hybrid_feed.py` NÃO puderam ser executados **localmente nesta sessão**: não há SDK Android/Gradle no sandbox ativo nem arquivo generate_hybrid_feed.py nesta branch ImageDB. A variante correta é `:app:testStandardDebugUnitTest`, pois há flavors. Conferir os runs CI indicados abaixo.

## CI e requisitos de aceitação
- Home 0.5.38 teste preliminar SEM ImageDB: https://github.com/LucianoToscano/PvPPokeGo-APK-Build/actions/runs/38825544494 — SUCCESS em `testStandardDebugUnitTest` e `assembleStandardDebug`; não promover a final.
- ImageDB 0.5.38 CI na branch isolada: https://github.com/LucianoToscano/PvPPokeGo-APK-Build/actions/runs/38825913916 — conferir resultado, assinatura, hash, tamanho e assets antes de qualquer declaração de APK pronto.
- Hybrid 0.5.30 teste de feed + build + fonte + teste launcher em emulador: https://github.com/LucianoToscano/PvPPokeGo-APK-Build/actions/runs/38821812163 — SUCCESS, somente nessa trilha.
- QA físico Samsung Android16 / reconhecimento 3 slots / histórico / escudos / HP / trocas / HUD / assinatura permanente não testados aqui.

## Próximo passo obrigatório antes de release definitiva
Integrar via revisão de PR o Hybrid 0.5.30 na árvore ImageDB 0.5.38 sem sobrescrever módulos recentes, conservar todos os testes e assets, validar hashes offline e status de Settings, executar suíte Standard Android e pytest do feed no mesmo build, comparar APK com baseline e executar QA real. Só então publicar ZIP completo editável e APK assinado, atualizar este guia e o Drive **novo**: https://drive.google.com/drive/folders/1ybogVRRnaOe0KtWjlovxS56bJOi_JewI.
