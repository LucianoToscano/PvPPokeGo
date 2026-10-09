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

