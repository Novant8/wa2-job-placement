package it.polito.wa2.g07.crm.dtos

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import it.polito.wa2.g07.crm.entities.*

data class CreateContactDTO(
    val name: String?,
    val surname: String?,
    val category: String?,
    val SSN : String?,
    val addresses: List<AddressDTO>
)
fun CreateContactDTO.toEntity(): Contact {
    val contact = Contact(
        this.name ?: "",
        this.surname ?: "",
        category = try {
            ContactCategory.valueOf(this.category?.uppercase() ?: "UNKNOWN")
        } catch (e: IllegalArgumentException) {
            ContactCategory.UNKNOWN
        },
        this.SSN
    )

    this.addresses.forEach { addressDTO ->
        val address = when (addressDTO) {
            is EmailDTO -> Email(addressDTO.email)
            is TelephoneDTO -> Telephone(addressDTO.phoneNumber)
            is DwellingDTO -> Dwelling(addressDTO.street, addressDTO.city, addressDTO.district, addressDTO.country)

        }
        contact.addAddress(address)
    }

    return contact
}
