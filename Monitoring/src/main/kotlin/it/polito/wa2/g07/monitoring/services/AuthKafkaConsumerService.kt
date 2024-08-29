package it.polito.wa2.g07.monitoring.services

import it.polito.wa2.g07.monitoring.dtos.AuthMonitoringDTO
import it.polito.wa2.g07.monitoring.entities.AuthMonitoring
import it.polito.wa2.g07.monitoring.repositories.AuthMonitoringRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.handler.annotation.Header
import org.springframework.stereotype.Service

@Service
class AuthKafkaConsumerService(
        private val authMonitoringRepository: AuthMonitoringRepository,
) {


        @KafkaListener(topics = ["IAM-LOGIN"], groupId = "consumer-monitoring-group",containerFactory = "kafkaAuthListenerContainerFactory")
        fun loginListener(@Header(KafkaHeaders.RECEIVED_TIMESTAMP) ts: Long, message: AuthMonitoringDTO ) {
                authMonitoringRepository.save(AuthMonitoring(username  =message.name,loginTime = ts))
        }
       /* @KafkaListener(topics = ["IAM-LOGOUT"], groupId = "group1",containerFactory = "kafkaAuthListenerContainerFactory")
        fun logoutListener(message: AuthMonitoringDTO,@Header(KafkaHeaders.RECEIVED_TIMESTAMP) ts: Long) {
                var auth = authMonitoringRepository.findById(message.userId) //It should not happen that we logout before login
                auth.get().isLogged= false
                auth.get().totalTime += ts- auth.get().lastLoginTime!!
                authMonitoringRepository.save(auth.get())
        }*/
}



