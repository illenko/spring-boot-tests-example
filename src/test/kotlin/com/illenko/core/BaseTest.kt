package com.illenko.core

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.instancio.Instancio

abstract class BaseTest {
    fun Any.toJson(): String = mapper.writeValueAsString(this)

    inline fun <reified T : Any> random(): T = Instancio.create(T::class.java)

    companion object {
        val mapper: ObjectMapper = jacksonObjectMapper().registerModule(JavaTimeModule())
    }
}
