import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.services)
}

val uploadKeyFile = Properties().also { props ->
    rootProject.file("keystore.properties").takeIf { it.isFile }?.inputStream()?.use(props::load)
}

fun uploadKey(name: String): String? =
    System.getenv("UPLOAD_${name.uppercase()}")?.ifBlank { null }
        ?: uploadKeyFile.getProperty(name)?.ifBlank { null }

val uploadKeyEntries = listOf("store_file", "store_password", "key_alias", "key_password")
    .associateWith(::uploadKey)
val absentUploadKeyEntries = uploadKeyEntries.filterValues { it == null }.keys
if (absentUploadKeyEntries.isNotEmpty() && absentUploadKeyEntries.size < uploadKeyEntries.size) {
    throw GradleException("Upload key is only partly configured, missing: ${absentUploadKeyEntries.joinToString()}")
}

android {
    namespace = "com.bplay.bhraspincasino"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.bplay.bhraspincasino"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0.1"
    }

    signingConfigs {
        if (absentUploadKeyEntries.isEmpty()) {
            create("upload") {
                storeFile = rootProject.file(uploadKeyEntries.getValue("store_file")!!)
                storePassword = uploadKeyEntries.getValue("store_password")
                keyAlias = uploadKeyEntries.getValue("key_alias")
                keyPassword = uploadKeyEntries.getValue("key_password")
            }
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.findByName("upload") ?: signingConfigs.getByName("debug")
            optimization {
                enable = true
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.play.services)
    implementation(platform(libs.koin.bom))
    implementation(libs.koin.android)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.messaging)
    implementation(libs.play.services.ads.identifier)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}
