buildscript {
    val snapshotSuffix = if (hasProperty("release")) {
        ""
    } else {
        "-SNAPSHOT"
    }

    extra.apply {
        set("snapshotSuffix", snapshotSuffix)
    }
}

group = "com.android"
version = "1.0.0${extra.get("snapshotSuffix")}"

plugins {
    distribution
}

repositories {
    mavenCentral()
    jcenter()
    google()
}

distributions {
    main {
        contents {
            from("${rootProject.buildDir}/repository")
            include("**/*.aar")
            include("**/*.pom")
        }
    }
}

tasks {
    distZip {
        dependsOn(project.getTasksByName("publish", true))
    }
}

tasks.register("release") {
    dependsOn(project.getTasksByName("test", true))
    dependsOn(":distZip")
}
