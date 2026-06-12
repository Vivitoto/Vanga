@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.compose.compiler)
}

group = "io.github.vivitoto.vanga.db.sqlite"
version = "unspecified"

kotlin {
    jvmToolchain(17)

    jvm {
        compilerOptions { jvmTarget.set(JvmTarget.JVM_17) }
    }
    androidTarget {
        compilerOptions { jvmTarget.set(JvmTarget.JVM_1_8) }
    }

    sourceSets {
        all {
            languageSettings.optIn("kotlin.time.ExperimentalTime")
            languageSettings.optIn("kotlin.uuid.ExperimentalUuidApi")
        }
        commonMain.dependencies {
            implementation(projects.vangaDomain.core)
            implementation(projects.vangaDomain.offline)
            implementation(projects.vangaDomain.komgaApi)
            implementation(projects.vangaInfra.database.shared)
            implementation(projects.vangaInfra.imageDecoder.shared)

            implementation(libs.compose.runtime)
            implementation(libs.compose.resources)
            implementation(libs.filekit.core)
            implementation(libs.kotlinx.datetime)
            implementation(libs.exposed.core)
            implementation(libs.exposed.jdbc)
            implementation(libs.exposed.json)
            implementation(libs.exposed.kotlin.datetime)
            implementation(libs.hikariCP)
            implementation(libs.flyway.core)
            implementation(libs.sqlite.xerial.jdbc)
        }
    }
}

android {
    namespace = "io.github.vivitoto.vanga.infra.database.sqlite"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    sourceSets["main"].jniLibs.srcDir(layout.buildDirectory.dir("generated/sqliteJniLibs"))

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

val sqliteExtract: Configuration by configurations.creating {
    isTransitive = false
}
dependencies { sqliteExtract(libs.sqlite.xerial.jdbc) }

fun TaskProvider<Sync>.extractSqliteNativeLib(jarPath: String, abi: String) = configure {
    from({
        val sqliteJar = sqliteExtract.singleFile
        zipTree(sqliteJar.absolutePath)
            .matching { include(jarPath) }
            .singleFile
    })
    into(layout.buildDirectory.dir("generated/sqliteJniLibs/$abi"))
}

val extractSqliteAndroidLibs = listOf(
    tasks.register<Sync>("android-arm64-ExtractSqliteLib")
        .also { it.extractSqliteNativeLib("org/sqlite/native/Linux-Android/aarch64/libsqlitejdbc.so", "arm64-v8a") },
    tasks.register<Sync>("android-armv7a-ExtractSqliteLib")
        .also { it.extractSqliteNativeLib("org/sqlite/native/Linux-Android/arm/libsqlitejdbc.so", "armeabi-v7a") },
    tasks.register<Sync>("android-x86_64-ExtractSqliteLib")
        .also { it.extractSqliteNativeLib("org/sqlite/native/Linux-Android/x86_64/libsqlitejdbc.so", "x86_64") },
    tasks.register<Sync>("android-x86-ExtractSqliteLib")
        .also { it.extractSqliteNativeLib("org/sqlite/native/Linux-Android/x86/libsqlitejdbc.so", "x86") },
)

tasks.matching { it.name == "preBuild" }.configureEach {
    dependsOn(extractSqliteAndroidLibs)
}
