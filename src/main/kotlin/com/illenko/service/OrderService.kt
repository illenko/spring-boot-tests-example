package com.illenko.service

import com.illenko.api.request.OrderRequest
import com.illenko.api.response.OrderResponse
import com.illenko.client.payment.PaymentClient
import com.illenko.client.payment.enum.PaymentStatus
import com.illenko.client.token.TokenClient
import com.illenko.enum.OrderStatus
import com.illenko.mapper.OrderMapper
import com.illenko.mapper.PaymentMapper
import com.illenko.model.Order
import com.illenko.repository.OrderRepository
import mu.KotlinLogging
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class OrderService(
    private val tokenClient: TokenClient,
    private val paymentClient: PaymentClient,
    private val paymentMapper: PaymentMapper,
    private val orderMapper: OrderMapper,
    private val orderRepository: OrderRepository,
) {
    private val log = KotlinLogging.logger {}

    fun process(request: OrderRequest): Mono<OrderResponse> {
        return orderRepository.save(orderMapper.toEntity(request))
            .doOnNext { log.info { "Saved order: $it" } }
            .flatMap { order ->
                tokenClient.getToken(order.tokenId)
                    .doOnNext { log.info { "Retrieved token by id: ${order.tokenId}" } }
                    .map { paymentMapper.toPaymentRequest(order = order, token = it.token) }
                    .doOnNext { log.info { "Prepared payment request: $it" } }
                    .flatMap { paymentClient.pay(it) }
                    .doOnNext { log.info { "Retrieved payment response: $it" } }
                    .flatMap { updateOrderStatus(order, it.status) }
                    .doOnNext { log.info { "Update order: $it" } }
                    .doOnError { log.error { "Handled an error while processing order: $order, $it" } }
                    .onErrorResume { updateOrderStatus(order = order, status = PaymentStatus.FAILED) }
            }
            .map { orderMapper.toResponse(it) }
    }

    private fun updateOrderStatus(
        order: Order,
        status: PaymentStatus,
    ): Mono<Order> =
        orderRepository.save(
            order.apply {
                this.status =
                    when (status) {
                        PaymentStatus.SUCCESS -> OrderStatus.PAID
                        PaymentStatus.FAILED -> OrderStatus.CANCELED
                    }
            },
        )
}
