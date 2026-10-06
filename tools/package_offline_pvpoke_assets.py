#!/usr/bin/env python3
"""Package a PvPoke snapshot into app/src/main/assets for offline battle use."""
from __future__ import annotations

import json
import urllib.request
from datetime import datetime, timezone
from pathlib import Path

OUT = Path("app/src/main/assets")
REF = "master"
RAW = f"https://raw.githubusercontent.com/pvpoke/pvpoke/{REF}/src/data"
LEAGUES = (1500, 2500, 10000)

def fetch_json(url: str):
    req = urllib.request.Request(url, headers={"User-Agent": "PvPPokeGo-offline-packager/1.0"})
    with urllib.request.urlopen(req, timeout=90) as r:
        return json.load(r)

def main() -> int:
    OUT.mkdir(parents=True, exist_ok=True)

    moves = fetch_json(f"{RAW}/gamemaster/moves.json")
    pokemon = fetch_json(f"{RAW}/gamemaster/pokemon.json")
    (OUT / "gamemaster.json").write_text(
        json.dumps({"pokemon": pokemon, "moves": moves}, ensure_ascii=False, separators=(",", ":")),
        encoding="utf-8",
    )

    ranking_sources = {}
    for cp in LEAGUES:
        url = f"https://pvpoke.com/data/rankings/all/overall/rankings-{cp}.json"
        ranking = fetch_json(url)
        (OUT / f"rankings-{cp}.json").write_text(
            json.dumps(ranking, ensure_ascii=False, separators=(",", ":")),
            encoding="utf-8",
        )
        ranking_sources[str(cp)] = url

    metadata = {
        "format": "PvPPokeGo-pvpoke-snapshot",
        "generatedAtUtc": datetime.now(timezone.utc).isoformat(),
        "pvpokeRef": REF,
        "gamemasterSources": {
            "moves": f"{RAW}/gamemaster/moves.json",
            "pokemon": f"{RAW}/gamemaster/pokemon.json",
        },
        "rankingSources": ranking_sources,
    }
    (OUT / "pvpoke_snapshot.json").write_text(
        json.dumps(metadata, ensure_ascii=False, indent=2),
        encoding="utf-8",
    )
    print("Offline PvPoke assets packaged:", ", ".join(p.name for p in OUT.glob("*.json")))
    return 0

if __name__ == "__main__":
    raise SystemExit(main())
