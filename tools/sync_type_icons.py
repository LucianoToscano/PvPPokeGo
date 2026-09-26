#!/usr/bin/env python3
"""Vendor the MIT-licensed Pokemon type SVG set used by the overlay.

Source: https://github.com/partywhale/pokemon-type-icons

The script runs at development/release time only. The Android overlay uses
bundled local assets during battle and never fetches icons at runtime.
"""

from __future__ import annotations

import argparse
import json
import re
import urllib.request
from datetime import datetime, timezone
from pathlib import Path

SOURCE_REPO = "https://github.com/partywhale/pokemon-type-icons"
RAW_BASE = "https://raw.githubusercontent.com/partywhale/pokemon-type-icons/main"
TYPE_NAMES = (
    "bug", "dark", "dragon", "electric", "fairy", "fighting", "fire",
    "flying", "ghost", "grass", "ground", "ice", "normal", "poison",
    "psychic", "rock", "steel", "water",
)


def fetch_text(url: str) -> str:
    request = urllib.request.Request(url, headers={"User-Agent": "PvPPokeGo-type-icon-sync/2.0"})
    with urllib.request.urlopen(request, timeout=30) as response:
        return response.read().decode("utf-8")


def symbol_only(svg: str) -> str:
    # Upstream icons are a colored circle followed by a white type glyph.
    # Remove the first circle so our Android progress view can draw the charge
    # underneath while preserving the white glyph exactly.
    return re.sub(r"\s*<circle\b[^>]*/>", "", svg, count=1, flags=re.IGNORECASE)


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument(
        "--assets-root",
        default="app/src/main/assets",
        help="Android assets directory where type_icons/ and type_symbols/ are generated.",
    )
    args = parser.parse_args()

    assets_root = Path(args.assets_root)
    icons = assets_root / "type_icons"
    symbols = assets_root / "type_symbols"
    icons.mkdir(parents=True, exist_ok=True)
    symbols.mkdir(parents=True, exist_ok=True)

    for type_name in TYPE_NAMES:
        url = f"{RAW_BASE}/icons/{type_name}.svg"
        svg = fetch_text(url)
        if "<svg" not in svg:
            raise RuntimeError(f"Unexpected response for {url}")
        (icons / f"{type_name}.svg").write_text(svg, encoding="utf-8")
        (symbols / f"{type_name}.svg").write_text(symbol_only(svg), encoding="utf-8")

    license_text = fetch_text(f"{RAW_BASE}/LICENSE")
    (icons / "UPSTREAM_LICENSE.txt").write_text(license_text, encoding="utf-8")

    metadata = {
        "source": SOURCE_REPO,
        "ref": "main",
        "generatedAtUtc": datetime.now(timezone.utc).isoformat(),
        "types": list(TYPE_NAMES),
        "runtimePolicy": "Bundled local SVG assets only; no network access during battle.",
        "derivedAssets": "type_symbols removes only the colored background circle for charge-progress rendering",
    }
    (icons / "metadata.json").write_text(
        json.dumps(metadata, indent=2, ensure_ascii=False) + "\n",
        encoding="utf-8",
    )

    print(f"Synced {len(TYPE_NAMES)} full icons and symbol-only SVGs into {assets_root}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
