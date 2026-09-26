# PvPPokeGo — Feature Allocation

Atualizado em: 2026-09-26

Este documento concentra somente o que vale a pena entrar no PvPPokeGo a partir dos APKs analisados, PGSStats.json, PGSData.dat, PvPoke/Poke Genie e pesquisas de OCR/PvP. Funções de localização, farm e GO Plus ficam fora do core.

## Objetivo do PvPPokeGo

O PvPPokeGo é o assistente de batalha: overlay, identificação visual, time próprio, oponente, fast/charged moves, energia, escudos, matchups, IV/rank e simulação local.

## Deve entrar no PvPPokeGo

### 1. Importação de coleção via PGSStats

Compartilhar o parser base com PGLOT, mas consumir somente campos úteis para PvP.

Criar/usar:
- `PGSStatsImporter` compartilhado;
- `ImportedPokemon`;
- `ImportedBattleParty`;
- `PvPCollectionAdapter`.

Campos necessários por Pokémon:
- espécie/forma;
- CP;
- IV ATK/DEF/STA;
- move1/move2/move3;
- shiny/lucky/shadow/purified quando disponível no modelo normalizado;
- favorite/tags apenas como metadado opcional;
- Mega/form state quando relevante.

Benefícios:
- carregar automaticamente o próprio time;
- evitar OCR do lado do usuário quando o Pokémon já estiver importado;
- comparar moves atuais com moveset recomendado;
- calcular rank de IV localmente;
- montar coleção PvP e favoritos de batalha.

### 2. Importação de battle parties

Usar as equipes salvas no PGSStats para pré-preencher:
- Great League;
- Ultra League;
- Master League;
- times de raid somente se forem úteis para análise de counters, sem trazer automação de raid.

O usuário deve poder escolher qual party vira o time ativo no overlay.

### 3. PvPIvRankEngine

Referências: PvPoke, pokego-pvp, Ohbem e implementações de validação cruzada.

Requisitos:
- 4096 combinações de IV;
- CP cap por liga;
- level cap 40/41/50/51;
- stat product;
- ranks empatados;
- IV floors configuráveis;
- evoluções/formas/Megas quando aplicável;
- indicação de rank do exemplar e rank da espécie separadamente.

### 4. MoveTimingEngine

Usar moves importados do PGSStats quando forem conhecidos.

Calcular:
- energia gerada por fast move;
- custo de charged move;
- fast moves necessários;
- turnos/tempo aproximado;
- candidatos possíveis do oponente;
- energia acumulada por Pokémon adversário.

### 5. BattleSimulator / CounterAdvisor

Referências: PvPoke, gopvpsim e pokego-pvp.

Funções:
- matchup;
- fraquezas/resistências;
- cenários de shield;
- dano/energia;
- recomendação de troca;
- recomendação de charged move;
- ameaça do move inimigo;
- breakpoints/bulkpoints em módulo separado.

### 6. OCR/visão

Poke Genie continua como principal referência arquitetural de overlay/OCR. Complementar com scanners de screenshot/vídeo.

Pipeline:
`captura -> ROI -> observações com confiança -> estabilização -> eventos de batalha -> BattleSession -> decisão -> overlay`

OCR deve focar principalmente em:
- Pokémon adversário;
- troca adversária;
- HP aproximado;
- shields/estado visual;
- elementos necessários para sincronizar a batalha.

Para o próprio time, preferir dados importados do PGSStats quando disponíveis.

### 7. Overlay modular

Exibir:
- nome/ícone do oponente;
- possíveis fast/charged moves;
- quantidade de fast moves até cada charged;
- energia estimada;
- fraquezas/resistências;
- sugestão de shield;
- melhor resposta do seu time;
- botão de correção manual rápida;
- memória dos três Pokémon adversários.

#### Requisito de UX — overlay não intrusivo

O overlay nunca deve ficar ocupando a tela sem necessidade.

Comportamento obrigatório:
- botão **Ocultar** no painel completo;
- ao ocultar manualmente, reduzir para uma **bolha mínima** reposicionável em vez de manter o painel grande;
- tocar na bolha deve restaurar o painel imediatamente;
- ação **Mostrar** e **Ocultar** também disponível pela notificação persistente, como rota de recuperação;
- **auto-ocultar/minimizar** quando o app não detectar uma batalha ativa ou não houver informação útil para exibir por alguns segundos;
- **auto-restaurar** quando uma batalha/estado relevante voltar a ser detectado, exceto quando o usuário tiver ocultado manualmente;
- respeitar preferência do usuário: `overlayAutoHide`, `overlayManualHidden`, tamanho/posição da bolha e posição do painel;
- fora da batalha, o estado preferido é minimizado/oculto;
- durante menus, troca de aplicativo, lobby, tela inicial ou períodos sem leitura útil, não cobrir a interface desnecessariamente;
- o estado de visibilidade deve ser separado do estado do `BattleSession`, para esconder UI sem perder energia/contagens já rastreadas.

Estados sugeridos:
`FULL -> COLLAPSED -> AUTO_HIDDEN`, com retorno para `FULL` quando apropriado.

A ocultação deve afetar apenas a interface. `BattleEnergyTracker`, memória dos três adversários e sessão continuam preservados enquanto a batalha estiver ativa.

### 8. Collection PvP / Team Manager

A partir de PGSStats:
- lista dos Pokémon da coleção que servem para cada liga;
- rank de IV;
- CP atual e CP alvo;
- moves atuais/recomendados;
- custo estimado de power-up/segundo golpe;
- times salvos;
- favoritos PvP.

### 9. ResourcePlanner compartilhável

A Calculadora de Stardust não entra como ferramenta principal de PvP, mas sua lógica pode alimentar um planejador de investimento:
- Stardust disponível importado da conta;
- custo para level alvo;
- segundo golpe;
- XL candy;
- comparar custo de vários candidatos.

### 10. StorageSearchGenerator

Compartilhável com PGLOT.

Gerar strings de busca oficiais do Pokémon GO para:
- tipos;
- move slots;
- favoritos;
- filtros PvP úteis;
- localizar rapidamente candidatos dentro do jogo.

## Compartilhar com PGLOT

Somente componentes genéricos próprios:
- parser PGSStats;
- `ImportedPokemon`;
- `ImportedBattleParty`;
- dados Pokémon/formas/moves/tipos;
- PvP IV engine;
- efetividade;
- move data;
- `StorageSearchGenerator`;
- logs/stats genéricos;
- ResourcePlanner base;
- padrão de estados de visibilidade do overlay, sem compartilhar lógica de localização/farm.

## Não entra no PvPPokeGo

Não trazer do PGSData/PGLOT:
- coordenadas;
- favoritos geográficos;
- rotas/GPX;
- ninhos;
- mapa global/feed;
- scanner de mundo;
- hunter/snipe;
- spoofing/cooldown/teleporte;
- farm;
- GO Plus/VGP;
- auto-catch/spin;
- Rocket automation;
- raid automation;
- root/injeção/companion privilegiado;
- licenças, autenticação, endpoints privados ou qualquer segredo do PGSData.

## Prioridade PvPPokeGo

### P0
1. `PGSStatsImporter` compartilhado;
2. `PvPCollectionAdapter`;
3. import de battle parties;
4. `PvPIvRankEngine`;
5. `MoveTimingEngine`;
6. overlay com time próprio pré-carregado;
7. controles Ocultar/Mostrar + auto-hide do overlay.

### P1
8. `BattleSimulator`;
9. `CounterAdvisor`;
10. `BreakpointEngine`;
11. Collection PvP/Team Manager;
12. ResourcePlanner.

### P2
13. scanner de coleção por screenshot/vídeo;
14. geração avançada de strings de busca;
15. analytics/replay/debug de batalha.

## Regra

PvPPokeGo continua externo ao processo do Pokémon GO, sem root, injeção, concealment ou bypass. Código próprio, com dados e algoritmos testáveis separadamente de Android/OCR.