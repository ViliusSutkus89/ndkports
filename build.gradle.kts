buildscript {
    } else {
    }

    extra.apply {
    }
}

group = "com.android"
version = "1.0.0"

repositories {
    mavenCentral()
    google()
}

tasks.register("release") {
    dependsOn(project.getTasksByName("test", true))
    dependsOn(project.getTasksByName("distZip", true))
}
