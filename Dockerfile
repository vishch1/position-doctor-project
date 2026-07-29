# Stage 1: Build Stage
FROM eclipse-temurin:17-jdk AS builder

WORKDIR /app

# Copy Maven wrapper files and pom.xml for dependency caching layer
COPY .mvn/ .mvn
COPY mvnw pom.xml ./

# Ensure execution permission on Maven wrapper script
RUN chmod +x mvnw

# Download dependencies to cache them in Docker layer
RUN ./mvnw dependency:go-offline -B

# Copy backend source code
COPY src ./src

# Build the executable Spring Boot JAR file skipping test execution
RUN ./mvnw clean package -DskipTests

# Stage 2: Runtime Stage
FROM eclipse-temurin:17-jre AS runner

WORKDIR /app

# Create non-root user for security best practices
RUN groupadd -r appgroup && useradd -r -g appgroup appuser

# Copy the compiled Spring Boot executable JAR from builder stage
COPY --from=builder /app/target/*.jar app.jar

# Set permissions for non-root runtime user
RUN chown -R appuser:appgroup /app

USER appuser

# Expose default application port
EXPOSE 8080

# Run Spring Boot application
ENTRYPOINT ["java", "-jar", "app.jar"]
