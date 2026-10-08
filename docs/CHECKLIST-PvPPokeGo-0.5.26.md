# CHECKLIST — PvPPokeGo 0.5.26

> Guia Mestre canônico de continuidade e registro:
> https://github.com/LucianoToscano/PvPPokeGo/blob/release/0.5.26/docs/PROJECT_CONTINUITY_GUIDE.md
>
> Toda mudança marcada neste checklist também deve ser registrada no Guia Mestre.

Atualizado em: 2026-10-07  
Branch: `release/0.5.26`  
Head de referência: `356f35cf11008921f500ad8fffdcfb891dc344fc`  
PR: #34  
Pacote: `com.lucianotoscano.pvppokego`  
versionName: `0.5.26`  
versionCode: `31`

> Fonte de planejamento: documento consolidado “PvPPokeGo — ideias, melhorias e evolução — itens 1 a 80”.
> Este checklist separa claramente implementação, validação parcial e pendências. Não transforma orientação em funcionalidade concluída sem evidência.

## Legenda

- ✅ Implementado no código e/ou validado em lógica.
- 🟡 Parcial: existe base funcional, mas falta completar, refinar ou validar no aparelho.
- ⬜ Ainda não implementado.
- ⏸ Adiado por decisão do projeto.

## Checklist 1–80

| # | Item | Status | Estado atual |
|---:|---|:---:|---|
| 1 | Auditoria da versão atual e continuidade | ✅ | Branch, versão, commits, PR, checkpoints e componentes auditados. |
| 2 | Matemática de turns e energia | ✅ | Energia, custo, residual, turns, cap 100 e cálculos por movimento implementados. |
| 3 | Contagem de Fast + deduplicação | ✅ | Eventos duplicados não geram energia duas vezes; IDs/timestamps e hipóteses preservados. |
| 4 | Energia adversária por Pokémon | ✅ | Estado/energia preservado por Pokémon inclusive após troca/retorno. |
| 5 | Hipóteses de Fast Move/moveset | ✅ | Múltiplas hipóteses; ranking como prior; cadência e dano reponderam candidatos. |
| 6 | Previsão de Charged Moves | ✅ | Predictor calcula READY, +N Fast, ranges, turns e confiança. |
| 7 | Residual, overfarm e ataques consecutivos | ✅ | Energia não zera; residual, overfarm, back-to-back e segunda carga visual existem. |
| 8 | Timeline, trocas e continuidade | ✅ | Continuidade, rematch, retorno da reserva e nova batalha tratados. |
| 9 | Alertas antecipados / reação | ✅ | ReactionWindowEstimator, turns restantes e lead time implementados; calibração física ainda pode melhorar. |
| 10 | Dano e matchup | ✅ | Tipos, STAB, Shadow, stages, dano observado/modelado e matchup presentes. |
| 11 | Preview de dano sobre HP | 🟡 | Engine + HUD implementados; falta validar posição/legibilidade em aparelho real. |
| 12 | Threat Forecast | ✅ | Energia + dano + KO + Fast pressure + confidence integrados. |
| 13 | Shield Assistant | ✅ | ShieldDecisionEngine retorna SHIELD/HOLD/OPTIONAL com motivo/confiança. |
| 14 | Reconhecimento visual de Pokémon | 🟡 | Busca mantém cards ambíguos em análise e reutiliza retratos estáveis; 18 testes direcionados passaram. QA em APK/aparelho pendente. |
| 15 | Matchup automático | ✅ | Recalcula tipos/matchup após identificação/troca. |
| 16 | Assistente de próxima ação | 🟡 | Advisor, Farm, Shield, Sac Swap e timing existem; planejador completo ainda não. |
| 17 | Aprendizado de padrões | 🟡 | Histórico/pattern hints existem; aprendizado estatístico/ML amplo ainda não. |
| 18 | Confidence Score | ✅ | Confidence por detector, predictor, dano, energia e reservas. |
| 19 | Battle State Confidence | ✅ | Battle confidence, detector confidence e consistency score presentes. |
| 20 | Modos Minimal / Normal / Advanced | 🟡 | Configurações e overlay existem; três perfis formais ainda não fechados. |
| 21 | Replay/análise pós-batalha | 🟡 | Histórico/exportação existem; replay completo ainda não. |
| 22 | Compatibilidade com gravação de tela | ⏸ | Adiado para não bloquear o restante da 0.5.26. |
| 23 | Banco de dados universal | ✅ | Game Master/PvPoke offline, espécies, formas, movimentos, rankings e fingerprints. |
| 24 | Fontes de referência | ✅ | PvPoke, GoPvPSim, PokéMoves, GBL Analyzer, Pokebattler e comunidades pesquisados. |
| 25 | PvPoke como referência matemática | ✅ | Fonte pinada por commit e usada para dados/paridade. |
| 26 | Arquitetura modular | ✅ | Trackers/predictors/forecast separados em módulos testáveis. |
| 27 | Desempenho e latência | 🟡 | Caches e processamento orientado a eventos; profiling físico ainda falta. |
| 28 | Testes e regressão | ✅ | Suíte determinística atual: 132 PASS / 0 FAIL; build Android final ainda pendente. |
| 29 | Dano + Shield + Energia integrados | ✅ | Forecast, ShieldDecision e threat usam o mesmo estado matemático. |
| 30 | Previsão de HP futuro | 🟡 | Dano restante/farm forecast existem; projeção multi-turn completa dos dois HP ainda não. |
| 31 | Farm Down | ✅ | TacticalForecastEngine.farmDown implementado. |
| 32 | Contador inteligente | ✅ | Fast count alimenta energia, Charged, overfarm e threat; não é contador isolado. |
| 33 | Identificação por comportamento | ✅ | Cadência + HP/dano + moves legais + Charged observado refinam Fast provável. |
| 34 | Detecção de bait | 🟡 | baitPotential/baitProbability existem; heurística ainda inicial. |
| 35 | Overfarm inteligente | ✅ | Energia além do custo e sequência residual acompanhadas. |
| 36 | CMP | ✅ | CMP por intervalo de Attack/IV; não finge conhecer IV oculto. |
| 37 | Tipos/fraquezas/resistências em tempo real | ✅ | Recalculados com o Pokémon ativo. |
| 38 | Bench intelligence | ✅ | Reservas preservam identidade, energia, moves, HP/status e retorno. |
| 39 | Timeline como fonte central | 🟡 | Há continuidade/history/events; ainda não é um único event stream formal para tudo. |
| 40 | Checkpoints inteligentes | ✅ | Estado preditivo/energia/hipóteses sobrevivem via snapshots. |
| 41 | Modo Debug | 🟡 | debugText/informações internas existem; falta modo técnico organizado. |
| 42 | Logs úteis e não excessivos | 🟡 | Logs/histórico existem; padronização orientada a eventos ainda pode melhorar. |
| 43 | Replay offline de desenvolvimento | ⬜ | Ainda não existe player completo de gravação/frame/eventos. |
| 44 | Dataset de batalhas/fixtures | 🟡 | Há fixtures unitários; falta dataset real organizado de batalhas. |
| 45 | Calibração por dispositivo | 🟡 | Reaction timing existe; perfil p95 por aparelho ainda não. |
| 46 | Detecção de frame atrasado | 🟡 | Capture health/stale existem; idade/rajada ainda pode ser refinada. |
| 47 | OCR como evidência, não fonte única | ✅ | OCR é cruzado com visual, timeline e matemática. |
| 48 | Score de consistência | ✅ | consistencyScore + anomalyCount implementados. |
| 49 | Recuperação de estado | ✅ | Frames perdidos/Charged impossível expandem hipóteses sem zerar tudo. |
| 50 | Previsão em intervalos | ✅ | Energia, dano e turns usam ranges quando necessário. |
| 51 | Separar fato, inferência e estratégia | ✅ | PredictionEvidenceClass e estados separados existem. |
| 52 | Recomendação com explicação curta | ✅ | Shield/Sac Swap/Advisor retornam razões curtas. |
| 53 | Qualidade das previsões | 🟡 | Estrutura permite medir; métricas agregadas completas ainda faltam. |
| 54 | Possibilidade ≠ intenção | ✅ | READY/POSSIBLE/ESTIMATE/OBSERVED tratados separadamente. |
| 55 | Dois Chargeds seguidos | ✅ | doubleReadyPossible, residual e sequências implementados. |
| 56 | Bait + move forte | 🟡 | Múltiplas ameaças coexistem; probabilidade comportamental ainda será refinada. |
| 57 | Previsão estratégica de Switch | ⬜ | Ainda não implementada. |
| 58 | Switch Timer | ✅ | Estado de timer próprio/adversário já faz parte do BattleUiState. |
| 59 | Reconhecimento de catch de Charged | ⬜ | Ainda não há detector específico. |
| 60 | Banco local + atualização remota | 🟡 | Banco offline + cache existem; atualização assinada/versionada ainda pode melhorar. |
| 61 | Cache | ✅ | Caches por espécie/moves/stats/range. |
| 62 | Fontes externas fora do hot path | ✅ | Batalha usa dados locais; web não é necessária por ataque. |
| 63 | Privacidade de dados | 🟡 | Não depende de coleta de terceiros hoje; consentimento formal será necessário para dataset colaborativo. |
| 64 | Não usar IA onde matemática resolve | ✅ | Arquitetura atual segue esse princípio. |
| 65 | Simulação curta de cenários | 🟡 | Tactical/Shield/Farm fazem cenários locais; busca geral de futuros ainda não. |
| 66 | Não sobrecarregar overlay | 🟡 | Mantido compacto; precisa revisão visual após novas features. |
| 67 | Prioridade de alertas | 🟡 | Advisor prioriza ações; fila/prioridade global ainda pode evoluir. |
| 68 | Alertas visuais discretos | 🟡 | Cores/ícones/HUD existem; vibração/opções específicas ainda não fechadas. |
| 69 | Acessibilidade visual | 🟡 | Escala/posição existem; daltonismo/contraste dedicados ainda faltam. |
| 70 | Histórico como treinador | 🟡 | Histórico existe; resumo pós-batalha de decisões ainda não. |
| 71 | Previsão × realidade | 🟡 | Estrutura permite registrar; painel de métricas ainda não pronto. |
| 72 | Corrigir bugs encontrados | ✅ | Bugs encontrados vêm sendo corrigidos com registro/testes quando aplicável. |
| 73 | Não implementar tudo de uma vez | ✅ | Trabalho dividido em etapas/commits verificáveis. |
| 74 | Auditoria objetiva por ideia | ✅ | Handoffs/checkpoints registram estado e próximo passo. |
| 75 | Continuar da versão real | ✅ | Desenvolvimento segue a linha atual 0.5.26. |
| 76 | Não perder trabalho atual | ✅ | Branches/checkpoints e baselines antigas preservados. |
| 77 | GitHub + Google Drive | 🟡 | Repositório/documentação atualizados; entrega final com APK ainda falta. |
| 78 | Checkpoint para novo chat | ✅ | Handoffs e checkpoints de release mantidos. |
| 79 | Assistente inteligente de batalha | 🟡 | Núcleo avançado existe; replay, treino avançado e modelos comportamentais ainda faltam. |
| 80 | Princípios finais | ✅ | Ranges, matemática, não inventar intenção, preservar código e evitar regressão. |

## Placar atual

- ✅ Feitos/essencialmente implementados: **50**
- 🟡 Parciais/em evolução: **25**
- ⬜ Ainda não implementados: **4**
- ⏸ Adiado propositalmente: **1**

## Pendências mais claras

1. **#43 — Replay offline completo**
2. **#57 — Previsão estratégica de switch**
3. **#59 — Reconhecimento de catch de Charged**
4. Evoluir dataset/replay/ML e métricas dos itens parciais

## Próxima sequência recomendada

1. #43 Replay offline
2. #44 Dataset/fixtures reais
3. #45–46 Calibração + frames atrasados
4. #53 e #71 Métricas de precisão
5. #20 Modos de overlay
6. #70 Histórico como treinador
7. #57 e #59 Switch/catch
8. Modelos comportamentais somente depois da base determinística

## Regra de atualização deste arquivo

A cada mudança relevante:
1. atualizar o status do item;
2. registrar o commit correspondente;
3. manter o placar coerente;
4. não marcar ✅ sem evidência no código/teste;
5. manter o APK/build como etapa separada de implementação lógica.


---

# CHECKLIST COMPLEMENTAR — Predictive Battle Engine (PBE)

Fonte adicional: `PVPPokeGo_Motor_Matematico_Preditivo.txt`.

Este bloco não substitui o checklist 1–80. Ele detalha o núcleo matemático/preditivo e permite acompanhar separadamente o que já existe, o que está parcial e quais melhorias devem ser incorporadas sem criar uma segunda engine paralela.

| PBE | Componente | Status | O que já existe / como aprimorar |
|---:|---|:---:|---|
| 1 | Modelo de estado da batalha `S=(t,n,E,H,Sh,M,F,P)` | 🟡 | O projeto já possui BattleUiState, estados por Pokémon, energia, HP, shields, moveset e FastMovePhase. Falta consolidar um snapshot preditivo único com turno lógico e distribuição explícita de próximas ações. |
| 2 | Energia exata em tempo real | ✅ | Energia residual, cap 100 e subtração do custo real já existem e sobrevivem a trocas. |
| 3 | Fórmula universal de contagem | ✅ | Fast necessários, turns e tempo lógico são calculados a partir de energia/custo/ganho/duração. |
| 4 | Segundo/terceiro Charged com residual | ✅ | EnergySequenceMath e trackers não zeram energia; back-to-back e sequências residuais já são calculados. |
| 5 | Energia como intervalo | ✅ | EnemyEnergyTracker mantém hipóteses e EnemyEnergyForecast expõe min/likely/max. |
| 6 | Máquina de hipóteses de ataques | ✅ | Fast/Charged legais, energia e custos impossíveis já eliminam/reduzem hipóteses. |
| 7 | Previsor de bait por score + Softmax | 🟡 | Há baitPotential/baitProbability heurísticos. Melhorar para score calibrado por features e Softmax somente quando houver amostra/confiança suficientes. |
| 8 | Aprendizado Bayesiano durante a batalha | 🟡 | Os pesos de hipóteses já são reponderados por cadência/dano/evidência. Falta formalizar likelihoods por fonte e atualização Bayesiana/log-probability auditável. |
| 9 | Padrão do jogador | 🟡 | Histórico e strategyPatternHint já existem. Falta PlayerPatternModel com BAIT_RATE, OVERFARM_RATE, INSTANT_THROW_RATE, SAC_SWAP_RATE, CMP_RATE, SHIELD_FIRST_RATE e FARM_DOWN_RATE usando suavização Beta-Binomial. |
| 10 | Detecção de overfarm | ✅ | Energia acima do custo, overflow e múltiplos Charged já são acompanhados. |
| 11 | Timing ótimo de Charged | 🟡 | TacticalForecastEngine.optimalThrow e FastMovePhaseTracker existem. Falta validar/calibrar melhor contra mecânicas atuais e frames reais. |
| 12 | Fast inimigo durante Chargeds | 🟡 | Há FastMovePhaseTracker e guards durante Charged. Falta representar explicitamente STARTED/RUNNING/REGISTERED/INTERRUPTED e expectedHitAt. |
| 13 | Sac Swap matemático | 🟡 | Sac Swap já existe e não recomenda depois que o prompt de Charged começou. Falta expor uma janela `Δ=T_charge-T_switch`/countdown baseada em turnos. |
| 14 | CMP preditivo | 🟡 | CMP por faixa de Attack/IV já existe e evita falsa certeza. Próximo passo: probabilidade empírica de WIN/TIE/LOSE a partir das combinações legais de IV/level/CP. |
| 15 | Eventos do motor preditivo | 🟡 | O projeto já é orientado a eventos em várias partes, mas ainda não há um único event stream tipado central para FAST/CHARGE/SHIELD/HP/SWITCH/FAINT/CMP. |
| 16 | Threat Score | 🟡 | Threat já combina energia, dano, urgência e confiança. Melhorar separando explicitamente P(throw), P(move), DamageRatio e Urgency sem misturar disponibilidade matemática com intenção. |
| 17 | Previsão de dano sobre HP | 🟡 | DamageForecastEngine + preview no HUD existem. Falta QA físico amplo e intervalos melhores quando IV/level são desconhecidos. |
| 18 | Separar certeza de previsão | ✅ | PredictionEvidenceClass e campos de confidence já separam matemática/inferência/observação; continuar ampliando a taxonomia sem mostrar porcentagem falsa. |
| 19 | Núcleo determinístico Kotlin | ✅ | A base já está em Kotlin com EnergyTracker, EnemyEnergyTracker, ChargedMovePredictor, DamageForecastEngine etc. |
| 20 | Arquitetura completa PBE | 🟡 | Deterministic engine, hypotheses, timing, Sac Swap, damage e decision já existem. Bayesian Predictor e Player Pattern Model ainda não estão formalizados. |
| 21 | Overlay PBE compacto | 🟡 | HUD já mostra Charged, energia/progresso, dano e recomendações. Falta um modo Advanced coerente com Energia + probabilidades + dano + Sac + Throw sem poluir a tela. |
| 22 | Banco de golpes versionado/atualizável | 🟡 | PvPoke/Game Master ficam offline e pinados por commit. Falta fechar atualização remota versionada/validada/assinada independente do APK. |
| 23 | Resposta completa do assistente | 🟡 | O app já sabe responder partes: disponibilidade, dano, Shield, Sac e timing. Falta consolidar em um único pacote de decisão com explicação e confidence. |
| 24 | Estimador + contador + simulador + predictor + decisão | 🟡 | Estimador/contador/predictor/decision estão avançados; simulador curto e modelo probabilístico calibrado ainda precisam amadurecer. |

## Placar PBE

- ✅ Implementado/essencialmente implementado: **8**
- 🟡 Parcial/em evolução: **16**
- ⬜ Não iniciado: **0**

## Melhorias do PBE que mais valem incorporar

### PBE-A — Snapshot preditivo unificado

Criar uma estrutura de leitura, sem substituir os estados atuais, que agregue:
- turno lógico;
- energia min/likely/max;
- Fast phase;
- HP;
- shields;
- moveset posterior;
- candidatos Charged;
- evidência;
- decisão tática.

Objetivo: diminuir divergências entre HUD, histórico, predictor e debug.

### PBE-B — Bayesian Move Hypothesis

Evoluir os pesos já existentes em EnemyEnergyTracker para likelihoods explícitas:
- `P(cadência | move)`;
- `P(dano observado | move)`;
- `P(OCR | move)`;
- `P(timing do Charged | moveset)`.

Usar log-probabilities para estabilidade numérica e manter ranking/meta somente como prior. Evidência observada deve dominar o prior.

### PBE-C — Player Pattern Model local e suavizado

Adicionar modelo local/opt-in, inicialmente sem ML pesado:
- bait;
- overfarm;
- throw imediato;
- Sac Swap;
- CMP;
- shield-first;
- farm-down.

Usar Beta-Binomial (por exemplo `alpha=2, beta=2`) para não gerar 0%/100% com poucas observações. Nunca permitir que o padrão comportamental altere energia matemática.

### PBE-D — Fast Phase explícita

Evoluir FastMovePhaseTracker para estados:
- STARTED;
- RUNNING;
- REGISTERED;
- INTERRUPTED/UNKNOWN.

Guardar `startedAt`, `expectedRegistrationAt` e idade da evidência. Isso melhora optimal throw, Sac Swap, recuperação após frame perdido e mecânicas de simultaneidade.

### PBE-E — Sac Swap Window por turnos

Adicionar forecast:
`deltaTurns = earliestChargedTurn - earliestSwitchTurn`.

Exibir somente quando confidence for suficiente:
- PREPARE +2;
- PREPARE +1;
- SAC NOW;
- TOO LATE/INCERTO.

Nunca sugerir Sac depois que o Charged prompt já começou.

### PBE-F — CMP probabilístico por distribuição real

AttackRangeEstimator já percorre combinações legais. Em vez de usar somente min/max, manter histograma/amostras de Attack compatíveis com CP/liga e comparar distribuições:
- P(WIN);
- P(TIE);
- P(LOSE).

Mostrar porcentagens apenas quando a distribuição estiver fundamentada; caso contrário continuar usando WIN/LOSE/UNCERTAIN.

### PBE-G — Threat V2 calibrável

Separar:
- disponibilidade matemática;
- probabilidade do movimento;
- probabilidade de lançar naquela janela;
- dano relativo ao HP;
- urgência temporal.

Uma forma inicial compatível com o documento:
`Threat = P_throw × P_move × DamageRatio × Urgency`.

Manter também o threat determinístico atual como fallback quando `P_throw`/`P_move` não forem confiáveis.

### PBE-H — Métricas de calibração

Para cada previsão registrar:
- candidato previsto;
- confidence;
- janela prevista;
- resultado observado;
- erro em turns;
- disponibilidade correta/incorreta;
- Brier score/log-loss somente para probabilidades já calibradas;
- antecedência do alerta.

Isso conecta diretamente PBE aos itens #53 e #71 do checklist principal.

### PBE-I — Replay/event fixtures como laboratório

Usar o futuro replay offline para reproduzir:
- Fast de 1–5 turns;
- frame perdido;
- bait;
- back-to-back;
- CMP;
- Sac Swap;
- optimal throw;
- lag;
- OCR incorreto;
- troca com energia armazenada.

A engine deve produzir o mesmo snapshot/forecast a cada replay determinístico.

### PBE-J — Overlay Advanced sem poluição

Organizar as informações em prioridade:
1. KO/Shield;
2. Charged READY/+N;
3. dano;
4. back-to-back;
5. Sac/Throw quando a janela realmente abrir.

Probabilidades e ranges detalhados ficam no modo Advanced/debug; modo Normal continua compacto.

## Ordem PBE recomendada

1. **PBE-D Fast Phase explícita**
2. **PBE-A Snapshot preditivo unificado**
3. **PBE-B Bayesian Move Hypothesis**
4. **PBE-F CMP probabilístico**
5. **PBE-E Sac Swap Window**
6. **PBE-G Threat V2**
7. **PBE-H Métricas/calibração**
8. **PBE-I Replay/fixtures**
9. **PBE-C Player Pattern Model**
10. **PBE-J Overlay Advanced**

## Regras de segurança matemática do PBE

- Energia determinística nunca deve ser substituída por probabilidade comportamental.
- Meta/ranking é prior, não fato.
- Bait provável nunca deve ser mostrado como bait confirmado.
- Se os dados não suportarem porcentagem calibrada, usar LOW/MEDIUM/HIGH ou POSSIBLE/LIKELY.
- OCR inconsistente deve baixar confidence ou abrir hipóteses; não destruir silenciosamente um estado matemático coerente.
- Nenhum modelo probabilístico deve depender de chamada web no hot path da batalha.
- Replay/telemetria de outros usuários, se usados no futuro, exigem consentimento e privacidade adequados.

## Continuação — correção dos ícones de reserva

Commit de código: `fd66d553fc37148c6ab53d96c040b047e01c3bb4` (base `fdd78ab2998e1ed9ead6d55cf71f436763add5ba`).

- Item #14 permanece 🟡: corrigida interrupção prematura da busca com CPs ambíguos/sem correspondência e liberado retrato de slot estável sem depender da posição nativa.
- Validação desta sessão: **18 PASS / 0 FAIL** em ReserveScanPolicyTest e ReserveCardMatcherTest. Os 132 testes anteriores são evidência histórica, não uma nova execução.
- Placar 1–80 e PBE inalterados; nenhum item elevado a concluído.
- Prioridade imediata do Guia Mestre: build Standard e QA real dos ícones de reserva/troca, seguido do ícone oficial instalado. A sequência de novas funcionalidades acima permanece posterior a esses bugs.
- Registro detalhado e limitações no Guia Mestre, seção 11.


## Continuação — launcher com fallback legacy (2026-10-07)

- Item #14: permanece 🟡; fallback do crop indexado ao card agora exige `cardMappingConfirmed` (commit `8dbf9f47`), sem validação física.
- Problema do launcher: a arte oficial `drawable-nodpi/pvppokego_launcher.png` foi conferida; além dos adaptive icons já existentes (API 26+), foram criados `mipmap-anydpi/ic_launcher.xml` e `ic_launcher_round.xml` para fallback pré-API 26 (commits `5b939efb`, `b3605eea`).
- Workflow Standard valida os fallbacks (commit `7fe575e6`). Ícone instalado no Samsung ainda **não validado**.
- GitHub Actions run `37702558022` terminou com `steps=null` antes do build; **não há APK real 0.5.26 gerado**. Não marcar como concluído ou renomear APK antigo.
- Próximo passo: desbloquear execução de runner, rodar suíte/assemble Standard e QA dos retratos de reserva/ícone, conferindo SHA-256.


## Entrega Standard QA 0.5.26 — build público validado

- Data: 2026-10-07 (build GitHub Actions 2026-10-08 UTC).
- Origem do código: `release/0.5.26` (privado); correções Kotlin reais: `9e395f5c`, `c9295962`, `2a98de56`.
- Snapshot público build-only: https://github.com/LucianoToscano/PvPPokeGo-APK-Build/commit/0b1c453add6e838c4de06eb07e5792481bb884d2
- Workflow concluído: https://github.com/LucianoToscano/PvPPokeGo-APK-Build/actions/runs/37707859527
- Artefato: `PvPPokeGo-0.5.26-QA-debug.apk` (78.046.047 bytes), assinado em debug para QA.
- SHA-256: `461815c2b8c9078f8fcd617145f1abeefb3a0be03f2376114227f44594708932`.
- Checks: `:app:testStandardDebugUnitTest`, `:app:assembleStandardDebug`, integridade ZIP APK, presença de atlas/Game Master/launcher, verificação de assinatura v2, SHA-256: **PASS** no build; checksum local pós-download corresponde.
- Download no Google Drive: https://drive.google.com/file/d/1bcbP8WJ7sWgd9lUTvqb0NzV9MSXq_nCH/view?usp=drivesdk
- A entrega QA debug está concluída como artefato; **não** afirmar que correções dos ícones foram validadas em Samsung sem teste no aparelho. #14 e requisitos visuais permanecem 🟡, e #77 continua 🟡 até processo de entrega/release permanente e QA final.
- APK QA/debug pode ter certificado diferente do instalado anteriormente. Exportar configurações antes de desinstalar.
- Recorder Compat adiado como antes; compilada apenas variante Standard.
