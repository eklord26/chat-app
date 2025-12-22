plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktor)
    alias(libs.plugins.kotlin.plugin.serialization)
}

group = "com.example"
version = "0.0.1"
val ktorVersion = "2.3.7"

application {
    mainClass = "io.ktor.server.netty.EngineMain"
    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories {
    mavenCentral()
}

dependencies {
    // Ktor
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.websockets)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.server.call.logging)
    implementation(libs.ktor.server.host.common)
    implementation(libs.ktor.server.netty)
    implementation(libs.logback.classic)
    implementation("at.favre.lib:bcrypt:0.10.2")
    implementation(libs.ktor.server.config.yaml)

    // Database
    implementation(libs.postgresql)
    implementation(libs.h2)

    // Exposed (полный набор необходимых зависимостей)
    implementation(libs.exposed.core)
    implementation(libs.exposed.dao)
    implementation(libs.exposed.jdbc)
    implementation(libs.exposed.java.time)

    // Kotlin
    implementation(kotlin("stdlib-jdk8"))

    // Testing
    testImplementation(libs.ktor.server.test.host)
    testImplementation(libs.kotlin.test.junit)

    // Swagger
    implementation(libs.ktor.swagger.ui)
    implementation(libs.ktor.swagger.openapi)
}