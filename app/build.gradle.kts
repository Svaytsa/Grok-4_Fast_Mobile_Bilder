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
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
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

    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.4")
    implementation("androidx.activity:activity-compose:1.9.1")
    implementation(platform("androidx.compose:compose-bom:2024.06.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.palette:palette-ktx:1.0.0")

    // Image loading - using coil for easy image loading in compose, but task says use ImageDecoder/BitmapFactory for logic.
    // For UI display, Coil is standard, but the task says "Minimize diff, don't add extra dependencies".
    // I will use Coil because loading URIs in Compose properly is hard without it.
    // Wait, "Не добавлять сетевые разрешения и лишние зависимости." (Do not add network permissions and extra dependencies).
    // Coil might be considered "extra". I can load Bitmap via ImageDecoder and pass it to ImageBitmap.
    // I will stick to standard Android APIs + Compose as requested to avoid "extra dependencies".
    // Actually, I need to show the image preview. I can load the Uri into a Bitmap using ImageDecoder and then use ImageBitmap.

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.robolectric:robolectric:4.13")
    // Robolectric is good for "app:testDebug" when testing Android dependencies.
    // "Unit tests utilize Robolectric and are executed via ./gradlew :app:testDebug" - wait, that was in the memory?
    // Memory said: "Unit tests utilize Robolectric and are executed via ./gradlew :app:testDebug from the Tests/Gemini directory."
    // BUT memory also said: "The repository contains multiple independent projects... Tests/Gemini".
    // I am in the root of the repo (or assuming so). The task says "relative to the repository/project root".
    // I should check if I am in a subdir. `ls -a` showed `.git` and `README.md`. I am in root.
    // So I am creating the project in the root.

    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.06.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
