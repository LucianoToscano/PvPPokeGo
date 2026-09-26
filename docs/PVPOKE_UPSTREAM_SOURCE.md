# PvPoke upstream source

Updated: 2026-09-26

PvPPokeGo links the official PvPoke repository as a Git submodule at:

`third_party/pvpoke`

Upstream repository: `https://github.com/pvpoke/pvpoke`

Pinned upstream commit at integration time:

`9dab4bcc8ce8cf201cca34c7632688674084eb16`

Upstream branch: `master`

## Why a submodule

The upstream repository is large and contains the full web application, assets, articles, build files, data and simulation sources. Keeping it as a submodule gives developers access to the exact official source without duplicating the entire repository in PvPPokeGo.

The Android application should not bundle the whole submodule. Release builds should import/convert only the battle data and logic that PvPPokeGo actually needs.

## Important upstream areas

- `src/data/gamemaster/` — Pokemon, moves, formats and validation data.
- `src/data/gamemaster.min.json` — combined/minified game master consumed by the site.
- `src/js/GameMaster.js` — game master loader/indexing logic.
- `src/js/pokemon/` — Pokemon model/calculation logic.
- `src/js/battle/Battle.js` — battle simulation engine.
- `src/js/battle/actions/` — battle decision/action state logic.
- `src/js/battle/rankers/` — ranking and team-analysis logic.

## Initialize after cloning PvPPokeGo

```bash
git submodule update --init --recursive
```

Or clone everything in one command:

```bash
git clone --recurse-submodules https://github.com/LucianoToscano/PvPPokeGo.git
```

## Update PvPoke later

```bash
git submodule update --remote --merge third_party/pvpoke
```

Review upstream changes before committing a new submodule pointer because battle values, moves, formats and simulator behavior can change.

## Local Android data

`tools/update_pvpoke_data.py` remains the preferred release-time converter. It downloads/normalizes the required PvPoke data into the compact `data/pvpoke/` files used offline by PvPPokeGo.

Do not make live PvPoke network requests during an active battle. The battle assistant should use locally generated data for speed, reliability and reproducibility.

## License

PvPoke source code is distributed under the MIT License. Preserve the upstream copyright/license notice when code or generated snapshots are distributed. Pokemon artwork, names, logos and other third-party assets may have separate rights and should not be assumed to be covered by the software license.
