import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val kotlinVersion = "1.4.20"

plugins {
    kotlin("jvm") version "1.4.20"
    application
}

group = "com.android"
version = "1.0.0-SNAPSHOT"

repositories {
    mavenCentral()
    jcenter()
    google()
    maven(url = "https://dl.bintray.com/s1m0nw1/KtsRunner")
}

dependencies {
    implementation(kotlin("stdlib", kotlinVersion))
    implementation(kotlin("reflect", kotlinVersion))

    // JSR223 support.
    implementation(kotlin("script-runtime", kotlinVersion))
    implementation(kotlin("script-util", kotlinVersion))
    implementation(kotlin("compiler-embeddable", kotlinVersion))
    implementation(kotlin("scripting-compiler-embeddable", kotlinVersion))
    implementation("net.java.dev.jna:jna:5.6.0")
    runtime(kotlin("scripting-compiler-embeddable", kotlinVersion))

    implementation("com.google.prefab:api:1.0.0")

    implementation("com.github.ajalt:clikt:2.2.0")
    implementation("org.apache.maven:maven-core:3.6.2")
    implementation("org.redundent:kotlin-xml-builder:1.5.3")

    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0-M1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.6.0-M1")
}

application {
    // Define the main class for the application.
    mainClassName = "com.android.ndkports.CliKt"
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
    kotlinOptions.freeCompilerArgs += listOf(
        "-progressive",
        "-Xuse-experimental=kotlinx.serialization.ImplicitReflectionSerializer"
    )
}

val portsBuildDir = buildDir.resolve("ports")

val allPorts = listOf("openssl", "curl", "jsoncpp")

// Can be specified in ~/.gradle/gradle.properties:
//
//     ndkPath=/path/to/ndk
//
// Or on the command line:
//
//     ./gradlew -PndkPath=/path/to/ndk run
val ndkPath: String by project
tasks.named<JavaExec>("run") {
    // Order matters since we don't do any dependency sorting, so we can't just
    // use the directory list.
    args = listOf("--ndk", ndkPath, "-o", portsBuildDir.toString()) + allPorts
}

for (port in allPorts) {
    distributions {
        create(port) {
            contents {
                includeEmptyDirs = false
                from(portsBuildDir.resolve(port)) {
                    include("**/*.aar")
                    include("**/*.pom")
                }
            }
        }
    }

    tasks.named("${port}DistTar") {
        dependsOn(":run")
    }

    tasks.named("${port}DistZip") {
        dependsOn(":run")
    }
}

distributions {
    create("all") {
        contents {
            includeEmptyDirs = false
            from(portsBuildDir) {
                include("**/*.aar")
                include("**/*.pom")
            }
        }
    }
}

tasks.named("allDistTar") {
    dependsOn(":run")
}

tasks.named("allDistZip") {
    dependsOn(":run")
}

tasks.register("release") {
    dependsOn(":allDistZip")
    for (port in allPorts) {
        dependsOn(":${port}DistZip")
    }
}
