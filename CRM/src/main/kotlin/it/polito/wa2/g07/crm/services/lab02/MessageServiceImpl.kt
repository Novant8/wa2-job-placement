package it.polito.wa2.g07.crm.services.lab02


import com.nimbusds.jose.shaded.gson.JsonObject
import it.polito.wa2.g07.crm.dtos.lab02.*
import it.polito.wa2.g07.crm.entities.lab02.*
import it.polito.wa2.g07.crm.exceptions.InvalidParamsException
import it.polito.wa2.g07.crm.exceptions.MessageNotFoundException
import it.polito.wa2.g07.crm.repositories.lab02.AddressRepository
import it.polito.wa2.g07.crm.repositories.lab02.ContactRepository
import it.polito.wa2.g07.crm.repositories.lab02.MessageRepository
import it.polito.wa2.g07.crm.services.lab02.ContactServiceImpl.Companion.logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime


@Service
class MessageServiceImpl(
    private val messageRepository: MessageRepository,
    private val addressRepository: AddressRepository,
    private val contactRepository: ContactRepository,
    private val kafkaTemplate: KafkaTemplate<String, MessageKafkaDTO>
    ): MessageService {


    @Transactional(readOnly = true)
    override fun getMessage(messageID: Long): MessageDTO? {
        val msg=  messageRepository.findById(messageID)
        if (msg.isEmpty) {
            logger.info("The message doesn't exist" )
            throw MessageNotFoundException("The message doesn't exist")
        }
        logger.info("Message found")
        return msg.get().toMessageDTO()
    }

    @Transactional(readOnly = true)
    override fun getMessages(filterBy: List<MessageStatus>?, pageable: Pageable): Page<MessageDTO>{ ///*ReducedMessageDTO*/

        val result = when (filterBy) {
            null ->    messageRepository.findAll(pageable)
            else ->    messageRepository.findAllByStatus(filterBy!!,pageable)

        }

        return result.map { m->m.toMessageDTO();/*toReducedDTO();*/ }
    }

    @Transactional
    override fun createMessage(msg: MessageCreateDTO) : MessageDTO? {


        val sender: Address = when (msg.sender) {
            is EmailDTO -> {
                val addr = addressRepository.findMailAddressByMail(msg.sender.email)
                if (!addr.isPresent) {
                    logger.info("Email NOT found -> Creating a new Contact")
                    val c = Contact("Auto-generated","Auto-generated", ContactCategory.UNKNOWN )
                    val addr= addressRepository.save(Email(msg.sender.email))
                    c.addAddress(addr)
                    val contactDTO = contactRepository.save(c).toContactDto()
                    logger.info("Created automatically Contact #${contactDTO.id}.")
                    addr
                } else {
                    logger.info("Email found: "+addr.get().email)
                    addr.get()
                }
            }
            is DwellingDTO -> {

                val addr = addressRepository.findDwellingAddressByStreet(
                    msg.sender.street,
                    msg.sender.city,
                    msg.sender.district,
                    msg.sender.country)
                if (!addr.isPresent) {

                    logger.info("Dwelling NOT found -> Creating a new Contact")
                    val c = Contact("Auto-generated","Auto-generated", ContactCategory.UNKNOWN )
                    val addr= addressRepository.save( Dwelling(     msg.sender.street,
                                                                    msg.sender.city,
                                                                    msg.sender.district,
                                                                    msg.sender.country)
                    )

                    c.addAddress(addr)
                    val contactDTO = contactRepository.save(c).toContactDto()
                    logger.info("Created automatically Contact #${contactDTO.id}.")
                    addr


                } else {
                    logger.info("Dwelling found:"+addr.get().street+", "+addr.get().city)
                    addr.get()
                }
            }
            is TelephoneDTO -> {
                val addr = addressRepository.findTelephoneAddressByTelephoneNumber(msg.sender.phoneNumber)
                if (! addr.isPresent) {
                    logger.info("Dwelling NOT found -> Creating a new Contact")
                    val c = Contact("Auto-generated","Auto-generated", ContactCategory.UNKNOWN )
                    val addr= addressRepository.save( Telephone(msg.sender.phoneNumber))

                    c.addAddress(addr)
                    val contactDTO = contactRepository.save(c).toContactDto()
                    logger.info("Created automatically Contact #${contactDTO.id}.")
                    addr

                } else {
                    logger.info("Telephone found "+addr.get().number)
                    addr.get()
                }
            }
            else -> error("Not supported DTO")
        }
        //addressRepository.save(sender)
        val m = Message(msg.subject, msg.body, sender, MessageChannel.valueOf(msg.channel.uppercase()))
        val event = MessageEvent(m, MessageStatus.RECEIVED, LocalDateTime.now())
        m.addEvent(event)

        val result = messageRepository.save(m)

        kafkaTemplate.send("MESSAGE",result.toMessageKafkaDTO() )
        return result.toMessageDTO()


    }

    private fun checkNewStatusValidity(new_status: MessageStatus, old_status: MessageStatus):Boolean{
        //check if the new state is reachable starting from the actual status

        return when(old_status){
            MessageStatus.RECEIVED ->  new_status == MessageStatus.READ
            MessageStatus.READ -> new_status != MessageStatus.READ && new_status != MessageStatus.RECEIVED
            MessageStatus.DISCARDED -> return false //no status update when the message is discarded
            MessageStatus.PROCESSING -> new_status== MessageStatus.DONE || new_status== MessageStatus.FAILED
            MessageStatus.DONE -> return false
            MessageStatus.FAILED -> return false
        }

    }

    @Transactional
    override fun updateStatus(id_msg : Long, event_data: MessageEventDTO): MessageEventDTO? {

       val result = messageRepository.findById(id_msg)
       val old_status = messageRepository.getLastEventByMessageId(id_msg)
        if (result.isEmpty || old_status==null){
            logger.info("The message doesn't exist")
           throw  MessageNotFoundException("The message doesn't exist")
       }
       val msg=result.get()
       if (!checkNewStatusValidity(event_data.status, old_status.status)){
           logger.info("The status cannot be assigned to the message")
           throw  InvalidParamsException("The status cannot be assigned to the message")
       }

       val date: LocalDateTime
        if (event_data.timestamp == null) {
            date=   LocalDateTime.now()
       } else{
            date= event_data.timestamp!!
       }
        logger.info("The status has been assigned to the message")
        val m_event = MessageEvent(msg,event_data.status,date,event_data.comments)
        msg.addEvent(m_event)
        kafkaTemplate.send("MESSAGE", msg.toMessageKafkaDTO())
        return m_event.toMessageEventDTO()
   }

    @Transactional(readOnly = true)
    override fun getHistory(id_msg: Long, pageable: Pageable): Page<MessageEventDTO> {
        if (messageRepository.findById(id_msg).isEmpty){
            logger.info("Message NOT found")
            throw  MessageNotFoundException("The message doesn't exist")
        }
        logger.info("Message found")
        return messageRepository.getEventsByMessageID(id_msg,pageable).map { it.toMessageEventDTO() }
    }

    @Transactional
    override fun changePriority(messageId: Long,priority:Int): MessageDTO?{
        val message = messageRepository.findById(messageId)
        if (message.isEmpty){
            logger.info("Message NOT found")
            throw  MessageNotFoundException("The message doesn't exist")
        }
        var msg  = message.get()
        msg.priority = priority
        var result = messageRepository.save(msg)
        kafkaTemplate.send("MESSAGE",result.toMessageKafkaDTO() )
        return result.toMessageDTO()

    }


}