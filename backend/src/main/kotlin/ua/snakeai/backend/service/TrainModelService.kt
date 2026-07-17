package ua.snakeai.backend.service

import org.springframework.stereotype.Service
import ua.snakeai.backend.exception.ResourceNotFoundException
import ua.snakeai.backend.repository.TrainedModelEntity
import ua.snakeai.backend.repository.TrainedModelRepository
import java.io.File

import ua.snakeai.backend.mapper.toDto
import ua.snakeai.contract.TrainedAiModel

@Service
class TrainModelService(
    private val repository: TrainedModelRepository
) {
    fun listModels(): List<TrainedAiModel> {
        return repository.findAll().map { it.toDto() }
    }

    fun getModel(name: String): TrainedModelEntity {
        return repository.findById(name)
            .orElseThrow { ResourceNotFoundException("Model $name not found") }
    }

    fun deleteModel(name: String) {
        val entity = repository.findById(name)
            .orElseThrow { ResourceNotFoundException("Model $name not found") }
        
        val file = File(entity.filePath)
        if (file.exists()) {
            file.delete()
        }
        repository.delete(entity)
    }
}
