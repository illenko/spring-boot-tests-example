package com.illenko.mapper

import com.illenko.api.request.OrderRequest
import com.illenko.api.response.OrderResponse
import com.illenko.client.payment.enum.PaymentStatus
import com.illenko.client.payment.request.PaymentRequest
import com.illenko.client.payment.response.PaymentResponse
import com.illenko.core.BaseUnitTest
import com.illenko.enum.OrderStatus
import com.illenko.model.Order
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class OrderMapperUnitTest : BaseUnitTest() {
    private val sut = OrderMapper()

    @Test
    fun `to entity new`() {
        val request = random<OrderRequest>()

        val expected =
            Order(
                userId = request.userId,
                tokenId = request.tokenId,
                itemId = request.itemId,
                price = request.price,
                status = OrderStatus.CREATED,
            )

        val actual = sut.toEntity(request)

        assertEquals(expected, actual)
    }

    @Test
    fun `to entity with payment response`() {
        val order = random<Order>().apply { id = random() }
        val paymentResponse = PaymentResponse(id = random(), status = PaymentStatus.SUCCESS)

        val expected =
            Order(
                id = order.id,
                userId = order.userId,
                tokenId = order.tokenId,
                itemId = order.itemId,
                price = order.price,
                status = OrderStatus.PAID,
                paymentId = paymentResponse.id,
            )

        val actual = sut.toEntity(order, paymentResponse)

        assertEquals(expected, actual)
    }

    @Test
    fun `to entity with failed payment`() {
        val order = random<Order>().apply { id = random() }

        val expected =
            Order(
                id = order.id,
                userId = order.userId,
                tokenId = order.tokenId,
                itemId = order.itemId,
                price = order.price,
                status = OrderStatus.PAYMENT_FAILED,
            )

        val actual = sut.toEntityWithFailedPayment(order)

        assertEquals(expected, actual)
    }

    @Test
    fun `to response`() {
        val order = random<Order>().apply { id = random() }

        val expected =
            OrderResponse(
                id = order.id!!,
                status = order.status,
            )

        val actual = sut.toResponse(order)

        assertEquals(expected, actual)
    }

    @Test
    fun `to payment request`() {
        val order = random<Order>().apply { id = random() }

        val expected =
            PaymentRequest(
                tokenId = order.tokenId,
                amount = order.price,
            )

        val actual = sut.toPaymentRequest(order)

        assertEquals(expected, actual)
    }
}
