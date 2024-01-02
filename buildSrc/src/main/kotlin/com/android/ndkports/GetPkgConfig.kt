package com.android.ndkports

import java.io.File

data class PkgConfig(
    val cflags: String,
    val libs: String,
)
private fun callPkgConfig(arguments: List<String>, pkgConfigLibDir: String): String {
    val pb = ProcessBuilder(arguments).redirectErrorStream(true)
    pb.environment()["PKG_CONFIG_LIBDIR"] = pkgConfigLibDir

    val result = pb.start()
    val status = result.waitFor();
    val output = result.inputStream.bufferedReader().use { it.readText() }.trim()
    if (status != 0) {
        throw RuntimeException("PkgConfigHelper failed with:\n$output")
    }
    return output
}

fun getPkgConfig(
    packageName: String,
    generatedDependenciesDir: File,
    isStatic: Boolean,
): Map<String, PkgConfig> {
    val config = mutableMapOf<String, PkgConfig>()
    val arguments = mutableListOf("pkg-config", packageName)
    if (isStatic)
        arguments.add("--static")
    generatedDependenciesDir.listFiles()?.forEach { generatedDirectory ->
        val triple = generatedDirectory.name
        val pkgConfigLibDir = generatedDirectory.resolve("lib/pkgconfig").absoluteFile
        assert(pkgConfigLibDir.exists())

        config[triple] = PkgConfig(
            cflags = callPkgConfig(arguments + "--cflags", pkgConfigLibDir = pkgConfigLibDir.path),
            libs = callPkgConfig(arguments + "--libs", pkgConfigLibDir = pkgConfigLibDir.path),
        )
    } ?: throw RuntimeException("Generated dependencies directory is empty!\n")
    return config
}
