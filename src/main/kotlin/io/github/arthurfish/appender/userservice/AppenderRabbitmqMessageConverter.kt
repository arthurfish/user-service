package io.github.arthurfish.appender.userservice

import org.slf4j.LoggerFactory
import org.springframework.amqp.core.Message
import org.springframework.amqp.core.MessageProperties
import org.springframework.amqp.support.converter.AbstractMessageConverter
import java.nio.charset.Charset

class AppenderRabbitmqMessageConverter : AbstractMessageConverter() {
  private val log = LoggerFactory.getLogger(AppenderRabbitmqMessageConverter::class.java)

  override fun fromMessage(message: Message): Map<String, String> {
    return try {
      val content = String(message.body, Charset.forName("UTF-8"))
      log.debug("Raw message content: $content")

      // 去掉开头的 "={" 和结尾的 "}"
      val trimmedContent = content.removePrefix("{").removeSuffix("}")

      // 分割成键值对并转换为 Map
      trimmedContent
        .split(", ")
        .map { pair ->
          val parts = pair.split("=", limit = 2)
          if (parts.size == 2) {
            parts[0].trim() to parts[1].trim()
          } else {
            log.warn("Invalid pair format: $pair")
            null
          }
        }
        .filterNotNull()
        .toMap()
        .also { map ->
          log.debug("Converted to map: {}", map)
        }
    } catch (e: Exception) {
      log.error("Error converting message: ${String(message.body)}", e)
      mapOf("message_convert_error" to "Can't convert message: ${String(message.body)}")
    }
  }

  override fun createMessage(obj: Any, messageProperties: MessageProperties): Message {
    // 如果需要发送消息，可以实现这个方法
    // 现在先返回简单的实现
    val str = when (obj) {
      is Map<*, *> -> {
        log.info("Sent to Exchange. Message: ${obj.entries}")
        obj.entries.joinToString(", ") { "${it.key}=${it.value}" }

      }
      else -> {
        log.info("At user-servcie.createMessage., Not Map<String, String>")
        obj.toString()
      }

    }
    return Message(str.toByteArray(), messageProperties)
  }
}
