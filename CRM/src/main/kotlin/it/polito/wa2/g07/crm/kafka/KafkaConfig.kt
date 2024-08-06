package it.polito.wa2.g07.crm.kafka

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.support.serializer.JsonDeserializer

@Configuration
@EnableKafka
class KafkaConfig {

    @Value("\${spring.kafka.consumer.bootstrap-servers}")
    lateinit var kafkaServer: String

    @Value("\${spring.kafka.consumer.group-id}")
    lateinit var groupId: String

    @Value("\${spring.kafka.consumer.auto-offset-reset}")
    lateinit var autoOffsetReset: String

    @Bean
    fun registerContainerFactory(consumerFactory: ConsumerFactory<String, RegisterEventValue>): ConcurrentKafkaListenerContainerFactory<String, RegisterEventValue> {
        val factory = ConcurrentKafkaListenerContainerFactory<String, RegisterEventValue>()
        factory.consumerFactory = consumerFactory
        return factory
    }

    @Bean
    fun registerConsumerFactory(): ConsumerFactory<String, RegisterEventValue> {
        val props = mapOf(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to kafkaServer,
            ConsumerConfig.GROUP_ID_CONFIG to groupId,
            ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to autoOffsetReset
        )
        return DefaultKafkaConsumerFactory(props, StringDeserializer(), JsonDeserializer(RegisterEventValue::class.java))
    }

}