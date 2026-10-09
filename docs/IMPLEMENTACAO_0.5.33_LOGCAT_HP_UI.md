# PvPPokeGo 0.5.33 — Logcat, estado OFF cinza, HP sobrevivente (2026-10-09)

## Status de entrega — IMPORTANTE

**Código fonte implementado em branch isolada `feature/diagnostics-hp-ui-0.5.33`, baseado em `feature/team-recognition-modes-0.5.32`.**
**Ainda NÃO existe APK 0.5.33 compilado e validado.** A tentativa de compilação automática no GitHub Actions falhou em poucos segundos **antes de qualquer etapa de Gradle/Android executar**; o job não apresenta steps e o endpoint de logs não oferece logs. NÃO afirmar sucesso do build, nem rotular APK 0.5.31 como 0.5.33.
- Exemplos de runs: https://github.com/LucianoToscano/PvPPokeGo/actions/runs/37901931447 e https://github.com/LucianoToscano/PvPPokeGo/actions/runs/37902276938
- Causa exata não aparece em logs. Conferir aviso de GitHub Actions (faturamento, minutos, habilitação ou runner/conta) na interface do repositório antes de concluir. Não foi testada no aparelho.
- Workflow para rerun futuro: `.github/workflows/pvppokego-0533-qa.yml`. Alvo: `testStandardDebugUnitTest`, `assembleStandardDebug`, SHA256 e artifact `PvPPokeGo-0.5.33-APK`.
- A versão no `app/build.gradle.kts` é `versionCode = 38`, `versionName = "0.5.33"`. **Isso sozinho não gera um APK**.

## Alterações implementadas em código

1. `app/src/main/java/com/lucianotoscano/pvppokego/data/AppDiagnosticLog.kt` (NOVO):
   - Trail app-private de eventos simples, limitado a 128 KiB, I/O em executor dedicado, relógio monotônico e `Log.i("PvPPokeGo", ...)`.
   - Coleta **apenas mensagens do processo do próprio PvPPokeGo** via `logcat -d -t 100 -v threadtime --pid=<myPid>`, best-effort com timeout, filtro pelo tag `PvPPokeGo`, sem `READ_LOGS` e sem root. Alguns aparelhos podem negar; nesse caso salvar aviso no ZIP em vez de simular dados.
   - Não coleta Logcat do Pokémon GO, apps de terceiros nem logs globais do Android; para problemas de sistema o usuário pode fornecer bugreport oficial/ADB, após revisar privacidade.
2. `MainActivity.kt`:
   - Inicia diagnósticos sem restaurar preferências e mantém exportador original por SAF. Acrescenta `runtime-events.log`, `logcat-app.txt`, `integrity-warnings.txt` dentro do **mesmo** `diagnostico.zip` existente.
   - `integrity-warnings.txt` apenas aponta sessõess sem resultado, evento fora de ordem (`elapsedMs`) e sessões curtíssimas com resultado; não altera o histórico, não inventa derrota, não funde sessões.
   - ZIP conserva `settings.json`, `battle_history.json`, `device.txt`, `README.txt`. `historico.zip` ainda mantém JSON/TXT/CSV.
3. `BattleOverlayService.kt` e `BattleHistoryRepository.kt`:
   - Eventos de criação/parada/reset do serviço, início/fim de sessão e breadcrumbs genéricos de diagnósticos. Não inclui texto bruto OCR/nomes de treinador no Logcat; permanece com amostragem limitada.
4. `QuickOverlayMenu.kt`:
   - Status ON agora **ciano**; OFF fica **cinza-escuro**, ícone/texto OFF com tinta acinzentada para diferenciar claramente; botões de ação neutros continuam neutros.
   - Vale para lista com rótulos e grade só com ícones; mantém posições, click targets e comportamento.
   - Menu avançado inclui opção de HP restante quando HP assistido estiver ativo.
5. `SettingsScreen.kt`:
   - Toggle ON em ciano com cartão leve; OFF cinza com texto atenuado; não muda o estado de opções persistidas. Informa no botão que ZIP inclui Logcat do app.
   - Controle separado para `Mostrar HP restante previsto`, visível quando o HP assistido estiver ligado.
6. `SettingsRepository.kt`:
   - `showHpRemainingForecast` persistente (padrão true); entrou no allowlist de backup. Não removeu/alterou os ajustes de posição.
7. `BattleOverlayView.kt`:
   - Mantém faixa fantasma de dano sobre a barra de HP nativa quando leitura e modelo permitem.
   - Exibe intervalo pós-golpe `HP ~baixo-alto%` configurável, segunda linha para escudo, indicador de KO possível/provável. Quando ROI Y está ausente, mostra prévia discreta junto ao indicador do Pokémon, se ambos HP e previsão são conhecidos.
   - Não desenha previsão em captura `STALE`/`PAUSED`, nem inventa percentuais sem dados. Sem interceptar toques no jogo.
8. `app/build.gradle.kts`: v0.5.33 build code 38; branch v0.5.29 release/historical v0.5.31 APK preservadas.

## Testes e pendências reais

- **Conferido via revisão textual do código:** preservação dos nomes dos arquivos existentes no diagnóstico, settings allowlist, enum de confidence do forecast, guarda para captura stale, esquema do Logcat do próprio PID, diferenciação visual OFF. Esta conferência estática não substitui compilação.
- **Tentativa de CI:** falhou sem iniciar etapas, portanto **0 testes Kotlin/Gradle executados e 0 APK resultante**.
- P0: restaurar disponibilidade de runner GitHub Actions (verificar aviso na execução indicada), reexecutar workflow, obter sucesso dos testes e SHA256 do APK; inspecionar Logcat em Samsung real e comportamento em Android 16 sem `READ_LOGS`.
- P0: QA visual da tela de configurações em temas claro/escuro e menu avançado com opções on/off; garantir ausência de regressões no HUD e proteção de toques.
- P0: reproduzir caso histórico 0.5.31 com OCR/manual/Auto, verificar que diagnósticos coletam causa sem vazar dados nem travar serviço.
- P1: aprimorar breadcrumbs de candidatos/rejeições OCR para a Caixa-Preta, após primeiros resultados reais.
- P1: avaliar filtros e dimensionamento de avisos de HP em telas diferentes, nunca afirmar % como medição exata.
- Não alterar commit de `release/0.5.29` ou distribuir APK com assinatura diferente sem avisar que pode exigir backup e reinstalação.

## Como obter o APK quando CI voltar

1. Reexecutar `PvPPokeGo 0.5.33 QA APK` na branch `feature/diagnostics-hp-ui-0.5.33`.
2. Exigir status **success** em Gradle testes + compile.
3. Baixar artifact `PvPPokeGo-0.5.33-APK`, extrair `PvPPokeGo-0.5.33.apk`, conferir SHA256.txt.
4. Se assinatura debug diferir da instalada, exportar config e histórico antes de desinstalar. Nunca alegar que é APK funcional sem smoke test físico.

A autorização desta conversa contempla implementar/build, mas falha de infraestrutura NÃO autoriza publicar espelho público ou expor dados privados do repositório sem novo consentimento.
