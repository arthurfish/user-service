package io.github.arthurfish.appender.userservice

import io.r2dbc.spi.ConnectionFactory
import jakarta.annotation.PostConstruct
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.core.annotation.Order
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.r2dbc.core.awaitSingle
import org.springframework.stereotype.Component

@Component
class DatabaseInitializer(
  private val connectionFactory: ConnectionFactory,
  private val databaseClient: DatabaseClient
) {
  private val logger = LoggerFactory.getLogger(DatabaseInitializer::class.java)

  @PostConstruct
  @Order(1)
  fun initializeDatabase() {
    runBlocking {
      val createTableSql = """
                CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
                CREATE TABLE IF NOT EXISTS users (
                    user_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                    password_salt_hash varchar(512) NOT NULL,
                    salt varchar(512) NOT NULL,
                    hash_algorithm varchar(512) NOT NULL,
                    user_info JSONB NOT NULL
                )
            """.trimIndent()

      databaseClient.sql(createTableSql)
        .fetch()
        .rowsUpdated()
        .awaitFirstOrNull()

      logger.info("Database initialized!")
    }
  }

  private suspend fun isTableEmpty(): Boolean {
    return databaseClient.sql("SELECT COUNT(*) FROM users")
      .map { row ->
        println(row.toString())
        row.get(0).toString().toLong() ?: 0L
      }
      .awaitSingle() == 0L
  }

  @PostConstruct
  @Order(2)
  fun insertDummyUser() {
    runBlocking {
      if (isTableEmpty()) {
        val users = listOf(
          User(
            passwordSaltHash = "bla",
            salt = "salt1",
            hashAlgorithm = "bcrypt",
            userInfo = """{"user_name": "Alice", "avatar": "minio blob", "description_markdown": "I am Alice."}"""
          ),
          User(
            passwordSaltHash = "bla",
            salt = "salt2",
            hashAlgorithm = "bcrypt",
            userInfo = """{"user_name": "Bob", "avatar": "minio blob", "description_markdown": "I am Bob."}"""
          ),
          User(
            passwordSaltHash = "bla",
            salt = "salt3",
            hashAlgorithm = "bcrypt",
            userInfo = """{"user_name": "Carol", "avatar": "minio blob", "description_markdown": "I am Carol."}"""
          ),
          User(
            passwordSaltHash = "bla",
            salt = "salt4",
            hashAlgorithm = "bcrypt",
            userInfo = """{"user_name": "Dave", "avatar": "minio blob", "description_markdown": "I am Dave."}"""
          )
        )

        users.forEach { user ->
          databaseClient.sql(
            """
                    INSERT INTO users (password_salt_hash, salt, hash_algorithm, user_info)
                    VALUES (:password_salt_hash, :salt, :hash_algorithm, to_jsonb(:user_info::text))
                    """
          )
            .bind("password_salt_hash", user.passwordSaltHash)
            .bind("salt", user.salt)
            .bind("hash_algorithm", user.hashAlgorithm)
            .bind("user_info", user.userInfo)
            .fetch()
            .rowsUpdated()
            .awaitFirst()
        }

        logger.info("Dummy users inserted!")
      }
    }
  }

  // 增加一个数据类来承载用户信息
  data class User(
    val passwordSaltHash: String,
    val salt: String,
    val hashAlgorithm: String,
    val userInfo: String
  )

}

