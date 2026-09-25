plugins {
    `java-library`
    kotlin("jvm")
}

fun version(name: String) = rootProject.file("versions/$name.txt").readLines().first()
val kotlinVersion = version("kotlin")
val coroutinesVersion = version("kotlinx-coroutines")
val serializationVersion = version("kotlinx-serialization")
val dateTimeVersion = version("kotlinx-datetime")
val ktorVersion = version("ktor")
val okioVersion = version("okio")
val okhttpVersion = version("okhttp")

val databaseApi = configurations.create("databaseApi")

dependencies {
    // Kotlin & Kotlinx
    implementation("org.jetbrains.kotlin:kotlin-stdlib:${kotlinVersion}")
    implementation("org.jetbrains.kotlin:kotlin-reflect:${kotlinVersion}")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${coroutinesVersion}")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:${serializationVersion}")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:${dateTimeVersion}")
//    implementation("org.jetbrains.kotlinx:kotlinx-io-core:${kotlinIoVersion}")
//    implementation("org.jetbrains.kotlinx:atomicfu:${atomicfuVersion}")

    // Ktor
    implementation("io.ktor:ktor-server-core:${ktorVersion}")
    implementation("io.ktor:ktor-server-netty:${ktorVersion}")
    implementation("io.ktor:ktor-server-host-common:${ktorVersion}")

    // Okio
    implementation("com.squareup.okhttp3:okhttp:${okhttpVersion}")
    implementation("com.squareup.okio:okio:${okioVersion}")

    // Other
    implementation("plutoproject.adventurekt:core:v3.0.0") {
        isTransitive = false
    }
    implementation("org.apache.groovy:groovy:5.0.7")

    // Shared configuration, reflection and Redis dependencies; versions match the copied modules.
    val sharedLibraries = listOf(
        "com.electronwill.night-config:core:3.6.7",
        "com.electronwill.night-config:toml:3.6.7",
        "com.electronwill.night-config:json:3.6.7",
        "com.electronwill.night-config:hocon:3.6.7",
        "com.typesafe:config:1.4.3",
        "org.yaml:snakeyaml:2.6",
        "redis.clients:jedis:4.2.3",
        "org.apache.commons:commons-pool2:2.11.1",
        "org.json:json:20211205",
        "org.apache.commons:commons-lang3:3.20.0",
        "org.ow2.asm:asm:9.10.1"
    )
    sharedLibraries.forEach {
        implementation(it) { isTransitive = false }
        databaseApi(it) { isTransitive = false }
    }
    compileOnly("com.google.guava:guava:33.5.0-jre")
    compileOnly("com.google.code.gson:gson:2.11.0")

    // Shared database runtime. Keep Hikari/MySQL aligned with the copied TabooLib module.
    implementation("com.zaxxer:HikariCP:4.0.3") { isTransitive = false }
    databaseApi("com.zaxxer:HikariCP:4.0.3") { isTransitive = false }
    implementation("com.mysql:mysql-connector-j:8.4.0") { isTransitive = false }
    implementation("org.xerial:sqlite-jdbc:3.53.0.0") { isTransitive = false }
    implementation("org.postgresql:postgresql:42.7.10") { isTransitive = false }
    compileOnly("org.slf4j:slf4j-api:2.0.18")

    compileOnly("net.kyori:adventure-api:5.2.0")
    compileOnly("net.kyori:adventure-text-minimessage:5.2.0")
    compileOnly("net.kyori:adventure-text-serializer-legacy:5.2.0")
    compileOnly("net.kyori:adventure-text-serializer-plain:5.2.0")
    compileOnly("net.kyori:adventure-text-serializer-gson:5.2.0")
}

