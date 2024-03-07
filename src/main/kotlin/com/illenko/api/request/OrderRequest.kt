package com.illenko.api.request

import java.math.BigDecimal
import java.util.UUID

data class OrderRequest(
    val userId: UUID,
    val tokenId: UUID,
    val itemId: UUID,
    val price: BigDecimal,
)
