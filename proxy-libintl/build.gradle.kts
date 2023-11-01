import com.android.ndkports.MesonPortTask
import com.android.ndkports.CMakeCompatibleVersion
import com.android.ndkports.PrefabSysrootPlugin
import org.gradle.jvm.tasks.Jar

val portVersion = "0.4.1"

group = rootProject.group
version = portVersion

plugins {
    id("maven-publish")
    id("signing")
    id("com.android.ndkports.NdkPorts")
}

ndkPorts {
    ndkPath.set(File(project.findProperty("ndkPath") as String))
    source.set(project.file("${name}-${portVersion}.tar.gz"))
    minSdkVersion.set(rootProject.extra.get("minSdkSupportedByNdk").toString().toInt())
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

    modules {
        create("intl") {
            includesPerAbi.set(true)
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
                name.set("proxy-libintl")
                description.set("This is a trivial minimal library intended to act as a proxy for a dynamically loaded optional libintl.")
                url.set("https://github.com/ViliusSutkus89/proxy-libintl")
                licenses {
                    license {
                        name.set("GPLv2")
                        url.set("https://raw.githubusercontent.com/ViliusSutkus89/proxy-libintl/0.2/COPYING")
                        distribution.set("repo")
                    }
                }
                developers {
                    // Developer list obtained from:
                    // https://github.com/ViliusSutkus89/proxy-libintl/blob/0.4.1/README.md
                    // https://github.com/ViliusSutkus89/proxy-libintl/graphs/contributors
                    developer {
                        name.set("Tor Lillqvist")
                        email.set("tml@iki.fi")
                    }
                    developer {
                        name.set("Geoffrey Wossum")
                    }
                    developer {
                        name.set("Nirbheek Chauhan")
                        id.set("nirbheek")
                    }
                    developer {
                        name.set("Amos Wenger")
                        id.set("fasterthanlime")
                    }
                    developer {
                        name.set("Aleksandr Mezin")
                        id.set("amezin")
                    }
                    developer {
                        name.set("Ole André Vadla Ravnås")
                        id.set("oleavr")
                    }
                    developer {
                        name.set("Vilius Sutkus '89")
                        id.set("ViliusSutkus89")
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
