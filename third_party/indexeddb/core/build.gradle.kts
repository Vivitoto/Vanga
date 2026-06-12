plugins {
    alias(libs.plugins.kotlinMultiplatform)
}

kotlin {
    js {
        browser()
        binaries.library()
    }

    sourceSets {
        commonMain.dependencies {
            api(libs.kotlinx.coroutines.core)
        }
        jsMain.dependencies {
            implementation(project(":third_party:indexeddb:external"))
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}
