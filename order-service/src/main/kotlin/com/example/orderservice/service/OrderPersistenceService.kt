package com.example.orderservice.service

import com.example.analytics.EnrichedOrder
import com.example.analytics.OrderItem
import com.example.orderservice.model.GeoInfo
import com.example.orderservice.model.OrderEntity
import com.example.orderservice.model.OrderGeoEntity
import com.example.orderservice.model.OrderItemEntity
import com.example.orderservice.model.OrderMessage
import com.example.orderservice.repository.OrderRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.OffsetDateTime

@Service
class OrderPersistenceService(
    private val orderRepository: OrderRepository,
) {
    @Transactional
    fun persistAndBuildEnriched(order: OrderMessage, geo: GeoInfo): EnrichedOrder {
        val orderEntity = OrderEntity(
            orderId = order.orderId,
            sellerId = order.sellerId,
            customerId = order.customerId,
            currency = order.currency,
            totalAmount = BigDecimal.valueOf(order.totalAmount),
            channel = order.channel,
            lat = order.lat,
            lon = order.lon,
            createdAt = OffsetDateTime.now(),
        )

        val items = order.items.map { item ->
            OrderItemEntity(
                sku = item.sku,
                qty = item.qty,
                price = BigDecimal.valueOf(item.price),
                order = orderEntity,
            )
        }
        orderEntity.items.addAll(items)

        val geoEntity = OrderGeoEntity(
            orderId = order.orderId,
            region = geo.region,
            city = geo.city,
            timezone = geo.timezone,
            regionalCoef = geo.regionalCoef,
            order = orderEntity,
        )
        orderEntity.geo = geoEntity

        val saved = orderRepository.save(orderEntity)

        return EnrichedOrder.newBuilder()
            .setOrderId(saved.orderId)
            .setSellerId(saved.sellerId)
            .setCustomerId(saved.customerId)
            .addAllItems(saved.items.map { OrderItem.newBuilder().setSku(it.sku).setQty(it.qty).setPrice(it.price.toDouble()).build() })
            .setTotalAmount(saved.totalAmount.toDouble())
            .setCurrency(saved.currency)
            .setChannel(saved.channel)
            .setLat(saved.lat)
            .setLon(saved.lon)
            .setRegion(saved.geo?.region ?: geo.region)
            .setCity(saved.geo?.city ?: geo.city)
            .setTimezone(saved.geo?.timezone ?: geo.timezone)
            .setRegionalCoef(saved.geo?.regionalCoef ?: geo.regionalCoef)
            .build()
    }
}
