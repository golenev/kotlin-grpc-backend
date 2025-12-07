pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven("https://repo.spring.io/release")
    }
    plugins {
        kotlin("jvm") version "1.9.25"
        id("org.springframework.boot") version "3.3.4"
        id("io.spring.dependency-management") version "1.1.6"
        id("com.google.protobuf") version "0.9.4"
    }
    resolutionStrategy {
        eachPlugin {
            when (requested.id.id) {
                "org.springframework.boot" -> useModule("org.springframework.boot:spring-boot-gradle-plugin:${requested.version}")
                "io.spring.dependency-management" -> useModule("io.spring.gradle:dependency-management-plugin:${requested.version}")
                "com.google.protobuf" -> useModule("com.google.protobuf:protobuf-gradle-plugin:${requested.version}")
            }
        }
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

rootProject.name = "kotlin-grpc-backend"

include("analytics-proto")
include("order-service")
include("analytics-service")
include("grpc-e2e")
include("wiremock-stubs")
