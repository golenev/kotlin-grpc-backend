

plugins {
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    kotlin("jvm")
    kotlin("plugin.spring") version "1.9.25"
}

dependencies {
    implementation(project(":analytics-proto"))

    implementation(platform("org.springframework.boot:spring-boot-dependencies:3.3.5"))

    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-kafka:3.3.5")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    val grpcVersion: String by rootProject.extra
    implementation("io.grpc:grpc-netty-shaded:$grpcVersion")
    implementation("io.grpc:grpc-stub:$grpcVersion")

    implementation("net.devh:grpc-client-spring-boot-starter:3.1.0.RELEASE")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

kotlin {
    jvmToolchain(21)
}
