/*
 * Copyright (C) 2021 The Android Open Source Project
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

class CMakeBuilder(val toolchain: Toolchain, val sysroot: File) :
    RunBuilder()

abstract class CMakePortTask @Inject constructor(objects: ObjectFactory) : PortTask(objects) {
    @get:Input
    abstract val cmake: Property<CMakeBuilder.() -> Unit>

    fun cmake(block: CMakeBuilder.() -> Unit) = cmake.set(block)

    override fun buildForAbi(
        toolchain: Toolchain,
        portDirectory: File,
        sourceDirectory: File,
        buildDirectory: File,
        installDirectory: File,
        generatedDirectory: File
    ) {
        val cmakeBlock = cmake.get()
        val builder = CMakeBuilder(toolchain, generatedDirectory)
        builder.cmakeBlock()

        val toolchainFile = toolchain.ndk.path.resolve("build/cmake/android.toolchain.cmake")

        val libraryTypeArguments = when (libraryType.get()) {
            LibraryType.Static -> listOf("-DBUILD_STATIC_LIBS=ON", "-DBUILD_SHARED_LIBS=OFF")
            LibraryType.Shared -> listOf("-DBUILD_STATIC_LIBS=OFF", "-DBUILD_SHARED_LIBS=ON")
            LibraryType.Both -> listOf("-DBUILD_STATIC_LIBS=ON", "-DBUILD_SHARED_LIBS=ON")
            else -> listOf("")
        }

        val logsDirectory = logsDirFor(toolchain.abi)

        executeSubprocess(
            args = listOf(
                "cmake",
                "-DCMAKE_TOOLCHAIN_FILE=${toolchainFile.absolutePath}",
                "-DCMAKE_BUILD_TYPE=RelWithDebInfo",
                "-DCMAKE_PREFIX_PATH=${generatedDirectory.absolutePath}",
                "-DCMAKE_FIND_ROOT_PATH=${generatedDirectory.absolutePath}",
                "-DCMAKE_INSTALL_PREFIX=${installDirectory.absolutePath}",
                "-DANDROID_ABI=${toolchain.abi.abiName}",
                "-DANDROID_API_LEVEL=${toolchain.api}",
                "-DANDROID_PLATFORM=${toolchain.api}",
                "-GNinja",
                sourceDirectory.absolutePath
            ) + libraryTypeArguments + builder.cmd,
            workingDirectory = buildDirectory,
            additionalEnvironment = mutableMapOf(
                "PKG_CONFIG_LIBDIR" to generatedDirectory.resolve("lib/pkgconfig").absolutePath,
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
            logFile = logsDirectory.resolve("configure.log")
        )

        executeSubprocess(listOf("ninja", "-v"), buildDirectory, logFile = logsDirectory.resolve("build.log"))

        executeSubprocess(listOf("ninja", "-v", "install"), buildDirectory, logFile = logsDirectory.resolve("install.log"))
    }
}
