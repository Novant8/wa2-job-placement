package it.polito.wa2.g07.crm.controllers

import it.polito.wa2.g07.crm.dtos.ContactFilterBy
import it.polito.wa2.g07.crm.dtos.ReducedContactDTO
import it.polito.wa2.g07.crm.entities.Category
import it.polito.wa2.g07.crm.exceptions.InvalidParamsException
import it.polito.wa2.g07.crm.services.ContactService
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("API/contacts")
class ContactController(private val contactService: ContactService) {

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