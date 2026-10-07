# PvPPokeGo 0.5.25 — pesquisa de fontes externas para o motor PvP

Data: 2026-10-07

## Conclusão curta

Há material útil de verdade. Não vale importar um simulador inteiro para o Android. O melhor caminho é continuar evoluindo a engine Kotlin atual e usar os projetos externos em quatro papéis distintos:

1. **PvPoke** como fonte principal de dados e oráculo matemático.
2. **GoPvPSim** como segunda implementação independente e, principalmente, fonte de testes de regressão/edge cases.
3. **PokeMoves** como referência excelente para sequências de contagem com energia residual e para intervalos de CMP.
4. **PoGO GBL Analyzer** como modelo para detectar mudanças entre snapshots de rankings/movesets, fora do hot path da batalha.

Pokebattler e Reddit devem servir como validação externa/comunitária, não como dependência de runtime.

## 1. PvPoke

Commit inspecionado: `f627e89e53c0c7b903fff097df7a0ad0ac95decc`  
Licença: MIT.

O projeto já estava corretamente pinado no `tools/package_offline_pvpoke_assets.py` do PvPPokeGo. A pesquisa confirmou que esse commit é a referência atual inspecionada e que os caminhos críticos continuam válidos.

### O que podemos aproveitar

- Game Master completo: espécies/formas/moves/power/energy/cooldown/buffs.
- Rankings e frequência de uso como **prior**, nunca como certeza.
- Fórmula de dano e constantes.
- Timeline baseada em ações/eventos.
- Heurísticas de turns-to-live, farm down, shield e charged timing.
- Regras de CMP.
- Casos especiais de forma/movimentos.
- Novas regras de turno de 2026.

### Achado importante

O próprio material atual do PvPoke descreve Fast Move com dano e energia registrando no **último turno do movimento**. Isso importa para o futuro módulo de fase do Fast Move, optimal throw, Sac Swap e reconstrução após frames perdidos.

O `ActionLogic.js` também mostra que uma decisão útil não depende somente de "tem energia": ele combina tempo para morrer, dano do Fast, Charged disponíveis, shields, CMP, energia residual e risco de overflow de 100 E.

### Já temos no PvPPokeGo

- snapshot offline do Game Master/rankings;
- energia por Pokémon;
- hipóteses de Fast Move;
- residual/overfarm;
- damage forecast;
- stat stages;
- reaction window;
- TypeChart;
- Charged predictor.

Portanto não devemos duplicar `Battle.js` em Kotlin. Devemos extrair regras verificáveis e criar testes equivalentes.

## 2. GoPvPSim

Commit inspecionado: `0952963f04bbc313c042a422a3d042c066678639`  
Licença: MIT.

É a fonte complementar mais valiosa encontrada. O projeto é um port Python do motor do PvPoke e mantém milhares de testes, auditorias contra o PvPoke e um documento explícito de divergências.

### O que podemos aproveitar

Principalmente **testes/oráculos**, não o runtime Python.

Arquivos prioritários:
- `tests/test_damage_formula_parity.py`
- `tests/test_new_turn_mechanics.py`
- `tests/test_farmdown_order_port.py`
- `tests/test_energy_lead.py`
- `tests/test_fire_now_cmp_shadow.py`
- `docs/pvpoke_divergences.md`
- `DEVELOPER_NOTES.md`

### Achados importantes

- Possui regressões para fórmulas de dano, incluindo limites de floor.
- Documenta o novo relógio de batalha e ordem de resolução.
- Tem testes específicos para farm-down em empates de turno com Fast de 1 turno.
- Trata CMP, shields, energia, buffs/debuffs e casos especiais.
- Mantém uma política explícita: divergência do PvPoke só deve existir com motivo e teste.

### Aplicação recomendada

Criar fixtures Kotlin pequenas com valores conhecidos derivados desses testes e do PvPoke. Isso é melhor do que portar o simulador Python inteiro.

## 3. PokeMoves

Commit inspecionado: `bd9ac2e2bb4df8dd72337d1aab299e04d42a3093`  
Licença: MIT.

É particularmente útil para o que estamos fazendo agora.

### Sequências de Charged com energia residual

O gerador calcula repetidamente:

`count = ceil((cost - residualEnergy) / fastEnergyGain)`

e após cada Charged conserva:

`residualEnergy += count * gain - cost`

Isso produz sequências reais como:

- 5, 4, 5, 4
- 6, 6, 5, 6
- 3, 2, 3, 2

em vez de assumir que todo Charged custa sempre a mesma quantidade de Fast Moves a partir de zero.

O PvPPokeGo já mantém energia dinâmica, portanto a matemática de runtime é superior a uma tabela fixa. Porém essas sequências são excelentes para:
- teste;
- UI compacta;
- explicação;
- validação de overfarm/back-to-back.

### CMP

`src/helpers/cmp.js` percorre níveis/IVs válidos por liga e guarda:
- Attack do rank 1 por stat product;
- Attack máximo possível.

Isso sugere uma melhoria clara no PvPPokeGo: quando IV/nível do inimigo são desconhecidos, CMP deve usar uma **faixa de Attack**, não um único IV padrão.

Isso também melhora a faixa de dano e evita falsa precisão.

### Atualização de dados

O PokeMoves baixa moves, Pokémon e rankings do PvPoke e recompila seu dataset. Podemos reaproveitar o conceito de atualização + diff, mas manter os dados essenciais offline no APK.

## 4. PoGO GBL Analyzer

Commit inspecionado: `d4dd848f29ff6b929eca5efe69b419aeefffef6d`  
Licença: MIT.

Não é um simulador de batalha ao vivo. Seu valor para nós é outro:

- comparar snapshot antigo/novo;
- detectar alterações de moveset;
- mudanças de rank;
- tendências de tipos.

### Aplicação recomendada

Adicionar futuramente uma etapa de build/update de dados que gere algo como:

`PvPoke dataset diff`
- movimentos alterados;
- Pokémon com moveset alterado;
- novos/retirados;
- mudanças de ranking relevantes;
- hash anterior/novo.

Isso ajuda a saber quando uma atualização do PvPoke pode alterar previsões do aplicativo.

Não deve entrar no hot path da batalha.

## 5. Pokebattler

O site publica uma API/referência para simulações e dados Pokémon GO.

Uso recomendado:
- validação cruzada de alguns valores;
- detectar inconsistências grandes entre motores;
- testes offline/de desenvolvimento.

Não recomendado:
- depender da rede durante batalha;
- tratar sua simulação de raids como regra de PvP;
- copiar implementação ou recursos proprietários.

## 6. Reddit / comunidades

r/TheSilphArena e r/TheSilphRoad são úteis para descobrir:
- casos de lag;
- mudanças práticas de timing;
- problemas visuais;
- como jogadores contam energia;
- edge cases que merecem fixture.

Não devem definir sozinhos uma regra matemática.

Discussões de 2026 reforçam que o sistema novo alterou a ordem de registro entre Fast e Charged em situações específicas, mas a contagem de duração/energia e o conceito de optimal timing continuam relevantes.

## 7. Mudanças concretas recomendadas para o PvPPokeGo

### Prioridade A — validar antes de adicionar mais estratégia

1. **Turn Phase Tracker**
   - representar Fast iniciado, turno atual da animação e instante de registro;
   - distinguir "começou" de "energia/dano já registraram";
   - integrar com perda temporária de visão.

2. **Fixtures de paridade PvPoke/GoPvPSim**
   - Fast 1/2/3/4/5 turns;
   - charged no mesmo turno do registro do Fast;
   - residual/100 E/back-to-back;
   - farm-down 1-turn;
   - CMP;
   - buff/debuff;
   - shadow;
   - casos de floor() no dano.

3. **CMP como intervalo**
   - parar de usar um único spread estimado para afirmar CMP quando IV inimigo não é conhecido;
   - calcular `AttackMin..AttackMax` coerente com CP/liga;
   - retornar FAVORÁVEL / DESFAVORÁVEL / INCERTO.

### Prioridade B — evolução do predictor

4. **Consistency Score**
   - OCR observado + energia máxima + janela de frames perdidos;
   - rejeitar/baixar confiança de evento impossível antes de destruir estado.

5. **Sequência residual de Charged**
   - expor "1º / 2º / 3º" ou "segundo +N Fast" quando útil;
   - a engine continua calculando a partir de energia real, não de tabela fixa.

6. **Threat + Shield**
   - reaproveitar turns-to-live e dano;
   - separar AVAILABLE, POSSIBLE, LIKELY, OBSERVED.

### Prioridade C — depois

7. Optimal throw.
8. Farm-down forecast.
9. Sac Swap.
10. Modelo comportamental/bait.

## 8. O que não recomendo copiar

- Um simulador web completo para dentro do APK.
- Código proprietário do Pokebattler.
- Meta ranking como se fosse moveset confirmado.
- Heurística de bait como fato.
- Dependência de internet no loop de Fast Move.
- Regras atuais copiadas sem fixture/versionamento: o sistema PvP mudou em 2026.

## 9. Licenças e atribuição

Os quatro repositórios GitHub analisados são MIT. Se algum trecho de código for adaptado diretamente, preservar o aviso de copyright/licença correspondente em `THIRD_PARTY_NOTICES`/documentação. Se apenas reimplementarmos a matemática e usarmos os projetos como oráculo de teste, ainda vale registrar a origem da validação.

O PvPPokeGo já empacota a licença do PvPoke junto do snapshot; manter essa prática.

## 10. Regra de integração

Nenhuma fonte externa deve entrar no hot path de batalha.

Fluxo recomendado:

`upstream -> atualização offline -> validação/diff -> assets locais -> engine Kotlin`

e, durante a batalha:

`frames/OCR -> eventos -> estado matemático -> predictor -> overlay`

sem chamada web por Fast Move.
