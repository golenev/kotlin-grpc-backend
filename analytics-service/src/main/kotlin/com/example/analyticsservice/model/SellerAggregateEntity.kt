package com.example.analyticsservice.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.math.BigDecimal
import java.time.Instant

@Entity
@Table(name = "seller_aggregates")
data class SellerAggregateEntity(
    @Id
    @Column(name = "seller_id")
    val sellerId: String,

    @Column(name = "total_orders")
    var totalOrders: Long,

    @Column(name = "total_items")
    var totalItems: Long,

    @Column(name = "total_revenue")
    var totalRevenue: BigDecimal,

    @Column(name = "avg_check")
    var avgCheck: BigDecimal,

    @Column(name = "last_order_at")
    var lastOrderAt: Instant,
)
