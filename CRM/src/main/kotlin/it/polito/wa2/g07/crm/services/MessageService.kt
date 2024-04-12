package it.polito.wa2.g07.crm.services

import it.polito.wa2.g07.crm.dtos.MessageCreateDTO
import it.polito.wa2.g07.crm.dtos.MessageDTO
import it.polito.wa2.g07.crm.dtos.ReducedMessageDTO
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Page

interface MessageService {
    fun getMessages(pageable: Pageable): Page<ReducedMessageDTO>
    fun createMessage(msg:MessageCreateDTO): MessageDTO
}