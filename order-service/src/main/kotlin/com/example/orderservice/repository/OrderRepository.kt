package com.example.orderservice.repository

import com.example.orderservice.model.OrderEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.math.BigDecimal
import java.time.OffsetDateTime

interface OrderRepository : JpaRepository<OrderEntity, String> {

    @Query(
        "SELECT o.sellerId as sellerId, COUNT(DISTINCT o) as totalOrders, COALESCE(SUM(oi.qty), 0) as totalItems, " +
            "COALESCE(SUM(o.totalAmount), 0) as totalRevenue, MAX(o.createdAt) as lastOrderAt " +
            "FROM OrderEntity o LEFT JOIN o.items oi WHERE o.sellerId = :sellerId GROUP BY o.sellerId",
    )
    fun calculateSellerAggregate(@Param("sellerId") sellerId: String): SellerAggregateProjection?
}

interface SellerAggregateProjection {
    val sellerId: String
    val totalOrders: Long
    val totalItems: Long
    val totalRevenue: BigDecimal
    val lastOrderAt: OffsetDateTime?
}
