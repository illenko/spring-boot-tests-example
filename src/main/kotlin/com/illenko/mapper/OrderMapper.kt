package com.illenko.mapper

import com.illenko.api.request.OrderRequest
import com.illenko.api.response.OrderResponse
import com.illenko.client.payment.enum.PaymentStatus
import com.illenko.client.payment.request.PaymentRequest
import com.illenko.client.payment.response.PaymentResponse
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

    fun toEntity(
        order: Order,
        paymentResponse: PaymentResponse,
    ): Order =
        Order(
            id = order.id,
            userId = order.userId,
            tokenId = order.tokenId,
            itemId = order.itemId,
            price = order.price,
            status =
                when (paymentResponse.status) {
                    PaymentStatus.FAILED -> OrderStatus.PAYMENT_FAILED
                    PaymentStatus.SUCCESS -> OrderStatus.PAID
                },
            paymentId = paymentResponse.id,
        )

    fun toEntity(
        order: Order,
        status: OrderStatus,
    ): Order =
        Order(
            id = order.id,
            userId = order.userId,
            tokenId = order.tokenId,
            itemId = order.itemId,
            price = order.price,
            status = OrderStatus.PAYMENT_FAILED,
        )

    fun toPaymentRequest(order: Order): PaymentRequest =
        PaymentRequest(
            orderId = order.id!!,
            tokenId = order.tokenId,
            amount = order.price,
        )
}
