rootProject.name = "ndkports"

pluginManagement {
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id == "kotlinx-serialization") {
                useModule("org.jetbrains.kotlin:kotlin-serialization:${requested.version}")
            }
        }
    }
    repositories {
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_PROJECT)
    repositories {
        mavenCentral()
    }
}

include(":libffi")
include(":freetype")
include(":pcre2")
include(":libpng")
include(":libjpeg-turbo")
include(":libtiff")
include(":pixman")
include(":libexpat")
include(":lcms2")
include(":openjpeg")
include(":fribidi")
include(":libtool")
include(":spiro")
