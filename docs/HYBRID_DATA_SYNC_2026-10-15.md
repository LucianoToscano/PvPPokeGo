# Hybrid data sync — implementation checkpoint (2026-10-15)

Branch: `feature/hybrid-data-sync-0.5.29` based on `release/0.5.29`.

## Existing architecture audited
- The current APK 0.5.29 checklist confirms packaged `gamemaster.json`, `pvpoke_snapshot.json` and icon atlas.
- `tools/package_offline_pvpoke_assets.py` already downloads PvPoke data, pins the upstream revision and calculates SHA-256 for the generated assets.
- Kotlin coroutines are installed; Room and WorkManager are **not** declared in `app/build.gradle.kts`.
- Preserve existing offline battle engine, OCR, UI/HUD and all packaged assets. No internet calls in battle hot path.

## Implemented in this checkpoint
- `HybridSnapshotStore`: isolated, checksum-verified staging; reject missing files and path traversal; keep snapshots immutable; explicit activation only between battles; active-version pointer for subsequent loading.
- JVM tests written for checksums, traversal and battle-safe activation.

## User-approved settings / next wiring
- Automatic sync enabled; Wi-Fi only; daily.
- Add WorkManager constrained to unmetered network, persistent preferences, manifest fetch, dataset download and validation, and UI state **after** verifying integration points.
- The current store is NOT yet wired to the Android data loader or scheduler. Never claim automatic updates work until integration tests and Android build succeed.
- Manifest must include dataset version, schema, file names, byte sizes and SHA-256; accept only trusted HTTPS endpoint, enforce size limits, validate semantic schema and minimum records before stage/activation.
- Freeze loaded dataset for each battle; switch only when a new battle starts, with previous snapshot available for rollback.
- Never send player history or personal data upstream without explicit consent.

## Validation / pending
- Static review performed; tests added but not executed; Android build not run; no APK produced.
- Next: inspect runtime loader and battle session lifecycle, implement Wi-Fi daily scheduling, run unit tests and Standard Android build, validate on device and document the SHA-256 of any produced APK.
