plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.parcelize")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.example.myapplication"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.myapplication"
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        viewBinding = true
        dataBinding = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    // Core KTX (not in libs.versions.toml)
    implementation("androidx.core:core-ktx:1.13.1")
    // Lifecycle KTX (not in libs.versions.toml)
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.0")
    // Activity Compose (not in libs.versions.toml, but related to compose)
    implementation("androidx.activity:activity-compose:1.9.0")

    // Use versions from libs.versions.toml
    implementation(libs.appcompat)
    implementation(libs.constraintlayout)
    implementation(libs.material)

    // Navigation KTX (not in libs.versions.toml)
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.7")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.7")

    // Use versions from libs.versions.toml
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // Using BOM for Compose to manage versions centrally
    val composeBom = platform("androidx.compose:compose-bom:2024.04.00")
    implementation(composeBom) // Make BOM available for all implementations
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    // Room components (not in libs.versions.toml)
    implementation("androidx.room:room-runtime:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")

    // Lifecycle components (ViewModel and LiveData) (not in libs.versions.toml)
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.0")

    // CameraX (not in libs.versions.toml)
    implementation("androidx.camera:camera-camera2:1.3.3")
    implementation("androidx.camera:camera-lifecycle:1.3.3")
    implementation("androidx.camera:camera-view:1.3.3")

    // ZXing Android Embedded (for barcode reader)
    // Changed version from 4.3.2 to 4.3.0 as 4.3.2 was not found in public repositories.
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")

    // Gson (not in libs.versions.toml)
    implementation("com.google.code.gson:gson:2.10.1")

    // Retrofit (using libs.versions.toml)
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    // OkHttp (using libs.versions.toml)
    implementation(libs.logging.interceptor)
}