package it.polito.wa2.g07.crm.dtos

import it.polito.wa2.g07.crm.entities.AddressType


data class MessageCreateDTO (
    val sender: AddressDTO,
    val channel: String,
    val subject: String,
    val body:String,
)
