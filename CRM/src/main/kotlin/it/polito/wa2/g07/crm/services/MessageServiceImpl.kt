package it.polito.wa2.g07.crm.services

import
import it.polito.wa2.g07.crm.repositories.MessageRepository
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import org.springframework.data.domain.Page
import it.polito.wa2.g07.crm.dtos.ReducedMessageDTO
@Service
class MessageServiceImpl (private val messageRepository: MessageRepository ):MessageService {


    override fun getMessages(pageable: Pageable): Page<ReducedMessageDTO>{
      return  messageRepository.findAll(pageable).map { m->m.toReducedDTO(); }
    }


}