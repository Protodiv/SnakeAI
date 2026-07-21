# Stage 1: Build the backend JAR
FROM amazoncorretto:21-alpine AS builder
WORKDIR /workspace

# Install dos2unix once — this layer is cached permanently
RUN apk add --no-cache dos2unix

# ── Step 1: Copy only the files needed to resolve dependencies ────────────────
# This layer is cached as long as build scripts don't change,
# so dependencies won't be re-downloaded on every source change.
COPY gradlew .
COPY gradle/ gradle/
COPY settings.gradle.kts .
COPY gradle.properties .
COPY build.gradle.kts .
COPY backend/build.gradle.kts backend/build.gradle.kts
COPY contract/ contract/

# Fix line endings and permissions
RUN dos2unix gradlew && chmod +x gradlew

# ── Step 2: Pre-download all dependencies (cached mount + layer cache) ─────────
RUN --mount=type=cache,id=backend-gradle,target=/root/.gradle \
    ./gradlew :backend:dependencies --no-daemon --quiet

# ── Step 3: Copy source and build ─────────────────────────────────────────────
COPY backend/src/ backend/src/

RUN --mount=type=cache,id=backend-gradle,target=/root/.gradle \
    ./gradlew :backend:bootJar --no-daemon --parallel --build-cache

# Stage 2: Runtime image
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Install system package libgomp1 required for ND4J CPU native execution
RUN apt-get update && apt-get install -y --no-install-recommends libgomp1 && rm -rf /var/lib/apt/lists/*

COPY --from=builder /workspace/backend/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
