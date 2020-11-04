package com.android.ndkports

import com.google.prefab.api.Android
import com.google.prefab.api.Package
import org.gradle.api.DefaultTask
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.util.zip.ZipFile
import javax.inject.Inject

abstract class SourceExtractTask : DefaultTask() {
    @get:InputFile
    abstract val source: RegularFileProperty

    @get:OutputDirectory
    abstract val outDir: DirectoryProperty

    @TaskAction
    fun run() {
        val pb = ProcessBuilder(
            listOf(
                "tar",
                "xf",
                source.get().asFile.absolutePath,
                "--strip-components=1"
            )
        ).redirectErrorStream(true).directory(outDir.get().asFile)

        val result = pb.start()
        val output = result.inputStream.bufferedReader().use { it.readText() }
        if (result.waitFor() != 0) {
            throw RuntimeException("Subprocess failed with:\n$output")
        }
    }
}

abstract class PrefabTask : DefaultTask() {
    @InputFiles
    lateinit var aars: FileCollection

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    @get:OutputDirectory
    val sysrootDirectory: Provider<Directory>
        get() = outputDirectory.dir("sysroot")

    @get:InputDirectory
    abstract val ndkPath: DirectoryProperty

    @get:Input
    abstract val minSdkVersion: Property<Int>

    private val ndk: Ndk
        get() = Ndk(ndkPath.asFile.get())

    @TaskAction
    fun run() {
        val packages = mutableListOf<Package>()
        val outDir = outputDirectory.get().asFile
        for (aar in aars) {
            val packagePath = outDir.resolve(aar.nameWithoutExtension)
            extract(aar, packagePath)
            packages.add(Package(packagePath.toPath().resolve("prefab")))
        }
        generateSysroot(packages, minSdkVersion.get(), ndk.version.major)
    }

    private fun extract(aar: File, extractDir: File) {
        ZipFile(aar).use { zip ->
            zip.entries().asSequence().forEach { entry ->
                zip.getInputStream(entry).use { input ->
                    val outFile = extractDir.resolve(entry.name)
                    if (entry.isDirectory) {
                        outFile.mkdirs()
                    } else {
                        outFile.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                }
            }
        }
    }

    private fun generateSysroot(
        packages: List<Package>, osVersion: Int, ndkVersion: Int
    ) {
        val buildSystemIntegration =
            PrefabSysrootPlugin(sysrootDirectory.get().asFile, packages)

        buildSystemIntegration.generate(Android.Abi.values().map {
            Android(it, osVersion, Android.Stl.CxxShared, ndkVersion)
        })
    }
}

@Suppress("UnstableApiUsage")
abstract class NdkPortsTask : DefaultTask() {

    @get:InputDirectory
    abstract val sourceDirectory: DirectoryProperty

    @get:OutputDirectory
    abstract val buildDir: DirectoryProperty

    @get:OutputDirectory
    val installDir: Provider<Directory>
        get() = buildDir.dir("install")

    @get:InputDirectory
    abstract val ndkPath: DirectoryProperty

    private val ndk: Ndk
        get() = Ndk(ndkPath.asFile.get())

    /**
     * The number of CPUs available for building.
     *
     * May be passed to the build system if required.
     */
    @Internal
    protected val ncpus = Runtime.getRuntime().availableProcessors()

    protected fun executeSubprocess(
        args: List<String>,
        workingDirectory: File,
        additionalEnvironment: Map<String, String>? = null
    ) {
        val pb = ProcessBuilder(args).redirectErrorStream(true)
            .directory(workingDirectory)

        if (additionalEnvironment != null) {
            pb.environment().putAll(additionalEnvironment)
        }

        val result = pb.start()
        val output = result.inputStream.bufferedReader().use { it.readText() }
        if (result.waitFor() != 0) {
            throw RuntimeException("Subprocess failed with:\n$output")
        }
    }

    @TaskAction
    fun run() {
        val workingDirectory = buildDir.asFile.get()

        val apiForAbi = mapOf(
            Abi.Arm to 16, Abi.Arm64 to 21, Abi.X86 to 16, Abi.X86_64 to 21
        )
        for (abi in Abi.values()) {
            val api = apiForAbi.getOrElse(abi) {
                throw RuntimeException(
                    "No API level specified for ${abi.abiName}"
                )
            }

            buildForAbi(
                Toolchain(ndk, abi, api),
                workingDirectory,
                buildDirectory = workingDirectory.resolve("build/$abi"),
                installDirectory = installDir.get().asFile.resolve("$abi")
            )
        }
    }

    abstract fun buildForAbi(
        toolchain: Toolchain,
        workingDirectory: File,
        buildDirectory: File,
        installDirectory: File
    )
}

abstract class ModuleProperty @Inject constructor(
    objectFactory: ObjectFactory,
    @get:Input val name: String,
) {
    @Suppress("UnstableApiUsage")
    @get:Input
    val headerOnly: Property<Boolean> =
        objectFactory.property(Boolean::class.java).convention(false)

    @Suppress("UnstableApiUsage")
    @get:Input
    val includesPerAbi: Property<Boolean> =
        objectFactory.property(Boolean::class.java).convention(false)

    @Suppress("UnstableApiUsage")
    @get:Input
    val dependencies: ListProperty<String> =
        objectFactory.listProperty(String::class.java).convention(emptyList())
}

abstract class PackageBuilderTask @Inject constructor(
    objectFactory: ObjectFactory,
) : DefaultTask() {
    /**
     * The name of the port. Will be used as the package name in prefab.json.
     */
    @Suppress("UnstableApiUsage")
    @get:Input
    val packageName: Property<String> =
        objectFactory.property(String::class.java).convention(project.name)

    /**
     * The version to encode in the prefab.json.
     *
     * This version must be compatible with CMake's `find_package` for
     * config-style packages. This means that it must be one to four decimal
     * separated integers. No other format is allowed.
     *
     * If not set, the default is [Project.getVersion] as interpreted by
     * [CMakeCompatibleVersion.parse].
     *
     * For example, OpenSSL 1.1.1g will set this value to
     * `CMakeCompatibleVersion(1, 1, 1, 7)`.
     */
    @get:Input
    abstract val version: Property<CMakeCompatibleVersion>

    @get:Nested
    abstract val modules: NamedDomainObjectContainer<ModuleProperty>

    @Suppress("UnstableApiUsage")
    @get:Input
    val licensePath: Property<String> =
        objectFactory.property(String::class.java).convention("LICENSE")

    @Suppress("UnstableApiUsage")
    @get:Input
    abstract val dependencies: MapProperty<String, String>

    @get:InputDirectory
    abstract val sourceDirectory: DirectoryProperty

    @get:InputDirectory
    abstract val installDirectory: DirectoryProperty

    @get:Internal
    abstract val outDir: DirectoryProperty

    @get:OutputDirectory
    val intermediatesDirectory: Provider<Directory>
        get() = outDir.dir("aar")

    @get:InputDirectory
    abstract val ndkPath: DirectoryProperty

    private val ndk: Ndk
        get() = Ndk(ndkPath.asFile.get())

    @TaskAction
    fun run() {
        val apiForAbi = mapOf(
            Abi.Arm to 16, Abi.Arm64 to 21, Abi.X86 to 16, Abi.X86_64 to 21
        )
        val modules = modules.asMap.values.map {
            ModuleDescription(
                it.name,
                it.headerOnly.get(),
                it.includesPerAbi.get(),
                it.dependencies.get()
            )
        }
        PrefabPackageBuilder(
            PackageData(
                packageName.get(),
                project.version as String,
                version.get(),
                licensePath.get(),
                modules,
                dependencies.get(),
            ),
            intermediatesDirectory.get().asFile,
            installDirectory.get().asFile,
            sourceDirectory.get().asFile,
            ndk,
            apiForAbi,
        ).build()
    }
}