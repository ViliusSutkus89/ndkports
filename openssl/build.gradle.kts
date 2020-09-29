
import com.android.ndkports.CMakeCompatibleVersion
import com.android.ndkports.NdkPortsTask
import com.android.ndkports.Toolchain

val portVersion = "1.1.1g"
val prefabVersion = CMakeCompatibleVersion(1, 1, 1, 7)

group = "com.android.ndk.thirdparty"
version = "$portVersion-SNAPSHOT"

plugins {
    id("maven-publish")
    id("com.android.ndkports.NdkPorts")
}

abstract class OpenSslPortTask : NdkPortsTask() {
    override fun buildForAbi(
        toolchain: Toolchain,
        workingDirectory: File,
        buildDirectory: File,
        installDirectory: File
    ) {
        buildDirectory.mkdirs()
        executeSubprocess(
            listOf(
                sourceDirectory.get().asFile.resolve("Configure").absolutePath,
                "android-${toolchain.abi.archName}",
                "-D__ANDROID_API__=${toolchain.api}",
                "--prefix=${installDirectory.absolutePath}",
                "--openssldir=${installDirectory.absolutePath}",
                "shared"
            ), buildDirectory, additionalEnvironment = mapOf(
                "ANDROID_NDK" to toolchain.ndk.path.absolutePath,
                "PATH" to "${toolchain.binDir}:${System.getenv("PATH")}"
            )
        )

        executeSubprocess(
            listOf(
                "make", "-j$ncpus", "SHLIB_EXT=.so"
            ), buildDirectory, additionalEnvironment = mapOf(
                "ANDROID_NDK" to toolchain.ndk.path.absolutePath,
                "PATH" to "${toolchain.binDir}:${System.getenv("PATH")}"
            )
        )

        executeSubprocess(
            listOf("make", "install_sw", "SHLIB_EXT=.so"),
            buildDirectory,
            additionalEnvironment = mapOf(
                "ANDROID_NDK" to toolchain.ndk.path.absolutePath,
                "PATH" to "${toolchain.binDir}:${System.getenv("PATH")}"
            )
        )
    }
}

ndkPorts {
    ndkPath.set(File(project.findProperty("ndkPath") as String))
    source.set(project.file("src.tar.gz"))
}

tasks.register<OpenSslPortTask>("buildPort")

tasks.prefabPackage {
    version.set(prefabVersion)

    modules {
        create("crypto")
        create("ssl")
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["prefab"])
            pom {
                name.set("OpenSSL")
                description.set("The ndkports AAR for OpenSSL.")
                url.set(
                    "https://android.googlesource.com/platform/tools/ndkports"
                )
                licenses {
                    license {
                        name.set("Dual OpenSSL and SSLeay License")
                        url.set("https://www.openssl.org/source/license-openssl-ssleay.txt")
                        distribution.set("repo")
                    }
                }
                developers {
                    developer {
                        name.set("The Android Open Source Project")
                    }
                }
                scm {
                    url.set("https://android.googlesource.com/platform/tools/ndkports")
                    connection.set("scm:git:https://android.googlesource.com/platform/tools/ndkports")
                }
            }
        }
    }

    repositories {
        maven {
            url = uri("${rootProject.buildDir}/repository")
        }
    }
}
