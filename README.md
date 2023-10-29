# ndkports

A collection of Android build scripts for various open source libraries and the
tooling to build them.

Buildscripts are based on Google's [ndkports](https://android.googlesource.com/platform/tools/ndkports/).

Compiled binaries are (will be) distributed through MavenCentral.

## Matrix

Each port is built on a matrix of NDK versions and library type.

- com.viliussutkus89.ndk.thirdparty:libfoo-ndk26-static:0.4.1
- com.viliussutkus89.ndk.thirdparty:libfoo-ndk26-shared:0.4.1
- com.viliussutkus89.ndk.thirdparty:libfoo-ndk25-static:0.4.1
- com.viliussutkus89.ndk.thirdparty:libfoo-ndk25-shared:0.4.1

#### Min SDK Version:

Builds compiled with NDK-26 support Android SDK 21 (Lollipop) and later.

Builds compiled with NDK-25 support Android SDK 19 (KitKat) and later.

Libraries built with different NDK versions should not be used in the same application.

#### Libraries are built as:

- static (libfoo.a) with static dependencies
- shared (libfoo.so) with static dependencies

## TODO

Run unit tests provided by upstream packages.

Figure out proper way to deliver per ABI headers.

## Ports

#### [GNU FriBidi](https://github.com/fribidi/fribidi)
