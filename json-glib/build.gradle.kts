import com.android.ndkports.CMakeCompatibleVersion
import com.android.ndkports.MesonPortTask
import com.android.ndkports.PrefabSysrootPlugin
import org.gradle.jvm.tasks.Jar

val portVersion = "1.8.0"

group = rootProject.group
version = "${portVersion}-beta-1"

plugins {
    id("maven-publish")
    id("signing")
    id("com.android.ndkports.NdkPorts")
}

dependencies {
    val ndkVersionSuffix = rootProject.extra.get("ndkVersionSuffix")
    implementation("com.viliussutkus89.ndk.thirdparty:glib2${ndkVersionSuffix}-static:2.78.1-beta-2")
}

ndkPorts {
    ndkPath.set(File(project.findProperty("ndkPath") as String))
    minSdkVersion.set(rootProject.extra.get("minSdkSupportedByNdk").toString().toInt())
    source.set(project.file("${name}-${portVersion}.tar.xz"))
}

tasks.prefab {
    generator.set(PrefabSysrootPlugin::class.java)
}

tasks.register<MesonPortTask>("buildPort") {
    meson { }
}

tasks.prefabPackage {
    version.set(CMakeCompatibleVersion.parse(portVersion))

    licensePath.set("COPYING")

    modules {
        create("json-glib-1.0") {
            static.set(project.findProperty("libraryType") == "static")
            dependencies.set(listOf("//glib2:gio-2.0"))
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
                name.set("JSON-GLib")
                description.set("JSON-GLib is a library providing serialization and deserialization support for the JavaScript Object Notation (JSON) format described by RFC 4627.")
                url.set("https://wiki.gnome.org/Projects/JsonGlib")
                licenses {
                    // Poppler licenses
                    license {
                        name.set("LGPL-2.1-or-later")
                        url.set("https://gitlab.gnome.org/GNOME/json-glib/-/raw/1.8.0/COPYING")
                        distribution.set("repo")
                    }
                }
                developers {
                    // Developer list obtained from:
                    // https://gitlab.gnome.org/GNOME/json-glib/-/raw/1.8.0/README.md
                    developer {
                        name.set("Emmanuele Bassi")
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
