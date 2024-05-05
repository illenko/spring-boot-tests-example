package com.illenko.core

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.instancio.Instancio
import java.nio.file.Files
import java.nio.file.Paths

abstract class BaseTest {
    private val mapper: ObjectMapper = initializeMapper()

    fun Any.toJson(): String = mapper.writeValueAsString(this)

    companion object {
        inline fun <reified T : Any> random(): T = Instancio.create(T::class.java)

        fun loadResource(file: String): String {
            val path = getResourcePath(file)
            return String(Files.readAllBytes(path))
        }

        private fun getResourcePath(file: String) = Paths.get("src", "test", "resources", "interactions", "$file.json")

        private fun initializeMapper(): ObjectMapper {
            return jacksonObjectMapper().registerModule(JavaTimeModule())
        }
    }
}
