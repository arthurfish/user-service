package io.github.arthurfish.appender.userservice

import org.springframework.amqp.core.*
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.amqp.core.Queue
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter
import org.springframework.amqp.support.converter.MessageConverter
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.stereotype.Component
import java.util.*


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
  fun messageConverter(): MessageConverter = AppenderRabbitmqMessageConverter()

  @Bean
  fun rabbitTemplate(connectionFactory: ConnectionFactory): RabbitTemplate {
    return RabbitTemplate(connectionFactory).apply {
      messageConverter = messageConverter()
    }
  }

  @Bean
  fun reactiveListenerContainerFactory(
    connectionFactory: ConnectionFactory
  ): SimpleRabbitListenerContainerFactory {
    return SimpleRabbitListenerContainerFactory().apply {
      setConnectionFactory(connectionFactory)
      setDefaultRequeueRejected(false)
    }
  }
}

