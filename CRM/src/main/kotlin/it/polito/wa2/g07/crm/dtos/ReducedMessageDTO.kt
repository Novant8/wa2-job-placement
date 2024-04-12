package it.polito.wa2.g07.crm.dtos

import it.polito.wa2.g07.crm.entities.Message


data class ReducedMessageDTO (
        val id: Long,
        val subject: String
)

fun Message.toReducedDTO():ReducedMessageDTO= ReducedMessageDTO(this.messageID,this.subject)


