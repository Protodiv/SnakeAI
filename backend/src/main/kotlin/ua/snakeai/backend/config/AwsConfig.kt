package ua.snakeai.backend.config

import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.CreateBucketRequest
import software.amazon.awssdk.services.s3.model.HeadBucketRequest
import software.amazon.awssdk.services.s3.model.NoSuchBucketException
import java.net.URI
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider

@Configuration
class AwsConfig {

    @Bean
    @Profile("prod")
    fun s3ClientProd(@Value("\${aws.region:eu-north-1}") regionStr: String): S3Client {
        return S3Client.builder()
            .region(Region.of(regionStr))
            .build()
    }

    @Bean
    @Profile("localstack")
    fun s3ClientLocalStack(
        @Value("\${aws.region:eu-north-1}") regionStr: String,
        @Value("\${aws.localstack.endpoint:http://localhost:4566}") endpointStr: String
    ): S3Client {
        return S3Client.builder()
            .region(Region.of(regionStr))
            .endpointOverride(URI.create(endpointStr))
            .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create("mock-access-key", "mock-secret-key")))
            .forcePathStyle(true) // Required for LocalStack
            .build()
    }

    @Bean
    @Profile("localstack")
    fun localStackBucketInitializer(
        s3Client: S3Client,
        @Value("\${aws.s3.bucket-name:snakeai-models-bucket}") bucketName: String
    ): LocalStackBucketInitializer {
        return LocalStackBucketInitializer(s3Client, bucketName)
    }
}

class LocalStackBucketInitializer(
    private val s3Client: S3Client,
    private val bucketName: String
) {
    private val log = LoggerFactory.getLogger(LocalStackBucketInitializer::class.java)

    @PostConstruct
    fun init() {
        try {
            s3Client.headBucket(HeadBucketRequest.builder().bucket(bucketName).build())
            log.info("LocalStack S3 Bucket '$bucketName' already exists.")
        } catch (e: NoSuchBucketException) {
            log.info("LocalStack S3 Bucket '$bucketName' not found. Creating bucket...")
            s3Client.createBucket(CreateBucketRequest.builder().bucket(bucketName).build())
            log.info("LocalStack S3 Bucket '$bucketName' created successfully.")
        } catch (e: Exception) {
            log.warn("Could not verify/create LocalStack S3 Bucket '$bucketName'. It might be pre-created or LocalStack offline.", e)
        }
    }
}
