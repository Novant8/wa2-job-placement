package it.polito.wa2.g07.crm.entities

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
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

    @ManyToOne
    lateinit var sender: Contact
    lateinit var channel: MessageChannel

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