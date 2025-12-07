plugins {
    kotlin("jvm")
}

dependencies {
    testImplementation(kotlin("test"))
}

sourceSets {
    test {
        resources.srcDir("stubs")
    }
}
