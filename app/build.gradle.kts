plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("ru.practicum.android.diploma.plugins.developproperties")
    id("kotlin-kapt")
    id("androidx.navigation.safeargs.kotlin")
    id("kotlin-parcelize")
}

android {
    namespace = "ru.practicum.android.diploma"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "ru.practicum.android.diploma"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField(type = "String", name = "API_ACCESS_TOKEN", value = "\"${developProperties.apiAccessToken}\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
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
        buildConfig = true
        viewBinding = true
    }
}

dependencies {
    // AndroidX Core
    implementation(libs.androidX.core)
    implementation(libs.androidX.appCompat)

    // UI layer libraries
    implementation(libs.ui.material)
    implementation(libs.ui.constraintLayout)

    // Lifecycle
    implementation(libs.lifecycle.viewmodelKtx)
    implementation(libs.lifecycle.runtimeKtx)
    implementation(libs.lifecycle.livedataKtx)
    implementation(libs.lifecycle.viewmodelSavedstate)

    // UI Components
    implementation(libs.ui.activityKtx)
    implementation(libs.ui.fragmentKtx)
    implementation(libs.ui.viewpager2)

    // Navigation Component
    implementation(libs.navigation.fragmentKtx)
    implementation(libs.navigation.uiKtx)

    // Network
    implementation(libs.network.retrofit)
    implementation(libs.network.converterGson)
    implementation(libs.network.okhttp)
    implementation(libs.network.loggingInterceptor)
    implementation(libs.network.gson)

    // Room
    implementation(libs.database.roomRuntime)
    implementation(libs.database.roomKtx)
    kapt(libs.database.roomCompiler)

    // Glide
    implementation(libs.imageLoading.glide)
    kapt(libs.imageLoading.glideCompiler)

    // Koin
    implementation(libs.di.koinCore)
    implementation(libs.di.koinAndroid)

    // Coil
    implementation(libs.imageLoading.coil)

    // Unit tests
    testImplementation(libs.unitTests.junit)

    // UI tests
    androidTestImplementation(libs.uiTests.junitExt)
    androidTestImplementation(libs.uiTests.espressoCore)
}
