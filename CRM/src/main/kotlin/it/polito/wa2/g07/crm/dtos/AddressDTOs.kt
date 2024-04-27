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
sealed class AddressDTO {
    abstract val addressType: AddressType
    abstract fun toEntity(): Address
}

fun Address.toAddressDTO(): AddressDTO {

    return  when (this) {
        is Email ->  EmailDTO(this.email)
        is Telephone ->  TelephoneDTO(this.number)
        is Dwelling ->  DwellingDTO(this.street, this.city, this.district, this.country)
        else -> {throw Exception("Unknown address type")}
    }
}

fun AddressResponseDTO.toAddressDTO(): AddressDTO {
    return  when (this) {
        is EmailResponseDTO -> EmailDTO(this.email)
        is TelephoneResponseDTO -> TelephoneDTO(this.phoneNumber)
        is DwellingResponseDTO -> DwellingDTO(this.street, this.city, this.district, this.country)
    }
}

data class EmailDTO(
    val email: String
) : AddressDTO() {
    override val addressType: AddressType
        get() = AddressType.EMAIL

    override fun toEntity(): Email {
        return Email(this.email)
    }
}

data class TelephoneDTO(
    val phoneNumber: String
) : AddressDTO() {
    override val addressType: AddressType
        get() = AddressType.TELEPHONE

    override fun toEntity(): Telephone {
        return Telephone(this.phoneNumber)
    }
}

@JsonInclude(JsonInclude.Include.NON_NULL)
data class DwellingDTO(
    val street: String,
    val city: String,
    val district: String?,
    val country: String?
) : AddressDTO() {
    override val addressType: AddressType
        get() = AddressType.DWELLING

    override fun toEntity(): Address {
        return Dwelling(this.street, this.city, this.district, this.country)
    }
}
