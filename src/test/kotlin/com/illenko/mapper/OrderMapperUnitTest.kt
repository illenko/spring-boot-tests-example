package com.illenko.mapper

import com.illenko.api.request.OrderRequest
import com.illenko.api.response.OrderResponse
import com.illenko.client.payment.request.PaymentRequest
import com.illenko.core.BaseUnitTest
import com.illenko.enum.OrderStatus
import com.illenko.model.Order
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class OrderMapperUnitTest : BaseUnitTest() {
    private val sut = OrderMapper()

    @Test
    fun `to entity`() {
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
                orderId = order.id!!,
                tokenId = order.tokenId,
                amount = order.price,
            )

        val actual = sut.toPaymentRequest(order)

        assertEquals(expected, actual)
    }
}
