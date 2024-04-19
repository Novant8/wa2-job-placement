package it.polito.wa2.g07.crm.services


import it.polito.wa2.g07.crm.dtos.*
import it.polito.wa2.g07.crm.entities.*
import it.polito.wa2.g07.crm.exceptions.InvalidParamsException
import it.polito.wa2.g07.crm.exceptions.MessageNotFoundException
import it.polito.wa2.g07.crm.repositories.MessageRepository
import org.springframework.data.domain.Pageable

import org.springframework.stereotype.Service
import org.springframework.data.domain.Page


import it.polito.wa2.g07.crm.repositories.*
import org.springframework.data.jpa.domain.AbstractPersistable_.id
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
        addressRepository.save(sender)
        val m = Message(msg.subject, msg.body, sender)
        val event = MessageEvent(m,MessageStatus.RECEIVED, LocalDateTime.now())
        val result = messageRepository.save(m).toMessageDTO()
        messageEventRepository.save(event)
        return result
    }

    private fun checkNewStatusValidity(new_status:MessageStatus, old_status:MessageStatus):Boolean{
        //check if the new state is reachable starting from the actual status

        return when(old_status){
            MessageStatus.RECEIVED ->  new_status == MessageStatus.READ

            MessageStatus.READ -> new_status != MessageStatus.READ && new_status != MessageStatus.RECEIVED
            MessageStatus.DISCARDED -> return false //no status update when the message is discarded
            MessageStatus.PROCESSING -> new_status==MessageStatus.DONE || new_status==MessageStatus.FAILED
            MessageStatus.DONE -> return false
            MessageStatus.FAILED -> return false
        }

    }

   override fun updateStatus(id_msg : String, event_data: MessageEventDTO): MessageEventDTO? {

       val new_status = MessageStatus.valueOf(event_data.status.toString().uppercase())//TODO: eccezione sul valueOF


       val msg = messageRepository.findById(id_msg.toLong())
       val old_status = messageEventRepository.getLastEventByMessageId(id_msg.toLong())
       if (msg == null || old_status==null){
           throw  MessageNotFoundException("The message doesn't exist")
       }
       if (!checkNewStatusValidity(new_status, old_status)){
           throw  InvalidParamsException("The new status is not appliable on the message")
       }


       var date: LocalDateTime
        if (event_data.timestamp == null) {
            date=   LocalDateTime.now()
       } else{
            date= event_data.timestamp!!
       }
       return messageEventRepository.save(MessageEvent(msg.get(),new_status,date,event_data.comments)).ToMessageEventDTO()

   }


}