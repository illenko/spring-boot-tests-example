package com.illenko.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("app.token")
data class TokenProperties(
    val url: String,
)