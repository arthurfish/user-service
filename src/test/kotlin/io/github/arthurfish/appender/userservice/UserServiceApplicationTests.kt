package io.github.arthurfish.appender.userservice

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.amqp.core.Message
import org.springframework.amqp.core.MessageBuilder

import org.springframework.messaging.converter.MappingJackson2MessageConverter
import org.springframework.messaging.converter.MessageConverter

class JsonToMapConversionTest {

  private val objectMapper: ObjectMapper = ObjectMapper()
  private val converter = MappingJackson2MessageConverter()

  @Test
  fun `test JSON to Map conversion`() {
    val json = """{"user_id": "4"}"""
    val message: Message = MessageBuilder
      .withBody(json.toByteArray())
      .setHeader("content_type", "application/json")
      .build()

    val result = converter.fromMessage(message, Map::class.java) as Map<String, String>
    assertEquals("4", result["user_id"])
  }
}

