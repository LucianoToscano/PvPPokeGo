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

## CI checkpoint
- Added `.github/workflows/hybrid-data-sync.yml` to run `:app:testStandardDebugUnitTest`, `:app:assembleStandardDebug`, ZIP integrity check, and artifact SHA256.
- A GitHub Actions run has **not** been confirmed; do not report a successful build or link an APK before a real run produces an artifact.
- The update feed is deliberately disabled until an operator publishes a trusted HTTPS manifest and sets `pvppokego.hybridManifestUrl` in the build. Untrusted feeds must never be enabled.
- Rollback currently covers an invalid Game Master; broader cross-file consistency and Android on-device regression checks are still necessary.

## Verified public build and feed hardening (2026-10-15)

- Public branch: LucianoToscano/PvPPokeGo-APK-Build / preview/hybrid-data-sync-20261015. Original main untouched.
- GitHub Actions run 38796466877 compiled and passed tests but failed at apksigner due obsolete file name. Filename fixed; run 38798974370 completed successfully.
- Verified artifact ID 12004061276: PvPPokeGo-0.5.29-Hybrid-QA-debug APK with ZIP integrity and APK Signature Scheme v2 verification; SHA-256 5e9594cd76dc0779cd1a50753b6e414ab6aad0f5eab4ff3152020472decd5547. Debug signature is not a permanent release key.
- Later additions not yet validated by completed Android build: explicit Wi-Fi network transport, WorkManager UPDATE, repository-pinned HTTPS feed, seven required data files, SHA-256 checksum ledger re-verification, cross-file validation and rollback tests.
- New tools/generate_hybrid_feed.py constructs deterministic versioned manifest, file sizes and SHA-256 for five core datasets plus metadata and license.
- Public workflow prepares to publish feed to isolated hybrid-feed branch ONLY after successful APK build and signature verification. Actual feed publication still needs proof.
- Expected manifest URL: https://raw.githubusercontent.com/LucianoToscano/PvPPokeGo-APK-Build/hybrid-feed/manifest.json
- Security limit: pinned GitHub HTTPS plus file checksums DOES NOT cryptographically sign the manifest. Production hardening would require owner-managed signing key and signature verification.
- Remaining: prove latest CI, verify public feed, test on Wi-Fi Android device and overlay/OCR continuity, and use signed release process for installable production.
