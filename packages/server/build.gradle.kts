import java.time.Clock

plugins {
    kotlin("jvm") version "1.7.20-Beta"
    id("com.github.johnrengelman.shadow") version "7.1.2"
    kotlin("plugin.serialization") version org.gradle.kotlin.dsl.embeddedKotlinVersion
    id("com.google.cloud.tools.jib") version "3.2.0"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
    jcenter()
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.0")
    implementation(kotlin("stdlib"))
    implementation("com.github.Kosert.FlowBus:FlowBus:1.1")
    implementation("io.javalin:javalin:4.6.4")
    implementation("io.javalin:javalin-openapi:4.6.4")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.13.3")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.13.3")
    implementation("ch.qos.logback:logback-classic:1.2.11")
    implementation("org.mongodb:mongo-java-driver:3.12.11")
    implementation("org.litote.kmongo:kmongo:4.6.1")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.13.3")
//    implementation("org.imgscalr:imgscalr-lib:4.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    implementation("com.sksamuel.scrimage:scrimage-core:4.0.30")
    implementation("com.sksamuel.scrimage:scrimage-filters:4.0.30")
    implementation("com.github.ajalt.mordant:mordant:2.0.0-beta4")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
    implementation("io.javalin:javalin-bundle:4.6.4")
    implementation("com.github.ajalt.mordant:mordant:2.0.0-beta5")
    testImplementation("org.mockito:mockito-core:4.6.1")
    testImplementation("io.mockk:mockk:1.12.5")
    testImplementation("org.assertj:assertj-core:3.23.1")
    implementation("org.sejda.imageio:webp-imageio:0.1.6")
    implementation("com.datastax.oss:java-driver-core:4.14.1")
    implementation("com.datastax.oss:java-driver-query-builder:4.14.1")
    implementation("com.datastax.oss:java-driver-mapper-processor:4.14.1")
    implementation("com.datastax.oss:java-driver-mapper-runtime:4.14.1")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.suppressWarnings = true
    kotlinOptions.jvmTarget = JavaVersion.VERSION_17.toString()
}
tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    archiveFileName.set("zenspaceapi.jar")
}
tasks.test {
    useJUnitPlatform()
}
jib {
    from {
        image = "ghcr.io/graalvm/graalvm-ce:ol8-java17"
    }
    container {
        mainClass = "rebase.ServerKt"
        creationTime = Clock.systemUTC().instant().toString()
    }
}
val compileKotlin: org.jetbrains.kotlin.gradle.tasks.KotlinCompile by tasks

