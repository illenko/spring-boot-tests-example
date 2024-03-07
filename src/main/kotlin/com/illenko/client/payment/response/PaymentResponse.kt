package com.illenko.client.payment.response

import com.illenko.client.payment.enum.PaymentStatus
import java.util.UUID

data class PaymentResponse(
    val id: UUID,
    val status: PaymentStatus,
)
