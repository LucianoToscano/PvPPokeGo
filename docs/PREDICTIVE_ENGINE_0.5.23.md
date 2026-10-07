# PvPPokeGo 0.5.23 — Predictive Enemy Energy / Charged Move Engine

## Objetivo

A 0.5.23 adiciona uma camada preditiva ao assistente visual. O app continua externo ao Pokémon GO: observa a captura de tela, mantém estado local e desenha o HUD; não lê memória do jogo, não injeta código e não envia toques.

O objetivo matemático é responder duas perguntas separadas:

1. **Quando cada Ataque Carregado inimigo pode ficar disponível?**
2. **Qual é o risco desse ataque para o Pokémon atual se não houver escudo?**

Energia e dano não são confundidos. Energia usa ganho/duração do Fast Move e energia residual. Dano usa stats, power, STAB, efetividade, Shadow e estágios de Attack/Defense.

## Fonte reproduzível

Snapshot PvPoke fixado no commit:

`f627e89e53c0c7b903fff097df7a0ad0ac95decc`

Esse commit era o HEAD de `pvpoke/pvpoke:master` consultado em 06/10/2026 e foi criado em 05/10/2026.

O empacotador `tools/package_offline_pvpoke_assets.py` registra:
- commit PvPoke;
- data/hora UTC do snapshot;
- duração do turn = 500 ms;
- `maxBuffStages=4`;
- `buffDivisor=4`;
- SHA-256 do Game Master, CPMs e rankings empacotados.

Os rankings 1500/2500/10000 continuam sendo baixados da fonte pública do PvPoke no build e recebem hash para permitir auditoria posterior.

## Mecânica usada

### Turns e Fast Moves

Um turn PvP é 500 ms.

O predictor recebe **Fast Moves concluídos/detectados**, não toques físicos. O detector visual continua fazendo a deduplicação por queda de HP/cadência/gates. O predictor adiciona uma segunda proteção por `eventId`.

Para uma hipótese com energia `E`, ganho `G`, duração `T` turns e Charged de custo `C`:

```
faltante = max(0, C - E)
fasts = ceil(faltante / G)
turns = fasts * T
tempo = turns * 500 ms
```

A diferença da implementação simples é que **cada Fast Move legal mantém uma hipótese própria**.

### Hipóteses discretas

`EnemyEnergyTracker` guarda, por hipótese:
- Fast Move;
- energia exata daquela hipótese;
- Fast Moves concluídos;
- turns decorridos;
- peso/confiança.

Se o Fast Move ainda não é conhecido, nenhuma hipótese é escolhida arbitrariamente. Cadência observada repondera as possibilidades.

Quando um Charged exato é revelado:
- hipóteses incapazes de pagar o custo são removidas;
- o custo é descontado só das hipóteses válidas;
- os pesos são renormalizados.

Quando o Charged não é identificado:
- o estado ramifica pelos custos legais que poderiam ter sido pagos;
- estados equivalentes são mesclados;
- o conjunto é limitado para manter custo baixo no hot path.

### Lag / frame perdido / Fast "flutuante"

Se um Charged real prova que todas as hipóteses estavam com energia insuficiente, o sistema não zera cegamente. Ele reconstrói o menor número de Fast Moves perdidos necessário para tornar o Charged possível e mantém também uma hipótese de overfarm adicional.

Após uma lacuna real de captura, o estado pode ser alargado com 0..N Fast Moves possíveis. Depois do Charged do próprio jogador, mantém 0/1 Fast inimigo possível para representar o caso de registro durante a sequência, sem afirmar que aconteceu.

### Trocas

O snapshot de energia preditiva fica em `BattlePokemonState`. Ao sair:
- energia e hipóteses são preservadas;
- Attack/Defense stages são zerados, como exige a troca.

Ao voltar:
- energia e hipóteses daquele Pokémon são restauradas;
- não herda estado de outro Pokémon.

`BattleStateStore.clearBattle()` remove tudo ao confirmar uma nova batalha.

## ChargedMovePredictor

Para todos os Charged Moves legais da espécie/forma, calcula:
- energia atual min/likely/max;
- Fast Moves restantes min/likely/max;
- turns restantes min/likely/max;
- janela mais cedo e provável;
- READY possível e READY certo;
- possibilidade de dois Chargeds já armazenados;
- EPT do(s) Fast(s);
- DPT do(s) Fast(s);
- DPE do Charged;
- dano de Fast até a janela do Charged;
- risco de farm down antes do Charged;
- bait potencial;
- pressão de escudo;
- faixa de dano do Charged;
- risco de KO;
- confidence e classe de evidência.

Os movepools completos vêm do Game Master. O uso do ranking PvPoke é apenas um **prior heurístico**; um golpe legal não é apagado só por não ser o moveset #1.

## Dano

`DamageForecastEngine` segue a estrutura do cálculo PvP do PvPoke:
- power;
- STAB;
- Attack/Defense;
- type effectiveness;
- PvP bonus;
- Shadow Attack/Defense;
- stages -4..+4;
- `percentMaxHP` quando especificado pelo Game Master.

IV/level ocultos não viram falsa precisão: o HUD continua mostrando intervalo. Dano realmente observado na própria batalha tem prioridade sobre o modelo.

## Buffs/debuffs e CMP

`MoveDef` lê:
- `buffs`;
- `buffTarget`;
- `buffApplyChance`;
- `damageMethod`;
- `archetype`.

`StatStageTracker`:
- aplica proc garantido exatamente;
- para proc probabilístico mantém as duas possibilidades em uma faixa;
- limita em -4..+4;
- zera stages em troca.

CMP é separado: a comparação usa Attack de batalha, sem aplicar o multiplicador de stage, acompanhando a implementação atual do PvPoke.

## Antecedência humana

`ReactionWindowEstimator` mede no aparelho a latência captura -> análise e usa p95 depois de amostras suficientes.

Lead time:

```
p95Pipeline
+ overlayBudget
+ humanReactionAllowance
+ safetyMargin
```

Bootstrap antes da calibração:
- pipeline: 650 ms;
- reação humana: 450 ms;
- render/overlay: 80 ms;
- margem: 250 ms.

Esses valores são pressupostos explícitos e separados; o pipeline deixa de ser fixo assim que há amostras. O resultado é convertido em turns de 500 ms.

## HUD

O bloco de ataques inimigos mantém o desenho compacto. Abaixo da energia pode mostrar até duas ameaças:

```
⚡ 42-48 E
Aqua Tail   READY?
Earthquake  +2 FAST • ~2.0s
```

O assistente gera:
- **PREPARE-SE** quando a ameaça entra na janela de reação;
- **PERIGO / READY** quando algum estado matemático já pode pagar o Charged.

A faixa fantasma de HP pode aparecer **antes** da animação de Charged quando a ameaça entra na janela segura, mostrando dano sem escudo e HP restante estimado.

## Aprendizado local e privacidade

A 0.5.23 não envia histórico do usuário para servidor de IA. O histórico local:
- salva estados de energia/shields;
- salva hipóteses, janela e threat da previsão;
- registra decisões PREPARE/READY;
- mantém padrões observados de Charged do adversário.

Isso prepara um dataset exportável para análise/treino futuro, mas qualquer agregação entre usuários deve ser opt-in explícito em versão futura.

## Performance

- movepools legais e priorizados ficam em cache por espécie/forma;
- estimativas de stats ficam em cache por espécie/PC/liga;
- atualização de energia é incremental por evento, não por frame;
- hipóteses equivalentes são mescladas;
- limite de 32 hipóteses;
- cálculos pesados continuam fora da UI thread junto do pipeline existente.

## Testes adicionados/expandidos

Cobertura determinística inclui:
- Fast 1/2/3/4 turns;
- ganhos de energia diferentes;
- residual/overfarm;
- dois Chargeds seguidos;
- Fast desconhecido;
- Charged conhecido/unknown branching;
- deduplicação de Fast por eventId;
- recuperação após Fast perdido;
- lacuna de captura;
- switch/restore;
- nova batalha limpa estado;
- janela earliest/likely/latest;
- lead p95 -> reaction turns;
- buffs garantidos;
- buffs probabilísticos;
- debuffs;
- reset de stages;
- CMP;
- Shadow;
- `percentMaxHP`;
- gates já existentes para Charged duplicado, continuidade, matcher, reservas, shields e dano observado.

## Limites honestos

A previsão não sabe IV/nível escondido, intenção do adversário, lag futuro ou se um proc aleatório realmente aconteceu sem evidência visual. Nesses casos usa range/hipóteses/confidence. Nenhuma heurística de ranking ou histórico deve ser exibida como fato observado.
