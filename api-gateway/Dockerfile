FROM openjdk:17-jdk-slim
WORKDIR /api-gateway

COPY build/libs/*.jar api-gateway.jar

EXPOSE 8080

ENTRYPOINT ["java", "-Dspring.profiles.active=docker", "-jar", "api-gateway.jar"]