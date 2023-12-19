import com.android.ndkports.AutoconfPortTask
import com.android.ndkports.CMakeCompatibleVersion
import com.android.ndkports.PrefabSysrootPlugin
import org.gradle.jvm.tasks.Jar

val portVersion = "20230916"

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
    source.set(project.file("${name}-dist-${portVersion}.tar.gz"))
}

tasks.prefab {
    generator.set(PrefabSysrootPlugin::class.java)
}

tasks.register<AutoconfPortTask>("buildPort") {
    autoconf { }
}

tasks.prefabPackage {
    version.set(CMakeCompatibleVersion.parse(portVersion))

    licensePath.set("LICENSE")

    modules {
        create("uninameslist") {
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
                name.set("libuninameslist")
                description.set("A library with a large (sparse) array mapping each unicode code point to the annotation data for it provided in http://www.unicode.org/Public/UNIDATA/NamesList.txt")
                url.set("https://github.com/fontforge/libuninameslist")
                licenses {
                    license {
                        name.set("BSD")
                        url.set("https://raw.githubusercontent.com/fontforge/libuninameslist/20230916/LICENSE")
                        distribution.set("repo")
                    }
                    license {
                        name.set("GPLv2")
                        url.set("https://raw.githubusercontent.com/fontforge/libuninameslist/20230916/COPYING")
                        distribution.set("repo")
                    }
                }
                developers {
                    // Developer list obtained from:
                    // https://raw.githubusercontent.com/fontforge/libuninameslist/20230916/AUTHORS
                    developer {
                        name.set("George Williams")
                    }
                    developer {
                        name.set("Khaled Hosny")
                        id.set("khaledhosny")
                    }
                    developer {
                        name.set("Dave Crossland")
                        id.set("davelab6")
                    }
                    developer {
                        name.set("Joe Da Silva")
                        id.set("JoesCat")
                    }
                    developer {
                        name.set("Hideki Yamane")
                        id.set("henrich")
                    }
                    developer {
                        name.set("Werner Lemberg")
                        id.set("lemzwerg")
                    }
                    developer {
                        name.set("Grégory Chatel")
                        id.set("rodgzilla")
                    }
                    developer {
                        name.set("Phil Krylov")
                        id.set("tuffnatty")
                    }
                    developer {
                        name.set("Shriramana Sharma")
                        id.set("jamadagni")
                    }
                    developer {
                        name.set("Jeremy Tan")
                        id.set("jtanx")
                    }
                    developer {
                        name.set("David Corbett")
                        id.set("dscorbett")
                    }
                    developer {
                        id.set("genisysram")
                    }
                    developer {
                        id.set("orbea")
                    }
                    developer {
                        name.set("Naohiro Aota")
                    }
                    developer {
                        name.set("Biswapriyo Nath")
                        id.set("Biswa96")
                    }
                    developer {
                        name.set("Jacques André")
                    }
                    developer {
                        name.set("Patrick Andries")
                    }
                    developer {
                        name.set("Bernard Chauvois")
                    }
                    developer {
                        name.set("Karljürgen Feuerherm")
                    }
                    developer {
                        name.set("Alain LaBonté")
                    }
                    developer {
                        name.set("Marc Lodewijck")
                    }
                    developer {
                        name.set("Michel Suignard")
                    }
                    developer {
                        name.set("François Yergeau")
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
