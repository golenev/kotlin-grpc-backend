package com.example.grpce2e.tests

import com.example.grpce2e.db.SellerAggregateRepository
import com.example.grpce2e.kafka.OrdersProducerKafkaSettings
import com.example.grpce2e.kafka.ProducerKafkaService
import com.example.grpce2e.model.OrderEvent
import com.example.grpce2e.model.OrderItem
import com.example.grpce2e.util.mapper
import com.example.grpce2e.util.step
import io.kotest.assertions.nondeterministic.eventuallyConfig
import io.kotest.assertions.withClue
import io.kotest.matchers.shouldBe
import io.kotest.matchers.nulls.shouldNotBeNull
import io.restassured.RestAssured
import io.restassured.module.kotlin.extensions.Extract
import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import io.restassured.response.Response
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import kotlin.time.Duration.Companion.seconds
import io.kotest.assertions.nondeterministic.eventually

class GrpcE2eTest {
    private val producerSettings = OrdersProducerKafkaSettings()
    private lateinit var producer: ProducerKafkaService<OrderEvent>
    private val sellerId = "SELLER-TEST"
    private val analyticsBaseUrl = "http://localhost:8081"

    @AfterEach
    fun clearDb () {
        step("clear seller aggregate") {
            SellerAggregateRepository.deleteBySellerId(sellerId)
        }
    }

    @Test
    fun `orders flow enriches and aggregates seller stats`() {
        RestAssured.baseURI = analyticsBaseUrl

        step("send orders to kafka") {
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

        val aggregate = runBlocking {
            step("aggregate orders and wait for persistence") {
                eventually(config = positiveConfig) {
                    val response: Response = Given {
                    } When {
                        post("/api/analytics/seller/${sellerId}/aggregate")
                    } Then {
                    } Extract {
                        response()
                    }

                    withClue("HTTP ${response.statusCode()} body: ${response.body().asString()}") {
                        response.statusCode() shouldBe 200
                    }

                    SellerAggregateRepository.findBySellerId(sellerId).shouldNotBeNull()
                }
            }
        }

        step("verify seller aggregate values") {
            aggregate.sellerId shouldBe sellerId
            aggregate.totalOrders shouldBe 3L
            aggregate.totalItems shouldBe 6L
            aggregate.totalRevenue shouldBe BigDecimal("550.00")
            aggregate.avgCheck shouldBe BigDecimal("183.33")
        }
    }

}

val positiveConfig = eventuallyConfig {
    duration = 40.seconds
    interval = 5.seconds
}
