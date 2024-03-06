package com.illenko.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("app.payment")
data class PaymentClient(
    val url: String,
    val apiKey: String,
)