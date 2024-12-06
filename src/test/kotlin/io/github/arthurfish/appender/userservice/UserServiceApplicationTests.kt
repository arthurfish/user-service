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

}

