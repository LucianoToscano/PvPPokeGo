# PvPPokeGo 0.5.35 — Estabilização do núcleo e QA

**Data:** 2026-10-09. **Branch fonte:** `feature/verified-core-0.5.35-20261009`, derivada da `feature/team-scan-0.5.34-implementation`. **Espelho público isolado de build:** `LucianoToscano/PvPPokeGo-APK-Build`, branch `preview/verified-core-0.5.35-20261009`. Nenhuma preferência privada, arquivo de jogador, senha, QR, histórico ou APK de terceiros enviado ao espelho.

## Código realmente implementado

1. `data/SpeciesCpPlausibilityPolicy.kt` (novo): cálculo de PC máximo matematicamente possível para cada forma/espécie com base nos stats do Game Master embutido, IV15/15/15 e CPM até nível51. Quando CP ausente, não assume PC nem inventa espécie. Quando faltam os stats, não bloqueia observação válida.
2. `data/GameDataRepository.kt`: `isSpeciesCpPlausible(species, cp)` reutilizando o catálogo offline e os CPMs já carregados.
3. `detect/BattleOcrDetector.kt`: aplica o guard nas detecções dos cards de batalha (aliado/adversário), nos cards da seleção 3x3 e no fallback OCR amplo, inclusive quando evidencia visual e textual divergem; casos impossíveis são descartados sem atualizar identidade, CP, histórico ou reservas. Registra só o código anônimo `ocr|species_cp_impossible`, sem imagens ou texto OCR.
4. `data/BattleHistoryRepository.kt`: relógios OCR assíncronos não podem acrescentar eventos com `elapsedMs` inferior ao último (clamp com breadcrumb diagnóstico); captura motivo de encerramento para todas as rotas que chamam `finish()`; sessões sem identidade de Pokémon nem evento de combate observável são descartadas do histórico competitivo em vez de contadas como partidas reais, com log de motivo.
5. `data/SpeciesCpPlausibilityPolicyTest.kt` (novo): testes locais Wingull PC1485 inválido, Vanillite PC1485 inválido, Cramorant PC1485 válido, CP ausente, dados ausentes e PC >10000.
6. `app/build.gradle.kts`: versionCode40, versionName0.5.35.

## Regra de estabilidade

Preservados sem alteração: overlay de batalha e seus controles de toque, posição do HUD, ícones originais, scanner manual de 3 Pokémon da 0.5.34, previsão fantasma de HP e faixa de sobrevivência, menu ON ciano/OFF cinza, equipe Manual/Auto, backup e import/export, classificadores de riscos e motor preditivo PvPoke. Mudanças limitadas a validação de identidade e integridade de histórico para evitar regressões.

## Separar o que existe do que é backlog

Estudos de Calcy IV, Poke Genie, Pokémon GO Hub e Regibot, modo comunidade (amigos, clãs, duelos, reides, trocas e eventos), app externo e banco de dados remoto **são propostas/documentos**, NÃO são implantados só por estarem analisados. Serviço backend, autenticação, moderação, privacidade e testes reais devem anteceder funcionalidades online. Não transplantar código ou assets proprietários nem usar injeção no jogo. Scanner Avaliar ainda não extrai IV exato de barras sem calibração e supervisão do usuário; live import ainda pendente. Não declarar que estes recursos estão prontos nesta versão.

## Build e QA

Workflow isolado `.github/workflows/verified-core-0.5.35.yml`, run GitHub Actions [37974565481](https://github.com/LucianoToscano/PvPPokeGo-APK-Build/actions/runs/37974565481): testes `testStandardDebugUnitTest` e `assembleStandardDebug`, integridade APK, SHA256 e verificação da assinatura. **Resultado deverá ser confirmado pela execução real e inserido aqui antes de afirmar sucesso.** APK QA debug pode exigir desinstalação se assinatura divergir. Exportar backup antes, especialmente posições do HUD e equipe; não desinstalar a versão funcional sem conseguir restaurar.

## Pendências P0 não resolvidas por esta entrega

- Reconhecimento e uso de escudo: relatório 0.5.33 apresenta 282 snapshots shields=2; validar com vídeos e logs reais, sem inventar decrementos.
- 14/20 Charged inimigos inferidos: distinguir OCR confirmado e hipóteses no HUD e treinar com dados reais; nenhum ganho de acurácia foi medido.
- Identidades transitórias sem CP e incerteza ao trocar: testes instrumentados reais em Samsung Android 16.
- Resultado de partida permanece indeterminado na ausência de prova; não contabilizar vitória, derrota ou Elo automaticamente.
- Ampliar diagnósticos relevantes do próprio processo sem logs de outros apps, permissão excessiva ou dados privados.
- Comunidade/reides/trocas exige produto separado e backend seguro; seguir roadmap e consentimentos.


## RESULTADO VERIFICADO DA COMPILAÇÃO — 2026-10-09
- Workflow público isolado **completou com conclusão SUCCESS**: [run 37974565481](https://github.com/LucianoToscano/PvPPokeGo-APK-Build/actions/runs/37974565481), job **113969447354**.
- SDK Android 36, Java 17, Gradle 8.11.1, pacotes offline PvPoke e atlas visual: todas as etapas SUCCESS.
- **Testes unitários** `:app:testStandardDebugUnitTest` e **compilação** `:app:assembleStandardDebug`: SUCCESS.
- Verificação APK ZIP: sem erros; assinatura `apksigner verify` **v2=true, 1 signatário**.
- Artifact: [`PvPPokeGo-0.5.35-QA-debug`](https://github.com/LucianoToscano/PvPPokeGo-APK-Build/actions/runs/37974565481), artifact ID **11638566198**; vence **2026-11-08** no armazenamento do GitHub Actions.
- Arquivos entregues na conversa: `PvPPokeGo-0.5.35-QA-debug.zip` (ZIP artifact) e `PvPPokeGo-0.5.35-QA-debug.apk` (APK extraído); **79.660.114 bytes**; SHA-256 do APK **`4b52dc60ea7ee29da7363a3e061bb72d44aa1377cc1a1106d25b6a54f9257172`**. Confere exata/independentemente com `SHA256SUMS.txt` do runner. APK 0.5.35 **não é byte-a-byte igual** ao 0.5.34 (SHA deste: `45e24fdf5d6c9a3c899bda7e08cec8a826231201c1f07f30604c99d3f7bc6d3e`), embora tenha mesmo tamanho. ZIP do APK também testado como íntegro.
- **QA no aparelho não realizado**: não afirmar combate/OCR/escudo/scanner de captura testados no Samsung. Pode exigir reinstalação se assinatura divergir; exportar backup primeiro. Não usar dados 0.5.33 sem revisar privacidade.

**Links permanentes de entrega:** [APK 0.5.35 no Google Drive](https://drive.google.com/file/d/1oZ4srqfqZG-gi0OLudI6_6yS91Vn8Tfi/view?usp=drivesdk) · [ZIP e checksum 0.5.35 no Drive](https://drive.google.com/file/d/19fh30lTLh4clFi8pSreXkFcN946VYYEG/view?usp=drivesdk). Ambos uploads foram aceitos na pasta final oficial indicada pelo usuário. Os hash/signature check foram executados anteriormente sobre o arquivo local; não se afirma que o download do Drive foi reinspecionado independentemente.
