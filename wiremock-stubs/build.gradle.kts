plugins {
    kotlin("jvm")
}

dependencies {
    testImplementation(kotlin("test"))
    testImplementation("io.kotest:kotest-assertions-core:5.9.1")
    testImplementation("io.rest-assured:kotlin-extensions:5.4.0")
    testImplementation("io.rest-assured:rest-assured:5.4.0")
}

sourceSets {
    test {
        resources.srcDir("stubs")
    }
}
