# PvPPokeGo — V03 baseline visual congelado

Data: 2026-09-26

## APK de referência confirmado

O APK fornecido pelo proprietário como a versão visual/funcional em que o projeto estava caminhando corretamente foi comparado com o artefato histórico do GitHub Actions da branch `v03-overlay-fixes`.

SHA-256 do APK de referência:

`7c44f8fb8eb2aff24050cdb78945ad5a8dc9f6e13692bc75cb44d2880f977a1d`

O hash é idêntico ao APK produzido pelo workflow histórico dessa branch. Portanto `v03-overlay-fixes` é a fonte canônica do design V03.

## Regra de evolução

A branch `v04-v03-stable-base` parte diretamente dessa fonte e deve preservar o `BattleOverlayView`/geometria do HUD V03. Novas funções entram por baixo do design, sem substituí-lo por um painel novo.

### Preservar

- posição dos ícones superiores;
- triângulo compacto Fast + 2 Charged;
- indicadores das reservas;
- progresso visual dos carregados;
- escala/opacidade e opções já existentes;
- overlay visual pass-through sobre o Pokémon GO.

### Adicionar sem redesenhar

- moveset inicial recomendado pelo PvPoke da liga;
- leitura automática da liga Grande/Ultra/Mestra no lobby e seleção manual de fallback;
- memória individual de energia/contagem/moves do inimigo após troca;
- OCR de `X usou Y` para qualquer charged move legal conhecido;
- aprendizado automático do charged real, corrigindo defaults errados;
- primeiro charged observado confirma/corrige um slot e o segundo diferente o outro;
- escolhas manuais protegidas contra sobrescrita automática;
- submenus tocáveis somente sobre os três ícones de ataques, sem transformar a tela inteira em touchable;
- cálculo por custo real de energia e preservação de energia residual.

## Limite arquitetural

O PvPPokeGo permanece um assistente externo. Não deve injetar no Pokémon GO, ler memória interna do jogo nem enviar toques/comandos ao processo do jogo. IDs de golpes são resolvidos localmente a partir de dados PvP/PvPoke depois de observação visual/OCR.

## Continuidade entre contas/desenvolvedores

Para continuar o projeto em outra conta/GPT, começar por:

1. esta documentação;
2. branch `v04-v03-stable-base`;
3. `docs/PROJECT_CONTEXT_MEMORY.md` quando disponível no repositório;
4. `BattleEngine.kt`, `EnergyTracker.kt`, `BattleOverlayService.kt`, `BattleOverlayView.kt` e `BattleOcrDetector.kt`.

A regra principal é: não recriar a interface do zero. Evoluir a partir do V03 confirmado.
