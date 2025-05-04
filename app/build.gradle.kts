plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    kotlin("kapt")
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.21" // use your Kotlin version

}

android {
    namespace = "com.example.analyzer"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.analyzer"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation (libs.androidx.material.icons.extended)
    implementation(libs.coil.compose)
    implementation ("androidx.camera:camera-camera2:1.4.2")

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.camera.view)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.runtime.livedata)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    val room_version = "2.6.1"
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0") // or latest

    implementation (libs.androidx.room.runtime)
    implementation (libs.androidx.room.ktx)
    kapt (libs.androidx.room.compiler)
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.logging.interceptor)
    implementation(libs.okhttp)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.glide)
    annotationProcessor("com.github.bumptech.glide:compiler:4.14.2")

    implementation("com.patrykandpatryk.vico:compose:1.12.0")
    implementation("com.patrykandpatryk.vico:compose-m3:1.12.0")
    implementation("com.patrykandpatryk.vico:core:1.12.0")


}