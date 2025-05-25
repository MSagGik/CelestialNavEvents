plugins {
    alias(libs.plugins.kotlin.jvm)
}

group = "io.github.msaggik"
version = libs.versions.versionName.get()

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    testImplementation(libs.jupiter)
    testImplementation(libs.jupiter.platform)
}

tasks.test {
    useJUnitPlatform()
    enabled = !project.hasProperty("skipTests")
}

tasks.register("releaseBuild") {
    dependsOn("build")
    doFirst {
        println("Running release build (tests are skipped).")
    }
    doLast {
        println("✅ Release build complete (tests skipped = ${!tasks.test.get().enabled}).")
    }
}

kotlin {
    jvmToolchain(19)
}