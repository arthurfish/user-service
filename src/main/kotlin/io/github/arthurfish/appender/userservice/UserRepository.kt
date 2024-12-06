package io.github.arthurfish.appender.userservice

import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.r2dbc.core.bind
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
class UserRepository(
  private val databaseClient: DatabaseClient
) {


  fun findUserCredential(userId: String): Mono<Map<String, String>> {
    return databaseClient.sql("""
        SELECT 
            password_salt_hash, 
            salt, 
            hash_algorithm, 
            user_info::text AS user_info 
        FROM users 
        WHERE user_id = :user_id::UUID
    """.trimIndent())
      .bind("user_id", userId)
      .map { row, _ ->
        mapOf(
          "password_salt_hash" to (row.get("password_salt_hash", String::class.java) ?: ""),
          "salt" to (row.get("salt", String::class.java) ?: ""),
          "hash_algorithm" to (row.get("hash_algorithm", String::class.java) ?: ""),
          "user_info" to (row.get("user_info", String::class.java) ?: "{}")
        )
      }
      .one()
      .defaultIfEmpty(emptyMap())
  }

  fun findUserInfo(userId: String): Mono<String> {
    return databaseClient.sql("SELECT user_info FROM users WHERE user_id = $1::UUID")
      .bind(0, userId)
      .map { row, _ -> row.get("user_info", String::class.java) ?: "{}" }
      .one()
  }


  fun updateUserCredential(
    userId: String,
    passwordSaltHash: String,
    salt: String,
    hashAlgorithm: String
  ): Mono<Long> {
    return databaseClient.sql("""
        UPDATE users 
        SET 
            password_salt_hash = :password_salt_hash, 
            salt = :salt, 
            hash_algorithm = :hash_algorithm 
        WHERE user_id = :user_id::UUID
    """.trimIndent())
      .bind("password_salt_hash", passwordSaltHash)
      .bind("salt", salt)
      .bind("hash_algorithm", hashAlgorithm)
      .bind("user_id", userId)
      .fetch()
      .rowsUpdated()
  }

  fun updateUserInfo(userId: String, info: String): Mono<Long> {
    return databaseClient.sql("UPDATE users SET user_info = :info WHERE user_id = :user_id::UUID")
      .bind("info", info)
      .bind("user_id", userId)
      .fetch()
      .rowsUpdated()
  }
}
