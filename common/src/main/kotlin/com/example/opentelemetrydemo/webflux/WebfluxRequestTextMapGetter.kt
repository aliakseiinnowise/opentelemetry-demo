package com.example.opentelemetrydemo.webflux

import io.opentelemetry.context.propagation.TextMapGetter
import org.springframework.http.server.reactive.ServerHttpRequest

object WebfluxRequestTextMapGetter : TextMapGetter<ServerHttpRequest> {

    override fun keys(carrier: ServerHttpRequest) = carrier.headers.keys

    override fun get(carrier: ServerHttpRequest?, key: String) = carrier?.headers?.getFirst(key)
}
