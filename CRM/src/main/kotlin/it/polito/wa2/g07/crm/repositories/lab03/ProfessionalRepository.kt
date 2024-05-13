package it.polito.wa2.g07.crm.repositories.lab03

import it.polito.wa2.g07.crm.entities.lab02.Contact
import it.polito.wa2.g07.crm.entities.lab03.Professional
import org.springframework.stereotype.Repository
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional


@Repository
interface ProfessionalRepository:JpaRepository<Professional,Long> {

    fun findByContactInfo(contact : Contact): Optional<Professional>
}