package it.polito.wa2.g07.crm.dtos.lab02

import it.polito.wa2.g07.crm.entities.lab02.Message


data class ReducedMessageDTO (
    val id: Long,
    val subject: String,
    val sender: AddressDTO,
    val channel: MessageChannel
)

fun Message.toReducedDTO(): ReducedMessageDTO {
        return ReducedMessageDTO(this.messageID,this.subject,this.sender.toAddressDTO(),
            this.channel
        )
}


