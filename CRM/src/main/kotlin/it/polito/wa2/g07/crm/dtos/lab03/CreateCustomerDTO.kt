package it.polito.wa2.g07.crm.dtos.lab03

import it.polito.wa2.g07.crm.dtos.lab02.CreateContactDTO

data class CreateCustomerDTO (

    val contact : CreateContactDTO?,
    val notes : String?

)