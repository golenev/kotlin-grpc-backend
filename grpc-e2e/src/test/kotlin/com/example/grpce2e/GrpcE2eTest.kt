package com.example.grpce2e

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertTimeout
import java.math.BigDecimal
import java.sql.DriverManager
import java.time.Duration
import java.util.Properties

class GrpcE2eTest {
    private val objectMapper = ObjectMapper().registerKotlinModule()

    @Test
    fun `orders flow enriches and aggregates seller stats`() {
        sendOrders()

        assertTimeout(Duration.ofSeconds(60)) {
            eventually {
                val aggregate = fetchAggregate()
                assertEquals(3L, aggregate.totalOrders)
                assertEquals(6L, aggregate.totalItems)
                assertEquals(BigDecimal("550.00"), aggregate.totalRevenue)
                assertTrue(aggregate.avgCheck.subtract(BigDecimal("183.33")).abs() < BigDecimal("0.1"))
            }
        }
    }

    private fun sendOrders() {
        val props = Properties()
        props[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] = System.getenv("KAFKA_BOOTSTRAP_SERVERS") ?: "localhost:9092"
        props[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java
        props[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java
        val producer = KafkaProducer<String, String>(props)

        val orders = listOf(
            mapOf(
                "orderId" to "ORD-1",
                "sellerId" to "SELLER-TEST",
                "customerId" to "CUST-1",
                "items" to listOf(mapOf("sku" to "SKU-1", "qty" to 1, "price" to 100.0)),
                "currency" to "RUB",
                "totalAmount" to 100.0,
                "lat" to 55.0,
                "lon" to 37.0,
                "channel" to "WEB"
            ),
            mapOf(
                "orderId" to "ORD-2",
                "sellerId" to "SELLER-TEST",
                "customerId" to "CUST-1",
                "items" to listOf(mapOf("sku" to "SKU-2", "qty" to 2, "price" to 150.0)),
                "currency" to "RUB",
                "totalAmount" to 300.0,
                "lat" to 55.0,
                "lon" to 37.0,
                "channel" to "WEB"
            ),
            mapOf(
                "orderId" to "ORD-3",
                "sellerId" to "SELLER-TEST",
                "customerId" to "CUST-1",
                "items" to listOf(mapOf("sku" to "SKU-3", "qty" to 3, "price" to 50.0)),
                "currency" to "RUB",
                "totalAmount" to 150.0,
                "lat" to 55.0,
                "lon" to 37.0,
                "channel" to "WEB"
            )
        )

        orders.forEach {
            val json = objectMapper.writeValueAsString(it)
            producer.send(ProducerRecord("big-communal-orders-topic", it["orderId"].toString(), json))
        }
        producer.flush()
        producer.close()
    }

    private fun fetchAggregate(): SellerAggregateRow {
        val url = System.getenv("ANALYTICS_JDBC_URL") ?: "jdbc:postgresql://localhost:5433/analytics"
        val user = System.getenv("ANALYTICS_DB_USER") ?: "analytics"
        val password = System.getenv("ANALYTICS_DB_PASSWORD") ?: "analytics"
        DriverManager.getConnection(url, user, password).use { conn ->
            conn.createStatement().use { stmt ->
                val rs = stmt.executeQuery("SELECT seller_id, total_orders, total_items, total_revenue, avg_check FROM seller_aggregates WHERE seller_id='SELLER-TEST'")
                if (rs.next()) {
                    return SellerAggregateRow(
                        sellerId = rs.getString("seller_id"),
                        totalOrders = rs.getLong("total_orders"),
                        totalItems = rs.getLong("total_items"),
                        totalRevenue = rs.getBigDecimal("total_revenue"),
                        avgCheck = rs.getBigDecimal("avg_check"),
                    )
                }
            }
        }
        throw IllegalStateException("Aggregate not ready")
    }

    private fun eventually(block: () -> Unit) {
        val deadline = System.currentTimeMillis() + 60000
        var lastError: Throwable? = null
        while (System.currentTimeMillis() < deadline) {
            try {
                block()
                return
            } catch (ex: Exception) {
                lastError = ex
                Thread.sleep(2000)
            }
        }
        throw lastError ?: IllegalStateException("Condition not met in time")
    }
}

data class SellerAggregateRow(
    val sellerId: String,
    val totalOrders: Long,
    val totalItems: Long,
    val totalRevenue: BigDecimal,
    val avgCheck: BigDecimal,
)
