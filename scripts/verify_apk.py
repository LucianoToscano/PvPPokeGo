#!/usr/bin/env python3
from pathlib import Path
import sys, zipfile

if len(sys.argv) != 2:
    raise SystemExit("usage: verify_apk.py path/to.apk")
p = Path(sys.argv[1])
if not p.is_file():
    raise SystemExit(f"APK não encontrado: {p}")
with zipfile.ZipFile(p) as z:
    bad = z.testzip()
    if bad:
        raise SystemExit(f"APK corrompido: {bad}")
    names = set(z.namelist())
    required = {
        "AndroidManifest.xml",
        "assets/gamemaster.json",
        "assets/pvpoke_snapshot.json",
        "assets/pokemon_visual_fingerprints.json",
        "assets/move_names_ptbr.json",
    }
    missing = sorted(required - names)
    if missing:
        raise SystemExit("Assets obrigatórios ausentes: " + ", ".join(missing))
print(f"OK: {p} — ZIP íntegro, {len(names)} entradas e assets críticos presentes")
