package io.github.arthurfish.appender.userservice

import org.springframework.amqp.core.*
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter
import org.springframework.amqp.support.converter.MessageConverter
import org.springframework.jdbc.core.simple.JdbcClient


@Configuration
class RabbitMQConfig {

  @Bean
  fun appenderCoreExchange(): HeadersExchange {
    return HeadersExchange("appender-core-exchange")
  }

  @Bean
  fun userOperationQueue(): Queue {
    return Queue("user-operation-queue")
  }

  @Bean
  fun userOperationBinding(userOperationQueue: Queue, appenderCoreExchange: HeadersExchange): Binding {
    return BindingBuilder
      .bind(userOperationQueue)
      .to(appenderCoreExchange)
      .whereAny("user_operation")
      .exist()

  }

  @Bean
  fun messageConverter(): MessageConverter {
    val objectMapper = ObjectMapper()
    return Jackson2JsonMessageConverter(objectMapper)
  }

  @Bean
  fun rabbitTemplate(connectionFactory: ConnectionFactory, messageConverter: MessageConverter): RabbitTemplate {
    return RabbitTemplate(connectionFactory).apply {
      this.messageConverter = messageConverter
    }
  }
}
