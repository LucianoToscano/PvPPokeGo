# PvPPokeGo — correções do overlay de batalha

Atualizado em: 2026-09-26

Este documento transforma os problemas observados nos screenshots em contrato de implementação para a camada Android. O core correspondente já expõe as operações de energia, OCR de charged move e seleção ordenada dos dois charged moves.

## 1. Contador depois do charged move

Regra obrigatória:

1. cada fast move detectado soma a energia do golpe ágil;
2. quando a energia estimada alcança o custo de um charged move, o contador pode mostrar `0`;
3. quando o inimigo usa um charged move, subtrair somente o custo desse move;
4. preservar energia residual;
5. recalcular imediatamente os dois contadores.

Exemplo: 56 de energia e `THUNDERBOLT` de custo 55 -> depois do uso sobra 1 e o contador deixa de ficar travado em `0`.

A atualização deve ocorrer pelo evento do banner de texto e não pela tela de escudo. Assim funciona tanto quando o jogador usa escudo quanto quando recebe o golpe sem escudo.

## 2. Banner `X usou Y`

A ROI de OCR do banner deve observar a faixa central-superior onde o jogo mostra mensagens como:

`Plusle usou Relâmpago!`

Fluxo:

`OCR -> BattleTextEventParser -> moveId -> BattleObservationProcessor -> BattleEnergyTracker.onChargedMove -> refresh do overlay`

O parser normaliza acentos e pequenos erros de OCR. Como PvPoke mantém nomes em inglês, aliases localizados ficam em `data/localization/moves_pt_BR.tsv` e são carregados com `MoveAliasTable`.

O mesmo banner costuma permanecer por vários frames. O processador aplica deduplicação temporal para não gastar a energia duas ou três vezes para o mesmo texto persistente.

## 3. Memória por Pokémon inimigo

A energia, número de fast moves, último charged e seleção de moves pertencem ao slot do Pokémon adversário. Troca de Pokémon não zera esses campos. O estado só é descartado quando o Pokémon desmaia ou a batalha é reiniciada.

## 4. Setas dos ataques

Os três seletores devem ser realmente interativos:

- `fast move ▼`;
- `charged esquerdo ▼`;
- `charged direito ▼`.

O core disponibiliza `availableFastMoves(slot)`, `availableChargedMoves(slot)`, `setFastMove(...)` e `setChargedMove(slot, buttonIndex, moveId)`.

`buttonIndex = 0` representa o charged da esquerda e `buttonIndex = 1` o da direita. A lista passou a ser ordenada para o Android não perder a correspondência visual dos dois botões. Se o usuário escolher no lado direito o move que já estava no lado esquerdo, os dois são trocados em vez de criar duplicata.

### Área de toque

Não limitar o toque ao pequeno triângulo. A linha inteira `ícone + nome + ▼` deve ter alvo mínimo de 44 dp. O overlay visual geral pode continuar pass-through; apenas os três chips de seleção precisam ser janelas/regiões touchable.

Ao tocar, abrir uma lista ancorada com somente golpes legais daquele Pokémon. A seleção deve atualizar nome, tipo, custo de energia e contadores no mesmo frame.

## 5. Ícones de tipo

Fonte escolhida pelo projeto:

`https://github.com/partywhale/pokemon-type-icons`

O repositório é MIT e a atribuição foi incluída em `THIRD_PARTY_NOTICES.md`.

Use `tools/sync_type_icons.py` em desenvolvimento/release para copiar os 18 SVGs para `assets/type-icons`. Não baixar ícones durante a batalha.

O Android deve preservar o desenho vetorial e as cores do SVG. Evitar emoji Unicode para tipos: emoji varia por fabricante, fonte e versão do Android e foi a causa visual de símbolos diferentes do esperado.

## 6. Posicionamento dos ícones superiores

Não usar coordenada absoluta de tela. Detectar primeiro os bounds dos dois cartões superiores do Pokémon GO e posicionar a partir deles.

### Seu Pokémon

O indicador de matchup (`forte / neutro / fraco`) fica ancorado ao cartão superior esquerdo, em uma pequena faixa imediatamente adjacente aos indicadores originais. Ele não deve cobrir nome, CP, Poké Balls ou shields.

### Inimigo

A faixa de tipos fortes contra o inimigo fica ancorada ao cartão superior direito, ao lado dos elementos originais do cabeçalho. Os ícones ficam em uma única linha compacta. Se não houver espaço para todos, mostrar os mais relevantes e um botão `›` para expandir; não empurrar a linha para o meio da tela.

### Regras de layout

- usar coordenadas relativas aos bounds detectados da UI do jogo;
- respeitar status bar/cutout;
- tamanho alvo: 26–32 dp por tipo no topo;
- gap: 3–5 dp;
- charged/fast widget permanece mais abaixo, separado do cabeçalho;
- perfis de ROI por resolução podem ajustar offsets, mas não substituir a ancoragem geométrica.

## 7. Atualização imediata da UI

Todo evento aplicado ao `BattleSession` deve emitir um snapshot novo para o overlay. Em especial:

- fast detectado -> energia/contadores;
- charged detectado -> gasto de energia/contadores;
- troca -> recuperar memória do slot;
- correção manual de move -> ícones/nome/contadores;
- desmaio -> encerrar slot.

O overlay não deve manter uma segunda cópia própria da energia. A fonte de verdade é `BattleEnergyTracker`.

## 8. Estado atual do repositório

O repositório atual contém o core Kotlin, dados, testes e documentação, mas ainda não contém o módulo Android responsável por `MediaProjection`, `WindowManager` e views do overlay. Portanto as correções de lógica já podem ser testadas no core; a mudança física de posição/toque será ligada assim que o módulo Android usado para gerar o APK for colocado neste repositório.
