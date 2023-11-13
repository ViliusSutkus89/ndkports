import com.android.ndkports.MesonPortTask
import com.android.ndkports.CMakeCompatibleVersion
import com.android.ndkports.PrefabSysrootPlugin
import org.gradle.jvm.tasks.Jar

val portVersion = "2.15"

group = rootProject.group
version = "${portVersion}-beta-4"

plugins {
    id("maven-publish")
    id("signing")
    id("com.android.ndkports.NdkPorts")
}

dependencies {
    val ndkVersionSuffix = rootProject.extra.get("ndkVersionSuffix")
    implementation("com.viliussutkus89.ndk.thirdparty:libjpeg-turbo${ndkVersionSuffix}-static:3.0.1-beta-2")
    implementation("com.viliussutkus89.ndk.thirdparty:libtiff${ndkVersionSuffix}-static:4.6.0-beta-4")
}

ndkPorts {
    ndkPath.set(File(project.findProperty("ndkPath") as String))
    minSdkVersion.set(rootProject.extra.get("minSdkSupportedByNdk").toString().toInt())
    source.set(project.file("${name}-${portVersion}.tar.gz"))
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

    dependencies.set(mapOf(
        "libjpeg-turbo" to "1",
        "libtiff" to "1",
    ))

    modules {
        create("lcms2") {
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
                name.set("Little CMS color engine")
                description.set("Little CMS or LCMS is an open-source color management system, released as a software library for use in other programs which will allow the use of International Color Consortium profiles.")
                url.set("https://www.littlecms.com/color-engine/")
                licenses {
                    license {
                        name.set("MIT")
                        url.set("https://github.com/mm2/Little-CMS/blob/lcms2.15/COPYING")
                        distribution.set("repo")
                    }
                }
                developers {
                    // Developer list obtained from:
                    // https://raw.githubusercontent.com/mm2/Little-CMS/lcms2.15/AUTHORS
                    developer {
                        name.set("Marti Maria")
                    }
                    developer {
                        name.set("Bob Friesenhahn")
                    }
                    developer {
                        name.set("Kai-Uwe Behrmann")
                    }
                    developer {
                        name.set("Stuart Nixon")
                    }
                    developer {
                        name.set("Jordi Vilar")
                    }
                    developer {
                        name.set("Richard Hughes")
                    }
                    developer {
                        name.set("Auke Nauta")
                    }
                    developer {
                        name.set("Chris Evans")
                    }
                    developer {
                        name.set("Lorenzo Ridolfi")
                    }
                    developer {
                        name.set("Robin Watts")
                    }
                    developer {
                        name.set("Shawn Pedersen")
                    }
                    developer {
                        name.set("Andrew Brygin")
                    }
                    developer {
                        name.set("Samuli Suominen")
                    }
                    developer {
                        name.set("Florian Höch")
                    }
                    developer {
                        name.set("Aurelien Jarno")
                    }
                    developer {
                        name.set("Claudiu Cebuc")
                    }
                    developer {
                        name.set("Michael Vhrel")
                    }
                    developer {
                        name.set("Michal Cihar")
                    }
                    developer {
                        name.set("Daniel Kaneider")
                    }
                    developer {
                        name.set("Mateusz Jurczyk")
                    }
                    developer {
                        name.set("Paul Miller")
                    }
                    developer {
                        name.set("Sébastien Léon")
                    }
                    developer {
                        name.set("Christian Schmitz")
                    }
                    developer {
                        name.set("XhmikosR")
                    }
                    developer {
                        name.set("Stanislav Brabec")
                    }
                    developer {
                        name.set("Leonhard Gruenschloss")
                    }
                    developer {
                        name.set("Patrick Noffke")
                    }
                    developer {
                        name.set("Christopher James Halse Rogers")
                    }
                    developer {
                        name.set("John Hein")
                    }
                    developer {
                        name.set("Thomas Weber")
                    }
                    developer {
                        name.set("Mark Allen")
                    }
                    developer {
                        name.set("Noel Carboni")
                    }
                    developer {
                        name.set("Sergei Trofimovic")
                    }
                    developer {
                        name.set("Philipp Knechtges")
                    }
                    developer {
                        name.set("Amyspark")
                    }
                    developer {
                        name.set("Lovell Fuller")
                    }
                    developer {
                        name.set("Eli Schwartz")
                    }
                    developer {
                        name.set("Artifex software")
                    }
                    developer {
                        name.set("AlienSkin software")
                    }
                    developer {
                        name.set("libVIPS")
                    }
                    developer {
                        name.set("Jan Morovic")
                    }
                    developer {
                        name.set("Jos Vernon")
                    }
                    developer {
                        name.set("Harald Schneider")
                    }
                    developer {
                        name.set("Christian Albrecht")
                    }
                    developer {
                        name.set("Dimitrios Anastassakis")
                    }
                    developer {
                        name.set("Lemke Software")
                    }
                    developer {
                        name.set("Tim Zaman")
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
