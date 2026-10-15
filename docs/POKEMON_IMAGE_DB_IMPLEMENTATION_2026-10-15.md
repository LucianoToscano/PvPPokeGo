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

## Resultado posterior: imagens reais obtidas e verificadas

- Runner público 38801519020: 1.071/1.071 retratos WEBP baixados; nenhum erro. ZIP com 1.072 membros (inclui relatório), 13.843.453 bytes, SHA256 `2d0a6434e1965596e2d05a23b395436e33a8694f0d71e5985579d86a13da0eaf`.
- Testes Python no ZIP: `testzip` PASS, 1.071 sha256 corretos, 1.071 imagens decodificáveis WebP RGBA 256x256. Prancha visual de todas as 46 Megas gerada (avaliação humana de nomes/identidade continua aberta).
- Link Drive: https://drive.google.com/file/d/1hScejMYPZrWSR4wuDiiQCVpuzaakPnZG/view
- Prancha: https://drive.google.com/file/d/143xIgW1f2OFUPqOsy7fgnRSW6auz0IE0/view
- Para uso rápido sem esperar lotes diários, acrescentado importador ZIP via Android OpenDocument com validação de nomes/arquivos/tamanho, execução em corrotina e armazenamento interno. UI contém botão Importar banco completo (ZIP).
- Primeiro CI APK identificou declaração duplicada `context` na UI; corrigido no fonte e na cópia pública. CI Android ainda em execução — não declarar APK testado nem entregue.

## Fechamento do build QA — 15/10/2026

**SUCESSO VERIFICADO:** https://github.com/LucianoToscano/PvPPokeGo-APK-Build/actions/runs/38809088247 ; todos os jobs e etapas concluíram com sucesso, com `testStandardDebugUnitTest`, `assembleStandardDebug`, validação do Manifest/APK e `apksigner verify`.

- **APK**: `PvPPokeGo-0.5.37-ImageDB-FINAL-QA-debug.apk`, 80.990.041 bytes, SHA-256 `7828f7ad092e0f807f3a95c0f3536f3ed8164c927bf2951306ee47f316f9b9b6`.
- **Artefato GH** `12008576522`, branch pública HEAD `02773db33ba39f7c6d914c9263daa218ce386e8d`.
- **Drive do APK:** https://drive.google.com/file/d/1-vmbqRPp73Qp-MqpPsqxBOXxE9FBXuGm/view — ZIP contém APK, SHA256SUMS, texto de assinatura e metadados.
- **Verificação:** ZIP exterior e APK íntegros; Game Master, PvPoke snapshot, atlas 1.167 identidades e fingerprints empacotados; `apksigner v2=true`, 1 signatário Android Debug (cert SHA-256 `ddd060c7a46b3581399b6012473936cdcd43a9fccb51473c9a6b3d9b71b85454`), pacote `com.lucianotoscano.pvppokego`, 0.5.37 (code 42).
- **Conteúdo opcional externo:** 1.071 WebPs RGBA 256×256, todos únicos e válidos (incluindo 46 Mega), banco no Drive https://drive.google.com/file/d/1hScejMYPZrWSR4wuDiiQCVpuzaakPnZG/view .
- **Correções adicionais e testes JUnit:** dedução da forma-base a partir do atlas 1.167 (preservar Mewtwo Armored, Pikachu Libre, Giratina/Deoxys etc), impedir confusão "Meganium" com "Mega", manter prioridade da espécie exata em relação ao Dex de OCR. Todos os testes Gradle do build final passaram.
- **No Android:** em `Configurações → Banco de imagens Pokémon → Importar banco completo (ZIP)`, selecionar o banco WebP salvo no aparelho. Atlas GO acompanha o APK para uso offline antes do import; os 1.071 WebPs não estão embutidos no APK, por padrão, para economizar espaço.
- **PENDÊNCIAS:** QA físico, teste de importar 1.071 imagens no aparelho, desempenho/consumo e OCR/reserva/troca; conferir atualização de assinatura do app atual; revisar direitos de artes © The Pokémon Company; não promover esta QA debug a release permanente nem considerar o APK 1.0.0 reconstruído.
