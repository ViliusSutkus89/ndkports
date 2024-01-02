import com.android.ndkports.CMakeCompatibleVersion
import com.android.ndkports.CMakePortTask
import com.android.ndkports.PrefabSysrootPlugin
import org.gradle.jvm.tasks.Jar

group = rootProject.group

val portVersion = "23.12.0"
version = "23.12.0-beta-6"

plugins {
    id("maven-publish")
    id("signing")
    id("com.android.ndkports.NdkPorts")
}

dependencies {
    val ndkVersionSuffix = rootProject.extra.get("ndkVersionSuffix")
    val dependencyLibraryTypeSuffix = rootProject.extra.get("dependencyLibraryTypeSuffix")
    implementation("com.viliussutkus89.ndk.thirdparty:freetype${ndkVersionSuffix}${dependencyLibraryTypeSuffix}:2.13.2-beta-8")
    implementation("com.viliussutkus89.ndk.thirdparty:libiconv${ndkVersionSuffix}${dependencyLibraryTypeSuffix}:1.17-beta-3")
    implementation("com.viliussutkus89.ndk.thirdparty:libpng${ndkVersionSuffix}${dependencyLibraryTypeSuffix}:1.6.40-beta-7")
    implementation("com.viliussutkus89.ndk.thirdparty:libjpeg-turbo${ndkVersionSuffix}${dependencyLibraryTypeSuffix}:3.0.1-beta-3")
    implementation("com.viliussutkus89.ndk.thirdparty:libtiff${ndkVersionSuffix}${dependencyLibraryTypeSuffix}:4.6.0-beta-6")
    implementation("com.viliussutkus89.ndk.thirdparty:openjpeg${ndkVersionSuffix}${dependencyLibraryTypeSuffix}:2.5.0-beta-4")
    implementation("com.viliussutkus89.ndk.thirdparty:glib2${ndkVersionSuffix}${dependencyLibraryTypeSuffix}:2.75.0-beta-1")
    implementation("com.viliussutkus89.ndk.thirdparty:cairo${ndkVersionSuffix}${dependencyLibraryTypeSuffix}:1.18.0-beta-7")
    implementation("com.viliussutkus89.ndk.thirdparty:lcms2${ndkVersionSuffix}${dependencyLibraryTypeSuffix}:2.16-beta-2")
}

ndkPorts {
    ndkPath.set(File(project.findProperty("ndkPath") as String))
    minSdkVersion.set(rootProject.extra.get("minSdkSupportedByNdk").toString().toInt())
    source.set(project.file("${name}-${portVersion}.tar.xz"))
}

tasks.findByName("extractSrc")?.dependsOn(
    tasks.register("extractAssets", com.android.ndkports.SourceExtractTask::class.java) {
        source.set(project.file("poppler-data-0.4.12.tar.gz"))
        outDir.set(layout.buildDirectory.get().asFile.resolve("assets/poppler"))
    }
)

fun File.replace(oldValue: String, newValue: String, ignoreCase: Boolean = false): File {
    writeText(readText().replace(oldValue, newValue, ignoreCase))
    return this
}

fun File.patch(patch: String) {
    patch(projectDir.resolve("patches/$portVersion").resolve(patch))
}

fun File.patch(patch: File) {
    val pb = ProcessBuilder(
        if (isFile) listOf("patch", "--ignore-whitespace", "-p0", absolutePath)
        else listOf("patch", "--ignore-whitespace", "-p0")
    )

    if (isDirectory)
        pb.directory(absoluteFile)

    val process = pb.start()
    process.outputStream.writer().use {
        it.write(patch.readText())
    }
    process.errorStream.bufferedReader().use {
        println(it.readText())
    }
    process.inputStream.bufferedReader().use {
        println(it.readText())
    }
    if (process.waitFor() != 0) {
        throw RuntimeException("Patch failed!\n")
    }
}

tasks.extractSrc {
    doLast {
        val srcDir = outDir.get().asFile
        srcDir.resolve("CMakeLists.txt").patch("FindCairo.patch")
        srcDir.resolve("cmake/modules/CheckFileOffsetBits.cmake").patch("CheckFileOffsetBits.patch")
        srcDir.patch("ExportPrivatesForPdf2htmlEX.patch")
    }
}

tasks.prefab {
    generator.set(PrefabSysrootPlugin::class.java)
}

tasks.register<CMakePortTask>("buildPort") {
    cmake {
        args(
            "-DENABLE_UNSTABLE_API_ABI_HEADERS=ON",
            "-DENABLE_NSS3=OFF",
            "-DENABLE_GPGME=OFF",
            "-DENABLE_QT5=OFF",
            "-DENABLE_QT6=OFF",
            "-DENABLE_BOOST=OFF",
            "-DENABLE_LIBCURL=OFF",
        )
    }
    doLast {
        com.android.ndkports.Abi.values().forEach { abi ->
            val pkgconfigdir = installDirectoryFor(abi).resolve("lib/pkgconfig")
            pkgconfigdir.resolve("poppler.pc")
                .appendText("Requires: freetype2 libpng16 libturbojpeg libtiff-4 libopenjp2 glib-2.0 cairo lcms2\n")
        }
    }
}


tasks.prefabPackage {
    version.set(CMakeCompatibleVersion.parse(portVersion))

    licensePath.set("COPYING")

    dependencies.set(mapOf(
        "freetype" to "1",
        "libiconv" to "1",
        "libpng" to "1",
        "libjpeg-turbo" to "1",
        "libtiff" to "1",
        "openjpeg" to "1",
        "glib2" to "1",
        "cairo" to "1",
        "lcms2" to "1",
    ))

    modules {
        create("poppler") {
            static.set(project.findProperty("libraryType") == "static")
            dependencies.set(listOf(
                "//freetype:freetype",
                "//libpng:png16",
                "//libjpeg-turbo:turbojpeg",
                "//libtiff:tiff",
                "//openjpeg:openjp2",
                "//glib2:glib-2.0",
                "//cairo:cairo",
                "//lcms2:lcms2",
            ))
        }
        create("poppler-cpp") {
            static.set(project.findProperty("libraryType") == "static")
            dependencies.set(listOf(":poppler"))
        }
        create("poppler-glib") {
            static.set(project.findProperty("libraryType") == "static")
            dependencies.set(listOf(
                ":poppler",
                "//glib2:glib-2.0",
                "//glib2:gobject-2.0",
                "//cairo:cairo",
            ))
        }
    }
}

val packageSources = tasks.register<Jar>("packageSources") {
    archiveClassifier.set("sources")
    from(projectDir.resolve("build.gradle.kts"))
    from(projectDir.resolve("patches/$portVersion"))
    from(ndkPorts.source)
}

publishing {
    publications {
        create<MavenPublication>("release") {
            from(components["prefab"])
            artifactId += rootProject.extra.get("ndkVersionSuffix")
            artifactId += rootProject.extra.get("libraryTypeSuffix")
            artifact(packageSources)
            pom {
                name.set("Poppler")
                description.set("Poppler is a free software utility library for rendering Portable Document Format (PDF) documents. Poppler-data bundled.")
                url.set("https://poppler.freedesktop.org")
                licenses {
                    // Poppler licenses
                    license {
                        name.set("GPLv2")
                        url.set("https://cgit.freedesktop.org/poppler/poppler/plain/COPYING?h=poppler-$portVersion")
                        distribution.set("repo")
                    }
                    license {
                        name.set("GPLv3")
                        url.set("https://cgit.freedesktop.org/poppler/poppler/plain/COPYING3?h=poppler-$portVersion")
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
                    // https://gitlab.freedesktop.org/poppler/poppler/-/raw/poppler-23.12.0/AUTHORS
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
