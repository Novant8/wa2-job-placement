package it.polito.wa2.g07.crm.repositories

import it.polito.wa2.g07.crm.entities.Contact
import it.polito.wa2.g07.crm.entities.MessageEvent
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
@Repository
interface MessageEventRepository : JpaRepository<MessageEvent, Long> {

}
