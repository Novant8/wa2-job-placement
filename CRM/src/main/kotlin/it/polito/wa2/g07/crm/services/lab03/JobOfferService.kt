package it.polito.wa2.g07.crm.services.lab03

import it.polito.wa2.g07.crm.dtos.lab03.JobOfferCreateDTO
import it.polito.wa2.g07.crm.dtos.lab03.JobOfferDTO

interface JobOfferService {

   fun createJobOffer(customerId:Long,job : JobOfferCreateDTO):JobOfferDTO
}