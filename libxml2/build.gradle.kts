import com.android.ndkports.AutoconfPortTask
import com.android.ndkports.CMakeCompatibleVersion
import com.android.ndkports.PrefabSysrootPlugin
import org.gradle.jvm.tasks.Jar

val portVersion = "2.11.5"

group = rootProject.group
version = "${portVersion}-beta-2"

plugins {
    id("maven-publish")
    id("signing")
    id("com.android.ndkports.NdkPorts")
}

dependencies {
    val ndkVersionSuffix = rootProject.extra.get("ndkVersionSuffix")
    implementation("com.viliussutkus89.ndk.thirdparty:libiconv${ndkVersionSuffix}-static:1.17-beta-2")
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
    val generatedDependencies = prefabGenerated.get().asFile
    autoconf {
        // can't find iconv
        generatedDependencies.resolve(toolchain.abi.triple).let {
            env["CFLAGS"] = "-I${it.resolve("include")}"
            env["CXXFLAGS"] = "-I${it.resolve("include")}"
            env["LDFLAGS"] = "-L${it.resolve("lib")}"
        }
        arg("--without-python")
    }
}

tasks.prefabPackage {
    version.set(CMakeCompatibleVersion.parse(portVersion))

    licensePath.set("Copyright")

    modules {
        create("xml2") {
            if (project.findProperty("libraryType") == "static") {
                static.set(true)
                dependencies.set(listOf("z", "//libiconv:iconv", "m"))
            } else {
                dependencies.set(listOf("//libiconv:iconv"))
            }
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
                name.set("libxml2")
                description.set("The XML C parser and toolkit of Gnome.")
                url.set("https://gitlab.gnome.org/GNOME/libxml2")
                licenses {
                    license {
                        name.set("MIT")
                        url.set("https://gitlab.gnome.org/GNOME/libxml2/-/raw/v2.11.5/Copyright")
                        distribution.set("repo")
                    }
                }
                developers {
                    // Developer list obtained from Authors section of
                    // https://gitlab.gnome.org/GNOME/libxml2/-/raw/v2.11.5/README.md
                    developer {
                        name.set("Daniel Veillard")
                    }
                    developer {
                        name.set("Bjorn Reese")
                    }
                    developer {
                        name.set("William Brack")
                    }
                    developer {
                        name.set("Igor Zlatkovic")
                    }
                    developer {
                        name.set("Aleksey Sanin")
                    }
                    developer {
                        name.set("Nick Wellnhofer")
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
