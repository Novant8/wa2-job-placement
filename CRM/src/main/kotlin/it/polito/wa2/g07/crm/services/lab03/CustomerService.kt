package it.polito.wa2.g07.crm.services.lab03

import it.polito.wa2.g07.crm.dtos.lab03.CreateCustomerDTO
import it.polito.wa2.g07.crm.dtos.lab03.CustomerDTO
import it.polito.wa2.g07.crm.dtos.lab03.ReducedCustomerDTO
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface CustomerService {

    fun createCustomer (customer : CreateCustomerDTO):CustomerDTO

    fun bindContactToCustomer(contactId: Long, notes : String? ) : CustomerDTO

    fun getCustomers(pageable: Pageable):Page<ReducedCustomerDTO>

    fun getCustomerById(customerId:Long): CustomerDTO
}