# PvPoke integration architecture

## Goal

PvPPokeGo uses PvPoke as an update source for battle data while keeping the live
battle engine completely local. The overlay must not depend on pvpoke.com being
reachable during a match.

The current upstream PvPoke data layout provides:

- `src/data/gamemaster/pokemon.json`
- `src/data/gamemaster/moves.json`
- `src/data/rankings/all/overall/rankings-1500.json`
- `src/data/rankings/all/overall/rankings-2500.json`
- `src/data/rankings/all/overall/rankings-10000.json`

The importer converts those files to a compact TSV format so that the Android
app can keep a stable internal schema even if upstream JSON details change.

## Modules

### `PvpokeRepository`

Responsibilities:

- resolve species and forms by `speciesId`;
- expose one or two Pokemon types;
- expose fast and charged move pools;
- expose fast-move energy gain;
- expose charged-move energy cost;
- expose PvPoke-recommended movesets when ranking data is present.

The rest of the application must not parse PvPoke JSON directly.

### `BattleEnergyTracker`

Maintains one independent state for each of the opponent's three Pokemon.

Rules:

1. A fast attack adds its `energyGain`.
2. Estimated energy is capped at 100.
3. A charged attack subtracts only that attack's energy cost.
4. Residual energy is preserved.
5. Switching out does not reset energy or fast-move count.
6. Switching the same Pokemon back in restores its exact stored state.
7. State is cleared for that slot only when the Pokemon faints.
8. A new battle resets all three enemy slots.

Example:

- enemy has 64 energy;
- enemy uses a 40-energy charged move;
- tracker stores 24 energy;
- if its fast move gains 8 energy, another 40-energy move is shown as
  `2 fast attacks remaining`.

### `BattleSession`

Owns one battle lifetime and stores:

- the three Pokemon identified on the player's pre-battle team screen;
- the opponent's three independent energy states;
- active/benched/fainted state.

The vision/overlay layer feeds detections into `BattleSession`. The session does
not click, inject, automate, or modify Pokemon GO.

### `TypeMatchupAdvisor`

Uses the three pre-battle species and their locally stored types to provide a
type-only recommendation against an identified opponent.

This is advisory only. It is intentionally separate from move detection and can
later be upgraded with actual observed attacks and full PvP simulations.

## Updating local data

From the repository root:

```bash
python tools/update_pvpoke_data.py
```

Generated files:

```text
data/pvpoke/
  moves.tsv
  pokemon.tsv
  rankings.tsv
  metadata.json
```

For an Android application, these generated files can be copied into:

```text
app/src/main/assets/pvpoke/
```

and loaded at startup into `InMemoryPvpokeRepository`.

Do not run the updater during an active battle.

## Overlay integration contract

A future vision layer should emit events similar to:

```text
PreBattlePokemonDetected(mySlot, speciesId)
EnemyPokemonDetected(enemySlot, speciesId)
EnemyFastMoveIdentified(enemySlot, moveId)
EnemyFastMoveObserved(enemySlot)
EnemyChargedMoveObserved(enemySlot, moveId)
EnemySwitchedTo(enemySlot)
EnemyFainted(enemySlot)
```

The overlay renders the state returned by `BattleEnergyTracker`; it should not
maintain a second independent counter.

## Manual correction

Visual recognition can miss animations. For that reason the engine exposes:

- `setFastMove(...)`
- `setChargedMoves(...)`
- `setEstimatedEnergy(...)`

Manual corrections immediately become the authoritative state used by the
overlay.

## Data-source boundary

PvPoke is an upstream data source, not a runtime service dependency. Keeping the
boundary behind `PvpokeRepository` allows another data provider or a changed
PvPoke schema to be supported without rewriting the battle engine.
