package it.polito.wa2.g07.crm.dtos


data class MessageCreateDTO (
    val sender: String,
    val channel: String,
    val subject: String,
    val body:String,
)
