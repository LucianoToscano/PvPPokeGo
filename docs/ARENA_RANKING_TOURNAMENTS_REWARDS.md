# PvPPokeGo Arena — Ranking, campeonatos, medalhas e recompensas

**Data:** 2026-10-08  
**Base:** release/0.5.29 (HEAD inicial `952ff14e5f97fb461619946473a42c144cdc2caa`)  
**Feature branch:** `feature/arena-foundation-20261008`  
**Referência visual:** captura proposta pelo usuário (perfil, ranking, progresso na liga, prêmios, medalhas, navegação). A captura é um **mockup**, não tela funcional. **HUD de batalha** continua referenciado à 0.5.17.

## Objetivo

Desenvolver Arena sem remover nem regressar OCR dos três aliados, reservas, contador de energia, preditor, olho/cadeado, personalização, backups, launcher oficial ou interface nativa do Pokémon GO. O módulo de Arena é um subsistema independente e não envia toques para o jogo.

## Matriz de entregas

| Módulo | Situação nesta branch | Dependências/critério |
|---|---|---|
| Painel Arena na tela inicial existente | **Código inicial implementado** | QA Compose/Android e aceite visual pendentes |
| Apelido do treinador | **Local e editável** | Sem conta, unicidade nem sincronização; não é login |
| XP de atividade | **Calculado do histórico local** | 25 vitória, 10 derrota, 15 empate; resultado explícito, sem status incerto |
| Medalhas | **Cinco regras locais implementadas** | Primeira vitória, 10 e 25 vitórias, sequência de 5, resultado nas três ligas |
| Faixas Bronze/Prata/Ouro/Platina/Diamante/Mestre | **Local, por XP** | Cosmético, NÃO Elo nem ranking oficial |
| Elo competitivo | **Motor matemático inicial implementado** | Apenas relatórios de ambos os lados, por temporada e liga |
| Validação pareada de resultado | **Política inicial implementada** | Rejeita sozinho/contraditório/participante duplicado/ligas conflitantes |
| Chaves eliminatórias | **Gerador básico implementado** | Sementes para 2, 4, 8, 16, 32, 64; sem byes e sem organizador |
| Ranking online global/nacional/semanal | **PENDENTE** | Backend, banco, identidade estável, autenticação, privacidade |
| Campeonatos online e fase de grupos | **PENDENTE** | API, inscrição, agenda, pareamento, árbitro, resultados finais |
| Loja/vitrine de medalhas e itens virtuais | **PENDENTE** | Inventário auditável, políticas e persistência |
| Prêmios reais e resgates | **BLOQUEADO** | Patrocínio, orçamento, regras públicas, fraude, jurídico/tributário e fornecedor autorizado |
| PokéCoins/passes Pokémon GO | **NÃO DISPONÍVEL** | Não prometer crédito in-game sem autorização/integrador oficial |
| Histórico vitalício além de 50 registros | **PENDENTE** | Migração segura/persistência independente antes de totais vitalícios |

## Separação obrigatória de três métricas

1. **XP local**: recompensa de engajamento, não competitivo e não transferível para ranking oficial; calculado só do histórico de batalhas com resultado explícito. Quando o histórico é limpo/limitado a 50 partidas, o número exibido muda; **rotular sempre como histórico disponível**.
2. **Rating Arena** (Elo/Glicko futuro): obtido exclusivamente de eventos de competição com dupla concordância e verificação adicional no servidor; apuração separada por liga/copa/temporada. Mecanismo puro `ArenaCompetitionEngine` é preparatório, não credita rating no aparelho.
3. **Ranking Pokémon GO oficial**: **nunca confundir** com o rating criado pelo PvPPokeGo.

Pontos de XP ou OCR não autorizam premiação e nunca devem ser usados para fabricar ranking global.

## Regras de partidas competitivas

- Participantes possuem `playerId` único emitido pelo servidor; apelido é cosmético.
- Uma partida vincula `matchId`, `seasonId`, `leagueCp`, ids de ambos, horário e formato do torneio.
- Dois relatos independentes devem indicar o mesmo confronto e resultados complementares (vitória/derrota, empate/empate).
- Um relato isolado, empate contraditório, o mesmo jogador dos dois lados, divergência de liga/temporada ou duplicata de participante não pode alterar a classificação.
- Dupla concordância é **necessária, mas NÃO suficiente** para competição com prêmio. Requer controle de múltiplas contas, conluio, replay de provas, rematches suspeitos, disputa, moderação, limite de tempo e trilha de auditoria no backend.
- Servidor autoritativo calcula o rating, mantém ledger append-only e pode suspender/estornar resultados com histórico visível.
- Separar `PROVISÓRIO`, `CONFIRMADO_PELOS_JOGADORES`, `EM_DISPUTA`, `APROVADO_PELA_ORGANIZAÇÃO` e `ANULADO` na evolução do serviço; só resultados com política do campeonato permitem prêmio.
- Não publicar automaticamente capturas, avatar, histórico, localização, telefone nem OCR. Exigir consentimento e controles de privacidade.

## Ranking e temporadas

- Rankings futuros: Geral, Brasil/país escolhido explicitamente, Semanal, Temporada, Grande, Ultra, Mestra e copas especiais (identificador próprio para copas).
- Ordenar rating, depois desempates regulamentados; transparência de partidas elegíveis, reclassificações e sanções.
- Começar com Elo determinístico K=32/base 1000 para testes, reavaliar Glicko-2, temporadas, volatilidade, proteção a inatividade e pareamento desigual antes do lançamento.
- A pontuação inicial **não deve depender simplesmente da quantidade de batalhas**.

## Campeonatos

- Tipos: eliminatória simples, melhor de 3/5, todos-contra-todos, fase de grupos e equipes.
- A primeira versão de gerador implementa apenas cruzamento de seeds para tamanhos de potência de dois, evitando inventar byes.
- No serviço futuro: inscrições com data limite, regulamento da liga/copa, convocação dos dois jogadores, janela de confronto, botão de resultado, confirmação da outra parte, upload opcional de evidência, mediação, avanço da chave e finalização.
- Mudar resultados já confirmados só mediante auditoria, nunca editar placar silenciosamente.
- Torneio teste sem dinheiro/prêmios reais antes de pilotos oficiais.

## Medalhas e recompensas

- Medalhas locais: regras auditáveis, limiares explicitados, marcos de 1/10/25 vitórias, sequência de 5, três ligas com resultado.
- Roadmap: desafios por liga, 100/500/1000 vitórias **após resolver limite de 50 partidas**, temporadas, vitrine pública opt-in, medalhas exclusivas de campeonatos, borda de perfil, temas do HUD, títulos.
- Recompensas cosméticas futuras não devem desbloquear capacidade tática superior no ranking competitivo.
- Prêmios materiais: parcerias, controle de fraude, orçamento, termos, elegibilidade regional/etária, privacidade e impostos antes do anúncio. Mockup com PokéCoins/passes é **ilustrativo**.

## UI: preservar o contrato existente

- **Tela principal:** card discreto `PvPPokeGo Arena`, recolhível, apelido local, XP, histórico disponível, 5 medalhas, ranking/torneios/recompensas rotulados com estado real. A nova área aparece antes de `Histórico de batalhas`, sem reestruturar o restante da tela.
- **Futuro:** perfil completo, navegação Início/Ranking/Recompensas/Histórico/Sobre, temporadas e chaves, após aceite visual.
- **Batalha:** sem HUD extra e sem alterar posições, toque, OCR, captura e identificação dos aliados.
- A captura do usuário é diretriz estética, não licença para colocar números, participantes, prêmios e medalhas fictícios em uma tela supostamente real.

## Arquitetura e migração futura

- `arena/ArenaProgress.kt`: função pura de resumo offline sobre `BattleHistoryEntry`, sem modificação do armazenamento.
- `arena/ArenaCompetitionEngine.kt`: verificação de relatos e cálculo isolado do ranking Elo; funções de primeira rodada.
- `ui/ArenaDashboard.kt`: interface Compose, apelido salvo em SharedPreferences separados, sem backend nem auto-upload.
- `ui/SettingsScreen.kt`: apenas integração do card; restante dos controles intactos.
- Fase seguinte: `ArenaApi`, `ArenaAuth`, `ArenaLedger`, `ArenaTournamentRepository` e sincronização explícita com API persistente. Exigir testes de consistência, migrações, reconexão, offline-first e perda de rede antes de ativar.
- **Não alterar versionCode/versionName para afirmar APK novo sem build e verificação.**

## Critérios de teste e pendências

- Testes unitários adicionados para: resultado incerto não pontuar, IDs duplicados não somarem XP, streak, medalhas, três ligas, relatos conflitantes, unilateralidade, Elo simétrico e chaves sem participantes duplicados.
- Ainda necessários: execução real da suíte Android, teste de renderização/legibilidade no Samsung, QA do apelido após reinício, persistência e exclusão do histórico, testes de ataque ao serviço futuro, carga de ranking e moderação.
- **Status global nesta etapa: PARCIAL**. Código de fundação implantado na feature branch; ranking global, competições reais, pagamentos/prêmios e APK não foram entregues.

