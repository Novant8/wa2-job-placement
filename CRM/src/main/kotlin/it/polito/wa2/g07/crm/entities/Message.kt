package it.polito.wa2.g07.crm.entities


import jakarta.persistence.*

import java.time.LocalDateTime


@Entity
class Message(
    var subject: String,
    var body: String,
    @ManyToOne val sender: Address,
    var priority: Int = 0,
    var creationTimestamp: LocalDateTime = LocalDateTime.now(),
) {

    @Id
    @GeneratedValue
    var messageID: Long = 0

    @OneToMany(cascade = [CascadeType.ALL])
    var events: MutableSet<MessageEvent> = mutableSetOf()

    fun addEvent (a:MessageEvent){
        // println(events)
        events.add(a)
    }

}