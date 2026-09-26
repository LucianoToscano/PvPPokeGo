# PvPPokeGo — Mapa Final de Posicionamento do HUD

Atualizado em: 2026-09-26

Este documento complementa o contrato visual canônico. A referência principal continua sendo a imagem aprovada do HUD. As regras abaixo definem onde cada elemento extra do PvPPokeGo deve ficar sem cobrir ou duplicar o HUD nativo do Pokémon GO.

## 1. Topo esquerdo — Pokémon atual

- O indicador de matchup do Pokémon atual (`verde`, `cinza` ou `vermelho`) fica ACIMA da borda branca da caixa nativa que mostra nome + CP.
- Ele deve ficar alinhado verticalmente com os ícones nativos do topo.
- Ele deve ficar ao lado dos ícones nativos, nunca por cima do nome, CP, Pokébolas ou escudos.
- Apenas UM indicador deve existir para o Pokémon atual.
- O botão vermelho `×` permanece separado do indicador e serve para ocultar o HUD.

Significado:
- verde = matchup favorável / pode permanecer;
- cinza = neutro;
- vermelho = matchup desfavorável / considerar troca.

## 2. Topo direito — Pokémon inimigo

- NÃO duplicar os tipos do inimigo, porque o Pokémon GO já mostra esses ícones nativamente.
- O PvPPokeGo mostra somente os TIPOS FORTES CONTRA O INIMIGO (fraquezas dele).
- Esses ícones ficam ACIMA da borda branca da caixa nativa que mostra nome + CP do inimigo.
- Devem ficar alinhados com os ícones nativos do Pokémon GO.
- Deve existir um pequeno espaço entre o último ícone nativo do jogo e o primeiro ícone extra do PvPPokeGo.
- Os ícones devem ficar colados no topo, sem margem vertical extra.

Regra visual:
`[ícones nativos do jogo]  espaço  [fraquezas do inimigo]`

## 3. Centro-direita — golpes do inimigo

Manter o layout aprovado em triângulo:

- Fast Move no topo;
- Charged Move 1 abaixo à esquerda;
- Charged Move 2 abaixo à direita.

Cada golpe mostra:
- ícone circular de tipo;
- nome abaixo;
- `▼` para abrir submenu;
- Charged Moves com preenchimento de energia de baixo para cima.

O cluster deve ficar afastado do treinador humano inimigo e não cobrir HP, personagem ou área importante de batalha.

## 4. Reservas — lado direito

Para cada Pokémon reserva:

- A caixa cinza nativa com a imagem do Pokémon permanece intacta.
- Os ÍCONES DE TIPO da reserva ficam IMEDIATAMENTE EMBAIXO da caixa cinza, mais altos e próximos da borda inferior da própria caixa; não devem ficar soltos mais abaixo na tela.
- O INDICADOR DE MATCHUP (`verde`, `cinza` ou `vermelho`) fica DO LADO DIREITO da caixa, CENTRALIZADO verticalmente no meio dela.
- Cada reserva possui seu próprio indicador.
- Não colocar o indicador por cima da imagem do Pokémon.
- Não colocar os tipos ao lado da imagem; tipos ficam abaixo da caixa.
- A visibilidade dos elementos extras da reserva DEVE ESPELHAR a visibilidade da caixa nativa do Pokémon GO.
- Quando um Pokémon da reserva desmaiar e a caixa/imagem dele desaparecer do HUD nativo, TODOS os elementos extras ligados a essa reserva também desaparecem imediatamente: tipos, indicador de matchup e qualquer outro ícone auxiliar.
- Não reaproveitar a posição da reserva derrotada para outro conjunto de ícones soltos. Cada conjunto permanece vinculado ao card correspondente enquanto esse card existir.

Estrutura visual:

`[ caixa da reserva ]   [ indicador matchup ]`

`      [ tipo 1 ] [ tipo 2 ]`

Ao desmaiar:

`[ card desaparece ] => [ indicador + tipos desaparecem juntos ]`

## 5. Parte inferior

- Manter um botão discreto de olho/olho riscado para ocultar/restaurar o overlay.
- Ele deve ficar na região inferior central, sem cobrir os Charged Moves do jogador.
- Quando o HUD for ocultado, a análise continua em segundo plano.

## 6. Opacidade

- 100% de opacidade significa elementos totalmente sólidos.
- Não aplicar alpha duas vezes em container + ícone.
- Tipos, indicadores e golpes devem permanecer nítidos e legíveis.

## 7. Regra de não-regressão

Não redesenhar o HUD.

Qualquer nova função deve:
- respeitar a imagem de referência aprovada;
- manter os elementos colados/alinhados ao HUD nativo;
- não duplicar informação já mostrada pelo Pokémon GO;
- não cobrir nome, CP, HP, Pokébolas, escudos, Charged Moves do usuário ou cartas de reserva;
- corrigir posicionamento fino sem trocar a linguagem visual aprovada.
