package it.polito.wa2.g07.crm.kafka

import it.polito.wa2.g07.crm.dtos.lab02.MessageKafkaDTO
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory
import org.springframework.kafka.support.serializer.JsonSerializer


@Configuration
class MessageKafkaProducerConfig {
    @Value("\${spring.kafka.consumer.bootstrap-servers}")
    lateinit var kafkaServer: String
    @Bean
    fun messageProducerFactory(): ProducerFactory<String, MessageKafkaDTO> {
        val configProps: MutableMap<String, Any> = HashMap()
        configProps[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] = kafkaServer
        configProps[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java
        configProps[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] =   JsonSerializer::class.java
        return DefaultKafkaProducerFactory(configProps)
    }

    @Bean
    fun messageKafkaTemplate(): KafkaTemplate<String, MessageKafkaDTO> {
        return KafkaTemplate(messageProducerFactory())
    }
}