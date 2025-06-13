import org.jetbrains.dokka.gradle.DokkaTask

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.dokka)
    `maven-publish`
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

kotlin {
    jvmToolchain(19)
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

tasks.register<Jar>("sourcesJar") {
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
}

val dokkaJavadoc = tasks.named<DokkaTask>("dokkaJavadoc")

tasks.register<Jar>("javadocJar") {
    dependsOn(dokkaJavadoc)
    archiveClassifier.set("javadoc")
    from(dokkaJavadoc.get().outputDirectory)
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["kotlin"])
            artifact(tasks["sourcesJar"])
            artifact(tasks["javadocJar"])
            groupId = project.group.toString()
            artifactId = "CelestialNavEvents"
            version = project.version.toString()

            pom {
                name.set("CelestialNavEvents")
                description.set("Library for calculation of Sun and Moon astronomical events.")
                url.set("https://github.com/MSagGik/CelestialNavEvents")

                licenses {
                    license {
                        name.set("Apache License 2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0")
                    }
                }

                developers {
                    developer {
                        id.set("msaggik")
                        name.set("Maxim Sagaciyang")
                        email.set("dev.saggik@yandex.com")
                        url.set("https://msaggik.github.io")
                    }
                }

                scm {
                    connection.set("scm:git:https://github.com/MSagGik/CelestialNavEvents.git")
                    developerConnection.set("scm:git:ssh://github.com:MSagGik/CelestialNavEvents.git")
                    url.set("https://github.com/MSagGik/CelestialNavEvents")
                }
            }
        }
    }
}