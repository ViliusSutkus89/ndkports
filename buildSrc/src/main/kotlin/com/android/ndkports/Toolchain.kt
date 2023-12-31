/*
 * Copyright (C) 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.ndkports

class Toolchain(val ndk: Ndk, val abi: Abi, val api: Int) {
    val binutilsTriple = when (abi) {
        Abi.Arm -> "arm-linux-androideabi"
        Abi.Arm64 -> "aarch64-linux-android"
        Abi.X86 -> "i686-linux-android"
        Abi.X86_64 -> "x86_64-linux-android"
    }

    private val clangTriple = when (abi) {
        Abi.Arm -> "armv7a-linux-androideabi$api"
        else -> "$binutilsTriple$api"
    }

    val sysrootLibs = ndk.sysrootDirectory.resolve("usr/lib/$binutilsTriple")

    val binDir = ndk.toolchainBinDirectory
    val ar = binDir.resolve("llvm-ar")
    val clang = binDir.resolve("$clangTriple-clang")
    val clangxx = binDir.resolve("$clangTriple-clang++")
    val nm = binDir.resolve("llvm-nm")
    val objdump = binDir.resolve("llvm-objdump")
    val ranlib = binDir.resolve("llvm-ranlib")
    val readelf = binDir.resolve("llvm-readelf")
    val strip = binDir.resolve("llvm-strip")
}