package it.polito.wa2.g07.crm.controllers

import it.polito.wa2.g07.crm.dtos.*
import it.polito.wa2.g07.crm.entities.AddressType
import it.polito.wa2.g07.crm.entities.ContactCategory
import it.polito.wa2.g07.crm.exceptions.InvalidParamsException
import it.polito.wa2.g07.crm.services.ContactService
import jakarta.validation.Valid
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
    fun saveContact (@Valid @RequestBody contact: CreateContactDTO): ContactDTO {
        return contactService.create(contact)
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/{contactId}/email")
    fun addEmail (
        @PathVariable("contactId") contactId : Long,
        @Valid @RequestBody emailDTO: EmailDTO
    ): ContactDTO {
        return contactService.insertAddress(contactId, emailDTO)
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/{contactId}/telephone")
    fun addTelephone (
        @PathVariable("contactId") contactId : Long,
        @Valid @RequestBody telephoneDTO: TelephoneDTO
    ): ContactDTO {
        return contactService.insertAddress(contactId, telephoneDTO)
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/{contactId}/address")
    fun addDwelling (
        @PathVariable("contactId") contactId : Long,
        @Valid @RequestBody dwellingDTO: DwellingDTO
    ): ContactDTO {
        return contactService.insertAddress(contactId, dwellingDTO)
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

        if (filterBy != ContactFilterBy.NONE && query.isEmpty()) {
            throw InvalidParamsException("A query must be given when specifying a filter")
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
    fun deleteEmail (@PathVariable("contactId") contactId: Long, @PathVariable("emailId") emailId : Long) {
        return contactService.deleteAddress(contactId, emailId, AddressType.EMAIL)
    }

    @DeleteMapping("/{contactId}/telephone/{telephoneId}")
    fun deleteTelephone (@PathVariable("contactId") contactId: Long, @PathVariable("telephoneId") telephoneId: Long) {
        return contactService.deleteAddress(contactId, telephoneId, AddressType.TELEPHONE)
    }

    @DeleteMapping("/{contactId}/address/{dwellingId}")
    fun deleteDwelling (@PathVariable("contactId") contactId: Long, @PathVariable("dwellingId") dwellingId: Long) {
        return contactService.deleteAddress(contactId, dwellingId, AddressType.DWELLING)
    }

    @PutMapping("/{contactId}/email/{emailId}")
    fun updateEmail (
        @PathVariable("contactId") contactId: Long, @PathVariable("emailId") emailId : Long,
        @Valid @RequestBody emailDTO: EmailDTO
    ): ContactDTO{
       return contactService.updateAddress(contactId, emailId, emailDTO)
    }

    @PutMapping("/{contactId}/telephone/{telephoneId}")
    fun updateTelephone (
        @PathVariable("contactId") contactId: Long,
        @PathVariable("telephoneId") telephoneId : Long,
        @Valid @RequestBody telephoneDTO: TelephoneDTO
    ): ContactDTO{
        return  contactService.updateAddress(contactId, telephoneId, telephoneDTO)
    }

    @PutMapping("/{contactId}/dwelling/{dwellingId}")
    fun updateDwelling (
        @PathVariable("contactId") contactId: Long,
        @PathVariable("dwellingId") dwellingId: Long,
        @Valid @RequestBody dwellingDTO: DwellingDTO
    ): ContactDTO{
        return contactService.updateAddress(contactId, dwellingId, dwellingDTO)
    }

}