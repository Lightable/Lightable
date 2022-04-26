val exposedVersion: String by project
plugins {
    kotlin("jvm") version "1.6.10"
    id("com.github.johnrengelman.shadow") version "7.1.2"
    kotlin("plugin.serialization") version org.gradle.kotlin.dsl.embeddedKotlinVersion
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
    jcenter()
}

dependencies {
    implementation("org.junit.jupiter:junit-jupiter:5.7.0")
    val kotlinVersion = "1.6.10"
    implementation(kotlin("stdlib"))
    implementation("com.github.Kosert.FlowBus:FlowBus:1.1")
    implementation("io.javalin:javalin:4.4.0")
    implementation("io.javalin:javalin-openapi:4.4.0")
    implementation("com.google.code.gson:gson:2.9.0")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.13.2.2")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.13.2")
    implementation("ch.qos.logback:logback-classic:1.2.11")
    implementation("org.json:json:20210307")
    implementation("org.mongodb:mongo-java-driver:3.12.10")
    implementation("org.litote.kmongo:kmongo:4.5.1")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.13.2")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.5.10")
    implementation("org.imgscalr:imgscalr-lib:4.2")
    implementation("io.prometheus:simpleclient_httpserver:0.15.0")
    implementation("io.prometheus:simpleclient:0.12.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.1-native-mt")
    implementation("com.sksamuel.scrimage:scrimage-core:4.0.30")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.2")
    implementation("com.github.ajalt.mordant:mordant:2.0.0-beta4")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
    compileOnly(kotlin("serialization", version = kotlinVersion))
    implementation("io.javalin:javalin-bundle:4.4.0")
    implementation("com.github.ajalt.mordant:mordant:2.0.0-beta5")
    testImplementation("org.mockito:mockito-core:4.5.1")
    testImplementation("io.mockk:mockk:1.12.3")
    implementation("org.sejda.imageio:webp-imageio:0.1.6")
}


tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.suppressWarnings = true
    kotlinOptions.jvmTarget = JavaVersion.VERSION_16.toString()
}
val jar by tasks.getting(Jar::class) {
    manifest {
        attributes["Main-Class"] = "rebase.ServerKt"
    }
}
tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    baseName = group
    classifier = "chattyapi"
    version = version
}
val compileKotlin: org.jetbrains.kotlin.gradle.tasks.KotlinCompile by tasks


