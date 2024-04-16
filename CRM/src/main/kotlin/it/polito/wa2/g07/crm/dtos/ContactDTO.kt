package it.polito.wa2.g07.crm.dtos

import it.polito.wa2.g07.crm.entities.*

data class ContactDTO(
        val id :Long ,
        val name : String ,
        val surname : String ,
        val category: String,
        val addresses: MutableSet<Address>

)

fun Contact.toContactDto(): ContactDTO=
        ContactDTO(
                this.contactId,
                this.name,
                this.surname,
                this.category.name,
                this.addresses
        )