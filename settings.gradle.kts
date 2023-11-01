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
include(":libiconv")
include(":pcre2")
include(":libpng")
include(":libjpeg-turbo")
include(":libtiff")
include(":pixman")
include(":libexpat")
include(":fontconfig")
include(":lcms2")
include(":openjpeg")
include(":libxml2")
include(":fribidi")
include(":libtool")
include(":spiro")
