package com.illenko.client.token.response

import java.util.UUID

data class TokenResponse(
    val id: UUID,
    val userId: UUID,
    val token: String,
)