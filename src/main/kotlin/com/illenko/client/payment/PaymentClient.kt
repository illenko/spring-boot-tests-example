package com.illenko.client.payment

import com.illenko.client.payment.request.PaymentRequest
import com.illenko.client.payment.response.PaymentResponse
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class PaymentClient {

    fun pay(request: PaymentRequest): Mono<PaymentResponse> {
        TODO("implement")
    }

}