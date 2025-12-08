package com.example.analyticsservice.controller

import com.example.analyticsservice.model.SellerAggregateDto
import com.example.analyticsservice.service.AnalyticsService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/analytics")
class AnalyticsController(
    private val analyticsService: AnalyticsService,
) {

    @GetMapping("/seller/{sellerId}/aggregate")
    fun getSellerAggregate(@PathVariable sellerId: String): SellerAggregateDto {
        return analyticsService.fetchSellerAggregate(sellerId)
    }

    @PostMapping("/seller/{sellerId}/aggregate")
    fun fetchAndPersist(@PathVariable sellerId: String): SellerAggregateDto {
        return analyticsService.fetchAndPersistSellerAggregate(sellerId)
    }
}
