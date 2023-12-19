import com.android.ndkports.CMakePortTask
import com.android.ndkports.CMakeCompatibleVersion
import com.android.ndkports.PrefabSysrootPlugin
import org.gradle.jvm.tasks.Jar

val portVersion = "10.42"

group = rootProject.group
version = "${portVersion}-beta-4"

plugins {
    id("maven-publish")
    id("signing")
    id("com.android.ndkports.NdkPorts")
}

ndkPorts {
    ndkPath.set(File(project.findProperty("ndkPath") as String))
    minSdkVersion.set(rootProject.extra.get("minSdkSupportedByNdk").toString().toInt())
    source.set(project.file("${name}-${portVersion}.tar.bz2"))
}

tasks.prefab {
    generator.set(PrefabSysrootPlugin::class.java)
}

tasks.register<CMakePortTask>("buildPort") {
    cmake {
        args(
            "-DPCRE2_STATIC_PIC=ON",
            "-DPCRE2_SUPPORT_JIT=ON",
        )
    }
}

tasks.prefabPackage {
    version.set(CMakeCompatibleVersion.parse(portVersion))

    licensePath.set("LICENCE")

    modules {
        create("pcre2-8") {
            static.set(project.findProperty("libraryType") == "static")
        }
        create("pcre2-posix") {
            static.set(project.findProperty("libraryType") == "static")
            dependencies.set(listOf(":pcre2-8"))
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
                name.set("PCRE2")
                description.set("Perl-Compatible Regular Expressions")
                url.set("https://github.com/PCRE2Project/pcre2")
                licenses {
                    license {
                        name.set("BSD")
                        url.set("https://raw.githubusercontent.com/PCRE2Project/pcre2/pcre2-10.42/LICENCE")
                        distribution.set("repo")
                    }
                }
                developers {
                    // Developer list obtained from:
                    // https://raw.githubusercontent.com/PCRE2Project/pcre2/pcre2-10.42/AUTHORS
                    // Emails not included, because AUTHORS file tries to obfuscate them.
                    developer {
                        name.set("Philip Hazel")
                    }
                    developer {
                        name.set("Zoltan Herczeg")
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
