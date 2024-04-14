package it.polito.wa2.g07.crm.services


import it.polito.wa2.g07.crm.dtos.*
import it.polito.wa2.g07.crm.entities.Address
import it.polito.wa2.g07.crm.entities.Dwelling
import it.polito.wa2.g07.crm.entities.Message
import it.polito.wa2.g07.crm.repositories.MessageRepository
import org.springframework.data.domain.Pageable

import org.springframework.stereotype.Service
import org.springframework.data.domain.Page


import it.polito.wa2.g07.crm.repositories.*

@Service
class MessageServiceImpl(
    private val messageRepository: MessageRepository,
    private val contactRepository: ContactRepository,
                          //private val addressRepository: AddressRepository,
    private val addressRepository: AddressRepository,
    private val dwellingRepository: DwellingRepository,

):MessageService {


    override fun getMessages(pageable: Pageable): Page<ReducedMessageDTO>{
      return  messageRepository.findAll(pageable).map { m->m.toReducedDTO(); }
    }
    override fun createMessage(msg: MessageCreateDTO):MessageDTO?{


        val m = Message()
        m.sender=Dwelling()
        addressRepository.save(m.sender)
       return messageRepository.save(m).toMessageDTO()

    }

}