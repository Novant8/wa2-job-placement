package it.polito.wa2.g07.crm.dtos.lab02

import it.polito.wa2.g07.crm.entities.lab02.ContactCategory
import it.polito.wa2.g07.crm.entities.lab02.Contact

data class ReducedContactDTO (
        val id:Long ,
        val name : String?,
        val surname : String? ,
        val category : ContactCategory
)

fun Contact.toReducedContactDTO(): ReducedContactDTO =
        ReducedContactDTO(this.contactId, this.name , this.surname, this.category)