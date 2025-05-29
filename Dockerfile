# Use official OpenJDK 17 image
FROM eclipse-temurin:17-jdk-alpine

# Set working directory
WORKDIR /app

# Copy Maven build artifact
COPY target/process-order-service.jar app.jar

# Expose port
EXPOSE 8383

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
