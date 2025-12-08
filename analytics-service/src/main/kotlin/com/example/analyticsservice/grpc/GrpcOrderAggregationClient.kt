package com.example.analyticsservice.grpc

import com.example.analyticsservice.client.OrderAggregationClient
import com.example.analyticsservice.model.SellerAggregateDto
import com.example.analyticsservice.model.toDto
import com.example.order.OrderAggregationServiceGrpc
import com.example.order.SellerAggregateRequest
import net.devh.boot.grpc.client.inject.GrpcClient
import org.springframework.stereotype.Component

@Component
class GrpcOrderAggregationClient(
    @GrpcClient("orderAggregation") private val stub: OrderAggregationServiceGrpc.OrderAggregationServiceBlockingStub,
) : OrderAggregationClient {
    override fun getSellerAggregate(sellerId: String): SellerAggregateDto {
        val request = SellerAggregateRequest.newBuilder().setSellerId(sellerId).build()
        return stub.getSellerAggregate(request).toDto()
    }
}
