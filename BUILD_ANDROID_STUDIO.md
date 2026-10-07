# PvPPokeGo 0.5.25 — abrir e compilar no Android Studio

- applicationId: `com.lucianotoscano.pvppokego`
- versionName: `0.5.25`
- versionCode: `30`
- minSdk: 29
- compile/targetSdk: 36
- JDK: 17

## Build padrão — foco atual

A gravação de tela/Recorder Compat não é critério de aceite desta etapa. O código existente não foi removido.

```bash
./gradlew :app:testStandardDebugUnitTest
./gradlew :app:assembleStandardDebug
```

APK esperado:
`app/build/outputs/apk/standard/debug/app-standard-debug.apk`

## Recorder Compat

O flavor `recorderCompat` permanece no projeto para preservar a funcionalidade existente, mas fica para validação posterior.

## Assinatura permanente

A release aceita `keystore.properties` local ou as variáveis `PVPPG_*`. Nenhuma chave privada ou senha deve ser commitada.
