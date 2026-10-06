# Release catalog — PvPPokeGo 0.5.20

## Identidade

- versionName: **0.5.20**
- versionCode: **25**
- applicationId/package: **com.lucianotoscano.pvppokego**
- branch de desenvolvimento: `release/0.5.20`
- branch estável: `v04-v03-stable-base`
- PR: **#22**
- merge: `475a6e68f9830f4d79019429e456f3a965d56bc8`

## Build validado

- GitHub Actions: **Build PvPPokeGo 0.5.20**
- run ID: **37492085202**
- artifact ID: **11425824095**
- testes + assembleDebug: **success**
- validação ZIP/APK: **success**
- APK: `PvPPokeGo-0.5.20.apk`
- SHA-256: `c134b25fc817384d3b6933eb95e77c15b9b2a36205c9959930829047ea9a1772`

## Projeto-fonte

O workflow `Package complete project ZIP` empacota:
- fonte Kotlin;
- recursos;
- assets PvPoke offline;
- banco visual `pokemon_visual_fingerprints.json`;
- testes;
- workflows;
- documentação.

O backup Android Studio distribuído externamente deve ainda incluir:
- Gradle Wrapper 8.11.1;
- `local.properties.example`;
- APK final + SHA em `reference/`;
- resumo/handoff e prompt para novo chat.

## Recursos principais consolidados

- overlay externo sem injeção/hook;
- OCR + CP + imagem para identidade;
- forms/speciesId exatos;
- matchups e movesets por forma;
- energia por Pokémon/forma;
- deduplicação de carregado por ciclo;
- dois carregados inimigos distintos;
- escolhas manuais preservadas;
- FAST com fusão de HP/cadência;
- continuidade conservadora de batalha;
- histórico estruturado e exportável;
- gravação interna;
- compatibilidade alternativa para gravadores Samsung/externos via Accessibility screenshot;
- ícone oficial Icon PVPPokeGo.png;
- package real sem `.debug`.

## Google Drive

Pasta oficial do projeto:
`https://drive.google.com/drive/folders/1uILvSzzWZO_FQ9aPy5aZ7lPeVF0n8lAT`

Manter a versão atual do APK, ZIP do build, projeto Android Studio e handoff nessa pasta/estrutura de releases.
