package it.polito.wa2.g07.crm.repositories

import it.polito.wa2.g07.crm.entities.Professional
import org.springframework.stereotype.Repository
import org.springframework.data.jpa.repository.JpaRepository


@Repository
interface ProfessionalRepository:JpaRepository<Professional,Long> {
}