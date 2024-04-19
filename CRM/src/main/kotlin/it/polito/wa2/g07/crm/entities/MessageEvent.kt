package it.polito.wa2.g07.crm.entities

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne
import java.time.LocalDateTime

enum class MessageStatus (status: String){
    RECEIVED("Received"),
    READ("Read"),
    DISCARDED("Discarded"),
    PROCESSING("Processing"),
    DONE("Done"),
    FAILED("Failed")
}

@Entity
class MessageEvent(
        @ManyToOne var message: Message,
        var status: MessageStatus,
        var timestamp: LocalDateTime,
        var comments: String? = null
) {
    init {
        message.events.add(this)
    }

    @Id
    @GeneratedValue
    var messageEventId : Long = 0
}