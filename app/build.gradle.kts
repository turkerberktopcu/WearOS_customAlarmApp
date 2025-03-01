plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("kotlin-parcelize")

}

android {
    namespace = "com.turkerberktopcu.customalarmapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.turkerberktopcu.customalarmapp"
        minSdk = 30
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
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
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    implementation(libs.play.services.wearable)
    implementation(platform(libs.compose.bom))
    implementation(libs.ui)
    implementation(libs.ui.tooling.preview)
    implementation(libs.compose.material)
    implementation(libs.compose.foundation)
    implementation(libs.activity.compose)
    implementation(libs.core.splashscreen)
    implementation(libs.material3.android)
    implementation(libs.navigation.compose)
    implementation(libs.navigation.runtime.android)
    implementation("com.google.android.material:material:1.11.0")
    implementation(libs.room.common)
    implementation ("com.google.code.gson:gson:2.10.1") // ← Bu satırı ekleyin
    implementation ("androidx.compose.ui:ui:1.6.2")
    implementation ("androidx.activity:activity-compose:1.8.2")
    implementation ("androidx.wear.compose:compose-material:1.3.0")
    implementation ("androidx.wear.compose:compose-foundation:1.3.0")
    implementation ("androidx.compose.material:material-icons-core:1.6.2")

    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.ui.test.junit4)
    debugImplementation(libs.ui.tooling)
    debugImplementation(libs.ui.test.manifest)
}