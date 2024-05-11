package it.polito.wa2.g07.crm.dtos.lab02

import io.swagger.v3.oas.annotations.media.Schema
import it.polito.wa2.g07.crm.entities.lab02.ContactCategory
import it.polito.wa2.g07.crm.entities.lab02.Contact

data class ReducedContactDTO (
        @field:Schema(example = "1")
        val id:Long ,

        @field:Schema(example = "John")
        val name : String?,

        @field:Schema(example = "Doe")
        val surname : String? ,

        val category: ContactCategory,
)

fun Contact.toReducedContactDTO(): ReducedContactDTO =
        ReducedContactDTO(this.contactId, this.name , this.surname, this.category)

