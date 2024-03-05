package com.illenko.api

import com.illenko.api.handler.OrderHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.server.router

@Configuration
class Routes {

    @Bean
    fun orderRouter(handler: OrderHandler) = router {
        POST("/orders", handler::process)
    }
}
