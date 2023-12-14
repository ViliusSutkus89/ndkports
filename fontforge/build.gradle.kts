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
        version = "20170731-beta-5"
        "20170731"
    }
    "20200314" -> {
        version = "20200314-beta-9"
        "20200314"
    }
    else /* "20230101" */ -> {
        version = "20230101-beta-8"
        "20230101"
    }
}

plugins {
    id("maven-publish")
    id("signing")
    id("com.android.ndkports.NdkPorts")
}

val minSupportedSdk = rootProject.extra.get("minSdkSupportedByNdk").toString().toInt()

// 20200314 and later requires complex math functions ( csqrt/csqrt/creal/cimag )
// that are available only from API level 24 . Use OpenLibm instead
val usingOpenLibm = portVersion != "20170731" && minSupportedSdk < 24

dependencies {
    val ndkVersionSuffix = rootProject.extra.get("ndkVersionSuffix")
    implementation("com.viliussutkus89.ndk.thirdparty:cairo${ndkVersionSuffix}-static:1.18.0-beta-3")
    implementation("com.viliussutkus89.ndk.thirdparty:freetype${ndkVersionSuffix}-static:2.13.2-beta-5")
    implementation("com.viliussutkus89.ndk.thirdparty:proxy-libintl${ndkVersionSuffix}-static:0.4.1.1")
    implementation("com.viliussutkus89.ndk.thirdparty:glib2${ndkVersionSuffix}-static:2.78.1-beta-4")
    implementation("com.viliussutkus89.ndk.thirdparty:libjpeg-turbo${ndkVersionSuffix}-static:3.0.1-beta-2")
    implementation("com.viliussutkus89.ndk.thirdparty:libtool${ndkVersionSuffix}-static:2.4.6-beta-2")
    implementation("com.viliussutkus89.ndk.thirdparty:libpng${ndkVersionSuffix}-static:1.6.40-beta-5")
    implementation("com.viliussutkus89.ndk.thirdparty:libuninameslist${ndkVersionSuffix}-static:20230916-beta-2")
    implementation("com.viliussutkus89.ndk.thirdparty:libxml2${ndkVersionSuffix}-static:2.11.5-beta-4")
    implementation("com.viliussutkus89.ndk.thirdparty:spiro${ndkVersionSuffix}-static:20221101-beta-2")
    implementation("com.viliussutkus89.ndk.thirdparty:pango${ndkVersionSuffix}-static:1.51.0-beta-4")

    if (portVersion != "20170731") {
        // libfontforge checks for TIFFRewriteField , which was deprecated in libtiff-4
        // http://www.simplesystems.org/libtiff/v4.0.0.html
        implementation("com.viliussutkus89.ndk.thirdparty:libtiff${ndkVersionSuffix}-static:4.6.0-beta-4")
    }

    if (usingOpenLibm) {
        implementation("com.viliussutkus89.ndk.thirdparty:openlibm${ndkVersionSuffix}-static:0.8.1-beta-1")
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

fun File.patch(patch: String) {
    patch(projectDir.resolve("patches/$portVersion").resolve(patch))
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

        when (portVersion) {
            "20170731" -> {
                // Use libtool from gradle dependencies
                srcDir.resolve("libltdl/ltdl.h").delete()

                // pthread_cancel unavailable on Android
                // @TODO: implement a proper workaround
                srcDir.resolve("gutils/gio.c").patch("gutils-gio.no-pthread-cancel.patch")

                // fontforgeexe/startnoui.c
                srcDir.resolve("fontforgeexe/startnoui.c").patch("fontforgeexe-startnoui.FindOrMakeEncoding.patch")

                // https://android.googlesource.com/platform/bionic/+/master/docs/32-bit-abi.md
                srcDir.resolve("config.h.in").patch("file_offset_bits.patch")

                // android-ndk-r20/toolchains/llvm/prebuilt/linux-x86_64/sysroot/usr/include/pwd.h
                // #if __ANDROID_API__ >= 26
                // struct passwd* getpwent(void) __INTRODUCED_IN(26);
                // void setpwent(void) __INTRODUCED_IN(26);
                // void endpwent(void) __INTRODUCED_IN(26);
                // #endif /* __ANDROID_API__ >= 26 */
                srcDir.resolve("gutils/fsys.c").patch("gutils-fsys.patch")

                // https://android.googlesource.com/platform/bionic/+/master/docs/status.md
                // New libc functions in P (API level 28):
                // endhostent/endnetent/endprotoent/getnetent/getprotoent/sethostent/setnetent/setprotoent (completing <netdb.h>)
                srcDir.resolve("gutils/gutils.c").patch("gutils-gutils.patch")

                // Leak some memory by not calling endhostent() and endprotoent()
                // These are deprecated functions, not used in the current upstream version of fontforge
                srcDir.resolve("fontforge/http.c").patch("fontforge-http.patch")

                // Fix sent upstream:
                // https://github.com/fontforge/fontforge/pull/3746
                // Available in fontforge-20190801
                //
                // android-ndk-r20/toolchains/llvm/prebuilt/linux-x86_64/sysroot/usr/include/langinfo.h:char*
                // #if __ANDROID_API__ >= 26
                // char* nl_langinfo(nl_item __item) __INTRODUCED_IN(26);
                // char* nl_langinfo_l(nl_item __item, locale_t __l) __INTRODUCED_IN(26);
                // #endif /* __ANDROID_API__ >= 26 */
                srcDir.resolve("fontforge/noprefs.c").patch("fontforge-noprefs.NL_LANGINFO.patch")

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
                    srcDir.patch("localeconv.patch")
                }

                // rpl_localtime calls itself until stack exhaustion
                srcDir.resolve("lib/localtime-buffer.c").patch("lib-localtime-buffer.patch")
            }
            "20200314" -> {
                srcDir.resolve("inc/CMakeLists.txt").patch("include-iconv.patch")

                srcDir.patch("pie.patch")

                // android-ndk-r20/toolchains/llvm/prebuilt/linux-x86_64/sysroot/usr/include/pwd.h
                // #if __ANDROID_API__ >= 26
                // struct passwd* getpwent(void) __INTRODUCED_IN(26);
                // void setpwent(void) __INTRODUCED_IN(26);
                // void endpwent(void) __INTRODUCED_IN(26);
                // #endif /* __ANDROID_API__ >= 26 */
                srcDir.resolve("gutils/fsys.c").patch("gutils-fsys.patch")

                srcDir.patch("FindGLib.patch")

                srcDir.patch("InstallLibrary.patch")

                if (usingOpenLibm) {
                    srcDir.resolve("fontforge/splinestroke.c").patch("splinestroke-complex-math.patch")
                    projectDir.resolve("patches/$portVersion/FindMathLib.cmake").copyTo(
                        target = srcDir.resolve("cmake/packages/FindMathLib.cmake"),
                        overwrite = true
                    )
                }

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
                    srcDir.patch("localeconv.patch")
                }
            }
            "20230101" -> {
                srcDir.patch("pie.patch")

                srcDir.patch("FindGLib.patch")

                // android-ndk-r20/toolchains/llvm/prebuilt/linux-x86_64/sysroot/usr/include/pwd.h
                // #if __ANDROID_API__ >= 26
                // struct passwd* getpwent(void) __INTRODUCED_IN(26);
                // void setpwent(void) __INTRODUCED_IN(26);
                // void endpwent(void) __INTRODUCED_IN(26);
                // #endif /* __ANDROID_API__ >= 26 */
                srcDir.resolve("gutils/fsys.c").patch("gutils-fsys.patch")

                srcDir.patch("InstallLibrary.patch")

                if (usingOpenLibm) {
                    srcDir.resolve("fontforge/splinestroke.c").patch("splinestroke-complex-math.patch")
                    projectDir.resolve("patches/$portVersion/FindMathLib.cmake").copyTo(
                        target = srcDir.resolve("cmake/packages/FindMathLib.cmake"),
                        overwrite = true
                    )
                }

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
                    srcDir.patch("localeconv.patch")
                }
            }
        }
    }
}

tasks.prefab {
    generator.set(PrefabSysrootPlugin::class.java)
}

when (portVersion) {
    "20170731" -> {
        tasks.register<AutoconfPortTask>("buildPort") {
            val generatedDependencies = prefabGenerated.get().asFile
            autoconf {
                args(
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
                    val pkgConfigDir = installDirectoryFor(abi).resolve("lib/pkgconfig")
                    pkgConfigDir.resolve("libfontforge.pc")
                        // Add Missing Requires:
                        .replace(
                            "Requires:",
                            "Requires: freetype2 intl gio-2.0 libxml-2.0 pango"
                        )
                        // Pull dependencies as Requires:, instead of Libs:
                        .replace("-ljpeg", "")
                        .replace("-lpng16", "")
                        .replace("-lspiro", "")
                        .replace("-luninameslist", "")
                        .replace(
                            "Requires:",
                            "Requires: libturbojpeg libpng16 libspiro libuninameslist"
                        )
                    pkgConfigDir.resolve("libfontforgeexe.pc")
                        // Add Missing Requires:
                        .replace(
                            "Requires:",
                            "Requires: freetype2 intl gio-2.0 libxml-2.0 pango"
                        )
                        // Pull dependencies as Requires:, instead of Libs:
                        .replace("-ljpeg", "")
                        .replace("-lpng16", "")
                        .replace("-lspiro", "")
                        .replace("-luninameslist", "")
                        .replace(
                            "Requires:",
                            "Requires: libturbojpeg libpng16 libspiro libuninameslist"
                        )
                }
            }
        }
    }
    "20200314" -> {
        tasks.register<CMakePortTask>("buildPort") {
            cmake {
                args(
                    "-DENABLE_GUI=OFF",
                    "-DENABLE_PYTHON_SCRIPTING=OFF",
                    "-DENABLE_PYTHON_EXTENSION=OFF",
                )
            }
            doLast {
                val pkgconfig = projectDir.resolve("patches/$portVersion/libfontforge.pc")
                com.android.ndkports.Abi.values().forEach { abi ->
                    pkgconfig.copyTo(
                        target = installDirectoryFor(abi)
                            .resolve("lib/pkgconfig").apply { mkdir() }
                            .resolve("libfontforge.pc"),
                        overwrite = true
                    )
                }
            }
        }
    }
    else -> /* "20230101" */ {
        tasks.register<CMakePortTask>("buildPort") {
            cmake {
                args(
                    "-DENABLE_GUI=OFF",
                    "-DENABLE_PYTHON_SCRIPTING=OFF",
                    "-DENABLE_PYTHON_EXTENSION=OFF",
                )
            }
            doLast {
                val pkgconfig = projectDir.resolve("patches/$portVersion/libfontforge.pc")
                com.android.ndkports.Abi.values().forEach { abi ->
                    pkgconfig.copyTo(
                        target = installDirectoryFor(abi)
                            .resolve("lib/pkgconfig").apply { mkdir() }
                            .resolve("libfontforge.pc"),
                        overwrite = true
                    )
                }
            }
        }
    }
}

tasks.prefabPackage {
    version.set(CMakeCompatibleVersion.parse(portVersion))

    licensePath.set("LICENSE")

    dependencies.set(mutableMapOf(
        "cairo" to "1",
        "freetype" to "1",
        "proxy-libintl" to "1",
        "glib2" to "1",
        "libjpeg-turbo" to "1",
        "libtool" to "1",
        "libpng" to "1",
        "libuninameslist" to "1",
        "libxml2" to "1",
        "spiro" to "1",
        "pango" to "1",
    ).apply {
        if (portVersion != "20170731") {
            put("libtiff", "1")
        }
        if (usingOpenLibm) {
            put("openlibm", "1")
        }
    })

    modules {
        val isStatic = project.findProperty("libraryType") == "static"
        when (portVersion) {
            "20170731" -> {
                create("fontforge") {
                    static.set(isStatic)
                    includesPerAbi.set(true)
                    dependencies.set(listOf(
                        ":gioftp",
                        ":gutils",
                        ":gunicode",
                        "z",
                        "m",
                        "//libtool:ltdl",
                        "//libjpeg-turbo:jpeg",
                        "//libpng:png16",
                        "//spiro:spiro",
                        "//libuninameslist:uninameslist",
                        "//freetype:freetype",
                        "//proxy-libintl:intl",
                        "//glib2:gio-2.0",
                        "//libxml2:xml2",
                        "//pango:pango-1.0",
                        "//cairo:cairo",
                    ))
                }
                create("fontforgeexe") {
                    static.set(isStatic)
                    includesPerAbi.set(true)
                    dependencies.set(listOf(
                        ":fontforge",
                        ":gioftp",
                        ":gutils",
                        ":gunicode",
                        "z",
                        "m",
                        "//libtool:ltdl",
                        "//libjpeg-turbo:jpeg",
                        "//libpng:png16",
                        "//spiro:spiro",
                        "//libuninameslist:uninameslist",
                        "//freetype:freetype",
                        "//proxy-libintl:intl",
                        "//glib2:gio-2.0",
                        "//libxml2:xml2",
                        "//pango:pango-1.0",
                        "//cairo:cairo",
                    ))
                }
                create("gioftp") {
                    static.set(isStatic)
                    includesPerAbi.set(true)
                }
                create("gunicode") {
                    static.set(isStatic)
                    includesPerAbi.set(true)
                }
                create("gutils") {
                    static.set(isStatic)
                    includesPerAbi.set(true)
                }
            }
            "20200314" -> {
                create("fontforge") {
                    static.set(isStatic)
                    includesPerAbi.set(true)
                    dependencies.set(listOf(
                        "z",
                        "//openlibm:openlibm",
                        "//libtool:ltdl",
                        "//libjpeg-turbo:jpeg",
                        "//libpng:png16",
                        "//spiro:spiro",
                        "//libuninameslist:uninameslist",
                        "//freetype:freetype",
                        "//proxy-libintl:intl",
                        "//glib2:gio-2.0",
                        "//libxml2:xml2",
                        "//pango:pango-1.0",
                        "//cairo:cairo",
                        "//libtiff:tiff",
                    ))
                }
            }
            "20230101" -> {
                create("fontforge") {
                    static.set(isStatic)
                    includesPerAbi.set(true)
                    dependencies.set(listOf(
                        "z",
                        "//openlibm:openlibm",
                        "//libtool:ltdl",
                        "//libjpeg-turbo:jpeg",
                        "//libpng:png16",
                        "//spiro:spiro",
                        "//libuninameslist:uninameslist",
                        "//freetype:freetype",
                        "//proxy-libintl:intl",
                        "//glib2:gio-2.0",
                        "//libxml2:xml2",
                        "//pango:pango-1.0",
                        "//cairo:cairo",
                        "//libtiff:tiff",
                    ))
                }
            }
        }
    }
}

val packageSources = tasks.register<Jar>("packageSources") {
    archiveClassifier.set("sources")
    from(projectDir.resolve("build.gradle.kts"))
    from(projectDir.resolve("patches/$portVersion"))
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
                        name.set("GPLv3-or-later")
                        url.set("https://raw.githubusercontent.com/fontforge/fontforge/${portVersion}/LICENSE")
                        distribution.set("repo")
                    }
                    license {
                        name.set("revised-BSD")
                        url.set("https://raw.githubusercontent.com/fontforge/fontforge/${portVersion}/LICENSE")
                        distribution.set("repo")
                    }
                }
                developers {
                    // Developer list obtained from:
                    // https://raw.githubusercontent.com/fontforge/fontforge/20170731/AUTHORS
                    developer {
                        name.set("George Williams")
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
                        name.set("Aktado")
                        email.set("aktado@users.sourceforge.jp")
                    }
                    developer {
                        name.set("Alex Miller")
                        id.set("aalex-millerr")
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
                        name.set("Alexey Kuznetsov")
                        id.set("catharanthus")
                    }
                    developer {
                        name.set("Alexey Kryukov")
                        id.set("Alexej")
                    }
                    developer {
                        name.set("Anand")
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
                        name.set("Anthony G. Basile")
                    }
                    developer {
                        name.set("Arthaey Angosii")
                    }
                    developer {
                        name.set("Barry Schwartz")
                        id.set("chemoelectric")
                    }
                    developer {
                        name.set("Baruch Even")
                        email.set("baruch@debian.org")
                    }
                    developer {
                        name.set("Bastien Dejean")
                    }
                    developer {
                        name.set("Behnam Esfahbod")
                    }
                    developer {
                        name.set("Ben Martin")
                    }
                    developer {
                        name.set("BensongChen")
                        id.set("bungeman")
                    }
                    developer {
                        name.set("Ben Wagner")
                    }
                    developer {
                        name.set("Bill Wang")
                    }
                    developer {
                        name.set("Charles M. Hannum")
                    }
                    developer {
                        name.set("Charles Reilly")
                        id.set("mungre")
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
                        name.set("Dave Crossland")
                        email.set("dave@lab6.com")
                    }
                    developer {
                        name.set("Dave Levine")
                    }
                    developer {
                        name.set("David Hedley")
                        id.set("davidhedley")
                    }
                    developer {
                        name.set("David Hull")
                        id.set("dhull")
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
                        name.set("Edward Lee")
                    }
                    developer {
                        name.set("Eric Bavier")
                        email.set("bavier@member.fsf.org")
                    }
                    developer {
                        name.set("Eva Denman")
                    }
                    developer {
                        name.set("Fabian Greffrath")
                        id.set("fabiangreffrath")
                    }
                    developer {
                        name.set("Felipe Corrêa da Silva Sanches")
                        id.set("felipesanches")
                    }
                    developer {
                        name.set("Frank Trampe")
                        id.set("frank-trampe")
                    }
                    developer {
                        name.set("Frédéric Wang")
                        id.set("fred-wang")
                    }
                    developer {
                        name.set("Georg A. Duffner")
                    }
                    developer {
                        name.set("Gioele Barabucci")
                        id.set("gioele")
                    }
                    developer {
                        name.set("Giuseppe Bilotta")
                    }
                    developer {
                        name.set("Giuseppe Ghibò")
                    }
                    developer {
                        name.set("Greg Ford")
                    }
                    developer {
                        name.set("Grzegorz")
                    }
                    developer {
                        name.set("Harald Harders")
                    }
                    developer {
                        name.set("Horváth Balázs")
                    }
                    developer {
                        name.set("J. Jansen")
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
                        name.set("Jens Reyer")
                        id.set("jre-wine")
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
                        name.set("Johannes Plass")
                    }
                    developer {
                        name.set("Johan Winge")
                    }
                    developer {
                        name.set("Jon Banquer")
                    }
                    developer {
                        name.set("Jonathan Hanna")
                        id.set("jhps")
                    }
                    developer {
                        name.set("Jonathan Hue")
                    }
                    developer {
                        name.set("Jose Da Silva")
                        id.set("JoesCat")
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
                        name.set("Julien Nabet")
                        id.set("serval2412")
                    }
                    developer {
                        name.set("Just Fill Bugs")
                        id.set("mozbugbox")
                    }
                    developer {
                        name.set("Kartik Mistry")
                    }
                    developer {
                        name.set("KANOU Hiroki")
                        id.set("kanou-h")
                    }
                    developer {
                        name.set("Kazuki Ohta (Kauzuyuki, Kazuyuki)")
                    }
                    developer {
                        name.set("Kelvinsong")
                    }
                    developer {
                        name.set("Kemie Guaida")
                    }
                    developer {
                        name.set("Kengo Ichiki")
                    }
                    developer {
                        name.set("Kęstutis Biliūnas")
                        email.set("kebil@kaunas.init.lt")
                    }
                    developer {
                        name.set("Kevin Fenzi")
                    }
                    developer {
                        name.set("Khaled Hosny")
                        id.set("khaledhosny")
                    }
                    developer {
                        name.set("Koki Takahashi")
                    }
                    developer {
                        name.set("Kyrylo Yatsenko")
                        id.set("hedrok")
                    }
                    developer {
                        name.set("Lasse Fister")
                    }
                    developer {
                        name.set("László Károly")
                    }
                    developer {
                        name.set("Lu Wang")
                        email.set("coolwanglu@gmail.com")
                    }
                    developer {
                        name.set("Ludwig Schwardt")
                    }
                    developer {
                        name.set("Luiz Matheus")
                        id.set("lzmths")
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
                        name.set("Martin Hosken")
                        email.set("martin_hosken@sil.org")
                    }
                    developer {
                        name.set("Mathias Wollin")
                    }
                    developer {
                        name.set("Matijs van Zuijlen")
                        id.set("mvz")
                    }
                    developer {
                        name.set("Matt Chisholm")
                    }
                    developer {
                        name.set("Matthew Petroff")
                    }
                    developer {
                        name.set("Matthew Skala")
                        id.set("mskala")
                    }
                    developer {
                        name.set("Matthias Klose")
                        email.set("doko@ubuntu.com")
                    }
                    developer {
                        name.set("Mayank Jha")
                    }
                    developer {
                        name.set("Max Rabkin")
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
                        name.set("Michal Nowakowski")
                        id.set("michal-n")
                    }
                    developer {
                        name.set("Michinari Nukazawa")
                        id.set("MichinariNukazawa")
                    }
                    developer {
                        id.set("midzer")
                        email.set("midzer@gmail.com")
                    }
                    developer {
                        name.set("MURAOKA Taro")
                    }
                    developer {
                        name.set("Nicolas Kaiser")
                    }
                    developer {
                        name.set("Nicolas Spalinger")
                    }
                    developer {
                        name.set("Ondřej Hošek")
                    }
                    developer {
                        name.set("Pander Musubi")
                        id.set("Pander")
                    }
                    developer {
                        name.set("Parag A Nemade")
                        id.set("pnemade")
                        email.set("pnemade@fedoraproject.org")
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
                        id.set("Qmpel")
                    }
                    developer {
                        name.set("R.L. Horn")
                    }
                    developer {
                        name.set("R.P.C. Rodgers")
                    }
                    developer {
                        name.set("Rafael Ferran i Peralta")
                    }
                    developer {
                        name.set("Rajeesh K Nambiar")
                    }
                    developer {
                        name.set("Raph Levien")
                        id.set("raphlinus")
                    }
                    developer {
                        name.set("Reuben Thomas")
                        id.set("rrthomas")
                    }
                    developer {
                        name.set("Richard Hughes")
                    }
                    developer {
                        name.set("Rob Madole")
                        id.set("robmadole")
                    }
                    developer {
                        name.set("Rogério Brito")
                        email.set("rbrito@ime.usp.br")
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
                        name.set("Sergey S Betke")
                        id.set("sergey-s-betke")
                    }
                    developer {
                        name.set("Scott Pakin")
                    }
                    developer {
                        name.set("Scott Romack")
                    }
                    developer {
                        name.set("Silvan Toledo")
                    }
                    developer {
                        name.set("Steve White")
                    }
                    developer {
                        name.set("TANIGAWA Takashi")
                        id.set("MihailJP")
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
                        name.set("Timothy O. Theisen")
                    }
                    developer {
                        name.set("Tom Harvey")
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
                        name.set("Vasudev Kamath")
                        id.set("copyninja")
                    }
                    developer {
                        name.set("Vernon Adams")
                    }
                    developer {
                        name.set("Ville Skytta")
                    }
                    developer {
                        name.set("Werner Lemberg")
                        id.set("lemzwerg")
                    }
                    developer {
                        name.set("Woensug Eric Choi")
                    }
                    developer {
                        name.set("Wojciech Muła")
                        email.set("wm@mahajana.net")
                    }
                    developer {
                        name.set("贤哲")
                        email.set("xiaozhe.hxz@alibaba-inc.com")
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
                        name.set("Rafael Ferran i Peralta")
                    }
                    developer {
                        name.set("Ettore Atalan")
                    }
                    developer {
                        name.set("Philipp Poll")
                    }
                    developer {
                        name.set("Apostolos Syropoulos")
                    }
                    developer {
                        name.set("George Williams")
                    }
                    developer {
                        name.set("Walter Echarri")
                    }
                    developer {
                        name.set("Jean-René Bastien")
                    }
                    developer {
                        name.set("Pierre Hanser")
                    }
                    developer {
                        name.set("Yannis Haralambous")
                    }
                    developer {
                        name.set("Claudio Beccari")
                    }
                    developer {
                        name.set("KANOU Hiroki")
                    }
                    developer {
                        name.set("TANIGAWA Takashi")
                    }
                    developer {
                        name.set("OKANO Takayoshi")
                    }
                    developer {
                        name.set("Woensug Eric Choi")
                    }
                    developer {
                        name.set("Hiran")
                    }
                    developer {
                        name.set("Michal Nowakowski")
                    }
                    developer {
                        name.set("Jose Da Silva")
                    }
                    developer {
                        name.set("Alexandre Prokoudine")
                    }
                    developer {
                        name.set("Valek Filippov")
                    }
                    developer {
                        name.set("Yuri Chornoivan")
                    }
                    developer {
                        name.set("Clytie Siddall")
                    }
                    developer {
                        name.set("Lee Chenhwa")
                    }
                    developer {
                        name.set("Wei-Lun Chao")
                    }

                    // https://raw.githubusercontent.com/fontforge/fontforge/20200314/AUTHORS
                    // https://raw.githubusercontent.com/fontforge/fontforge/20230101/AUTHORS
                    if (portVersion == "20200314" || portVersion == "20230101") {
                        developer {
                            name.set("pfaedit")
                            id.set("pfaedit")
                        }
                        developer {
                            name.set("Aidan Kane")
                            email.set("aidankane@gmail.com")
                            id.set("aidos")
                        }
                        developer {
                            name.set("Alan Coopersmith")
                        }
                        developer {
                            name.set("Alex Henrie")
                            id.set("alexhenrie")
                        }
                        developer {
                            name.set("Alexander Pruss")
                        }
                        developer {
                            name.set("Anders Lund")
                        }
                        developer {
                            name.set("Anshuman Pandey")
                        }
                        developer {
                            name.set("Bernhard M. Wiedemann")
                            email.set("bwiedemann@suse.de")
                            id.set("bmwiedemann")
                        }
                        developer {
                            name.set("Brian Murray")
                            email.set("brian@ubuntu.com")
                        }
                        developer {
                            name.set("Chia-I Wu (Chia)")
                        }
                        developer {
                            name.set("C.W. Betts")
                            id.set("MaddTheSane")
                        }
                        developer {
                            name.set("Daniel Eggert")
                        }
                        developer {
                            name.set("Daniel Gillmor")
                        }
                        developer {
                            name.set("David Binderman")
                        }
                        developer {
                            name.set("David Corbett")
                            id.set("dscorbett")
                        }
                        developer {
                            name.set("David Lee")
                        }
                        developer {
                            name.set("Edward Betts")
                            email.set("edward@4angle.com")
                            id.set("EdwardBetts")
                        }
                        developer {
                            name.set("Fredrick Brennan")
                            email.set("copypaste@kittens.ph")
                            id.set("ctrlcctrlv")
                        }
                        developer {
                            name.set("Gabriel Holodak")
                            id.set("keidax")
                        }
                        developer {
                            name.set("ggl329")
                        }
                        developer {
                            name.set("Guillermo Robles")
                            email.set("guillerobles1995@gmail.com")
                            id.set("wynro")
                        }
                        developer {
                            name.set("Hanachan Pao")
                            id.set("Ishotihadus")
                        }
                        developer {
                            name.set("Henry Wong")
                            id.set("movietravelcode@outlook.com")
                        }
                        developer {
                            name.set("Herbert Duerr")
                        }
                        developer {
                            name.set("Hideki Yamane")
                            email.set("henrich@debian.org")
                            id.set("henrich")
                        }
                        developer {
                            name.set("Ivo Straka")
                            id.set("ivo-s")
                        }
                        developer {
                            name.set("Jonathyn Bet'nct")
                        }
                        developer {
                            name.set("L.S. Tikhomrov")
                        }
                        developer {
                            name.set("Maks Naumov")
                            email.set("maksqwe1@ukr.net")
                            id.set("maksqwe")
                        }
                        developer {
                            name.set("Mike Gilbert")
                            id.set("floppym")
                        }
                        developer {
                            name.set("Nayana Hettiarachchi")
                            email.set("nayana@corp-gems.com")
                            id.set("devzer01")
                        }
                        developer {
                            name.set("Neil Parker")
                        }
                        developer {
                            name.set("Nelson Beebe")
                        }
                        developer {
                            name.set("Nigel Tao")
                            email.set("nigeltao@golang.org")
                            id.set("nigeltao")
                        }
                        developer {
                            name.set("Peter Denisevich")
                        }
                        developer {
                            name.set("Peter Facey")
                        }
                        developer {
                            name.set("probonopd")
                            id.set("probonopd")
                        }
                        developer {
                            name.set("Ralf Stubner")
                        }
                        developer {
                            name.set("Richard Kinch")
                        }
                        developer {
                            name.set("Rikard Falkeborn")
                            id.set("rikardfalkeborn")
                        }
                        developer {
                            name.set("Schrijvers Luc")
                            id.set("begasus")
                        }
                        developer {
                            name.set("Skef Iterum")
                            id.set("skef")
                        }
                        developer {
                            name.set("Stefan Wanger")
                        }
                        developer {
                            name.set("Stephane Despret")
                        }
                        developer {
                            name.set("TANIGAWA Takashi")
                            id.set("MihailJP")
                        }
                        developer {
                            name.set("Tobias Mueller")
                            id.set("muelli")
                        }
                        developer {
                            name.set("Vadim Penzin")
                            id.set("vvp2014")
                        }
                        developer {
                            name.set("Vilius Sutkus")
                            id.set("ViliusSutkus")
                        }
                        developer {
                            name.set("Won-kyu Park")
                        }
                        developer {
                            name.set("Chris-je")
                        }
                        developer {
                            name.set("Ettore Atalan")
                        }
                        developer {
                            name.set("Georg A. Duffner")
                        }
                        developer {
                            name.set("Philipp Poll")
                        }
                        developer {
                            name.set("Adam Coquery")
                        }
                        developer {
                            name.set("Thaega")
                            id.set("Thaega")
                        }
                        developer {
                            name.set("Milo Ivir")
                            id.set("milotype")
                        }
                        developer {
                            name.set("Martino Deotto")
                        }
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
