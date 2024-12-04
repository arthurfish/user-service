package io.github.arthurfish.appender.userservice

import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.core.annotation.Order
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component

@Component
class DatabaseInitializer(private val jdbcTemplate: JdbcTemplate) {
  @PostConstruct
  @Order(1)
  fun initializeDatabase() {
    val createTableSql = """
      CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
            CREATE TABLE IF NOT EXISTS users (
                user_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                user_credential JSONB NOT NULL,
                user_info JSONB NOT NULL
            )
        """
    jdbcTemplate.execute(createTableSql)
    val logger = LoggerFactory.getLogger(DatabaseInitializer::class.java)
    logger.info("Database initialized!")
  }

  fun isTableEmpty(): Boolean {
    val sql = "SELECT COUNT(*) FROM users"
    val count = jdbcTemplate.queryForObject(sql, Int::class.java)
    return count == 0
  }

  @PostConstruct
  @Order(2)
  fun insertDummyUser() {
    val sql = """
            INSERT INTO users (user_credential, user_info)
            VALUES (to_jsonb(?::text), to_jsonb(?::text))
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
    if(isTableEmpty()){
    users.forEach { (cred, info) ->
      jdbcTemplate.update(sql, cred, info)
    }}
  }

}
