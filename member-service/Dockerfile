FROM openjdk:17-jdk-slim
WORKDIR /member-service
COPY build/libs/*.jar member-service.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "./member-service.jar"]