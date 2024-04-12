package it.polito.wa2.g07.crm.services


import it.polito.wa2.g07.crm.dtos.*
import it.polito.wa2.g07.crm.repositories.MessageRepository
import org.springframework.data.domain.Pageable

import org.springframework.stereotype.Service
import org.springframework.data.domain.Page
import it.polito.wa2.g07.crm.entities.Contact
import it.polito.wa2.g07.crm.entities.Message

import it.polito.wa2.g07.crm.repositories.ContactRepository

@Service
class MessageServiceImpl (private val messageRepository: MessageRepository,
                          private val contactRepository: ContactRepository

                ):MessageService {


    override fun getMessages(pageable: Pageable): Page<ReducedMessageDTO>{
      return  messageRepository.findAll(pageable).map { m->m.toReducedDTO(); }
    }
    override fun createMessage(msg: MessageCreateDTO):MessageDTO{

        /* IMMAGINO CHE CI SIA UN CONTROLLO CHE TRIGGERI SE NON ESISTE*/
        //eg.
        //val contact = contactRepositoryAddIfNotExist(msg.sender)
        //per adesso creo un nuovo sender tutte le volte.....

        val m = Message();
        m.sender= Contact()
        contactRepository.save(m.sender)
        m.subject=msg.subject
        m.body=msg.body
            return messageRepository.save(m).toMessageDTO()
    }

}