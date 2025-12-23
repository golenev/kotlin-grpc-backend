plugins {
    kotlin("jvm")
}

dependencies {
    val grpcVersion: String by rootProject.extra
    val allureVersion = "2.27.0"

    implementation(project(":analytics-proto"))
    testImplementation(kotlin("test"))
    testImplementation("io.kotest:kotest-assertions-core:5.9.1")
    testImplementation("io.rest-assured:kotlin-extensions:5.4.0")
    testImplementation("io.rest-assured:rest-assured:5.4.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
    testImplementation("org.apache.kafka:kafka-clients:3.7.1")
    testImplementation("org.postgresql:postgresql:42.7.4")
    testImplementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.17.1")
    testImplementation("org.jetbrains.exposed:exposed-core:0.55.0")
    testImplementation("org.jetbrains.exposed:exposed-dao:0.55.0")
    testImplementation("org.jetbrains.exposed:exposed-jdbc:0.55.0")
    testImplementation("org.jetbrains.exposed:exposed-java-time:0.55.0")
    testImplementation("io.grpc:grpc-netty-shaded:$grpcVersion")
    testImplementation("io.qameta.allure:allure-junit5:$allureVersion")
    testImplementation("ch.qos.logback:logback-classic:1.5.6")
}

kotlin {
    jvmToolchain(21)
}

tasks.test {
    testLogging {
        showStandardStreams = true
    }
}
