import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val ktor_version: String by project
val kotlin_version: String by project
val logback_version: String by project

val exposed_version: String by project
val h2_version: String by project
val prometeus_version: String by project
plugins {
    kotlin("jvm") version "1.9.10"
    id("io.ktor.plugin") version "2.3.5"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.10"
}

group = "com.example"
version = "0.0.1"

repositories {
    mavenCentral()
}

val opentelemetry by configurations.creating

dependencies {
    implementation("io.ktor:ktor-server-core-jvm")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm")
    implementation("io.ktor:ktor-server-content-negotiation-jvm")
    implementation("org.jetbrains.exposed:exposed-core:$exposed_version")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposed_version")
    implementation("com.h2database:h2:$h2_version")
    implementation("io.ktor:ktor-server-metrics-micrometer-jvm")
    implementation("io.micrometer:micrometer-registry-prometheus:$prometeus_version")
    implementation("io.ktor:ktor-server-metrics-jvm")
    implementation("io.ktor:ktor-server-call-logging-jvm")
    implementation("io.ktor:ktor-server-host-common-jvm")
    implementation("io.ktor:ktor-server-status-pages-jvm")
    implementation("io.ktor:ktor-server-netty-jvm")
    implementation("ch.qos.logback:logback-classic:$logback_version")
    implementation("io.ktor:ktor-server-config-yaml:2.3.5")
    testImplementation("io.ktor:ktor-server-tests-jvm")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")
    // ---
    // Telemetry
    implementation("io.opentelemetry.instrumentation:opentelemetry-ktor-2.0:1.30.0-alpha")
    opentelemetry("io.opentelemetry.javaagent:opentelemetry-javaagent:1.20.2")
    // ---
    // Ktor-client
    implementation("io.ktor:ktor-client-core:$ktor_version")
    implementation("io.ktor:ktor-client-cio:$ktor_version")
    implementation("io.ktor:ktor-client-logging:$ktor_version")
}

application {
    mainClass.set("io.ktor.server.netty.EngineMain")
    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf(
        "-Dio.ktor.development=$isDevelopment",
        "-javaagent:$buildDir/otel/otel-javaagent.jar",
        "-Dotel.service.name=ktor-opentelemetry",
        "-Dotel.exporter.otlp.endpoint=http://localhost:4317",
        "-Dotel.logs.exporter=otlp"
    )
}

val otelAgent = tasks.register<Copy>("otel-agent") {
    from(opentelemetry) {
        rename { "otel-javaagent.jar" }
    }
    into(file("$buildDir/otel/"))
}

tasks {
    buildFatJar {
        dependsOn(otelAgent)
    }
}

tasks {
    getByName("run").dependsOn(otelAgent)
}