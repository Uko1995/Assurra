# Multi-stage build for EaaS Payment Service
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app
COPY pom.xml .
COPY src ./src

# Download Maven wrapper and build
RUN chmod +x mvnw && ./mvnw clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Install necessary utilities
RUN apk add --no-cache curl

# Create non-root user
RUN addgroup -S eaas && adduser -S eaas -G eaas

# Copy jar from builder
COPY --from=builder /app/target/eaas-payment-service-*.jar app.jar

# Change ownership
RUN chown -R eaas:eaas /app

USER eaas

EXPOSE 8083

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8083/actuator/health || exit 1

ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
