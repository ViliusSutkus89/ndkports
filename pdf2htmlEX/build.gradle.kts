import com.android.ndkports.CMakeCompatibleVersion
import com.android.ndkports.CMakePortTask
import com.android.ndkports.PrefabSysrootPlugin
import org.gradle.jvm.tasks.Jar

group = rootProject.group

// Hardcode a list of available versions
val portVersion = when(project.findProperty("packageVersion")) {
    "0.18.7-poppler-0.81.0" -> {
        version = "0.18.7-poppler-0.81.0-beta-2"
        "0.18.7-poppler-0.81.0"
    }
    else /* "0.18.8.rc1" */ -> {
        version = "0.18.8.rc1-beta-1"
        "0.18.8.rc1"
    }
}

plugins {
    id("maven-publish")
    id("signing")
    id("com.android.ndkports.NdkPorts")
}

dependencies {
    val ndkVersionSuffix = rootProject.extra.get("ndkVersionSuffix")
    implementation("com.viliussutkus89.ndk.thirdparty:cairo${ndkVersionSuffix}-static:1.18.0-beta-3")
    implementation("com.viliussutkus89.ndk.thirdparty:freetype${ndkVersionSuffix}-static:2.13.2-beta-5")

    when (portVersion) {
        "0.18.7-poppler-0.81.0" -> {
            implementation("com.viliussutkus89.ndk.thirdparty:fontforge${ndkVersionSuffix}-static:20170731-beta-4")
            implementation("com.viliussutkus89.ndk.thirdparty:poppler${ndkVersionSuffix}-static:0.81.0-beta-3")
        }
        "0.18.8.rc1" -> {
            implementation("com.viliussutkus89.ndk.thirdparty:fontforge${ndkVersionSuffix}-static:20200314-beta-4")
            implementation("com.viliussutkus89.ndk.thirdparty:poppler${ndkVersionSuffix}-static:0.89.0-beta-3")
        }
    }
}

ndkPorts {
    ndkPath.set(File(project.findProperty("ndkPath") as String))
    minSdkVersion.set(when(portVersion) {
        "0.18.7-poppler-0.81.0" -> rootProject.extra.get("minSdkSupportedByNdk").toString().toInt()
        // fontforge-20200314 and newer requires Android API 24
        else -> 24
    })
    source.set(project.file("v${portVersion}.tar.gz"))
}

fun File.replace(oldValue: String, newValue: String, ignoreCase: Boolean = false) {
    writeText(readText().replace(oldValue, newValue, ignoreCase))
}

fun File.patch(patch: String): File {
    return patch(projectDir.resolve("patches/$portVersion").resolve(patch))
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
        when (portVersion) {
            "0.18.7-poppler-0.81.0" -> {
                srcDir.resolve("CMakeLists.txt")
                    .patch("find-libraries.patch")
                    .patch("cflags.patch")
                srcDir.patch("make-a-library.patch")
            }
        }
    }
}

tasks.prefab {
    generator.set(PrefabSysrootPlugin::class.java)
}

tasks.register<CMakePortTask>("buildPort") {
    when (portVersion) {
        "0.18.8.rc1" -> {
            sourceSubDirectory.set("pdf2htmlEX")
        }
    }

    cmake { }

    doLast {
        // @TODO: verify other ABIs have matching assets
        val dst = project.buildDir.resolve("assets/pdf2htmlEX/share/pdf2htmlEX").apply { mkdirs() }
        installDirectoryFor(com.android.ndkports.Abi.Arm).resolve("share/pdf2htmlEX").copyRecursively(dst) { file, exception ->
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

tasks.prefabPackage {
    version.set(CMakeCompatibleVersion.parse(when(portVersion) {
        "0.18.7-poppler-0.81.0" -> "0.18.7.0810"
        "0.18.8.rc1" -> "0.18.8.1"
        else -> portVersion
    }))

    licensePath.set("LICENSE")

    dependencies.set(mapOf(
        "cairo" to "1",
        "freetype" to "1",
        "fontforge" to "1",
        "poppler" to "1",
    ))

    modules {
        create("pdf2htmlEX") {
            static.set(project.findProperty("libraryType") == "static")
            dependencies.set(listOf(
                "//cairo:cairo",
                "//freetype:freetype",
                "//fontforge:fontforge",
                "//poppler:poppler",
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
                name.set("pdf2htmlEX")
                description.set("Convert PDF to HTML without losing text or format")
                url.set("https://github.com/pdf2htmlEX/pdf2htmlEX")
                licenses {
                    // Poppler licenses
                    license {
                        name.set("GPLv3-or-later")
                        url.set("https://raw.githubusercontent.com/pdf2htmlEX/pdf2htmlEX/$portVersion/LICENSE")
                        distribution.set("repo")
                    }
                    license {
                        name.set("MIT")
                        url.set("https://raw.githubusercontent.com/pdf2htmlEX/pdf2htmlEX/$portVersion/share/LICENSE")
                        distribution.set("repo")
                    }
                    license {
                        name.set("CC-BY-3.0")
                        url.set("https://raw.githubusercontent.com/pdf2htmlEX/pdf2htmlEX/$portVersion/logo/LICENSE")
                        distribution.set("repo")
                    }
                }
                developers {
                    // Developer list obtained from:
                    // https://raw.githubusercontent.com/pdf2htmlEX/pdf2htmlEX/v0.18.7-poppler-0.81.0/AUTHORS
                    // https://raw.githubusercontent.com/pdf2htmlEX/pdf2htmlEX/v0.18.8.rc1/AUTHORS
                    developer {
                        name.set("Lu Wang")
                        email.set("coolwanglu@gmail.com")
                    }
                    developer {
                        name.set("Aamir Adnan")
                        email.set("s33k.n.d3str0y@gmail.com")
                    }
                    developer {
                        name.set("Chris Cinelli")
                        email.set("chris@allestelle.com")
                    }
                    developer {
                        name.set("Daniel Bonniot de Ruisselet")
                        email.set("dbonniot@chemaxon.com")
                    }
                    developer {
                        name.set("Denis Sablic")
                        email.set("denis.sablic@gmail.com")
                    }
                    developer {
                        name.set("Duan Yao")
                        email.set("duanyao@ustc.edu")
                    }
                    developer {
                        name.set("filodej")
                        email.set("philode@gmail.com")
                    }
                    developer {
                        name.set("hasufell")
                        email.set("julian.ospald@googlemail.com")
                    }
                    developer {
                        name.set("Herbert Jones")
                        email.set("herbert@mediafire.com")
                    }
                    developer {
                        name.set("Hongliang Tian")
                        email.set("tatetian@gmail.com")
                    }
                    developer {
                        name.set("Johannes Schauer")
                        email.set("j.schauer@email.de")
                    }
                    developer {
                        name.set("John Hewson")
                        email.set("john@jahewson.com")
                    }
                    developer {
                        name.set("Marc Sanfacon")
                        email.set("marc.sanfacon@gmail.com")
                    }
                    developer {
                        name.set("Michele Redolfi")
                        email.set("michele@tecnicaict.com")
                    }
                    developer {
                        name.set("Mick Giles")
                        email.set("mick@mickgiles.com")
                    }
                    developer {
                        name.set("Ryan Morlok")
                        email.set("ryan.morlok@morlok.com")
                    }
                    developer {
                        name.set("Simon Chenard")
                        email.set("chenard.simon@gmail.com")
                    }
                    developer {
                        name.set("Wanmin Liu")
                        email.set("wanminliu@gmail.com")
                    }
                    developer {
                        name.set("Arthur Titeica")
                        email.set("arthur.titeica@gmail.com")
                    }
                    developer {
                        name.set("Deepak Thukral")
                        email.set("iapain@iapa.in")
                    }
                    developer {
                        name.set("Jamie Ly")
                        email.set("me@jamie.ly")
                    }
                    developer {
                        name.set("Steven Lee")
                        email.set("rubypdf@gmail.com")
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
