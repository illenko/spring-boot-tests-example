package com.illenko.service

import com.illenko.api.request.OrderRequest
import com.illenko.api.response.OrderResponse
import com.illenko.client.payment.PaymentClient
import com.illenko.enum.OrderStatus
import com.illenko.mapper.OrderMapper
import com.illenko.repository.OrderRepository
import mu.KotlinLogging
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

@Service
class OrderService(
    private val paymentClient: PaymentClient,
    private val orderMapper: OrderMapper,
    private val orderRepository: OrderRepository,
) {
    private val log = KotlinLogging.logger {}

    fun process(request: OrderRequest): Mono<OrderResponse> {
        val entity = orderMapper.toEntity(request)
        return orderRepository.save(entity)
            .doOnNext { log.info { "Saved new order: $it" } }
            .flatMap { order ->
                val paymentRequest = orderMapper.toPaymentRequest(order)
                log.info { "Prepared payment request: $paymentRequest" }
                paymentClient.pay(paymentRequest)
                    .doOnNext { log.info { "Retrieved payment response: $it" } }
                    .map { orderMapper.toEntity(order, it) }
                    .onErrorResume { orderMapper.toEntity(order, OrderStatus.PAYMENT_FAILED).toMono() }
                    .doOnNext { log.info { "Prepared order entity to update: $it" } }
                    .flatMap { orderRepository.save(it) }
                    .doOnNext { log.info { "Updated order: $it" } }
                    .map { orderMapper.toResponse(it) }
                    .doOnNext { log.info { "Mapped order to response: $it" } }
            }
    }
}
