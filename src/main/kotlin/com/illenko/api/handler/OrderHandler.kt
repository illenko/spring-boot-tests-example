package com.illenko.api.handler

import com.illenko.service.OrderService
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono

@Component
class OrderHandler(
    private val orderService: OrderService,
) {

    fun process(request: ServerRequest): Mono<ServerResponse> {
        TODO("implement")
    }
}