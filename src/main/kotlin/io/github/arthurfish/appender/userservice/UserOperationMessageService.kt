package io.github.arthurfish.appender.userservice

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Service
import org.springframework.amqp.core.Message
import org.springframework.amqp.core.MessageBuilder
import org.springframework.amqp.core.MessageProperties

// TODO: 改网关服务器，支持header exchange

@Service
class UserOperationMessageService (
  private val rabbitTemplate: RabbitTemplate,
  private val userRepository: UserRepository,
) {

  @RabbitListener(queues = ["#{userOperationQueue.name}"])
  fun processUserOperation(message: Map<String, String>) {
    val userId = message["user_id"] ?: "no_body"
    val requestId = message["request_id"] ?: "no_request_id"
    val operation = message["user_operation"]!!

    val responseHeaders: Map<String, String> = mapOf("user_operation_result" to "success", "request_id" to requestId, "user_id" to userId)

    when (operation) {
      "query_credential" -> {
        val credential = userRepository.findUserCredential(userId)
        val responseBody = mapOf("user_credential" to (credential ?: "{}"), "request_id" to requestId, "user_id" to userId)
        sendResponseMessage(responseHeaders, responseBody)
      }
      "query_info" -> {
        val info = userRepository.findUserInfo(userId)
        val responseBody = mapOf("user_info" to (info ?: "{}"), "request_id" to requestId, "user_id" to userId)
        sendResponseMessage(responseHeaders, responseBody)
      }
      "update_credential" -> {
        val newCredential = message["target_credential"] as String
        userRepository.updateUserCredential(userId, newCredential)
        sendResponseMessage(responseHeaders, mapOf("user_credential" to "updated"))
      }
      "update_info" -> {
        val newInfo = message["target_info"] as String
        userRepository.updateUserInfo(userId, newInfo)
        sendResponseMessage(responseHeaders, mapOf("user_info" to "updated"))
      }
      else -> {
        // Optionally handle unknown operations
      }
    }
  }

  private fun sendResponseMessage(headers: Map<String, String>, body: Map<String, String>) {
    val objectMapper = ObjectMapper()
    val messageProperties = MessageProperties()
    headers.forEach { (key, value) -> messageProperties.setHeader(key, value) }
    val bodyJson = ObjectMapper().writeValueAsString(body)
    val message = MessageBuilder.withBody(bodyJson.toByteArray())
      .andProperties(messageProperties)
      .build()

    rabbitTemplate.send("appender-core-exchange", "", message)
  }
}
