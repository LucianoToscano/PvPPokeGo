# Inventário do backup editável — PvPPokeGo 0.5.25

## Gradle / projeto
- `settings.gradle.kts`
- `build.gradle.kts`
- `app/build.gradle.kts`
- `gradle.properties`
- `gradle/wrapper/*`
- `gradlew`, `gradlew.bat`
- `local.properties.example`
- `keystore.properties.example`

## Android
- `app/src/main/AndroidManifest.xml` — APK padrão sem AccessibilityService/specialUse.
- `app/src/recorderCompat/AndroidManifest.xml` — variante separada, preservada para teste futuro.
- `app/proguard-rules.pro`

## Código Kotlin
- `MainActivity.kt`, `PvPPokeGoApp.kt`
- `capture/`, `data/`, `detect/`, `engine/`, `overlay/`, `ui/`

## Recursos
- `app/src/main/res/`
- `app/src/main/assets/`
- `tools/package_offline_pvpoke_assets.py`
- `tools/package_visual_fingerprints.py`

## Assinatura
A release aceita chave permanente local/CI sem armazenar segredos no Git. O material privado nunca deve ser commitado.
