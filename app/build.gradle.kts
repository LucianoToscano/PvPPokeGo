plugins {
    id("com.android.application")
    kotlin("android")
}

android {
    namespace = "com.lucianotoscano.pvppokego"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.lucianotoscano.pvppokego"
        minSdk = 28
        targetSdk = 35
        versionCode = 3
        versionName = "0.3.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    sourceSets.getByName("main").assets.srcDir(rootProject.file("data"))
}

dependencies {
    implementation(project(":core"))
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("com.google.mlkit:text-recognition:16.0.1")
    implementation("com.caverock:androidsvg-aar:1.4")
}
