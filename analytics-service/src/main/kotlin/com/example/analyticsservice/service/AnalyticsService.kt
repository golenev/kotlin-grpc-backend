package com.example.analyticsservice.service

import com.example.analyticsservice.client.OrderAggregationClient
import com.example.analyticsservice.model.SellerAggregateDto
import com.example.analyticsservice.model.toEntity
import com.example.analyticsservice.repository.SellerAggregateRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AnalyticsService(
    private val orderAggregationClient: OrderAggregationClient,
    private val repository: SellerAggregateRepository,
) {

    fun fetchSellerAggregate(sellerId: String): SellerAggregateDto {
        return orderAggregationClient.getSellerAggregate(sellerId)
    }

    @Transactional
    fun fetchAndPersistSellerAggregate(sellerId: String): SellerAggregateDto {
        val aggregate = orderAggregationClient.getSellerAggregate(sellerId)
        repository.save(aggregate.toEntity())
        return aggregate
    }
}
