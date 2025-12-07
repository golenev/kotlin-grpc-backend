package com.example.grpce2e.db

object Environment {
    val DB_URL: String = System.getenv("ANALYTICS_JDBC_URL") ?: "jdbc:postgresql://localhost:5433/analytics"
    const val DB_DRIVER: String = "org.postgresql.Driver"
    val DB_USER: String = System.getenv("ANALYTICS_DB_USER") ?: "analytics"
    val DB_PASSWORD: String = System.getenv("ANALYTICS_DB_PASSWORD") ?: "analytics"
}
