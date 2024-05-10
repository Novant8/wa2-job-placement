package it.polito.wa2.g07.crm.controllers.lab03


import it.polito.wa2.g07.crm.dtos.lab02.ContactFilterDTO
import it.polito.wa2.g07.crm.dtos.lab03.CreateCustomerDTO
import it.polito.wa2.g07.crm.dtos.lab03.CustomerDTO
import it.polito.wa2.g07.crm.dtos.lab03.ReducedCustomerDTO
import it.polito.wa2.g07.crm.dtos.lab03.JobOfferCreateDTO
import it.polito.wa2.g07.crm.dtos.lab03.JobOfferDTO
import it.polito.wa2.g07.crm.services.lab02.ContactService
import it.polito.wa2.g07.crm.services.lab03.CustomerService

import org.springframework.data.domain.Page
import it.polito.wa2.g07.crm.services.lab03.JobOfferService
import jakarta.validation.Valid

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.data.domain.Pageable


@RestController
@RequestMapping("API/customers")
class CustomerController (  private val customerService: CustomerService,
                            private val contactService: ContactService,
                            private val jobOfferService: JobOfferService
                        ){
    /* The web application must allow the creation of a new customer or
  professional, search existing ones, update their properties, and add notes on
  them.*/

    @GetMapping("", "/")
    fun getCustomers(contactFilterDTO: ContactFilterDTO, pageable: Pageable): Page<ReducedCustomerDTO> {
        if(contactFilterDTO.isEmpty()) {
            return customerService.getCustomers(pageable)
        } else {
            val contactIds = contactService.getContacts(contactFilterDTO, pageable).map{ it.id }
            return customerService.getCustomersByContactIds(contactIds.toList(), pageable)
        }
    }

    @GetMapping("/{customerId}", "/{customerId}/")
    fun getCustomerById(@PathVariable("customerId") customerId: Long):CustomerDTO {
        return customerService.getCustomerById(customerId)
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("", "/")
    fun createCustomer(@RequestBody @Valid customer:CreateCustomerDTO):CustomerDTO {

       return customerService.createCustomer(customer)

    }

    @PutMapping("/{customerId}", "/{customerId}/")
    fun editCustomer(@PathVariable("customerId") customerId: Long, @RequestBody notes : Map<String, String?>): CustomerDTO {
        return customerService.postCustomerNotes(customerId,notes["notes"])
    }

    @PostMapping("/{customerId}/job-offers", "/{customerId}/job-offers/")
    fun createJobOffer( @PathVariable("customerId") customerId: Long,
                        @RequestBody @Valid jobDTO: JobOfferCreateDTO): JobOfferDTO {

        return jobOfferService.createJobOffer(customerId,jobDTO)

    }

}