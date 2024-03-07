package com.illenko.client.payment

import com.illenko.client.payment.request.PaymentRequest
import com.illenko.client.payment.response.PaymentResponse
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

@Component
class PaymentClient(
    @Qualifier("paymentWebClient")
    private val client: WebClient,
) {
    private val log = KotlinLogging.logger {}

    fun pay(request: PaymentRequest): Mono<PaymentResponse> {
        log.info { "Sending payment request: $request" }
        return client.post()
            .uri("/api/v1/payments")
            .bodyValue(request)
            .retrieve()
            .bodyToMono(PaymentResponse::class.java)
            .doOnNext { log.info { "Retrieved payment response: $it" } }
    }
}
