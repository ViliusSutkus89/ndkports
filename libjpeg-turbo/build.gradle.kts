import com.android.ndkports.CMakePortTask
import com.android.ndkports.CMakeCompatibleVersion
import com.android.ndkports.PrefabSysrootPlugin
import org.gradle.jvm.tasks.Jar

val portVersion = "3.0.1"

group = rootProject.group
version = "${portVersion}-beta-2"

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

tasks.register<CMakePortTask>("buildPort") {
    val libraryType = project.findProperty("libraryType")
    cmake {
        arg("-DREQUIRE_SIMD=ON")
        when (libraryType) {
            "shared" -> args(
                "-DENABLE_SHARED=ON",
                "-DENABLE_STATIC=OFF",
            )
            "static" -> args(
                "-DENABLE_SHARED=OFF",
                "-DENABLE_STATIC=ON",
            )
        }
    }
}

tasks.prefabPackage {
    version.set(CMakeCompatibleVersion.parse(portVersion))

    licensePath.set("LICENSE.md")

    modules {
        create("jpeg") {
            static.set(project.findProperty("libraryType") == "static")
        }
        create("turbojpeg") {
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
                name.set("libjpeg-turbo")
                description.set("libjpeg-turbo is a JPEG image codec that uses SIMD instructions (MMX, SSE2, AVX2, Neon, AltiVec) to accelerate baseline JPEG compression and decompression on x86, x86-64, Arm, and PowerPC systems, as well as progressive JPEG compression on x86, x86-64, and Arm systems. On such systems, libjpeg-turbo is generally 2-6x as fast as libjpeg, all else being equal. On other types of systems, libjpeg-turbo can still outperform libjpeg by a significant amount, by virtue of its highly-optimized Huffman coding routines. In many cases, the performance of libjpeg-turbo rivals that of proprietary high-speed JPEG codecs.")
                url.set("https://libjpeg-turbo.org")
                licenses {
                    license {
                        name.set("BSD-modified-3-clause")
                        url.set("https://raw.githubusercontent.com/libjpeg-turbo/libjpeg-turbo/3.0.1/LICENSE.md")
                        distribution.set("repo")
                    }
                    license {
                        name.set("IJG")
                        url.set("https://raw.githubusercontent.com/libjpeg-turbo/libjpeg-turbo/3.0.1/README.ijg")
                        distribution.set("repo")
                    }
                    license {
                        name.set("zlib")
                        url.set("https://opensource.org/licenses/Zlib")
                        distribution.set("repo")
                    }
                }
                developers {
                    // Developer list obtained from:
                    // https://github.com/libjpeg-turbo/libjpeg-turbo/graphs/contributors
                    developer {
                        id.set("dcommander")
                    }
                    developer {
                        id.set("jwright-arm")
                    }
                    developer {
                        id.set("CendioOssman")
                    }
                    developer {
                        id.set("mayeut")
                    }
                    developer {
                        id.set("fbossen")
                    }
                    developer {
                        id.set("kornelski")
                    }
                    developer {
                        id.set("oerdnj")
                    }
                    developer {
                        id.set("sykhro")
                    }
                    developer {
                        id.set("ksmurchison")
                    }
                    developer {
                        id.set("fhanau")
                    }
                    developer {
                        id.set("FlyGoat")
                    }
                    developer {
                        id.set("pkasting")
                    }
                    developer {
                        id.set("ProgramMax")
                    }
                    developer {
                        id.set("arichardson")
                    }
                    developer {
                        id.set("stephengroat")
                    }
                    developer {
                        id.set("jcowgill")
                    }
                    developer {
                        id.set("mattsarett")
                    }
                    developer {
                        id.set("mathstuf")
                    }
                    developer {
                        id.set("thefloweringash")
                    }
                    developer {
                        id.set("y-guyon")
                    }
                    developer {
                        id.set("robertocr")
                    }
                    developer {
                        id.set("chris-y")
                    }
                    developer {
                        id.set("AdrianBunk")
                    }
                    developer {
                        id.set("modbw")
                    }
                    developer {
                        id.set("masal64")
                    }
                    developer {
                        id.set("rouault")
                    }
                    developer {
                        id.set("richard-townsend-arm")
                    }
                    developer {
                        id.set("ccawley2011")
                    }
                    developer {
                        id.set("nyg")
                    }
                    developer {
                        id.set("colincross")
                    }
                    developer {
                        id.set("dwatteau")
                    }
                    developer {
                        id.set("dimhotepus")
                    }
                    developer {
                        id.set("pkubaj")
                    }
                    developer {
                        id.set("Hello71")
                    }
                    developer {
                        id.set("orivej")
                    }
                    developer {
                        id.set("walisser")
                    }
                    developer {
                        id.set("neheb")
                    }
                    developer {
                        id.set("luzpaz")
                    }
                    developer {
                        id.set("chandlerc")
                    }
                    developer {
                        id.set("MartynJacques-Arm")
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
