import com.android.ndkports.CMakePortTask
import com.android.ndkports.CMakeCompatibleVersion
import com.android.ndkports.PrefabSysrootPlugin
import org.gradle.jvm.tasks.Jar

val portVersion = "2.5.0"

group = rootProject.group
version = "${portVersion}-beta-3"

plugins {
    id("maven-publish")
    id("signing")
    id("com.android.ndkports.NdkPorts")
}

ndkPorts {
    ndkPath.set(File(project.findProperty("ndkPath") as String))
    minSdkVersion.set(rootProject.extra.get("minSdkSupportedByNdk").toString().toInt())
    source.set(project.file("v${portVersion}.tar.gz"))
}

tasks.prefab {
    generator.set(PrefabSysrootPlugin::class.java)
}

val buildTask = tasks.register<CMakePortTask>("buildPort") {
    cmake {
        arg("-DBUILD_CODEC=OFF")
    }

    doLast {
        com.android.ndkports.Abi.values().forEach { abi ->
            installDirectoryFor(abi).let { installDirectory ->
                val dstDir = installDirectory.resolve("include/android.${abi.abiName}/lib/openjpeg-2.5").apply { mkdirs() }
                installDirectory.resolve("lib/openjpeg-2.5").listFiles()?.forEach {
                    dstDir.resolve(it.name).writeText(
                        it.readText().replace(installDirectory.absolutePath, "/__PREFAB__PACKAGE__PATH__")
                    )
                }
            }
        }
    }
}


tasks.prefabPackage {
    version.set(CMakeCompatibleVersion.parse(portVersion))

    licensePath.set("LICENSE")

    modules {
        create("openjp2") {
            static.set(project.findProperty("libraryType") == "static")
            dependencies.set(listOf("m"))
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
                name.set("Poppler")
                description.set("An open-source JPEG 2000 codec written in C.")
                url.set("https://www.openjpeg.org")
                licenses {
                    // Poppler licenses
                    license {
                        name.set("BSD-2-Clause")
                        url.set("https://raw.githubusercontent.com/uclouvain/openjpeg/v2.5.0/LICENSE")
                        distribution.set("repo")
                    }
                }
                developers {
                    // Developer list obtained from:
                    // https://raw.githubusercontent.com/uclouvain/openjpeg/v2.5.0/AUTHORS.md
                    // https://raw.githubusercontent.com/uclouvain/openjpeg/v2.5.0/THANKS.md
                    developer {
                        name.set("David Janssens")
                    }
                    developer {
                        name.set("Kaori Hagihara")
                    }
                    developer {
                        name.set("Jerome Fimes")
                    }
                    developer {
                        name.set("Giuseppe Baruffa")
                    }
                    developer {
                        name.set("Mickaël Savinaud")
                    }
                    developer {
                        name.set("Mathieu Malaterre")
                    }
                    developer {
                        name.set("Yannick Verschueren")
                    }
                    developer {
                        name.set("Giuseppe Baruffa")
                    }
                    developer {
                        name.set("Ben Boeckel")
                    }
                    developer {
                        name.set("Aaron Boxer")
                    }
                    developer {
                        name.set("David Burken")
                    }
                    developer {
                        name.set("Matthieu Darbois")
                    }
                    developer {
                        name.set("Rex Dieter")
                    }
                    developer {
                        name.set("Herve Drolon")
                    }
                    developer {
                        name.set("Antonin Descampe")
                    }
                    developer {
                        name.set("Francois-Olivier Devaux")
                    }
                    developer {
                        name.set("Parvatha Elangovan")
                    }
                    developer {
                        name.set("Jerôme Fimes")
                    }
                    developer {
                        name.set("Bob Friesenhahn")
                    }
                    developer {
                        name.set("Kaori Hagihara")
                    }
                    developer {
                        name.set("Luc Hermitte")
                    }
                    developer {
                        name.set("Luis Ibanez")
                    }
                    developer {
                        name.set("David Janssens")
                    }
                    developer {
                        name.set("Hans Johnson")
                    }
                    developer {
                        name.set("Callum Lerwick")
                    }
                    developer {
                        name.set("Ke Liu")
                    }
                    developer {
                        name.set("Sebastien Lugan")
                    }
                    developer {
                        name.set("Benoit Macq")
                    }
                    developer {
                        name.set("Mathieu Malaterre")
                    }
                    developer {
                        name.set("Julien Malik")
                    }
                    developer {
                        name.set("Arnaud Maye")
                    }
                    developer {
                        name.set("Vincent Nicolas")
                    }
                    developer {
                        name.set("Aleksander Nikolic")
                    }
                    developer {
                        name.set("Glenn Pearson")
                    }
                    developer {
                        name.set("Even Rouault")
                    }
                    developer {
                        name.set("Dzonatas Sol")
                    }
                    developer {
                        name.set("Winfried Szukalski")
                    }
                    developer {
                        name.set("Vincent Torri")
                    }
                    developer {
                        name.set("Yannick Verschueren")
                    }
                    developer {
                        name.set("Peter Wimmer")
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
