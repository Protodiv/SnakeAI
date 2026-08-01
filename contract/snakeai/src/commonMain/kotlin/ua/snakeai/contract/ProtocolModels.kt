package ua.snakeai.contract

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator
import kotlinx.serialization.SerialName

@Serializable
@JsonClassDiscriminator("action")
sealed interface PlayCommand {
    @Serializable
    @SerialName("START")
    data class Start(
        val modelName: String? = null,
        val fieldSize: FieldSize? = null,
        val tickRateMs: Long? = null
    ) : PlayCommand

    @Serializable
    @SerialName("PAUSE")
    data object Pause : PlayCommand

    @Serializable
    @SerialName("RESUME")
    data object Resume : PlayCommand

    @Serializable
    @SerialName("RESTART")
    data object Restart : PlayCommand

    @Serializable
    @SerialName("STOP")
    data object Stop : PlayCommand
}

object Actions {
    const val STRAIGHT = "STRAIGHT"
    const val TURN_LEFT = "TURN_LEFT"
    const val TURN_RIGHT = "TURN_RIGHT"
}

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
    val maxEpisodes: Int? = null,
    val batchSize: Int = 64
)

@Serializable
@JsonClassDiscriminator("action")
sealed interface TrainCommand {
    @Serializable
    @SerialName("START_TRAINING")
    data class Start(
        val modelName: String? = null,
        val fieldSize: FieldSize? = null,
        val hyperparameters: TrainHyperparameters? = null
    ) : TrainCommand

    @Serializable
    @SerialName("STOP")
    data object Stop : TrainCommand
}

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
    val metrics: TrainingProgressMetrics,
    val gameState: GameState? = null
)

@Serializable
data class ErrorResponse(
    val message: String? = null,
    val code: String? = null,
    val timestamp: String? = null,
    val status: Int? = null,
    val error: String? = null,
    val path: String? = null
)
