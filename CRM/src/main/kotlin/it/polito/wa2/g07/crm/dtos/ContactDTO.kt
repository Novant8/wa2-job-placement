package it.polito.wa2.g07.crm.dtos

import it.polito.wa2.g07.crm.entities.*

enum class ContactFilterBy {
        NONE,
        FULL_NAME,
        CATEGORY,
        ADDRESS,
        TELEPHONE,
        SSN,
        EMAIL
}

data class ContactDTO(
        val id :Long ,
        val name : String ,
        val surname : String ,
        val category: ContactCategory,
        val addresses: List<AddressDTO>,
        val SSN: String?
)

fun Contact.toContactDto(): ContactDTO=
        ContactDTO(
                this.contactId,
                this.name,
                this.surname,
                this.category,
                this.addresses.map { address: Address ->
                        when (address){
                                is Email -> {
                                        EmailDTO(address.email)
                                }
                                is Telephone ->{
                                        TelephoneDTO(address.number)
                                }
                                is Dwelling ->{
                                        DwellingDTO(address.street ?: "", address.city ?:"",address.district?:"", address.country?:"")
                                }
                                else -> throw IllegalArgumentException("Unknown address type")
                        }},
                this.SSN
        )