# ─────────────────────────────────────────────────────────────────────────────
# Stage 1: Build — compile Kotlin/WasmJS frontend bundle
#
# Layer caching strategy:
#   - Gradle wrapper / build config copied before source → rarely invalidated
#   - Source code copied last → only invalidates the compile layer
#   - BuildKit cache mounts persist ~/.gradle and ~/.konan across builds
#     so Gradle distribution + all dependencies are downloaded ONCE only
# ─────────────────────────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jdk-jammy AS builder

WORKDIR /frontend

COPY gradlew .
COPY gradle/ gradle/

RUN apt-get update && apt-get install -y --no-install-recommends dos2unix libatomic1 \
    && rm -rf /var/lib/apt/lists/* \
    && dos2unix gradlew \
    && chmod +x gradlew

COPY gradle.properties .
COPY settings.gradle.kts .
COPY build.gradle.kts .
COPY contract/ contract/
COPY backend/build.gradle.kts backend/build.gradle.kts
COPY frontend/ frontend/

# ── 6. Compile the WasmJS production bundle ───────────────────────────────────
# --mount=type=cache keeps ~/.gradle and ~/.konan on your host machine
# between builds, so Gradle + Kotlin/Wasm toolchain are downloaded ONCE only.
# Subsequent builds skip all downloads and go straight to compilation.
#
# SERVER_HOST / SERVER_PORT are baked into BuildConfig at compile time.
# Pass them via:  docker build --build-arg SERVER_HOST=api.example.com ...
ARG SERVER_HOST=localhost
ARG SERVER_PORT=8080
ENV SERVER_HOST=$SERVER_HOST
ENV SERVER_PORT=$SERVER_PORT

RUN --mount=type=cache,id=frontend-gradle,target=/root/.gradle \
    --mount=type=cache,id=frontend-konan,target=/root/.konan \
    ./gradlew :frontend:composeApp:wasmJsBrowserDistribution --no-daemon

# Stage 2: Serve base image
FROM nginx:alpine AS base
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]

# Stage 3: Fast CI/CD target (copies host-compiled wasm files)
FROM base AS fast-ci
COPY frontend/composeApp/build/dist/wasmJs/productionExecutable /usr/share/nginx/html

# Stage 4: Local development target (compiles inside Docker)
# Default stage since it is the last stage
FROM base AS local
COPY --from=builder /frontend/frontend/composeApp/build/dist/wasmJs/productionExecutable /usr/share/nginx/html
