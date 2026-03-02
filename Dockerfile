FROM maven:3.8.4-openjdk-17-slim AS build
WORKDIR /app
# Copy pom.xml and download dependencies (caching layer)
COPY pom.xml .
RUN mvn dependency:go-offline -B
# Copy source code and build
COPY src ./src
RUN mvn clean package -DskipTests
# Run stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
RUN addgroup -g 1001 appuser && \
    adduser -u 1001 -G appuser -s /bin/sh -D appuser
# Copy the built jar from build stage
COPY --from=build /app/target/*.jar app.jar
RUN mkdir -p logs && \
    chown -R appuser:appuser /app
# Switch to non-root user
USER appuser

# Expose the application port
EXPOSE 8080
# Health check
HEALTHCHECK --interval=30s --timeout=3s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
