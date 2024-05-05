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
            status = orderStatus(paymentResponse),
            paymentId = paymentResponse.id,
        )

    fun toEntityWithFailedPayment(order: Order): Order =
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
            tokenId = order.tokenId,
            amount = order.price,
        )

    fun toResponse(entity: Order): OrderResponse =
        OrderResponse(
            id = entity.id!!,
            status = entity.status,
        )

    private fun orderStatus(paymentResponse: PaymentResponse) =
        when (paymentResponse.status) {
            PaymentStatus.REJECTED -> OrderStatus.PAYMENT_REJECTED
            PaymentStatus.SUCCESS -> OrderStatus.PAID
        }
}
