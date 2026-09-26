# PvPPokeGo V03 — contrato do HUD clássico

Atualizado em: 2026-09-26

## Decisão visual

A V02 funcional introduziu um painel central escuro grande para os ataques do inimigo. O painel funcionou, e o preenchimento progressivo dos Charged Moves agradou, mas o design alterou demais o HUD original do PvPPokeGo.

A partir da V03, o padrão oficial volta a ser o HUD clássico/minimalista:

- sem retângulo escuro grande sobre a batalha;
- informações distribuídas próximas às áreas que já existiam no design anterior;
- painel de ataques do inimigo compacto, transparente e afastado do treinador adversário;
- ícones de tipos favoráveis em uma linha compacta no topo, junto ao cabeçalho inimigo;
- indicador de matchup do Pokémon atual no topo esquerdo;
- indicadores compactos para os Pokémon da reserva;
- seletor de liga não fica permanentemente na batalha: só aparece se a leitura automática falhar;
- painel de correção manual de energia fica oculto por padrão e pode ser aberto por pressão longa no nome do inimigo.

## Funções que devem permanecer

A volta ao design anterior não significa voltar a uma lógica antiga. A V03 mantém as melhorias funcionais acumuladas:

1. moveset inicial baseado no rank #1 do PvPoke para a liga selecionada;
2. leitura automática da liga no lobby (Grande/Ultra/Mestra), com seleção manual como fallback;
3. leitura dos três Pokémon do jogador no lobby;
4. memória independente dos três Pokémon inimigos, inclusive após troca e retorno;
5. energia residual preservada depois de Charged Move;
6. contador de Fast Moves recalculado depois do Charged Move;
7. OCR de qualquer Charged Move legal pelo banner `X usou Y`/`X used Y`;
8. atualização automática do primeiro Charged Move observado;
9. quando um segundo Charged Move diferente for observado, atualizar o outro slot ainda não confirmado;
10. seleção manual de Fast/Charged por toque nos próprios chips;
11. seleção manual nunca é substituída automaticamente pelo OCR até voltar para Automático;
12. carregamento visual progressivo dentro do círculo do Charged Move;
13. ícones SVG de tipos, sem emojis dependentes do fabricante;
14. recomendação compacta de matchup para Pokémon atual e reservas;
15. aplicação permanece externa ao Pokémon GO, usando MediaProjection/OCR e overlay.

## Regras de UX

- O HUD de batalha não deve aparecer por cima da tela principal do próprio PvPPokeGo enquanto nenhum inimigo tiver sido identificado.
- O usuário pode tocar no Fast Move ou em cada Charged Move para abrir o submenu de golpes legais daquele Pokémon.
- Pressão longa em Charged Move devolve aquele botão ao modo Automático (PvPoke/OCR).
- Pressão longa no nome do inimigo mostra/oculta a correção manual de energia.
- O preenchimento do Charged Move usa a energia estimada como proporção do custo do golpe.
- A posição dos ataques deve deixar um espaço visual claro antes do treinador humano adversário.

Este documento deve ser consultado antes de alterar o layout em versões futuras para evitar perda de funções ou nova mudança visual incompatível com o design aprovado.
