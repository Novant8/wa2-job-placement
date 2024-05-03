package it.polito.wa2.g07.crm.dtos.lab02

import it.polito.wa2.g07.crm.entities.lab02.*

data class ContactDTO(
        val id :Long,
        val name : String,
        val surname : String,
        val category: ContactCategory,
        val addresses: List<AddressResponseDTO>,
        val ssn: String?
)

sealed  class AddressResponseDTO(
        open val id: Long
)

data class EmailResponseDTO(
        override val id: Long,
        val email: String
) : AddressResponseDTO(id)

data class TelephoneResponseDTO(
        override val id: Long,
        val phoneNumber: String
) : AddressResponseDTO(id)

data class DwellingResponseDTO(
        override val id:  Long,
        val street: String,
        val city: String,
        val district: String?,
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