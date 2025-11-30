plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.example.palette"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.palette"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2024.06.00"))
    implementation("androidx.activity:activity-compose:1.9.1")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")

    // Palette API
    implementation("androidx.palette:palette-ktx:1.0.0")

    // Image Loading - using Coil usually, but constraints say "Минимизировать дифф, не трогать ничего вне перечисленных путей. Сеть — off (кроме CI шагов скачивания зависимостей)".
    // And "Не добавлять сетевые разрешения и лишние зависимости."
    // Also "Декодирование изображений: ImageDecoder (API 28+) с fallback на BitmapFactory для ниже."
    // So I should implement image loading manually or with minimal deps if possible.
    // Compose has `AsyncImage` in Coil, but if I want to strictly follow "no extra dependencies" and use ImageDecoder/BitmapFactory as requested, I should probably do it manually or stick to standard calls.
    // Wait, the constraint "Репозитории только google() и mavenCentral()" is fine for Coil.
    // However, the constraint "Не добавлять сетевые разрешения и лишние зависимости" and explicit instructions on how to decode images ("Декодирование изображений: ImageDecoder (API 28+) с fallback на BitmapFactory для ниже.") suggests I should handle bitmap loading myself and pass the Bitmap to Palette and to the UI (as ImageBitmap).

    // Tests
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.robolectric:robolectric:4.12.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.06.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
