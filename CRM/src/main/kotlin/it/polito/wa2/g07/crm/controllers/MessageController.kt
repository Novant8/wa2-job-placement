package it.polito.wa2.g07.crm.controllers

import it.polito.wa2.g07.crm.dtos.ReducedMessageDTO
import it.polito.wa2.g07.crm.services.MessageService
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.net.http.HttpHeaders

@RestController
@RequestMapping("API/messages")
class MessageController (private val messageService: MessageService) {


    @GetMapping("","/")
    fun getMessages(pageable: Pageable):Page<ReducedMessageDTO>{
        return messageService.getMessages(pageable);
    }



    //Getting a specific message
    @GetMapping("/{messageId}", "/{messageId}/")
    fun getMessageById(@PathVariable("messageId") messageId: Long) {
        //val message= messageService.getMessage(messageId)
        //return message
    }

    //change the state of a specific message
    @PostMapping("/{messageId}", "/{messageID}/")
    fun updateMessageState(){


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

