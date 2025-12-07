package com.example.orderservice.model

data class OrderItemMessage(
    val sku: String,
    val qty: Int,
    val price: Double,
)

data class OrderMessage(
    val orderId: String,
    val sellerId: String,
    val customerId: String,
    val items: List<OrderItemMessage>,
    val currency: String,
    val totalAmount: Double,
    val lat: Double,
    val lon: Double,
    val channel: String,
)

data class GeoInfo(
    val region: String,
    val city: String,
    val timezone: String,
    val regionalCoef: Double,
)
