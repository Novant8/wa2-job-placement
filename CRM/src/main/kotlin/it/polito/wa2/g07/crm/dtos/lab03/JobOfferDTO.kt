package it.polito.wa2.g07.crm.dtos.lab03

import it.polito.wa2.g07.crm.dtos.lab02.ReducedContactDTO
import it.polito.wa2.g07.crm.dtos.lab02.toReducedContactDTO
import it.polito.wa2.g07.crm.entities.lab03.Customer
import it.polito.wa2.g07.crm.entities.lab03.JobOffer
import it.polito.wa2.g07.crm.entities.lab03.OfferStatus
import it.polito.wa2.g07.crm.entities.lab03.Professional
import kotlin.time.Duration

data class JobOfferDTO (
    val id:Long?,
    val customer: ReducedContactDTO,
    val requiredSkills: Set<String>,
    val duration: Long,
    val offerStatus: OfferStatus,
    val notes: String?,
    val professional: Professional?,    //during the lifecycle of a joboffer professional can be empty
    val value:Double?                   //when a professional is not present value cannot be computed
)


fun JobOffer.toJobOfferDTO(): JobOfferDTO {
    return JobOfferDTO(
        id = this.offerId,
        customer = this.customer.contactInfo.toReducedContactDTO(),
        requiredSkills = this.requiredSkills,
        duration = this.duration,
        notes = this.notes,
        professional = this.professional,
        value = this.value,
        offerStatus = this.status
    )
}