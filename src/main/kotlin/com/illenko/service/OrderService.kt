package com.illenko.service

import com.illenko.api.request.OrderRequest
import com.illenko.api.response.OrderResponse
import com.illenko.client.payment.PaymentClient
import com.illenko.client.token.TokenClient
import com.illenko.repository.OrderRepository
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class OrderService(
    private val tokenClient: TokenClient,
    private val paymentClient: PaymentClient,
    private val orderRepository: OrderRepository,
) {

    fun process(orderRequest: OrderRequest): Mono<OrderResponse> {
        TODO("implement")
    }

}