# Generated PvPoke data

This directory is intentionally kept without the full generated dataset in the
repository.

Generate/update the local files with:

```bash
python tools/update_pvpoke_data.py
```

Expected outputs:

- `moves.tsv`
- `pokemon.tsv`
- `rankings.tsv`
- `metadata.json`

The Android app should bundle a generated snapshot in its assets and use it
offline during battles.
