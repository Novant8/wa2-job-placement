package it.polito.wa2.g07.monitoring.services

import it.polito.wa2.g07.monitoring.dtos.AuthMonitoringDTO
import it.polito.wa2.g07.monitoring.dtos.JobOfferMonitoringDTO
import it.polito.wa2.g07.monitoring.entities.AuthMonitoring
import it.polito.wa2.g07.monitoring.entities.JobOfferMonitoring
import it.polito.wa2.g07.monitoring.repositories.AuthMonitoringRepository
import it.polito.wa2.g07.monitoring.repositories.JobOfferMonitoringRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.handler.annotation.Header
import org.springframework.stereotype.Service

@Service
class JobOfferKafkaConsumerService(
        private val jobOfferMonitoringRepository: JobOfferMonitoringRepository,
) {

        @KafkaListener(topics = ["JOB_OFFER-UPDATE"], groupId = "consumer-monitoring-group",containerFactory = "kafkaJobOfferListenerContainerFactory")
        @KafkaListener(topics = ["JOB_OFFER-CREATE"], groupId = "consumer-monitoring-group",containerFactory = "kafkaJobOfferListenerContainerFactory")
        fun createdJobOfferListener( offer: JobOfferMonitoringDTO ) {
                print("received JOB_OFFER-CREATE/UPDATE")
                val offerRetrived = jobOfferMonitoringRepository.findById(offer.id)
                if (offerRetrived.isEmpty) { // During the test the same id could be reused
                        jobOfferMonitoringRepository.save(
                                JobOfferMonitoring(
                                        offer.id,offer.description,offer.customer,
                                        offer.requiredSkills,offer.duration,
                                        offer.offerStatus,offer.notes,offer.professional,offer.value))

                }else{
                        val offer_monitored = offerRetrived.get()
                        offer_monitored.offerStatus=offer.offerStatus
                        offer_monitored.notes=offer.notes
                        offer_monitored.value=offer.value
                        offer_monitored.description=offer.description
                        offer_monitored.requiredSkills=offer.requiredSkills
                        offer_monitored.duration=offer.duration
                        jobOfferMonitoringRepository.save(offer_monitored)
                }

        }


     /*   fun updatedJobOfferListener( offer: JobOfferMonitoringDTO ) {
                print("received JOB_OFFER-UPDATE")
                val offer_monitored = jobOfferMonitoringRepository.findById(offer.id).get()
                offer_monitored.offerStatus=offer.offerStatus
                offer_monitored.notes=offer.notes
                offer_monitored.value=offer.value
                offer_monitored.description=offer.description
                offer_monitored.requiredSkills=offer.requiredSkills
                offer_monitored.duration=offer.duration
                jobOfferMonitoringRepository.save(offer_monitored)
        }*/
}



