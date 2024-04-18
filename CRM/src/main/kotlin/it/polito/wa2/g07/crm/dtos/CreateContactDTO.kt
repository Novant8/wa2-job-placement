package it.polito.wa2.g07.crm.dtos

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import it.polito.wa2.g07.crm.entities.*

data class CreateContactDTO(
    val name: String,
    val surname: String,
    val category: String?,
    val SSN : String?,
    val addresses: List<AddressDTO>
)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes(
    JsonSubTypes.Type(value = EmailDTO::class, name = "mail"),
    JsonSubTypes.Type(value = TelephoneDTO::class, name = "phone"),
    JsonSubTypes.Type(value = DwellingDTO::class, name = "dwelling")
)
sealed  class AddressDTO


data class EmailDTO(
    val email: String
) : AddressDTO()

data class TelephoneDTO(
    val phoneNumber: String
) : AddressDTO()

data class DwellingDTO(
    val street: String,
    val city: String,
    val district: String,
    val country: String
) : AddressDTO()

fun CreateContactDTO.toEntity(): Contact {
    val contact = Contact()
    contact.name = this.name
    contact.surname = this.surname
    contact.category = try {
        ContactCategory.valueOf(this.category?.uppercase() ?: "UNKNOWN")
    } catch (e: IllegalArgumentException) {
        ContactCategory.UNKNOWN
    }
    contact.SSN = this.SSN

    this.addresses.forEach { addressDTO ->
        when (addressDTO) {
            is EmailDTO -> {
                val email = Email()
                email.email = addressDTO.email
                contact.addAddress(email)
            }
            is TelephoneDTO -> {
                val telephone = Telephone()
                telephone.number = addressDTO.phoneNumber
                contact.addAddress(telephone)
            }
            is DwellingDTO -> {
                val dwelling = Dwelling()
                dwelling.street = addressDTO.street
                dwelling.city = addressDTO.city
                dwelling.district = addressDTO.district
                dwelling.country = addressDTO.country
                contact.addAddress(dwelling)
            }
        }
    }

    return contact
}
