package com.example.grpce2e.kafka

import java.util.UUID

class OrdersProducerKafkaSettings(
    val bootstrapServers: String = System.getenv("KAFKA_BOOTSTRAP_SERVERS") ?: "localhost:9092",
    val inputTopic: String = System.getenv("ORDERS_TOPIC") ?: "big-communal-orders-topic",
    val username: String? = System.getenv("KAFKA_SASL_USERNAME"),
    val password: String? = System.getenv("KAFKA_SASL_PASSWORD"),
    val securityProtocol: String = System.getenv("KAFKA_SECURITY_PROTOCOL") ?: "PLAINTEXT",
    val saslMechanism: String? = System.getenv("KAFKA_SASL_MECHANISM"),
) {
    fun createProducerConfig(): ProducerKafkaConfig =
        ProducerKafkaConfig(
            bootstrapServers = bootstrapServers,
            username = username,
            password = password,
        ).apply {
            securityProtocol = this@OrdersProducerKafkaSettings.securityProtocol
            this@OrdersProducerKafkaSettings.saslMechanism?.let { saslMechanism = it }
            clientId = "grpc-e2e-tests-${UUID.randomUUID()}"
        }
}
