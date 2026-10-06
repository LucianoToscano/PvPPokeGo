# START HERE — PvPPokeGo 0.5.20

Este arquivo é o ponto de entrada para qualquer novo chat, nova conta ou nova sessão de desenvolvimento.

## Estado oficial

- Projeto: **PvPPokeGo**
- Versão estável: **0.5.20**
- versionCode: **25**
- package/applicationId: **com.lucianotoscano.pvppokego**
- branch estável: **v04-v03-stable-base**
- PR da versão: **#22**
- merge da 0.5.20: `475a6e68f9830f4d79019429e456f3a965d56bc8`
- build validado: GitHub Actions run **37492085202**
- artifact APK: **11425824095**
- SHA-256 APK: **c134b25fc817384d3b6933eb95e77c15b9b2a36205c9959930829047ea9a1772**
- Android: minSdk 29 / targetSdk 36 / compileSdk 36 / Java 17
- Gradle de CI: 8.11.1

## Leia nesta ordem

1. `docs/CURRENT_DEVELOPMENT_HANDOFF.md`
2. `docs/RELEASE_0.5.20.md`
3. `CHANGELOG.md`
4. `docs/MASTER_PROJECT_CONTEXT_2026-09-27.md`
5. `docs/FINAL_HUD_POSITION_MAP.md`
6. `docs/NATIVE_HUD_PROTECTION.md`
7. `docs/PVPOKE_INTEGRATION.md`

## Regra permanente

PvPPokeGo é um aplicativo Android **externo de overlay/assistência visual**. Não modificar, injetar, hookar nem automatizar cliques/gestos dentro do Pokémon GO.

## Baseline visual que não pode regredir

- manter posições aprovadas do HUD;
- olho e cadeado delicados;
- ícone de configurações discreto;
- Icon PVPPokeGo.png é o ícone oficial do APK;
- não cobrir controles importantes do jogo;
- badge da liga não deve permanecer visível fora do contexto de batalha;
- menu deve continuar simples, organizado e com ícone + texto;
- preservar backup/restauração, histórico, exportações e gravação interna.

## Como continuar

Antes de alterar código:
1. conferir o HEAD de `v04-v03-stable-base`;
2. conferir o último workflow da versão;
3. preservar os testes da 0.5.20;
4. criar a próxima versão sequencial: **0.5.21**;
5. nunca reutilizar 0.5.20 para binário diferente.

Se o problema vier de uma batalha real, pedir/usar:
- ZIP exportado do histórico;
- diagnóstico do app;
- vídeo/gravação quando disponível;
- screenshots relevantes.

Não volte a hipóteses já corrigidas. A 0.5.20 já contém continuidade de batalha, deduplicação por ciclo de carregado, CP estável, slots distintos de carregados inimigos, reconhecimento visual por forma e fallback para gravador externo.
