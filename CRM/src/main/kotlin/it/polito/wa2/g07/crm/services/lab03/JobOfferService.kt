package it.polito.wa2.g07.crm.services.lab03

import it.polito.wa2.g07.crm.dtos.lab03.JobOfferCreateDTO
import it.polito.wa2.g07.crm.dtos.lab03.JobOfferDTO
import it.polito.wa2.g07.crm.dtos.lab03.JobOfferUpdateStatusDTO
import it.polito.wa2.g07.crm.dtos.lab03.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface JobOfferService {

   fun createJobOffer(customerId:Long,job : JobOfferCreateDTO):JobOfferDTO
   fun searchJobOffer(filterDTO: JobOfferFilterDTO, pageable: Pageable): Page<JobOfferReducedDTO>
   fun searchJobOfferById(idOffer:Long): JobOfferDTO
   fun getJobOfferValue(idOffer:Long):Double?

   fun updateJobOfferStatus(jobOfferId: Long, jobOfferUpdateStatusDTO: JobOfferUpdateStatusDTO): JobOfferDTO
   fun updateJobOffer(jobOfferId: Long, jobOfferUpdateDTO: JobOfferUpdateDTO): JobOfferDTO

   fun addCandidate (jobOfferId: Long, professionalId: Long): JobOfferDTO
   fun removeCandidate (jobOfferId: Long, professionalId: Long): Long
   fun addRefusedCandidate (jobOfferId: Long, professionalId: Long): JobOfferDTO
}