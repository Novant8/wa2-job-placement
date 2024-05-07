package it.polito.wa2.g07.crm.services.lab03



import it.polito.wa2.g07.crm.dtos.lab03.*
import it.polito.wa2.g07.crm.entities.lab03.*
import it.polito.wa2.g07.crm.repositories.lab03.*
import it.polito.wa2.g07.crm.exceptions.EntityNotFoundException
import it.polito.wa2.g07.crm.repositories.lab03.CustomerRepository

import org.springframework.stereotype.Service

import kotlin.jvm.optionals.getOrElse

@Service
class JobOfferServiceImpl(
    private val jobOfferRepository: JobOfferRepository,
    private val customerRepository: CustomerRepository,
): JobOfferService {
    override fun createJobOffer(customerId: Long, job: JobOfferCreateDTO): JobOfferDTO {


        val customer = customerRepository.findById(customerId).getOrElse { throw EntityNotFoundException("The customer does not exist") }

        val jobOffer = JobOffer(
            customer = customer,
            requiredSkills = job.requiredSkills,
            duration= job.duration,
            notes = job.notes,
            status = OfferStatus.CREATED,
            description = job.description
            )
        return jobOfferRepository.save(jobOffer).toJobOfferDTO()
    }



}