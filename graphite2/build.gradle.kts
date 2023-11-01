import com.android.ndkports.CMakePortTask
import com.android.ndkports.CMakeCompatibleVersion
import com.android.ndkports.PrefabSysrootPlugin
import org.gradle.jvm.tasks.Jar

val portVersion = "1.3.14"

group = rootProject.group
version = "${portVersion}-beta-1"

plugins {
    id("maven-publish")
    id("signing")
    id("com.android.ndkports.NdkPorts")
}

ndkPorts {
    ndkPath.set(File(project.findProperty("ndkPath") as String))
    minSdkVersion.set(rootProject.extra.get("minSdkSupportedByNdk").toString().toInt())
    source.set(project.file("${name}-${portVersion}.tgz"))
}

tasks.prefab {
    generator.set(PrefabSysrootPlugin::class.java)
}

val buildTask = tasks.register<CMakePortTask>("buildPort") {
    cmake { }
}

tasks.prefabPackage {
    version.set(CMakeCompatibleVersion.parse(portVersion))

    licensePath.set("LICENSE")

    modules {
        create("graphite2") {
            static.set(project.findProperty("libraryType") == "static")
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
                name.set("Graphite engine")
                description.set("Graphite is a system that can be used to create “smart fonts” capable of displaying writing systems with various complex behaviors. A smart font contains not only letter shapes but also additional instructions indicating how to combine and position the letters in complex ways.")
                url.set("https://graphite.sil.org")
                licenses {
                    license {
                        name.set("LGPLv2.1-or-later")
                        url.set("https://raw.githubusercontent.com/silnrsi/graphite/1.3.14/COPYING")
                        distribution.set("repo")
                    }
                    license {
                        name.set("MPL")
                        url.set("https://raw.githubusercontent.com/silnrsi/graphite/1.3.14/COPYING")
                        distribution.set("repo")
                    }
                    license {
                        name.set("GPLv2-or-later")
                        url.set("https://raw.githubusercontent.com/silnrsi/graphite/1.3.14/COPYING")
                        distribution.set("repo")
                    }
                }
                developers {
                    // Developer list obtained from:
                    // https://github.com/silnrsi/graphite/graphs/contributors
                    developer {
                        id.set("mhosken")
                    }
                    developer {
                        id.set("tim-eves")
                    }
                    developer {
                        id.set("sharoncorrell")
                    }
                    developer {
                        id.set("annie-o")
                    }
                    developer {
                        id.set("jfkthame")
                    }
                    developer {
                        id.set("jvgaultney")
                    }
                    developer {
                        id.set("bgermann")
                    }
                    developer {
                        id.set("simoncozens")
                    }
                    developer {
                        id.set("glandium")
                    }
                    developer {
                        id.set("mirabilos")
                    }
                    developer {
                        id.set("khorben")
                    }
                    developer {
                        id.set("n7s")
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
