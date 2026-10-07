# CHECKLIST — PvPPokeGo 0.5.26

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
| 14 | Reconhecimento visual de Pokémon | 🟡 | OCR + visual + CP + espécie/forma + reservas; precisa QA real mais amplo. |
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
