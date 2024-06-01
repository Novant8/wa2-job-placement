package it.polito.wa2.g07.communicationmanager.dtos

data class MessageCreateDTO (
    val sender: EmailDTO,
    val subject: String,
    val body:String
) {
    val channel: String = "EMAIL"
}