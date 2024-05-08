package it.polito.wa2.g07.crm.services.lab03

import it.polito.wa2.g07.crm.dtos.lab03.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.transaction.annotation.Transactional

interface JobOfferService {

   fun createJobOffer(customerId:Long,job : JobOfferCreateDTO):JobOfferDTO
   fun searchJobOffer(filterDTO: JobOfferFilterDTO, pageable: Pageable): Page<JobOfferReducedDTO>
   fun searchJobOfferById(idOffer:Long): JobOfferDTO
   fun getJobOfferValue(idOffer:Long):Double?

}