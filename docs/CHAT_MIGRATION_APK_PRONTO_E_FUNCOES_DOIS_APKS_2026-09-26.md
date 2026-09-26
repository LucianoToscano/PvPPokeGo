# Migração de contexto — “APK pronto para baixar” + “Funções Para Dois APKs”

Atualizado em: 2026-09-26

Este documento concentra o conteúdo recuperado dos dois tópicos antigos que deve continuar valendo para o **Projeto PvPPokeGo**, juntamente com as implementações feitas depois deles. O objetivo é não depender dos chats antigos para lembrar decisões, ideias, arquitetura, pendências e próximos passos.

> Regra: fatos recuperados dos chats antigos são preservados como contexto histórico/planejado; itens já presentes no repositório atual são tratados como estado implementado. Funções de PGLOT permanecem fora do core PvP.

---

## 1. Objetivo consolidado do PvPPokeGo

PvPPokeGo é um assistente de batalha externo para Pokémon GO, focado em:

- captura de tela com MediaProjection;
- OCR/visão por regiões da tela;
- reconhecimento de Pokémon próprio e adversário;
- memória independente dos três Pokémon inimigos;
- identificação e correção de Fast/Charged Moves;
- contagem de energia e fast moves;
- previsão de charged moves;
- leitura de banners de charged moves;
- tipos, resistências e fraquezas;
- matchup/counter advice;
- shield advice;
- switch advice/timer;
- PvP IV/rank;
- integração de dados com PvPoke;
- overlay modular, compacto e não intrusivo;
- replay/diagnóstico e modo treino;
- importação do próprio time/coleção via PGSStats.

O projeto deve permanecer separado de root, injeção, concealment, bypass, manipulação de memória do Pokémon GO e cópia de binários proprietários.

---

## 2. APK-base analisado no tópico antigo

O APK de referência histórica analisado era:

- package: `com.tandembot.tandempvp`;
- versão: `0.1.0` / version code 1;
- min SDK 29;
- target/compile SDK 35.

Características observadas naquela base:

- MediaProjection;
- VirtualDisplay;
- ImageReader;
- ML Kit OCR;
- serviço de overlay;
- permissões de overlay e foreground capture;
- modelos equivalentes a `PokemonDef`, `MoveDef`, `GameMaster`, `RankingEntry`, `PokemonRepo`, `Analysis`, `Team`, `TeamHistoryRepo`;
- uso de dados PvPoke/GameMaster e rankings.

O overlay antigo possuía controles como `fast`, `form`, `reset`, `win` e `loss`.

Tentativas antigas de patch agressivo do DEX causaram falhas de inicialização/overlay, incluindo `ClassNotFoundException`. A decisão consolidada foi **abandonar patch binário** e manter uma reimplementação própria em código-fonte.

O package pretendido na reescrita passou a ser `com.lucianotoscano.pvppokego`.

---

## 3. Arquitetura visual recuperada

Pipeline consolidado:

`MediaProjection -> frame -> ROI -> OCR/visão -> observações com confiança -> estabilização -> eventos de batalha -> BattleSession/BattleEnergyTracker -> Battle Brain -> overlay`

Regras importantes:

- OCR deve ser rate-limited;
- Fast Move detector deve ser um módulo de ROI leve separado;
- frame count por si só não é Fast Move detection;
- leituras visuais não devem alterar o estado imediatamente quando a confiança for baixa;
- reservas podem exigir sprite/classificador/template matching e sempre devem admitir correção/seleção manual;
- o próprio time deve preferir dados importados de PGSStats quando disponíveis, reduzindo OCR desnecessário.

---

## 4. Overlay — especificação consolidada

### 4.1 Informações principais

Mostrar de forma compacta:

- Pokémon inimigo atual;
- Pokémon já revelados/reservas;
- matchup com indicação visual;
- Fast Move atual/provável;
- dois Charged Moves;
- quantidade de Fast Moves até cada Charged;
- energia estimada;
- faixa de energia quando a contagem não for exata;
- energia residual após Charged;
- estado de shields;
- timer de troca;
- recomendação curta de shield/switch;
- última ação importante.

### 4.2 Matchup/reservas

Foi definido usar indicadores simples, como:

- verde: favorável;
- cinza: neutro/incerto;
- vermelho: desfavorável/ameaça.

A interface deve mostrar o mínimo necessário durante a batalha.

### 4.3 Overlay não intrusivo

Requisito obrigatório:

- botão Ocultar;
- modo compacto/bolha mínima;
- forma rápida de voltar;
- auto-hide quando não houver informação útil;
- modo “somente alertas”;
- ocultar a UI não perde BattleSession, energia, adversários ou histórico;
- se o usuário ocultar manualmente, respeitar a ação e não reaparecer sem motivo crítico/configurado.

### 4.4 Controles touch-safe

A arquitetura antiga previa overlay pass-through e, na fase dos seletores, somente pequenas regiões touchable sobre os controles necessários.

O objetivo atual continua sendo:

- toque no Fast Move -> submenu com Fast Moves legais daquele Pokémon;
- toque no Charged 1 -> submenu de Charged Moves legais;
- toque no Charged 2 -> submenu de Charged Moves legais;
- correção manual rápida sem abrir telas grandes;
- edição/drag somente em modo de configuração, não durante operação normal.

---

## 5. PvPoke e dados

PvPoke é a principal fonte de dados/rankings para:

- espécies/formas;
- tipos;
- Fast Moves;
- Charged Moves;
- geração/custo de energia;
- rankings e movesets de Great/Ultra/Master;
- matchup/simulação e validação cruzada.

O app deve funcionar com dados locais atualizados previamente; não deve depender do site durante a batalha.

Moveset inicial do inimigo:

1. identificar a espécie;
2. carregar automaticamente o moveset recomendado/rankeado #1 para a liga/copa;
3. marcar esses golpes como **PREVISTOS**;
4. substituir previsões por golpes **CONFIRMADOS** conforme forem observados.

---

## 6. Golpes do inimigo — comportamento consolidado

### 6.1 Charged Moves

O core já evoluiu para diferenciar:

- `selectedChargedMoveIds`: golpes mostrados nos dois controles;
- `confirmedChargedMoveIds`: golpes realmente observados na batalha.

Regra:

- primeiro charged observado substitui somente uma previsão não confirmada;
- o golpe observado fica confirmado;
- repetição do mesmo golpe não altera o outro slot;
- segundo charged diferente observado ocupa o outro slot não confirmado;
- dois golpes confirmados não devem ser sobrescritos por uma nova previsão;
- correção manual deliberada pode remover a confirmação de um OCR incorreto.

### 6.2 OCR -> move ID

O banner visível da batalha é usado como fonte de verdade, por exemplo:

`Plusle usou Relâmpago!`

Fluxo:

`OCR -> nome localizado -> alias pt-BR -> moveId interno -> validar se é golpe legal da espécie -> aplicar custo -> atualizar energia/progresso`

O parser aceita aliases localizados e tolerância a pequenas falhas de OCR.

### 6.3 Fast Move

Fast Move não costuma aparecer em banner textual a cada uso. Portanto:

- início: usar Fast Move do top moveset PvPoke como **PREVISTO**;
- usuário pode corrigir pelo submenu;
- detector futuro deve inferir por ROI/animação/turnos/ritmo/energia;
- só trocar automaticamente com confiança suficiente;
- Fast Move errado afeta toda a contagem, então baixa confiança nunca deve virar certeza silenciosamente.

---

## 7. Memória dos três Pokémon inimigos

Cada slot adversário deve preservar independentemente:

- espécie;
- status ativo/bench/fainted;
- Fast Move e nível de evidência;
- Charged Moves previstos/confirmados;
- energia;
- faixa de energia;
- quantidade de Fast Moves observados;
- último Charged Move;
- número de Charged Moves usados;
- correções de energia;
- HP aproximado quando disponível;
- última ação relevante.

Trocar o Pokémon para o banco **não zera** sua energia.

Quando voltar, o overlay deve restaurar o estado daquele mesmo slot.

---

## 8. Energia, contagem e sincronização

### 8.1 Energia residual

Depois de um Charged Move, subtrair somente o custo real e preservar energia restante.

Exemplo conceitual:

`64 - 40 = 24 energia residual`

### 8.2 Faixa de energia

Quando houver frames perdidos/baixa confiança, não mostrar um número falso como exato.

Exemplo:

`Energia provável: 44–52`

`EnergyRangeEstimate` representa esse estado.

### 8.3 Autocorreção

Se o OCR confirma um Charged Move que seria impossível com a energia calculada atual, isso é evidência de Fast Moves perdidos.

O tracker deve:

- elevar a energia prévia ao mínimo legal necessário;
- aplicar o custo do golpe;
- registrar a correção;
- continuar a batalha sem quebrar a sincronização.

### 8.4 Double-charge

Mostrar quando o oponente realmente possui duas cargas completas armazenadas.

O cálculo deve respeitar o máximo de 100 de energia:

- golpes de custo <= 50 podem ter duas cargas completas;
- golpes de custo > 50 nunca têm duas cargas completas simultâneas;
- para golpes > 50, pode existir energia residual alta após a primeira, mas isso não é `x2` pronto.

---

## 9. Confidence Engine

Estados consolidados:

- `PREDICTED` / Previsto;
- `PROBABLE` / Provável;
- `CONFIRMED` / Confirmado.

Aplicar confiança a:

- espécie;
- Fast Move;
- Charged Move;
- energia/faixa;
- observações OCR/visuais;
- recomendações dependentes de dados incertos.

Objetivo: nunca apresentar estimativa como fato confirmado.

---

## 10. Battle Brain / assistência em tempo real

Arquitetura conceitual consolidada:

`OCR/MediaProjection -> Battle State -> Energy Tracker -> Move inference -> Battle Brain -> Recommendation -> Overlay / vibração / voz opcional`

Funções planejadas/aplicadas no core:

- charged timing;
- double-charge;
- bait risk;
- shield advisor com motivo;
- switch/counter advisor;
- switch timer;
- win-condition analysis futura;
- CMP/timing futuro;
- Sac Swap / Catch Assist como conceito futuro;
- alertas críticos;
- last action;
- replay/diagnóstico.

### Shield Advisor

Não mostrar apenas `SHIELD`.

Preferir motivo curto:

- `SHIELD — provável KO`;
- `SEM SHIELD — risco aceitável`;
- `BAIT POSSÍVEL`;
- `SUPER EFETIVO`.

### Bait

Quando houver energia para um golpe barato e um golpe forte plausível, sinalizar risco de bait em vez de fingir saber qual será usado.

### Switch Advisor

Recomendação deve incluir explicação curta de matchup/resistência, sem poluir a tela.

---

## 11. Timer de troca

Manter relógio independente para troca do jogador/adversário quando houver evento confiável de switch.

Requisitos:

- duração parametrizada pela regra/copa e não hardcoded em UI;
- restaurar corretamente após troca;
- aparecer de forma discreta;
- modo somente alertas pode exibir apenas nos segundos finais.

---

## 12. Replay e diagnóstico

Registrar timeline local normalizada, por exemplo:

- Pokémon identificado;
- Fast Move observado;
- Charged Move confirmado;
- energia antes/depois;
- troca;
- correção de sincronização;
- faint;
- decisão/alerta produzido.

Objetivos:

- rever a luta;
- encontrar erros de OCR;
- ajustar detector;
- criar testes reproduzíveis;
- melhorar o app sem depender de adivinhação.

Nenhuma credencial ou dado sensível deve ser gravado no replay.

---

## 13. Modo treino

Deve existir uma forma de testar o core sem Pokémon GO aberto:

- selecionar equipes;
- registrar Fast Moves manualmente;
- disparar Charged Moves;
- simular switch/faint;
- testar energia/alertas/replay;
- validar o overlay/decisões sem uma batalha real.

Isso reduz muito o custo de testar mudanças.

---

## 14. PGSStats / próprio time

PvPPokeGo deve consumir somente a parte PvP útil do PGSStats:

- espécie/forma;
- CP;
- IV ATK/DEF/STA;
- Fast/Charged Moves atuais;
- battle parties;
- shiny/lucky/shadow/purified como metadados quando úteis;
- tags/favorite opcionalmente.

Objetivos:

- carregar o próprio time antes da batalha;
- reduzir OCR do lado do jogador;
- comparar moves atuais/recomendados;
- PvP IV rank;
- Team Manager/Collection PvP;
- Resource Planner para investimento.

---

## 15. Análises dos APKs/repositórios que alimentaram o PvPPokeGo

### Poke Genie

Conceitos aproveitáveis:

- OCR/captura;
- `OcrHelper`/IVision;
- Battle Team overlay;
- PvP IV;
- movesets/rankings;
- type effectiveness;
- Battle Simulator;
- dados locais de Pokémon/moves/evolução/CPM.

Usar apenas como referência arquitetural/funcional; não copiar binários ou código proprietário/modificado.

### PGSharp-like APK

Achados registrados em `docs/PGSHARP_BATTLE_FINDINGS.md` incluem:

- `instantRocket`;
- `rocket.fast`;
- `rocket.charge`;
- `rocket.shield`;
- `battleAssistant`;
- `CombatVNextBattleStateShim`;
- `GymBattleState`;
- `RaidLobbyState`;
- `GymRootState`;
- `Minigame`;
- `StartBattle`;
- `SwapPokemon`;
- `AttackTelegraph`;
- `ActorMove`;
- `BattleEnd`;
- `Charge2`;
- `SpecialCharge`;
- referências separadas a Max Battle fast/charge/dodge/orb.

Essas strings são pistas de estados/módulos, não fonte de implementação.

### iPogo

Aproveitar somente conceitos de:

- overlay;
- scanners/estado;
- organização de opções de batalha/skip;
- UX de módulos independentes.

### Pokemod

Aproveitar no PvPPokeGo apenas conceitos genéricos úteis a diagnóstico/conta/scan/logs quando fizer sentido. Root/injector/concealment ficam fora.

### PGTools/Shungo

Para PvPPokeGo aproveitar apenas ideias genéricas de:

- filtros;
- IV/stats;
- notifications;
- prioridades/configuração.

Farm, localização, hunter e automações geográficas permanecem no PGLOT.

### PvPoke / pokego-pvp / gopvpsim / Ohbem

Principais referências para:

- Battle Simulator;
- PvP IV rank;
- breakpoints/bulkpoints;
- move timing;
- matchups/counters;
- validação cruzada de algoritmos.

---

## 16. Battle/Rocket/Raid/Max — material recuperado dos chats

Os tópicos antigos também separaram funções de batalha observadas em APKs para possível reaproveitamento conceitual:

### Rocket

Estados conceituais separados para:

- fast attack;
- charged attack;
- shield;
- detecção de batalha/prompt.

### Raid/Ginásio

Referências de estado/lobby podem orientar um detector visual separado.

### Max Battle

Tratar como módulo separado, pois há conceitos distintos de:

- fast;
- charged;
- dodge;
- orb.

### Regra de escopo atual

O núcleo PvP deve continuar prioridade. Rocket/Raid/Max entram como módulos separados somente depois que a base de reconhecimento/estado estiver sólida, reutilizando infraestrutura genérica de visão/estado sem misturar regras de combate.

---

## 17. Modos de operação discutidos

Historicamente foram discutidos:

- `ASSIST`: recomendações e overlay;
- `SEMI_AUTO`: algumas ações automatizadas e outras manuais;
- `AUTO_BATTLE`: automação ampla.

Estado ativo do projeto: priorizar **assistência externa, reconhecimento, cálculo e recomendação**, mantendo o core separado de injeção/concealment/bypass. Qualquer executor Android futuro deve permanecer modular e não fazer parte do motor de decisão.

---

## 18. Roadmap histórico recuperado — fases 1 a 10

### F1 — Overlay estável

- foreground capture;
- pass-through;
- escala/opacidade;
- estabilidade.

### F2 — OCR nome/CP

- próprio Pokémon;
- inimigo;
- ROIs;
- estabilização.

### F3 — tipos/matchup/reservas

- tipos;
- efetividade;
- indicadores verde/cinza/vermelho;
- reconhecimento/correção de reservas.

### F4 — triângulo/controles de ataques

- Fast Move;
- Charged 1;
- Charged 2;
- progresso/contagem.

### F5 — seletores `▼`

- pequenas regiões touchable;
- listar somente golpes legais;
- troca manual rápida.

### F6 — Fast Move detector

- ROI leve;
- score/confiança;
- correção manual;
- não usar frame-count ingênuo.

### F7 — energia/charged logic

- energia 0–100;
- residual;
- fasts necessários;
- double-charge;
- faixa/correção.

### F8 — identificação automática de golpes

- charged banner OCR;
- aliases localizados;
- Fast Move inference;
- confirmação/predição.

### F9 — swaps/timers

- troca adversária;
- restauração do slot;
- cooldown/timer;
- estado dos três inimigos.

### F10 — polimento/build

- persistência;
- diagnósticos;
- replay;
- performance;
- perfis de layout;
- APK debug/release testado e assinado quando a camada Android estiver integrada.

---

## 19. Estado atual do core após a migração

Componentes já existentes ou criados durante esta consolidação/continuação:

- `BattleSession`;
- `BattleEnergyTracker`;
- `BattleObservationProcessor`;
- `BattleTextEventParser`;
- `VisionObservation` / estabilização;
- `BattleDecisionEngine`;
- `BattleAutomationController` como motor/intenção desacoplado;
- `BattleConfidenceEngine`;
- `BattleAssistEngine`;
- `BattleReplayRecorder`;
- `BattleExperienceModels`;
- `BattleTrainingController`;
- `EvidenceLevel`;
- `EnergyRangeEstimate`;
- confirmed/predicted charged move state;
- energia residual;
- correção de energia;
- double-charge respeitando limite de 100;
- seletor lógico independente dos dois Charged Moves;
- OCR charged banner -> move ID;
- deduplicação de banners repetidos.

Importante: existência no core não significa que cada função já esteja integrada ao APK Android/overlay visual.

---

## 20. Pendências prioritárias preservadas dos chats

P0/P1 imediato:

1. build/test runner para validar todo o core atual;
2. restaurar/ligar camada Android ao core atual;
3. overlay touch-safe com seletores `▼`;
4. mostrar PREVISTO/PROVÁVEL/CONFIRMADO;
5. mostrar faixa de energia;
6. double-charge visual;
7. switch timer visual;
8. modo somente alertas;
9. replay/última ação na UI;
10. PGSStatsImporter + battle parties;
11. PvPIvRankEngine;
12. MoveTimingEngine;
13. CounterAdvisor/ShieldAdvisor final;
14. recognition de reservas;
15. Fast Move detector confiável;
16. perfis de liga/copa/layout;
17. debug visual opcional;
18. APK Android realmente compilado/testável.

Não considerar como concluído só porque está em documentação.

---

## 21. O que NÃO migrar para o PvPPokeGo

Dos chats antigos e das análises de outros APKs, estas funções pertencem ao PGLOT ou ficam fora deste projeto:

- spoofing/teleporte;
- cooldown geográfico;
- mapa/feed mundial;
- coordenadas/favoritos geográficos;
- GPX/rotas;
- ninhos;
- hunter/snipe;
- farm automático;
- auto-catch/spin/transfer/discard;
- GO Plus/VGP;
- injeção/root/concealment;
- licenças/premium bypass;
- credenciais/tokens/endpoints privados;
- cópia de DEX/SO/JAR/firmware proprietário.

A única coisa compartilhada com PGLOT deve ser código/modelos genéricos próprios quando fizer sentido, como dados Pokémon/moves/tipos, parser PGSStats, PvP IV engine e ResourcePlanner base.

---

## 22. Regra de continuidade daqui para frente

Este documento + `docs/SINGLE_THREAD_MASTER_CONTEXT_2026-09-26.md` passam a ser a referência durável do que veio dos chats:

- “APK pronto para baixar”;
- “Funções Para Dois APKs”;
- continuação no “Projeto PVPPokeGo”.

Novas decisões devem ser aplicadas ao repositório e registradas no contexto mestre, evitando voltar a dividir o raciocínio entre chats.
