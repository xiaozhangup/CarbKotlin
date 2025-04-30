plugins {
    id("com.gradleup.shadow") version "8.3.5"
    java
}

val kotlinVersion = version("kotlin")
val coroutinesVersion = version("kotlinx-coroutines")
val serializationVersion = version("kotlinx-serialization")
val atomicfuVersion = version("kotlinx-atomicfu")
val kotlinIoVersion = version("kotlinx-io")
val dateTimeVersion = version("kotlinx-datetime")
val ktorVersion = version("ktor")

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

group = "xyz.gmitch215.kotlinmc"
version = kotlinVersion
description = "A Library Jar containing various Kotlin Dependencies for Minecraft Plugins."

project.ext["url"] = "https://github.com/gmitch215/KotlinMC"
project.ext["kotlin_version"] = kotlinVersion

tasks {
    jar.configure {
        enabled = false
        dependsOn(shadowJar)
    }

    shadowJar {
        archiveClassifier.set("")

        dependencies {
            exclude {
                it.moduleName.contains("annotations")
            }
        }
    }

    processResources {
        expand(project.properties)
    }

}

repositories {
    mavenCentral()
    mavenLocal()

    maven("https://plugins.gradle.org/m2/")
    maven("https://oss.sonatype.org/content/repositories/snapshots")
    maven("https://oss.sonatype.org/content/repositories/central")
    maven("https://repo.papermc.io/repository/maven-public/")
}

// Dependencies & Plugins

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib:${kotlinVersion}")
    implementation("org.jetbrains.kotlin:kotlin-reflect:${kotlinVersion}")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${coroutinesVersion}")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:${serializationVersion}")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:${dateTimeVersion}")
//    implementation("org.jetbrains.kotlinx:kotlinx-io-core:${kotlinIoVersion}")
//    implementation("org.jetbrains.kotlinx:atomicfu:${atomicfuVersion}")

    implementation("io.ktor:ktor-server-core:${ktorVersion}")
    implementation("io.ktor:ktor-server-netty:${ktorVersion}")
    implementation("io.ktor:ktor-server-host-common:${ktorVersion}")

    compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")
    compileOnly("com.velocitypowered:velocity-api:3.1.1")
}

fun version(name: String): String = File("versions/${name}.txt").bufferedReader().use { it.readLine() }
