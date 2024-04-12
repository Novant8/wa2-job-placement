package it.polito.wa2.g07.crm.dtos

import it.polito.wa2.g07.crm.entities.*

data class MessageDTO(
   val id:Long
)

fun Message.toMessageDTO(): MessageDTO=
    MessageDTO(this.messageID
    )