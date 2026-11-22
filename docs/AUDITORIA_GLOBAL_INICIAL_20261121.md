# Auditoria inicial conservadora — PvPPokeGo

> **Data de referência:** 21/11/2026 (America/Fortaleza), 22/11/2026 UTC.  
> **Estado:** inventário inicial e documentação; NÃO é auditoria recursiva exaustiva, nem conclusão funcional.  
> **Repositório oficial:** `LucianoToscano/PvPPokeGo` / `main`.  
> **Commit-base verificado:** `10c410de228f4b019d49f89af1375afdd0ec706a`.  
> **Branch QA:** `qa/protocolo-global-sem-perdas-20261121`.  
> **Protocolo:** [Desenvolvimento paralelo sem perdas](PROTOCOLO_GLOBAL_DESENVOLVIMENTO_PARALELO_SEM_PERDAS.md).

## Evidências da inspeção inicial

- **Base oficial:** `main` com merge recente do PR #40, consolidando código Android da linha PvPPokeGo-APK-Build.
- **Documentação consultada:** `README.md`, `docs/START_HERE_PVPPokeGo.md` e arquivos em `docs/`. Não localizado `AGENTS.md` nem `docs/GUIA_MESTRE.md`; não criar Mestre concorrente por suposição.
- **Arquitetura declarada:** PvPoke como fonte de dados local, `BattleEnergyTracker` com três estados, `BattleTextEventParser` e pipeline de observações visuais com confiança. Isso não comprova execução no aparelho.
- **PRs paralelos:** há tarefas experimentais de ImageDB, data sync, HUD, team builder, releases e CI pendentes. Preservar sem mesclar automaticamente.
- **Drive:** pastas de APKs, código, auditorias, assets, guias, backups e temporários. Há relatório histórico de regressões 0.5.10–0.5.39 e QA 0.5.40.
- **Regra do produto:** assistente externo (OCR/visão/overlay) sem injeção, gestos ou cliques automatizados no jogo, conforme START_HERE.
- **Testes não executados:** execução Android, OCR, energia, escudos, timing, estabilidade do overlay e build.

## Classificação preliminar A/B/C — sem exclusão

| Classe | Candidato | Estado |
|---|---|---|
| A | Duplicatas exatas ou temporários com referências verificadas | **Nenhum item aprovado ou excluído.** A confirmação de hash e dependências ainda é necessária. |
| B | Documentação histórica/atual com conteúdo sobreposto | **Analisar antes de propor mesclagem.** Preservar textos e decisões únicos. |
| C | Mestres, código, assets, builds funcionais e versões históricas | **Manter.** Nenhuma exclusão ou movimentação realizada. |

## Proteção de projetos paralelos

Não houve modificação de código-fonte, assets, Mestre, Drive, workflow, assinatura, APK ou release neste PR. Antes de integrar, comparar o SHA da `main` novamente, reavaliar PRs concorrentes e validar conflitos. O arquivo criado é uma anotação técnica de QA, subordinada à documentação canônica existente.

## Validação desta proposta

- Verificado: existência do repositório e da branch `main`, identidade do commit-base, estrutura inicial, documentação de entrada e pastas de Drive relacionadas.
- Não verificado: hash de todos os arquivos, dependências recursivas, execução de aplicativo, testes em aparelho, compilação e resultados de redes/APIs em produção.
- Rollback: fechar o PR sem merge; nenhum arquivo anterior foi substituído.
- Próximo passo: inventário recursivo e matriz de regressão por projeto, sem operações destrutivas.

