# PvPPokeGo — Banco híbrido de retratos Pokémon / checkpoint 15-10-2026

**Estado: PARCIAL.** A branch parte de `rebuild/1.0.0-checklist-20261013` (referência funcional 0.5.37). Não altera nem reverte a versão 0.5.36, não inicia um APK 1.0.0 sem aprovação e não cria coleções artificiais de imagens.

## Implementação
- `PokemonImageCatalog.kt`: 1.025 espécies-base + 46 candidatos Mega, separados por (Dex, forma), inclusive Charizard/Mewtwo Mega X/Y. Regional desconhecida não substitui pela base.
- `PokemonImageSyncWorker.kt`: WorkManager 24h, preferir Wi-Fi (configuração inicial ligada), limitar a 48 retratos por ciclo, conferir assinatura PNG, dimensões, comprimir WebP até 256px, substituir cache atomicamente.
- `PokemonImageStore.kt`: leitura/decodificação fora do Canvas do jogo, cache LRU de 16MB; nenhum request web dentro do draw.
- `PokemonIconAtlas.kt`: consulta imagem de cache para espécie/forma e usa atlas antigo como fallback; resolução Dex pelo atlas para telas que só têm speciesId.
- `BattleOverlayView.kt`, `HomeDashboard.kt`, `TeamBuilderPanel.kt`: apresentação assíncrona de retratos no HUD, Home e equipe com redraw sob atualização.
- `SettingsRepository.kt` + `SettingsScreen.kt`: ajustes de sincronização diária e Wi-Fi com backup.
- `PvPPokeGoApp.kt`, `app/build.gradle.kts`: agendamento e dependência WorkManager.
- `PokemonImageCatalogTest.kt`: 5 testes JUnit preparados (aguardando execução Gradle).
- `scripts/download_pokemon_images.py` e workflows: ferramenta de coleta opcional em CI, com checksums e relatório de faltantes.

## Fonte, imagens e direitos
- Fonte candidata: `PokeAPI/sprites`, pasta Pokémon HOME 3D; aproxima a estética da referência, **não é um pacote de imagens extraídas de Pokémon GO**.
- A fonte declara explicitamente que as imagens são copyright da The Pokémon Company; a licença CC0 do repositório não remove esses direitos. Revisar autorização antes de redistribuir imagens.
- URLs/IDs de Mega são **candidatos**; sem 46 PNG reais baixados e revisados não marcar Mega validada.
- Nenhuma imagem protegida foi acrescentada ao repositório.
- Pacote Fase 3 no Drive oficial: https://drive.google.com/file/d/1qgvRaI7zz8G8CS3sdIyfSnL1tMOMhP8X/view?usp=drivesdk
- Pasta de imagens oficial: https://drive.google.com/drive/folders/1zAEL_PhkrvXSyJ8twCWcQFKaybEtAyWI

## Verificações realizadas
- Catálogo Python original: **5/5 PASS** (chaves, cobertura Dex, X/Y, HTTPS); otimização WebP mockada: **4/4 PASS** (transparência, cache, falhas e host).
- Python compileall: PASS; ZIP Fase 3 criado e enviado para Drive via conector (ID 1qgvRaI7zz8G8CS3sdIyfSnL1tMOMhP8X).
- Cópia pública isolada: https://github.com/LucianoToscano/PvPPokeGo-APK-Build/tree/preview/pokemon-image-db-20261015
- PR de QA: https://github.com/LucianoToscano/PvPPokeGo-APK-Build/pull/1
- Primeiro run 38801047690 **falhou na instalação SDK** antes de Gradle (sdkmanager tools obsoleto), workflow corrigido; run 38801180350 **em fila ao registrar**. NÃO afirmar build concluído antes de conferir.
- O ambiente local não acessa o host dos PNGs; **0 imagens reais baixadas**. APK de imagem DB ainda não comprovado.

## Próximos passos e aprovação
1. Completar download em runner com internet; conferir cada Mega e relatório de faltantes.
2. Validar compile e testes Gradle no workflow isolado; depurar eventuais falhas.
3. Testes Android físicos: Home 3 cards, seleção de Mega, fallback offline, cache Wi-Fi, UI fluida, reconhecimento de reservas, continuidade, sem alterações no overlay.
4. Antes da distribuição pública do pacote, validar direitos das artes.
5. Apenas após QA e aprovação, integrar ao fluxo da reconstrução incremental 1.0.0; atualizar este Guia Mestre, checklist e espelho Drive; SHA256 do APK real.

**Nunca substituir um APK estável com um arquivo não compilado ou baseado em versão anterior.**
