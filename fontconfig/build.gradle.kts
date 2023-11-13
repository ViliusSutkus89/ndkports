import com.android.ndkports.CMakeCompatibleVersion
import com.android.ndkports.MesonPortTask
import com.android.ndkports.PrefabSysrootPlugin
import org.gradle.jvm.tasks.Jar

val portVersion = "2.14.2"

group = rootProject.group
version = "${portVersion}-beta-3"

plugins {
    id("maven-publish")
    id("signing")
    id("com.android.ndkports.NdkPorts")
}

dependencies {
    val ndkVersionSuffix = rootProject.extra.get("ndkVersionSuffix")
    implementation("com.viliussutkus89.ndk.thirdparty:freetype${ndkVersionSuffix}-static:2.13.2-beta-5")
    implementation("com.viliussutkus89.ndk.thirdparty:libpng${ndkVersionSuffix}-static:1.6.40-beta-5")
    implementation("com.viliussutkus89.ndk.thirdparty:libexpat${ndkVersionSuffix}-static:2.5.0-beta-3")
}

ndkPorts {
    ndkPath.set(File(project.findProperty("ndkPath") as String))
    minSdkVersion.set(rootProject.extra.get("minSdkSupportedByNdk").toString().toInt())
    source.set(project.file("${name}-${portVersion}.tar.xz"))
}

tasks.prefab {
    generator.set(PrefabSysrootPlugin::class.java)
}

fun File.replace(oldValue: String, newValue: String, ignoreCase: Boolean = false): File {
    writeText(readText().replace(oldValue, newValue, ignoreCase))
    return this
}

tasks.extractSrc {
    doLast {
        outDir.get().asFile.resolve("fonts.conf.in").replace("<cachedir>@FC_CACHEDIR@</cachedir>", "")
    }
}

tasks.register<MesonPortTask>("buildPort") {
    meson { }

    doLast {
        // @TODO: verify other ABIs have matching assets
        val dst = project.buildDir.resolve("assets/fontconfig").apply { mkdirs() }
        val iDir = installDirectoryFor(com.android.ndkports.Abi.Arm)
        listOf(iDir.resolve("etc"), iDir.resolve("share")).forEach {
            it.copyRecursively(dst.resolve(it.name)) { file, exception ->
                if (exception !is FileAlreadyExistsException) {
                    throw exception
                }
                if (!file.readBytes().contentEquals(exception.file.readBytes())) {
                    throw exception
                }
                OnErrorAction.SKIP
            }
        }
    }
}

tasks.prefabPackage {
    version.set(CMakeCompatibleVersion.parse(portVersion))

    licensePath.set("COPYING")

    dependencies.set(mapOf(
        "libexpat" to "1",
        "freetype" to "1",
        "libpng" to "1",
    ))

    modules {
        create("fontconfig") {
            static.set(project.findProperty("libraryType") == "static")
            dependencies.set(listOf(
                "//freetype:freetype",
                "//libpng:png16",
                "//libexpat:expat",
            ))
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
                name.set("Fontconfig")
                description.set("Fontconfig is a library for configuring and customizing font access.")
                url.set("https://www.freedesktop.org/wiki/Software/fontconfig/")
                licenses {
                    // Poppler licenses
                    license {
                        name.set("MIT")
                        url.set("https://gitlab.freedesktop.org/fontconfig/fontconfig/-/raw/2.14.2/COPYING")
                        distribution.set("repo")
                    }
                }
                developers {
                    // Developer list obtained from:
                    // https://gitlab.freedesktop.org/fontconfig/fontconfig/-/raw/2.14.2/AUTHORS
                    developer {
                        name.set("Keith Packard")
                        email.set("keithp@keithp.com")
                    }
                    developer {
                        name.set("Patrick Lam")
                        email.set("plam@mit.edu")
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
