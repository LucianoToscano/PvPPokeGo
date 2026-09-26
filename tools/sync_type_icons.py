#!/usr/bin/env python3
"""Vendor the MIT-licensed Pokemon type SVG set used by the overlay.

Source: https://github.com/partywhale/pokemon-type-icons

The script runs at development/release time only. The Android overlay must use
local bundled assets during battle and must not fetch icons at runtime.
"""

from __future__ import annotations

import argparse
import json
import urllib.request
from datetime import datetime, timezone
from pathlib import Path

SOURCE_REPO = "https://github.com/partywhale/pokemon-type-icons"
RAW_BASE = "https://raw.githubusercontent.com/partywhale/pokemon-type-icons/main"
TYPE_NAMES = (
    "bug",
    "dark",
    "dragon",
    "electric",
    "fairy",
    "fighting",
    "fire",
    "flying",
    "ghost",
    "grass",
    "ground",
    "ice",
    "normal",
    "poison",
    "psychic",
    "rock",
    "steel",
    "water",
)


def fetch_text(url: str) -> str:
    request = urllib.request.Request(
        url,
        headers={"User-Agent": "PvPPokeGo-type-icon-sync/1.0"},
    )
    with urllib.request.urlopen(request, timeout=30) as response:
        return response.read().decode("utf-8")


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument(
        "--output",
        default="assets/type-icons",
        help="Directory where SVG files are vendored.",
    )
    args = parser.parse_args()

    output = Path(args.output)
    output.mkdir(parents=True, exist_ok=True)

    for type_name in TYPE_NAMES:
        url = f"{RAW_BASE}/icons/{type_name}.svg"
        svg = fetch_text(url)
        if "<svg" not in svg:
            raise RuntimeError(f"Unexpected response for {url}")
        (output / f"{type_name}.svg").write_text(svg, encoding="utf-8")

    license_text = fetch_text(f"{RAW_BASE}/LICENSE")
    (output / "UPSTREAM_LICENSE.txt").write_text(license_text, encoding="utf-8")

    metadata = {
        "source": SOURCE_REPO,
        "ref": "main",
        "generatedAtUtc": datetime.now(timezone.utc).isoformat(),
        "types": list(TYPE_NAMES),
        "runtimePolicy": "Use bundled local SVG assets. No network access during battle.",
    }
    (output / "metadata.json").write_text(
        json.dumps(metadata, indent=2, ensure_ascii=False) + "\n",
        encoding="utf-8",
    )

    print(f"Synced {len(TYPE_NAMES)} type SVGs into {output}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
