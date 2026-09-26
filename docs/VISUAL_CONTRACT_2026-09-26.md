# PvPPokeGo — Contrato Visual Canônico do Overlay

Atualizado em: 2026-09-26

Este documento registra as decisões visuais que NÃO devem ser perdidas em versões futuras. Ele complementa `docs/CONSOLIDATED_PROJECT_CONTEXT_2026-09-26.md`.

## Regra principal

A base visual aprovada é o HUD compacto mostrado nos mockups do projeto. Novas funções devem ser adicionadas por cima dessa base sem redesenhar toda a interface e sem remover funções já existentes.

## Topo da batalha

Referência 864x1536:

- indicador do Pokémon atual: centro aproximado `(42, 38)`;
- botão de ocultar: centro aproximado `(96, 38)`;
- tipos fortes contra o inimigo: centros aproximados `574, 616, 658, 701` em `y=39`;
- seta: centro aproximado `739`;
- tipos do inimigo: centros aproximados `777, 819` em `y=39`.

Esses elementos ficam junto ao HUD original do Pokémon GO, sem cobrir nome, CP, Pokébolas, escudos ou treinador.

## Ícones de tipo

O visual desejado é o mesmo estilo circular usado na folha de referência fornecida no projeto:

- círculo colorido por tipo;
- símbolo branco central;
- borda branca fina;
- contraste suficiente para leitura rápida;
- mesma linguagem visual nos tipos do topo, golpes do inimigo e reservas.

A paleta de cor deve seguir a referência fornecida, e não emoji/texto genérico.

## Golpes do inimigo

Layout em triângulo:

- Fast Move no topo;
- Charged Move 1 abaixo à esquerda;
- Charged Move 2 abaixo à direita.

Cada golpe mostra:

- ícone circular do tipo;
- nome abaixo;
- `▼` para abrir submenu de golpes legais do Pokémon;
- contador de Fast Moves restante em cada Charged.

O padrão inicial vem do moveset #1 do PvPoke para a liga/copa selecionada.

Quando o OCR observar `X usou Y`, o golpe real deve corrigir automaticamente um slot previsto não confirmado. Um segundo Charged diferente corrige/confirma o outro slot. Escolha manual do usuário não é sobrescrita.

## Carregamento dos Charged Moves do inimigo

O ícone deve se comportar visualmente como um botão de Charged Move do próprio Pokémon GO:

- área ainda sem energia permanece escura/cinza;
- a cor do tipo sobe de baixo para cima conforme a energia acumulada;
- ao ficar pronto, o círculo fica totalmente preenchido;
- o número lateral mostra quantos Fast Moves faltam;
- após o inimigo usar um Charged Move, descontar o custo real, preservar energia residual e recalcular imediatamente os dois Charged Moves.

## Reservas

Para cada reserva conhecida:

- indicador `✓`, `•` ou `×` conforme matchup;
- tipos do Pokémon usando os mesmos ícones circulares;
- posição junto às cartas de reserva do Pokémon GO, sem cobrir HP/CP.

## Ocultar / restaurar HUD

O HUD deve ter um botão vermelho `×` pequeno no topo esquerdo para ocultar a interface durante a batalha.

Quando oculto:

- todos os elementos e áreas de toque do overlay desaparecem;
- fica somente um pequeno botão/bubble do PvPPokeGo para restaurar o HUD;
- a batalha continua sendo analisada normalmente em segundo plano;
- o overlay não pode bloquear os toques do Pokémon GO.

## Interação

O HUD inteiro permanece click-through. Somente pequenas janelas transparentes sobre:

- Fast Move;
- Charged Move 1;
- Charged Move 2;
- botão ocultar/restaurar

podem receber toque.

## Continuidade

Não substituir este layout por painel central grande. Não reposicionar os elementos sem comparar com as referências aprovadas. Novos recursos como Battle Brain, timing, Sac Swap, Win Condition, timers, confiança e recorder devem entrar de forma adaptativa/minimalista, preservando este contrato visual.
