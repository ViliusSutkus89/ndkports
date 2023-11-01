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
include(":proxy-libintl")
include(":pcre2")
include(":glib2")
include(":libpng")
include(":libjpeg-turbo")
include(":libtiff")
include(":pixman")
include(":libexpat")
include(":fontconfig")
include(":cairo")
include(":lcms2")
include(":openjpeg")
include(":libxml2")
include(":fribidi")
include(":graphite2")
include(":harfbuzz")
include(":json-glib")
include(":libtool")
include(":spiro")
