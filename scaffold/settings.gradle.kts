pluginManagement {
    includeBuild("build-logic")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolution {
    @Suppress("UnstableApiUsage")
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "{{PROJECT_NAME}}"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include(":app")

// Core modules
include(":core:model")
include(":core:domain")
include(":core:data")
include(":core:database")
include(":core:network")
include(":core:datastore")
include(":core:designsystem")
include(":core:ui")
include(":core:navigation")
include(":core:common")
include(":core:testing")

// Feature modules
include(":feature:home:api")
include(":feature:home:impl")
include(":feature:settings:impl")

// Sync
include(":sync:work")
