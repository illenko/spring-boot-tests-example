package com.illenko.model

import com.illenko.enum.OrderStatus
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal
import java.util.UUID

@Table("order_record")
data class Order(
    @Id
    var id: UUID? = null,
    val userId: UUID,
    val tokenId: UUID,
    val itemId: UUID,
    val price: BigDecimal,
    var status: OrderStatus,
    var paymentId: UUID? = null,
)
