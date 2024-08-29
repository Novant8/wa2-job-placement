package it.polito.wa2.g07.monitoring.services

import it.polito.wa2.g07.monitoring.dtos.MessageMonitoringDTO
import it.polito.wa2.g07.monitoring.entities.MessageMonitoring
import it.polito.wa2.g07.monitoring.repositories.MessageMonitoringRepository
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Service


@Service
class MessageKafkaConsumerService(private val messageMonitoring: MessageMonitoringRepository) {
    @KafkaListener(topics = ["MESSAGE"], groupId = "group1",containerFactory = "kafkaMessageListenerContainerFactory")
    fun messageListener( message: MessageMonitoringDTO) {
        var msg = messageMonitoring.findById(message.id)
        if (msg.isEmpty){
            messageMonitoring.save(MessageMonitoring(message.id,message.channel,message.priority,message.creationTimestamp,message.status,message.statusTimestamp))
        }else{
            msg.get().status =message.status
            msg.get().statusTimestamp =message.statusTimestamp
            msg.get().priority = message.priority
            messageMonitoring.save(msg.get())
        }

    }

}