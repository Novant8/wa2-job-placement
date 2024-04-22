package it.polito.wa2.g07.crm.controllers

import it.polito.wa2.g07.crm.dtos.*
import it.polito.wa2.g07.crm.exceptions.MissingFieldException
import it.polito.wa2.g07.crm.entities.ContactCategory
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
    fun saveContact (@RequestBody contact: CreateContactDTO ): ReducedContactDTO{
        if (contact.name.isNullOrBlank() || contact.surname.isNullOrBlank()){
            throw MissingFieldException("Name and surname are required fields.")
        }
        return contactService.create(contact)
    }

    @PostMapping("/{contactId}/email")
    fun addEmail (@PathVariable("contactId") contactId : Long, @RequestBody emailValue :Map<String, String> ){
        val email = emailValue["email"]
        if (!email.isNullOrBlank()) {
            contactService.insertEmail(contactId, email)
        } else {
            throw MissingFieldException("You should provide an email ")
        }
    }

    @GetMapping("/{contactId}")
    fun getContactById (@PathVariable("contactId") contactId: Long): ContactDTO{
        return contactService.getContactById(contactId)
    }

    @GetMapping("", "/")
    fun getContacts(
        pageable: Pageable,
        @RequestParam("filterBy") filterByStr: String = "NONE",
        @RequestParam("q") query: String = ""
    ): Page<ReducedContactDTO> {
        val filterBy = try {
            ContactFilterBy.valueOf(filterByStr.uppercase())
        } catch (e: IllegalArgumentException) {
            throw InvalidParamsException("'$filterByStr' is not a valid filter. Possible filters: ${ContactFilterBy.entries}.")
        }

        if (filterBy == ContactFilterBy.CATEGORY) {
            try {
                ContactCategory.valueOf(query.uppercase())
            } catch (e: IllegalArgumentException) {
                throw InvalidParamsException("'$query' is not a valid category. Possible categories: ${ContactCategory.entries}.")
            }
        }

        return contactService.getContacts(filterBy, query, pageable)
    }

    @DeleteMapping("/{contactId}/email/{emailId}")
    fun deleteEmail (@PathVariable("contactId") contactId: Long, @PathVariable("emailId") emailId : Long){
        return contactService.deleteEmail(contactId,emailId)
    }

    @PutMapping("/{contactId}/email/{emailId}")
    fun updateEmail (@PathVariable("contactId") contactId: Long, @PathVariable("emailId") emailId : Long, @RequestBody emailValue :Map<String, String>): ContactDTO{

        val email = emailValue["email"]
        if (!email.isNullOrBlank()) {
           return  contactService.updateEmail(contactId, emailId, email)
        } else {
            throw MissingFieldException("You should provide an email ")
        }
    }

    @PutMapping("/{contactId}/telephone/{telephoneId}")
    fun updateTelephone (@PathVariable("contactId") contactId: Long, @PathVariable("telephoneId") telephoneId : Long, @RequestBody phoneNumber :Map<String, String>): ContactDTO{

        val number = phoneNumber["phoneNumber"]
        if (!number.isNullOrBlank()) {
            return  contactService.updateTelephone(contactId, telephoneId, number)
        } else {
            throw MissingFieldException("You should provide a phone number ")
        }
    }

}