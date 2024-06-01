package it.polito.wa2.g07.crm.dtos.lab02

import com.fasterxml.jackson.annotation.*
import io.swagger.v3.oas.annotations.media.Schema
import it.polito.wa2.g07.crm.entities.lab02.*
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern

@JsonTypeInfo(use = JsonTypeInfo.Id.DEDUCTION)
@JsonSubTypes(
    JsonSubTypes.Type(value = EmailDTO::class),
    JsonSubTypes.Type(value = TelephoneDTO::class),
    JsonSubTypes.Type(value = DwellingDTO::class)
)
sealed class AddressDTO {
    @get:JsonIgnore
    val id:Long?=0
    @get:JsonIgnore
    abstract val addressType: AddressType
    @get:JsonIgnore
    abstract val compatibleChannels: List<MessageChannel>

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
    @field:NotBlank(message = "Email must not be blank")
    @field:jakarta.validation.constraints.Email(message = "must be a valid email")
    @field:Schema(example = "john.doe@example.com")
    val email: String
) : AddressDTO() {
    override val addressType: AddressType
        get() = AddressType.EMAIL

    override val compatibleChannels: List<MessageChannel>
        get() = listOf(MessageChannel.EMAIL)

    override fun toEntity(): Email {
        return Email(this.email)
    }
}
data class TelephoneDTO(
    @field:NotBlank(message = "Telephone must not be blank")
    @field:Pattern(
        regexp = "(\\+[0-9]{1,3}\\s)?([0-9\\s-]+)",
        message = "must be a valid phone number"
    )
    @field:Schema(example = "+01 0100 555-0199")
    val phoneNumber: String
) : AddressDTO() {
    override val addressType: AddressType
        get() = AddressType.TELEPHONE

    override val compatibleChannels: List<MessageChannel>
        get() = listOf(MessageChannel.TEXT_MESSAGE, MessageChannel.PHONE_CALL)

    override fun toEntity(): Telephone {
        return Telephone(this.phoneNumber)
    }
}

@JsonInclude(JsonInclude.Include.NON_NULL)
data class DwellingDTO(
    @field:NotBlank(message = "Street must not be blank")
    @field:Schema(example = "123 Main St.")
    val street: String,

    @field:NotBlank(message = "City must not be blank")
    @field:Schema(example = "New York")
    val city: String,

    // Can be null but not blank
    @field:Pattern(regexp = "^(?!\\s*$).+", message = "District must not be blank")
    @field:Schema(example = "NY")
    val district: String?,

    // Can be null but not blank
    @field:Pattern(regexp = "^(?!\\s*$).+", message = "Country must not be blank")
    @field:Schema(example = "US")
    val country: String?
) : AddressDTO() {
    override val addressType: AddressType
        get() = AddressType.DWELLING

    override val compatibleChannels: List<MessageChannel>
        get() = listOf(MessageChannel.POSTAL_MAIL)

    override fun toEntity(): Address {
        return Dwelling(this.street, this.city, this.district, this.country)
    }
}
