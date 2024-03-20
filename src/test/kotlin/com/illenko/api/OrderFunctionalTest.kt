package com.illenko.api

import com.illenko.api.request.OrderRequest
import com.illenko.api.response.OrderResponse
import com.illenko.client.payment.enum.PaymentStatus
import com.illenko.client.payment.request.PaymentRequest
import com.illenko.client.payment.response.PaymentResponse
import com.illenko.core.BaseFunctionalTest
import com.illenko.enum.OrderStatus
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import org.junit.jupiter.params.provider.ArgumentsSource
import org.springframework.http.HttpStatus
import java.util.stream.Stream

class OrderFunctionalTest : BaseFunctionalTest() {
    @ParameterizedTest
    @ArgumentsSource(DataProvider::class)
    fun `orders endpoint test`(
        orderStatus: OrderStatus,
        paymentStatus: PaymentStatus?,
    ) {
        val request = random<OrderRequest>()

        if (paymentStatus == null) {
            mockPost(
                uri = "/api/v1/payments",
                requestBody = PaymentRequest(tokenId = request.tokenId, amount = request.price),
                requestHeaders = mapOf("API-KEY" to "secret-api-key"),
                responseStatus = HttpStatus.BAD_REQUEST,
            )
        } else {
            mockPost(
                uri = "/api/v1/payments",
                requestBody = PaymentRequest(tokenId = request.tokenId, amount = request.price),
                requestHeaders = mapOf("API-KEY" to "secret-api-key"),
                responseBody = PaymentResponse(id = random(), status = paymentStatus),
            )
        }

        val actual = doPost<OrderResponse>(uri = "/orders", body = request).responseBody.blockFirst()!!

        assertNotNull(actual.id)
        assertEquals(orderStatus, actual.status)
    }

    class DataProvider : ArgumentsProvider {
        override fun provideArguments(context: ExtensionContext): Stream<Arguments> =
            Stream.of(
                Arguments.of(OrderStatus.PAID, PaymentStatus.SUCCESS),
                Arguments.of(OrderStatus.PAYMENT_REJECTED, PaymentStatus.REJECTED),
                Arguments.of(OrderStatus.PAYMENT_FAILED, null),
            )
    }
}
