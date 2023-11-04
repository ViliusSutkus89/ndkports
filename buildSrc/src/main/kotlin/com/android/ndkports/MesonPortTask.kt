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

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import java.io.File
import javax.inject.Inject

class MesonBuilder(val toolchain: Toolchain, val sysroot: File) :
    RunBuilder()

@Suppress("UnstableApiUsage")
abstract class MesonPortTask @Inject constructor(objects: ObjectFactory) :
    PortTask(objects) {

    @get:Input
    abstract val meson: Property<MesonBuilder.() -> Unit>

    fun meson(block: MesonBuilder.() -> Unit) = meson.set(block)

    override fun buildForAbi(
        toolchain: Toolchain,
        portDirectory: File,
        sourceDirectory: File,
        buildDirectory: File,
        installDirectory: File,
        generatedDirectory: File
    ) {
        val mesonBlock = meson.get()
        val builder = MesonBuilder(toolchain, generatedDirectory)
        builder.mesonBlock()

        val cpuFamily = when (toolchain.abi) {
            Abi.Arm -> "arm"
            Abi.Arm64 -> "aarch64"
            Abi.X86 -> "x86"
            Abi.X86_64 -> "x86_64"
        }

        val cpu = when (toolchain.abi) {
            Abi.Arm -> "armv7a"
            Abi.Arm64 -> "armv8a"
            Abi.X86 -> "i686"
            Abi.X86_64 -> "x86_64"
        }

        val crossFile = portDirectory.resolve("cross_file-${toolchain.abi.triple}.txt").apply {
            writeText(
                """
            [binaries]
            ar = '${toolchain.ar}'
            c = '${toolchain.clang}'
            cpp = '${toolchain.clangxx}'
            strip = '${toolchain.strip}'
            pkg-config = 'pkg-config'

            [host_machine]
            system = 'android'
            cpu_family = '$cpuFamily'
            cpu = '$cpu'
            endian = 'little'

            [built-in options]
            default_library = '${libraryType.get().argument}'

            [properties]
            needs_exe_wrapper = true
            pkg_config_libdir = '${generatedDirectory.resolve("lib/pkgconfig").absolutePath}'

            """.trimIndent()
            )
        }

        val logsDirectory = logsDirFor(toolchain.abi)
        executeSubprocess(
            args = listOf(
                "meson", "setup",
                "--cross-file", crossFile.absolutePath,
                "--buildtype", "release",
                "--prefix", installDirectory.absolutePath,
            ) + builder.cmd + listOf(
                buildDirectory.absolutePath,
                sourceDirectory.absolutePath
            ),
            workingDirectory = buildDirectory,
            additionalEnvironment = mutableMapOf<String, String>(
                "CFLAGS" to "-fPIC",
                "CXXFLAGS" to "-fPIC",
                "LDFLAGS" to "-pie",
            ).apply {
                builder.env.forEach {
                    put(it.key,
                        if (listOf("CFLAGS", "CXXFLAGS").contains(it.key))
                            "-fPIC ${it.value}"
                        else if ("LDFLAGS" == it.key)
                            "-pie ${it.value}"
                        else it.value
                    )
                }
            },
            logFile = logsDirectory.resolve("configure.log"
            )
        )

        executeSubprocess(
            args = listOf("ninja", "-v"), workingDirectory = buildDirectory, logFile = logsDirectory.resolve("build.log")
        )

        executeSubprocess(
            args = listOf("ninja", "-v", "install"), workingDirectory = buildDirectory, logFile = logsDirectory.resolve("install.log")
        )
    }
}