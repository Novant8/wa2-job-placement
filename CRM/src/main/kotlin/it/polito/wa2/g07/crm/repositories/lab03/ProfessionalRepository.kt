package it.polito.wa2.g07.crm.repositories.lab03

import it.polito.wa2.g07.crm.entities.lab02.Contact
import it.polito.wa2.g07.crm.entities.lab03.Customer
import it.polito.wa2.g07.crm.entities.lab03.Professional
import org.springframework.stereotype.Repository
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import java.util.*


@Repository
interface ProfessionalRepository:JpaRepository<Professional,Long>, JpaSpecificationExecutor<Professional> {

    fun findByContactInfo(contact: Contact): Optional<Professional>
}