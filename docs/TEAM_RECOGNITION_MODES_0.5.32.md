# PvPPokeGo — Sua Equipe: modos Automático e Manual (0.5.32)

Data do registro: 2026-10-09. Estado: **implementado em código na branch de desenvolvimento, NÃO compilado ou validado em aparelho por solicitação expressa do usuário**.

## Fontes obrigatórias e continuidade

- Guia Mestre canônico usado como referência: https://github.com/LucianoToscano/PvPPokeGo/blob/release/0.5.29/docs/PROJECT_CONTINUITY_GUIDE.md
- Guia histórico: https://github.com/LucianoToscano/PvPPokeGo/blob/release/0.5.26/docs/PROJECT_CONTINUITY_GUIDE.md
- Branch isolada: `feature/team-recognition-modes-0.5.32`, derivada de `feature/team-builder-0.5.31-20261008` (último HEAD-base `79d3372c77a3d1cff31c21847e648ea50b03725e`).
- `release/0.5.29` permanece preservada em `952ff14e5f97fb461619946473a42c144cdc2caa`.
- A `0.5.31` entregou editor dos três Pokémon e APK QA. Este documento implementa os *modos de origem* dos dados sem substituir recursos já presentes.
- Referências visuais: duas imagens do usuário com página inicial e cartões Sua Equipe para Altaria, Houndoom e Rillaboom. A disposição é inspiração de layout, **não evidência de quais Pokémon são realmente usados pelo jogador**.

## Comportamento solicitado pelo usuário

Na página inicial `SUA EQUIPE` há duas opções excludentes:

### Automático — padrão

- O app **tenta reconhecer** os três Pokémon via detecção/OCR de seleção, PC e imagem/formas, usando os caminhos já existentes `BattleOcrDetector`, `BattleEngine.onOwnTeamDetected`, `ReserveCardMatcher` e `BattleCorrespondenceTracker`.
- Não presume que todos os slots foram identificados antes de haver evidência. Identidades, posições e confiança permanecem as reconhecidas pelo detector; scans incompletos não devem substituir a composição inteira.
- Retomar modo Automático retira a equipe manual da injeção do motor; a lista manual fica **salva para próxima seleção**, sem apagar os dados digitados.
- A exibição do time detectado na *própria página inicial* a partir do estado em tempo real do serviço (sem persistir informação possivelmente incorreta em `manual_team_v1`) é etapa de interface separada, **pendente**. O motor/HUD já possuem reconhecimento automático preexistente.

### Manual — jogador define equipe ATUAL

- Ao selecionar Manual, os três Pokémon que o jogador salvou em `SUA EQUIPE` tornam-se **o time atual declarado**, assim que todos os slots têm espécie válida do banco local, PC válido e elegibilidade numérica de PC da liga.
- Esta equipe é repassada ao `BattleEngine` por `TeamSetupRepository.currentlySelectedTeam` e `BattleEngine.selectManualTeam` durante a análise. Não requer que o app leia novamente a tela de seleção para saber **quais são os três membros declarados**.
- Mesmo em Manual, o app precisa da leitura da tela para determinar **qual dos três Pokémon está ativo**, o lugar das duas reservas, HP, energia e ocorrência de ataques. A ordem em que o jogador cadastrou os três **não é prova** da ordem nas cartas nativas do Pokémon GO.
- Se os três slots estiverem incompletos ou a liga for incompatível, **não** forçar a equipe como confirmada; os detectores podem continuar trabalhando enquanto o reconhecimento automático geral estiver habilitado.
- O botão de modo persiste junto ao backup de configurações (`team_recognition_mode_v1`). O cadastro continua independente em `manual_team_v1`.
- Uma mudança na equipe selecionada não pode reaproveitar a energia/HP do Pokémon anterior nem sobrescrever o inimigo com identidade aliada.
- Ao reiniciar/encerrar uma batalha, aplicar novamente a equipe manual ainda selecionada no início do novo contexto, sem assumir HP, contador de golpes ou escudos da partida anterior.

## Proteções de identidade / anti-regressão

- Identificação do membro **ativo** usa `ManualTeamMatchPolicy.memberIndex`: cruzamento de `speciesId` visual confiável, PC e espécie OCR; resultados ambíguos não escolhem um slot ao acaso.
- Se PC aponta para Altaria, mas uma imagem com boa confiança indica Houndoom, a leitura é considerada **conflitante**, não renomeia silenciosamente Altaria como Houndoom.
- Uma espécie que não faça parte do time declarado não pode substituir um dos três slots enquanto Manual estiver ativo.
- Um snapshot de equipe reconhecida automaticamente não substitui os slots manuais quando Manual estiver selecionado.
- Pokémon repetidos na mesma espécie exigem PC inequívoco; a ausência de diferenciação confiável mantém slot indeterminado.
- Os Pokémon do time manual são **declarados/selecionados**, não `CONFIRMADOS POR OCR` nem vitórias verificadas. A UI explicita essa procedência.
- Os dois caminhos devem usar a **mesma preferência de liga** já existente, sem configurações de liga duplicadas.
- `autoRecognition` global continua separada do modo da equipe. Quando está OFF, o time Manual pode permanecer pré-selecionado, mas não há identificação visual do ativo até que o reconhecimento geral seja ligado novamente. UI descreve essa limitação.
- Modos não modificam nenhum controle nativo do Pokémon GO. Não remover nem reposicionar olho, cadeado, HUD, ícones de reserva, contadores, OCR inimigo, energia ou botões anteriores.

## Implementação do código (ainda sem build)

- `app/src/main/java/com/lucianotoscano/pvppokego/data/TeamSetupRepository.kt`: enum `TeamRecognitionMode`, modo persistido, função `currentlySelectedTeam`.
- `app/src/main/java/com/lucianotoscano/pvppokego/data/SettingsRepository.kt`: backup da flag `team_recognition_mode_v1`.
- `app/src/main/java/com/lucianotoscano/pvppokego/ui/TeamBuilderPanel.kt`: escolhas Automático/Manual e texto condicional sobre time atual e reconhecimento global.
- `app/src/main/java/com/lucianotoscano/pvppokego/ui/SettingsScreen.kt`: integração com a preferência global preexistente.
- `app/src/main/java/com/lucianotoscano/pvppokego/data/ManualTeamMatchPolicy.kt`: regras puras para associação conservadora do ativo.
- `app/src/main/java/com/lucianotoscano/pvppokego/engine/BattleEngine.kt`: slots da equipe manual, associação do ativo, proteção contra time OCR sobrepor time declarado, resets e correspondência de batalha conservadores.
- `app/src/main/java/com/lucianotoscano/pvppokego/overlay/BattleOverlayService.kt`: carrega a seleção salva no serviço e a reaplica após a limpeza de uma batalha e nos ciclos de análise.
- `app/src/test/java/com/lucianotoscano/pvppokego/data/ManualTeamMatchPolicyTest.kt`: **código de teste criado, não executado**; casos PC, visual, contradição, duplicatas, espécie externa, troca de equipe.
- `app/build.gradle.kts`: rótulo de desenvolvimento `0.5.32`/`versionCode 37`. **Não representa APK compilado ou publicado**.

## Lista de aceite futuro — NÃO EXECUTADA

1. Testes unitários e compilação Kotlin/Compose/Android Standard, inclusive `ManualTeamMatchPolicyTest` e testes herdados.
2. Automático: 0/1/2/3 Pokémon vistos na tela, variação de confiança e diferença de forma; confirmar que OCR do inimigo e medição de energia continuam estáveis.
3. Manual: cadastrar três membros, selecionar, conferir pré-carga sem captura da tela de seleção e depois detectar o ativo correto.
4. Trocas durante a batalha, mesma espécie com PC diferente, mesmo PC de duas espécies, imagem e OCR discordantes, time não cadastrado, reservas superior/inferior invertidas.
5. Editar ou trocar time manual com serviço em execução; garantir isolamento de energia/HP e não reclassificar partida por palpite de um frame.
6. Troca Manual→Automático→Manual, mudança de liga, backup/exportar/importar, reiniciar serviço e recuperar de captura interrompida.
7. QA em Samsung com print do menu inicial e leitura de time real em batalha; sem alterar posição do HUD da referência V03/0.5.17.
8. Somente após QA, considerar exibir time automático na tela inicial com status de certeza e persistência de *snapshot do detector* separada do roster manual.
9. Não entregar nenhum APK 0.5.32 nem afirmar testes aprovados nesta etapa.

## Registro de segurança sobre repositórios

- O código autoritativo de desenvolvimento fica em `LucianoToscano/PvPPokeGo:feature/team-recognition-modes-0.5.32`.
- A branch pública de build `PvPPokeGo-APK-Build:preview/team-recognition-0.5.32-20261009` foi criada parcialmente durante o preparo anterior. **Não está sincronizada/validada e não deve ser usada como fonte de APK nem iniciar build sem novo pedido**.
- Permanecem intactos os APKs 0.5.30/0.5.31 e suas evidências; nenhum foi modificado/renomeado.

**Status final deste checkpoint:** funcionalidades de seleção de equipe e ligação ao motor **escritas em código**, documentação pronta; **testes e build NÃO EXECUTADOS**, QA/validação física pendentes.
