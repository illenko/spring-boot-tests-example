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
        paymentResponse: PaymentResponse?,
        paymentResponseStatus: HttpStatus,
    ) {
        val request = random<OrderRequest>()

        mockPaymentEndpoint(request, paymentResponse, paymentResponseStatus)

        val actual = doPost<OrderResponse>(uri = "/orders", body = request.toJson()).responseBody.blockFirst()!!
        assertNotNull(actual.id)
        assertEquals(orderStatus, actual.status)
    }

    private fun mockPaymentEndpoint(
        request: OrderRequest,
        paymentResponse: PaymentResponse?,
        paymentResponseStatus: HttpStatus,
    ) {
        mockPost(
            uri = "/api/v1/payments",
            requestBody = PaymentRequest(tokenId = request.tokenId, amount = request.price).toJson(),
            requestHeaders = mapOf("API-KEY" to "secret-api-key"),
            responseBody = paymentResponse?.toJson() ?: "{}",
            responseStatus = paymentResponseStatus,
        )
    }

    class DataProvider : ArgumentsProvider {
        override fun provideArguments(context: ExtensionContext): Stream<Arguments> =
            Stream.of(
                Arguments.of(
                    OrderStatus.PAID,
                    PaymentResponse(id = random(), status = PaymentStatus.SUCCESS),
                    HttpStatus.OK,
                ),
                Arguments.of(
                    OrderStatus.PAYMENT_REJECTED,
                    PaymentResponse(id = random(), status = PaymentStatus.REJECTED),
                    HttpStatus.OK,
                ),
                Arguments.of(OrderStatus.PAYMENT_FAILED, null, HttpStatus.BAD_REQUEST),
            )
    }
}
