package it.polito.wa2.g07.keycloak

import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import java.util.*


object Producer {
    private const val BOOTSTRAP_SERVER = "kafka:29092"

    fun publishEvent(topic: String?, value: String) {
        //reset thread context
        resetThreadContext()

        // create the producer
        val producer = KafkaProducer<String, String>(properties)
        // create a producer record
        val eventRecord =
            ProducerRecord<String, String>(topic, value)

        // send data - asynchronous
        producer.send(eventRecord)

        // flush data
        producer.flush()
        // flush and close producer
        producer.close()
    }

    private fun resetThreadContext() {
        Thread.currentThread().contextClassLoader = null
    }

    private val properties: Properties
        get() {
            val properties = Properties()
            properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVER)
            properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer::class.java.name)
            properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer::class.java.name)
            return properties
        }
}