package it.polito.wa2.g07.crm.dtos

import it.polito.wa2.g07.crm.entities.*


data class ReducedMessageDTO (
        val id: Long,
        val subject: String,
        val sender:AddressDTO,
        val channel: MessageChannel
)

fun Message.toReducedDTO():ReducedMessageDTO{
        return ReducedMessageDTO(this.messageID,this.subject,this.sender.toAddressDTO(),
            this.channel
        )
}


