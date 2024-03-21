package com.illenko.core

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.instancio.Instancio
import java.io.File
import java.nio.file.Files

abstract class BaseTest {
    fun Any.toJson(): String = mapper.writeValueAsString(this)

    companion object {
        val mapper: ObjectMapper = jacksonObjectMapper().registerModule(JavaTimeModule())

        inline fun <reified T : Any> random(): T = Instancio.create(T::class.java)

        fun loadResource(file: String) = String(Files.readAllBytes(File("src/test/resources/interactions/$file.json").toPath()))
    }
}
