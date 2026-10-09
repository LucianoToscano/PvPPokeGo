# PvPPokeGo 0.5.36 — compilação isolada com atlas reaproveitado e teste Kotlin corrigido

**Checkpoint: 2026-10-09.** Continuação do projeto, sem reset e sem modificar a base estável 0.5.34/0.5.35.

## Motivação e proveniência

O workflow público de QA `37990812354` (branch `preview/battle-correspondence-0.5.36-20261009`) ficou muito tempo em `Package form-aware Pokemon icon atlas`, uma tarefa que baixa milhares de sprites. Em vez de suprimir ícones ou mudar HUD, foi criado um espelho de compilação **paralelo e isolado**, em `LucianoToscano/PvPPokeGo-APK-Build`, branch `preview/battle-correspondence-offline-0.5.36-20261009`, arquivo `.github/workflows/battle-correspondence-0.5.36-offline.yml`.

O workflow restaura SOMENTE os três recursos `pokemon_visual_fingerprints.json`, `pokemon_icon_atlas.json` e `pokemon_icon_atlas.png` do APK previamente compilado/assinado e aprovado em CI, `PvPPokeGo-0.5.35-QA-debug.apk`, artifact `PvPPokeGo-0.5.35-QA-debug` run `37974565481`. Usa autorização nativa de GitHub Actions (`actions:read`) e confere SHA-256 do **APK de origem**: `4b52dc60ea7ee29da7363a3e061bb72d44aa1377cc1a1106d25b6a54f9257172`. O teste checa integridade ZIP, existência e no mínimo 500 entradas de fingerprint e ícone, portanto preserva cobertura e interfaces. Fonte `gamemaster.json` é empacotada localmente pelo script padrão. **Não transportar dados de jogador, screenshots, credenciais ou fontes privados**.

## Erro REAL descoberto e reparado

O primeiro build offline [run 37991507117](https://github.com/LucianoToscano/PvPPokeGo-APK-Build/actions/runs/37991507117) chegou aos testes, mas falhou em `:app:compileStandardDebugUnitTestKotlin` no arquivo `app/src/test/java/com/lucianotoscano/pvppokego/data/BattleHistorySegmentMatcherTest.kt`, linha 91: instância `BattleHistoryEvent` tinha apenas três argumentos `(2_000, "PARTIDA", "INÍCIO")`, enquanto o contrato requer `elapsedMs, actor, category, moveName`. Correção efetiva: `BattleHistoryEvent(2_000, "SISTEMA", "PARTIDA", "INÍCIO")`, preservando o sentido do caso de teste. Commit fonte privada `bd67821e44bf19f21e7effe5343d086e9cfab90a`; mirror offline `a19644936b8b9c8dad0324b3e9ffed5b11e0e934`. Nenhuma alteração de produção foi necessária por esse erro do teste.

Rebuild automático [run 37991837778](https://github.com/LucianoToscano/PvPPokeGo-APK-Build/actions/runs/37991837778) disparado pelo push desse espelhamento; conferir testes, assinatura, hash e artifact **antes de declarar 0.5.36 concluída**.

## Arquivos alterados nesta continuação
- `engine/BattleCorrespondenceTracker.kt` e `BattleCorrespondenceTrackerTest.kt` nas melhorias de continuidade, gap e switches; `engine/BattleEngine.kt`, `overlay/BattleOverlayService.kt` no matcher.
- `data/BattleHistoryRepository.kt` e `data/SpeciesCpPlausibilityPolicy.kt` em correções anteriores preservadas na branch derivada.
- `app/src/test/java/com/lucianotoscano/pvppokego/data/BattleHistorySegmentMatcherTest.kt` **corrigido nesta rodada**.
- Workflow espelho público `.github/workflows/battle-correspondence-0.5.36-offline.yml` **criado**, sem mudar distribuição nem opções do aplicativo.
- Documentos `docs/` registram decisões e testes; última versão com QA físico ainda precisa de instalação/observação no aparelho real.

## Pendente até confirmação
- CI 0.5.36 end-to-end com `:app:testStandardDebugUnitTest`, `:app:assembleStandardDebug`, atlas/banco íntegros, assinatura v2 e SHA-256 de APK extraído.
- QA em Samsung Android16: times 3x3 e formas regionais, troca simultânea, adversário unilateral/visual contraditório, limpeza de estado no final, escudos/HP e diagnóstico. Sem testes físicos, não alegar funcionamento no aparelho.
- Comunidade online, reides, trocas, IVs Avaliar automáticos e recursos de outros APKs são backlog; não podem ser declarados concluídos apenas por análise.
