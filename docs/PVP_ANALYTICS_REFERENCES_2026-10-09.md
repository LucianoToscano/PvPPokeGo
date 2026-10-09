# PvPPokeGo — referências de análise PvP, CMP, rankings e simuladores

**Data:** 2026-10-09 · **Status:** material recebido, inspecionado, preservado no Drive e requisitos documentados. **Este conjunto não representa funcionalidades novas já implementadas, código compilado ou APK entregue.**

## Continuidade / fonte

- Repositório: https://github.com/LucianoToscano/PvPPokeGo
- Guia canônico estável lido: [release/0.5.29](https://github.com/LucianoToscano/PvPPokeGo/blob/release/0.5.29/docs/PROJECT_CONTINUITY_GUIDE.md); histórico: [release/0.5.26](https://github.com/LucianoToscano/PvPPokeGo/blob/release/0.5.26/docs/PROJECT_CONTINUITY_GUIDE.md).
- Branch de trabalho **exclusivamente de desenvolvimento**: `feature/team-recognition-modes-0.5.32`, HEAD confirmado no início deste adendo: `75f380fc8ac801350bcbf9be65e95e35cefce3ea`.
- A `release/0.5.29` permanece no HEAD `952ff14e5f97fb461619946473a42c144cdc2caa`.
- Respeitar os módulos existentes: `GameDataRepository`, `GameMaster`, `TypeChart`, `BattleEngine`, motor preditivo, equipe automática/manual, HUD V03 e reconhecimento OCR, sem duplicar conceitos ou trocar fontes de verdade.

## Referências públicas explicitamente fornecidas

- Exemplo de simulação configurada 1v1: https://pvpoke.com/battle/1500/moltres_galarian/glalie/22/0-3-2/3-1-4/
- Gráfico de prioridade de carregados CMP: https://pvpoke.com/attack-cmp-chart/all/1500/froslass/
- Simulador principal: https://pvpoke.com/battle/
- Rankings: https://pvpoke.com/rankings/
- Construtor de equipe: https://pvpoke.com/team-builder/all/1500/
- Repositório de código-fonte de referência: https://github.com/pvpoke/pvpoke
- O PvPoke documenta que **score de ranking (0–100), battle rating, ranking IV e diferenças de matriz são medidas diferentes**. Não apresentar todas como `Rank` ou como probabilidade real de ganhar. Conferir versão, regras e licença MIT antes de reaproveitar código. Não copiar marca/UI/arte de terceiros ou reivindicar afiliação.

## Material original preservado (Google Drive oficial)

Nove imagens nesta leva, de exemplos de rankings, gráfico CMP, troca/escudos, linha do tempo, pontos de dano, resultados de matchup, IV e matriz (os nomes abaixo são identificadores do lote, não títulos oficiais das telas):

1. [Referência 01](https://drive.google.com/file/d/1Tbws_EFGL059Iz6Ut4McMgQFthMNTn9p/view?usp=drivesdk)
2. [Referência 02](https://drive.google.com/file/d/1edLI7KL2emSi6NFlMYWe3sWBcNJkvjDb/view?usp=drivesdk)
3. [Referência 03](https://drive.google.com/file/d/1Dg3fkqRSoFWZaxllI4eCbj3K8zVzy8oW/view?usp=drivesdk)
4. [Referência 04](https://drive.google.com/file/d/1Wnk9VP1XQH4lAxuPgiJ8QIdvdDADn3wB/view?usp=drivesdk)
5. [Referência 05](https://drive.google.com/file/d/1QbH5m3HsgueDDNiSPZHs7lAbfGXxR5Fj/view?usp=drivesdk)
6. [Referência 06](https://drive.google.com/file/d/1p7dFJ7J560fD5j1m15ZRhKHcqVVVhNAz/view?usp=drivesdk)
7. [Referência 07](https://drive.google.com/file/d/1nTAXGIV71p6O_u60tERkARlGZRvofuBK/view?usp=drivesdk)
8. [Referência 08](https://drive.google.com/file/d/1SSLlo_0GZ7SCdDy45uVHR8LjjSJWlQ5v/view?usp=drivesdk)
9. [Referência 09](https://drive.google.com/file/d/1MvA_ColwZIh-PzAd2Vjkk4Ik3048WNkw/view?usp=drivesdk)

**CSV associado:** [_vs .csv — matriz exportada pelo usuário](https://drive.google.com/file/d/1sYxNDXV0RszjN922ALwIARmmxu8dtDnf/view?usp=drivesdk), arquivo original de **370 bytes**, SHA-256 `bfc98fe13870a4b795466454a1bdfd114143c6f21ed10ff49d3206e09fdf3dc1`. CSV conferido: **4 linhas incluindo cabeçalho, 8 colunas**.

## Estrutura real do CSV observado

Colunas: `[Pokémon do grupo A]`, três oponentes (`Alomomola W+HP/Bl 5/15/12`, `Tapu Bulu BS+NM/GK 4/13/5`, `Alcremie Ch+Psh/EB 5/15/15`), `Wins`, `Losses`, `Draws`, `Average`.

Linhas:
- `Abra CB+Psh/SB 12/15/15`: **3-0-0**, diferença média **+53,78**.
- `Abomasnow PS+WBI/EB 4/15/15`: **1-2-0**, diferença média **−2,31**.
- `Alomomola W+HP/Bl 5/15/12`: **0-2-1**, diferença média **−22,76**.

**Interpretação importante:** os três valores centrais são diferenças numéricas assinadas da exportação e `Average` é sua média arredondada. **Não são, por si só, battle ratings 0–1000, porcentagem de vitória, pontos Elo, CP nem probabilidades.** Preservar valores brutos, contexto da liga, metadados de origem e versão da simulação; decodificar abreviações de moves apenas com tabela de referência, não por palpite. Esta exportação é exemplo externo de matriz, **não histórico de vitórias do usuário**.

## Escopo para a Central de Consulta e Laboratório de Batalha

### A. Rankings PvP (comparação de espécies, não ranking Arena de usuários)
- Liga e temporada, papéis Overall/Leads/Closers/Switches/Chargers/Attackers/Consistency, busca, formas e estado Sombroso.
- Score e ranking como `estimativa de desempenho da simulação / fonte / data`; separar rank IV por combinação exata de Pokémon.
- Abrir detalhes e melhores golpes; comparar versão de dados e critérios do ranking; exportar resultados quando estiverem calculados de verdade.

### B. CMP / prioridade de carregados
- Estatística de **Ataque efetivo** na configuração exata de espécie, forma, nível e IVs; faixas configuráveis por liga.
- Quando dois carregados iniciam no mesmo turno, avaliar a prioridade por Ataque (empates podem depender de sorte); distinguir empate CMP de “falso CMP” por fim de golpe ágil e descompasso do turno.
- Não confundir Ataque base com Ataque final nem aplicar bônus Sombroso de dano como se alterassem a ordem CMP.

### C. Simulação 1×1 configurável
- Seleção de espécie/forma, PC/nível/IVs/golpes, escudos 0/1/2, HP e energia iniciais, modificadores, baiting, switch e tempo.
- Saída auditável: eventos/turnos, recursos finais, dano e energia, motivo do resultado e cenários alternativos; identificar hipóteses e incerteza ao invés de afirmar predição perfeita.
- Testes de equivalência e regressão contra fixtures estáveis, evitando usar um resultado de screenshot como verdade permanente.

### D. Matrizes de equipes (Team Builder)
- Grupo do usuário até três para batalha real e grupos maiores **somente para simulação offline**, sem sobrescrever `manual_team_v1` ou o atual reconhecimento automático.
- Matriz A×B com battle ratings reais calculados, cenários 0/1/2 escudos, estatísticas de win/loss/draw e médias; visões de diferenças/strengths/weaknesses, exportação CSV e importação validada.
- Avaliação de coberturas, ameaças comuns, sugestões de substituição e anti-meta devem ser rotuladas `simulação`, não detecção ao vivo.

### E. Breakpoints, bulkpoints e timing ideal
- Dano inteiro por golpe ágil, limiar de Ataque que aumenta 1 dano, limiar de Defesa que reduz 1, ambos dependentes da composição específica de atacante, defensor, moves e estágios.
- Timing do carregado em função de turnos dos golpes ágeis dos dois Pokémon, minimizando turnos gratuitos cedidos ao adversário.
- Indicadores de incerteza para relógio visual, lags, animações, trocas e ausência de IV/nível exatos. **Não gerar recomendações numéricas até haver simulador validado**.

### F. Histórico e aprendizado
- Comparar partida real observada com cenários simulados, mantendo separadas observações OCR verificadas, hipóteses e decisões do usuário.
- Respeitar privacidade, backup, limites e integridade do histórico. Não misturar eventos de battle real com CSV importado para treinamento automático sem opt-in e validação.

## Reutilizar o que já existe (verificado em código)

- `GameDataRepository.kt`: Game Master offline, moves, ranking snapshot e multiplicadores de CP.
- `Models.kt`: estatísticas de golpes e Pokémon, rankings e estados de batalha.
- `TypeChart.kt`: multiplicadores de tipo (já usados na prévia do treinador).
- `BattleEngine.kt`: energia, golpes e continuidade; usar interface própria de simulação para não alterar estado da partida real.
- `TeamBuilderPanel.kt`, `TrainerSettingsPanel.kt`: formulário dos aliados e comparação inicial, **não** simulador completo.
- Não duplicar pacote PvPoke já embutido no APK, nem executar chamadas online em cada frame do overlay.

## Ordem recomendada e critérios de aceite (PENDENTES)

1. **Fonte e versões:** inventariar snapshot offline, multiplicadores CPM, lista de espécies/formas, regras por liga, legalidade de movesets, validade de fontes e licenças. Acrescentar campo `sourceVersion` e `simulatorVersion` em resultados futuros.
2. **Base matemática testável:** módulos puros para CP/IV produto, dano PvP com arredondamento, energia, turnos, escudos, estágios, CMP, breakpoints e bulkpoints; resultados determinísticos para fixtures especificadas.
3. **Simulador offline isolado:** 1v1, múltiplos escudos, baiting e estados iniciais; não compartilhar estado mutável com `BattleEngine` online nem sobrescrever HUD.
4. **Matrix & CSV:** parser seguro com separação `difference`, `battleRating` e `rankScore`, identificação de linhas/colunas, estatísticas W/L/D e saída CSV reproduzível. Não classificar matriz apenas com sinais positivos/negativos sem conhecer semântica da exportação.
5. **CMP/timing:** faixas de Ataque por configurações legais, opt timing como recomendação de simulação com confiança, não regra infalível.
6. **UI clean:** central com abas Rankings / Simular / Equipes / CMP / Tipos / Ataques; usar as imagens como referência informativa, não cópia do visual de terceiros.
7. **QA posterior, apenas com autorização:** testes unitários, diff histórico, backup/importação, regressão OCR/reservas/HUD, Android build, aparelho Samsung; não alegar êxito prematuramente.

## Status e instrução do usuário

**Nenhum código funcional desses novos módulos de rankings/CMP/matriz foi implementado neste lote, nenhum teste compilado nem APK gerado.** Referências + CSV arquivados, requisitos/etapas documentados. A versão 0.5.32 segue em desenvolvimento. O pedido permanente é **acumular mudanças, preservar releases, documentar cada alteração e só compilar/gerar APK quando solicitado expressamente**.
