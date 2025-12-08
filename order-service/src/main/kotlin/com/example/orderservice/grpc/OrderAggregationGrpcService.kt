package com.example.orderservice.grpc

import com.example.order.OrderAggregationServiceGrpc
import com.example.order.SellerAggregateRequest
import com.example.order.SellerAggregateResponse
import com.example.orderservice.repository.OrderRepository
import io.grpc.Status
import io.grpc.stub.StreamObserver
import net.devh.boot.grpc.server.service.GrpcService
import org.slf4j.LoggerFactory
import java.math.BigDecimal
import java.math.RoundingMode

@GrpcService
class OrderAggregationGrpcService(
    private val orderRepository: OrderRepository,
) : OrderAggregationServiceGrpc.OrderAggregationServiceImplBase() {

    private val logger = LoggerFactory.getLogger(javaClass)

    override fun getSellerAggregate(
        request: SellerAggregateRequest,
        responseObserver: StreamObserver<SellerAggregateResponse>,
    ) {
        val aggregate = orderRepository.calculateSellerAggregate(request.sellerId)
        if (aggregate == null) {
            responseObserver.onError(
                Status.NOT_FOUND.withDescription("Seller aggregate not found for id ${request.sellerId}").asRuntimeException(),
            )
            return
        }

        val avgCheck = if (aggregate.totalOrders > 0) {
            aggregate.totalRevenue.divide(BigDecimal.valueOf(aggregate.totalOrders), 2, RoundingMode.HALF_UP)
        } else {
            BigDecimal.ZERO
        }

        val response = SellerAggregateResponse.newBuilder()
            .setSellerId(aggregate.sellerId)
            .setOrdersCount(aggregate.totalOrders)
            .setTotalItems(aggregate.totalItems)
            .setTotalAmount(aggregate.totalRevenue.toDouble())
            .setAvgCheck(avgCheck.toDouble())
            .setLastOrderAt(aggregate.lastOrderAt?.toString().orEmpty())
            .build()

        logger.info("Calculated aggregate for seller {}", aggregate.sellerId)
        responseObserver.onNext(response)
        responseObserver.onCompleted()
    }
}
