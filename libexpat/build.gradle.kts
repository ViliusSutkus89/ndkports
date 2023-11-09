import com.android.ndkports.AutoconfPortTask
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
    source.set(project.file("expat-${portVersion}.tar.xz"))
}

tasks.prefab {
    generator.set(PrefabSysrootPlugin::class.java)
}

tasks.register<AutoconfPortTask>("buildPort") {
    autoconf { }
}

tasks.prefabPackage {
    version.set(CMakeCompatibleVersion.parse(portVersion))

    licensePath.set("COPYING")

    modules {
        create("expat") {
            static.set(project.findProperty("libraryType") == "static")
            includesPerAbi.set(true)
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
                name.set("libexpat")
                description.set("Expat is a stream-oriented XML 1.0 parser library, written in C.")
                url.set("https://github.com/libexpat/libexpat")
                licenses {
                    license {
                        name.set("MIT")
                        url.set("https://raw.githubusercontent.com/libexpat/libexpat/R_2_5_0/COPYING")
                        distribution.set("repo")
                    }
                }
                developers {
                    // Developer list obtained from:
                    // https://raw.githubusercontent.com/libexpat/libexpat/R_2_5_0/expat/AUTHORS
                    developer {
                        name.set("Clark Cooper")
                    }
                    developer {
                        name.set("Fred L. Drake, Jr.")
                    }
                    developer {
                        name.set("Greg Stein")
                    }
                    developer {
                        name.set("James Clark")
                    }
                    developer {
                        name.set("Karl Waclawek")
                    }
                    developer {
                        name.set("Rhodri James")
                    }
                    developer {
                        name.set("Sebastian Pipping")
                    }
                    developer {
                        name.set("Steven Solie")
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
