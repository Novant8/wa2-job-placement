package it.polito.wa2.g07.crm.dtos

import it.polito.wa2.g07.crm.entities.*

data class ContactDTO(
        val id :Long ,
        val name : String? ,
        val surname : String? ,
        val category: Category,
        val addresses: MutableSet<Address>,
        val SSN : String?

)

fun Contact.toContactDto(): ContactDTO=
        ContactDTO(
                this.contactId,
                this.name,
                this.surname,
                this.category,
                this.addresses,
                this.SSN
        )