package it.polito.wa2.g07.crm.controllers

import it.polito.wa2.g07.crm.dtos.ContactDTO
import it.polito.wa2.g07.crm.dtos.CreateContactDTO
import it.polito.wa2.g07.crm.exceptions.MissingFieldException
import it.polito.wa2.g07.crm.services.ContactService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/API/contacts")
class ContactController(private val contactService: ContactService) {

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/","")
    fun saveContact (@RequestBody contact: CreateContactDTO ): ContactDTO{

        if (contact.name.isBlank() || contact.surname.isBlank()){
            throw MissingFieldException("Name and surname are required fields.")
        }
        return contactService.create(contact)
    }


}