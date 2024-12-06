package io.github.arthurfish.appender.userservice

import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.reactive.awaitFirstOrElse
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Service
import org.springframework.amqp.core.MessageBuilder
import org.springframework.amqp.core.MessageProperties
import reactor.core.publisher.Mono


@Service
class UserOperationMessageService(
  private val rabbitTemplate: RabbitTemplate,
  private val userRepository: UserRepository,
) {
  private val log = LoggerFactory.getLogger(UserOperationMessageService::class.java)
  private val objectMapper = ObjectMapper()

  @RabbitListener(queues = ["#{userOperationQueue.name}"])
  fun processUserOperation(message: Map<String, String>) = runBlocking{
    log.info("Processing user operation: ${message}")
    val userId = message["user_id"] ?: run {
      log.error("No user_id provided")
      return@runBlocking
    }
    val requestId = message["request_id"] ?: "no_request_id"
    val operation = message["user_operation"] ?: "do_nothing"

    val responseHeaders = mapOf(
      "user_operation_result" to "success",
      "request_id" to requestId,
      "user_id" to userId,
      "debug" to "true"
    )

    val responseBodyMono: Mono<Map<String, String>> = when (operation) {
      "query_credential" -> handleQueryCredential(userId)
      "query_info" -> handleQueryInfo(userId)
      "update_credential" -> handleUpdateCredential(message, userId)
      "update_info" -> handleUpdateInfo(message, userId)
      else -> {
        log.warn("Unknown operation: $operation")
        Mono.empty()
      }
    }.map{it.plus(message.minus("user_operation"))}

    // 使用 subscribe 来处理响应
      responseBodyMono
        .doOnNext { responseBody ->
          log.info("Preparing to send response: $responseBody")
          sendResponseMessage(responseHeaders, responseBody)
        }
        .doOnSuccess { log.info("Processing completed successfully") }
        .doOnError { error -> log.error("Error processing message", error) }
        .awaitFirstOrNull()
  }
  private fun handleQueryCredential(userId: String): Mono<Map<String, String>> {
    return userRepository.findUserCredential(userId)
      .defaultIfEmpty(mapOf("result" to "failed"))
  }

  private fun handleQueryInfo(userId: String): Mono<Map<String, String>> {
    return userRepository.findUserInfo(userId)
      .map { userInfo ->
        mapOf("user_info" to userInfo, "user_id" to userId, "debug" to "user_query")
      }
  }

  private fun handleUpdateCredential(message: Map<String, String>, userId: String): Mono<Map<String, String>> {
    val passwordSaltHash = message["password_salt_hash"] ?: return Mono.empty()
    val salt = message["salt"] ?: return Mono.empty()
    val hashAlgorithm = message["hash_algorithm"] ?: return Mono.empty()
    log.info("Processing credential change")
    return userRepository.updateUserCredential(userId, passwordSaltHash, salt, hashAlgorithm)
      .thenReturn(mapOf("user_credential" to "updated"))
  }

  private fun handleUpdateInfo(message: Map<String, String>, userId: String): Mono<Map<String, String>> {
    val newInfo = message["target_info"] ?: return Mono.empty()
    return userRepository.updateUserInfo(userId, newInfo)
      .thenReturn(mapOf("user_info" to "updated"))
  }
  private fun sendResponseMessage(headers: Map<String, String>, body: Map<String, String>) {
    rabbitTemplate.convertAndSend(
      "appender-core-exchange",
      "default.key",
      body // AppenderRabbitmqMessageConverter会处理这个Map
    ) { message ->
      // 添加headers
      headers.forEach { (key, value) ->
        message.messageProperties.headers[key] = value
      }
      message
    }
  }

}
