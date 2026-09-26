#!/usr/bin/env python3
"""Generate a complete pt-BR Pokemon GO move alias table for PvPPokeGo.

The battle core uses PvPoke move IDs. Pokemon GO's charged-move banner is
localized (for example, "Plusle usou Relâmpago!"). This script pairs official
English and Brazilian Portuguese `move_name_XXXX` resources by resource ID,
then resolves the English text to the corresponding PvPoke move ID.

Sources are fetched only at development/release time. The generated TSV is
bundled into the APK; no network request is made during a battle.
"""

from __future__ import annotations

import argparse
import csv
import io
import re
import urllib.request
from pathlib import Path

PVPOKE_MOVES = (
    "https://raw.githubusercontent.com/pvpoke/pvpoke/master/"
    "src/data/gamemaster/moves.json"
)
POGO_TEXT_BASE = (
    "https://raw.githubusercontent.com/PokeMiners/pogo_assets/master/Texts"
)
TEXT_SOURCES = (
    ("Latest APK", "English.txt", "BrazilianPortuguese.txt"),
    ("Latest Remote", "English.txt", "BrazilianPortuguese.txt"),
)

RESOURCE_RE = re.compile(
    r"RESOURCE ID:\s*(move_name_\d+)\s*\n+TEXT:\s*(.+?)(?=\n+RESOURCE ID:|\Z)",
    re.IGNORECASE | re.DOTALL,
)


def fetch_text(url: str) -> str:
    request = urllib.request.Request(
        url,
        headers={"User-Agent": "PvPPokeGo-localization-updater/1.0"},
    )
    with urllib.request.urlopen(request, timeout=60) as response:
        return response.read().decode("utf-8", errors="replace")


def normalize_name(value: str) -> str:
    return re.sub(r"[^A-Z0-9]+", "_", value.upper()).strip("_")


def parse_resources(text: str) -> dict[str, str]:
    result: dict[str, str] = {}
    for resource_id, value in RESOURCE_RE.findall(text.replace("\r\n", "\n")):
        cleaned = " ".join(value.split())
        if cleaned:
            result[resource_id.lower()] = cleaned
    return result


def load_pvpoke_moves() -> dict[str, str]:
    import json

    raw = json.loads(fetch_text(PVPOKE_MOVES))
    by_english: dict[str, str] = {}
    for move in raw:
        move_id = str(move.get("moveId") or "").strip().upper()
        name = str(move.get("name") or "").strip()
        if not move_id or not name:
            continue
        by_english[normalize_name(name)] = move_id
        by_english[normalize_name(move_id.replace("_", " "))] = move_id
    return by_english


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument(
        "--output",
        default="app/src/main/assets/localization/moves_pt_BR.tsv",
    )
    args = parser.parse_args()

    english: dict[str, str] = {}
    portuguese: dict[str, str] = {}
    for folder, en_name, pt_name in TEXT_SOURCES:
        quoted = folder.replace(" ", "%20")
        english.update(parse_resources(fetch_text(f"{POGO_TEXT_BASE}/{quoted}/{en_name}")))
        portuguese.update(parse_resources(fetch_text(f"{POGO_TEXT_BASE}/{quoted}/{pt_name}")))

    pvpoke = load_pvpoke_moves()
    aliases: dict[tuple[str, str], str] = {}

    for resource_id, pt_name in portuguese.items():
        en_name = english.get(resource_id)
        if not en_name:
            continue
        move_id = pvpoke.get(normalize_name(en_name))
        if not move_id:
            continue
        aliases[(pt_name.casefold(), move_id)] = pt_name

    # Keep a small fallback set for names known from observed screenshots even
    # if upstream text resources temporarily lag behind a game update.
    fallback = {
        "Relâmpago": "THUNDERBOLT",
        "Descarga": "DISCHARGE",
        "Jogo Sujo": "FOUL_PLAY",
        "Nó de Grama": "GRASS_KNOT",
        "Trovão": "THUNDER",
        "Troca Elétrica": "VOLT_SWITCH",
        "Ataque Rápido": "QUICK_ATTACK",
    }
    for alias, move_id in fallback.items():
        aliases[(alias.casefold(), move_id)] = alias

    output = Path(args.output)
    output.parent.mkdir(parents=True, exist_ok=True)
    with output.open("w", encoding="utf-8", newline="") as handle:
        writer = csv.writer(handle, delimiter="\t", lineterminator="\n")
        writer.writerow(["locale", "alias", "move_id"])
        for (_, move_id), alias in sorted(
            aliases.items(), key=lambda item: (item[0][1], item[1].casefold())
        ):
            writer.writerow(["pt-BR", alias, move_id])

    print(f"Generated {len(aliases)} pt-BR move aliases at {output}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
