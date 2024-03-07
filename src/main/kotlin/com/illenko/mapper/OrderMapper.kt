package com.illenko.mapper

import com.illenko.api.request.OrderRequest
import com.illenko.api.response.OrderResponse
import com.illenko.enum.OrderStatus
import com.illenko.model.Order
import org.springframework.stereotype.Component

@Component
class OrderMapper {

    fun toEntity(request: OrderRequest): Order =
        Order(
            userId = request.userId,
            tokenId = request.tokenId,
            price = request.price,
            itemId = request.itemId,
            status = OrderStatus.CREATED,
        )

    fun toResponse(entity: Order): OrderResponse =
        OrderResponse(
            id = entity.id!!,
            status = entity.status,
        )
}
