package ua.snakeai.backend.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.HandlerMapping
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping
import org.springframework.web.reactive.socket.WebSocketHandler
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter
import ua.snakeai.backend.handler.AiPlayWebSocketHandler
import ua.snakeai.backend.handler.AiTrainWebSocketHandler

@Configuration
class WebSocketConfig(
    private val aiPlayWebSocketHandler: AiPlayWebSocketHandler,
    private val aiTrainWebSocketHandler: AiTrainWebSocketHandler
) {
    @Bean
    fun webSocketHandlerMapping(): HandlerMapping {
        val map = mapOf<String, WebSocketHandler>(
            "/ws/ai/play" to aiPlayWebSocketHandler,
            "/ws/ai/train" to aiTrainWebSocketHandler
        )
        val mapping = SimpleUrlHandlerMapping()
        mapping.order = 1
        mapping.urlMap = map
        return mapping
    }

    @Bean
    fun handlerAdapter(): WebSocketHandlerAdapter {
        return WebSocketHandlerAdapter()
    }
}
