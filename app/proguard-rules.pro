# PvPPokeGo - perfil isolado de distribuicao (R8).
# Nao desativar warnings globalmente nem manter indiscriminadamente todo o codigo.
# O Android Gradle Plugin protege automaticamente componentes declarados no Manifest.
# As tres entradas do framework ficam explicitas para reduzir risco de regressao.
-keep class com.lucianotoscano.pvppokego.PvPPokeGoApp { *; }
-keep class com.lucianotoscano.pvppokego.MainActivity { *; }
-keep class com.lucianotoscano.pvppokego.overlay.BattleOverlayService { *; }

# Metadados necessarios a bibliotecas com serializacao e anotacoes.
-keepattributes Signature,InnerClasses,EnclosingMethod,*Annotation*
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}
