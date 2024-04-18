package it.polito.wa2.g07.crm.dtos

import it.polito.wa2.g07.crm.entities.ContactCategory
import it.polito.wa2.g07.crm.entities.Contact

data class ReducedContactDTO (
        val id:Long ,
        val name : String?,
        val surname : String? ,
        val category : ContactCategory
)

fun Contact.toReducedContactDTO():ReducedContactDTO=
        ReducedContactDTO(this.contactId, this.name , this.surname, this.category)