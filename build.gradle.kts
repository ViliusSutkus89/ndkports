plugins {
    id("io.github.gradle-nexus.publish-plugin") version "1.3.0"
}

buildscript {
    val ndkVersion = File(project.findProperty("ndkPath") as String).name
    val majorNdkVersion = ndkVersion.split(".")[0].toInt()
    val minSdkSupportedByNdk = if (majorNdkVersion >= 26) {
        21
    } else {
        19
    }
    extra.apply {
        set("ndkVersionSuffix", "-ndk${majorNdkVersion}")
        set("minSdkSupportedByNdk", minSdkSupportedByNdk)

        when (project.findProperty("libraryType")) {
            "shared" -> {
                set("libraryTypeSuffix", "-shared")
                set("dependencyLibraryTypeSuffix", "-static")
            }
            "shared-with-shared-deps" -> {
                set("libraryTypeSuffix", "-shared-with-shared-deps")
                set("dependencyLibraryTypeSuffix", "-shared")
            }
            /* static */ else -> {
                set("libraryTypeSuffix", "-static")
                set("dependencyLibraryTypeSuffix", "-static")
            }
        }
    }
}

group = "com.viliussutkus89.ndk.thirdparty"
version = "1.0.0"

repositories {
    mavenCentral()
    google()
}

nexusPublishing {
    project.findProperty("repoDescription")?.toString()?.let {
        if (it.isNotEmpty()) {
            repositoryDescription.set(it)
        }
    }
    repositories {
        sonatype()
    }
}
