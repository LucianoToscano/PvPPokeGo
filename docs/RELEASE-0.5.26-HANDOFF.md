# PvPPokeGo 0.5.26 — handoff de build

## Escopo

A 0.5.26 parte da 0.5.25 e aplica a pesquisa externa sem empobrecer funções existentes.

Fontes de validação:
- PvPoke @ `f627e89e53c0c7b903fff097df7a0ad0ac95decc`
- GoPvPSim @ `0952963f04bbc313c042a422a3d042c066678639`
- PokéMoves @ `bd9ac2e2bb4df8dd72337d1aab299e04d42a3093`
- PoGO GBL Analyzer @ `d4dd848f29ff6b929eca5efe69b419aeefffef6d`

Os commits exatos e licenças ficam em `docs/research/pvp-sources.lock.json`.

## Mudanças principais

- Fast Move Phase Tracker com registro final por turnos de 500 ms.
- Energia residual e sequências sucessivas de Charged.
- Consistency Score/anomalias para não deixar uma leitura contraditória destruir o estado.
- CMP por intervalo de Attack compatível com CP/liga e IVs ocultos.
- Shield Decision Forecast.
- Farm-down forecast.
- Optimal throw consultivo com gate de confiança.
- Sac Swap consultivo com gate de resistência/HP/troca disponível.
- Bait permanece probabilístico/heurístico.
- HUD preserva camada escura de overfarm e ganha contexto de fase/consistência/CMP/seq/bait.

## Segurança de arquitetura

O aplicativo continua somente assistivo:
- não injeta código;
- não modifica Pokémon GO;
- não envia taps;
- não automatiza escudo, troca ou ataques;
- fontes externas são empacotadas/validadas fora do hot path;
- a batalha usa dados locais e observações visuais.

## Variantes

- Standard: sem AccessibilityService/specialUse.
- Recorder Compat: mantém fallback de screenshot por Acessibilidade em variante separada.

## Validação exigida para entrega

O workflow `.github/workflows/release-0.5.26.yml` deve:
1. reconstruir snapshot PvPoke e atlas offline;
2. executar `:app:testStandardDebugUnitTest`;
3. executar `:app:testRecorderCompatDebugUnitTest`;
4. compilar Standard e Recorder Compat;
5. validar manifests mesclados;
6. testar integridade dos APKs com `unzip -t`;
7. confirmar assets offline em ambos;
8. calcular SHA-256;
9. assinar release com keystore permanente quando Secrets existirem; caso contrário entregar QA/debug explicitamente identificado.

## Testes matemáticos adicionais

Além da suíte já existente, a 0.5.26 adiciona testes para:
- `EnergySequenceMath`;
- `FastMovePhaseTracker`;
- `CmpForecastEngine`;
- `ShieldDecisionEngine`;
- `TacticalForecastEngine`.

Um harness JVM local executado durante desenvolvimento destes cinco módulos concluiu 12 testes com 0 falhas. Isso não substitui o build/test Android via Gradle.

## Regra de entrega

Só chamar um arquivo de APK 0.5.26 se ele vier do código 0.5.26 compilado e passar pelos checks acima. Nunca renomear APK antigo.
