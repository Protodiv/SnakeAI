package ua.snakeai.contract

import kotlinx.serialization.Serializable

@Serializable
data class PlayCommand(
    val action: String, // START, PAUSE, RESUME, RESTART, STOP
    val modelName: String? = null,
    val fieldSize: FieldSize? = null,
    val tickRateMs: Long? = null
)

@Serializable
data class DecisionMetrics(
    val dangerStraight: Double,
    val dangerLeft: Double,
    val dangerRight: Double,
    val foodNorth: Double,
    val foodEast: Double,
    val foodSouth: Double,
    val foodWest: Double,
    val qValues: List<Double>,
    val selectedAction: String, // STRAIGHT, TURN_LEFT, TURN_RIGHT
    val isExploration: Boolean,
    val epsilon: Double
)

@Serializable
data class GameFrame(
    val type: String = "GAME_FRAME",
    val state: GameState,
    val decisionMetrics: DecisionMetrics
)

@Serializable
data class TrainHyperparameters(
    val learningRate: Double = 0.001,
    val maxEpisodes: Int = 1000,
    val batchSize: Int = 64
)

@Serializable
data class TrainCommand(
    val action: String, // START_TRAINING, STOP
    val modelName: String? = null,
    val fieldSize: FieldSize? = null,
    val hyperparameters: TrainHyperparameters? = null
)

@Serializable
data class TrainingProgressMetrics(
    val episode: Int,
    val epsilon: Double,
    val loss: Double,
    val averageReward: Double,
    val topScore: Int,
    val recentScore: Int,
    val stepsPlayed: Int,
    val stepsPerSecond: Double,
    val elapsedTimeMs: Long
)

@Serializable
data class TrainingMetricsFrame(
    val type: String = "TRAINING_METRICS",
    val metrics: TrainingProgressMetrics
)

@Serializable
data class ErrorResponse(
    val message: String? = null,
    val code: String? = null
)
