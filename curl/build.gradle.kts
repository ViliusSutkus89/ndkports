import com.android.ndkports.AutoconfPortTask
import com.android.ndkports.CMakeCompatibleVersion
import com.android.ndkports.PrefabTask

val portVersion = "7.69.1"

group = "com.android.ndk.thirdparty"
version = "$portVersion-SNAPSHOT"

plugins {
    id("maven-publish")
    id("com.android.ndkports.NdkPorts")
}

dependencies {
    implementation(project(":openssl"))
}

abstract class CurlPortTask : AutoconfPortTask() {
    @get:InputDirectory
    abstract val sysroot: DirectoryProperty

    override fun configureArgs(
        workingDirectory: File,
        toolchain: com.android.ndkports.Toolchain
    ): List<String> {
        return listOf(
            "--disable-ntlm-wb",
            "--enable-ipv6",
            "--with-zlib",
            "--with-ca-path=/system/etc/security/cacerts",
            "--with-ssl=${sysroot.get().asFile.resolve(toolchain.abi.triple)}"
        )
    }

    override fun configureEnv(
        workingDirectory: File,
        toolchain: com.android.ndkports.Toolchain
    ): Map<String, String> = mapOf(
        // aarch64 still defaults to bfd which transitively checks libraries.
        // When curl is linking one of its own libraries which depends on
        // openssl, it doesn't pass -rpath-link to be able to find the SSL
        // libraries and fails to build because of it.
        //
        // TODO: Switch to lld once we're using r21.
        "LDFLAGS" to "-fuse-ld=gold"
    )
}

ndkPorts {
    ndkPath.set(File(project.findProperty("ndkPath") as String))
    source.set(project.file("src.tar.gz"))
}

tasks.register<CurlPortTask>("buildPort") {
    sysroot.set(tasks.getByName<PrefabTask>("prefab").sysrootDirectory)
}

tasks.prefabPackage {
    version.set(CMakeCompatibleVersion.parse(portVersion))

    licensePath.set("COPYING")

    @Suppress("UnstableApiUsage")
    dependencies.set(mapOf(
        "openssl" to "1.1.1g"
    ))

    modules {
        create("curl") {
            dependencies.set(listOf(
                "//openssl:crypto",
                "//openssl:ssl"
            ))
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["prefab"])
            pom {
                name.set("curl")
                description.set("The ndkports AAR for curl.")
                url.set(
                    "https://android.googlesource.com/platform/tools/ndkports"
                )
                licenses {
                    license {
                        name.set("The curl License")
                        url.set("https://curl.haxx.se/docs/copyright.html")
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
