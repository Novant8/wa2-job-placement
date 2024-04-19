package it.polito.wa2.g07.crm.entities

import com.fasterxml.jackson.databind.JsonSerializer.None
import jakarta.persistence.*
import java.time.LocalDate
import java.time.LocalDateTime

enum class MessageChannel {
    PHONE_CALL,
    TEXT_MESSAGE,
    EMAIL
}

@Entity
class Message(
        var subject: String,
        var body: String,
        @ManyToOne var sender: Address? = null,
        var priority: Int = 0,
        var creationTimestamp: LocalDateTime = LocalDateTime.now(),
) {

    @Id
    @GeneratedValue
    var messageID: Long = 0

    @OneToMany(mappedBy = "message")
    val events: MutableSet<MessageEvent> = mutableSetOf()

    fun addEvent(e: MessageEvent) {
        e.message = this
        this.events.add(e)
    }
}