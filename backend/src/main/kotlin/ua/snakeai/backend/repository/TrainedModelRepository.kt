package ua.snakeai.backend.repository

import jakarta.persistence.*
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Entity
@Table(name = "trained_models")
data class TrainedModelEntity(
    @Id
    @Column(name = "name", nullable = false)
    val name: String,

    @Column(name = "episodes_run", nullable = false)
    val episodesRun: Long,

    @Column(name = "efficiency", nullable = false)
    val efficiency: Double,

    @Column(name = "top_score", nullable = false)
    val topScore: Int,

    @Column(name = "file_path", nullable = false)
    val filePath: String,

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Lob
    @Column(name = "history_json")
    val historyJson: String? = null
)

@Repository
interface TrainedModelRepository : JpaRepository<TrainedModelEntity, String>
