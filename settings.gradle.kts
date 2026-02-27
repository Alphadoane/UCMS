pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // For Agora SDK if needed
        maven { url = uri("https://maven.zoom.us/artifactory/zoom-repo/") }
    }
}

rootProject.name = "StudentPortal"
include(":app", ":shared")
