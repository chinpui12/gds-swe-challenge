# Stage 1: Build the application
FROM eclipse-temurin:25-jdk-alpine AS builder

WORKDIR /src
COPY pom.xml .
COPY src ./src

# Install Maven manually since standard maven images for Java 25 might not be available yet
RUN apk add --no-cache maven

# Build the application (skip tests to speed up build)
RUN mvn clean package -DskipTests

# Stage 2: Run the application
FROM eclipse-temurin:25-jre-alpine

WORKDIR /app

# Copy the built jar from the builder stage
# The finalName in pom.xml is 'app', so the jar is app.jar
COPY --from=builder /src/target/app.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
