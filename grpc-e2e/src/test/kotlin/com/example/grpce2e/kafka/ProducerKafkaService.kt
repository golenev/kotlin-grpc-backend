package com.example.grpce2e.kafka

import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.errors.AuthenticationException
import org.junit.platform.commons.logging.LoggerFactory
import java.util.concurrent.TimeUnit

class ProducerKafkaService<T : Any>(
    private val cfg: ProducerKafkaConfig,
    private val topic: String,
    private val mapper: ObjectMapper,
) : AutoCloseable {

    private val logger = LoggerFactory.getLogger(ProducerKafkaService::class.java)
    private val producer: KafkaProducer<String, String>

    init {
        producer = try {
            KafkaProducer(cfg.toProperties())
        } catch (authException: AuthenticationException) {
            logger.info(authException) {
                "Failed to initialize Kafka producer for topic $topic due to authentication error"
            }
            throw authException
        } catch (ex: Exception) {
            logger.error(ex){"Failed to initialize Kafka producer for topic $topic "}
            throw ex
        }
    }

    fun send(key: String?, payload: T) {
        val json = mapper.writeValueAsString(payload)
        val record = if (key != null) {
            ProducerRecord(topic, key, json)
        } else {
            ProducerRecord(topic, json)
        }

        try {
            producer.send(record).get(30, TimeUnit.SECONDS)
            logger.debug{"Sent payload with key={$key} to topic {$topic}: {$json}"}
        } catch (ex: Exception) {
            val cause = ex.cause ?: ex
            if (cause is AuthenticationException) {
                logger.error(cause){" \"Authentication failed while sending payload with key={$key} to topic {$topic}\""}

            } else {
                logger.error(ex){"Failed to send payload with key={$key} to topic {$topic}: {$json}"}
            }
            throw ex
        }
    }

    override fun close() {
        producer.use {
            it.flush()
        }
    }
}
