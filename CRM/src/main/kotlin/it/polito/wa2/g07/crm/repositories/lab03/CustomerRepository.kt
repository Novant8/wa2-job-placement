package it.polito.wa2.g07.crm.repositories.lab03

import it.polito.wa2.g07.crm.entities.lab02.Contact
import it.polito.wa2.g07.crm.entities.lab03.Customer
import it.polito.wa2.g07.crm.entities.lab03.Professional
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.Specification
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface CustomerRepository: JpaRepository<Customer, Long> {

    fun findByContactInfo(contact:Contact):Optional<Customer>

    @Query("SELECT c FROM Customer c WHERE c.contactInfo.contactId IN :contactIds")
    fun findByContactIds(@Param("contactIds") customerIds: Collection<Long>, pageable: Pageable): Page<Customer>

    @Query("SELECT c FROM Customer c WHERE c.contactInfo.userId = :userId")
    fun findByUserId(@Param("userId") userId: String): Optional<Customer>
}