package com.example.analyticsservice.client

import com.example.analyticsservice.model.SellerAggregateDto

interface OrderAggregationClient {
    fun getSellerAggregate(sellerId: String): SellerAggregateDto
}
