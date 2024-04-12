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
class MessageEvent {
    @Id
    @GeneratedValue
    var messageEventId : Long = 0

    lateinit var status: MessageStatus
    lateinit var timestamp: LocalDateTime

    var comments: String? = null

    @ManyToOne
    var message: Message? = null
}