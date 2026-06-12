@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlinMultiplatform)
}

group = "io.github.vivitoto.vanga.db.wasm"
version = "unspecified"

kotlin {
    jvmToolchain(17)

    wasmJs {
        outputModuleName = "vanga-infra-databaseb"
        browser()
    }

    sourceSets {
        all {
            languageSettings.optIn("kotlin.time.ExperimentalTime")
        }
        wasmJsMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.browser)
//            implementation(projects.vangaCore)
            implementation(projects.vangaInfra.database.shared)
            implementation(projects.vangaInfra.imageDecoder.shared)
            implementation(projects.thirdParty.indexeddb.core)
        }
    }
}