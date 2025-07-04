FROM openjdk:17-jdk-slim
WORKDIR /payment-service
COPY build/libs/*.jar payment-service.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "./payment-service.jar"]