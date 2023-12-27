import com.android.ndkports.AutoconfPortTask
import com.android.ndkports.CMakeCompatibleVersion
import com.android.ndkports.PrefabSysrootPlugin
import org.gradle.jvm.tasks.Jar

val portVersion = "1.14.51"

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
    implementation("com.viliussutkus89.ndk.thirdparty:glib2${ndkVersionSuffix}${dependencyLibraryTypeSuffix}:2.75.0-beta-1")
    implementation("com.viliussutkus89.ndk.thirdparty:libxml2${ndkVersionSuffix}${dependencyLibraryTypeSuffix}:2.12.3-beta-2")
}

ndkPorts {
    ndkPath.set(File(project.findProperty("ndkPath") as String))
    minSdkVersion.set(rootProject.extra.get("minSdkSupportedByNdk").toString().toInt())
    source.set(project.file("${name}-${portVersion}.tar.xz"))
}

tasks.prefab {
    generator.set(PrefabSysrootPlugin::class.java)
}

val buildTask = tasks.register<AutoconfPortTask>("buildPort") {
    val minSdk = rootProject.extra.get("minSdkSupportedByNdk").toString().toInt()
    autoconf {
        if (minSdk < 24 && listOf(com.android.ndkports.Abi.Arm, com.android.ndkports.Abi.X86).contains(toolchain.abi)) {
            arg("--disable-largefile")
        }
    }
}

tasks.prefabPackage {
    version.set(CMakeCompatibleVersion.parse(portVersion))

    licensePath.set("COPYING")

    dependencies.set(mapOf(
        "glib2" to "1",
        "libxml2" to "1",
    ))

    modules {
        create("gsf-1") {
            static.set(project.findProperty("libraryType") == "static")
            dependencies.set(listOf(
                "//glib2:glib-2.0",
                "//glib2:gobject-2.0",
                "//libxml2:xml2",
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
                name.set("libgsf")
                description.set("The G Structured File Library")
                url.set("https://gitlab.gnome.org/GNOME/libgsf/")
                licenses {
                    license {
                        name.set("lGPLv2.1")
                        url.set("https://gitlab.gnome.org/GNOME/libgsf/-/raw/LIBGSF_1_14_51/COPYING")
                        distribution.set("repo")
                    }
                }
                developers {
                    // Developer list obtained from:
                    // https://gitlab.gnome.org/GNOME/libgsf/-/raw/LIBGSF_1_14_51/MAINTAINERS
                    // https://gitlab.gnome.org/GNOME/libgsf/-/raw/LIBGSF_1_14_51/AUTHORS
                    developer {
                        name.set("Morten Welinder")
                        email.set("terra@gnome.org")
                        id.set("mortenw")
                    }
                    developer {
                        name.set("Andreas J. Guelzow")
                        id.set("guelzow")
                    }
                    developer {
                        name.set("Jean Brefort")
                        id.set("jbrefort")
                    }
                    developer {
                        name.set("Jody Goldberg")
                        email.set("jody@gnome.org")
                    }
                    developer {
                        name.set("Manuel Mausz")
                        email.set("Manuel.Mausz@fabasoft.com")
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
