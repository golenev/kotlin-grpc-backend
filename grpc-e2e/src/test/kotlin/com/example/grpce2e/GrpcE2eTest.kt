package com.example.grpce2e

import com.example.grpce2e.kafka.OrdersProducerKafkaSettings
import com.example.grpce2e.kafka.ProducerKafkaService
import com.example.grpce2e.model.OrderEvent
import com.example.grpce2e.model.OrderItem
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertTimeout
import java.math.BigDecimal
import java.sql.DriverManager
import java.time.Duration

class GrpcE2eTest {
    private val objectMapper = ObjectMapper().registerKotlinModule()
    private val producerSettings = OrdersProducerKafkaSettings()
    private lateinit var producer: ProducerKafkaService<OrderEvent>

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
        val producerConfig = producerSettings.createProducerConfig()

      producer =  ProducerKafkaService(
            cfg = producerConfig,
            topic = producerSettings.inputTopic,
            mapper = objectMapper
        )

        producer.use { producer ->
            val orders = listOf(
                OrderEvent(
                    orderId = "ORD-1",
                    sellerId = "SELLER-TEST",
                    customerId = "CUST-1",
                    items = listOf(OrderItem(sku = "SKU-1", qty = 1, price = BigDecimal("100.00"))),
                    currency = "RUB",
                    totalAmount = BigDecimal("100.00"),
                    lat = 55.0,
                    lon = 37.0,
                    channel = "WEB",
                ),
                OrderEvent(
                    orderId = "ORD-2",
                    sellerId = "SELLER-TEST",
                    customerId = "CUST-1",
                    items = listOf(OrderItem(sku = "SKU-2", qty = 2, price = BigDecimal("150.00"))),
                    currency = "RUB",
                    totalAmount = BigDecimal("300.00"),
                    lat = 55.0,
                    lon = 37.0,
                    channel = "WEB",
                ),
                OrderEvent(
                    orderId = "ORD-3",
                    sellerId = "SELLER-TEST",
                    customerId = "CUST-1",
                    items = listOf(OrderItem(sku = "SKU-3", qty = 3, price = BigDecimal("50.00"))),
                    currency = "RUB",
                    totalAmount = BigDecimal("150.00"),
                    lat = 55.0,
                    lon = 37.0,
                    channel = "WEB",
                ),
            )

            orders.forEach { order ->
                producer.send(order.orderId, order)
            }
        }
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
