package com.illenko.api.response

import com.illenko.enum.OrderStatus
import java.util.*

data class OrderResponse(
    val id: UUID,
    val status: OrderStatus,
)