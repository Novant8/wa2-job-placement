package it.polito.wa2.g07.crm.controllers.lab03


import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import it.polito.wa2.g07.crm.dtos.lab02.*
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
import org.springdoc.core.annotations.ParameterObject

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.data.domain.Pageable
import org.springframework.http.ProblemDetail

@Tag(name = "Customers", description = "Create, search and update customers")
@RestController
@RequestMapping("API/customers")
class CustomerController (  private val customerService: CustomerService,
                            private val contactService: ContactService,
                            private val jobOfferService: JobOfferService
                        ){
    @Operation(summary = "Retrieve all customers that match the given filters on their contact information, with paging and sorting")
    @ApiResponses(value=[
        ApiResponse(responseCode = "200"),
        ApiResponse(
            responseCode = "400",
            description = "Something in the given filter is invalid",
            content = [ Content(mediaType = "application/problem+json", schema = Schema(implementation = ProblemDetail::class)) ]
        )
    ])
    @GetMapping("", "/")
    fun getCustomers(
        contactFilterDTO: ContactFilterDTO,
        @ParameterObject pageable: Pageable
    ): Page<ReducedCustomerDTO> {
        if(contactFilterDTO.isEmpty()) {
            return customerService.getCustomers(pageable)
        } else {
            val contactIds = contactService.getContacts(contactFilterDTO, pageable).map{ it.id }
            return customerService.getCustomersByContactIds(contactIds.toList(), pageable)
        }
    }

    @Operation(summary = "Retrieve a specific customer's information")
    @ApiResponses(value=[
        ApiResponse(responseCode = "200"),
        ApiResponse(
            responseCode = "404",
            description = "The customer was not found",
            content = [ Content(mediaType = "application/problem+json", schema = Schema(implementation = ProblemDetail::class)) ]
        )
    ])
    @GetMapping("/{customerId}", "/{customerId}/")
    fun getCustomerById(@PathVariable("customerId") customerId: Long):CustomerDTO {
        return customerService.getCustomerById(customerId)
    }

    @Operation(summary = "Create a new customer")
    @ApiResponses(value=[
        ApiResponse(
            responseCode = "201",
            description = "The customer was successfully created"
        ),
        ApiResponse(
            responseCode = "400",
            description = "Incompatible customer data was supplied.",
            content = [ Content(mediaType = "application/problem+json", schema = Schema(implementation = ProblemDetail::class)) ]
        ),
        ApiResponse(
            responseCode = "422",
            description = "Invalid customer data was supplied. Additional property `fieldErrors` shows for each wrong field its reason.",
            content = [ Content(mediaType = "application/problem+json", schema = Schema(implementation = ProblemDetail::class)) ]
        )
    ])
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("", "/")
    fun createCustomer(@RequestBody @Valid customer:CreateCustomerDTO):CustomerDTO {
       return customerService.createCustomer(customer)
    }

    @Operation(summary = "Add or edit notes of a given customer")
    @ApiResponses(value=[
        ApiResponse(responseCode = "200"),
        ApiResponse(
            responseCode = "404",
            description = "The customer was not found",
            content = [ Content(mediaType = "application/problem+json", schema = Schema(implementation = ProblemDetail::class)) ]
        )
    ])
    @PutMapping("/{customerId}/notes", "/{customerId}/notes/")
    fun editCustomerNotes(
        @PathVariable("customerId") customerId: Long,
        @RequestBody notesDTO: NotesDTO
    ): CustomerDTO {
        return customerService.postCustomerNotes(customerId,notesDTO.notes)
    }

    @Operation(summary = "Edit the e-mail address of a given customer")
    @ApiResponses(value=[
        ApiResponse(responseCode = "200"),
        ApiResponse(
            responseCode = "404",
            description = "The customer or e-mail address was not found",
            content = [ Content(mediaType = "application/problem+json", schema = Schema(implementation = ProblemDetail::class)) ]
        ),
        ApiResponse(
            responseCode = "422",
            description = "Invalid customer data was supplied. Additional property `fieldErrors` shows for each wrong field its reason.",
            content = [ Content(mediaType = "application/problem+json", schema = Schema(implementation = ProblemDetail::class)) ]
        )
    ])
    @PutMapping("/{customerId}/email/{emailId}", "/{customerId}/email/{emailId}/")
    fun editCustomerEmail(@PathVariable("customerId") customerId: Long, @PathVariable("emailId") emailId : Long,
                          @Valid @RequestBody emailDTO: EmailDTO
    ): CustomerDTO {
        val customer = customerService.getCustomerById(customerId)
        val contactId = customer.contactInfo.id
        val contactDTO=  contactService.updateAddress(contactId,emailId,emailDTO)

        return CustomerDTO(customer.id, contactDTO, customer.notes)
    }

    @Operation(summary = "Edit the phone number of a given customer")
    @ApiResponses(value=[
        ApiResponse(responseCode = "200"),
        ApiResponse(
            responseCode = "404",
            description = "The customer or phone number was not found",
            content = [ Content(mediaType = "application/problem+json", schema = Schema(implementation = ProblemDetail::class)) ]
        ),
        ApiResponse(
            responseCode = "422",
            description = "Invalid customer data was supplied. Additional property `fieldErrors` shows for each wrong field its reason.",
            content = [ Content(mediaType = "application/problem+json", schema = Schema(implementation = ProblemDetail::class)) ]
        )
    ])
    @PutMapping("/{customerId}/telephone/{telephoneId}", "/{customerId}/telephone/{telephoneId}/")
    fun editCustomerTelephone(@PathVariable("customerId") customerId: Long, @PathVariable("telephoneId") telephoneId : Long,
                          @Valid @RequestBody telephoneDTO: TelephoneDTO
    ): CustomerDTO{
        val customer = customerService.getCustomerById(customerId)
        val contactId = customer.contactInfo.id
        val contactDTO=  contactService.updateAddress(contactId,telephoneId,telephoneDTO)

        return CustomerDTO(customer.id, contactDTO, customer.notes)
    }

    @Operation(summary = "Edit the home/dwelling address of a given customer")
    @ApiResponses(value=[
        ApiResponse(responseCode = "200"),
        ApiResponse(
            responseCode = "404",
            description = "The customer or address was not found",
            content = [ Content(mediaType = "application/problem+json", schema = Schema(implementation = ProblemDetail::class)) ]
        ),
        ApiResponse(
            responseCode = "422",
            description = "Invalid customer data was supplied. Additional property `fieldErrors` shows for each wrong field its reason.",
            content = [ Content(mediaType = "application/problem+json", schema = Schema(implementation = ProblemDetail::class)) ]
        )
    ])
    @PutMapping("/{customerId}/address/{dwellingId}", "/{customerId}/address/{dwellingId}/")
    fun editCustomerDwelling(@PathVariable("customerId") customerId: Long, @PathVariable("dwellingId") dwellingId : Long,
                              @Valid @RequestBody dwellingDTO: DwellingDTO
    ): CustomerDTO{
        val customer = customerService.getCustomerById(customerId)
        val contactId = customer.contactInfo.id
        val contactDTO=  contactService.updateAddress(contactId,dwellingId,dwellingDTO)

        return CustomerDTO(customer.id, contactDTO, customer.notes)
    }

    @Operation(summary = "Create a new job offer from the given customer")
    @ApiResponses(value=[
        ApiResponse(
            responseCode = "201",
            description = "The job offer was successfully created"
        ),
        ApiResponse(
            responseCode = "404",
            description = "The customer was not found",
            content = [ Content(mediaType = "application/problem+json", schema = Schema(implementation = ProblemDetail::class)) ]
        ),
        ApiResponse(
            responseCode = "422",
            description = "Invalid customer data was supplied. Additional property `fieldErrors` shows for each wrong field its reason.",
            content = [ Content(mediaType = "application/problem+json", schema = Schema(implementation = ProblemDetail::class)) ]
        )
    ])
    @PostMapping("/{customerId}/job-offers", "/{customerId}/job-offers/")
    fun createJobOffer( @PathVariable("customerId") customerId: Long,
                        @RequestBody @Valid jobDTO: JobOfferCreateDTO): JobOfferDTO {
        return jobOfferService.createJobOffer(customerId,jobDTO)
    }

}