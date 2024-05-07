package it.polito.wa2.g07.crm.dtos.lab03

import it.polito.wa2.g07.crm.dtos.lab02.ReducedContactDTO
import it.polito.wa2.g07.crm.dtos.lab02.toReducedContactDTO
import it.polito.wa2.g07.crm.entities.lab03.Customer

data class ReducedCustomerDTO(
    val id : Long,
    val contactInfo: ReducedContactDTO,
    val notes : String?
)

fun Customer.toReduceCustomerDTO():ReducedCustomerDTO=
    ReducedCustomerDTO(this.customerId, this.contactInfo.toReducedContactDTO(), this.notes)