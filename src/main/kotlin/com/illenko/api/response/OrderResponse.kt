package com.illenko.api.response

import com.illenko.client.payment.enum.PaymentStatus
import java.util.UUID

data class OrderResponse(
    val id: UUID,
    val status: PaymentStatus,
)