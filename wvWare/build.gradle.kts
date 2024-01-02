import com.android.ndkports.AutoconfPortTask
import com.android.ndkports.CMakeCompatibleVersion
import com.android.ndkports.PrefabSysrootPlugin
import org.gradle.jvm.tasks.Jar

val portVersion = "1.2.9"

group = rootProject.group
version = "${portVersion}-beta-1"

plugins {
    id("maven-publish")
    id("signing")
    id("com.android.ndkports.NdkPorts")
}

dependencies {
    val ndkVersionSuffix = rootProject.extra.get("ndkVersionSuffix")
    val dependencyLibraryTypeSuffix = rootProject.extra.get("dependencyLibraryTypeSuffix")
    implementation("com.viliussutkus89.ndk.thirdparty:libgsf${ndkVersionSuffix}${dependencyLibraryTypeSuffix}:1.14.51-beta-1")
    implementation("com.viliussutkus89.ndk.thirdparty:libpng${ndkVersionSuffix}${dependencyLibraryTypeSuffix}:1.6.40-beta-7")
    implementation("com.viliussutkus89.ndk.thirdparty:libwmf${ndkVersionSuffix}${dependencyLibraryTypeSuffix}:0.2.13-beta-1")
}

ndkPorts {
    ndkPath.set(File(project.findProperty("ndkPath") as String))
    minSdkVersion.set(rootProject.extra.get("minSdkSupportedByNdk").toString().toInt())
    source.set(project.file("wv-${portVersion}.tar.gz"))
}

tasks.prefab {
    generator.set(PrefabSysrootPlugin::class.java)
}

fun File.replace(oldValue: String, newValue: String, ignoreCase: Boolean = false): File {
    writeText(readText().replace(oldValue, newValue, ignoreCase))
    return this
}

fun File.patch(patch: String): File {
    patch(projectDir.resolve("patches/$portVersion").resolve(patch))
    return this
}

fun File.patch(patch: File): File {
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
    return this
}

tasks.extractSrc {
    doLast {
        val srcDir = outDir.get().asFile

        srcDir.resolve("GNUmakefile.am").patch("skip-programs.patch")
        srcDir.resolve("wv.h").patch("printf-redirect.patch")

        srcDir.resolve("config.sub").delete()
        val pb = ProcessBuilder(srcDir.resolve("autogen.sh").absolutePath).directory(srcDir)
        pb.environment()["NOCONFIGURE"] = "1"
        if (pb.start().waitFor() != 0)
            throw RuntimeException("autogen.sh failed!")
    }
}

val buildTask = tasks.register<AutoconfPortTask>("buildPort") {
    val generatedDependencies = prefabGenerated.get().asFile
    val isStatic = project.findProperty("libraryType") == "static"
    autoconf {
        val generated = generatedDependencies.resolve(toolchain.abi.triple)
        arg("--with-libwmf=$generated")
        env["ac_cv_path_LIBWMF_CONFIG"] = "pkg-config libwmf"
        if (isStatic)
            env["ac_cv_path_LIBWMF_CONFIG"] += " --static"

        // wvWare can't find libpng
        generated.let {
            env["CFLAGS"] = "-I${generated.resolve("include")}"
            env["LDFLAGS"] = "-L${generated.resolve("lib")}"
        }

        env["CFLAGS"] += " -Wno-error=incompatible-function-pointer-types"
    }

    doLast {
        com.android.ndkports.Abi.values().forEach { abi ->
            val installDir = installDirectoryFor(abi)
            buildDirectoryFor(abi).resolve("config.h").copyTo(
                target = installDir.resolve("include/wv/config.h"), overwrite = true
            )

            installDir.resolve("lib/pkgconfig/wv-1.0.pc").let { pc ->
                pc.replace("-lpng", "")
                pc.replace("Requires:", "Requires: libpng16")
            }
        }
    }
}

tasks.prefabPackage {
    version.set(CMakeCompatibleVersion.parse(portVersion))

    licensePath.set("COPYING")

    dependencies.set(mapOf(
        "libgsf" to "1",
        "libpng" to "1",
        "libwmf" to "1",
    ))
    modules {
        create("wv") {
            includesPerAbi.set(true)
            static.set(project.findProperty("libraryType") == "static")
            dependencies.set(listOf(
                "//libgsf:gsf-1",
                "//libpng:png16",
                "//libwmf:wmf",
                "//libwmf:wmflite",
            ))
        }
    }
}

val packageSources = tasks.register<Jar>("packageSources") {
    archiveClassifier.set("sources")
    from(projectDir.resolve("build.gradle.kts"))
    from(projectDir.resolve("patches/$portVersion"))
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
                name.set("wvWare")
                description.set("Library to load and parse Word 2000, 97, 95 and 6 files")
                url.set("http://wvware.sourceforge.net")
                licenses {
                    license {
                        name.set("GPLv2")
                        url.set("http://www.gnu.org/copyleft/gpl.html")
                        distribution.set("repo")
                    }
                }
                developers {
                    // Developer list obtained from:
                    // http://wvware.sourceforge.net/#dom
                    developer {
                        name.set("Dom Lachowicz")
                    }
                    developer {
                        name.set("Caolán McNamara")
                    }
                    developer {
                        name.set("Martin Vermeer")
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
