plugins {
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    kotlin("jvm")
    kotlin("plugin.spring") version "1.9.25"
    kotlin("plugin.jpa") version "1.9.25"
}

dependencies {
    implementation(project(":analytics-proto"))

    // Базовый Boot + WebFlux
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    // Вместо spring-boot-starter-kafka используем напрямую spring-kafka
    // Boot BOM под управлением плагина сам проставит версию (3.3.x)
    implementation("org.springframework.kafka:spring-kafka")

    // gRPC
    val grpcVersion: String by rootProject.extra
    implementation("io.grpc:grpc-netty-shaded:$grpcVersion")
    implementation("io.grpc:grpc-stub:$grpcVersion")

    // gRPC client starter (трогать не будем)
    implementation("net.devh:grpc-client-spring-boot-starter:3.1.0.RELEASE")

    // Persistence & migrations
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-database-postgresql")
    runtimeOnly("org.postgresql:postgresql")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

kotlin {
    jvmToolchain(21)
}
