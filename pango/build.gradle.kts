import com.android.ndkports.CMakeCompatibleVersion
import com.android.ndkports.MesonPortTask
import com.android.ndkports.PrefabSysrootPlugin
import org.gradle.jvm.tasks.Jar

group = rootProject.group

val portVersion = "1.51.0"
version = "1.51.0-beta-6"

plugins {
    id("maven-publish")
    id("signing")
    id("com.android.ndkports.NdkPorts")
}

dependencies {
    val ndkVersionSuffix = rootProject.extra.get("ndkVersionSuffix")
    val dependencyLibraryTypeSuffix = rootProject.extra.get("dependencyLibraryTypeSuffix")
    implementation("com.viliussutkus89.ndk.thirdparty:cairo${ndkVersionSuffix}${dependencyLibraryTypeSuffix}:1.18.0-beta-5")
    implementation("com.viliussutkus89.ndk.thirdparty:fontconfig${ndkVersionSuffix}${dependencyLibraryTypeSuffix}:2.14.2-beta-5")
    implementation("com.viliussutkus89.ndk.thirdparty:freetype${ndkVersionSuffix}${dependencyLibraryTypeSuffix}:2.13.2-beta-7")
    implementation("com.viliussutkus89.ndk.thirdparty:fribidi${ndkVersionSuffix}${dependencyLibraryTypeSuffix}:1.0.13-beta-3")
    implementation("com.viliussutkus89.ndk.thirdparty:glib2${ndkVersionSuffix}${dependencyLibraryTypeSuffix}:2.78.3-beta-2")
    implementation("com.viliussutkus89.ndk.thirdparty:harfbuzz${ndkVersionSuffix}${dependencyLibraryTypeSuffix}:8.3.0-beta-2")
}

ndkPorts {
    ndkPath.set(File(project.findProperty("ndkPath") as String))
    minSdkVersion.set(rootProject.extra.get("minSdkSupportedByNdk").toString().toInt())
    source.set(project.file("${name}-${portVersion}.tar.xz"))
}

fun File.patch(patch: String): File {
    return patch(projectDir.resolve("patches/$portVersion").resolve(patch))
}

fun File.patch(patch: File): File {
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
    return this
}

tasks.extractSrc {
    doLast {
        outDir.get().asFile.resolve("pango/pango-layout.c")
            .patch("localeconv.patch")
    }
}

tasks.prefab {
    generator.set(PrefabSysrootPlugin::class.java)
}

tasks.register<MesonPortTask>("buildPort") {
    meson { }
}

tasks.prefabPackage {
    version.set(CMakeCompatibleVersion.parse(portVersion))

    licensePath.set("COPYING")

    dependencies.set(mutableMapOf(
        "cairo" to "1",
        "fontconfig" to "1",
        "freetype" to "1",
        "fribidi" to "1",
        "glib2" to "1",
        "harfbuzz" to "1",
    ))

    modules {
        create("pango-1.0") {
            static.set(project.findProperty("libraryType") == "static")
            dependencies.set(listOf(
                "m",
                "//glib2:glib-2.0",
                "//glib2:gobject-2.0",
                "//glib2:gio-2.0",
                "//fribidi:fribidi",
                "//harfbuzz:harfbuzz",
                "//fontconfig:fontconfig",
                "//freetype:freetype",
                "//cairo:cairo",
            ))
        }
        create("pangocairo-1.0") {
            static.set(project.findProperty("libraryType") == "static")
            dependencies.set(listOf(
                "m",
                ":pango",
                ":pangoft2",
                "//glib2:glib-2.0",
                "//glib2:gobject-2.0",
                "//glib2:gio-2.0",
                "//fribidi:fribidi",
                "//harfbuzz:harfbuzz",
                "//fontconfig:fontconfig",
                "//freetype:freetype",
                "//cairo:cairo",
            ))
        }
        create("pangoft2-1.0") {
            static.set(project.findProperty("libraryType") == "static")
            dependencies.set(listOf(
                "m",
                ":pango",
                "//glib2:glib-2.0",
                "//glib2:gobject-2.0",
                "//glib2:gio-2.0",
                "//fribidi:fribidi",
                "//harfbuzz:harfbuzz",
                "//fontconfig:fontconfig",
                "//freetype:freetype",
                "//cairo:cairo",
            ))
        }
    }
}

val packageSources = tasks.register<Jar>("packageSources") {
    archiveClassifier.set("sources")
    from(projectDir.resolve("build.gradle.kts"))
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
                name.set("Pango")
                description.set("Pango is a library for laying out and rendering of text, with an emphasis on internationalization")
                url.set("https://pango.gnome.org")
                licenses {
                    license {
                        name.set("LGPLv2")
                        url.set("https://gitlab.gnome.org/GNOME/pango/-/raw/1.51.1/COPYING")
                        distribution.set("repo")
                    }
                }
                developers {
                    // Developer list obtained from:
                    // https://gitlab.gnome.org/GNOME/pango/-/blob/1.51.1/THANKS
                    developer {
                        name.set("Abigail Brady")
                    }
                    developer {
                        name.set("Hans Breuer")
                    }
                    developer {
                        name.set("Matthias Clasen")
                    }
                    developer {
                        name.set("Sivaraj Doddannan")
                    }
                    developer {
                        name.set("Behdad Esfahbod")
                    }
                    developer {
                        name.set("Dov Grobgeld")
                    }
                    developer {
                        name.set("Karl Koehler")
                    }
                    developer {
                        name.set("Alex Larsson")
                    }
                    developer {
                        name.set("Tor Lillqvist")
                    }
                    developer {
                        name.set("Changwoo Ryu")
                    }
                    developer {
                        name.set("Havoc Pennington")
                    }
                    developer {
                        name.set("Roozbeh Pournader")
                    }
                    developer {
                        name.set("Chookij Vanatham")
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
