package com.example.grpce2e.db

import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import java.math.BigDecimal

object OrdersTable : Table("orders") {
    val orderId = text("order_id")
    val sellerId = text("seller_id")
    val customerId = text("customer_id")
    val currency = text("currency")
    val totalAmount = decimal("total_amount", 18, 2)
    val channel = text("channel")
    val lat = double("lat")
    val lon = double("lon")

    override val primaryKey = PrimaryKey(orderId)
}

object OrderItemsTable : Table("order_items") {
    val id = long("id").autoIncrement()
    val orderId = text("order_id") references OrdersTable.orderId
    val sku = text("sku")
    val qty = integer("qty")
    val price = decimal("price", 18, 2)

    override val primaryKey = PrimaryKey(id)
}

object OrderGeoTable : Table("order_geo") {
    val orderId = text("order_id") references OrdersTable.orderId
    val region = text("region")
    val city = text("city")
    val timezone = text("timezone")
    val regionalCoef = double("regional_coef")

    override val primaryKey = PrimaryKey(orderId)
}

data class OrderSeed(
    val orderId: String,
    val sellerId: String,
    val customerId: String,
    val currency: String,
    val totalAmount: BigDecimal,
    val channel: String,
    val lat: Double,
    val lon: Double,
    val items: List<OrderItemSeed>,
    val geo: GeoSeed,
)

data class OrderItemSeed(
    val sku: String,
    val qty: Int,
    val price: BigDecimal,
)

data class GeoSeed(
    val region: String,
    val city: String,
    val timezone: String,
    val regionalCoef: Double,
)

fun insertLinkedEntitiesAsOrder(order: OrderSeed) {
    dbOrdersExec {
        OrdersTable.insert {
            it[orderId] = order.orderId
            it[sellerId] = order.sellerId
            it[customerId] = order.customerId
            it[currency] = order.currency
            it[totalAmount] = order.totalAmount
            it[channel] = order.channel
            it[lat] = order.lat
            it[lon] = order.lon
        }

        OrderGeoTable.insert {
            it[orderId] = order.orderId
            it[region] = order.geo.region
            it[city] = order.geo.city
            it[timezone] = order.geo.timezone
            it[regionalCoef] = order.geo.regionalCoef
        }

        order.items.forEach { item ->
            OrderItemsTable.insert {
                it[orderId] = order.orderId
                it[sku] = item.sku
                it[qty] = item.qty
                it[price] = item.price
            }
        }
    }
}

fun deleteOrder(orderId: String) {
    dbOrdersExec {
        OrderItemsTable.deleteWhere { OrderItemsTable.orderId eq orderId }
        OrderGeoTable.deleteWhere { OrderGeoTable.orderId eq orderId }
        OrdersTable.deleteWhere { OrdersTable.orderId eq orderId }
    }
}
