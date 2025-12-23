package com.example.grpce2e.util

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.grpc.ManagedChannel
import io.qameta.allure.Allure

fun <T> step(description: String, block: () -> T): T = Allure.step(description, block)

inline fun <T> ManagedChannel.useGrpc(block: (ManagedChannel) -> T): T {
    try {
        return block(this)
    } finally {
        this.shutdownNow()
    }
}

val mapper: ObjectMapper = jacksonObjectMapper()
    .findAndRegisterModules()
    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    .configure(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES, true)//если в data class есть поле, которого нет в JSON → ошибка
    .setSerializationInclusion(JsonInclude.Include.NON_NULL)
