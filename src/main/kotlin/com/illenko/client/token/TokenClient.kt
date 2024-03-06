package com.illenko.client.token

import com.illenko.client.token.response.TokenResponse
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import java.util.UUID

@Component
class TokenClient(
    @Qualifier("tokenWebClient")
    private val client: WebClient,
) {

    private val log = KotlinLogging.logger {}

    fun getToken(id: UUID): Mono<TokenResponse> {
        log.info { "Sending payment request: $id" }
        return client.get()
            .uri { it.path("/tokens/{id}").build(id) }
            .retrieve()
            .bodyToMono(TokenResponse::class.java)
            .doOnNext { log.info { "Retrieved token response: $it" } }
    }
}
