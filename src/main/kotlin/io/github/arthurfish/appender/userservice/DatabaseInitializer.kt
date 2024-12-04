package io.github.arthurfish.appender.userservice

import jakarta.annotation.PostConstruct
import org.springframework.boot.CommandLineRunner
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Component
class DatabaseInitializer(private val jdbcTemplate: JdbcTemplate) {
  @PostConstruct
  fun initializeDatabase() {
    val createTableSql = """
            CREATE TABLE IF NOT EXISTS users (
                user_id SERIAL PRIMARY KEY,
                user_credential JSONB NOT NULL,
                user_info JSONB NOT NULL
            )
        """
    jdbcTemplate.execute(createTableSql)
    val logger = LoggerFactory.getLogger(DatabaseInitializer::class.java)
    logger.info("Database initialized!")
  }
}
