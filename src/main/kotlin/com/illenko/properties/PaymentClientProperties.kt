package com.illenko.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("app.payment-client")
data class PaymentClientProperties(
    val url: String,
    val apiKey: String,
)
