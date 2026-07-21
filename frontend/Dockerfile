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
ENV SERVER_HOST=localhost
ENV SERVER_PORT=8080

RUN --mount=type=cache,target=/root/.gradle \
    --mount=type=cache,target=/root/.konan \
    ./gradlew :frontend:composeApp:wasmJsBrowserDistribution --no-daemon

# ─────────────────────────────────────────────────────────────────────────────
# Stage 2: Serve — tiny Nginx image with only the compiled static files.
# No JDK, no source code, no Gradle. Final image size: ~50MB.
# ─────────────────────────────────────────────────────────────────────────────
FROM nginx:alpine

COPY --from=builder /frontend/frontend/composeApp/build/dist/wasmJs/productionExecutable /usr/share/nginx/html

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
