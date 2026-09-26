# Enemy Move Selector + Auto Detection — 2026-09-26

## Objetivo

O overlay do PvPPokeGo deve permitir tocar no ícone/nome do golpe do Pokémon inimigo e abrir um submenu com somente os golpes legais daquela espécie.

## Comportamento inicial

Quando um inimigo é identificado, o core consulta `PvpokeRepository.recommendedMovesets(speciesId, leagueCp)` e usa o moveset de rank mais alto disponível como previsão inicial:

- fast move recomendado;
- até dois charged moves recomendados.

Esses valores são previsões, não confirmações.

## Seleção manual no overlay

### Fast move

Tocar no fast move abre a lista de `availableFastMoves(slot)`.

Cada item deve mostrar:

- nome;
- tipo;
- energia gerada;
- turns/tempo quando disponível;
- indicador de legado/Elite quando o dataset fornecer esse metadado.

Ao escolher, chamar `setFastMove(slot, moveId)`.

### Charged move

Tocar em qualquer um dos dois charged moves abre `availableChargedMoves(slot)`.

Ao escolher:

- botão esquerdo -> `setChargedMove(slot, 0, moveId)`;
- botão direito -> `setChargedMove(slot, 1, moveId)`.

Se o usuário escolher no segundo botão um move que já está no primeiro, os dois são trocados em vez de duplicados.

## Detecção automática por batalha

O Pokémon GO mostra um banner de texto quando um charged move é usado, por exemplo:

`<Pokémon> usou <Golpe>!`

O core já possui:

- `BattleTextEventParser` para transformar o texto OCR em `moveId` local;
- aliases pt-BR -> ID para nomes localizados;
- fuzzy matching para tolerar pequenos erros de OCR;
- `BattleObservationProcessor` para deduplicar o mesmo banner visto em vários frames e aplicar o evento ao `BattleEnergyTracker`.

Portanto não é necessário depender de leitura de memória/processo do jogo para descobrir o ID do charged move. O fluxo alvo é:

`MediaProjection/screenshot -> OCR do banner -> texto -> BattleTextEventParser -> moveId -> BattleObservationProcessor -> BattleEnergyTracker -> overlay`

## Regra nova de confirmação automática

`EnemyPokemonState` agora diferencia:

- `selectedChargedMoveIds`: os dois moves mostrados no overlay, que podem começar como previsão;
- `confirmedChargedMoveIds`: moves realmente observados em batalha.

Quando um charged move diferente da previsão é observado:

1. substitui somente um slot ainda não confirmado;
2. o move observado vira confirmado;
3. se o mesmo move aparecer novamente, o segundo slot NÃO é alterado;
4. quando um segundo move diferente for observado, ele substitui o outro slot ainda não confirmado;
5. os dois moves confirmados ficam preservados até correção manual/reset da batalha.

Isso implementa exatamente o comportamento desejado para descobrir progressivamente o moveset real do adversário.

## Correção manual

Se OCR identificar um golpe errado, o usuário deve poder tocar no botão e selecionar outro. A substituição manual daquele botão remove a confirmação do move substituído, permitindo corrigir o estado.

## Visual recomendado

Estados visuais distintos:

- `PREVISTO`: move rank #1/PvPoke, aparência normal/mais discreta;
- `CONFIRMADO`: move observado em batalha, mostrar pequeno ✓ ou borda destacada;
- `MANUAL`: opcionalmente indicar que foi escolhido pelo usuário;
- contador ao lado: fast moves restantes até carregar o golpe.

O submenu deve fechar imediatamente após a escolha e não bloquear a batalha mais que o necessário.

## Sobre obter IDs diretamente do inimigo

Não assumir acesso a um identificador oculto do moveset do adversário em runtime. A implementação preferida é observacional e externa:

- charged move: reconhecer o texto visível do banner e mapear para o ID local;
- fast move: usar a previsão inicial e permitir correção manual; no futuro pode haver inferência visual por cadência/animação/turns, com confiança, sem depender de acesso interno ao processo do jogo.

## Arquivos do core relacionados

- `core/src/main/kotlin/pvppokego/core/model/BattleModels.kt`
- `core/src/main/kotlin/pvppokego/core/battle/BattleEnergyTracker.kt`
- `core/src/main/kotlin/pvppokego/core/battle/BattleObservationProcessor.kt`
- `core/src/main/kotlin/pvppokego/core/vision/BattleTextEventParser.kt`
- `core/src/main/kotlin/pvppokego/core/data/PvpokeRepository.kt`

## Estado em 2026-09-26

Core implementado para:

- previsão rank #1;
- listas legais por espécie;
- troca manual de fast/charged move;
- OCR -> move ID para charged move;
- deduplicação do banner;
- gasto de energia;
- confirmação progressiva de até dois charged moves sem sobrescrever o primeiro confirmado;
- testes unitários para a preservação dos slots confirmados.

Ainda falta ligar esses recursos a uma camada Android de overlay clicável/menu visual no repositório atual.
