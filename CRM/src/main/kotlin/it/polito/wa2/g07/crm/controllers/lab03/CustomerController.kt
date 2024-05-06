package it.polito.wa2.g07.crm.controllers.lab03


import it.polito.wa2.g07.crm.dtos.lab03.CreateCustomerDTO
import it.polito.wa2.g07.crm.dtos.lab03.CustomerDTO
import it.polito.wa2.g07.crm.dtos.lab03.JobOfferCreateDTO
import it.polito.wa2.g07.crm.dtos.lab03.JobOfferDTO
import it.polito.wa2.g07.crm.services.lab03.CustomerService
import it.polito.wa2.g07.crm.services.lab03.JobOfferService
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("API/customers")
class CustomerController (  private val customerService: CustomerService,
                            private val jobOfferService: JobOfferService
                        ){
    /* The web application must allow the creation of a new customer or
  professional, search existing ones, update their properties, and add notes on
  them.*/

    @GetMapping("", "/")
    fun getCustomers(pageable: Pageable) {
        TODO("Not yet implemented")
    }

    @GetMapping("/{customerId}", "/{customerId}/")
    fun getCustomerById(@PathVariable("customerId") customerId: Long) {
        TODO("Not yet implemented")
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("", "/")
    fun createCustomer(@RequestBody customer:CreateCustomerDTO):CustomerDTO {

       return customerService.createCustomer(customer)

    }

    @PutMapping("/{customerId}", "/{customerId}/")
    fun editCustomer(@PathVariable("customerId") customerId: Long) {
        TODO("Not yet implemented")
    }

    @PostMapping("/{customerId}/job-offers", "/{customerId}/job-offers/")
    fun createJobOffer( @PathVariable("customerId") customerId: Long,
                        @RequestBody jobDTO: JobOfferCreateDTO): JobOfferDTO {

        return jobOfferService.createJobOffer(customerId,jobDTO)

    }

}