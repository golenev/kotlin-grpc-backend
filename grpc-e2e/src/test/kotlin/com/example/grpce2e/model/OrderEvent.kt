package com.example.grpce2e.model

import java.math.BigDecimal

data class OrderItem(
    val sku: String,
    val qty: Int,
    val price: BigDecimal,
)

data class OrderEvent(
    val orderId: String,
    val sellerId: String,
    val customerId: String,
    val items: List<OrderItem>,
    val currency: String,
    val totalAmount: BigDecimal,
    val lat: Double,
    val lon: Double,
    val channel: String,
)
