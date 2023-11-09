import com.android.ndkports.CMakeCompatibleVersion
import com.android.ndkports.MesonPortTask
import com.android.ndkports.PrefabSysrootPlugin
import org.gradle.jvm.tasks.Jar

val portVersion = "1.0.13"

group = rootProject.group
version = "${portVersion}-beta-2"

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


val buildPort = tasks.register<MesonPortTask>("buildPort") {
    meson { }
}

tasks.prefabPackage {
    version.set(CMakeCompatibleVersion.parse(portVersion))

    licensePath.set("COPYING")

    modules {
        create("fribidi") {
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
                scm {
                    url.set("https://github.com/ViliusSutkus89/ndkports")
                    connection.set("scm:git:https://github.com/ViliusSutkus89/ndkports.git")
                }
                name.set("GNU FriBidi")
                description.set("The Free Implementation of the Unicode Bidirectional Algorithm.")
                url.set("https://github.com/fribidi/fribidi")
                licenses {
                    // Poppler licenses
                    license {
                        name.set("LGPL-2.1")
                        url.set("https://raw.githubusercontent.com/fribidi/fribidi/v1.0.13/COPYING")
                        distribution.set("repo")
                    }
                }
                developers {
                    // Developer list obtained from:
                    // https://github.com/fribidi/fribidi/blob/v1.0.13/README.md#maintainers-and-contributors
                    // https://raw.githubusercontent.com/fribidi/fribidi/v1.0.13/AUTHORS
                    // https://raw.githubusercontent.com/fribidi/fribidi/v1.0.13/THANKS
                    developer {
                        name.set("Dov Grobgeld")
                        email.set("dov.grobgeld@gmail.com")
                    }
                    developer {
                        name.set("Behdad Esfahbod")
                        email.set("behdad@gnu.org")
                    }
                    developer {
                        name.set("Roozbeh Pournader")
                        email.set("roozbeh@gnu.org")
                    }
                    developer {
                        name.set("Khaled Hosny")
                        email.set("khaledhosny@eglug.org")
                    }
                    developer {
                        name.set("Behdad Esfahbod")
                        email.set("behdad@behdad.org")
                    }
                    developer {
                        name.set("Behnam Esfahbod")
                        email.set("behnam@esfahbod.info")
                    }
                    developer {
                        name.set("Tim-Philipp Müller")
                        email.set("tim@centricular.com")
                    }
                    developer {
                        name.set("Tomas Frydrych")
                        email.set("tomas@frydrych.uklinux.net")
                    }
                    developer {
                        name.set("Franck Portaneri")
                    }
                    developer {
                        name.set("Roozbeh Pournader")
                        email.set("roozbeh@sharif.edu")
                    }
                    developer {
                        name.set("Pablo Saratxaga")
                        email.set("pablo@mandrakesoft.com")
                    }
                    developer {
                        name.set("Owen Tayler")
                        email.set("otaylor@redhat.com")
                    }
                    developer {
                        name.set("Omer Zak")
                        email.set("w1@zak.co.il")
                    }
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
