package com.illenko.service

import com.illenko.api.request.OrderRequest
import com.illenko.api.response.OrderResponse
import com.illenko.client.payment.PaymentClient
import com.illenko.client.payment.enum.PaymentStatus
import com.illenko.client.payment.request.PaymentRequest
import com.illenko.client.payment.response.PaymentResponse
import com.illenko.core.BaseUnitTest
import com.illenko.enum.OrderStatus
import com.illenko.mapper.OrderMapper
import com.illenko.model.Order
import com.illenko.repository.OrderRepository
import io.mockk.Ordering
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import org.junit.jupiter.params.provider.ArgumentsSource
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import reactor.test.StepVerifier
import java.util.stream.Stream

class OrderServiceUnitTest : BaseUnitTest() {
    private val paymentClient = mockk<PaymentClient>()
    private val orderRepository = mockk<OrderRepository>()
    private val orderMapper = mockk<OrderMapper>()

    private val sut =
        OrderService(
            paymentClient = paymentClient,
            orderRepository = orderRepository,
            orderMapper = orderMapper,
        )

    @BeforeEach
    fun setup() {
        clearAllMocks()
    }

    @Suppress("LongMethod")
    @ParameterizedTest
    @ArgumentsSource(DataProvider::class)
    fun `processing order`(
        orderStatus: OrderStatus,
        paymentStatus: PaymentStatus?,
    ) {
        val request = random<OrderRequest>()

        val newOrder =
            random<Order>().apply {
                id = null
                status = OrderStatus.CREATED
            }

        val savedOrder =
            newOrder.apply {
                id = random()
            }

        val updatedOrder =
            newOrder.apply {
                id = random()
                status = orderStatus
            }

        val paymentRequest =
            PaymentRequest(
                tokenId = savedOrder.tokenId,
                amount = request.price,
            )

        val paymentResponseValue =
            paymentStatus?.let {
                PaymentResponse(
                    id = random(),
                    status = it,
                )
            }

        val paymentResponse = paymentResponseValue?.toMono() ?: Mono.error(RuntimeException())

        val expected =
            OrderResponse(
                id = updatedOrder.id!!,
                status = updatedOrder.status,
            )

        every { orderMapper.toEntity(any()) } returns newOrder
        every { orderRepository.save(newOrder) } returns savedOrder.toMono()
        every { orderMapper.toPaymentRequest(any()) } returns paymentRequest
        every { paymentClient.pay(any()) } returns paymentResponse
        if (paymentResponseValue == null) {
            every { orderMapper.toEntity(any(), any<OrderStatus>()) } returns updatedOrder
        } else {
            every { orderMapper.toEntity(any(), any<PaymentResponse>()) } returns updatedOrder
        }
        every { orderRepository.save(updatedOrder) } returns updatedOrder.toMono()
        every { orderMapper.toResponse(any()) } returns expected

        StepVerifier
            .create(sut.process(request))
            .assertNext { assertThat(it).isEqualTo(expected) }
            .verifyComplete()

        verify(atLeast = 1, ordering = Ordering.ORDERED) {
            orderMapper.toEntity(request)
            orderRepository.save(newOrder)
            orderMapper.toPaymentRequest(savedOrder)
            paymentClient.pay(paymentRequest)
            if (paymentResponseValue != null) {
                orderMapper.toEntity(
                    savedOrder,
                    paymentResponseValue,
                )
            } else {
                orderMapper.toEntity(
                    savedOrder,
                    OrderStatus.PAYMENT_FAILED,
                )
            }
            orderRepository.save(updatedOrder)
            orderMapper.toResponse(savedOrder)
        }
    }

    class DataProvider : ArgumentsProvider {
        override fun provideArguments(context: ExtensionContext): Stream<Arguments> =
            Stream.of(
                Arguments.of(OrderStatus.PAID, PaymentStatus.SUCCESS),
                Arguments.of(OrderStatus.PAYMENT_REJECTED, PaymentStatus.REJECTED),
                Arguments.of(OrderStatus.PAYMENT_FAILED, null),
            )
    }
}
