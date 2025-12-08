package com.example.grpce2e.db

object Environment {
    const val DB_DRIVER: String = "org.postgresql.Driver"

    val ANALYTICS_DB_URL: String = System.getenv("ANALYTICS_JDBC_URL") ?: "jdbc:postgresql://localhost:5433/analytics"
    val ANALYTICS_DB_USER: String = System.getenv("ANALYTICS_DB_USER") ?: "analytics"
    val ANALYTICS_DB_PASSWORD: String = System.getenv("ANALYTICS_DB_PASSWORD") ?: "analytics"

    val ORDER_DB_URL: String = System.getenv("ORDER_JDBC_URL") ?: "jdbc:postgresql://localhost:5434/orders"
    val ORDER_DB_USER: String = System.getenv("ORDER_DB_USER") ?: "orders"
    val ORDER_DB_PASSWORD: String = System.getenv("ORDER_DB_PASSWORD") ?: "orders"
}
