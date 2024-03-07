package com.illenko.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("app.token-client")
data class TokenClientProperties(
    val url: String,
)
