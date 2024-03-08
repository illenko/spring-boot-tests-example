package com.illenko.service

import com.illenko.api.request.OrderRequest
import com.illenko.api.response.OrderResponse
import com.illenko.client.payment.PaymentClient
import com.illenko.client.payment.enum.PaymentStatus
import com.illenko.client.payment.request.PaymentRequest
import com.illenko.client.payment.response.PaymentResponse
import com.illenko.client.token.TokenClient
import com.illenko.client.token.response.TokenResponse
import com.illenko.core.BaseUnitTest
import com.illenko.enum.OrderStatus
import com.illenko.mapper.OrderMapper
import com.illenko.mapper.PaymentMapper
import com.illenko.model.Order
import com.illenko.repository.OrderRepository
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

    @BeforeEach
    fun setup() {
        clearAllMocks()
    }

    @ParameterizedTest
    @ArgumentsSource(DataProvider::class)
    fun `processing order`(
        paymentStatus: PaymentStatus,
        orderStatus: OrderStatus,
        tokenClientError: Boolean,
        paymentClientError: Boolean,
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
                status = orderStatus
            }

        val token = random<String>()
        val tokenResponse =
            if (tokenClientError) {
                Mono.error(RuntimeException())
            } else {
                random<TokenResponse>().copy(token = token).toMono()
            }

        val paymentRequest =
            PaymentRequest(
                orderId = savedOrder.id!!,
                token = token,
                amount = request.price,
            )

        val paymentResponse =
            if (paymentClientError) {
                Mono.error(RuntimeException())
            } else {
                PaymentResponse(
                    id = random(),
                    status = paymentStatus,
                ).toMono()
            }

        val expected = random<OrderResponse>()

        every { orderMapper.toEntity(any()) } returns newOrder
        every { orderRepository.save(any()) } returns savedOrder.toMono()
        every { tokenClient.getToken(any()) } returns tokenResponse
        every { paymentMapper.toPaymentRequest(any(), any()) } returns paymentRequest
        every { paymentClient.pay(any()) } returns paymentResponse
        every { orderMapper.toResponse(any()) } returns expected

        StepVerifier
            .create(sut.process(request))
            .assertNext { assertThat(it).isEqualTo(expected) }
            .verifyComplete()

        verify {
            orderMapper.toEntity(request)
            orderRepository.save(newOrder)
            orderRepository.save(savedOrder)
            tokenClient.getToken(savedOrder.tokenId)
            if (!tokenClientError) {
                paymentMapper.toPaymentRequest(savedOrder, token)
                paymentClient.pay(paymentRequest)
            }
            orderMapper.toResponse(savedOrder)
        }
    }

    class DataProvider : ArgumentsProvider {
        override fun provideArguments(context: ExtensionContext): Stream<Arguments> =
            Stream.of(
                Arguments.of(PaymentStatus.SUCCESS, OrderStatus.PAID, false, false),
                Arguments.of(PaymentStatus.FAILED, OrderStatus.CANCELED, false, false),
                Arguments.of(PaymentStatus.SUCCESS, OrderStatus.CANCELED, true, false),
                Arguments.of(PaymentStatus.FAILED, OrderStatus.CANCELED, false, true),
            )
    }
}
