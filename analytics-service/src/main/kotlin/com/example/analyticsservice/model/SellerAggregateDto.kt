package com.example.analyticsservice.model

import com.example.order.SellerAggregateResponse
import java.math.BigDecimal
import java.time.Instant
import java.time.OffsetDateTime
import java.time.format.DateTimeParseException

data class SellerAggregateDto(
    val sellerId: String,
    val totalOrders: Long,
    val totalItems: Long,
    val totalRevenue: BigDecimal,
    val avgCheck: BigDecimal,
    val lastOrderAt: Instant?,
)

fun SellerAggregateDto.toEntity(): SellerAggregateEntity = SellerAggregateEntity(
    sellerId = sellerId,
    totalOrders = totalOrders,
    totalItems = totalItems,
    totalRevenue = totalRevenue,
    avgCheck = avgCheck,
    lastOrderAt = lastOrderAt ?: Instant.EPOCH,
)

fun SellerAggregateResponse.toDto(): SellerAggregateDto {
    val lastOrderInstant = parseInstantSafely(lastOrderAt)
    return SellerAggregateDto(
        sellerId = sellerId,
        totalOrders = ordersCount,
        totalItems = totalItems,
        totalRevenue = BigDecimal.valueOf(totalAmount),
        avgCheck = BigDecimal.valueOf(avgCheck),
        lastOrderAt = lastOrderInstant,
    )
}

private fun parseInstantSafely(value: String): Instant? = try {
    if (value.isBlank()) null else OffsetDateTime.parse(value).toInstant()
} catch (ex: DateTimeParseException) {
    null
}
