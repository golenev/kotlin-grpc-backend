package com.example.grpce2e

import com.example.analytics.AnalyticsServiceGrpc
import com.example.analytics.EnrichedOrder
import com.example.analytics.GetSellerAggregateRequest
import com.example.analytics.ProcessOrderResult
import com.example.grpce2e.db.GeoSeed
import com.example.grpce2e.db.OrderItemSeed
import com.example.grpce2e.db.OrderSeed
import com.example.grpce2e.db.deleteOrder
import com.example.grpce2e.db.seedOrder
import io.grpc.ManagedChannelBuilder
import io.kotest.matchers.shouldBe
import io.qameta.allure.Allure
import io.qameta.allure.junit5.AllureJunit5
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.math.BigDecimal

@ExtendWith(AllureJunit5::class)
class OrderServiceGrpcIntegrationTest {

    private val sellerId = "seller-1"
    private val orderIds = listOf("o1", "o2", "o3")

    @AfterEach
    fun tearDown() {
        orderIds.forEach { deleteOrder(it) }
    }

    @Test
    fun `order service aggregates seller orders via grpc`() {
        val target = step("Определяем адрес gRPC-эндпоинта, куда будем подключаться") {
            System.getenv("ANALYTICS_GRPC_TARGET") ?: "localhost:9091"
        }

        val channel = step("Открываем небезопасный (plaintext) gRPC-канал до Analytics Service по адресу $target") {
            ManagedChannelBuilder.forTarget(target).usePlaintext().build()
        }

        val stub = step("Создаем blocking stub — клиентский объект, через который будем делать gRPC-вызовы") {
            AnalyticsServiceGrpc.newBlockingStub(channel)
        }

        val amounts = listOf(100.0, 50.0, 25.0)

        val orders = step("Подготавливаем в базе Orders три заказа одного продавца $sellerId") {
            seedOrder(
                OrderSeed(
                    orderId = orderIds[0],
                    sellerId = sellerId,
                    customerId = "customer-g1",
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
                    customerId = "customer-g2",
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
                    customerId = "customer-g3",
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
            orderIds.mapIndexed { index, orderId ->
                EnrichedOrder.newBuilder()
                    .setOrderId(orderId)
                    .setSellerId(sellerId)
                    .setCustomerId("customer-g${index + 1}")
                      .addAllItems(
                        listOf(
                            com.example.analytics.OrderItem.newBuilder()
                                .setSku("SKU-${100 + index}")
                                .setQty(1)
                                .setPrice(amounts[index])
                                .build(),
                        ),
                    )
                      .setTotalAmount(amounts[index])
                    .setCurrency("RUB")
                    .setChannel("WEB")
                    .setLat(55.75)
                    .setLon(37.61)
                    .setRegion("Москва")
                    .setCity("Москва")
                    .setTimezone("Europe/Moscow")
                    .setRegionalCoef(1.0)
                    .build()
            }
        }

        step("Отправляем обогащенные заказы напрямую в Analytics Service через processOrder") {
            orders.forEach { order ->
                val result: ProcessOrderResult = stub.processOrder(order)
                result.success shouldBe true
            }
        }

        try {
            val aggregate = step("Дергаем метод getSellerAggregate — Order Service агрегирует заказы продавца") {
                stub.getSellerAggregate(
                    GetSellerAggregateRequest.newBuilder().setSellerId(sellerId).build(),
                )
            }

            step("Проверяем, что агрегат собран из трех заказов и суммы корректны") {
                aggregate.sellerId shouldBe sellerId
                aggregate.totalOrders shouldBe 3
                aggregate.totalItems shouldBe 3
                aggregate.totalRevenue shouldBe 175.0
            }
        } finally {
            step("Аккуратно закрываем gRPC-канал, чтобы не оставлять висящие подключения") {
                channel.shutdownNow()
            }
        }
    }

    private fun <T> step(description: String, block: () -> T): T = Allure.step(description, block)
}
