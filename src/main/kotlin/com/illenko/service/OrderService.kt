package com.illenko.service

import com.illenko.api.request.OrderRequest
import com.illenko.api.response.OrderResponse
import com.illenko.client.payment.PaymentClient
import com.illenko.client.payment.enum.PaymentStatus
import com.illenko.client.token.TokenClient
import com.illenko.enum.OrderStatus
import com.illenko.mapper.OrderMapper
import com.illenko.mapper.PaymentMapper
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
        log.info { "Processing order request: $request" }

        return orderRepository.save(orderMapper.toEntity(request))
            .zipWith(tokenClient.getToken(request.tokenId))
            .flatMap {
                paymentClient.pay(paymentMapper.toPaymentRequest(it.t1, it.t2.token))
                    .flatMap { paymentResult ->
                        orderRepository.save(it.t1.apply {
                            this.status = when (paymentResult.status) {
                                PaymentStatus.SUCCESS -> OrderStatus.PAID
                                PaymentStatus.FAILED -> OrderStatus.CANCELED
                            }
                        })
                    }
            }
            .map { orderMapper.toResponse(it) }
    }
}
