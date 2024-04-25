package it.polito.wa2.g07.crm.dtos


data class MessageCreateDTO (
    val sender: AddressDTO,
    val subject: String,
    val body:String,
)
