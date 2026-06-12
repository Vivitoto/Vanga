rootProject.name = "Vanga"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        mavenLocal()
    }
}

include(":vanga-app")
include(":vanga-domain:core")
include(":vanga-domain:offline")
include(":vanga-domain:komga-api")
include(":vanga-ui")

include(":vanga-infra:database:transaction")
include(":vanga-infra:database:shared")
include(":vanga-infra:database:sqlite")
include(":vanga-infra:database:wasm")
include(":vanga-infra:image-decoder:shared")
include(":vanga-infra:image-decoder:vips")
include(":vanga-infra:image-decoder:wasm-image-worker")
include(":vanga-infra:jni")
include(":vanga-infra:webview")

include(":third_party:ChipTextField:chiptextfield-core")
include(":third_party:ChipTextField:chiptextfield-m3")
include(":third_party:compose-sonner:sonner")
include(":third_party:indexeddb:core")
include(":third_party:indexeddb:external")

includeBuild("third_party/secret-service") {
    dependencySubstitution { substitute(module("de.swiesend:secret-service")) }
}
