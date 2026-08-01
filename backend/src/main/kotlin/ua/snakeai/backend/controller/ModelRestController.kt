package ua.snakeai.backend.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import ua.snakeai.backend.repository.TrainedModelEntity
import ua.snakeai.backend.service.TrainModelService
import ua.snakeai.contract.TrainedAiModel

@RestController
@RequestMapping("/api/models")
@CrossOrigin(origins = ["*"])
class ModelRestController(
    private val service: TrainModelService
) {

    @GetMapping
    fun listModels(): ResponseEntity<List<TrainedAiModel>> {
        return ResponseEntity.ok(service.listModels())
    }

    @GetMapping("/{name}")
    fun getModel(@PathVariable name: String): ResponseEntity<TrainedModelEntity> {
        return ResponseEntity.ok(service.getModel(name))
    }

    @DeleteMapping("/{name}")
    fun deleteModel(@PathVariable name: String): ResponseEntity<Void> {
        service.deleteModel(name)
        return ResponseEntity.ok().build()
    }
}
