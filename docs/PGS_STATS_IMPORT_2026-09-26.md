# PGSStats import plan — 2026-09-26

`PGSStats.json` was evaluated as a local source for collection and team data in PvPPokeGo.

## Useful fields

Per Pokemon the export includes species/form, CP, IV Attack/Defense/Stamina, move IDs, favorite/tags, shiny/lucky state, Mega-related state and PvP-related stats when available.

The export also contains saved battle parties, which can be mapped into the user's Great/Ultra/Master team setup.

## Proposed flow

PGSStatsImporter -> ImportedPokemon/ImportedBattleParty -> PvPCollectionAdapter -> PvPIvRankEngine/BattleTeam/MoveTimingEngine.

## Priorities

1. Import collection and saved battle parties.
2. Resolve species and move IDs against the local Game Master/PvPoke data.
3. Calculate PvP IV ranks locally.
4. Compare current moves against recommended movesets.
5. Pre-fill the user's own team in the overlay.
6. Keep OCR focused on live opponent recognition and as a fallback.

## Privacy

Do not commit real export files. Tests should use small anonymized fixtures containing only fields needed by the battle engine.

## Sharing with PGLOT

The raw parser and normalized import models can be shared. PGLOT may consume the full snapshot for inventory and analytics, while PvPPokeGo should consume only collection/team/IV/CP/move data.
