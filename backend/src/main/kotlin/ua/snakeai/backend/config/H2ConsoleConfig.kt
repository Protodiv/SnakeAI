package ua.snakeai.backend.config

import org.h2.tools.Server
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import java.sql.SQLException

@Configuration
@Profile("dev")
class H2ConsoleConfig {

    @Bean(initMethod = "start", destroyMethod = "stop")
    @Throws(SQLException::class)
    fun h2WebServer(): Server {
        // Starts the H2 Web Console on port 8082 and allows connections from outside the docker container
        return Server.createWebServer("-web", "-webAllowOthers", "-webPort", "8082")
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    @Throws(SQLException::class)
    fun h2TcpServer(): Server {
        // Starts the H2 TCP Server on port 9092 for external DB clients (like IntelliJ, DBeaver, etc.)
        return Server.createTcpServer("-tcp", "-tcpAllowOthers", "-tcpPort", "9092")
    }
}
