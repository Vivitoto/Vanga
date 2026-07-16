import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinSerialization)
}

group = "io.github.vivitoto.vanga.offline"
version = "unspecified"

kotlin {
    jvmToolchain(17)

    androidTarget { compilerOptions { jvmTarget.set(JvmTarget.JVM_17) } }
    jvm { compilerOptions { jvmTarget.set(JvmTarget.JVM_17) } }
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        outputModuleName = "vanga-webview"
        browser()
    }

    sourceSets {
        all {
            languageSettings.optIn("kotlin.ExperimentalStdlibApi")
            languageSettings.optIn("kotlin.ExperimentalUnsignedTypes")
            languageSettings.optIn("kotlin.time.ExperimentalTime")
            languageSettings.optIn("kotlin.uuid.ExperimentalUuidApi")
        }
        commonMain.dependencies {
            implementation(projects.vangaDomain.komgaApi)
            implementation(projects.vangaInfra.database.transaction)
            implementation(projects.vangaInfra.imageDecoder.shared)

            implementation(libs.filekit.core)
            implementation(libs.coil)
            implementation(libs.kotlin.logging)
            implementation(libs.kotlinx.datetime)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.io.core)
            implementation(libs.kotlinx.serialization.core)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.komga.client)
            implementation(libs.ktor.client.core)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }

        androidMain.dependencies {
            implementation(libs.commons.compress)
            implementation(libs.androidx.documentfile)
            implementation(libs.androidx.workManager)
            implementation(libs.androidx.workManager.ktx)
        }

        jvmMain.dependencies {
            implementation(libs.commons.compress)
            implementation(libs.ktor.client.okhttp)
            implementation(projects.vangaInfra.jni)
        }
    }
}
android {
    namespace = "io.github.vivitoto.vanga.offline"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
