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

class AutoconfBuilder(val toolchain: Toolchain, val sysroot: File) :
    RunBuilder()

abstract class AutoconfPortTask @Inject constructor(objects: ObjectFactory) : PortTask(objects) {

    @get:Input
    abstract val autoconf: Property<AutoconfBuilder.() -> Unit>

    fun autoconf(block: AutoconfBuilder.() -> Unit) = autoconf.set(block)

    override fun buildForAbi(
        toolchain: Toolchain,
        portDirectory: File,
        sourceDirectory: File,
        buildDirectory: File,
        installDirectory: File,
        generatedDirectory: File
    ) {
        val autoconfBlock = autoconf.get()
        val builder = AutoconfBuilder(
            toolchain, prefabGenerated.get().asFile.resolve(toolchain.abi.triple)
        )
        builder.autoconfBlock()

        val libraryTypeArguments = when (libraryType.get()) {
            LibraryType.Static -> listOf("--enable-static", "--disable-shared")
            LibraryType.Shared -> listOf("--disable-static", "--enable-shared")
            LibraryType.Both -> listOf("--enable-static", "--enable-shared")
            else -> listOf("")
        }

        val logsDirectory = logsDirFor(toolchain.abi)

        executeSubprocess(
            args = listOf(
                sourceDirectory.resolve("configure").absolutePath,
                "--host=${toolchain.binutilsTriple}",
                "--prefix=${installDirectory.absolutePath}"
            ) + libraryTypeArguments + builder.cmd,
            workingDirectory = buildDirectory,
            additionalEnvironment = mutableMapOf(
                "AR" to toolchain.ar.absolutePath,
                "CC" to toolchain.clang.absolutePath,
                "CXX" to toolchain.clangxx.absolutePath,
                "RANLIB" to toolchain.ranlib.absolutePath,
                "STRIP" to toolchain.strip.absolutePath,
                "PKG_CONFIG_LIBDIR" to generatedDirectory.resolve("lib/pkgconfig").absolutePath,
                "PATH" to "${toolchain.binDir}:${System.getenv("PATH")}",
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
            }, logFile = logsDirectory.resolve("configure.log")
        )

        executeSubprocess(
            listOf("make", "-j$ncpus"), buildDirectory, logFile = logsDirFor(toolchain.abi).resolve("build.log")
        )

        executeSubprocess(
            listOf("make", "-j$ncpus", "install"), buildDirectory, logFile = logsDirFor(toolchain.abi).resolve("install.log")
        )
    }
}