# PvPPokeGo 0.5.31 — Seleção e cadastro manual da equipe inicial

**Data:** 2026-10-08/09  
**Referências visuais:** mockups fornecidos pelo usuário: painel principal "SUA EQUIPE", três cartões (Altaria, Houndoom e Rillaboom), seletor de Grande/Ultra/Mestra, IVs, CP, sexo, ícones de Mega/Sombroso e movimentos. São **diretrizes visuais**, não resultados reais confirmados nem fontes de rank/HP.  
**Fonte base:** `feature/arena-foundation-20261008` (0.5.30).  
**Implementação isolada:** `feature/team-builder-0.5.31-20261008` (0.5.31, versionCode 36).  
**Branch QA pública isolada:** `LucianoToscano/PvPPokeGo-APK-Build:preview/team-builder-0.5.31-20261008`.

## Objetivo aprovado

Permitir ao jogador preparar os três Pokémon na tela inicial antes da partida, usando espécies e golpes reais do Game Master local, mantendo um formulário organizado, sem confundir o que foi digitado com a detecção automática.

## Funções disponíveis no código da feature

1. Card "SUA EQUIPE" após o título do app, antes de Arena/Batalha, com três vagas fixas, PC salvo de cada uma, contagem de cadastros válidos e média PC dos valores informados.
2. Seletor de liga que reaproveita `SettingsRepository.leagueMode` existente (Automático, Grande, Ultra, Mestra); a seção "Liga e análise" permanece com a mesma flag persistida e o menu rápido continua usando o mesmo valor. Em automático, a validação tem como referência a última liga detectada, sinalizando que a confirmação depende do lobby.
3. Em cada vaga: busca por espécie/forma no Game Master offline incluído no APK, evitando espécies inventadas; seleção válida com ID estável; PC, HP máximo opcional, IV Ataque/Defesa/PS, seleção sexo, toggles Mega/Sombroso e até 3 golpes selecionados do movepool de PvPoke local (ágil e dois carregados). Golpes renderizam nomes pt-BR quando já existem no asset `move_names_ptbr.json`.
4. "Salvar Pokémon" e "Limpar vaga" operacionais. Troca de vaga descarta alterações não salvas e há aviso explícito. Nenhum slot pode ser marcado completo só com texto de pesquisa sem espécie confirmada + PC válido.
5. Dados salvos exclusivamente localmente em `manual_team_v1` no mesmo SharedPreferences `pvppokego_prefs` usado pelo sistema. Essa chave foi adicionada às chaves autorizadas de `exportBackupJson/importBackupJson`, sem tocar no formato de backup externo.
6. Validações puras em `ManualTeamPolicy`: exatamente 3 slots, IV 0–15, PC 10–10000, HP 1–2000 quando preenchido, gênero válido, limite da liga, slot só "pronto" se espécie+PC reais; não preencher outros campos automaticamente nem inventar classificação RANK.

## Limites deliberados e próximos passos

- **Importante:** este é cadastro MANUAL e não é evidência visual. O motor de batalha, OCR e reserva **não consomem automaticamente** esses dados até que seja implementada política de correspondência (identificador de espécie/forma, IV, CP, confiança e replay) com salvaguardas para não misturar slots em troca.
- Sexo, Mega e Sombroso são marcações locais; elegibilidade real da liga/copa e existência da forma devem ser verificadas em integração posterior, não estão garantidas por toggle.
- Sem calcular RANK/Elo a partir do cadastro. Rank de PvPoke e HP real dependem de dados corretos de nível, IV e forma, algoritmo auditado e versão.
- Imagens oficiais dos Pokémon nos cartões e acabamento visual idêntico aos mockups **pendentes**. A versão inicial prioriza operação limpa e validação de dados num editor recolhível, sem criar imagens falsas.
- Não alterar controles nativos Pokémon GO, HUD da 0.5.17, motor matemático ou identificação automática.
- Em versões seguintes: selecionar espécies e movimentos por listas mais eficientes, recortes oficiais do atlas para cada slot, informar quais campos foram reconhecidos e oferecer aceitar/corrigir, histórico de equipes e prévia de cobertura PvP com dados validados.
- Se houver múltiplas telas ou modalidades, manter sempre a **mesma** preferência `league_mode` e um único roster persistente.
- Rodar suite completa `:app:testStandardDebugUnitTest :app:assembleStandardDebug`, conferir APK e assinatura v2 e testar em dispositivo antes de considerar a 0.5.31 entregue.

## Arquivos

- `app/src/main/java/com/lucianotoscano/pvppokego/data/TeamSetupModels.kt`
- `app/src/main/java/com/lucianotoscano/pvppokego/data/TeamSetupRepository.kt`
- `app/src/main/java/com/lucianotoscano/pvppokego/ui/TeamBuilderPanel.kt`
- `app/src/main/java/com/lucianotoscano/pvppokego/ui/SettingsScreen.kt`
- `app/src/main/java/com/lucianotoscano/pvppokego/data/SettingsRepository.kt`
- `app/build.gradle.kts`
- `app/src/test/java/com/lucianotoscano/pvppokego/data/ManualTeamPolicyTest.kt`

**Status neste registro inicial:** implementação de código + testes adicionados; Android CI/APK/QA visual ainda aguardam resultados dos workflows para serem declarados.
