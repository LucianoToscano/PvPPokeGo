# PvPPokeGo

Aplicativo Android de assistência PvP para Pokémon GO com overlay externo,
contador de ataques e ferramentas de batalha.

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

A batalha não depende de acesso ao site PvPoke. Os dados são convertidos antes
da execução e usados localmente.

### Memória dos três Pokémon inimigos

`BattleEnergyTracker` mantém três estados independentes.

- troca não zera a contagem;
- energia residual é preservada depois de um carregado;
- ao voltar para a batalha o Pokémon recupera o estado anterior;
- o slot só é encerrado quando o Pokémon desmaia ou a partida termina.

### Meu time antes da batalha

`BattleSession` registra os três Pokémon detectados na tela pré-batalha. Os
tipos vêm do repositório local e podem alimentar a assistência visual de
fraquezas, resistências e vantagem de tipos.

## Estrutura atual

```text
core/
  src/main/kotlin/pvppokego/core/
    battle/
    data/
    model/

data/pvpoke/
docs/PVPOKE_INTEGRATION.md
docs/APK_CROSS_REFERENCE_2026-09-25.md
tools/update_pvpoke_data.py
THIRD_PARTY_NOTICES.md
```

## Atualizar dados PvPoke

```bash
python tools/update_pvpoke_data.py
```

Consulte `docs/PVPOKE_INTEGRATION.md` para o contrato completo de integração.

## Auditoria de APKs de referência

A análise comparativa de Poke Genie, iPogo, Pokemod e Pokémon GO oficial está em
`docs/APK_CROSS_REFERENCE_2026-09-25.md`. O documento registra quais ideias
arquiteturais podem ser reimplementadas no PvPPokeGo e quais devem permanecer
isoladas no projeto de recuperação.

> O PvPPokeGo é uma ferramenta externa de assistência. O motor descrito aqui
> não executa cliques, ataques, trocas ou injeção de código no Pokémon GO.
