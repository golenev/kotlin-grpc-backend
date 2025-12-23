package com.example.grpce2e.tests

import com.example.grpce2e.db.SellerAggregateRepository
import com.example.grpce2e.kafka.OrdersProducerKafkaSettings
import com.example.grpce2e.kafka.ProducerKafkaService
import com.example.grpce2e.model.OrderEvent
import com.example.grpce2e.model.OrderItem
import com.example.grpce2e.util.mapper
import io.kotest.assertions.nondeterministic.eventuallyConfig
import io.kotest.assertions.withClue
import io.kotest.matchers.shouldBe
import io.kotest.matchers.nulls.shouldNotBeNull
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import kotlin.time.Duration.Companion.seconds
import io.kotest.assertions.nondeterministic.eventually

class GrpcE2eTest {
    private val producerSettings = OrdersProducerKafkaSettings()
    private lateinit var producer: ProducerKafkaService<OrderEvent>
    private val httpClient = HttpClient.newHttpClient()
    val sellerId = "SELLER-TEST"

    @AfterEach
    fun clearDb () {
        SellerAggregateRepository.deleteBySellerId(sellerId)
    }

    @Test
    fun `orders flow enriches and aggregates seller stats`() {

        sendOrders()

        val aggregate = runBlocking {
            eventually(config = positiveConfig) {
                triggerAggregation()
                SellerAggregateRepository.findBySellerId(sellerId).shouldNotBeNull()
           }
        }

        aggregate.sellerId shouldBe sellerId
        aggregate.totalOrders shouldBe 3L
        aggregate.totalItems shouldBe 6L
        aggregate.totalRevenue shouldBe BigDecimal("550.00")
        aggregate.avgCheck shouldBe BigDecimal("183.33")

    }

    private fun triggerAggregation() {
        val request = HttpRequest.newBuilder()
            .uri(URI.create("http://localhost:8081/api/analytics/seller/${sellerId}/aggregate"))
            .POST(HttpRequest.BodyPublishers.noBody())
            .build()

        val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
        withClue("HTTP ${response.statusCode()} body: ${response.body()}") {
            response.statusCode() shouldBe 200
        }
    }

    private fun sendOrders() {
        val producerConfig = producerSettings.createProducerConfig()

        producer = ProducerKafkaService(
            cfg = producerConfig,
            topic = producerSettings.inputTopic,
            mapper = mapper
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

}

val positiveConfig = eventuallyConfig {
    duration = 40.seconds
    interval = 5.seconds
}
