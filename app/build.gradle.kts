import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    id("com.google.gms.google-services")
    alias(libs.plugins.google.firebase.crashlytics)
}

android {
    namespace = "com.kbyai.idcardrecognition"
    compileSdk = 36

    // ✅ (اختياري) لو محتاج Java 17 زي README
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    splits {
        abi {
            isEnable = true
            reset()
            include("armeabi-v7a", "arm64-v8a")
            isUniversalApk = false
        }
    }

    defaultConfig {
        applicationId = "com.kbyai.idcardrecognition"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            buildConfigField("boolean", "DEMO_MODE", "false")

            buildConfigField("boolean", "SIMULATE_OTP", "false")

            isMinifyEnabled = false
            isShrinkResources = false
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true

            buildConfigField("boolean", "DEMO_MODE", "false")
            buildConfigField("boolean", "SIMULATE_OTP", "false")

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

    buildFeatures {
        compose = true
        buildConfig = true
    }

}


kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_11)
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material3.adaptive.navigation.suite)
    implementation(libs.firebase.auth)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)


    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation("androidx.navigation:navigation-compose:2.8.5")

    implementation("com.stripe:stripe-android:22.7.0")

    implementation("io.scanbot:sdk-package-3:7.0.0")
    implementation("io.scanbot:sdk-creditcard-assets:7.0.0")
    implementation("io.scanbot:rtu-ui-v2-bundle:7.0.0")


    implementation ("androidx.datastore:datastore-preferences:1.2.0")


    // Accura OCR
    implementation("com.github.accurascan:AccuraOCR:6.2.5")
    // Accura Face match
    implementation("com.github.accurascan:AccuraFaceMatch:4.0.0")
    // Accura Liveness
    implementation("com.github.accurascan:Liveness-Android:3.7.0")



    implementation("androidx.biometric:biometric:1.2.0-alpha05")


    implementation(project(":libidsdk"))
    implementation(project(":libfotoapparat"))
    implementation("com.itextpdf:itextpdf:5.5.13.5")


    implementation("androidx.compose.material:material-icons-extended")

    implementation(libs.material)

}