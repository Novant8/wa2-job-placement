package it.polito.wa2.g07.crm.controllers.lab03

import org.springframework.web.bind.annotation.*
import java.awt.print.Pageable

@RestController
@RequestMapping("API/customers")
class CustomerController {
    /* The web application must allow the creation of a new customer or
  professional, search existing ones, update their properties, and add notes on
  them.*/

    @GetMapping("", "/")
    fun getCustomers(pageable: Pageable) {
        TODO("Not yet implemented")
    }

    @GetMapping("/{customerId}", "/{customerId}")
    fun getCustomerById(@PathVariable("customerId") customerId: Long) {
        TODO("Not yet implemented")
    }

    @PostMapping("", "/")
    fun createCustomer() {
        TODO("Not yet implemented")
    }

    @PutMapping("/{customerId}", "/{customerId}/")
    fun editCustomer(@PathVariable("customerId") customerId: Long) {
        TODO("Not yet implemented")
    }

    @PostMapping("/{customerId}/job-offers", "/{customerId}/job-offers/")
    fun createJobOffer(@PathVariable("customerId") customerId: Long) {
        TODO("Not yet implemented")
    }

}