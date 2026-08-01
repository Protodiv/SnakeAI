package ua.snakeai.backend.handler

import kotlinx.coroutines.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.slf4j.Logger
import org.springframework.web.reactive.socket.WebSocketHandler
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.Mono
import ua.snakeai.backend.ai.DqnAgent
import ua.snakeai.contract.*
import java.io.File
import java.time.LocalDateTime

abstract class BaseAiWebSocketHandler(
    protected val modelStoragePath: String,
    protected val log: Logger
) : WebSocketHandler {

    protected val json = Json { ignoreUnknownKeys = true }

    override fun handle(session: WebSocketSession): Mono<Void> {
        log.info("New WebSocket session initiated. ID: ${session.id}, URI: ${session.handshakeInfo.uri}")
        val sessionJob = Job()
        val scope = CoroutineScope(Dispatchers.Default + sessionJob)

        scope.launch {
            try {
                handleSession(session, scope)
            } catch (e: CancellationException) {
                log.debug("WebSocket session coroutine cancelled (normal teardown). ID: ${session.id}")
                throw e
            } catch (e: Exception) {
                log.error("Error handling WebSocket session [ID: ${session.id}]", e)
                try {
                    val errorResponse = ErrorResponse(
                        message = e.message ?: "Unknown error occurred during WS execution",
                        code = "WS_INTERNAL_ERROR",
                        timestamp = LocalDateTime.now().toString()
                    )
                    val jsonStr = json.encodeToString(errorResponse)
                    sendSafe(session, jsonStr)
                } catch (sendEx: Exception) {
                    log.error("Failed to send error details over WebSocket [ID: ${session.id}]", sendEx)
                }
            } finally {
                sessionJob.cancel()
            }
        }

        return Mono.never<Void>()
            .doFinally { signalType ->
                log.info("WebSocket connection closed. ID: ${session.id}, Signal: $signalType")
                sessionJob.cancel()
            }
    }

    protected abstract suspend fun handleSession(session: WebSocketSession, scope: CoroutineScope)

    protected fun loadOrCreateAgent(
        modelName: String,
        hyperparameters: TrainHyperparameters? = null
    ): DqnAgent {
        val modelsDir = File(modelStoragePath)
        if (!modelsDir.exists()) modelsDir.mkdirs()
        val modelFile = File(modelsDir, "$modelName.zip")
        return if (modelFile.exists()) {
            DqnAgent(modelName, modelFile)
        } else {
            if (hyperparameters != null) {
                DqnAgent(
                    name = modelName,
                    learningRate = hyperparameters.learningRate,
                    batchSize = hyperparameters.batchSize,
                    memorySize = 50000
                )
            } else {
                DqnAgent(modelName)
            }
        }
    }

    protected fun resolveFieldSize(size: FieldSize?): FieldSize {
        val actualSize = size ?: FieldSize.MEDIUM
        return if (actualSize == FieldSize.RANDOM) {
            listOf(FieldSize.SMALL, FieldSize.MEDIUM, FieldSize.LARGE).random()
        } else actualSize
    }

    protected fun createDecisionMetrics(
        obs: DoubleArray,
        action: Int,
        isExploration: Boolean,
        epsilon: Double,
        qValues: DoubleArray
    ): DecisionMetrics {
        return DecisionMetrics(
            dangerStraight = obs[0],
            dangerLeft = obs[1],
            dangerRight = obs[2],
            foodNorth = obs[7],
            foodEast = obs[8],
            foodSouth = obs[9],
            foodWest = obs[10],
            qValues = qValues.toList(),
            selectedAction = when (action) {
                0 -> Actions.STRAIGHT
                1 -> Actions.TURN_LEFT
                2 -> Actions.TURN_RIGHT
                else -> Actions.STRAIGHT
            },
            isExploration = isExploration,
            epsilon = epsilon
        )
    }

    protected fun sendSafe(session: WebSocketSession, message: String) {
        session.send(Mono.just(session.textMessage(message)))
            .subscribe(
                null,
                { error ->
                    val msg = error.message ?: ""
                    if (msg.contains("Connection has been closed", ignoreCase = true) ||
                        msg.contains("AbortedException", ignoreCase = true) ||
                        msg.contains("Broken pipe", ignoreCase = true) ||
                        msg.contains("Connection reset", ignoreCase = true)
                    ) {
                        log.debug("WebSocket connection closed before send operation on session: ${session.id}")
                    } else {
                        log.warn("Error sending message on session ${session.id}: $msg", error)
                    }
                }
            )
    }
}
