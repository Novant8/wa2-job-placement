package it.polito.wa2.g07.crm.entities

import jakarta.persistence.*
import java.time.LocalDate
import java.time.LocalDateTime

enum class MessageChannel(channel: String) {
    PHONE_CALL("Phone_call"),
    TEXT_MESSAGE("Text_message"),
    EMAIL("Email")
}

@Entity
class Message {
    @Id
    @GeneratedValue
    var messageID: Long = 0

    @OneToOne
    lateinit var sender: Address

    lateinit var subject: String
    lateinit var body: String

    var priority: Int = 0

    lateinit var creationTimestamp: LocalDateTime

    @OneToMany(mappedBy = "message")
    val events: MutableSet<MessageEvent> = mutableSetOf()

    fun addEvent(e: MessageEvent) {
        e.message = this
        this.events.add(e)
    }
}