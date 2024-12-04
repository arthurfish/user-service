package io.github.arthurfish.appender.userservice

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.stereotype.Repository
import java.util.*

@Repository
class UserRepository(private val jdbcTemplate: JdbcTemplate,
  private val jdbcClient: JdbcClient
) {

  fun findUserCredential(userId: String): String? {
    val sql = "SELECT user_credential FROM users WHERE user_id = ?"
    return jdbcTemplate.queryForObject(sql, arrayOf(userId)) { rs, _ ->
      rs.getString("user_credential")
    }
  }

  fun findUserInfo(userId: String): String {
    // Create a client to perform the query operation
      return jdbcClient.sql("SELECT user_info FROM users WHERE user_id = :user_id::UUID")
        .param("user_id", userId)
        .query(String::class.java)
        .optional().orElse("{}")
  }

  fun updateUserCredential(userId: String, credential: String) {
    val sql = "UPDATE users SET user_credential = ? WHERE user_id = ?"
    jdbcTemplate.update(sql, credential, userId)
  }

  fun updateUserInfo(userId: String, info: String) {
    val sql = "UPDATE users SET user_info = ? WHERE user_id = ?"
    jdbcTemplate.update(sql, info, userId)
  }
}

