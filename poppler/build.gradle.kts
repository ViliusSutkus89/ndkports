import com.android.ndkports.CMakeCompatibleVersion
import com.android.ndkports.CMakePortTask
import com.android.ndkports.PrefabSysrootPlugin
import org.gradle.jvm.tasks.Jar

group = rootProject.group

// Hardcode a list of available versions
val portVersion = when(project.findProperty("packageVersion")) {
    "0.81.0" -> {
        version = "0.81.0-beta-1"
        "0.81.0"
    }
    "0.89.0" -> {
        version = "0.89.0-beta-1"
        "0.89.0"
    }
    "21.02.0" -> {
        version = "21.02.0-beta-1"
        "21.02.0"
    }
    "23.10.0" -> {
        version = "23.10.0-beta-1"
        "0.81.0"
    }
    // @TODO:
    else -> {
        version = "0.81.0-beta-1"
        "0.81.0"
    }
}

plugins {
    id("maven-publish")
    id("signing")
    id("com.android.ndkports.NdkPorts")
}

dependencies {
    val ndkVersionSuffix = rootProject.extra.get("ndkVersionSuffix")
    implementation("com.viliussutkus89.ndk.thirdparty:freetype${ndkVersionSuffix}-static:2.13.2-beta-3")
    implementation("com.viliussutkus89.ndk.thirdparty:libiconv${ndkVersionSuffix}-static:1.17-beta-1")
    implementation("com.viliussutkus89.ndk.thirdparty:libpng${ndkVersionSuffix}-static:1.6.40-beta-4")
    implementation("com.viliussutkus89.ndk.thirdparty:libjpeg-turbo${ndkVersionSuffix}-static:3.0.1-beta-1")
    implementation("com.viliussutkus89.ndk.thirdparty:libtiff${ndkVersionSuffix}-static:4.6.0-beta-2")
    implementation("com.viliussutkus89.ndk.thirdparty:openjpeg${ndkVersionSuffix}-static:2.5.0-beta-2")
    implementation("com.viliussutkus89.ndk.thirdparty:glib2${ndkVersionSuffix}-static:2.78.1-beta-2")
    implementation("com.viliussutkus89.ndk.thirdparty:cairo${ndkVersionSuffix}-static:1.18.0-beta-1")
    implementation("com.viliussutkus89.ndk.thirdparty:lcms2${ndkVersionSuffix}-static:2.15-beta-2")

    when (portVersion) {
        // 23.10.0 supports Android's native alternative of fontconfig
        "0.81.0", "0.89.0", "21.02.0" -> {
            implementation("com.viliussutkus89.ndk.thirdparty:fontconfig${ndkVersionSuffix}-static:2.14.2-beta-1")
        }
    }
}

ndkPorts {
    ndkPath.set(File(project.findProperty("ndkPath") as String))
    minSdkVersion.set(rootProject.extra.get("minSdkSupportedByNdk").toString().toInt())
    source.set(project.file("${name}-${portVersion}.tar.xz"))
}

tasks.findByName("extractSrc")?.dependsOn(
    tasks.register("extractAssets", com.android.ndkports.SourceExtractTask::class.java) {
        source.set(project.file("poppler-data-0.4.12.tar.gz"))
        outDir.set(buildDir.resolve("assets/poppler"))
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
        if (isFile) listOf("patch", "-p0", absolutePath)
        else listOf("patch", "-p0")
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
        when (portVersion) {
            "0.81.0", "0.89.0", "21.02.0" -> {
                srcDir.resolve("CMakeLists.txt").patch("fontconfig.patch")
                srcDir.resolve("CMakeLists.txt").patch("FindCairo.patch")
                srcDir.resolve("ConfigureChecks.cmake").patch("have_unistd_h.patch")
            }
            "0.89.0", "21.02.0" -> {
                TODO()
            }
            "23.10.0" -> {
                // https://android.googlesource.com/platform/bionic/+/master/docs/32-bit-abi.md
                projectDir.resolve("CheckFileOffsetBits.cmake").copyTo(
                    target = outDir.get().asFile.resolve("cmake/modules/CheckFileOffsetBits.cmake"),
                    overwrite = true
                )
            }
        }
    }
}

tasks.prefab {
    generator.set(PrefabSysrootPlugin::class.java)
}

tasks.register<CMakePortTask>("buildPort") {
    when (portVersion) {
        "0.81.0", "0.89.0", "21.02.0" -> {
            cmake {
                args(
                    "-DENABLE_UNSTABLE_API_ABI_HEADERS=ON",

                    // poppler-gLib requires older GLib version.
                    "-DENABLE_GLIB=OFF",
                )
            }
            doLast {
                com.android.ndkports.Abi.values().forEach { abi ->
                    installDirectoryFor(abi)
                        .resolve("include/android.${abi.abiName}/lib/pkgconfig/poppler.pc").appendText(
                            "Requires: freetype2 libpng16 libturbojpeg libtiff-4 libopenjp2 glib-2.0 cairo lcms2 fontconfig"
                        )
                }
            }
        }
        "23.10.0" -> {
            cmake {
                args(
                    "-DENABLE_NSS3=OFF",
                    "-DENABLE_GPGME=OFF",
                    "-DENABLE_QT5=OFF",
                    "-DENABLE_QT6=OFF",
                    "-DENABLE_BOOST=OFF",
                    "-DENABLE_LIBCURL=OFF",

                    "-DENABLE_GLIB=OFF",
                )
            }
        }
    }
}


tasks.prefabPackage {
    version.set(CMakeCompatibleVersion.parse(portVersion))

    licensePath.set("COPYING")

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
                "//fontconfig:fontconfig",
            ))
        }
        create("poppler-cpp") {
            static.set(project.findProperty("libraryType") == "static")
            dependencies.set(listOf(":poppler"))
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