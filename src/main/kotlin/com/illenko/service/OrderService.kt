package com.illenko.service

import com.illenko.api.request.OrderRequest
import com.illenko.api.response.OrderResponse
import com.illenko.client.payment.PaymentClient
import com.illenko.client.token.TokenClient
import com.illenko.enum.OrderStatus
import com.illenko.mapper.PaymentMapper
import com.illenko.repository.OrderRepository
import mu.KotlinLogging
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

@Service
class OrderService(
    private val tokenClient: TokenClient,
    private val paymentClient: PaymentClient,
    private val paymentMapper: PaymentMapper,
    private val orderRepository: OrderRepository,
) {

    private val log = KotlinLogging.logger {}

    fun process(request: OrderRequest): Mono<OrderResponse> {
        log.info { "Processing order request: $request" }

        return request.toMono()
            .map { paymentMapper.toEntity(it) }
            .flatMap { orderRepository.save(it) }
            .map {
                OrderResponse(
                    id = it.id!!,
                    status = OrderStatus.PAID,
                )
            }
    }
}
