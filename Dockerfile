FROM openjdk:11-jre
COPY stepper-*.jar /app/stepper.jar
EXPOSE 8080
CMD ["java", "-jar", "/app/stepper.jar"]
