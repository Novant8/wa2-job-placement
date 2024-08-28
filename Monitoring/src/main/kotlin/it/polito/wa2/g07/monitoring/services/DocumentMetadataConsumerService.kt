package it.polito.wa2.g07.monitoring.services

import it.polito.wa2.g07.monitoring.dtos.DocumentMetadataMonitoringDTO
import it.polito.wa2.g07.monitoring.dtos.MessageMonitoringDTO
import it.polito.wa2.g07.monitoring.entities.DocumentMetadataMonitoring
import it.polito.wa2.g07.monitoring.entities.MessageMonitoring
import it.polito.wa2.g07.monitoring.repositories.DocumentMetadataMonitoringRepository
import it.polito.wa2.g07.monitoring.repositories.MessageMonitoringRepository
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Service

class DocumentMetadataConsumerService {
}
@Service
class DocumentKafkaConsumerService(private val documentMonitoringMetadataRepository: DocumentMetadataMonitoringRepository) {
    @KafkaListener(topics = ["DOCUMENT"], groupId = "consumer-monitoring-group",containerFactory = "kafkaDocumentListenerContainerFactory")
    fun messageListener( document: DocumentMetadataMonitoringDTO) {

        var doc = documentMonitoringMetadataRepository.findById(document.id)
        if (doc.isEmpty){
            documentMonitoringMetadataRepository.save(DocumentMetadataMonitoring(document.id,document.name,document.size,document.contentType,document.creationTimestamp))
        }else{
            doc.get().name = document.name
            doc.get().size =document.size
            doc.get().contentType = document.contentType
            doc.get().creationTimestamp = document.creationTimestamp
            documentMonitoringMetadataRepository.save(doc.get())
        }

    }

}