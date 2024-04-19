package it.polito.wa2.g07.crm.dtos

import it.polito.wa2.g07.crm.entities.*
import java.time.LocalDateTime

data class MessageDTO(
    val id:Long,
    val sender: Address,
    val channel: MessageChannel,
    val subject: String,
    val body: String,
    val priority: Int,
    val creationTimestamp: LocalDateTime,
    val events: MutableSet<MessageEvent>
)

fun Message.toMessageDto(): MessageDTO=
    MessageDTO(
        this.messageID,
        this.sender,
        this.channel,
        this.subject,
        this.body,
        this.priority,
        this.creationTimestamp,
        this.events
    )
