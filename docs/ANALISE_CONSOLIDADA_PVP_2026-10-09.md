# PvPPokeGo — análise consolidada das referências PvPoke / Calcy IV / Training / Team Builder

**Data:** 2026-10-09. **Atividade:** análise técnica e documentação, sem alteração de código de produção, sem testes, sem compilação, sem APK. O usuário solicitou análise após encaminhar diversas levas de imagens, links e dois CSV.

## Continuidade canônica conferida

- Guia Mestre de referência: https://github.com/LucianoToscano/PvPPokeGo/blob/release/0.5.29/docs/PROJECT_CONTINUITY_GUIDE.md
- Histórico: https://github.com/LucianoToscano/PvPPokeGo/blob/release/0.5.26/docs/PROJECT_CONTINUITY_GUIDE.md
- Branch isolada em desenvolvimento: `feature/team-recognition-modes-0.5.32`, HEAD inicial deste exame `7480d7b340784b48416ac7d72f10ba1290da38e2`. Branch `release/0.5.29` permanece `952ff14e5f97fb461619946473a42c144cdc2caa`.
- Documentação anterior que deve ser preservada: `TEAM_RECOGNITION_MODES_0.5.32.md`, `TRAINER_SETTINGS_0.5.32.md` e `PVP_ANALYTICS_REFERENCES_2026-10-09.md`.

## Levantamento do que já existe de fato no código

**Implementado em código / ainda SEM compilação QA desta branch:**
- `BattleEngine.kt`: 2.462 linhas; reconhecimento/estado dos aliados e inimigos, energia e fase do golpe ágil, contadores, correspondência, previsão aproximada, `selectManualTeam`. É a fonte das observações **reais/inferidas**, não deve ser usada diretamente como instância mutável de simulador offline.
- `GameDataRepository.kt`: leitura de `gamemaster.json`, `rankings-<liga>.json`, `pvpoke_cpms.json`, `pvpoke_snapshot.json`; golpes possíveis e pesos. Já existe material que deve ser auditado antes de criar um banco duplicado.
- `Models.kt`: `MoveDef`, `PokemonDef`, `GameMaster` e estados; `TypeChart.kt`: efetividade e multiplicadores.
- `TeamBuilderPanel.kt` e `TeamSetupRepository.kt`: cadastro dos 3 aliados, liga, IVs/PC e modo manual/automático. A equipe atual manual **não deve ser substituída** por uma equipe hipotética do Laboratório.
- `TrainerSettingsPanel.kt`: subseções de treino/IVs/nomes/datas e comparador **simples de tipos**; isso não constitui um simulador verdadeiro.
- `BattleOverlayService.kt`: integração do reconhecimento visual à batalha em andamento. Não aumentar, reposicionar ou reescrever HUD V03 sem requisito separado.

**Não implementado/validado:** simulador 1×1 completo e isolado, treino interativo contra IA, rankings recalculados on-device, cálculo de rank IV exato para todas combinações, analítico CMP, matrix 3×N, top counters com cálculos, recursos Tera. Não alegar funcionalidades existentes pelo fato de estarem em screenshots ou checklist.

## Arquivos originais CSV fornecidos — leitura efetiva dos bytes

1) **Team vs. All Pokemon.csv**
- Novo arquivo original preservado no Drive: https://drive.google.com/file/d/1bUjTtGHH36wyojFrLqlG7JI4kOgC40K7/view?usp=drivesdk
- 51.369 bytes; SHA-256 `d704a831d54ccad2fb4818c1767131fc17adedf29ef1bb8e2468fc3cf066b1a1`.
- 1.153 linhas incluindo cabeçalho, portanto **1.152 linhas de entrada**. Todas as linhas têm **6 campos**; **0 erros de interpretação numérica** e **0 rótulos de adversário duplicados**.
- Cabeçalho: `[Pokémon adversário]`, `Altaria DB+M/Ft`, `Houndoom I+FoP/TBl`, `Rillaboom Scr+DrB/EP`, `Threat Score`, `Overall Rating`.
- As três colunas de confrontos contêm inteiros dentro de `0..1000` (ranges observados: Altaria 20–932; Houndoom 19–880; Rillaboom 17–859). `Threat Score` varia 9,3–933,1 e **o CSV está ordenado decrescentemente pelo Threat Score**, não pelo Overall Rating. `Overall Rating` varia 7,8–94,4.
- Primeiras ameaças por Threat Score nesta exportação: `Mimikyu SC+SS/PR` (869,652,613; score 933,1), `Hydreigon (Shadow) DB+BrS/Fl` (554,749,736; 932,1), `Cradily Ac+RT/GK` (604,631,612; 930,3), `Cramorant Pk+Dve/Fl` (547,594,548; 928,3).
- 34 entradas têm ratings de confrontos `>=500` nas três colunas; 330 têm `<500` nas três. Isto **não significa** que 34 são as únicas ameaças ou que 330 são garantidamente fracas; as interpretações dependem da orientação da simulação, do moveset, escudos, liga, IV, versão e critérios do PvPoke.
- Campos ausentes no CSV: data/versão Game Master, configuração de shields, IV/nível exatos dos três aliados, seed/cenários, definições matemáticas do Threat Score e Overall Rating, esquema de abreviações do moveset. **Não publicar os valores como estado da meta atual ou probabilidade de vitória.**

2) **_vs .csv**
- Original preservado nesta leva: https://drive.google.com/file/d/1W1XYj4gOAOjG9-Gz1GsA_fgk-Lo9SMpd/view?usp=drivesdk
- 370 bytes, SHA-256 `bfc98fe13870a4b795466454a1bdfd114143c6f21ed10ff49d3206e09fdf3dc1`, 4 linhas (cabeçalho + 3), 8 campos por linha. 3×3 de **diferenças numéricas assinadas**, com `Wins`, `Losses`, `Draws` e `Average`.
- `Abra CB+Psh/SB 12/15/15`: **3-0-0**, média +53,78; `Abomasnow PS+WBI/EB 4/15/15`: **1-2-0**, −2,31; `Alomomola W+HP/Bl 5/15/12`: **0-2-1**, −22,76. Todas as médias conferem com a média aritmética dos valores centrais arredondada para duas casas.
- Estas diferenças assinadas NÃO são battle ratings 0–1000, score de ranking 0–100, rank IV, nível, porcentagem de vitória, pontuação de Arena ou Elo.

**Contrato do parser futuro:** detectar esquema de cabeçalho (matriz de diferenças vs. Team vs All), limite de tamanho de entrada e encoding, strings escapadas, colunas dinâmicas, origem e versão, valores nulos/fora da faixa e saída tipada. Importação sempre para espaço **simulação/referência**, nunca para histórico real.

## Funcionalidades extraídas das imagens — arquitetura recomendada

### Pilar 1 — Assistente visual de batalha existente
- Manter HUD com identificação de Pokémon ativo/reservas, contadores de golpes ágeis, energia inferida, escudos, previsão de dano/HP, alertas e confiança.
- Conectar cálculos offline aprovados ao motor preditivo **somente depois de validar**, sem agravar latência do reconhecimento e sem alterar automaticamente controles do Pokémon GO.
- Separar `observado_por_OCR`, `cadastrado_pelo_jogador`, `inferido`, `simulado`, `não_determinado`. Quando houver discordância, mostrar incerteza, nunca substituir identidade silenciosamente.
- *Timing recomendado* pode virar mensagem concisa, mas somente com fase/turnos confiáveis; não presumir estabilidade de rede, execução perfeita ou energia absoluta.

### Pilar 2 — Laboratório PvP
- **Equipe atual de 3 (fonte manual ou OCR)** → comando "Analisar equipe" que gera **cópia imutável para simulação**, sem sobrescrever estado ao vivo.
- **Relatório de equipe**: Coverage (cobertura do meta pelo threat score), Bulk (defesa × HP), Safety (consistência como switch e gravidade das derrotas), Consistency (dependência de bait) conforme as definições públicas do PvPoke. As notas são **indicadores heurísticos comparativos**, não garantias de vitória ou avaliação pessoal de quem joga.
- **Scorecard 3×N** de matchups com badges para vitórias favoráveis/desfavoráveis; threats, potential alternatives, histograma de battle ratings, tipagem ofensiva e defensiva por Pokémon e liga, filtros de espécies/formas/legacy/shadow. Incluir explicação sobre fonte/versão, seleção do meta e escudos.
- **Simulador 1×1 e matriz**: fast/charged moves, dano inteiro por turno, energia, shields 0/1/2, troca, cooldown, buffs/debuffs, baiting, estados iniciais customizados, battle rating 0–1000, breakpoints/bulkpoints, CMP e timing; reprodutibilidade e testes contra cenários externos auditados.
- **Pesquisa**: rankings de espécies 0–100 por liga/papel; rank PvP IV por espécie/nível/forma/CP; top counters com movesets, disponibilidade/legalidade; export CSV/backup sem vazamento de histórico.
- **Usabilidade**: botões de ação e comparações em telas dedicadas, não transformar a página inicial em tabela longa. Incluir acessibilidade: não depender só da cor para indicar vitória/perda.

### Pilar 3 — Academia PvP / Treinamento
- Modo separado `Treinar` com equipe manual/salva/importada, adversário virtual, dificuldade ajustável, combate simulado 3×3, contagem de turnos, shields, switch timer, HP e energia.
- Interface "batalha de treino" com escolhas do jogador, replay, resumo DAMAGE/SHIELDS/ENERGY, métricas de energia perdida/gerada, escudos desperdiçados, overfarm, decisões de troca, acertos e erros, prática focada em CMP/timing.
- Botões `Autotap` ou IA usados **somente na simulação offline**. Não comandar toques/ações do Pokémon GO ao vivo.
- `Top performers`: rankings derivados do PvPoke Train representam batalhas **contra bots e jogadores do simulador**, com amostragem/viés; não são vitórias verificadas no GO nem a posição do usuário no ranking Arena. Não vincular automaticamente a recompensas reais.
- Sistema de progressão da Academia pode contar exercícios **locais** e domínio por tema; pontuação pública da Arena, campeonatos e prêmios exigem backend, regras antifraude, moderação e aceitação explícita, não usar resultados simulados como evidência de campeonato competitivo.

### Função analisada mas fora de escopo GO PvP: Tera Raid Counter Calculator
- URLs de referência: https://pvpoke.com/tera/altaria/dragon/dragon-flying
- **Tera Raid é mecânica de Pokémon Scarlet/Violet, não de Pokémon GO.** Não introduzir Terastalização, Tera Type nem cálculos de raids Scarlet/Violet no motor Pokémon GO, no HUD ou como se fossem Megas.
- Reaproveitar a **ideia genérica** "selecionar chefe, tipagens ofensivas do chefe e procurar counters" somente em futuro módulo **Pokémon GO — Raides/PvE**, com cálculos de GO validados e completamente separados do simulador PvP. Baixa prioridade para o app de PvP.

## Separar cinco métricas que aparecem próximas nas referências

| Nome exibido | Escala/unidade | Significado |
|---|---|---|
| PvPoke Ranking Score | comumente 0–100 | avaliação por espécie/papel/temporada no modelo de simulação |
| Battle Rating | 0–1000 | vantagem de um confronto simulado com escudos/IVs/golpes fixos |
| Threat Score | escala e fórmula próprias de ferramenta/exportação | avaliação agregada de ameaça do time; não é ranking de jogador |
| PvP IV Rank | posição entre combinações válidas de IV/nível em uma liga | otimização de stats e CP por configuração; não mede habilidade do treinador |
| Arena Elo/Rank PvPPokeGo | regra própria, ainda sujeita a backend/antifraude | resultado de partidas elegíveis, **não** inferido do PvPoke |

Outros: `Team Rating` no Train usa escala 0–1000 e amostras de bots/jogadores do simulador; `Individual Rating` pode exceder 100%, não é percentil. `_vs Average` é média de **diferenças** assinadas, não um battle rating.

## Prioridades técnicas propostas (nenhuma executada neste registro)

1. **P0 — Isolamento e integridade:** verificar compilação e testes da branch 0.5.32 quando autorizado; preservar HUD e reconhecimento; criar enum de proveniência e contrato de versão Game Master; QA de modos Manual/Auto.
2. **P1 — Importador CSV seguro + relatório de equipe:** permite analisar exportação já fornecida, mostrar ameaças e cobertura com avisos de proveniência; depende de schema validado mas não exige imediatamente simulação 1×1 on-device.
3. **P1 — Motor matemático offline determinístico e testado:** regras CPM/IV/CP, moves legais, dano, energia, turns, shields, CMP e efeitos; fase antes de gráficos avançados.
4. **P2 — Simulador 1×1 / Matrix / relatórios completos** alimentados pelo motor auditado; comparar fixtures e benchmarks com datasets versionados. Tabelas/visualizações não devem travar a tela de captura.
5. **P2 — Academia PvP offline:** treino 3×3, IA por níveis, decisões de escudos e troca, resumo e replays; histórico de treino separado de batalha real.
6. **P3 — Estudos de meta/Top Performers, ranking Arena, campeonatos e premiação:** licença, consentimento, backend e integridade antes de uso público.
7. **Fora do MVP:** Tera Scarlet/Violet. Eventual **Raides/PvE Pokémon GO** independente, depois do núcleo PvP.

## Fontes externas consultadas e notas

- Código/licença do PvPoke: https://github.com/pvpoke/pvpoke (MIT para código; nomes, sprites, marca e assets pertencem aos titulares — nunca interpretar MIT como licença de todos os elementos visuais).
- Explicação pública dos quatro critérios de equipe: https://pvpoke.com/articles/strategy/ultimate-guide-to-team-building/
- PvPoke Train Top Performers e limitações da amostragem: https://pvpoke.com/train/analysis/
- Página da mecânica Tera Raid Scarlet/Violet: https://www.pokemon.com/br/noticias/rillaboom-com-mightiest-mark-retorna-as-tera-raid-battles-de-7-estrelas
- Referências das levas anteriores: https://pvpoke.com/team-builder/all/1500/altaria-m-0-4-3%2Choundoom-m-1-4-5%2Crillaboom-m-1-1-2 ; https://pvpoke.com/train/ ; https://pvpoke.com/rankings/ ; https://pvpoke.com/attack-cmp-chart/all/1500/froslass/ ; https://pvpoke.com/battle/

## Encerramento da análise
- **Feito:** conferência do HEAD, leitura de dois CSVs reais e da estrutura de código, análise do material visual, definição de fronteiras e plano, upload dos dois CSV ao Drive, criação deste registro no GitHub e no Guia Mestre/checklist.
- **Não feito:** modificar produção, implementar nova feature, executar testes Android/Kotlin, compilar, assinar, publicar ou gerar APK.
- **Regra mantida:** nada de gerar APK até pedido explícito do usuário.
