package io.github.arthurfish.appender.userservice

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository

@Repository
class UserRepository(private val jdbcTemplate: JdbcTemplate) {

  fun findUserCredential(userId: String): String? {
    val sql = "SELECT user_credential FROM users WHERE user_id = ?"
    return jdbcTemplate.queryForObject(sql, arrayOf(userId)) { rs, _ ->
      rs.getString("user_credential")
    }
  }

  fun findUserInfo(userId: String): String? {
    val sql = "SELECT user_info FROM users WHERE user_id = ?"
    return jdbcTemplate.queryForObject(sql, arrayOf(userId)) { rs, _ ->
      rs.getString("user_info")
    }
  }

  fun updateUserCredential(userId: String, credential: String) {
    val sql = "UPDATE users SET user_credential = ? WHERE user_id = ?"
    jdbcTemplate.update(sql, credential, userId)
  }

  fun updateUserInfo(userId: String, info: String) {
    val sql = "UPDATE users SET user_info = ? WHERE user_id = ?"
    jdbcTemplate.update(sql, info, userId)
  }

  // Test data inserted
  fun insertUsers() {
    val sql = """
            INSERT INTO users (user_credential, user_info) VALUES (?, ?)
        """

    val users = listOf(
      Pair(
        """{"password_hash": "bla", "hash_algorithm": "bcrypt"}""",
        """{"user_name": "Alice", "avatar": "minio blob", "description_markdown": "I am Alice."}"""
      ),
      Pair(
        """{"password_hash": "bla", "hash_algorithm": "bcrypt"}""",
        """{"user_name": "Bob", "avatar": "minio blob", "description_markdown": "I am Bob."}"""
      ),
      Pair(
        """{"password_hash": "bla", "hash_algorithm": "bcrypt"}""",
        """{"user_name": "Carol", "avatar": "minio blob", "description_markdown": "I am Carol."}"""
      ),
      Pair(
        """{"password_hash": "bla", "hash_algorithm": "bcrypt"}""",
        """{"user_name": "Dave", "avatar": "minio blob", "description_markdown": "I am Dave."}"""
      )
    )

    users.forEach { (cred, info) ->
      jdbcTemplate.update(sql, cred, info)
    }
  }

}
