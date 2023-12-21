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

import com.google.prefab.api.AndroidAbiMetadata
import com.google.prefab.api.ModuleMetadataV1
import com.google.prefab.api.PackageMetadataV1
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.redundent.kotlin.xml.xml
import java.io.File
import java.io.Serializable

data class PackageData(
    val name: String,
    val mavenVersion: String,
    val prefabVersion: CMakeCompatibleVersion,
    val minSdkVersion: Int,
    val licensePath: String,
    val modules: List<ModuleDescription>,
    val dependencies: Map<String, String>,
)

/**
 * A module exported by the package.
 *
 * As currently implemented by ndkports, one module is exactly one library.
 * Prefab supports header-only libraries, but ndkports does not support these
 * yet.
 *
 * Static libraries are not currently supported by ndkports.
 *
 * @property[name] The name of the module. Note that currently the name of the
 * installed library file must be exactly `lib$name.so`.
 * @property[includesPerAbi] Set to true if a different set of headers should be
 * exposed per-ABI. Not currently implemented.
 * @property[dependencies] A list of other modules required by this module, in
 * the format described by https://google.github.io/prefab/.
 */
data class ModuleDescription(
    val name: String,
    val static: Boolean,
    val headerOnly: Boolean,
    val includesPerAbi: Boolean,
    val dependencies: List<String>,
) : Serializable

class PrefabPackageBuilder(
    private val packageData: PackageData,
    private val packageDirectory: File,
    private val installDirectory: File,
    private val sourceDirectory: File,
    private val ndk: Ndk,
) {
    private val prefabDirectory = packageDirectory.resolve("prefab")
    private val modulesDirectory = prefabDirectory.resolve("modules")
    private val assetsDirectory = packageDirectory.resolve("assets")

    // TODO: Get from gradle.
    private val packageName = "com.viliussutkus89.ndk.thirdparty.${packageData.name.replace("-", "_")}"

    private fun preparePackageDirectory() {
        if (packageDirectory.exists()) {
            packageDirectory.deleteRecursively()
        }
        modulesDirectory.mkdirs()
    }

    private fun makePackageMetadata() {
        prefabDirectory.resolve("prefab.json").writeText(
            Json.encodeToString(
                PackageMetadataV1(
                    packageData.name,
                    schemaVersion = 1,
                    dependencies = packageData.dependencies.keys.toList(),
                    version = packageData.prefabVersion.toString()
                )
            )
        )
    }

    private fun makeModuleMetadata(module: ModuleDescription, moduleDirectory: File) {
        moduleDirectory.resolve("module.json").writeText(
            Json.encodeToString(
                ModuleMetadataV1(
                    exportLibraries = module.dependencies
                )
            )
        )
    }

    private fun installLibForAbi(module: ModuleDescription, abi: Abi, libsDir: File) {
        val extension = if (module.static) "a" else "so"
        val libName = "lib${module.name}.${extension}"
        val dstDir = libsDir.resolve("android.${abi.abiName}")

        installDirectory.resolve("$abi/lib/$libName").copyTo(dstDir.resolve(libName))

        dstDir.resolve("abi.json").writeText(
            Json.encodeToString(
                AndroidAbiMetadata(
                    abi = abi.abiName,
                    api = abi.adjustMinSdkVersion(packageData.minSdkVersion),
                    ndk = ndk.version.major,
                    stl = "c++_shared"
                )
            )
        )
    }

    private fun installLicense() {
        val src = sourceDirectory.resolve(packageData.licensePath)
        val dest = packageDirectory.resolve("META-INF")
            .resolve(File(packageData.licensePath).name)
        src.copyTo(dest)
    }

    private fun createAndroidManifest() {
        packageDirectory.resolve("AndroidManifest.xml")
            .writeText(xml("manifest") {
                attributes(
                    "xmlns:android" to "http://schemas.android.com/apk/res/android",
                    "package" to packageName,
                    "android:versionCode" to 1,
                    "android:versionName" to "1.0"
                )

                "uses-sdk" {
                    attributes(
                        "android:minSdkVersion" to packageData.minSdkVersion,
                        "android:targetSdkVersion" to 34
                    )
                }
            }.toString())
    }

    private fun installAssets() {
        val sourceAssets = installDirectory.parentFile.parentFile.resolve("assets")
        if (sourceAssets.exists()) {
            sourceAssets.copyRecursively(assetsDirectory)
        }
    }

    fun build() {
        preparePackageDirectory()
        makePackageMetadata()
        for (module in packageData.modules) {
            val moduleDirectory = modulesDirectory.resolve(module.name).apply {
                mkdirs()
            }

            makeModuleMetadata(module, moduleDirectory)

            val libsDir = moduleDirectory.resolve("libs").apply { mkdir() }
            for (abi in Abi.values()) {
                libsDir.resolve("android.${abi.abiName}").apply { mkdir() }
                installConfigForAbi(module, abi, libsDir)
                if (!module.headerOnly) {
                    installLibForAbi(module, abi, libsDir)
                }
            }

            Abi.values().forEach { abi ->
                val destination = if (module.includesPerAbi) {
                    libsDir.resolve("android.${abi.abiName}").apply{ mkdir() }.resolve("include").apply { mkdir() }
                } else {
                    moduleDirectory.resolve("include").apply { mkdir() }
                }
                installDirectory.resolve("${abi}/include").copyRecursively(destination) { file, exception ->
                    if (exception !is FileAlreadyExistsException) {
                        throw exception
                    }

                    if (!file.readBytes().contentEquals(exception.file.readBytes())) {
                        val path = file.relativeTo(destination)
                        throw RuntimeException(
                            "Found duplicate headers with non-equal contents: $path"
                        )
                    }
                    OnErrorAction.SKIP
                }
            }
        }

        installAssets()

        installLicense()

        createAndroidManifest()
    }

    private fun installConfigForAbi(module: ModuleDescription, abi: Abi, libsDir: File) {
        val srcDir = installDirectory.resolve("$abi/lib")
        val dstDir = libsDir.resolve("android.${abi.abiName}")

        val abiInstallDir = installDirectory.resolve("$abi").canonicalPath
        val generatedDir = installDirectory.resolve("../dependencies/generated/${abi.triple}").canonicalPath
        val ndkPath = ndk.path.canonicalPath

        srcDir.walkTopDown().forEach { srcFile ->
            if (srcFile.isFile && listOf("cmake", "la", "pc").contains(srcFile.extension)) {
                val dstFile = dstDir.resolve(srcFile.relativeTo(srcDir))
                dstFile.parentFile.mkdirs()
                var configFileContent = srcFile.readText()
                    .replace(abiInstallDir, "/__PREFAB__PACKAGE__PATH__")
                    .replace(generatedDir, "/__PREFAB__PACKAGE__PATH__")
                    .replace(ndkPath, "/__NDK__PATH__")

                // Some pkg-config file parsers don't like when "Libs" and "Requires" appear multiple times
                // Merge them.
                if (dstFile.extension == "pc") {
                    val sb = StringBuilder()
                    val libs = mutableListOf<String>()
                    val libsPrivate = mutableListOf<String>()
                    val requires = mutableListOf<String>()
                    val requiresPrivate = mutableListOf<String>()

                    configFileContent.split("\r\n", "\r", "\n").forEach { line ->
                        if (line.startsWith(prefix = "Libs:", ignoreCase = true)) {
                            libs.add(line.substring("Libs:".length))
                        }
                        else if (line.startsWith(prefix = "Libs.private:", ignoreCase = true)) {
                            libsPrivate.add(line.substring("Libs.private:".length))
                        }
                        else if (line.startsWith(prefix = "Requires:", ignoreCase = true)) {
                            requires.add(line.substring("Requires:".length))
                        }
                        else if (line.startsWith(prefix = "Requires.private:", ignoreCase = true)) {
                            requiresPrivate.add(line.substring("Requires.private:".length))
                        }
                        else {
                            sb.appendLine(line)
                        }
                    }

                    // Some dependencies link against static libraries,
                    // but don't pick up private dependencies.
                    // Workaround this issue by marking all private
                    // dependencies as public dependencies in pkg-config.
                    if (module.static) {
                        libs.addAll(libsPrivate)
                        libsPrivate.clear()
                        requires.addAll(requiresPrivate)
                        requiresPrivate.clear()
                    }
                    listOf(
                        Pair("Requires", requires),
                        Pair("Requires.private", requiresPrivate),
                        Pair("Libs", libs),
                        Pair("Libs.private", libsPrivate),
                    ).forEach { deps ->
                        deps.second.joinToString(" ").trim().let { depsJoined ->
                            if (depsJoined.isNotEmpty()) {
                                sb.appendLine("${deps.first}: $depsJoined")
                            }
                        }
                    }
                    configFileContent = sb.toString()
                }

                dstFile.writeText(configFileContent)
            }
        }
    }
}
