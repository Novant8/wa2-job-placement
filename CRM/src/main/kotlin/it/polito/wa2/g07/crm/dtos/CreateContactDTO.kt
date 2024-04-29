package it.polito.wa2.g07.crm.dtos

import it.polito.wa2.g07.crm.entities.*
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern

data class CreateContactDTO(
    @field:NotNull
    @field:NotBlank
    val name: String?,

    @field:NotNull
    @field:NotBlank
    val surname: String?,

    @field:NotNull
    @field:NotBlank
    val category: String?,

    // Can be null but not blank
    @field:Pattern(regexp = "^(?!\\s*$).+", message = "must not be blank")
    val ssn : String?,

    @field:Valid
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
            this.ssn
    )

    this.addresses.forEach { addressDTO ->
        val address = when (addressDTO) {
            is EmailDTO -> Email(addressDTO.email)
            is TelephoneDTO -> Telephone(addressDTO.phoneNumber)
            is DwellingDTO -> Dwelling(addressDTO.street, addressDTO.city, addressDTO.district, addressDTO.country)
            else -> {throw  IllegalArgumentException()}
        }
        contact.addAddress(address)
    }

    return contact
}
