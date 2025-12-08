package com.example.grpce2e

import com.example.analytics.AnalyticsServiceGrpc
import com.example.analytics.EnrichedOrder
import com.example.analytics.GetSellerAggregateRequest
import com.example.analytics.OrderItem
import com.example.grpce2e.db.GeoSeed
import com.example.grpce2e.db.OrderItemSeed
import com.example.grpce2e.db.OrderSeed
import com.example.grpce2e.db.SellerAggregateRepository
import com.example.grpce2e.db.deleteOrder
import com.example.grpce2e.db.seedOrder
import io.grpc.ManagedChannelBuilder
import io.kotest.matchers.shouldBe
import io.qameta.allure.Allure
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class AnalyticsGrpcIntegrationTest {

    private val sellerId = "SELLER-GRPC"
    private val orderId = "ORD-GRPC-1"

    @BeforeEach
    fun setUp() {
        seedOrder(
            OrderSeed(
                orderId = orderId,
                sellerId = sellerId,
                customerId = "CUSTOMER-GRPC",
                currency = "RUB",
                totalAmount = BigDecimal("123.46"),
                channel = "WEB",
                lat = 55.75,
                lon = 37.61,
                items = listOf(
                    OrderItemSeed(sku = "SKU-1", qty = 1, price = BigDecimal("45.00")),
                    OrderItemSeed(sku = "SKU-2", qty = 2, price = BigDecimal("39.23")),
                ),
                geo = GeoSeed(
                    region = "Москва",
                    city = "Москва",
                    timezone = "Europe/Moscow",
                    regionalCoef = 1.25,
                ),
            ),
        )
    }

    @AfterEach
    fun tearDown() {
        deleteOrder(orderId)
        SellerAggregateRepository.deleteBySellerId(sellerId)
    }

    @Test
    fun `analytics aggregates orders via grpc call`() {
        val target = step("Определяем адрес gRPC Analytics Service, куда будем подключаться") {
            System.getenv("ANALYTICS_GRPC_TARGET") ?: "localhost:9091"
        }

        val channel = step("Открываем небезопасный (plaintext) gRPC-канал до Analytics Service по адресу $target") {
            ManagedChannelBuilder.forTarget(target).usePlaintext().build()
        }

        val stub = step("Создаем blocking stub — клиентский объект, через который будем делать gRPC-вызовы") {
            AnalyticsServiceGrpc.newBlockingStub(channel)
        }

        val request = step("Собираем gRPC-запрос EnrichedOrder — передаем заказ и гео, как будто заказ пришел в Analytics") {
            EnrichedOrder.newBuilder()
                .setOrderId(orderId)
                .setSellerId(sellerId)
                .setCustomerId("CUSTOMER-GRPC")
                .addAllItems(
                    listOf(
                        OrderItem.newBuilder().setSku("SKU-1").setQty(1).setPrice(45.0).build(),
                        OrderItem.newBuilder().setSku("SKU-2").setQty(2).setPrice(39.23).build(),
                    ),
                )
                .setTotalAmount(123.46)
                .setCurrency("RUB")
                .setChannel("WEB")
                .setLat(55.75)
                .setLon(37.61)
                .setRegion("Москва")
                .setCity("Москва")
                .setTimezone("Europe/Moscow")
                .setRegionalCoef(1.25)
                .build()
        }

        try {
            step("Шлем заказ в Analytics по публичному методу processOrder — сервис дальше сам вызовет Order Service") {
                stub.processOrder(request)
            }

            val aggregate = step("Дергаем метод getSellerAggregate — Analytics читает все заказы продавца и отдает агрегат") {
                stub.getSellerAggregate(
                    GetSellerAggregateRequest.newBuilder().setSellerId(sellerId).build(),
                )
            }

            step("Проверяем, что пришел один агрегированный ответ с ожидаемыми суммами и метаданными") {
                aggregate.sellerId shouldBe sellerId
                aggregate.totalOrders shouldBe 1
                aggregate.totalItems shouldBe 3
                aggregate.totalRevenue shouldBe 123.46
            }
        } finally {
            step("Аккуратно закрываем gRPC-канал, чтобы не оставлять висящие подключения") {
                channel.shutdownNow()
            }
        }
    }

    private fun <T> step(description: String, block: () -> T): T = Allure.step(description, block)
}
