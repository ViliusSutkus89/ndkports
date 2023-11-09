package com.android.ndkports

import com.google.prefab.api.BuildSystemInterface
import com.google.prefab.api.Module
import com.google.prefab.api.Package
import com.google.prefab.api.PlatformDataInterface
import java.io.File

class PrefabSysrootPlugin(
    override val outputDirectory: File, override val packages: List<Package>
) : BuildSystemInterface {

    override fun generate(requirements: Collection<PlatformDataInterface>) {
        prepareOutputDirectory(outputDirectory)

        for (requirement in requirements) {
            val libDir = outputDirectory.resolve(requirement.targetTriple).apply { mkdir() }.resolve("lib").apply { mkdir() }
            createZlibPkgconf(libDir)
            for (pkg in packages) {
                for (module in pkg.modules) {
                    installModule(module, requirement)
                }
            }
        }
    }

    private fun installModule(
        module: Module, requirement: PlatformDataInterface
    ) {
        val installDir = outputDirectory.resolve(requirement.targetTriple)
        val includeDir = installDir.resolve("include").apply { mkdir() }
        val libDir = installDir.resolve("lib")

        installHeaders(module, includeDir, requirement.targetTriple)
        installConfigFiles(module, libDir, requirement.targetTriple)

        if (!module.isHeaderOnly) {
            module.getLibraryFor(requirement).path.toFile().apply {
                copyTo(libDir.resolve(name))
            }
        }
    }

    private fun targetTripleToAbiName(triple: String): String {
        return when (triple) {
            "arm-linux-androideabi" -> "armeabi-v7a"
            "aarch64-linux-android" -> "arm64-v8a"
            "i686-linux-android" -> "x86"
            "x86_64-linux-android" -> "x86_64"
            else -> ""
        }
    }

    private fun installHeaders(module: Module, includeDir: File, abiTriple: String) {
        val commonHeaders = module.includePath.toFile().listFiles() ?: arrayOf<File>()
        val perAbiHeaders = module.path.toFile().resolve("libs/android.${abiTriple}/include").listFiles() ?: arrayOf<File>()
        (commonHeaders + perAbiHeaders).forEach {
            it.copyRecursively(includeDir.resolve(it.name)) { file, exception ->
                if (exception !is FileAlreadyExistsException) {
                    throw exception
                }

                if (!file.readBytes().contentEquals(exception.file.readBytes())) {
                    val path = file.relativeTo(includeDir)
                    throw RuntimeException(
                        "Found duplicate headers with non-equal contents: $path"
                    )
                }

                OnErrorAction.SKIP
            }
        }
    }

    private fun installConfigFiles(module: Module, libDir: File, abiTriple: String) {
        val src = module.path.toFile().resolve("libs/android.${targetTripleToAbiName(abiTriple)}")

        src.listFiles { file, filename ->
            listOf("cmake", "pkgconfig").contains(filename) || file.extension == "la"
        }?.forEach {
            it.copyRecursively(libDir.resolve(it.name)) { file, exception ->
                if (exception !is FileAlreadyExistsException) {
                    throw exception
                }

                if (!file.readBytes().contentEquals(exception.file.readBytes())) {
                    val path = file.relativeTo(src)
                    throw RuntimeException(
                        "Found duplicate headers with non-equal contents: $path"
                    )
                }

                OnErrorAction.SKIP
            }
        }
    }

    private fun createZlibPkgconf(libDir: File) {
        val zlibPc = libDir.resolve("pkgconfig").apply { mkdir() }.resolve("zlib.pc")
        if (!zlibPc.exists()) {
            zlibPc.writeText("""
                Name: zlib
                Description: zlib compression library
                Version: 1.2.7

                Requires:
                Libs: -lz

            """.trimIndent())
        }
    }
}