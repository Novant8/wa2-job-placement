package it.polito.wa2.g07.crm.controllers.lab02

import it.polito.wa2.g07.crm.dtos.lab02.*

import it.polito.wa2.g07.crm.dtos.lab03.CustomerDTO
import it.polito.wa2.g07.crm.entities.lab02.AddressType
import it.polito.wa2.g07.crm.services.lab02.ContactService
import it.polito.wa2.g07.crm.services.lab03.CustomerService
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/API/contacts")
class ContactController(private val contactService: ContactService, private val customerService: CustomerService) {

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/","")
    fun saveContact (@Valid @RequestBody contact: CreateContactDTO): ContactDTO {
        return contactService.create(contact)
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/{contactId}/customers",)
    fun saveCustomer ( @PathVariable("contactId") contactId : Long, @RequestBody notes :Map<String, String>): CustomerDTO {

        return customerService.bindContactToCustomer(contactId,notes["notes"])

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
    fun getContactById (@PathVariable("contactId") contactId: Long): ContactDTO {
        return contactService.getContactById(contactId)
    }

    @GetMapping("", "/")
    fun getContacts(
        filterDTO: ContactFilterDTO,
        pageable: Pageable
    ): Page<ReducedContactDTO> {
        return contactService.getContacts(filterDTO, pageable)
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{contactId}/email/{emailId}")
    fun deleteEmail (@PathVariable("contactId") contactId: Long, @PathVariable("emailId") emailId : Long) {
        return contactService.deleteAddress(contactId, emailId, AddressType.EMAIL)
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{contactId}/telephone/{telephoneId}")
    fun deleteTelephone (@PathVariable("contactId") contactId: Long, @PathVariable("telephoneId") telephoneId: Long) {
        return contactService.deleteAddress(contactId, telephoneId, AddressType.TELEPHONE)
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{contactId}/address/{dwellingId}")
    fun deleteDwelling (@PathVariable("contactId") contactId: Long, @PathVariable("dwellingId") dwellingId: Long) {
        return contactService.deleteAddress(contactId, dwellingId, AddressType.DWELLING)
    }

    @PutMapping("/{contactId}/email/{emailId}")
    fun updateEmail (
        @PathVariable("contactId") contactId: Long, @PathVariable("emailId") emailId : Long,
        @Valid @RequestBody emailDTO: EmailDTO
    ): ContactDTO {
       return contactService.updateAddress(contactId, emailId, emailDTO)
    }

    @PutMapping("/{contactId}/telephone/{telephoneId}")
    fun updateTelephone (
        @PathVariable("contactId") contactId: Long,
        @PathVariable("telephoneId") telephoneId : Long,
        @Valid @RequestBody telephoneDTO: TelephoneDTO
    ): ContactDTO {
        return  contactService.updateAddress(contactId, telephoneId, telephoneDTO)
    }

    @PutMapping("/{contactId}/address/{dwellingId}")
    fun updateDwelling (
        @PathVariable("contactId") contactId: Long,
        @PathVariable("dwellingId") dwellingId: Long,
        @Valid @RequestBody dwellingDTO: DwellingDTO
    ): ContactDTO {
        return contactService.updateAddress(contactId, dwellingId, dwellingDTO)
    }



}