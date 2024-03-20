package com.illenko.core

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.junit5.WireMockExtension
import com.illenko.repository.OrderRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.extension.RegisterExtension
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.returnResult
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container

@Tag("FunctionalTest")
@ActiveProfiles("test", "functional")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient(timeout = "PT10M")
@DirtiesContext
abstract class BaseFunctionalTest : BaseTest() {
    @Autowired
    lateinit var webTestClient: WebTestClient

    @Autowired
    lateinit var orderRepository: OrderRepository

    @BeforeEach
    fun setup() {
        orderRepository.deleteAll().block()
        wiremock.resetAll()
    }

    final inline fun <reified T : Any> doPost(
        uri: String,
        body: Any,
    ) = webTestClient.post()
        .uri(uri)
        .accept(MediaType.APPLICATION_JSON)
        .bodyValue(body)
        .exchange()
        .returnResult<T>()

    fun mockPost(
        uri: String,
        requestBody: Any? = null,
        requestHeaders: Map<String, String> = mapOf(),
        responseBody: Any? = null,
        responseStatus: HttpStatus = HttpStatus.OK,
    ) {
        wiremock.stubFor(
            WireMock.post(WireMock.urlPathEqualTo(uri))
                .apply {
                    if (requestBody != null) withRequestBody(WireMock.equalToJson(requestBody.toJson()))
                }
                .apply {
                    requestHeaders.forEach {
                        withHeader(it.key, WireMock.equalTo(it.value))
                    }
                }
                .willReturn(WireMock.jsonResponse(responseBody?.toJson() ?: "{}", responseStatus.value())),
        )
    }

    @Suppress("unused")
    companion object {
        private const val JDBC_PREFIX = "jdbc"
        private const val R2DBC_PREFIX = "r2dbc"

        @Container
        @JvmStatic
        val psqlContainer: PostgreSQLContainer<*> =
            PostgreSQLContainer("postgres:16-alpine")
                .withUsername("postgres")
                .withPassword("postgres")
                .withDatabaseName("order-service")
                .withExposedPorts(5432)

        @JvmField
        @RegisterExtension
        val wiremock =
            WireMockExtension.newInstance()
                .options(WireMockConfiguration.wireMockConfig().port(0))
                .build()!!

        @JvmStatic
        @DynamicPropertySource
        fun registerContainers(registry: DynamicPropertyRegistry) {
            psqlContainer.start()
            registry.apply {
                add("spring.liquibase.url") { psqlContainer.jdbcUrl }
                add("spring.r2dbc.url") { psqlContainer.jdbcUrl.replace(JDBC_PREFIX, R2DBC_PREFIX) }
                add("spring.r2dbc.username") { psqlContainer.username }
                add("spring.r2dbc.password") { psqlContainer.password }
                add("app.payment-client.url") { wiremock.baseUrl() }
            }
        }
    }
}
