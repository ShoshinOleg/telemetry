package com.example.plugins.telemetry

import io.ktor.server.application.*
import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.instrumentation.ktor.v2_0.server.KtorServerTracing

fun Application.configureTelemetry(otel: OpenTelemetry) {
    install(KtorServerTracing) {
        setOpenTelemetry(otel)
    }
}