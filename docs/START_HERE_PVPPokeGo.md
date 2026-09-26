# START HERE — PvPPokeGo

Este é o ponto único de entrada para continuar o projeto em outro chat, outra conta ou com outro desenvolvedor sem recomeçar do zero.

## 1. Leia primeiro

`docs/CONSOLIDATED_PROJECT_CONTEXT_2026-09-26.md`

Esse arquivo consolida as decisões dos chats do projeto, arquitetura, referências analisadas, integração PvPoke, OCR/visão, memória de batalha, Battle Brain, roadmap, pendências e regras de continuidade.

## 2. Contrato visual obrigatório

`docs/VISUAL_CONTRACT_2026-09-26.md`

Novas funções devem preservar o HUD compacto aprovado. Não substituir por painel central grande nem reposicionar elementos sem comparar com as referências aprovadas.

## 3. Linha Android atual

Branch de desenvolvimento:

`v04-v03-stable-base`

Ela parte da V03 que foi validada como melhor base visual/funcional e recebe melhorias incrementalmente.

No próprio branch existe:

`docs/CURRENT_DEVELOPMENT_HANDOFF.md`

com o estado atual da implementação e próximos passos.

## 4. Regra de produto

PvPPokeGo é um assistente externo de batalha:

- captura/visão/OCR;
- estado da batalha;
- energia e golpes prováveis/confirmados;
- recomendações;
- overlay/vibração/voz.

Não deve injetar/modificar Pokémon GO nem enviar cliques/gestos para o jogo.

## 5. Referência de dados

PvPoke é a principal referência open source para dados, rankings e simulação. O repositório mantém integração/versionamento próprio e deve preferir dados locais/offline em releases estáveis.

## 6. Prioridade imediata

Antes de avançar para o Battle Brain completo, validar no aparelho:

1. posições do HUD;
2. ícones de tipo;
3. preenchimento visual dos Charged Moves do inimigo;
4. botão ocultar/restaurar;
5. menus de Fast/Charged;
6. reconhecimento e contagem real dos Fast Moves;
7. OCR de qualquer banner `X usou Y` e recálculo de energia residual;
8. memória dos três adversários e das reservas do usuário.

Depois disso, avançar para Battle Brain, Move Timing, Sac Swap/Catch Assist, Win Condition e importação `PGSStats.json`.
