import com.android.ndkports.CMakeCompatibleVersion
import com.android.ndkports.CMakePortTask
import com.android.ndkports.PrefabSysrootPlugin
import org.gradle.jvm.tasks.Jar

val portVersion = "4.6.0"

group = rootProject.group
version = "${portVersion}-beta-6"

plugins {
    id("maven-publish")
    id("signing")
    id("com.android.ndkports.NdkPorts")
}

dependencies {
    val ndkVersionSuffix = rootProject.extra.get("ndkVersionSuffix")
    val dependencyLibraryTypeSuffix = rootProject.extra.get("dependencyLibraryTypeSuffix")
    implementation("com.viliussutkus89.ndk.thirdparty:libjpeg-turbo${ndkVersionSuffix}${dependencyLibraryTypeSuffix}:3.0.1-beta-3")
//    -- Could NOT find Deflate (missing: Deflate_LIBRARY Deflate_INCLUDE_DIR)
//    -- Could NOT find JBIG (missing: JBIG_LIBRARY JBIG_INCLUDE_DIR)
//    -- Could NOT find LERC (missing: LERC_LIBRARY LERC_INCLUDE_DIR)
//    -- Could NOT find liblzma (missing: LIBLZMA_LIBRARY LIBLZMA_INCLUDE_DIR LIBLZMA_HAS_AUTO_DECODER LIBLZMA_HAS_EASY_ENCODER LIBLZMA_HAS_LZMA_PRESET)
//    -- Could NOT find ZSTD (missing: ZSTD_LIBRARY ZSTD_INCLUDE_DIR)
//    -- Could NOT find WebP (missing: WebP_LIBRARY WebP_INCLUDE_DIR)
}

ndkPorts {
    ndkPath.set(File(project.findProperty("ndkPath") as String))
    minSdkVersion.set(rootProject.extra.get("minSdkSupportedByNdk").toString().toInt())
    source.set(project.file("tiff-${portVersion}.tar.xz"))
}

tasks.prefab {
    generator.set(PrefabSysrootPlugin::class.java)
}

tasks.register<CMakePortTask>("buildPort") {
    cmake { }
}

tasks.prefabPackage {
    version.set(CMakeCompatibleVersion.parse(portVersion))

    licensePath.set("LICENSE.md")

    dependencies.set(mapOf("libjpeg-turbo" to "1"))
    modules {
        val isStatic = project.findProperty("libraryType") == "static"
        create("tiff") {
            includesPerAbi.set(true)
            if (isStatic) {
                static.set(true)
                dependencies.set(listOf("m", "z", "//libjpeg-turbo:jpeg"))
            }
        }
        create("tiffxx") {
            includesPerAbi.set(true)
            static.set(isStatic)
            dependencies.set(listOf(":tiff"))
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
                name.set("LibTIFF")
                description.set("The LibTIFF software provides support for the Tag Image File Format (TIFF), a widely used format for storing image data.")
                url.set("http://www.simplesystems.org/libtiff")
                licenses {
                    license {
                        name.set("BSD-like")
                        url.set("https://gitlab.com/libtiff/libtiff/-/raw/v4.6.0/LICENSE.md")
                        distribution.set("repo")
                    }
                }
                developers {
                    // Developer list obtained from:
                    // https://gitlab.com/libtiff/libtiff/-/raw/v4.6.0/COMMITTERS
                    developer {
                        id.set("fwarmerdam")
                        name.set("Frank Warmerdam")
                        email.set("warmerdam@pobox.com")
                    }
                    developer {
                        id.set("dron")
                        name.set("Andrey Kiselev")
                        email.set("dron@ak4719.spb.edu")
                    }
                    developer {
                        id.set("bfriesen")
                        name.set("Bob Friesenhahn")
                        email.set("bfriesen@simple.dallas.tx.us")
                    }
                    developer {
                        id.set("joris")
                        name.set("Joris Van Damme")
                        email.set("info@awaresystems.be")
                    }
                    developer {
                        id.set("faxguy")
                        name.set("Lee Howard")
                        email.set("faxguy@howardsilvan.com")
                    }
                    developer {
                        id.set("olivier")
                        name.set("Olivier Paquet")
                        email.set("olivier.paquet@gmail.com")
                    }
                    developer {
                        id.set("tgl")
                        name.set("Tom Lane")
                        email.set("tgl@sss.pgh.pa.us")
                    }
                    developer {
                        id.set("erouault")
                        name.set("Even Rouault")
                        email.set("even.rouault@spatialys.com")
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
