package com.example.orderservice.model

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import java.math.BigDecimal
import java.time.OffsetDateTime

@Entity
@Table(name = "orders")
data class OrderEntity(
    @Id
    @Column(name = "order_id")
    val orderId: String,

    @Column(name = "seller_id", nullable = false)
    val sellerId: String,

    @Column(name = "customer_id", nullable = false)
    val customerId: String,

    @Column(name = "currency", nullable = false)
    val currency: String,

    @Column(name = "total_amount", nullable = false)
    val totalAmount: BigDecimal,

    @Column(name = "channel", nullable = false)
    val channel: String,

    @Column(name = "lat", nullable = false)
    val lat: Double,

    @Column(name = "lon", nullable = false)
    val lon: Double,

    @Column(name = "created_at", nullable = false)
    val createdAt: OffsetDateTime,

    @OneToMany(mappedBy = "order", fetch = FetchType.LAZY, cascade = [CascadeType.ALL], orphanRemoval = true)
    val items: MutableList<OrderItemEntity> = mutableListOf(),

    @OneToOne(mappedBy = "order", fetch = FetchType.LAZY, cascade = [CascadeType.ALL], orphanRemoval = true)
    var geo: OrderGeoEntity? = null,
)
