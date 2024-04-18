package it.polito.wa2.g07.crm.services


import it.polito.wa2.g07.crm.dtos.*
import it.polito.wa2.g07.crm.entities.*
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

):MessageService {


    override fun getMessages(pageable: Pageable): Page<ReducedMessageDTO>{
      return  messageRepository.findAll(pageable).map { m->m.toReducedDTO(); }
    }
    override fun createMessage(msg: MessageCreateDTO):MessageDTO?{

        val m = Message()
        when (msg.channel) {

            "email" -> {
                val addr = addressRepository.findMailAddressByMail(msg.sender)
                if (! addr.isPresent) {
                    println("EMAIL NOT FOUND")
                    val email = Email()
                    email.email=msg.sender
                    m.sender=email
                    addressRepository.saveAndFlush(m.sender)
                } else{
                    println("EMAIL FOUND:"+addr.get().email)
                    m.sender = addr.get()
                    //addressRepository.saveAndFlush(m.sender)
                }


            }
            "dwelling" -> {
                val dwelling = Dwelling()
                dwelling.city = msg.sender /// da parsificare meglio
            }
            "telephone" -> {
                val addr = addressRepository.findTelephoneAddressByTelephoneNumber(msg.sender)
                if (! addr.isPresent) {
                    println("TELEPHONE NOT FOUND")
                    val telephone = Telephone()
                    telephone.number=msg.sender
                    m.sender=telephone
                    addressRepository.saveAndFlush(m.sender)
                } else{
                    println("TELEPHONE FOUND:"+addr.get().number)
                    m.sender = addr.get()

                }



            }
        }
        m.subject=msg.subject
        m.body=msg.body
       return messageRepository.save(m).toMessageDTO()

    }

}