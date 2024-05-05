package com.illenko.api

import com.illenko.core.BaseFunctionalTest
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import org.junit.jupiter.params.provider.ArgumentsSource
import org.skyscreamer.jsonassert.Customization
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode
import org.skyscreamer.jsonassert.comparator.CustomComparator
import org.springframework.http.HttpStatus
import java.util.stream.Stream

class OrderJsonFunctionalTest : BaseFunctionalTest() {
    @ParameterizedTest
    @ArgumentsSource(DataProvider::class)
    fun `orders endpoint test using jsons`(
        expected: String,
        paymentResponse: String,
        paymentResponseStatus: HttpStatus,
    ) {
        val request = loadResource("/internal/request")

        mockPaymentResponse(paymentResponse, paymentResponseStatus)

        val actual = doPost<String>(uri = "/orders", body = request).responseBody.blockFirst()!!

        assertJson(expected, actual)
    }

    private fun mockPaymentResponse(
        paymentResponse: String,
        paymentResponseStatus: HttpStatus,
    ) {
        mockPost(
            uri = "/api/v1/payments",
            requestBody = loadResource("external/payment/request"),
            requestHeaders = mapOf("API-KEY" to "secret-api-key"),
            responseBody = paymentResponse,
            responseStatus = paymentResponseStatus,
        )
    }

    private fun assertJson(
        expected: String,
        actual: String,
    ) {
        JSONAssert.assertEquals(
            expected,
            actual,
            CustomComparator(JSONCompareMode.LENIENT, Customization("id") { _, _ -> true }),
        )
    }

    class DataProvider : ArgumentsProvider {
        override fun provideArguments(context: ExtensionContext): Stream<Arguments> =
            Stream.of(
                Arguments.of(
                    loadResource("internal/response_success"),
                    loadResource("external/payment/response_success"),
                    HttpStatus.OK,
                ),
                Arguments.of(
                    loadResource("internal/response_rejected"),
                    loadResource("external/payment/response_rejected"),
                    HttpStatus.OK,
                ),
                Arguments.of(
                    loadResource("internal/response_failed"),
                    "{}",
                    HttpStatus.BAD_REQUEST,
                ),
            )
    }
}
