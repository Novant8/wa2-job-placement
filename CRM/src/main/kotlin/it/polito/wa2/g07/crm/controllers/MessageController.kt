package it.polito.wa2.g07.crm.controllers

import it.polito.wa2.g07.crm.dtos.MessageDTO
import it.polito.wa2.g07.crm.dtos.ReducedMessageDTO
import it.polito.wa2.g07.crm.dtos.MessageCreateDTO
import it.polito.wa2.g07.crm.dtos.MessageEventDTO
import it.polito.wa2.g07.crm.services.ContactService
import it.polito.wa2.g07.crm.services.MessageService
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.query.Param
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("API/messages")
class MessageController (private val messageService: MessageService,
                         private val contactService: ContactService
                        ) {


    @GetMapping("","/")
    fun getMessages(pageable: Pageable):Page<ReducedMessageDTO>{
        return messageService.getMessages(pageable);
    }

    @PostMapping("","/", )
    fun createNewMessage(msg: MessageCreateDTO):MessageDTO?{
       // sender, channel, subject, body
        return messageService.createMessage(msg)
    }

    /*POST /API/messages/{messageId} – change the state of a specific
    message. This endpoint must receive the target state and possibly a
    comment to enrich the history of actions on the message. Manage the
    case where the new state is not an allowed one for the message */
    @PostMapping("/{id_message}","/{id_message}/")
    fun updateMessageState(@PathVariable("id_message") idMessage: Long,
                            event:MessageEventDTO ):MessageEventDTO?{
        return messageService.updateStatus(idMessage, event)
    }




    //retrieve the list of state changes, with their comments, for a specific message
    @GetMapping("/messages/{messageId}/history","/messages/{messageId}/history/")
    fun retrieveMessageStateChanges()
    {

    }

    //modify the priority value of a message
    @PutMapping("/messages/{messageId}/priority","/messages/{messageId}/priority/")
    fun modifyMessagePriority()
    {

    }
















}

