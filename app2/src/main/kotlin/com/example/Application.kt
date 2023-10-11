package com.example

import com.example.plugins.*
import com.example.plugins.telemetry.configureTelemetry
import io.ktor.server.application.*
import io.opentelemetry.api.GlobalOpenTelemetry

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    configureSerialization()
    val otel = GlobalOpenTelemetry.get()
    configureTelemetry(otel)
//    configureDatabases()
//    configureMonitoring()
    configureRouting(otel)
}
