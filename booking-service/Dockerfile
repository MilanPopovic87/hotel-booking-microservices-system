# Use Java 21 runtime
FROM eclipse-temurin:21-jre-jammy

# Working directory
WORKDIR /app

# Copy built jar
COPY target/*.jar app.jar

# Expose port
EXPOSE 8082

# Start application
ENTRYPOINT ["java", "-jar", "app.jar"]