import com.android.ndkports.CMakeCompatibleVersion
import com.android.ndkports.MesonPortTask
import com.android.ndkports.PrefabSysrootPlugin
import org.gradle.jvm.tasks.Jar

val portVersion = "8.2.2"

group = rootProject.group
version = "${portVersion}-beta-3"

plugins {
    id("maven-publish")
    id("signing")
    id("com.android.ndkports.NdkPorts")
}

dependencies {
    val ndkVersionSuffix = rootProject.extra.get("ndkVersionSuffix")
    implementation("com.viliussutkus89.ndk.thirdparty:cairo${ndkVersionSuffix}-static:1.18.0-beta-3")
    implementation("com.viliussutkus89.ndk.thirdparty:fontconfig${ndkVersionSuffix}-static:2.14.2-beta-3")
    implementation("com.viliussutkus89.ndk.thirdparty:freetype${ndkVersionSuffix}-static:2.13.2-beta-5")
    implementation("com.viliussutkus89.ndk.thirdparty:glib2${ndkVersionSuffix}-static:2.78.1-beta-4")
}

ndkPorts {
    ndkPath.set(File(project.findProperty("ndkPath") as String))
    minSdkVersion.set(rootProject.extra.get("minSdkSupportedByNdk").toString().toInt())
    source.set(project.file("${name}-${portVersion}.tar.xz"))
}

tasks.prefab {
    generator.set(PrefabSysrootPlugin::class.java)
}

tasks.extractSrc {
    doLast {
        // #if __ANDROID_API__ >= 21
        // locale_t _Nullable uselocale(locale_t _Nullable __l) __INTRODUCED_IN(21);
        // #endif /* __ANDROID_API__ >= 21 */
        val minSdkVersion = rootProject.extra.get("minSdkSupportedByNdk").toString().toInt()
        if (minSdkVersion < 21) {
            outDir.get().asFile.resolve("meson.build").let {
                // uselocale detection gives a false positive. Disable it on pre 21
                it.writeText(it.readText().replace(
                    "['uselocale'],",
                    ""
                ))
            }
        }
    }
}

tasks.register<MesonPortTask>("buildPort") {
    meson {
//        arg("-Dgraphite2=enabled")
    }
}

tasks.prefabPackage {
    version.set(CMakeCompatibleVersion.parse(portVersion))

    licensePath.set("COPYING")

    dependencies.set(mapOf(
        "cairo" to "1",
        "fontconfig" to "1",
        "freetype" to "1",
        "glib2" to "1",
    ))

    modules {
        val isStatic = project.findProperty("libraryType") == "static"
        create("harfbuzz") {
            static.set(isStatic)
            dependencies.set(listOf(
                "m",
                "//freetype:freetype",
                "//glib2:glib-2.0",
            ))
        }
        create("harfbuzz-cairo") {
            static.set(isStatic)
            dependencies.set(listOf(
                "m",
                ":harfbuzz",
                "//freetype:freetype",
                "//glib2:glib-2.0",
                "//cairo:cairo",
            ))
        }
        create("harfbuzz-gobject") {
            static.set(isStatic)
            dependencies.set(listOf(
                ":harfbuzz",
                "//glib2:glib-2.0",
                "//glib2:gobject-2.0",
            ))
        }
        create("harfbuzz-subset") {
            static.set(isStatic)
            dependencies.set(listOf(
                "m",
                ":harfbuzz",
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
                name.set("HarfBuzz")
                description.set("HarfBuzz is a text shaping library. Using the HarfBuzz library allows programs to convert a sequence of Unicode input into properly formatted and positioned glyph output—for any writing system and language.")
                url.set("https://harfbuzz.github.io/")
                licenses {
                    license {
                        name.set("Old MIT")
                        url.set("https://github.com/harfbuzz/harfbuzz/blob/3.2.0/COPYING")
                        distribution.set("repo")
                    }
                    license {
                        name.set("OFL-1.1")
                        url.set("https://raw.githubusercontent.com/harfbuzz/harfbuzz/8.2.2/test/COPYING")
                        distribution.set("repo")
                    }
                    license {
                        name.set("Apache-2.0")
                        url.set("https://raw.githubusercontent.com/harfbuzz/harfbuzz/8.2.2/test/shape/data/aots/COPYING")
                        distribution.set("repo")
                    }
                    license {
                        name.set("Apache-2.0")
                        url.set("https://raw.githubusercontent.com/harfbuzz/harfbuzz/8.2.2/test/shape/data/text-rendering-tests/COPYING")
                        distribution.set("repo")
                    }
                }
                developers {
                    // Developer list obtained from:
                    // https://raw.githubusercontent.com/harfbuzz/harfbuzz/8.2.2/AUTHORS
                    // https://raw.githubusercontent.com/harfbuzz/harfbuzz/8.2.2/THANKS
                    developer {
                        name.set("Behdad Esfahbod")
                    }
                    developer {
                        name.set("David Corbett")
                    }
                    developer {
                        name.set("David Turner")
                    }
                    developer {
                        name.set("Ebrahim Byagowi")
                    }
                    developer {
                        name.set("Garret Rieger")
                    }
                    developer {
                        name.set("Jonathan Kew")
                    }
                    developer {
                        name.set("Khaled Hosny")
                    }
                    developer {
                        name.set("Lars Knoll")
                    }
                    developer {
                        name.set("Martin Hosken")
                    }
                    developer {
                        name.set("Owen Taylor")
                    }
                    developer {
                        name.set("Roderick Sheeter")
                    }
                    developer {
                        name.set("Roozbeh Pournader")
                    }
                    developer {
                        name.set("Simon Hausmann")
                    }
                    developer {
                        name.set("Werner Lemberg")
                    }
                    developer {
                        name.set("Bradley Grainger")
                    }
                    developer {
                        name.set("Kenichi Ishibashi")
                    }
                    developer {
                        name.set("Ivan Kuckir")
                        url.set("https://photopea.com/")
                    }
                    developer {
                        name.set("Ryan Lortie")
                    }
                    developer {
                        name.set("Jeff Muizelaar")
                    }
                    developer {
                        name.set("suzuki toshiya")
                    }
                    developer {
                        name.set("Philip Withnall")
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
