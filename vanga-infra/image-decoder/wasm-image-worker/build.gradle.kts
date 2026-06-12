import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    kotlin("multiplatform")
}

group = "io.github.vivitoto.vanga.infra.image_decoder"
version = "unspecified"

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain(17)

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        outputModuleName = "vangaImageWorker"
        browser {
            commonWebpackConfig {
                outputFileName = "vangaImageWorker.js"
            }
        }
        binaries.executable()
    }

    sourceSets {
        wasmJsMain.dependencies {
            implementation(libs.kotlin.logging)
            implementation(libs.kotlinx.browser)
            implementation(libs.kotlinx.coroutines.core)
            api(projects.vangaInfra.imageDecoder.shared)
//            implementation(npm("wasm-vips", "0.0.11"))
        }
    }
}