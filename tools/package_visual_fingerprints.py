#!/usr/bin/env python3
"""Generate compact visual fingerprints for Pokémon portraits.

The APK does NOT embed the source artwork. It stores only small perceptual
fingerprints (dHash + hue histogram) keyed by National Dex number.

Input:
  app/src/main/assets/gamemaster.json (already generated from PvPoke)
Source artwork:
  PokeAPI HOME renders on GitHub

Requires Pillow.
"""
from __future__ import annotations

import io
import json
import math
import urllib.request
from concurrent.futures import ThreadPoolExecutor, as_completed
from pathlib import Path

from PIL import Image, ImageOps

ASSETS = Path("app/src/main/assets")
GM = ASSETS / "gamemaster.json"
OUT = ASSETS / "pokemon_visual_fingerprints.json"
URL = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/home/{dex}.png"
WORKERS = 20


def fetch_bytes(url: str) -> bytes:
    req = urllib.request.Request(url, headers={"User-Agent": "PvPPokeGo-visual-bank/1.0"})
    with urllib.request.urlopen(req, timeout=25) as response:
        return response.read()


def normalize_image(raw: bytes) -> Image.Image:
    img = Image.open(io.BytesIO(raw)).convert("RGBA")
    alpha = img.getchannel("A")
    bbox = alpha.point(lambda a: 255 if a > 12 else 0).getbbox()
    if bbox:
        img = img.crop(bbox)

    side = max(img.width, img.height, 1)
    pad = max(2, int(side * 0.08))
    canvas = Image.new("RGBA", (side + pad * 2, side + pad * 2), (255, 255, 255, 255))
    fitted = ImageOps.contain(img, (side, side), Image.Resampling.LANCZOS)
    x = (canvas.width - fitted.width) // 2
    y = (canvas.height - fitted.height) // 2
    canvas.alpha_composite(fitted, (x, y))
    return canvas.convert("RGB").resize((72, 72), Image.Resampling.LANCZOS)


def dhash64(img: Image.Image) -> str:
    gray = img.convert("L").resize((9, 8), Image.Resampling.LANCZOS)
    px = list(gray.getdata())
    value = 0
    bit = 0
    for y in range(8):
        row = y * 9
        for x in range(8):
            if px[row + x] > px[row + x + 1]:
                value |= 1 << bit
            bit += 1
    return f"{value:016x}"


def hue_histogram(img: Image.Image, bins: int = 12) -> list[int]:
    hsv = img.convert("HSV")
    counts = [0.0] * bins
    total = 0.0
    for h, s, v in hsv.getdata():
        # Ignore near-white/gray background; emphasize actual Pokémon colors.
        sat = s / 255.0
        val = v / 255.0
        if sat < 0.12 and val > 0.72:
            continue
        weight = max(0.08, sat) * (0.35 + 0.65 * (1.0 - abs(val - 0.55)))
        idx = min(bins - 1, int((h / 256.0) * bins))
        counts[idx] += weight
        total += weight
    if total <= 1e-9:
        return [0] * bins
    scaled = [int(round((c / total) * 255.0)) for c in counts]
    # Keep total close to 255 so runtime L1 distance is well behaved.
    diff = 255 - sum(scaled)
    if scaled:
        scaled[max(range(len(scaled)), key=lambda i: scaled[i])] += diff
    return scaled


def build_one(dex: int):
    try:
        raw = fetch_bytes(URL.format(dex=dex))
        img = normalize_image(raw)
        return {
            "dex": dex,
            "dhash": dhash64(img),
            "hue": hue_histogram(img),
        }
    except Exception as exc:
        return {"dex": dex, "error": str(exc)}


def main() -> int:
    if not GM.exists():
        raise SystemExit(f"Missing {GM}; run package_offline_pvpoke_assets.py first")

    gm = json.loads(GM.read_text(encoding="utf-8"))
    pokemon = gm.get("pokemon", [])
    dexes = sorted({
        int(p.get("dex", 0) or 0)
        for p in pokemon
        if int(p.get("dex", 0) or 0) > 0
    })
    if len(dexes) < 500:
        raise SystemExit(f"Gamemaster exposed only {len(dexes)} dex numbers; refusing incomplete visual bank")

    entries = []
    failures = []
    with ThreadPoolExecutor(max_workers=WORKERS) as pool:
        futures = {pool.submit(build_one, dex): dex for dex in dexes}
        for future in as_completed(futures):
            item = future.result()
            if "error" in item:
                failures.append(item)
            else:
                entries.append(item)

    entries.sort(key=lambda x: x["dex"])
    if len(entries) < 500:
        raise SystemExit(f"Only {len(entries)} visual fingerprints generated ({len(failures)} failures)")

    payload = {
        "format": "PvPPokeGo-visual-fingerprints",
        "version": 1,
        "source": "PokeAPI HOME renders",
        "sourceUrlTemplate": URL,
        "entries": entries,
        "missingDex": [f["dex"] for f in failures],
    }
    OUT.write_text(json.dumps(payload, ensure_ascii=False, separators=(",", ":")), encoding="utf-8")
    print(f"Visual fingerprint bank: {len(entries)} dex entries -> {OUT}")
    if failures:
        print(f"Missing sprites: {len(failures)}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
