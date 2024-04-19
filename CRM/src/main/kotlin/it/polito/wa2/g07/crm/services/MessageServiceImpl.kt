package it.polito.wa2.g07.crm.services


import it.polito.wa2.g07.crm.dtos.*
import it.polito.wa2.g07.crm.entities.*
import it.polito.wa2.g07.crm.repositories.MessageRepository
import org.springframework.data.domain.Pageable

import org.springframework.stereotype.Service
import org.springframework.data.domain.Page


import it.polito.wa2.g07.crm.repositories.*
import java.time.LocalDateTime
import java.time.LocalTime

@Service
class MessageServiceImpl(
    private val messageRepository: MessageRepository,
    private val contactRepository: ContactRepository,
    private val messageEventRepository: MessageEventRepository,
    private val addressRepository: AddressRepository,

):MessageService {


    override fun getMessages(pageable: Pageable): Page<ReducedMessageDTO>{
      return  messageRepository.findAll(pageable).map { m->m.toReducedDTO(); }
    }
    override fun createMessage(msg: MessageCreateDTO) : MessageDTO? {

        val sender: Address = when (msg.channel) {
            "email" -> {
                val addr = addressRepository.findMailAddressByMail(msg.sender)
                if (!addr.isPresent) {
                    println("EMAIL NOT FOUND")
                    Email(msg.sender)
                } else {
                    println("EMAIL FOUND:"+addr.get().email)
                    addr.get()
                }
            }
            "dwelling" -> {
                Dwelling("", msg.sender, "", "") /// da parsificare meglio
            }
            "telephone" -> {
                val addr = addressRepository.findTelephoneAddressByTelephoneNumber(msg.sender)
                if (! addr.isPresent) {
                    println("TELEPHONE NOT FOUND")
                    Telephone(msg.sender)
                } else {
                    println("TELEPHONE FOUND:"+addr.get().number)
                    addr.get()
                }
            }
            else -> TODO("THROW EXCEPTION HERE")
        }
        addressRepository.saveAndFlush(sender)
        val m = Message(msg.subject, msg.body, sender)
        val event = MessageEvent(m,MessageStatus.RECEIVED, LocalDateTime.now())
        m.addEvent(event)
        val result = messageRepository.saveAndFlush(m).toMessageDTO()
        messageEventRepository.saveAndFlush(event)
        return result
    }

}