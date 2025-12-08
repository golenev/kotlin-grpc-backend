package com.example.orderservice.service

import com.example.orderservice.model.OrderMessage
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class OrderProcessor(
    private val geoClient: GeoClient,
    private val objectMapper: ObjectMapper,
    private val orderPersistenceService: OrderPersistenceService,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @KafkaListener(topics = ["big-communal-orders-topic"], groupId = "order-service-consumer")
    fun onMessage(message: String) {
        val order: OrderMessage = objectMapper.readValue(message)
        logger.info("Received order {}", order.orderId)

        val geo = geoClient.fetchGeo(order.lat, order.lon)
        orderPersistenceService.persistOrder(order, geo)
        logger.info("Persisted order {} for seller {}", order.orderId, order.sellerId)
    }
}
