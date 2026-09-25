import org.gradle.api.plugins.JavaPluginExtension

plugins {
    base
    `maven-publish`
    kotlin("jvm") version "2.3.20" apply false
    id("com.gradleup.shadow") version "9.4.3" apply false
}

allprojects {
    group = "me.xiaozhangup.crab"
    version = rootProject.file("versions/kotlin.txt").readLines().first()
    description = "Shared Kotlin libraries and plugin services for Minecraft."
    repositories {
        mavenCentral()
        mavenLocal()
        maven("https://maven.nostal.ink/repository/maven-public")
        maven("https://repo.papermc.io/repository/maven-public/")
    }
}

subprojects {
    plugins.withId("java") {
        extensions.configure<JavaPluginExtension> {
            toolchain.languageVersion.set(JavaLanguageVersion.of(25))
            sourceCompatibility = JavaVersion.VERSION_25
            targetCompatibility = JavaVersion.VERSION_25
        }
    }
}

tasks.assemble { dependsOn(":paper:assemble", ":velocity:assemble") }
tasks.check { dependsOn(":common:check", ":paper:check", ":velocity:check") }
tasks.clean { dependsOn(":common:clean", ":paper:clean", ":velocity:clean") }
// Publish only the two compile-time APIs under one coordinate and one POM.
evaluationDependsOn(":paper")
evaluationDependsOn(":velocity")
publishing {
    publications {
        create<MavenPublication>("maven") {
            artifactId = "CrabKotlin"
            for (platform in listOf("paper", "velocity")) {
                artifact(project(":$platform").tasks.named("apiJar")) {
                    classifier = platform
                }
            }
        }
    }
}
