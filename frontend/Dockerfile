# Stage 1: Build Wasm/JS site
FROM openjdk:21-slim AS builder
WORKDIR /workspace
COPY . .

# Convert gradlew line endings to Unix and make it executable
RUN apt-get update && apt-get install -y dos2unix && dos2unix gradlew && chmod +x gradlew
RUN ./gradlew :frontend:composeApp:wasmJsBrowserDistribution --no-daemon

# Stage 2: Serve using Nginx
FROM nginx:alpine
COPY --from=builder /workspace/frontend/composeApp/build/dist/wasmJs/productionExecutable /usr/share/nginx/html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
