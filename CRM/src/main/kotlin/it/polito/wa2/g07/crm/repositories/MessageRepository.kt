package it.polito.wa2.g07.crm.repositories


import it.polito.wa2.g07.crm.entities.Message
import it.polito.wa2.g07.crm.entities.MessageEvent
import it.polito.wa2.g07.crm.entities.MessageStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface MessageRepository:JpaRepository<Message,Long> {
    @Query( "SELECT m " +
            "FROM MessageEvent m " +
            "WHERE m.message.messageID = :messageId " +
            "ORDER BY m.timestamp DESC LIMIT 1")
    fun getLastEventByMessageId(messageId: Long): MessageEvent?



    @Query("SELECT m.events FROM Message m WHERE m.messageID = :messageID")
    fun getEventsByMessageID(messageID: Long,pageable: Pageable): Page<MessageEvent>

}