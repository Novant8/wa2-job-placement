package it.polito.wa2.g07.crm.controllers


import it.polito.wa2.g07.crm.dtos.*
import it.polito.wa2.g07.crm.entities.AddressType
import it.polito.wa2.g07.crm.entities.MessageStatus
import it.polito.wa2.g07.crm.exceptions.InvalidParamsException
import it.polito.wa2.g07.crm.services.ContactService
import it.polito.wa2.g07.crm.services.MessageService
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable


import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("API/messages")
class MessageController (private val messageService: MessageService
                        ) {


    @GetMapping("","/")
    fun getMessages(@RequestParam("filterBy", required = false) filterByStr: List<String>? = null,
                    pageable: Pageable):Page<ReducedMessageDTO>{

        if (filterByStr == null  ){  return messageService.getMessages(null,pageable = pageable)}
        if (filterByStr.isEmpty() ) {  throw InvalidParamsException("'$filterByStr' is not a valid filter. Possible filters: ${MessageStatus.entries}.") }

        val filterBy = try {
            filterByStr.map{MessageStatus.valueOf(it.uppercase())}
        } catch (e: IllegalArgumentException) {
                throw InvalidParamsException("'$filterByStr' is not a valid filter. Possible filters: ${MessageStatus.entries}.")
        }
        return messageService.getMessages(filterBy=filterBy, pageable = pageable)


    }
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("","/", )
    fun createNewMessage(@RequestBody @Valid  msg: MessageCreateDTO):MessageDTO?{
       // sender, channel, subject, body
        try {
            AddressType.valueOf(msg.channel.uppercase())
        }catch (e : IllegalArgumentException) {
            throw InvalidParamsException("'$msg.channel' is not a valid address channel. Possible filters: ${AddressType.entries}.")
        }
        if (msg.sender.addressType != AddressType.valueOf(msg.channel.toString().uppercase())){
            throw InvalidParamsException("Sender's fields are incompatible with channel type")
        }
         return messageService.createMessage(msg)
    }


    /*POST /API/messages/{messageId} – change the state of a specific
    message. This endpoint must receive the target state and possibly a
    comment to enrich the history of actions on the message. Manage the
    case where the new state is not an allowed one for the message */
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/{id_message}","/{id_message}/")
    fun updateMessageState(@PathVariable("id_message") idMessage: Long,
                            @RequestBody   event:MessageEventDTO ):MessageEventDTO?{
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

