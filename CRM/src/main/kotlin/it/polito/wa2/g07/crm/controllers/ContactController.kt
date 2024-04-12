package it.polito.wa2.g07.crm.controllers

import it.polito.wa2.g07.crm.services.ContactService
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("API/contacts")
class ContactController(private val contactService: ContactService) {


}