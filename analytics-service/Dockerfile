FROM eclipse-temurin:21-jdk as build
WORKDIR /app
COPY .. .
RUN ./gradlew :analytics-service:bootJar --no-daemon

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/analytics-service/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
