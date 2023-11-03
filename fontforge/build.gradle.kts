import com.android.ndkports.AutoconfPortTask
import com.android.ndkports.CMakePortTask
import com.android.ndkports.CMakeCompatibleVersion
import com.android.ndkports.PrefabSysrootPlugin
import org.gradle.jvm.tasks.Jar

// @TODO: bundle assets

group = rootProject.group

// Hardcode a list of available versions
val portVersion = when(project.findProperty("packageVersion")) {
    "20170731" -> {
        version = "20170731-beta-1"
        "20170731"
    }
    "20200314" -> {
        version = "20200314-beta-1"
        "20200314"
    }
//    "20230101" -> {
    else -> {
        version = "20230101-beta-1"
        "20230101"
    }
}

plugins {
    id("maven-publish")
    id("signing")
    id("com.android.ndkports.NdkPorts")
}

val minSupportedSdk = rootProject.extra.get("minSdkSupportedByNdk").toString().toInt()

dependencies {
    val ndkVersionSuffix = rootProject.extra.get("ndkVersionSuffix")
    implementation("com.viliussutkus89.ndk.thirdparty:cairo${ndkVersionSuffix}-static:1.18.0-beta-1")
    implementation("com.viliussutkus89.ndk.thirdparty:freetype${ndkVersionSuffix}-static:2.13.2-beta-3")
    implementation("com.viliussutkus89.ndk.thirdparty:proxy-libintl${ndkVersionSuffix}-static:0.4.1")
    implementation("com.viliussutkus89.ndk.thirdparty:glib2${ndkVersionSuffix}-static:2.78.1-beta-2")
    implementation("com.viliussutkus89.ndk.thirdparty:libjpeg-turbo${ndkVersionSuffix}-static:3.0.1-beta-1")
    implementation("com.viliussutkus89.ndk.thirdparty:libtool${ndkVersionSuffix}-static:2.4.6-beta-1")
    implementation("com.viliussutkus89.ndk.thirdparty:libpng${ndkVersionSuffix}-static:1.6.40-beta-4")
    implementation("com.viliussutkus89.ndk.thirdparty:libuninameslist${ndkVersionSuffix}-static:20230916-beta-1")
    implementation("com.viliussutkus89.ndk.thirdparty:libxml2${ndkVersionSuffix}-static:2.11.5-beta-1")
    implementation("com.viliussutkus89.ndk.thirdparty:spiro${ndkVersionSuffix}-static:20221101-beta-1")

    if (minSupportedSdk >= 21)
        implementation("com.viliussutkus89.ndk.thirdparty:pango${ndkVersionSuffix}-static:1.51.0-beta-1")
    else
        implementation("com.viliussutkus89.ndk.thirdparty:pango${ndkVersionSuffix}-static:1.49.4-beta-1")

    if (portVersion != "20170731") {
        // libfontforge checks for TIFFRewriteField , which was deprecated in libtiff-4
        // http://www.simplesystems.org/libtiff/v4.0.0.html
        implementation("com.viliussutkus89.ndk.thirdparty:libtiff${ndkVersionSuffix}-static:4.6.0-beta-2")
    }

    // -- Could NOT find GIF (missing: GIF_LIBRARY GIF_INCLUDE_DIR)
    // ENABLE_LIBREADLINE = AUTO => OFF
    // ENABLE_WOFF2 = AUTO => OFF
}

ndkPorts {
    ndkPath.set(File(project.findProperty("ndkPath") as String))
    minSdkVersion.set(minSupportedSdk)
    if (portVersion == "20170731") {
        source.set(project.file("${name}-dist-${portVersion}.tar.xz"))
    } else {
        source.set(project.file("${name}-${portVersion}.tar.xz"))
    }
}

fun File.replace(oldValue: String, newValue: String, ignoreCase: Boolean = false): File {
    writeText(readText().replace(oldValue, newValue, ignoreCase))
    return this
}

fun File.patch(patch: File) {
    val pb = ProcessBuilder(
        if (isFile) listOf("patch", "-p0", absolutePath)
        else listOf("patch", "-p0")
    )

    if (isDirectory)
        pb.directory(absoluteFile)

    val process = pb.start()
    process.outputStream.writer().use {
        it.write(patch.readText())
    }
    process.errorStream.bufferedReader().use {
        println(it.readText())
    }
    process.inputStream.bufferedReader().use {
        println(it.readText())
    }
    if (process.waitFor() != 0) {
        throw RuntimeException("Patch failed!\n")
    }
}

tasks.extractSrc {
    doLast {
        val srcDir = outDir.get().asFile

        if (portVersion == "20170731") {
            // Use libtool from gradle dependencies
            srcDir.resolve("libltdl/ltdl.h").delete()

            // pthread_cancel unavailable on Android
            // @TODO: implement a proper workaround
            srcDir.resolve("gutils/gio.c").patch(projectDir.resolve("patches/gutils-gio.no-pthread-cancel.patch"))

            // fontforgeexe/startnoui.c
            srcDir.resolve("fontforgeexe/startnoui.c").patch(projectDir.resolve("patches/fontforgeexe-startnoui.FindOrMakeEncoding.patch"))

            // https://android.googlesource.com/platform/bionic/+/master/docs/32-bit-abi.md
            srcDir.resolve("config.h.in").patch(projectDir.resolve("patches/file_offset_bits.patch"))

            // android-ndk-r20/toolchains/llvm/prebuilt/linux-x86_64/sysroot/usr/include/pwd.h
            // #if __ANDROID_API__ >= 26
            // struct passwd* getpwent(void) __INTRODUCED_IN(26);
            // void setpwent(void) __INTRODUCED_IN(26);
            // void endpwent(void) __INTRODUCED_IN(26);
            // #endif /* __ANDROID_API__ >= 26 */
            srcDir.resolve("gutils/fsys.c").patch(projectDir.resolve("patches/gutils-fsys.patch"))

            // https://android.googlesource.com/platform/bionic/+/master/docs/status.md
            // New libc functions in P (API level 28):
            // endhostent/endnetent/endprotoent/getnetent/getprotoent/sethostent/setnetent/setprotoent (completing <netdb.h>)
            srcDir.resolve("gutils/gutils.c").patch(projectDir.resolve("patches/gutils-gutils.patch"))

            // Leak some memory by not calling endhostent() and endprotoent()
            // These are deprecated functions, not used in the current upstream version of fontforge
            srcDir.resolve("fontforge/http.c").patch(projectDir.resolve("patches/fontforge-http.patch"))

            // Fix sent upstream:
            // https://github.com/fontforge/fontforge/pull/3746
            // Available in fontforge-20190801
            //
            // android-ndk-r20/toolchains/llvm/prebuilt/linux-x86_64/sysroot/usr/include/langinfo.h:char*
            // #if __ANDROID_API__ >= 26
            // char* nl_langinfo(nl_item __item) __INTRODUCED_IN(26);
            // char* nl_langinfo_l(nl_item __item, locale_t __l) __INTRODUCED_IN(26);
            // #endif /* __ANDROID_API__ >= 26 */
            srcDir.resolve("fontforge/noprefs.c").patch(projectDir.resolve("patches/fontforge-noprefs.NL_LANGINFO.patch"))

            if (minSupportedSdk < 21) {
                // fontforge uses newlocale and localeconv, which are not available on Android pre 21 (Lollipop)
                // locale_t is available, we should not redefine it while using the BAD_LOCALE_HACK in splinefont.h
                //
                // From /usr/include/locale.h:
                // #if __ANDROID_API__ >= 21
                // locale_t duplocale(locale_t __l) __INTRODUCED_IN(21);
                // void freelocale(locale_t __l) __INTRODUCED_IN(21);
                // locale_t newlocale(int __category_mask, const char* __locale_name, locale_t __base) __INTRODUCED_IN(21);
                // #endif /* __ANDROID_API__ >= 21 */
                // ...
                // #if __ANDROID_API__ >= 21
                // struct lconv* localeconv(void) __INTRODUCED_IN(21);
                // #endif /* __ANDROID_API__ >= 21 */
                srcDir.patch(projectDir.resolve("patches/localeconv.patch"))
            }

            // rpl_localtime calls itself until stack exhaustion
            srcDir.resolve("lib/localtime-buffer.c").patch(projectDir.resolve("patches/lib-localtime-buffer.patch"))
        }

        if (portVersion == "20230101") {
            val dollar = "$"
            srcDir.resolve("cmake/packages/FindGLIB.cmake").replace(
                """INTERFACE_LINK_LIBRARIES "$dollar{GLIB_LIBRARIES}")""",
                """
                    INTERFACE_LINK_LIBRARIES "$dollar{PC_GLIB_LIBRARIES}")
                    set_property(TARGET GLIB::GLIB PROPERTY INTERFACE_LINK_DIRECTORIES "$dollar{PC_GLIB_LIBDIR}")
                """.trimIndent()
            )

            srcDir.resolve("CMakeLists.txt").replace(
                "find_package(GLIB 2.6 REQUIRED COMPONENTS gio gobject)",
                "find_package(GLIB 2.6 REQUIRED COMPONENTS gio gobject gmodule)"
            )

            srcDir.resolve("fontforge/CMakeLists.txt").replace(
                "if(BUILD_SHARED_LIBS)",
                "if(TRUE)"
            )
        }
    }
}

tasks.prefab {
    generator.set(PrefabSysrootPlugin::class.java)
}

if (portVersion == "20170731") {
    tasks.register<AutoconfPortTask>("buildPort") {
        val generatedDependencies = prefabGenerated.get().asFile
        autoconf {
            args(
                "--disable-programs",
                "--disable-python-scripting",
                "--disable-python-extension",
                "--without-included-ltdl",
            )

            generatedDependencies.resolve(toolchain.abi.triple).let {
                // libfontforge fails to pick up libpng on its own.
                // Vars generated by PKG_CONFIG_LIBDIR=${it.resolve("lib/pkgconfig")} pkg-config --libs --static libpng

                env["LIBPNG_CFLAGS"] = "-I${it.resolve("include/libpng16")}"
                env["LIBPNG_LIBS"] = "-L${it.resolve("lib")} -lpng16 -lz -lm -lz"

                // Needed to find libtool
                env["CPPFLAGS"] = "-I${it.resolve("include")}"
                env["LDFLAGS"] = "-L${it.resolve("lib")}"
            }
        }
        doLast {
            com.android.ndkports.Abi.values().forEach { abi ->
                installDirectoryFor(abi)
                    .resolve("include/android.${abi.abiName}/lib/pkgconfig/libfontforge.pc")
                    .replace("-ljpeg", "")
                    .replace("-lpng16", "")
                    .replace("-lspiro", "")
                    .replace("-luninameslist", "")
                    .replace(
                        "Requires.private:",
                        "Requires.private: freetype2 intl gio-2.0 libturbojpeg libuninameslist libxml-2.0 libspiro pango libpng16"
                    )
            }
        }
    }
} else {
    // 20200314

    //if (portVersion == "20230101") {
    tasks.register<CMakePortTask>("buildPort") {
        cmake {
            args(
                "-DENABLE_GUI=OFF",
                "-DENABLE_PYTHON_SCRIPTING=OFF",
                "-DENABLE_PYTHON_EXTENSION=OFF",
            )
        }
    }
}


tasks.prefabPackage {
    version.set(CMakeCompatibleVersion.parse(portVersion))

    licensePath.set("LICENSE")

    modules {
        create("fontforge") {
            static.set(project.findProperty("libraryType") == "static")
            includesPerAbi.set(true)
        }
        create("fontforgeexe") {
            static.set(project.findProperty("libraryType") == "static")
            includesPerAbi.set(true)
        }
        create("gioftp") {
            static.set(project.findProperty("libraryType") == "static")
            includesPerAbi.set(true)
        }
        create("gunicode") {
            static.set(project.findProperty("libraryType") == "static")
            includesPerAbi.set(true)
        }
        create("gutils") {
            static.set(project.findProperty("libraryType") == "static")
            includesPerAbi.set(true)
        }
    }
}

val packageSources = tasks.register<Jar>("packageSources") {
    archiveClassifier.set("sources")
    from(projectDir.resolve("build.gradle.kts"))
    from(projectDir.resolve("patches"))
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
                name.set("FontForge")
                description.set("FontForge is a free (libre) font editor for Windows, Mac OS X and GNU+Linux. Use it to create, edit and convert fonts in OpenType, TrueType, UFO, CID-keyed, Multiple Master, and many other formats.")
                url.set("https://fontforge.org")
                licenses {
                    license {
                        name.set("GPLv3")
                        url.set("https://raw.githubusercontent.com/fontforge/fontforge/20230101/COPYING.gplv3")
                        distribution.set("repo")
                    }
                }
                developers {
                    // Developer list obtained from:
                    // https://raw.githubusercontent.com/fontforge/fontforge/20230101/AUTHORS
                    developer {
                        name.set("George Williams")
                    }
                    developer {
                        name.set("pfaedit <pfaedit>")
                    }
                    developer {
                        name.set("Abel Cheung")
                    }
                    developer {
                        name.set("Adam Goode")
                    }
                    developer {
                        name.set("Adolfo Jayme Barrientos")
                    }
                    developer {
                        name.set("Adrien Tétar")
                    }
                    developer {
                        name.set("Aidan Kane <aidankane@gmail.com> <aidos>")
                    }
                    developer {
                        name.set("Aktado <aktado@users.sourceforge.jp>")
                    }
                    developer {
                        name.set("Alan Coopersmith")
                    }
                    developer {
                        name.set("Alex Henrie <alexhenrie>")
                    }
                    developer {
                        name.set("Alex Miller <aalex-millerr>")
                    }
                    developer {
                        name.set("Alex Raftis")
                    }
                    developer {
                        name.set("Alexandre Julliard")
                    }
                    developer {
                        name.set("Alexandre Prokoudine")
                    }
                    developer {
                        name.set("Alexander Pruss")
                    }
                    developer {
                        name.set("Alexey Kuznetsov <catharanthus>")
                    }
                    developer {
                        name.set("Alexey Kryukov (Alexej) - nowakowskittfinstr.c")
                    }
                    developer {
                        name.set("Anand")
                    }
                    developer {
                        name.set("Anders Lund")
                    }
                    developer {
                        name.set("Andreas Larsen")
                    }
                    developer {
                        name.set("Andrew Janke")
                    }
                    developer {
                        name.set("Andrey Panov")
                    }
                    developer {
                        name.set("Anshuman Pandey")
                    }
                    developer {
                        name.set("Anthony G. Basile")
                    }
                    developer {
                        name.set("Arthaey Angosii")
                    }
                    developer {
                        name.set("Barry Schwartz <chemoelectric>")
                    }
                    developer {
                        name.set("Baruch Even <baruch@debian.org>")
                    }
                    developer {
                        name.set("Bastien Dejean")
                    }
                    developer {
                        name.set("Behnam Esfahbod")
                    }
                    developer {
                        name.set("Ben Martin - hotkeys.c, hotkeys.h")
                    }
                    developer {
                        name.set("BensongChen, bungeman")
                    }
                    developer {
                        name.set("Ben Wagner")
                    }
                    developer {
                        name.set("Bernhard M. Wiedemann <bwiedemann@suse.de> <bmwiedemann>")
                    }
                    developer {
                        name.set("Brian Murray <brian@ubuntu.com>")
                    }
                    developer {
                        name.set("Bill Wang")
                    }
                    developer {
                        name.set("Charles M. Hannum")
                    }
                    developer {
                        name.set("Charles Reilly <mungre>")
                    }
                    developer {
                        name.set("Chia-I Wu (Chia)")
                    }
                    developer {
                        name.set("Christopher Meng")
                    }
                    developer {
                        name.set("Claudio Beccari")
                    }
                    developer {
                        name.set("Craig Lennox")
                    }
                    developer {
                        name.set("C.W. Betts <MaddTheSane>")
                    }
                    developer {
                        name.set("Daniel Eggert")
                    }
                    developer {
                        name.set("Daniel Gillmor")
                    }
                    developer {
                        name.set("Dave Crossland (dave@lab6.com)")
                    }
                    developer {
                        name.set("Dave Levine")
                    }
                    developer {
                        name.set("David Binderman")
                    }
                    developer {
                        name.set("David Corbett <dscorbett>")
                    }
                    developer {
                        name.set("David Hedley <davidhedley>")
                    }
                    developer {
                        name.set("David Hull <dhull>")
                    }
                    developer {
                        name.set("David Lee")
                    }
                    developer {
                        name.set("David Lemon")
                    }
                    developer {
                        name.set("David Opstad")
                    }
                    developer {
                        name.set("David Shaal")
                    }
                    developer {
                        name.set("Deron Meranda")
                    }
                    developer {
                        name.set("Divay Prakash")
                    }
                    developer {
                        name.set("Dominyk Tiller")
                    }
                    developer {
                        name.set("Dong Yang")
                    }
                    developer {
                        name.set("Dražen Lučanin")
                    }
                    developer {
                        name.set("Duane Moody")
                    }
                    developer {
                        name.set("E.J. Neafsey")
                    }
                    developer {
                        name.set("Eddie Yuen")
                    }
                    developer {
                        name.set("Edward Betts <edward@4angle.com> <EdwardBetts>")
                    }
                    developer {
                        name.set("Edward Lee")
                    }
                    developer {
                        name.set("Eric Bavier <bavier@member.fsf.org>")
                    }
                    developer {
                        name.set("Eva Denman")
                    }
                    developer {
                        name.set("Fabian Greffrath <fabiangreffrath>")
                    }
                    developer {
                        name.set("Felipe Corrêa da Silva Sanches <felipesanches>")
                    }
                    developer {
                        name.set("Frank Trampe <frank-trampe>")
                    }
                    developer {
                        name.set("Fredrick Brennan <copypaste@kittens.ph> <ctrlcctrlv>")
                    }
                    developer {
                        name.set("Frédéric Wang <fred-wang>")
                    }
                    developer {
                        name.set("Gabriel Holodak <keidax>")
                    }
                    developer {
                        name.set("Georg A. Duffner")
                    }
                    developer {
                        name.set("Gioele Barabucci <gioele>")
                    }
                    developer {
                        name.set("Giuseppe Bilotta")
                    }
                    developer {
                        name.set("Giuseppe Ghibò")
                    }
                    developer {
                        name.set("ggl329")
                    }
                    developer {
                        name.set("Greg Ford")
                    }
                    developer {
                        name.set("Grzegorz")
                    }
                    developer {
                        name.set("Guillermo Robles <guillerobles1995@gmail.com> <wynro>")
                    }
                    developer {
                        name.set("Hanachan Pao <Ishotihadus>")
                    }
                    developer {
                        name.set("Harald Harders")
                    }
                    developer {
                        name.set("Henry Wong <movietravelcode@outlook.com>")
                    }
                    developer {
                        name.set("Herbert Duerr")
                    }
                    developer {
                        name.set("Hideki Yamane <henrich@debian.org> <henrich>")
                    }
                    developer {
                        name.set("Horváth Balázs")
                    }
                    developer {
                        name.set("Ivo Straka <ivo-s>")
                    }
                    developer {
                        name.set("J. Jansen - gv - ANALYZE_MAP.COM")
                    }
                    developer {
                        name.set("Jacob Jansen")
                    }
                    developer {
                        name.set("James Cloos")
                    }
                    developer {
                        name.set("James Crippen")
                    }
                    developer {
                        name.set("James Woodcock")
                    }
                    developer {
                        name.set("Jason Pagura")
                    }
                    developer {
                        name.set("Jens Reyer <jre-wine>")
                    }
                    developer {
                        name.set("Jérémy Bobbio")
                    }
                    developer {
                        name.set("Jeremy Tan")
                    }
                    developer {
                        name.set("Jim Killock")
                    }
                    developer {
                        name.set("Jiwon Choi")
                    }
                    developer {
                        name.set("Joel Santos")
                    }
                    developer {
                        name.set("Johannes Plass - gv = ANALYZE_MAP.COM")
                    }
                    developer {
                        name.set("Johan Winge")
                    }
                    developer {
                        name.set("Jon Banquer")
                    }
                    developer {
                        name.set("Jonathan Hanna <jhps>")
                    }
                    developer {
                        name.set("Jonathan Hue")
                    }
                    developer {
                        name.set("Jonathyn Bet'nct")
                    }
                    developer {
                        name.set("Jose Da Silva <JoesCat>")
                    }
                    developer {
                        name.set("Joshua Richardson")
                    }
                    developer {
                        name.set("Jouk Jansen")
                    }
                    developer {
                        name.set("Juergen Buntrock")
                    }
                    developer {
                        name.set("Julien Nabet <serval2412>")
                    }
                    developer {
                        name.set("Just Fill Bugs <mozbugbox>")
                    }
                    developer {
                        name.set("Kartik Mistry")
                    }
                    developer {
                        name.set("KANOU Hiroki <KANOU> <kanou-h>")
                    }
                    developer {
                        name.set("Kazuki Ohta (Kauzuyuki, Kazuyuki)")
                    }
                    developer {
                        name.set("Kelvinsong")
                    }
                    developer {
                        name.set("Kemie Guaida - Tango PixMaps")
                    }
                    developer {
                        name.set("Kengo Ichiki")
                    }
                    developer {
                        name.set("Kęstutis Biliūnas <kebil@kaunas.init.lt>")
                    }
                    developer {
                        name.set("Kevin Fenzi")
                    }
                    developer {
                        name.set("Khaled Hosny <khaledhosny>")
                    }
                    developer {
                        name.set("Koki Takahashi")
                    }
                    developer {
                        name.set("Kyrylo Yatsenko <hedrok>")
                    }
                    developer {
                        name.set("Lasse Fister")
                    }
                    developer {
                        name.set("L.S. Tikhomrov")
                    }
                    developer {
                        name.set("László Károly")
                    }
                    developer {
                        name.set("Lu Wang <coolwanglu@gmail.com>")
                    }
                    developer {
                        name.set("Ludwig Schwardt")
                    }
                    developer {
                        name.set("Luiz Matheus <lzmths>")
                    }
                    developer {
                        name.set("Maks Naumov <maksqwe1@ukr.net> <maksqwe>")
                    }
                    developer {
                        name.set("Marius Larsen")
                    }
                    developer {
                        name.set("Mark G. Adams")
                    }
                    developer {
                        name.set("Mark Oteiza")
                    }
                    developer {
                        name.set("Markus Schwarzenberg")
                    }
                    developer {
                        name.set("Martin Giese")
                    }
                    developer {
                        name.set("Martin Hosken <martin_hosken@sil.org>")
                    }
                    developer {
                        name.set("Mathias Wollin")
                    }
                    developer {
                        name.set("Matijs van Zuijlen <mvz>")
                    }
                    developer {
                        name.set("Matt Chisholm")
                    }
                    developer {
                        name.set("Matthew Petroff")
                    }
                    developer {
                        name.set("Matthew Skala < mskala>")
                    }
                    developer {
                        name.set("Matthias Klose <doko@ubuntu.com>")
                    }
                    developer {
                        name.set("Mayank Jha")
                    }
                    developer {
                        name.set("Max Rabkin - excepthook.py")
                    }
                    developer {
                        name.set("Maxim Iorsh")
                    }
                    developer {
                        name.set("Michael Gährken")
                    }
                    developer {
                        name.set("Michal Mazurek")
                    }
                    developer {
                        name.set("Michal Nowakowski <michal-n> - nowakowskittfinstr.c")
                    }
                    developer {
                        name.set("Michinari Nukazawa <MichinariNukazawa>")
                    }
                    developer {
                        name.set("midzer <midzer@gmail.com>")
                    }
                    developer {
                        name.set("Mike Gilbert <floppym>")
                    }
                    developer {
                        name.set("MURAOKA Taro")
                    }
                    developer {
                        name.set("Nayana Hettiarachchi <nayana@corp-gems.com> <devzer01>")
                    }
                    developer {
                        name.set("Neil Parker")
                    }
                    developer {
                        name.set("Nelson Beebe")
                    }
                    developer {
                        name.set("Nicolas Kaiser")
                    }
                    developer {
                        name.set("Nicolas Spalinger")
                    }
                    developer {
                        name.set("Nigel Tao <nigeltao@golang.org> <nigeltao>")
                    }
                    developer {
                        name.set("Ondřej Hošek")
                    }
                    developer {
                        name.set("Pander Musubi <Pander>")
                    }
                    developer {
                        name.set("Parag A Nemade <pnemade@fedoraproject.org> <pnemade>")
                    }
                    developer {
                        name.set("Peter Denisevich")
                    }
                    developer {
                        name.set("Peter Facey")
                    }
                    developer {
                        name.set("Petr Gajdos")
                    }
                    developer {
                        name.set("Phil Krylov")
                    }
                    developer {
                        name.set("Phil Lindsay")
                    }
                    developer {
                        name.set("Pierre Hanser")
                    }
                    developer {
                        name.set("Primoz PETERLIN")
                    }
                    developer {
                        name.set("probonopd <probonopd>")
                    }
                    developer {
                        name.set("Qmpel")
                    }
                    developer {
                        name.set("R.L. Horn")
                    }
                    developer {
                        name.set("R.P.C. Rodgers - standard man macros")
                    }
                    developer {
                        name.set("Rafael Ferran i Peralta")
                    }
                    developer {
                        name.set("Rajeesh K Nambiar")
                    }
                    developer {
                        name.set("Ralf Stubner")
                    }
                    developer {
                        name.set("Raph Levien <raphlinus>")
                    }
                    developer {
                        name.set("Reuben Thomas <rrthomas>")
                    }
                    developer {
                        name.set("Richard Hughes")
                    }
                    developer {
                        name.set("Richard Kinch")
                    }
                    developer {
                        name.set("Rikard Falkeborn <rikardfalkeborn>")
                    }
                    developer {
                        name.set("Rob Madole <robmadole>")
                    }
                    developer {
                        name.set("Rogério Brito <rbrito@ime.usp.br>")
                    }
                    developer {
                        name.set("Rogier van Dalen")
                    }
                    developer {
                        name.set("Ryusei Yamaguchi")
                    }
                    developer {
                        name.set("Saeed Rasooli")
                    }
                    developer {
                        name.set("Sergey S Betke <sergey-s-betke>")
                    }
                    developer {
                        name.set("Schrijvers Luc <begasus>")
                    }
                    developer {
                        name.set("Scott Pakin - Packaging")
                    }
                    developer {
                        name.set("Scott Romack")
                    }
                    developer {
                        name.set("Silvan Toledo")
                    }
                    developer {
                        name.set("Skef Iterum <skef>")
                    }
                    developer {
                        name.set("Stefan Wanger")
                    }
                    developer {
                        name.set("Stephane Despret")
                    }
                    developer {
                        name.set("Steve White - ASMO 708 codepage, & more")
                    }
                    developer {
                        name.set("TANIGAWA Takashi <MihailJP>")
                    }
                    developer {
                        name.set("Thayne McCombs")
                    }
                    developer {
                        name.set("Thomas Shinnick")
                    }
                    developer {
                        name.set("Tim Heilig")
                    }
                    developer {
                        name.set("Timothy O. Theisen - gv = ANALYZE_MAP.COM")
                    }
                    developer {
                        name.set("Tobias Mueller <muelli>")
                    }
                    developer {
                        name.set("Tom Harvey - fontforge.1")
                    }
                    developer {
                        name.set("Tom Lechner")
                    }
                    developer {
                        name.set("Uli Schlachter")
                    }
                    developer {
                        name.set("Ulrich Klauer")
                    }
                    developer {
                        name.set("Vadim Belman")
                    }
                    developer {
                        name.set("Valek Filippov")
                    }
                    developer {
                        name.set("Vasudev Kamath <copyninja>")
                    }
                    developer {
                        name.set("Vadim Penzin <vvp2014>")
                    }
                    developer {
                        name.set("Vernon Adams")
                    }
                    developer {
                        name.set("Vilius Sutkus <ViliusSutkus>")
                    }
                    developer {
                        name.set("Ville Skytta")
                    }
                    developer {
                        name.set("Werner Lemberg <lemzwerg>")
                    }
                    developer {
                        name.set("Woensug Eric Choi")
                    }
                    developer {
                        name.set("Wojciech Muła <wm@mahajana.net>")
                    }
                    developer {
                        name.set("Won-kyu Park")
                    }
                    developer {
                        name.set("贤哲 <xiaozhe.hxz@alibaba-inc.com>")
                    }
                    developer {
                        name.set("Yannis Haralambous")
                    }
                    developer {
                        name.set("Yao Wei (魏銘廷)")
                    }
                    developer {
                        name.set("Yuri Chornoivan")
                    }
                    developer {
                        name.set("Yi Yang (ahyangyi)")
                    }
                    developer {
                        name.set("Rafael Ferran i Peralta - ca - Catalan")
                    }
                    developer {
                        name.set("Chris-je - de - German")
                    }
                    developer {
                        name.set("Ettore Atalan - de - German")
                    }
                    developer {
                        name.set("Georg A. Duffner - de - German")
                    }
                    developer {
                        name.set("Philipp Poll - de - German")
                    }
                    developer {
                        name.set("Apostolos Syropoulos - el - Greek")
                    }
                    developer {
                        name.set("George Williams - en_GB")
                    }
                    developer {
                        name.set("Walter Echarri - es - Spanish")
                    }
                    developer {
                        name.set("Adam Coquery - fr - French")
                    }
                    developer {
                        name.set("Jean-René Bastien - fr - French")
                    }
                    developer {
                        name.set("Pierre Hanser - fr - French")
                    }
                    developer {
                        name.set("Thaega <Thaega> - fr - French")
                    }
                    developer {
                        name.set("Yannis Haralambous - fr - French")
                    }
                    developer {
                        name.set("Milo Ivir <milotype> - hr - Croatian")
                    }
                    developer {
                        name.set("Claudio Beccari - it - Italian")
                    }
                    developer {
                        name.set("Martino Deotto - it - Italian")
                    }
                    developer {
                        name.set("KANOU Hiroki - ja - Japanese")
                    }
                    developer {
                        name.set("TANIGAWA Takashi - ja - Japanese")
                    }
                    developer {
                        name.set("OKANO Takayoshi - ja - Japanese")
                    }
                    developer {
                        name.set("Woensug Eric Choi - ko - Korean")
                    }
                    developer {
                        name.set("Hiran - ml - Malayalam")
                    }
                    developer {
                        name.set("Michal Nowakowski - po - Polish")
                    }
                    developer {
                        name.set("Jose Da Silva - pt - Portuguese")
                    }
                    developer {
                        name.set("Alexandre Prokoudine - ru - Russian")
                    }
                    developer {
                        name.set("Valek Filippov - ru - Russian")
                    }
                    developer {
                        name.set("Yuri Chornoivan - uk - Ukrainian")
                    }
                    developer {
                        name.set("Clytie Siddall - vi - Vietnamese")
                    }
                    developer {
                        name.set("Lee Chenhwa - zh_CN - Chinese")
                    }
                    developer {
                        name.set("Wei-Lun Chao - zh_TW - Traditional Chinese")
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
