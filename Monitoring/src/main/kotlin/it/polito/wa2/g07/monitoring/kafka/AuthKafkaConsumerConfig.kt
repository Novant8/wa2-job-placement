package it.polito.wa2.g07.monitoring.kafka

import it.polito.wa2.g07.monitoring.dtos.AuthMonitoringDTO
import it.polito.wa2.g07.monitoring.dtos.MessageMonitoringDTO
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory

import org.springframework.kafka.support.serializer.JsonDeserializer
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory


@EnableKafka
@Configuration
class AuthKafkaConsumerConfig {
    @Value("\${spring.kafka.consumer.bootstrap-servers}")
    lateinit var kafkaServer: String
    @Value("\${spring.kafka.consumer.group-id}")
    lateinit var groupId: String
    @Bean
    fun authConsumerFactory(): ConsumerFactory<String, AuthMonitoringDTO> {
        val configProps = mapOf(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to kafkaServer,
            ConsumerConfig.GROUP_ID_CONFIG to groupId,
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to ErrorHandlingDeserializer::class.java,
            ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS to JsonDeserializer::class.java,
            JsonDeserializer.VALUE_DEFAULT_TYPE to AuthMonitoringDTO::class.java.name
        )

        val jsonDeserializer = JsonDeserializer(AuthMonitoringDTO::class.java)

        return DefaultKafkaConsumerFactory(
            configProps,
            StringDeserializer(),
            jsonDeserializer
        )
    }

    @Bean
    fun kafkaListenerContainerFactory(): ConcurrentKafkaListenerContainerFactory<String, AuthMonitoringDTO> {
        val factory = ConcurrentKafkaListenerContainerFactory<String, AuthMonitoringDTO>()
        factory.consumerFactory = authConsumerFactory()
        return factory
    }

}