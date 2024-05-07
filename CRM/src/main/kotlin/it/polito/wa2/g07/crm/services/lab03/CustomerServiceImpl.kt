package it.polito.wa2.g07.crm.services.lab03


import it.polito.wa2.g07.crm.dtos.lab03.*
import it.polito.wa2.g07.crm.entities.lab03.Customer
import it.polito.wa2.g07.crm.exceptions.ContactAssociationException
import it.polito.wa2.g07.crm.exceptions.EntityNotFoundException
import it.polito.wa2.g07.crm.repositories.lab03.CustomerRepository
import it.polito.wa2.g07.crm.exceptions.InvalidParamsException
import it.polito.wa2.g07.crm.repositories.lab02.ContactRepository
import jakarta.transaction.Transactional
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class CustomerServiceImpl(private val customerRepository: CustomerRepository,
                          private val contactRepository: ContactRepository):CustomerService {
    @Transactional
    override fun createCustomer(customer: CreateCustomerDTO): CustomerDTO {
        if (customer.contact.category?.uppercase()!= "CUSTOMER" ){
            throw InvalidParamsException("You must register a Customer user ")
        }

       return customerRepository.save(customer.toEntity()).toCustomerDto()

    }
@Transactional
    override fun bindContactToCustomer(contactId: Long, notes: String?): CustomerDTO {
        val contactOpt = contactRepository.findById(contactId)
        if (!contactOpt.isPresent){
            throw EntityNotFoundException("Contact with Id :$contactId is not found")
        }

        val contact = contactOpt.get()
        if (customerRepository.findByContactInfo(contact).isPresent){
            throw ContactAssociationException("Contact with id : $contactId is already associated to another Customer ")
        }else if(contact.category.name.uppercase() != "CUSTOMER"){
            throw InvalidParamsException("You must register a Customer user ")
        }
        val customer = Customer(contactInfo = contact, notes)

        return customerRepository.save(customer).toCustomerDto()
    }

    @Transactional
    override fun getCustomers(pageable: Pageable): Page<ReducedCustomerDTO> {
        return customerRepository.findAll(pageable).map { it.toReduceCustomerDTO() }
    }
    @Transactional
    override fun getCustomerById(customerId: Long): CustomerDTO {
      val customerOpt = customerRepository.findById(customerId)

        if (!customerOpt.isPresent){
            throw EntityNotFoundException("Customer with id : $customerId is not present ")
        }

        return customerOpt.get().toCustomerDto()
    }
}