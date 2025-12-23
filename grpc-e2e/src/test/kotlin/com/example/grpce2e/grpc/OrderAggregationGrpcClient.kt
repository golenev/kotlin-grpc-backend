package com.example.grpce2e.grpc

import com.example.order.OrderAggregationServiceGrpc
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder

object OrderAggregationGrpcClient {

    val managedChannel: ManagedChannel by lazy {
        createChannel()
    }

    val stub: OrderAggregationServiceGrpc.OrderAggregationServiceBlockingStub
        get() = OrderAggregationServiceGrpc.newBlockingStub(managedChannel)

    private fun createChannel(): ManagedChannel {
        val target = System.getenv("ORDER_GRPC_TARGET") ?: "localhost:9090"
        return ManagedChannelBuilder.forTarget(target)
            .usePlaintext()
            .intercept(LoggingGrpcInterceptor())
            .build()
    }
}
