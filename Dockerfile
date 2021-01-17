FROM openjdk:11-jre
COPY ./build/libs/recommendations.jar /app/recommendations.jar
EXPOSE 8080
CMD ["java", "-jar", "/app/recommendations.jar"]
