package com.illenko.mapper

import com.illenko.client.payment.request.PaymentRequest
import com.illenko.core.BaseUnitTest
import com.illenko.model.Order
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class PaymentMapperUnitTest : BaseUnitTest() {
    private val sut = PaymentMapper()

    @Test
    fun `to payment request conversion`() {
        val order = random<Order>().apply { id = random() }
        val token = random<String>()

        val expected =
            PaymentRequest(
                orderId = order.id!!,
                token = token,
                amount = order.price,
            )

        val actual = sut.toPaymentRequest(order, token)

        assertEquals(expected, actual)
    }
}
