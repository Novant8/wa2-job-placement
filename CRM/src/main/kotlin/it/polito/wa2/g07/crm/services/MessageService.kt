package it.polito.wa2.g07.crm.services

import it.polito.wa2.g07.crm.dtos.*

import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Page

interface MessageService {
    fun getMessages(pageable: Pageable): Page<ReducedMessageDTO>
    fun getMessage(messageID:Long): ReducedMessageDTO

    fun createMessage(msg:MessageCreateDTO):MessageDTO?
    fun updateStatus(id_msg : Long, event_data: MessageEventDTO): MessageEventDTO?
    fun getHistory(id_msg: Long, pageable: Pageable): Page<MessageEventDTO>
    fun changePriority(messageId: Long,priority:Int): MessageDTO?
}