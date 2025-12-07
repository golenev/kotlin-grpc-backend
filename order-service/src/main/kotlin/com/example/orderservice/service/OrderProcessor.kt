package com.example.orderservice.service

import com.example.analytics.AnalyticsServiceGrpc
import com.example.analytics.EnrichedOrder
import com.example.analytics.OrderItem
import com.example.orderservice.model.OrderMessage
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import net.devh.boot.grpc.client.inject.GrpcClient
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class OrderProcessor(
    private val geoClient: GeoClient,
    private val objectMapper: ObjectMapper,
    @GrpcClient("analytics") private val analyticsStub: AnalyticsServiceGrpc.AnalyticsServiceBlockingStub,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @KafkaListener(topics = ["big-communal-orders-topic"], groupId = "order-service-consumer")
    fun onMessage(message: String) {
        val order: OrderMessage = objectMapper.readValue(message)
        logger.info("Received order {}", order.orderId)

        val geo = geoClient.fetchGeo(order.lat, order.lon)
        val enriched = EnrichedOrder.newBuilder()
            .setOrderId(order.orderId)
            .setSellerId(order.sellerId)
            .setCustomerId(order.customerId)
            .addAllItems(order.items.map { OrderItem.newBuilder().setSku(it.sku).setQty(it.qty).setPrice(it.price).build() })
            .setTotalAmount(order.totalAmount)
            .setCurrency(order.currency)
            .setChannel(order.channel)
            .setLat(order.lat)
            .setLon(order.lon)
            .setRegion(geo.region)
            .setCity(geo.city)
            .setTimezone(geo.timezone)
            .setRegionalCoef(geo.regionalCoef)
            .build()

        analyticsStub.processOrder(enriched)
        logger.info("Sent enriched order {} to analytics", order.orderId)
    }
}
