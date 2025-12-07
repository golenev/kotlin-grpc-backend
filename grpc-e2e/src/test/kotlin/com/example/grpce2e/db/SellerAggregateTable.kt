package com.example.grpce2e.db

import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.select
import java.math.BigDecimal

object SellerAggregatesTable : Table("seller_aggregates") {
    val sellerId = text("seller_id")
    val totalOrders = long("total_orders")
    val totalItems = long("total_items")
    val totalRevenue = decimal("total_revenue", 19, 2)
    val avgCheck = decimal("avg_check", 19, 2)

    override val primaryKey = PrimaryKey(sellerId)
}

data class SellerAggregateRow(
    val sellerId: String,
    val totalOrders: Long,
    val totalItems: Long,
    val totalRevenue: BigDecimal,
    val avgCheck: BigDecimal,
)

fun mapToSellerAggregate(row: ResultRow): SellerAggregateRow = SellerAggregateRow(
    sellerId = row[SellerAggregatesTable.sellerId],
    totalOrders = row[SellerAggregatesTable.totalOrders],
    totalItems = row[SellerAggregatesTable.totalItems],
    totalRevenue = row[SellerAggregatesTable.totalRevenue],
    avgCheck = row[SellerAggregatesTable.avgCheck],
)

object SellerAggregateRepository {

    fun findBySellerId(sellerId: String): SellerAggregateRow? = dbAnalyticsExec {
        SellerAggregatesTable.select { SellerAggregatesTable.sellerId eq sellerId }
            .limit(1)
            .firstOrNull()
            ?.let { mapToSellerAggregate(it) }
    }

    fun deleteBySellerId(sellerId: String) = dbAnalyticsExec {
        SellerAggregatesTable.deleteWhere { SellerAggregatesTable.sellerId eq sellerId }
    }
}
