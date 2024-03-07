package com.illenko.service

import com.illenko.api.request.OrderRequest
import com.illenko.api.response.OrderResponse
import com.illenko.client.payment.PaymentClient
import com.illenko.client.payment.enum.PaymentStatus
import com.illenko.client.payment.request.PaymentRequest
import com.illenko.client.payment.response.PaymentResponse
import com.illenko.client.token.TokenClient
import com.illenko.client.token.response.TokenResponse
import com.illenko.mapper.OrderMapper
import com.illenko.mapper.PaymentMapper
import com.illenko.model.Order
import com.illenko.repository.OrderRepository
import core.BaseUnitTest
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import reactor.kotlin.core.publisher.toMono
import java.util.UUID

class OrderServiceUnitTest : BaseUnitTest() {
    private val tokenClient = mockk<TokenClient>()
    private val paymentClient = mockk<PaymentClient>()
    private val paymentMapper = mockk<PaymentMapper>()
    private val orderRepository = mockk<OrderRepository>()
    private val orderMapper = mockk<OrderMapper>()

    private val sut =
        OrderService(
            tokenClient = tokenClient,
            paymentClient = paymentClient,
            paymentMapper = paymentMapper,
            orderRepository = orderRepository,
            orderMapper = orderMapper,
        )

    @Test
    fun `processing order`() {
        val request = random<OrderRequest>()
        val newOrder = random<Order>().apply { id = null }
        val order = newOrder.apply { id = random<UUID>() }
        val tokenResponse = random<TokenResponse>()
        val paymentRequest =
            PaymentRequest(
                orderId = order.id!!,
                token = tokenResponse.token,
                amount = request.price,
            )
        val paymentResponse =
            PaymentResponse(
                id = random<UUID>(),
                status = PaymentStatus.SUCCESS,
            )

        val expected = random<OrderResponse>()

        every { orderMapper.toEntity(any()) } returns order
        every { orderRepository.save(any()) } returns order.toMono()
        every { tokenClient.getToken(any()) } returns tokenResponse.toMono()
        every { paymentMapper.toPaymentRequest(any(), any()) } returns paymentRequest
        every { paymentClient.pay(any()) } returns paymentResponse.toMono()
        every { orderMapper.toResponse(any()) } returns expected

        val actual = sut.process(request).block()

        verify {
            orderMapper.toEntity(request)
            orderRepository.save(newOrder)
            orderRepository.save(order)
            tokenClient.getToken(order.tokenId)
            paymentMapper.toPaymentRequest(order, tokenResponse.token)
            paymentClient.pay(paymentRequest)
            orderMapper.toResponse(order)
        }

        assertEquals(expected, actual)
    }
}
