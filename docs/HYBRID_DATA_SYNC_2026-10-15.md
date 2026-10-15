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

## Second checkpoint — Android scheduling
- Added `androidx.work:work-runtime-ktx:2.10.1`.
- Added `HybridSyncScheduler`: unique periodic WorkManager task every 24h, unmetered network by default, enabled by default, with a configuration method for future settings UI.
- Registered the scheduler in `PvPPokeGoApp.onCreate()`.
- Worker is deliberately no-op until trusted manifest endpoint, schema validation and battle-session integration are implemented. It records only its last scheduler invocation.
- This is scheduling infrastructure, NOT live data synchronization. Wi-Fi-only means Android `NetworkType.UNMETERED` (not a literal Wi-Fi transport guarantee).
- WorkManager periodic tasks are inexact; daily means periodic eligibility, not execution at an exact clock time.
- No Gradle build or test execution performed. No APK generated.

## Third checkpoint — downloader and loader (unverified)
- WorkManager worker reads optional `pvppokego.hybridManifestUrl` Gradle property. When unset it is a safe no-op. The project has no verified public update manifest yet.
- Expected manifest: HTTPS JSON with `schema: "pvppokego-hybrid-v1"`, `version`, `files: [{name,sha256,size}]`. Five required datasets: `gamemaster.json`, `pvpoke_cpms.json`, `rankings-1500.json`, `rankings-2500.json`, `rankings-10000.json`.
- HTTPS, same-host file fetch, no redirects, timeouts, maximum file size, SHA-256 and minimum JSON record counts enforced.
- Downloads staged only. Activation happens at the next Android application process start, before the battle engine exists. No active dataset changes during a running process.
- `GameDataRepository` reads activated local snapshots with bundled assets fallback; existing online fallback remains only for absent assets.
- Automatic daily work is inexact and unmetered-network constrained; no exact-hour guarantee.
- **Not tested**: Gradle compilation, Android instrumentation, on-device regressions, remote endpoint and APK.
- **Security follow-up**: pin manifest integrity/signature and test update rollback before production deployment.
