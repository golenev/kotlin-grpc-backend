package com.example.orderservice.service

import com.example.orderservice.model.GeoInfo
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient

@Component
class GeoClient(
    @Value("\${geo.base-url}") private val baseUrl: String,
    builder: WebClient.Builder,
) {
    private val client: WebClient = builder.baseUrl(baseUrl).build()

    fun fetchGeo(lat: Double, lon: Double): GeoInfo {
        return client.get()
            .uri { uriBuilder -> uriBuilder.path("/geo").queryParam("lat", lat).queryParam("lon", lon).build() }
            .retrieve()
            .bodyToMono(GeoInfo::class.java)
            .block() ?: throw IllegalStateException("Geo service returned empty body")
    }
}
