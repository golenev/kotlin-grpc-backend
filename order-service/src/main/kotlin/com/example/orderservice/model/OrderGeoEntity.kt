package com.example.orderservice.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.MapsId
import jakarta.persistence.OneToOne
import jakarta.persistence.Table

@Entity
@Table(name = "order_geo")
data class OrderGeoEntity(
    @Id
    @Column(name = "order_id")
    val orderId: String,

    @Column(name = "region", nullable = false)
    val region: String,

    @Column(name = "city", nullable = false)
    val city: String,

    @Column(name = "timezone", nullable = false)
    val timezone: String,

    @Column(name = "regional_coef", nullable = false)
    val regionalCoef: Double,

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "order_id", referencedColumnName = "order_id")
    val order: OrderEntity,
)
