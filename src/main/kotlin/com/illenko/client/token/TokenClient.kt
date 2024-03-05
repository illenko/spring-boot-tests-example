package com.illenko.client.token

import com.illenko.client.token.response.TokenResponse
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import java.util.UUID

@Component
class TokenClient {

    fun getToken(id: UUID): Mono<TokenResponse> {
        TODO("implement")
    }
}
