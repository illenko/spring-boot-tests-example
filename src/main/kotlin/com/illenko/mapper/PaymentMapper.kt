package com.illenko.mapper

import com.illenko.api.request.OrderRequest
import com.illenko.client.payment.request.PaymentRequest
import com.illenko.enum.OrderStatus
import com.illenko.model.Order
import org.springframework.stereotype.Component

@Component
class PaymentMapper {

    fun toEntity(request: OrderRequest): Order =
        Order(
            userId = request.userId,
            tokenId = request.tokenId,
            price = request.price,
            itemId = request.itemId,
            status = OrderStatus.CREATED,
        )

    fun toPaymentRequest(order: Order, token: String): PaymentRequest =
        PaymentRequest(
            orderId = order.id!!,
            token = token,
            amount = order.price,
        )
}