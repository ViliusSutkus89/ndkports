import com.android.ndkports.CMakePortTask
import com.android.ndkports.CMakeCompatibleVersion
import com.android.ndkports.PrefabSysrootPlugin

val portVersion = "2.13.2"

group = rootProject.group
version = portVersion

plugins {
    id("maven-publish")
    id("signing")
    id("com.android.ndkports.NdkPorts")
}

ndkPorts {
    ndkPath.set(File(project.findProperty("ndkPath") as String))
    source.set(project.file("freetype-${portVersion}.tar.xz"))
    minSdkVersion.set(rootProject.extra.get("minSdkSupportedByNdk").toString().toInt())
}

tasks.prefab {
    generator.set(PrefabSysrootPlugin::class.java)
}

val buildTask = tasks.register<CMakePortTask>("buildPort") {
    cmake { }
}

tasks.prefabPackage {
    version.set(CMakeCompatibleVersion.parse(portVersion))

    licensePath.set("LICENSE.TXT")

    modules {
        create("freetype") {
            static.set(true)
            dependencies.set(listOf(":freetype"))
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("release") {
            from(components["prefab"])
            artifactId += rootProject.extra.get("ndkVersionSuffix")
            pom {
                name.set("FreeType")
                description.set("FreeType is a freely available software library to render fonts.")
                url.set("https://freetype.org")
                licenses {
                    license {
                        name.set("FreeType License")
                        url.set("https://gitlab.freedesktop.org/freetype/freetype/-/blob/VER-2.13.2/LICENSE.TXT")
                        distribution.set("repo")
                    }
                    license {
                        name.set("FreeType License")
                        url.set("https://gitlab.freedesktop.org/freetype/freetype/-/blob/VER-2.13.2/docs/FTL.TXT")
                        distribution.set("repo")
                    }
                    license {
                        name.set("GPLv2-or-later")
                        url.set("https://www.gnu.org/licenses/gpl-2.0.html")
                        distribution.set("repo")
                    }
                }
                developers {
                    // Developer list obtained from:
                    // https://gitlab.freedesktop.org/freetype/freetype/-/blob/VER-2-13-2/.mailmap
                    developer {
                        name.set("Behdad Esfahbod (بهداد اسفهبد)")
                        email.set("behdad@behdad.org")
                        email.set("behdad.esfahbod@gmail.com")
                    }
                    developer {
                        name.set("Alexander Borsuk")
                        email.set("me@alex.bio")
                        email.set("alexander.borsuk@qnective.com")
                    }
                    developer {
                        name.set("Ewald Hew (Hew Yih Shiuan 丘毅宣)")
                        email.set("ewaldhew@gmail.com")
                    }
                    developer {
                        name.set("Moazin Khatti (موؤذن کھٹی)")
                        email.set("moazinkhatri@gmail.com")
                    }
                    developer {
                        name.set("Priyesh Kumar (प्रियेश कुमार)")
                        email.set("priyeshkkumar@gmail.com")
                    }
                    developer {
                        name.set("Alexei Podtelezhnikov (Алексей Подтележников)")
                        email.set("apodtele@gmail.com")
                    }
                    developer {
                        name.set("Nikhil Ramakrishnan (निखिल रामकृष्णन)")
                        email.set("ramakrishnan.nikhil@gmail.com")
                    }
                    developer {
                        name.set("Dominik Röttsches")
                        email.set("drott@chromium.org")
                        email.set("drott@google.com")
                    }
                    developer {
                        name.set("Kostya Serebryany")
                        email.set("kcc@google.com")
                        email.set("konstantin.s.serebryany@gmail.com")
                    }
                    developer {
                        name.set("Suzuki, Toshiya (鈴木俊哉)")
                        email.set("mpsuzuki@hiroshima-u.ac.jp")
                        email.set("sssa@flavor1.ipc.hiroshima-u.ac.jp")
                        email.set("sssa@IPA2004-mps.local")
                        email.set("mpsuzuki@hiroshima-u.ac.jp")
                    }
                    developer {
                        name.set("Bram Tassyns")
                        email.set("BramT@enfocus.be")
                        email.set("BramT@enfocus.com")
                    }
                    developer {
                        name.set("David Turner")
                        email.set("david@freetype.org")
                        email.set("david.turner.dev@gmail.com")
                    }
                    developer {
                        name.set("Anuj Verma (अनुज वर्मा)")
                        email.set("anujv@iitbhilai.ac.in")
                    }
                    developer {
                        name.set("Ben Wagner")
                        email.set("bungeman@gmail.com")
                        email.set("bungeman@google.com")
                        email.set("bungeman@chromium.org")
                    }
                    developer {
                        name.set("Nikolaus Waxweiler")
                        email.set("madigens@gmail.com")
                        email.set("nikolaus.waxweiler@daltonmaag.com")
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
