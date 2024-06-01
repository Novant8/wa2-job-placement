package it.polito.wa2.g07.crm.entities.lab02

import jakarta.persistence.*
import java.time.LocalDateTime

enum class MessageStatus{
    RECEIVED,
    READ,
    DISCARDED,
    PROCESSING,
    DONE,
    FAILED
}

@Entity
class MessageEvent(
    @ManyToOne
    var message: Message,
    var status: MessageStatus,
    var timestamp: LocalDateTime,
    var comments: String? = null
) {

    @Id
    @GeneratedValue
    var messageEventId : Long = 0
}