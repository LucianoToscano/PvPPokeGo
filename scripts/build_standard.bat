@echo off
setlocal
call gradlew.bat --no-daemon :app:testStandardDebugUnitTest :app:assembleStandardDebug
if errorlevel 1 exit /b %errorlevel%
python scripts\verify_apk.py app\build\outputs\apk\standard\debug\app-standard-debug.apk
if errorlevel 1 exit /b %errorlevel%
certutil -hashfile app\build\outputs\apk\standard\debug\app-standard-debug.apk SHA256
