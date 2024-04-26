package it.polito.wa2.g07.crm.controllers


import it.polito.wa2.g07.crm.dtos.*
import it.polito.wa2.g07.crm.exceptions.InvalidParamsException
import it.polito.wa2.g07.crm.services.ContactService
import it.polito.wa2.g07.crm.services.MessageService
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable


import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("API/messages")
class MessageController (private val messageService: MessageService
                        ) {


    @GetMapping("","/")
    fun getMessages(pageable: Pageable):Page<ReducedMessageDTO>{
        return messageService.getMessages(pageable)
    }

    @PostMapping("","/", )
    fun createNewMessage(@RequestBody msg: MessageCreateDTO):MessageDTO?{
       // sender, channel, subject, body
        return messageService.createMessage(msg)
    }


    /*POST /API/messages/{messageId} – change the state of a specific
    message. This endpoint must receive the target state and possibly a
    comment to enrich the history of actions on the message. Manage the
    case where the new state is not an allowed one for the message */
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/{id_message}","/{id_message}/")
    fun updateMessageState(@PathVariable("id_message") idMessage: Long,
                            event:MessageEventDTO ):MessageEventDTO?{
        return messageService.updateStatus(idMessage, event)
    }
  //Getting a specific message
    @GetMapping("/{messageId}", "/{messageId}/")
    fun getMessageById(@PathVariable("messageId") messageId: Long) : MessageDTO? {
        return  messageService.getMessage(messageId)

    }


    //retrieve the list of state changes, with their comments, for a specific message
    @GetMapping("/{messageId}/history","/{messageId}/history/")
    fun retrieveMessageStateChanges(@PathVariable("messageId") messageId: Long,
                                    pageable: Pageable): Page<MessageEventDTO>
    {
        return messageService.getHistory(messageId,pageable)
    }

    //modify the priority value of a message
    @PutMapping("/{messageId}/priority","/{messageId}/priority/")
    fun modifyMessagePriority(@PathVariable("messageId") messageId: Long,
                              @RequestBody priority :Map<String, Int>
                              ): MessageDTO?
    {
        if (priority["priority"] ==null) throw InvalidParamsException("Priority cannot be empty")
        if (priority["priority"]!! <0){
            throw InvalidParamsException("Priority can not be negative")
        }
        return messageService.changePriority(messageId,priority["priority"]!! )
    }
















}

