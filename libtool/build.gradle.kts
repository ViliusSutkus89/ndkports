import com.android.ndkports.AutoconfPortTask
import com.android.ndkports.CMakeCompatibleVersion
import com.android.ndkports.PrefabSysrootPlugin
import org.gradle.jvm.tasks.Jar

val portVersion = "2.4.6"

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
    source.set(project.file("${name}-${portVersion}.tar.xz"))
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
        create("ltdl") {
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
                name.set("GNU Libtool")
                description.set("GNU Libtool is a generic library support script that hides the complexity of using shared libraries behind a consistent, portable interface.")
                url.set("https://www.gnu.org/software/libtool/")
                licenses {
                    license {
                        name.set("GPLv2-or-later")
                        url.set("http://git.savannah.gnu.org/cgit/libtool.git/tree/AUTHORS?h=v2.4.6")
                        distribution.set("repo")
                    }
                }
                developers {
                    // Developer list obtained from:
                    // http://git.savannah.gnu.org/cgit/libtool.git/tree/AUTHORS?h=v2.4.6
                    developer {
                        name.set("Gordon Matzigkeit")
                        email.set("gord@gnu.org")
                    }
                    developer {
                        name.set("Thomas Tanner")
                        email.set("tanner@ffii.org")
                    }
                    developer {
                        name.set("Alexandre Oliva")
                        email.set("oliva@dcc.unicamp.br")
                    }
                    developer {
                        name.set("Ossama Othman")
                        email.set("ossama@debian.org")
                    }
                    developer {
                        name.set("Robert Boehne")
                        email.set("rboehne@ricardo-us.com")
                    }
                    developer {
                        name.set("Scott James Remnant")
                        email.set("scott@netsplit.com")
                    }
                    developer {
                        name.set("Peter O'Gorman")
                        email.set("peter@pogma.com")
                    }
                    developer {
                        name.set("Ralf Wildenhues")
                        email.set("Ralf.Wildenhues@gmx.de")
                    }
                    developer {
                        name.set("Gary V. Vaughan")
                        email.set("gary@gnu.org")
                    }
                    developer {
                        name.set("Bob Friesenhahn")
                        email.set("bfriesen@simple.dallas.tx.us")
                    }
                    developer {
                        name.set("Peter Rosin")
                        email.set("peda@lysator.liu.se")
                    }
                    developer {
                        name.set("Noah Misch")
                        email.set("noah@cs.caltech.edu")
                    }
                    developer {
                        name.set("Charles Wilson")
                        email.set("libtool@cwilson.fastmail.fm")
                    }
                    developer {
                        name.set("Brooks Moses")
                        email.set("bmoses@google.com")
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
