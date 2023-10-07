package com.example.plugins

import com.example.plugins.telemetry.FakeHttpClient
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.opentelemetry.api.OpenTelemetry

fun Application.configureRouting(otel: OpenTelemetry) {
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.respondText(text = "500: $cause" , status = HttpStatusCode.InternalServerError)
        }
    }
    routing {
        get("/") {
            val client = FakeHttpClient(otel)
            client.callClient()
            call.respondText("Hello World!")
        }
    }
}
