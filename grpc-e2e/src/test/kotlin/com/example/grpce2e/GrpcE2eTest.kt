package com.example.grpce2e

import com.example.grpce2e.kafka.OrdersProducerKafkaSettings
import com.example.grpce2e.kafka.ProducerKafkaService
import com.example.grpce2e.model.OrderEvent
import com.example.grpce2e.model.OrderItem
import com.example.grpce2e.db.SellerAggregateRepository
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.junit.jupiter.api.Test
import io.kotest.assertions.timing.eventually
import io.kotest.assertions.timing.eventuallyConfig
import io.kotest.matchers.comparables.plusOrMinus
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import java.math.BigDecimal
import kotlin.time.Duration.Companion.seconds

class GrpcE2eTest {
    private val objectMapper = ObjectMapper().registerKotlinModule()
    private val producerSettings = OrdersProducerKafkaSettings()
    private lateinit var producer: ProducerKafkaService<OrderEvent>

    @Test
    fun `orders flow enriches and aggregates seller stats`() = runBlocking {
        val sellerId = "SELLER-TEST"
        try {
            sendOrders()

            eventually(positiveConfig) {
                val aggregate = SellerAggregateRepository.findBySellerId(sellerId).shouldNotBeNull()
                aggregate.sellerId shouldBe sellerId
                aggregate.totalOrders shouldBe 3L
                aggregate.totalItems shouldBe 6L
                aggregate.totalRevenue shouldBe BigDecimal("550.00")
                aggregate.avgCheck shouldBe BigDecimal("183.33").plusOrMinus(BigDecimal("0.1"))
            }
        } finally {
            SellerAggregateRepository.deleteBySellerId(sellerId)
        }
    }

    private fun sendOrders() {
        val producerConfig = producerSettings.createProducerConfig()

        producer = ProducerKafkaService(
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

    private val positiveConfig = eventuallyConfig {
        duration = 40.seconds
        interval = 5.seconds
    }
}
