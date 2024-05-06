package it.polito.wa2.g07.crm.dtos.lab03

import it.polito.wa2.g07.crm.dtos.lab02.CreateContactDTO
import it.polito.wa2.g07.crm.dtos.lab02.toEntity
import it.polito.wa2.g07.crm.entities.lab03.Customer

data class CreateCustomerDTO (

    val contact : CreateContactDTO,
    val notes : String?

)

fun CreateCustomerDTO.toEntity():Customer = Customer(this.contact.toEntity(), this.notes)