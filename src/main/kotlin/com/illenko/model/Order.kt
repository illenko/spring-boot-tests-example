package com.illenko.model

import com.illenko.enum.OrderStatus
import java.math.BigDecimal
import java.util.UUID

data class Order(
    var id: UUID? = null,
    val userId: UUID,
    val tokenId: UUID,
    val itemId: UUID,
    val price: BigDecimal,
    var status: OrderStatus,
    var paymentId: UUID? = null,
)
