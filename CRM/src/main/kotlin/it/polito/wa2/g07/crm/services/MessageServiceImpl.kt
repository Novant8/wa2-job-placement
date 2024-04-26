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
import it.polito.wa2.g07.crm.services.ContactServiceImpl.Companion.logger
import jakarta.transaction.Transactional

import java.time.LocalDateTime


@Service
class MessageServiceImpl(
    private val messageRepository: MessageRepository,
    private val addressRepository: AddressRepository,

    ):MessageService {


    @Transactional
    override fun getMessage(messageID: Long): MessageDTO? {
        val msg=  messageRepository.getMessageByMessageID(messageID)

        if (msg == null) {
            logger.info("The message doesn't exist" )
            throw MessageNotFoundException("The message doesn't exist")
        }
        logger.info("Message found")
        return msg.toMessageDTO()
    }

    override fun getMessages(pageable: Pageable): Page<ReducedMessageDTO>{
        return  messageRepository.findAll(pageable).map { m->m.toReducedDTO(); }
    }
    @Transactional
    override fun createMessage(msg: MessageCreateDTO) : MessageDTO? {

        val sender: Address = when (msg.sender) {
            is EmailDTO -> {
                val addr = addressRepository.findMailAddressByMail(msg.sender.email)
                if (!addr.isPresent) {
                    logger.info("Email NOT found")
                    Email(msg.sender.email)
                } else {
                    logger.info("Email found: "+addr.get().email)
                    addr.get()
                }
            }
            is DwellingDTO -> {

                val addr = addressRepository.findDwellingAddressByAddress(  msg.sender.street,
                    msg.sender.city,
                    msg.sender.district,
                    msg.sender.country)
                if (!addr.isPresent) {
                    logger.info("Dwelling NOT found")
                    Dwelling(    msg.sender.street,
                                msg.sender.city,
                                msg.sender.district,
                                msg.sender.country)
                } else {
                    logger.info("Dwelling found:"+addr.get().street+", "+addr.get().city)
                    addr.get()
                }
            }
            is TelephoneDTO -> {
                val addr = addressRepository.findTelephoneAddressByTelephoneNumber(msg.sender.phoneNumber)
                if (! addr.isPresent) {
                    logger.info("Telephone found")
                    Telephone(msg.sender.phoneNumber)
                } else {
                    logger.info("Telephone found "+addr.get().number)
                    addr.get()
                }
            }
            else -> TODO("THROW EXCEPTION HERE")
        }
        addressRepository.save(sender)
        val m = Message(msg.subject, msg.body, sender)
        val event = MessageEvent(m,MessageStatus.RECEIVED, LocalDateTime.now())
        m.addEvent(event)
        return messageRepository.save(m).toMessageDTO()



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

    @Transactional
   override fun updateStatus(id_msg : Long, event_data: MessageEventDTO): MessageEventDTO? {

       val result = messageRepository.findById(id_msg)
       val old_status = messageRepository.getLastEventByMessageId(id_msg)
        if (result.isEmpty || old_status==null){
           throw  MessageNotFoundException("The message doesn't exist")
       }
       val msg=result.get()
       if (!checkNewStatusValidity(event_data.status, old_status.status)){
           throw  InvalidParamsException("The status cannot be assigned to the message")
       }

       val date: LocalDateTime
        if (event_data.timestamp == null) {
            date=   LocalDateTime.now()
       } else{
            date= event_data.timestamp!!
       }

       val m_event = MessageEvent(msg,event_data.status,date,event_data.comments)
       msg.addEvent(m_event)
        return m_event.toMessageEventDTO()
   }
    override fun getHistory(id_msg: Long, pageable: Pageable): Page<MessageEventDTO> {
        if (messageRepository.findById(id_msg).isEmpty){
            logger.info("Message NOT found")
            throw  MessageNotFoundException("The message doesn't exist")
        }
        logger.info("Message found")
        return messageRepository.getEventsByMessageID(id_msg,pageable).map { it.toMessageEventDTO() }
    }
    override fun changePriority(messageId: Long,priority:Int): MessageDTO?{
        val message = messageRepository.findById(messageId)
        if (message.isEmpty){
            logger.info("Message NOT found")
            throw  MessageNotFoundException("The message doesn't exist")
        }
        var msg  = message.get()
        msg.priority = priority
        return messageRepository.save(msg).toMessageDTO()

    }


}