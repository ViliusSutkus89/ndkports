import com.android.ndkports.AutoconfPortTask
import com.android.ndkports.CMakeCompatibleVersion
import com.android.ndkports.PrefabSysrootPlugin
import org.gradle.jvm.tasks.Jar

val portVersion = "1.17"

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
    source.set(project.file("${name}-${portVersion}.tar.gz"))
}

tasks.prefab {
    generator.set(PrefabSysrootPlugin::class.java)
}

tasks.register<AutoconfPortTask>("buildPort") {
    autoconf { }
}

tasks.prefabPackage {
    version.set(CMakeCompatibleVersion.parse(portVersion))

    licensePath.set("COPYING.LIB")

    modules {
        create("iconv") {
            static.set(project.findProperty("libraryType") == "static")
        }
        create("charset") {
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
                name.set("iconv")
                description.set("This library provides an iconv() implementation, for use on systems which don't have one, or whose implementation cannot convert from/to Unicode.")
                url.set("https://www.gnu.org/software/libiconv/")
                licenses {
                    license {
                        name.set("LGPL-2.1")
                        url.set("https://www.gnu.org/licenses/lgpl-2.1.html")
                        distribution.set("repo")
                    }
                }
                developers {
                    // Developer list obtained from:
                    // https://git.savannah.gnu.org/gitweb/?p=libiconv.git;a=blob_plain;f=AUTHORS;hb=refs/tags/v1.17
                    // https://git.savannah.gnu.org/gitweb/?p=libiconv.git;a=blob_plain;f=THANKS;hb=refs/tags/v1.17
                    developer {
                        name.set("Bruno Haible")
                        email.set("bruno@clisp.org")
                    }
                    developer {
                        name.set("Edmund Grimley Evans")
                        email.set("edmundo@rano.org")
                    }
                    developer {
                        name.set("Taro Muraoka")
                        email.set("koron@tka.att.ne.jp")
                    }
                    developer {
                        name.set("Akira Hatakeyama")
                        email.set("akira@sra.co.jp")
                    }
                    developer {
                        name.set("Juan Manuel Guerrero")
                        email.set("st001906@hrz1.hrz.tu-darmstadt.de")
                    }
                    developer {
                        name.set("Hironori Sakamoto")
                        email.set("hsaka@mth.biglobe.ne.jp")
                    }
                    developer {
                        name.set("Ken Lunde")
                        email.set("lunde@adobe.com")
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
