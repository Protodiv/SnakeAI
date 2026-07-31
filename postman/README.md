# SnakeAI — Docker & Testing Guide

## Quick Commands

### Start EVERYTHING (Backend + Frontend)
```powershell
# From the project root (where docker-compose.yml lives)
docker compose up --build
```
- Backend → http://localhost:8080
- Frontend → http://localhost:80

To run in background (detached):
```powershell
docker compose up --build -d
```

To stop:
```powershell
docker compose down
```

---

### Start Backend ONLY
```powershell
# Uses the dedicated backend-only compose file
docker compose -f docker-compose-backend-only.yml up --build
```
- Backend → http://localhost:8080
- No frontend started

To run in background:
```powershell
docker compose -f docker-compose-backend-only.yml up --build -d
```

To stop:
```powershell
docker compose -f docker-compose-backend-only.yml down
```

---

## Checking Logs

```powershell
# Full stack
docker compose logs -f

# Backend only
docker compose -f docker-compose-backend-only.yml logs -f backend
```

Look for this line to know the backend is ready:
```
Started Main in X.XXX seconds
```

---

## Testing with Postman

### Import Collection
1. Open **Postman**
2. Click **Import** and select both files from the `postman/` folder:
   - `SnakeAI_Backend.postman_collection.json`
   - `SnakeAI_Local.postman_environment.json`
3. In the top-right dropdown, select environment: **SnakeAI — Local**

### Run REST Tests (Recommended order)

| Step | Request | What it tests |
|------|---------|---------------|
| 1 | `Health & Diagnostics / Connectivity Check` | Backend is up on port 8080 |
| 2 | `Models API / 1. List All Models` | GET /api/models — returns [] if DB empty |
| 3 | `Models API / 2a. Get Non-Existent Model` | 404 error handling via GlobalExceptionHandler |
| 4 | `Models API / 3a. Delete Non-Existent Model` | 404 error handling on DELETE |
| 5 | (after training) `Models API / 2. Get Model by Name` | GET /api/models/{name} |
| 6 | (after training) `Models API / 3. Delete Model by Name` | DELETE /api/models/{name} |

> **Note**: Steps 5 & 6 require a trained model in the database. Train one first via WebSocket.

---

### Test WebSocket Endpoints

Postman supports WebSocket testing natively:

1. Click **New** > **WebSocket Request**
2. Enter URL and click **Connect**

#### Training Session — ws://localhost:8080/ws/ai/train (or `{{wsUrl}}/ws/ai/train`)

After connecting, send:
```json
{
  "action": "START_TRAINING",
  "modelName": "MyTestAgent",
  "fieldSize": "MEDIUM",
  "hyperparameters": {
    "maxEpisodes": 100,
    "learningRate": 0.001,
    "batchSize": 64
  }
}
```
You will receive a metrics frame every 200ms containing both training metrics and the current game state:
```json
{
  "type": "TRAINING_METRICS",
  "metrics": {
    "episode": 1,
    "epsilon": 0.99,
    "loss": 0.024,
    "averageReward": -0.85,
    "topScore": 2,
    "recentScore": 1,
    "stepsPlayed": 45,
    "stepsPerSecond": 150.0,
    "elapsedTimeMs": 300
  },
  "gameState": {
    "snake": [{"x": 10, "y": 10}, {"x": 10, "y": 11}],
    "food": {"x": 5, "y": 5},
    "score": 1,
    "status": "PLAYING",
    "direction": "UP",
    "fieldSize": "MEDIUM"
  }
}
```
*Note: `gameState` is nullable and sent only when a training episode is active. This allows you to inspect real-time game state/play data during training.*

To stop training:
```json
{ "action": "STOP" }
```

#### Play Session — ws://localhost:8080/ws/ai/play (or `{{wsUrl}}/ws/ai/play`)

After connecting, send a message referencing the model you want to load (you can use `{{modelName}}` to load the last trained/listed model):
```json
{
  "action": "START",
  "modelName": "{{modelName}}",
  "fieldSize": "MEDIUM",
  "tickRateMs": 120
}
```
*Tip: Change `{{modelName}}` in your Postman variables to test different models!*

You will receive game frames every 120ms. Controls:
```json
{ "action": "PAUSE" }
{ "action": "RESUME" }
{ "action": "RESTART" }
{ "action": "STOP" }
```

---

## API Reference

### REST — ModelRestController at /api/models

| Method | Endpoint | Description | Response |
|--------|----------|-------------|----------|
| GET | /api/models | List all trained models | 200 List of TrainedAiModel |
| GET | /api/models/{name} | Get model entity by name | 200 TrainedModelEntity or 404 |
| DELETE | /api/models/{name} | Delete model and file from disk | 200 void or 404 |

### WebSocket — WebSocketConfig

| URL | Handler | Description |
|-----|---------|-------------|
| ws://localhost:8080/ws/ai/train | AiTrainWebSocketHandler | DQN training session with live metrics |
| ws://localhost:8080/ws/ai/play | AiPlayWebSocketHandler | Live game replay using a trained model |

### Error Response Schema (GlobalExceptionHandler)
```json
{
  "message": "Model X not found",
  "code": "NOT_FOUND",
  "status": 404,
  "error": "Not Found",
  "timestamp": "2024-01-01T12:00:00Z",
  "path": "/api/models/X"
}
```
