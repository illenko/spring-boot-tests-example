package com.illenko.client.payment.request

import java.math.BigDecimal
import java.util.UUID

data class PaymentRequest(
    val orderId: UUID,
    val token: String,
    val amount: BigDecimal,
)
