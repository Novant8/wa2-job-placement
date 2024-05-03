package it.polito.wa2.g07.crm.repositories.lab02


import it.polito.wa2.g07.crm.entities.lab02.Message
import it.polito.wa2.g07.crm.entities.lab02.MessageEvent
import it.polito.wa2.g07.crm.entities.lab02.MessageStatus
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
            "ORDER BY m.timestamp DESC LIMIT 1"  )
    fun getLastEventByMessageId(messageId: Long): MessageEvent?

    @Query (""" 
                SELECT ma
                FROM Message ma
                where   (
                        SELECT m.status  FROM MessageEvent m
                        where m.message.messageID = ma.messageID
                        ORDER BY m.timestamp DESC LIMIT 1
                )IN :filter
                
        """, countQuery = """ 
                SELECT count(ma)
                FROM Message ma
                where  (
                        SELECT m.status  FROM MessageEvent m
                        where m.message.messageID = ma.messageID
                        ORDER BY m.timestamp DESC LIMIT 1
                )IN :filter 
        """)
    fun findAllByStatus(filter: List<MessageStatus>, pageable: Pageable):Page<Message?>

    @Query("SELECT events FROM Message m ,MessageEvent events " +
            "WHERE events.message.messageID = :messageID AND m.messageID= :messageID " +
            "ORDER BY events.timestamp desc ")
    fun getEventsByMessageID(messageID: Long,pageable: Pageable): Page<MessageEvent>

}