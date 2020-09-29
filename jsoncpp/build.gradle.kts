
import com.android.ndkports.CMakeCompatibleVersion
import com.android.ndkports.PackageBuilderTask
import com.android.ndkports.MesonPortTask

val portVersion = "1.8.4"

group = "com.android.ndk.thirdparty"
version = "$portVersion-SNAPSHOT"

plugins {
    id("maven-publish")
    id("com.android.ndkports.NdkPorts")
}

ndkPorts {
    ndkPath.set(File(project.findProperty("ndkPath") as String))
    source.set(project.file("src.tar.gz"))
}

tasks.extractSrc {
    doLast {
        // jsoncpp has a "version" file on the include path that conflicts with
        // https://en.cppreference.com/w/cpp/header/version. Remove it so we can
        // build.
        outDir.get().asFile.resolve("version").delete()
    }
}

tasks.register<MesonPortTask>("buildPort")

tasks.prefabPackage {
    version.set(CMakeCompatibleVersion.parse(portVersion))

    modules {
        create("jsoncpp")
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["prefab"])
            pom {
                name.set("JsonCpp")
                description.set("The ndkports AAR for JsonCpp.")
                url.set(
                    "https://android.googlesource.com/platform/tools/ndkports"
                )
                licenses {
                    license {
                        name.set("The JsonCpp License")
                        url.set("https://github.com/open-source-parsers/jsoncpp/blob/master/LICENSE")
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
