package com.example.grpce2e.kafka

import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.common.config.SaslConfigs
import java.util.Properties

open class KafkaConfig(
    private val bootstrapServers: String,
    private val username: String?,
    private val password: String?,
) {
    var securityProtocol: String = "PLAINTEXT"
    var saslMechanism: String? = null

    open fun toProperties(): Properties {
        return Properties().apply {
            put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers)
            put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, securityProtocol)
            saslMechanism?.let { put(SaslConfigs.SASL_MECHANISM, it) }
            if (!username.isNullOrBlank() && !password.isNullOrBlank()) {
                put(
                    SaslConfigs.SASL_JAAS_CONFIG,
                    "org.apache.kafka.common.security.plain.PlainLoginModule required username=\"$username\" password=\"$password\";",
                )
            }
        }
    }
}
