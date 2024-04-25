package it.polito.wa2.g07.crm.dtos

import it.polito.wa2.g07.crm.entities.*
import java.time.LocalDateTime


data class MessageDTO(
    val id:Long,
    val sender: AddressDTO,
    val subject: String,
    val body: String,
    val priority: Int,
    val creationTimestamp: LocalDateTime,
    val events: MutableSet<MessageEventDTO>
)

fun Message.toMessageDTO(): MessageDTO=
    MessageDTO(
        this.messageID,
        this.sender.toAddressDTO(),
        this.subject,
        this.body,
        this.priority,
        this.creationTimestamp,
        this.events.map{ it.toMessageEventDTO()}.toMutableSet()

    )