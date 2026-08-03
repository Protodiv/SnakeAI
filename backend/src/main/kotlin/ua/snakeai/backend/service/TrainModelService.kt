package ua.snakeai.backend.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import ua.snakeai.backend.exception.ModelSaveException
import ua.snakeai.backend.exception.ResourceNotFoundException
import ua.snakeai.backend.repository.ModelStorageRepository
import ua.snakeai.backend.repository.TrainedModelEntity
import ua.snakeai.backend.repository.TrainedModelRepository
import java.io.File
import java.time.LocalDateTime

import ua.snakeai.backend.mapper.toDto
import ua.snakeai.contract.TrainedAiModel

@Service
class TrainModelService(
    private val repository: TrainedModelRepository,
    private val modelStorageRepository: ModelStorageRepository
) {
    private val log = LoggerFactory.getLogger(TrainModelService::class.java)
    fun listModels(): List<TrainedAiModel> {
        return repository.findAll().map { it.toDto() }
    }

    fun getModel(name: String): TrainedModelEntity {
        return repository.findById(name)
            .orElseThrow { ResourceNotFoundException("Model $name not found") }
    }

    fun saveModel(
        agentName: String,
        episodesRun: Long,
        efficiency: Double,
        topScore: Int,
        tempModelFile: File,
        historyJson: String?
    ): TrainedModelEntity {
        try {
            // Save model file using active storage repository implementation (Local/S3/LocalStack)
            val filePath = modelStorageRepository.saveModel(agentName, tempModelFile)

            val entity = TrainedModelEntity(
                name = agentName,
                episodesRun = episodesRun,
                efficiency = efficiency,
                topScore = topScore,
                filePath = filePath,
                createdAt = LocalDateTime.now(),
                historyJson = historyJson
            )
            return repository.save(entity)
        } catch (e: Exception) {
            if (e is ModelSaveException) {
                throw e
            }
            throw ModelSaveException("Failed to save model entity for agent: $agentName", e)
        }
    }

    fun deleteModel(name: String) {
        val entity = repository.findById(name)
            .orElseThrow { ResourceNotFoundException("Model $name not found") }
        
        try {
            modelStorageRepository.deleteModel(entity.filePath)
        } catch (e: Exception) {
            log.warn("Failed to delete model file for agent $name at ${entity.filePath}, proceeding with database deletion.", e)
        }
        
        repository.delete(entity)
    }

    fun downloadModel(name: String, destinationFile: File): File {
        return modelStorageRepository.fetchModel(name, destinationFile)
    }
}
