# PvPPokeGo — Contexto Consolidado dos Chats e Roadmap Canônico

Atualizado em: 2026-09-26

Este documento consolida o contexto técnico, funcional, visual e de produto que estava espalhado entre os chats **“APK pronto para baixar”**, **“Funções Para Dois APKs”** e o tópico principal do **Projeto PvPPokeGo**.

A partir desta consolidação, este arquivo deve ser tratado como a memória canônica do projeto para evitar perda de decisões, ideias, descobertas e trabalho já realizado.

---

## 1. Objetivo do PvPPokeGo

O PvPPokeGo é um aplicativo Android de assistência em batalhas do Pokémon GO, com foco inicial em PvP/GO Battle League e arquitetura extensível para Rocket, raids/ginásios e Max Battles.

O núcleo do projeto deve atuar como um **copiloto de batalha**:

`captura -> ROI -> OCR/visão -> estabilização -> Battle State -> Energy Tracker -> inferência de golpes -> Battle Brain -> Recommendation Engine -> overlay/vibração/voz`

O core de decisão deve permanecer separado da camada Android, OCR e qualquer executor de ações, para ser testável isoladamente.

---

## 2. Origem e reescrita do projeto

O `PVPPokeGo_V01.apk` original foi analisado como referência de comportamento.

Dados recuperados da versão inicial:

- package original: `com.tandembot.tandempvp`;
- versão: `0.1.0`, versionCode `1`;
- min SDK 29;
- target/compile SDK 35;
- `MainActivity`;
- `OverlayService`;
- permissões incluíam overlay, foreground media projection, notificações, internet, billing, estado de rede e `QUERY_ALL_PACKAGES`.

Foi tomada a decisão de **abandonar patches agressivos de DEX** depois de falhas como `ClassNotFoundException` para `TandemPvpApp`.

O projeto foi reescrito em código-fonte editável/testável usando o novo package:

`com.lucianotoscano.pvppokego`

Isso permite instalação lado a lado e evita depender de remendos frágeis em APK compilado.

A fundação reescrita removeu dependências desnecessárias como billing e `QUERY_ALL_PACKAGES`.

---

## 3. Arquitetura Android base

Arquitetura principal definida:

- `MainActivity` / Compose / Settings;
- `capture/ScreenCapture.kt`;
- `detect/BattleOcrDetector.kt`;
- `data/GameDataRepository.kt`;
- `engine/EnergyTracker.kt`;
- `engine/BattleStateStore.kt`;
- `engine/BattleEngine.kt`;
- `engine/TypeChart.kt`;
- `overlay/BattleOverlayService.kt`;
- `overlay/BattleOverlayView.kt`.

A captura visual usa `MediaProjection`.

O overlay usa `TYPE_APPLICATION_OVERLAY`.

A camada de visão deve trabalhar com ROIs pequenas e específicas, não OCR da tela inteira.

---

## 4. Pipeline de visão e confiança

Já foi criada a base em:

`core/src/main/kotlin/pvppokego/core/vision/VisionObservation.kt`

Conceitos implementados:

- `ObservationSource`;
- `FieldObservation<T>` com confiança independente por campo;
- `BattleFrameObservation`;
- `StableObservation<T>`;
- `StableObservationReducer<T>`.

Regra fundamental:

**frame não equivale a ataque**.

A contagem automática de Fast Move não deve incrementar apenas porque frames foram recebidos. Ela precisa de ROI/eventos visuais validados e correção manual quando necessário.

Reservas/oponentes também não devem ser considerados reconhecidos com confiança apenas por OCR genérico.

---

## 5. Dados PvP e PvPoke

Referências fornecidas para o projeto:

- https://pvpoke.com/
- https://pvpoke.com/battle/
- https://pvpoke.com/rankings/
- https://pvpogo.streamlit.app/?lang=Brazilian
- https://github.com/pvpoke/pvpoke

O PvPoke é a principal referência aberta para:

- Pokémon e formas;
- tipos;
- Fast Moves;
- Charged Moves;
- geração/custo de energia;
- rankings de Grande, Ultra e Mestre;
- simulação de batalha;
- matchups;
- Team Builder;
- lógica de combate e timelines.

O repositório oficial `pvpoke/pvpoke` está vinculado como submódulo em:

`third_party/pvpoke`

O snapshot inicialmente fixado foi o commit:

`9dab4bcc8ce8cf201cca34c7632688674084eb16`

A branch oficial é `master`.

A licença do código do PvPoke é MIT, com os avisos preservados em `THIRD_PARTY_NOTICES.md`.

O projeto já possui:

`tools/update_pvpoke_data.py`

para gerar dados locais do PvPoke. A batalha deve usar dados offline/localmente durante a execução, sem depender de chamadas ao site.

Snapshot histórico importado anteriormente:

- rankings 1500: 1128 entradas;
- rankings 2500: 830 entradas;
- rankings 10000: 400 entradas.

Essas contagens são históricas e podem mudar após sincronizações futuras.

Componentes upstream particularmente relevantes:

- `GameMaster.js`;
- `Battle.js`;
- `ActionLogic.js`;
- lógica de rankings/TeamRanker;
- gamemaster de Pokémon/moves/formats.

---

## 6. Modelos e dados preservados do projeto inicial

Conceitos/estruturas que devem continuar sendo considerados:

- `PokemonDef`;
- `MoveDef`;
- `GameMaster`;
- `RankingEntry`;
- `PokemonRepo`;
- `Analysis`;
- `Team`;
- `TeamHistoryRepo`;
- histórico local de times/batalhas;
- preferências de liga/exibição;
- aliases localizados de golpes.

As preferências antigas incluíam conceitos como:

- `display_mode`;
- `league_cp`, padrão 1500;
- `is_premium`.

Não há obrigação de manter o modelo comercial antigo; esses nomes são preservados como contexto histórico/compatibilidade.

---

## 7. BattleEnergyTracker e memória dos três inimigos

O `BattleEnergyTracker` deve manter três estados independentes para os três Pokémon adversários.

Regras:

- troca não zera energia;
- energia residual é preservada após Charged Move;
- quando o Pokémon volta, o estado anterior é restaurado;
- slot é encerrado somente por desmaio/fim da partida;
- Fast Move pode ser corrigido manualmente;
- Charged Move 1/2 pode ser corrigido manualmente;
- os dois carregados mantêm ordem correspondente aos dois ícones/botões do overlay.

O sistema também deve preservar golpes confirmados mesmo após mudanças de hipótese/previsão.

---

## 8. Golpe previsto versus confirmado

Decisão de UX e lógica:

No início, o sistema pode selecionar o moveset PvPoke mais provável/rankeado como:

`PREVISTO #1`

Quando um golpe é efetivamente observado, deve passar a:

`✓ CONFIRMADO`

Regras:

- um Charged Move confirmado não deve ser apagado por nova previsão;
- um segundo Charged Move diferente ocupa o outro slot;
- tocar no ícone do golpe abre seletor manual;
- o seletor deve mostrar apenas golpes legais daquele Pokémon/forma;
- pode mostrar tipo, custo de energia e informação de legado quando disponível;
- Fast Move também começa por previsão e pode ser alterado manualmente ou por inferência futura.

O OCR de banner de batalha deve mapear texto localizado para `moveId` local.

Exemplos já previstos no mapeamento:

- `HYDRO_CANNON`;
- `ICE_BEAM`;
- `CRUNCH`;
- `EARTHQUAKE`;
- `BODY_SLAM`.

---

## 9. Charged Move por OCR do banner

`BattleTextEventParser` e `BattleObservationProcessor` devem interpretar banners como:

`Plusle usou Relâmpago!`

Ao confirmar o Charged Move:

- subtrair custo da energia estimada;
- preservar energia residual;
- recalcular imediatamente o número de Fast Moves necessários para os próximos Charged Moves;
- deduplicar o mesmo banner observado em frames consecutivos.

Aliases em português ficam em:

`data/localization/moves_pt_BR.tsv`

com carregamento por `MoveAliasTable`.

---

## 10. Overlay visual canônico

O overlay do adversário deve manter o conceito definido nas imagens/guias do projeto:

### Triângulo de golpes

- Fast Move no topo;
- Charged Move 1 abaixo à esquerda;
- Charged Move 2 abaixo à direita.

Cada golpe deve mostrar:

- ícone;
- nome abaixo do ícone;
- seta `▼` quando houver seleção manual;
- contador de Fast Moves restante ao lado do Charged Move;
- progresso visual circular de energia.

Exemplo de layout:

`INIMIGO`

`[Fast Move ▼]`

`[Charged 1 ▼] 3`

`[Charged 2 ▼] 5`

Exemplos já usados nos mockups:

- `Ataque de Asa`;
- `Nitrocarga`;
- `Superaquecimento`.

Quando um Charged Move estiver pronto:

- contador = `0`;
- ícone/progresso totalmente preenchido.

Quando ainda estiver carregando:

- círculo parcialmente preenchido.

---

## 11. Painel principal do overlay

Itens definidos para o painel:

- título `PvP Assistant`;
- seu Pokémon;
- oponente;
- CP;
- tipos;
- seção `TIPOS CONTRA O OPONENTE`;
- seção `INFORMAÇÕES DO OPONENTE`;
- energia estimada, por exemplo `42 / 100`;
- `SUA TROCA`;
- `TROCA INIMIGA`;
- `HISTÓRICO DO OPONENTE`;
- indicadores `SUPER EFETIVO`, `RESISTE A`, `SEM EFEITO`;
- barras/ícones de matchup;
- escala do overlay;
- opacidade;
- debug.

O overlay normal permanece passivo para não bloquear os toques do jogo.

Pequenas áreas específicas dos seletores `▼` podem ser interativas quando a camada de UI correspondente estiver implementada.

---

## 12. Times e reservas

O projeto deve preservar o conceito de time de três Pokémon e histórico dos adversários.

Estados visuais planejados para reservas/time:

- `✓` disponível/confirmado;
- `•` estado intermediário/ativo conforme contexto;
- `×` derrotado/indisponível.

Se o OCR não reconhecer uma reserva com confiança, deve haver seleção/correção manual.

Reconhecimento visual confiável das reservas ainda é pendente.

---

## 13. Referências de APKs analisados

Fontes analisadas como referências funcionais/arquiteturais:

- Pokémon GO oficial 0.429.1 APKM;
- PGSharp 1.271;
- arquivo LOT equivalente ao PGSharp analisado;
- iPogo 237;
- Poke Genie 8.15.0;
- Pokemod 13.3.4;
- PGSRoot 1.0.15;
- PGTools 6.0.6 + TEST;
- Shungo 1.7.0;
- Data Hub 2.5.2;
- Pokecoord;
- Pokemon Coords.

### Baseline oficial

Pokémon GO oficial 0.429.1:

- package `com.nianticlabs.pokemongo`;
- versionCode `2026091602`;
- ARM64;
- Android 9+.

Hashes registrados:

- APKM oficial: `af397f568a929d37e8c46d3cd9f6bdcf1a1a908bc18c4b85cebf17cf78251ad9`;
- base.apk oficial: `5c6cc61a6752c42f6114845b45cb6d4d2568dbb9a7eab34af290706a86f8d29e`;
- split ARM64 oficial: `70364d9968ce14c4c5f450a99e76ca9f9491ba7732db7df2a5df540c58fd90c4`.

O hash:

`df4d00f24f4165063715eb7890fb331bc55acc04a12c090173e02c354832ac0c`

corresponde ao PGSharp 1.271 sem root/arquivo LOT analisado, não ao Pokémon GO oficial.

---

## 14. Poke Genie — principal referência externa de UX/visão

Elementos identificados:

- `IVisionHelper`;
- `IVisionView`;
- `OcrHelper`;
- `OverlayMenuView`;
- `BattleTeamOverlayView`;
- `BattleSimulatorManager`;
- `SimulationBestCounterActivity`;
- `SimulationAllMovesActivity`;
- `BreakpointActivity`;
- `PvPIvChartActivity`;
- `PvpCandidateActivity`;
- movesets;
- Pokédex;
- efetividade de tipos;
- histórico;
- rename;
- backup;
- correções OCR letras/números.

Aplicação ao PvPPokeGo:

- captura/MediaProjection;
- OCR especializado por ROI;
- overlay modular;
- Battle Simulator;
- PvP IV;
- candidatos;
- moveset reference;
- histórico/diagnóstico;
- correção rápida;
- perfis/layouts.

Não copiar código fechado, modelos proprietários ou mecanismos premium.

---

## 15. iPogo — evidências úteis

Strings/funções encontradas no `libmain.so` modificado incluíram:

- `iPogo`;
- `setLocationNative`;
- `set_autoconnectvgp`;
- `set_shinyscanner`;
- `set_skip_animations`;
- `set_skip_team_leader`;
- `set_skip_team_rocket`;
- `set_fastmap`;
- `set_boostspawn`;
- `set_freezepokemon`;
- `set_spin`;
- `set_throw`;
- `set_arplus`;
- `set_challenge`;
- `release_pokemon`;
- `update_shinies`;
- `get_player_stats`;
- `individual_attack`;
- `individual_defense`;
- `individual_stamina`;
- `excellent`.

Para PvPPokeGo, essas evidências servem principalmente para identificar conceitos/estados. Localização, farm, VGP e manipulação de captura pertencem ao PGLOT, não ao core PvP.

---

## 16. Pokemod — referências aproveitáveis

Elementos encontrados incluem:

- Account Manager;
- contas nomeadas;
- reorder/delete/one-tap switching;
- vPGP;
- Bluetooth;
- Injection Settings;
- Shiny Scanner;
- alertas Legendary/Shundo;
- Teleport;
- logs/recording;
- atualizações/compatibilidade;
- Discord Rich Presence;
- configurações root/SELinux.

Para PvPPokeGo, aproveitar apenas conceitos genéricos como:

- perfis;
- logs;
- notificações;
- separação modular;
- diagnóstico.

Root/injeção/concealment não entram no core PvPPokeGo.

---

## 17. PGTools — funções analisadas

PGTools 6.0.6 confirmou módulos separados:

- `PaC`: catch/farm;
- `PaG`: Rocket;
- `PaS`: Shiny Hunter;
- `PaH`: ovos;
- `PaR`: raids.

Também foram observados conceitos de:

- Hundo/Shundo/IV0/IV customizado;
- filtros;
- transferência;
- quests;
- presentes;
- buddy;
- Mega;
- cura;
- itens;
- stats;
- scheduler/configuração;
- notificações.

Aplicação permitida ao PvPPokeGo:

- filtros genéricos;
- IV/stats;
- modelos de configuração;
- logs;
- notificações;
- dados de Rocket/Raid para recomendação;
- engine de prioridade.

Ficam fora do core PvP:

- auto farm;
- teleporte;
- hunt por coordenadas;
- transferência automática;
- executor automático de raid/Rocket;
- root/injeção.

---

## 18. Shungo 1.7.0 — referência

Foram observados conceitos como:

- `getPokemons`;
- latitude/longitude;
- `setLocation`;
- hunter baseado em alvo/localização.

Para PvPPokeGo, reaproveitar somente conceitos abstratos de:

- filtro de alvo;
- prioridade;
- regras de seleção;
- notificações.

Hunter/localização pertencem ao PGLOT.

---

## 19. Descobertas específicas de batalha em PGSharp/LOT

Evidências textuais registradas:

- `instantRocket`;
- `rocket.fast`;
- `rocket.charge`;
- `rocket.shield`;
- `CombatVNextBattleStateShim`;
- `battleAssistant`;
- `GymBattleState`;
- `RaidLobbyState`;
- `GymRootState`;
- `breadBattleDodge`;
- `breadBattleOrb`;
- `breadBattleFast`;
- `breadBattleCharge1`;
- `Minigame`;
- `StartBattle`;
- `SwapPokemon`;
- `AttackTelegraph`;
- `ActorMove`;
- `BattleEnd`;
- `Charge2`;
- `SpecialCharge`.

Essas referências originaram o documento:

`docs/PGSHARP_BATTLE_FINDINGS.md`

Commit histórico correspondente:

`11375465bad0990043e63d2bae66a1fb5bec8a54`

A string `enhancedThrowExcellent` pertence ao lançamento de captura e **não** é evidência do minigame Excelente do Charged Move.

---

## 20. Battle State Detector planejado

Estados que o detector deve conseguir classificar:

- fora de batalha;
- início;
- batalha ativa;
- Charged Move disponível;
- prompt de escudo;
- troca;
- minigame do Charged Move;
- fim da batalha;
- lobby de raid;
- Rocket;
- PvP;
- raid/ginásio;
- Max Battle.

---

## 21. Núcleo de automação/intents já concebido

O core possui/prevê configurações independentes:

- `autoFastAttack`;
- `autoChargedAttack`;
- `autoShield`.

Intents de decisão:

- `TapFastAttack`;
- `TapChargedAttack(buttonIndex, ...)`;
- `TapShield`.

Também existe a ideia de seleção de Charged Move por `buttonIndex`.

Esses intents são parte do **motor de decisão**. A camada Android de execução/gestos não deve ficar acoplada ao core.

Modos de produto concebidos:

- `ASSIST`: recomendações;
- `SEMI_AUTO`: conceito de fast automático com decisões sensíveis manuais;
- `AUTO_BATTLE`: conceito experimental de automação completa de batalha.

No estado atual do aplicativo, o runtime principal continua sendo um assistente externo/visual e a execução automática não deve ser considerada concluída.

---

## 22. ChargedMinigameSolver — conceito preservado

Foi proposta uma camada separada:

`ChargedMinigameSolver`

Responsabilidades conceituais:

- reconhecer qual minigame de Charged Move está na tela;
- estimar trajetória/ordem dos elementos;
- produzir instrução de gesto para maximizar a pontuação/Excelente.

Esse módulo deve permanecer separado do Battle Brain e do reconhecimento geral.

---

## 23. Max Battles

As referências analisadas mostraram ações distintas:

- Fast;
- Charged;
- Dodge;
- Orb.

Portanto Max Battle deve possuir módulo/state machine própria e não ser misturado com PvP/Rocket/Raid tradicional.

---

## 24. Battle Brain — evolução principal

O projeto deve evoluir de simples contador para um verdadeiro copiloto em tempo real.

O Battle Brain deve considerar:

- Pokémon ativo de ambos;
- HP aproximado;
- energia;
- escudos;
- golpes previstos/confirmados;
- Fast Moves observados;
- Pokémon já revelados;
- timers de troca;
- matchup;
- win condition;
- nível de confiança da visão.

Saídas possíveis:

- atacar;
- segurar energia;
- usar Charged 1;
- usar Charged 2;
- trocar;
- escudar;
- não escudar;
- sacrificar/catch;
- preservar Pokémon-chave.

---

## 25. Ideias funcionais que devem permanecer no roadmap

### 25.1 Detecção de bait

Calcular probabilidade do adversário estar usando golpe barato/bait versus golpe forte, usando energia, moveset, shields, HP, matchup e comportamento já observado na partida.

### 25.2 Contagem dupla/tripla de Charged Moves

Exibir simultaneamente quantos Fast Moves faltam para cada possível carregado.

Exemplo:

- `Nitrocarga: PRONTO`;
- `Superaquecimento: +3`.

### 25.3 Move Timing

Indicar o melhor momento/turno para disparar Charged Move reduzindo energia grátis dada ao adversário.

Exemplo:

- `AGORA ✓`;
- `Espere +1 Fast Move`.

### 25.4 Sac Swap / Catch Assist

Indicar janela aproximada de troca para tentar absorver o Charged Move em outro Pokémon.

### 25.5 Relógios de troca

Mostrar separadamente:

- `SUA TROCA`;
- `TROCA INIMIGA`.

### 25.6 CMP Predictor

Estimar Charged Move Priority quando houver dados suficientes de espécie/CP/IV/ataque.

Saídas:

- `CMP: VOCÊ`;
- `CMP: ADVERSÁRIO`;
- `INCERTO`.

### 25.7 Win Condition

Detectar condição necessária para vencer.

Exemplos:

- preservar um Pokémon específico;
- guardar energia;
- não gastar o último escudo;
- alinhar determinado matchup.

### 25.8 Reconhecimento dos três adversários

Salvar slot, energia, golpes e estado quando um Pokémon sai e restaurar ao retornar.

### 25.9 Inferência dinâmica de Fast Move

Quando uma espécie tiver múltiplos Fast Moves possíveis, usar timing/observações para migrar de previsão para confirmação.

### 25.10 Memória comportamental da partida

Registrar padrões como bait, tendência de shield e momento das trocas apenas dentro daquela batalha.

### 25.11 Importação do inventário real

Importar `PGSStats.json` para que Team Builder e recomendações saibam quais Pokémon o usuário realmente possui.

### 25.12 Team Builder inteligente

Usar PvPoke + inventário local para gerar times considerando:

- lead;
- safe swap;
- closer;
- cobertura;
- core breakers;
- fraquezas compartilhadas;
- Elite TM;
- disponibilidade real no inventário.

### 25.13 Pré-batalha automática

Reconhecer os três Pokémon selecionados antes da partida e carregar automaticamente perfil/time correto.

### 25.14 Battle Recorder

Registrar eventos relevantes:

- Pokémon;
- Fast Moves;
- Charged Moves;
- shields;
- swaps;
- energia estimada;
- resultado.

### 25.15 Análise pós-batalha

Exemplos de feedback:

- shield gasto cedo;
- Fast Moves entregues ao adversário;
- Charged Move lançado um turno tarde;
- oportunidade de catch perdida.

### 25.16 Modo treinador

Sinais visuais, vibração e/ou voz:

- `ESCUDO`;
- `TROCA`;
- `CARREGADO 2`;
- `SEGURE`.

### 25.17 Overlay adaptativo

Mostrar apenas a informação importante naquele momento para evitar poluição visual.

### 25.18 Correção manual rápida

Controles rápidos:

- `+1` Fast;
- `-1` Fast;
- trocar Fast Move;
- trocar Charged Move;
- corrigir Pokémon;
- ressincronizar energia.

### 25.19 Confidence Score

Toda recomendação baseada em visão deve conhecer a confiança da entrada.

Exemplos:

- Pokémon 99%;
- Fast Move 86%;
- energia estimada;
- golpe confirmado versus provável.

Com confiança baixa, evitar recomendações agressivas.

### 25.20 Perfis por Liga/Copa

Perfis distintos para:

- Grande Liga;
- Ultra Liga;
- Mestre;
- Copas especiais.

Cada perfil pode alterar ranking, layout, prioridade e movesets.

---

## 26. Prioridades estratégicas

As cinco prioridades de maior impacto definidas foram:

1. `Battle Brain`;
2. `MoveTimingEngine` / timing de Charged Move;
3. `Sac Swap / Catch Assist`;
4. `Win Condition`;
5. importação do inventário via `PGSStats.json`.

Antes disso, é indispensável estabilizar reconhecimento visual/energia, pois o cérebro não pode recomendar corretamente sobre dados inconsistentes.

---

## 27. Engines/módulos-alvo

- `BattleSimulator`;
- `BreakpointEngine`;
- `PvPIvRankEngine`;
- `MoveTimingEngine`;
- `CounterAdvisor`;
- `BattleBrain`;
- `OpponentMoveInference`;
- `BattleStateDetector`;
- `BattleRecorder`;
- `BattleReplayTimeline`;
- `CollectionVisionScanner`;
- `StorageSearchGenerator`;
- `ChargedMinigameSolver`;
- `TeamBuilder`;
- `WinConditionEngine`;
- `CmpPredictor`.

---

## 28. PvP IV e simulação

O `PvPIvRankEngine` deve considerar:

- todas as 4096 combinações de IV;
- CP cap;
- level cap;
- stat product;
- ranks empatados;
- evoluções;
- formas;
- Megas quando aplicável ao módulo de análise;
- IV floors configuráveis;
- custos Stardust/candy/XL quando útil.

O `BattleSimulator` deve permanecer desacoplado do Android/OCR para poder ser comparado com resultados conhecidos do PvPoke e outros oracles open source.

---

## 29. Referências adicionais open source já levantadas

Além do PvPoke:

- `sikora-pawel/pvpoke` — pipeline mobile/offline;
- `cartolanofacundo/pokego-pvp` — combate 3x3, counters, turnos, times, IV rank;
- `mglerner/gopvpsim` — simulação/breakpoints/bulkpoints;
- `Mygod/ohbem` — rank IV otimizado;
- `pedrosancao/pokemon-go-pvp-rank`;
- `jgmcelwain/go-iv`;
- `Sigafoos/iv`;
- `DeathbyToast/PvP_IVs`;
- `ingimar-eyfjord/iv-pokemon-scanner`;
- `AlansApps/PGO-Scanner`;
- `PGSS/TheRaidMapper` como referência histórica de OCR;
- `PokeMiners/game_masters` para atualização de dados;
- `PokeMiners/pogo_assets` apenas com atenção a direitos/licença;
- `JP-study-list/poke-change` para pipeline de rebuild/import/export.

---

## 30. Integração com PGSStats.json

Objetivo:

O PvPPokeGo deve poder importar o `PGSStats.json` fornecido pelo usuário para criar uma base local da coleção.

Isso permitirá:

- saber Pokémon disponíveis;
- IV;
- CP;
- nível quando disponível;
- filtros por liga;
- candidatos para investimento;
- geração de times apenas com Pokémon realmente possuídos;
- comparação com rankings PvPoke;
- sugestões de Elite TM/custos.

Essa importação deve ser um módulo separado do OCR da batalha.

---

## 31. Pendências técnicas já conhecidas

Ainda precisam ser concluídos/validados:

- reconhecimento visual confiável das reservas;
- Fast Move automático validado;
- Charged Move usado com reconhecimento robusto em todos os casos;
- timer de troca;
- seletores `▼` interativos;
- drag/reordenação/edição do overlay;
- perfis de ROI por resolução;
- correção manual completa;
- APK Debug/Release completo e testado no aparelho;
- Team Builder com inventário;
- Battle Brain;
- pós-batalha/replay.

---

## 32. Créditos/identidade do projeto

Crédito definido nas telas/mockups:

`Desenvolvimento PvPPokGo by: LucianoToscano`

Contato definido no material do projeto:

`(84) 99131-9021`

Manter a identidade visual do projeto sem confundir a marca PvPPokeGo com Pokémon GO/PvPoke/Poke Genie ou outros terceiros.

---

## 33. Relação com PGLOT

Os projetos podem compartilhar componentes próprios/genéricos:

- modelos de Pokémon;
- tipos;
- moves;
- efetividade;
- PvP IV;
- battle simulator;
- filtros;
- OCR/vision;
- logs/stats;
- notificações;
- modelos de configuração;
- `StorageSearchGenerator`;
- schemas de importação do inventário.

Permanecem fora do core PvPPokeGo e pertencem ao PGLOT:

- localização/teleporte;
- mapa global;
- farm;
- hunter por coordenadas;
- VGP/GO Plus;
- scanner de mundo;
- automação de inventário/farm;
- root/injeção/concealment.

Dados de Rocket/Raid podem ser compartilhados como **dados/recomendação**, sem misturar os executores dos dois projetos.

---

## 34. Regra clean-room e licenças

Para referências proprietárias:

- reproduzir comportamento/UX/arquitetura com código próprio;
- não copiar DEX, `libmain.so`, JAR, modelos OCR proprietários ou outros binários;
- não remover premium/licenciamento;
- não incorporar concealment, bypass ou anti-detection.

Para open source:

- verificar licença individual;
- manter copyright/avisos necessários;
- usar upstream como referência/oracle quando apropriado;
- preferir dados locais versionados.

---

## 35. Documentos existentes que continuam válidos

Este documento não apaga os arquivos anteriores. Ele os consolida e referencia.

Continuam importantes:

- `docs/PROJECT_CONTEXT_MEMORY.md`;
- `docs/APK_CROSS_REFERENCE_2026-09-25.md`;
- `docs/APK_BASELINE_AND_IMPLEMENTATION_MAP_2026-09-26.md`;
- `docs/PGTOOLS_SHUNGO_REFERENCE_2026-09-26.md`;
- `docs/PGSHARP_BATTLE_FINDINGS.md`;
- `docs/PVPOKE_INTEGRATION.md`;
- `docs/PVPOKE_UPSTREAM_SOURCE.md`;
- `docs/BATTLE_OVERLAY_FIXES_2026-09-26.md`;
- `THIRD_PARTY_NOTICES.md`.

Se houver divergência entre uma nota antiga e uma implementação mais recente, prevalece o código atual + este documento, registrando a nota antiga como contexto histórico.

---

## 36. Princípio de continuidade

O projeto **não deve reiniciar do zero** quando um novo chat ou etapa for aberta.

Sempre continuar levando em conta:

- decisões de arquitetura;
- dados já importados;
- módulos já criados;
- UX aprovada;
- descobertas dos APKs;
- integração PvPoke;
- backlog do Battle Brain;
- pendências técnicas;
- arquivos de projeto existentes;
- histórico de correções/erros que motivaram a reescrita.

Este arquivo é o ponto central para essa continuidade.

---
## Adendo de 22/11/2026 — pesquisa de feeds/scanners (dependência cruzada, fora do núcleo PvP)

**Estado:** incorporação documental do estudo realizado para PGBR, PokeSearch, PvPPokeGo e PGSharp. **Não altera o core, visão/OCR, modelo PvPoke, HUD, versão, APK nem as PRs em andamento.** O PvPPokeGo continua sendo um **assistente externo de batalha**, e seus documentos `START_HERE_PVPPokeGo.md` e `SINGLE_THREAD_MASTER_CONTEXT_2026-09-26.md` mantêm a fronteira explícita: **scanner geográfico, mapa, spoofing, teleporte, captura, rotas/GPX e VGP pertencem a outros projetos** e não devem migrar para o PvPPokeGo.

### Registro da pesquisa transversal

- **Origens de dados diferentes:** PokéAPI e masterfiles fornecem informações de catálogo; não fornecem posição/IV/CP de encontros ao vivo. Foi **não verificada** qualquer API pública, autorizada e operacional de spawns Pokémon GO em 22/11/2026. Eventos de batalha do PvPPokeGo vêm de captura visual/OCR/observações locais e dados offline PvPoke; **não** devem depender do Golbat, PoracleNG ou de um feed geográfico.
- **Arquiteturas estudadas:** scan ativo histórico (spawnScan, Monocle, RocketMap, RealDeviceMap, MAD); agregação/scraping (PKMSpawn, ScanGO, Pogo-Scanner-Android); feeder JSON/HTTP (PSSnipeFeeder, PogoLocationFeeder); processamento de webhooks, geofences e mapa (Golbat, PoracleNG, ReactMap, Koji). São referências para **separação de produtores/consumidores, validação de eventos, cache, filtragem e alertas**, não fontes diretas de detecção de combate.
- **Situação pública verificada:** Golbat se descreve processador experimental; PoracleNG consome webhooks e entrega alertas, não detecta spawns sozinho; ReactMap é frontend com filtros, release v1.59.0 em 19/11/2026; RealDeviceMap arquivado em 03/11/2025; RocketMap e POGOProtos são legados. Código presente não equivale a recurso operacional ou permissão de acesso ao jogo.
- **Artefatos de conversa:** `SpawnFeedKit.kt`, HTML de pesquisa, `pesquisa-pokemon-go-android-arquitetura.zip`, `pesquisa-pokemon-go-android-validada.zip`, `pesquisa_pokemon_go_scanners.zip` (somente relatório) e `PokeSearch_Observacoes_v2_2026-11-22.zip` (modelo de observações, normalizador, testes). Foram aprovados **37 testes Kotlin/JVM** do núcleo de observações em 22/11/2026 no ambiente da conversa; isso **não** é build Android nem validação de qualquer comportamento PvP/PvPPokeGo. Nada do kit foi importado neste adendo.

### Padrões potencialmente úteis, sem confundir domínios

A experiência do kit de observações reforça o contrato de **evento com proveniência, timestamp, confiança e distinção clara entre confirmado, estimado e simulado**, aplicável conceitualmente a `BattleFrameObservation`, `StableObservationReducer`, reconhecimento de banners e `EvidenceLevel`. Não reutilizar `LIVE` de spawn como `CONFIRMED` de golpe: são domínios distintos. Não inventar leituras de tela, energia, escudos ou ataque usado quando o OCR estiver inconclusivo. Mesclar observações repetidas de frames contíguos sem perder mudanças reais; reavaliar previsão após evidência confirmada. Alertas podem seguir arquitetura de produtor de eventos → regras → overlay/vibração/voz, **sem** acionamento de cliques no Pokémon GO.

Referências: [Golbat](https://github.com/UnownHash/Golbat), [PoracleNG](https://github.com/jfberry/PoracleNG), [ReactMap](https://github.com/WatWowMap/ReactMap), [PokéAPI](https://pokeapi.co/docs/v2), [RealDeviceMap](https://github.com/RealDeviceMap/RealDeviceMap), [RocketMap](https://github.com/RocketMap/RocketMap), [Appium UiAutomator2](https://github.com/appium/appium-uiautomator2-driver). O Appium é útil exclusivamente para testar UI própria/autorizada.

**Pendências:** manter trabalho separado das PRs de estado de batalha e escudos; testar overlay em aparelho; não anunciar ataque automático, captura, injeção no jogo, radar de spawns ou integração live; não inserir código geográfico no core PvP sem mudança de escopo deliberada. Nenhum APK foi compilado nesta atualização documental.


---

<!-- CHAT_PGSHARP_PVP_PVPPKGO_20261122 -->
## 22/11/2026 — Continuidade: pesquisa PGSharp 1.33/1.50/1.278 e integração experimental PvP no PGBR LT035

**Origem:** conversa com solicitação de investigar funções *exatas* do assistente de batalha PGSharp, comparar três APKs, interpretar vídeos 13/14, portar para PGBR, testar, gerar APK e atualizar mestres. Registro documental do PvPPokeGo, **não** integração de código nativo de terceiros, build PvPPokeGo ou homologação em aparelho. O núcleo de PvPPokeGo continua um componente testável/desacoplado e seus próprios fluxos de desenvolvimento não devem ser confundidos com releases PGBR.

### 1. Fontes e graus de comprovação

| Referência | Achado | Limite |
| --- | --- | --- |
| [PGSharp 1.33.0](https://drive.google.com/file/d/1qvz4iYBRGfZZycazYagpxLaXMQqx2d48/view) + [romainthomas/pgsharp](https://github.com/romainthomas/pgsharp) | Pesquisa histórica independente com scripts/símbolos de hooks; não achado textual conclusivo de Auto PvP | Não é fonte completa oficial, nem prova de ausência binária. |
| [PGSharp 1.50.3](https://drive.google.com/file/d/1GuH3JkL77DYC7FfZujcEFxfy4rrafM32/view) | Introdução da família **Raid & Gym Battle Assistant** | Automação PvE não equivale a ações automáticas na Liga GO. |
| [PGSharp 1.278.0/PoGO 0.437.0](https://drive.google.com/file/d/13wvFdgVpnVHXsRUftJYjWiyQCzmJDsiU/view) | Identificadores `BATTLEASSIST`, `BREAD_BATTLEASSIST`, `rocketFlowIncident`, `battleAssistant`, `breadBattleFast`, `breadBattleCharge1`, `CombatVNextBattleStateShim`, `GymBattleState`, `RaidBattleState`, `pvpCombatStats` e `PVPIV` | `PVPIV`/estatística ≠ executor PvP; strings/menus não demonstram timing ou chamadas reais de ataques e escudos. |
| [Vídeo 13 (~44,2 s)](https://drive.google.com/file/d/1O6OyTOvJcFcKps5Qn9tHgJoqsfU3tTXM/view) / [Vídeo 14 (~76,6 s)](https://drive.google.com/file/d/1CuMTP0q8X2NSV_8wHmogbeGwTIMimz7n/view) | Auditoria visual anterior documentou HUD flutuante de ataque ágil, carregado e escudo durante PvP, trocas/animações | Vídeos não reproduzidos novamente nesta sessão e HUD ≠ prova de controle autônomo de jogo. |

O responsável declarou possuir autorização de uso, mas **não ocorreu extração binária de métodos/diff de APKs** no runtime disponível e **nenhuma função executável do PGSharp foi copiada** no PGBR/PvPPokeGo. A extensão dos direitos de terceiros não foi auditada.

### 2. O que o PGBR realmente implementou

Arquivos do projeto irmão: `pgrazor/native/cutscene_skip.cpp` (bootstrap IL2CPP tardio sem falha definitiva, observador validado de `GuiController.Open` útil mesmo sem hook de animação); `pgrazor/native/pvp_scene.h` e `combat_host.cpp` (Hub → Preparação → CombatVNext, exclusão de NPC/results, renovação de sessão apenas por atualizações VNext, timeout por inatividade e teto de 20 minutos); `pgrazor/native/pvp_scene_test.cpp` e `pgrazor/test/test_pvp_trace_bootstrap.py` (regressões). Os comandos nativos se relacionam a `rocket_combat.cpp` e `rocket_actions.cpp`; integrações e permissão Rocket/PvP ainda precisam de separação/validação de estados.

Rastreabilidade:
- [PGBR PR #76](https://github.com/LucianoToscano/PGBR/pull/76): primeiro patch, fechado sem merge quando substituído; [#78](https://github.com/LucianoToscano/PGBR/pull/78) tentativa intermediária também fechada sem merge.
- [PGBR PR #79](https://github.com/LucianoToscano/PGBR/pull/79): integração real, cinco checks CI aprovados, merge `e91b881f000fe7da9f57aed2cafabb1b4a9a1899`.
- [PR #80](https://github.com/LucianoToscano/PGBR/pull/80): Mestre pós-merge, merge `13a7c91d329fc179bbedbe4e514b303958a7d389`; [#81](https://github.com/LucianoToscano/PGBR/pull/81): Mestre/README/hash do release, merge `01111cad8f0ddf0e6f3cd16a63752334956777da`.
- [Build LT035](https://github.com/LucianoToscano/PGBR/actions/runs/45018980405) **success**, [pré-release PGBR v0.00.35-LT](https://github.com/LucianoToscano/PGBR/releases/tag/v0.00.35-LT) publicada com [APK não Root](https://github.com/LucianoToscano/PGBR/releases/download/v0.00.35-LT/PGBR-v0.00.35-LT.apk), SHA-256 `31f361c2b3284a329a6a82e1bf194d6d2dd8a9dd03e6485bccfe94ce6db5d3aa`; [Root](https://github.com/LucianoToscano/PGBR/releases/download/v0.00.35-LT/PGBR-v0.00.35-LT-Root.apk), SHA-256 `031e4dd2113027520df37fe97f172f7838c6defbf524d2b15b511cdf11645597`; [Zygisk](https://github.com/LucianoToscano/PGBR/releases/download/v0.00.35-LT/PGBR-v0.00.35-LT-Root-module.zip), SHA-256 `0d972a46dc128b93c8dfab9b00b80676c3cb1adeb59d789a6d4c222a1918ff6e`.
- O release é **QA experimental, `device_tested=false`**. Testes automáticos e builds assinados **não provam** toque ágil/carregado/escudo aceito em GBL/desafio real, nem escolha do carregado 2 supereficaz.

### 3. Contrato de interoperabilidade proposto para PvPPokeGo (NÃO implementado nesta tarefa)

A arquitetura já documentada em [SMART_BATTLE_AUTOMATION.md](SMART_BATTLE_AUTOMATION.md) e no core `BattleAutomationController` diferencia políticas independentes de `autoFastAttack`/`autoChargedAttack`/`autoShield`, escolha `FIRST_READY`/`TYPE_PRIORITY`/`SMART`, ameaça, STAB, energia e `TapChargedAttack(buttonIndex)`. O PGBR já possui `PvpBattleController.java` e `PvpChargedPreference.java` de decisão, mas o segundo carregado super eficaz **não está conectado/validado no executor nativo**.

**Interoperabilidade futura segura:** observador (PvPPokeGo) produz eventos tipados com `mode=PVP`, ID estável de batalha, tempo, origem, confidence/evidence, energia, tipos/moves e estado dos prompts; core avalia e retorna **intenção**, sem prometer que será executada. Adaptador PGBR, em tarefa/PR própria, confirma escopo GBL, flags de permissão e UI thread, aplica deduplicação/janela de validade, escolhe carregado 1/2 e observa resultado. Rejeitar estados Rocket, raid/gym, Max, NPC, amostra antiga, frame incerto e ausência de energia. Separar sem misturar licença, dependências, namespaces, build e configuração de segurança dos dois apps. Não fazer esse acoplamento por cópia de offset/binário histórico.

**Critérios pendentes:** capturas instrumentadas e testes Android reais de GBL/desafio, fast attack, charged 1/2, escudos, troca, charged minigame, encerramento, reconexão, cooldown/latência e alternância de modo; teste de hipótese no gameplay não pode ser inferido de texto OCR ou de booleano `invoked=true`. Preferências Rocket e PvP compartilhadas exigem decisão explícita de migração antes de separação definitiva.

### 4. Relação com outros mestres

- [Mestre PGBR](https://github.com/LucianoToscano/PGBR/blob/main/PGBR_ARQUIVO_MESTRE_UNICO.md): autoridade sobre commits, binários, APKs e flags nativas.
- [Guia Mestre PokeSearch](https://github.com/LucianoToscano/PokeSearch/blob/main/docs/GUIA_MESTRE.md): catálogo/rank PvP, tipos, sprites; **não** é executor de batalha; nenhuma mudança de P1/P2 ou release LT409 neste chat.
- [PGSharp próprio](https://github.com/LucianoToscano/PGSharp): fonte experimental e inventário do usuário; não confundir com código fechado do fornecedor ou repositório acadêmico.
- Este `CONSOLIDATED_PROJECT_CONTEXT_2026-09-26.md` continua o **mestre canônico do PvPPokeGo**; preservar todos os 09/2026 e atualizações prévias, sem criar outro mestre concorrente.

**Resultado deste registro:** documentação cross-project; **nenhuma implementação/compilação/release de PvPPokeGo** e nenhuma função literal do PGSharp importada. Protocolo Global de branches isoladas e PRs sem perda continua obrigatório.

<!-- PGBR_LT037_TRAINING_COMBAT_20261122_PVPPK -->
## Registro cruzado PvPPokeGo: batalha Rocket compartilhada e LT037

### Registro cruzado de 22/11/2026 — falha de Auto Batalha no Treinamento e PGBR LT037

**Relato/imagem do usuário:** combate de treinador/líder de equipe contra Sneasel PC 1465 com Lickilicky PC 1500 e ausência de autoataques percebida. A imagem, isoladamente, não demonstra qual APK e módulo estavam carregados; não prova que LT037 falhou. O pedido é usar as opções Rocket (ataque ágil, carregado, shield) **apenas** em Rocket (recrutas, Arlo, Cliff, Sierra, Giovanni), PvP/GBL, desafios privados entre amigos e treino/NPC comprovado; autorizar e desarmar automaticamente conforme o escopo do combate, mantendo as três opções do usuário. Não ativar em raid, Max, encontro selvagem, desconhecido nem ginásio no motor Rocket.

**Auditoria verificável no código PGBR:** [PR #87](https://github.com/LucianoToscano/PGBR/pull/87) integrou o motor próprio `rocket_combat.cpp` compartilhado por sessão tipada: `battle_target_policy.h` permite ROCKET/PVP/TRAINER_NPC e nega RAID/GYM/MAX/WILD/UNKNOWN; `pvp_scene.h` exige Hub→Preparação→VNext ou `CombatTypeExtensions.IsPvPCombat` validado + VNext para privado, ou interstitial NPC (candidato 45s)→Preparação→VNext para treino. `combat_host.cpp` usa lease ≤2500ms e mascara 1/2/4 sob permissão. Prioridade Rocket, encerramento VNext, resultados, perda de lease, PvE, parada e modo desconhecido revogam. O jogador **não é transformado em Rocket** nos dados do servidor; só há compartilhamento local do executor. Ginásio usa controlador independente `battles.cpp`, por isso **não** entra na allowlist Rocket.

**Verificações e entrega:** [GitHub Actions LT037 #45028477340](https://github.com/LucianoToscano/PGBR/actions/runs/45028477340) `completed/success` e [pré-release QA v0.00.37-LT](https://github.com/LucianoToscano/PGBR/releases/tag/v0.00.37-LT) publicada em 22/11/2026 13:45:20 UTC. [APK sem Root](https://github.com/LucianoToscano/PGBR/releases/download/v0.00.37-LT/PGBR-v0.00.37-LT.apk) SHA-256 `1df76ebdfaa9c4d72d203c61f4f317f5b08af5970bd6271c9d3d9b425e4d4a88`; [instalador Root](https://github.com/LucianoToscano/PGBR/releases/download/v0.00.37-LT/PGBR-v0.00.37-LT-Root.apk) SHA-256 `e2b594a21738a81eb4bc99858a6ad8f6ed094539db8dee69ae2cfb43b9d678c9`; [módulo Zygisk](https://github.com/LucianoToscano/PGBR/releases/download/v0.00.37-LT/PGBR-v0.00.37-LT-Root-module.zip) SHA-256 `33d9611fcdd46c31272ddd488d4e04a24f11ef7857b8f741a2613da28f1b15dd`; [SHA256SUMS](https://github.com/LucianoToscano/PGBR/releases/download/v0.00.37-LT/SHA256SUMS.txt). Versões LT035/LT036 preservadas.

**Distinção de evidência:** os testes C++/Java/Python e a compilação só confirmam lógica/empacotamento. **`device_tested=false`**: ainda não demonstraram ação ágil aceita, golpe carregado lançado, escudo efetivamente consumido, troca, segundo carregado mais supereficaz ou encerramento sem toque após combate. `pvpExecutorReady` e `pvpAction.invoked=true` são eventos de tentativa; não comprovam dano. Rotas NPC não identificadas continuam OFF. A identificação de classes do jogo exige 0.437.0 e hooks compatíveis. Se o APK sem Root não instalar por certificado diferente em `com.nianticlabs.pokemongo`, verificar `adb install -r` e o código do erro sem apagar aplicativos ou dados automaticamente.

**Referências e limites do PGSharp:** [análise independente histórica 1.33.0](https://github.com/romainthomas/pgsharp), [APK 1.50.3](https://drive.google.com/file/d/1GuH3JkL77DYC7FfZujcEFxfy4rrafM32/view) e [APK 1.278.0](https://drive.google.com/file/d/13wvFdgVpnVHXsRUftJYjWiyQCzmJDsiU/view), vídeos [13](https://drive.google.com/file/d/1O6OyTOvJcFcKps5Qn9tHgJoqsfU3tTXM/view)/[14](https://drive.google.com/file/d/1CuMTP0q8X2NSV_8wHmogbeGwTIMimz7n/view). A linha 1.50 é conhecida por assistente de Raid/Gym, não por Auto PvP comprovado; vídeos evidenciam HUD de três opções, não comportamento automático verificável. **Não foi copiada função binária exata dos APKs PGSharp**; a LT037 reaproveita código próprio do PGBR. A declaração de autorização do usuário não substitui auditoria de direitos. Não inventar offsets, API, códigos ou testes físicos.

**Limite PvPPokeGo:** o core de decisão (`BattleAutomationController`, energia, eficácia, ameaça, carregados 1/2, estratégias de escudo) segue separado do executor PGBR. A LT037 não comprova que tais estratégias são executadas, nem trouxe código novo para este repositório. Futuro contrato precisa de modo/tipo confirmado, observação com origem e confiança, TTL, prompt de escudo e confirmação de efeito real; priorizar estados fail-closed. Mudanças somente documentais nesta PR, sem build PvPPokeGo, ABI, releases ou dependências novas.


---
<!-- PGBR_LT038_SMART_ATTACK_CHAT_20261122_PVPP -->
## 22/11/2026 — Continuação do chat: Ataque Inteligente único, escolha manual e minijogo

**Pedido consolidado:** na página de Batalhas, ordem **Batalha Inteligente → Ataque Inteligente → Cura Inteligente**. Um único interruptor Ataque Inteligente deve futuramente priorizar golpe carregado por eficácia **Super eficaz > Neutro > Pouco eficaz** em PvP/GBL, Equipe Rocket, Treinamento/NPC, duelo privado/amigos, Ginásios, Raides e Max; desativar a opção volta ao comportamento original. Usar o texto que o jogo exibe sob ataques apenas como hipótese de fonte de dados: o mapeamento Unity texto→golpe→botão não foi comprovado. Ataques carregados continuam instantâneos. A troca automática deve permitir **2300 ms** de escolha manual antes do botão; a escolha do escudo deve esperar **1700 ms**, respeitando a decisão do jogador. A tela esbranquiçada do minijogo carregado ainda é investigada; não mudar colisor/shader sem evidência. Cura Inteligente opera com itens no mapa ou após combate, e não usa poção no meio do PvP.

**Estado confirmado no GitHub PGBR:** [PR #93](https://github.com/LucianoToscano/PGBR/pull/93) integrou interruptor `smartAttack` persistente e migração da preferência PvP; [PR #94](https://github.com/LucianoToscano/PGBR/pull/94) concedeu 2300ms de troca Rocket e 1700ms de escudo; [PR #96](https://github.com/LucianoToscano/PGBR/pull/96) alinhou também `smart_battle.cpp` ao prazo e à precedência do seletor manual; PRs [#95](https://github.com/LucianoToscano/PGBR/pull/95) e [#97](https://github.com/LucianoToscano/PGBR/pull/97) registraram o resultado pós-merge. **Sem release novo para essas PRs até esta solicitação**. [PR #98](https://github.com/LucianoToscano/PGBR/pull/98) prepara **LT038 QA experimental** para empacotar apenas o menu e os temporizadores, preservando [LT037 publicada](https://github.com/LucianoToscano/PGBR/releases/tag/v0.00.37-LT). Não confirmar PR98 merged, build aprovado, tag publicada nem hashes enquanto CI estiver pendente.

**Limites críticos:** `PvpChargedPreference.java` implementa política de eficácia com testes Java, mas `rocket_combat.cpp` e `battles.cpp` ainda escolhem primeiro carregado pronto. Não existe ponte nativa tipada e validada com espécies, dois movimentos, disponibilidade, origem, geração de batalha e botão correspondente. Não classificar Ataque Inteligente global como operacional; só a preferência está salva. `smart_battle.cpp` escolhe equipe antes de Rocket/Raides/Ginásios e pode trocar em Rocket/Ginásios, não comprova mesma funcionalidade em PvP/treino/Max. O usuário relatou três comandos Rocket (Ágil, Carregado, Escudo) funcionando no aparelho na **LT037**, não testou nova prioridade/atrasos. Na coleta automática de partículas, `rocket_minigame.cpp` amplia colisor 1000 vezes; esta pode ser hipótese para tela branca, não prova e não justifica alteração às cegas. `device_tested=false` para o conjunto novo.

**Governança:** consultar cada Mestre e HEAD, preservar branches simultâneas, histórico, ABI, licenças, APKs, registros do usuário e releases já publicadas. Este adendo é **documentação**, não implementação de recursos em outros projetos.

**PvPPokeGo:** motor consultivo pode fornecer escolhas por eficácia, mas nenhum comando nativo foi conectado à batalha do PGBR nesta tarefa. Seu build e core de energia/escudo permanecem intactos.

<!-- PGBR_LT038_RELEASE_PUBLISHED_PVPPOKEGO_20261122 -->

### 22/11/2026 — Recibo verificado da release experimental PGBR LT038
A candidata PGBR [PR #98](https://github.com/LucianoToscano/PGBR/pull/98) foi **merged** em `4ca4c48d417cc4218915db7d763fd360f2c0fc85`. A [release QA v0.00.38-LT](https://github.com/LucianoToscano/PGBR/releases/tag/v0.00.38-LT) foi publicada em `2026-11-22T15:46:48Z`, após [Actions #45039119340](https://github.com/LucianoToscano/PGBR/actions/runs/45039119340) `completed/success`. Os três arquivos diretos e SHA-256 da API GitHub são:
- [APK sem Root](https://github.com/LucianoToscano/PGBR/releases/download/v0.00.38-LT/PGBR-v0.00.38-LT.apk): `cdea0e27a9a1adf198bc25163b0313ed7dd1381f3c8cccc07470823dcc4f01ea`.
- [Instalador Root](https://github.com/LucianoToscano/PGBR/releases/download/v0.00.38-LT/PGBR-v0.00.38-LT-Root.apk): `36cd54df01036a4e9e81defd3ddd1cecfa824034e693a4971ac1c9f012026597`.
- [Módulo Zygisk](https://github.com/LucianoToscano/PGBR/releases/download/v0.00.38-LT/PGBR-v0.00.38-LT-Root-module.zip): `ee9eea160001e9b9495df91ff9d77b21b4fd720722c057b06ec33b5a8dc5c9a1`.
- [SHA256SUMS](https://github.com/LucianoToscano/PGBR/releases/download/v0.00.38-LT/SHA256SUMS.txt).

**Limitação:** menu Ataque Inteligente e temporizadores presentes; escolha nativa por eficácia em todas batalhas e a correção da tela branca **não foram implementadas na LT038**. `device_tested=false` para essa versão. Este registro é apenas documentação cruzada, não atualização do código-fonte nem APK deste projeto.

<!-- PVPPOKEGO_SPAWN_FEED_SCOPE_20261122 -->
## 22/11/2026 — Governança cruzada de fontes de coordenadas e PvP (sem alterações no motor)

**Base:** PvPPokeGo `main` `f58154df441303e66bb468550edc945db97f04c5`, branch isolada `docs/spawn-source-provenance-20261122`. O pedido de pesquisa abrangeu webhooks, radar, Sniper, scanners históricos, a palestra de PGSharp 1.33.0 (Ekoparty 2021), PoracleNG, RocketMap, ReactMap e GoBattleSim.

**Fronteira obrigatória:** PvPPokeGo é assistente de combate visual/OCR, não cliente de scanner nem serviço de GPS, webhook ou teleporte. Espécies, golpes e IV/rank PvP exigem dados de Game Master/PvPoke; IV 100% não equivale a Rank 1 de Grande/Ultra. Observações de mapa com coordenadas e despawn não podem ser usadas como prova de ações do adversário em PvP.

**Integração apenas futura:** a branch independente `feat/authorized-spawn-webhook-normalizer-20261122` do repositório PokeSearch implementou um conversor OFFLINE e opt-in de eventos para o esquema RocketMap usado pelo PGBR, sem acesso a servidores privados. Esta entrega PvPPokeGo apenas documenta limites e ponte possível; código de combate, OCR, overlay, Firebase, APK, certificados e releases permanecem intocados.

**Arquivo de documentação desta branch:** `docs/POKESEARCH_WEBHOOK_PVP_SCOPE_20261122.md`. [Catálogo central](https://docs.google.com/document/d/12-I-rUfnQR0YL_mDExTJfx2zXgfxgB8-Bkp2tyuVPfI/edit) e [PDF histórico](https://drive.google.com/file/d/1nEf72fUU4hXS2V8alnuc4pv3unUdINV3/view). O PDF não demonstra integração global ao servidor oficial nem um Auto PvP executável atual.

**Status:** mudança documental em PR pendente de revisão/merge; sem APK, sem teste físico ou alteração de runtime. Preservar PRs paralelas #42/#43 e todas as demais.


<!-- PVPPOKEGO_WEBHOOK_PR_RECEIPT_20261122 -->
### Registro da PR documental #55
[PR #55](https://github.com/LucianoToscano/PvPPokeGo/pull/55) aberta em branch separada; não contém alterações no núcleo de batalha nem APK. O workflow de validação Android [#45049067639](https://github.com/LucianoToscano/PvPPokeGo/actions/runs/45049067639) estava **in_progress** no momento da consulta, não devendo ser rotulado aprovado até conclusão verificável. Mantidos os limites de escopo para mapa/teleporte, que continuam fora do motor PvPPokeGo.
