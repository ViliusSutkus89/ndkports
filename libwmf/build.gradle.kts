import com.android.ndkports.AutoconfPortTask
import com.android.ndkports.CMakeCompatibleVersion
import com.android.ndkports.PrefabSysrootPlugin
import org.gradle.jvm.tasks.Jar

val portVersion = "0.2.13"

group = rootProject.group
version = "${portVersion}-beta-1"

plugins {
    id("maven-publish")
    id("signing")
    id("com.android.ndkports.NdkPorts")
}

dependencies {
    val ndkVersionSuffix = rootProject.extra.get("ndkVersionSuffix")
    val dependencyLibraryTypeSuffix = rootProject.extra.get("dependencyLibraryTypeSuffix")
    implementation("com.viliussutkus89.ndk.thirdparty:freetype${ndkVersionSuffix}${dependencyLibraryTypeSuffix}:2.13.2-beta-8")
    implementation("com.viliussutkus89.ndk.thirdparty:libexpat${ndkVersionSuffix}${dependencyLibraryTypeSuffix}:2.5.0-beta-4")
    implementation("com.viliussutkus89.ndk.thirdparty:libjpeg-turbo${ndkVersionSuffix}${dependencyLibraryTypeSuffix}:3.0.1-beta-3")
    implementation("com.viliussutkus89.ndk.thirdparty:libpng${ndkVersionSuffix}${dependencyLibraryTypeSuffix}:1.6.40-beta-7")
}

ndkPorts {
    ndkPath.set(File(project.findProperty("ndkPath") as String))
    minSdkVersion.set(rootProject.extra.get("minSdkSupportedByNdk").toString().toInt())
    source.set(project.file("v${portVersion}.tar.gz"))
}

tasks.prefab {
    generator.set(PrefabSysrootPlugin::class.java)
}

val buildTask = tasks.register<AutoconfPortTask>("buildPort") {
    val generatedDependencies = prefabGenerated.get().asFile
    val isStatic = project.findProperty("libraryType") == "static"
    autoconf {
        val generated = generatedDependencies.resolve(toolchain.abi.triple)
        args(
            "--with-expat=$generated",
            "--with-freetype=$generated",
            "--with-png=$generated",
            "--with-jpeg=$generated",
        )
        env["ac_cv_path_FREETYPE_CONFIG"] = "pkg-config freetype2"
        if (isStatic)
            env["ac_cv_path_FREETYPE_CONFIG"] += " --static"
    }
}

tasks.prefabPackage {
    version.set(CMakeCompatibleVersion.parse(portVersion))

    licensePath.set("COPYING")

    dependencies.set(mapOf(
        "freetype" to "1",
        "libexpat" to "1",
        "libjpeg-turbo" to "1",
        "libpng" to "1",
    ))
    
    modules {
        create("wmf") {
            static.set(project.findProperty("libraryType") == "static")
            dependencies.set(listOf(
                "//freetype:freetype",
                "//libexpat:expat",
                "//libjpeg-turbo:jpeg",
                "//libpng:png",
            ))
        }
        create("wmflite") {
            static.set(project.findProperty("libraryType") == "static")
            dependencies.set(listOf(
                "//freetype:freetype",
                "//libexpat:expat",
                "//libjpeg-turbo:jpeg",
                "//libpng:png",
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
                name.set("libwmf")
                description.set("library for converting WMF files")
                url.set("https://github.com/caolanm/libwmf")
                licenses {
                    license {
                        name.set("GPLv2")
                        url.set("https://github.com/caolanm/libwmf/blob/v${portVersion}/COPYING")
                        distribution.set("repo")
                    }
                }
                developers {
                    // Developer list obtained from:
                    // https://github.com/caolanm/libwmf/blob/v0.2.13/CREDITS
                    developer {
                        name.set("Thomas Boutell")
                    }
                    developer {
                        id.set("allegro")
                    }
                    developer {
                        id.set("Gdtclft")
                    }
                    developer {
                        id.set("wine")
                    }
                    developer {
                        name.set("Bjorn Reese")
                    }
                    developer {
                        name.set("Daniel Stenberg")
                    }
                    developer {
                        name.set("Caolan McNamara")
                    }
                    developer {
                        name.set("Francis James Franklin")
                    }
                    developer {
                        name.set("David Airlie")
                    }
                    developer {
                        name.set("Frédéric Vivien")
                    }
                    developer {
                        name.set("Martin Vermeer")
                    }
                    developer {
                        name.set("Bob Friesenhahn")
                    }
                    developer {
                        name.set("Raj Manandhar")
                    }
                    developer {
                        name.set("Steven Michael Robbins")
                    }
                    developer {
                        name.set("Steven Michael Robbins")
                    }
                    developer {
                        name.set("Bob Bell")
                    }
                    developer {
                        name.set("Albert Chin")
                    }
                    developer {
                        name.set("Matej Vila")
                    }
                    developer {
                        name.set("Benjamin Geer")
                    }
                    developer {
                        name.set("Peter Ohlerich")
                    }
                    developer {
                        name.set("Steve Oney")
                    }
                    developer {
                        name.set("Michael Cree")
                    }
                    developer {
                        name.set("Dom Lachowicz")
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
