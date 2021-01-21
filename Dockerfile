FROM openjdk:11-jre
COPY ktor-starter.jar /app/ktor-starter.jar
EXPOSE 8080
CMD ["java", "-jar", "/app/ktor-starter.jar"]
