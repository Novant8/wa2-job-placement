package it.polito.wa2.g07.crm.dtos

import org.aspectj.bridge.Message

data class ReducedMessageDTO (
        val id: Int,
        val subject: String
        )

fun Message.toReducedDTO():ReducedMessageDTO{
    return ReducedMessageDTO(this.id,this.subject)
}

