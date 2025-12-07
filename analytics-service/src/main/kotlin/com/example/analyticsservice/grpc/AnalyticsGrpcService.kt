package com.example.analyticsservice.grpc

import com.example.analytics.AnalyticsServiceGrpc
import com.example.analytics.EnrichedOrder
import com.example.analytics.GetSellerAggregateRequest
import com.example.analytics.ProcessOrderResult
import com.example.analytics.SellerAggregate
import com.example.analytics.OrderItem
import com.example.analytics.OrderItem as ProtoOrderItem
import com.example.analyticsservice.model.SellerAggregateEntity
import com.example.analyticsservice.repository.SellerAggregateRepository
import io.grpc.stub.StreamObserver
import net.devh.boot.grpc.server.service.GrpcService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Instant

@GrpcService
@Service
class AnalyticsGrpcService(
    private val repository: SellerAggregateRepository,
) : AnalyticsServiceGrpc.AnalyticsServiceImplBase() {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Transactional
    override fun processOrder(request: EnrichedOrder, responseObserver: StreamObserver<ProcessOrderResult>) {
        val totalItems = request.itemsList.sumOf { it.qty.toLong() }
        val orderAmount = BigDecimal.valueOf(request.totalAmount)
        val aggregate = repository.findById(request.sellerId).orElseGet {
            SellerAggregateEntity(
                sellerId = request.sellerId,
                totalOrders = 0,
                totalItems = 0,
                totalRevenue = BigDecimal.ZERO,
                avgCheck = BigDecimal.ZERO,
                lastOrderAt = Instant.now(),
            )
        }

        aggregate.totalOrders += 1
        aggregate.totalItems += totalItems
        aggregate.totalRevenue = aggregate.totalRevenue.add(orderAmount)
        aggregate.avgCheck = if (aggregate.totalOrders > 0) {
            aggregate.totalRevenue.divide(BigDecimal.valueOf(aggregate.totalOrders), 2, RoundingMode.HALF_UP)
        } else BigDecimal.ZERO
        aggregate.lastOrderAt = Instant.now()

        repository.save(aggregate)
        logger.info("Processed order {} for seller {}", request.orderId, request.sellerId)

        responseObserver.onNext(ProcessOrderResult.newBuilder().setSuccess(true).build())
        responseObserver.onCompleted()
    }

    override fun getSellerAggregate(request: GetSellerAggregateRequest, responseObserver: StreamObserver<SellerAggregate>) {
        val aggregate = repository.findById(request.sellerId)
            .orElseThrow { IllegalArgumentException("Seller aggregate not found for id ${'$'}{request.sellerId}") }

        val proto = SellerAggregate.newBuilder()
            .setSellerId(aggregate.sellerId)
            .setTotalOrders(aggregate.totalOrders)
            .setTotalItems(aggregate.totalItems)
            .setTotalRevenue(aggregate.totalRevenue.toDouble())
            .setAvgCheck(aggregate.avgCheck.toDouble())
            .setLastOrderAt(aggregate.lastOrderAt.toString())
            .build()

        responseObserver.onNext(proto)
        responseObserver.onCompleted()
    }
}
