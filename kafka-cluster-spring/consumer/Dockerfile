# Multi-stage build. Build context must be the repository root.
FROM gradle:9.6.1-jdk21 AS build
WORKDIR /workspace
COPY . .
RUN gradle :kafka-cluster-spring:consumer:bootJar --no-daemon

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/kafka-cluster-spring/consumer/build/libs/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]

