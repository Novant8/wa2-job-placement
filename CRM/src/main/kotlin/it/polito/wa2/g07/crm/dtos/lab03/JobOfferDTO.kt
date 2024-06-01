package it.polito.wa2.g07.crm.dtos.lab03

import it.polito.wa2.g07.crm.dtos.lab02.ReducedContactDTO
import it.polito.wa2.g07.crm.dtos.lab02.toReducedContactDTO

import it.polito.wa2.g07.crm.entities.lab03.JobOffer
import it.polito.wa2.g07.crm.entities.lab03.OfferStatus
import it.polito.wa2.g07.crm.entities.lab03.Professional

data class JobOfferDTO (
    val id:Long?,
    val description : String,
    val customer: ReducedCustomerDTO,
    val requiredSkills: MutableSet<String>,
    val duration: Long,
    val offerStatus: OfferStatus,
    val notes: String?,
    val professional: ProfessionalReducedDTO?,    //during the lifecycle of a joboffer professional can be empty
    val value:Double?                   //when a professional is not present value cannot be computed
)


fun JobOffer.toJobOfferDTO(): JobOfferDTO {
    if (this.professional==null){
        return JobOfferDTO(
            id = this.offerId,
            description = this.description,
            customer = this.customer.toReduceCustomerDTO_Basic(),
            requiredSkills = this.requiredSkills,
            duration = this.duration,
            notes = this.notes,
            professional = null,
            value = this.value,
            offerStatus = this.status
        )
    }
    return JobOfferDTO(
        id = this.offerId,
        description = this.description,
        customer = this.customer.toReduceCustomerDTO_Basic(),
        requiredSkills = this.requiredSkills,
        duration = this.duration,
        notes = this.notes,
        professional = this.professional!!.toProfessionalReducedDTO_Basic(),
        value = this.value,
        offerStatus = this.status
    )
}