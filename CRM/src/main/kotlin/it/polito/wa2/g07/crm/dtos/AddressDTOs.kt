package it.polito.wa2.g07.crm.dtos

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import it.polito.wa2.g07.crm.entities.*
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern

@JsonTypeInfo(use = JsonTypeInfo.Id.DEDUCTION)
@JsonSubTypes(
    JsonSubTypes.Type(value = EmailDTO::class),
    JsonSubTypes.Type(value = TelephoneDTO::class),
    JsonSubTypes.Type(value = DwellingDTO::class)
)
sealed class AddressDTO {
    //@get:JsonIgnore
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
    @field:NotBlank
    @field:jakarta.validation.constraints.Email(message = "must be a valid email")
    val email: String
) : AddressDTO() {
    override val addressType: AddressType
        get() = AddressType.EMAIL

    override fun toEntity(): Email {
        return Email(this.email)
    }
}

data class TelephoneDTO(
    @field:NotBlank
    @field:Pattern(
        regexp = "(\\+[0-9]{1,3}\\s)?([0-9\\s-]+)",
        message = "must be a valid phone number"
    )
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
    @field:NotBlank
    val street: String,

    @field:NotBlank
    val city: String,

    // Can be null but not blank
    @field:Pattern(regexp = "^(?!\\s*$).+", message = "must not be blank")
    val district: String?,

    // Can be null but not blank
    @field:Pattern(regexp = "^(?!\\s*$).+", message = "must not be blank")
    val country: String?
) : AddressDTO() {
    override val addressType: AddressType
        get() = AddressType.DWELLING

    override fun toEntity(): Address {
        return Dwelling(this.street, this.city, this.district, this.country)
    }
}
