# PvPPokeGo 0.5.25 — auditoria e handoff

Data da consolidação: 2026-10-07

PR de consolidação: #30 (`release/0.5.25` → `release/0.5.24`)

## Linha de desenvolvimento auditada

- Baseline congelada: `release/0.5.20` @ `7099bee91ec728b7c7df6cff930c6b75b7ec69b0`. Não foi alterada.
- Linhas posteriores conferidas: `release/0.5.21`, `release/0.5.22`, `dev/0.5.23-reserves`, `release/0.5.23`, `release/0.5.24` e `main`.
- Ponto mais novo da linha Android antes desta consolidação: `release/0.5.24` @ `f73ecbdc5b1536b7b2a82d8bbae9c7a13b3179b9`.
- `dev/0.5.23-reserves` estava atrás da 0.5.24; `main` é uma linha divergente/legada e não substitui a linha release Android.
- PRs históricos relevantes: #26 merged; #27 e #28 permanecem draft; #29 tentou reconciliar reservas 0.5.23→0.5.24, mas não é mergeável diretamente. A 0.5.25 reaproveita apenas o que faltava sem remover o motor preditivo novo.

## Inconsistências reais encontradas

1. A branch `release/0.5.24` ainda declarava `versionName 0.5.23` / `versionCode 28`.
2. A linha 0.5.23 tinha thumbnails ao vivo dos cards de reserva; a 0.5.24 perdeu essa apresentação ao consolidar o atlas/predictor.
3. O HUD 0.5.24 descartava completamente um bloco de reserva quando `reserve.name == null`, contrariando o estado seguro `ANALISANDO`.
4. O preview de dano mostrava apenas a faixa sem escudo, sem explicitar no mesmo rótulo o resultado com escudo e o risco de KO.
5. O manifest principal ainda apontava diretamente para o PNG do launcher; os recursos adaptive/round oficiais existiam, mas não eram os recursos selecionados pelo manifest.
6. Os workflows existentes produziam debug APKs, portanto a assinatura podia mudar entre runners. Releases atualizáveis exigem um keystore permanente fora do repositório.

## Estado funcional auditado

| Área | Estado por código |
|---|---|
| Time de 3 / estado individual / energia em troca | ✅ implementado |
| OCR + CP + visual + speciesId/forma + StableCpTracker | ✅ implementado |
| Reservas por CP/speciesId/Dex com gates de confiança | ✅ implementado; regressão visual corrigida em 0.5.25 |
| Atlas offline por speciesId/forma + Dex inequívoco | ✅ implementado |
| Matchup move-aware / HP / energia / escudos | ✅ implementado |
| FastMoveSignalFuser / cadência / HP / motion auxiliar | ✅ implementado |
| ChargedAnimationCycleGate | ✅ implementado |
| ChargedMoveSlotResolver / proteção manual | ✅ implementado |
| Escudos 2→1→0 + estado desconhecido em captura no meio | ✅ implementado |
| Continuidade CONTINUE/SWITCH/HOLD/NEW/INSUFFICIENT | ✅ implementado |
| Assistente com ANALISANDO + estabilização de conselho | ✅ implementado |
| Preview de dano real/estimado | ✅ implementado; UI com/sem escudo ampliada em 0.5.25 |
| Textos da batalha + lobby + animação + trocas | ✅ múltiplas fontes |
| Predictor matemática/PvPoke/histórico local | ✅ implementado, com classes de evidência |
| Histórico conversa + estados + JSON/CSV/TXT/ZIP | ✅ implementado |
| HUD aprovado / blocos independentes / olho e cadeado | ✅ preservado |
| Menu rápido compacto | ✅ implementado |
| Home/configurações por grupos | ✅ implementado |
| Gravação interna: primeiro frame, fallback, validação MP4 | ✅ implementado por código; teste físico ainda obrigatório |
| Standard vs Recorder Compat | ✅ separados |
| Standard sem Accessibility/specialUse | ✅ por manifest fonte; CI valida merged manifest |
| Assinatura permanente | ⚠️ pipeline preparado; depende de secrets/keystore do proprietário |
| Ícone normal/adaptive/round | ✅ recursos unificados; CI valida |

## Regras de assinatura

O repositório não deve conter o keystore nem credenciais. O workflow 0.5.25 aceita estes GitHub Secrets:

- `PVPPKGO_RELEASE_KEYSTORE_B64`
- `PVPPKGO_RELEASE_KEY_ALIAS`
- `PVPPKGO_RELEASE_STORE_PASSWORD`
- `PVPPKGO_RELEASE_KEY_PASSWORD`

Quando os quatro existem, a build release é assinada com o mesmo keystore e o certificado é verificado/registrado no relatório. Quando faltam, o pipeline publica somente APKs QA/debug com nome explícito; eles não são declarados como release atualizável.

## Testes obrigatórios do pipeline 0.5.25

Equivalentes com flavors ao pedido `:app:testDebugUnitTest` / `:app:assembleDebug`:

- `:app:testStandardDebugUnitTest`
- `:app:testRecorderCompatDebugUnitTest`
- `:app:assembleStandardDebug`
- `:app:assembleRecorderCompatDebug`
- `:app:assembleStandardRelease` e `:app:assembleRecorderCompatRelease` quando há assinatura permanente
- validação dos manifests mesclados;
- `unzip -t` nos APKs;
- presença de atlas, fingerprint bank e snapshot PvPoke;
- SHA-256 de todos os APKs entregues;
- relatório TODO/FIXME e lista de recursos críticos.

## Teste físico ainda necessário

Mesmo com CI verde, estes pontos dependem do Samsung real: qualidade OCR/crops nas resoluções do aparelho, estabilidade de MediaProjection ao abrir/fechar o Pokémon GO, Samsung Screen Recorder no Recorder Compat, codec OEM do gravador interno, aparência do adaptive icon no launcher/cache Samsung, posição final dos blocos aprovados e comportamento de recomendações em batalhas reais.

A regra desta linha continua: **MELHORAR E ORGANIZAR, NUNCA EMPOBRECER**.
