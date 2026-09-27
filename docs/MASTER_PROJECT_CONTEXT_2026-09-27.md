# PvPPokeGo — Contexto mestre para novos chats

Atualizado em: 2026-09-27
Branch de trabalho: `v04-v03-stable-base`

> LEIA ESTE ARQUIVO PRIMEIRO EM QUALQUER NOVO CHAT, CONTA OU SESSÃO DE DESENVOLVIMENTO.
> Ele existe para evitar perda de contexto entre chats e registrar o que já foi construído, quais arquivos foram usados, decisões de arquitetura, regras visuais, builds e problemas ainda em validação.

## 1. Objetivo do projeto

O PvPPokeGo é um aplicativo Android externo de assistência para batalhas PvP do Pokémon GO.

O aplicativo deve funcionar como overlay e copiloto de batalha em tempo real, analisando apenas o que está visível na tela por captura/visão/OCR e exibindo informações auxiliares.

Regra permanente de arquitetura:

- NÃO modificar o processo do Pokémon GO;
- NÃO injetar código no jogo;
- NÃO fazer hook de memória;
- NÃO automatizar toques/cliques/gestos no Pokémon GO;
- NÃO usar Accessibility para jogar automaticamente;
- o PvPPokeGo pode analisar, calcular, recomendar, mostrar, vibrar ou falar; o toque final no jogo continua sendo humano.

## 2. Repositório, branch e build atual

Repositório: `LucianoToscano/PvPPokeGo`

Branch ativa de desenvolvimento: `v04-v03-stable-base`

Package atual: `com.lucianotoscano.pvppokego`

Versão Android atual no código:

- `versionCode = 5`
- `versionName = 0.5.0-copilot-hardening`
- `minSdk = 29`
- `compileSdk = 36`
- `targetSdk = 36`
- Java/Kotlin target 17

Workflow de build principal:

`.github/workflows/android-debug.yml`

APK gerado pelo workflow:

`PVPPokeGo_V05-copilot-debug.apk`

Último head conhecido ao salvar este documento:

`4f48cda9238f1ea40d4223cbb7389541e7078cf1`

Último build validado:

- GitHub Actions run `#78`
- run id `36312971519`
- conclusão: `success`
- artifact id `10930015695`
- artifact: `PVPPokeGo-V05-copilot-build`
- artifact expira em 2026-10-11

Importante: artifacts do GitHub Actions são temporários. O código-fonte é a fonte permanente. APKs considerados checkpoints importantes devem futuramente ser colocados em Releases/tags permanentes.

## 3. Origem do projeto e APK original

O arquivo `PVPPokeGo_V01.apk` foi usado como base de referência histórica/análise.

Dados recuperados do V01 e já documentados no projeto:

- package original: `com.tandembot.tandempvp`
- versão 0.1.0 / versionCode 1
- min SDK 29
- target/compile antigos 35
- possuía MainActivity e OverlayService

Depois de problemas com patch de APK/DEX, o projeto foi reescrito como código-fonte Android editável usando `com.lucianotoscano.pvppokego`.

O binário `PVPPokeGo_V01.apk` NÃO deve ser presumido como backup permanente dentro da branch atual. Ele deve ser preservado externamente ou futuramente anexado a uma Release de referência.

## 4. Fontes e referências externas usadas

Fonte principal de dados e lógica PvP: PvPoke.

Referências usadas ao longo do projeto:

- `https://pvpoke.com/`
- `https://pvpoke.com/battle/`
- `https://pvpoke.com/rankings/`
- `https://github.com/pvpoke/pvpoke`
- `https://pvpogo.streamlit.app/?lang=Brazilian`
- `https://rankedboost.com/pokemon-go/tier-list/` como fonte secundária/validação de stats gerais, NÃO como ranking PvP principal
- `partywhale/pokemon-type-icons` foi considerado como referência visual MIT para ícones de tipo; não confundir esses ícones com assets oficiais do Pokémon GO

O PvPoke é a referência principal para rankings por liga, movesets, tipos, energia, matchups e simulações.

`THIRD_PARTY_NOTICES.md` registra o uso de dados estruturados derivados do PvPoke e o aviso de licença MIT. Não copiar branding/artwork do PvPoke ou do Pokémon GO.

## 5. APKs/apps analisados como referência funcional

O contexto consolidado da main registra análises/referências de:

- Pokémon GO oficial
- PGSharp
- iPogo
- Poke Genie
- Pokemod
- PGSRoot
- PGTools
- Shungo
- Data Hub
- Pokecoord
- Pokemon Coords

Esses APKs/apps foram tratados como referência de UX/conceito/arquitetura. Não copiar código fechado, mecanismos proprietários ou assets sem licença.

## 6. Arquivos principais do projeto atual

Android/app:

- `app/src/main/java/com/lucianotoscano/pvppokego/MainActivity.kt`
- `app/src/main/java/com/lucianotoscano/pvppokego/capture/ScreenCapture.kt`
- `app/src/main/java/com/lucianotoscano/pvppokego/detect/BattleOcrDetector.kt`
- `app/src/main/java/com/lucianotoscano/pvppokego/detect/BattleFrameEventDetector.kt`
- `app/src/main/java/com/lucianotoscano/pvppokego/detect/BattlePresenceTracker.kt`
- `app/src/main/java/com/lucianotoscano/pvppokego/data/Models.kt`
- `app/src/main/java/com/lucianotoscano/pvppokego/data/BattleLeague.kt`
- `app/src/main/java/com/lucianotoscano/pvppokego/data/GameDataRepository.kt`
- `app/src/main/java/com/lucianotoscano/pvppokego/data/SettingsRepository.kt`
- `app/src/main/java/com/lucianotoscano/pvppokego/engine/EnergyTracker.kt`
- `app/src/main/java/com/lucianotoscano/pvppokego/engine/BattleStateStore.kt`
- `app/src/main/java/com/lucianotoscano/pvppokego/engine/BattleEngine.kt`
- `app/src/main/java/com/lucianotoscano/pvppokego/engine/BattleAssistAdvisor.kt`
- `app/src/main/java/com/lucianotoscano/pvppokego/engine/TypeChart.kt`
- `app/src/main/java/com/lucianotoscano/pvppokego/overlay/BattleOverlayService.kt`
- `app/src/main/java/com/lucianotoscano/pvppokego/overlay/BattleOverlayView.kt`
- `app/src/main/java/com/lucianotoscano/pvppokego/overlay/QuickOverlayMenu.kt`
- `app/src/main/assets/move_names_ptbr.json`

Core/testável:

- pasta `core/`
- visão/confiança, Battle Brain e testes devem permanecer desacoplados da UI Android sempre que possível

Dados:

- pasta `data/pvpoke/`
- assets PvPoke gerados/localizados usados offline durante a batalha

Documentação existente importante:

- `docs/CURRENT_DEVELOPMENT_HANDOFF.md`
- `docs/V05_COPILOT_HARDENING.md`
- `docs/FINAL_HUD_POSITION_MAP.md`
- `docs/NATIVE_HUD_PROTECTION.md`
- `docs/PVPOKE_INTEGRATION.md`
- `docs/V03_BASELINE_LOCK.md`
- `docs/quick-overlay-menu.md`
- na branch `main`: `docs/CONSOLIDATED_PROJECT_CONTEXT_2026-09-26.md`
- na branch `main`: `docs/VISUAL_CONTRACT_2026-09-26.md`

## 7. Captura, OCR e detecção de batalha

A captura usa `MediaProjection`.

O overlay usa `TYPE_APPLICATION_OVERLAY`.

OCR usa Google ML Kit Text Recognition e ROIs específicas. Não fazer OCR full-screen contínuo sem necessidade.

O `BattlePresenceTracker` combina evidências visuais e OCR para decidir se uma batalha está realmente ativa, evitando deixar o HUD aparecendo fora da batalha.

Evidências usadas incluem:

- barras/estrutura visual do PvP
- cards brancos superiores
- jogador + oponente reconhecidos
- texto de batalha
- prompt de troca

O HUD deve desaparecer quando não houver batalha real.

## 8. Regra crítica da contagem de ataques

`frame != ataque`.

Nunca incrementar Fast Move só porque chegou um novo frame ou porque o jogador atacou.

A contagem do inimigo deve reagir a evidência visual do ataque do inimigo, principalmente dano/queda de HP do jogador, com duração/debounce do Fast Move.

A energia do jogador deve ser inferida separadamente, principalmente pela queda de HP do adversário.

Os dois lados não podem compartilhar o mesmo evento de contagem.

## 9. Energia do inimigo

Cada Pokémon inimigo possui memória independente.

Regras:

- troca não zera energia
- quando ele volta, energia/contagem são restauradas
- Fast Move soma energia
- Charged Move confirmado desconta o custo real
- preservar energia residual
- se OCR confirma que houve Charged mas não identifica qual golpe, trabalhar com faixa min/max em vez de inventar um custo
- UI pode mostrar `CONFIRMADA`, `ESTIMADA` ou `FAIXA`

Os dois Charged Moves usam a mesma reserva de energia do Pokémon. Portanto ambos podem progredir ao mesmo tempo, cada um conforme seu próprio custo.

## 10. Energia do próprio jogador

V05 adicionou modelo de energia própria separado do inimigo.

Objetivo do Assistido:

- `+1 FAST`
- `USE <GOLPE>`
- `SEGURE ENERGIA`
- `TROQUE`
- `ESCUDO`
- `NÃO ESCUDE`
- aviso de bait/nuke quando possível

Esse sistema é recomendação visual; não toca no jogo.

## 11. Movesets e confirmação de golpes

No início, o app usa moveset provável/rankeado pelo PvPoke.

UX conceitual:

- `PREVISTO #1`
- `✓ CONFIRMADO`

Quando OCR observa um banner como `Pokémon usou Golpe!`, o app deve:

- identificar o moveId
- confirmar/corrigir slot
- descontar custo real
- preservar energia residual
- recalcular os dois Charged
- deduplicar frames repetidos do mesmo banner

Escolha manual do usuário não deve ser sobrescrita automaticamente.

O asset pt-BR atual é `app/src/main/assets/move_names_ptbr.json`. Exemplo verificado atual: `BODY_SLAM = Pancada Corporal`.

## 12. HUD visual — regra canônica

O layout deve parecer integrado ao Pokémon GO e NÃO virar painel central grande.

Regra visual mais importante dada pelo usuário:

**nenhum elemento do PvPPokeGo pode ficar em cima de elementos nativos importantes do Pokémon GO.**

Não cobrir:

- nome do Pokémon
- CP/PC
- HP
- Pokébolas do time
- escudos
- ícones de tipo nativos
- botões de Fast/Charged do jogador
- áreas importantes de toque

Os screenshots oficiais/limpos fornecidos pelo usuário foram usados como regra visual. Como os uploads de chat não são garantidos em novos chats, as regras derivadas deles foram convertidas para documentação/código.

## 13. Topo da batalha

O usuário confirmou que os ícones extras do overlay no topo devem ficar na área livre próxima aos cards do Pokémon GO, sem se esconder atrás da barra de status e sem cobrir os ícones nativos.

V05 usa ancoragem dinâmica baseada no topo real dos cards brancos quando possível.

Os ícones de tipo do próprio Pokémon GO podem ocupar 1 ou 2 posições; esse espaço deve ser tratado como reservado ao jogo.

O ícone de matchup, tipos fortes e demais extras devem respeitar essa área nativa.

## 14. Ataques inimigos

Layout atual desejado:

- Fast Move em cima
- Charged 1 embaixo à esquerda
- Charged 2 embaixo à direita

Design:

- circular, parecido com linguagem visual dos ataques do próprio jogador
- parte ainda não carregada escura
- energia sobe de baixo para cima com cor do tipo
- símbolo de tipo no centro
- nome abaixo
- `▼` para trocar manualmente
- contador de Fast Moves restantes
- pontos/progresso opcionais

Submenu de golpes:

- pequeno/discreto
- sem grande background preto atrás
- cada opção usa cor do tipo com transparência moderada
- mostra nome e dados necessários

## 15. Reservas / Pokémon 2 e 3

As duas reservas devem ser tratadas como blocos independentes.

Cada uma pode mostrar:

- matchup favorável/neutro/desfavorável
- tipos
- recomendação `MELHOR`, `NEUTRO` ou `EVITAR`

Na tela `Quer trocar por um Pokémon novo?`, o indicador deve aparecer relacionado a cada opção real mostrada pelo jogo.

Reservas não devem desaparecer por uma leitura OCR/visual isolada. Há cache/histerese para reduzir sumiço falso.

## 16. Quick menu da overlay

Existe `QuickOverlayMenu.kt`.

Comportamento desejado:

- engrenagem flutuante apenas com ícone, sem texto externo
- menu abre dentro da overlay
- menu interno pode mostrar ícone + texto, estilo compacto semelhante à referência do PGSharp
- opção para `Somente ícones`
- menu básico e avançado
- ao escolher uma opção, manter posição da rolagem e não voltar automaticamente ao topo
- engrenagem pode ser arrastada e tem posição independente

A engrenagem NÃO deve ficar colada ao olho, cadeado ou ícone da liga.

## 17. Olho / ocultar HUD

O olho é controle independente.

Regra desejada:

- toque no olho oculta TUDO do PvPPokeGo
- só o próprio controle mínimo de olho deve permanecer para restaurar
- esconder também engrenagem, badge da liga, cadeado, ataques, reservas, textos, assistente e demais blocos
- análise pode continuar em segundo plano
- ocultar o HUD não deve parar a captura/estado da batalha

## 18. Cadeado / arraste livre

Foi adicionado cadeado global independente, visualmente no mesmo estilo do olho, porém sem ficar colado.

Semântica:

- cadeado fechado = posições protegidas
- cadeado aberto = todos os blocos principais podem ser arrastados livremente
- olho e cadeado também podem ter posições independentes

`SettingsRepository.layoutLocked` é o lock global.

Commit importante que corrige o problema relatado de os ícones permanecerem travados:

`66e40a9247bee74b489dd724e630ac7f5014a18e` — `fix: unlock all HUD blocks for free drag`

Esse commit faz o cadeado global virar a fonte principal de verdade e ignora locks individuais enquanto o lock global estiver aberto.

O build de verificação desse reparo é o run `#78`, concluído com sucesso.

Status de validação: build passou, mas ainda precisa de confirmação em aparelho de que todos os elementos realmente arrastam conforme esperado.

## 19. Modo edição avançado

Além do cadeado global, existe editor avançado/legado com:

- arrastar bloco
- escala `- / +`
- opacidade por bloco
- esconder bloco
- lock individual
- desfazer posição
- salvar/sair
- resetar customização

Quando possível, a experiência normal deve preferir o cadeado global simples. O modo edição avançado fica para ajustes finos.

## 20. Ícone da liga e reconhecimento automático

O badge da liga é independente da engrenagem e pode ser arrastado.

Referência visual escolhida pelo usuário: emblemas triangulares inspirados nos modos Grande/Ultra/Mestra mostrados em screenshot, sem copiar asset oficial diretamente.

Estados:

- Grande
- Ultra
- Mestra
- Automático

Detecção AUTO usa OCR de textos como limites de PC/CP e também fallback por CPs reconhecidos durante a batalha.

`BattleLeague.kt` contém `BattleLeagueTextDetector`.

CP cap interno:

- Grande = 1500
- Ultra = 2500
- Mestra = valor interno 10000 para representar sem limite prático do modo

## 21. Timer de troca

O timer foi transformado em contador de cooldown aproximado de troca quando o app detecta mudança do Pokémon do jogador ou do adversário.

Objetivo:

- `SUA TROCA`
- `TROCA INIMIGA`

Se não houver detecção confiável da troca, não apresentar o timer como fato exato.

## 22. Battle Assist / Copilot

Primeira camada do Assistido já implementada.

Pode recomendar qualitativamente:

- continue Fast
- troque
- escude
- não escude
- escudo opcional
- atenção para energia adversária

V05 adicionou recomendações baseadas em energia própria e provável moveset.

Princípio: mostrar incerteza quando não houver evidência suficiente.

## 23. Shield Advisor

Direção implementada/planejada:

- analisar possíveis Charged do inimigo
- efetividade
- custo/energia
- bait barato vs nuke
- risco para o Pokémon ativo

Mensagens possíveis:

- `PODE SER BAIT`
- `ESCUDO`
- `NÃO ESCUDE`
- `ESCUDO OPCIONAL`

## 24. Histórico do adversário

Há suporte opcional para histórico compacto por inimigo:

- Fast count
- energia/faixa
- último Charged observado

O histórico deve sobreviver a troca e retorno do mesmo Pokémon durante a batalha.

## 25. Android moderno

Projeto atualizado para:

- compileSdk 36
- targetSdk 36
- Android Gradle Plugin 8.10.1
- Gradle 8.11.1 em CI
- Java 17

Objetivo: eliminar avisos de app antigo e melhorar comportamento moderno de overlays/insets.

## 26. Regras de segurança visual

Nunca assumir coordenadas fixas como universais entre aparelhos.

Preferir:

- ancoragem dinâmica
- insets reais do sistema
- posição manual salva pelo usuário
- elementos independentes

Quando o usuário arrasta manualmente, essa posição deve prevalecer sobre o default automático.

## 27. Dados e licença dos ícones de tipo

O app atualmente também possui glyphs/desenhos procedurais de tipo.

Não afirmar que os ícones desenhados são assets oficiais do Pokémon GO.

Se usar projeto MIT externo para ícones, preservar licença e evitar assets cuja licença não esteja clara.

## 28. RankedBoost

A página `rankedboost.com/pokemon-go/tier-list/` pode ser usada somente como fonte auxiliar de stats gerais/consulta.

Não usar como ranking PvP principal porque CP máximo e stats brutos não substituem ranking por liga, bulk, moveset, energia e matchup.

PvPoke permanece a fonte principal para PvP.

## 29. Build e artefatos

Workflow principal:

`.github/workflows/android-debug.yml`

Atualmente o workflow guarda artifact por 14 dias.

Para continuidade de longo prazo, manter código e documentação no GitHub e criar Releases/tags para APKs estáveis importantes.

O APK baixado em um chat/local `/mnt/data` NÃO deve ser tratado como armazenamento permanente entre conversas.

## 30. Ordem recomendada para qualquer novo chat

1. Abrir o repo `LucianoToscano/PvPPokeGo`.
2. Usar a branch `v04-v03-stable-base`.
3. Ler este arquivo inteiro.
4. Ler `docs/CURRENT_DEVELOPMENT_HANDOFF.md`.
5. Ler `docs/V05_COPILOT_HARDENING.md`.
6. Se houver questão visual, ler `docs/FINAL_HUD_POSITION_MAP.md` e `docs/NATIVE_HUD_PROTECTION.md`.
7. Se houver questão de dados, ler `docs/PVPOKE_INTEGRATION.md` e `THIRD_PARTY_NOTICES.md`.
8. Para histórico completo anterior, consultar na `main` `docs/CONSOLIDATED_PROJECT_CONTEXT_2026-09-26.md` e `docs/VISUAL_CONTRACT_2026-09-26.md`.
9. Conferir o head atual da branch e a última GitHub Action antes de editar.
10. Não regredir funções já aprovadas para resolver um novo detalhe.

## 31. Prioridades de teste atuais

Ao continuar o projeto, validar primeiro em aparelho real:

- cadeado aberto realmente permite mover TODOS os blocos
- cadeado fechado realmente impede movimentação acidental
- olho realmente oculta tudo e restaura tudo
- engrenagem, olho, cadeado e badge da liga são independentes
- badge de liga reconhece corretamente Grande/Ultra/Mestra
- topo não fica atrás da barra de status e não cobre os ícones nativos
- ataques inimigos só sobem quando o inimigo ataca
- energia própria só sobe quando o jogador ataca
- Charged inimigo desconta o custo correto
- reservas não somem falsamente
- menu interno mantém scroll após mudar uma opção
- HUD some fora da batalha

## 32. Último problema reportado antes deste handoff

O usuário relatou: `Os icones estao travados.. nao consigo mover..`.

Foi aplicado o reparo no commit `66e40a9247bee74b489dd724e630ac7f5014a18e` e o build de verificação `#78` passou.

A próxima sessão deve perguntar apenas pelo resultado do teste em aparelho ou continuar corrigindo se ainda houver bloco não arrastável. Não reimplementar todo o sistema do zero.

## 33. Regra de continuidade

Não depender da memória de um chat.

Qualquer decisão importante, arquivo usado, regra visual, bug confirmado ou mudança estrutural deve ser registrada neste arquivo, no `CURRENT_DEVELOPMENT_HANDOFF.md`, em documentação específica ou em commits claros.

O GitHub é a fonte persistente do projeto.
