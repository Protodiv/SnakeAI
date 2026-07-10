# SnakeAiCompose

> [!NOTE]
> Currently, the project has already been written for the JVM platform, and the goal is to rewrite it for **Compose Multiplatform**. Please note that the project is currently **under construction**.

**SnakeAi** is a game application that integrates a classic **Snake Game** with a reinforcement learning agent trained via **Deep Q-Learning**. The game's graphical user interface is being rebuilt using **Compose Multiplatform**, and the AI agent is powered by **Deeplearning4j (DL4J)**.

---
<video src="doc/demo.mp4" width="100%" controls autoplay loop muted></video>
---

## 🚀 Key Features

*   **Reinforcement Learning Agent:** Plays the game autonomously using a Double Deep Q-Network (Double DQN) model.
*   **Modern Compose Multiplatform UI:** Being rebuilt to target multiple platforms using Jetpack Compose, featuring smooth rendering, live score tracking, record stats, and starvation progress.
*   **Virtual Input Gamepad:** Real-time visual rendering of the active steering decision selected by the AI model.
*   **DL4J Training & UI Server:** Integrates with the Deeplearning4j UI Server for training visualization and stat monitoring (accessible at `http://localhost:9000/train/overview`).

---

## 🛠️ Tech Stack

*   **Language:** Kotlin
*   **UI Framework:** [Compose Multiplatform](frontend/composeApp/build.gradle.kts)
*   **AI/Deep Learning Library:** Deeplearning4j (DL4J) & RL4J
*   **Math Engine:** ND4J with native platform backend (`nd4j-native-platform`)
*   **Build System:** Gradle

---

## 🧠 AI Architecture & RL Environment

The project models the classic Snake game as a Markov Decision Process (MDP) using the following elements:

### 1. State Space (Observation Space)
Rather than passing the entire game grid as pixels or coordinates, the project uses a highly efficient 4-dimensional heuristic vector. The state vector encodes the status of the four immediate directions (Left, Up, Right, Down):
*   `-1.0` if moving in that direction results in an immediate collision (walls, body tail, or starvation).
*   `1.0` if the direction is safe and brings the snake head closer to the food.
*   `0.0` if the direction is safe but moves the snake head further from the food.

### 2. Action Space
A discrete action space of size 4 representing the direction decisions: `Left`, `Up`, `Right`, and `Down`.

### 3. Reward System
*   **`+100.0`** for eating food.
*   **`-100.0`** for colliding with walls or tail.
*   **`+1.0`** for a step moving closer to the food.
*   **`-10.0`** for a step moving away from the food.
