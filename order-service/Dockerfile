FROM eclipse-temurin:21-jdk as build
WORKDIR /app
COPY . .
RUN ./gradlew :order-service:bootJar --no-daemon

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/order-service/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
