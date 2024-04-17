package it.polito.wa2.g07.crm.controllers

import it.polito.wa2.g07.crm.dtos.ContactDTO
import it.polito.wa2.g07.crm.dtos.CreateContactDTO
import it.polito.wa2.g07.crm.exceptions.MissingFieldException
import it.polito.wa2.g07.crm.dtos.ContactFilterBy
import it.polito.wa2.g07.crm.dtos.ReducedContactDTO
import it.polito.wa2.g07.crm.entities.Category
import it.polito.wa2.g07.crm.exceptions.InvalidParamsException
import it.polito.wa2.g07.crm.services.ContactService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

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

    @GetMapping("", "/")
    fun getContacts(
        pageable: Pageable,
        @RequestParam("filterBy") filterBy: ContactFilterBy = ContactFilterBy.NONE,
        @RequestParam("q") query: String = ""
    ): Page<ReducedContactDTO> {
        if (filterBy == ContactFilterBy.CATEGORY) {
            try {
                Category.valueOf(query)
            } catch (e: IllegalArgumentException) {
                throw InvalidParamsException("'${query}' is not a valid category")
            }
        }

        return contactService.getContacts(filterBy, query, pageable)
    }

}