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
        val extraRepositories: String? by settings
        extraRepositories?.split(" ")?.forEach {
            maven {
                url = java.net.URI("https://oss.sonatype.org/service/local/repositories/comviliussutkus89-${it}/content/")
            }
        }
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
include(":poppler")
include(":libxml2")
include(":fribidi")
include(":graphite2")
include(":harfbuzz")
include(":pango")
include(":libtool")
include(":libuninameslist")
include(":spiro")
include(":openlibm")
include(":fontforge")
include(":pdf2htmlEX")
include("libgsf")
