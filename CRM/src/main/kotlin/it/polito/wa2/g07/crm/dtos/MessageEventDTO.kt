package it.polito.wa2.g07.crm.dtos

import com.fasterxml.jackson.annotation.JsonInclude
import it.polito.wa2.g07.crm.entities.Message
import it.polito.wa2.g07.crm.entities.MessageEvent
import it.polito.wa2.g07.crm.entities.MessageStatus
import java.time.LocalDateTime


@JsonInclude(JsonInclude.Include.NON_NULL)
data class MessageEventDTO(
    var status: MessageStatus,
    var timestamp: LocalDateTime?, // È NULLO COSÌ LO RIUSO PER LA POST DI MESSAGE_EVENT,
    var comments: String? = null
)

fun MessageEvent.toMessageEventDTO() : MessageEventDTO {
    return MessageEventDTO(status=this.status, timestamp=this.timestamp, comments = this.comments)

}