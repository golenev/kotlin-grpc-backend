import com.google.protobuf.gradle.id

plugins {
    `java-library`
    id("com.google.protobuf")
}

dependencies {
    val grpcVersion: String by rootProject.extra
    api("io.grpc:grpc-protobuf:$grpcVersion")
    api("io.grpc:grpc-stub:$grpcVersion")
    api("io.grpc:grpc-netty-shaded:$grpcVersion")
    api("com.google.protobuf:protobuf-java:3.25.5")
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
