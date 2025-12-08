package com.example.grpce2e.tests

import io.restassured.RestAssured
import io.restassured.http.ContentType
import io.restassured.module.kotlin.extensions.Extract
import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import io.restassured.response.Response
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.hamcrest.Matchers
import org.junit.jupiter.api.Test

class GeoStubTest {
    private val geoServiceBaseUrl = System.getenv("GEO_STUB_BASE_URL") ?: "http://localhost:8031"

    @Test
    fun `should return geo response from stub`() {
        RestAssured.baseURI = geoServiceBaseUrl

        val response: Response = Given {
            param("lat", "55.7558")
            param("lon", "37.6176")
        } When {
            get("/geo")
        } Then {
            contentType(ContentType.JSON)
            body(
                "region", Matchers.equalTo("Москва"),
                "city", Matchers.equalTo("Москва"),
                "timezone", Matchers.equalTo("Europe/Moscow"),
                "regionalCoef", Matchers.equalTo(1.25f)
            )
        } Extract {
            response()
        }

        response.statusCode shouldBe 200
        response.contentType shouldBe ContentType.JSON.toString()

        val json = response.jsonPath()
        json.shouldNotBeNull()
        json.getString("region") shouldBe "Москва"
        json.getString("city") shouldBe "Москва"
        json.getString("timezone") shouldBe "Europe/Moscow"
        json.getFloat("regionalCoef") shouldBe 1.25f
    }
}
