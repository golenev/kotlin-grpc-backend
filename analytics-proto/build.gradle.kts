import com.google.protobuf.gradle.id

plugins {
    java
    id("com.google.protobuf")
}

dependencies {
    val grpcVersion: String by rootProject.extra
    implementation("io.grpc:grpc-protobuf:$grpcVersion")
    implementation("io.grpc:grpc-stub:$grpcVersion")
    implementation("io.grpc:grpc-netty-shaded:$grpcVersion")
    compileOnly("javax.annotation:javax.annotation-api:1.3.2")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:3.25.5"
    }
    plugins {
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:${rootProject.extra["grpcVersion"]}"
        }
    }
    generateProtoTasks {
        all().forEach { task ->
            task.plugins {
                id("grpc")
            }
        }
    }
}

sourceSets {
    main {
        java.srcDirs("build/generated/source/proto/main/java", "build/generated/source/proto/main/grpc")
    }
}
