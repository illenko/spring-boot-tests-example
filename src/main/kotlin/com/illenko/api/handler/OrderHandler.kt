package com.illenko.api.handler

import com.illenko.api.request.OrderRequest
import com.illenko.api.response.OrderResponse
import com.illenko.service.OrderService
import mu.KotlinLogging
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.bodyToMono
import reactor.core.publisher.Mono

@Component
@RegisterReflectionForBinding(
    OrderRequest::class,
    OrderResponse::class,
)
class OrderHandler(
    private val service: OrderService,
) {
    private val log = KotlinLogging.logger {}

    fun process(request: ServerRequest): Mono<ServerResponse> =
        request.bodyToMono<OrderRequest>()
            .doOnNext { log.info { "Processing order request: $it" } }
            .flatMap { service.process(it) }
            .doOnNext { log.info { "Processing order result: $it" } }
            .flatMap { ServerResponse.ok().bodyValue(it) }
}
