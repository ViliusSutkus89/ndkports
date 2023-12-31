# ndkports

A collection of Android build scripts for various open source libraries and the
tooling to build them.

Buildscripts are based on Google's [ndkports](https://android.googlesource.com/platform/tools/ndkports/).

Compiled binaries are distributed through MavenCentral.

Most packages aim to deliver up-to-date versions, unless specified otherwise.

## Matrix

Each port is built on a matrix of NDK versions and library type.

- com.viliussutkus89.ndk.thirdparty:libfoo-ndk26-static:0.4.1
- com.viliussutkus89.ndk.thirdparty:libfoo-ndk26-shared:0.4.1
- com.viliussutkus89.ndk.thirdparty:libfoo-ndk26-shared-with-shared-deps:0.4.1
- com.viliussutkus89.ndk.thirdparty:libfoo-ndk25-static:0.4.1
- com.viliussutkus89.ndk.thirdparty:libfoo-ndk25-shared:0.4.1
- com.viliussutkus89.ndk.thirdparty:libfoo-ndk25-shared-with-shared-deps:0.4.1

#### Min SDK Version:

Builds compiled with NDK-26 support Android SDK 21 (Lollipop) and later.

Builds compiled with NDK-25 support Android SDK 19 (KitKat) and later.

Libraries built with different NDK versions should not be used in the same application.

#### Libraries are built as:

- static (libfoo.a) with all dependencies linked as static libraries too,
- shared (libfoo.so) with all dependencies linked as static libraries,
- shared-with-shared-deps (libfoo.so) with all dependencies linked as shared libraries too.

## Ports

#### [GLib](https://gitlab.gnome.org/GNOME/glib/)

Released versions 2.78.1 and 2.78.3 are problematic (issue #20).
Usable version is 2.75.0

[![glib2](https://github.com/ViliusSutkus89/ndkports/actions/workflows/glib2.yml/badge.svg)](https://github.com/ViliusSutkus89/ndkports/actions/workflows/glib2.yml)
[![Maven Central](https://img.shields.io/maven-central/v/com.viliussutkus89.ndk.thirdparty/glib2-ndk25-static.svg?label=Maven%20Central%20glib2-ndk25-static)](https://search.maven.org/search?q=g:com.viliussutkus89.ndk.thirdparty%20AND%20a:glib2-ndk25-static)
[![Maven Central](https://img.shields.io/maven-central/v/com.viliussutkus89.ndk.thirdparty/glib2-ndk25-shared.svg?label=Maven%20Central%20glib2-ndk25-shared)](https://search.maven.org/search?q=g:com.viliussutkus89.ndk.thirdparty%20AND%20a:glib2-ndk25-shared)
[![Maven Central](https://img.shields.io/maven-central/v/com.viliussutkus89.ndk.thirdparty/glib2-ndk26-static.svg?label=Maven%20Central%20glib2-ndk26-static)](https://search.maven.org/search?q=g:com.viliussutkus89.ndk.thirdparty%20AND%20a:glib2-ndk26-static)
[![Maven Central](https://img.shields.io/maven-central/v/com.viliussutkus89.ndk.thirdparty/glib2-ndk26-shared.svg?label=Maven%20Central%20glib2-ndk26-shared)](https://search.maven.org/search?q=g:com.viliussutkus89.ndk.thirdparty%20AND%20a:glib2-ndk26-shared)

#### [libpng](http://libpng.org/pub/png/libpng.html)

[![libpng](https://github.com/ViliusSutkus89/ndkports/actions/workflows/libpng.yml/badge.svg)](https://github.com/ViliusSutkus89/ndkports/actions/workflows/libpng.yml)
[![Maven Central](https://img.shields.io/maven-central/v/com.viliussutkus89.ndk.thirdparty/libpng-ndk25-static.svg?label=Maven%20Central%20libpng-ndk25-static)](https://search.maven.org/search?q=g:com.viliussutkus89.ndk.thirdparty%20AND%20a:libpng-ndk25-static)
[![Maven Central](https://img.shields.io/maven-central/v/com.viliussutkus89.ndk.thirdparty/libpng-ndk25-shared.svg?label=Maven%20Central%20libpng-ndk25-shared)](https://search.maven.org/search?q=g:com.viliussutkus89.ndk.thirdparty%20AND%20a:libpng-ndk25-shared)
[![Maven Central](https://img.shields.io/maven-central/v/com.viliussutkus89.ndk.thirdparty/libpng-ndk26-static.svg?label=Maven%20Central%20libpng-ndk26-static)](https://search.maven.org/search?q=g:com.viliussutkus89.ndk.thirdparty%20AND%20a:libpng-ndk26-static)
[![Maven Central](https://img.shields.io/maven-central/v/com.viliussutkus89.ndk.thirdparty/libpng-ndk26-shared.svg?label=Maven%20Central%20libpng-ndk26-shared)](https://search.maven.org/search?q=g:com.viliussutkus89.ndk.thirdparty%20AND%20a:libpng-ndk26-shared)

#### [libjpeg-turbo](https://libjpeg-turbo.org)

[![libjpeg-turbo](https://github.com/ViliusSutkus89/ndkports/actions/workflows/libjpeg-turbo.yml/badge.svg)](https://github.com/ViliusSutkus89/ndkports/actions/workflows/libjpeg-turbo.yml)
[![Maven Central](https://img.shields.io/maven-central/v/com.viliussutkus89.ndk.thirdparty/libjpeg-turbo-ndk25-static.svg?label=Maven%20Central%20libjpeg-turbo-ndk25-static)](https://search.maven.org/search?q=g:com.viliussutkus89.ndk.thirdparty%20AND%20a:libjpeg-turbo-ndk25-static)
[![Maven Central](https://img.shields.io/maven-central/v/com.viliussutkus89.ndk.thirdparty/libjpeg-turbo-ndk25-shared.svg?label=Maven%20Central%20libjpeg-turbo-ndk25-shared)](https://search.maven.org/search?q=g:com.viliussutkus89.ndk.thirdparty%20AND%20a:libjpeg-turbo-ndk25-shared)
[![Maven Central](https://img.shields.io/maven-central/v/com.viliussutkus89.ndk.thirdparty/libjpeg-turbo-ndk26-static.svg?label=Maven%20Central%20libjpeg-turbo-ndk26-static)](https://search.maven.org/search?q=g:com.viliussutkus89.ndk.thirdparty%20AND%20a:libjpeg-turbo-ndk26-static)
[![Maven Central](https://img.shields.io/maven-central/v/com.viliussutkus89.ndk.thirdparty/libjpeg-turbo-ndk26-shared.svg?label=Maven%20Central%20libjpeg-turbo-ndk26-shared)](https://search.maven.org/search?q=g:com.viliussutkus89.ndk.thirdparty%20AND%20a:libjpeg-turbo-ndk26-shared)

#### [Fontconfig](https://www.freedesktop.org/wiki/Software/fontconfig/)

[![fontconfig](https://github.com/ViliusSutkus89/ndkports/actions/workflows/fontconfig.yml/badge.svg)](https://github.com/ViliusSutkus89/ndkports/actions/workflows/fontconfig.yml)
[![Maven Central](https://img.shields.io/maven-central/v/com.viliussutkus89.ndk.thirdparty/fontconfig-ndk25-static.svg?label=Maven%20Central%20fontconfig-ndk25-static)](https://search.maven.org/search?q=g:com.viliussutkus89.ndk.thirdparty%20AND%20a:fontconfig-ndk25-static)
[![Maven Central](https://img.shields.io/maven-central/v/com.viliussutkus89.ndk.thirdparty/fontconfig-ndk25-shared.svg?label=Maven%20Central%20fontconfig-ndk25-shared)](https://search.maven.org/search?q=g:com.viliussutkus89.ndk.thirdparty%20AND%20a:fontconfig-ndk25-shared)
[![Maven Central](https://img.shields.io/maven-central/v/com.viliussutkus89.ndk.thirdparty/fontconfig-ndk26-static.svg?label=Maven%20Central%20fontconfig-ndk26-static)](https://search.maven.org/search?q=g:com.viliussutkus89.ndk.thirdparty%20AND%20a:fontconfig-ndk26-static)
[![Maven Central](https://img.shields.io/maven-central/v/com.viliussutkus89.ndk.thirdparty/fontconfig-ndk26-shared.svg?label=Maven%20Central%20fontconfig-ndk26-shared)](https://search.maven.org/search?q=g:com.viliussutkus89.ndk.thirdparty%20AND%20a:fontconfig-ndk26-shared)

#### [Poppler](https://poppler.freedesktop.org)

[![poppler](https://github.com/ViliusSutkus89/ndkports/actions/workflows/poppler.yml/badge.svg)](https://github.com/ViliusSutkus89/ndkports/actions/workflows/poppler.yml)
[![Maven Central](https://img.shields.io/maven-central/v/com.viliussutkus89.ndk.thirdparty/poppler-ndk25-static.svg?label=Maven%20Central%20poppler-ndk25-static)](https://search.maven.org/search?q=g:com.viliussutkus89.ndk.thirdparty%20AND%20a:poppler-ndk25-static)
[![Maven Central](https://img.shields.io/maven-central/v/com.viliussutkus89.ndk.thirdparty/poppler-ndk25-shared.svg?label=Maven%20Central%20poppler-ndk25-shared)](https://search.maven.org/search?q=g:com.viliussutkus89.ndk.thirdparty%20AND%20a:poppler-ndk25-shared)
[![Maven Central](https://img.shields.io/maven-central/v/com.viliussutkus89.ndk.thirdparty/poppler-ndk26-static.svg?label=Maven%20Central%20poppler-ndk26-static)](https://search.maven.org/search?q=g:com.viliussutkus89.ndk.thirdparty%20AND%20a:poppler-ndk26-static)
[![Maven Central](https://img.shields.io/maven-central/v/com.viliussutkus89.ndk.thirdparty/poppler-ndk26-shared.svg?label=Maven%20Central%20poppler-ndk26-shared)](https://search.maven.org/search?q=g:com.viliussutkus89.ndk.thirdparty%20AND%20a:poppler-ndk26-shared)

#### [pdf2htmlEX](https://github.com/pdf2htmlEX/pdf2htmlEX)

pdf2htmlEX-0.18.8.rc2 version is based on PR https://github.com/pdf2htmlEX/pdf2htmlEX/pull/154 , not an official release,

pdf2htmlEX also has a Java wrapper - [pdf2htmlEX-Android](https://github.com/ViliusSutkus89/pdf2htmlEX-Android)

[![pdf2htmlEX](https://github.com/ViliusSutkus89/ndkports/actions/workflows/pdf2htmlEX.yml/badge.svg)](https://github.com/ViliusSutkus89/ndkports/actions/workflows/pdf2htmlEX.yml)
[![Maven Central](https://img.shields.io/maven-central/v/com.viliussutkus89.ndk.thirdparty/pdf2htmlEX-ndk25-static.svg?label=Maven%20Central%20pdf2htmlEX-ndk25-static)](https://search.maven.org/search?q=g:com.viliussutkus89.ndk.thirdparty%20AND%20a:pdf2htmlEX-ndk25-static)
[![Maven Central](https://img.shields.io/maven-central/v/com.viliussutkus89.ndk.thirdparty/pdf2htmlEX-ndk25-shared.svg?label=Maven%20Central%20pdf2htmlEX-ndk25-shared)](https://search.maven.org/search?q=g:com.viliussutkus89.ndk.thirdparty%20AND%20a:pdf2htmlEX-ndk25-shared)
[![Maven Central](https://img.shields.io/maven-central/v/com.viliussutkus89.ndk.thirdparty/pdf2htmlEX-ndk26-static.svg?label=Maven%20Central%20pdf2htmlEX-ndk26-static)](https://search.maven.org/search?q=g:com.viliussutkus89.ndk.thirdparty%20AND%20a:pdf2htmlEX-ndk26-static)
[![Maven Central](https://img.shields.io/maven-central/v/com.viliussutkus89.ndk.thirdparty/pdf2htmlEX-ndk26-shared.svg?label=Maven%20Central%20pdf2htmlEX-ndk26-shared)](https://search.maven.org/search?q=g:com.viliussutkus89.ndk.thirdparty%20AND%20a:pdf2htmlEX-ndk26-shared)

#### [GNU FriBidi](https://github.com/fribidi/fribidi)

[![fribidi](https://github.com/ViliusSutkus89/ndkports/actions/workflows/fribidi.yml/badge.svg)](https://github.com/ViliusSutkus89/ndkports/actions/workflows/fribidi.yml)
[![Maven Central](https://img.shields.io/maven-central/v/com.viliussutkus89.ndk.thirdparty/fribidi-ndk25-static.svg?label=Maven%20Central%20fribidi-ndk25-static)](https://search.maven.org/search?q=g:com.viliussutkus89.ndk.thirdparty%20AND%20a:fribidi-ndk25-static)
[![Maven Central](https://img.shields.io/maven-central/v/com.viliussutkus89.ndk.thirdparty/fribidi-ndk25-shared.svg?label=Maven%20Central%20fribidi-ndk25-shared)](https://search.maven.org/search?q=g:com.viliussutkus89.ndk.thirdparty%20AND%20a:fribidi-ndk25-shared)
[![Maven Central](https://img.shields.io/maven-central/v/com.viliussutkus89.ndk.thirdparty/fribidi-ndk26-static.svg?label=Maven%20Central%20fribidi-ndk26-static)](https://search.maven.org/search?q=g:com.viliussutkus89.ndk.thirdparty%20AND%20a:fribidi-ndk26-static)
[![Maven Central](https://img.shields.io/maven-central/v/com.viliussutkus89.ndk.thirdparty/fribidi-ndk26-shared.svg?label=Maven%20Central%20fribidi-ndk26-shared)](https://search.maven.org/search?q=g:com.viliussutkus89.ndk.thirdparty%20AND%20a:fribidi-ndk26-shared)

#### [FreeType](https://freetype.org)

[![freetype](https://github.com/ViliusSutkus89/ndkports/actions/workflows/freetype.yml/badge.svg)](https://github.com/ViliusSutkus89/ndkports/actions/workflows/freetype.yml)
[![Maven Central](https://img.shields.io/maven-central/v/com.viliussutkus89.ndk.thirdparty/freetype-ndk25-static.svg?label=Maven%20Central%20freetype-ndk25-static)](https://search.maven.org/search?q=g:com.viliussutkus89.ndk.thirdparty%20AND%20a:freetype-ndk25-static)
[![Maven Central](https://img.shields.io/maven-central/v/com.viliussutkus89.ndk.thirdparty/freetype-ndk25-shared.svg?label=Maven%20Central%20freetype-ndk25-shared)](https://search.maven.org/search?q=g:com.viliussutkus89.ndk.thirdparty%20AND%20a:freetype-ndk25-shared)
[![Maven Central](https://img.shields.io/maven-central/v/com.viliussutkus89.ndk.thirdparty/freetype-ndk26-static.svg?label=Maven%20Central%20freetype-ndk26-static)](https://search.maven.org/search?q=g:com.viliussutkus89.ndk.thirdparty%20AND%20a:freetype-ndk26-static)
[![Maven Central](https://img.shields.io/maven-central/v/com.viliussutkus89.ndk.thirdparty/freetype-ndk26-shared.svg?label=Maven%20Central%20freetype-ndk26-shared)](https://search.maven.org/search?q=g:com.viliussutkus89.ndk.thirdparty%20AND%20a:freetype-ndk26-shared)

#### [PCRE2 - Perl-Compatible Regular Expressions](https://github.com/PCRE2Project/pcre2)

[![pcre2](https://github.com/ViliusSutkus89/ndkports/actions/workflows/pcre2.yml/badge.svg)](https://github.com/ViliusSutkus89/ndkports/actions/workflows/pcre2.yml)
[![Maven Central](https://img.shields.io/maven-central/v/com.viliussutkus89.ndk.thirdparty/pcre2-ndk25-static.svg?label=Maven%20Central%20pcre2-ndk25-static)](https://search.maven.org/search?q=g:com.viliussutkus89.ndk.thirdparty%20AND%20a:pcre2-ndk25-static)
[![Maven Central](https://img.shields.io/maven-central/v/com.viliussutkus89.ndk.thirdparty/pcre2-ndk25-shared.svg?label=Maven%20Central%20pcre2-ndk25-shared)](https://search.maven.org/search?q=g:com.viliussutkus89.ndk.thirdparty%20AND%20a:pcre2-ndk25-shared)
[![Maven Central](https://img.shields.io/maven-central/v/com.viliussutkus89.ndk.thirdparty/pcre2-ndk26-static.svg?label=Maven%20Central%20pcre2-ndk26-static)](https://search.maven.org/search?q=g:com.viliussutkus89.ndk.thirdparty%20AND%20a:pcre2-ndk26-static)
[![Maven Central](https://img.shields.io/maven-central/v/com.viliussutkus89.ndk.thirdparty/pcre2-ndk26-shared.svg?label=Maven%20Central%20pcre2-ndk26-shared)](https://search.maven.org/search?q=g:com.viliussutkus89.ndk.thirdparty%20AND%20a:pcre2-ndk26-shared)

#### [libffi](https://sourceware.org/libffi/)

[![libffi](https://github.com/ViliusSutkus89/ndkports/actions/workflows/libffi.yml/badge.svg)](https://github.com/ViliusSutkus89/ndkports/actions/workflows/libffi.yml)
[![Maven Central](https://img.shields.io/maven-central/v/com.viliussutkus89.ndk.thirdparty/libffi-ndk25-static.svg?label=Maven%20Central%20libffi-ndk25-static)](https://search.maven.org/search?q=g:com.viliussutkus89.ndk.thirdparty%20AND%20a:libffi-ndk25-static)
[![Maven Central](https://img.shields.io/maven-central/v/com.viliussutkus89.ndk.thirdparty/libffi-ndk25-shared.svg?label=Maven%20Central%20libffi-ndk25-shared)](https://search.maven.org/search?q=g:com.viliussutkus89.ndk.thirdparty%20AND%20a:libffi-ndk25-shared)
[![Maven Central](https://img.shields.io/maven-central/v/com.viliussutkus89.ndk.thirdparty/libffi-ndk26-static.svg?label=Maven%20Central%20libffi-ndk26-static)](https://search.maven.org/search?q=g:com.viliussutkus89.ndk.thirdparty%20AND%20a:libffi-ndk26-static)
[![Maven Central](https://img.shields.io/maven-central/v/com.viliussutkus89.ndk.thirdparty/libffi-ndk26-shared.svg?label=Maven%20Central%20libffi-ndk26-shared)](https://search.maven.org/search?q=g:com.viliussutkus89.ndk.thirdparty%20AND%20a:libffi-ndk26-shared)

#### [LibTIFF](http://www.simplesystems.org/libtiff/)

[![libtiff](https://github.com/ViliusSutkus89/ndkports/actions/workflows/libtiff.yml/badge.svg)](https://github.com/ViliusSutkus89/ndkports/actions/workflows/libtiff.yml)
[![Maven Central](https://img.shields.io/maven-central/v/com.viliussutkus89.ndk.thirdparty/libtiff-ndk25-static.svg?label=Maven%20Central%20libtiff-ndk25-static)](https://search.maven.org/search?q=g:com.viliussutkus89.ndk.thirdparty%20AND%20a:libtiff-ndk25-static)
[![Maven Central](https://img.shields.io/maven-central/v/com.viliussutkus89.ndk.thirdparty/libtiff-ndk25-shared.svg?label=Maven%20Central%20libtiff-ndk25-shared)](https://search.maven.org/search?q=g:com.viliussutkus89.ndk.thirdparty%20AND%20a:libtiff-ndk25-shared)
[![Maven Central](https://img.shields.io/maven-central/v/com.viliussutkus89.ndk.thirdparty/libtiff-ndk26-static.svg?label=Maven%20Central%20libtiff-ndk26-static)](https://search.maven.org/search?q=g:com.viliussutkus89.ndk.thirdparty%20AND%20a:libtiff-ndk26-static)
[![Maven Central](https://img.shields.io/maven-central/v/com.viliussutkus89.ndk.thirdparty/libtiff-ndk26-shared.svg?label=Maven%20Central%20libtiff-ndk26-shared)](https://search.maven.org/search?q=g:com.viliussutkus89.ndk.thirdparty%20AND%20a:libtiff-ndk26-shared)

#### [Expat](https://github.com/libexpat/libexpat)

[![libexpat](https://github.com/ViliusSutkus89/ndkports/actions/workflows/libexpat.yml/badge.svg)](https://github.com/ViliusSutkus89/ndkports/actions/workflows/libexpat.yml)
[![Maven Central](https://img.shields.io/maven-central/v/com.viliussutkus89.ndk.thirdparty/libexpat-ndk25-static.svg?label=Maven%20Central%20libexpat-ndk25-static)](https://search.maven.org/search?q=g:com.viliussutkus89.ndk.thirdparty%20AND%20a:libexpat-ndk25-static)
[![Maven Central](https://img.shields.io/maven-central/v/com.viliussutkus89.ndk.thirdparty/libexpat-ndk25-shared.svg?label=Maven%20Central%20libexpat-ndk25-shared)](https://search.maven.org/search?q=g:com.viliussutkus89.ndk.thirdparty%20AND%20a:libexpat-ndk25-shared)
[![Maven Central](https://img.shields.io/maven-central/v/com.viliussutkus89.ndk.thirdparty/libexpat-ndk26-static.svg?label=Maven%20Central%20libexpat-ndk26-static)](https://search.maven.org/search?q=g:com.viliussutkus89.ndk.thirdparty%20AND%20a:libexpat-ndk26-static)
[![Maven Central](https://img.shields.io/maven-central/v/com.viliussutkus89.ndk.thirdparty/libexpat-ndk26-shared.svg?label=Maven%20Central%20libexpat-ndk26-shared)](https://search.maven.org/search?q=g:com.viliussutkus89.ndk.thirdparty%20AND%20a:libexpat-ndk26-shared)

#### [Little CMS color engine](https://www.littlecms.com/color-engine/)

[![lcms2](https://github.com/ViliusSutkus89/ndkports/actions/workflows/lcms2.yml/badge.svg)](https://github.com/ViliusSutkus89/ndkports/actions/workflows/lcms2.yml)
[![Maven Central](https://img.shields.io/maven-central/v/com.viliussutkus89.ndk.thirdparty/lcms2-ndk25-static.svg?label=Maven%20Central%20lcms2-ndk25-static)](https://search.maven.org/search?q=g:com.viliussutkus89.ndk.thirdparty%20AND%20a:lcms2-ndk25-static)
[![Maven Central](https://img.shields.io/maven-central/v/com.viliussutkus89.ndk.thirdparty/lcms2-ndk25-shared.svg?label=Maven%20Central%20lcms2-ndk25-shared)](https://search.maven.org/search?q=g:com.viliussutkus89.ndk.thirdparty%20AND%20a:lcms2-ndk25-shared)
[![Maven Central](https://img.shields.io/maven-central/v/com.viliussutkus89.ndk.thirdparty/lcms2-ndk26-static.svg?label=Maven%20Central%20lcms2-ndk26-static)](https://search.maven.org/search?q=g:com.viliussutkus89.ndk.thirdparty%20AND%20a:lcms2-ndk26-static)
[![Maven Central](https://img.shields.io/maven-central/v/com.viliussutkus89.ndk.thirdparty/lcms2-ndk26-shared.svg?label=Maven%20Central%20lcms2-ndk26-shared)](https://search.maven.org/search?q=g:com.viliussutkus89.ndk.thirdparty%20AND%20a:lcms2-ndk26-shared)

#### [GNU Libtool](https://www.gnu.org/software/libtool/)

[![libtool](https://github.com/ViliusSutkus89/ndkports/actions/workflows/libtool.yml/badge.svg)](https://github.com/ViliusSutkus89/ndkports/actions/workflows/libtool.yml)
[![Maven Central](https://img.shields.io/maven-central/v/com.viliussutkus89.ndk.thirdparty/libtool-ndk25-static.svg?label=Maven%20Central%20libtool-ndk25-static)](https://search.maven.org/search?q=g:com.viliussutkus89.ndk.thirdparty%20AND%20a:libtool-ndk25-static)
[![Maven Central](https://img.shields.io/maven-central/v/com.viliussutkus89.ndk.thirdparty/libtool-ndk25-shared.svg?label=Maven%20Central%20libtool-ndk25-shared)](https://search.maven.org/search?q=g:com.viliussutkus89.ndk.thirdparty%20AND%20a:libtool-ndk25-shared)
[![Maven Central](https://img.shields.io/maven-central/v/com.viliussutkus89.ndk.thirdparty/libtool-ndk26-static.svg?label=Maven%20Central%20libtool-ndk26-static)](https://search.maven.org/search?q=g:com.viliussutkus89.ndk.thirdparty%20AND%20a:libtool-ndk26-static)
[![Maven Central](https://img.shields.io/maven-central/v/com.viliussutkus89.ndk.thirdparty/libtool-ndk26-shared.svg?label=Maven%20Central%20libtool-ndk26-shared)](https://search.maven.org/search?q=g:com.viliussutkus89.ndk.thirdparty%20AND%20a:libtool-ndk26-shared)

#### [OpenJPEG](https://www.openjpeg.org)

[![openjpeg](https://github.com/ViliusSutkus89/ndkports/actions/workflows/openjpeg.yml/badge.svg)](https://github.com/ViliusSutkus89/ndkports/actions/workflows/openjpeg.yml)
[![Maven Central](https://img.shields.io/maven-central/v/com.viliussutkus89.ndk.thirdparty/openjpeg-ndk25-static.svg?label=Maven%20Central%20openjpeg-ndk25-static)](https://search.maven.org/search?q=g:com.viliussutkus89.ndk.thirdparty%20AND%20a:openjpeg-ndk25-static)
[![Maven Central](https://img.shields.io/maven-central/v/com.viliussutkus89.ndk.thirdparty/openjpeg-ndk25-shared.svg?label=Maven%20Central%20openjpeg-ndk25-shared)](https://search.maven.org/search?q=g:com.viliussutkus89.ndk.thirdparty%20AND%20a:openjpeg-ndk25-shared)
[![Maven Central](https://img.shields.io/maven-central/v/com.viliussutkus89.ndk.thirdparty/openjpeg-ndk26-static.svg?label=Maven%20Central%20openjpeg-ndk26-static)](https://search.maven.org/search?q=g:com.viliussutkus89.ndk.thirdparty%20AND%20a:openjpeg-ndk26-static)
[![Maven Central](https://img.shields.io/maven-central/v/com.viliussutkus89.ndk.thirdparty/openjpeg-ndk26-shared.svg?label=Maven%20Central%20openjpeg-ndk26-shared)](https://search.maven.org/search?q=g:com.viliussutkus89.ndk.thirdparty%20AND%20a:openjpeg-ndk26-shared)

#### [Spiro](https://github.com/fontforge/libspiro)

[![spiro](https://github.com/ViliusSutkus89/ndkports/actions/workflows/spiro.yml/badge.svg)](https://github.com/ViliusSutkus89/ndkports/actions/workflows/spiro.yml)
[![Maven Central](https://img.shields.io/maven-central/v/com.viliussutkus89.ndk.thirdparty/spiro-ndk25-static.svg?label=Maven%20Central%20spiro-ndk25-static)](https://search.maven.org/search?q=g:com.viliussutkus89.ndk.thirdparty%20AND%20a:spiro-ndk25-static)
[![Maven Central](https://img.shields.io/maven-central/v/com.viliussutkus89.ndk.thirdparty/spiro-ndk25-shared.svg?label=Maven%20Central%20spiro-ndk25-shared)](https://search.maven.org/search?q=g:com.viliussutkus89.ndk.thirdparty%20AND%20a:spiro-ndk25-shared)
[![Maven Central](https://img.shields.io/maven-central/v/com.viliussutkus89.ndk.thirdparty/spiro-ndk26-static.svg?label=Maven%20Central%20spiro-ndk26-static)](https://search.maven.org/search?q=g:com.viliussutkus89.ndk.thirdparty%20AND%20a:spiro-ndk26-static)
[![Maven Central](https://img.shields.io/maven-central/v/com.viliussutkus89.ndk.thirdparty/spiro-ndk26-shared.svg?label=Maven%20Central%20spiro-ndk26-shared)](https://search.maven.org/search?q=g:com.viliussutkus89.ndk.thirdparty%20AND%20a:spiro-ndk26-shared)

#### [Pixman](https://www.pixman.org)

[![pixman](https://github.com/ViliusSutkus89/ndkports/actions/workflows/pixman.yml/badge.svg)](https://github.com/ViliusSutkus89/ndkports/actions/workflows/pixman.yml)
[![Maven Central](https://img.shields.io/maven-central/v/com.viliussutkus89.ndk.thirdparty/pixman-ndk25-static.svg?label=Maven%20Central%20pixman-ndk25-static)](https://search.maven.org/search?q=g:com.viliussutkus89.ndk.thirdparty%20AND%20a:pixman-ndk25-static)
[![Maven Central](https://img.shields.io/maven-central/v/com.viliussutkus89.ndk.thirdparty/pixman-ndk25-shared.svg?label=Maven%20Central%20pixman-ndk25-shared)](https://search.maven.org/search?q=g:com.viliussutkus89.ndk.thirdparty%20AND%20a:pixman-ndk25-shared)
[![Maven Central](https://img.shields.io/maven-central/v/com.viliussutkus89.ndk.thirdparty/pixman-ndk26-static.svg?label=Maven%20Central%20pixman-ndk26-static)](https://search.maven.org/search?q=g:com.viliussutkus89.ndk.thirdparty%20AND%20a:pixman-ndk26-static)
[![Maven Central](https://img.shields.io/maven-central/v/com.viliussutkus89.ndk.thirdparty/pixman-ndk26-shared.svg?label=Maven%20Central%20pixman-ndk26-shared)](https://search.maven.org/search?q=g:com.viliussutkus89.ndk.thirdparty%20AND%20a:pixman-ndk26-shared)

#### [libiconv](https://www.gnu.org/software/libiconv/)

[![libiconv](https://github.com/ViliusSutkus89/ndkports/actions/workflows/libiconv.yml/badge.svg)](https://github.com/ViliusSutkus89/ndkports/actions/workflows/libiconv.yml)
[![Maven Central](https://img.shields.io/maven-central/v/com.viliussutkus89.ndk.thirdparty/libiconv-ndk25-static.svg?label=Maven%20Central%20libiconv-ndk25-static)](https://search.maven.org/search?q=g:com.viliussutkus89.ndk.thirdparty%20AND%20a:libiconv-ndk25-static)
[![Maven Central](https://img.shields.io/maven-central/v/com.viliussutkus89.ndk.thirdparty/libiconv-ndk25-shared.svg?label=Maven%20Central%20libiconv-ndk25-shared)](https://search.maven.org/search?q=g:com.viliussutkus89.ndk.thirdparty%20AND%20a:libiconv-ndk25-shared)
[![Maven Central](https://img.shields.io/maven-central/v/com.viliussutkus89.ndk.thirdparty/libiconv-ndk26-static.svg?label=Maven%20Central%20libiconv-ndk26-static)](https://search.maven.org/search?q=g:com.viliussutkus89.ndk.thirdparty%20AND%20a:libiconv-ndk26-static)
[![Maven Central](https://img.shields.io/maven-central/v/com.viliussutkus89.ndk.thirdparty/libiconv-ndk26-shared.svg?label=Maven%20Central%20libiconv-ndk26-shared)](https://search.maven.org/search?q=g:com.viliussutkus89.ndk.thirdparty%20AND%20a:libiconv-ndk26-shared)

#### [libxml2](https://gitlab.gnome.org/GNOME/libxml2)

[![libxml2](https://github.com/ViliusSutkus89/ndkports/actions/workflows/libxml2.yml/badge.svg)](https://github.com/ViliusSutkus89/ndkports/actions/workflows/libxml2.yml)
[![Maven Central](https://img.shields.io/maven-central/v/com.viliussutkus89.ndk.thirdparty/libxml2-ndk25-static.svg?label=Maven%20Central%20libxml2-ndk25-static)](https://search.maven.org/search?q=g:com.viliussutkus89.ndk.thirdparty%20AND%20a:libxml2-ndk25-static)
[![Maven Central](https://img.shields.io/maven-central/v/com.viliussutkus89.ndk.thirdparty/libxml2-ndk25-shared.svg?label=Maven%20Central%20libxml2-ndk25-shared)](https://search.maven.org/search?q=g:com.viliussutkus89.ndk.thirdparty%20AND%20a:libxml2-ndk25-shared)
[![Maven Central](https://img.shields.io/maven-central/v/com.viliussutkus89.ndk.thirdparty/libxml2-ndk26-static.svg?label=Maven%20Central%20libxml2-ndk26-static)](https://search.maven.org/search?q=g:com.viliussutkus89.ndk.thirdparty%20AND%20a:libxml2-ndk26-static)
[![Maven Central](https://img.shields.io/maven-central/v/com.viliussutkus89.ndk.thirdparty/libxml2-ndk26-shared.svg?label=Maven%20Central%20libxml2-ndk26-shared)](https://search.maven.org/search?q=g:com.viliussutkus89.ndk.thirdparty%20AND%20a:libxml2-ndk26-shared)

#### [proxy-libintl](https://github.com/ViliusSutkus89/proxy-libintl)

[![proxy-libintl](https://github.com/ViliusSutkus89/ndkports/actions/workflows/proxy-libintl.yml/badge.svg)](https://github.com/ViliusSutkus89/ndkports/actions/workflows/proxy-libintl.yml)
[![Maven Central](https://img.shields.io/maven-central/v/com.viliussutkus89.ndk.thirdparty/proxy-libintl-ndk25-static.svg?label=Maven%20Central%20proxy-libintl-ndk25-static)](https://search.maven.org/search?q=g:com.viliussutkus89.ndk.thirdparty%20AND%20a:proxy-libintl-ndk25-static)
[![Maven Central](https://img.shields.io/maven-central/v/com.viliussutkus89.ndk.thirdparty/proxy-libintl-ndk25-shared.svg?label=Maven%20Central%20proxy-libintl-ndk25-shared)](https://search.maven.org/search?q=g:com.viliussutkus89.ndk.thirdparty%20AND%20a:proxy-libintl-ndk25-shared)
[![Maven Central](https://img.shields.io/maven-central/v/com.viliussutkus89.ndk.thirdparty/proxy-libintl-ndk26-static.svg?label=Maven%20Central%20proxy-libintl-ndk26-static)](https://search.maven.org/search?q=g:com.viliussutkus89.ndk.thirdparty%20AND%20a:proxy-libintl-ndk26-static)
[![Maven Central](https://img.shields.io/maven-central/v/com.viliussutkus89.ndk.thirdparty/proxy-libintl-ndk26-shared.svg?label=Maven%20Central%20proxy-libintl-ndk26-shared)](https://search.maven.org/search?q=g:com.viliussutkus89.ndk.thirdparty%20AND%20a:proxy-libintl-ndk26-shared)

#### [Cairo](https://cairographics.org)

[![cairo](https://github.com/ViliusSutkus89/ndkports/actions/workflows/cairo.yml/badge.svg)](https://github.com/ViliusSutkus89/ndkports/actions/workflows/cairo.yml)
[![Maven Central](https://img.shields.io/maven-central/v/com.viliussutkus89.ndk.thirdparty/cairo-ndk25-static.svg?label=Maven%20Central%20cairo-ndk25-static)](https://search.maven.org/search?q=g:com.viliussutkus89.ndk.thirdparty%20AND%20a:cairo-ndk25-static)
[![Maven Central](https://img.shields.io/maven-central/v/com.viliussutkus89.ndk.thirdparty/cairo-ndk25-shared.svg?label=Maven%20Central%20cairo-ndk25-shared)](https://search.maven.org/search?q=g:com.viliussutkus89.ndk.thirdparty%20AND%20a:cairo-ndk25-shared)
[![Maven Central](https://img.shields.io/maven-central/v/com.viliussutkus89.ndk.thirdparty/cairo-ndk26-static.svg?label=Maven%20Central%20cairo-ndk26-static)](https://search.maven.org/search?q=g:com.viliussutkus89.ndk.thirdparty%20AND%20a:cairo-ndk26-static)
[![Maven Central](https://img.shields.io/maven-central/v/com.viliussutkus89.ndk.thirdparty/cairo-ndk26-shared.svg?label=Maven%20Central%20cairo-ndk26-shared)](https://search.maven.org/search?q=g:com.viliussutkus89.ndk.thirdparty%20AND%20a:cairo-ndk26-shared)

#### [Graphite](https://graphite.sil.org)

[![graphite2](https://github.com/ViliusSutkus89/ndkports/actions/workflows/graphite2.yml/badge.svg)](https://github.com/ViliusSutkus89/ndkports/actions/workflows/graphite2.yml)
[![Maven Central](https://img.shields.io/maven-central/v/com.viliussutkus89.ndk.thirdparty/graphite2-ndk25-static.svg?label=Maven%20Central%20graphite2-ndk25-static)](https://search.maven.org/search?q=g:com.viliussutkus89.ndk.thirdparty%20AND%20a:graphite2-ndk25-static)
[![Maven Central](https://img.shields.io/maven-central/v/com.viliussutkus89.ndk.thirdparty/graphite2-ndk25-shared.svg?label=Maven%20Central%20graphite2-ndk25-shared)](https://search.maven.org/search?q=g:com.viliussutkus89.ndk.thirdparty%20AND%20a:graphite2-ndk25-shared)
[![Maven Central](https://img.shields.io/maven-central/v/com.viliussutkus89.ndk.thirdparty/graphite2-ndk26-static.svg?label=Maven%20Central%20graphite2-ndk26-static)](https://search.maven.org/search?q=g:com.viliussutkus89.ndk.thirdparty%20AND%20a:graphite2-ndk26-static)
[![Maven Central](https://img.shields.io/maven-central/v/com.viliussutkus89.ndk.thirdparty/graphite2-ndk26-shared.svg?label=Maven%20Central%20graphite2-ndk26-shared)](https://search.maven.org/search?q=g:com.viliussutkus89.ndk.thirdparty%20AND%20a:graphite2-ndk26-shared)

#### [HarfBuzz](https://harfbuzz.github.io/)

[![harfbuzz](https://github.com/ViliusSutkus89/ndkports/actions/workflows/harfbuzz.yml/badge.svg)](https://github.com/ViliusSutkus89/ndkports/actions/workflows/harfbuzz.yml)
[![Maven Central](https://img.shields.io/maven-central/v/com.viliussutkus89.ndk.thirdparty/harfbuzz-ndk25-static.svg?label=Maven%20Central%20harfbuzz-ndk25-static)](https://search.maven.org/search?q=g:com.viliussutkus89.ndk.thirdparty%20AND%20a:harfbuzz-ndk25-static)
[![Maven Central](https://img.shields.io/maven-central/v/com.viliussutkus89.ndk.thirdparty/harfbuzz-ndk25-shared.svg?label=Maven%20Central%20harfbuzz-ndk25-shared)](https://search.maven.org/search?q=g:com.viliussutkus89.ndk.thirdparty%20AND%20a:harfbuzz-ndk25-shared)
[![Maven Central](https://img.shields.io/maven-central/v/com.viliussutkus89.ndk.thirdparty/harfbuzz-ndk26-static.svg?label=Maven%20Central%20harfbuzz-ndk26-static)](https://search.maven.org/search?q=g:com.viliussutkus89.ndk.thirdparty%20AND%20a:harfbuzz-ndk26-static)
[![Maven Central](https://img.shields.io/maven-central/v/com.viliussutkus89.ndk.thirdparty/harfbuzz-ndk26-shared.svg?label=Maven%20Central%20harfbuzz-ndk26-shared)](https://search.maven.org/search?q=g:com.viliussutkus89.ndk.thirdparty%20AND%20a:harfbuzz-ndk26-shared)

#### [libuninameslist](https://github.com/fontforge/libuninameslist)

[![libuninameslist](https://github.com/ViliusSutkus89/ndkports/actions/workflows/libuninameslist.yml/badge.svg)](https://github.com/ViliusSutkus89/ndkports/actions/workflows/libuninameslist.yml)
[![Maven Central](https://img.shields.io/maven-central/v/com.viliussutkus89.ndk.thirdparty/libuninameslist-ndk25-static.svg?label=Maven%20Central%20libuninameslist-ndk25-static)](https://search.maven.org/search?q=g:com.viliussutkus89.ndk.thirdparty%20AND%20a:libuninameslist-ndk25-static)
[![Maven Central](https://img.shields.io/maven-central/v/com.viliussutkus89.ndk.thirdparty/libuninameslist-ndk25-shared.svg?label=Maven%20Central%20libuninameslist-ndk25-shared)](https://search.maven.org/search?q=g:com.viliussutkus89.ndk.thirdparty%20AND%20a:libuninameslist-ndk25-shared)
[![Maven Central](https://img.shields.io/maven-central/v/com.viliussutkus89.ndk.thirdparty/libuninameslist-ndk26-static.svg?label=Maven%20Central%20libuninameslist-ndk26-static)](https://search.maven.org/search?q=g:com.viliussutkus89.ndk.thirdparty%20AND%20a:libuninameslist-ndk26-static)
[![Maven Central](https://img.shields.io/maven-central/v/com.viliussutkus89.ndk.thirdparty/libuninameslist-ndk26-shared.svg?label=Maven%20Central%20libuninameslist-ndk26-shared)](https://search.maven.org/search?q=g:com.viliussutkus89.ndk.thirdparty%20AND%20a:libuninameslist-ndk26-shared)

#### [Pango](https://pango.gnome.org)

[![pango](https://github.com/ViliusSutkus89/ndkports/actions/workflows/pango.yml/badge.svg)](https://github.com/ViliusSutkus89/ndkports/actions/workflows/pango.yml)
[![Maven Central](https://img.shields.io/maven-central/v/com.viliussutkus89.ndk.thirdparty/pango-ndk25-static.svg?label=Maven%20Central%20pango-ndk25-static)](https://search.maven.org/search?q=g:com.viliussutkus89.ndk.thirdparty%20AND%20a:pango-ndk25-static)
[![Maven Central](https://img.shields.io/maven-central/v/com.viliussutkus89.ndk.thirdparty/pango-ndk25-shared.svg?label=Maven%20Central%20pango-ndk25-shared)](https://search.maven.org/search?q=g:com.viliussutkus89.ndk.thirdparty%20AND%20a:pango-ndk25-shared)
[![Maven Central](https://img.shields.io/maven-central/v/com.viliussutkus89.ndk.thirdparty/pango-ndk26-static.svg?label=Maven%20Central%20pango-ndk26-static)](https://search.maven.org/search?q=g:com.viliussutkus89.ndk.thirdparty%20AND%20a:pango-ndk26-static)
[![Maven Central](https://img.shields.io/maven-central/v/com.viliussutkus89.ndk.thirdparty/pango-ndk26-shared.svg?label=Maven%20Central%20pango-ndk26-shared)](https://search.maven.org/search?q=g:com.viliussutkus89.ndk.thirdparty%20AND%20a:pango-ndk26-shared)

#### [FontForge](https://fontforge.org)

[![fontforge](https://github.com/ViliusSutkus89/ndkports/actions/workflows/fontforge.yml/badge.svg)](https://github.com/ViliusSutkus89/ndkports/actions/workflows/fontforge.yml)
[![Maven Central](https://img.shields.io/maven-central/v/com.viliussutkus89.ndk.thirdparty/fontforge-ndk25-static.svg?label=Maven%20Central%20fontforge-ndk25-static)](https://search.maven.org/search?q=g:com.viliussutkus89.ndk.thirdparty%20AND%20a:fontforge-ndk25-static)
[![Maven Central](https://img.shields.io/maven-central/v/com.viliussutkus89.ndk.thirdparty/fontforge-ndk25-shared.svg?label=Maven%20Central%20fontforge-ndk25-shared)](https://search.maven.org/search?q=g:com.viliussutkus89.ndk.thirdparty%20AND%20a:fontforge-ndk25-shared)
[![Maven Central](https://img.shields.io/maven-central/v/com.viliussutkus89.ndk.thirdparty/fontforge-ndk26-static.svg?label=Maven%20Central%20fontforge-ndk26-static)](https://search.maven.org/search?q=g:com.viliussutkus89.ndk.thirdparty%20AND%20a:fontforge-ndk26-static)
[![Maven Central](https://img.shields.io/maven-central/v/com.viliussutkus89.ndk.thirdparty/fontforge-ndk26-shared.svg?label=Maven%20Central%20fontforge-ndk26-shared)](https://search.maven.org/search?q=g:com.viliussutkus89.ndk.thirdparty%20AND%20a:fontforge-ndk26-shared)

#### [OpenLibm](https://openlibm.org)

[![openlibm](https://github.com/ViliusSutkus89/ndkports/actions/workflows/openlibm.yml/badge.svg)](https://github.com/ViliusSutkus89/ndkports/actions/workflows/openlibm.yml)
[![Maven Central](https://img.shields.io/maven-central/v/com.viliussutkus89.ndk.thirdparty/openlibm-ndk25-static.svg?label=Maven%20Central%20openlibm-ndk25-static)](https://search.maven.org/search?q=g:com.viliussutkus89.ndk.thirdparty%20AND%20a:openlibm-ndk25-static)
[![Maven Central](https://img.shields.io/maven-central/v/com.viliussutkus89.ndk.thirdparty/openlibm-ndk25-shared.svg?label=Maven%20Central%20openlibm-ndk25-shared)](https://search.maven.org/search?q=g:com.viliussutkus89.ndk.thirdparty%20AND%20a:openlibm-ndk25-shared)
[![Maven Central](https://img.shields.io/maven-central/v/com.viliussutkus89.ndk.thirdparty/openlibm-ndk26-static.svg?label=Maven%20Central%20openlibm-ndk26-static)](https://search.maven.org/search?q=g:com.viliussutkus89.ndk.thirdparty%20AND%20a:openlibm-ndk26-static)
[![Maven Central](https://img.shields.io/maven-central/v/com.viliussutkus89.ndk.thirdparty/openlibm-ndk26-shared.svg?label=Maven%20Central%20openlibm-ndk26-shared)](https://search.maven.org/search?q=g:com.viliussutkus89.ndk.thirdparty%20AND%20a:openlibm-ndk26-shared)

#### [libgsf](https://gitlab.gnome.org/GNOME/libgsf)

[![libgsf](https://github.com/ViliusSutkus89/ndkports/actions/workflows/libgsf.yml/badge.svg)](https://github.com/ViliusSutkus89/ndkports/actions/workflows/libgsf.yml)
[![Maven Central](https://img.shields.io/maven-central/v/com.viliussutkus89.ndk.thirdparty/libgsf-ndk25-static.svg?label=Maven%20Central%20libgsf-ndk25-static)](https://search.maven.org/search?q=g:com.viliussutkus89.ndk.thirdparty%20AND%20a:libgsf-ndk25-static)
[![Maven Central](https://img.shields.io/maven-central/v/com.viliussutkus89.ndk.thirdparty/libgsf-ndk25-shared.svg?label=Maven%20Central%20libgsf-ndk25-shared)](https://search.maven.org/search?q=g:com.viliussutkus89.ndk.thirdparty%20AND%20a:libgsf-ndk25-shared)
[![Maven Central](https://img.shields.io/maven-central/v/com.viliussutkus89.ndk.thirdparty/libgsf-ndk26-static.svg?label=Maven%20Central%20libgsf-ndk26-static)](https://search.maven.org/search?q=g:com.viliussutkus89.ndk.thirdparty%20AND%20a:libgsf-ndk26-static)
[![Maven Central](https://img.shields.io/maven-central/v/com.viliussutkus89.ndk.thirdparty/libgsf-ndk26-shared.svg?label=Maven%20Central%20libgsf-ndk26-shared)](https://search.maven.org/search?q=g:com.viliussutkus89.ndk.thirdparty%20AND%20a:libgsf-ndk26-shared)

#### [libwmf](https://github.com/caolanm/libwmf)

[![libwmf](https://github.com/ViliusSutkus89/ndkports/actions/workflows/libwmf.yml/badge.svg)](https://github.com/ViliusSutkus89/ndkports/actions/workflows/libwmf.yml)
[![Maven Central](https://img.shields.io/maven-central/v/com.viliussutkus89.ndk.thirdparty/libwmf-ndk25-static.svg?label=Maven%20Central%20libwmf-ndk25-static)](https://search.maven.org/search?q=g:com.viliussutkus89.ndk.thirdparty%20AND%20a:libwmf-ndk25-static)
[![Maven Central](https://img.shields.io/maven-central/v/com.viliussutkus89.ndk.thirdparty/libwmf-ndk25-shared.svg?label=Maven%20Central%20libwmf-ndk25-shared)](https://search.maven.org/search?q=g:com.viliussutkus89.ndk.thirdparty%20AND%20a:libwmf-ndk25-shared)
[![Maven Central](https://img.shields.io/maven-central/v/com.viliussutkus89.ndk.thirdparty/libwmf-ndk26-static.svg?label=Maven%20Central%20libwmf-ndk26-static)](https://search.maven.org/search?q=g:com.viliussutkus89.ndk.thirdparty%20AND%20a:libwmf-ndk26-static)
[![Maven Central](https://img.shields.io/maven-central/v/com.viliussutkus89.ndk.thirdparty/libwmf-ndk26-shared.svg?label=Maven%20Central%20libwmf-ndk26-shared)](https://search.maven.org/search?q=g:com.viliussutkus89.ndk.thirdparty%20AND%20a:libwmf-ndk26-shared)

#### [wvWare](http://wvware.sourceforge.net)

wvWare has a Java wrapper - [wvWare-Android](https://github.com/ViliusSutkus89/wvWare-Android)

[![wvWare](https://github.com/ViliusSutkus89/ndkports/actions/workflows/wvWare.yml/badge.svg)](https://github.com/ViliusSutkus89/ndkports/actions/workflows/wvWare.yml)
[![Maven Central](https://img.shields.io/maven-central/v/com.viliussutkus89.ndk.thirdparty/wvWare-ndk25-static.svg?label=Maven%20Central%20wvWare-ndk25-static)](https://search.maven.org/search?q=g:com.viliussutkus89.ndk.thirdparty%20AND%20a:wvWare-ndk25-static)
[![Maven Central](https://img.shields.io/maven-central/v/com.viliussutkus89.ndk.thirdparty/wvWare-ndk25-shared.svg?label=Maven%20Central%20wvWare-ndk25-shared)](https://search.maven.org/search?q=g:com.viliussutkus89.ndk.thirdparty%20AND%20a:wvWare-ndk25-shared)
[![Maven Central](https://img.shields.io/maven-central/v/com.viliussutkus89.ndk.thirdparty/wvWare-ndk26-static.svg?label=Maven%20Central%20wvWare-ndk26-static)](https://search.maven.org/search?q=g:com.viliussutkus89.ndk.thirdparty%20AND%20a:wvWare-ndk26-static)
[![Maven Central](https://img.shields.io/maven-central/v/com.viliussutkus89.ndk.thirdparty/wvWare-ndk26-shared.svg?label=Maven%20Central%20wvWare-ndk26-shared)](https://search.maven.org/search?q=g:com.viliussutkus89.ndk.thirdparty%20AND%20a:wvWare-ndk26-shared)
