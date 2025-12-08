package com.example.grpce2e

import com.example.order.OrderAggregationServiceGrpc
import com.example.order.SellerAggregateRequest
import io.grpc.ManagedChannelBuilder
import io.kotest.matchers.shouldBe
import io.qameta.allure.Allure
import io.qameta.allure.junit5.AllureJunit5
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.math.BigDecimal
import com.example.grpce2e.db.GeoSeed
import com.example.grpce2e.db.OrderItemSeed
import com.example.grpce2e.db.OrderSeed
import com.example.grpce2e.db.SellerAggregateRepository
import com.example.grpce2e.db.deleteOrder
import com.example.grpce2e.db.seedOrder

@ExtendWith(AllureJunit5::class)
class OrderGrpcAggregationIntegrationTest {

    private val sellerId = "seller-1"
    private val orderIds = listOf("o1", "o2", "o3")

    @AfterEach
    fun cleanUp() {
        step("Очистка данных") {
            orderIds.forEach { deleteOrder(it) }
            SellerAggregateRepository.deleteBySellerId(sellerId)

        }
    }

    @Test
    fun `order-service aggregates orders via grpc`() {
        val channel = ManagedChannelBuilder
            .forAddress("localhost", 9090)
            .usePlaintext()
            .build()

        val stub = OrderAggregationServiceGrpc.newBlockingStub(channel)

        try {
            step("Подготовка данных в БД order-service") {
                seedOrder(
                    OrderSeed(
                        orderId = orderIds[0],
                        sellerId = sellerId,
                        customerId = "customer-1",
                        currency = "RUB",
                        totalAmount = BigDecimal("100.00"),
                        channel = "WEB",
                        lat = 55.75,
                        lon = 37.61,
                        items = listOf(OrderItemSeed(sku = "SKU-100", qty = 1, price = BigDecimal("100.00"))),
                        geo = GeoSeed(
                            region = "Москва",
                            city = "Москва",
                            timezone = "Europe/Moscow",
                            regionalCoef = 1.0,
                        ),
                    ),
                )
                seedOrder(
                    OrderSeed(
                        orderId = orderIds[1],
                        sellerId = sellerId,
                        customerId = "customer-2",
                        currency = "RUB",
                        totalAmount = BigDecimal("50.00"),
                        channel = "WEB",
                        lat = 55.75,
                        lon = 37.61,
                        items = listOf(OrderItemSeed(sku = "SKU-101", qty = 1, price = BigDecimal("50.00"))),
                        geo = GeoSeed(
                            region = "Москва",
                            city = "Москва",
                            timezone = "Europe/Moscow",
                            regionalCoef = 1.0,
                        ),
                    ),
                )
                seedOrder(
                    OrderSeed(
                        orderId = orderIds[2],
                        sellerId = sellerId,
                        customerId = "customer-3",
                        currency = "RUB",
                        totalAmount = BigDecimal("25.00"),
                        channel = "WEB",
                        lat = 55.75,
                        lon = 37.61,
                        items = listOf(OrderItemSeed(sku = "SKU-102", qty = 1, price = BigDecimal("25.00"))),
                        geo = GeoSeed(
                            region = "Москва",
                            city = "Москва",
                            timezone = "Europe/Moscow",
                            regionalCoef = 1.0,
                        ),
                    ),
                )
            }

            val aggregate =
                step("Вызов gRPC ручки order-service") {
                    stub.getSellerAggregate(SellerAggregateRequest.newBuilder().setSellerId(sellerId).build())
                }

            step("Проверка агрегированного ответа") {
                aggregate.sellerId shouldBe sellerId
                aggregate.ordersCount shouldBe 3
                aggregate.totalAmount shouldBe 175.0
            }
        } finally {
            step("Закрываем канал") {
                channel.shutdownNow()
            }
        }
    }

    private fun <T> step(description: String, block: () -> T): T = Allure.step(description, block)
}
