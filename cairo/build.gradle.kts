import com.android.ndkports.CMakeCompatibleVersion
import com.android.ndkports.MesonPortTask
import com.android.ndkports.PrefabSysrootPlugin
import org.gradle.jvm.tasks.Jar

val portVersion = "1.18.0"

group = rootProject.group
version = "${portVersion}-beta-5"

plugins {
    id("maven-publish")
    id("signing")
    id("com.android.ndkports.NdkPorts")
}

dependencies {
    val ndkVersionSuffix = rootProject.extra.get("ndkVersionSuffix")
    val dependencyLibraryTypeSuffix = rootProject.extra.get("dependencyLibraryTypeSuffix")
    implementation("com.viliussutkus89.ndk.thirdparty:freetype${ndkVersionSuffix}${dependencyLibraryTypeSuffix}:2.13.2-beta-7")
    implementation("com.viliussutkus89.ndk.thirdparty:libpng${ndkVersionSuffix}${dependencyLibraryTypeSuffix}:1.6.40-beta-6")
    implementation("com.viliussutkus89.ndk.thirdparty:pixman${ndkVersionSuffix}${dependencyLibraryTypeSuffix}:0.42.2-beta-4")
    implementation("com.viliussutkus89.ndk.thirdparty:glib2${ndkVersionSuffix}${dependencyLibraryTypeSuffix}:2.78.3-beta-2")
    implementation("com.viliussutkus89.ndk.thirdparty:fontconfig${ndkVersionSuffix}${dependencyLibraryTypeSuffix}:2.14.2-beta-5")

    // Test dependencies
//    Run-time dependency libspectre found: NO (tried pkgconfig and cmake) - CAIRO_CAN_TEST_PS_SURFACE
//    Run-time dependency poppler-glib found: NO (tried pkgconfig and cmake) - CAIRO_CAN_TEST_PDF_SURFACE
//    Run-time dependency librsvg-2.0 found: NO (tried pkgconfig and cmake) - CAIRO_CAN_TEST_SVG_SURFACE
}

ndkPorts {
    ndkPath.set(File(project.findProperty("ndkPath") as String))
    minSdkVersion.set(rootProject.extra.get("minSdkSupportedByNdk").toString().toInt())
    source.set(project.file("${name}-${portVersion}.tar.xz"))
}

tasks.extractSrc {
    doLast {
        // malloc-stats fails to build because of missing symbol "backtrace_symbols"
        // util already removed from master ( https://gitlab.freedesktop.org/cairo/cairo/-/merge_requests/519 )
        outDir.get().asFile.resolve("util/meson.build").let { utilMesonBuildFile ->
            utilMesonBuildFile.writeText(
                utilMesonBuildFile.readText().replace(
            "libmallocstats = library('malloc-stats', 'malloc-stats.c', dependencies : dl_dep)", ""
                )
            )
        }
    }
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

    dependencies.set(mapOf(
        "fontconfig" to "1",
        "freetype" to "1",
        "glib2" to "1",
        "pixman" to "1",
        "libpng" to "1",
    ))

    modules {
        create("cairo") {
            static.set(project.findProperty("libraryType") == "static")
            dependencies.set(listOf(
                "m", "dl", "z",
                "//libpng:png16",
                "//fontconfig:fontconfig",
                "//freetype:freetype",
                "//pixman:pixman-1",
            ))
        }

        create("cairo-gobject") {
            static.set(project.findProperty("libraryType") == "static")
            dependencies.set(listOf(
                "m", "dl",
                ":cairo",
                "//glib2:glib-2.0",
                "//glib2:gobject-2.0",
                "z",
                "//libpng:png16",
                "//fontconfig:fontconfig",
                "//freetype:freetype",
                "//pixman:pixman-1",
            ))
        }
        create("cairo-script-interpreter") {
            static.set(project.findProperty("libraryType") == "static")
            dependencies.set(listOf(
                "m", "dl",
                ":cairo",
                "z",
                "//libpng:png16",
                "//fontconfig:fontconfig",
                "//freetype:freetype",
                "//pixman:pixman-1",
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
                name.set("Cairo")
                description.set("Cairo is a 2D graphics library with support for multiple output devices.")
                url.set("https://cairographics.org")
                licenses {
                    license {
                        name.set("LGPLv2.1-or-MPLv1.1")
                        url.set("https://cgit.freedesktop.org/cairo/plain/COPYING?h=1.18.0")
                        distribution.set("repo")
                    }
                    license {
                        name.set("LGPLv2.1")
                        url.set("https://cgit.freedesktop.org/cairo/plain/COPYING-LGPL-2.1?h=1.18.0")
                        distribution.set("repo")
                    }
                    license {
                        name.set("MPLv1.1")
                        url.set("https://cgit.freedesktop.org/cairo/plain/COPYING-MPL-1.1?h=1.18.0")
                        distribution.set("repo")
                    }
                }
                developers {
                    // Developer list obtained from:
                    // https://cgit.freedesktop.org/cairo/plain/AUTHORS?h=1.18.0
                    developer {
                        name.set("Josh Aas")
                        email.set("joshmoz@gmail.com")
                    }
                    developer {
                        name.set("Daniel Amelang")
                        email.set("dan@amelang.net")
                    }
                    developer {
                        name.set("Shawn T. Amundson")
                        email.set("amundson@gtk.org")
                    }
                    developer {
                        name.set("Olivier Andrieu")
                        email.set("oliv__a@users.sourceforge.net")
                    }
                    developer {
                        name.set("Peter Dennis Bartok")
                        email.set("peter@novonyx.com")
                    }
                    developer {
                        name.set("Dave Beckett")
                        email.set("dajobe@debian.org")
                    }
                    developer {
                        name.set("Kai-Uwe Behrmann")
                        email.set("ku.b@gmx.de")
                    }
                    developer {
                        name.set("Christian Biesinger")
                        email.set("cbiesinger@web.de")
                    }
                    developer {
                        name.set("Billy Biggs")
                        email.set("vektor@dumbterm.net")
                    }
                    developer {
                        name.set("Hans Breuer")
                        email.set("hans@breuer.org")
                    }
                    developer {
                        name.set("Brian Cameron")
                        email.set("brian.cameron@sun.com")
                    }
                    developer {
                        name.set("Carlos Garcia Campos")
                        email.set("carlosgc@gnome.org")
                    }
                    developer {
                        name.set("Andrea Canciani")
                        email.set("ranma42@gmail.com")
                    }
                    developer {
                        name.set("Damien Carbery")
                        email.set("damien.carbery@sun.com")
                    }
                    developer {
                        name.set("Andrew Chant")
                        email.set("andrew.chant@utoronto.ca")
                    }
                    developer {
                        name.set("Steve Chaplin")
                        email.set("stevech1097@yahoo.com.au")
                    }
                    developer {
                        name.set("Tomasz Cholewo")
                        email.set("cholewo@ieee-cis.org")
                    }
                    developer {
                        name.set("Manu Cornet")
                        email.set("manu@manucornet.net")
                    }
                    developer {
                        name.set("Frederic Crozat")
                        email.set("fcrozat@mandriva.com")
                    }
                    developer {
                        name.set("Julien Danjou")
                        email.set("julien@danjou.info")
                    }
                    developer {
                        name.set("Radek Doulík")
                        email.set("rodo@novell.com")
                    }
                    developer {
                        name.set("John Ehresman")
                        email.set("jpe@wingide.com")
                    }
                    developer {
                        name.set("John Ellson")
                        email.set("ellson@research.att.com")
                    }
                    developer {
                        name.set("Michael Emmel")
                        email.set("mike.emmel@gmail.com")
                    }
                    developer {
                        name.set("Miklós Erdélyi")
                        email.set("erdelyim@gmail.com")
                    }
                    developer {
                        name.set("Behdad Esfahbod")
                        email.set("behdad@behdad.org")
                    }
                    developer {
                        name.set("Gilles Espinasse")
                        email.set("g.esp@free.fr")
                    }
                    developer {
                        name.set("Larry Ewing")
                        email.set("lewing@novell.com")
                    }
                    developer {
                        name.set("Brian Ewins")
                        email.set("Brian.Ewins@gmail.com")
                    }
                    developer {
                        name.set("Bertram Felgenhauer")
                        email.set("int-e@gmx.de")
                    }
                    developer {
                        name.set("Damian Frank")
                        email.set("damian.frank@gmail.com")
                    }
                    developer {
                        name.set("Bdale Garbee")
                        email.set("bdale@gag.com")
                    }
                    developer {
                        name.set("Jens Granseuer")
                        email.set("jensgr@gmx.net")
                    }
                    developer {
                        name.set("Laxmi Harikumar")
                        email.set("laxmi.harikumar@digital.com")
                    }
                    developer {
                        name.set("J. Ali Harlow")
                        email.set("ali@avrc.city.ac.uk")
                    }
                    developer {
                        name.set("Bryce Harrington")
                        email.set("bryce@osg.samsung.com")
                    }
                    developer {
                        name.set("Mathias Hasselmann")
                        email.set("mathias.hasselmann@gmx.de")
                    }
                    developer {
                        name.set("Richard Henderson")
                        email.set("rth@twiddle.net")
                    }
                    developer {
                        name.set("James Henstridge")
                        email.set("james@daa.com.au")
                    }
                    developer {
                        name.set("Graydon Hoare")
                        email.set("graydon@redhat.com")
                    }
                    developer {
                        name.set("Thomas Hunger")
                        email.set("info@teh-web.de")
                    }
                    developer {
                        name.set("Thomas Jaeger")
                        email.set("ThJaeger@gmail.com")
                    }
                    developer {
                        name.set("Björn Lindqvist")
                        email.set("bjourne@gmail.com")
                    }
                    developer {
                        name.set("Kristian Høgsberg")
                        email.set("krh@redhat.com")
                    }
                    developer {
                        name.set("Amaury Jacquot")
                        email.set("sxpert@esitcom.org")
                    }
                    developer {
                        name.set("Adrian Johnson")
                        email.set("ajohnson@redneon.com")
                    }
                    developer {
                        name.set("Michael Johnson")
                        email.set("ahze@ahze.net")
                    }
                    developer {
                        name.set("Jonathon Jongsma")
                        email.set("jonathon.jongsma@gmail.com")
                    }
                    developer {
                        name.set("Øyvind Kolås")
                        email.set("pippin@freedesktop.org")
                    }
                    developer {
                        name.set("Martin Kretzschmar")
                        email.set("martink@gnome.org")
                    }
                    developer {
                        name.set("Mathieu Lacage")
                        email.set("Mathieu.Lacage@sophia.inria.fr")
                    }
                    developer {
                        name.set("Dominic Lachowicz")
                        email.set("domlachowicz@gmail.com")
                    }
                    developer {
                        name.set("Alexander Larsson")
                        email.set("alexl@redhat.com")
                    }
                    developer {
                        name.set("Sylvestre Ledru")
                        email.set("sylvestre@mozilla.com")
                    }
                    developer {
                        name.set("Tor Lillqvist")
                        email.set("tml@novell.com")
                    }
                    developer {
                        name.set("Jinghua Luo")
                        email.set("sunmoon1997@gmail.com")
                    }
                    developer {
                        name.set("Luke-Jr")
                        email.set("luke-jr@utopios.org")
                    }
                    developer {
                        name.set("Kjartan Maraas")
                        email.set("kmaraas@gnome.org")
                    }
                    developer {
                        name.set("Nis Martensen")
                        email.set("nis.martensen@web.de")
                    }
                    developer {
                        name.set("Jordi Mas")
                        email.set("jordi@ximian.com")
                    }
                    developer {
                        name.set("Nicholas Miell")
                        email.set("nmiell@gmail.com")
                    }
                    developer {
                        name.set("Eugeniy Meshcheryakov")
                        email.set("eugen@debian.org")
                    }
                    developer {
                        name.set("Zakharov Mikhail")
                        email.set("zmey20000@yahoo.com")
                    }
                    developer {
                        name.set("Christopher (Monty) Montgomery")
                        email.set("xiphmont@gmail.com")
                    }
                    developer {
                        name.set("Tim Mooney")
                        email.set("enchanter@users.sourceforge.net")
                    }
                    developer {
                        name.set("Jeff Muizelaar")
                        email.set("jeff@infidigm.net")
                    }
                    developer {
                        name.set("Yevgen Muntyan")
                        email.set("muntyan@tamu.edu")
                    }
                    developer {
                        name.set("Ravi Nanjundappa")
                        email.set("nravi.n@samsung.com")
                    }
                    developer {
                        name.set("Declan Naughton")
                        email.set("piratepenguin@gmail.com")
                    }
                    developer {
                        name.set("Peter Nilsson")
                        email.set("c99pnn@cs.umu.se")
                    }
                    developer {
                        name.set("Henning Noren")
                        email.set("henning.noren.402@student.lu.se")
                    }
                    developer {
                        name.set("Geoff Norton")
                        email.set("gnorton@customerdna.com")
                    }
                    developer {
                        name.set("Robert O'Callahan")
                        email.set("rocallahan@novell.com")
                    }
                    developer {
                        name.set("Ian Osgood")
                        email.set("iano@quirkster.com")
                    }
                    developer {
                        name.set("Benjamin Otte")
                        email.set("otte@gnome.org")
                    }
                    developer {
                        name.set("Mike Owens")
                        email.set("etc@filespanker.com")
                    }
                    developer {
                        name.set("Emmanuel Pacaud")
                        email.set("emmanuel.pacaud@lapp.in2p3.fr")
                    }
                    developer {
                        name.set("Keith Packard")
                        email.set("keithp@keithp.com")
                    }
                    developer {
                        name.set("Stuart Parmenter")
                        email.set("pavlov@pavlov.net")
                    }
                    developer {
                        name.set("Alfred Peng")
                        email.set("alfred.peng@sun.com")
                    }
                    developer {
                        name.set("Christof Petig")
                        email.set("christof@petig-baender.de")
                    }
                    developer {
                        name.set("Joonas Pihlaja")
                        email.set("jpihlaja@cc.helsinki.fi")
                    }
                    developer {
                        name.set("Mart Raudsepp")
                        email.set("leio@dustbite.net")
                    }
                    developer {
                        name.set("David Reveman")
                        email.set("davidr@novell.com")
                    }
                    developer {
                        name.set("Calum Robinson")
                        email.set("calumr@mac.com")
                    }
                    developer {
                        name.set("Pavel Roskin")
                        email.set("proski@gnu.org")
                    }
                    developer {
                        name.set("Tim Rowley")
                        email.set("tim.rowley@gmail.com")
                    }
                    developer {
                        name.set("Soeren Sandmann")
                        email.set("sandmann@daimi.au.dk")
                    }
                    developer {
                        name.set("Uli Schlachter")
                        email.set("psychon@znc.in")
                    }
                    developer {
                        name.set("Torsten Schönfeld")
                        email.set("kaffeetisch@gmx.de")
                    }
                    developer {
                        name.set("Jamey Sharp")
                        email.set("jamey@minilop.net")
                    }
                    developer {
                        name.set("Jason Dorje Short")
                        email.set("jdorje@users.sf.net")
                    }
                    developer {
                        name.set("Jeff Smith")
                        email.set("whydoubt@yahoo.com")
                    }
                    developer {
                        name.set("Travis Spencer")
                        email.set("tspencer@cs.pdx.edu")
                    }
                    developer {
                        name.set("Bill Spitzak")
                        email.set("spitzak@d2.com")
                    }
                    developer {
                        name.set("Zhe Su")
                        email.set("james.su@gmail.com")
                    }
                    developer {
                        name.set("Owen Taylor")
                        email.set("otaylor@redhat.com")
                    }
                    developer {
                        name.set("Pierre Tardy")
                        email.set("tardyp@gmail.com")
                    }
                    developer {
                        name.set("Karl Tomlinson")
                        email.set("karlt+@karlt.net")
                    }
                    developer {
                        name.set("Alp Toker")
                        email.set("alp@atoker.com")
                    }
                    developer {
                        name.set("Malcolm Tredinnick")
                        email.set("malcolm@commsecure.com.au")
                    }
                    developer {
                        name.set("David Turner")
                        email.set("david@freetype.org")
                    }
                    developer {
                        name.set("Kalle Vahlman")
                        email.set("kalle.vahlman@gmail.com")
                    }
                    developer {
                        name.set("Sasha Vasko")
                        email.set("sasha@aftercode.net")
                    }
                    developer {
                        name.set("Vladimir Vukicevic")
                        email.set("vladimir@pobox.com")
                    }
                    developer {
                        name.set("Jonathan Watt")
                        email.set("jwatt@jwatt.org")
                    }
                    developer {
                        name.set("Peter Weilbacher")
                        email.set("pmw@avila.aip.de")
                    }
                    developer {
                        name.set("Dan Williams")
                        email.set("dcbw@redhat.com")
                    }
                    developer {
                        name.set("Chris Wilson")
                        email.set("chris@chris-wilson.co.uk")
                    }
                    developer {
                        name.set("Carl Worth")
                        email.set("cworth@isi.edu")
                    }
                    developer {
                        name.set("Richard D. Worth")
                        email.set("richard@theworths.org")
                    }
                    developer {
                        name.set("Kent Worsnop")
                        email.set("kworsnop@accesswave.ca")
                    }
                    developer {
                        name.set("Dave Yeo")
                        email.set("daveryeo@telus.net")
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
