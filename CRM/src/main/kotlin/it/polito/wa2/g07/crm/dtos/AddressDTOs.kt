package it.polito.wa2.g07.crm.dtos

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import it.polito.wa2.g07.crm.entities.*

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "channel")
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

@JsonInclude(JsonInclude.Include.NON_NULL)
data class DwellingDTO(val street: String, val city: String, val district: String?,val country: String?) : AddressDTO()
