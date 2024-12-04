package io.github.arthurfish.appender.userservice

import org.springframework.amqp.core.*
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

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
      .whereAny("user_operation", null)
      .exist()

  }
}
