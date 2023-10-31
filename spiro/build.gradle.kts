import com.android.ndkports.AutoconfPortTask
import com.android.ndkports.CMakeCompatibleVersion
import com.android.ndkports.PrefabSysrootPlugin
import org.gradle.jvm.tasks.Jar

val portVersion = "20221101"

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
    source.set(project.file("lib${name}-dist-${portVersion}.tar.gz"))
}

tasks.prefab {
    generator.set(PrefabSysrootPlugin::class.java)
}

tasks.register<AutoconfPortTask>("buildPort") {
    autoconf { }
}

fun File.replace(oldValue: String, newValue: String, ignoreCase: Boolean = false): File {
    writeText(readText().replace(oldValue, newValue, ignoreCase))
    return this
}

tasks.extractSrc {
    doLast {
        // No libthread.so on Android. Threads are available by default, no need to link against it
        outDir.get().asFile.resolve("tests/Makefile.in").replace("@WANTPTHREADS_TRUE@am__append_1 = -lpthread", "")
    }
}

tasks.prefabPackage {
    version.set(CMakeCompatibleVersion.parse(portVersion))

    licensePath.set("COPYING")

    modules {
        create("spiro") {
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
                name.set("Spiro")
                description.set("A library for curve design. Clothoid to bezier conversion. A mechanism for drawing smooth contours with constant curvature at the spline joins.")
                url.set("https://github.com/fontforge/libspiro")
                licenses {
                    license {
                        name.set("GPLv3")
                        url.set("https://raw.githubusercontent.com/fontforge/libspiro/20221101/COPYING")
                        distribution.set("repo")
                    }
                }
                developers {
                    // Developer list obtained from:
                    // https://raw.githubusercontent.com/fontforge/libspiro/20221101/AUTHORS
                    developer {
                        name.set("Raph Levien")
                        email.set("raph.levien@gmail.com")
                    }
                    developer {
                        name.set("George Williams")
                        email.set("gww@silcom.com")
                    }
                    developer {
                        name.set("Joe Da Silva")
                    }
                    developer {
                        name.set("Dave Crossland")
                    }
                    developer {
                        name.set("Shriramana Sharma")
                    }
                    developer {
                        name.set("Horváth Balázs")
                    }
                    developer {
                        name.set("Luigi Scarso")
                    }
                    developer {
                        name.set("Jeremy Tan")
                    }
                    developer {
                        name.set("Wiesław Šoltés")
                    }
                    developer {
                        name.set("Mingye Wang")
                    }
                    developer {
                        name.set("Frederic Cambus")
                    }
                    developer {
                        name.set("Fredrick Brennan")
                    }
                    developer {
                        name.set("C.W. Betts")
                    }
                    developer {
                        id.set("orbea")
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
