# PvPPokeGo 0.5.35 — Correspondência visual, equipes e continuidade das batalhas

**Data:** 2026-10-09. **Fonte canônica:** `feature/verified-core-0.5.35-20261009`. **Base preservada:** 0.5.34 scanner + 0.5.33 HUD/HP/Logcat. Nenhum dado pessoal ou backup bruto foi publicado.

## Problema concreto, causa e correções

O histórico 0.5.33 apresenta pares espécie+PC fisicamente impossíveis (Wingull 1485, Vanillite 1485), sessões partidas indevidamente, múltiplos ataques inimigos estimados e eventos com inversão temporal. A auditoria correspondente é `docs/AUDITORIA_EXPORTS_0.5.33_2026-10-09.md`.

### Proteções de identidade aplicadas

- `data/SpeciesCpPlausibilityPolicy.kt`, `data/GameDataRepository.kt`, `detect/BattleOcrDetector.kt`: impedir que pares espécie+PC matematicamente impossíveis causem identidade ativa falsa, troca, reserva ou início de batalha. Sem descartar dados caso Game Master/CPM estejam ausentes. Registrar motivo sem texto pessoal.
- `engine/BattleEngine.kt`: matcher consome IDs de espécie/forma normalizados da base oficial offline; equipe manual é lista de membros, **não** presunção de ativo. A correspondência manual requer um slot compatível com PC ou espécie visual forte; visual e PC contraditórios não se sobrepõem. Reservas continuam separadas.
- `engine/BattleCorrespondenceTracker.kt`: compara IDs exatos de forma (Ninetales e Ninetales Alolan não são iguais); aceita trocas para membro conhecido sem reiniciar; novo oponente exige confirmação por frames (exceto volta de membro conhecido ou prompt explícito); troca simultânea de próprio+inimigo requer estabilidade; evidência unilateral desconhecida ou baixa confiança segura a identidade anterior.
- **Correção final da sessão atual:** para iniciar outra batalha com ambos os IDs desconhecidos e incompatíveis, o par deve ter **intervalo real de ausência de evidência** antes da **primeira** observação, mais a segunda observação concordante dentro da janela. A atualização de `lastBattleEvidenceAtMs` após a primeira observação **não invalida** a segunda, porque o detector mantém a mesma assinatura como candidata. Sem intervalo inicial, duas leituras OCR repetidas e erradas **não dividem a partida** e, quando violam o time conhecido, ficam em `HOLD_CURRENT`.
- `overlay/BattleOverlayService.kt`: consulta o tracker antes de mutar `engine.onDetection`. Em `HOLD_CURRENT` ou candidato `INSUFFICIENT` pendente, mantém o estado do ativo, adversário, energia e histórico sem sobrescrever; nova sessão só é aberta após `NEW_BATTLE` confirmado.

### Continuidade e histórico

- `data/BattleHistoryRepository.kt`: relógio de eventos de OCR assíncronos deve ser **não decrescente** dentro da sessão; entradas vazias sem identidade e sem evento real de combate são descartadas; registrar motivo de encerramento em todos os caminhos de `finish`.
- Não inventar resultados nem pontuação: Vitória/Derrota indeterminados permanecem desconhecidos.
- Os 14/20 carregados inimigos estimados na auditoria não foram convertidos em OCR observado: manter a fonte e confiança no histórico. Validação de escudos/HP **ainda depende de QA no aparelho real**.

## Arquivos corrigidos e testes

### Arquivos de produção
- `app/src/main/java/com/lucianotoscano/pvppokego/data/SpeciesCpPlausibilityPolicy.kt`
- `app/src/main/java/com/lucianotoscano/pvppokego/data/GameDataRepository.kt`
- `app/src/main/java/com/lucianotoscano/pvppokego/detect/BattleOcrDetector.kt`
- `app/src/main/java/com/lucianotoscano/pvppokego/engine/BattleCorrespondenceTracker.kt`
- `app/src/main/java/com/lucianotoscano/pvppokego/engine/BattleEngine.kt`
- `app/src/main/java/com/lucianotoscano/pvppokego/overlay/BattleOverlayService.kt`
- `app/src/main/java/com/lucianotoscano/pvppokego/data/BattleHistoryRepository.kt`
- `app/build.gradle.kts` (0.5.35 / versionCode 40).

### Testes JUnit e integração lógica
- `SpeciesCpPlausibilityPolicyTest.kt` — Wingull/Vanillite 1485 inválidos, Cramorant 1485 válido; CP desconhecido não causa falsos descartes.
- `BattleCorrespondenceTrackerTest.kt` — pares de espécies/formas diferentes, equipe/reserva conhecida, falsas evidências, switch simultâneo, repetição de oponente novo, pair incompatível com/sem intervalo e reset do contador de inatividade após a primeira observação.
- `ManualTeamMatchPolicyTest.kt`, `ReserveCardMatcherTest.kt` e demais testes do módulo `engine` executados juntos no `:app:testStandardDebugUnitTest` da compilação pública.

## Estado real de teste e distribuição

Workflow isolado na branch `preview/verified-core-0.5.35-20261009` do repo público `LucianoToscano/PvPPokeGo-APK-Build`:
- Primeiro build após correções de PC/histórico: [run 37974565481](https://github.com/LucianoToscano/PvPPokeGo-APK-Build/actions/runs/37974565481), **SUCCESS**.
- Rebuild após correções de matcher/formas/continuidade: [run 37990550678](https://github.com/LucianoToscano/PvPPokeGo-APK-Build/actions/runs/37990550678), **resultado a verificar antes de oferecer APK**.
- APK de QA/debug exige conferência de assinatura, SHA-256 e, possivelmente, backup antes de reinstalar devido a diferença de chave. Não alegar QA físico Samsung Android16 que não aconteceu.

## Pendências remanescentes (não esconder nem declarar implementadas)
- Confirmar execução final de JUnit/Gradle, artefato e hash no segundo run; avaliar erros concretos de CI caso apareçam.
- Reproduzir sessões sequenciais num **Samsung Android16** com screenshots/logs do próprio app; caso imagem e PC da espécie entrem em conflito, registrar ambos como candidatos não confirmados; confirmar recuperação real da posição do HUD.
- Coleta confiável de redução de escudos e HP, ataque carregado inimigo inferido vs confirmado e resultados de batalha continuam trabalhos futuros dependentes de dados reais. Nunca forçar valores sem evidência.
- Calibração geométrica da tela Avaliar (IVs exatos), rede social/comunidade, backends de reides e recursos dos APKs estudados **não foram implementados** nesta correção. Não copiar hooks, código proprietário ou binários de terceiros.
