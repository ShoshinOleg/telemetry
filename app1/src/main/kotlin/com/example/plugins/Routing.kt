package com.example.plugins

import com.example.plugins.telemetry.FakeHttpClient
import com.example.plugins.telemetry.trace
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.api.common.AttributeKey
import io.opentelemetry.api.common.Attributes
import io.opentelemetry.api.trace.StatusCode
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import java.util.concurrent.Executors
import kotlin.random.Random

private val logger = LoggerFactory.getLogger(FakeHttpClient::class.java)
private val singleThreadDispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()

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
        get("/sample") {
            val tracer = otel.getTracer(FakeHttpClient::class.java.name)
            val meter = otel.getMeter(FakeHttpClient::class.java.name)
            val doWorkHistogram = meter.histogramBuilder("do-work").ofLongs().build()
            val random = Random(42)

            val client = HttpClient(CIO) {
                install(Logging) {
                    level = LogLevel.INFO
                }
            }

            tracer.trace("sample span") {
                it.addEvent("starting sample work")
                val delayTime = random.nextLong(1_000)
                withContext(singleThreadDispatcher) {
                    logger.info("dispatched to ${Thread.currentThread().name}")
                    tracer.trace("call app2") { app2Span ->
                        delay(delayTime)
                        val response = client.get("http://app2:8080/")
                        it.addEvent("app2 response = $response")
//                        client.request("https://ktor.io/")
                        app2Span.setStatus(StatusCode.OK)
                        call.respondText("appResp=$response")
                    }
                    it.addEvent("completed delay")
                }
                it.addEvent("sample work completed")
                doWorkHistogram.record(delayTime, Attributes.of(AttributeKey.stringKey("method"), "ping"))
            }
        }
    }
}