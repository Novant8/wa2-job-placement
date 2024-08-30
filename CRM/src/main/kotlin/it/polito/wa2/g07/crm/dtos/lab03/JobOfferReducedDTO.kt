package it.polito.wa2.g07.crm.dtos.lab03

import it.polito.wa2.g07.crm.dtos.lab02.ReducedContactDTO
import it.polito.wa2.g07.crm.dtos.lab02.toReducedContactDTO
import it.polito.wa2.g07.crm.entities.lab03.Customer
import it.polito.wa2.g07.crm.entities.lab03.JobOffer
import it.polito.wa2.g07.crm.entities.lab03.OfferStatus
import it.polito.wa2.g07.crm.entities.lab03.Professional



data class JobOfferReducedDTO (
    val id:Long?,
    val description : String,
    val customer: ReducedCustomerDTO,
    val offerStatus: OfferStatus,
    val professional: ProfessionalReducedDTO?,    //during the lifecycle of a joboffer professional can be empty
)


fun JobOffer.toJobOfferReducedDTO(): JobOfferReducedDTO {
    if (this.professional==null){
        return JobOfferReducedDTO(
            id = this.offerId,
            description = this.description,
            professional = null,
            customer =  this.customer.toReduceCustomerDTO_Basic(),
            offerStatus = this.status
        )
    }
    return JobOfferReducedDTO(
        id = this.offerId,
        description = this.description,
        professional = this.professional!!.toProfessionalReducedDto(),
        customer = this.customer.toReduceCustomerDTO_Basic(),
        offerStatus = this.status
    )
}