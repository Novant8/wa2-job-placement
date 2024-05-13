package it.polito.wa2.g07.crm.dtos.lab02

import io.swagger.v3.oas.annotations.media.Schema
import it.polito.wa2.g07.crm.entities.lab02.*
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern

data class CreateContactDTO(
    @field:NotNull(message = "Name must not be null ")
    @field:NotBlank (message = "Name must not be blank ")
    @field:Schema(example = "John")
    val name: String?,

    @field:NotNull(message = "Surname must not be null ")
    @field:NotBlank(message = "Surname must not be blank ")
    @field:Schema(example = "Doe")
    val surname: String?,

    @field:NotNull(message = "Category must not be null ")
    @field:NotBlank(message = "Category must not be blank ")
    @field:Schema(implementation = ContactCategory::class)
    val category: String?,

    // Can be null but not blank
    @field:Pattern(regexp = "^(?!\\s*$).+", message = "must not be blank")
    @field:Schema(example = "123456")
    val ssn : String?,

    @field:Valid
    val addresses: List<AddressDTO> = listOf()
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
