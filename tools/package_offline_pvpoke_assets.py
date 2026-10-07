#!/usr/bin/env python3
"""Package a PvPoke snapshot into app/src/main/assets for offline battle use."""
from __future__ import annotations

import json
import re
import urllib.request
from datetime import datetime, timezone
from pathlib import Path

OUT = Path("app/src/main/assets")
REF = "f627e89e53c0c7b903fff097df7a0ad0ac95decc"
RAW = f"https://raw.githubusercontent.com/pvpoke/pvpoke/{REF}/src/data"
RAW_JS = f"https://raw.githubusercontent.com/pvpoke/pvpoke/{REF}/src/js"
PVP_TURN_MS = 500
LEAGUES = (1500, 2500, 10000)

def fetch_json(url: str):
    req = urllib.request.Request(url, headers={"User-Agent": "PvPPokeGo-offline-packager/1.0"})
    with urllib.request.urlopen(req, timeout=90) as r:
        return json.load(r)

def fetch_text(url: str) -> str:
    req = urllib.request.Request(url, headers={"User-Agent": "PvPPokeGo-offline-packager/1.0"})
    with urllib.request.urlopen(req, timeout=90) as r:
        return r.read().decode("utf-8")


def extract_cpms(pokemon_js: str) -> list[float]:
    match = re.search(r"var\s+cpms\s*=\s*\[([^\]]+)\]", pokemon_js, re.S)
    if not match:
        raise RuntimeError("Could not locate PvPoke cpms array in Pokemon.js")
    values = [
        float(token.strip())
        for token in match.group(1).split(",")
        if token.strip()
    ]
    if len(values) < 100:
        raise RuntimeError(f"PvPoke cpms array is unexpectedly short: {len(values)}")
    return values

def main() -> int:
    OUT.mkdir(parents=True, exist_ok=True)

    moves = fetch_json(f"{RAW}/gamemaster/moves.json")
    pokemon = fetch_json(f"{RAW}/gamemaster/pokemon.json")
    pokemon_js_url = f"{RAW_JS}/pokemon/Pokemon.js"
    base_url = f"{RAW}/gamemaster/base.json"
    cpms = extract_cpms(fetch_text(pokemon_js_url))
    base = fetch_json(base_url)
    settings = base.get("settings", {})
    max_buff_stages = int(settings.get("maxBuffStages", 4))
    buff_divisor = float(settings.get("buffDivisor", 4))
    (OUT / "gamemaster.json").write_text(
        json.dumps({"pokemon": pokemon, "moves": moves}, ensure_ascii=False, separators=(",", ":")),
        encoding="utf-8",
    )
    (OUT / "pvpoke_cpms.json").write_text(
        json.dumps({"format": "PvPPokeGo-pvpoke-cpms", "version": 1, "cpms": cpms}, separators=(",", ":")),
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
        "turnDurationMs": PVP_TURN_MS,
        "settings": {
            "maxBuffStages": max_buff_stages,
            "buffDivisor": buff_divisor,
        },
        "gamemasterSources": {
            "moves": f"{RAW}/gamemaster/moves.json",
            "pokemon": f"{RAW}/gamemaster/pokemon.json",
            "base": base_url,
            "cpms": pokemon_js_url,
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
