import com.android.ndkports.CMakePortTask
import com.android.ndkports.CMakeCompatibleVersion
import com.android.ndkports.PrefabSysrootPlugin
import org.gradle.jvm.tasks.Jar

val portVersion = "1.6.40"

group = rootProject.group
version = "${portVersion}-beta-6"

plugins {
    id("maven-publish")
    id("signing")
    id("com.android.ndkports.NdkPorts")
}

ndkPorts {
    ndkPath.set(File(project.findProperty("ndkPath") as String))
    minSdkVersion.set(rootProject.extra.get("minSdkSupportedByNdk").toString().toInt())
    source.set(project.file("${name}-${portVersion}.tar.xz"))
}

tasks.prefab {
    generator.set(PrefabSysrootPlugin::class.java)
}

tasks.register<CMakePortTask>("buildPort") {
    val libraryType = project.findProperty("libraryType")
    cmake {
        when (libraryType) {
            "shared" -> args(
                "-DPNG_SHARED=ON",
                "-DPNG_STATIC=OFF",
            )
            "static" -> args(
                "-DPNG_SHARED=OFF",
                "-DPNG_STATIC=ON",
            )
        }
    }
}

tasks.prefabPackage {
    version.set(CMakeCompatibleVersion.parse(portVersion))

    licensePath.set("LICENSE")

    modules {
        create("png16") {
            static.set(project.findProperty("libraryType") == "static")
            dependencies.set(listOf("z", "m"))
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
                name.set("libpng")
                description.set("libpng is the official PNG reference library.")
                url.set("http://libpng.org/pub/png/libpng.html")
                licenses {
                    // Poppler licenses
                    license {
                        name.set("libpng")
                        url.set("http://www.libpng.org/pub/png/src/libpng-LICENSE.txt")
                        distribution.set("repo")
                    }

                }
                developers {
                    // Developer list obtained from:
                    // https://sourceforge.net/projects/libpng/files/libpng16/1.6.40/AUTHORS.md/download
                    developer {
                        name.set("Andreas Dilger")
                    }
                    developer {
                        name.set("Cosmin Truta")
                    }
                    developer {
                        name.set("Dave Martindale")
                    }
                    developer {
                        name.set("Eric S. Raymond")
                    }
                    developer {
                        name.set("Gilles Vollant")
                    }
                    developer {
                        name.set("Glenn Randers-Pehrson")
                    }
                    developer {
                        name.set("Greg Roelofs")
                    }
                    developer {
                        name.set("Guy Eric Schalnat")
                    }
                    developer {
                        name.set("James Yu")
                    }
                    developer {
                        name.set("John Bowler")
                    }
                    developer {
                        name.set("Kevin Bracey")
                    }
                    developer {
                        name.set("Magnus Holmgren")
                    }
                    developer {
                        name.set("Mandar Sahastrabuddhe")
                    }
                    developer {
                        name.set("Mans Rullgard")
                    }
                    developer {
                        name.set("Matt Sarett")
                    }
                    developer {
                        name.set("Mike Klein")
                    }
                    developer {
                        name.set("Pascal Massimino")
                    }
                    developer {
                        name.set("Paul Schmidt")
                    }
                    developer {
                        name.set("Philippe Antoine")
                    }
                    developer {
                        name.set("Qiang Zhou")
                    }
                    developer {
                        name.set("Sam Bushell")
                    }
                    developer {
                        name.set("Samuel Williams")
                    }
                    developer {
                        name.set("Simon-Pierre Cadieux")
                    }
                    developer {
                        name.set("Tim Wegner")
                    }
                    developer {
                        name.set("Tom Lane")
                    }
                    developer {
                        name.set("Tom Tanner")
                    }
                    developer {
                        name.set("Vadim Barkov")
                    }
                    developer {
                        name.set("Willem van Schaik")
                    }
                    developer {
                        name.set("Zhijie Liang")
                    }
                    developer {
                        name.set("Richard Townsend")
                    }
                    developer {
                        name.set("Dan Field")
                    }
                    developer {
                        name.set("Leon Scroggins III")
                    }
                    developer {
                        name.set("Sami Boukortt")
                    }
                    developer {
                        name.set("Wan-Teh Chang")
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
