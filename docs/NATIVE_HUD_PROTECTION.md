# Regra de proteção do HUD nativo do Pokémon GO

Esta regra é obrigatória para todo elemento visual do PvPPokeGo.

## Regra principal

Nenhum ícone, texto, alerta, botão, menu, contador, indicador ou outra imagem do overlay pode ser desenhado por cima de um elemento que já pertence à interface nativa do Pokémon GO.

A captura limpa de batalha fornecida pelo projeto é a referência visual para as zonas nativas protegidas.

## Zonas sempre protegidas

O overlay deve preservar, com margem de segurança:

- barra de status do Android;
- botão/tipo nativo no topo esquerdo;
- área nativa de tipos do Pokémon aliado;
- área nativa de tipos do Pokémon inimigo;
- caixas brancas de nome e PC dos dois Pokémon;
- Pokébolas e escudos dentro dessas caixas;
- botão de troca/corrida do jogo;
- barras de HP e textos nativos de efetividade;
- cartões dos Pokémon reserva;
- botões e textos dos ataques carregados do jogador;
- telas nativas de escudo, troca e mensagens de ataque.

## Regra especial dos tipos no topo

Cada Pokémon possui 1 ou 2 tipos. Portanto, o PvPPokeGo deve reservar SEMPRE espaço para até 2 ícones de tipo nativos, mesmo quando o Pokémon atual possuir apenas 1 tipo.

- Lado aliado: a faixa dos dois possíveis tipos nativos fica reservada. O indicador verde/cinza/vermelho do PvPPokeGo começa somente depois dessa faixa, com uma pequena margem.
- Lado inimigo: a faixa dos dois possíveis tipos nativos fica reservada no extremo direito. Os ícones extras de fraqueza do PvPPokeGo terminam antes dessa faixa, com uma pequena margem.
- O botão de ocultar/mostrar pode usar apenas a faixa central livre entre os dois lados.

## Implementação

`BattleOverlayView` deve aplicar uma máscara de exclusão (`clipOutNativePokemonGoUi`) antes de desenhar o HUD. Essa máscara é a última barreira de segurança: mesmo que uma coordenada futura esteja errada, o overlay não deve conseguir pintar dentro das regiões nativas protegidas.

Qualquer novo elemento de HUD deve respeitar esta regra antes de ser aceito.