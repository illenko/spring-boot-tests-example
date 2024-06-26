package com.illenko.core

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.illenko.repository.OrderRepository
import io.mockk.clearAllMocks
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
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
        clearAllMocks()
        clearDatabase()
        resetWiremock()
    }

    @AfterEach
    fun afterEach() {
        checkForUnmatchedRequests()
    }

    final inline fun <reified T : Any> doPost(
        uri: String,
        body: String,
    ) = webTestClient.post()
        .uri(uri)
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(body)
        .exchange()
        .returnResult<T>()

    fun mockPost(
        uri: String,
        requestBody: String?,
        requestHeaders: Map<String, String> = mapOf(),
        responseBody: String = "{}",
        responseStatus: HttpStatus = HttpStatus.OK,
    ) {
        wiremock.stubFor(
            WireMock.post(WireMock.urlPathEqualTo(uri))
                .apply {
                    if (requestBody != null) withRequestBody(WireMock.equalToJson(requestBody))
                }
                .apply {
                    requestHeaders.forEach {
                        withHeader(it.key, WireMock.equalTo(it.value))
                    }
                }
                .willReturn(WireMock.jsonResponse(responseBody, responseStatus.value())),
        )
    }

    private fun clearDatabase() {
        orderRepository.deleteAll().block()
    }

    private fun resetWiremock() {
        wiremock.resetAll()
    }

    private fun checkForUnmatchedRequests() {
        wiremock.checkForUnmatchedRequests()
    }

    companion object {
        private const val JDBC_PREFIX = "jdbc"
        private const val R2DBC_PREFIX = "r2dbc"

        @JvmStatic
        val psqlContainer: PostgreSQLContainer<*> =
            PostgreSQLContainer("postgres:16-alpine")
                .withUsername("postgres")
                .withPassword("postgres")
                .withDatabaseName("order-service")
                .withExposedPorts(5432)

        @JvmField
        val wiremock = WireMockServer(WireMockConfiguration.wireMockConfig().port(0))

        @JvmStatic
        @DynamicPropertySource
        fun registerContainers(registry: DynamicPropertyRegistry) {
            psqlContainer.start()
            wiremock.start()
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
