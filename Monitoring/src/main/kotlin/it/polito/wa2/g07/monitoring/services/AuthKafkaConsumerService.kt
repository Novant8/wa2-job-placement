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


        @KafkaListener(topics = ["IAM-LOGIN"], groupId = "consumer-monitoring-group",containerFactory = "kafkaListenerContainerFactory")
        fun loginListener(@Header(KafkaHeaders.RECEIVED_TIMESTAMP) ts: Long, message: AuthMonitoringDTO ) {
                var auth = authMonitoringRepository.findById(message.userId)
                if (auth.isEmpty) {
                        authMonitoringRepository.save(AuthMonitoring(message.userId,message.name,ts,0,true))
                }else{
                        if (auth.get().isLogged==false) {
                                auth.get().lastLoginTime = ts
                                auth.get().isLogged=true
                                authMonitoringRepository.save(auth.get())
                        }
                }
        }
        @KafkaListener(topics = ["IAM-LOGOUT"], groupId = "group1",containerFactory = "kafkaListenerContainerFactory")
        fun logoutListener(message: AuthMonitoringDTO,@Header(KafkaHeaders.RECEIVED_TIMESTAMP) ts: Long) {
                var auth = authMonitoringRepository.findById(message.userId) //It should not happen that we logout before login
                auth.get().isLogged= false
                auth.get().totalTime += ts- auth.get().lastLoginTime!!
                authMonitoringRepository.save(auth.get())
        }
}



