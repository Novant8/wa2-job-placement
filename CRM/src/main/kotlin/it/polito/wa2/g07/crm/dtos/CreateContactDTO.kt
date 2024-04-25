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
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes(
    JsonSubTypes.Type(value = EmailDTO::class, name = "email"),
    JsonSubTypes.Type(value = TelephoneDTO::class, name = "phone"),
    JsonSubTypes.Type(value = DwellingDTO::class, name = "dwelling")
)
sealed  class AddressDTO

fun Address.toAddressDTO(): AddressDTO {

   return  when (this) {
        is Email ->  EmailDTO(this.email)
        is Telephone ->  TelephoneDTO(this.number)
        is Dwelling ->  DwellingDTO(this.street, this.city, this.district, this.country)
        else -> {throw Exception("Unknown address type")}
   }
}

data class EmailDTO(
    val email: String
) : AddressDTO()

data class TelephoneDTO(
    val phoneNumber: String
) : AddressDTO()

data class DwellingDTO(
    val street: String,
    val city: String,
    val district: String?,
    val country: String?
) : AddressDTO()

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
