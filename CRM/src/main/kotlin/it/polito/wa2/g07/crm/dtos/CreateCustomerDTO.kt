package it.polito.wa2.g07.crm.dtos

data class CreateCustomerDTO (

    val contact : CreateContactDTO?,
    val notes : String?

)