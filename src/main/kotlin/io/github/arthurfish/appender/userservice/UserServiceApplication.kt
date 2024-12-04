package io.github.arthurfish.appender.userservice

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class UserServiceApplication

fun main(args: Array<String>) {
  runApplication<UserServiceApplication>(*args)
}
