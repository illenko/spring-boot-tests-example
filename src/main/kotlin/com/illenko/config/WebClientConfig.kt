package com.illenko.config

import com.illenko.properties.PaymentClientProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class WebClientConfig {
    @Bean("paymentWebClient")
    fun paymentWebClient(properties: PaymentClientProperties): WebClient =
        WebClient.builder()
            .baseUrl(properties.url)
            .defaultHeader("API-KEY", properties.apiKey)
            .build()
}
