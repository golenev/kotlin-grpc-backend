package com.example.analyticsservice.grpc

import io.grpc.CallOptions
import io.grpc.Channel
import io.grpc.ClientCall
import io.grpc.ClientInterceptor
import io.grpc.ForwardingClientCall
import io.grpc.ForwardingClientCallListener
import io.grpc.Metadata
import io.grpc.MethodDescriptor
import io.grpc.Status
import net.devh.boot.grpc.client.interceptor.GrpcGlobalClientInterceptor
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Клиентский gRPC-интерцептор для логирования вызовов.
 *
 * Поведение:
 * - При отправке сообщения (sendMessage) логирует имя RPC-метода и строковое
 *   представление запроса.
 * - При получении ответа (onMessage) пишет строковое представление ответа в TRACE.
 * - При завершении вызова (onClose) логирует ошибку, если статус не OK.
 *
 * Интерцептор оборачивает ClientCall и его Listener, поэтому логирование
 * выполняется прозрачно для клиента. Подходит для диагностики и отладки,
 * но в продакшне требует осторожности (возможны чувствительные данные
 * и большой объём логов).
 */
@GrpcGlobalClientInterceptor
class LoggingGrpcInterceptor : ClientInterceptor {

    private val log: Logger = LoggerFactory.getLogger(LoggingGrpcInterceptor::class.java)

    override fun <ReqT, RespT> interceptCall(
        method: MethodDescriptor<ReqT, RespT>,
        callOptions: CallOptions,
        next: Channel,
    ): ClientCall<ReqT, RespT> {
        return object : ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(
            next.newCall(method, callOptions),
        ) {

            private val methodName = method.bareMethodName

            override fun sendMessage(message: ReqT) {
                val requestMsg = message.toString()

                log.info(
                    """
                    gRPC method: {} is called
                    Message -->
                    {}
                    """.trimIndent(),
                    methodName,
                    requestMsg,
                )

                super.sendMessage(message)
            }

            override fun start(responseListener: Listener<RespT>, headers: Metadata) {
                super.start(
                    object :
                        ForwardingClientCallListener.SimpleForwardingClientCallListener<RespT>(
                            responseListener,
                        ) {

                        override fun onMessage(message: RespT) {
                            log.trace(message.toString())
                            super.onMessage(message)
                        }

                        override fun onClose(status: Status, trailers: Metadata) {
                            if (!status.isOk) {
                                log.error("gRPC call {} failed: {}", methodName, status)
                            }

                            super.onClose(status, trailers)
                        }
                    },
                    headers,
                )
            }
        }
    }
}
