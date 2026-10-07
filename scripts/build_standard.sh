#!/usr/bin/env bash
set -euo pipefail
./gradlew --no-daemon :app:testStandardDebugUnitTest :app:assembleStandardDebug
APK=app/build/outputs/apk/standard/debug/app-standard-debug.apk
python3 scripts/verify_apk.py "$APK"
sha256sum "$APK"
