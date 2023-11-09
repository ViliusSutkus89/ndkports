import com.android.ndkports.CMakeCompatibleVersion
import com.android.ndkports.MesonPortTask
import com.android.ndkports.PrefabSysrootPlugin
import org.gradle.jvm.tasks.Jar

val portVersion = "0.42.2"

group = rootProject.group
version = "${portVersion}-beta-3"

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

tasks.register<MesonPortTask>("buildPort") {
    meson {
        // @TODO: try to re-enable SIMD and NEON for pixman-0.42.3
        // Potentially related issues
        // https://gitlab.freedesktop.org/pixman/pixman/-/issues/46
        // https://gitlab.freedesktop.org/pixman/pixman/-/issues/45
        // https://gitlab.freedesktop.org/pixman/pixman/-/issues/80
        // https://github.com/android/ndk/issues/1569
        if (toolchain.abi == com.android.ndkports.Abi.Arm) {
            args(
                "-Dneon=disabled",
                "-Darm-simd=disabled",
            )
        }
        if (toolchain.abi == com.android.ndkports.Abi.Arm64) {
            arg("-Da64-neon=disabled")
        }
    }
}

tasks.prefabPackage {
    version.set(CMakeCompatibleVersion.parse(portVersion))

    licensePath.set("COPYING")

    modules {
        create("pixman-1") {
            static.set(project.findProperty("libraryType") == "static")
            dependencies.set(listOf("m"))
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
                name.set("Pixman")
                description.set("Pixman is a low-level software library for pixel manipulation, providing features such as image compositing and trapezoid rasterization.")
                url.set("https://www.pixman.org")
                licenses {
                    // Poppler licenses
                    license {
                        name.set("MIT")
                        url.set("https://gitlab.freedesktop.org/pixman/pixman/-/raw/pixman-0.42.2/COPYING")
                        distribution.set("repo")
                    }
                }
                developers {
                    // Developer list obtained from:
                    // https://gitlab.freedesktop.org/pixman/pixman/-/graphs/pixman-0.40.0
                    developer {
                        name.set("Søren Sandmann Pedersen")
                        email.set("sandmann@redhat.com")
                    }
                    developer {
                        name.set("Søren Sandmann Pedersen")
                        email.set("ssp@redhat.com")
                    }
                    developer {
                        name.set("Siarhei Siamashka")
                        email.set("siarhei.siamashka@nokia.com")
                    }
                    developer {
                        name.set("Matt Turner")
                        email.set("mattst88@gmail.com")
                    }
                    developer {
                        name.set("Andrea Canciani")
                        email.set("ranma42@gmail.com")
                    }
                    developer {
                        name.set("Soren Sandmann Pedersen")
                        email.set("ssp@dhcp83-218.boston.redhat.com")
                    }
                    developer {
                        name.set("Siarhei Siamashka")
                        email.set("siarhei.siamashka@gmail.com")
                    }
                    developer {
                        name.set("Benjamin Otte")
                        email.set("otte@gnome.org")
                    }
                    developer {
                        name.set("Søren Sandmann Pedersen")
                        email.set("sandmann@daimi.au.dk")
                    }
                    developer {
                        name.set("Ben Avison")
                        email.set("bavison@riscosopen.org")
                    }
                    developer {
                        name.set("Nemanja Lukic")
                        email.set("nemanja.lukic@rt-rk.com")
                    }
                    developer {
                        name.set("Aaron Plattner")
                        email.set("aplattner@nvidia.com")
                    }
                    developer {
                        name.set("Jeff Muizelaar")
                        email.set("jmuizelaar@mozilla.com")
                    }
                    developer {
                        name.set("Taekyun Kim")
                        email.set("tkq.kim@samsung.com")
                    }
                    developer {
                        name.set("Oded Gabbay")
                        email.set("oded.gabbay@gmail.com")
                    }
                    developer {
                        name.set("Chris Wilson")
                        email.set("chris@chris-wilson.co.uk")
                    }
                    developer {
                        name.set("Pekka Paalanen")
                        email.set("pekka.paalanen@collabora.co.uk")
                    }
                    developer {
                        name.set("Adam Jackson")
                        email.set("ajax@redhat.com")
                    }
                    developer {
                        name.set("Jonathan Morton")
                        email.set("jmorton@sd070.hel.movial.fi")
                    }
                    developer {
                        name.set("M Joonas Pihlaja")
                        email.set("jpihlaja@cc.helsinki.fi")
                    }
                    developer {
                        name.set("Chun-wei Fan")
                        email.set("fanchunwei@src.gnome.org")
                    }
                    developer {
                        name.set("Dylan Baker")
                        email.set("dylan@pnwbakers.com")
                    }
                    developer {
                        name.set("Alexander Larsson")
                        email.set("alexl@redhat.com")
                    }
                    developer {
                        name.set("Luca Barbato")
                        email.set("lu_zero@gentoo.org")
                    }
                    developer {
                        name.set("Vladimir Vukicevic")
                        email.set("vladimir@pobox.com")
                    }
                    developer {
                        name.set("Carl Worth")
                        email.set("cworth@cworth.org")
                    }
                    developer {
                        name.set("Bill Spitzak")
                        email.set("spitzak@gmail.com")
                    }
                    developer {
                        name.set("Benjamin Otte")
                        email.set("otte@redhat.com")
                    }
                    developer {
                        name.set("Alan Coopersmith")
                        email.set("alan.coopersmith@sun.com")
                    }
                    developer {
                        name.set("José Fonseca")
                        email.set("jrfonseca@tungstengraphics.com")
                    }
                    developer {
                        name.set("Basile Clement")
                        email.set("basile-pixman@clement.pm")
                    }
                    developer {
                        name.set("Maarten Lankhorst")
                        email.set("maarten.lankhorst@linux.intel.com")
                    }
                    developer {
                        name.set("Søren Sandmann Pedersen")
                        email.set("soren.sandmann@gmail.com")
                    }
                    developer {
                        name.set("Jeremy Huddleston")
                        email.set("jeremyhu@apple.com")
                    }
                    developer {
                        name.set("André Tupinambá")
                        email.set("andrelrt@gmail.com")
                    }
                    developer {
                        name.set("Julien Cristau")
                        email.set("jcristau@debian.org")
                    }
                    developer {
                        name.set("Stefan Weil")
                        email.set("sw@weilnetz.de")
                    }
                    developer {
                        name.set("Antti S. Lankila")
                        email.set("alankila@bel.fi")
                    }
                    developer {
                        name.set("Dmitri Vorobiev")
                        email.set("dmitri.vorobiev@movial.com")
                    }
                    developer {
                        name.set("Jonathan Morton")
                        email.set("jonathan.morton@movial.com")
                    }
                    developer {
                        name.set("Carlos Garcia Campos")
                        email.set("carlosgc@gnome.org")
                    }
                    developer {
                        name.set("Søren Sandmann Pedersen")
                        email.set("ssp@dhcp-100-2-40.bos.redhat.com")
                    }
                    developer {
                        name.set("Antoine Azar")
                        email.set("cairo@antoineazar.com")
                    }
                    developer {
                        name.set("Bertram Felgenhauer")
                        email.set("int-e@gmx.de")
                    }
                    developer {
                        name.set("Sebastian Bauer")
                        email.set("mail@sebastianbauer.info")
                    }
                    developer {
                        name.set("Alan Coopersmith")
                        email.set("alan.coopersmith@oracle.com")
                    }
                    developer {
                        name.set("Jon TURNEY")
                        email.set("jon.turney@dronecode.org.uk")
                    }
                    developer {
                        name.set("Rolland Dudemaine")
                        email.set("rolland@ghs.com")
                    }
                    developer {
                        name.set("Makoto Kato")
                        email.set("m_kato@ga2.so-net.ne.jp")
                    }
                    developer {
                        name.set("Frédéric Plourde")
                        email.set("frederic.plourde@polymtl.ca")
                    }
                    developer {
                        name.set("Christoph Reiter")
                        email.set("reiter.christoph@gmail.com")
                    }
                    developer {
                        name.set("Simon Richter")
                        email.set("Simon.Richter@hogyros.de")
                    }
                    developer {
                        name.set("Behdad Esfahbod")
                        email.set("behdad@behdad.org")
                    }
                    developer {
                        name.set("Keith Packard")
                        email.set("keithp@keithp.com")
                    }
                    developer {
                        name.set("Shiyou Yin")
                        email.set("yinshiyou-hf@loongson.cn")
                    }
                    developer {
                        name.set("Mathieu Duponchelle")
                        email.set("mathieu@centricular.com")
                    }
                    developer {
                        name.set("Jonathan Kew")
                        email.set("jfkthame@googlemail.com")
                    }
                    developer {
                        name.set("Thomas Petazzoni")
                        email.set("thomas.petazzoni@free-electrons.com")
                    }
                    developer {
                        name.set("Brad Smith")
                        email.set("brad@comstyle.com")
                    }
                    developer {
                        name.set("ingmar@irsoft.de")
                        email.set("ingmar@irsoft.de")
                    }
                    developer {
                        name.set("Bobby Salazar")
                        email.set("bobby8934@gmail.com")
                    }
                    developer {
                        name.set("Gilles Espinasse")
                        email.set("g.esp@free.fr")
                    }
                    developer {
                        name.set("Alexandros Frantzis")
                        email.set("alexandros.frantzis@linaro.org")
                    }
                    developer {
                        name.set("Cyril Brulebois")
                        email.set("kibi@debian.org")
                    }
                    developer {
                        name.set("Liu Xinyun")
                        email.set("xinyun.liu@intel.com")
                    }
                    developer {
                        name.set("Maarten Bosmans")
                        email.set("mkbosmans@gmail.com")
                    }
                    developer {
                        name.set("Matthias Hopf")
                        email.set("mhopf@suse.de")
                    }
                    developer {
                        name.set("Adrian Bunk")
                        email.set("adrian.bunk@movial.com")
                    }
                    developer {
                        name.set("Luo Jinghua")
                        email.set("sunmoon1997@gmail.com")
                    }
                    developer {
                        name.set("Eric Anholt")
                        email.set("eric@anholt.net")
                    }
                    developer {
                        name.set("Arcady Goldmints-Orlov")
                        email.set("arcadyg@nvidia.com")
                    }
                    developer {
                        name.set("Daniel Stone")
                        email.set("daniel@fooishbar.org")
                    }
                    developer {
                        name.set("Ghabry")
                        email.set("gabriel+github@mastergk.de")
                    }
                    developer {
                        name.set("Federico Mena Quintero")
                        email.set("federico@gnome.org")
                    }
                    developer {
                        name.set("Antonio Ospite")
                        email.set("ao2@ao2.it")
                    }
                    developer {
                        name.set("Khem Raj")
                        email.set("raj.khem@gmail.com")
                    }
                    developer {
                        name.set("Fan Jinke")
                        email.set("fanjinke@hygon.cn")
                    }
                    developer {
                        name.set("Niveditha Rau")
                        email.set("niveditha.rau@oracle.com")
                    }
                    developer {
                        name.set("Vladimir Smirnov")
                        email.set("civil@gentoo.org")
                    }
                    developer {
                        name.set("Dan Horák")
                        email.set("dan@danny.cz")
                    }
                    developer {
                        name.set("Fernando Seiti Furusato")
                        email.set("ferseiti@linux.vnet.ibm.com")
                    }
                    developer {
                        name.set("James Cowgill")
                        email.set("james410@cowgill.org.uk")
                    }
                    developer {
                        name.set("Jakub Bogusz")
                        email.set("qboosh@pld-linux.org")
                    }
                    developer {
                        name.set("Ritesh Khadgaray")
                        email.set("khadgaray@gmail.com")
                    }
                    developer {
                        name.set("Alexander Troosh")
                        email.set("trush@yandex.ru")
                    }
                    developer {
                        name.set("Matthieu Herrb")
                        email.set("matthieu.herrb@laas.fr")
                    }
                    developer {
                        name.set("Markos Chandras")
                        email.set("markos.chandras@imgtec.com")
                    }
                    developer {
                        name.set("Peter Breitenlohner")
                        email.set("peb@mppmu.mpg.de")
                    }
                    developer {
                        name.set("Marko Lindqvist")
                        email.set("cazfi74@gmail.com")
                    }
                    developer {
                        name.set("Benjamin Gilbert")
                        email.set("bgilbert@backtick.net")
                    }
                    developer {
                        name.set("Joshua Root")
                        email.set("jmr@macports.org")
                    }
                    developer {
                        name.set("Benny Siegert")
                        email.set("bsiegert@gmail.com")
                    }
                    developer {
                        name.set("Colin Walters")
                        email.set("walters@verbum.org")
                    }
                    developer {
                        name.set("Naohiro Aota")
                        email.set("naota@gentoo.org")
                    }
                    developer {
                        name.set("Søren Sandmann")
                        email.set("sandmann@cs.au.dk")
                    }
                    developer {
                        name.set("Nis Martensen")
                        email.set("nis.martensen@web.de")
                    }
                    developer {
                        name.set("Dave Yeo")
                        email.set("dave.r.yeo@gmail.com")
                    }
                    developer {
                        name.set("Scott McCreary")
                        email.set("scottmc2@gmail.com")
                    }
                    developer {
                        name.set("Mika Yrjola")
                        email.set("mika.yrjola@movial.com")
                    }
                    developer {
                        name.set("Tor Lillqvist")
                        email.set("tml@iki.fi")
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
