package com.illenko.service

import com.illenko.api.request.OrderRequest
import com.illenko.client.payment.PaymentClient
import com.illenko.client.payment.enum.PaymentStatus
import com.illenko.client.payment.request.PaymentRequest
import com.illenko.client.payment.response.PaymentResponse
import com.illenko.core.BaseIntegrationTest
import com.illenko.enum.OrderStatus
import com.illenko.model.Order
import com.ninjasquad.springmockk.MockkBean
import io.mockk.Ordering
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import org.junit.jupiter.params.provider.ArgumentsSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.aot.DisabledInAotMode
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import java.util.stream.Stream

@DisabledInAotMode
class OrderServiceIntegrationTest : BaseIntegrationTest() {
    @MockkBean
    private lateinit var paymentClient: PaymentClient

    @Autowired
    private lateinit var sut: OrderService

    @ParameterizedTest
    @ArgumentsSource(DataProvider::class)
    fun `process order`(
        orderStatus: OrderStatus,
        paymentStatus: PaymentStatus?,
    ) {
        val orderRequest = random<OrderRequest>()

        val paymentResponseValue =
            paymentStatus?.let {
                PaymentResponse(
                    id = random(),
                    status = it,
                )
            }

        val paymentResponse = paymentResponseValue?.toMono() ?: Mono.error(RuntimeException())
        every { paymentClient.pay(any()) } returns paymentResponse

        val actual = sut.process(orderRequest).block()!!

        assertNotNull(actual.id)
        assertEquals(orderStatus, actual.status)

        val savedOrder = orderRepository.findById(actual.id).block()!!

        val expectedSavedOrder =
            Order(
                id = savedOrder.id,
                itemId = orderRequest.itemId,
                userId = orderRequest.userId,
                tokenId = orderRequest.tokenId,
                price = orderRequest.price,
                status = orderStatus,
                paymentId = paymentResponseValue?.id,
            )

        assertEquals(expectedSavedOrder, savedOrder)

        verify(atLeast = 1, ordering = Ordering.ORDERED) {
            paymentClient.pay(
                PaymentRequest(
                    tokenId = orderRequest.tokenId,
                    amount = orderRequest.price,
                ),
            )
        }
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
