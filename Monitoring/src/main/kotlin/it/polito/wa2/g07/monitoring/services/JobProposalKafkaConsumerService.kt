package it.polito.wa2.g07.monitoring.services

import it.polito.wa2.g07.monitoring.dtos.JobProposalMonitoringDTO
import it.polito.wa2.g07.monitoring.entities.JobProposalMonitoring
import it.polito.wa2.g07.monitoring.repositories.JobProposalMonitoringRepository
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Service

@Service
class JobProposalKafkaConsumerService (
    private val jobProposalMonitoringRepository: JobProposalMonitoringRepository
){
    @KafkaListener(topics = ["JOB_PROPOSAL-UPDATE"], groupId = "consumer-monitoring-group",containerFactory = "kafkaJobProposalListenerContainerFactory")
    @KafkaListener(topics = ["JOB_PROPOSAL-CREATE"], groupId = "consumer-monitoring-group",containerFactory = "kafkaJobProposalListenerContainerFactory")
    fun createdJobProposalListener(proposal:JobProposalMonitoringDTO){
        print("received JOB_PROPOSAL-CREATE/UPDATE")
        val proposeReceived = jobProposalMonitoringRepository.findById(proposal.id)
        if (proposeReceived.isEmpty){
            jobProposalMonitoringRepository.save(
                JobProposalMonitoring(
                    proposal.id,proposal.status,proposal.documentId,proposal.professionalSignedContract,proposal.customer,proposal.professional,proposal.jobOffer
                )
            )
        }else{
          val proposalMonitored = proposeReceived.get()
            proposalMonitored.status = proposal.status
            proposalMonitored.documentId=proposal.documentId
            proposalMonitored.professionalSignedContract = proposal.professionalSignedContract
            proposalMonitored.customer = proposal.customer
            proposalMonitored.professional = proposal.professional
            proposalMonitored.jobOffer= proposal.jobOffer
            jobProposalMonitoringRepository.save(proposalMonitored)
        }
    }
}