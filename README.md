# PvPPokeGo V03

Assistente visual externo para batalhas PvP do Pokémon GO.

O aplicativo usa `MediaProjection` para analisar pixels visíveis da tela e `TYPE_APPLICATION_OVERLAY` para exibir informações. Não modifica Pokémon GO, não injeta código, não acessa memória do jogo e não automatiza cliques.

## Correções da V03

- triângulo de golpes mais compacto e alinhado à referência;
- ícones de tipos superiores ancorados ao card do oponente: `tipos fortes › tipos do inimigo`;
- símbolos de tipo desenhados no próprio app em vez de abreviações;
- contador automático de Fast Moves pela redução visual da barra de HP do jogador;
- fallback por movimento/animação somente quando a barra de HP não puder ser localizada;
- detecção visual do ataque carregado e reset automático do contador/energia;
- preservação de energia residual quando a estimativa é suficiente;
- estado individual de cada Pokémon inimigo preservado quando ele troca e volta;
- memória dos três Pokémon do jogador para exibir indicador e tipos das duas reservas;
- indicadores/tipos das reservas posicionados ao lado dos cards originais do jogo.

### Reservas
Os cards laterais da batalha não mostram o nome do Pokémon em texto. A V03 tenta memorizar os três Pokémon na tela de equipe/pré-batalha e também aprende cada Pokémon quando ele entra em campo. Para melhor resultado, inicie a captura antes de entrar na batalha.

## Créditos

Desenvolvimento PvPPokGo by: LucianoToscano  
(84) 99131-9021
