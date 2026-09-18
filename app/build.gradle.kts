plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "com.petermathie.vibetrainer"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.petermathie.vibetrainer"
        minSdk = 23
        targetSdk = 36
        versionCode = 1
        versionName = "0.2.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.17.0")
    implementation("androidx.activity:activity-compose:1.11.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.9.4")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.9.4")

    implementation("androidx.compose.ui:ui:1.9.2")
    implementation("androidx.compose.ui:ui-tooling-preview:1.9.2")
    implementation("androidx.compose.foundation:foundation:1.9.2")
    implementation("androidx.compose.material3:material3:1.4.0")
    implementation("androidx.compose.material:material-icons-extended:1.9.2")
    debugImplementation("androidx.compose.ui:ui-tooling:1.9.2")

    implementation("androidx.room:room-runtime:2.8.1")
    implementation("androidx.room:room-ktx:2.8.1")
    ksp("androidx.room:room-compiler:2.8.1")

    implementation("androidx.datastore:datastore-preferences:1.1.7")

    implementation("com.google.dagger:hilt-android:2.60.1")
    ksp("com.google.dagger:hilt-compiler:2.60.1")
}
