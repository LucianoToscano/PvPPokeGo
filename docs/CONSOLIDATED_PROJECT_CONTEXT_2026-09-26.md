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


<!-- REGISTRO-DRIVE-20261122 -->
## 22/11/2026 — Registro transversal da organização do Google Drive e biblioteca Pokémon GO

> **Natureza:** histórico documental das operações realizadas na conversa "Banco de Dados Pokemon Go", verificado por listagens e metadados do Google Drive. **Não é alteração de código, integração de recursos, teste de APK ou homologação.** Mantêm-se integralmente os registros anteriores; divergências entre snapshots devem ser resolvidas consultando GitHub/Drive atuais, não sobrescrevendo o histórico.

### Estrutura central e migração de pastas

Pasta-pai: [Pokemon Go, em Projetos](https://drive.google.com/drive/folders/180wu9mqDYttMwYFP3XxXEUmOShLyvTJv). Foram realocadas as seis pastas de origem dentro dessa pasta-pai; depois a raiz foi consolidada e verificada com **exatamente cinco projetos**: [PGBR](https://drive.google.com/drive/folders/1RJ2NuBv-tnEDf9Pcxgn1I7-9CTdPGAHX), [PokeSearch](https://drive.google.com/drive/folders/1pHnwNFYlDxxt9g5PtdYWWFAylBkvVyRU), [PvPPokeGo](https://drive.google.com/drive/folders/1ybogVRRnaOe0KtWjlovxS56bJOi_JewI), [PGSharp](https://drive.google.com/drive/folders/1WAEmEpZmvUw9dDiapNcygtmZQq6XIB_j) e [Banco de Dados Pokemon Go](https://drive.google.com/drive/folders/1TPgVIGAWkY2Ez1VS3bHAitv9Y_NpczbK).

As pastas legadas **PGSharpLOT**, **PgSharp Publico**, **PokeGoBR** e **PokeLOT** deixaram de existir como áreas paralelas; os conteúdos identificados foram redistribuídos antes de excluir os contêineres vazios. Material legado de PokeGoBR foi incorporado à área PGBR; PvPPokeGo e PGSharp foram retirados de PokeLOT; builds PGBRLOT que estavam no PokeSearch foram para PGBR. Arquivos de terceiros, dossiês, referências e arquivos soltos identificados foram movidos às áreas correspondentes. Essas mudanças no Drive não significam merge entre repositórios GitHub nem validam novas versões.

### Biblioteca única de imagens, dados, referências e APKs

O [Banco de Dados Pokemon Go](https://drive.google.com/drive/folders/1TPgVIGAWkY2Ez1VS3bHAitv9Y_NpczbK) permaneceu a referência compartilhada, organizada em catálogos; sprites/imagens/sons/modelos; dados, rankings e inventários; mapas/radar/eventos; APKs de referência; links/guias/ferramentas; arquivo ZIP original; backups e históricos; e quarentena de duplicatas.

[Acervo visual](https://drive.google.com/drive/folders/1MnVVAsDOvam1laU6Ahv-whIPpBF0gz68): 01 Pokémon completos (normal, Shiny, formas e trajes); 02 recortes/partes/sprite sheets; 03 ícones e símbolos; 04 itens/acessórios/roupas; 05 efeitos/animações; 06 layouts/menus/interface; 07 fundos/cenários/telas; 08 modelos 3D; 09 sons/áudio; 10 eventos/banners/figurinhas; 11 avatares/personagens; 90 acervos de origem e materiais técnicos. **Shiny e formas** pertencem ao acervo de Pokémon, e os recortes de olhos/peças, ícones de interface e itens permanecem separados por finalidade. Acervos de origens distintas são preservados sem mesclar arbitrariamente arquivos de mesmo nome.

Os [catálogos](https://drive.google.com/drive/folders/1rH8aNhTP6HFy4VevrXlkGBPT75w5qXdl) foram divididos em manifestos/catálogos, inventários/índices, auditorias/validações e guias de aproveitamento. Há [quarentena separada para duplicatas](https://drive.google.com/drive/folders/1Z-DQuYIhsu-kgBg8nEXPJKEk8jumPUuM), inclusive itens apenas suspeitos de repetição: **não apagar por igualdade de nomes nem por aparência**. O ZIP original foi preservado. Pacotes ZIP não foram todos extraídos/classificados individualmente.

No [acervo de APKs](https://drive.google.com/drive/folders/1ghacTFCc7LpRnvP9GqmcjBpfN9joAobt), a pasta de ferramentas externas foi verificada com **74 itens** distribuídos em 64 instaladores/pacotes, 6 arquivos descompilados, 2 projetos externos e 2 bancos de configuração; o acervo PokeSearch de releases tem área separada. São **referências**, não código oficialmente licenciado ou APK funcional verificado apenas por estar armazenado.

### Privacidade, integridade e tarefas pendentes

**Permissões constatadas durante a organização:** a biblioteca Banco de Dados Pokemon Go permite leitura por link; a pasta PokeSearch tinha compartilhamento com permissão de edição por link (situação que exige revisão); PGBR, PvPPokeGo e PGSharp são pastas privadas. **Não transportar chaves, PGSData/PGSStats pessoais, configurações de conta, capturas privadas ou APKs internos à biblioteca pública sem rever permissões e classificar cada item.** Não expor links de material privado ou secretos em repositórios públicos. O backup marcado como privado do PokeSearch permaneceu separado.

Movimentos de arquivos do Drive **não atualizam automaticamente** caminhos em builds, GitHub Actions, scripts, Gradle, referências em documentos ou permissões herdadas. Verificar os consumidores antes de corrigir caminhos. Não declarar conclusão total da consolidação privada, equivalência de conteúdo por hash, compatibilidade dos APKs, autorização de uso de código de terceiros ou funcionamento de automação sem auditoria técnica. Os arquivos originais de código, histórico, versões, backups e licenças devem ser preservados.

### Protocolo global — desenvolvimento paralelo sem perdas

1. Ler integralmente o mestre canônico e sua documentação vinculada, depois conferir HEAD, PRs e estado atualizado do repositório oficial.
2. Nunca editar a partir de cópia antiga nem sobrescrever mudanças realizadas em outros chats.
3. Usar **branch independente por tarefa**, registrar commit-base/arquivos afetados/mudanças/testes e preparar Pull Request, **sem publicar diretamente na `main`**.
4. Preservar funcionalidades, arquivos de referência, assets, backups, histórico e direitos de terceiros; enriquecer o conteúdo, nunca reduzi-lo.
5. Distinguir **pedido, documentação, arquivo presente, implementação, teste local, CI, teste em aparelho e release publicada**. Documentar limitações e pendências sem promover intenção a fato.

### Registro específico — PvPPokeGo

A pasta PvPPokeGo foi retirada de PokeLOT e classificada em `01_APKs_e_Releases/`, `02_Codigo_e_Projetos/`, `03_Auditorias_e_Relatorios/`, `04_Assets_Imagens_e_Layouts/`, `05_Guias_e_Ferramentas/`, `90_Backups_e_Historico/` e `99_Temporarios/`. Foram preservadas múltiplas versões APK/QA, fontes editáveis, patches, auditorias, histórico, imagens, interfaces, referências de vídeo e material de overlay. Na listagem auditada, a área de releases continha 17 entradas; o registro é um inventário de Drive, **não** certificação de 17 APKs instaláveis.

Os acervos visuais e builds próprios do PvPPokeGo **permaneceram privados**, pois a biblioteca central tem leitura por link; não foram publicados automaticamente. A biblioteca de imagens do APK 0.5.37 e suas pastas de ícones, backgrounds e telas de usuário continuam disponíveis para futura triagem de privacidade. A redistribuição não confirma que OCR, decisões de escudo, Auto Attack, PvP ao vivo ou APK final funcionem: manter distintos o assistente visual/recomendação, o simulador offline e a execução real. A identidade técnica do PvPPokeGo não foi fundida com PGSharp ou com antigos projetos LOT.

