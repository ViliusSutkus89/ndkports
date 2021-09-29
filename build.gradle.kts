group = "com.android"
version = "1.0.0-SNAPSHOT"

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
