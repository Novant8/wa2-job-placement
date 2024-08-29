package it.polito.wa2.g07.crm.dtos.lab02

import it.polito.wa2.g07.crm.entities.lab02.Message
import java.time.LocalDateTime



data class MessageKafkaDTO(
    val id:Long,
    val channel : String,
    val priority: Int,
    var creationTimestamp: LocalDateTime?= null,
    var status: String?= null,
    var statusTimestamp: LocalDateTime?= null,
)

fun Message.toMessageKafkaDTO(): MessageKafkaDTO =
    MessageKafkaDTO(
        this.messageID,
        this.channel.toString(),
        this.priority,
        this.creationTimestamp,
        this.events.sortedBy { it.timestamp }.reversed().map{ it.toMessageEventDTO()}[0] .status.toString(),
        this.events.sortedBy { it.timestamp }.reversed().map{ it.toMessageEventDTO()}[0] .timestamp,
    )