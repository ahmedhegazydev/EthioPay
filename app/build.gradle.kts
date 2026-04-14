import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

fun Project.readSigningValue(
    properties: Properties,
    propertyName: String,
    envName: String,
): String? = properties.getProperty(propertyName)
    ?: findProperty(propertyName)?.toString()
    ?: System.getenv(envName)

val signingProperties = Properties()
val signingPropertiesFile = rootProject.file("keystore.properties")
if (signingPropertiesFile.exists()) {
    signingPropertiesFile.inputStream().use(signingProperties::load)
}

val defaultReleaseKeystorePath = "${System.getProperty("user.home")}/Desktop/EtioPay.jks"
val releaseStoreFilePath = project.readSigningValue(
    properties = signingProperties,
    propertyName = "etiopay.storeFile",
    envName = "ETIOPAY_STORE_FILE",
) ?: defaultReleaseKeystorePath
val releaseStorePassword = project.readSigningValue(
    properties = signingProperties,
    propertyName = "etiopay.storePassword",
    envName = "ETIOPAY_STORE_PASSWORD",
)
val releaseKeyAlias = project.readSigningValue(
    properties = signingProperties,
    propertyName = "etiopay.keyAlias",
    envName = "ETIOPAY_KEY_ALIAS",
) ?: "key0"
val releaseKeyPassword = project.readSigningValue(
    properties = signingProperties,
    propertyName = "etiopay.keyPassword",
    envName = "ETIOPAY_KEY_PASSWORD",
) ?: releaseStorePassword
val hasReleaseSigning =
    releaseStorePassword != null &&
        releaseKeyPassword != null &&
        file(releaseStoreFilePath).exists()

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("org.jetbrains.kotlin.kapt")
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

    signingConfigs {
        if (hasReleaseSigning) {
            create("release") {
                storeFile = file(releaseStoreFilePath)
                storePassword = releaseStorePassword
                keyAlias = releaseKeyAlias
                keyPassword = releaseKeyPassword
            }
        }
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

            if (hasReleaseSigning) {
                signingConfig = signingConfigs.getByName("release")
            }
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
    implementation(platform(libs.firebase.bom))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.10.0")
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material3.adaptive.navigation.suite)
    implementation(libs.firebase.auth)
    implementation("com.google.firebase:firebase-firestore")
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

    implementation("io.insert-koin:koin-android:3.5.6")
    implementation("io.insert-koin:koin-androidx-compose:3.5.6")

    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    implementation("androidx.room:room-runtime:2.8.4")
    implementation("androidx.room:room-ktx:2.8.4")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.8.1")
    kapt("androidx.room:room-compiler:2.8.4")

    testImplementation(libs.junit)
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")

    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}

kapt {
    correctErrorTypes = true
}
