package com.illenko.mapper

import com.illenko.client.payment.request.PaymentRequest
import com.illenko.model.Order
import org.springframework.stereotype.Component

@Component
class PaymentMapper {
    fun toPaymentRequest(
        order: Order,
        token: String,
    ): PaymentRequest =
        PaymentRequest(
            orderId = order.id!!,
            token = token,
            amount = order.price,
        )
}
