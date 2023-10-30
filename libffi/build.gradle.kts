import com.android.ndkports.AutoconfPortTask
import com.android.ndkports.CMakeCompatibleVersion
import com.android.ndkports.PrefabSysrootPlugin
import org.gradle.jvm.tasks.Jar

val portVersion = "3.4.4"

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

fun File.patch(patch: File) {
    val pb = ProcessBuilder(
        if (isFile) listOf("patch", "-p0", absolutePath)
        else listOf("patch", "-p0")
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
        when (portVersion) {
            "3.4.4" -> {
                outDir.get().asFile.apply {
                    // https://github.com/libffi/libffi/pull/800
                    patch(projectDir.resolve("patches/0001-Put-optional-symbols-behind-ifdefs-800.patch"))

                    // https://github.com/libffi/libffi/issues/760
                    // https://github.com/libffi/libffi/issues/764
                    patch(projectDir.resolve("patches/0002-Forward-declare-open_temp_exec_file-764.patch"))

                    patch(projectDir.resolve("patches/0003-longdouble.patch"))
                }
            }
        }
    }
}

tasks.register<AutoconfPortTask>("buildPort") {
    autoconf { }
}

tasks.prefabPackage {
    version.set(CMakeCompatibleVersion.parse(portVersion))

    modules {
        create("ffi") {
            static.set(project.findProperty("libraryType") == "static")
            includesPerAbi.set(true)
        }
    }
}

val packageSources = tasks.register<Jar>("packageSources") {
    archiveClassifier.set("sources")
    from(projectDir.resolve("build.gradle.kts"))
    from(projectDir.resolve("patches"))
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
                name.set("libffi")
                description.set("libffi is a foreign function interface library. It provides a C programming language interface for calling natively compiled functions given information about the target function at run time instead of compile time. It also implements the opposite functionality: libffi can produce a pointer to a function that can accept and decode any combination of arguments defined at run time.")
                url.set("http://sourceware.org/libffi")
                licenses {
                    license {
                        name.set("MIT")
                        url.set("https://raw.githubusercontent.com/libffi/libffi/v3.4.4/LICENSE")
                        distribution.set("repo")
                    }
                    license {
                        name.set("GPLv2")
                        url.set("https://raw.githubusercontent.com/libffi/libffi/v3.4.4/LICENSE-BUILDTOOLS")
                        distribution.set("repo")
                    }
                }
                developers {
                    // Developer list obtained from:
                    // https://sourceware.org/libffi/
                    developer {
                        name.set("Anthony Green")
                        email.set("green@moxielogic.com")
                    }
                    developer {
                        name.set("Gianni Mariani")
                    }
                    developer {
                        name.set("Kresten Krab Thorup")
                    }
                    developer {
                        name.set("Marcus Shawcroft")
                    }
                    developer {
                        name.set("James Greenhalgh")
                    }
                    developer {
                        name.set("Richard Henderson")
                    }
                    developer {
                        name.set("Hackers at Synopsis")
                    }
                    developer {
                        name.set("Raffaele Sena")
                    }
                    developer {
                        name.set("Bradley Smith")
                    }
                    developer {
                        name.set("Alexandre Keunecke I. de Mendonca")
                    }
                    developer {
                        name.set("Simon Posnjak")
                    }
                    developer {
                        name.set("Hans-Peter Nilsson")
                    }
                    developer {
                        name.set("Ma Jun")
                    }
                    developer {
                        name.set("Zhang Wenmeng")
                    }
                    developer {
                        name.set("Hans Boehm")
                    }
                    developer {
                        name.set("Yann Sionneau")
                    }
                    developer {
                        name.set("Kazuhiro Inaoka")
                    }
                    developer {
                        name.set("Andreas Schwab")
                    }
                    developer {
                        name.set("Miod Vallat")
                    }
                    developer {
                        name.set("Hackers at Imagination Technologies")
                    }
                    developer {
                        name.set("Nathan Rossi")
                    }
                    developer {
                        name.set("Casey Marshall")
                    }
                    developer {
                        name.set("David Daney")
                    }
                    developer {
                        name.set("Sandra Loosemore")
                    }
                    developer {
                        name.set("Sebastian Macke")
                    }
                    developer {
                        name.set("Randolph Chung")
                    }
                    developer {
                        name.set("Dave Anglin")
                    }
                    developer {
                        name.set("Andreas Tobler")
                    }
                    developer {
                        name.set("Geoffrey Keating")
                    }
                    developer {
                        name.set("David Edelsohn")
                    }
                    developer {
                        name.set("John Hornkvist")
                    }
                    developer {
                        name.set("Jakub Jelinek")
                    }
                    developer {
                        name.set("Michael Knyszek")
                    }
                    developer {
                        name.set("Andrew Waterman")
                    }
                    developer {
                        name.set("Stef O'Rear")
                    }
                    developer {
                        name.set("Gerhard Tonn")
                    }
                    developer {
                        name.set("Ulrich Weigand")
                    }
                    developer {
                        name.set("Kaz Kojima")
                    }
                    developer {
                        name.set("Gordon Irlam")
                    }
                    developer {
                        name.set("Walter Lee")
                    }
                    developer {
                        name.set("Jon Beniston")
                    }
                    developer {
                        name.set("Bo Thorsen")
                    }
                    developer {
                        name.set("Chris Zankel")
                    }
                    developer {
                        name.set("Jesper Skov")
                    }
                    developer {
                        name.set("Andrew Haley")
                    }
                    developer {
                        name.set("Tom Tromey")
                    }
                    developer {
                        name.set("Jim Blandy")
                    }
                    developer {
                        name.set("Andreas Tobler")
                    }
                    developer {
                        name.set("Alex Oliva")
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
