# Stage 1: Build the backend JAR
FROM openjdk:21-slim AS builder
WORKDIR /workspace
COPY . .

# Convert gradlew line endings to Unix and make it executable
RUN apt-get update && apt-get install -y dos2unix && dos2unix gradlew && chmod +x gradlew
RUN ./gradlew :backend:bootJar --no-daemon

# Stage 2: Runtime image
FROM openjdk:21-slim
WORKDIR /app

# Install system package libgomp1 required for ND4J CPU native execution
RUN apt-get update && apt-get install -y libgomp1 && rm -rf /var/lib/apt/lists/*

COPY --from=builder /workspace/backend/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
