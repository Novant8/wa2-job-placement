package it.polito.wa2.g07.crm.dtos.lab03

import it.polito.wa2.g07.crm.dtos.lab02.ReducedContactDTO
import it.polito.wa2.g07.crm.dtos.lab02.toReducedContactDTO

import it.polito.wa2.g07.crm.entities.lab03.JobOffer
import it.polito.wa2.g07.crm.entities.lab03.OfferStatus
import it.polito.wa2.g07.crm.entities.lab03.Professional

data class JobOfferKafkaDTO (
    val id:Long?,
    val description : String,
    val customer: String,
    val requiredSkills: MutableSet<String>,
    val duration: Long,
    val offerStatus: String,
    val notes: String?,
    val professional: String?,    //during the lifecycle of a joboffer professional can be empty
    val value:Double?                   //when a professional is not present value cannot be computed
)


fun JobOffer.toJobOfferKafkaDTO(): JobOfferKafkaDTO {
    if (this.professional==null){
        return JobOfferKafkaDTO(
            id = this.offerId,
            description = this.description,
            customer = this.customer.contactInfo.name+" "+this.customer.contactInfo.surname,
            requiredSkills = this.requiredSkills,
            duration = this.duration,
            notes = this.notes,
            professional = null,
            value = this.value,
            offerStatus = this.status.toString()
        )
    }
    return JobOfferKafkaDTO(
        id = this.offerId,
        description = this.description,
        customer = this.customer.contactInfo.name+" "+this.customer.contactInfo.surname,
        requiredSkills = this.requiredSkills,
        duration = this.duration,
        notes = this.notes,
        professional = this.professional!!.contactInfo.name+" "+this.professional!!.contactInfo.surname,
        value = this.value,
        offerStatus = this.status.toString()
    )
}