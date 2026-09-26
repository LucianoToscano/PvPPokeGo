# PvPPokeGo

Aplicativo Android de assistência PvP para Pokémon GO com overlay externo, contador de ataques e ferramentas de batalha.

## Arquitetura em desenvolvimento

O projeto separa reconhecimento visual, dados de batalha e estado da partida.

### PvPoke como fonte de dados

A integração usa o projeto PvPoke como fonte atualizável para:

- espécies e formas;
- tipos;
- ataques rápidos e carregados;
- geração de energia dos ataques rápidos;
- custo de energia dos ataques carregados;
- movesets/rankings de Grande, Ultra e Mestre.

A batalha não depende de acesso ao site PvPoke. Os dados são convertidos antes da execução e usados localmente.

### Memória dos três Pokémon inimigos

`BattleEnergyTracker` mantém três estados independentes.

- troca não zera a contagem;
- energia residual é preservada depois de um carregado;
- ao voltar para a batalha o Pokémon recupera o estado anterior;
- o slot só é encerrado quando o Pokémon desmaia ou a partida termina;
- os dois charged moves são armazenados em ordem, correspondendo aos botões esquerdo e direito do overlay;
- `setFastMove` e `setChargedMove` permitem correção manual pelas setas do overlay.

### Charged move pelo banner da batalha

`BattleTextEventParser` e `BattleObservationProcessor` tratam mensagens OCR como:

`Plusle usou Relâmpago!`

O charged move é aplicado independentemente de o jogador usar escudo. O custo é subtraído da energia estimada, a energia residual é preservada e os contadores são recalculados imediatamente. Eventos repetidos do mesmo banner em frames consecutivos são deduplicados.

Aliases localizados ficam em `data/localization/moves_pt_BR.tsv` e podem ser carregados com `MoveAliasTable`.

### Camada de visão com confiança

`core/src/main/kotlin/pvppokego/core/vision/VisionObservation.kt` implementa a primeira camada derivada da auditoria dos APKs:

- observações OCR/visuais com confiança por campo;
- `BattleFrameObservation` para cada frame;
- `StableObservationReducer` para exigir confirmações antes de alterar o estado da batalha.

Pipeline alvo:

`captura -> ROI -> observações -> estabilização -> eventos -> BattleSession/BattleEnergyTracker -> decisão -> overlay`

### Ícones de tipo

O projeto pode sincronizar localmente o conjunto SVG MIT de `partywhale/pokemon-type-icons` com:

```bash
python tools/sync_type_icons.py
```

Os assets devem ser empacotados no APK e usados offline durante a batalha. A atribuição está em `THIRD_PARTY_NOTICES.md`.

## Estrutura atual

```text
core/
  src/main/kotlin/pvppokego/core/
    battle/
    data/
    model/
    vision/

data/pvpoke/
data/localization/
docs/PVPOKE_INTEGRATION.md
docs/APK_CROSS_REFERENCE_2026-09-25.md
docs/APK_BASELINE_AND_IMPLEMENTATION_MAP_2026-09-26.md
docs/BATTLE_OVERLAY_FIXES_2026-09-26.md
docs/PROJECT_CONTEXT_MEMORY.md
tools/update_pvpoke_data.py
tools/sync_type_icons.py
THIRD_PARTY_NOTICES.md
```

## Atualizar dados PvPoke

```bash
python tools/update_pvpoke_data.py
```

Consulte `docs/PVPOKE_INTEGRATION.md` para o contrato completo de integração.

## Auditoria de APKs de referência

A análise comparativa e o baseline oficial corrigido estão em:

- `docs/APK_CROSS_REFERENCE_2026-09-25.md`;
- `docs/APK_BASELINE_AND_IMPLEMENTATION_MAP_2026-09-26.md`.

O Pokémon GO oficial 0.429.1 recebido em APKM é usado apenas como baseline para distinguir funções do jogo base das adicionadas por terceiros.

## Memória técnica do projeto

O contexto consolidado dos APKs analisados, funções candidatas, prioridades e regras de compartilhamento com PGLOT está em `docs/PROJECT_CONTEXT_MEMORY.md`.

> O PvPPokeGo é uma ferramenta externa de assistência. O core de batalha permanece separado de root/injeção/concealment e não depende de modificar o processo do Pokémon GO.
