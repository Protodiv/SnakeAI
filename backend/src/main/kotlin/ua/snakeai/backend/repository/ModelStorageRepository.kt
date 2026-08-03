package ua.snakeai.backend.repository

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Repository
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import ua.snakeai.backend.exception.ModelSaveException
import ua.snakeai.backend.exception.ModelDeleteException
import ua.snakeai.backend.exception.ModelLoadException
import java.io.File

interface ModelStorageRepository {
    fun saveModel(name: String, file: File): String
    fun deleteModel(filePath: String)
    fun fetchModel(name: String, destinationFile: File): File
}

@Repository
@Profile("h2", "dev", "default")
class LocalModelStorageRepository(
    @Value("\${model.storage.path:models}") private val modelStoragePath: String
) : ModelStorageRepository {
    override fun saveModel(name: String, file: File): String {
        val modelsDir = File(modelStoragePath)
        if (!modelsDir.exists()) modelsDir.mkdirs()
        val destination = File(modelsDir, "${name}.zip")
        try {
            if (file.absolutePath != destination.absolutePath) {
                file.copyTo(destination, overwrite = true)
            }
            return destination.absolutePath
        } catch (e: Exception) {
            throw ModelSaveException("Failed to save model file locally", e)
        }
    }

    override fun deleteModel(filePath: String) {
        val file = File(filePath)
        if (file.exists()) {
            val deleted = file.delete()
            if (!deleted) {
                throw ModelDeleteException("Failed to delete local model file: $filePath")
            }
        }
    }

    override fun fetchModel(name: String, destinationFile: File): File {
        val modelsDir = File(modelStoragePath)
        val source = File(modelsDir, "${name}.zip")
        if (!source.exists()) {
            throw ModelLoadException("Model file $name.zip does not exist locally")
        }
        if (source.absolutePath != destinationFile.absolutePath) {
            try {
                source.copyTo(destinationFile, overwrite = true)
            } catch (e: Exception) {
                throw ModelLoadException("Failed to copy local model file to destination", e)
            }
        }
        return destinationFile
    }
}

@Repository
@Profile("localstack")
class LocalStackModelStorageRepository(
    private val s3Client: S3Client,
    @Value("\${aws.s3.bucket-name:snakeai-models}") private val bucketName: String
) : ModelStorageRepository {
    override fun saveModel(name: String, file: File): String {
        val s3Key = "models/${name}.zip"
        try {
            s3Client.putObject(
                PutObjectRequest.builder().bucket(bucketName).key(s3Key).build(),
                RequestBody.fromFile(file)
            )
            return s3Key
        } catch (e: Exception) {
            throw ModelSaveException("Failed to save model to LocalStack S3", e)
        }
    }

    override fun deleteModel(filePath: String) {
        try {
            s3Client.deleteObject(
                DeleteObjectRequest.builder().bucket(bucketName).key(filePath).build()
            )
        } catch (e: Exception) {
            throw ModelDeleteException("Failed to delete model from LocalStack S3: $filePath", e)
        }
    }

    override fun fetchModel(name: String, destinationFile: File): File {
        val s3Key = "models/${name}.zip"
        try {
            s3Client.getObject(
                GetObjectRequest.builder().bucket(bucketName).key(s3Key).build(),
                destinationFile.toPath()
            )
            return destinationFile
        } catch (e: Exception) {
            throw ModelLoadException("Failed to load model from LocalStack S3", e)
        }
    }
}

@Repository
@Profile("prod")
class AwsProdModelStorageRepository(
    private val s3Client: S3Client,
    @Value("\${aws.s3.bucket-name}") private val bucketName: String
) : ModelStorageRepository {
    override fun saveModel(name: String, file: File): String {
        val s3Key = "models/${name}.zip"
        try {
            s3Client.putObject(
                PutObjectRequest.builder().bucket(bucketName).key(s3Key).build(),
                RequestBody.fromFile(file)
            )
            return s3Key
        } catch (e: Exception) {
            throw ModelSaveException("Failed to save model to AWS S3 Production", e)
        }
    }

    override fun deleteModel(filePath: String) {
        try {
            s3Client.deleteObject(
                DeleteObjectRequest.builder().bucket(bucketName).key(filePath).build()
            )
        } catch (e: Exception) {
            throw ModelDeleteException("Failed to delete model from AWS S3 Production: $filePath", e)
        }
    }

    override fun fetchModel(name: String, destinationFile: File): File {
        val s3Key = "models/${name}.zip"
        try {
            s3Client.getObject(
                GetObjectRequest.builder().bucket(bucketName).key(s3Key).build(),
                destinationFile.toPath()
            )
            return destinationFile
        } catch (e: Exception) {
            throw ModelLoadException("Failed to load model from AWS S3 Production", e)
        }
    }
}
