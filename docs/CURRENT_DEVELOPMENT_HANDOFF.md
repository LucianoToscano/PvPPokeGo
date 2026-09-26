# PvPPokeGo — Handoff atual (V04 sobre base V03)

Atualizado em: 2026-09-26

Este branch (`v04-v03-stable-base`) é a linha de desenvolvimento que parte da V03 aprovada e adiciona funções sem redesenhar o HUD.

## Memória canônica do projeto

A memória completa dos chats, arquitetura, APKs analisados, PvPoke, Battle Brain e roadmap está na `main`:

- `docs/CONSOLIDATED_PROJECT_CONTEXT_2026-09-26.md`
- `docs/VISUAL_CONTRACT_2026-09-26.md`

Esses documentos devem ser lidos primeiro por qualquer nova conta/GPT/desenvolvedor.

## Estado atual deste branch

- base visual/funcional derivada da V03 aprovada;
- OCR via captura de tela/MediaProjection;
- reconhecimento de jogador e oponente;
- leitura da liga no lobby (Grande/Ultra/Mestra) com fallback manual;
- moveset inicial provável pelo PvPoke;
- Fast/Charged 1/Charged 2 com submenu manual;
- Charged observado por OCR pode corrigir automaticamente slots previstos;
- segundo Charged diferente ocupa/confirma o outro slot;
- escolha manual é protegida contra substituição automática;
- energia residual preservada depois de Charged;
- memória individual dos inimigos ao trocar e voltar;
- contador de Fast Moves restante para os dois Charged;
- HUD click-through com pequenas zonas de toque apenas nos controles;
- botão ocultar/restaurar HUD;
- ícones de tipo no estilo circular da referência;
- carregamento do Charged inimigo de baixo para cima, imitando o botão de Charged do jogo;
- posições do topo ancoradas nas coordenadas aprovadas do mockup 864x1536.

## Regra visual

Não substituir o HUD compacto por painel central grande. O contrato visual da `main` é a referência obrigatória.

## Próximos passos prioritários

1. validar em aparelho as posições do topo e das reservas;
2. validar botão ocultar/restaurar e zonas de toque;
3. validar Fast Move detector em batalhas reais;
4. confirmar OCR de qualquer banner `X usou Y` e recálculo de energia;
5. melhorar confirmação visual `PREVISTO #1` / `✓ CONFIRMADO`;
6. avançar para Battle Brain, timing de Charged, Sac Swap/Catch Assist, Win Condition e importação `PGSStats.json`.

## Restrição de arquitetura

O PvPPokeGo continua como assistente externo: não injeta código no Pokémon GO, não modifica o processo do jogo e não envia cliques/gestos para o jogo.
