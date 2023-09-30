import com.android.ndkports.CMakePortTask
import com.android.ndkports.CMakeCompatibleVersion
import com.android.ndkports.PrefabSysrootPlugin

val portVersion = "21.02.0"

group = rootProject.group
version = portVersion

plugins {
    id("maven-publish")
    id("signing")
    id("com.android.ndkports.NdkPorts")
}

repositories {
    mavenCentral()
    google()
}
dependencies {
    val ndkVersionSuffix = rootProject.extra.get("ndkVersionSuffix")
    implementation("com.viliussutkus89.ndk.thirdparty:freetype${ndkVersionSuffix}:2.13.2")
    implementation("com.viliussutkus89.ndk.thirdparty:iconv${ndkVersionSuffix}:1.17")
}

ndkPorts {
    ndkPath.set(File(project.findProperty("ndkPath") as String))
    source.set(project.file("poppler-${portVersion}.tar.xz"))
    minSdkVersion.set(rootProject.extra.get("minSdkSupportedByNdk").toString().toInt())
}

tasks.prefab {
    generator.set(PrefabSysrootPlugin::class.java)
}

val buildTask = tasks.register<CMakePortTask>("buildPort") {
    // @TODO: prefabGeneratedAbiDir should be handled by buildSrc
    val prefabGeneratedDir = prefabGenerated.get().asFile
    cmake {
        val prefabGeneratedAbiDir = prefabGeneratedDir.resolve(toolchain.abi.triple)
        arg("-DCMAKE_FIND_ROOT_PATH=${prefabGeneratedAbiDir.absolutePath}")

        args(
            "-DENABLE_DCTDECODER=none",
            "-DENABLE_LIBOPENJPEG=none",
            "-DENABLE_CMS=none",
            "-DBUILD_GTK_TESTS=OFF",
            "-DBUILD_QT5_TESTS=OFF",
            "-DBUILD_QT6_TESTS=OFF",
            "-DBUILD_CPP_TESTS=OFF",
            "-DBUILD_MANUAL_TESTS=OFF",
            "-DENABLE_BOOST=OFF",
            "-DENABLE_UTILS=OFF",
            "-DENABLE_CPP=ON",
            "-DENABLE_GLIB=OFF",
            "-DENABLE_GOBJECT_INTROSPECTION=OFF",
            "-DENABLE_QT5=OFF",
            "-DENABLE_QT6=OFF",
            "-DENABLE_LIBCURL=OFF",
            "-DRUN_GPERF_IF_PRESENT=OFF",

            "-DBUILD_SHARED_LIBS=OFF"
        )
    }
}

tasks.findByName("extractSrc")?.dependsOn(
    tasks.register("extractAssets", com.android.ndkports.SourceExtractTask::class.java) {
        source.set(project.file("poppler-data-0.4.12.tar.gz"))
        outDir.set(buildDir.resolve("assets/poppler"))
    }
)

tasks.prefabPackage {
    version.set(CMakeCompatibleVersion.parse(portVersion))

    licensePath.set("COPYING")

    modules {
        create("poppler") {
            static.set(true)
            dependencies.set(listOf(":poppler"))
        }
        create("poppler-cpp") {
            static.set(true)
            dependencies.set(listOf(":poppler-cpp"))
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("release") {
            from(components["prefab"])
            artifactId += rootProject.extra.get("ndkVersionSuffix")
            pom {
                name.set("Poppler")
                description.set("Poppler is a free software utility library for rendering Portable Document Format (PDF) documents. Poppler-data bundled.")
                url.set("https://poppler.freedesktop.org")
                licenses {
                    // Poppler licenses
                    license {
                        name.set("GPLv2")
                        url.set("https://cgit.freedesktop.org/poppler/poppler/plain/COPYING?h=poppler-21.02.0")
                        distribution.set("repo")
                    }
                    license {
                        name.set("GPLv3")
                        url.set("https://cgit.freedesktop.org/poppler/poppler/plain/COPYING3?h=poppler-21.02.0")
                        distribution.set("repo")
                    }

                    // Poppler-data licenses
                    license {
                        name.set("MIT")
                        url.set("https://cgit.freedesktop.org/poppler/poppler-data/tree/COPYING?h=POPPLER_DATA_0_4_12")
                        distribution.set("repo")
                    }
                    license {
                        name.set("COPYING.adobe")
                        url.set("https://cgit.freedesktop.org/poppler/poppler-data/tree/COPYING.adobe?h=POPPLER_DATA_0_4_12")
                        distribution.set("repo")
                    }
                    license {
                        name.set("GPLv2")
                        url.set("https://cgit.freedesktop.org/poppler/poppler-data/tree/COPYING.gpl2?h=POPPLER_DATA_0_4_12")
                        distribution.set("repo")
                    }

                }
                developers {
                    // Developer list obtained from:
                    // https://gitlab.freedesktop.org/poppler/poppler/-/blob/poppler-21.02.0/AUTHORS
                    // https://gitlab.freedesktop.org/poppler/poppler-data/-/blob/POPPLER_DATA_0_4_12/README
                    developer {
                        name.set("Derek Noonburg")
                        email.set("derekn@foolabs.com")
                    }
                    developer {
                        name.set("Albert Astals Cid")
                        email.set("aacid@kde.org")
                    }
                }
                scm {
                    url.set("https://github.com/ViliusSutkus89/ndkports")
                    connection.set("scm:git:https://github.com/ViliusSutkus89/ndkports.git")
                }
            }
        }
    }
}

afterEvaluate {
    System.getenv("SIGNING_KEY")?.let { signingKey ->
        signing {
            isRequired = true
            useInMemoryPgpKeys(signingKey, System.getenv("SIGNING_PASS"))
            sign(publishing.publications.findByName("release"))
        }
    }
}
