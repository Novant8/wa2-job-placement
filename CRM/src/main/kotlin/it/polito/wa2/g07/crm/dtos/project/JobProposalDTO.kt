package it.polito.wa2.g07.crm.dtos.project

import it.polito.wa2.g07.crm.dtos.lab03.*
import it.polito.wa2.g07.crm.entities.lab03.Customer
import it.polito.wa2.g07.crm.entities.lab03.JobOffer
import it.polito.wa2.g07.crm.entities.lab03.Professional
import it.polito.wa2.g07.crm.entities.project.JobProposal
import it.polito.wa2.g07.crm.entities.project.ProposalStatus

data class JobProposalDTO (
    val id : Long?,
    val customer: CustomerDTO,
    val professional: ProfessionalDTO,
    val jobOffer : JobOfferDTO,
    val documentId : Long?,
    val status: ProposalStatus,
    val customerConfirmation : Boolean,
    val professionalSignedContract: Long?
)

fun JobProposal.toJobProposalDTO(): JobProposalDTO{
    return JobProposalDTO(
        id = this.proposalID,
        customer= this.customer.toCustomerDto(),
        professional= this.professional.toProfessionalDto(),
        jobOffer= this.jobOffer.toJobOfferDTO(),
        documentId= this.documentId,
        status= this.status,
        customerConfirmation= this.customerConfirm,
        professionalSignedContract = this.professionalSignedContract
    )
}