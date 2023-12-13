import com.android.ndkports.AutoconfPortTask
import com.android.ndkports.CMakeCompatibleVersion
import com.android.ndkports.PrefabSysrootPlugin
import org.gradle.jvm.tasks.Jar

val portVersion = "0.8.1"

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
    source.set(project.file("v${portVersion}.tar.gz"))
}

fun File.patch(patch: String) {
    patch(projectDir.resolve("patches/$portVersion").resolve(patch))
}

fun File.patch(patch: File) {
    val pb = ProcessBuilder(
        if (isFile) listOf("patch", "--ignore-whitespace", "-p0", absolutePath)
        else listOf("patch", "--ignore-whitespace", "-p0")
    )

    if (isDirectory)
        pb.directory(absoluteFile)

    val process = pb.start()
    process.outputStream.writer().use {
        it.write(patch.readText())
    }
    process.errorStream.bufferedReader().use {
        println(it.readText())
    }
    process.inputStream.bufferedReader().use {
        println(it.readText())
    }
    if (process.waitFor() != 0) {
        throw RuntimeException("Patch failed!\n")
    }
}

tasks.extractSrc {
    doLast {
        val srcDir = outDir.get().asFile

        val targetConfigure = srcDir.resolve("configure")
        projectDir.resolve("patches/$portVersion/configure").copyTo(
            target = targetConfigure,
            overwrite = true
        )
        targetConfigure.setExecutable(true)
        srcDir.resolve("Make.inc").patch("no-hard-float.patch")
        srcDir.resolve("Make.inc").patch("i387-long-double-not-double.patch")
        srcDir.resolve("openlibm.pc.in").patch("pcfile-includesubdir.patch")
    }
}

tasks.prefab {
    generator.set(PrefabSysrootPlugin::class.java)
}

tasks.register<AutoconfPortTask>("buildPort") {
    autoconf { }
}

tasks.prefabPackage {
    version.set(CMakeCompatibleVersion.parse(portVersion))

    licensePath.set("LICENSE.md")

    modules {
        create("openlibm") {
            static.set(project.findProperty("libraryType") == "static")
            includesPerAbi.set(true)
        }
    }
}

val packageSources = tasks.register<Jar>("packageSources") {
    archiveClassifier.set("sources")
    from(projectDir.resolve("build.gradle.kts"))
    from(ndkPorts.source)
    from(projectDir.resolve("patches/$portVersion"))
}

publishing {
    publications {
        create<MavenPublication>("release") {
            from(components["prefab"])
            artifactId += rootProject.extra.get("ndkVersionSuffix")
            artifactId += rootProject.extra.get("libraryTypeSuffix")
            artifact(packageSources)
            pom {
                name.set("OpenLibm")
                description.set("OpenLibm is an effort to have a high quality, portable, standalone C mathematical library (libm)")
                url.set("https://openlibm.org")
                licenses {
                    license {
                        name.set("OpenLibm")
                        url.set("https://raw.githubusercontent.com/JuliaMath/openlibm/v0.8.1/LICENSE.md")
                        distribution.set("repo")
                    }
                }
                developers {
                    // Developer list obtained from:
                    // https://raw.githubusercontent.com/JuliaMath/openlibm/v0.8.1/.mailmap
                    developer {
                        name.set("JuliaLang")
                    }
                    developer {
                        name.set("Jeff Bezanson")
                    }
                    developer {
                        name.set("Stefan Karpinski")
                    }
                    developer {
                        name.set("Viral B. Shah")
                    }
                    developer {
                        name.set("George Xing")
                    }
                    developer {
                        name.set("Stephan Boyer")
                    }
                    developer {
                        name.set("Giuseppe Zingales")
                    }
                    developer {
                        name.set("Jameson Nash")
                    }
                    developer {
                        name.set("Alan Edelman")
                    }
                    developer {
                        name.set("PlayMyCode")
                    }
                    developer {
                        name.set("Corey M. Hoffstein")
                    }
                    developer {
                        name.set("Stefan Kroboth")
                    }
                    developer {
                        name.set("Tim Holy")
                    }
                    developer {
                        name.set("Patrick O'Leary")
                    }
                    developer {
                        name.set("Ivan Mantova")
                    }
                    developer {
                        name.set("Keno Fischer")
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
