package com.illenko.core

import com.illenko.repository.OrderRepository
import io.mockk.junit5.MockKExtension.CheckUnnecessaryStub
import io.mockk.junit5.MockKExtension.ConfirmVerification
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@Tag("IntegrationTest")
@ActiveProfiles("test", "integration")
@SpringBootTest
@ConfirmVerification
@CheckUnnecessaryStub
abstract class BaseIntegrationTest : BaseTest() {
    @Autowired
    lateinit var orderRepository: OrderRepository

    @BeforeEach
    fun setup() {
        orderRepository.deleteAll().block()
    }
}
