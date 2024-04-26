package it.polito.wa2.g07.crm.dtos

import it.polito.wa2.g07.crm.entities.Dwelling
import it.polito.wa2.g07.crm.entities.Email
import it.polito.wa2.g07.crm.entities.Message
import it.polito.wa2.g07.crm.entities.Telephone


data class ReducedMessageDTO (
        val id: Long,
        val subject: String,
        val sender:AddressDTO,
)

fun Message.toReducedDTO():ReducedMessageDTO{
        return ReducedMessageDTO(this.messageID,this.subject,this.sender.toAddressDTO())
}


