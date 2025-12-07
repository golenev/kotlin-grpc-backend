plugins {
    kotlin("jvm")
}

dependencies {
    implementation(project(":analytics-proto"))
    testImplementation(kotlin("test"))
    testImplementation("org.apache.kafka:kafka-clients:3.7.1")
    testImplementation("org.postgresql:postgresql:42.7.4")
    testImplementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.17.1")
}

kotlin {
    jvmToolchain(21)
}
