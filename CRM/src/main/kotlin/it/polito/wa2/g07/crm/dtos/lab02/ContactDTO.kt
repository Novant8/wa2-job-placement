package it.polito.wa2.g07.crm.dtos.lab02

import io.swagger.v3.oas.annotations.media.Schema
import it.polito.wa2.g07.crm.entities.lab02.*

data class ContactDTO(
        @field:Schema(example = "1")
        val id :Long,

        @field:Schema(example = "John")
        val name : String,

        @field:Schema(example = "Doe")
        val surname : String,

        val category: ContactCategory,

        val addresses: List<AddressResponseDTO>,

        @field:Schema(example = "123456")
        val ssn: String?
)

sealed  class AddressResponseDTO(
        open val id: Long
)

data class EmailResponseDTO(
        @field:Schema(example = "1")
        override val id: Long,

        @field:Schema(example = "john.doe@example.org")
        val email: String
) : AddressResponseDTO(id)

data class TelephoneResponseDTO(
        @field:Schema(example = "2")
        override val id: Long,

        @field:Schema(example = "+01 0100 555-0199")
        val phoneNumber: String
) : AddressResponseDTO(id)

data class DwellingResponseDTO(
        @field:Schema(example = "3")
        override val id:  Long,

        @field:Schema(example = "123 Main St.")
        val street: String,

        @field:Schema(example = "New York")
        val city: String,

        @field:Schema(example = "NY")
        val district: String?,

        @field:Schema(example = "US")
        val country: String?
) : AddressResponseDTO(id)

fun Address.toAddressResponseDTO(): AddressResponseDTO {
        return  when (this) {
                is Email -> EmailResponseDTO(this.id, this.email)
                is Telephone -> TelephoneResponseDTO(this.id, this.number)
                is Dwelling -> DwellingResponseDTO(this.id, this.street, this.city, this.district, this.country)
                else -> error("Invalid address")
        }
}

fun Contact.toContactDto(): ContactDTO =
        ContactDTO(
                this.contactId,
                this.name,
                this.surname,
                this.category,
                this.addresses.map { address: Address ->
                        when (address){
                                is Email -> EmailResponseDTO(address.id,address.email)
                                is Telephone -> TelephoneResponseDTO(address.id,address.number)
                                is Dwelling -> DwellingResponseDTO(address.id,address.street, address.city, address.district?:"", address.country?:"")
                                else -> throw IllegalArgumentException("Unknown address type")
                        }
                },
                this.ssn
        )