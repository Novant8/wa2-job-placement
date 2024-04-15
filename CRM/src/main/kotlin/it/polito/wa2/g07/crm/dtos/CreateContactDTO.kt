package it.polito.wa2.g07.crm.dtos

import it.polito.wa2.g07.crm.entities.*

data class CreateContactDTO (

    val name : String,
    val surname : String,
    val category: String?,
    val street: String?,
    val city: String?,
    val district: String?,
    val country: String?,
    val phoneNumbers : MutableSet<String?>,
    val SSN : String?,
    val emails : MutableSet<String?>
)



fun CreateContactDTO.toEntity(): Contact {
    val contact = Contact()
    contact.name = this.name
    contact.surname = this.surname
    contact.category = when (this.category?.lowercase()) {
        "customer" -> Category.CUSTOMER
        "professional" -> Category.PROFESSIONAL
        else -> Category.UNKNOWN
    }
    contact.SSN = this.SSN

    // Creating and adding address
    val address = Address()
    address.street = this.street ?: ""
    address.city = this.city ?: ""
    address.district = this.district ?: ""
    address.country = this.country ?: ""
    contact.addAddress(address)

    // Creating and adding emails
    this.emails.filterNotNull().map { email ->
        val emailEntity = Email()
        emailEntity.email = email
        contact.addEmail(emailEntity)
    }

    // Creating and adding telephones
    this.phoneNumbers.filterNotNull().map { phoneNumber ->
        val telephone = Telephone()
        telephone.number = phoneNumber
        contact.addTelephone(telephone)
    }

    return contact
}
