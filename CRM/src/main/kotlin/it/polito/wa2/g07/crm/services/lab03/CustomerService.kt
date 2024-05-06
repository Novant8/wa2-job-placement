package it.polito.wa2.g07.crm.services.lab03

import it.polito.wa2.g07.crm.dtos.lab03.CreateCustomerDTO
import it.polito.wa2.g07.crm.dtos.lab03.CustomerDTO

interface CustomerService {

    fun createCustomer (customer : CreateCustomerDTO):CustomerDTO

    fun bindContactToCustomer(contactId: Long, notes : String? ) : CustomerDTO
}