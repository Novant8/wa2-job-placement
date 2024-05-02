package it.polito.wa2.g07.crm.entities


import it.polito.wa2.g07.crm.dtos.MessageChannel
import jakarta.persistence.*

import java.time.LocalDateTime


@Entity
class Message(
    var subject: String,
    var body: String,
    @ManyToOne val sender: Address,
    val channel: MessageChannel,
    var priority: Int = 0,
    var creationTimestamp: LocalDateTime = LocalDateTime.now(),
) {

    @Id
    @GeneratedValue
    var messageID: Long = 0

    @OneToMany(mappedBy = "message",cascade = [CascadeType.ALL])
    var events: MutableSet<MessageEvent> = mutableSetOf()

    fun addEvent (a:MessageEvent){
        // println(events)
        events.add(a)
    }

}