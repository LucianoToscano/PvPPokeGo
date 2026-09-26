#!/usr/bin/env python3
from pathlib import Path
from urllib.request import Request, urlopen
OUT = Path(__file__).resolve().parents[1] / "app" / "src" / "main" / "assets"
OUT.mkdir(parents=True, exist_ok=True)
for cp in (1500, 2500, 10000):
    url = f"https://pvpoke.com/data/rankings/all/overall/rankings-{cp}.json"
    req = Request(url, headers={"User-Agent": "PVPPokeGo-data-updater/1.0"})
    data = urlopen(req, timeout=30).read()
    path = OUT / f"rankings-{cp}.json"
    path.write_bytes(data)
    print(f"{path.name}: {len(data):,} bytes")
