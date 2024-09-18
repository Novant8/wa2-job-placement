package it.polito.wa2.g07.crm.dtos.project

import com.fasterxml.jackson.annotation.JsonProperty
import it.polito.wa2.g07.crm.dtos.lab03.toJobOfferKafkaDTO
import it.polito.wa2.g07.crm.entities.project.JobProposal

data class JobProposalKafkaDTO (
    val id:Long,
    val status :String,
    val documentId : Long?,
    val professionalSignedContract : Long?,
    val customer : String,
    val professional : String,
    val jobOffer: String
)
fun JobProposal.toJobProposalKafkaDTO():JobProposalKafkaDTO{
    return JobProposalKafkaDTO(
        id = this.proposalID,
        status = this.status.toString(),
        documentId = this.documentId,
        professionalSignedContract =  this.professionalSignedContract,
        customer = this.customer.contactInfo.name+" "+this.customer.contactInfo.surname,
        professional = this.professional.contactInfo.name+" "+this.professional.contactInfo.surname,
        jobOffer = "ID: "+ this.jobOffer.offerId +" Description: "+this.jobOffer.description
    )
}
