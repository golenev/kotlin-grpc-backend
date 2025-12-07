FROM eclipse-temurin:21-jdk as build
WORKDIR /app
COPY gradlew gradlew
COPY gradle gradle
COPY settings.gradle.kts settings.gradle.kts
COPY build.gradle.kts build.gradle.kts
COPY analytics-proto analytics-proto
COPY order-service order-service
RUN chmod +x gradlew
RUN ./gradlew :order-service:bootJar --no-daemon

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/order-service/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
