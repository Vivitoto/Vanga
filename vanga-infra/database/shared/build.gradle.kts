import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinSerialization)
}

group = "io.github.vivitoto.vanga.db.shared"
version = "unspecified"

kotlin {
    jvmToolchain(17)
    jvm {}
    androidTarget {}

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        outputModuleName = "vanga-infra-database-shared"
        browser()
    }

    sourceSets {
        all {
            languageSettings.optIn("kotlin.time.ExperimentalTime")
        }
        commonMain.dependencies {
            implementation(projects.vangaDomain.core)
            implementation(projects.vangaDomain.offline)
            implementation(libs.filekit.core)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.datetime)
            implementation(libs.kotlinx.io.core)
            implementation(libs.kotlinx.serialization.core)
            implementation(libs.ktor.client.core)
            implementation(libs.komga.client)
            implementation(projects.vangaInfra.imageDecoder.shared)
        }
    }
}

android {
    namespace = "io.github.vivitoto.vanga.infra.database.shared"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

