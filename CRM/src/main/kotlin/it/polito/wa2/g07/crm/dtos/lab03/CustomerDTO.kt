package it.polito.wa2.g07.crm.dtos.lab03

import it.polito.wa2.g07.crm.dtos.lab02.ContactDTO
import it.polito.wa2.g07.crm.dtos.lab02.toContactDto
import it.polito.wa2.g07.crm.entities.lab03.Customer

data class CustomerDTO (
    val id : Long,
    val contactInfo : ContactDTO,
    val notes : String ?

)

fun Customer.toCustomerDto(): CustomerDTO =
    CustomerDTO(this.customerId, this.contactInfo.toContactDto(), this.notes)