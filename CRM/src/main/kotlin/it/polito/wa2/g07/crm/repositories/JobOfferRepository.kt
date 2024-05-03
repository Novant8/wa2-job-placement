package it.polito.wa2.g07.crm.repositories

import it.polito.wa2.g07.crm.entities.JobOffer
import org.springframework.stereotype.Repository
import org.springframework.data.jpa.repository.JpaRepository

@Repository
interface JobOfferRepository:JpaRepository<JobOffer,Long> {
}