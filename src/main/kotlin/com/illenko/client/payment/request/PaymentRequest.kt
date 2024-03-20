package com.illenko.client.payment.request

import java.math.BigDecimal
import java.util.UUID

data class PaymentRequest(
    val tokenId: UUID,
    val amount: BigDecimal,
)
