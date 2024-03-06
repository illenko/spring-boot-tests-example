package com.illenko.config

import com.illenko.properties.PaymentClient
import com.illenko.properties.TokenProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class WebClientConfig {

    @Bean("tokenWebClient")
    fun tokenWebClient(properties: TokenProperties): WebClient =
        WebClient.builder()
            .baseUrl(properties.url)
            .build()

    @Bean("paymentWebClient")
    fun paymentWebClient(properties: PaymentClient): WebClient =
        WebClient.builder()
            .baseUrl(properties.url)
            .defaultHeader("API-KEY", properties.apiKey)
            .build()
}
