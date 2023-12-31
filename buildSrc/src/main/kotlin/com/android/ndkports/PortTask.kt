package com.android.ndkports

import org.gradle.api.DefaultTask
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import java.io.File

@Suppress("UnstableApiUsage")
abstract class PortTask(objects: ObjectFactory) : DefaultTask() {
    enum class LibraryType(val argument: String) {
        Both("both"), Shared("shared"), Static("static")
    }

    @get:Input
    val libraryType: Property<LibraryType> =
        objects.property(LibraryType::class.java).convention(
            if (project.findProperty("libraryType") as String == "static") {
                LibraryType.Static
            } else {
                LibraryType.Shared
            }
        )

    @get:InputDirectory
    abstract val sourceDirectory: DirectoryProperty

    @get:Optional
    @get:Input
    abstract val sourceSubDirectory: Property<String>

    @get:OutputDirectory
    abstract val buildDir: DirectoryProperty

    @get:OutputDirectory
    val installDir: Provider<Directory>
        get() = buildDir.dir("install")

    @get:OutputDirectory
    val logsDir: Provider<Directory>
        get() = buildDir.dir("logs")

    @get:InputDirectory
    abstract val prefabGenerated: DirectoryProperty

    @get:Input
    abstract val minSdkVersion: Property<Int>

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
        additionalEnvironment: Map<String, String>? = null,
        logFile: File? = null
    ) {
        val pb = ProcessBuilder(args).redirectErrorStream(true)
            .directory(workingDirectory)

        logFile?.let {
            pb.redirectOutput(it)
        }

        if (additionalEnvironment != null) {
            pb.environment().putAll(additionalEnvironment)
        }

        val result = pb.start()
        if (result.waitFor() != 0) {
            val output = logFile?.readText() ?: result.inputStream.bufferedReader().use { it.readText() }
            throw RuntimeException("Subprocess failed with:\n$output")
        }
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun buildDirectoryFor(abi: Abi): File =
        buildDir.asFile.get().resolve("build/$abi")

    @Suppress("MemberVisibilityCanBePrivate")
    fun installDirectoryFor(abi: Abi): File =
        installDir.get().asFile.resolve("$abi")

    @Suppress("MemberVisibilityCanBePrivate")
    fun generatedDirectoryFor(abi: Abi): File =
        prefabGenerated.get().asFile.resolve(abi.triple)

    fun logsDirFor(abi: Abi): File =
        logsDir.get().asFile.resolve(abi.archName)

    @TaskAction
    fun run() {
        for (abi in Abi.values()) {
            logsDirFor(abi).mkdir()
            val api = abi.adjustMinSdkVersion(minSdkVersion.get())
            val sourceDir = if (sourceSubDirectory.isPresent) {
                sourceDirectory.get().asFile.resolve(sourceSubDirectory.get())
            } else {
                sourceDirectory.get().asFile
            }
            buildForAbi(
                Toolchain(ndk, abi, api),
                portDirectory = buildDir.asFile.get(),
                sourceDirectory = sourceDir,
                buildDirectory = buildDirectoryFor(abi).apply { mkdirs() },
                installDirectory = installDirectoryFor(abi),
                generatedDirectoryFor(abi)
            )
        }
    }

    abstract fun buildForAbi(
        toolchain: Toolchain,
        portDirectory: File,
        sourceDirectory: File,
        buildDirectory: File,
        installDirectory: File,
        generatedDirectory: File,
    )
}