# Use Java 21 runtime
FROM eclipse-temurin:21-jre-jammy

# Working directory
WORKDIR /app

# Copy built jar
COPY target/*.jar app.jar

# Gateway port
EXPOSE 8080

# Start application
ENTRYPOINT ["java", "-jar", "app.jar"]